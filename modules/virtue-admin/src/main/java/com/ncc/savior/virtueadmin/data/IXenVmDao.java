package com.ncc.savior.virtueadmin.data;

import java.util.Collection;

import com.ncc.savior.virtueadmin.model.VirtualMachine;

public interface IXenVmDao {

	public void updateXenVms(VirtualMachine vm);

	public void updateXenVms(Collection<VirtualMachine> elements);

	public VirtualMachine getXenVm(String id);
}
