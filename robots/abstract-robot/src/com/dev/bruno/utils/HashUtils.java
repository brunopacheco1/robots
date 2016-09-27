package com.dev.bruno.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static String getHash(String text) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(text.getBytes(), 0, text.length());

			BigInteger hash = new BigInteger(1, m.digest());

			return String.format("%32x", hash).trim();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
