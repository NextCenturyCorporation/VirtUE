sudo ~/post-deploy-config.sh --domain ${cifsDomain} --admin ${domainAdmin} --password ${domainPassword} --hostname ${cifsHostname} --dcip ${domainIp} --verbose &> post-deploy.log
$?
echo 'savior.cifsproxy.principal=http/${cifsHostname}.${cifsDomain}' > cifs-proxy.properties
echo 'savior.security.ad.domain=${cifsDomain}' >> cifs-proxy-security.properties;
echo 'savior.security.ad.url=${cifsAdUrl}' >> cifs-proxy-security.properties;
echo 'savior.security.ldap=${cifsAdUrl}' >> cifs-proxy-security.properties;
echo 'Echo *.properties';cat *.properties
echo sleeping; sleep 30; echo slept;
sudo kinit -k ${principal}
while ! curl http://${cifsHostname}:${cifsPort}/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done; echo 'Connected'