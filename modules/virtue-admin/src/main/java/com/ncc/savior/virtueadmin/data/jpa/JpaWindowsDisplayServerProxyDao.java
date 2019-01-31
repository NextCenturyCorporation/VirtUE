package com.ncc.savior.virtueadmin.data.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.ncc.savior.virtueadmin.cifsproxy.ICifsProxyDao;
import com.ncc.savior.virtueadmin.infrastructure.windows.IWindowsDisplayServerDao;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.WindowsDisplayServerData;

/**
 * JPA implementation of {@link ICifsProxyDao}. Used when persistent storage is
 * needed.
 *
 */
public class JpaWindowsDisplayServerProxyDao implements IWindowsDisplayServerDao {

	@Autowired
	private IWindowsDisplayServerRepository wdsRepo;

	@Autowired
	private VirtualMachineRepository virtualMachineRepository;

	public JpaWindowsDisplayServerProxyDao() {
	}

	@Override
	public VirtualMachine getDisplayServerVmByWindowsApplicationVmId(String windowsApplicationVmId) {
		Iterable<WindowsDisplayServerData> all = wdsRepo.findAll();
		for(WindowsDisplayServerData wdsd:all) {
			System.out.println(wdsd);
		}
		Optional<WindowsDisplayServerData> val = wdsRepo.findById(windowsApplicationVmId);
		if (val.isPresent()) {
			return val.get().getWdsVm();
		} else {
			return null;
		}
	}

	@Override
	public Collection<VirtualMachine> getDisplayServerVmsByWindowsApplicationVmIds(List<String> windowsApplicationVmIds) {
		Iterable<WindowsDisplayServerData> data = wdsRepo.findAllById(windowsApplicationVmIds);
		Collection<VirtualMachine> col = new ArrayList<VirtualMachine>();
		for(WindowsDisplayServerData datum:data) {
			col.add(datum.getWdsVm());
		}
		return col;
	}

	@Override
	public void updateDisplayServerVm(String username, String windowsApplicationVmId, VirtualMachine vm) {
		Optional<WindowsDisplayServerData> old = wdsRepo.findById(windowsApplicationVmId);
		if (old.isPresent()) {
			WindowsDisplayServerData oldcifs = old.get();
			if (!vm.getId().equals(oldcifs.getWdsVm().getId())) {
				virtualMachineRepository.delete(oldcifs.getWdsVm());
			}
		}
		virtualMachineRepository.save(vm);
		WindowsDisplayServerData wdsd = new WindowsDisplayServerData(username, windowsApplicationVmId, vm);
		wdsRepo.save(wdsd);
	}

	@Override
	public void deleteDisplayServerVmsForUser(String username) {
		Iterable<? extends WindowsDisplayServerData> entities = wdsRepo.findByUsername(username);
		wdsRepo.deleteAll(entities);
	}

	@Override
	public void deleteDisplayServerVmId(String windowsApplicationVmId) {
		wdsRepo.deleteById(windowsApplicationVmId);
	}
}
