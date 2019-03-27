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
package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * This is an interface that will help encapsulate the IconResourceService class
 */

public interface IIconService {

	public void getImage(String iconKey, Consumer<Image> consumer)
			throws IOException, InterruptedException, ExecutionException;

	public Image getImageNow(String iconKey);

}
