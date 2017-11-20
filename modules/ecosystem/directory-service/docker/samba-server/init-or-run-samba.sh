#!/bin/bash
#
# Run the samba server, initializing it first if necessary.
#
# Pass --init-only to just do initialization.
#

set -e

INITFILE=/var/lib/samba/private/.initialized

printHelp() {
	echo "$0: usage: $0 [--init-only] [-- <sambaArgs>]"
	echo "$0: environment variables used:"
	cat<<EOF
	SAMBA_ADMIN_PASSWORD	REQUIRED: Administrator account password
	SAMBA_DNS				DNS to forward requests to (default: 127.0.0.11 (the docker server))
	SAMBA_REALM				Domain name (e.g., SUBDOMAIN.COMPANY.COM)
	SAMBA_PROVISION_OPTIONS Extra options for the 'samba-tool domain provision' command
	SAMBA_OPTIONS			Extra options for samba (default: --interactive)
EOF
}

SAMBA_REALM=${SAMBA_REALM:-SAVIOR.NEXTCENTURY.COM}
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

	if [ -z "${SAMBA_ADMIN_PASSWORD}" ]; then
		echo "$0: error: Domain not initialized, so environment variable SAMBA_ADMIN_PASSWORD must be set" 1>&2
		exit 2
	fi
	if [ -n "${SAMBA_DNS}" ]; then
		SAMBA_DNS_OPTION="--option=dns forwarder = ${SAMBA_DNS}"
		echo "$0: info: SAMBA_DNS_OPTION="${SAMBA_DNS_OPTION}
	fi
	rm -rf /etc/samba/smb.conf /etc/krb5.conf /var/lib/samba/*
	samba-tool domain provision \
			   --server-role=dc \
			   --use-rfc2307 \
			   --dns-backend=SAMBA_INTERNAL \
			   --realm=${SAMBA_REALM} \
			   --domain=${SAMBA_REALM/.*/} \
			   --adminpass="${SAMBA_ADMIN_PASSWORD}" \
			   "${SAMBA_DNS_OPTION}" \
			   ${SAMBA_PROVISION_OPTIONS}
	cp -f /var/lib/samba/private/krb5.conf /etc

	echo "$0: initialized at $(date)" > "${INITFILE}"
	echo "$0: initialized"
else
	echo "$0: already initialized"
fi

# run
if [ $runSamba -eq 1 ]; then
	echo "$0: running Samba AD DC server"
	# need input from /dev/null, otherwise samba will exit immediately if run by docker
	exec samba $sambaArgs < /dev/null
fi
