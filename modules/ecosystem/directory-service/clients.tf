# Start a samba client
#

resource "docker_container" "client-image" {
  name = "client1"
  hostname = "client1"
  image = "savior-client"
  domainname = "${docker_container.samba-server.domainname}"
  networks = ["${docker_network.savior_network.name}"]
  command = ["/sbin/init"]
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]

  # Can't directly disable the default docker bridge network. See
  # https://github.com/terraform-providers/terraform-provider-docker/issues/10
  provisioner "local-exec" {
	command = "docker network disconnect ${var.docker_default_network} ${self.name}"
  }

  # This avoids having to put the admin password in an environment
  # variable or file or something accessible w/in the container.
  provisioner "local-exec" {
	command = "docker exec ${docker_container.client-image.name} net ads join -U administrator%${var.sambaAdminPassword}"
  }
}
