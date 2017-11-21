# Start a samba client
#

resource "docker_container" "client-image" {
  name = "client1"
  hostname = "client1"
  image = "savior-client"
  domainname = "${docker_container.samba-server.domainname}"
  networks = ["savior_network"]
  command = ["/sbin/init"]
  env = [
	"SAMBA_REALM=${docker_container.samba-server.domainname}",
  ]
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]
  links = [ "${docker_container.samba-server.name}" ]
  host {
	host = "${docker_container.samba-server.name}.${docker_container.samba-server.domainname}"
	ip = "${docker_container.samba-server.ip_address}"
  }
}
