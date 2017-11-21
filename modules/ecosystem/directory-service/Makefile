
SAMBA_CONFIG_DIR := samba-config
export TF_VAR_SAMBA_CONFIG_DIR := \
	$(shell readlink --canonicalize $(SAMBA_CONFIG_DIR))
export TF_VAR_DNS_SERVER := \
	$(shell awk '$$1 == "nameserver" { print $$2; exit 0; } END { exit 1; }' /etc/resolv.conf)
ifndef TF_VAR_DNS_SERVER
$(error Cannot determine DNS server from /etc/resolv.conf)
endif

.PHONY: init run plan

run: init ${SAMBA_CONFIG_DIR}
	terraform apply -input=false

# we may need to init other times, but definitely if there's no .terraform dir
init: .terraform
	terraform init -input=false

plan:
	terraform plan -input=false

${SAMBA_CONFIG_DIR}:
	mkdir -p $@
