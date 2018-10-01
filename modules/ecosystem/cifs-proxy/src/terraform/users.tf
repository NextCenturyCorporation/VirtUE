#
# Create users in our AD domain
#

resource "null_resource" "user_creation" {

  connection {
	user = "fedora"
	host = "${aws_instance.directory_service.public_ip}"
	type = "ssh"
	private_key = "${file(var.user_private_key_file)}"
  }
  
  provisioner "remote-exec" {
	inline = [
	  "sudo samba-tool user create bob ${var.bob_password}"
	]
  }

#  provisioner "remote-exec" {
#	when = "destroy"
#	inline = [
#	  "sudo samba-tool user delete bob"
#	]
#  }
}
