sudo mkdir -p /home/ec2-user/app-domains/${guestVmTemplate.templatePath} && sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${guestVmTemplate.templatePath}/ /home/ec2-user/app-domains/${guestVmTemplate.templatePath}
