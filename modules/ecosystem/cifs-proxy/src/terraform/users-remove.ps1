# In the future, maybe remove all non-system users (Administrator,
# Guest, krbtgt, and Admin), but for now just keep this file in sync
# with users.ps1 since we have few users.

$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password

Remove-ADUser -Identity bob -Credential $cred -Confirm:$False
