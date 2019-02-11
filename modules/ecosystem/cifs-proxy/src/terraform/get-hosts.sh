#!/bin/bash
#
# print host names for servers
#
mydir=$(readlink --canonicalize "$(dirname $0)")
cd "$mydir" || exit 1
echo -ne "directory server: \t"
terraform state show aws_directory_service_directory.directory_service |  sed -n 's/dns_ip_addresses.*=\(.*\..*\)$/\1/p' | tr -d '\n'
echo
echo -ne "file server: \t"
terraform state show aws_instance.file_server | sed -n 's/public_dns *= \(.*\)/\1/p'
echo -ne "print server: \t"
terraform state show aws_instance.print_server | sed -n 's/public_dns *= \(.*\)/\1/p'
echo -ne "user server: \t"
terraform state show aws_instance.user_facing_server | sed -n 's/public_dns *= \(.*\)/\1/p'
echo
