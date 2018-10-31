#!/bin/bash
#
# /etc/samba/virtue/<virtue>/<share>.conf
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
	configDir=$1
else
	configDir=virtue-shares
fi

for conffile in $(find $configDir -name '*.conf') ; do
	echo "include = $conffile"
done > virtue-shares.conf
