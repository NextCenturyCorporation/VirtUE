
SAMBA_CONFIG_DIR := samba-config
export TF_VAR_SAMBA_CONFIG_DIR := \
	$(shell readlink --canonicalize $(SAMBA_CONFIG_DIR))

.DEFAULT: run

TF_FILES := $(wildcard *.tf)

# we may need to init other times, but definitely if there's no .terraform dir
init: .terraform
	terraform init -input=false

run: $(TF_FILES) init ${SAMBA_CONFIG_DIR}
		terraform apply -input=false

plan:
		terraform plan -input=false

${SAMBA_CONFIG_DIR}:
	mkdir -p $@
