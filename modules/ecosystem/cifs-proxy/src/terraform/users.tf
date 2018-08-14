#
# Create users in our AD domain
#

data "template_file" "users_script" {
  template = "${file("users.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
	bob_password = "${var.bob_password}"
  }
}

resource "null_resource" "user_creation" {
  triggers {
	ad_id = "${aws_directory_service_directory.active_directory.id}"
  }

  connection {
	user = "Administrator"
	password = "${var.admin_password}"
	host = "${aws_instance.file_server.public_ip}"
	type = "winrm"
	https = false
	use_ntlm = false
  }
  
  provisioner "file" {
	content = "${data.template_file.users_script.rendered}"
	destination = "\\temp\\users.ps1"
  }
  
  provisioner "remote-exec" {
	inline = [
	  "powershell \\temp\\users.ps1"
	]
  }
}
