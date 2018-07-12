package cifsproxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Subset of types and methods from the GSS API (usually found in
 * /usr/include/gss/{gssapi.h,gssapi_ext.h}).
 * 
 * For full descriptions of the types and functions, see
 * <a href="https://tools.ietf.org/html/rfc2744">RFC 2744</a>.
 * 
 * @author clong
 * @see org.ietf.jgss
 *
 */
public interface GssApi extends Library {
	/**
	 * The interface-based access to the native lib.
	 */
	final GssApi INSTANCE = (GssApi) Native.loadLibrary("gssapi_krb5", GssApi.class);

	final gss_cred_id_t GSS_C_NO_CREDENTIAL = new gss_cred_id_t(Pointer.NULL);
	final gss_channel_bindings_struct GSS_C_NO_CHANNEL_BINDINGS = null;
	final Pointer GSS_C_NO_CONTEXT = Pointer.NULL;
	final gss_name_t GSS_C_NO_NAME = new gss_name_t(Pointer.NULL);

	/**
	 * An OID that specifies a name as a host-based service (e.g.,
	 * "http@webserver").
	 * 
	 * @see gss_name_t
	 * @see #gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc,
	 *      PointerByReference)
	 */
	final gss_OID_desc GSS_C_NT_HOSTBASED_SERVICE = new gss_OID_desc(10, JnaUtils
			.newMemory(new byte[] { 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x12, 0x01, 0x02, 0x01, 0x04 }));
	// "\0x2a\0x86\0x48\0x86\0xf7\0x12\0x01\0x02\0x01\0x04"));

	/**
	 * An OID that specifies to use the Kerberos 5 mechanism.
	 * 
	 * @see #gss_init_sec_context(IntByReference, gss_cred_id_t, PointerByReference,
	 *      Pointer, gss_OID_desc, int, int, gss_channel_bindings_struct,
	 *      gss_buffer_desc, PointerByReference, gss_buffer_desc, IntByReference,
	 *      IntByReference)
	 * @see #gss_acquire_cred(IntByReference, gss_name_t, int, gss_OID_set_desc,
	 *      int, PointerByReference, PointerByReference, IntByReference)
	 */
	final gss_OID_desc MECH_KRB5 = new gss_OID_desc(9,
			JnaUtils.newMemory(new byte[] { 052, (byte) 0206, 0110, (byte) 0206, (byte) 0367, 022, 001, 002, 002, 0 }));

	/**
	 * How a credential will be used
	 * 
	 * @see gss_cred_id_t
	 */
	public enum GssCredentialUsage {
				/** Initiate or accept a connection. Note: this must be used for S2U4Proxy. */
				GSS_C_BOTH(0),
				/** Initiate a connection (e.g., a client) */
				GSS_C_INITIATE(1),
				/** Accept a connection (e.g, a server) */
				GSS_C_ACCEPT(2);
		private final int value;
		static Map<Integer, GssCredentialUsage> valueMap = new HashMap<>();
		static {
			for (GssCredentialUsage usage : GssCredentialUsage.values()) {
				valueMap.put(usage.getValue(), usage);
			}
		}

		private GssCredentialUsage(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		static GssCredentialUsage fromValue(int value) {
			return valueMap.get(value);
		}
	}

	final gss_OID_desc GSS_C_NO_OID = null;

	/**
	 * A list of values. The type of value depends on usage.
	 * 
	 * @author clong
	 *
	 */
	class gss_buffer_desc extends Structure {
		/**
		 * Number of values
		 */
		public NativeLong length;
		/**
		 * Pointer to congiguous memory chunk containing {@link #length} values
		 */
		public Pointer value;

		public gss_buffer_desc() {
		}

		/**
		 * Convenience ctor for a list of bytes.
		 * 
		 * @param value
		 */
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

	/**
	 * Used for identifying various things, including name types and encryption
	 * mechansim types.
	 * 
	 * @author clong
	 *
	 */
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

	/**
	 * A list of {@link gss_OID_desc}
	 * 
	 * @author clong
	 *
	 */
	class gss_OID_set_desc extends Structure {
		public NativeLong count;
		/**
		 * This can be one element, or (if {@link #count} > 1) a contiguous block of
		 * multiple elements.
		 * 
		 * @see https://www.eshayne.com/jnaex/index.html?example=15
		 */
		public gss_OID_desc.ByReference elements; // gss_OID_desc[]

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("count", "elements");
		}
	}

	/**
	 * For adding channels to a communications link.
	 * 
	 * @author clong
	 *
	 */
	class gss_channel_bindings_struct extends Structure {
		public int initiator_addrtype;
		public gss_buffer_desc initiator_address;
		public int acceptor_addrtype;
		public gss_buffer_desc acceptor_address;
		public gss_buffer_desc application_data;

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("initiator_addrtype", "initiator_address", "acceptor_addrtype", "acceptor_address",
					"application_data");
		}
	}

	/**
	 * A GSS name (e.g., user, service, computer).
	 * 
	 * @author clong
	 *
	 */
	class gss_name_t extends Memory {

		public gss_name_t() {
		}

		gss_name_t(Pointer p) {
			super();
			peer = Pointer.nativeValue(p);
		}
	}

	/**
	 * A GSS credential (opaque object)
	 * 
	 * @author clong
	 *
	 */
	class gss_cred_id_t extends Pointer {
		public gss_cred_id_t(long peer) {
			super(peer);
		}

		public gss_cred_id_t(Pointer p) {
			super(Pointer.nativeValue(p));
		}
	}

	/**
	 * A GSS context (opaque object)
	 * 
	 * @author clong
	 *
	 */
	class gss_ctx_id_t extends Memory {
	}

	/**
	 * Converts a string name to internal form.
	 * 
	 * @param minorStatus
	 * @param targetName
	 * @param nameType
	 * @param gss_name
	 * @return
	 */
	int gss_import_name(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc targetName, /* input_name_buffer */
			gss_OID_desc nameType, /* input_name_type(used to be const) */
			PointerByReference gss_name); /* output_name */

	/**
	 * Initiates a secure connection between this computer and another (usually a
	 * server). To be portable, an app should call this in a loop.
	 * 
	 * @param minorStatus
	 * @param credHandle
	 * @param contextHandle
	 * @param gssTargetName
	 * @param mechType
	 * @param flags
	 * @param time
	 * @param gssInputChannelBindings
	 * @param inputToken
	 * @param actualMechType
	 * @param outputToken
	 * @param retFlags
	 * @param retTime
	 * @return
	 */
	int gss_init_sec_context(IntByReference minorStatus, /* minor_status */
			gss_cred_id_t credHandle, /* claimant_cred_handle */
			PointerByReference contextHandle, /* context_handle */
			Pointer gssTargetName, /* target_name */
			gss_OID_desc mechType, /* mech_type (used to be const) */
			int flags, /* req_flags */
			int time, /* time_req */
			gss_channel_bindings_struct gssInputChannelBindings, /* input_chan_bindings */
			gss_buffer_desc inputToken, /* input_token */
			PointerByReference actualMechType, /* actual_mech_type */
			gss_buffer_desc outputToken, /* output_token */
			IntByReference retFlags, /* ret_flags */
			IntByReference retTime); /* time_rec */

	/**
	 * Assume a global identity; Obtain a GSS-API credential handle for pre-existing
	 * credentials. (from RFC 2744)
	 * 
	 * @param minorStatus
	 * @param desiredName
	 * @param time
	 * @param desiredMechs
	 * @param credUsage
	 * @param outputCredHandle
	 * @param actualMechs
	 * @param retTime
	 * @return
	 */
	int gss_acquire_cred(IntByReference minorStatus, /* minor_status */
			gss_name_t desiredName, /* desired_name */
			int time, /* time_req */
			gss_OID_set_desc desiredMechs, /* desired_mechs */
			int credUsage, /* cred_usage */
			PointerByReference outputCredHandle, /* output_cred_handle */
			PointerByReference actualMechs, /* actual_mechs */
			IntByReference retTime); /* time_rec */

	/**
	 * Allows a process to import a security context established by another process.
	 * A given interprocess token may be imported only once. (from RFC 2744) Of
	 * limited use with Java because Java does not export a context.
	 * 
	 * @param minorStatus
	 * @param buffer
	 * @param context
	 * @return
	 */
	int gss_import_sec_context(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc buffer, /* interprocess_token */
			gss_ctx_id_t context); /* context_handle */

	// from gssapi_ext.h

	/**
	 * A key-value pair.
	 * 
	 * @author clong
	 *
	 */
	class gss_key_value_element extends Structure {
		public String key;
		public String value;

		public gss_key_value_element(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("key", "value");
		}

		public static class ByReference extends gss_key_value_element implements Structure.ByReference {
			public ByReference(String key, String value) {
				super(key, value);
			}
		}

		public static class ByValue extends gss_key_value_element implements Structure.ByValue {
			public ByValue(String key, String value) {
				super(key, value);
			}
		}
	}

	/**
	 * A group of key-value pairs (i.e., a map).
	 * 
	 * @author clong
	 *
	 */
	class gss_key_value_set extends Structure {
		public int count;
		/**
		 * If more than one, a contiguous block of them.
		 */
		public gss_key_value_element.ByReference elements;

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("count", "elements");
		}
	}

	/**
	 * Export credentials into a store. For valid store types, see
	 * http://web.mit.edu/kerberos/krb5-devel/doc/basic/ccache_def.html#ccache-types
	 * 
	 * @param minorStatus
	 * @param credHandle
	 * @param gssCInitiate
	 * @param desiredMech
	 * @param overwriteCred
	 * @param defaultCred
	 * @param credStore
	 * @param oidsStored
	 * @param credStored
	 * @return
	 */
	int gss_store_cred_into(IntByReference minorStatus, /* minor_status */
			Pointer credHandle, /* input_cred_handle */
			int gssCInitiate, /* input_usage */
			gss_OID_desc desiredMech, /* desired_mech */
			int overwriteCred, /* overwrite_cred */
			int defaultCred, /* default_cred */
			gss_key_value_set credStore, /* cred_store */
			PointerByReference oidsStored, /* elements_stored */
			IntByReference credStored); /* cred_usage_stored */

}
