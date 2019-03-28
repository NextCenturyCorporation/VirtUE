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
# In the future, maybe remove all non-system users (Administrator,
# Guest, krbtgt, and Admin), but for now just keep this file in sync
# with users.ps1 when users are added/removed since we have few users.
Start-Transcript -Path "c:\user_data.log" -append -force 
Install-WindowsFeature -Name RSAT-AD-PowerShell

$password = ConvertTo-SecureString -AsPlainText -Force "${admin_password}"
$cred = new-object -typename System.Management.Automation.PSCredential -argumentlist ${domain_admin_user}, $password

echo "removing user bob"
Remove-ADUser -Identity bob -Credential $cred -Confirm:$False
echo "removed user bob"
