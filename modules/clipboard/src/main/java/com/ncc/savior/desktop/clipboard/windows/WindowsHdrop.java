package com.ncc.savior.desktop.clipboard.windows;

import java.util.List;
/**
 * UINT DragQueryFileA(
 * HDROP hDrop,
 * UINT  iFile,
 * LPSTR lpszFile,
 * UINT  cch
 * );
 * 
 * 
 * 
 */

import com.sun.jna.Structure;
import com.sun.jna.Structure.ByReference;

public class WindowsHdrop extends Structure implements ByReference {

	@Override
	protected List<String> getFieldOrder() {
		// TODO Auto-generated method stub
		return null;
	}

}
