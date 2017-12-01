/* 
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),8001)); 
*  H2Bootstrap.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Nov 29, 2017
*  
*  Copyright (c) 2017 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ncc.savior.virtueadmin.entity.VirtueEntity;
import com.ncc.savior.virtueadmin.repository.VirtueRepository;

@Component
public class H2Bootstrap implements CommandLineRunner{

	@Autowired
	VirtueRepository virtueRepository; 
	
	
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		//String id = UUID.randomUUID().toString(); 
		
		System.out.println("BootStrapping data: ");
		
		Integer RoleId1 = 8001; 
		Integer RoleId2 = 8002; 

		VirtueEntity myVE = new VirtueEntity(UUID.randomUUID().toString(),RoleId1); 
		
		
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId1)); 
		
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId1)); 
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId1)); 
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId1)); 
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId2)); 
		virtueRepository.save(new VirtueEntity(UUID.randomUUID().toString(),RoleId2)); 
		
		
		System.out.println("Printing data: ");
		
		Iterable<VirtueEntity> myVirtues = virtueRepository.findAll(); 
		for(VirtueEntity vtEntity: myVirtues) {
			System.out.println(vtEntity.getVirtueUniqueId());
		}
		
	}
	

}
