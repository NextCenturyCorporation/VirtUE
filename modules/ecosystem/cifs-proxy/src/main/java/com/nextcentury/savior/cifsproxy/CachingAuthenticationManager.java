/**
 * 
 */
package com.nextcentury.savior.cifsproxy;

import java.nio.file.Path;

import org.ietf.jgss.GSSException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;

import com.nextcentury.savior.cifsproxy.GssApi.GssCredentialUsage;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_set_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
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
public class CachingAuthenticationManager implements AuthenticationManager {

	private AuthenticationManager amDelegate;
	private Path cacheFile;

	public CachingAuthenticationManager(AuthenticationManager amDelegate, Path cacheFile) {
		this.amDelegate = amDelegate;
		this.cacheFile = cacheFile;
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
		if (!(authentication instanceof KerberosServiceRequestToken)) {
			throw new AuthenticationServiceException(
					"non-Kerberos authentication methods are not supported (token is of type "
							+ authentication.getClass().getName() + ")");
		}
		KerberosServiceRequestToken kerberosToken = (KerberosServiceRequestToken) authentication;
		byte[] serviceTicket = kerberosToken.getToken();
		try {
			cacheServiceTicket(serviceTicket);
		} catch (GSSException e) {
			throw new AuthenticationServiceException("error caching service ticket", e);
		}
		return amDelegate.authenticate(authentication);
	}

	private void cacheServiceTicket(byte[] serviceTicket) throws GSSException {
		// maybe disable this for production
		Native.setProtected(true);

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
		desiredMechs.elements = new gss_OID_desc.ByReference();
		// there might be an easier way to reuse the predefined one, but ByReference
		// makes it challenging
		desiredMechs.elements.length = GssApi.MECH_KRB5.length;
		desiredMechs.elements.elements = GssApi.MECH_KRB5.elements;
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
		gss_key_value_element.ByReference credElement = new gss_key_value_element.ByReference("ccache",
				"FILE:" + file);
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
