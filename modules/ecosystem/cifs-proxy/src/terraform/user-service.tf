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
  iam_instance_profile = "${aws_iam_instance_profile.instance_profile_file_server.name}"

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
	#    ignore_changes = ["user_data"]
  }

  user_data = <<EOF
#!/bin/bash
set -x
exec > /var/log/user_data.log 2>&1
date
yum -y update && yum -y install sssd realmd krb5-workstation samba-common-tools java-1.8.0-openjdk
hostnamectl set-hostname ${local.myname}.${var.domain}
sed -i 's/\(^127\.0\.0\.1 *\)/\1${local.myname}.${var.domain} ${local.myname} /' /etc/hosts
(echo supersede domain-name-servers "${aws_directory_service_directory.active_directory.dns_ip_addresses[0]}", "${aws_directory_service_directory.active_directory.dns_ip_addresses[1]}" ';'
echo supersede domain-search \"${var.domain}\";
echo supersede domain-name \"${var.domain}\";
) >> /etc/dhcp/dhclient.conf
systemctl restart network.service
echo ${var.admin_password} | realm join --user Admin ${var.domain}
(
echo security = ads
echo realm = ${var.domain}
echo workgroup = "${element(split(".", "${var.domain}"),0)}"
echo kerberos method = secrets and keytab
) | sed -i -e '/^\[global\]$/r /dev/stdin' \
    -e '/ *\(security\|realm\|workgroup\|kerberos method\) *=/d' \
    /etc/samba/smb.conf
echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config
systemctl restart sshd.service
systemctl start sssd.service
echo '${var.admin_password}' | sudo net -k ads keytab add HTTP -U Admin
date
EOF

  depends_on = [ "aws_directory_service_directory.active_directory", "aws_instance.file_server" ]
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
  }
}

resource "null_resource" "fix_user_service_upn" {
  triggers {
	ad_id = "${aws_instance.user_facing_server.id}"
  }

  # Note: this runs on the file server because there doesn't appear to
  # be a way to do set a UPN from Linux.

  connection {
	type     = "winrm"
	user     = "Administrator"
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
