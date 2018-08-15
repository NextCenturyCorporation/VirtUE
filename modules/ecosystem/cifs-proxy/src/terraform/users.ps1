$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist Admin, $password
$bobpassword = ConvertTo-SecureString -AsPlainText -Force "${bob_password}"

New-ADUser -Name "bob" -Credential $cred -AccountPassword $bobpassword -Enabled $true -PasswordNeverExpires $true

