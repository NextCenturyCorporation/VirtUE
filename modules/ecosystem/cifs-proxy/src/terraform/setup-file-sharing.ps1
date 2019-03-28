#
# Copyright (C) 2019 Next Century Corporation
# 
# This file may be redistributed and/or modified under either the GPL
# 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
# granted government purpose rights. For details, see the COPYRIGHT.TXT
# file at the root of this project.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
# 
# SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
#
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
$cifsProxy = Get-ADComputer -Identity cifs-proxy -Credential $cred
Set-ADComputer -Identity $fileserver -PrincipalsAllowedToDelegateToAccount $cifsProxy -Credential $cred
Set-ADComputer $cifsProxy -add @{"msDS-AllowedToDelegateTo"="cifs/fileserver.test.savior"} -Credential $cred
