#!/bin/bash
REMOTEPORTFILE=ports.properties
USER=user
CERT=virginiatech_ec2.pem

portForward () {
        EXTERNALPORT=$1;
        GUESTIP=$2;
        INTERNALPORT=$3;

        echo Adding port forwarding from $EXTERNALPORT to $GUESTIP:$INTERNALPORT
        sudo iptables -t nat -A PREROUTING -p tcp -i eth0  --dport $EXTERNALPORT -j DNAT --to-destination $GUESTIP:$INTERNALPORT
        sudo iptables -A FORWARD -p tcp -d $GUESTIP --dport $INTERNALPORT  -m state --state NEW,ESTABLISHED,RELATED -j ACCEPT
        ssh -i $CERT $USER@$GUESTIP "echo $INTERNALPORT=$EXTERNALPORT >> $REMOTEPORTFILE"
}


portForward $1 $2 $3
