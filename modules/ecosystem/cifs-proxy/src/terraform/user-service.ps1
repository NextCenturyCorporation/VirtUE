Add-WindowsFeature RSAT-AD-PowerShell
Import-Module ActiveDirectory
$password = ConvertTo-SecureString -AsPlainText -Force "${password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password
Set-ADComputer -UserPrincipalName http/${hostname}.${domain}@${domain} -Identity ${hostname} -Credential $cred
