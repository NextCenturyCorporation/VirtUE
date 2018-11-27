#!/bin/bash
#
# Create a master include file for all the samba configs for the Virtue shares
#
progname=$0

printUsage() {
	echo $progname: usage: $progname [configDir]
}

if [ $# -gt 1 ]; then
   printUsage
   exit -1
fi

if [ $# -eq 1 ]; then
	configDir="$1"
else
	configDir=virtue-shares
fi

find "$(readlink --canonicalize $configDir)" \
	 -name '*.conf' \
	 -exec echo "include = {}" \; \
	 > virtue-shares.conf
