package com.ncc.savior.desktop.authorization;

import javafx.scene.image.Image;

/**
 * Model object for the user on a desktop used for the desktop app.
 *
 *
 */
public class DesktopUser {
	private static final String DOMAIN_SEPARATOR = "\\";
	private String username;
	private String domain;
	private Image image;

	public DesktopUser(String domain, String username) {
		this.domain = domain;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getDomain() {
		return domain;
	}

	public String getFullQualifiedDomainName() {
		return domain + DOMAIN_SEPARATOR + username;
	}

	public static DesktopUser fromFullyQualifiedDomainName(String fqd) {
		int index = fqd.lastIndexOf(DOMAIN_SEPARATOR);
		if (index < 0) {
			return new DesktopUser(null, fqd);
		}else {
			String domain = fqd.substring(0, index);
			String user = fqd.substring(index+1);
			return new DesktopUser(domain, user);
		}
	}

	public void setImage(Image img) {
		image = img;
	}

	public Image getImage() {
		return image;
	}
}
