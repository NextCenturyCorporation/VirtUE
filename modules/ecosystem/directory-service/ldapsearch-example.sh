ldapsearch -Y GSSAPI -D "DC=savior,DC=local" -b "DC=savior,DC=local" -H ldap://172.18.0.2 "(| (userPrincipalName=test) (sAMAccountName=test))"
#ldapsearch -D "CN=Administrator,CN=Users,DC=savior,DC=local" -b "DC=savior,DC=local" -x -H ldap://172.18.0.2 -w Sav1or.. "(cn=Administrator)"
