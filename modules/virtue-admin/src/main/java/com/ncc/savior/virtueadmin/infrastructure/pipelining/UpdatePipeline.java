package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;

/**
 * Implementation of {@link IUpdatePipeline}. See {@link IUpdatePipeline}
 * documentation for more details.
 * 
 *
 * @param <T>
 */
public class UpdatePipeline<T> implements IUpdatePipeline<T>, IUpdatePipelineResultListener<T> {
	private static final Logger logger = LoggerFactory.getLogger(UpdatePipeline.class);
	private List<IPipelineComponent<T>> pipeline = new ArrayList<IPipelineComponent<T>>();
	private boolean isStarted;
	private IUpdateListener<T> notifier;
	private String descriptor;

	public UpdatePipeline(IUpdateListener<T> notifier, String descriptor) {
		this.notifier = notifier;
		this.descriptor = descriptor;
	}

	@Override
	public void addPipelineComponent(IPipelineComponent<T> component, int index) {
		if (component == null) {
			throw new SaviorException(SaviorErrorCode.CONFIGURATION_ERROR,
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
	public void addPipelineComponent(IPipelineComponent<T> component) {
		if (isStarted) {
			throw new RuntimeException("Cannot add pipeline components to and already started pipeline");
		}
		component.setResultListener(this);
		addPipelineComponent(component, pipeline.size());
	}

	@Override
	public void addToPipeline(Collection<T> elements) {
		Collection<PipelineWrapper<T>> wrappers = new ArrayList<PipelineWrapper<T>>();
		for (T element : elements) {
			PipelineWrapper<T> wrapper = new PipelineWrapper<T>(element);
			wrappers.add(wrapper);
		}
		pipeline.get(0).addPipelineElements(wrappers);

	}

	@Override
	public void start() {
		isStarted = true;
		int index = 0;
		for (IPipelineComponent<T> component : pipeline) {
			component.onPipelineStart(index);
			index++;
		}
	}

	@Override
	public void onSuccess(PipelineWrapper<T> wrapper, int currentPipelineIndex) {
		ArrayList<PipelineWrapper<T>> wrappers = new ArrayList<PipelineWrapper<T>>(1);
		wrappers.add(wrapper);
		onSuccess(wrappers, currentPipelineIndex);
	}

	@Override
	public void onSuccess(Collection<PipelineWrapper<T>> wrappers, int currentPipelineIndex) {
		if (wrappers.isEmpty()) {
			return;
		}
		Collection<T> vms = unwrap(wrappers);
		notifier.updateElements(vms);
		if (currentPipelineIndex < 0) {
			throw new RuntimeException("Error currentPipelineIndex is invalid=" + currentPipelineIndex);
		}
		currentPipelineIndex++;

		if (currentPipelineIndex < pipeline.size()) {
			IPipelineComponent<T> component = pipeline.get(currentPipelineIndex);
			logger.debug("Moving VMs to next pipeline component.  Pipeline=" + descriptor + " Component="
					+ component.getClass().getSimpleName() + " index=" + currentPipelineIndex + " VMs(" + vms.size()
					+ ")=" + vms);
			component.addPipelineElements(wrappers);
		} else {
			logger.debug("VMs exiting pipeline.  Pipeline=" + descriptor + " VMs=" + vms);
		}
	}

	@Override
	public void onFatalError(PipelineWrapper<T> wrapper) {
		logger.debug("VM exiting pipeline due to failure. Pipeline=" + descriptor + " VM=" + wrapper.get());

	}

	@Override
	public void onFatalError(Collection<PipelineWrapper<T>> wrappers) {
		logger.debug("VMs exiting pipeline due to failure. Pipeline=" + descriptor + " VMs=" + unwrap(wrappers));

	}

	protected Collection<T> unwrap(Collection<PipelineWrapper<T>> wrapper) {
		Collection<T> col = new ArrayList<T>(wrapper.size());
		for (PipelineWrapper<T> p : wrapper) {
			col.add(p.get());
		}
		return col;
	}
}
