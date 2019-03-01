sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${templatePath}/disk.qcow2 /home/ec2-user/app-domains/${guestVm.name}/disk.qcow2
