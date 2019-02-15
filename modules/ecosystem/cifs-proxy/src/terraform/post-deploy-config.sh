#!/bin/bash
#
# Configure the computer to be part of a domain
#

progname="$0"

# some helpers and error handling:
info() { printf "\n%s %s\n\n" "$( date )" "$*" >&2; }
error() { printf "%s: %s: error: %s\n" "$( date )" "$progname" "$*" >&2; }
usage() {
	echo "${progname}: usage: ${progname} OPTIONS [--pretend] [--verbose]"
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
cat > /etc/netplan/99-virtue.yaml <<EOF
network:
    version: 2
    ethernets:
        eth0:
          nameservers:
            addresses: [${dcip}]
            search: [${domain}]
          dhcp4-overrides:
            use-dns: false
EOF
netplan apply

sudo sed -i '/preserve_hostname: false/c\preserve_hostname: true' /etc/cloud/cloud.cfg
hostnamectl set-hostname $hostname
domainname $domain
sed -i "s/\(^127\.0\.0\.1 *\)/\1${hostname}.${domain} ${hostname} /" /etc/hosts

# join the domain
echo "${domainAdminPassword}" | \
	realm join \
		  --membership-software=samba \
		  --user-principal "http/${hostname}.${domain}@${domain^^}" \
		  --user ${domainAdmin} \
		  ${domain}
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
mkdir --parents /etc/krb5.conf.d
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
# savior.conf should be enough, but the CIFS Proxy has problems if default_realm isn't set in krb5.conf itself
sed -i -e "s/\( *default_realm =\).*/\1 ${domain^^}/" /etc/krb5.conf
