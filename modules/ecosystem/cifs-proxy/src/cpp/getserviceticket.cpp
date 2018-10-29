/*
 * getserviceticket.cpp
 *
 * This is an attempt at partially replicating the kvno(1) program. It
 * attempts to get a service ticket for the specified service and adds
 * it to the specified file. The user may also be specified, otherwise
 * the current Kerberos user is used.
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

static void error(const char* message) {
    std::cerr << message << std::endl;
}

krb5_error_code getServiceTicket(const char* inCacheFilename,
                                 const char* serviceName, const char* user,
                                 krb5_creds* outCreds) {
	auto_free<krb5_context> context(krb5_free_context);
	krb5_error_code ret = 0;

    ret = krb5_init_context(&*context);
    if (ret) {
        error("krb5_init_context failed");
        return ret;
    }

    ret = krb5_cc_set_default_name(*context, inCacheFilename);
    if (ret) {
        error("krb5_cc_set_default_name failed");
        return ret;
    }
	krb5_ccache cache;
    ret = krb5_cc_default(*context, &cache);
    if (ret) {
        error("krb5_cc_default failed");
        return ret;
    }

	krb5_principal princ = NULL;
    if (user == NULL) {
        ret = krb5_cc_get_principal(*context, cache, &princ);
        if (ret) {
            error("krb5_cc_get_principal failed");
            return ret;
        }
    }
    else {
        ret = krb5_parse_name(*context, user, &princ);
        if (ret) {
            error("krb5_parse_name (user) failed");
            return ret;
        }
    }

	krb5_creds inCreds;

    memset(&inCreds, 0, sizeof(inCreds));

	inCreds.client = princ;
    ret = krb5_parse_name(*context, serviceName, &inCreds.server);
    if (ret) {
        error("krb5_parse_name (service) failed");
    	krb5_free_principal(*context, princ);
    	return ret;
    }

	ret = krb5_get_credentials(*context, 0, cache, &inCreds, &outCreds);
    if (ret) {
        error("krb5_get_credentials failed");
    }

	krb5_free_principal(*context, princ);
	krb5_free_principal(*context, inCreds.server);

	return ret;
}

int main(int argc, const char* argv[]) {
	if (argc < 3 || argc > 4) {
		std::cerr << "error: usage: "
				<< argv[0]
				<< " <input-cache-file>"
				<< " <service-name>"
                  << " [user]"
				<< std::endl;
		return -1;
	}
	int i = 1;
	const char* inCacheFilename = argv[i++];
	const char* serviceName = argv[i++];
    const char* user;
    if (i < argc) {
        user = argv[i++];
    }
    else {
        user = NULL;
    }
	krb5_creds* creds = NULL;
	krb5_error_code ret;
	ret = getServiceTicket(inCacheFilename, serviceName, user, creds);
	if (ret) {
		std::cerr << "error getting service ticket: " << ret << std::endl;
	}
	return ret;
}
