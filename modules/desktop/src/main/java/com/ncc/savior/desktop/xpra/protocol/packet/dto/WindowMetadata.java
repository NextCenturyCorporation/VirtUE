package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This represents the metadata passed around for a new window.
 *
 *
 */
public class WindowMetadata {

	private Map<String, Object> metadata;

	public WindowMetadata(Map<String, Object> raw) {
		this.metadata = new HashMap<String, Object>();
		for (Entry<String, Object> entry : raw.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof byte[]) {
				value = new String((byte[]) value);
			}
			this.metadata.put(entry.getKey(), value);
		}
	}

	public List<String> getClassInstance() {
		return getStringList("class-instance");
	}

	public boolean getFullscreen() {
		return getBoolean("fullscreen");
	}

	public Boolean getFullscreenOrNull() {
		return getBooleanOrNull("fullscreen");
	}

	public int getGroupLeaderXid() {
		return getInt("group-leader-xid");
	}

	public int getOpacity() {
		return getInt("opacity");
	}

	public boolean getDecorations() {
		return getBoolean("decorations", true);
	}

	public boolean getDecorations(boolean defaultValue) {
		return getBoolean("decorations", defaultValue);
	}

	public int getPid() {
		return getInt("pid");
	}

	public String getRole() {
		return getString("role");
	}

	public boolean getSkipTaskbar() {
		return getBoolean("skip-taskbar");
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getSizeConstraints() {
		return (Map<String, Object>) metadata.get("size-constraints");
	}

	public boolean getMaximized() {
		return getBoolean("maximized");
	}

	public Boolean getMaximizedOrNull() {
		return getBooleanOrNull("maximized");
	}

	public boolean getModal() {
		return getBoolean("modal");
	}

	public String getIconTitle() {
		return getString("icon-title");
	}

	public String getTitle() {
		return getString("title");
	}

	public List<String> getWindowType() {
		return getStringList("window-type");
	}

	public String getXid() {
		return getString("xid");
	}

	public int getParentId() {
		return getInt("transient-for");
	}

	public boolean getIconic() {
		return getBoolean("iconic");
	}

	public Boolean getIconicOrNull() {
		return getBooleanOrNull("iconic");
	}

	private List<String> getStringList(String key) {
		Object ret = metadata.get(key);
		if (ret != null) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) ret;
			// List<String> list = new ArrayList<String>();
			// for (byte[] b : ret) {
			// list.add(new String(b));
			// }
			return list;
		}
		return new ArrayList<String>(0);
	}

	// private List<Integer> getIntegerList(String key) {
	// Object ret = metadata.get(key);
	// if (ret != null) {
	// @SuppressWarnings("unchecked")
	// List<Integer> list = (List<Integer>) ret;
	// return list;
	// }
	// return new ArrayList<Integer>(0);
	// }

	private boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	private Boolean getBooleanOrNull(String key) {
		Number num = (Number) metadata.get(key);
		if (num == null) {
			return null;
		} else {
			return num.intValue() > 0;
		}
	}

	private boolean getBoolean(String key, boolean defaultValue) {
		Number num = (Number) metadata.get(key);
		return (num == null ? defaultValue : num.intValue() > 0);
	}

	private int getInt(String key) {
		return getInt(key, -1);
	}

	private int getInt(String key, int defaultVal) {
		Number num = ((Number) metadata.get(key));
		return (num == null ? defaultVal : num.intValue());
	}

	private String getString(String key) {
		return (String) (metadata.get(key));
	}

	@Override
	public String toString() {
		return "WindowMetadata [metadata=" + metadata + "]";
	}
}
