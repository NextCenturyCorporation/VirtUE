SUBDIRS := samba-client samba-server

build: $(addprefix build-,$(SUBDIRS))

build-%: %
	$(MAKE) -C $< build
