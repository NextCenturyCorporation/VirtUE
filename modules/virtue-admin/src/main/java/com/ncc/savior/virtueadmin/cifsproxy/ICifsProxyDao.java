/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.cifsproxy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Dao than handles storing existance and timeout for CIFS Proxy VMs as well as
 * their timeout time.
 *
 */
public interface ICifsProxyDao {

	VirtualMachine getCifsVm(VirtueUser user);

	void updateCifsVm(VirtueUser user, VirtualMachine vm);

	void updateUserTimeout(VirtueUser user);

	long getUserTimeout(VirtueUser user);

	void deleteCifsVm(VirtueUser user);

	Set<VirtueUser> getAllUsers();

	Collection<VirtualMachine> getAllCifsVms();

	void saveShareParams(CifsShareCreationParameter share);

	List<CifsShareCreationParameter> getSharesForVirtue(String virtueId);

}
