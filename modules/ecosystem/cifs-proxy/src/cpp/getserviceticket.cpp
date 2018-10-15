/*
 * getserviceticket.cpp
 *
 *  Created on: Jul 5, 2018
 *      Author: clong
 */


#include <iostream>
#include <krb5/krb5.h>
#include <cstring>

template < typename T >
class auto_free {
public:
	typedef void (*FreeFunc)(T);
	auto_free(FreeFunc freeFunc)
	: _freeFunc(freeFunc)
	{
	}
	~auto_free() {
		_freeFunc(_value);
	}
	T& operator*() {
		return _value;
	}
private:
	FreeFunc _freeFunc;
	T _value;
};

krb5_error_code getServiceTicket(const char* inCacheFilename,
		const char* serviceName, krb5_creds* outCreds) {
	auto_free<krb5_context> context(krb5_free_context);
	krb5_error_code ret = 0;

    ret = krb5_init_context(&*context);
    if (ret) {
        return ret;
    }

    ret = krb5_cc_set_default_name(*context, inCacheFilename);
    if (ret) {
        return ret;
    }
	krb5_ccache cache;
    ret = krb5_cc_default(*context, &cache);
    if (ret) {
        return ret;
    }

	krb5_principal princ = NULL;
    ret = krb5_cc_get_principal(*context, cache, &princ);
	if (ret) {
		return ret;
	}

	krb5_creds inCreds;

    memset(&inCreds, 0, sizeof(inCreds));

	inCreds.client = princ;
    ret = krb5_parse_name(*context, serviceName, &inCreds.server);
    if (ret) {
    	krb5_free_principal(*context, princ);
    	return ret;
    }

	ret = krb5_get_credentials(*context, 0, cache, &inCreds, &outCreds);

	krb5_free_principal(*context, princ);
	krb5_free_principal(*context, inCreds.server);

	return ret;
}

int main(int argc, const char* argv[]) {
	if (argc != 3) {
		std::cerr << "error: usage: "
				<< argv[0]
				<< " <input-cache-file>"
				<< " <service-name>"
				<< std::endl;
		return -1;
	}
	int i = 1;
	const char* inCacheFilename = argv[i++];
	const char* serviceName = argv[i++];
	krb5_creds* creds = NULL;
	krb5_error_code ret;
	ret = getServiceTicket(inCacheFilename, serviceName, creds);
	if (ret) {
		std::cerr << "error getting service ticket: " << ret << std::endl;
	}
	return ret;
}
