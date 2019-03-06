#!/bin/bash
#
# Process for testing tickets
#
# First must terraform to set up the environment.
# Then, the cifs-proxy ancestor directory, execute "make && ./gradlew bootDeploy nativeDeploy"
#

set -e

sudo pkill java

sudo kinit -k http/cifs-proxy.test.savior
sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar /usr/local/lib/cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&
while ! curl http://cifs-proxy:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done
echo server running
echo 'Test1234.' | kinit bob
docsVirtue=$(curl --negotiate -u bob: http://cifs-proxy:8080/virtue/ -H 'Content-Type: application/json' -d '{"name":"Docs","id":"docs","username":"docs"}')
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
cd /mnt/cifs-proxy/docs/Docs\ test\ share

password=$(echo ${docsVirtue} | jq --raw-output '.password')
mkdir /tmp/mnt
sudo mount -t cifs //localhost/Docs\ test\ share /tmp/mnt -o username=docs,password="${password}"
ls -l /tmp/mnt/hello.txt
sudo touch /tmp/mnt/foo
echo foo should exist
ls -l /tmp/mnt
sudo rm /tmp/mnt/foo
echo foo should NOT exist
ls -l /tmp/mnt
