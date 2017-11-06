package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.Collection;

public interface IKeyMap {

	Collection<KeyCodeDto> getKeyCodes();

	int getKeyCode(int key, boolean isShift);

	String getUnicodeName(int key, boolean isShift);

	KeyCodeDto getKeyCodeDto(int ordinal, boolean isShift);

}
