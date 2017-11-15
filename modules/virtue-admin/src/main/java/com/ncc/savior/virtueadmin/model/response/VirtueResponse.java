package com.ncc.savior.virtueadmin.model.response;

import java.util.List;

public class VirtueResponse {
	private long userToken; 
	private List<Virtue> virtueList;
	
	
	public VirtueResponse() {
		super();
	}

	public VirtueResponse(long id, List<Virtue> virtueList) {
		super();
		this.userToken = id;
		this.virtueList = virtueList;
	}

	public List<Virtue> getVirtueList() {
		return virtueList;
	}
	
	public void setVirtueList(List<Virtue> virtueList) {
		this.virtueList = virtueList;
	}
	
	public long getId() {
		return userToken;
	}
	
	public void setId(long userToken) {
		this.userToken = userToken;
	} 
	

}
