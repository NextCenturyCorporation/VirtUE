package com.ncc.savior.virtueadmin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class IconModel {
	@Id
	private String id;
	@Lob
	@Column(length = 100000)
	private byte[] data;

	// for Jackson serialization
	protected IconModel() {

	}

	public IconModel(String id, byte[] data) {
		super();
		this.id = id;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
