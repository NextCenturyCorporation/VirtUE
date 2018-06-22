
resource "aws_instance" "file_server" {
	ami           = "${data.aws_ami.adds_ami.image_id}"
	instance_type = "${var.instance_type}"
	key_name      = "vrtu"

	vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
	subnet_id = "${data.aws_subnet.subnet.id}"

	tags {
		Name = "Windows File Server"
		Owner = "${data.external.local_user.result.user}"
		class = "cifs"
		automated = "terraform"
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

	# get the magic script from https://community.spiceworks.com/scripts/show/1540-join-computer-to-domain-with-powershell-one-click-method
	# need to join the domain and share some files
	user_data = <<EOF
<powershell>
  Start-Transcript -Path "c:\user_data.log" -append -force 
  echo Setting password
  net user Administrator "${var.admin_password}"
  echo Joining the domain ${var.domain}
  $domain = "${var.domain}"
  $password = "${var.admin_password}" | ConvertTo-SecureString -asPlainText -Force
  $username = "${var.domain}\Administrator" 
  $credential = New-Object System.Management.Automation.PSCredential($username,$password)
  Add-Computer -DomainName $domain -Credential $credential
  echo Setup done
</powershell>
EOF

	depends_on = [ "aws_instance.active_directory" ]
}
