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

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;

/**
 * Windows JNA based {@link ILockingKeysService} implementation. Uses user32.dll
 * GetKeyState function.
 *
 *
 */
public class WindowsJnaLockingKeyService implements ILockingKeysService {

	// documentation says least significant bit. Most significant bit is whether it
	// is currently pressed
	private static final int LOCKING_KEY_TOGGLE_BIT = 0x01;
	private static WindowsJnaLockingKeyService INSTANCE = null;
	private static Object lock = new Object();
	private IWindowsClipboardUser32 user32;

	private WindowsJnaLockingKeyService() {
		user32 = IWindowsClipboardUser32.INSTANCE;
	}

	public static ILockingKeysService getInstance() {
		if (INSTANCE == null) {
			synchronized (lock) {
				if (INSTANCE == null) {
					INSTANCE = new WindowsJnaLockingKeyService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public boolean getLockingKeyState(int vKey) {
		short state = user32.GetKeyState(vKey);
		boolean toggled = (state & LOCKING_KEY_TOGGLE_BIT) == 1;
		return toggled;
	}
}
