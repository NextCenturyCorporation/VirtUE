# Variables describing the directory service, which has been manually
# created (at least for now).
#

locals {
  ds_private_ip = "${aws_directory_service_directory.directory_service.dns_ip_addresses[0]}"
}
