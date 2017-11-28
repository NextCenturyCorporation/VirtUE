SUBDIRS := samba-client samba-server

BUILD_TARGETS = $(addprefix build-,$(SUBDIRS))

.PHONY: build $(BUILD_TARGETS)

build: $(BUILD_TARGETS)

build-%: %
	$(MAKE) -C $< build
