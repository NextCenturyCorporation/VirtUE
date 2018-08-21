package com.nextcentury.savior.cifsproxy;

import org.ietf.jgss.GSSException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.nextcentury.savior.cifsproxy.GssApi.gss_OID_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_buffer_desc;
import com.nextcentury.savior.cifsproxy.GssApi.gss_name_t;
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

	/**
	 * Convenience method for
	 * {@link GssApi#gss_import_name(IntByReference, gss_buffer_desc, gss_OID_desc, PointerByReference)}.
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
}
