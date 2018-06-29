package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class test {
	public static void main(final String args[]) {
		JFrame frame = new JFrame("Alignment Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel container = new JPanel();
		container.setBackground(Color.WHITE);
		frame.getContentPane().add(container);
		container.setLayout(new BorderLayout(0, 0));

		JLabel title = new JLabel("Savior VirtUE Desktop");
		title.setFont(new Font("Arial", Font.BOLD, 18));
		title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/saviorLogo.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg); // transform it back
		title.setIcon(imageIcon);

		title.setHorizontalAlignment(SwingConstants.CENTER);

		container.add(title, BorderLayout.NORTH);

		JPanel footer = new JPanel();
		footer.setBackground(Color.WHITE);
		container.add(footer, BorderLayout.SOUTH);

		JLabel copyright = new JLabel("©2018-2019 Next Century Corporation. All rights reserved");
		footer.add(copyright);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		container.add(scrollPane, BorderLayout.CENTER);

		JPanel textContainer = new JPanel();
		textContainer.setBackground(Color.WHITE);
		textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));

		JLabel disclaimerHeader = new JLabel("<html><center> Disclaimer Third Parties <br><br></center></html>",
				SwingConstants.CENTER);
		JLabel disclaimers = new JLabel(
				"<html><center> All product and company names are trademarks™ or <br> registered® trademarks of their respective holders. Use of <br> them does not imply any affiliation with or endorsement by them.<br><br>"
						+ "All specifications are subject to change without notice.<br><br>"
						+ "Chrome and Chromium are trademarks owned by Google LLC.<br><br>"
						+ "Firefox and the Firefox logos are trademarks of the <br> Mozilla Foundation.<br><br>"
						+ "LibreOffice and LibreOffice logos are trademarks of The <br> Document Foundation.<br><br>"
						+ "Microsoft, Microsoft Office, Microsoft Excel, Microsoft PowerPoint <br> and Microsoft Word are registered trademarks of Microsoft <br> Corporation in the United States and/or other countries.<br><br></center></html>",
				SwingConstants.CENTER);
		JLabel credits = new JLabel("<html><center> Software Team Credits <br><br></center></html>",
				SwingConstants.CENTER);
		JLabel nextCentury = new JLabel("<html><center> Next Century Corporation: <br><br></center></html>",
				SwingConstants.CENTER);

		JLabel twoSix = new JLabel("<html><center> Two Six Labs: <br><br></center></html>", SwingConstants.CENTER);

		JLabel vt = new JLabel("<html><center> Virginia Tech: <br><br></center></html>", SwingConstants.CENTER);

		disclaimerHeader.setFont(new Font("Tahoma", Font.BOLD, 15));
		credits.setFont(new Font("Tahoma", Font.BOLD, 15));
		nextCentury.setFont(new Font("Tahoma", Font.PLAIN, 13));
		twoSix.setFont(new Font("Tahoma", Font.PLAIN, 13));
		vt.setFont(new Font("Tahoma", Font.PLAIN, 13));

		textContainer.add(disclaimerHeader);
		textContainer.add(disclaimers);
		textContainer.add(credits);
		textContainer.add(nextCentury);
		textContainer.add(twoSix);
		textContainer.add(vt);

		scrollPane.setViewportView(textContainer);
		frame.setSize(500, 500);
		frame.setVisible(true);

	}
}
