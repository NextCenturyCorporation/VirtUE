/*
 * getserviceticket.cpp
 *
 *  Created on: Jul 5, 2018
 *      Author: clong
 */


#include <iostream>
#include <krb5/krb5.h>
#include <cstring>

krb5_error_code getServiceTicket(const char* inCacheFilename,
		const char* serviceName, const char* outCacheFilename) {
	krb5_context context;
	krb5_error_code ret = 0;

    ret = krb5_init_context(&context);
    if (ret) {
        return ret;
    }

    ret = krb5_cc_set_default_name(context, inCacheFilename);
    if (ret) {
        return ret;
    }
	krb5_ccache cache;
	krb5_principal princ;

    ret = krb5_cc_get_principal(context, cache, &princ);
    krb5_cc_cursor cur;
	ret = krb5_cc_start_seq_get(context, cache, &cur);
	if (ret) {
		return ret;
	}
	krb5_creds creds;
	while ((ret = krb5_cc_next_cred(context, cache, &cur, &creds)) == 0) {
		krb5_creds* outCreds;
		krb5_get_credentials(context, 0, cache, &creds, &outCreds);
		krb5_free_cred_contents(context, &creds);
	}
	krb5_free_principal(context, princ);

	if (ret == KRB5_CC_END) {
		ret = krb5_cc_end_seq_get(context, cache, &cur);
	}

	return ret;
}

int main(int argc, const char* argv[]) {
	if (argc != 4) {
		std::cerr << "error: usage: "
				<< argv[0]
				<< " <input-cache-file>"
				<< " <service-name>"
				<< " <output-cache-file>"
				<< std::endl;
		return -1;
	}
	int i = 1;
	const char* inCacheFilename = argv[i++];
	const char* serviceName = argv[i++];
	const char* outCacheFilename = argv[i++];

	getServiceTicket(inCacheFilename, serviceName, outCacheFilename);

	return 0;
}
