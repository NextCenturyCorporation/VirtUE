sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${templatePath}/disk.qcow2 /home/ec2-user/app-domains/${xenVm.name}/${xenVm.name}.qcow2
sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${templatePath}/swap.qcow2 /home/ec2-user/app-domains/${xenVm.name}/${xenVm.name}_swap.qcow2
sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${templatePath}/initrd.img-4.2.0-42-generic /home/ec2-user/app-domains/${basePath}/initrd.img-4.2.0-42-generic
sudo java -cp /home/ec2-user/s3download.jar ${mainClass} ${region} ${kmsKey} ${bucket} ${templatePath}/vmlinuz-4.2.0-42-generic /home/ec2-user/app-domains/${basePath}/vmlinuz-4.2.0-42-generic
