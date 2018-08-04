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

  # TODO: enable sharing some files
  user_data = <<EOF
#!/bin/bash
set -x
exec > /var/log/user_data.log 2>&1
date
yum -y update && yum -y install sssd realmd krb5-workstation samba-common-tools
hostnamectl set-hostname ${local.myname}.${var.domain}
sed -i 's/\(^127\.0\.0\.1 *\)/\1${local.myname}.${var.domain} ${local.myname} /' /etc/hosts
(echo supersede domain-name-servers "${aws_directory_service_directory.active_directory.dns_ip_addresses[0]}", "${aws_directory_service_directory.active_directory.dns_ip_addresses[1]}" ';'
echo supersede domain-search \"${var.domain}\";
echo supersede domain-name \"${var.domain}\";
) >> /etc/dhcp/dhclient.conf
systemctl restart network.service
echo ${var.admin_password} | realm join --user Admin ${var.domain}
cat >> /etc/samba/smb.conf <<_EOF
security = ads
realm = ${var.domain}
workgroup = test
_EOF
echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config
systemctl restart sshd.service
systemctl start sssd.service
echo '${var.admin_password}' | sudo net -k ads keytab add HTTP -U Admin
date
EOF

  provisioner "file" {
	source = "cifs-proxy-server-0.0.1.jar"
	destination = "/usr/local/lib"
  }
  
  depends_on = [ "aws_directory_service_directory.active_directory" ]
}
