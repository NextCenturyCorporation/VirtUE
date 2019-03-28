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
# Start a samba domain controller
#

variable "SAMBA_CONFIG_DIR" {
  description = "Location of samba configuration files on host"
}

variable "DNS_SERVER" {
  description = "IP address of DNS server the host uses, and therefore the AD DS forward to use"
}

resource "docker_container" "samba-server" {
  # to prevent samba configuration on startup, uncomment the following line
  #entrypoint = [ "/sbin/init" ]
  name = "saviordc"
  image = "savior-dc"
  hostname = "saviordc"
  domainname = "${var.sambaDomain}"
  networks = ["${docker_network.savior_network.name}"]
  # need to use the AD DNS as the primary one
  dns = [ "127.0.0.1" ]
  dns_search = [ "${var.sambaDomain}" ]
  # Need privileged for the xattr that samba needs to work
  privileged = true
  must_run = true
  env = [
	"SAMBA_REALM=${var.sambaDomain}",
	"SAMBA_ADMIN_PASSWORD=${var.sambaAdminPassword}",
	"SAMBA_DNS=${var.DNS_SERVER}",
  ]
  volumes {
	host_path = "${var.SAMBA_CONFIG_DIR}"
	container_path = "/var/lib/samba"
  }
  # for debugging
  volumes {
	host_path = "/etc/resolv.conf"
	container_path = "/etc/resolv.conf-host"
  }
  publish_all_ports = true
  ports {
	internal = 135
	external = 135
  }
  ports {
	internal = 137
	external = 137
  }
  ports {
	internal = 138
	external = 138
  }
  ports {
	internal = 139
	external = 139
  }
  ports {
	internal = 445
	external = 445
  }

  # Don't let the terraform consider the container ready until samba
  # is actually up and running.
  provisioner "local-exec" {
	# test readiness using DNS (port 53)
	command =<<EOF
while ! nc -z $(docker inspect --format '{{(index .NetworkSettings.Networks "${docker_network.savior_network.name}").IPAddress}}' ${self.name}) 53; do
  sleep 5;
done
EOF
  }
}
