package com.ncc.savior.virtueadmin.infrastructure.pipelining;

public class PipelineWrapper<T> {
	private T element;

	public PipelineWrapper(T element) {
		this.element = element;
	}

	public T get() {
		return element;
	}
}
