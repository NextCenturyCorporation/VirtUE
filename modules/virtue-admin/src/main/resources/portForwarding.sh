#!/bin/bash
#
# Copyright (C) 2019 Next Century Corporation
# 
# This file may be redistributed and/or modified under either the GPL
# 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
# granted government purpose rights. For details, see the COPYRIGHT.TXT
# file at the root of this project.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
# 
# SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
#
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
