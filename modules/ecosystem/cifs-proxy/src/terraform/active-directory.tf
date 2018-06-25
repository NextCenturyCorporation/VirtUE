#
# Start an Active Directory Domain Server
#


resource "aws_directory_service_directory" "active_directory" {
	short_name = "adds"
	name = "adds.${var.domain}"
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
