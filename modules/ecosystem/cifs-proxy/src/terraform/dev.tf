#
# stuff for developer convenience
#

resource "local_file" "file_server_rdp" {
  content = <<EOF
full address:s:${aws_instance.file_server.public_ip}
username:s:Administrator
EOF
  filename = "${path.module}/file_server.rdp"
}
