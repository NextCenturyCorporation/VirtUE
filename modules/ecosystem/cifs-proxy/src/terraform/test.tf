#
# stuff for running tests
#

resource "local_file" "test_remote_setup_sh" {
  content = <<EOF
#!/bin/bash
# Created automatically by test.tf. To change, edit test.tf and re-run terraform.
#
sudo apt-get install -y jq

sudo /usr/local/bin/post-deploy-config.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password '${var.admin_password}' --hostname ${local.myname} --dcip ${local.ds_private_ip} --verbose
sleep 5
sudo /usr/local/bin/allow-delegation.sh --domain ${var.domain} --admin ${var.domain_admin_user} --password '${var.admin_password}' --delegater ${local.myname} --target ${local.fsname} --verbose
EOF
  filename = "${path.module}/test-remote-setup.sh"
}
