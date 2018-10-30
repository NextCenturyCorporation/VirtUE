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
hostnamectl set-hostname ${local.myname}.${var.domain}
sed -i 's/\(^127\.0\.0\.1 *\)/\1${local.myname}.${var.domain} ${local.myname} /' /etc/hosts
(echo supersede domain-name-servers "${local.ds_private_ip}" ';'
echo supersede domain-search \"${var.domain}\";
echo supersede domain-name \"${var.domain}\";
) >> /etc/dhcp/dhclient.conf
systemctl restart network.service
sed -i 's/^\(\[libdefaults\]\)/\1\n  rdns = false/' /etc/krb5.conf
echo ${var.admin_password} | realm join --membership-software=samba --user ${var.domain_admin_user} ${var.domain}
echo Making minimal smb.conf so net ads keytab works
cp /etc/samba/smb.conf /etc/samba/smb.conf-orig
(
echo security = user
echo realm = ${var.domain}
echo workgroup = "${local.domain_prefix}"
echo kerberos method = secrets and keytab
) | sed -i -e '/^\[global\]$/r /dev/stdin' \
    -e '/ *\(security\|realm\|workgroup\|kerberos method\) *=/d' \
	-e '/ *printing *=/,$d' \
    /etc/samba/smb.conf
echo '${var.admin_password}' | net -k ads keytab flush -U ${var.domain_admin_user}
echo '${var.admin_password}' | net -k ads keytab add http -U ${var.domain_admin_user}
echo '${var.admin_password}' | net -k ads keytab add HTTP -U ${var.domain_admin_user}

echo Configuring Kerberos for our domain
cat > /etc/krb5.conf.d/savior.conf <<EOCONF
[libdefaults]
	default_realm = ${upper(var.domain)}
	dns_lookup_realm = false
	dns_lookup_kdc = true
	forwardable = true
	proxiable = true
	default_keytab_name = FILE:/etc/krb5.keytab

[realms]
	${upper(var.domain)} = {
		kdc = ${var.domain}
		admin_server = ${var.domain}
	}
EOCONF

echo Setting userPrincipalName and adding permissions for constrained delegation
ldapfile=/tmp/ldap-$$$$
domainparts=$(echo ${var.domain} | sed 's/\./,dc=/g')
cat > $ldapfile <<EOLDAP
dn: cn=${local.myname},cn=Computers,dc=$domainparts
changetype: modify
add: msDS-AllowedToDelegateTo
msDS-AllowedToDelegateTo: cifs/${upper(local.fsname)}
msDS-AllowedToDelegateTo: cifs/${local.fsname}.${var.domain}
-
replace: userPrincipalName
userPrincipalName: http/${local.myname}.${var.domain}@${upper(var.domain)}
-
EOLDAP
ldapmodify -f $ldapfile -w '${var.admin_password}' -x -H ldap://${local.ds_private_ip} -D "${var.domain_admin_user}@${var.domain}"

echo fixing reverse dns
myaddress=$(ip address show dev eth0 | sed -n 's_ *inet \([^/]*\)/.*_\1_p')
echo '${var.admin_password}' | kinit ${var.domain_admin_user}
# if the zone doesn't exist, create it
mysubnet=$(echo $myaddress | sed 's/\.[^.]*$//')
zone=$(echo $mysubnet | awk -F . '{ print $3 "." $2 "." $1 }').in-addr.arpa
samba-tool dns zoneinfo ${local.ds_private_ip} $zone \
	   --kerberos=1 \
	   >& /dev/null || \
	samba-tool dns zonecreate ${local.ds_private_ip} $zone \
		   --kerberos=1

# remove reverse DNS PTR record (if any)
lastOctet=$${myaddress/*./}
samba-tool dns delete ${local.ds_private_ip} $zone $lastOctet PTR ${local.myname}.${var.domain} \
	   --kerberos=1 \
	   >& /dev/null || true
# add new reverse DNS PTR record
samba-tool dns add ${local.ds_private_ip} $zone $lastOctet PTR ${local.myname}.${var.domain} \
	   --kerberos=1

# create user that will mount files
useradd --shell /bin/false mounter

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
	  "echo '    include = virtue.conf' | sudo tee --append /etc/samba/smb.conf > /dev/null",
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

  provisioner "remote-exec" {
	inline = [
	  # install will make them executable by default
	  "sudo install --target-directory=/usr/local/bin /tmp/${var.import_creds_program} /tmp/${var.switch_principal_program}"
	]
  }
}

#  provisioner "file" {
#	destination = "/etc/krb5.conf.d/savior.conf"
#	content =<<EOF
#EOF
#  }
  
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
