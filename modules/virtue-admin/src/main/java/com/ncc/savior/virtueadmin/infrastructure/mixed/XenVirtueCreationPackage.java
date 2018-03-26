package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public class XenVirtueCreationPackage {

	private VirtueInstance virtue;
	private Collection<VirtualMachineTemplate> linuxVmt;
	private VirtualMachine xenVm;

	public XenVirtueCreationPackage(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts,
			VirtualMachine xenVm) {
		this.virtue = virtue;
		this.linuxVmt = linuxVmts;
		this.xenVm = xenVm;
	}

	protected VirtueInstance getVirtue() {
		return virtue;
	}

	protected Collection<VirtualMachineTemplate> getLinuxVmt() {
		return linuxVmt;
	}

	protected VirtualMachine getXenVm() {
		return xenVm;
	}

}
