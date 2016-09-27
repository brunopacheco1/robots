package com.dev.bruno.utils;

public class StringUtils {

	public static String clearText(String text) {
		if(text == null) {
			return null;
		}
		
		return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
}