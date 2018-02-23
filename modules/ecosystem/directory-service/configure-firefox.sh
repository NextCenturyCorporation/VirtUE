#!/bin/bash
#
# Configure firefox to allow Negotiated authentication with the saviorvc.
#

usage="usage: $0 firefoxContainer saviorvcContainer saviorNetwork"

function error() {
	retval=$1
	shift
	echo -e "$0: error: $@" 1>&2
	exit $retval
}

if [ ! $# -eq 3 ]; then
	error -1 "invalid arguments\n\t${usage}"
fi

ffContainer=$1
saviorvcContainer=$2
saviorNetwork=$3

set -e

saviorvcIp=$(docker inspect --format "{{(index .NetworkSettings.Networks \"${saviorNetwork}\").IPAddress}}" ${saviorvcContainer})

docker exec -i "${ffContainer}" bash -c 'cat > /usr/local/lib/firefox/defaults/pref/autoconfig-savior.js' <<EOF
// Autoconfig Firefox for SAVIOR
pref("general.config.filename", "mozilla-savior.cfg");
pref("general.config.obscure_value", 0);
EOF

docker exec -i "${ffContainer}" bash -c 'cat > /usr/local/lib/firefox/mozilla-savior.cfg' <<EOF
// Firefox preferences for SAVIOR
defaultPref("network.negotiate-auth.trusted-uris", "${saviorvcIp}");
EOF
