tvEval = $(foreach f,$(1),$(shell echo "$(f)" | terraform console))

PROXY_JAR := $(call tvEval,var.proxy_jar)
NETPLAN_DEB := $(call tvEval,var.netplan_deb)
KERBEROS_HELPERS := \
	$(call tvEval,format(\"%s/%s\",var.helper_program_location,var.import_creds_program)) \
	$(call tvEval,format(\"%s/%s\",var.helper_program_location,var.switch_principal_program))
EXTERNAL_FILES := $(PROXY_JAR) $(NETPLAN_DEB) $(KERBEROS_HELPERS)

.PHONY: plan apply next Destroy external

plan:
	terraform plan -out=.plan.out | tee .plan.log

apply: .plan.out external
	terraform apply .plan.out && mv -f .plan.out .plan.out.last | tee .apply.log

next:
	if [ -f .plan.out ] then \
		$(MAKE) apply ; \
	else \
		$(MAKE) plan \
	fi

Destroy:
	terraform destroy -auto-approve | tee .destroy.log

# how to build external files we need:

external: $(EXTERNAL_FILES)

$(PROXY_JAR):
	cd ../.. && ./gradlew bootJar

$(NETPLAN_DEB):
	$(MAKE) -C ../../../netplan

# intermediate target gets make to do just one build to make all KERBEROS_HELPERS
$(KERBEROS_HELPERS): kerberos-helpers.intermediate ;

.INTERMEDIATE: kerberos-helpers.intermediate
kerberos-helpers.intermediate:
	cd ../.. && cmake . && $(MAKE)
