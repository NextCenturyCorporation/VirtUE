package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;

import com.ncc.savior.desktop.authorization.InvalidUserLoginException;

public class IconResourceService implements IIconService {

	/**
	 * This class is a service that is used to retrieve icons for different
	 * applications
	 */

	private DesktopResourceService desktopService;

	private HashMap<String, Image> imageCache;

	public IconResourceService(DesktopResourceService desktopService) {
		this.desktopService = desktopService;
		this.imageCache = new HashMap<String, Image>();
	}

	@Override
	public Image getImage(String iconKey) throws InvalidUserLoginException, IOException {
		Image img = imageCache.get(iconKey);
		if (img != null) {
			return img;
		} else {
			Image foundImg = desktopService.getIcon(iconKey);
			imageCache.put(iconKey, img);
			return foundImg;
		}
	}

}
