#!/bin/bash
#
# Allow Kerberos constrained delegation, for example:
#
# allow-delegation.sh --domain thingy.com --admin Admin --password NOTREALPWD --delegater cifsproxy --target fileserver
#

progname="$0"

# some helpers and error handling:
info() { printf "\n%s %s\n\n" "$( date )" "$*" >&2; }
error() { printf "%s: %s: error: %s\n" "$( date )" "$progname" "$*" >&2; }
usage() {
	echo "${progname}: usage: ${progname} OPTIONS [--verbose]"
	echo -e "\t--domain DOMAIN"
	echo -e "\t--admin ADMIN_USER [default=Administrator]"
	echo -e	"\t--password ADMIN_PASSWORD"
	echo -e "\t--delegater DELEGATER_HOST"
	echo -e "\t--target TARGET_HOST"
	echo -e "\t--dc DOMAIN_CONTROLLER [default=DOMAIN]"
}

domain=''
domainAdmin=Administrator
domainAdminPassword=''
delegater=''
target=''
dc=''

while [ $# -gt 0 ]; do
	case "$1" in
		--domain) domain="$2"
				  shift
				  ;;
		--admin*) domainAdmin="$2"
				  shift
				  ;;
		--password) domainAdminPassword="$2"
					shift
					;;
		--delegater) delegater="$2"
					 shift
					 ;;
		--target) target="$2"
				  shift
				  ;;
		--dc) dc="$2"
			  shift
			  ;;
		--verbose) verbose=1
				   ;;
		--help|-h) usage
				   exit 0
				   ;;
		*) error unknown option "'$1'"
		   usage >&2
		   exit 1
		   ;;
	esac
	shift
done

mustBeSet() { [ -z "$1" ] && (error "$2" must be specified; usage) >&2 && exit 2; }
mustBeSet "$domain" DOMAIN
mustBeSet "$domainAdmin" ADMIN_USER
mustBeSet "$domainAdminPassword" ADMIN_PASSWORD
mustBeSet "$delegater" DELEGATER_HOST
mustBeSet "$target" TARGET_HOST
if [ -z "$dc" ]; then
	dc=$domain
fi

[ $verbose -eq 1 ] && set -x

ldapfile=/tmp/ldap-$$
domainPrefix=${domain/.*}
domainparts="${domain//./,dc=}"
[ $verbose -eq 1 ] && ldapsearch -w "${domainAdminPassword}" -x -H ldap://${dc} -D "${domainAdmin}@${domain}" -b "OU=Computers,OU=${domainPrefix},DC=${domainparts}" cn="${delegater}"
cat > $ldapfile <<EOLDAP
dn: cn=${delegater},ou=Computers,ou=${domainPrefix},dc=${domainparts}
changetype: modify
add: msDS-AllowedToDelegateTo
msDS-AllowedToDelegateTo: cifs/${target^^}
msDS-AllowedToDelegateTo: cifs/${target}.${domain}
-
EOLDAP
ldapmodify -f $ldapfile -w "${domainAdminPassword}" -x -H ldap://${dc} \
		   -D "${domainAdmin}@${domain}"
retval=$?
# 20 is "Type or value exists", which happens if the target was
# already in the delegation list, which is ok. (There doesn't seem to
# be a way to tell ldapmodify to ignore that "error", so we'll ignore
# it here.)
if [ $retval -eq 0 ] || [ $retval -eq 20 ]; then
	rm -f $ldapfile
else
	exit $retval
fi
