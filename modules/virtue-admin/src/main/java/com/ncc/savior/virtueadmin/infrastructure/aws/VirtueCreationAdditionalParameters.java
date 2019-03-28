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
package com.ncc.savior.virtueadmin.infrastructure.aws;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtuePrimaryPurpose;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil.VirtueSecondaryPurpose;

/**
 * POJO class that handles extra information for a Virtue. This class is for
 * transient data used in virtue setup.
 */
public class VirtueCreationAdditionalParameters {

	private String subnetId;
	private String name;
	private String securityGroupId;
	private String virtueId;
	private String virtueTemplateId;
	private VirtuePrimaryPurpose primaryPurpose;
	private VirtueSecondaryPurpose secondaryPurpose;
	private String username;

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}

	public void setVirtueTemplateId(String virtueTemplateId) {
		this.virtueTemplateId = virtueTemplateId;
	}

	public VirtueCreationAdditionalParameters(String name) {
		this.name = name;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecurityGroupId() {
		return securityGroupId;
	}

	public void setSecurityGroupId(String securityGroupId) {
		this.securityGroupId = securityGroupId;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public String getVirtueTemplateId() {
		return virtueTemplateId;
	}

	public VirtuePrimaryPurpose getPrimaryPurpose() {
		return primaryPurpose;
	}

	public void setPrimaryPurpose(VirtuePrimaryPurpose primaryPurpose) {
		this.primaryPurpose = primaryPurpose;
	}

	public VirtueSecondaryPurpose getSecondaryPurpose() {
		return secondaryPurpose;
	}

	public void setSecondaryPurpose(VirtueSecondaryPurpose secondaryPurpose) {
		this.secondaryPurpose = secondaryPurpose;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
