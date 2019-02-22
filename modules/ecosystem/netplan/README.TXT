Netplan

SAVIOR uses Ubuntu 18.04 for the Virtue Admin server and for the CIFS
Proxy. But the version of netplan available for it (0.40.1~18.04.4)
can't suppress DHCP-provided DNS. See

https://bugs.launchpad.net/netplan/+bug/1759014/comments/5
https://askubuntu.com/questions/1001241/can-netplan-configured-nameservers-supersede-not-merge-with-the-dhcp-nameserve

A more recent version (0.95-2) has the DHCP DNS support we need, so
this tree builds that version.
