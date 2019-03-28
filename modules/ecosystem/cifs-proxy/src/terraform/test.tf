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
# stuff for running tests
#

resource "local_file" "test_remote_setup_sh" {
  content = <<EOF
#!/bin/bash
# Created automatically by test.tf. To change, edit test.tf and re-run terraform.
#
sudo apt-get install -y jq

sudo /usr/local/bin/post-deploy-config.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password '${var.admin_password}' --hostname ${local.myname} --dcip ${local.ds_private_ip}
sleep 5
sudo /usr/local/bin/allow-delegation.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password '${var.admin_password}' --delegater ${local.myname} --target ${local.fsname}
sudo /usr/local/bin/allow-delegation.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password '${var.admin_password}' --delegater ${local.myname} --target ${local.psname}
EOF
  filename = "${path.module}/test-remote-setup.sh"
}
