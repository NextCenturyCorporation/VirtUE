#!/bin/bash
#
# (Re)Deploy the cifs jar file
#

getValue() {
	echo $@ | terraform console
}

set -e

privateKey=$(getValue var.user_private_key_file)
user=$(getValue var.linux_user)
cifsHost=$(getValue aws_instance.cifs_proxy.public_dns)
jar=$(getValue var.proxy_jar)
jarDest=$(getValue var.proxy_jar_dest)
make -j2 proxy-jar
scp -i "${privateKey}" "${jar}" ${user}@${cifsHost}:/tmp
ssh -i "${privateKey}" ${user}@${cifsHost} sudo mv "/tmp/$(basename ${jar})" "${jarDest}"
echo deployed to ${cifsHost} at ${remoteJar}
