/*
 * switchprincipal.cpp
 *
 * Read a Kerberos credential cache and write it out with the same
 * credentials but a different main principal. Does not support realm changes.
 *
 * Was originally written to enable mount.cifs to work with S4U2Proxy creds,
 * because the cifs.upcall helper ignores the username passed to mount and
 * always uses the principal from the cred file.
 *
 *  Created on: Oct 5, 2018
 *      Author: clong
 */

#include <iostream>
#include <getopt.h>
#include <krb5/krb5.h>
#include <cstring>

static char* progname;

void usage() {
	std::cerr << "usage: "
			<< progname
			<< " [-i inputCCache ] [-o outputCCache ] newPrincipal | -"
			<< std::endl;
}

// NOTE: this macro must evaluate RETVAL only once
#define CHECK_ERROR(RETVAL, MESSAGE, RETURN) \
{ \
	int retval = RETVAL; \
	if (retval) { \
		std::cerr << progname << ": error: " \
			<< MESSAGE << " (" << retval << ")" \
			<< std::endl; \
		return RETURN; \
	} \
}

/**
 * Get a Kerberos cert cache. If name is NULL, return the default ccache.
 */
krb5_ccache getCCache(krb5_context& context, const char* name) {
	krb5_ccache ccache;
	if (name == NULL) {
		CHECK_ERROR(krb5_cc_default(context, &ccache),
				"could not get default credential cache", NULL);
	} else {
		CHECK_ERROR(krb5_cc_resolve(context, name, &ccache),
				"could not get read cache '" << name << "'", NULL);
	}
	return ccache;
}

int main(int argc, char **argv) {
	progname = argv[0];

	char opt;
	char* inputCCacheName = NULL;
	char* outputCCacheName = NULL;

	while ((opt = getopt(argc, argv, "i:o:h")) != -1) {
		switch (opt) {
		case 'i':
			inputCCacheName = optarg;
			break;
		case 'o':
			outputCCacheName = optarg;
			break;
		case 'h':
			usage();
			return 0;
		case '?':
			usage();
			return -1;
		default:
			std::cerr << progname
					<< ": error: internal error on option '"
					<< optarg
					<< "'"
					<< std::endl;
			usage();
			return -1;
		}
	}

	if (optind >= argc) {
		std::cerr << progname
				<< ": error: newPrincipal is required"
				<< std::endl;
		usage();
		return -1;
	}
	char* newPrincipalName = argv[optind];

	krb5_context context;
	CHECK_ERROR(krb5_init_context(&context),
			"could not initialize Kerberos context", 1);

	krb5_ccache inCCache = getCCache(context, inputCCacheName);

	// get the original principal just to get the realm
	krb5_principal origPrincipal;
	CHECK_ERROR(krb5_cc_get_principal(context, inCCache, &origPrincipal),
			"could not determine principal from input cache", 2);

	krb5_principal newPrincipal;
    if (strcmp(newPrincipalName, "-") != 0) {
        CHECK_ERROR(
                    krb5_build_principal(context, &newPrincipal, origPrincipal->realm.length,
                                         origPrincipal->realm.data, newPrincipalName, NULL),
                    "could not make new principal named '"
					<< newPrincipalName
					<< "' (maybe bad principal name format)",
                    3);
        krb5_free_principal(context, origPrincipal);
    }
    else {
        newPrincipal = origPrincipal;
    }

	// use a temp in case the input & output are the same
	krb5_ccache tempCCache;
	CHECK_ERROR(krb5_cc_new_unique(context, "MEMORY", NULL, &tempCCache),
			"could not create temporary cred cache", 8);
	CHECK_ERROR(krb5_cc_initialize(context, tempCCache, newPrincipal),
			"could not initialize temporary cred cache", 7);

	CHECK_ERROR(krb5_cc_copy_creds(context, inCCache, tempCCache),
			"could not copy credentials", 5);


	krb5_ccache outCCache;
	if (outputCCacheName != NULL) {
		CHECK_ERROR(krb5_cc_set_default_name(context, outputCCacheName),
				"could not set output name (maybe bad name format)", 6);
	}
	krb5_cc_default(context, &outCCache);

	CHECK_ERROR(krb5_cc_initialize(context, outCCache, newPrincipal),
			"could not initialize new credential context", 4);

	CHECK_ERROR(krb5_cc_copy_creds(context, tempCCache, outCCache),
			"could not copy credentials", 5);

	return 0;
}

