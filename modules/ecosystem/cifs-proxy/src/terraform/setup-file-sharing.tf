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
# Make file shares and test files to share
#


data "template_file" "setup_file_sharing_script" {
  template = "${file("setup-file-sharing.ps1")}"

  vars {
	admin_password = "${var.admin_password}"
  }
}

resource "null_resource" "setup_file_sharing" {

  connection {
	user = "Administrator"
	password = "${var.admin_password}"
	host = "${aws_instance.file_server.public_ip}"
	type = "winrm"
	https = false
	use_ntlm = false
  }

  provisioner "file" {
	content = "${data.template_file.setup_file_sharing_script.rendered}"
	destination = "\\temp\\setup-file-sharing.ps1"
  }

  provisioner "remote-exec" {
	inline = [
	  "powershell \\temp\\setup-file-sharing.ps1"
	]
  }

  depends_on = [ "aws_instance.file_server", "null_resource.user_creation" ]
}
