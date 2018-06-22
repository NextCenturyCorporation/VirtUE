#
# Start an Active Directory Domain Server
#

data "aws_ami" "adds_ami" {
	most_recent = true

	owners      = ["amazon"]

	filter {
		name   = "name"
		values = ["Windows_Server-2016-English-Full-Base*"]
	}
}

resource "aws_instance" "active_directory" {
	ami           = "${data.aws_ami.adds_ami.image_id}"
	instance_type = "${var.instance_type}"
	key_name      = "vrtu"

	subnet_id = "${data.aws_subnet.subnet.id}"

	vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]

	tags {
		Name = "Windows AD DS"
		Owner = "${data.external.local_user.result.user}"
		class = "activedirectory"
		automated = "true"
	}
	lifecycle {
		prevent_destroy = false

		#    ignore_changes = ["user_data"]
	}

	connection {
		type     = "winrm"
		user     = "Administrator"
		password = "${var.admin_password}"
		https    = true
		
		# set from default of 5m to 10m to avoid winrm timeout5
		timeout = "10m"
	}

	# based on https://docs.microsoft.com/en-us/windows-server/identity/ad-ds/deploy/install-a-new-windows-server-2012-active-directory-forest--level-200-

	# Note: This takes more than 10 minutes, including creating &
	# launching the instance and doing the AD DS setup.
	user_data = <<EOF
<powershell>
  Start-Transcript -Path "c:\user_data.log" -append -force 
  echo Setting password
  net user Administrator "${var.admin_password}"
  echo Installing AD Services
  install-windowsfeature -name AD-Domain-Services
  echo Setting to be a Domain Controller
  install-addsdomaincontroller -DomainName ${var.domain} -safemodeadministratorpassword (convertto-securestring "${var.admin_password}" -asplaintext -force) -force
  echo Setup done
</powershell>
EOF
#  Install-addsforest -DomainName ${var.domain} -safemodeadministratorpassword (convertto-securestring "${var.admin_password}" -asplaintext -force) -skipprechecks -force
}
