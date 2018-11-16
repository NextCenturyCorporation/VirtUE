#
# Make file shares and test files to share
#


data "template_file" "setup_file_sharing_script" {
  template = "${file("setup-file-sharing.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
  }
}

resource "null_resource" "setup_file_sharing" {

  connection {
	user = "Administrator"
	password = "${var.admin_password}"
	host = "${aws_instance.file_server.public_ip}"
	type = "winrm"
	https = false
	use_ntlm = false
  }

  provisioner "file" {
	content = "${data.template_file.setup_file_sharing_script.rendered}"
	destination = "\\temp\\setup-file-sharing.ps1"
  }

  provisioner "remote-exec" {
	inline = [
	  "powershell \\temp\\setup-file-sharing.ps1"
	]
  }

  depends_on = [ "null_resource.user_creation" ]
}
