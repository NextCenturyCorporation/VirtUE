/**
 * 
 */
package com.nextcentury.savior.cifsproxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

import org.ietf.jgss.GSSException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;

import com.nextcentury.savior.cifsproxy.GssApi.GssCredentialUsage;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_set_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_element;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_set;
import com.nextcentury.savior.cifsproxy.GssApi.gss_name_t;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Wraps a real {@link AuthenticationManager}. Takes the user credentials passed
 * to us and creates a Kerberos service ticket and caches it, so it can be used
 * later to mount the filesystem with cifs.mount(8).
 * 
 * @author clong
 *
 */
public class DelegatingAuthenticationManager implements AuthenticationManager {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(DelegatingAuthenticationManager.class);

	/**
	 * The real {@link AuthenticationManager} to which we delegate.
	 */
	private final AuthenticationManager amDelegate;
	/**
	 * Where the service ticket will be stored.
	 */
	private final Path cacheFile;
	/**
	 * The instance of the {@link GssApi} to use.
	 */
	private GssApi gssapi = GssApi.INSTANCE;

	/**
	 * The name of the local host (e.g., "webserver").
	 */
	private String myhostname;

	public DelegatingAuthenticationManager(AuthenticationManager amDelegate, Path cacheFile) {
		LOGGER.entry(amDelegate, cacheFile);
		this.amDelegate = amDelegate;
		this.cacheFile = cacheFile;
		initHostname();
		// maybe disable this for production
		Native.setProtected(true);
		LOGGER.exit();
	}

	/**
	 * Determine the local hostname and initialize it.
	 */
	private void initHostname() {
		LOGGER.entry();
		UnknownHostException exception = null;
		try {
			myhostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			exception = e;
		}
		if (exception != null) {
			// try another strategy
			if (System.getProperty("os.name").contains("windows")) {
				myhostname = System.getenv("COMPUTERNAME");
			} else {
				myhostname = System.getenv("HOSTNAME");
			}
		}
		if (myhostname == null || myhostname.isEmpty()) {
			UndeclaredThrowableException undeclaredThrowableException = new UndeclaredThrowableException(exception);
			LOGGER.throwing(undeclaredThrowableException);
			throw undeclaredThrowableException;
		}
		LOGGER.exit();
	}

	/**
	 * Delegates to the {@link AuthenticationManager} passed to
	 * {@link #DelegatingAuthenticationManager(AuthenticationManager, String)}.
	 * 
	 * @throws AuthenticationServiceException
	 *                                            if <code>authentication</code> is
	 *                                            not a
	 *                                            {@link KerberosServiceRequestToken}
	 * @see org.springframework.security.authentication.AuthenticationManager#authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		LOGGER.entry(authentication);
		if (!(authentication instanceof KerberosServiceRequestToken)) {
			throw new AuthenticationServiceException(
					"non-Kerberos authentication methods are not supported (token is of type "
							+ authentication.getClass().getName() + ")");
		}
		KerberosServiceRequestToken kerberosToken = (KerberosServiceRequestToken) authentication;
		byte[] serviceTicket = kerberosToken.getToken();
		LOGGER.debug("serviceTicket: " + new String(serviceTicket));
		gss_cred_id_t acceptorCredential = null;
		gss_cred_id_t proxyCred = null;
		try {
			acceptorCredential = createAcceptorCred();
			proxyCred = getProxyCredential(serviceTicket, acceptorCredential);
			storeCredInto(proxyCred, cacheFile, 1);
		} catch (GSSException e) {
			LOGGER.debug("got exception: " + e);
			AuthenticationException exception = new AuthenticationCredentialsNotFoundException(
					"error obtaining credentials for '" + authentication.getName() + "'");
			exception.initCause(e);
			LOGGER.throwing(exception);
			throw exception;
		} finally {
			if (acceptorCredential != null) {
				IntByReference minorStatus = new IntByReference();
				int retval = gssapi.gss_release_cred(minorStatus, acceptorCredential);
				if (retval != 0) {
					LOGGER.warn("error while freeing acceptor credential: " + retval + "." + minorStatus.getValue());
				}
			}
			if (proxyCred != null) {
				IntByReference minorStatus = new IntByReference();
				int retval = gssapi.gss_release_cred(minorStatus, proxyCred);
				if (retval != 0) {
					LOGGER.warn("error while freeing proxy credential: " + retval + "." + minorStatus.getValue());
				}
			}
		}
		Authentication newAuth = amDelegate.authenticate(authentication);
		LOGGER.exit(newAuth);
		return newAuth;
	}

	/**
	 * Create a GSS credential that can be used to accept or initiate connections.
	 * 
	 * @return GSS accept/initiate credential
	 * @throws GSSException
	 *                          if there was an error with a GSS call (e.g.,
	 *                          {@link GssApi#gss_acquire_cred(IntByReference, gss_name_t, int, gss_OID_set_desc, int, PointerByReference, PointerByReference, IntByReference)})
	 */
	private gss_cred_id_t createAcceptorCred() throws GSSException {
		LOGGER.entry();
		IntByReference minorStatus = new IntByReference();
		int retval;

		gss_OID_desc nameType = GssApi.GSS_C_NT_HOSTBASED_SERVICE;
		gss_name_t gssWebServiceName = GssUtils.importName(gssapi, "http@" + myhostname, nameType);
		gss_OID_set_desc desiredMechs = new gss_OID_set_desc();
		desiredMechs.count = new NativeLong(1);
		desiredMechs.elements = new gss_OID_desc.ByReference(GssApi.MECH_KRB5.length, GssApi.MECH_KRB5.elements);
		PointerByReference outputCredHandle = new PointerByReference();
		LOGGER.trace(">>>about to call acquire_cred");
		// retval = gssapi.gss_acquire_cred(minorStatus, gssWebServiceName, 0,
		// desiredMechs,
		retval = gssapi.gss_acquire_cred(minorStatus, gssWebServiceName, 0, GssApi.GSS_C_NO_OID_SET,
				GssCredentialUsage.GSS_C_BOTH.getValue(), outputCredHandle, null, null);
		LOGGER.trace("<<<back from acquire_cred");
		GSSException exception = null;
		try {
			GssUtils.releaseName(gssapi, gssWebServiceName);
		} catch (GSSException e) {
			exception = e;
		}
		if (retval != 0) {
			/*
			 * this exception is more important than one from release_name (if any), so
			 * clobber it
			 */
			if (exception != null) {
				LOGGER.warn("ignoring exception from GssUtils.releaseName: " + exception);
			}
			exception = new GSSException(retval, minorStatus.getValue(), "acquiring credential (for S4U2Proxy)");
			LOGGER.warn("error from gss_acquire_cred: " + retval + "." + minorStatus.getValue());
		}
		if (exception != null) {
			LOGGER.throwing(exception);
			throw exception;
		}
		gss_cred_id_t outputCred = new gss_cred_id_t(outputCredHandle.getValue());
		LOGGER.exit(outputCred);
		return outputCred;
	}

	/**
	 * Get a GSS credential that can be used as a proxy for delegating
	 * authentication.
	 * 
	 * @param serviceToken
	 *                               the raw token sent to the server (e.g., created
	 *                               by
	 *                               {@link GssApi#gss_init_sec_context(IntByReference, gss_cred_id_t, PointerByReference, Pointer, gss_OID_desc, int, int, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, gss_buffer_desc, PointerByReference, gss_buffer_desc, IntByReference, IntByReference)})
	 * @param acceptorCredential
	 *                               our service credential (from
	 *                               {@link #createAcceptorCred()})
	 * @return a GSS proxy credential
	 * @throws GSSException
	 *                          * if there was an error with a GSS call (e.g.,
	 *                          {@link GssApi#gss_accept_sec_context(IntByReference, PointerByReference, gss_cred_id_t, gss_buffer_desc, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, PointerByReference, PointerByReference, gss_buffer_desc, IntByReference, IntByReference, PointerByReference)})
	 */
	private gss_cred_id_t getProxyCredential(byte[] serviceToken, gss_cred_id_t acceptorCredential)
			throws GSSException {
		LOGGER.entry(serviceToken, acceptorCredential);
		GssApi gssapi = GssApi.INSTANCE;
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc outputToken = new gss_buffer_desc();

		IntByReference retFlags = new IntByReference(0);
		PointerByReference contextHandle = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		gss_buffer_desc inputToken = new gss_buffer_desc(serviceToken);
		PointerByReference delegatedCredHandle = new PointerByReference();
		LOGGER.trace(">>>about to call accept_sec_context");
		PointerByReference sourceName = new PointerByReference(GssApi.GSS_C_NO_NAME);
		int retval = gssapi.gss_accept_sec_context(minorStatus, contextHandle, acceptorCredential, inputToken,
				GssApi.GSS_C_NO_CHANNEL_BINDINGS, sourceName, null, outputToken, retFlags, null, delegatedCredHandle);
		gss_name_t plainSourceName = new gss_name_t(sourceName.getValue());
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("<<back from accept_sec_context: " + retval + "." + minorStatus.getValue() + "\tname='"
					+ GssUtils.getStringName(gssapi, plainSourceName) + "'");
		}
		/*
		 * In theory you have to do this in a loop. In practice that doesn't seem to
		 * ever happen. But just in case, check and warn.
		 */
		if (outputToken.length.longValue() != 0) {
			LOGGER.warn("unsupported protocol (only single init/accept context packet is supported)");
		}
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"accepting context (" + retval + "." + minorStatus.getValue() + ")");
			LOGGER.throwing(exception);
			throw exception;
		}

		return new gss_cred_id_t(delegatedCredHandle.getValue());
	}

	/**
	 * Take a credential and store it in the specified file.
	 * 
	 * @param acquiredCred
	 *                          the credential to store
	 * @param file
	 *                          the path to the file to store the credential in
	 * @param overwriteCred
	 *                          whether to overwrite an existing file or not (0 is
	 *                          <code>false</code>, 1 is <code>true</code>)
	 * @throws GSSException
	 *                          if there was an error calling
	 *                          {@link GssApi#gss_store_cred_into(IntByReference, Pointer, int, gss_OID_desc, int, int, gss_key_value_set, PointerByReference, IntByReference)}
	 */
	private void storeCredInto(Pointer acquiredCred, Path file, int overwriteCred) throws GSSException {
		IntByReference minorStatus = new IntByReference();
		int defaultCred = 0;
		gss_key_value_element.ByReference credElement = new gss_key_value_element.ByReference("ccache", "FILE:" + file);
		gss_key_value_set credStore = new gss_key_value_set();
		credStore.count = 1;
		credStore.elements = credElement;
		Pointer oidsStored = new Pointer(0);
		PointerByReference oidsStoredHandle = new PointerByReference(oidsStored);
		IntByReference credStored = new IntByReference();

		LOGGER.trace(">>>about to call store_cred_into");
		int retval = gssapi.gss_store_cred_into(minorStatus, acquiredCred, GssCredentialUsage.GSS_C_INITIATE.getValue(),
				GssApi.GSS_C_NO_OID, overwriteCred, defaultCred, credStore, oidsStoredHandle, credStored);
		LOGGER.trace("<<<back from store_cred_into:" + retval + "." + minorStatus.getValue());
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(),
					"storing credential: " + retval + "." + minorStatus.getValue());
		}
	}

}
