sudo /usr/local/bin/post-deploy-config.sh --domain ${cifsDomain} --admin ${domainAdmin} --password ${domainPassword} --hostname ${cifsHostname} --dcip ${domainIp} --verbose &> post-deploy.log
$?
echo 'savior.cifsproxy.principal=http/${cifsHostname}.${cifsDomain}' > cifs-proxy.properties
echo 'savior.security.ad.domain=${cifsDomain}' >> cifs-proxy-security.properties;
echo 'savior.security.ad.url=${cifsAdUrl}' >> cifs-proxy-security.properties;
echo 'savior.security.ldap=${cifsAdUrl}' >> cifs-proxy-security.properties;
echo 'Echo *.properties';cat *.properties
echo sleeping; sleep 20; echo slept;
sudo kinit -k ${principal}
sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -Dlogging.config.disabled=/home/ubuntu/trace-logback.xml -jar /usr/local/lib/cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&
#while ! curl http://${cifsHostname}:${cifsPort}/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done; echo 'Connected'
export counter=0; while ! curl http://localhost:8080/hello 2> /dev/null ; do echo -n "." ;((counter++)); sleep 1; if [ $counter = "45" ]; then break; fi; done; echo 'Done'