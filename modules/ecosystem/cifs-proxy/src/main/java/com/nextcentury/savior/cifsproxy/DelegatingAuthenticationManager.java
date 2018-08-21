/**
 * 
 */
package com.nextcentury.savior.cifsproxy;

import java.nio.file.Path;

import org.ietf.jgss.GSSException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
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

	public DelegatingAuthenticationManager(AuthenticationManager amDelegate, String serviceName, Path cacheFile) {
		LOGGER.entry(amDelegate, serviceName, cacheFile);
		this.amDelegate = amDelegate;
		this.serviceName = serviceName;
		this.cacheFile = cacheFile;
		// maybe disable this for production
		Native.setProtected(true);
		LOGGER.exit();
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
		gss_cred_id_t acceptorCredential;
		try {
			acceptorCredential = createAcceptorCred();
			gss_cred_id_t proxyCred = getProxyCredential(serviceTicket, acceptorCredential);
			gss_cred_id_t targetCred = getTargetCredential(proxyCred);
			IntByReference minorStatus = new IntByReference();
			storeCredInto(GssApi.INSTANCE, minorStatus, targetCred, cacheFile);
		} catch (GSSException e) {
			org.springframework.ldap.AuthenticationException exception = new org.springframework.ldap.AuthenticationException();
			exception.initCause(e);
			LOGGER.throwing(exception);
			throw exception;
		}
		// TODO
		// cacheCredential(delegatedCredHandle);
		Authentication newAuth = amDelegate.authenticate(authentication);
		LOGGER.exit(newAuth);
		return newAuth;
	}

	private gss_cred_id_t createAcceptorCred() throws GSSException {
		LOGGER.entry();
		GssApi gssapi = GssApi.INSTANCE;
		IntByReference minorStatus = new IntByReference();
		int retval;

		// it appears to be impossible to use gss_acquire_cred with any name except
		// GSS_C_NO_NAME
		// gss_name_t gssServiceName = new gss_name_t();
		// gss_OID_desc nameType = GssApi.GSS_C_NT_HOSTBASED_SERVICE;
		// gss_buffer_desc serviceNameBuffer = new
		// gss_buffer_desc(serviceName.getBytes());
		// retval = gssapi.gss_import_name(minorStatus, serviceNameBuffer, nameType,
		// new PointerByReference(gssServiceName));
		// if (retval != 0) {
		// GSSException exception = new GSSException(retval, minorStatus.getValue(),
		// "importing name '" + serviceName + "'");
		// LOGGER.throwing(exception);
		// throw exception;
		// }
		gss_OID_set_desc desiredMechs = new gss_OID_set_desc();
		desiredMechs.count = new NativeLong(1);
		desiredMechs.elements = new gss_OID_desc.ByReference(GssApi.MECH_KRB5.length, GssApi.MECH_KRB5.elements);
		int credUsage = GssCredentialUsage.GSS_C_BOTH.getValue();
		PointerByReference outputCredHandle = new PointerByReference();
		retval = gssapi.gss_acquire_cred(minorStatus, GssApi.GSS_C_NO_NAME, 0, desiredMechs, credUsage,
				outputCredHandle, null, null);
		if (retval != 0) {
			LOGGER.warn("error from gss_acquire_cred: " + retval + "." + minorStatus.getValue());
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"acquiring credential (for S4U2Proxy)");
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
		int retval = gssapi.gss_accept_sec_context(minorStatus, contextHandle, acceptorCredential, inputToken,
				GssApi.GSS_C_NO_CHANNEL_BINDINGS, null, null, outputToken, retFlags, null, delegatedCredHandle);
		/*
		 * In theory you have to do this in a loop. In practice that doesn't seem to
		 * happen. But just in case, check and bail out.
		 */
		if (outputToken.length.longValue() != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"unsupported protocol (only single init/accept context packet is supported)");
			gssapi.gss_release_buffer(minorStatus, outputToken);
			LOGGER.throwing(exception);
			throw exception;
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
		IntByReference minorStatus = new IntByReference();
		PointerByReference contextHandle = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		gss_buffer_desc inputToken = new gss_buffer_desc();
		gss_name_t importedTargetName = GssUtils.importName(gssapi, serviceName,
				GssApi.GSS_C_NT_HOSTBASED_SERVICE);
		int retval = gssapi.gss_init_sec_context(minorStatus, proxyCred, contextHandle, importedTargetName,
				GssApi.MECH_KRB5, 0, 0, GssApi.GSS_C_NO_CHANNEL_BINDINGS, inputToken, null, null, null, null);
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(), "initializing context");
			LOGGER.throwing(exception);
			throw exception;
		}

		gss_cred_id_t targetCredHandle = new gss_cred_id_t(0);
		gssapi.gss_add_cred(minorStatus, GssApi.GSS_C_NO_CREDENTIAL, importedTargetName, GssApi.MECH_KRB5,
				GssCredentialUsage.GSS_C_INITIATE, 0, 0, targetCredHandle, null, 0, null);
		LOGGER.exit(targetCredHandle);
		return targetCredHandle;
	}

	private void cacheServiceTicket(byte[] serviceTicket) throws GSSException {
		GssApi gssapi = GssApi.INSTANCE;

		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc gssToken = new gss_buffer_desc(serviceTicket);
		acceptSecContext(gssapi, gssToken);

		Pointer acquiredCred = acquireCred(gssapi, minorStatus);
		storeCredInto(gssapi, minorStatus, acquiredCred, cacheFile);
	}

	private static void acceptSecContext(GssApi gssapi, gss_buffer_desc clientToken) throws GSSException {
		IntByReference minorStatus = new IntByReference();
		PointerByReference contextHandle = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		PointerByReference srcName = new PointerByReference(GssApi.GSS_C_NO_NAME);
		PointerByReference mechType = new PointerByReference(GssApi.MECH_KRB5.getPointer());
		gss_buffer_desc outputToken = new gss_buffer_desc();
		IntByReference retFlags = new IntByReference();
		IntByReference timeRec = new IntByReference();
		PointerByReference delegatedCredHandle = new PointerByReference(new Pointer(0));
		int retval = gssapi.gss_accept_sec_context(minorStatus, contextHandle, GssApi.GSS_C_NO_CREDENTIAL, clientToken,
				GssApi.GSS_C_NO_CHANNEL_BINDINGS, srcName, mechType, outputToken, retFlags, timeRec,
				delegatedCredHandle);
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(),
					"accepting context (" + retval + "." + minorStatus.getValue() + ")");
		}
	}

	private static Pointer acquireCred(GssApi gssapi, IntByReference minorStatus) throws GSSException {
		gss_name_t desiredName = GssApi.GSS_C_NO_NAME;
		gss_OID_set_desc desiredMechs = new gss_OID_set_desc();
		desiredMechs.count = new NativeLong(1);
		desiredMechs.elements = new gss_OID_desc.ByReference(GssApi.MECH_KRB5.length, GssApi.MECH_KRB5.elements);
		GssCredentialUsage credUsage = GssCredentialUsage.GSS_C_INITIATE;
		PointerByReference acquiredCredHandle = new PointerByReference();
		Pointer actualMechsPtr = new Pointer(0);
		PointerByReference actualMechsHandle = new PointerByReference(actualMechsPtr);
		IntByReference retTime = new IntByReference();
		int retval = gssapi.gss_acquire_cred(minorStatus, desiredName, 0, desiredMechs, credUsage.getValue(),
				acquiredCredHandle, actualMechsHandle, retTime);
		if (retval != 0) {
			System.err.println("error acquiring credential: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "acquiring credential");
		}
		System.out.println("credential acquired");
		return acquiredCredHandle.getValue();
	}

	private static void storeCredInto(GssApi gssapi, IntByReference minorStatus, Pointer acquiredCred, Path file)
			throws GSSException {
		int overwriteCred = 1;
		int defaultCred = 0;
		gss_key_value_element.ByReference credElement = new gss_key_value_element.ByReference("ccache", "FILE:" + file);
		gss_key_value_set credStore = new gss_key_value_set();
		credStore.count = 1;
		credStore.elements = credElement;
		Pointer oidsStored = new Pointer(0);
		PointerByReference oidsStoredHandle = new PointerByReference(oidsStored);
		IntByReference credStored = new IntByReference();

		int retval = gssapi.gss_store_cred_into(minorStatus, acquiredCred, GssCredentialUsage.GSS_C_INITIATE.getValue(),
				GssApi.GSS_C_NO_OID, overwriteCred, defaultCred, credStore, oidsStoredHandle, credStored);
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(),
					"storing credential: " + retval + "." + minorStatus.getValue());
		}
	}

}
