
DOWNLOAD_SITE := http://deb.debian.org/debian/pool/main/n/netplan.io

DOWNLOADABLES := netplan.io_0.95-2.dsc netplan.io_0.95.orig.tar.gz netplan.io_0.95-2.debian.tar.xz

TARGET := netplan.io_0.95-2_amd64.deb

all: netplan.io_0.95-2_amd64.deb

$(TARGET): .patch-applied
	cd netplan.io-0.95 && debuild -i -us -uc -b

.patch-applied: netplan.io-0.95
	sed -i -e 's/ systemd (>= 239~),/ systemd (>= 237~),/' $</debian/control
	touch $@

netplan.io-0.95: $(DOWNLOADABLES)
	dpkg-source -x netplan.io_0.95-2.dsc

$(DOWNLOADABLES):
	wget $(DOWNLOAD_SITE)/$@

clean:
	rm -rf .patch-applied netplan.io-0.95 $(TARGET) netplan.io_0.95-2_amd64.{build,buildinfo,changes}

clobber: clean
	rm -f $(DOWNLOADABLES)
