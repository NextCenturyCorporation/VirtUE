Start-Transcript -Path "c:\user_data.log" -append -force

Get-WindowsFeature -Name FS-FileServer
New-FileShare -Name TestShare -SourceVolume (Get-Volume -DriveLetter C) -FileServerFriendlyName $env:COMPUTERNAME
echo Hello > \Shares\TestShare\hello.txt
Get-FileShare -Name TestShare | Grant-FileShareAccess -AccountName bob -AccessRight Full

echo Setting up delegation
Add-WindowsFeature RSAT-AD-PowerShell
Import-Module ActiveDirectory
$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password
$fileserver = Get-ADComputer -Identity fileserver -Credential $cred
$webserver = Get-ADComputer -Identity webserver -Credential $cred
Set-ADComputer -Identity $fileserver -PrincipalsAllowedToDelegateToAccount $webserver -Credential $cred
Set-ADComputer $webserver -add @{"msDS-AllowedToDelegateTo"="cifs/fileserver.test.savior"} -Credential $cred
