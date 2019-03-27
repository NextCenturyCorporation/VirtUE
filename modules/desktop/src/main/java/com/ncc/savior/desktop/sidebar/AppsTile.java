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
package com.ncc.savior.desktop.sidebar;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.ncc.savior.desktop.virtues.VirtueService;

/**
 * This is the application tile component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsTile extends AbstractAppsView {

	public AppsTile(VirtueService virtueService, JScrollPane sp)
			throws IOException {
		super(virtueService, sp);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	@Override
	public void addTile(VirtueApplicationItem va) {
		container.add(va.getContainer());
	}

}
