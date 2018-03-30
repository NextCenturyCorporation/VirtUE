#!/bin/bash
REMOTEPORTFILE=testPorts.properties
USER=user
CERT=virginiatech_ec2.pem

portForward () {
        EXTERNALPORT=$1;
        GUESTIP=$2;
        INTERNALPORT=$3;

        echo sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport $EXTERNALPORT -j DNAT --to-destination $GUESTIP:$INTERNALPORT
        echo sudo iptables -A FORWARD -p tcp -d $GUESTIP --dport $INTERNALPORT  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT
        ssh -v -i $CERT $USER@$GUESTIP "echo '$INTERNALPORT=$EXTERNALPORT' >> $REMOTEPORTFILE"
}


portForward 12007 192.168.0.38 11007
