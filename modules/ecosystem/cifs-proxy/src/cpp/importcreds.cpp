/*
 * importcreds.cpp
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

#define CHECK_ERROR(major,  minor, STRING) \
	if (GSS_ERROR(major)) { \
		printErrors(major, STRING); \
		return -1; \
	}

void printErrors(OM_uint32 status_code, const char* message) {
	OM_uint32 message_context;
	OM_uint32 maj_status;
	OM_uint32 min_status;
	gss_buffer_desc status_string;

	message_context = 0;

	do {

		maj_status = gss_display_status(&min_status, status_code,
		GSS_C_GSS_CODE,
		GSS_C_NO_OID, &message_context, &status_string);

		fprintf(stderr, "%s: %.*s\n",
				message,
				(int) status_string.length,
				(char *) status_string.value);

		gss_release_buffer(&min_status, &status_string);

	} while (message_context != 0);
}

int main(int argc, char **argv) {
	OM_uint32 minorStatus;
	int majorStatus;
	gss_name_t name = GSS_C_NO_NAME;
	gss_key_value_element_desc credElement;
	credElement.key = "ccache";
	const char* prefix = "FILE:";
	credElement.value = strcat(strdup(prefix), argv[1]);

	gss_key_value_set_desc credStore;
	credStore.count = 1;
	credStore.elements = &credElement;

	gss_cred_id_t outputCred;
	majorStatus = gss_acquire_cred_from(&minorStatus, name, 0, GSS_C_NO_OID_SET,
			GSS_C_BOTH, &credStore, &outputCred, NULL, NULL);
	CHECK_ERROR(majorStatus, minorStatus, "acquire_cred_from");

	majorStatus = gss_store_cred(&minorStatus, outputCred, GSS_C_BOTH, GSS_C_NO_OID, 1, 1, NULL, NULL);
	CHECK_ERROR(majorStatus, minorStatus, "store_cred");

	free((void*) credElement.value);

	return 0;
}

