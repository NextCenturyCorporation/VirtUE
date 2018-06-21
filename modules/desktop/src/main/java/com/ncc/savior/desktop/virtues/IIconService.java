package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;

import com.ncc.savior.desktop.authorization.InvalidUserLoginException;

public interface IIconService {

	/**
	 * This is an interface that will help encapsulate the IconResourceService class
	 */

	public Image getImage(String iconKey) throws InvalidUserLoginException, IOException;

}
