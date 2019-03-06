#!/bin/bash
#
# Set up and run a remote test on the CIFS proxy. Assumes that 'terraform apply' has been run.
#

getValue() {
	echo $@ | terraform console
}

[ -r test-remote-setup.sh ] || { echo $0: error: run terraform first 1>&2 ; exit -1; }

set -e

privateKey=$(getValue var.user_private_key_file)
user=$(getValue var.linux_user)
cifsIp=$(getValue aws_instance.cifs_proxy.public_dns)
scp -i "${privateKey}" test-remote-setup.sh test-remote.sh ${user}@${cifsIp}:.

ssh -i "${privateKey}" ${user}@${cifsIp} bash test-remote-setup.sh |& tee test-remote-setup.log
ssh -i "${privateKey}" ${user}@${cifsIp} bash test-remote.sh |& tee test-remote.log