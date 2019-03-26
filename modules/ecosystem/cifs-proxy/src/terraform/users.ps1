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
# Note: need to sync this with users-remove.ps1 when users are added/removed
Start-Transcript -Path "c:\users.log" -append -force 
Install-WindowsFeature -Name RSAT-AD-PowerShell

$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist TEST\${domain_admin_user}, $password
$bobpassword = ConvertTo-SecureString -AsPlainText -Force "${bob_password}"

echo "creating user bob"
New-ADUser -Name "bob" -Credential $cred -AccountPassword $bobpassword -Enabled $true -PasswordNeverExpires $true
echo "created user bob"
