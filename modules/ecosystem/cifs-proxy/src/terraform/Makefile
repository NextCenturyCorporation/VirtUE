
.IGNORE: .plan.out

plan:
	terraform plan -out=.plan.out

apply: .plan.out
	terraform apply .plan.out && mv -f .plan.out .plan.out.last

next:
	if [ -f .plan.out ] then \
		$(MAKE) apply ; \
	else \
		$(MAKE) plan \
	fi

Destroy:
	terraform destroy -auto-approve
