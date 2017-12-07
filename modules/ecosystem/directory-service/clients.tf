# Start a samba client
#

resource "docker_container" "client-image" {
  name = "client1"
  hostname = "client1"
  image = "savior-client"
  domainname = "${docker_container.samba-server.domainname}"
  command = ["/sbin/init"]
  networks = ["${docker_network.savior_network.name}"]
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]

  # This avoids having to put the admin password in an environment
  # variable or file or something accessible w/in the container.
  provisioner "local-exec" {
	command = "docker exec ${self.name} net ads join -U administrator%${var.sambaAdminPassword}"
  }
}
