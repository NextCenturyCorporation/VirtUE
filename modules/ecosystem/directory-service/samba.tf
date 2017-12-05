# Start a samba domain controller
#

variable "sambaDomain" {
  description = "Domain name"
  default = "SAVIOR.NEXTCENTURY.COM"
}

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

  # Can't directly disable the default docker bridge network. See
  # https://github.com/terraform-providers/terraform-provider-docker/issues/10
  provisioner "local-exec" {
	command = "docker network disconnect ${var.docker_default_network} ${self.name}"
  }
}
