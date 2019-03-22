package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueCreationAdditionalParameters;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public interface IXenVmProvider {

	VirtualMachine getNewXenVm(VirtueInstance virtue, VirtueCreationAdditionalParameters virtueMods, String virtueName,
			Collection<String> secGroupIds);

}
