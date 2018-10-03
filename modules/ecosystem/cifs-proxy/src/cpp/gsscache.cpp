/*
 * gsscache.cpp
 *
 *  Created on: Jul 9, 2018
 *      Author: clong
 */

#include <gssapi/gssapi.h>
#include <gssapi/gssapi_ext.h>
#include <iostream>
#include <cstring>
#include <cstdio>
#include <fstream>
#include <ios>

static gss_OID_desc mech_krb5 = { 9,
		(void*) "\052\206\110\206\367\022\001\002\002" };

void printErrors(OM_uint32 status_code) {
	OM_uint32 message_context;
	OM_uint32 maj_status;
	OM_uint32 min_status;
	gss_buffer_desc status_string;

	message_context = 0;

	do {

		maj_status = gss_display_status(&min_status, status_code,
		GSS_C_GSS_CODE,
		GSS_C_NO_OID, &message_context, &status_string);

		fprintf(stderr, "%.*s\n", (int) status_string.length,
				(char *) status_string.value);

		gss_release_buffer(&min_status, &status_string);

	} while (message_context != 0);
}

int main(int argc, char** argv) {
	unsigned int minorStatus;
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
	OM_uint32 majorStatus;

	targetName.value = (void*) "cifs@ws9.hq.nextcentury.com";
	//targetName.value = (void*) "cifs@pc-5000-cl.hq.nextcentury.com";
	targetName.length = strlen((char*) targetName.value) + 1;
	majorStatus = gss_import_name(&minorStatus, &targetName,
			GSS_C_NT_HOSTBASED_SERVICE, &gssTargetName);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on import_name: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
	}

#define INIT_NEEDED
#ifdef INIT_NEEDED
	inputToken.length = 0;
	majorStatus = gss_init_sec_context(&minorStatus, initCredHandle,
			&contextHandle, gssTargetName, kerbMechanism, reqFlags, timeReq,
			GSS_C_NO_CHANNEL_BINDINGS, &inputToken, &actualMechType,
			&outputToken, &retFlags, &timeRec);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on init_sec_context: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}

    majorStatus = gss_delete_sec_context(&minorStatus, &contextHandle, GSS_C_NO_BUFFER);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on delete_sec_context: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
    
	std::cout << "context initialized" << std::endl;
#endif
    
    //#define ACCEPT_NEEDED
#ifdef ACCEPT_NEEDED
	gss_ctx_id_t acceptContext = GSS_C_NO_CONTEXT;
	gss_name_t srcName = GSS_C_NO_NAME;
	gss_buffer_desc acceptOutToken;
	gss_cred_id_t delegatedCredHandle;
	majorStatus = gss_accept_sec_context(&minorStatus, &acceptContext,
			GSS_C_NO_CREDENTIAL, &outputToken, GSS_C_NO_CHANNEL_BINDINGS,
			&srcName, &kerbMechanism, &acceptOutToken, &retFlags, &timeRec,
			&delegatedCredHandle);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on accept_sec_context: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
    std::cout << "context accepted" << std::endl;
#endif

    //	const gss_name_t desiredName = GSS_C_NO_NAME;
	const gss_name_t desiredName = gssTargetName;
	gss_cred_usage_t credUsage = GSS_C_BOTH;
	gss_cred_id_t newCredHandle = 0;
    majorStatus = gss_add_cred(&minorStatus, GSS_C_NO_CREDENTIAL, desiredName, kerbMechanism,
                               credUsage, GSS_C_INDEFINITE, GSS_C_INDEFINITE, &newCredHandle, 0,
                               static_cast<OM_uint32*>(0),
                               static_cast<OM_uint32*>(0)
                               );
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on add_cred: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
	std::cout << "credential(s) added" << std::endl;

    //#define ACQUIRE_NEEDED 
#ifdef ACQUIRE_NEEDED 
	gss_OID_set_desc desiredMechs = { 1, kerbMechanism };
	gss_cred_id_t acquiredCredHandle = 0;
	gss_OID_set actualMechs;
	majorStatus = gss_acquire_cred(&minorStatus, desiredName, timeReq,
			GSS_C_NULL_OID_SET, GSS_C_INITIATE, &acquiredCredHandle, &actualMechs,
			&timeRec);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on acquire_cred: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
	std::cout << "credential(s) acquired" << std::endl;
	gss_name_t outName;
	unsigned int outLifetime;
	int outUsage;
	gss_OID_set outMechs;
	gss_inquire_cred(&minorStatus, acquiredCredHandle, &outName, &outLifetime,
			&outUsage, &outMechs);
#endif

    majorStatus = gss_release_name(&minorStatus, &gssTargetName);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on release_name: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}

    
	//gss_cred_id_t inputCredHandle;
	OM_uint32 overwriteCred = 1;
	OM_uint32 defaultCred = 0;
	gss_key_value_element_desc credElement;
	credElement.key = "ccache";
	credElement.value = "FILE:credstore";
	gss_key_value_set_desc credStore;
	credStore.count = 1;
	credStore.elements = &credElement;

	gss_OID_set elementsStored;
	gss_cred_usage_t credUsageStored;
	majorStatus = gss_store_cred_into(&minorStatus,
                                      //acquiredCredHandle,
                                      newCredHandle,
	GSS_C_INITIATE,
	GSS_C_NULL_OID, overwriteCred, defaultCred, &credStore, &elementsStored,
			&credUsageStored);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on store_cred_into: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
	std::cout << "stored " << elementsStored << " credentials" << std::endl;

    majorStatus = gss_release_cred(&minorStatus, &newCredHandle);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on release_cred: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}

    majorStatus = gss_release_oid_set(&minorStatus, &elementsStored);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on release_oid_set: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
    
	std::ofstream token("token", std::ios::binary);
	token.write(reinterpret_cast<const char*>(outputToken.value),
			outputToken.length);
	token.close();

    majorStatus = gss_release_buffer(&minorStatus, &outputToken);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on release_buffer: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus);
		return 1;
	}
    
	//std::cout << login(context, argv[1], argv[2]) << std::endl;

	//    gss_release_buffer(&minorStatus, &targetName);
}