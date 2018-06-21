#
# Start an Active Directory Domain Server
#

variable "admin_password" {
	# Define this in a .auto.tfvars file that's not checked into git
}

data "external" "local_user" {
  program = ["python", "-c", "import os; print '{ \"user\": \"%s\" }' % os.environ['USER']"]
}

data "aws_ami" "adds_ami" {
  most_recent = true

  owners      = ["amazon"]

  filter {
	name   = "name"
	values = ["Windows_Server-2016-English-Full-Base*"]
  }
}

locals {
#  "public_1a_id" = "${aws_subnet.public_1a.id}"
}

resource "aws_instance" "active_directory" {
  ami           = "${data.aws_ami.adds_ami.image_id}"
  instance_type = "t2.micro"
  key_name      = "vrtu"

#  subnet_id = "${local.public_1a_id}"

  vpc_security_group_ids = ["${aws_security_group.default_sg.id}","${ aws_security_group.rdp_sg.id}"]

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
  echo Setting password
  net user Administrator "${var.admin_password}"
  echo Installing AD Services
  install-windowsfeature -name AD-Domain-Services
  echo Setting to be a Domain Controller
  Install-addsforest -DomainName test.savior -safemodeadministratorpassword (convertto-securestring "${var.admin_password}" -asplaintext -force) -skipprechecks -force
  echo Setup done
</powershell>
EOF
}
/*
*/

/*
resource "aws_instance" "adds" {
  # The connection block tells our provisioner how to
  # communicate with the resource (instance)
  connection {
	type     = "winrm"
	user     = "Administrator"
	password = "${var.admin_password}"
	https    = true
	
	# set from default of 5m to 10m to avoid winrm timeout5
	timeout = "10m"
  }

  instance_type = "t2.micro"
  ami           = "${data.aws_ami.adds_ami.image_id}"

  # The name of our SSH keypair you've created and downloaded
  # from the AWS console.
  #
  # https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#KeyPairs
  #
  key_name = "vrtu"

  # Our Security group to allow WinRM access
#  security_groups = ["${aws_security_group.default.name}"]

  user_data = <<EOF
<script>
  winrm quickconfig -q & winrm set winrm/config @{MaxTimeoutms="1800000"} & winrm set winrm/config/service @{AllowUnencrypted="true"} & winrm set winrm/config/service/auth @{Basic="true"}
</script>
<powershell>
  netsh advfirewall firewall add rule name="WinRM in" protocol=TCP dir=in profile=any localport=5985 remoteip=any localip=any action=allow
  # Set Administrator password
  $admin = [adsi]("WinNT://./administrator, user")
  $admin.psbase.invoke("SetPassword", "${var.admin_password}")
</powershell>
EOF
}
*/
