<#setting number_format="computer">
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "echo hostname=${hostname} >> ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "echo dns=${externalDns} >> ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "cat ${portsFile}"
ssh -i virginiatech_ec2.pem ${vm.userName}@${xenInternalIpAddress} "touch sensorsStarted; nohup sudo ./run_sensors.sh > sensing.log 2>&1 "
sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport ${vm.sshPort} -j DNAT --to-destination ${xenInternalIpAddress}:${internalSshPort} 
sudo iptables -A FORWARD -p tcp -d ${xenInternalIpAddress} --dport ${internalSshPort}  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT
sudo iptables -vnL -t nat