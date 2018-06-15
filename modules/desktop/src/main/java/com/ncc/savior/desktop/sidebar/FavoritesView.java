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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class FavoritesView extends AbstractAppsView {

	private static final Logger logger = LoggerFactory.getLogger(FavoritesView.class);

	public FavoritesView(VirtueService vs, JScrollPane sp) throws IOException {
		super(vs, sp);
		container.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		container.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		container.validate();
		container.repaint();
	}

	public void addFavorite(ApplicationDefinition ad, DesktopVirtue virtue, VirtueContainer vc) throws IOException {
		if (tiles.get(ad.getName()) == null) {
			JPanel tile = new JPanel();
			tile.setPreferredSize(new Dimension(90, 90));
			tile.setBackground(Color.WHITE);
			JLabel appName = new JLabel(ad.getName());
			appName.setHorizontalAlignment(SwingConstants.CENTER);
			appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
			tile.setLayout(new BorderLayout(0, 0));

			ImageIcon imageIcon = new ImageIcon(AppsTile.class.getResource("/images/Test.png"));
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back

			JLabel picLabel = new JLabel(imageIcon);
			tile.add(picLabel);
			tile.add(appName, BorderLayout.SOUTH);

			JLabel removeFavorite = new JLabel();
			removeFavorite.setIcon(new ImageIcon(AppsList.class.getResource("/images/remove-from-favorites.png")));
			removeFavorite.setHorizontalAlignment(SwingConstants.LEFT);
			tile.add(removeFavorite, BorderLayout.NORTH);

			addListener(removeFavorite, tile, vc, virtue, ad);

			container.validate();
			container.repaint();
			container.add(tile);
			tiles.put(ad, tile);
		}
	}

	public void addListener(JLabel removeFavorite, JPanel tile, VirtueContainer vc, DesktopVirtue virtue,
			ApplicationDefinition ad) {
		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (SwingUtilities.isLeftMouseButton(event)) {
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
								String msg = "Error attempting to start a " + ad.getName() + " application";
								logger.error(msg, e);
							}
						}
					});

					pm.setPopupSize(415, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				} else {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to unfavorite the " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							container.remove(tiles.get(ad.getName()));
							container.validate();
							container.repaint();
							tiles.remove(ad.getName());
						}
					});

					pm.setPopupSize(415, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}
			}
		});

		removeFavorite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to unfavorite the " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
						container.remove(tiles.get(ad));
							container.validate();
							container.repaint();
						tiles.remove(ad);
						}
					});

					pm.setPopupSize(415, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
			}
		});
	}

	public void addFavorite(JPanel tile, ApplicationDefinition ad) {
		// container.validate();
		// container.repaint();
		container.add(tile);
		tiles.put(ad, tile);
	}

	public void removeFavorite(ApplicationDefinition ad) {
		if (tiles != null) {
			container.remove(tiles.get(ad));
			container.validate();
			container.repaint();
			tiles.remove(ad);
		}
	}

	public JPanel getView() {
		return container;
	}
}
