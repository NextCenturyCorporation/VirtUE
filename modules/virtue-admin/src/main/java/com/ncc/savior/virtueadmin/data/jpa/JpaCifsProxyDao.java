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
package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.cifsproxy.ICifsProxyDao;
import com.ncc.savior.virtueadmin.model.CifsProxyData;
import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * JPA implementation of {@link ICifsProxyDao}. Used when persistent storage is
 * needed.
 *
 */
public class JpaCifsProxyDao implements ICifsProxyDao {

	@Autowired
	private ICifsProxyRepository cifsProxyRepo;

	@Autowired
	private ICifsShareRepository cifsShareRepo;

	@Autowired
	private VirtualMachineRepository virtualMachineRepository;

	private long timeoutDelayMillis;

	public JpaCifsProxyDao(long timeoutDelayMillis) {
		this.timeoutDelayMillis = timeoutDelayMillis;
	}

	@Override
	public VirtualMachine getCifsVm(VirtueUser user) {
		Optional<CifsProxyData> val = cifsProxyRepo.findById(user.getUsername());
		if (val.isPresent()) {
			return val.get().getCifsVm();
		} else {
			return null;
		}
	}

	@Override
	public void updateCifsVm(VirtueUser user, VirtualMachine vm) {
		Optional<CifsProxyData> old = cifsProxyRepo.findById(user.getUsername());
		if (old.isPresent()) {
			CifsProxyData oldcifs = old.get();
			if (!vm.getId().equals(oldcifs.getCifsVm().getId())) {
				virtualMachineRepository.delete(oldcifs.getCifsVm());
			}
		}
		virtualMachineRepository.save(vm);
		CifsProxyData cpd = new CifsProxyData(user, vm, getTimeoutTimeFromNow());
		cifsProxyRepo.save(cpd);
	}

	@Override
	public void updateUserTimeout(VirtueUser user) {
		VirtualMachine vm = getCifsVm(user);
		updateCifsVm(user, vm);
	}

	@Override
	public long getUserTimeout(VirtueUser user) {
		Optional<CifsProxyData> val = cifsProxyRepo.findById(user.getUsername());
		if (val.isPresent()) {
			return val.get().getTimeoutMillis();
		} else {
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_NOT_FOUND,
					"CIFS Proxy not found for user=" + user.getUsername());
		}
	}

	@Override
	public void deleteCifsVm(VirtueUser user) {
		Optional<CifsProxyData> cifs = cifsProxyRepo.findById(user.getUsername());
		cifsProxyRepo.deleteById(user.getUsername());
		if (cifs.isPresent()) {
			virtualMachineRepository.delete(cifs.get().getCifsVm());
		}
	}

	@Override
	public Set<VirtueUser> getAllUsers() {
		// TODO can probably be optimized to just get data from database, but since we
		// don't intend to have large number of users during this phase, this will do.
		Iterable<CifsProxyData> all = cifsProxyRepo.findAll();
		Set<VirtueUser> users = new HashSet<VirtueUser>();
		for (CifsProxyData data : all) {
			users.add(data.getUser());
		}
		return users;
	}

	private long getTimeoutTimeFromNow() {
		return System.currentTimeMillis() + timeoutDelayMillis;
	}

	@Override
	public Collection<VirtualMachine> getAllCifsVms() {
		// TODO can probably be optimized to just get data from database, but since we
		// don't intend to have large number of users during this phase, this will do.
		Iterable<CifsProxyData> all = cifsProxyRepo.findAll();
		Set<VirtualMachine> vms = new HashSet<VirtualMachine>();
		for (CifsProxyData data : all) {
			vms.add(data.getCifsVm());
		}
		return vms;
	}

	@Override
	public void saveShareParams(CifsShareCreationParameter share) {
		cifsShareRepo.save(share);
	}

	@Override
	public List<CifsShareCreationParameter> getSharesForVirtue(String virtueId) {
		List<CifsShareCreationParameter> ret = cifsShareRepo.findByVirtueId(virtueId);
		return ret;
	}
}
