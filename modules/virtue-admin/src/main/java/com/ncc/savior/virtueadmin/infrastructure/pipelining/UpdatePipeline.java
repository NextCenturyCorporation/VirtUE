package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsVmUpdater.IUpdateNotifier;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.SaviorException;

public class UpdatePipeline implements IUpdatePipeline, IUpdatePipelineResultListener {

	private List<IPipelineComponent> pipeline = new ArrayList<IPipelineComponent>();
	private boolean isStarted;
	private IUpdateNotifier notifier;

	public UpdatePipeline(IUpdateNotifier notifier) {
		this.notifier = notifier;
	}

	@Override
	public void addPipelineComponent(IPipelineComponent component, int index) {
		if (component == null) {
			throw new SaviorException(SaviorException.CONFIGURATION_ERROR,
					"Attempted to add a null pipeline component to pipeline");
		}
		if (index >= pipeline.size()) {
			pipeline.add(component);
		} else if (index < 0) {
			index = 0;
		} else {
			pipeline.add(index, component);
		}

	}

	@Override
	public void addPipelineComponent(IPipelineComponent component) {
		if (isStarted) {
			throw new RuntimeException("Cannot add pipeline components to and already started pipeline");
		}
		component.setResultListener(this);
		addPipelineComponent(component, pipeline.size());
	}

	@Override
	public void addToPipeline(Collection<VirtualMachine> vms) {
		pipeline.get(0).addVirtualMachines(vms);

	}

	@Override
	public void start() {
		isStarted = true;
		int index = 0;
		for (IPipelineComponent component : pipeline) {
			component.onPipelineStart(index);
			index++;
		}
	}

	@Override
	public void onSuccess(VirtualMachine vm, int currentPipelineIndex) {
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(1);
		vms.add(vm);
		onSuccess(vms, currentPipelineIndex);
	}

	@Override
	public void onSuccess(Collection<VirtualMachine> vms, int currentPipelineIndex) {
		notifier.notifyUpdatedVms(vms);
		if (currentPipelineIndex < 0) {
			throw new RuntimeException("Error currentPipelineIndex is invalid=" + currentPipelineIndex);
		}
		currentPipelineIndex++;
		if (currentPipelineIndex < pipeline.size()) {
			pipeline.get(currentPipelineIndex).addVirtualMachines(vms);
		}
	}

	@Override
	public void onFatalError(VirtualMachine vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFatalError(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub

	}

}
