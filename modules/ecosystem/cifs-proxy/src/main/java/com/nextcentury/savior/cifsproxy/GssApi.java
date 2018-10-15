package com.nextcentury.savior.cifsproxy;

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
 * <code>/usr/include/gss/{gssapi.h,gssapi_ext.h}</code>).
 * 
 * For full descriptions of the types and functions, see one or more of
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc2744">RFC 2744</a>
 * <li><a href=
 * "http://web.mit.edu/kerberos/krb5-current/doc/appdev/gssapi.html">Developing
 * with GSSAPI</a> (part of the official Kerberos documentation)
 * <li><a href=
 * "https://docs.oracle.com/cd/E19683-01/816-1331/index.html">GSS-API
 * Programming Guide</a>
 * </ul>
 * 
 * (This class was motived by the lack of ability of Java to export credentials
 * in a way other existing processes could use (<code>mount.cifs</code>, in
 * particular).
 * {@link #gss_store_cred_into(IntByReference, Pointer, int, gss_OID_desc, int, int, gss_key_value_set, PointerByReference, IntByReference)}
 * is the missing feature.)
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

	// Error codes
	final int GSS_S_COMPLETE = 0;

	final int GSS_C_CALLING_ERROR_OFFSET = 24;
	final int GSS_C_ROUTINE_ERROR_OFFSET = 16;
	final int GSS_C_SUPPLEMENTARY_OFFSET = 0;
	final int GSS_C_CALLING_ERROR_MASK = 0377;
	final int GSS_C_ROUTINE_ERROR_MASK = 0377;
	final int GSS_C_SUPPLEMENTARY_MASK = 0177777;

	// See GssUtils for functions to help parse error codes

	// Calling errors
	final int GSS_S_CALL_INACCESSIBLE_READ = 1 << GSS_C_CALLING_ERROR_OFFSET;
	final int GSS_S_CALL_INACCESSIBLE_WRITE = 2 << GSS_C_CALLING_ERROR_OFFSET;
	final int GSS_S_CALL_BAD_STRUCTURE = 3 << GSS_C_CALLING_ERROR_OFFSET;

	// Routine errors
	final int GSS_S_BAD_MECH = 1 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_NAME = 2 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_NAMETYPE = 3 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_BINDINGS = 4 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_STATUS = 5 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_SIG = 6 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_MIC = GSS_S_BAD_SIG;
	final int GSS_S_NO_CRED = 7 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_NO_CONTEXT = 8 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_DEFECTIVE_CREDENTIAL = 10 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_CREDENTIALS_EXPIRED = 11 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_CONTEXT_EXPIRED = 12 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_FAILURE = 13 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_QOP = 14 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_UNAUTHORIZED = 15 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_UNAVAILABLE = 16 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_DUPLICATE_ELEMENT = 17 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_NAME_NOT_MN = 18 << GSS_C_ROUTINE_ERROR_OFFSET;
	final int GSS_S_BAD_MECH_ATTR = 19 << GSS_C_ROUTINE_ERROR_OFFSET;

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

	/*
	 * This name form shall be represented by the Object Identifier {iso(1)
	 * member-body(2) United States(840) mit(113554) infosys(1) gssapi(2) krb5(2)
	 * krb5_name(1)}. The recommended symbolic name for this type is
	 * "GSS_KRB5_NT_PRINCIPAL_NAME".
	 */
	final gss_OID_desc GSS_KRB5_NT_PRINCIPAL_NAME = new gss_OID_desc(10, JnaUtils
			.newMemory(new byte[] { 0x2a, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xf7, 0x12, 0x01, 0x02, 0x02, 0x01 }));

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

	enum GssContextFlag {
		GSS_C_DELEG_FLAG(1), GSS_C_MUTUAL_FLAG(2), GSS_C_REPLAY_FLAG(4), GSS_C_SEQUENCE_FLAG(8), GSS_C_CONF_FLAG(
				16), GSS_C_INTEG_FLAG(32), GSS_C_ANON_FLAG(
						64), GSS_C_PROT_READY_FLAG(128), GSS_C_TRANS_FLAG(256), GSS_C_DELEG_POLICY_FLAG(32768);

		private int value;

		private GssContextFlag(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * How a credential will be used
	 * 
	 * @see gss_cred_id_t
	 */
	enum GssCredentialUsage {
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
	final gss_OID_set_desc GSS_C_NO_OID_SET = null;

	final gss_buffer_desc GSS_C_NO_BUFFER = null;

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
			this.length = new NativeLong(0);
			this.value = null;
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

		public gss_buffer_desc(Pointer pointer) {
			super(pointer);
			read();
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

		public gss_OID_desc(Pointer p) {
			super(p);
			read();
		}

		@Override
		protected List<String> getFieldOrder() {
			return createFieldsOrder("length", "elements");
		}

		public static class ByReference extends gss_OID_desc implements Structure.ByReference {

			public ByReference() {
				super();
			}

			public ByReference(int length, Pointer elements) {
				super(length, elements);
			}
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

		public gss_OID_set_desc(Pointer p) {
			super(p);
			read();
		}

		public gss_OID_set_desc() {
			// TODO Auto-generated constructor stub
		}

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
	class gss_name_t extends Pointer {

		public gss_name_t(Pointer p) {
			super(Pointer.nativeValue(p));
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
	 * Note: the returned <code>gss_name</code> should be freed with
	 * {@link GssApi#gss_release_name(IntByReference, gss_name_t)}.
	 * 
	 * @param minorStatus
	 * @param targetName
	 * @param nameType
	 * @param gss_name
	 *                        Note: should be freed with
	 *                        {@link GssApi#gss_release_name(IntByReference, gss_name_t)}.
	 * 
	 * @return
	 */
	int gss_import_name(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc targetName, /* input_name_buffer */
			gss_OID_desc nameType, /* input_name_type(used to be const) */
			PointerByReference gss_name); /* output_name */

	/**
	 * Free GSSAPI-allocated storage associated with an internal-form name. (from
	 * RFC 2744)
	 * 
	 * @param minor_status
	 * @param name
	 * @return
	 * 
	 * @see #gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc,
	 *      PointerByReference)
	 */
	int gss_release_name(IntByReference minor_status, gss_name_t name);

	int gss_display_name(IntByReference minorStatus /* minor_status */, gss_name_t inputName /* input_name */,
			gss_buffer_desc outputNameBuffer /* output_name_buffer */,
			PointerByReference outputNameType /* output_name_type */);

	int gss_oid_to_str(IntByReference minorStatus, /* minor_status */
			gss_OID_desc oid, /* oid */
			gss_buffer_desc outBuffer); /* oid_str */

	int gss_inquire_names_for_mech(IntByReference minorStatus, /* minor_status */
			gss_OID_desc mech, /* mechanism */
			gss_OID_set_desc mechNames); /* name_types */

	/**
	 * Initiates a secure connection between this computer and another (usually a
	 * server). To be portable, an app should call this in a loop.
	 * 
	 * @param minorStatus
	 * @param credHandle
	 * @param contextHandle
	 *                                    Note: should be freed with
	 *                                    {@link #gss_delete_sec_context(IntByReference, gss_ctx_id_t, gss_buffer_desc)}
	 * @param gssTargetName
	 * @param mechType
	 * @param flags
	 * @param time
	 * @param gssInputChannelBindings
	 * @param inputToken
	 * @param actualMechType
	 * @param outputToken
	 *                                    Note: should be freed with
	 *                                    {@link #gss_release_buffer(IntByReference, gss_buffer_desc)}
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
	 * Accept a security context initiated by a peer application. (from RFC 2744)
	 * 
	 * @param minorStatus
	 * @param contextHandle
	 *                                 Note: should be freed with
	 *                                 {@link #gss_delete_sec_context(IntByReference, gss_ctx_id_t, gss_buffer_desc)}
	 * 
	 * @param credHandle
	 * @param inputToken
	 * @param inputChannelBindings
	 * @param sourceName
	 *                                 Note: should be freed with
	 *                                 {@link #gss_release_name(IntByReference, gss_name_t)}
	 * @param mechType
	 * @param outputToken
	 *                                 Note: should be freed with
	 *                                 {@link #gss_release_buffer(IntByReference, gss_buffer_desc)}
	 * @param retFlags
	 * @param timeRec
	 * @param delegatedCredHandle
	 *                                 Note: should be freed with
	 *                                 {@link #gss_release_cred(IntByReference, gss_cred_id_t)}
	 * @return
	 */
	int gss_accept_sec_context(IntByReference minorStatus, /* minor_status */
			PointerByReference contextHandle, /* context_handle */
			gss_cred_id_t credHandle, /* acceptor_cred_handle */
			gss_buffer_desc inputToken, /* input_token_buffer */
			gss_channel_bindings_struct inputChannelBindings, /* input_chan_bindings */
			PointerByReference sourceName, /* src_name (gss_name_t *) */
			PointerByReference mechType, /* mech_type (gss_OID*) */
			gss_buffer_desc outputToken, /* output_token */
			IntByReference retFlags, /* ret_flags */
			IntByReference timeRec, /* time_rec */
			PointerByReference delegatedCredHandle); /* delegated_cred_handle (gss_cred_id_t*) */

	/**
	 * Delete a security context. (from RFC 2744)
	 * 
	 * @param minor_status
	 * @param context_handle
	 * @param output_token
	 * @return
	 */
	int gss_delete_sec_context(IntByReference minor_status, gss_ctx_id_t context_handle, gss_buffer_desc output_token);

	/**
	 * Assume a global identity; Obtain a GSS-API credential handle for pre-existing
	 * credentials. (from RFC 2744)
	 * 
	 * @param minorStatus
	 * @param desiredName
	 * @param time
	 * @param desiredMechs
	 * @param credUsage
	 *                             a value from {@link GssCredentialUsage}
	 * @param outputCredHandle
	 *                             Note: should be freed with
	 *                             {@link #gss_release_cred(IntByReference, gss_cred_id_t)}
	 * @param actualMechs
	 *                             Note: should be freed with
	 *                             {@link #gss_release_oid_set(IntByReference, gss_OID_set_desc)}
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
	 * Release storage for a credential.
	 * 
	 * @param minorStatus
	 * @param credential
	 * @return
	 */
	int gss_release_cred(IntByReference minorStatus, /* minor_status */
			gss_cred_id_t credential); /* cred_handle */

	/**
	 * 
	 * @param minorStatus
	 * @param oid
	 * @return
	 */
	int gss_release_oid(IntByReference minorStatus, /* minor_status */
			gss_OID_desc oid); /* oid */

	/**
	 * Free storage associated with a GSSAPI-generated gss_OID_set object. (from RFC
	 * 2744)
	 * 
	 * @param minor_status
	 * @param set
	 * @return
	 */
	int gss_release_oid_set(IntByReference minor_status, gss_OID_set_desc set);

	/**
	 * Adds a credential-element to a credential. (from RFC 2744)
	 * 
	 * @param minorStatus
	 * @param inputCredHandle
	 * @param desiredName
	 * @param desiredMech
	 * @param credUsage
	 *                                 a value from {@link GssCredentialUsage}
	 * @param initiatorTimeRequest
	 * @param acceptorTimeRequest
	 * @param outputCredHandle
	 * @param actualMechs
	 *                                 Note: should be freed with
	 *                                 {@link #gss_release_oid_set(IntByReference, gss_OID_set_desc)}
	 * @param initiatorTimeRec
	 * @param acceptorTimeRec
	 * @return
	 */
	int gss_add_cred(IntByReference minorStatus, /* minor_status */
			gss_cred_id_t inputCredHandle, /* input_cred_handle */
			gss_name_t desiredName, /* desired_name */
			gss_OID_desc desiredMech, /* desired_mech */
			int credUsage, /* cred_usage */
			int initiatorTimeRequest, /* initiator_time_req */
			int acceptorTimeRequest, /* acceptor_time_req */
			PointerByReference outputCredHandle, /* output_cred_handle */
			PointerByReference actualMechs, /* actual_mechs */
			int initiatorTimeRec, /* initiator_time_rec */
			IntByReference acceptorTimeRec /* acceptor_time_rec */
	);

	/**
	 * Obtains information about a credential.
	 * 
	 * @param minorStatus
	 * @param credHandle
	 * @param credNameOutput
	 *                              Note: should be freed with
	 *                              {@link #gss_release_name(IntByReference, gss_name_t)}
	 * @param lifetimeRemaining
	 * @param credUsage
	 *                              a value from {@link GssCredentialUsage}
	 * @param mechanismsOidSet
	 *                              Note: should be freed with
	 *                              {@link #gss_release_oid_set(IntByReference, gss_OID_set_desc)}
	 * @return
	 */
	int gss_inquire_cred(IntByReference minorStatus, /* minor_status */
			gss_cred_id_t credHandle, /* cred_handle */
			PointerByReference credNameOutput, /* name */
			IntByReference lifetimeRemaining, /* lifetime */
			IntByReference credUsage, /* cred_usage */
			PointerByReference mechanismsOidSet); /* mechanisms */

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

	/**
	 * Free storage for a buffer. It must have been allocated by a GSS-API function.
	 * 
	 * @param minorStatus
	 * @param buffer
	 * @return
	 */
	int gss_release_buffer(IntByReference minorStatus, /* minor_status */
			gss_buffer_desc buffer /* buffer */);

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
	 * @param usage
	 *                          a value from {@link GssCredentialUsage}
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
			int usage, /* input_usage */
			gss_OID_desc desiredMech, /* desired_mech */
			int overwriteCred, /* overwrite_cred */
			int defaultCred, /* default_cred */
			gss_key_value_set credStore, /* cred_store */
			PointerByReference oidsStored, /* elements_stored */
			IntByReference credStored); /* cred_usage_stored */

}
