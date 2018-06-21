package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;

/**
 * This is an interface that will help encapsulate the IconResourceService class
 */

public interface IIconService {

	public Image getImage(String iconKey) throws IOException;

}
