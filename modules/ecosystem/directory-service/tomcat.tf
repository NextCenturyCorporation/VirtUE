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
  description = "The configuration directory for tomcat (inside its container)."
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
  #  image = "${docker_image.tomcat.latest}"
  image = "savior-client"
  name = "saviorvc"
  hostname = "saviorvc"
  domainname = "${docker_container.samba-server.domainname}"
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]
  networks = ["${docker_network.savior_network.name}"]
  publish_all_ports = true
  ports {
	internal = 443
	external = "${var.saviorvcPort}"
  }
  volumes {
	host_path = "${path.module}/tomcat"
	container_path = "/var/lib/ssk"
  }
  upload {
	file = "/etc/samba/smb.conf"
	content =<<EOF
[global]
	security = ADS
	workgroup = SAVIOR
	realm = ${docker_container.samba-server.domainname}
	client schannel = yes
	client signing = yes
	kerberos method = secrets and keytab

	log file = /var/log/samba/%m.log
	log level = 1

	# Default ID mapping
	idmap config * : backend = tbd
	idmap config * : range = 3000-7999
EOF
  }
  upload {
	file = "/etc/krb5.conf"
	content =<<EOF
[libdefaults]
	default_realm = ${docker_container.samba-server.domainname}
	dns_lookup_realm = false
	dns_lookup_kdc = true
	forwardable = true
	proxiable = true
	default_keytab_name = FILE:/etc/krb5.keytab

[realms]
	${upper(docker_container.samba-server.domainname)} = {
		kdc = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
		admin_server = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
	}
EOF
  }
  #entrypoint = [ "/sbin/init" ]

  # This avoids having to put the admin password in an environment
  # variable or file or something accessible w/in the container.
  provisioner "local-exec" {
	command = "docker exec ${self.name} net ads join -U administrator%${var.sambaAdminPassword}"
  }

  provisioner "local-exec" {
	command = "./register-service.sh ${self.name} ${docker_network.savior_network.name} ${var.sambaAdminPassword} ${var.tomcatConfigDir}/tomcat.keytab"
  }
}
