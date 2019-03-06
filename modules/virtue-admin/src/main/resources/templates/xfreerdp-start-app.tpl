export DISPLAY=:${display} ; xfreerdp /v:${applicationVm.internalHostname} /u:${applicationVm.windowsUser} /p:'${applicationVm.password}' /app:'outlook' /app-cmd:'${params}' /cert-ignore /span 
