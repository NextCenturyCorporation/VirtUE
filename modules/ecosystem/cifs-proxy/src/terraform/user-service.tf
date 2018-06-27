#
# User-facing service
#

resource "aws_instance" "user_facing_server" {
  ami           = "${var.linux_ami}"
  instance_type = "${var.linux_instance_type}"
  key_name      = "vrtu"
  iam_instance_profile = "${aws_iam_instance_profile.instance_profile_file_server.name}"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "user-service"
	Owner = "${data.external.local_user.result.user}"
	class = "webapp"
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
(echo supersede domain-name-servers "${aws_directory_service_directory.active_directory.dns_ip_addresses[0]}", "${aws_directory_service_directory.active_directory.dns_ip_addresses[1]}" ';'
echo supersede domain-search \"${var.domain}\";
echo supersede domain-name \"${var.domain}\";
) >> /etc/dhcp/dhclient.conf
sudo systemctl restart network.service
echo ${var.admin_password} | realm join --user Admin ${var.domain}
echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config
systemctl restart sshd.service
systemctl start sssd.service
date
EOF

  depends_on = [ "aws_directory_service_directory.active_directory" ]
  
}
