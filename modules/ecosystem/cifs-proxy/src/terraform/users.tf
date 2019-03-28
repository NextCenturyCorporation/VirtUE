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
# Create users in our AD domain
#


data "template_file" "users_script" {
  template = "${file("users.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
	bob_password = "${var.bob_password}"
	domain_admin_user = "${var.domain_admin_user}"
  }
}

data "template_file" "remove_users_script" {
  template = "${file("users-remove.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
	domain_admin_user = "${var.domain_admin_user}"
  }
}

resource "null_resource" "user_creation" {
  triggers {
	script_sha1 = "${sha1(data.template_file.users_script.template)}"
	# Not depending on the remove script because it should only change
	# when the add script does.
  }

  connection {
	user = "Administrator" # local admin (for some reason cannot log in as domain admin when using Amazon AD)
	password = "${var.admin_password}"
	host = "${aws_instance.file_server.public_ip}"
	type = "winrm"
	https = false
	use_ntlm = false
  }
  
  provisioner "file" {
	content = "${data.template_file.users_script.rendered}"
	destination = "\\temp\\users.ps1"
  }
  
  provisioner "remote-exec" {
	inline = [
	  "powershell \\temp\\users.ps1"
	]
  }

  provisioner "file" {
	when = "destroy"
	content = "${data.template_file.remove_users_script.rendered}"
	destination = "\\temp\\users-remove.ps1"
  }
  
  provisioner "remote-exec" {
	when = "destroy"
	inline = [
	  "powershell \\temp\\users-remove.ps1"
	]
  }
}
