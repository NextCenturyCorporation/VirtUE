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
package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Schema(description="Entry to store an icon.")
public class IconModel {
	@Id
	@Schema(description="The ID to find the icon, also described as the key.")
	private String id;
	@Lob
	@Column(length = 100000)
	@Schema(description="The bytes for the image.")
	private byte[] data;

	// for Jackson serialization
	protected IconModel() {

	}

	public IconModel(String id, byte[] data) {
		super();
		this.id = id;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public static final Comparator<? super IconModel> CASE_INSENSITIVE_ID_COMPARATOR = new CaseInsensitiveIdComparator();
	private static class CaseInsensitiveIdComparator implements Comparator<IconModel> {
		@Override
		public int compare(IconModel o1, IconModel o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId());
		}
	}
}
