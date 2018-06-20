package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;

/**
 *
 * This is the initial login page for the application
 *
 */

public class LoginPage {
	private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

	private JPanel container;
	private AuthorizationService authService;
	private Set<ILoginEventListener> loginListeners;

	public LoginPage(AuthorizationService authService) throws IOException {
		this.container = new JPanel();
		this.loginListeners = new HashSet<ILoginEventListener>();
		this.authService = authService;

		setup();
	}

	public void setup() {
		container.setBackground(Color.DARK_GRAY);
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = .5;
		c.insets = new Insets(30, 0, 0, 0);

		ImageIcon saviorIcon = new ImageIcon(LoginPage.class.getResource("/images/saviorLogo.png"));
		Image image = saviorIcon.getImage();
		Image newimg = image.getScaledInstance(80, 75, java.awt.Image.SCALE_SMOOTH);
		saviorIcon = new ImageIcon(newimg);

		JLabel saviorImage = new JLabel();
		saviorImage.setHorizontalAlignment(SwingConstants.CENTER);
		saviorImage.setIcon(saviorIcon);
		container.add(saviorImage, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.0;
		JLabel header1 = new JLabel("Good morning.");
		header1.setFont(new Font("Tahoma", Font.PLAIN, 22));
		header1.setHorizontalAlignment(SwingConstants.CENTER);
		header1.setForeground(Color.WHITE);
		container.add(header1, c);

		c.gridy = 2;
		JLabel header2 = new JLabel("Please login to begin your");
		header2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		header2.setForeground(Color.WHITE);
		header2.setHorizontalAlignment(SwingConstants.CENTER);
		container.add(header2, c);

		c.gridy = 3;
		JLabel header3 = new JLabel("Savior Desktop session");
		header3.setFont(new Font("Tahoma", Font.PLAIN, 18));
		header3.setForeground(Color.WHITE);
		header3.setVerticalAlignment(SwingConstants.TOP);
		header3.setHorizontalAlignment(SwingConstants.CENTER);
		container.add(header3, c);

		c.gridy = 4;
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		usernameLabel.setForeground(Color.WHITE);
		usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		usernameLabel.setBounds(159, 388, 86, 30);
		container.add(usernameLabel, c);

		c.gridy = 5;
		c.ipadx = 85;
		c.fill = GridBagConstraints.NONE;
		JTextField textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setFont(new Font("Arial", Font.PLAIN, 18));
		textField.setColumns(10);
		textField.setMinimumSize(new Dimension(143, 26));
		container.add(textField, c);

		c.gridy = 6;
		c.ipadx = 0;
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		passwordLabel.setForeground(Color.WHITE);
		passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		container.add(passwordLabel, c);

		c.gridy = 7;
		c.fill = GridBagConstraints.NONE;
		JPasswordField password = new JPasswordField();
		password.setFont(new Font("Tahoma", Font.PLAIN, 18));
		password.setHorizontalAlignment(SwingConstants.CENTER);
		password.setColumns(15);
		password.setMinimumSize(new Dimension(230, 26));
		container.add(password, c);

		c.gridy = 8;
		c.ipadx = 150;
		c.ipady = 40;
		JPanel loginContainer = new JPanel();
		loginContainer.setBorder(new LineBorder(new Color(255, 255, 255)));
		loginContainer.setBackground(new Color(51, 204, 51));
		loginContainer.setLayout(new BorderLayout(0, 0));
		container.add(loginContainer, c);

		JLabel loginLabel = new JLabel("Login");
		loginLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		loginContainer.add(loginLabel);
		loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginLabel.setVerticalAlignment(SwingConstants.CENTER);

		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel footer = new JPanel();
		footer.setBackground(Color.DARK_GRAY);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		c.weighty = 1.0;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy = 9;
		container.add(footer, c);

		String initialDomain = "";
		if (authService.getRequiredDomain() != null) {
			initialDomain = authService.getRequiredDomain();
		}
		final String domain = initialDomain;

		loginContainer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				String username = textField.getText();
				String password1 = password.getName();
				try {
					doLogin(domain, username, password1);
				} catch (IOException e) {
					logger.error("Login error");
				}
			}
		});
	}

	private void doLogin(String domain, String username, String password) throws IOException {
		DesktopUser user = authService.login(domain, username, password);
		triggerLoginSuccessListener(user);
	}

	public JPanel getContainer() {
		return container;
	}

	public void addLoginEventListener(ILoginEventListener listener) {
		loginListeners.add(listener);
	}

	public void removeLoginEventListener(ILoginEventListener listener) {
		loginListeners.remove(listener);
	}

	protected void triggerLoginSuccessListener(DesktopUser user) throws IOException {
		for (ILoginEventListener listener : loginListeners) {
			listener.onLoginSuccess(user);
		}
	}

	protected void triggerLoginCancelListener() {
		for (ILoginEventListener listener : loginListeners) {
			listener.onCancel();
		}
	}

	protected void triggerLoginFailureListener(String username, String domain, RuntimeException e) {
		for (ILoginEventListener listener : loginListeners) {
			listener.onLoginFailure(username, domain, e);
		}
	}

	public static interface ILoginEventListener {
		public void onLoginSuccess(DesktopUser user) throws IOException;

		public void onLoginFailure(String username, String domain, RuntimeException e);

		public void onCancel();
	}
}
