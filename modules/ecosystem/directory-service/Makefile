#
# Makefile for running terraform to start samba AD DS server and
# client docker containers.
#

# for viewing the terraform graph
IMAGE_VIEWER := display
IMAGE_FORMAT := png

SAMBA_CONFIG_DIR := samba-config
export TF_VAR_SAMBA_CONFIG_DIR := \
	$(shell readlink --canonicalize $(SAMBA_CONFIG_DIR))
# pick the first "nameserver" line from resolv.conf
export TF_VAR_DNS_SERVER := \
	$(shell awk '$$1 == "nameserver" { print $$2; exit 0; } END { exit 1; }' /etc/resolv.conf)
ifndef TF_VAR_DNS_SERVER
$(error Cannot determine DNS server from /etc/resolv.conf)
endif

.PHONY: init run plan clobber show-graph

## Run the terraform plan
run: init $(SAMBA_CONFIG_DIR)
	terraform apply -input=false

## Initialize terraform (don't normally need to explicitly invoke this)
init: .terraform secrets.auto.tfvars
	terraform init -input=false

# makes the secrets file automaticaly if it doesn't exist
secrets.auto.tfvars: secrets.auto.tfvars-example
	@[ -f $@ ] || ( \
	echo "warning: no secrets file '$@', creating random secrets. It's recommended that you edit '$@'." 1>&2 ; \
	RANDOMPASS=`tr -dc _A-Z-a-z-0-9 < /dev/urandom | head -c12`; \
	sed "s/PutYourPasswordHere/$${RANDOMPASS}/" $< > $@ \
	)

## Show the current terraform plan
plan:
	terraform plan -input=false

$(SAMBA_CONFIG_DIR):
	mkdir -p $@

# Not calling this "clean" because that usually refers to things that
# compilation regenerates. This will wipe out any domain info we've
# built up.
## WARNING: This removes the config for the samba AD DS. It wipes out your domain info.
clobber:
	rm -rf ${SAMBA_CONFIG_DIR} secrets.auto.tfvars

## Show the terraform plan graph
show-graph:
	terraform graph | dot -Tpng | $(IMAGE_VIEWER) -

include Makefile-help
