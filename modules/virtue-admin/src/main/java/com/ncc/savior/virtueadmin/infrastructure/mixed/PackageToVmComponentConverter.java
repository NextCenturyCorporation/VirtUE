package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Collection;

import com.ncc.savior.virtueadmin.infrastructure.pipelining.IPipelineComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.IUpdatePipelineResultListener;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.PipelineWrapper;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class PackageToVmComponentConverter implements IPipelineComponent<XenVirtueCreationPackage> {

	private IPipelineComponent<VirtualMachine> subPipeline;
	private IUpdatePipelineResultListener<XenVirtueCreationPackage> myResultListener;

	public PackageToVmComponentConverter(IPipelineComponent<VirtualMachine> subPipeline) {
		this.subPipeline = subPipeline;
	}

	@Override
	public void addPipelineElements(Collection<PipelineWrapper<XenVirtueCreationPackage>> packs) {
		Collection<PipelineWrapper<VirtualMachine>> vms = new ArrayList<PipelineWrapper<VirtualMachine>>();
		for (PipelineWrapper<XenVirtueCreationPackage> pack : packs) {
			VirtualMachine vm = pack.get().getXenVm();
			PipelineWrapper<VirtualMachine> wrapper = new PipelineWrapper<VirtualMachine>(vm);
			wrapper.setExtended(pack);
			vms.add(wrapper);
		}
		subPipeline.addPipelineElements(vms);
	}

	@Override
	public void onPipelineStart(int index) {
		this.subPipeline.onPipelineStart(index);
	}

	@Override
	public void setResultListener(IUpdatePipelineResultListener<XenVirtueCreationPackage> updatePipeline) {
		this.myResultListener = updatePipeline;
		subPipeline.setResultListener(new IUpdatePipelineResultListener<VirtualMachine>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(PipelineWrapper<VirtualMachine> wrapper, int currentPipelineIndex) {
				myResultListener.onSuccess((PipelineWrapper<XenVirtueCreationPackage>) wrapper.getExtended(),
						currentPipelineIndex);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Collection<PipelineWrapper<VirtualMachine>> wrappers, int currentPipelineIndex) {
				Collection<PipelineWrapper<XenVirtueCreationPackage>> packs = new ArrayList<PipelineWrapper<XenVirtueCreationPackage>>();
				for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
					packs.add((PipelineWrapper<XenVirtueCreationPackage>) wrapper.getExtended());
				}
				myResultListener.onSuccess(packs, currentPipelineIndex);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onFatalError(PipelineWrapper<VirtualMachine> wrapper) {
				myResultListener.onFatalError((PipelineWrapper<XenVirtueCreationPackage>) wrapper.getExtended());
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onFatalError(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
				Collection<PipelineWrapper<XenVirtueCreationPackage>> packs = new ArrayList<PipelineWrapper<XenVirtueCreationPackage>>();
				for (PipelineWrapper<VirtualMachine> wrapper : wrappers) {
					packs.add((PipelineWrapper<XenVirtueCreationPackage>) wrapper.getExtended());
				}
				myResultListener.onFatalError(packs);
			}
		});

	}

}
