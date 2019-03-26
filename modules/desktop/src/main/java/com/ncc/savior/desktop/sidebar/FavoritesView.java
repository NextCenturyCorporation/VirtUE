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
import java.util.Comparator;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the favorites component that can be set as the view to the sidebar
 * scrollPane
 */

public class FavoritesView extends AbstractAppsView {

	private Preferences favorites;

	public FavoritesView(VirtueService virtueService, JScrollPane sp, Preferences favorites) throws IOException {
		super(virtueService, sp);
		this.favorites = favorites;

		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addFavorite(ApplicationDefinition ad, DesktopVirtue virtue, VirtueApplicationItem va,
			JTextField textField, Comparator<VirtueApplicationItem> comp, GhostText ghostText) {
		if (tiles.get(ad.getId() + virtue.getTemplateId()) == null) {
			favorites.putBoolean(ad.getId() + virtue.getTemplateId(), true);

			container.add(va.getContainer());
			tiles.put(ad.getId() + virtue.getTemplateId(), va);
		}
		String input = textField.getText();
		if (ghostText.getIsVisible()) {
			input = "";
		}
		String keyword = input;
		search(keyword, comp, currVa -> currVa.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
	}

	public void removeFavorite(ApplicationDefinition ad, DesktopVirtue virtue) {
		if (tiles != null && tiles.get(ad.getId() + virtue.getTemplateId()) != null) {
			container.remove(tiles.get(ad.getId() + virtue.getTemplateId()).getContainer());
			container.validate();
			container.repaint();

			favorites.remove(ad.getId() + virtue.getTemplateId());
			tiles.remove(ad.getId() + virtue.getTemplateId());
		}
	}

	public void setTileImage(ApplicationDefinition ad, DesktopVirtue virtue, ImageIcon image) {
		VirtueApplicationItem va = tiles.get(ad.getId() + virtue.getTemplateId());
		if (va != null) {
			va.setImage(image);
		}
	}

	@Override
	public void addTile(VirtueApplicationItem va) {
		container.add(va.getContainer());
	}
}
