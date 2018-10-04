#
# Create users in our AD domain
#


data "template_file" "users_script" {
  template = "${file("users.ps1")}"

  vars {
	domain_admin_user = "${var.domain_admin_user}"
	admin_password = "${var.admin_password}"
	bob_password = "${var.bob_password}"
  }
}

data "template_file" "remove_users_script" {
  template = "${file("users-remove.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
  }
}

resource "null_resource" "user_creation" {
  triggers {
	script_sha1 = "${sha1(data.template_file.users_script.template)}"
	# Not depending on the remove script because it should only change
	# when the add script does.
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

  provisioner "file" {
	when = "destroy"
	content = "${data.template_file.remove_users_script.rendered}"
	destination = "\\temp\\users-remove.ps1"
  }
  
  provisioner "remote-exec" {
	when = "destroy"
	inline = [
	  "powershell \\temp\\users-remove.ps1"
	]
  }
}
