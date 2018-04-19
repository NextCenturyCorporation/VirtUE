package com.ncc.savior.virtueadmin.infrastructure.pipelining;

/**
 * Wrapper around pipeline objects such that we can add extra information to be
 * carried through the pipeline.
 * 
 *
 * @param <T>
 */
public class PipelineWrapper<T> {
	private T element;
	private Object extended;

	public PipelineWrapper(T element) {
		this.element = element;
	}

	public T get() {
		return element;
	}

	public void setExtended(Object extended) {
		this.extended = extended;
	}

	public Object getExtended() {
		return extended;
	}
}
