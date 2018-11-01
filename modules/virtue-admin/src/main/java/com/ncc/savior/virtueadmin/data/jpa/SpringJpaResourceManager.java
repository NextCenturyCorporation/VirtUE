package com.ncc.savior.virtueadmin.data.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IResourceManager;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.FileSystem;

/**
 * {@link ITemplateManager} that uses Spring and JPA.
 */
@Repository
public class SpringJpaResourceManager implements IResourceManager {
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

	/** #uncommented */
	@Override
	public Map<String, Printer> getPrintersForVirtueTemplate(VirtueTemplate virtue) {
		Collection<Printer> printers = virtue.getPrinters();
		Map<String, Printer> map = new HashMap<String, Printer>();
		for (Printer p : printers) {
			map.put(p.getId(), p);
		}
		return map;
	}

	/** #uncommented */
	@Override
	public Map<String, FileSystem> getFileSystemsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<FileSystem> fileSystems = virtue.getFileSystems();
		Map<String, FileSystem> map = new HashMap<String, FileSystem>();
		for (FileSystem fs : fileSystems) {
			map.put(fs.getId(), fs);
		}
		return map;
	}

	/**
	 * #uncommented
	 *
	 * See commented line - the code below it does the same thing as getPrinterIds, except it doesn't use a hashset.
	 * Do we really need that?
	 */
	@Override
	public Collection<String> getPrinterIdsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<Printer> printers = virtue.getPrinters();
		Collection<String> ids = new HashSet<String>();
		for (Printer p : printers) {
			ids.add(p.getId());
		}
		return ids;
	}

	/**
	 * #uncommented
	 */
	@Override
	public Collection<String> getFileSystemIdsForVirtueTemplate(VirtueTemplate virtue) {
		Collection<String> fileSystemIds = virtue.getFileSystemIds();
		Collection<String> ids = new HashSet<String>();
		for (String fsId : fileSystemIds) {
			ids.add(fsId);
		}
		return ids;
	}

	/** This doesn't appear to be something that'd ever be used - no function makes a rest call to simply add/revoke one item
	 * from another, they just make a change to the virtue object and update the whole thing. These functions exist for users
	 * and virtues as well, but they aren't used there either.
	 */
	// /**
	//  * #uncommented
	//  */
	// @Override
	// public void assignPrinterToVirtueTemplate(String virtueTemplateId, String printerId) {
	// 	VirtueTemplate virtue = getVirtueTemplate(virtueTemplateId);
	// 	Printer printer = getPrinter(printerId);
	// 	virtue.addPrinter(printer);
	// 	virtueTemplateRepo.save(virtue);
	// }
	//
	//
	// /** #uncommented */
	// @Override
	// public void assignFileSystemToVirtueTemplate(String virtueTemplateId, String fileSystemId) {
	// 	VirtueTemplate virtue = getVirtueTemplate(virtueTemplateId);
	// 	FileSystem fileSystem = getFileSystem(fileSystemId);
	// 	virtue.addFileSystem(fileSystem);
	// 	virtueTemplateRepo.save(virtue);
	// }
	//
	// /**
	//  * #uncommented
	//  */
	// @Override
	// public void revokePrinterFromVirtueTemplate(String virtueTemplateId, String printerId) {
	// 	VirtueTemplate virtue = getVirtueTemplate(virtueTemplateId);
	// 	Printer printer = getPrinter(printerId);
	//
	// 	virtue.removePrinter(printer);
	// 	virtueTemplateRepo.save(virtue);
	// }
	//
	// /**
	//  * #uncommented
	//  */
	// @Override
	// public void revokeFileSystemFromVirtueTemplate(String virtueTemplateId, String fileSystemId) {
	// 	VirtueTemplate virtue = getVirtueTemplate(virtueTemplateId);
	// 	FileSystem fileSystem = getFileSystem(fileSystemId);
	//
	// 	virtue.removeFileSystem(fileSystem);
	// 	virtueTemplateRepo.save(virtue);
	// }



	/** #uncommented */
	@Override
	public Iterable<Printer> getAllPrinters() {
		return printerRepo.findAll();
	}

	/** #uncommented */
	@Override
	public Iterable<FileSystem> getAllFileSystems() {
		return fileSystemRepo.findAll();
	}


	/** #uncommented */
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

	/** #uncommented */
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

	/** #uncommented */
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

	/** #uncommented */
	@Override
	public Printer addPrinter(Printer printer) {
		checkPrinterExists(printer.getId(), true);
		return printerRepo.save(printer);
	}

	/** #uncommented */
	@Override
	public FileSystem addFileSystem(FileSystem fileSystem) {
		checkFileSystemExists(fileSystem.getId(), true);
		return fileSystemRepo.save(fileSystem);
	}

	/** #uncommented */
	@Override
	public Printer updatePrinter(String id, Printer printer) {
		checkPrinterExists(id, false);
		printer.setId(id);
		return printerRepo.save(printer);
	}

	/** #uncommented */
	@Override
	public FileSystem updateFileSystem(String id, FileSystem fileSystem) {
		checkFileSystemExists(id, false);
		fileSystem.setId(id);
		return fileSystemRepo.save(fileSystem);
	}

	/** #uncommented */
	@Override
	public void deleteFileSystem(String fileSystemId) {
		fileSystemRepo.deleteById(fileSystemId);
	}

	/** #uncommented */
	@Override
	public void deletePrinter(String printerId) {
		printerRepo.deleteById(printerId);
	}

	/** #uncommented */
	@Override
	public Iterable<Printer> getPrinters(Collection<String> printerIds) {
		return printerRepo.findAllById(printerIds);
	}

	/** #uncommented */
	@Override
	public Iterable<FileSystem> getFileSystems(Collection<String> fileSystemIds) {
		return fileSystemRepo.findAllById(fileSystemIds);
	}

	/** #uncommented */
	@Override
	public boolean containsPrinter(String id) {
		return printerRepo.existsById(id);
	}

	/** #uncommented */
	@Override
	public boolean containsFileSystem(String id) {
		return fileSystemRepo.existsById(id);
	}


	/**
	 * If breakIffExists is true, an exception will be thrown if the printer exists in the repository already.
	 * If breakIffExists is false, an exception will be thrown if the printer does not exist.
	 */
	@Override
	public void checkPrinterExists(String printerId, boolean breakIffExists) {

		boolean printerExists = containsPrinter(printerId);

		if ( printerExists && breakIffExists ) {
			throw new SaviorException(SaviorErrorCode.PRINTER_ALREADY_EXISTS,
					"Printer with id=" + printerId + " already exists.");
		}
		else if ( !printerExists && !breakIffExists ) {
			throw new SaviorException(SaviorErrorCode.PRINTER_NOT_FOUND,
					"Printer with id=" + printerId + " not found.");
		}
	}

	/**
	 * If breakIffExists is true, an exception will be thrown if the fileSystem exists in the repository already.
	 * If breakIffExists is false, an exception will be thrown if the fileSystem does not exist.
	 */
	@Override
	public void checkFileSystemExists(String fileSystemId, boolean breakIffExists) {

		boolean fileSystemExists = containsFileSystem(fileSystemId);

		if ( fileSystemExists && breakIffExists ) {
			throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_ALREADY_EXISTS,
					"FileSystem with id=" + fileSystemId + " already exists.");
		}
		else if ( !fileSystemExists && !breakIffExists ) {
			throw new SaviorException(SaviorErrorCode.FILE_SYSTEM_NOT_FOUND,
					"FileSystem with id=" + fileSystemId + " not found.");
		}
	}

	/** #uncommented */
	@Override
	public void clear() {
		printerRepo.deleteAll();
		fileSystemRepo.deleteAll();
	}
}
