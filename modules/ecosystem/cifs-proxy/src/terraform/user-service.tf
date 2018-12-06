#
# User-facing service
#

locals {
  myname = "webserver"
}

resource "aws_instance" "user_facing_server" {
  ami           = "${var.linux_ami}"
  instance_type = "${var.linux_instance_type}"
  key_name      = "vrtu"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${local.myname}"
	Owner = "${data.external.local_user.result.user}"
	class = "webserver"
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
# samba-dc provides samba-tool
yum -y install \
	adcli \
	cifs-utils \
	java-1.8.0-openjdk \
	krb5-workstation \
	oddjob \
	oddjob-mkhomedir \
	openldap-clients \
	realmd \
	samba \
	samba-common-tools \
	samba-dc \
	sssd

sed -i 's/^\(\[libdefaults\]\)/\1\n  rdns = false/' /etc/krb5.conf

echo Adding permissions for constrained delegation
ldapfile=/tmp/ldap-$$$$
domainparts=$(echo ${var.domain} | sed 's/\./,dc=/g')
cat > $ldapfile <<EOLDAP
dn: cn=${local.myname},cn=Computers,dc=$domainparts
changetype: modify
add: msDS-AllowedToDelegateTo
msDS-AllowedToDelegateTo: cifs/${upper(local.fsname)}
msDS-AllowedToDelegateTo: cifs/${local.fsname}.${var.domain}
-
EOLDAP
ldapmodify -f $ldapfile -w '${var.admin_password}' -x -H ldap://${local.ds_private_ip} -D "${var.domain_admin_user}@${var.domain}"

# create user that will mount files
useradd --shell /bin/false --no-create-home mounter

date
touch /tmp/user_data-finished
EOF
  # end of user_data

  connection {
	type = "ssh"
	user = "${var.linux_user}"
	private_key = "${file(var.user_private_key_file)}"
  }
  
  # The following file and remote-exec provisioners set up samba
  provisioner "file" {
	source = "virtue.conf"
	destination = "/tmp/virtue.conf"
  }

  provisioner "remote-exec" {
	inline = [
	  "while ! [ -e /tmp/user_data-finished ]; do echo -n '.' ; sleep 2; done",
	  "echo '    include = /etc/samba/virtue.conf' | sudo tee --append /etc/samba/smb.conf > /dev/null",
	  "sudo cp /tmp/virtue.conf /etc/samba",
	  "sudo touch /etc/samba/virtue-shares.conf",
	  "sudo systemctl enable smb nmb",
	  "sudo systemctl start smb nmb",
	]
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
  
  provisioner "remote-exec" {
	inline = [
	  # install will make them executable by default
	  "sudo install --target-directory=/usr/local/bin /tmp/${var.import_creds_program} /tmp/${var.switch_principal_program} /tmp/make-virtue-shares.sh /tmp/post-deploy-config.sh",
	  "/usr/local/bin/post-deploy-config.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password ${var.admin_password} --hostname ${local.myname} --dcip ${local.ds_private_ip}"
	]
  }  
}

  
# The stuff below sets the userPrincipalName (upn) of the user
# service.  Without this, kinit doesn't work for the http principal,
# whose keytab entry gets created by the net command above. It seems
# like kinit should work even w/o this, but it doesn't.

data "template_file" "fix_user_service_upn_script" {
  template = "${file("user-service.ps1")}"

  vars {
	password = "${var.admin_password}"
	hostname = "${local.myname}"
    domain = "${var.domain}"
    domain_admin_user = "${var.domain_admin_user}"
  }
}

resource "null_resource" "fix_user_service_upn" {
  triggers {
	ad_id = "${aws_instance.user_facing_server.id}"
  }

  # Note: this runs on the file server because there doesn't appear to
  # be a way to set a UPN from Linux.

  connection {
	type     = "winrm"
	user     = "Administrator" # local admin user
	host     = "${aws_instance.file_server.public_dns}"
	password = "${var.admin_password}"
	https    = false
	use_ntlm = false
  }

  provisioner "file" {
	content = "${data.template_file.fix_user_service_upn_script.rendered}"
	destination = "\\temp\\user-service.ps1"
  }

  provisioner "remote-exec" {
	inline = [
	  "powershell \\temp\\user-service.ps1"
	]
  }
  
  depends_on = [ "aws_instance.file_server", "aws_instance.user_facing_server" ]

}
