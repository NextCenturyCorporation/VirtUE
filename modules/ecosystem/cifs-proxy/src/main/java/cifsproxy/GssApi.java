package cifsproxy;

import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface GssApi extends Library {
	GssApi INSTANCE = (GssApi) Native.loadLibrary("gssapi_krb5", GssApi.class);
	NativeLibrary NATIVE_INSTANCE = NativeLibrary.getInstance("gssapi_krb5");

	final gss_cred_id_t GSS_C_NO_CREDENTIAL = new gss_cred_id_t(Pointer.NULL);
	final gss_channel_bindings_struct GSS_C_NO_CHANNEL_BINDINGS = null;

	final gss_OID_desc GSS_C_NT_HOSTBASED_SERVICE = (gss_OID_desc) Structure.newInstance(gss_OID_desc.class,
			NATIVE_INSTANCE.getGlobalVariableAddress("GSS_C_NT_HOSTBASED_SERVICE"));

	final gss_OID_desc MECH_KRB5 = new gss_OID_desc(9,
			JnaUtils.newMemory(new byte[] { 052, (byte) 0206, 0110, (byte) 0206, (byte) 0367, 022, 001, 002, 002, 0 }));

	class gss_buffer_desc extends Structure {
		public NativeLong length;
		public Pointer value;

		public gss_buffer_desc() {
		}

		public gss_buffer_desc(byte[] value) {
			length = new NativeLong(value.length);
			this.value = new Memory(value.length);
			this.value.write(0, value, 0, value.length);
		}

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("length", "value");
		}

		public static class ByReference extends gss_buffer_desc implements Structure.ByReference {
		}

		public static class ByValue extends gss_buffer_desc implements Structure.ByValue {
		}
	}

	class gss_OID_desc extends Structure {
		public int length;
		public Pointer elements;

		public gss_OID_desc() {
		}

		public gss_OID_desc(int length, Pointer elements) {
			this.length = length;
			this.elements = elements;
		}

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("length", "elements");
		}

		public static class ByReference extends gss_OID_desc implements Structure.ByReference {
		}

		public static class ByValue extends gss_OID_desc implements Structure.ByValue {
		}
	}

	class gss_OID_set_desc {
		NativeLong count;
		gss_OID_desc[] elements;
	}

	class gss_channel_bindings_struct extends Structure {
		int initiator_addrtype;
		gss_buffer_desc.ByReference initiator_address;
		int acceptor_addrtype;
		gss_buffer_desc.ByReference acceptor_address;
		gss_buffer_desc.ByReference application_data;

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("initiator_addrtype", "initiator_address", "acceptor_addrtype", "acceptor_address",
					"application_data");
		}
	}

	/*
	 * We don't actually *have* to have these simple subclasses of
	 * PointerByReference, but it helps both document what the values are and
	 * prevent passing the wrong type to a function.
	 */

	class gss_name_t extends Memory {
	}

	class gss_cred_id_t extends PointerByReference {
		public gss_cred_id_t() {
		}

		public gss_cred_id_t(Pointer p) {
			super(p);
		}
	}

	class gss_ctx_id_t extends Memory {
	}

	int gss_import_name(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc buffer, /* input_name_buffer */
			gss_OID_desc oid, /* input_name_type(used to be const) */
			PointerByReference gss_name); /* output_name */

	int gss_init_sec_context(IntByReference minorStatus, /* minor_status */
			gss_cred_id_t credHandle, /* claimant_cred_handle */
			PointerByReference contextHandle, /* context_handle */
			Pointer gssTargetName, /* target_name */
			gss_OID_desc mechType, /* mech_type (used to be const) */
			int flags, /* req_flags */
			int time, /* time_req */
			gss_channel_bindings_struct inputBindings, /* input_chan_bindings */
			gss_buffer_desc inputToken, /* input_token */
			PointerByReference actualMechType, /* actual_mech_type */
			gss_buffer_desc outputToken, /* output_token */
			IntByReference retFlags, /* ret_flags */
			IntByReference retTime); /* time_rec */

	int gss_acquire_cred(IntByReference minorStatus, /* minor_status */
			gss_name_t desiredName, /* desired_name */
			int time, /* time_req */
			gss_OID_set_desc desiredMechs, /* desired_mechs */
			int credUsage, /* cred_usage */
			gss_cred_id_t outputCredHandle, /* output_cred_handle */
			gss_OID_set_desc actualMechs, /* actual_mechs */
			IntByReference retTime); /* time_rec */

	int gss_import_sec_context(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc buffer, /* interprocess_token */
			gss_ctx_id_t context); /* context_handle */

}
