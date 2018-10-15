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
 * Wraps a real {@link AuthenticationManager} and caches the user credentials
 * passed to us, so they can be used later to mount the filesystem with
 * cifs.mount(8).
 * 
 * @author clong
 *
 */
public class CachingAuthenticationManager implements AuthenticationManager {

	private AuthenticationManager amDelegate;
	private Path cacheFile;

	/**
	 * 
	 * @param amDelegate
	 *                       The actual {@link AuthenticationManager}
	 * @param cacheFile
	 *                       where to store the credentials (in Kerberos credential
	 *                       cache format). This file will be overwritten if it
	 *                       exists.
	 */
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
		byte[] serviceToken = kerberosToken.getToken();
		try {
			cacheServiceTicket(serviceToken);
		} catch (GSSException e) {
			throw new AuthenticationServiceException("error caching service ticket", e);
		}
		return amDelegate.authenticate(authentication);
	}

	/**
	 * Create a service ticket for ourselves and cache it for later use.
	 * 
	 * @param serviceToken
	 *                         token from service authentication initiation (e.g.,
	 *                         sent with gss_sec_init_context)
	 * @throws GSSException
	 *                          if the ticket could not be created or cached
	 */
	private void cacheServiceTicket(byte[] serviceToken) throws GSSException {
		// maybe disable this for production
		Native.setProtected(true);

		GssApi gssapi = GssApi.INSTANCE;

		gss_buffer_desc gssToken = new gss_buffer_desc(serviceToken);
		acceptSecContext(gssapi, gssToken);

		Pointer acquiredCred = acquireCred(gssapi);
		storeCredInto(gssapi, acquiredCred, cacheFile);
	}

	/**
	 * Use
	 * {@link GssApi#gss_accept_sec_context(IntByReference, PointerByReference, com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t, gss_buffer_desc, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, PointerByReference, PointerByReference, gss_buffer_desc, IntByReference, IntByReference, PointerByReference)}
	 * to get a service ticket into the default cache.
	 * 
	 * @param gssapi
	 *                        the api instance to use
	 * @param clientToken
	 *                        the raw Kerberos token
	 * @throws GSSException
	 *                          if there was an error calling
	 *                          {@link GssApi#gss_accept_sec_context(IntByReference, PointerByReference, com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t, gss_buffer_desc, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, PointerByReference, PointerByReference, gss_buffer_desc, IntByReference, IntByReference, PointerByReference)}
	 */
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

	/**
	 * Get the Kerberos delegated credential from the default cache.
	 * 
	 * @param gssapi
	 *                   the api instance to use
	 * @return the delegated Kerberos credential, as a {@link Pointer} to a
	 *         {@link gss_cred_id_t}
	 * @throws GSSException
	 *                          if there was an error calling
	 *                          {@link GssApi#gss_acquire_cred(IntByReference, gss_name_t, int, gss_OID_set_desc, int, PointerByReference, PointerByReference, IntByReference)}
	 */
	private static Pointer acquireCred(GssApi gssapi) throws GSSException {
		IntByReference minorStatus = new IntByReference();
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

	/**
	 * Take a credential and store it in the specified file, overwriting any
	 * preexisting file.
	 * 
	 * @param gssapi
	 *                         the api instance to use
	 * @param acquiredCred
	 *                         the credential to store
	 * @param file
	 *                         the path to the file to store the credential in
	 * @throws GSSException
	 *                          if there was an error calling
	 *                          {@link GssApi#gss_store_cred_into(IntByReference, Pointer, int, gss_OID_desc, int, int, gss_key_value_set, PointerByReference, IntByReference)}
	 */
	private static void storeCredInto(GssApi gssapi, Pointer acquiredCred, Path file) throws GSSException {
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

		int retval = gssapi.gss_store_cred_into(minorStatus, acquiredCred, GssCredentialUsage.GSS_C_INITIATE.getValue(),
				GssApi.GSS_C_NO_OID, overwriteCred, defaultCred, credStore, oidsStoredHandle, credStored);
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(),
					"storing credential: " + retval + "." + minorStatus.getValue());
		}
	}

}
