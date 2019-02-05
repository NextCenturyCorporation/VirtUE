echo '' >> ${credentialFilePath}
echo 'username=${cifsVirtueParams.username}' >> ${credentialFilePath}
sudo sh -c "echo '//${cifsVm.internalIpAddress}/${cifsShare.exportedName} /media/${cifsShare.exportedName} cifs credentials=${credentialFilePath} 0 2' >> /etc/fstab"
sudo mount /media/${cifsShare.exportedName}