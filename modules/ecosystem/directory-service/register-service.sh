#!/bin/bash
#
# Copyright (C) 2019 Next Century Corporation
# 
# This file may be redistributed and/or modified under either the GPL
# 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
# granted government purpose rights. For details, see the COPYRIGHT.TXT
# file at the root of this project.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
# 
# SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
#
#
# Register a service with the DS and make a kerberos keytab for it
#

usage="usage: $0 container network adminPassword keytabPath"

function error() {
	retval=$1
	shift
	echo -e "$0: error: $@" 1>&2
	exit $retval
}

if [ ! $# -eq 4 ]; then
	error -1 "invalid arguments\n\t${usage}"
fi

container=$1
network=$2
adminPassword=$3
keytabPath=$4

set -e

SAMBA_CONFIG_DIR=${SAMBA_CONFIG_DIR:-/var/lib/samba}
ADDC_CONTAINER=${ADDC_CONTAINER:-saviordc}
SAMBA_DOMAIN=$network
realm=${SAMBA_DOMAIN/.*/}

#
# Set up DNS
#
serviceIp=$(docker inspect --format "{{(index .NetworkSettings.Networks \"${network}\").IPAddress}}" $container)
[ "<no value>" == "$serviceIp" ] && error 1 "Could not find IP address for container '$container' in network '$network'"
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
	   samba-tool user delete http-${serviceHostname} \
	   -U administrator --password="${adminPassword}" \
	   >& /dev/null || true

docker exec $ADDC_CONTAINER \
	   samba-tool user create --random-password http-${serviceHostname} \
	   -U administrator --password="${adminPassword}"

# nuke any existing service name
docker exec $ADDC_CONTAINER \
	   samba-tool spn delete HTTP/${serviceFqdn} \
	   -U administrator --password="${adminPassword}" >& /dev/null || true
docker exec $ADDC_CONTAINER \
	   samba-tool spn add HTTP/${serviceFqdn} http-${serviceHostname} \
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
	   --principal=HTTP/${serviceFqdn}@${SAMBA_DOMAIN} \
	   --configfile="${SAMBA_CONFIG_DIR}/etc/smb.conf" \
	   -U administrator --password="${adminPassword}"

# copy the keytab to the service container
tempKeytab=$(mktemp)
docker cp "${ADDC_CONTAINER}:${newKeytab}" "${tempKeytab}"
docker exec $container mkdir -p "$(dirname ${keytabPath})"
docker cp "${tempKeytab}" "${container}:${keytabPath}"
docker exec $ADDC_CONTAINER rm "$newKeytab"
rm "${tempKeytab}"
