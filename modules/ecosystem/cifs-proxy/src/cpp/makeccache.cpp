#include <iostream>
#include <krb5/krb5.h>
#include <cstring>

krb5_error_code login(krb5_context context, const char* princname, const char* password) {
    krb5_error_code ret;
    krb5_creds creds;
    krb5_principal client_princ = NULL;

    memset(&creds, 0, sizeof(creds));
    ret = krb5_parse_name(context, princname, &client_princ);
    if (ret)
        goto cleanup;
    ret = krb5_get_init_creds_password(context, &creds, client_princ,
                                       password, NULL, NULL, 0, NULL, NULL);
    if (ret)
        goto cleanup;
    ret = krb5_verify_init_creds(context, &creds, NULL, NULL, NULL, NULL);

 cleanup:
    krb5_free_principal(context, client_princ);
    krb5_free_cred_contents(context, &creds);
    return ret;
}

int main(int argc, char** argv) {
    krb5_context context;
    krb5_init_context(&context);
    
    std::cout << "Hello, world!" << std::endl;
    std::cout << login(context, argv[1], argv[2]) << std::endl;
}
