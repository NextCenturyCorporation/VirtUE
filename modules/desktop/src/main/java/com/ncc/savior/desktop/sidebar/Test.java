package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;

public class Test {

	private static ImageIcon inactiveFavoriteIcon = (new ImageIcon(
			Sidebar.class.getResource("/images/favorite-inactive.png")));
	private static ImageIcon activeFavoriteIcon = (new ImageIcon(
			Sidebar.class.getResource("/images/favorite-active.png")));

	private static ImageIcon inactiveTileIcon = (new ImageIcon(
			Sidebar.class.getResource("/images/tile-inactive2.png")));
	private static ImageIcon activeTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-active2.png")));

	private static ImageIcon inactiveListIcon = (new ImageIcon(
			Sidebar.class.getResource("/images/list-inactive2.png")));
	private static ImageIcon activeListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-active2.png")));

	private static ImageIcon saviorIcon = new ImageIcon(AppsTile.class.getResource("/images/saviorLogo.png"));

	private static ImageIcon searchIcon;
	private static ImageIcon closeIcon = new ImageIcon(Sidebar.class.getResource("/images/close.png"));

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(491, 620);

		ToolTipManager.sharedInstance().setReshowDelay(1);
		ToolTipManager.sharedInstance().setInitialDelay(1250);

		JPanel desktopContainer = new JPanel();
		JScrollPane sp = new JScrollPane();
		desktopContainer.setLayout(new BorderLayout(0, 0));

		JPanel topBorder = new JPanel();
		topBorder.setLayout(new BorderLayout());
		topBorder.setBackground(Color.DARK_GRAY);
		topBorder.setSize(20, 100);
		desktopContainer.add(topBorder, BorderLayout.NORTH);

		JLabel name = new JLabel("Administrator");
		name.setFont(new Font("Roboto", Font.PLAIN, 14));
		name.setIcon(null);
		name.setForeground(Color.WHITE);
		name.setFont(new Font("Tahoma", Font.PLAIN, 17));
		topBorder.add(name, BorderLayout.WEST);

		ImageIcon aboutIcon = new ImageIcon(Sidebar.class.getResource("/images/play.png"));
		JLabel about = new JLabel();
		about.setIcon(aboutIcon);
		topBorder.add(about, BorderLayout.EAST);

		JPanel bottomBorder = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) bottomBorder.getLayout();
		flowLayout_1.setVgap(0);
		bottomBorder.setBackground(Color.DARK_GRAY);
		desktopContainer.add(bottomBorder, BorderLayout.SOUTH);

		JLabel logoutLabel = new JLabel();

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/u73.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg); // transform it back
		logoutLabel.setIcon(imageIcon);

		bottomBorder.add(logoutLabel);

		JLabel logout = new JLabel("Logout");
		logout.setFont(new Font("Roboto", Font.PLAIN, 19));
		logout.setForeground(Color.WHITE);
		bottomBorder.add(logout);

		JPanel center = new JPanel();
		desktopContainer.add(center, BorderLayout.CENTER);
		center.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();
		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets = new Insets(0, 5, 0, 0);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.gridy = 0;
		GridBagConstraints c5 = new GridBagConstraints();
		GridBagConstraints c6 = new GridBagConstraints();
		GridBagConstraints c7 = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;

		JPanel applications = new JPanel();
		applications.setMinimumSize(new Dimension(140, 38));
		applications.setBorder(new LineBorder(SystemColor.windowBorder));
		applications.setBackground(SystemColor.scrollbar);
		c.weightx = 0.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		center.add(applications, c);
		applications.setLayout(new GridBagLayout());
		GridBagConstraints appC = new GridBagConstraints();
		appC.insets = new Insets(8, 0, 0, 0);
		GridBagConstraints appD = new GridBagConstraints();
		appD.insets = new Insets(5, 0, 0, 0);
		appD.ipady = 2;
		appD.weightx = 1.0;
		appD.anchor = GridBagConstraints.PAGE_END;
		appC.gridx = 0;
		appC.gridy = 0;

		JLabel applicationsLabel = new JLabel("Applications");
		applicationsLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		applicationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		applications.add(applicationsLabel, appC);

		appD.gridx = 0;
		appD.gridy = 1;
		appD.fill = GridBagConstraints.HORIZONTAL;
		JPanel applicationsSelected = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) applicationsSelected.getLayout();
		flowLayout_2.setVgap(1);
		applicationsSelected.setBackground(new Color(148, 0, 211));
		applications.add(applicationsSelected, appD);

		// JPanel applicationsHeader = new JPanel();
		// applicationsHeader.setBackground(SystemColor.scrollbar);
		// applications.add(applicationsHeader, BorderLayout.NORTH);

		JPanel virtues = new JPanel();
		virtues.setMinimumSize(new Dimension(140, 38));
		virtues.setBorder(new LineBorder(SystemColor.windowBorder));
		virtues.setBackground(SystemColor.scrollbar);
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 0.5;
		c1.gridx = 1;
		c1.gridy = 0;
		center.add(virtues, c1);
		virtues.setLayout(new BorderLayout(0, 4));

		JLabel virtuesLabel = new JLabel("Virtues");
		virtuesLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		virtuesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		virtues.add(virtuesLabel);

		JPanel virtuesHeader = new JPanel();
		virtuesHeader.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesHeader, BorderLayout.NORTH);

		JPanel virtuesSelected = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) virtuesSelected.getLayout();
		flowLayout_3.setVgap(1);
		virtuesSelected.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesSelected, BorderLayout.SOUTH);

		JPanel search = new JPanel();
		search.setMinimumSize(new Dimension(140, 38));
		search.setMinimumSize(new Dimension(140, 38));
		search.setBorder(new LineBorder(SystemColor.windowBorder));
		search.setBackground(SystemColor.scrollbar);
		search.setLayout(new GridBagLayout());
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 0.5;
		c2.gridx = 2;
		c2.gridy = 0;
		center.add(search, c2);

		c3.gridx = 0;
		c3.gridy = 0;
		c3.weightx = 1.0;
		c3.fill = GridBagConstraints.BOTH;

		JLabel searchLabel = new JLabel();
		searchLabel.setBackground(SystemColor.scrollbar);
		ImageIcon initialSearchIcon = new ImageIcon(AppsTile.class.getResource("/images/search.png"));
		Image searchImage = initialSearchIcon.getImage();
		Image newSearchImage = searchImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
		ImageIcon searchIcon = new ImageIcon(newSearchImage);

		searchLabel.setIcon(searchIcon);

		JTextField textField = new JTextField();
		textField.setColumns(6);
		textField.setFont(new Font("Tahoma", Font.PLAIN, 13));

		search.add(textField, c3);

		c4.weightx = 0.0;
		c4.gridx = 1;
		search.add(searchLabel, c4);

		c5.fill = GridBagConstraints.HORIZONTAL;
		c5.weightx = 0.5;
		c5.gridx = 2;
		c5.gridy = 1;

		JPanel icons = new JPanel();
		icons.setBackground(new Color(248, 248, 255));
		center.add(icons, c5);
		icons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		c6.fill = GridBagConstraints.BOTH;
		c6.gridx = 0;
		c6.gridwidth = 2;
		JPanel sortBy = new JPanel();
		sortBy.setBackground(new Color(248, 248, 255));
		sortBy.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		center.add(sortBy, c6);

		JLabel sortByLabel = new JLabel("sorted by: ");
		sortByLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		sortByLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		String[] sortingOptions = { "Alphabetical", "Status" };
		JComboBox<String> cb = new JComboBox<String>();
		cb.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		cb.setBackground(new Color(248, 248, 255));
		Color bgColor = cb.getBackground();
		cb.setRenderer(new DefaultListCellRenderer() {
			@Override
			public void paint(Graphics g) {
				setBackground(bgColor);
				super.paint(g);
			}
		});

		cb.setVisible(true);
		sortBy.add(sortByLabel);
		sortBy.add(cb);

		JLabel listLabel = new JLabel(inactiveListIcon);
		listLabel.setBackground(new Color(248, 248, 255));

		JLabel tileLabel = new JLabel(activeTileIcon);
		tileLabel.setBackground(new Color(248, 248, 255));

		JLabel favoritesLabel = new JLabel(inactiveFavoriteIcon);
		favoritesLabel.setBackground(new Color(248, 248, 255));

		JPanel favoritesView = new JPanel();
		favoritesView.setBackground(new Color(248, 248, 255));
		favoritesView.add(favoritesLabel);
		icons.add(favoritesView);
		favoritesView.setToolTipText("Favorites view");

		JPanel listView = new JPanel();
		listView.setBackground(new Color(248, 248, 255));
		listView.add(listLabel);
		icons.add(listView);
		listView.setToolTipText("List view");

		JPanel tileView = new JPanel();
		tileView.setBackground(new Color(248, 248, 255));
		tileView.add(tileLabel);
		icons.add(tileView);
		tileView.setToolTipText("Tile view");

		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setSize(300, 800);
		sp.setPreferredSize(new Dimension(0, 800));
		sp.getVerticalScrollBar().setUnitIncrement(16);
		c7.fill = GridBagConstraints.BOTH;
		c7.ipady = 0;
		c7.weighty = 1.0; // request any extra vertical space
		c7.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c7.gridx = 0;
		c7.gridwidth = 3; // 3 columns wide
		c7.gridy = 2; // third row
		center.add(sp, c7);

		sp.getViewport().revalidate();
		sp.validate();
		sp.repaint();

		frame.getContentPane().add(desktopContainer);
		frame.setVisible(true);
	}

}
