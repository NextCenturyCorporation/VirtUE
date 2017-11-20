# Start a samba domain controller
#

provider "docker" {
  version = "~> 0.1"
}

variable "sambaAdminPassword" {
  # Set this in terraform.tfvars and it will get automatically included
  description = "Administrator for AD DC"
}

variable "sambaDomain" {
  description = "Domain name"
  default = "SAVIOR.NEXTCENTURY.COM"
}

resource "docker_container" "samba-server" {
  entrypoint = [ "/sbin/init" ]
  name = "saviordc"
  image = "samba-savior"
  hostname = "saviordc"
  domainname = "${var.sambaDomain}"
  networks = ["savior_network"]
  dns_search = [ "${var.sambaDomain}" ]
  dns = [ "127.0.0.1" ]
  # Need privileged for the xattr that samba needs to work
  privileged = true
  must_run = true
  env = [
	"SAMBA_REALM=${var.sambaDomain}",
	"SAMBA_ADMIN_PASSWORD=${var.sambaAdminPassword}"
  ]
#  volumes {
#	host_path = "${path.cwd}/samba-config"
#	container_path = "/var/lib/samba"
#  }
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
}
