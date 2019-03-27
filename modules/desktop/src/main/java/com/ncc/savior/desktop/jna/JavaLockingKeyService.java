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
package com.ncc.savior.desktop.jna;

import java.awt.Toolkit;

/**
 * Java implementation of {@link ILockingKeysService}. Note that this
 * implementation is no reliable on all systems! Particularly Windows 7.
 *
 *
 */
public class JavaLockingKeyService implements ILockingKeysService {

	private static JavaLockingKeyService INSTANCE = null;
	private static Object lock = new Object();

	private JavaLockingKeyService() {
	}

	public static ILockingKeysService getInstance() {
		if (INSTANCE == null) {
			synchronized (lock) {
				if (INSTANCE == null) {
					INSTANCE = new JavaLockingKeyService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public boolean getLockingKeyState(int key) {
		return Toolkit.getDefaultToolkit().getLockingKeyState(key);
	}

}
