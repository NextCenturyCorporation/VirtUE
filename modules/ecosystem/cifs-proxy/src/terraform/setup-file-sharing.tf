#
# Make file shares and test files to share
#

resource "null_resource" "setup_file_sharing" {
  triggers {
	ad_id = "${aws_directory_service_directory.active_directory.id}"
  }

  connection {
	user = "Admin" # domain admin
	password = "${var.admin_password}"
	host = "${aws_instance.file_server.public_ip}"
	type = "winrm"
	https = "true"
  }

  provisioner "remote-exec" {
	inline = [
	  "Get-WindowsFeature -Name FS-FileServer",
	  "New-FileShare -Name TestShare -SourceVolume (Get-Volume -DriveLetter C) -FileServerFriendlyName $env:COMPUTERNAME",
	  "echo Hello > \\shares\\TestShare\\hello.txt",
	  "Get-FileShare -Name TestShare | Grant-FileShareAccess -AccountName bob -AccessRight Full"
	]
  }

  depends_on = [ "null_resource.user_creation" ]
}
