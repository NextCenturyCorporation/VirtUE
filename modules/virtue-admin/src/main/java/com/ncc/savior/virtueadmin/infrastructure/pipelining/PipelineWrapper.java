package com.ncc.savior.virtueadmin.infrastructure.pipelining;

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
