# Start a samba client
#

resource "docker_image" "client-image" {
  name = "fedora"
  keep_locally = true
}

resource "docker_container" "client-image" {
  name = "client1"
  image = "${docker_image.client-image.latest}"
  command = ["/sbin/init"]
  env = [
	"SAMBA_REALM=SAVIOR.NEXTCENTURY.COM",
  ]
}
