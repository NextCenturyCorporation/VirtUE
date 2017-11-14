package com.ncc.savior.virtueadmin.model.response;

import java.util.List;

public class VirtueResponse {
	private long id; 
	private List<String> virtueList;
	
	
	public VirtueResponse() {
		super();
	}

	public VirtueResponse(long id, List<String> virtueList) {
		super();
		this.id = id;
		this.virtueList = virtueList;
	}

	public List<String> getVirtueList() {
		return virtueList;
	}
	
	public void setVirtueList(List<String> virtueList) {
		this.virtueList = virtueList;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	} 
	

}
