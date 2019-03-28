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
package com.ncc.savior.desktop.xpra.protocol;

/**
 * These are the different encodings for image data used by Xpra. The client
 * does not need to handle all, but must let the server know which ones it can
 * handle. The server has a prioritized list and will choose the best available.
 *
 *
 */
public enum ImageEncoding {
	rgb24, premult_argb32, jpeg, png, pngP("png/P"), pngL("png/L"), h264;

	private final String code;

	ImageEncoding() {
		code = name();

	}

	ImageEncoding(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code;
	}

	public static ImageEncoding parse(String string) {
		for (ImageEncoding ie : ImageEncoding.values()) {
			if (ie.getCode().equals(string)) {
				return ie;
			}
		}
		return ImageEncoding.valueOf(string);
	}
}
