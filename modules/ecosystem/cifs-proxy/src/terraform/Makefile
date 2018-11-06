
.IGNORE: .plan.out

plan:
	terraform plan -out=.plan.out

apply: .plan.out
	terraform apply .plan.out && mv -f .plan.out .plan.out.last

next:
	[ -f .plan.out ] && $(MAKE) apply || $(MAKE) plan

Destroy:
	terraform destroy -auto-approve
