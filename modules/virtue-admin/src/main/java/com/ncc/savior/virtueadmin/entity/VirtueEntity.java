/* 
*  VirtueEntity.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Nov 29, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="Virtue")
public class VirtueEntity {

	public VirtueEntity() {
		super();
	}

	public VirtueEntity(String virtueUniqueId, Integer roleId) {
		super();
		this.virtueUniqueId = virtueUniqueId;
		this.roleId = roleId;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id = (long) 0; 
	
	@NotNull
	private String virtueUniqueId; 
	
	@NotNull
	private Integer roleId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVirtueUniqueId() {
		return virtueUniqueId;
	}

	public void setVirtueUniqueId(String virtueUniqueId) {
		this.virtueUniqueId = virtueUniqueId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	} 
	
}
