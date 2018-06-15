package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class AppsList extends AbstractAppsView {

	public AppsList(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout());
	}

	public void addTiles(DesktopVirtue virtue, VirtueContainer vc, FavoritesView fv) throws IOException {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			JPanel tile = new JPanel();
			tile.setBorder(new LineBorder(Color.GRAY, 1));
			tile.setBackground(Color.WHITE);
			JLabel appImage = new JLabel(ad.getName());
			appImage.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
			appImage.setHorizontalAlignment(SwingConstants.LEFT);
			tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
			tile.add(appImage);
			tile.setSize(new Dimension(475, 70));
			tile.setMinimumSize(new Dimension(475, 70));
			tile.setMaximumSize(new Dimension(475, 70));
			tile.setPreferredSize(new Dimension(475, 70));

			addListener(tile, vc, fv, ad, virtue);

			tiles.put(ad.getName(), tile);

			container.add(tile);
		}
		container.validate();
		container.repaint();
	}


	public JPanel getContainer() {
		return container;
	}

}
