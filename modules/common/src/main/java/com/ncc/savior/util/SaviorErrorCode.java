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
package com.ncc.savior.util;

/**
 * Error codes for savior server. These are generally grouped into sections. See
 * comments below.
 *
 *
 */
public enum SaviorErrorCode {
	//@formatter:off
	//User/authentication errors
	USER_DISABLED(2, "User has been disabled", 403),
	USER_NOT_AUTHORIZED(3, "User is not authorized", 403),
	REQUESTED_USER_NOT_LOGGED_IN(4, "User not logged in"),
	//Missing item database errors
	//Should these have 404s?  it somewhat depends on where they are called
	VIRTUE_TEMPLATE_NOT_FOUND(20, "Virtue Template was not found", 400),
	VIRTUE_NOT_FOUND(21, "Virtue was not found", 400),
	VM_TEMPLATE_NOT_FOUND(22, "Virtual Machine template not found", 400),
	VM_NOT_FOUND(23, "Virtual Machine not found", 400),
	USER_NOT_FOUND(24, "User not found", 400),
	APPLICATION_NOT_FOUND(25, "Application not found", 400),
	PERMISSION_NOT_FOUND(26, "Permission not found", 400),
	ID_MISMATCH(27, "Object IDs do not match", 400),
	STORAGE_NOT_FOUND(28, "Storage not found", 400),
	CIFS_PROXY_NOT_FOUND(29, "CIFS Proxy not found", 400),
	PRINTER_NOT_FOUND(30, "Printer not found"),
	FILE_SYSTEM_NOT_FOUND(31, "File System not found"),
	PRINTER_ALREADY_EXISTS(32, "A printer with that ID already exists"),
	FILE_SYSTEM_ALREADY_EXISTS(33, "A file system with that ID already exists"),

	//bad data?
	INVALID_INPUT(50, "Invalid input"),

	SSH_ERROR(60, "SSH Error"),
	//Configuration errors
	CONFIGURATION_ERROR(100, "Configuration error", 500),
	//Configuration specific to import
	IMPORT_NOT_FOUND(120, "Import was not found"),
	IMAGE_IMPORT_ERROR(121, "Error importing image"),
	//Runtime like errors
	SERVICE_TIMEOUT(200, "Service timed out", 500),
	XPRA_FAILED(201, "Error with XPRA server", 500),

	JSON_ERROR(203, "Error reading json", 500),
	INVALID_STATE(204, "Object in invalid state to perform action", 500),
	//Other

	AWS_ERROR(300, "Error with AWS", 500),
	VOLUME_IN_USE(301, "Volume in use" ,412),
	MULTIPLE_STORAGE_ERROR(302, "Multiple Storage Errors", 400),
	SECURITY_GROUP_NOT_FOUND(310, "Security Group Not Found",400),

	DATABASE_ERROR(401, "Database Error", 500),
	CIFS_PROXY_ERROR(402, "CIFS Proxy error", 500),
	TEMPLATE_ERROR(403, "Template Service Error", 500),

	NOT_IMPLEMENTED(254, "function not implemented", 501),
	UNKNOWN_ERROR(255, "unknown error"),
	//Errors we shouldn't see because its older code
	VIRTUAL_BOX_ERROR(1001, "Error with virtual box");
	//@formatter:on
	private final int errorCode;
	private final String readableString;
	private int httpResponseCode;

	SaviorErrorCode(int errorCode, String readableString) {
		this(errorCode, readableString, 400);
	}

	SaviorErrorCode(int errorCode, String readableString, int httpResponseCode) {
		this.errorCode = errorCode;
		this.readableString = readableString;
		this.httpResponseCode = httpResponseCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getReadableString() {
		return readableString;
	}

	public int getValue() {
		return errorCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}
}
