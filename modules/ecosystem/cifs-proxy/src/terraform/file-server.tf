// factor out raw text to make any future refactoring/reuse easier

locals {
  fsname = "fileserver"
  rolePolicy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
               "Service": "ec2.amazonaws.com"
            },
            "Effect": "Allow",
            "Sid": ""
        }
    ]
}
EOF

  domainJoinPolicy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowAccessToSSM",
            "Effect": "Allow",
            "Action": [
                "ssm:DescribeAssociation",
                "ssm:ListAssociations",
                "ssm:GetDocument",
                "ssm:ListInstanceAssociations",
                "ssm:UpdateAssociationStatus",
			    "ssm:UpdateInstanceAssociationStatus",
                "ssm:UpdateInstanceInformation",
                "ec2messages:AcknowledgeMessage",
                "ec2messages:DeleteMessage",
                "ec2messages:FailMessage",
                "ec2messages:GetEndpoint",
                "ec2messages:GetMessages",
                "ec2messages:SendReply",
                "ds:CreateComputer",
                "ds:DescribeDirectories",
                "ec2:DescribeInstanceStatus"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
EOF
}

resource "aws_iam_instance_profile" "instance_profile_file_server" {
  name  = "INSTANCE_PROFILE_FILE_SERVER"
  role = "${aws_iam_role.iam_role_file_server.name}"
}

resource "aws_iam_role" "iam_role_file_server" {
  name = "IAM_ROLE_FILE_SERVER"
  path = "/"

  assume_role_policy = "${local.rolePolicy}"
}

resource "aws_iam_role_policy" "policy_allow_all_ssm" {
  name = "IAM_POLICY_ALLOW_ALL_SSM"
  role = "${aws_iam_role.iam_role_file_server.id}"
  policy = "${local.domainJoinPolicy}"
}

resource "aws_ssm_document" "file_server_default_doc" {
  name  = "file_server_default_doc"
  document_type = "Command"
  content = <<EOF
{
        "schemaVersion": "1.0",
        "description": "Join an instance to a domain (inline doc)",
        "runtimeConfig": {
           "aws:domainJoin": {
               "properties": {
                  "directoryId": "${aws_directory_service_directory.active_directory.id}",
                  "directoryName": "${var.domain}",
                  "dnsIpAddresses": [
                     "${aws_directory_service_directory.active_directory.dns_ip_addresses[0]}",
                     "${aws_directory_service_directory.active_directory.dns_ip_addresses[1]}"
                  ]
               }
           }
        }
}
EOF
}

resource "aws_ssm_association" "file_server" {
  name = "file_server_default_doc"
  instance_id = "${aws_instance.file_server.id}"
  depends_on = ["aws_ssm_document.file_server_default_doc"]
}

resource "aws_instance" "file_server" {
  ami           = "${data.aws_ami.windows_server2016.image_id}"
  instance_type = "${var.instance_type}"
  key_name      = "vrtu"
  iam_instance_profile = "${aws_iam_instance_profile.instance_profile_file_server.name}"

  vpc_security_group_ids = [ "${data.aws_security_group.sg.*.id}" ]
  subnet_id = "${data.aws_subnet.public_subnet.id}"

  tags {
	Name = "${local.fsname}"
	Owner = "${data.external.local_user.result.user}"
	class = "cifs"
	automated = "terraform"
  }
  lifecycle {
	prevent_destroy = false
	#    ignore_changes = ["user_data"]
  }

  connection {
	type     = "winrm"
	user     = "Administrator"
	password = "${var.admin_password}"
	https    = true
	
	# set from default of 5m to 10m to avoid winrm timeout
	timeout = "10m"
  }

  user_data = <<EOF
<powershell>
  Start-Transcript -Path "c:\user_data.log" -append -force 
  echo Setting password
  net user Administrator "${var.admin_password}"
  Clear-ADAccountExpiration -Identity Admin
  Clear-ADAccountExpiration -Identity Administrator
  echo Setting up delegation
  Add-WindowsFeature RSAT-AD-PowerShell
  Import-Module ActiveDirectory
  
  echo Setup done  
</powershell>
EOF

  # can't join the domain until the AD server is up
  depends_on = [ "aws_directory_service_directory.active_directory" ]
}
