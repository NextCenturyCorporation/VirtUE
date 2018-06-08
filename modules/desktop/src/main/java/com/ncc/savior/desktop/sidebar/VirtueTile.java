package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

public class VirtueTile {
	private JPanel container;

	public VirtueTile() throws IOException {
		this.container = new JPanel();
		container.setLayout(new GridLayout(0, 1, 0, 15));

		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 140, 0));
		container.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(210, 105, 30));
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(panel_2, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Artist");
		lblNewLabel.setForeground(new Color(255, 255, 255));
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		panel_2.add(lblNewLabel);

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(new Color(255, 140, 0));
		panel.add(panel_3, BorderLayout.CENTER);
		GridLayout gl_view = new GridLayout(0, 3);
		gl_view.setVgap(30);
		gl_view.setHgap(30);
		panel_3.setLayout(gl_view);
		panel_3.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

		for (Integer i = 0; i < 11; i++) {
			addTile(i.toString(), panel_3);
		}

		JPanel panel5 = new JPanel();
		panel5.setBackground(new Color(255, 140, 0));
		container.add(panel5);
		panel5.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(0, 0, 128));
		FlowLayout flowLayout2 = (FlowLayout) panel_6.getLayout();
		flowLayout2.setAlignment(FlowLayout.LEFT);
		panel5.add(panel_6, BorderLayout.NORTH);

		JLabel lblNewLabel2 = new JLabel("Artist");
		lblNewLabel2.setForeground(new Color(255, 255, 255));
		lblNewLabel2.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel2.setVerticalAlignment(SwingConstants.TOP);
		panel_6.add(lblNewLabel2);

		JPanel panel_7 = new JPanel();
		panel_7.setBackground(new Color(65, 105, 225));
		panel5.add(panel_7, BorderLayout.CENTER);
		GridLayout gl_view2 = new GridLayout(0, 3);
		gl_view2.setVgap(30);
		gl_view2.setHgap(30);
		panel_7.setLayout(gl_view2);
		panel_7.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

		for (Integer i = 0; i < 11; i++) {
			addTile(i.toString(), panel_7);
		}
	}

	public void addTile(String name, JPanel panel) throws IOException {
		JPanel tile = new JPanel();
		tile.setBackground(Color.WHITE);
		JLabel lblNewLabel_1 = new JLabel(name);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		tile.setLayout(new BorderLayout(0, 0));
		BufferedImage myPicture = ImageIO.read(new File("Test.png"));
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		tile.add(picLabel);
		tile.add(lblNewLabel_1, BorderLayout.SOUTH);
		tile.setSize(0, 40);

		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem mi1 = new JMenuItem("Yes");
				JMenuItem mi2 = new JMenuItem("No");
				JLabel prompt = new JLabel("Would you like to start a virtue?");
				pm.add(new JLabel("Would you like to start a virtue?"));
				prompt.setHorizontalAlignment(SwingConstants.CENTER);

				pm.setPopupSize(225, 75);
				pm.add(mi1);
				pm.add(mi2);
				pm.show(container, 155, 200);
			}
		});

		panel.add(tile);
	}

	public static void main(String[] args) throws IOException {
		VirtueTile vt = new VirtueTile();
		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vt.getContainer());
		frame.setSize(600, 875);
		frame.setVisible(true);
	}

	public JPanel getContainer() {
		return container;
	}

}
