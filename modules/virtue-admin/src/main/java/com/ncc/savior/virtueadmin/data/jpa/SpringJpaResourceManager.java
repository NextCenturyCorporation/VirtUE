/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IResourceManager;
import com.ncc.savior.virtueadmin.data.ITemplateManager;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * {@link ITemplateManager} that uses Spring and JPA.
 */
@Repository
public class SpringJpaResourceManager implements IResourceManager {
	private final static Logger logger = LoggerFactory.getLogger(SpringJpaResourceManager.class);

	@Autowired
	private PrinterRepository printerRepo;
	@Autowired
	private FileSystemRepository fileSystemRepo;
	@Autowired
	private VirtueTemplateRepository virtueTemplateRepo;

	public SpringJpaResourceManager(PrinterRepository printerRepo, FileSystemRepository fileSystemRepo, VirtueTemplateRepository vtRepo) {
		this.printerRepo = printerRepo;
		this.fileSystemRepo = fileSystemRepo;
		this.virtueTemplateRepo = vtRepo;
	}

	@Override
	public Printer getPrinterForVirtueTemplate(VirtueTemplate virtue, String printerId) {
		for (Printer printer : virtue.getPrinters()) {
			if (printer.getId().equals(printerId)) {
				return printer;
			}
		}
		throw new SaviorException(SaviorErrorCode.PRINTER_NOT_FOUND,
				"Printer id=" + printerId + " not found.");
	}

	/**
	 * @return the fileSystem attached to the virtueTemplate, that has the given fileSystemId.
	 */
	@Override
	public FileSystem getFileSystemForVirtueTemplate(VirtueTemplate virtue, String fileSystemId) {
		for (FileSystem fileSystem : virtue.getFileSystems()) {
			if (fileSystem.getId().equals(fileSystemId)) {
				return fileSystem;
			}
		}
		throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_NOT_FOUND,
				"File System id=" + fileSystemId + " not found.");
	}

	@Override
	public Map<String, Printer> getPrintersForVirtueTemplate(VirtueTemplate virtue) {
		Collection<Printer> printers = virtue.getPrinters();
		Map<String, Printer> map = new HashMap<String, Printer>();
		for (Printer p : printers) {
			map.put(p.getId(), p);
		}
		return map;
	}

	@Override
	public Map<String, FileSystem> getFileSystemsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<FileSystem> fileSystems = virtue.getFileSystems();
		Map<String, FileSystem> map = new HashMap<String, FileSystem>();
		for (FileSystem fs : fileSystems) {
			map.put(fs.getId(), fs);
		}
		return map;
	}

	@Override
	public Collection<String> getPrinterIdsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<Printer> printers = virtue.getPrinters();
		Collection<String> ids = new HashSet<String>();
		for (Printer p : printers) {
			ids.add(p.getId());
		}
		return ids;
	}

	@Override
	public Collection<String> getFileSystemIdsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<String> fileSystemIds = virtue.getFileSystemIds();
		Collection<String> ids = new HashSet<String>();
		for (String fsId : fileSystemIds) {
			ids.add(fsId);
		}
		return ids;
	}

	@Override
	public Iterable<Printer> getAllPrinters() {
		return printerRepo.findAll();
	}

	@Override
	public Iterable<FileSystem> getAllFileSystems() {
		return fileSystemRepo.findAll();
	}


	@Override
	public Printer getPrinter(String printerId) {
		Printer printer = printerRepo.findById(printerId).get();
		if (printer != null) {
			return printer;
		} else {
			throw new SaviorException(SaviorErrorCode.PRINTER_NOT_FOUND,
					"Cannot find printer with id=" + printerId);
		}
	}

	@Override
	public FileSystem getFileSystem(String fileSystemId) {
		FileSystem fileSystem = fileSystemRepo.findById(fileSystemId).get();
		if (fileSystem != null) {
			return fileSystem;
		} else {
			throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_NOT_FOUND,
					"Cannot find fileSystem with id=" + fileSystemId);
		}
	}

	@Override
	public VirtueTemplate getVirtueTemplate(String virtueTemplateId) {
		VirtueTemplate virtueTemplate = virtueTemplateRepo.findById(virtueTemplateId).get();
		if (virtueTemplate != null) {
			return virtueTemplate;
		} else {
			throw new SaviorException(SaviorErrorCode.VIRTUE_TEMPLATE_NOT_FOUND,
					"Cannot find virtueTemplate with id=" + virtueTemplateId);
		}
	}

	@Override
	public Printer addPrinter(Printer printer) {
		breakIfPrinterExists(printer.getId());
		return printerRepo.save(printer);
	}

	@Override
	public FileSystem addFileSystem(FileSystem fileSystem) {
		breakIfFileSystemExists(fileSystem.getId());
		return fileSystemRepo.save(fileSystem);
	}

	@Override
	public Printer updatePrinter(String id, Printer printer) {
		breakIfPrinterDoesntExist(id);
		printer.setId(id);
		return printerRepo.save(printer);
	}

	@Override
	public FileSystem updateFileSystem(String id, FileSystem fileSystem) {
		breakIfFileSystemDoesntExist(id);
		fileSystem.setId(id);
		return fileSystemRepo.save(fileSystem);
	}

	@Override
	public void deleteFileSystem(String fileSystemId) {
		fileSystemRepo.deleteById(fileSystemId);
	}

	@Override
	public void deletePrinter(String printerId) {
		printerRepo.deleteById(printerId);
	}

	@Override
	public Iterable<Printer> getPrinters(Collection<String> printerIds) {
		return printerRepo.findAllById(printerIds);
	}

	@Override
	public Iterable<FileSystem> getFileSystems(Collection<String> fileSystemIds) {
		return fileSystemRepo.findAllById(fileSystemIds);
	}

	@Override
	public boolean containsPrinter(String id) {
		return printerRepo.existsById(id);
	}

	@Override
	public boolean containsFileSystem(String id) {
		return fileSystemRepo.existsById(id);
	}

	private void breakIfPrinterExists(String printerId) {
		if ( containsPrinter(printerId) ) {
			throw new SaviorException(SaviorErrorCode.PRINTER_ALREADY_EXISTS,
					"Printer with id=" + printerId + " already exists.");
		}
	}

	private void breakIfPrinterDoesntExist(String printerId) {
		if ( ! containsPrinter(printerId) ) {
			throw new SaviorException(SaviorErrorCode.PRINTER_NOT_FOUND,
					"Printer with id=" + printerId + " not found.");
		}
	}

	private void breakIfFileSystemExists(String fileSystemId) {

		if ( containsFileSystem(fileSystemId) ) {
			throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_ALREADY_EXISTS,
					"FileSystem with id=" + fileSystemId + " already exists.");
		}
	}

	private void breakIfFileSystemDoesntExist(String fileSystemId) {
		if ( !containsFileSystem(fileSystemId) ) {
			throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_NOT_FOUND,
					"FileSystem with id=" + fileSystemId + " not found.");
		}
	}

	/** delete all printer and fileSystem records */
	@Override
	public void clear() {
		printerRepo.deleteAll();
		fileSystemRepo.deleteAll();
	}
}
