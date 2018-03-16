package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsVmUpdater.IUpdateNotifier;
import com.ncc.savior.virtueadmin.util.SaviorException;

import persistance.JpaVirtualMachine;

public class UpdatePipeline implements IUpdatePipeline, IUpdatePipelineResultListener {
	private static final Logger logger = LoggerFactory.getLogger(UpdatePipeline.class);
	private List<IPipelineComponent> pipeline = new ArrayList<IPipelineComponent>();
	private boolean isStarted;
	private IUpdateNotifier notifier;
	private String descriptor;

	public UpdatePipeline(IUpdateNotifier notifier, String descriptor) {
		this.notifier = notifier;
		this.descriptor = descriptor;
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
	public void addToPipeline(Collection<JpaVirtualMachine> vms) {
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
	public void onSuccess(JpaVirtualMachine vm, int currentPipelineIndex) {
		ArrayList<JpaVirtualMachine> vms = new ArrayList<JpaVirtualMachine>(1);
		vms.add(vm);
		onSuccess(vms, currentPipelineIndex);
	}

	@Override
	public void onSuccess(Collection<JpaVirtualMachine> vms, int currentPipelineIndex) {
		notifier.notifyUpdatedVms(vms);
		if (currentPipelineIndex < 0) {
			throw new RuntimeException("Error currentPipelineIndex is invalid=" + currentPipelineIndex);
		}
		currentPipelineIndex++;

		if (currentPipelineIndex < pipeline.size()) {
			IPipelineComponent component = pipeline.get(currentPipelineIndex);
			logger.debug("Moving VMs to next pipeline component.  Pipeline=" + descriptor + " Component="
					+ component.getClass().getSimpleName() + " index=" + currentPipelineIndex + " VMs(" + vms.size()
					+ ")=" + vms);
			component.addVirtualMachines(vms);
		} else {
			logger.debug("VMs exiting pipeline.  Pipeline=" + descriptor + " VMs=" + vms);
		}
	}

	@Override
	public void onFatalError(JpaVirtualMachine vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFatalError(Collection<JpaVirtualMachine> vms) {
		// TODO Auto-generated method stub

	}

}
