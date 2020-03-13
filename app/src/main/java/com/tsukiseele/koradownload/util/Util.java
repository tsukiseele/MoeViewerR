package com.tsukiseele.koradownload.util;

public class Util {
	public static String getUrlFilename(String url) {
		String filename = url.substring(url.lastIndexOf("/") + 1);
		int endIndex = filename.indexOf("?");
		if (endIndex != -1)
			filename = filename.substring(0, endIndex);
		return filename;
	}
}
