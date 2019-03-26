/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.virtueadmin.model.OS;

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
		loadDefaultImages();
	}

	private void loadDefaultImages() {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource linuxIcon = resolver.getResource("/images/linux.png");
		Resource windowsIcon = resolver.getResource("/images/windows.png");
		try {
			BufferedImage image = ImageIO.read(linuxIcon.getInputStream());
			imageCache.put(OS.LINUX.toString(), image);
		} catch (IOException e) {
			logger.warn("Failed to load default Linux icon", e);
		}
		try {
			BufferedImage image = ImageIO.read(windowsIcon.getInputStream());
			imageCache.put(OS.WINDOWS.toString(), image);
		} catch (IOException e) {
			logger.warn("Failed to load default Windows icon", e);
		}
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
					} else {
						Image foundImg = desktopService.getIcon(iconKey);
						consumer.accept(foundImg);
						imageCache.put(iconKey, foundImg);
					}
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
