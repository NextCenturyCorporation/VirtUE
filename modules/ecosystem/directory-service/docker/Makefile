SUBDIRS := samba-client samba-server savior-firefox

BUILD_TARGETS = $(addprefix build-,$(SUBDIRS))

.PHONY: build $(BUILD_TARGETS)

build: $(BUILD_TARGETS)

build-%: %
	$(MAKE) -C $< build

build-savior-firefox: | build-samba-client
