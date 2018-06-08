package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class SidebarSkeleton {

	private JPanel container;
	private static boolean applicationsOpen = true;

	public SidebarSkeleton() throws IOException {
		this.container = new JPanel();
		container.setLayout(new BorderLayout(0, 0));

		JPanel topBorder = new JPanel();
		FlowLayout flowLayout = (FlowLayout) topBorder.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(30);
		topBorder.setBackground(Color.DARK_GRAY);
		topBorder.setSize(20, 100);
		container.add(topBorder, BorderLayout.NORTH);

		JLabel name = new JLabel("Kevin Aranyi");
		name.setIcon(new ImageIcon(SidebarSkeleton.class.getResource("/images/play.png")));
		name.setForeground(Color.WHITE);
		name.setFont(new Font("Comic Sans MS", Font.PLAIN, 17));
		topBorder.add(name);

		JPanel bottomBorder = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) bottomBorder.getLayout();
		flowLayout_1.setVgap(13);
		bottomBorder.setBackground(Color.DARK_GRAY);
		container.add(bottomBorder, BorderLayout.SOUTH);

		JLabel lblNewLabel = new JLabel("");

		ImageIcon imageIcon = new ImageIcon(SidebarSkeleton.class.getResource("/images/u73.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg);  // transform it back
		lblNewLabel.setIcon(imageIcon);

		bottomBorder.add(lblNewLabel);

		JLabel logout = new JLabel("Logout");
		logout.setFont(new Font("Tahoma", Font.PLAIN, 19));
		logout.setForeground(Color.WHITE);
		bottomBorder.add(logout);

		JPanel center = new JPanel();
		container.add(center, BorderLayout.CENTER);
		center.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();
		GridBagConstraints c3 = new GridBagConstraints();
		GridBagConstraints c4 = new GridBagConstraints();
		GridBagConstraints c5 = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel applications = new JPanel();
		applications.setBorder(new LineBorder(SystemColor.windowBorder));
		applications.setBackground(SystemColor.scrollbar);
		c.weightx = 0.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		center.add(applications, c);
		applications.setLayout(new BorderLayout(0, 13));

		JLabel lblA = new JLabel("Applications");
		lblA.setVerticalAlignment(SwingConstants.TOP);
		lblA.setHorizontalAlignment(SwingConstants.CENTER);
		applications.add(lblA);

		JPanel applicationsSelected = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) applicationsSelected.getLayout();
		flowLayout_2.setVgap(2);
		applicationsSelected.setBackground(new Color(148, 0, 211));
		applications.add(applicationsSelected, BorderLayout.SOUTH);

		JPanel applicationsHeader = new JPanel();
		applicationsHeader.setBackground(SystemColor.scrollbar);
		applications.add(applicationsHeader, BorderLayout.NORTH);

		JPanel virtues = new JPanel();
		virtues.setBorder(new LineBorder(SystemColor.windowBorder));
		virtues.setBackground(SystemColor.scrollbar);
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.weightx = 0.5;
		c2.gridx = 1;
		c2.gridy = 0;
		center.add(virtues, c2);
		virtues.setLayout(new BorderLayout(0, 13));

		JLabel lblVirtues = new JLabel("Virtues");
		lblVirtues.setHorizontalAlignment(SwingConstants.CENTER);
		virtues.add(lblVirtues);

		JPanel virtuesHeader = new JPanel();
		virtuesHeader.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesHeader, BorderLayout.NORTH);

		JPanel virtuesSelected = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) virtuesSelected.getLayout();
		flowLayout_3.setVgap(2);
		virtuesSelected.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesSelected, BorderLayout.SOUTH);

		JPanel search = new JPanel();
		search.setBorder(new LineBorder(SystemColor.windowBorder));
		search.setBackground(SystemColor.scrollbar);
		FlowLayout flowLayout_4 = (FlowLayout) search.getLayout();
		flowLayout_4.setVgap(20);
		c3.fill = GridBagConstraints.HORIZONTAL;
		c3.weightx = 0.5;
		c3.gridx = 2;
		c3.gridy = 0;
		center.add(search, c3);

		JLabel lblSearch = new JLabel("Search");
		search.add(lblSearch);

		JPanel icons = new JPanel();
		icons.setBorder(new LineBorder(SystemColor.windowBorder));
		icons.setBackground(SystemColor.menu);
		c4.fill = GridBagConstraints.HORIZONTAL;
		// c4.ipady = 40; // make this component tall
		c4.weightx = 0.0;
		c4.gridwidth = 3;
		c4.gridx = 0;
		c4.gridy = 1;
		center.add(icons, c4);
		icons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		BufferedImage myPicture = ImageIO.read(new File("Play.png"));
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));

		BufferedImage myPicture2 = ImageIO.read(new File("Play.png"));
		JLabel picLabel2 = new JLabel(new ImageIcon(myPicture2));

		JPanel listView = new JPanel();
		listView.add(picLabel);
		icons.add(listView);

		JPanel tileView = new JPanel();
		tileView.add(picLabel2);
		icons.add(tileView);

		JScrollPane sp = new JScrollPane();
		sp.setSize(300, 800);
		sp.setPreferredSize(new Dimension(200, 800));
		sp.getVerticalScrollBar().setUnitIncrement(16);
		c5.fill = GridBagConstraints.BOTH;
		c5.ipady = 0; // reset to default
		c5.weighty = 1.0; // request any extra vertical space
		c5.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c5.gridx = 0; // aligned with button 2
		c5.gridwidth = 3; // 3 columns wide
		c5.gridy = 2; // third row
		center.add(sp, c5);

		AppsTile at = new AppsTile();
		AppsList al = new AppsList();
		VirtueTile vt = new VirtueTile();
		VirtueList vl = new VirtueList();
		sp.setViewportView(at.getView());

		tileView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (applicationsOpen) {
					sp.setViewportView(at.getView());
				} else {
					sp.setViewportView(vt.getContainer());
				}
			}
		});

		listView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (applicationsOpen) {
					sp.setViewportView(al.getView());
				} else {
					sp.setViewportView(vl.getContainer());
				}
			}
		});

		applications.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!applicationsOpen) {
					applicationsOpen = true;
					virtuesSelected.setBackground(SystemColor.scrollbar);
					applicationsSelected.setBackground(new Color(148, 0, 211));
					sp.setViewportView(at.getView());
				}
			}
		});

		virtues.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (applicationsOpen) {
					applicationsOpen = false;
					applicationsSelected.setBackground(SystemColor.scrollbar);
					virtuesSelected.setBackground(new Color(148, 0, 211));
					sp.setViewportView(vt.getContainer());
				}
			}
		});
	}

	public JPanel getContainer() {
		return container;
	}

	public static void applyQualityRenderingHints(Graphics2D g2d) {

		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

	}

	public static void main(String[] args) throws IOException {
		SidebarSkeleton ss = new SidebarSkeleton();

		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(ss.getContainer());
		frame.setSize(420, 875);
		frame.setVisible(true);
		// frame.setResizable(false);
	}
}
