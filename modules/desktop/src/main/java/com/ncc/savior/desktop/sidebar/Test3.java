package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Test3 {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JPanel saviorContainer = new JPanel();
		saviorContainer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(saviorContainer);
		saviorContainer.setLayout(new BorderLayout(0, 0));

		JLabel saviorLogo = new JLabel("");
		saviorLogo.setHorizontalAlignment(SwingConstants.CENTER);
		saviorContainer.add(saviorLogo);
		ImageIcon saviorIcon = new ImageIcon(LoginPage.class.getResource("/images/saviorLogo.png"));
		Image image = saviorIcon.getImage();
		Image newimg = image.getScaledInstance(80, 75, java.awt.Image.SCALE_SMOOTH);
		saviorIcon = new ImageIcon(newimg);
		saviorLogo.setIcon(saviorIcon);

		JPanel greetingsContainer = new JPanel();
		greetingsContainer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(greetingsContainer);
		greetingsContainer.setLayout(new BorderLayout(0, 0));

		JLabel greetings = new JLabel("Good Morning");
		greetings.setFont(new Font("Roboto", Font.PLAIN, 18));
		greetings.setForeground(Color.WHITE);
		greetings.setHorizontalAlignment(SwingConstants.CENTER);
		greetingsContainer.add(greetings, BorderLayout.CENTER);

		JPanel promptContainer = new JPanel();
		promptContainer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(promptContainer);
		promptContainer.setLayout(new BorderLayout(0, 0));

		JLabel prompt = new JLabel(
				"<html><center> Please login to begin your <br> Savior Desktop session </center></html>");
		prompt.setFont(new Font("Roboto", Font.PLAIN, 18));
		prompt.setForeground(Color.WHITE);
		prompt.setHorizontalAlignment(SwingConstants.CENTER);
		promptContainer.add(prompt, BorderLayout.CENTER);

		JPanel usernameContainer = new JPanel();
		usernameContainer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(usernameContainer);
		GridBagLayout gbl_usernameContainer = new GridBagLayout();
		gbl_usernameContainer.columnWeights = new double[] { 1.0 };
		usernameContainer.setLayout(gbl_usernameContainer);

		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 18));
		usernameLabel.setForeground(Color.WHITE);
		usernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 85));
		usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_usernameLabel = new GridBagConstraints();
		gbc_usernameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_usernameLabel.fill = GridBagConstraints.BOTH;
		gbc_usernameLabel.gridx = 0;
		gbc_usernameLabel.gridy = 0;
		usernameContainer.add(usernameLabel, gbc_usernameLabel);

		JTextField usernameField = new JTextField();
		usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.gridx = 0;
		gbc_usernameField.gridy = 1;
		usernameContainer.add(usernameField, gbc_usernameField);
		usernameField.setColumns(12);
		usernameField.setPreferredSize(new Dimension(160, 30));

		JPanel passwordContainer = new JPanel();
		passwordContainer.setBackground(Color.DARK_GRAY);
		GridBagLayout gbl_passwordContainer = new GridBagLayout();
		gbl_passwordContainer.columnWeights = new double[] { 1.0 };
		passwordContainer.setLayout(gbl_passwordContainer);
		frame.getContentPane().add(passwordContainer);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 18));
		passwordLabel.setForeground(Color.WHITE);
		passwordLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 85));
		passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
		gbc_passwordLabel.insets = new Insets(0, 0, 5, 0);
		gbc_passwordLabel.gridx = 0;
		gbc_passwordLabel.gridy = 0;
		passwordContainer.add(passwordLabel, gbc_passwordLabel);

		JPasswordField passwordField = new JPasswordField();
		passwordField.setHorizontalAlignment(SwingConstants.CENTER);
		passwordField.setColumns(12);
		passwordField.setPreferredSize(new Dimension(160, 30));
		passwordField.setFont(new Font("Roboto", Font.PLAIN, 18));
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.gridx = 0;
		gbc_passwordField.gridy = 1;
		passwordContainer.add(passwordField, gbc_passwordField);

		JPanel loginContainer = new JPanel();
		loginContainer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(loginContainer);
		GridBagLayout gbl_loginContainer = new GridBagLayout();
		gbl_loginContainer.rowWeights = new double[] { 1.0 };
		gbl_loginContainer.columnWeights = new double[] { 1.0 };
		loginContainer.setLayout(gbl_loginContainer);

		JPanel loginBox = new JPanel();
		loginBox.setPreferredSize(new Dimension(175, 10));
		loginBox.setBackground(new Color(51, 204, 51));
		GridBagConstraints gbc_loginBox = new GridBagConstraints();
		gbc_loginBox.ipady = 40;
		gbc_loginBox.gridx = 0;
		gbc_loginBox.gridy = 0;
		loginContainer.add(loginBox, gbc_loginBox);
		loginBox.setLayout(new BorderLayout(0, 0));
		loginBox.setBorder(new LineBorder(new Color(255, 255, 255)));

		JLabel loginLabel = new JLabel("Login");
		loginLabel.setFont(new Font("Roboto", Font.PLAIN, 18));
		loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginBox.add(loginLabel);
		loginBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JPanel footer = new JPanel();
		footer.setBackground(Color.DARK_GRAY);
		frame.getContentPane().add(footer);
		footer.setLayout(new BorderLayout(0, 0));
		frame.setSize(600, 600);
		frame.setVisible(true);
	}
}
