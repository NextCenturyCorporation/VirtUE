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
