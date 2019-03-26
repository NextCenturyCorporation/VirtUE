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
package com.ncc.savior.virtueadmin.data;

import java.util.Collection;
import java.util.Map;

import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.Printer;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;

/**
 * Interface for a class that manages and stores all resource information and definitions.
 *
 */
public interface IResourceManager {

	public Printer getPrinterForVirtueTemplate(VirtueTemplate virtue, String printerId);

	public FileSystem getFileSystemForVirtueTemplate(VirtueTemplate virtue, String fileSystemId);

	public Map<String, Printer> getPrintersForVirtueTemplate(VirtueTemplate virtue);

	public Map<String, FileSystem> getFileSystemsForVirtueTemplate(VirtueTemplate virtue);

	public Collection<String> getPrinterIdsForVirtueTemplate(VirtueTemplate virtue);

	public Collection<String> getFileSystemIdsForVirtueTemplate(VirtueTemplate virtue);


	// public void assignPrinterToVirtueTemplate(String virtueTemplateId, String printerId);
	//
	// public void assignFileSystemToVirtueTemplate(String virtueTemplateId, String fileSystemId);
	//
	// public void revokePrinterFromVirtueTemplate(String virtueId, String printerId);
	//
	// public void revokeFileSystemFromVirtueTemplate(String virtueId, String fileSystemId);


	public Iterable<Printer> getAllPrinters();

	public Iterable<FileSystem> getAllFileSystems();


	public Printer getPrinter(String printerId);

	public FileSystem getFileSystem(String fileSystemId);

	public VirtueTemplate getVirtueTemplate(String virtueTemplateId);


	public Printer addPrinter(Printer printer);

	public FileSystem addFileSystem(FileSystem fileSystem);


	public Printer updatePrinter(String id, Printer printer);

	public FileSystem updateFileSystem(String id, FileSystem fileSystem);


	public void deleteFileSystem(String fileSystemId);

	public void deletePrinter(String printerId);


	public Iterable<Printer> getPrinters(Collection<String> printerIds);

	public Iterable<FileSystem> getFileSystems(Collection<String> fileSystemIds);

	public boolean containsPrinter(String id);

	public boolean containsFileSystem(String id);

	public void clear();
}
