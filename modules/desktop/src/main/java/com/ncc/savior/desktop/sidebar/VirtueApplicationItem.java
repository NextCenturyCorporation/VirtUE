package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
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

public class VirtueApplicationItem implements Comparable<VirtueApplicationItem> {

	private static final Logger logger = LoggerFactory.getLogger(VirtueApplicationItem.class);

	private ChangeListener changeListener;
	private PropertyChangeListener listener;

	private static ImageIcon favoritedImage = new ImageIcon(
			VirtueApplicationItem.class.getResource("/images/favorited.png"));
	private static ImageIcon unfavoritedImage = new ImageIcon(
			VirtueApplicationItem.class.getResource("/images/unfavorited.png"));
	private Image image;

	private VirtueService virtueService;
	private JScrollPane sp;

	private ApplicationDefinition ad;
	private DesktopVirtue virtue;

	private FavoritesView fv;
	private VirtueTileContainer vc;

	private JLabel favoritedLabel;
	private JLabel appName;
	private JLabel appIcon;
	private JPanel container;

	private boolean isFavorited;
	private JTextField textField;
	private JComboBox<String> cb;

	private Comparator<VirtueApplicationItem> sortAppsByStatus;

	private JFrame frame;

	public VirtueApplicationItem(ApplicationDefinition ad, VirtueService virtueService, JScrollPane sp,
			VirtueTileContainer vc, DesktopVirtue virtue, FavoritesView fv, PropertyChangeListener listener,
			Image image, boolean isFavorited, JFrame frame, JTextField textField, JComboBox<String> cb,
			Comparator<VirtueApplicationItem> sortAppsByStatus) {
		this.sp = sp;
		this.vc = vc;
		this.virtueService = virtueService;
		this.ad = ad;
		this.virtue = virtue;
		this.fv = fv;
		this.image = image; // Must be 47x50 for tileView and 30x30 for listView
		this.frame = frame;
		this.textField = textField;
		this.cb = cb;
		this.sortAppsByStatus = sortAppsByStatus;

		this.appIcon = new JLabel();
		this.container = new JPanel();
		this.favoritedLabel = new JLabel();
		this.appName = new JLabel(ad.getName());
		this.appIcon.setHorizontalAlignment(SwingConstants.CENTER);

		this.isFavorited = isFavorited;

		favoritedLabel.setToolTipText("Click to favorite or unfavorite");
		container.setToolTipText("<html>" + "Virtue: " + virtue.getName() + "<br>" + "OS: " + ad.getOs() + "<br>"
				+ "Status: " + virtue.getVirtueState() + "<br>" + "</html>");

		this.changeListener = new ChangeListener();
		this.listener = listener;
	}

	public void tileSetup() {
		container.setBorder(new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.DARK_GRAY));
		JPanel favoritedContainer = new JPanel();
		favoritedContainer.setLayout(new GridBagLayout());
		favoritedContainer.setBackground(Color.WHITE);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 65);

		container.setPreferredSize(new Dimension(90, 90));
		container.setBackground(Color.WHITE);
		appName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		appName.setHorizontalAlignment(SwingConstants.CENTER);
		container.setLayout(new BorderLayout(0, 0));

		ImageIcon imageIcon = new ImageIcon(image);

		appIcon.setIcon(imageIcon);

		if (isFavorited) {
			favoritedLabel.setIcon(favoritedImage);
		} else {
			favoritedLabel.setIcon(unfavoritedImage);
		}

		favoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);
		favoritedContainer.add(favoritedLabel, gbc);

		container.add(appIcon, BorderLayout.CENTER);
		container.add(appName, BorderLayout.SOUTH);
		container.add(favoritedContainer, BorderLayout.NORTH);

		addListener(vc, fv, ad, virtue, false);
	}

	public void listSetup() {
		container.setBorder(new LineBorder(Color.GRAY, 1));
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());
		this.appIcon = new JLabel(ad.getName());

		ImageIcon imageIcon = new ImageIcon(image);

		appIcon.setIcon(imageIcon);
		appIcon.setHorizontalAlignment(SwingConstants.LEFT);

		this.favoritedLabel = new JLabel();

		if (isFavorited) {
			favoritedLabel.setIcon(favoritedImage);
		} else {
			favoritedLabel.setIcon(unfavoritedImage);
		}

		favoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);

		favoritedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 7));
		appIcon.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));

		container.add(favoritedLabel, BorderLayout.EAST);
		container.add(appIcon, BorderLayout.WEST);

		container.setSize(new Dimension(450, 57));
		// container.setMinimumSize(new Dimension(450, 57));
		// container.setMaximumSize(new Dimension(10000, 57));
		container.setPreferredSize(new Dimension(450, 57));

		addListener(vc, fv, ad, virtue, true);
	}

	public void addListener(VirtueTileContainer vc, FavoritesView fv, ApplicationDefinition ad, DesktopVirtue virtue,
			boolean fullBorder) {

		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!Sidebar.askAgain) {
					try {
						virtueService.startApplication(vc.getVirtue(), ad, new RgbColor(0, 0, 0, 0));
						// virtue.setVirtueState(VirtueState.LAUNCHING);
						// vc.updateVirtue(virtue);
					} catch (IOException e) {
						String msg = "Error attempting to start a " + ad.getName() + " application";
						logger.error(msg);
					}
				} else {
					setupDialog(fullBorder);
				}
			}
		});

		favoritedLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				sendChangeEvent(new PropertyChangeEvent("", "isFavorited", null, null));
			}
		});
	}

	public void setupDialog(boolean fullBorder) {
		JDialog dialog = new JDialog();
		dialog.setUndecorated(true);

		JPanel dialogContainer = new JPanel();
		dialogContainer.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		dialogContainer.setLayout(new BorderLayout());
		dialogContainer.setBackground(Color.WHITE);

		JLabel prompt = new JLabel("Would you like to start a " + ad.getName() + " application?");
		prompt.setBackground(Color.WHITE);
		prompt.setHorizontalAlignment(SwingConstants.CENTER);
		dialogContainer.add(prompt, BorderLayout.NORTH);

		JCheckBox checkBox = new JCheckBox("Don't show me again");
		checkBox.setHorizontalAlignment(SwingConstants.CENTER);
		dialogContainer.add(checkBox, BorderLayout.CENTER);
		checkBox.setBackground(Color.WHITE);


		JPanel bottomContainer = new JPanel();
		bottomContainer.setBackground(Color.WHITE);
		bottomContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		dialogContainer.add(bottomContainer, BorderLayout.SOUTH);

		JButton yesButton = new JButton("Yes");
		yesButton.setSize(new Dimension(50, 30));
		bottomContainer.add(yesButton, gbc);

		gbc.gridx = 1;
		JButton noButton = new JButton("No");
		noButton.setSize(new Dimension(50, 30));
		bottomContainer.add(noButton, gbc);

		dialog.setModal(false);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.add(dialogContainer);

		dialog.pack();
		dialog.setSize(new Dimension(375, 100));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);

		yesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				try {
					virtueService.startApplication(vc.getVirtue(), ad, new RgbColor(0, 0, 0, 0));
					// virtue.setVirtueState(VirtueState.LAUNCHING);
					// vc.updateVirtue(virtue);
				} catch (IOException e1) {
					String msg = "Error attempting to start a " + ad.getName() + " application";
					logger.error(msg);
				}
			}
		});

		noButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (checkBox.isSelected()) {
					Sidebar.askAgain = false;
				} else {
					Sidebar.askAgain = true;
				}
			}
		});

		dialog.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				if (fullBorder) {
					container.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.DARK_GRAY, Color.DARK_GRAY));
				} else {
					container.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.WHITE, Color.DARK_GRAY));
				}
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// do nothing

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				dialog.setVisible(false);
				if (fullBorder) {
					container.setBorder(new LineBorder(Color.GRAY, 1));
				} else {
					container.setBorder(new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.DARK_GRAY));
				}
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// do nothing

			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// do nothing
			}

		});

		sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				dialog.setVisible(false);
			}

		});
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public JPanel getContainer() {
		return container;
	}

	public void setTileImage(Image image) {
		this.image = image;
		Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		ImageIcon imageIcon = new ImageIcon(newimg); // transform it back
		appIcon.setIcon(imageIcon);
	}

	public void setListImage(Image image) {
		this.image = image;
		Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		ImageIcon imageIcon = new ImageIcon(newimg); // transform it back
		appIcon.setIcon(imageIcon);
	}

	public void favorite() {
		String selected = (String) cb.getSelectedItem();
		VirtueApplicationItem va = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, fv, listener, image,
				true, frame, textField, cb, sortAppsByStatus);
		va.tileSetup();
		switch (selected) {
			case "Alphabetical":
				fv.addFavorite(ad, virtue, va, textField, null);
				break;
			case "Status":
				fv.addFavorite(ad, virtue, va, textField, sortAppsByStatus);
				break;
		}
		favoritedLabel.setIcon(favoritedImage);
	}

	public void setToFavorited() {
		favoritedLabel.setIcon(favoritedImage);
	}

	public void unfavorite() {
		fv.removeFavorite(ad, virtue);
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

	public void update(DesktopVirtue virtue) {
		container.setToolTipText("<html>" + "Virtue: " + virtue.getName() + "<br>" + "OS: " + ad.getOs() + "<br>"
				+ "Status: " + virtue.getVirtueState() + "<br>" + "</html>");
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

	@Override
	public int compareTo(VirtueApplicationItem va) {
		return ad.getName().compareTo(va.getApplicationName());
	}

}