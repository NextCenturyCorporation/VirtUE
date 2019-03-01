<#setting number_format="computer">
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "echo hostname=${hostname} >> ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "echo dns=${externalDns} >> ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "cat ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "touch sensorsStarted; nohup sudo ./run_sensors.sh > sensing.log 2>&1 "
