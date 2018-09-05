#!/bin/bash
#
# print host names for servers
#
mydir=$(readlink --canonicalize $(dirname $0))
cd "$mydir"
echo -ne "file server: \t"
terraform state show aws_instance.file_server | sed -n 's/public_dns *= \(.*\)/\1/p'
echo -ne "user server: \t"
terraform state show aws_instance.user_facing_server | sed -n 's/public_dns *= \(.*\)/\1/p'
