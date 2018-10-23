# Variables describing the directory service.
#

locals {
  ds_private_ip = "${aws_directory_service_directory.directory_service.dns_ip_addresses[0]}"
}
