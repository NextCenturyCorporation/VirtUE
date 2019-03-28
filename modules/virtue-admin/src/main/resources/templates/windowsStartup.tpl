echo mount -o mtype=hard ${nfs.internalIpAddress}:/disk/nfs t: > "c:\\Users\\All Users\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\mountNfs.bat"
echo net use t: \\${nfs.internalIpAddress}\disk\nfs /PERSISTENT:YES > "c:\\virtue\\mount.txt"
#echo c:\windows\system32\cmd.exe /c mount -o mtype=hard ${nfs.internalIpAddress}:/disk/nfs t: >> "c:\\virtue\\mount.txt"
#echo C:\\Users\\Administrator\\savior\\bin\\run-all.bat > "c:\\Users\\All Users\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\startSensors.bat"