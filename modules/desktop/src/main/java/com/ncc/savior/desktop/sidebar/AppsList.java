package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class AppsList {
	private JPanel container;
	private JScrollPane sp;
	private VirtueService virtueService;

	public AppsList(VirtueService vs, JScrollPane sp) throws IOException {
		this.container = new JPanel();
		this.sp = sp;
		this.virtueService = vs;
		container.setLayout(new ModifiedFlowLayout());
	}

	public void addTiles(DesktopVirtue virtue, VirtueContainer vc, FavoritesView fv) throws IOException {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			JPanel tile = new JPanel();
			tile.setBorder(new LineBorder(new Color(128, 128, 128), 1));
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

			container.add(tile);
		}
		container.validate();
		container.repaint();
	}

	public void addListener(JPanel tile, VirtueContainer vc, FavoritesView fv, ApplicationDefinition ad,
			DesktopVirtue virtue) {
		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (SwingUtilities.isLeftMouseButton(arg0)) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to start a " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								virtueService.startApplication(virtue, ad, new RgbColor(0, 0, 0, 0));
								virtue.setVirtueState(VirtueState.LAUNCHING);
								vc.updateVirtue(virtue);
							} catch (IOException e) {
							}
						}
					});

					pm.setPopupSize(375, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}

				if (SwingUtilities.isRightMouseButton(arg0)) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to favorite the " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								fv.addFavorite(ad);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});

					pm.setPopupSize(375, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}
			}
		});
	}

	public JPanel getContainer() {
		return container;
	}

}
