locals {
  fsname = "fileserver"
}

resource "aws_instance" "file_server" {
  ami           = "${data.aws_ami.windows_server2016.image_id}"
  instance_type = "${var.windows_instance_type}"
  key_name      = "vrtu"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${local.fsname}"
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
	https    = false
	use_ntlm = false
	
	# set from default of 5m to 10m to avoid winrm timeout
	timeout = "10m"
  }

  #TODO create spn, for example: sudo samba-tool spn add cifs/fileserver.test.savior@TEST.SAVIOR fileserver$
  
  user_data = <<EOF
<powershell>
  Start-Transcript -Path "c:\user_data.log" -append -force 
  netsh advfirewall firewall add rule name="WinRM in" protocol=TCP dir=in profile=any localport=5985 remoteip=any localip=any action=allow
  winrm set winrm/config/service/Auth '@{Basic="true"}'
  winrm set winrm/config/service '@{AllowUnencrypted="true"}'
  winrm set winrm/config/winrs '@{MaxMemoryPerShellMB="1024"}'
  echo "Setting password"
  net user Administrator "${var.admin_password}"
  echo "Installing RSAT"
  Install-WindowsFeature -Name RSAT-AD-PowerShell -LogPath "c:\install.log"

  echo "fixing DNS"
  Get-NetAdapter | Set-DnsClientServerAddress -ServerAddresses "${var.ds_private_ip}"
  Set-DnsClientGlobalSetting -SuffixSearchList "${var.domain}"

  echo "Joining domain & renaming, then rebooting"
  $password = ConvertTo-SecureString -AsPlainText -Force "${var.admin_password}"
  $cred = new-object -typename System.Management.Automation.PSCredential -argumentlist ${var.domain_admin_user}, $password
  Add-Computer -DomainName "${var.domain}" -Credential $cred -Restart -NewName "${local.fsname}"
  echo "Setup done"
</powershell>
EOF
}

#  $ip = Get-NetIPAddress "${self.private_ip}"
