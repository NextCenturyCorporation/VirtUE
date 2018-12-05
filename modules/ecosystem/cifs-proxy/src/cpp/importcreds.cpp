/*
 * importcreds.cpp
 *
 * This program takes Kerberos credentials from an existing Kerberos
 * credentials cache file and copies (imports) them into the current
 * credential cache. The existing principal in the current cache is
 * unmodified. If credentials in the imported file match (i.e., have
 * the same service and principal as) those in the current cache, I'm
 * not sure which credentials are retained.
 *
 *  Created on: Sep 7, 2018
 *      Author: clong
 */

#include <krb5.h>
#include <gssapi/gssapi.h>
#include <gssapi/gssapi_ext.h>
#include <cstring>
#include <cstdio>
#include <iostream>

void printUsage(const char* progname) {
    fprintf(stderr, "%s: %s certCacheFile\n", progname, progname);
}

#define CHECK_ERROR(major,  minor, STRING)                              \
	if (GSS_ERROR(major)) {                                             \
		printErrors(major, minor, STRING);                              \
		return -1;                                                      \
	}

void printErrors(OM_uint32 status_code, OM_uint32 minorStatus, const char* message) {
	OM_uint32 message_context;
	OM_uint32 maj_status;
	OM_uint32 min_status;
	gss_buffer_desc status_string;

	message_context = 0;

	do {

		maj_status = gss_display_status(&min_status, status_code,
		GSS_C_GSS_CODE,
		GSS_C_NO_OID, &message_context, &status_string);

		fprintf(stderr, "%s (%d.%d): %.*s\n", message,
                status_code, minorStatus,
                (int) status_string.length,
				(char *) status_string.value);

		gss_release_buffer(&min_status, &status_string);

	} while (message_context != 0);
}

int main(int argc, char **argv) {
    if (argc != 2) {
        fprintf(stderr, "%s: error: missing arguments\n", argv[0]);
        printUsage(argv[0]);
        exit(-1);
    }
    
	OM_uint32 minorStatus;
	int majorStatus;
	gss_name_t name = GSS_C_NO_NAME;
	gss_key_value_element_desc credElement;
	credElement.key = "ccache";
	const char* prefix = "FILE:";
    credElement.value = static_cast<char*>(malloc(strlen(prefix) + strlen(argv[1]) + 1));
    strcpy(const_cast<char*>(credElement.value), prefix);
    strcat(const_cast<char*>(credElement.value), argv[1]);

	gss_key_value_set_desc credStore;
	credStore.count = 1;
	credStore.elements = &credElement;

	gss_cred_id_t outputCred;
	majorStatus = gss_acquire_cred_from(&minorStatus, name, 0, GSS_C_NO_OID_SET,
                                        GSS_C_INITIATE, &credStore, &outputCred, NULL, NULL);
	CHECK_ERROR(majorStatus, minorStatus, "acquire_cred_from");

	majorStatus = gss_store_cred(&minorStatus, outputCred, GSS_C_INITIATE,
	GSS_C_NO_OID, 1, 1, NULL, NULL);
	CHECK_ERROR(majorStatus, minorStatus, "store_cred");

	free((void*) credElement.value);

	return 0;
}

