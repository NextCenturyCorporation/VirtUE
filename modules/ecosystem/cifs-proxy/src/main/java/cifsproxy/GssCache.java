package cifsproxy;

import org.ietf.jgss.GSSException;

import com.nextcentury.savior.cifsproxy.GssApi;
import com.nextcentury.savior.cifsproxy.GssApi.GssCredentialUsage;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_set_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_element;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_set;
import com.nextcentury.savior.cifsproxy.GssApi.gss_name_t;
import com.nextcentury.savior.cifsproxy.GssUtils;
import com.nextcentury.savior.cifsproxy.JnaUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * This class demonstrates how to call many of the GSSAPI functions (from
 * {@link GssApi}). It is not used in the production system.
 * 
 * @author clong
 *
 */
@SuppressWarnings("unused")
public class GssCache {

	/**
	 * No args are used.
	 * 
	 * @param args
	 * @throws GSSException
	 */
	public static void main(String[] args) throws GSSException {
		Native.setProtected(true);
		GssApi gssapi = GssApi.INSTANCE;

		IntByReference minorStatus = new IntByReference();
		Pointer gssTargetName = importName(gssapi, minorStatus);

		gss_cred_id_t initCred = GssApi.GSS_C_NO_CREDENTIAL;
		gss_buffer_desc outputToken = initSecContext(gssapi, minorStatus, gssTargetName, initCred);

		// acceptSecContext(gssapi, outputToken);

		Pointer acquiredCred = acquireCred(gssapi, minorStatus);
		storeCredInto(gssapi, minorStatus, acquiredCred);
	}

	/**
	 * Example of calling
	 * {@link GssApi#gss_accept_sec_context(IntByReference, PointerByReference, gss_cred_id_t, gss_buffer_desc, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, PointerByReference, PointerByReference, gss_buffer_desc, IntByReference, IntByReference, PointerByReference)}
	 * 
	 * @param gssapi
	 * @param clientToken
	 *                        raw token passed to the "server" (e.g., obtained from
	 *                        {@link GssApi#gss_init_sec_context(IntByReference, gss_cred_id_t, PointerByReference, Pointer, gss_OID_desc, int, int, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, gss_buffer_desc, PointerByReference, gss_buffer_desc, IntByReference, IntByReference)})
	 * @throws GSSException
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
			System.err.println("error accepting context: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "accepting context");
		}
		System.out.println("credential acquired");
	}

	/**
	 * Example of calling
	 * {@link GssApi#gss_acquire_cred(IntByReference, gss_name_t, int, gss_OID_set_desc, int, PointerByReference, PointerByReference, IntByReference)}
	 * 
	 * @param gssapi
	 * @param minorStatus
	 * @return
	 * @throws GSSException
	 */
	private static Pointer acquireCred(GssApi gssapi, IntByReference minorStatus) throws GSSException {
		gss_name_t desiredName = GssApi.GSS_C_NO_NAME;
		gss_OID_set_desc desiredMechs = new gss_OID_set_desc();
		desiredMechs.count = new NativeLong(1);
		desiredMechs.elements = new gss_OID_desc.ByReference();
		// there might be an easier way to reuse the predefined one, but ByReference
		// makes it challenging
		desiredMechs.elements.length = GssApi.MECH_KRB5.length;
		desiredMechs.elements.elements = GssApi.MECH_KRB5.elements;
		PointerByReference acquiredCredHandle = new PointerByReference();
		int retval = gssapi.gss_acquire_cred(minorStatus, desiredName, 0, desiredMechs,
				GssCredentialUsage.GSS_C_INITIATE.getValue(), acquiredCredHandle, null, null);
		if (retval != 0) {
			System.err.println("error acquiring credential: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "acquiring credential");
		}
		System.out.println("credential acquired");
		return acquiredCredHandle.getValue();
	}

	/**
	 * Example of calling
	 * {@link GssApi#gss_init_sec_context(IntByReference, gss_cred_id_t, PointerByReference, Pointer, gss_OID_desc, int, int, com.nextcentury.savior.cifsproxy.GssApi.gss_channel_bindings_struct, gss_buffer_desc, PointerByReference, gss_buffer_desc, IntByReference, IntByReference)}
	 * 
	 * @param gssapi
	 * @param minorStatus
	 * @param gssTargetName
	 * @param initCred
	 * @return
	 * @throws GSSException
	 */
	private static gss_buffer_desc initSecContext(GssApi gssapi, IntByReference minorStatus, Pointer gssTargetName,
			gss_cred_id_t initCred) throws GSSException {
		gss_buffer_desc inputToken = new gss_buffer_desc();
		Pointer actualMechType = new Pointer(0);
		PointerByReference actualMechTypeHandle = new PointerByReference(actualMechType);
		gss_buffer_desc outputToken = new gss_buffer_desc();
		IntByReference retFlags = new IntByReference();
		IntByReference retTime = new IntByReference();
		PointerByReference contextRef = new PointerByReference(GssApi.GSS_C_NO_CONTEXT);
		int retval = gssapi.gss_init_sec_context(minorStatus, initCred, contextRef, gssTargetName, GssApi.MECH_KRB5, 0,
				0, GssApi.GSS_C_NO_CHANNEL_BINDINGS, inputToken, actualMechTypeHandle, outputToken, retFlags, retTime);
		if (retval != 0) {
			System.err.println("error initializing context: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "initializing context");
		}
		Pointer contextHandle = contextRef.getValue();
		System.out.println("initialized context: " + contextHandle);
		System.out.println("output token: " + outputToken);
		return outputToken;
	}

	/**
	 * Example of calling
	 * {@link GssApi#gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc, PointerByReference)}
	 * 
	 * @param gssapi
	 * @param minorStatus
	 * @return
	 * @throws GSSException
	 */
	private static Pointer importName(GssApi gssapi, IntByReference minorStatus) throws GSSException {
		Pointer gssTargetName;
		gss_buffer_desc targetName = new gss_buffer_desc();
		String target = "http@webserver.test.savior";
		Memory targetNameBuffer = JnaUtils.newMemory(target.getBytes());
		targetName.value = targetNameBuffer;
		targetName.length = new NativeLong(target.length() + 1);
		PointerByReference tempPtr = new PointerByReference();
		System.out.println("global OID: " + GssApi.GSS_C_NT_HOSTBASED_SERVICE);

		int retval = gssapi.gss_import_name(minorStatus, targetName, GssApi.GSS_KRB5_NT_PRINCIPAL_NAME, tempPtr);
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(), "importing name");
		}
		gssTargetName = tempPtr.getValue();
		System.out.println("imported name (" + target + ", " + gssTargetName + ", " + tempPtr + ")");
		System.out.println("internal name is '" + GssUtils.getStringName(gssapi, new gss_name_t(gssTargetName)) + "'");
		return gssTargetName;

	}

	/**
	 * Example of calling
	 * {@link GssApi#gss_store_cred_into(IntByReference, Pointer, int, gss_OID_desc, int, int, gss_key_value_set, PointerByReference, IntByReference)}
	 * 
	 * @param gssapi
	 * @param minorStatus
	 * @param acquiredCred
	 * @throws GSSException
	 */
	private static void storeCredInto(GssApi gssapi, IntByReference minorStatus, Pointer acquiredCred)
			throws GSSException {
		int overwriteCred = 1;
		int defaultCred = 0;
		gss_key_value_element.ByReference credElement = new gss_key_value_element.ByReference("ccache",
				"FILE:jcredstore");
		gss_key_value_set credStore = new gss_key_value_set();
		credStore.count = 1;
		credStore.elements = credElement;
		Pointer oidsStored = new Pointer(0);
		PointerByReference oidsStoredHandle = new PointerByReference(oidsStored);
		IntByReference credStored = new IntByReference();
		System.out.println("about to store...");
		int retval = gssapi.gss_store_cred_into(minorStatus, acquiredCred, GssCredentialUsage.GSS_C_INITIATE.getValue(),
				GssApi.GSS_C_NO_OID, overwriteCred, defaultCred, credStore, oidsStoredHandle, credStored);
		if (retval != 0) {
			System.err.println("storing credential: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "storing credential");
		}
		System.out.println("credential stored");
	}
}
