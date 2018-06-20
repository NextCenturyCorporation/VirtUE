package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;

import com.ncc.savior.desktop.authorization.InvalidUserLoginException;

public class IconResourceService {

	private DesktopResourceService desktopService;

	private HashMap<String, Image> imageCache;

	public IconResourceService(DesktopResourceService desktopService) {
		this.desktopService = desktopService;
		this.imageCache = new HashMap<String, Image>();
	}

	public Image getImage(String iconKey) throws InvalidUserLoginException, IOException {
		Image img = imageCache.get(iconKey);
		if (img != null) {
			return img;
		} else {
			return desktopService.getIcon(iconKey);
		}
	}

	public void addToCache(String iconKey, Image img) {
		imageCache.put(iconKey, img);
	}

}
