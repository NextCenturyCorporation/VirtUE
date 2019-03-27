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
# CIFS Proxy
#

locals {
  myname = "cifs-proxy"
}

resource "aws_instance" "cifs_proxy" {
  ami           = "${var.linux_ami}"
  instance_type = "${var.linux_instance_type}"
  key_name      = "vrtu"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${local.myname}"
	Owner = "${data.external.local_user.result.user}"
	class = "cifs proxy"
	automated = "terraform"
  }
  lifecycle {
	prevent_destroy = false
  }

  user_data = <<EOF
#!/bin/bash
set -x
exec > /var/log/user_data.log 2>&1
date
# prevent questions about kerberos configuration (it'll get set by post-deploy-config.sh)
export DEBIAN_FRONTEND=noninteractive
apt-get update && \
apt-get -y --with-new-pkgs upgrade && \
apt-get -y install \
	adcli \
	auth-client-config \
	cifs-utils \
	keyutils \
	krb5-user \
	libnss-sss \
	libpam-ccreds \
	libpam-krb5 \
	libpam-sss \
	openjdk-8-jdk-headless \
	packagekit \
	python3-netifaces \
	realmd \
	samba \
	samba-dsdb-modules \
	smbclient \
	sssd \
	sssd-tools

sed -i 's/^\(\[libdefaults\]\)/\1\n  rdns = false/' /etc/krb5.conf

# create user that will mount files
useradd --shell /bin/false --no-create-home mounter

date
touch /tmp/user_data-finished
EOF
  # end of user_data

  connection {
	# Normal (on-create) connection works w/o specifying host here,
	# but on-destroy ssh connection fails with an empty host if host
	# is not specified. Weird. (see
	# https://github.com/hashicorp/terraform/issues/15219)
	host = "${self.public_ip}"
	type = "ssh"
	user = "${var.linux_user}"
	private_key = "${file(var.user_private_key_file)}"
  }
  
  # custom config file for samba
  provisioner "file" {
	source = "virtue.conf"
	destination = "/tmp/virtue.conf"
  }

  # the CIFS Proxy jar file
  provisioner "file" {
	source = "${var.proxy_jar}"
	destination = "/tmp/${basename(var.proxy_jar)}"
  }

  #
  # Helper programs
  #
  provisioner "file" {
	source = "${var.helper_program_location}/${var.import_creds_program}"
	destination = "/tmp/${var.import_creds_program}"
  }

  provisioner "file" {
	source = "${var.helper_program_location}/${var.switch_principal_program}"
	destination = "/tmp/${var.switch_principal_program}"
  }

  provisioner "file" {
	source = "make-virtue-shares.sh"
	destination = "/tmp/make-virtue-shares.sh"
  }
  
  provisioner "file" {
	source = "post-deploy-config.sh"
	destination = "/tmp/post-deploy-config.sh"
  }
  
  provisioner "file" {
	source = "allow-delegation.sh"
	destination = "/tmp/allow-delegation.sh"
  }
  
  provisioner "file" {
	source = "${var.netplan_deb}"
	destination = "/tmp/${basename(var.netplan_deb)}"
  }

  provisioner "remote-exec" {
	inline = [
	  "while ! [ -e /tmp/user_data-finished ]; do sleep 2; done",
	  "sudo cp /tmp/virtue.conf /etc/samba/",
	  "sudo touch /etc/samba/virtue-shares.conf",
	  "sudo systemctl enable smbd nmbd",
	  "sudo systemctl start smbd nmbd",
	  "sudo cp --target-directory=${var.proxy_jar_dest} /tmp/${basename(var.proxy_jar)}",
	  # install will make them executable by default
	  "sudo install --target-directory=/usr/local/bin /tmp/${var.import_creds_program} /tmp/${var.switch_principal_program} /tmp/make-virtue-shares.sh /tmp/post-deploy-config.sh /tmp/allow-delegation.sh",
	  "sudo dpkg -i /tmp/${basename(var.netplan_deb)}",
	]
  }  

  provisioner "remote-exec" {
	connection {
	  timeout = "30s"
	}
	when = "destroy"
	on_failure = "continue"
	inline = [
	  "echo '${var.admin_password}' | sudo realm leave --remove --user ${var.domain_admin_user} ${var.domain} || true"
	]
  }
}
