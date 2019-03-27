package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public interface IXenVmProvider {
	public static final String VM_NAME_POOL_PREFIX = "XEN_POOL-";

	VirtualMachine getNewXenVm(VirtueInstance virtue, VirtueCreationAdditionalParameters virtueMods, String virtueName,
			Collection<String> secGroupIds);

	void setXenPoolSize(int poolSize);

	int getXenPoolSize();

}
