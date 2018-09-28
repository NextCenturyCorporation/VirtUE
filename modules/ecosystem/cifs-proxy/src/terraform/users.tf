#
# Create users in our AD domain
#

resource "null_resource" "user_creation" {

  connection {
	user = "fedora"
	host = "${aws_instance.user_facing_server.public_ip}"
	type = "ssh"
	private_key = "${file(var.user_private_key_file)}"
  }
  
  provisioner "remote-exec" {
	inline = [
	  "sudo samba-tool user create bob ${bob_password}"
	]
  }

  provisioner "remote-exec" {
	when = "destroy"
	inline = [
	  "sudo samba-tool user delete bob"
	]
  }
}
