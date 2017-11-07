package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.Collection;

/**
 * Interface to abstract away any keyboard specific implementation.
 *
 *
 */
public interface IKeyMap {
	Collection<KeyCodeDto> getKeyCodes();

	int getKeyCode(int key);

	String getUnicodeName(int key);

	KeyCodeDto getKeyCodeDto(int key);

}
