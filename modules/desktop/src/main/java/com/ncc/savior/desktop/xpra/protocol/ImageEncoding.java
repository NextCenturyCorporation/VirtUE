package com.ncc.savior.desktop.xpra.protocol;

/**
 * These are the different encodings for image data used by Xpra. The client
 * does not need to handle all, but must let the server know which ones it can
 * handle. The server has a prioritized list and will choose the best available.
 *
 *
 */
public enum ImageEncoding {
	rgb24, premult_argb32, jpeg, png, pngP("png/P"), pngL("png/L"), h264;

	private final String code;

	ImageEncoding() {
		code = name();

	}

	ImageEncoding(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code;
	}

	public static ImageEncoding parse(String string) {
		for (ImageEncoding ie : ImageEncoding.values()) {
			if (ie.getCode().equals(string)) {
				return ie;
			}
		}
		return ImageEncoding.valueOf(string);
	}
}
