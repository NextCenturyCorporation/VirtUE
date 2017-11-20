FROM centos:latest

LABEL description="Baseline for SAVIOR client" \
	  name="savior-client"

# Make a baseline with the first update so we update less for future
# runs when we need to change what we need to be installed.
RUN yum -y update
RUN yum -y update && \
	yum -y install samba-client krb5-workstation && \
	yum clean all

CMD ["/usr/sbin/init"]
