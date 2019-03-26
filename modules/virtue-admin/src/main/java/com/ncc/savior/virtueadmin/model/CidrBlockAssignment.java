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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Database model Entity for storing the assignment of a cidr block to a subnet.
 * 
 *
 */
@Entity
public class CidrBlockAssignment {
	@Column(unique = true)
	private String cidrBlock;
	@Id
	private String assignmentId;
	private String username;
	private String infrastructureId;

	/**
	 * Used for jackson deserialization
	 */
	protected CidrBlockAssignment() {

	}

	public CidrBlockAssignment(String cidrBlock, String assignmentId, String username, String infrastructureId) {
		super();
		this.cidrBlock = cidrBlock;
		this.assignmentId = assignmentId;
		this.username = username;
		this.infrastructureId = infrastructureId;
	}

	public String getCidrBlock() {
		return cidrBlock;
	}

	public void setCidrBlock(String cidrBlock) {
		this.cidrBlock = cidrBlock;
	}

	public String getAssignmentId() {
		return assignmentId;
	}

	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getInfrastructureId() {
		return infrastructureId;
	}

	public void setInfrastructureId(String infrastructureId) {
		this.infrastructureId = infrastructureId;
	}

	@Override
	public String toString() {
		return "CidrBlockAssignment [cidrBlock=" + cidrBlock + ", assignmentId=" + assignmentId + ", username="
				+ username + ", infrastructurId=" + infrastructureId + "]";
	}

}
