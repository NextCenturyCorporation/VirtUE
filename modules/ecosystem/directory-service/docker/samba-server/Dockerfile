# The CentOS/RHEL build doesn't include Active Directory. Using Fedora
# because it is the most similar distro that does.
ARG FEDORA_VERSION=latest

FROM fedora:${FEDORA_VERSION}

LABEL description="Image for SAVIOR samba server" \
	  name="savior-client"

ARG SAMBA_REALM=SAVIOR.NEXTCENTURY.COM

# Make a layer with some level of updates so we don't have to update
# everything from the base OS image every time.
RUN yum -y update

# Now install what we actually need (with update first to make sure
# we're up to date.)
RUN yum -y update && \
	yum -y install \
		samba samba-dc \
		krb5-server-ldap krb5-workstation \
		bind-utils \
		iproute \
		procps-ng && \
	yum clean all
# Need procps-ng for ps command; can remove for production version.
# iproute provides ip command; can remove for production version.
RUN rm -rf /etc/samba/smb.conf /etc/krb5.conf /var/lib/samba/*

COPY init-or-run-samba.sh /usr/local/sbin/init-or-run-samba

ENTRYPOINT ["/usr/local/sbin/init-or-run-samba"]

# TODO: add winbind support
