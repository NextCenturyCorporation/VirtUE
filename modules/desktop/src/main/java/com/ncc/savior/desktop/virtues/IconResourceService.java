package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.InvalidUserLoginException;

/**
 * This class is a service that is used to retrieve icons for different
 * applications
 */

public class IconResourceService implements IIconService {

	private IconExecutor executor;

	private DesktopResourceService desktopService;

	private static final Logger logger = LoggerFactory.getLogger(IconResourceService.class);

	private HashMap<String, Image> imageCache;

	public IconResourceService(DesktopResourceService desktopService) {
		this.desktopService = desktopService;
		this.executor = new IconExecutor();
		this.imageCache = new HashMap<String, Image>();
	}

	@Override
	public void getImage(String iconKey, Consumer<Image> consumer)
			throws InvalidUserLoginException, IOException, InterruptedException, ExecutionException {
		Image img = imageCache.get(iconKey);
		if (img != null) {
			consumer.accept(img);
		} else {
			Runnable runnable = () -> {
				try {
					Image cached = imageCache.get(iconKey);
					if (cached != null) {
						consumer.accept(cached);
					}
					Image foundImg = desktopService.getIcon(iconKey);
					consumer.accept(foundImg);
					imageCache.put(iconKey, foundImg);
				} catch (IOException e) {
					logger.debug("Error with image retrieval");
				}
			};
			executor.submitThread(runnable);
		}
	}

	@Override
	public Image getImageNow(String iconKey) {
		Image img = imageCache.get(iconKey);
		return img;
	}

}
