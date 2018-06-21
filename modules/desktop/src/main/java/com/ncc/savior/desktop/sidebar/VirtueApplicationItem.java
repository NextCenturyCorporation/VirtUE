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

/**
 * This is the application list component that can be set as the view to the
 * sidebar scrollPane
 */

public class VirtueApplicationItem {

	private static final Logger logger = LoggerFactory.getLogger(VirtueApplicationItem.class);

	private ChangeListener changeListener;
	private PropertyChangeListener listener;

	private ImageIcon favoritedImage = new ImageIcon(VirtueApplicationItem.class.getResource("/images/favorited.png"));
	private ImageIcon unfavoritedImage = new ImageIcon(
			VirtueApplicationItem.class.getResource("/images/unfavorited.png"));

	private VirtueService virtueService;
	private JScrollPane sp;

	private ApplicationDefinition ad;
	private DesktopVirtue virtue;

	private FavoritesView fv;
	private VirtueContainer vc;

	private JLabel favoritedLabel;
	private JLabel appName;
	private JLabel appIcon;
	private JPanel container;

	public VirtueApplicationItem(ApplicationDefinition ad, VirtueService virtueService, JScrollPane sp,
			VirtueContainer vc, DesktopVirtue virtue, FavoritesView fv, PropertyChangeListener listener) {
		this.sp = sp;
		this.vc = vc;
		this.virtueService = virtueService;
		this.ad = ad;
		this.virtue = virtue;
		this.fv = fv;

		this.appIcon = new JLabel();
		this.container = new JPanel();
		this.favoritedLabel = new JLabel();
		this.appName = new JLabel(ad.getName());
		this.appIcon.setHorizontalAlignment(SwingConstants.CENTER);

		this.changeListener = new ChangeListener();
		this.listener = listener;
	}

	public void tileSetup() {
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

		addListener(vc, fv, ad, virtue);
	}

	public void listSetup() {
		this.container = new JPanel();
		container.setBorder(new LineBorder(Color.GRAY, 1));
		container.setBackground(Color.WHITE);
		this.appIcon = new JLabel(ad.getName());
		appIcon.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
		appIcon.setHorizontalAlignment(SwingConstants.LEFT);

		this.favoritedLabel = new JLabel();
		favoritedLabel.setIcon(unfavoritedImage);
		favoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);

		container.add(favoritedLabel, BorderLayout.NORTH);
		container.add(appIcon);

		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		container.setSize(new Dimension(475, 70));
		container.setMinimumSize(new Dimension(475, 70));
		container.setMaximumSize(new Dimension(475, 70));
		container.setPreferredSize(new Dimension(475, 70));

		addListener(vc, fv, ad, virtue);
	}

	public void addListener(VirtueContainer vc, FavoritesView fv, ApplicationDefinition ad, DesktopVirtue virtue) {

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
							virtueService.startApplication(vc.getVirtue(), ad, new RgbColor(0, 0, 0, 0));
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

		favoritedLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				sendChangeEvent(new PropertyChangeEvent("", "isFavorited", null, null));
			}
		});
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public JPanel getContainer() {
		return container;
	}

	public void favorite() {
		fv.addFavorite(ad, virtue, vc, sp, listener);
		favoritedLabel.setIcon(favoritedImage);
	}

	public void setToFavorited() {
		favoritedLabel.setIcon(favoritedImage);
	}

	public void unfavorite() {
		fv.removeFavorite(ad);
		favoritedLabel.setIcon(unfavoritedImage);
	}

	public ChangeListener getChangeListener() {
		return changeListener;
	}

	public String getApplicationName() {
		return ad.getName();
	}

	public ApplicationDefinition getApplication() {
		return ad;
	}

	public void registerListener(PropertyChangeListener listener) {
		this.listener = listener;
	}

	private void sendChangeEvent(PropertyChangeEvent propertyChangeEvent) {
		listener.propertyChange(propertyChangeEvent);
	}


	private class ChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ((boolean) evt.getNewValue() == true) {
				favorite();
			} else {
				unfavorite();
			}
		}
	}

}