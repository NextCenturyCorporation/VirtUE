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
 * Generic exception for savior that could be passed to user or APIs
 * 
 *
 */
public class SaviorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	// List of errors and assigned codes.
	// TODO codes need to be assigned (255 means unassigned/unknown) and probably
	// should be turned in to enums.


	private SaviorErrorCode errorCode;

	public SaviorException(SaviorErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public SaviorException(SaviorErrorCode errorCode, String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public SaviorErrorCode getErrorCode() {
		return errorCode;
	}
}
