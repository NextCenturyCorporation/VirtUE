package com.ncc.savior.desktop.dnd;

import java.util.List;

public interface IDndDragHandler {

	void onDragLeave(int x, int y, List<String> modifiers, int id);

	void onDragEnter(int x, int y, List<String> modifiers, int id);

}
