#
# Copyright (C) 2019 Next Century Corporation
# 
# This file may be redistributed and/or modified under either the GPL
# 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
# granted government purpose rights. For details, see the COPYRIGHT.TXT
# file at the root of this project.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
# 
# SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
#
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
  #
  # NOTE: If you change distributions, you probably need to change
  # linux_user, too.
  description = "Canonical, Ubuntu, 18.04 LTS, amd64 bionic image build on 2018-08-14"
  default = "ami-05fb04e2687120d6b"
}

variable "linux_user" {
  description = "The default user for our linux instance"
  default = "ubuntu"
}

variable "linux_instance_type" {
  default = "t2.micro"
}

variable "proxy_jar" {
  # based on baseName and version set in ../../build.gradle
  description = "Path to the jar file for the CIFS Proxy."
  default = "../../build/libs/cifs-proxy-server-0.0.1.jar"
}

variable "proxy_jar_dest" {
  description = "Destination for the proxy jar file (on the cifs proxy)"
  default = "/usr/local/lib"
}

variable "helper_program_location" {
  default = "../.."
}

variable "import_creds_program" {
  description = "Helper program for importing Kerberos credentials from a file into the default context."
  default = "importcreds"
}

variable "switch_principal_program" {
  description = "Helper program for changing the Kerberos principal in a file."
  default = "switchprincipal"
}

variable "netplan_deb" {
  description = "Path to the netplan .deb package."
  default = "../../../netplan/netplan.io_0.95-2_amd64.deb"
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
