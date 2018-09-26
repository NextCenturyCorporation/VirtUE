
resource "aws_vpc_dhcp_options" "dhcp_ad_options" {
  domain_name          = "${var.domain}"

  domain_name_servers = [ "${aws_directory_service_directory.directory_service.dns_ip_addresses[0]}", "${aws_directory_service_directory.directory_service.dns_ip_addresses[1]}" ]

  tags {
	automated = "terraform"
  }
}

resource "aws_vpc_dhcp_options_association" "dhcp_ad_association" {
  vpc_id = "${data.aws_vpc.ad_vpc.id}"
  dhcp_options_id = "${aws_vpc_dhcp_options.dhcp_ad_options.id}"
}
