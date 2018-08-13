#
# Create users in our AD domain
#

resource "null_resource" "user_creation" {
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
	  "net user bob ${var.bob_password} /add /domain"
	]
  }
}
