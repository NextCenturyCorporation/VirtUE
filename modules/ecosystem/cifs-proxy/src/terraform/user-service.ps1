Start-Transcript -Path "c:\user_data.log" -append -force

Add-WindowsFeature RSAT-AD-PowerShell
Import-Module ActiveDirectory
$password = ConvertTo-SecureString -AsPlainText -Force "${password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist TEST\${domain_admin_user}, $password
Set-ADComputer -UserPrincipalName http/${hostname}.${domain}@${domain} -Identity ${hostname} -Credential $cred
