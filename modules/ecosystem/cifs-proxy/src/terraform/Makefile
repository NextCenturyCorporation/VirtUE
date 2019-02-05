plan:
	terraform plan -out=.plan.out | tee .plan.log

apply: .plan.out
	terraform apply .plan.out && mv -f .plan.out .plan.out.last | tee .apply.log

next:
	if [ -f .plan.out ] then \
		$(MAKE) apply ; \
	else \
		$(MAKE) plan \
	fi

Destroy:
	terraform destroy -auto-approve | tee .destroy.log
