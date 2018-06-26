#
# Start an Active Directory Domain Server
#

# For AWS managed AD DC, the admin user is "Admin", not "Administrator"

resource "aws_directory_service_directory" "active_directory" {
	name = "${var.domain}"
	password = "${var.admin_password}"
	edition = "Standard"
	type     = "MicrosoftAD"

	vpc_settings {
		subnet_ids = [ "${data.aws_subnet.private_subnet.id}", "${data.aws_subnet.public_subnet.id}" ]
		vpc_id = "${data.aws_subnet.public_subnet.vpc_id}"
	}

	tags {
		Name = "Windows AD DS"
		Owner = "${data.external.local_user.result.user}"
		class = "activedirectory"
		automated = "true"
	}
}
