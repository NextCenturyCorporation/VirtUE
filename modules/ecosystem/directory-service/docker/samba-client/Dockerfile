FROM 

LABEL description="Baseline for SAVIOR client" \
	  name="savior-client"

RUN yum -y update && \
	yum -y install samba-client

CMD ["/usr/sbin/init"]
