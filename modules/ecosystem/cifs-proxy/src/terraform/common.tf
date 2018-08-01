#
# Definitions shared across terraform recipes
#

provider "aws" {
  region = "us-east-1"
  version = "~> 1.23"
}

provider "external" {
  version = "~> 1.0"
}

provider "null" {
  version = "~> 1.0"
}

data "external" "local_user" {
  program = ["python", "-c", "import os; print '{ \"user\": \"%s\" }' % os.environ['USER']"]
}

variable "user_private_key_file" {
  # the private key to use to ssh into a machine we create (as a normal user)
  default = "vrtu.pem"
}

variable "admin_password" {
  # Define this in a .auto.tfvars file that's not checked into git
}

variable "instance_type" {
  # t2.micro is free, but maybe too small for Windows 2016 Server to run well
  default = "t2.medium"
}

variable "security_group_names" {
  default = [ "default_sg_ad", "rdp_sg_ad", "open_private_dev_sg_ad" ]
}

variable "subnet_name" {
  default = "public_1a"
}

variable "domain" {
  default = "test.savior"
}

variable "linux_ami" {
  description = "Amazon Linux 2 LTS Candidate 2 AMI (HVM), SSD Volume Type"
  default = "ami-afd15ed0"
}

variable "linux_instance_type" {
  default = "t2.micro"
}

data "aws_vpc" "ad_vpc" {
  filter {
	name = "tag:Name"
	values = [ "ADTEST" ]
  }
}

data "aws_subnet" "private_subnet" {
  vpc_id = "${data.aws_vpc.ad_vpc.id}"
  filter {
	name = "tag:Name"
	values = [ "Private_1b" ]
  }
}

data "aws_subnet" "public_subnet" {
  vpc_id = "${data.aws_vpc.ad_vpc.id}"
  filter {
	name = "tag:Name"
	values = [ "Public_1a" ]
  }
}

data "aws_security_group" "sg" {
  vpc_id = "${data.aws_vpc.ad_vpc.id}"
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

