# packages to install:
# epel-release rpmbuild

build:
	docker build -t samba-savior .

env SAMBA_REALM=SAVIOR.NEXTCENTURY.COM SAMBA_PASSWORD=Savior17 docker run -i -t -e SAMBA_REALM -e SAMBA_PASSWORD -e SAMBA_DNS_FORWARDER=192.168.1.1  pitkley/samba-ad-dc
