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
# Start a samba client
#

locals {
  clientNames = {
	"0" = "client1"
	"1" = "savior-firefox"
  }
  clientHostnames = {
	"0" = "client1"
	"1" = "ff"
  }
  clientImages = {
	"0" = "savior-client"
	"1" = "savior-firefox"
  }
  clientCommand = {
	"0" = "/sbin/init"
	"1" = "/usr/sbin/sshd,-D"
  }
  clientScript = {
	"0" = "true"
	"1" = "./configure-firefox.sh savior-firefox ${docker_container.saviorvc.name} ${docker_network.savior_network.name}"
  }
}

resource "docker_container" "clients" {
  count = "2"
  name = "${lookup(local.clientNames, count.index)}"
  hostname = "${lookup(local.clientHostnames, count.index)}"
  image = "${lookup(local.clientImages, count.index)}"
  domainname = "${docker_container.samba-server.domainname}"
  command = "${split(",", lookup(local.clientCommand, count.index))}"
  networks = ["${docker_network.savior_network.name}"]
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]

  upload {
	file = "/etc/samba/smb.conf"
	content =<<EOF
[global]
	security = ADS
	workgroup = SAVIOR
	realm = ${docker_container.samba-server.domainname}
	client schannel = yes
	client signing = yes
	kerberos method = secrets and keytab

	log file = /var/log/samba/%m.log
	log level = 1

	# Default ID mapping
	idmap config * : backend = tbd
	idmap config * : range = 3000-7999
EOF
  }
  upload {
	file = "/etc/krb5.conf"
	content =<<EOF
[libdefaults]
	default_realm = ${docker_container.samba-server.domainname}
	dns_lookup_realm = false
	dns_lookup_kdc = true
	forwardable = true
	proxiable = true
	default_keytab_name = FILE:/etc/krb5.keytab

[realms]
	${upper(docker_container.samba-server.domainname)} = {
		kdc = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
		admin_server = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
	}
EOF
  }
  
  # This avoids having to put the admin password in an environment
  # variable or file or something accessible w/in the container.
  provisioner "local-exec" {
	command = "docker exec ${self.name} net ads join -U administrator%${var.sambaAdminPassword}"
  }

  provisioner "local-exec" {
	command = "${lookup(local.clientScript, count.index)}"
  }
}
