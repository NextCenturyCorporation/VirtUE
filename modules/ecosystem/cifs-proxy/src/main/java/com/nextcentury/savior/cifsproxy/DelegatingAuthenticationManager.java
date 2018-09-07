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
 * Cache the user credentials passed to us.
 * 
 * @author clong
 *
 */
public class DelegatingAuthenticationManager implements AuthenticationManager {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(DelegatingAuthenticationManager.class);

	private AuthenticationManager amDelegate;
	private Path cacheFile;
	private String serviceName;

	private GssApi gssapi = GssApi.INSTANCE;

	private String myhostname;

	public DelegatingAuthenticationManager(AuthenticationManager amDelegate, String serviceName, Path cacheFile) {
		LOGGER.entry(amDelegate, serviceName, cacheFile);
		this.amDelegate = amDelegate;
		this.serviceName = serviceName;
		this.cacheFile = cacheFile;
		initHostname();
		// maybe disable this for production
		Native.setProtected(true);
		LOGGER.exit();
	}

	private void initHostname() {
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
			throw new UndeclaredThrowableException(exception);
		}
	}

	/**
	 * Delegates to the {@link AuthenticationManager} passed to
	 * {@link #CachingAuthenticationManager(AuthenticationManager, String)}.
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
		gss_cred_id_t targetCred = null;
		try {
			acceptorCredential = createAcceptorCred();
			//System.out.println("acceptorCred: " + GssUtils.getCredInfo(gssapi, acceptorCredential));
			proxyCred = getProxyCredential(serviceTicket, acceptorCredential);
			//System.out.println("proxyCred: " + GssUtils.getCredInfo(gssapi, proxyCred));
			//targetCred = getTargetCredential(proxyCred);
			storeCredInto(proxyCred, cacheFile);
		} catch (GSSException e) {
			LOGGER.debug("got exception: " + e);
			AuthenticationException exception = new AuthenticationCredentialsNotFoundException(
					"error obtaining credentials for '" + authentication.getName() + "'");
			exception.initCause(e);
			LOGGER.throwing(exception);
			throw exception;
		} finally {
			if (acceptorCredential != null) {
				// TODO
			}
			// TODO clean up other creds
		}
		Authentication newAuth = amDelegate.authenticate(authentication);
		LOGGER.exit(newAuth);
		return newAuth;
	}

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
		System.out.println(">>>about to call acquire_cred");
		// retval = gssapi.gss_acquire_cred(minorStatus, gssWebServiceName, 0,
		// desiredMechs,
		retval = gssapi.gss_acquire_cred(minorStatus, gssWebServiceName, 0, GssApi.GSS_C_NO_OID_SET,
				GssCredentialUsage.GSS_C_BOTH.getValue(), outputCredHandle, null, null);
		System.out.println("<<<back from acquire_cred");
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

	private gss_cred_id_t getProxyCredential(byte[] serviceTicket, gss_cred_id_t acceptorCredential)
			throws GSSException {
		LOGGER.entry(serviceTicket, acceptorCredential);
		GssApi gssapi = GssApi.INSTANCE;
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc outputToken = new gss_buffer_desc();

		IntByReference retFlags = new IntByReference(0);
		PointerByReference contextHandle = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		gss_buffer_desc inputToken = new gss_buffer_desc(serviceTicket);
		PointerByReference delegatedCredHandle = new PointerByReference();
		System.out.println(">>>about to call accept_sec_context");
		PointerByReference sourceName = new PointerByReference(GssApi.GSS_C_NO_NAME);
		int retval = gssapi.gss_accept_sec_context(minorStatus, contextHandle, acceptorCredential, inputToken,
				GssApi.GSS_C_NO_CHANNEL_BINDINGS, sourceName, null, outputToken, retFlags, null, delegatedCredHandle);
		gss_name_t plainSourceName = new gss_name_t(sourceName.getValue());
		System.out.println("<<back from accept_sec_context: " + retval + "." + minorStatus.getValue() + "\tname='"
				+ GssUtils.getStringName(gssapi, plainSourceName) + "'");
		/*
		 * In theory you have to do this in a loop. In practice that doesn't seem to
		 * happen. But just in case, check and warn.
		 */
		if (outputToken.length.longValue() != 0) {
			// GSSException exception = new GSSException(retval, minorStatus.getValue(),
			// "unsupported protocol (only single init/accept context packet is
			// supported)");
			// gssapi.gss_release_buffer(minorStatus, outputToken);
			// LOGGER.throwing(exception);
			// throw exception;
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

	private gss_cred_id_t getTargetCredential(gss_cred_id_t proxyCred) throws GSSException {
		LOGGER.entry(proxyCred);
		GssApi gssapi = GssApi.INSTANCE;
		int retval;
		IntByReference minorStatus = new IntByReference();
		gss_name_t gssServiceName = GssUtils.importName(gssapi, serviceName, GssApi.GSS_C_NT_HOSTBASED_SERVICE);

		PointerByReference contextHandle = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		gss_buffer_desc outputToken = new gss_buffer_desc();
		// flags from t_s4u2proxy_krb5.c
		int flags = GssApi.GssContextFlag.GSS_C_REPLAY_FLAG.getValue()
				| GssApi.GssContextFlag.GSS_C_SEQUENCE_FLAG.getValue();
		System.out.println(">>>about to call init_sec_context (service=" + serviceName + ")");
		retval = gssapi.gss_init_sec_context(minorStatus, proxyCred, contextHandle, gssServiceName, GssApi.GSS_C_NO_OID,
				flags, 0, GssApi.GSS_C_NO_CHANNEL_BINDINGS, GssApi.GSS_C_NO_BUFFER, null, outputToken, null, null);
		// GssApi.MECH_KRB5, 0, 0, GssApi.GSS_C_NO_CHANNEL_BINDINGS, inputToken, null,
		// null, null, null);
		System.out.println("<<<back from init_sec_context:" + retval + "." + minorStatus.getValue());
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"initializing context (" + retval + "." + minorStatus.getValue() + ")");
			LOGGER.throwing(exception);
			throw exception;
		}

		PointerByReference targetCredHandle = new PointerByReference();
		System.out.println(">>>about to call add_cred");
		retval = gssapi.gss_add_cred(minorStatus, proxyCred, gssServiceName, GssApi.GSS_C_NO_OID,
				GssCredentialUsage.GSS_C_INITIATE.getValue(), 0, 0, targetCredHandle, null, 0, null);
		System.out.println("<<<back from add_cred:" + retval + "." + minorStatus.getValue());
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"adding credentials (" + retval + "." + minorStatus.getValue() + ")");
			LOGGER.throwing(exception);
			throw exception;
		}
		LOGGER.exit(targetCredHandle);
		return new gss_cred_id_t(targetCredHandle.getValue());
	}

	private void storeCredInto(Pointer acquiredCred, Path file) throws GSSException {
		IntByReference minorStatus = new IntByReference();
		int overwriteCred = 1;
		int defaultCred = 0;
		gss_key_value_element.ByReference credElement = new gss_key_value_element.ByReference("ccache", "FILE:" + file);
		gss_key_value_set credStore = new gss_key_value_set();
		credStore.count = 1;
		credStore.elements = credElement;
		Pointer oidsStored = new Pointer(0);
		PointerByReference oidsStoredHandle = new PointerByReference(oidsStored);
		IntByReference credStored = new IntByReference();

		System.out.println(">>>about to call store_cred_into");
		int retval = gssapi.gss_store_cred_into(minorStatus, acquiredCred, GssCredentialUsage.GSS_C_INITIATE.getValue(),
				GssApi.GSS_C_NO_OID, overwriteCred, defaultCred, credStore, oidsStoredHandle, credStored);
		System.out.println("<<<back from store_cred_into:" + retval + "." + minorStatus.getValue());
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(),
					"storing credential: " + retval + "." + minorStatus.getValue());
		}
	}

}
