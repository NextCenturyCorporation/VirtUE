#!/bin/bash
#
# Register a service with the DS and make a kerberos keytab for it
#

usage="usage: $0 container network adminPassword keytabPath"

if [ ! $# -eq 4 ]; then
	echo "$0: error: invalid arguments" 1>&2
	echo "$0: ${usage}" 1>&2
	exit -1
fi

container=$1
network=$2
adminPassword=$3
keytabPath=$4

set -e

SAMBA_CONFIG_DIR=${SAMBA_CONFIG_DIR:-/var/lib/samba}
ADDC_CONTAINER=${ADDC_CONTAINER:-saviordc}
SAMBA_DOMAIN=${SAMBA_DOMAIN:-SAVIOR.NEXTCENTURY.COM}
realm=${SAMBA_DOMAIN/.*/}

#
# Set up DNS
#
serviceIp=$(docker inspect --format "{{.NetworkSettings.Networks.${network}.IPAddress}}" $container)
addcHostname=$(docker inspect --format '{{.Config.Hostname}}' $ADDC_CONTAINER)
serviceSubnet=$(echo $serviceIp | sed 's/\.[^.]*$//')
zone=$(echo $serviceSubnet | awk -F . '{ print $3 "." $2 "." $1 }').in-addr.arpa
lastOctet=${serviceIp/*./}
serviceHostname=$(docker inspect --format '{{.Config.Hostname}}' $container)
serviceFqdn=${serviceHostname}.${SAMBA_DOMAIN,,}

# if the zone doesn't exist, create it
docker exec $ADDC_CONTAINER samba-tool dns zoneinfo $addcHostname $zone \
	   -U administrator --password="${adminPassword}" \
	   >& /dev/null || \
	docker exec $ADDC_CONTAINER samba-tool dns zonecreate $addcHostname $zone \
		   -U administrator --password="${adminPassword}"
# remove reverse DNS PTR record (if any)
docker exec $ADDC_CONTAINER \
	   samba-tool dns delete $addcHostname $zone $lastOctet PTR ${serviceFqdn} \
	   -U administrator --password="${adminPassword}" \
	   >& /dev/null || true
# add new reverse DNS PTR record
docker exec $ADDC_CONTAINER \
	   samba-tool dns add $addcHostname $zone $lastOctet PTR ${serviceFqdn} \
	   -U administrator --password="${adminPassword}"
	   
#
# create the service account
#
# nuke any existing service account
docker exec $ADDC_CONTAINER \
	   samba-tool user delete $serviceHostname \
	   -U administrator --password="${adminPassword}" \
	   >& /dev/null || true

docker exec $ADDC_CONTAINER \
	   samba-tool user create --random-password $serviceHostname \
	   -U administrator --password="${adminPassword}"
docker exec $ADDC_CONTAINER \
	   samba-tool spn add HTTP/${serviceFqdn} ${serviceHostname} \
	   -U administrator --password="${adminPassword}"

#
# Make a keytab
#
# There's a weird bug with samba-tool exportkeytab so it can fail if
# the filename is mixed case (e.g., "/tmp/tmp.hD2HIEoZOZ" fails), so
# we'll make up a filename (instead of using mktemp).
newKeytab=/tmp/${container}.${$}.keytab
# Note: this samba-tool command needs the configfile option although
# previous ones did not
docker exec $ADDC_CONTAINER \
	   samba-tool domain exportkeytab "$newKeytab" \
	   --principal=HTTP/${serviceFqdn} \
	   --configfile="${SAMBA_CONFIG_DIR}/etc/smb.conf" \
	   -U administrator --password="${adminPassword}"

# copy the keytab to the service container
tempKeytab=$(mktemp)
docker cp "${ADDC_CONTAINER}:${newKeytab}" "${tempKeytab}"
docker cp "${tempKeytab}" "${container}:${keytabPath}"
docker exec $ADDC_CONTAINER rm "$newKeytab"
rm "${tempKeytab}"
