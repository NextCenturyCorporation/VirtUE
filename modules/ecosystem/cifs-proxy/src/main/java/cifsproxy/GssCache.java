package cifsproxy;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import cifsproxy.GssApi.gss_buffer_desc;
import cifsproxy.GssApi.gss_cred_id_t;

public class GssCache {

	public static void main(String[] args) throws GSSException {
		GssApi gssapi = GssApi.INSTANCE;

		IntByReference minorStatus = new IntByReference();
		Pointer gssTargetName;
		{
			gss_buffer_desc targetName = new gss_buffer_desc();
			String target = "cifs@WS9.HQ.NEXTCENTURY.COM";
			Memory targetNameBuffer = JnaUtils.newMemory(target.getBytes());
			targetName.value = targetNameBuffer;
			targetName.length = new NativeLong(target.length() + 1);
			PointerByReference tempPtr = new PointerByReference();
			int retval = gssapi.gss_import_name(minorStatus, targetName, GssApi.GSS_C_NT_HOSTBASED_SERVICE,
					tempPtr);
			if (retval != 0) {
				System.err.println("error importing name: " + retval + "." + minorStatus.getValue());
				return;
			}
			gssTargetName = tempPtr.getPointer();
			System.out.println("imported name (" + target + ", " + gssTargetName + ", " + tempPtr + ")");
		}

		gss_cred_id_t initCred = GssApi.GSS_C_NO_CREDENTIAL;
		Pointer contextHandle;
		{
			gss_buffer_desc inputToken = new gss_buffer_desc();
			PointerByReference actualMechType = new PointerByReference();
			gss_buffer_desc outputToken = new gss_buffer_desc();
			IntByReference retFlags = new IntByReference();
			IntByReference retTime = new IntByReference();
			PointerByReference contextRef = new PointerByReference();
			int retval = gssapi.gss_init_sec_context(minorStatus, initCred, contextRef, gssTargetName,
					GssApi.MECH_KRB5, 0, 0, GssApi.GSS_C_NO_CHANNEL_BINDINGS, inputToken, actualMechType, outputToken,
					retFlags, retTime);
			if (retval != 0) {
				System.err.println("error initializing context: " + retval + "." + minorStatus.getValue());
				return;
			}
			contextHandle = contextRef.getPointer();
			System.out.println("initialized context: " + contextHandle);
		}
		return;
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
