package com.ncc.savior.desktop.virtues;

import java.awt.Image;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * This is an interface that will help encapsulate the IconResourceService class
 */

public interface IIconService {

	public void getImage(String iconKey, Consumer<Image> consumer)
			throws IOException, InterruptedException, ExecutionException;

}
