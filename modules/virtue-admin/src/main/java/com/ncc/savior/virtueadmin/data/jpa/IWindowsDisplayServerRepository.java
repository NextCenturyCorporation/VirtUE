package com.ncc.savior.virtueadmin.data.jpa;

import org.springframework.data.repository.CrudRepository;

import com.ncc.savior.virtueadmin.model.WindowsDisplayServerData;

public interface IWindowsDisplayServerRepository extends CrudRepository<WindowsDisplayServerData, String> {

	Iterable<? extends WindowsDisplayServerData> findByUsername(String username);

}
