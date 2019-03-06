#!/bin/bash
#
# Process for testing tickets
#
# First must terraform to set up the environment.
# Then, the cifs-proxy ancestor directory, execute "make && ./gradlew bootDeploy nativeDeploy"
#

set -e

sudo pkill java || true

sudo kinit -k http/cifs-proxy.test.savior
sudo nohup env KRB5_TRACE=/dev/stdout java -Xint -jar /usr/local/lib/cifs-proxy-server-0.0.1.jar --spring.config.location=cifs-proxy.properties,cifs-proxy-security.properties >& proxy.log&
sudoPid=$!
while ! curl http://cifs-proxy:8080/hello 2> /dev/null ; do
	if ! ps $sudoPid > /dev/null ; then
		echo $0: error: cifs proxy failed to start, check proxy.log 1>&2
		exit 1
	fi
	echo -n '.' ;
	sleep 1
done
echo server running
echo 'Test1234.' | kinit bob
curl --negotiate -u bob: http://cifs-proxy:8080/virtue/ -H 'Content-Type: application/json' -d '{"name":"Docs","id":"docs","username":"docs"}'
docsVirtue=$(curl --negotiate -u bob: http://cifs-proxy:8080/virtue/docs)
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
curl --negotiate -u bob: http://cifs-proxy:8080/share/ -H 'Content-Type: application/json' -d @new-share.json
ls -l /mnt/cifs-proxy/docs/Docs\ test\ share/hello.txt
cd /mnt/cifs-proxy/docs/Docs\ test\ share

password=$(echo ${docsVirtue} | jq --raw-output '.password')
mkdir -p /tmp/mnt
sudo umount /tmp/mnt || true
sudo mount -t cifs //localhost/Docs\ test\ share /tmp/mnt -o username=docs,password="${password}"
ls -l /tmp/mnt/hello.txt
sudo touch /tmp/mnt/foo
echo foo should exist
ls -l /tmp/mnt
sudo rm /tmp/mnt/foo
echo foo should NOT exist
ls -l /tmp/mnt
sudo umount /tmp/mnt

curl --negotiate -u bob: -X DELETE http://cifs-proxy:8080/share/Docs%20test%20share
curl --negotiate -u bob: -X DELETE http://cifs-proxy:8080/virtue/docs
echo Remaining shares:
curl --negotiate -u bob: http://cifs-proxy:8080/share
echo Remaining virtues:
curl --negotiate -u bob: http://cifs-proxy:8080/virtue
