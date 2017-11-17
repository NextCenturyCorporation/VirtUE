package com.ncc.savior.virtueadmin.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ncc.savior.virtueadmin.model.response.Virtue;
import com.ncc.savior.virtueadmin.model.response.VirtueResponse;

@RestController
@RequestMapping(ResourceConstants.VIRTUE_MANAGEMENT_V1)
public class VirtueResource {
	
	@RequestMapping(path ="allvirtues", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<VirtueResponse> getAvailableVirtues(
			@RequestParam(value="userToken")
			int userToken)
	{

		List<Virtue> virtueList = new ArrayList<Virtue>();
			
		//Virtue 1
		int id = 0x001;
		String username = "Kyle Drumm"; 
		int roleid = 0x100; 
		List<String> applications = new ArrayList<String>(); 
		applications.add("Microsoft Word"); 
		applications.add("Gimp"); 
		applications.add("Firefox"); 
		
		List<String> transducers = new ArrayList<String>(); 
		transducers.add("network"); 
		transducers.add("app_monitor"); 
		
		String ipaddr = "192.168.1.22"; 
		
		Virtue v1 = new Virtue(id, username, roleid, applications, transducers, ipaddr); 
		virtueList.add(v1); 
		
		//Virtue 2
		id = 0x002;
		username = "Kyle Drumm"; 
		roleid = 0x100; 
		applications = new ArrayList<String>(); 
		applications.add("Microsoft Word"); 
		applications.add("Gimp"); 
		applications.add("Firefox"); 
		
		transducers = new ArrayList<String>(); 
		transducers.add("network"); 
		transducers.add("app_monitor"); 
		
		Virtue v2 = new Virtue(id, username, roleid, applications, transducers, ipaddr); 
		virtueList.add(v2); 
		
		return new ResponseEntity<>(new VirtueResponse(userToken, virtueList), HttpStatus.OK);
	}

}
