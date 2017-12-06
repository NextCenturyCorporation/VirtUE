#
# Start a SAVIOR tomcat server
#
# Default environment is
#
# CATALINA_BASE:   /usr/local/tomcat
# CATALINA_HOME:   /usr/local/tomcat
# CATALINA_TMPDIR: /usr/local/tomcat/temp
# JRE_HOME:        /usr
# CLASSPATH:       /usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat

variable "saviorvcPort" {
  description = "The https port the saviorvc will expose."
  default = "443"
}

variable "tomcatConfigDir" {
  description = "The configuration directory for tomcat."
  default = "/usr/local/tomcat/conf"
}

data "docker_registry_image" "tomcat" {
  name = "tomcat:8.5"
}

resource "docker_image" "tomcat" {
  name          = "${data.docker_registry_image.tomcat.name}"
  pull_triggers = ["${data.docker_registry_image.tomcat.sha256_digest}"]
}

# vc for Virtue Controller
resource "docker_container" "saviorvc" {
  image = "${docker_image.tomcat.latest}"
  name = "saviorvc"
  hostname = "saviorvc"
  domainname = "${docker_container.samba-server.domainname}"
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]
  publish_all_ports = true
  ports {
	internal = 443
	external = "${var.saviorvcPort}"
  }

  provisioner "local-exec" {
	command = "./register-service.sh ${self.name} ${var.sambaAdminPassword} ${var.tomcatConfigDir}/tomcat.keytab"
  }
}
