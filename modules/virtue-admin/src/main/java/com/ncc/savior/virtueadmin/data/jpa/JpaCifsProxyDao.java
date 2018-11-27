package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.cifsproxy.ICifsProxyDao;
import com.ncc.savior.virtueadmin.model.CifsProxyData;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * JPA implementation of {@link ICifsProxyDao}. Used when persistent storage is
 * needed.
 *
 */
public class JpaCifsProxyDao implements ICifsProxyDao {

	@Autowired
	private ICifsProxyRepository cifsRepo;

	@Autowired
	private VirtualMachineRepository virtualMachineRepository;

	private long timeoutDelayMillis;

	public JpaCifsProxyDao(long timeoutDelayMillis) {
		this.timeoutDelayMillis = timeoutDelayMillis;
	}

	@Override
	public VirtualMachine getCifsVm(VirtueUser user) {
		Optional<CifsProxyData> val = cifsRepo.findById(user.getUsername());
		if (val.isPresent()) {
			return val.get().getCifsVm();
		} else {
			return null;
		}
	}

	@Override
	public void updateCifsVm(VirtueUser user, VirtualMachine vm) {
		Optional<CifsProxyData> old = cifsRepo.findById(user.getUsername());
		if (old.isPresent()) {
			CifsProxyData oldcifs = old.get();
			if (!vm.getId().equals(oldcifs.getCifsVm().getId())) {
				virtualMachineRepository.delete(oldcifs.getCifsVm());
			}
		}
		virtualMachineRepository.save(vm);
		CifsProxyData cpd = new CifsProxyData(user, vm, getTimeoutTimeFromNow());
		cifsRepo.save(cpd);
	}

	@Override
	public void updateUserTimeout(VirtueUser user) {
		VirtualMachine vm = getCifsVm(user);
		updateCifsVm(user, vm);
	}

	@Override
	public long getUserTimeout(VirtueUser user) {
		Optional<CifsProxyData> val = cifsRepo.findById(user.getUsername());
		if (val.isPresent()) {
			return val.get().getTimeoutMillis();
		} else {
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_NOT_FOUND,
					"CIFS Proxy not found for user=" + user.getUsername());
		}
	}

	@Override
	public void deleteCifsVm(VirtueUser user) {
		Optional<CifsProxyData> cifs = cifsRepo.findById(user.getUsername());
		cifsRepo.deleteById(user.getUsername());
		if (cifs.isPresent()) {
			virtualMachineRepository.delete(cifs.get().getCifsVm());
		}
	}

	@Override
	public Set<VirtueUser> getAllUsers() {
		// TODO can probably be optimized to just get data from database, but since we
		// don't intend to have large number of users during this phase, this will do.
		Iterable<CifsProxyData> all = cifsRepo.findAll();
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
		Iterable<CifsProxyData> all = cifsRepo.findAll();
		Set<VirtualMachine> vms = new HashSet<VirtualMachine>();
		for (CifsProxyData data : all) {
			vms.add(data.getCifsVm());
		}
		return vms;
	}
}
