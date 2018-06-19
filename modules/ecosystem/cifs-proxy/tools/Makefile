#
# Download ec2 ansible helper files and install a Python module they
# need (boto), if necessary
#
# Based on https://docs.ansible.com/ansible/2.5/user_guide/intro_dynamic_inventory.html#example-aws-ec2-external-inventory-script

init: ec2.py ec2.ini install-boto

ec2.py:
	wget https://raw.github.com/ansible/ansible/devel/contrib/inventory/ec2.py
	chmod a+x $@

# should only be needed if something happened to the customized one
ec2.ini:
	wget https://raw.githubusercontent.com/ansible/ansible/devel/contrib/inventory/ec2.ini

install-boto:
	@echo Checking Python module boto
	@python -c 'import boto' || \
	(echo Python module boto needs to be installed; sudo apt-get install python-boto)
