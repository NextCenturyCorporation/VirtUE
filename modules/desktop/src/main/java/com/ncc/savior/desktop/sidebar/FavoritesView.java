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
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class FavoritesView {
	private JPanel view;
	private JScrollPane sp;
	private HashMap<String, JPanel> favorites;

	public FavoritesView(JScrollPane sp) throws IOException {
		this.view = new JPanel();
		this.sp = sp;
		this.favorites = new HashMap<String, JPanel>();
		view.setLayout(new ModifiedFlowLayout(FlowLayout.CENTER, 20, 20));

		view.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		view.validate();
		view.repaint();
	}

	public void addFavorite(ApplicationDefinition ad) throws IOException {
		if (favorites.get(ad.getName()) == null) {
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
			tile.setSize(0, 40);

			addListener(tile, ad.getName());

			view.validate();
			view.repaint();
			view.add(tile);
			favorites.put(ad.getName(), tile);
		}
	}

	public void addListener(JPanel tile, String name) {
		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (SwingUtilities.isLeftMouseButton(arg0)) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to start a " + name + " application?"));

					pm.setPopupSize(415, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				} else {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to unfavorite the " + name + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							view.remove(favorites.get(name));
							view.validate();
							view.repaint();
							favorites.remove(name);
						}
					});

					pm.setPopupSize(415, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}
			}
		});
	}

	public JPanel getView() {
		return view;
	}
}
