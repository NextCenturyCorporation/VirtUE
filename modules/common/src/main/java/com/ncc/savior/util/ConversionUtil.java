package com.ncc.savior.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.ncc.savior.virtueadmin.model.HasId;
import com.ncc.savior.virtueadmin.model.dto.VirtualMachineTemplateDto;
import com.ncc.savior.virtueadmin.model.dto.VirtueInstanceDto;
import com.ncc.savior.virtueadmin.model.dto.VirtueTemplateDto;
import com.ncc.savior.virtueadmin.model.dto.VirtueUserDto;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueInstance;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueTemplate;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

public class ConversionUtil {

	public static Iterable<VirtueTemplateDto> virtueTemplateIterable(Iterable<JpaVirtueTemplate> iterable) {
		Iterator<JpaVirtueTemplate> itr = iterable.iterator();
		ArrayList<VirtueTemplateDto> list = new ArrayList<VirtueTemplateDto>();
		while (itr.hasNext()) {
			JpaVirtueTemplate x = itr.next();
			list.add(new VirtueTemplateDto(x));
		}
		return list;
	}

	public static Iterable<VirtualMachineTemplateDto> vmTemplateIterable(Iterable<JpaVirtualMachineTemplate> itr) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Iterable<VirtueInstanceDto> virtueInstanceIterable(Iterable<JpaVirtueInstance> itr) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Collection<String> hasIdIterable(Iterable<? extends HasId> collection) {
		return null;
	}

	public static Iterable<VirtueUserDto> userIterable(Iterable<JpaVirtueUser> allUsers) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Collection<String> virtueTemplateCollection(Collection<JpaVirtueTemplate> virtueTemplates) {
		// TODO Auto-generated method stub
		return null;
	}

}
