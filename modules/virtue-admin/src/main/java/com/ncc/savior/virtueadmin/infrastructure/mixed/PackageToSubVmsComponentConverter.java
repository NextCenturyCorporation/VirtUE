package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.Collection;

import com.ncc.savior.virtueadmin.infrastructure.pipelining.IPipelineComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipelineResultListener;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.PipelineWrapper;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class PackageToSubVmsComponentConverter implements IPipelineComponent<XenVirtueCreationPackage> {
	private IPipelineComponent<VirtualMachine> subPipeline;
	private IUpdatePipelineResultListener<XenVirtueCreationPackage> myResultListener;

	public PackageToSubVmsComponentConverter(IPipelineComponent<VirtualMachine> subPipeline) {
		this.subPipeline = subPipeline;
	}

	@Override
	public void addPipelineElements(Collection<PipelineWrapper<XenVirtueCreationPackage>> vms) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPipelineStart(int index) {
		this.subPipeline.onPipelineStart(index);
	}

	@Override
	public void setResultListener(IUpdatePipelineResultListener<XenVirtueCreationPackage> updatePipeline) {
		// TODO Auto-generated method stub

	}

}
