# Note: need to sync this with users-remove.ps1 when users are added/removed
Start-Transcript -Path "c:\users.log" -append -force 
Install-WindowsFeature -Name RSAT-AD-PowerShell

$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password
$bobpassword = ConvertTo-SecureString -AsPlainText -Force "${bob_password}"

echo "creating user bob"
New-ADUser -Name "bob" -Credential $cred -AccountPassword $bobpassword -Enabled $true -PasswordNeverExpires $true
echo "created user bob"
