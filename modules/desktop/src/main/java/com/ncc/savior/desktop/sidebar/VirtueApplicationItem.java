package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class VirtueApplicationItem {

	private static final Logger logger = LoggerFactory.getLogger(SidebarController.class);

	private ArrayList<PropertyChangeListener> listeners;

	private boolean isFavorited = false;

	private ImageIcon favoritedImage = new ImageIcon(VirtueApplicationItem.class.getResource("/images/favorited.png"));
	private ImageIcon unfavoritedImage = new ImageIcon(
			VirtueApplicationItem.class.getResource("/images/unfavorited.png"));

	private VirtueService virtueService;
	private JScrollPane sp;

	private JLabel tileFavoritedLabel;
	private JLabel tileAppName;
	private JLabel tileAppIcon;
	private JPanel tileContainer;

	private JLabel favoriteFavoritedLabel;
	private JLabel favoriteAppName;
	private JLabel favoriteAppIcon;
	private JPanel favoriteContainer;

	private JLabel virtueTileFavoritedLabel;
	private JLabel virtueTileAppName;
	private JLabel virtueTileAppIcon;
	private JPanel virtueTileContainer;

	private JLabel listFavoritedLabel;
	private JLabel listAppIcon;
	private JPanel listContainer;

	public VirtueApplicationItem(ApplicationDefinition ad, VirtueService virtueService, JScrollPane sp,
			VirtueContainer vc) {
		this.sp = sp;
		this.virtueService = virtueService;
		String name = ad.getName();

		this.listeners = new ArrayList<PropertyChangeListener>();

		this.tileAppIcon = new JLabel();
		this.tileContainer = new JPanel();
		this.tileFavoritedLabel = new JLabel();
		this.tileAppName = new JLabel(name);
		this.tileAppIcon.setHorizontalAlignment(SwingConstants.CENTER);
		tileSetup(tileContainer, tileAppName, tileAppIcon, tileFavoritedLabel);

		this.favoriteAppIcon = new JLabel();
		this.favoriteContainer = new JPanel();
		this.favoriteFavoritedLabel = new JLabel();
		this.favoriteAppName = new JLabel(name);
		this.favoriteAppIcon.setHorizontalAlignment(SwingConstants.CENTER);
		tileSetup(favoriteContainer, favoriteAppName, favoriteAppIcon, favoriteFavoritedLabel);

		this.virtueTileAppIcon = new JLabel();
		this.virtueTileContainer = new JPanel();
		this.virtueTileFavoritedLabel = new JLabel();
		this.virtueTileAppName = new JLabel(name);
		this.virtueTileAppIcon.setHorizontalAlignment(SwingConstants.CENTER);
		tileSetup(virtueTileContainer, virtueTileAppName, virtueTileAppIcon, virtueTileFavoritedLabel);

		listSetup(name);

		addListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ((boolean) evt.getNewValue() == true) {
					isFavorited = true;
					listFavoritedLabel.setIcon(favoritedImage);
					tileFavoritedLabel.setIcon(favoritedImage);
					favoriteFavoritedLabel.setIcon(favoritedImage);
					virtueTileFavoritedLabel.setIcon(favoritedImage);
				} else {
					isFavorited = false;
					listFavoritedLabel.setIcon(unfavoritedImage);
					tileFavoritedLabel.setIcon(unfavoritedImage);
					favoriteFavoritedLabel.setIcon(unfavoritedImage);
					virtueTileFavoritedLabel.setIcon(unfavoritedImage);
				}
			}

		});
	}

	public void tileSetup(JPanel container, JLabel appName, JLabel appIcon, JLabel favoritedLabel) {
		container.setPreferredSize(new Dimension(90, 90));
		container.setBackground(Color.WHITE);
		appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		appName.setHorizontalAlignment(SwingConstants.CENTER);
		container.setLayout(new BorderLayout(0, 0));

		ImageIcon imageIcon = new ImageIcon(AppsTile.class.getResource("/images/Test.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg); // transform it back

		appIcon.setIcon(imageIcon);

		favoritedLabel.setIcon(unfavoritedImage);
		favoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);

		container.add(appIcon, BorderLayout.CENTER);
		container.add(appName, BorderLayout.SOUTH);
		container.add(favoritedLabel, BorderLayout.NORTH);
	}

	public void listSetup(String appName) {
		this.listContainer = new JPanel();
		listContainer.setBorder(new LineBorder(Color.GRAY, 1));
		listContainer.setBackground(Color.WHITE);
		this.listAppIcon = new JLabel(appName);
		listAppIcon.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
		listAppIcon.setHorizontalAlignment(SwingConstants.LEFT);

		this.listFavoritedLabel = new JLabel();
		listFavoritedLabel.setIcon(unfavoritedImage);
		listFavoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);

		listContainer.add(listFavoritedLabel, BorderLayout.NORTH);
		listContainer.add(listAppIcon);

		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.X_AXIS));
		listContainer.setSize(new Dimension(475, 70));
		listContainer.setMinimumSize(new Dimension(475, 70));
		listContainer.setMaximumSize(new Dimension(475, 70));
		listContainer.setPreferredSize(new Dimension(475, 70));
	}

	public void setup(VirtueContainer vc, FavoritesView fv, ApplicationDefinition ad,
			DesktopVirtue virtue) {
		addListener(tileContainer, tileFavoritedLabel, vc, fv, ad, virtue);
		addListener(virtueTileContainer, virtueTileFavoritedLabel, vc, fv, ad, virtue);
		addListener(favoriteContainer, favoriteFavoritedLabel, vc, fv, ad, virtue);
		addListener(listContainer, listFavoritedLabel, vc, fv, ad, virtue);
	}

	public void addListener(JPanel container, JLabel favorited, VirtueContainer vc, FavoritesView fv,
			ApplicationDefinition ad, DesktopVirtue virtue) {

		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem mi1 = new JMenuItem("Yes");
				JMenuItem mi2 = new JMenuItem("No");
				pm.add(new JLabel("Would you like to start a " + ad.getName() + " application?"));

				mi1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.startApplication(virtue, ad, new RgbColor(0, 0, 0, 0));
							// virtue.setVirtueState(VirtueState.LAUNCHING);
							// vc.updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to start a " + ad.getName() + " application";
							logger.error(msg);
						}
					}
				});

				pm.setPopupSize(375, 75);
				pm.add(mi1);
				pm.add(mi2);
				pm.show(sp, 50, 150);
			}
		});

		favorited.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (isFavorited) {
					fv.removeFavorite(ad);
					unfavorite();
				} else {
					fv.addFavorite(favoriteContainer, ad);
					favorite();
				}
			}
		});
	}

	public void addListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	private void sendChangeEvent(PropertyChangeEvent propertyChangeEvent) {
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(propertyChangeEvent);
		}
	}

	public JPanel getTileContainer() {
		return tileContainer;
	}

	public JPanel getListContainer() {
		return listContainer;
	}

	public JPanel getVirtueTileContainer() {
		return virtueTileContainer;
	}

	public void favorite() {
		sendChangeEvent(new PropertyChangeEvent("favorite", "isFavorited", false, true));
	}

	public void unfavorite() {
		sendChangeEvent(new PropertyChangeEvent("favorite", "isFavorited", true, false));
	}

}