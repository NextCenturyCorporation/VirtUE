# In the future, maybe remove all non-system users (Administrator,
# Guest, krbtgt, and Admin), but for now just keep this file in sync
# with users.ps1 when users are added/removed since we have few users.
Start-Transcript -Path "c:\user_data.log" -append -force 
Install-WindowsFeature -Name RSAT-AD-PowerShell

$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password

echo "removing user bob"
Remove-ADUser -Identity bob -Credential $cred -Confirm:$False
echo "removed user bob"
