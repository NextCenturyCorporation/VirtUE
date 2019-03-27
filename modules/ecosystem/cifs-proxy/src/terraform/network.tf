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

resource "aws_vpc_dhcp_options" "dhcp_ad_options" {
  domain_name          = "${var.domain}"

#  domain_name_servers = [ "${aws_instance.directory_service.private_ip}" ]
  domain_name_servers = ["AmazonProvidedDNS"]

  tags {
	automated = "terraform"
  }
}

resource "aws_vpc_dhcp_options_association" "dhcp_ad_association" {
  vpc_id = "${data.aws_vpc.ad_vpc.id}"
  dhcp_options_id = "${aws_vpc_dhcp_options.dhcp_ad_options.id}"
}
