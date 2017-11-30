package com.ncc.savior.virtueadmin.infrastructure;

import java.util.Collection;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VmState;

public interface IVmManager {
	public void addStateUpdateListener(IStateUpdateListener listener);

	public void removeStateUpdateListener(IStateUpdateListener listener);

	public VirtualMachine provisionVirtualMachineTemplate(VirtualMachineTemplate vmt);

	public VirtualMachine startVirtualMachine(VirtualMachine vm);

	public VirtualMachine stopVirtualMachine(VirtualMachine vm);

	public void deleteVirtualMachine(VirtualMachine vm);

	public VmState getVirtialMachineState(VirtualMachine vm);

	public Map<String, VirtualMachine> provisionVirtualMachineTemplates(Collection<VirtualMachineTemplate> vmTemplates);
}
