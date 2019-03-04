#!/bin/bash
#
# Process for testing tickets
#
# First must terraform to set up the environment.
# Then, the cifs-proxy ancestor directory, execute "make && ./gradlew bootDeploy nativeDeploy"
#

sudo kinit -k http/cifs-proxy.test.savior
sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&
while ! curl http://cifs-proxy:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done
echo server running
echo 'Test1234.' | kinit bob
curl -v --negotiate -u bob: http://cifs-proxy:8080/virtue/ -H 'Content-Type: application/json' -d '{"name":"Docs","id":"docs","username":"docs"}'
cat > new-share.json <<EOF
{
	"name": "Docs test share",
	"virtueId": "docs",
	"server": "fileserver.test.savior",
	"path": "/TestShare",
	"permissions": [ "READ", "WRITE" ],
	"type": "CIFS"
}
EOF
curl -v --negotiate -u bob: http://cifs-proxy:8080/share/ -H 'Content-Type: application/json' -d @new-share.json
ls -l /mnt/cifs-proxy/docs/Docs\ test\ share/hello.txt
