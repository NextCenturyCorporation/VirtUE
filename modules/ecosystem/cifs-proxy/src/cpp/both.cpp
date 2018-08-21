/*
 * both.cpp
 *
 *  Created on: Aug 16, 2018
 *      Author: clong
 */

#include <iostream>
#include <cstring>
#include <cstdio>
#include <fstream>
#include <ios>

#include <gssapi/gssapi.h>
#include <gssapi/gssapi_ext.h>
#include <gssapi/gssapi_krb5.h>

static gss_OID_desc mech_krb5 = { 9,
		(void*) "\052\206\110\206\367\022\001\002\002" };
gss_OID_set_desc mechset_krb5 = { 1, &mech_krb5 };

void printErrors(OM_uint32 majorStatus, OM_uint32 minorStatus) {
	OM_uint32 message_context;
	OM_uint32 majorStatusOut;
	OM_uint32 min_status;
	gss_buffer_desc status_string;

	message_context = 0;

	do {
		majorStatusOut = gss_display_status(&minorStatus, majorStatus,
				GSS_C_GSS_CODE, GSS_C_NO_OID, &message_context, &status_string);

		fprintf(stderr, "%.*s\n", (int) status_string.length,
				(char *) status_string.value);

		gss_release_buffer(&min_status, &status_string);

	} while (message_context != 0);
}

int main(int argc, char* argv[]) {
	unsigned int majorStatus;
	unsigned int minorStatus;
	const gss_name_t acceptor_name = GSS_C_NO_NAME;
	gss_cred_id_t initCredHandle = GSS_C_NO_CREDENTIAL;
	gss_ctx_id_t contextHandle = GSS_C_NO_CONTEXT;
	gss_buffer_desc targetName;
	gss_name_t gssTargetName;
	gss_OID kerbMechanism = &mech_krb5;
	OM_uint32 reqFlags = 0; // GSS_C_DELEG_FLAG
	OM_uint32 timeReq = 0;
	gss_OID actualMechType = 0;
	gss_buffer_desc inputToken;
	gss_buffer_desc outputToken = { 0, 0 };
	unsigned int retFlags;
	unsigned int timeRec;

    if (argc > 1) {
        targetName.value = (void*) argv[1];
    }
    else {
        //	targetName.value = (void*) "cifs@ws9.hq.nextcentury.com";
        //targetName.value = (void*) "cifs@pc-5000-cl.hq.nextcentury.com";
        targetName.value = (void*) "PC-5000-CL$@HQ.NEXTCENTURY.COM";
        std::cout << "using target: " << targetName.value << std::endl;
    }
    
	targetName.length = strlen((char*) targetName.value);
	majorStatus = gss_import_name(&minorStatus, &targetName,
                                  //		GSS_KRB5_NT_PRINCIPAL_NAME, &gssTargetName);
    			GSS_C_NT_HOSTBASED_SERVICE, &gssTargetName);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on import_name: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
	}

    gss_cred_id_t service1_cred;
	majorStatus = gss_acquire_cred(&minorStatus, gssTargetName, 0, &mechset_krb5,
			GSS_C_INITIATE, &service1_cred, 0, 0);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on acquire_cred: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		return 1;
	}

	OM_uint32 overwriteCred = 1;
	OM_uint32 defaultCred = 0;
	gss_key_value_element_desc credElement;
	credElement.key = "bothcache";
	credElement.value = "FILE:credstore";
	gss_key_value_set_desc credStore;
	credStore.count = 1;
	credStore.elements = &credElement;

	gss_OID_set elementsStored;
	gss_cred_usage_t credUsageStored;
	majorStatus = gss_store_cred_into(&minorStatus,
                                      service1_cred,
	GSS_C_BOTH,
	GSS_C_NULL_OID, overwriteCred, defaultCred, &credStore, &elementsStored,
			&credUsageStored);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on store_cred_into: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		return 1;
	}
	std::cout << "stored " << elementsStored << " credentials" << std::endl;

	return 0;
}
