#!/bin/bash
#
# Process for testing tickets
#
# First must terraform to set up the environment.
# Then, the cifs-proxy ancestor directory, execute "make && ./gradlew bootDeploy nativeDeploy"
#

sudo chmod a+r /etc/krb5.keytab
sudo kinit -k http/webserver.test.savior
sudo nohup env KRB5_TRACE=/dev/stdout java -jar cifs-proxy-server-0.0.1.jar >& s.log&
while ! curl http://webserver:8080/hello 2> /dev/null ; do echo -n '.' ; sleep 1; done
echo server running
echo 'Test1234.' | kinit bob
curl -v --negotiate -u bob: http://webserver:8080/protected
kinit -k http/webserver.test.savior
sudo install -o fedora /tmp/cifsproxy ccache # can use any filename for ccache
chmod a+x importcreds switchprincipal 
./importcreds ccache # can use any filename for ccache
kvno -k /etc/krb5.keytab -P -U bob cifs/fileserver.test.savior # this fails if delegation is not enabled in the AD
./switchprincipal bob
sudo mount -t cifs //fileserver.test.savior/TestShare /mnt -v -o sec=krb5,cruid=$USER
