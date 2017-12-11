# Start a samba client
#

locals {
  clientNames = {
	"0" = "client1"
	"1" = "savior-firefox"
  }
  clientHostnames = {
	"0" = "client1"
	"1" = "ff"
  }
  clientImages = {
	"0" = "savior-client"
	"1" = "savior-firefox"
  }
}

resource "docker_container" "clients" {
  count = "2"
  name = "${lookup(local.clientNames, count.index)}"
  hostname = "${lookup(local.clientHostnames, count.index)}"
  image = "${lookup(local.clientImages, count.index)}"
  domainname = "${docker_container.samba-server.domainname}"
  command = ["/sbin/init"]
  networks = ["${docker_network.savior_network.name}"]
  dns = [ "${docker_container.samba-server.ip_address}" ]
  dns_search = [ "${docker_container.samba-server.domainname}" ]

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
	SAVIOR = {
		kdc = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
		admin_server = ${docker_container.samba-server.hostname}.${docker_container.samba-server.domainname}
	}
EOF
  }
  
  # This avoids having to put the admin password in an environment
  # variable or file or something accessible w/in the container.
  provisioner "local-exec" {
	command = "docker exec ${self.name} net ads join -U administrator%${var.sambaAdminPassword}"
  }
}
