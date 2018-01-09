package com.ncc.savior.desktop.authorization;

import java.io.InputStream;

import javafx.scene.image.Image;

public class DummyAuthentication implements IActiveDirectoryAuthorizationProvider {

	private DesktopUser currentUser;

	@Override
	public DesktopUser getCurrentUser() {
		return currentUser;
	}

	@Override
	public DesktopUser login(String domain, String username, String password) {
		currentUser = new DesktopUser(domain, username);
		setCurrentImage(currentUser);
		return currentUser;
	}

	private void setCurrentImage(DesktopUser user) {
		String uri = "/images/user/" + user.getUsername() + ".jpg";
		InputStream stream = DesktopUser.class.getResourceAsStream(uri);
		if (stream != null) {
			Image img = new Image(stream);
			user.setImage(img);
		}
	}

	@Override
	public byte[] getCurrentToken() {
		return null;
	}

	@Override
	public void logout() {
		// do nothing
	}

}
