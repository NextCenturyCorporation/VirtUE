export DISPLAY=:${display} ; xfreerdp /v:${applicationVm.internalHostname} /u:${applicationVm.windowsUser} /p:'${applicationVm.password}' /app:'c:\virtue\clipboard-0.1.0-SNAPSHOT-all.jar' /app-cmd:'' /cert-ignore /span 