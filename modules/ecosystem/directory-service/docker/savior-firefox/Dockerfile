FROM savior-client

LABEL description="SAVIOR Firefox container" \
	  name="savior-firefox"

RUN yum -y update
# Installing firefox to get dependencies for Firefox ESR, but it
# misses gtk3
RUN yum -y update && \
	yum -y install \
		deltarpm \
		bzip2 \
		firefox \
		gtk3 \
		openssh-clients \
		openssh-server \
		xauth && \
	yum clean all
RUN curl --location 'https://download.mozilla.org/?product=firefox-esr-latest-ssl&os=linux64&lang=en-US' | \
	tar -C /usr/local/lib -xjf - && \
	ln -s /usr/local/lib/firefox/firefox /usr/local/bin
RUN ssh-keygen -A && \
	useradd -c 'Firefox user' --create-home ff && \
	mkdir ~ff/.ssh && \
	echo 'AddressFamily inet' >> /etc/ssh/sshd_config
COPY id_rsa.pub /home/ff/.ssh/authorized_keys
RUN chown -R ff:ff ~ff/.ssh

CMD ["/usr/sbin/sshd", "-D"]


# TODO
# add this:
# user_pref("network.negotiate-auth.trusted-uris", "172.18.0.4");
# to pref.js in ~/.mozilla/*.default*
# but need to substitute the address of saviorvc (or maybe saviorvc would work)
