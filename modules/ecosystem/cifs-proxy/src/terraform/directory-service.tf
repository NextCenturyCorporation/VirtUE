locals {
  samba_config_dir = "/var/lib/samba"
}

resource "aws_instance" "directory_service" {
  ami           = "${var.linux_ami}"
  instance_type = "${var.linux_instance_type}"
  key_name      = "vrtu"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${var.dsname}"
	Owner = "${data.external.local_user.result.user}"
	class = "directory service"
	automated = "terraform"
  }
  lifecycle {
	prevent_destroy = false
  }

  user_data = <<EOF
#!/bin/bash
set -x # log it all
set -e # bail out on error
exec > /var/log/user_data.log 2>&1
date
hostnamectl set-hostname ${var.dsname}.${var.domain}
sed -i 's/\(^127\.0\.0\.1 *\)/\1${var.dsname}.${var.domain} ${var.dsname} /' /etc/hosts
dnf -y install \
	krb5-workstation \
	samba \
	samba-dc
mv /etc/krb5.conf /etc/krb5.conf-orig || true
samba-tool domain provision \
	--adminpass="${var.admin_password}" \
	--dns-backend=SAMBA_INTERNAL \
	--domain=${local.domain_prefix} \
	--realm=${var.domain} \
	--server-role=dc \
	--targetdir="${local.samba_config_dir}" \
	--use-rfc2307
cp -f --backup=existing /var/lib/samba/etc/smb.conf /etc/samba

[ -e /etc/krb5.conf ] && echo "$0: kerberos config krb5.conf already exists, overwriting"
cp -f --backup=existing /var/lib/samba/private/krb5.conf /etc

[ -e /var/kerberos/krb5kdc/kdc.conf ] && echo "$0: kerberos config kdc.conf already exists, overwriting"
cp -f --backup=existing /var/lib/samba/private/kdc.conf /var/kerberos/krb5kdc

testparm --suppress-prompt && systemctl start samba
systemctl enable samba

# DNS to use ourselves
(echo supersede domain-name-servers "127.0.0.1" ';' # note: trailing ; is required
echo supersede domain-search \"${var.domain}\";
echo supersede domain-name \"${var.domain}\";
) >> /etc/dhcp/dhclient.conf
systemctl restart network.service

echo fixing reverse dns
myaddress=$(ip address show dev eth0 | sed -n 's_ *inet \([^/]*\)/.*_\1_p')
echo '${var.admin_password}' | kinit Administrator
# if the zone doesn't exist, create it
mysubnet=$(echo $myaddress | sed 's/\.[^.]*$//')
zone=$(echo $mysubnet | awk -F . '{ print $3 "." $2 "." $1 }').in-addr.arpa
samba-tool dns zoneinfo ${var.dsname} $zone \
	   --kerberos=1 \
	   >& /dev/null || \
	samba-tool dns zonecreate ${var.dsname} $zone \
		   --kerberos=1

# remove reverse DNS PTR record (if any)
lastOctet=$${myaddress/*./}
samba-tool dns delete ${var.dsname} $zone $lastOctet PTR ${var.dsname}.${var.domain} \
	   --kerberos=1 \
	   >& /dev/null || true
# add new reverse DNS PTR record
samba-tool dns add ${var.dsname} $zone $lastOctet PTR ${var.dsname}.${var.domain} \
	   --kerberos=1

EOF
}
