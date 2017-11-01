package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.Collection;

public interface IKeyMap {

	Collection<KeyCodeDto> getKeyCodes();

	int getKeyCode(int key);

	String getUnicodeName(int keycode);

}
