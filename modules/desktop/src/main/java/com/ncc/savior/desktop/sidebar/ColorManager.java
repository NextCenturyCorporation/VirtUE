package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Manager to handles which colors are to be associated with which virtues in
 * the current instance of the desktop.
 *
 *
 */
public class ColorManager {
	private Iterator<Pair<Color, Color>> colorItr;
	private ArrayList<Pair<Color, Color>> colorList;
	private Map<String, Pair<Color, Color>> idToColor;

	public ColorManager() {
		idToColor = new HashMap<String, Pair<Color, Color>>();
		colorList = loadColors();
		colorItr = colorList.iterator();
	}

	private Pair<Color, Color> getNextColor() {
		if (!colorItr.hasNext()) {
			colorItr = colorList.iterator();
		}
		return colorItr.next();
	}

	private ArrayList<Pair<Color, Color>> loadColors() {
		ArrayList<Pair<Color, Color>> colors = new ArrayList<Pair<Color, Color>>();
		colors.add(Pair.of(new Color(189, 0, 38), new Color(227, 26, 28)));
		colors.add(Pair.of(new Color(34, 94, 168), new Color(29, 145, 192)));
		colors.add(Pair.of(new Color(35, 132, 67), new Color(65, 171, 93)));
		colors.add(Pair.of(new Color(204, 76, 2), new Color(236, 112, 20)));
		colors.add(Pair.of(new Color(136, 65, 157), new Color(140, 107, 177)));
		colors.add(Pair.of(new Color(206, 18, 86), new Color(231, 41, 138)));
		colors.add(Pair.of(new Color(106, 81, 163), new Color(128, 125, 186)));
		colors.add(Pair.of(new Color(203, 24, 29), new Color(239, 59, 44)));
		colors.add(Pair.of(new Color(191, 129, 45), new Color(223, 194, 125)));
		colors.add(Pair.of(new Color(53, 151, 143), new Color(128, 205, 193)));
		colors.add(Pair.of(new Color(127, 188, 65), new Color(184, 225, 134)));
		return colors;
	}

	/**
	 * Gets color associated with id that is typically bolder than the body color.
	 * If no color is currently associated with id, the next color will get
	 * assigned.
	 *
	 * @param id
	 * @return
	 */
	public Color getHeaderColor(String id) {
		Pair<Color, Color> colorPair = getColorPairForId(id);
		return colorPair.getLeft();
	}

	private synchronized Pair<Color, Color> getColorPairForId(String id) {
		Pair<Color, Color> color = idToColor.get(id);
		if (color == null) {
			color = getNextColor();
			idToColor.put(id, color);
		}
		return color;
	}

	/**
	 * Gets color associated with id that is typically duller than the header color.
	 * If no color is currently associated with id, the next color will get
	 * assigned.
	 *
	 * @param id
	 * @return
	 */
	public Color getBodyColor(String id) {
		return getColorPairForId(id).getRight();
	}
}
