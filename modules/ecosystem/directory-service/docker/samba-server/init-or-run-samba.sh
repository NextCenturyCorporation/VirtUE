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
	SAMBA_ADMIN_PASSWORD	Administrator account password
	SAMBA_REALM				Domain name (e.g., SUBDOMAIN.COMPANY.COM)
EOF
}

SAMBA_REALM=${SAMBA_REALM:-SAVIOR.NEXTCENTURY.COM}
runSamba=1
sambaArgs="--daemon"

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
	rm -rf /etc/samba/smb.conf /etc/krb5.conf /var/lib/samba/*
	samba-tool domain provision \
			   --server-role=dc \
			   --use-rfc2307 \
			   --dns-backend=SAMBA_INTERNAL \
			   --realm=${SAMBA_REALM} \
			   --domain=${SAMBA_REALM/.*/} \
			   --adminpass="${SAMBA_ADMIN_PASSWORD}" \
			   --debug 3
	echo "$0: initialized at $(date)" > "${INITFILE}"
	echo "$0: initialized"
fi

# run
if [ $runSamba -eq 1 ]; then
	samba "$sambaArgs"
fi
