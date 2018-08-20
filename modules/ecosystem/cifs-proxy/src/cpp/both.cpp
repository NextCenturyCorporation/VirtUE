/*
 * both.cpp
 *
 *  Created on: Aug 16, 2018
 *      Author: clong
 */

#include <iostream>

#include <gssapi/gssapi.h>

static gss_OID_desc mech_krb5 = { 9,
		(void*) "\052\206\110\206\367\022\001\002\002" };
gss_OID_set_desc mechset_krb5 = { 1, &mech_krb5 };

int main(int argc, char* argv[]) {
	unsigned int major;
	unsigned int minor;
	const gss_name_t acceptor_name = GSS_C_NO_NAME;
	gss_cred_id_t service1_cred;
	major = gss_acquire_cred(&minor, acceptor_name, 0, &mechset_krb5,
			GSS_C_BOTH, &service1_cred, 0, 0);
	if (GSS_ERROR(major)) {
		std::cerr << "error" << std::endl;
		return 1;
	}
	return 0;
}
