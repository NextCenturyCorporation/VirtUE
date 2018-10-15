/*
 * both.cpp
 *
 *  Created on: Aug 16, 2018
 *      Author: clong
 */

// an incantation that works on webserver:
// env KRB5_CLIENT_KTNAME=/etc/krb5.keytab KRB5_TRACE=/dev/stdout ./both http@webserver.test.savior
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

gss_name_t importName(const char* name, gss_OID nameType =
		GSS_C_NT_HOSTBASED_SERVICE) {
	unsigned int majorStatus;
	unsigned int minorStatus;
	gss_buffer_desc targetName = { strlen(name), const_cast<char*>(name) };
	gss_name_t result;

	majorStatus = gss_import_name(&minorStatus, &targetName, nameType, &result);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on import_name: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		result = 0;
	}
	return result;
}

int main(int argc, char* argv[]) {
	unsigned int majorStatus;
	unsigned int minorStatus;
	const gss_name_t acceptor_name = GSS_C_NO_NAME;
	gss_cred_id_t initCredHandle = GSS_C_NO_CREDENTIAL;
	const char* principalName;
	const char* serviceName;
	gss_name_t gssPrincipalName;
	gss_name_t gssServiceName;
	gss_OID kerbMechanism = &mech_krb5;
	OM_uint32 reqFlags = 0; // GSS_C_DELEG_FLAG
	OM_uint32 timeReq = 0;
	gss_OID actualMechType = 0;
	unsigned int retFlags;
	unsigned int timeRec;

	if (argc > 1) {
		principalName = argv[1];
	} else {
		//	principalName.value = (void*) "cifs@ws9.hq.nextcentury.com";
		//targetName.value = (void*) "cifs@pc-5000-cl.hq.nextcentury.com";
		principalName = "PC-5000-CL$@HQ.NEXTCENTURY.COM";
		std::cout << "using principal: " << principalName << std::endl;
	}

	if (argc > 2) {
		serviceName = argv[2];
	} else {
		serviceName = "cifs@ws9.hq.nextcentury.com";
		std::cout << "using service: " << serviceName << std::endl;
	}

	gssPrincipalName = importName(principalName);
	gssServiceName = importName(serviceName);

	gss_cred_id_t serviceCred;
	std::cout << ">>about to call acquire_cred" << std::endl;
	majorStatus = gss_acquire_cred(&minorStatus, gssPrincipalName, 0,
			&mechset_krb5,
			GSS_C_BOTH, &serviceCred, 0, 0);
	std::cout << "<<back from acquire_cred" << std::endl;
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on acquire_cred: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		return 1;
	}

#define INIT_NEEDED
#ifdef INIT_NEEDED
	gss_buffer_desc inputToken;
	gss_buffer_desc outputToken = { 0, 0 };
	gss_ctx_id_t contextHandle = GSS_C_NO_CONTEXT;

	inputToken.length = 0;
	std::cout << ">>about to call init_sec_context" << std::endl;
	majorStatus = gss_init_sec_context(&minorStatus, GSS_C_NO_CREDENTIAL,
			&contextHandle, gssServiceName, kerbMechanism, reqFlags, timeReq,
			GSS_C_NO_CHANNEL_BINDINGS, &inputToken, &actualMechType,
			&outputToken, &retFlags, &timeRec);
	std::cout << "<<back from init_sec_context" << std::endl;
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on init_sec_context: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		return 1;
	}

	majorStatus = gss_delete_sec_context(&minorStatus, &contextHandle,
			GSS_C_NO_BUFFER);
	if (GSS_ERROR(majorStatus)) {
		std::cerr << "error on delete_sec_context: "
				<< majorStatus
				<< "."
				<< minorStatus
				<< std::endl;
		printErrors(majorStatus, minorStatus);
		return 1;
	}

	std::cout << "context initialized" << std::endl;
#endif

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
	std::cout << ">>about to call store_cred_into" << std::endl;
	majorStatus = gss_store_cred_into(&minorStatus, serviceCred, GSS_C_BOTH,
			GSS_C_NULL_OID, overwriteCred, defaultCred, &credStore,
			&elementsStored, &credUsageStored);
	std::cout << "<<back from store_cred_into" << std::endl;
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
