[${exportedName}]
	path = /var/spool/samba
	printable = yes
	printer name = ${name}
	lpq command = /usr/bin/sudo -u ${localUser} /usr/bin/smbclient --kerberos -c queue '${serviceName}'
	lprm command = /usr/bin/sudo -u ${localUser} /usr/bin/smbclient --kerberos -c 'cancel %j' '${serviceName}'
	print command = /usr/bin/sudo -u ${localUser} /usr/bin/smbspool 0 '${domainUser}' '%f' 1 '' '%s' ; /bin/rm '%s'
