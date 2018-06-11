package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class AppsTile {
	private JPanel container;
	private VirtueService virtueService;
	private JScrollPane sp;

	public AppsTile(VirtueService vs, JScrollPane sp) throws IOException {
		this.container = new JPanel();
		this.virtueService = vs;
		this.sp = sp;
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addTiles(DesktopVirtue virtue, VirtueContainer vc, FavoritesView fv) throws IOException {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			JPanel tile = new JPanel();
			tile.setPreferredSize(new Dimension(90, 90));
			tile.setBackground(Color.WHITE);
			JLabel appName = new JLabel(ad.getName());
			appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
			appName.setHorizontalAlignment(SwingConstants.CENTER);
			tile.setLayout(new BorderLayout(0, 0));

			ImageIcon imageIcon = new ImageIcon(AppsTile.class.getResource("/images/Test.png"));
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back

			JLabel appImage = new JLabel(imageIcon);
			tile.add(appImage);
			tile.add(appName, BorderLayout.SOUTH);
			tile.setSize(0, 40);

			addListener(tile, vc, fv, ad, virtue);

			container.validate();
			container.repaint();
			container.add(tile);
		}
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
					JLabel prompt = new JLabel("Would you like to start a " + ad.getName() + " application?");
					pm.add(new JLabel("Would you like to start a " + ad.getName() + " application?"));
					prompt.setHorizontalAlignment(SwingConstants.CENTER);

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								// virtueService.startVirtue(virtue);
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
