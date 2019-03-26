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
package com.nextcentury.savior.freerdp;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FreeRDPWrapperTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.loadLibrary("jfreerdp");
	}

	@Test
	void testContextNew() {
		FreeRDPWrapper rdp = new FreeRDPWrapper();
		rdp_context context = new rdp_context();
		Assert.assertTrue(rdp.contextNew(context));
	}

	@Test
	void testContextFree() {
		FreeRDPWrapper rdp = new FreeRDPWrapper();
		rdp_context context = new rdp_context();
		Assert.assertTrue(rdp.contextNew(context));
		rdp.contextFree(context);
	}

	@Test
	void testPreConnect() {
		Callback cb = makeCallback();
		rdp_freerdp internal = cb.getInstance();
		freerdp.freerdp_connect(internal);
		Assert.assertTrue(cb.preConnectCalled);
	}

	private Callback makeCallback() {
		Callback cb = new Callback();
		rdp_context context = new rdp_context();
		cb.contextNew(context);
		return cb;
	}
	
	public static class Callback extends FreeRDPWrapper {
		boolean preConnectCalled = false;

		@Override
		public boolean preConnect() {
			preConnectCalled = true;
			return true;
		}

	}
}
