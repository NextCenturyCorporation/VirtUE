package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

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
	
	public static final Comparator<? super IconModel> CASE_INSENSITIVE_ID_COMPARATOR = new CaseInsensitiveIdComparator();
	private static class CaseInsensitiveIdComparator implements Comparator<IconModel> {
		@Override
		public int compare(IconModel o1, IconModel o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId());
		}
	}
}
