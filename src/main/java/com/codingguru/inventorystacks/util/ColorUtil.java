package com.codingguru.inventorystacks.util;

public final class ColorUtil {

	public static String replace(String s) {
		return s.replaceAll("(&([a-f0-9]))", "\u00A7$2").replaceAll("&l", "\u00A7l").replaceAll("&o", "\u00A7o")
				.replaceAll("&k", "\u00A7k").replaceAll("&r", "\u00A7r").replaceAll("&n", "\u00A7n")
				.replaceAll("&m", "\u00A7m");
	}

}