#!/bin/bash
#
# Configure the computer to be part of a domain
#

progname="$0"

# some helpers and error handling:
info() { printf "\n%s %s\n\n" "$( date )" "$*" >&2; }
error() { printf "%s: %s: error: %s\n" "$( date )" "$progname" "$*" >&2; }
usage() {
	echo "${progname}: usage: ${progname} OPTIONS [--pretend]"
	echo -e "\t--domain DOMAIN"
	echo -e "\t--admin ADMIN_USER [default=Administrator]"
	echo -e	"\t--password ADMIN_PASSWORD"
	echo -e "\t--hostname HOSTNAME"
	echo -e "\t--dcip DOMAIN_CONTROLLER_IP"
}

domain=''
domainAdmin=Administrator
domainAdminPassword=''
hostname=''
# domain controller IP address
dcip=''
pretend=0
verbose=0

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
		--hostname) hostname="$2"
					shift
					;;
		--dcip) dcip="$2"
				shift
				;;
		--pretend) pretend=1
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
mustBeSet "$hostname" HOSTNAME
mustBeSet "$dcip" DOMAIN_CONTROLLER_IP

[ $pretend -eq 1 ] && exit 0

set -e

[ $verbose -eq 1 ] && set -x

# set hostname and make DHCP resolve against the DC
hostnamectl set-hostname $hostname.$domain
sed -i "s/\(^127\.0\.0\.1 *\)/\1${hostname}.${domain} ${hostname} /" /etc/hosts
(echo supersede domain-name-servers "${dcip}" ';'
echo supersede domain-search \"${domain}\";
echo supersede domain-name \"${domain}\";
) >> /etc/dhcp/dhclient.conf
systemctl restart network.service

# join the domain
echo "${domainAdminPassword}" | realm join --membership-software=samba --user ${domainAdmin} ${domain}
# Making minimal smb.conf so net ads keytab works
domainPrefix=${domain/.*}
cp /etc/samba/smb.conf /etc/samba/smb.conf-orig
(
echo security = user
echo realm = ${domain}
echo workgroup = "${domainPrefix}"
echo kerberos method = secrets and keytab
echo include = /etc/samba/virtue.conf
) | sed -i -e '/^\[global\]$/r /dev/stdin' \
    -e '/ *\(security\|realm\|workgroup\|kerberos method\) *=/d' \
	-e '/ *printing *=/,$d' \
    /etc/samba/smb.conf
echo "${domainAdminPassword}" | net -k ads keytab flush -U ${domainAdmin}
echo "${domainAdminPassword}" | net -k ads keytab add http -U ${domainAdmin}
echo "${domainAdminPassword}" | net -k ads keytab add HTTP -U ${domainAdmin}

# Configuring Kerberos for our domain
cat > /etc/krb5.conf.d/savior.conf <<EOCONF
[libdefaults]
	default_realm = ${domain^^}
	dns_lookup_realm = false
	dns_lookup_kdc = true
	forwardable = true
	proxiable = true
	default_keytab_name = FILE:/etc/krb5.keytab

[realms]
	${domain^^} = {
		kdc = ${domain}
		admin_server = ${domain}
	}
EOCONF

# fixing reverse dns
myaddress=$(ip address show dev eth0 | sed -n 's_ *inet \([^/]*\)/.*_\1_p')
echo "${domainAdminPassword}" | kinit ${domainAdmin}
# if the zone doesn't exist, create it
mysubnet=$(echo $myaddress | sed 's/\.[^.]*$//')
zone=$(echo $mysubnet | awk -F . '{ print $3 "." $2 "." $1 }').in-addr.arpa
samba-tool dns zoneinfo ${dcip} $zone \
	   --kerberos=1 \
	   >& /dev/null || \
	samba-tool dns zonecreate ${dcip} $zone \
		   --kerberos=1

# remove reverse DNS PTR record (if any)
lastOctet=${myaddress/*./}
samba-tool dns delete ${dcip} $zone $lastOctet PTR ${hostname}.${domain} \
	   --kerberos=1 \
	   >& /dev/null || true
# add new reverse DNS PTR record
samba-tool dns add ${dcip} $zone $lastOctet PTR ${hostname}.${domain} \
	   --kerberos=1

# set userPrincipalName
ldapfile=/tmp/ldap-$$
domainparts=${domain//./,dc=}
cat > $ldapfile <<EOLDAP
dn: cn=${hostname},cn=Computers,dc=$domainparts
changetype: modify
replace: userPrincipalName
userPrincipalName: http/${hostname}.${domain}@${domain^^}
-
EOLDAP
ldapmodify -f $ldapfile -w "${domainAdminPassword}" -x -H ldap://${dcip} \
		   -D "${domainAdmin}@${domain}" \
	&& rm -f $ldapfile
