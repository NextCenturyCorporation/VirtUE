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

provider "local" {
  version = "~> 1.1"
}

provider "template" {
  version = "~> 1.0"
}

data "external" "local_user" {
  program = ["python", "-c", "import os; print '{ \"user\": \"%s\" }' % os.environ['USER']"]
}

variable "user_private_key_file" {
  # the private key to use to ssh into a machine we create (as a normal user)
  default = "vrtu.pem"
}

variable "linux_user" {
  # The user name to use to ssh into a machine we create (as a normal user)
  # Change this if the Linux AMI distro changes
  default = "fedora"
}

variable "bob_password" {
  default = "Test1234."
}

variable "admin_password" {
  # Define this in a .auto.tfvars file that's not checked into git
}

variable "windows_instance_type" {
  # t2.micro is free, but maybe too small for Windows 2016 Server to run well
  default = "t2.medium"
}

variable "security_group_names" {
  default = [ "default_sg_ad", "rdp_sg_ad", "open_private_dev_sg_ad", "winrm_sg_ad" ]
}

variable "subnet_name" {
  default = "public_1a"
}

variable "dsname" {
  description = "Host name of the directory service"
  default = "ds"
}

variable "domain" {
  default = "test.savior"
}

variable "domain_admin_user" {
  default = "Admin" # for EC2 Active Directory
}

locals {
  # the first part of the domain (e.g., if the domain is 'foo.com',
  # this is 'foo')"
  domain_prefix = "${element(split(".", "${var.domain}"),0)}"
}

variable "linux_ami" {
  # Using Fedora 28 because it has more recent version of Samba than
  # Ubuntu 18.04 LTS.  See https://alt.fedoraproject.org/cloud/ for
  # info.
  # If you change to a new distro, update linux_user
  description = "Fedora-Cloud-Base-28-20180922.0.x86_64-hvm-us-east-1-standard-0"
  default = "ami-0064c3021927a1bd5"
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

