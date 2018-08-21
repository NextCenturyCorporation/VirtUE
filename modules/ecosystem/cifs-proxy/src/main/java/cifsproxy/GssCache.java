package cifsproxy;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.nextcentury.savior.cifsproxy.GssApi;
import com.nextcentury.savior.cifsproxy.GssApi.GssCredentialUsage;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_set_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_element;
import com.nextcentury.savior.cifsproxy.GssApi.gss_key_value_set;
import com.nextcentury.savior.cifsproxy.GssApi.gss_name_t;
import com.nextcentury.savior.cifsproxy.JnaUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class GssCache {

	public static void main(String[] args) throws GSSException {
		Native.setProtected(true);
		GssApi gssapi = GssApi.INSTANCE;

		IntByReference minorStatus = new IntByReference();
		Pointer gssTargetName = importName(gssapi, minorStatus);

		gss_cred_id_t initCred = GssApi.GSS_C_NO_CREDENTIAL;
		System.out.println("Look, no init!");
		gss_buffer_desc outputToken = initSecContext(gssapi, minorStatus, gssTargetName, initCred);

		System.out.println("Look, no accept!");
		// acceptSecContext(gssapi, outputToken);

		Pointer acquiredCred = acquireCred(gssapi, minorStatus);
		storeCredInto(gssapi, minorStatus, acquiredCred);
		System.out.println("Done");
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
			System.err.println("error accepting context: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "accepting context");
		}
		System.out.println("credential acquired");
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
		PointerByReference acquiredCredHandle = new PointerByReference();
		Pointer actualMechsPtr = new Pointer(0);
		PointerByReference actualMechsHandle = new PointerByReference(actualMechsPtr);
		IntByReference retTime = new IntByReference();
		int retval = gssapi.gss_acquire_cred(minorStatus, desiredName, 0, desiredMechs,
				GssCredentialUsage.GSS_C_INITIATE.getValue(), acquiredCredHandle, null, null);
		if (retval != 0) {
			System.err.println("error acquiring credential: " + retval + "." + minorStatus.getValue());
			throw new GSSException(retval, minorStatus.getValue(), "acquiring credential");
		}
		System.out.println("credential acquired");
		return acquiredCredHandle.getValue();
	}

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

	private static Pointer importName(GssApi gssapi, IntByReference minorStatus) throws GSSException {
		Pointer gssTargetName;
		gss_buffer_desc targetName = new gss_buffer_desc();
		String target = "http@webserver.test.savior\000";
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
		return gssTargetName;

	}

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

	private static final Oid KERB_V5_OID;
	static {
		Oid tempOid;
		try {
			tempOid = new Oid("1.2.840.113554.1.2.2");
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tempOid = null;
		}
		KERB_V5_OID = tempOid;
	}

	@SuppressWarnings("unused")
	private void dummy() throws GSSException {
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

		GSSManager manager = GSSManager.getInstance();

		GSSName clientName = manager.createName("clong@HQ.NEXTCENTURY.COM", GSSName.NT_USER_NAME);
		GSSCredential clientCred = manager.createCredential(clientName, 8 * 3600, createKerberosOid(),
				GSSCredential.INITIATE_ONLY);

		GSSName serverName = manager.createName("cifs/WS9@HQ.NEXTCENTURY.COM", GSSName.NT_HOSTBASED_SERVICE);

		GSSContext context = manager.createContext(serverName, createKerberosOid(), clientCred,
				GSSContext.DEFAULT_LIFETIME);
		context.requestMutualAuth(true);
		context.requestConf(false);
		context.requestInteg(true);

		byte[] outToken = context.initSecContext(new byte[0], 0, 0);

		Memory rawToken = new Memory(outToken.length);
		System.out.println("context exported");

		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc buffer = new gss_buffer_desc();
		// buffer.value = rawContextBytes;
		// buffer.length = new NativeLong(contextBytes.length);
		PointerByReference contextHandle = new PointerByReference();
		// GssApi.INSTANCE.gss_import_sec_context(minorStatus, buffer, contextHandle);
		System.out.println("context imported");
	}

	private static Oid createKerberosOid() {
		return KERB_V5_OID;
	}

}
