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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	private ImageIcon favoritedImage = new ImageIcon(VirtueApplicationItem.class.getResource("/images/favorited.png"));
	private ImageIcon unfavoritedImage = new ImageIcon(
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

	private JFrame frame;

	public VirtueApplicationItem(ApplicationDefinition ad, VirtueService virtueService, JScrollPane sp,
			VirtueTileContainer vc, DesktopVirtue virtue, FavoritesView fv, PropertyChangeListener listener,
			Image image, boolean isFavorited, JFrame frame) {
		this.sp = sp;
		this.vc = vc;
		this.virtueService = virtueService;
		this.ad = ad;
		this.virtue = virtue;
		this.fv = fv;
		this.image = image;
		this.frame = frame;

		this.appIcon = new JLabel();
		this.container = new JPanel();
		this.favoritedLabel = new JLabel();
		this.appName = new JLabel(ad.getName());
		this.appIcon.setHorizontalAlignment(SwingConstants.CENTER);

		this.isFavorited = isFavorited;

		favoritedLabel.setToolTipText("Click to favorite or unfavorite");
		container.setToolTipText("<html>" + "Virtue: " + virtue.getName() + "<br>" + "OS: " + ad.getName() + "<br>"
				+ "Status: " + virtue.getVirtueState() + "<br>" + "</html>");

		this.changeListener = new ChangeListener();
		this.listener = listener;
		container.setBorder(new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.DARK_GRAY));
	}

	public void tileSetup() {
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

		Image newimg = image.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		ImageIcon imageIcon = new ImageIcon(newimg); // transform it back

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

		addListener(vc, fv, ad, virtue);
	}

	public void listSetup() {
		container.setBorder(new LineBorder(Color.GRAY, 1));
		container.setBackground(Color.WHITE);
		this.appIcon = new JLabel(ad.getName());

		Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		ImageIcon imageIcon = new ImageIcon(newimg); // transform it back

		appIcon.setIcon(imageIcon);
		appIcon.setHorizontalAlignment(SwingConstants.LEFT);

		this.favoritedLabel = new JLabel();

		if (isFavorited) {
			favoritedLabel.setIcon(favoritedImage);
		} else {
			favoritedLabel.setIcon(unfavoritedImage);
		}

		favoritedLabel.setHorizontalAlignment(SwingConstants.LEFT);

		container.add(favoritedLabel, BorderLayout.NORTH);
		container.add(appIcon);

		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		container.setSize(new Dimension(450, 70));
		container.setMinimumSize(new Dimension(450, 70));
		container.setMaximumSize(new Dimension(10000, 70));
		container.setPreferredSize(new Dimension(450, 70));

		addListener(vc, fv, ad, virtue);
	}

	public void addListener(VirtueTileContainer vc, FavoritesView fv, ApplicationDefinition ad, DesktopVirtue virtue) {

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
					setupDialog();
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

	public void setupDialog() {
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
		dialog.setLocationRelativeTo(container);
		dialog.setSize(new Dimension(375, 100));
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
				container.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.WHITE, Color.DARK_GRAY));
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
				container.setBorder(new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.DARK_GRAY));
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

	public void favorite() {
		fv.addFavorite(ad, virtue, vc, sp, listener, image, frame);
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