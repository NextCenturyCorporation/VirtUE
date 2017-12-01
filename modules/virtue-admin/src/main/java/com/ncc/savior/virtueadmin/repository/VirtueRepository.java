/* 
*  VirtueRepository.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Nov 29, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.entity.VirtueEntity;

public interface VirtueRepository extends CrudRepository<VirtueEntity, Long>{

	List<VirtueEntity> findById(Long id); 
}
