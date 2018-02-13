resource "docker_network" "savior_network" {
  name = "${var.sambaDomain}"
}
