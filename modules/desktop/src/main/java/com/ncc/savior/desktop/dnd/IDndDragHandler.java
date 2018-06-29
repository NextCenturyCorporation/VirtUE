package com.ncc.savior.desktop.dnd;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.TransferHandler;

public interface IDndDragHandler {

	void onDragLeave(int x, int y, List<String> modifiers, int id);

	void onDragEnter(int x, int y, List<String> modifiers, int id);

	void onMouseDrag(MouseEvent event);

	TransferHandler getTransferHandler();

}
