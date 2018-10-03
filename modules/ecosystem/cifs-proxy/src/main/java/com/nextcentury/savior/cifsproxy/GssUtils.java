package com.nextcentury.savior.cifsproxy;

import org.ietf.jgss.GSSException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.nextcentury.savior.cifsproxy.GssApi.GssCredentialUsage;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_set_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_cred_id_t;
import com.nextcentury.savior.cifsproxy.GssApi.gss_name_t;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Utility methods for using {@link GssApi}.
 * 
 * @author clong
 *
 */
public class GssUtils {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(GssUtils.class);

	private GssUtils() {
	}

	public static int GSS_CALLING_ERROR(int retval) {
		return retval & (GssApi.GSS_C_CALLING_ERROR_MASK << GssApi.GSS_C_CALLING_ERROR_OFFSET);
	}

	public static int GSS_ROUTINE_ERROR(int retval) {
		return retval & (GssApi.GSS_C_ROUTINE_ERROR_MASK << GssApi.GSS_C_ROUTINE_ERROR_OFFSET);
	}

	public static int GSS_SUPPLEMENTARY_ERROR(int retval) {
		return retval & (GssApi.GSS_C_SUPPLEMENTARY_MASK << GssApi.GSS_C_SUPPLEMENTARY_OFFSET);
	}

	public static int GSS_ERROR(int retval) {
		return retval & ((GssApi.GSS_C_CALLING_ERROR_MASK << GssApi.GSS_C_CALLING_ERROR_OFFSET)
				| (GssApi.GSS_C_ROUTINE_ERROR_MASK << GssApi.GSS_C_ROUTINE_ERROR_OFFSET));
	}

	/**
	 * Convenience method for
	 * {@link GssApi#gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc, PointerByReference)}.
	 * 
	 * Note: The returned <code>gss_name_t</code> must be freed with
	 * {@link #releaseName(gss_name_t)} (or
	 * {@link GssApi#gss_release_name(IntByReference, gss_name_t)}) when it is no
	 * longer in use.
	 * 
	 * @param api
	 *                     api reference
	 * @param name
	 *                     name to import
	 * @param nameType
	 *                     type of name (e.g.,
	 *                     {@link GssApi#GSS_C_NT_HOSTBASED_SERVICE})
	 * @return an imported name
	 * @throws GSSException
	 *                          if the name could not be imported (e.g., was the
	 *                          wrong format)
	 */
	public static gss_name_t importName(GssApi api, String name, gss_OID_desc nameType) throws GSSException {
		LOGGER.entry(api, name, nameType);
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc gssInputName = new gss_buffer_desc(name.getBytes());
		PointerByReference gssName = new PointerByReference();
		int retval = api.gss_import_name(minorStatus, gssInputName, nameType, gssName);
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(),
					"importing name '" + name + "' as type " + nameType);
			LOGGER.throwing(exception);
			throw exception;
		}
		gss_name_t importedName = new gss_name_t(gssName.getValue());
		LOGGER.exit(importedName);
		return importedName;
	}

	/**
	 * Release storage for a name allocated with
	 * {@link #importName(GssApi, String, gss_OID_desc)} (or
	 * {@link GssApi#gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc, PointerByReference)}).
	 * 
	 * @param api
	 *                 api reference
	 * @param name
	 *                 name to free
	 * @throws GSSException
	 *                          if the name could not be freed
	 */
	public static void releaseName(GssApi api, gss_name_t name) throws GSSException {
		LOGGER.entry(api, name);
		IntByReference minorStatus = new IntByReference();
		int retval = api.gss_release_name(minorStatus, name);
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(), "releasing name");
			LOGGER.throwing(exception);
			throw exception;
		}
		LOGGER.exit();
	}

	public static String getStringName(GssApi api, gss_name_t name) throws GSSException {
		LOGGER.entry(name);
		if (name.equals(GssApi.GSS_C_NO_NAME)) {
			return "";
		}
		String stringname;
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc outputNameBuffer = new gss_buffer_desc();// outputNameHandle.getPointer());
		int retval = api.gss_display_name(minorStatus, name, outputNameBuffer, null);
		if (retval != 0) {
			GSSException exception = new GSSException(retval, minorStatus.getValue(), "getting display name for: " + name);
			LOGGER.throwing(exception);
			throw exception;
		}
		byte[] nameAsBytes = outputNameBuffer.value.getByteArray(0, outputNameBuffer.length.intValue());
		stringname = new String(nameAsBytes);
		LOGGER.exit(stringname);
		return stringname;
	}

	public static class UnpackedName {
		public String name;
		public String type;
	}

	public static UnpackedName unpackName(GssApi api, gss_name_t name) throws GSSException {
		UnpackedName result = new UnpackedName();
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc outputNameBuffer = new gss_buffer_desc();
		gss_OID_desc nameType = new gss_OID_desc();
		PointerByReference nameTypeRef = new PointerByReference(nameType.getPointer());
		int retval;
		try {
			retval = api.gss_display_name(minorStatus, name, outputNameBuffer, nameTypeRef);
			if (retval != 0) {
				GSSException exception = new GSSException(retval, minorStatus.getValue(),
						"getting display name for:" + name);
				LOGGER.throwing(exception);
				throw exception;
			}
			byte[] nameAsBytes = outputNameBuffer.value.getByteArray(0, outputNameBuffer.length.intValue());
			result.name = new String(nameAsBytes);
		} finally {
			if (outputNameBuffer.value != null) {
				retval = api.gss_release_buffer(minorStatus, outputNameBuffer);
				if (retval != 0) {
					LOGGER.error("failed to release name buffer: " + retval + "." + minorStatus.getValue());
				}
			}
		}
		//if (nameTypeRef.getValue() != null) {
			//gss_OID_desc nameType = new gss_OID_desc(nameTypeRef.getValue());
			result.type = getOidString(api, nameType);
		//}
		//else {
			//result.type = "(null)";			
		//}
		return result;
	}

	public static String getOidString(GssApi api, gss_OID_desc oid) throws GSSException {
		if (oid == null || oid.length == 0) {
			return "(null)";
		}
		int retval;
		IntByReference minorStatus = new IntByReference();
		gss_buffer_desc typeBuffer = new gss_buffer_desc();
		String nameTypeString;
		try {
			System.out.println(">>> about to call oid_to_str: oid=" + oid + ", typeBuffer=" + typeBuffer);
			retval = api.gss_oid_to_str(minorStatus, oid, typeBuffer);
			System.out.println("<<< back from oid_to_str");
			if (retval != 0) {
				GSSException exception = new GSSException(retval, minorStatus.getValue(),
						"getting OID string for:" + oid);
				LOGGER.throwing(exception);
				throw exception;
			}
			nameTypeString = new String(typeBuffer.value.getByteArray(0, typeBuffer.length.intValue()));
		} finally {
			if (typeBuffer.value != null) {
				retval = api.gss_release_buffer(minorStatus, typeBuffer);
				if (retval != 0) {
					LOGGER.error("failed to release type buffer: " + retval + "." + minorStatus.getValue());
				}
			}
		}
		return nameTypeString;
	}

	public static String getCredInfo(GssApi api, gss_cred_id_t cred) {
		LOGGER.entry(api, cred);
		StringBuilder result = new StringBuilder(cred.toString());
		IntByReference minorStatus = new IntByReference();
		PointerByReference credName = new PointerByReference();
		IntByReference lifetimeRemaining = new IntByReference();
		IntByReference credUsage = new IntByReference();
		PointerByReference mechanismsOidSet = new PointerByReference();
		result.append('[');
		try {
			int retval = api.gss_inquire_cred(minorStatus, cred, credName, lifetimeRemaining, credUsage,
					mechanismsOidSet);
			switch (retval) {
			case GssApi.GSS_S_COMPLETE:
				result.append("name=");
				try {
					UnpackedName name = unpackName(api, new gss_name_t(credName.getValue()));
					result.append(name.name);
					result.append("[type=");
					result.append(name.type);
					result.append(']');
				} catch (GSSException e) {
					result.append("<ERROR>");
				}
				result.append(",lifetime=");
				result.append(lifetimeRemaining.getValue());
				result.append(",usage=");
				result.append(GssCredentialUsage.fromValue(credUsage.getValue()));

				// TODO maybe change this to do something mechanism-specific with
				// gss_inquire_names_for_mech
				gss_OID_set_desc oidSet = new gss_OID_set_desc(mechanismsOidSet.getValue());
				int numOids = oidSet.count.intValue();
				result.append(",mechanisms(");				
				result.append(numOids);
				result.append(")=[");
				if (numOids > 0) {
					gss_OID_desc[] oidArray = (gss_OID_desc[]) oidSet.elements.toArray(numOids);
					for (int i = 0; i < numOids; i++) {
						gss_OID_desc oid = oidArray[i];
						result.append('[');
						try {
							String[] namesForMech = getNamesForMech(api, oid);
							if (namesForMech.length > 0) {
								result.append(String.join(",", namesForMech));
							}
							else {
								result.append(getOidString(api, oid));
							}
						} catch (GSSException e) {
							result.append("<ERROR>");
						}
						result.append("],");
					}
					// delete trailing ','
					result.deleteCharAt(result.length() - 1);
				}
				result.append("]");
				break;
			case GssApi.GSS_S_NO_CRED:
				result.append("<GSS_S_NO_CRED>");
				break;
			case GssApi.GSS_S_DEFECTIVE_CREDENTIAL:
				result.append("<GSS_S_DEFECTIVE_CREDENTIAL>");
				break;
			}
		} finally {
			if (credName.getValue() != null) {
				api.gss_release_name(minorStatus, new gss_name_t(credName.getValue()));
			}
			if (mechanismsOidSet.getValue() != null) {
				// TODO free this somehow
				/*
				gss_OID_set_desc tempOidSet = new gss_OID_set_desc(mechanismsOidSet.getValue());
				if (tempOidSet.count.intValue() > 0) {
					System.out.println(">>> about to release_oid_set: " + tempOidSet);
					api.gss_release_oid_set(minorStatus, tempOidSet);
					System.out.println("<<< back from release_oid_set");
				}
				*/
			}
		}
		result.append(']');
		LOGGER.exit(result.toString());
		return result.toString();
	}
	
	public static String[] getNamesForMech(GssApi api, gss_OID_desc oid) throws GSSException {
		LOGGER.entry(api, oid);
		IntByReference minorStatus = new IntByReference();
		gss_OID_set_desc mechNames = new gss_OID_set_desc();
		mechNames.count = new NativeLong(0);
		System.out.println(">>>about to call inquire_names_for_mech");
		int retval = api.gss_inquire_names_for_mech(minorStatus, oid, mechNames);
		System.out.println("<<<back from inquire_names_for_mech");
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(), "could not get names for mech: " + oid);
		}
		int numNames = mechNames.count.intValue();
		System.out.println("*** gNFM: converting " + numNames);
		String[] result;
		if (mechNames.elements != null) {
			gss_OID_desc[] mechNameArray = (gss_OID_desc[]) mechNames.elements.toArray(numNames);
			System.out.println("*** gNFM: converted " + numNames);
			result = new String[numNames];
			for (int i = 0; i < numNames; i++) {
				System.out.println("*** gNFM: trying #" + i);
				result[i] = getOidString(api, mechNameArray[i]);
			}
		}
		else {
			result = new String[0];
		}
		System.out.println(">>>about to call release_oid_set");
		retval = api.gss_release_oid_set(minorStatus, mechNames);
		System.out.println(">>>back from release_oid_set");
		if (retval != 0) {
			throw new GSSException(retval, minorStatus.getValue(), "could not release mechanism names");
		}
		LOGGER.exit(result);
		return result;
	}

	public static String decodeMajorStatus(int major) {
		LOGGER.entry(major);
		StringBuilder result = new StringBuilder();
		result.append(major).append("(").append("calling=0x").append(Integer.toHexString(GSS_CALLING_ERROR(major)))
				.append(",routine=0x").append(Integer.toHexString(GSS_ROUTINE_ERROR(major))).append(",suppl=0x")
				.append(Integer.toHexString(GSS_SUPPLEMENTARY_ERROR(major))).append(")");
		LOGGER.exit(result.toString());
		return result.toString();
	}
}
