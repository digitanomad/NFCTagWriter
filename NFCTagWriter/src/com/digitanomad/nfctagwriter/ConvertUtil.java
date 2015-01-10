package com.digitanomad.nfctagwriter;

public class ConvertUtil {
	
	public static String stringToHex(String s) {
		String result = "";

		for (int i = 0; i < s.length(); i++) {
			result += String.format("%02X ", (int)s.charAt(i));
		}

		return result;
	}
	
	public static String stringToHex0x(String s) {
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("0x%02X ", (int)s.charAt(i));
        }

        return result;
    }
	
	public static String bytesToHex0x(byte[] s) {
        String result = "";

        for (int i = 0; i < s.length; i++) {
            result += String.format("0x%02X ", (int)s[i]);
        }

        return result;
    }
}
