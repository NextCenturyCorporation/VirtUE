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
package com.ncc.savior.virtueadmin.infrastructure.subnet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ncc.savior.virtueadmin.model.CidrBlock;

public class CidrBlockTest {

	@Test
	public void testNextCidrBlock1() {
		CidrBlock first = new CidrBlock(10, 0, 0, 1, 24);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 1, 1, 24);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock2() {
		CidrBlock first = new CidrBlock(10, 34, 56, 1, 24);
		CidrBlock expectedSecond = new CidrBlock(10, 34, 57, 1, 24);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock3() {
		CidrBlock first = new CidrBlock(10, 0, 0, 1, 32);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 0, 2, 32);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock4() {
		CidrBlock first = new CidrBlock(10, 0, 0, 0, 28);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 0,16, 28);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
}
