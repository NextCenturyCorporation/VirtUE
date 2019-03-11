#
# User-facing service
#

locals {
  psname = "printserver"
}

resource "aws_instance" "print_server" {
  ami           = "${var.linux_ami}"
  instance_type = "${var.linux_instance_type}"
  key_name      = "vrtu"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${local.psname}"
	Owner = "${data.external.local_user.result.user}"
	class = "printserver"
	automated = "terraform"
  }
  lifecycle {
	prevent_destroy = false
  }

  user_data = <<EOF
#!/bin/bash
set -x -e
exec > /var/log/user_data.log 2>&1
date
# prevent questions about kerberos configuration (it'll get set by post-deploy-config.sh)
export DEBIAN_FRONTEND=noninteractive
apt-get update && \
apt-get -y --with-new-pkgs upgrade && \
apt-get -y install \
	adcli \
	auth-client-config \
	cups \
	ghostscript \
	keyutils \
	krb5-user \
	libnss-sss \
	libpam-ccreds \
	libpam-krb5 \
	libpam-sss \
	packagekit \
	printer-driver-cups-pdf \
	realmd \
	samba \
	samba-dsdb-modules \
	sssd \
	sssd-tools

sed -i 's/^\(\[libdefaults\]\)/\1\n  rdns = false/' /etc/krb5.conf

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
  
  #
  # Helper programs
  #
  provisioner "file" {
	source = "post-deploy-config.sh"
	destination = "/tmp/post-deploy-config.sh"
  }

  provisioner "file" {
	source = "${var.netplan_deb}"
	destination = "/tmp/${basename(var.netplan_deb)}"
  }

  provisioner "remote-exec" {
	inline = [
	  "while ! [ -e /tmp/user_data-finished ]; do sleep 2; done",
	  "sudo systemctl enable smbd nmbd",
	  "sudo systemctl start smbd nmbd",
	  "sudo touch /etc/samba/virtue.conf",
	  "sudo dpkg -i /tmp/${basename(var.netplan_deb)}",
	  # install will make them executable by default
	  "sudo install --target-directory=/usr/local/bin /tmp/post-deploy-config.sh",
	  "sudo /usr/local/bin/post-deploy-config.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password ${var.admin_password} --hostname ${local.psname} --dcip ${local.ds_private_ip} --service cifs --security ads --keep-keytab --verbose",
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
