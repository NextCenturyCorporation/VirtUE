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

import com.ncc.savior.virtueadmin.model.response.VirtueResponse;

@RestController
@RequestMapping(ResourceConstants.VIRTUE_MANAGEMENT_V1)
public class VirtueResource {
	
	@RequestMapping(path ="allvirtues", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<VirtueResponse> getAvailableVirtues(
			@RequestParam(value="user")
			int userID)
	{
		List<String> virtueList = new ArrayList<String>();
		virtueList.add("virtue-Office");
		virtueList.add("virtue-development");
		
		return new ResponseEntity<>(new VirtueResponse(userID, virtueList), HttpStatus.OK);
		
	}

}
