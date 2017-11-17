# Start a samba domain controller
#

provider "docker" {
  version = "~> 0.1"
}

variable "sambaAdminPassword" {
  description = "Administrator for AD DC"
}

resource "docker_container" "samba-server" {
  name = "samba-server"
#  image = "${docker_image.samba-server.latest}"
  image = "samba-savior"
  privileged = true
  must_run = true
  env = [
	"SAMBA_REALM=SAVIOR.NEXTCENTURY.COM",
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
