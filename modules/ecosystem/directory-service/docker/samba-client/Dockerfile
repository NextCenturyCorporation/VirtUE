FROM centos:latest

LABEL description="Baseline for SAVIOR client" \
	  name="savior-client"

# Make a baseline with the first update so we update less for future
# runs when we need to change what we need to be installed.
RUN yum -y update
RUN yum -y update && \
	yum -y install samba samba-client krb5-workstation
# debug tools
RUN yum -y update && \
	yum -y install bind-utils iproute procps-ng && \
	yum clean all

RUN mv -f /etc/samba/smb.conf /etc/samba/smb.conf-orig && \
	mv -f /etc/krb5.conf /etc/krb5.conf-orig
COPY smb.conf /etc/samba
COPY krb5.conf /etc

CMD ["/usr/sbin/init"]
