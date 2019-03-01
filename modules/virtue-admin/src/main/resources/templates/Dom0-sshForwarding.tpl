<#setting number_format="computer">
sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport ${vm.sshPort} -j DNAT --to-destination ${xenInternalIpAddress}:${internalSshPort} 
sudo iptables -A FORWARD -p tcp -d ${xenInternalIpAddress} --dport ${internalSshPort}  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT
sudo iptables -vnL -t nat