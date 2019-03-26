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
package com.ncc.savior.virtueadmin.infrastructure.aws.subnet;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;

/**
 * {@link IVpcSubnetProvider} that uses a single VPC and single subnet and
 * always returns those.
 * 
 *
 */
public class StaticVpcSubnetProvider implements IVpcSubnetProvider {
	private static final Logger logger = LoggerFactory.getLogger(StaticVpcSubnetProvider.class);
	private String subnetId;
	private String vpcId;
	private AwsEc2Wrapper ec2Wrapper;

	public StaticVpcSubnetProvider(AwsEc2Wrapper ec2Wrapper, String vpcName, String subnetName) {
		this.ec2Wrapper = ec2Wrapper;
		getVpcAndSubnetIds(subnetName, vpcName);
	}

	public void getVpcAndSubnetIds(String subnetName, String vpcName) {
		String vpcId = AwsUtil.getVpcIdFromVpcName(vpcName, ec2Wrapper);
		String newSubnetId = AwsUtil.getSubnetIdFromName(vpcId, subnetName, ec2Wrapper);

		if (newSubnetId != null) {
			this.subnetId = newSubnetId;
			this.vpcId = vpcId;
		} else {
			logger.error("Failed to find subnet with name=" + subnetName);
		}
	}

	@Override
	public String getVpcId() {
		return vpcId;
	}

	@Override
	public String getSubnetId(String subnetKey, Map<String, String> tags) {
		return this.subnetId;
	}

	@Override
	public void releaseBySubnetKey(String id) {
		// do nothing
	}

	@Override
	public void sync(Collection<String> existingVirtueIds) {
		//do nothing
	}
}
