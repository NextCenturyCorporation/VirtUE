#!/bin/bash
#
# Run the samba server, initializing it first if necessary.
#
# Pass --init-only to just do initialization.
#

set -e

SAMBA_CONFIG_DIR="${SAMBA_CONFIG_DIR:-/var/lib/samba}"
INITFILE="${SAMBA_CONFIG_DIR}/private/.initialized"

printHelp() {
	echo "$0: usage: $0 [--init-only] [-- <sambaArgs>]"
	echo "$0: environment variables used:"
	cat<<EOF
	SAMBA_ADMIN_PASSWORD	REQUIRED: Administrator account password
	SAMBA_REALM				REQUIRED: Domain name (e.g., SUBDOMAIN.COMPANY.COM)
	SAMBA_DNS				DNS to forward requests to (default: 127.0.0.11 (the docker server))
	SAMBA_CONFIG_DIR		Where samba config files live (default: /var/lib/samba)
	SAMBA_PROVISION_OPTIONS Extra options for the 'samba-tool domain provision' command
	SAMBA_OPTIONS			Extra options for samba (default: --interactive)
EOF
}

SAMBA_DNS=${SAMBA_DNS:-127.0.0.11}
SAMBA_PROVISION_OPTIONS=${SAMBA_PROVISION_OPTIONS:-}
sambaArgs=${SAMBA_OPTIONS:---interactive}
runSamba=1

case "$1" in
	--init-only) runSamba=0 
				 ;;
	-h|--help) printHelp
			   exit 0
			   ;;
	--) shift
		sambaArgs="${@}"
		break
		;;
	"") echo "$0: info: ignoring zero-length argument"
		;;
	*) echo "$0: error: unknown argument '$1'" 1>&2
	   printHelp 1>&2
	   exit 1
	   ;;
esac

# initialize the system if it's not already
if [ ! -f "${INITFILE}" ]; then
	echo "$0: initializing..."

	fatalError=0
	if [ -z "${SAMBA_ADMIN_PASSWORD}" ]; then
		echo "$0: error: Domain not initialized, so environment variable SAMBA_ADMIN_PASSWORD must be set" 1>&2
		fatalError=1
	fi
	if [ -z "${SAMBA_REALM}" ]; then
		echo "$0: error: Domain not initialized, so environment variable SAMBA_REALM must be set" 1>&2
		fatalError=1
	fi
	if [ 1 -eq $fatalError ]; then
		exit 2
	fi

	if [ -n "${SAMBA_DNS}" ]; then
		SAMBA_DNS_OPTION="--option=dns forwarder = ${SAMBA_DNS}"
		echo "$0: info: SAMBA_DNS_OPTION="${SAMBA_DNS_OPTION}
	fi
	rm -rfv /etc/samba/smb.conf /etc/krb5.conf "${SAMBA_CONFIG_DIR}"/*
	# TODO: generate a certificate for LDAPS and change "ldap server
	# require strong auth" to "yes".
	samba-tool domain provision \
			   --server-role=dc \
			   --use-rfc2307 \
			   --dns-backend=SAMBA_INTERNAL \
			   --targetdir="${SAMBA_CONFIG_DIR}" \
			   --realm=${SAMBA_REALM} \
			   --domain=${SAMBA_REALM/.*/} \
			   --adminpass="${SAMBA_ADMIN_PASSWORD}" \
			   --option="server schannel = yes" \
			   --option="kerberos method = secrets and keytab" \
			   --option="lm interval = 0" \
			   --option="server signing = mandatory" \
			   --option="show add printer wizard = no" \
			   --option="ldap server require strong auth = no" \
			   "${SAMBA_DNS_OPTION}" \
			   ${SAMBA_PROVISION_OPTIONS}
	echo "$0: initialized at $(date)" > "${INITFILE}"
	echo "$0: initialized"
else
	echo "$0: already initialized"
fi

[ -e /etc/krb5.conf ] && echo "$0: kerberos config krb5.conf already exists, overwriting"
cp -f --backup=existing /var/lib/samba/private/krb5.conf /etc

[ -e /var/kerberos/krb5kdc/kdc.conf ] && echo "$0: kerberos config kdc.conf already exists, overwriting"
cp -f --backup=existing /var/lib/samba/private/kdc.conf /var/kerberos/krb5kdc

# run
if [ $runSamba -eq 1 ]; then
	echo "$0: running Samba AD DC server"
	# need input from /dev/null, otherwise samba will exit immediately if run by docker
	exec samba \
		 --configfile="${SAMBA_CONFIG_DIR}"/etc/smb.conf \
		 $sambaArgs \
		 < /dev/null
fi
