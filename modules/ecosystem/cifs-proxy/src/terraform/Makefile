tvEval = $(foreach f,$(1),$(shell echo "$(f)" | terraform console))

NETPLAN_DEB := $(call tvEval,var.netplan_deb)
EXTERNAL_FILES := proxy-jar $(NETPLAN_DEB) kerberos-helpers

.PHONY: plan apply next Destroy external proxy-jar kerberos-helpers

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

# Always ask gradle to remake this because we can't tell from here if
# it's up to date or not (but gradle can).
proxy-jar:
	cd ../.. && ./gradlew bootJar

$(NETPLAN_DEB):
	$(MAKE) -C ../../../netplan

# Always ask cmake+make to remake these because we can't tell from
# here if they're out of date or not (but cmake+make can).
kerberos-helpers:
	cd ../.. && cmake . && $(MAKE)
