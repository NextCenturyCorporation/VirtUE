#
# Definitions shared across terraform recipes
#

provider "aws" {
  region = "us-east-1"
}

data "external" "local_user" {
	program = ["python", "-c", "import os; print '{ \"user\": \"%s\" }' % os.environ['USER']"]
}

variable "admin_password" {
	# Define this in a .auto.tfvars file that's not checked into git
}

variable "instance_type" {
	# t2.micro is free, but maybe too small for Windows 2016 Server to run well
	default = "t2.medium"
}

variable "security_group_names" {
	default = [ "default_sg", "rdp_sg", "open_private_dev_sg" ]
}

variable "subnet_name" {
	default = "public_clong"
}

variable "domain" {
	default = "test.savior"
}

data "aws_subnet" "private_subnet" {
	filter {
		name = "tag:Name"
		values = [ "Private_clong" ]
	}
}

data "aws_subnet" "public_subnet" {
	filter {
		name = "tag:Name"
		values = [ "Public_clong" ]
	}
}

data "aws_security_group" "sg" {
	count = "${length(var.security_group_names)}"
	filter {
		name = "tag:Name"
		values = [ "${element(var.security_group_names, count.index)}" ]
	}
}

data "aws_ami" "windows_server2016" {
  most_recent = true

  owners      = ["amazon"]

  filter {
	name   = "name"
	values = ["Windows_Server-2016-English-Full-Base*"]
  }
}

