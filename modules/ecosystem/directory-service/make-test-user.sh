#!/bin/sh
#
adminPassword=$1
[ -n "${adminPassword}" ] || (echo $0: usage: $0 adminPassword 1>&2; exit -1)

docker exec saviordc samba-tool user create test Test123 \
	   -U administrator --password="${adminPassword}"
