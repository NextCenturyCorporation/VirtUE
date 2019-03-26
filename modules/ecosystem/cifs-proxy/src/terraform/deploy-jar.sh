#!/bin/bash
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
# (Re)Deploy the cifs jar file
#

getValue() {
	echo $@ | terraform console
}

set -e

privateKey=$(getValue var.user_private_key_file)
user=$(getValue var.linux_user)
cifsHost=$(getValue aws_instance.cifs_proxy.public_dns)
jar=$(getValue var.proxy_jar)
jarDest=$(getValue var.proxy_jar_dest)
make -j2 proxy-jar
scp -i "${privateKey}" "${jar}" ${user}@${cifsHost}:/tmp
ssh -i "${privateKey}" ${user}@${cifsHost} sudo mv "/tmp/$(basename ${jar})" "${jarDest}"
echo deployed to ${cifsHost} at ${jarDest}
