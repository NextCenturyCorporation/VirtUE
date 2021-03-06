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
package com.ncc.savior.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.IconModel;
import com.ncc.savior.virtueadmin.model.SecurityGroupPermission;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Application class that just lists what is contained in a exported zip file.
 */
public class ExportViewer {

	private static final String INDENT = "  ";

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			printUsage();
			System.exit(1);
		}
		File file = new File(args[0]);
		if (file.exists() && file.isFile()) {
			readFile(file);
		} else {
			System.err.println("File '" + file.getCanonicalPath() + "' not found.");
		}
	}

	private static void readFile(File file) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		List<ApplicationDefinition> apps = new ArrayList<ApplicationDefinition>();
		List<VirtualMachineTemplate> vms = new ArrayList<VirtualMachineTemplate>();
		List<VirtueTemplate> vts = new ArrayList<VirtueTemplate>();
		List<VirtueUser> users = new ArrayList<VirtueUser>();
		List<IconModel> icons = new ArrayList<IconModel>();
		List<SecurityGroupPermission> sgps = new ArrayList<SecurityGroupPermission>();
		Map<String, Long> imageToSizeMap = new TreeMap<String, Long>();
		try {
			BiConsumer<ZipEntry, InputStream> vmImageConsumer = (ze, stream) -> {
				long size = ze.getSize();
				String name = ze.getName();
				String path = name.substring(ImportExportUtils.VIRTUAL_MACHINE_IMAGE_ZIP_ROOT.length());
				// String extension = "";
				int dotIndex = path.lastIndexOf(".");
				if (dotIndex > -1) {
					// extension = path.substring(dotIndex + 1);
					path = path.substring(0, dotIndex);
				}
				imageToSizeMap.put(path, size);
			};
			ImportExportUtils.readImportExportZipStream(fis, users, vts, vms, apps, icons, sgps, vmImageConsumer);
			printUsers(users);
			printVirtues(vts);
			printVms(vms, imageToSizeMap);
			printApps(apps);
			printIcons(icons);
			printImages(imageToSizeMap);
			printSecurityGroupPermissions(sgps);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void printImages(Map<String, Long> imageToSizeMap) {
		System.out.println("VM Images (" + imageToSizeMap.size() + "): ");
		for (Entry<String, Long> entry : imageToSizeMap.entrySet()) {
			System.out.println(INDENT + entry.getKey());
		}
		System.out.println();
	}

	private static void printIcons(List<IconModel> icons) {
		System.out.println("Icons (" + icons.size() + "): ");
		Collections.sort(icons, IconModel.CASE_INSENSITIVE_ID_COMPARATOR);
		for (IconModel icon : icons) {
			System.out.println(INDENT + icon.getId());
		}
		System.out.println();
	}

	private static void printApps(List<ApplicationDefinition> apps) {
		System.out.println("Applications (" + apps.size() + "):");
		Collections.sort(apps, ApplicationDefinition.CASE_INSENSITIVE_NAME_COMPARATOR);
		for (ApplicationDefinition app : apps) {
			System.out.println(INDENT + app.getName() + " - " + app.getId());
		}
		System.out.println();
	}

	private static void printVms(List<VirtualMachineTemplate> vms, Map<String, Long> imageToSizeMap) {
		System.out.println("Virtual Machines (" + imageToSizeMap.size() + "):");
		Collections.sort(vms, VirtualMachineTemplate.CASE_INSENSITIVE_NAME_COMPARATOR);
		for (VirtualMachineTemplate vm : vms) {
			// Long exists = imageToSizeMap.get(vm.getTemplatePath());
			// TODO after testing, fix this look
			System.out.println(INDENT + vm.getName() + " - " + vm.getId() + " Image:"
					+ imageToSizeMap.containsKey(vm.getTemplatePath()));
		}
		System.out.println();
	}

	private static void printVirtues(List<VirtueTemplate> vts) {
		System.out.println("Virtues (" + vts.size() + "):");
		Collections.sort(vts, VirtueTemplate.CASE_INSENSITIVE_NAME_COMPARATOR);
		for (VirtueTemplate vt : vts) {
			System.out.println(INDENT + vt.getName() + " - " + vt.getId());
		}
		System.out.println();
	}

	private static void printUsers(List<VirtueUser> users) {
		System.out.println("Users (" + users.size() + "):");
		Collections.sort(users, VirtueUser.USERNAME_COMPARATOR);
		for (VirtueUser user : users) {
			System.out.println(INDENT + user.getUsername() + " - " + user.getAuthorities());
		}
		System.out.println();
	}

	private static void printSecurityGroupPermissions(List<SecurityGroupPermission> sgps) {
		System.out.println("SecurityGroupPermissions (" + sgps.size() + "):");
		Collections.sort(sgps, SecurityGroupPermission.TEMPLATE_ID_COMPARATOR);
		for (SecurityGroupPermission sgp : sgps) {
			System.out.println(INDENT + " Key: " + sgp.getKey() + " Template: " + sgp.getTemplateId()
					+ " SecurityGroup: " + sgp.getSecurityGroupId() + " Ingress: " + sgp.isIngress() + " CidrIp: "
					+ sgp.getCidrIp() + " Protocol: " + sgp.getIpProtocol() + " ToPort: " + sgp.getToPort()
					+ " FromPort: " + sgp.getFromPort());
		}

	}

	private static void printUsage() {
		System.out.println("Usage: <cmd> <zipfile>");

	}

}
