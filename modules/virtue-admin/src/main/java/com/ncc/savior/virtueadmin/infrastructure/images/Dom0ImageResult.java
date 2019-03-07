package com.ncc.savior.virtueadmin.infrastructure.images;

public class Dom0ImageResult {

	private String ami;

	public Dom0ImageResult(String ami) {
		this.ami = ami;
	}

	public String getAmi() {
		return ami;
	}

	public void setAmi(String ami) {
		this.ami = ami;
	}

	@Override
	public String toString() {
		return "Dom0ImageResult [ami=" + ami + "]";
	}

}
