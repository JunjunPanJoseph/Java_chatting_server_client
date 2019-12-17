package assignment;

import java.util.List;

public class EncodeDecode {
	private static  String seperator = "_";
	public static String encode(String[] code) {
		String str = "";
		boolean start = true;
		for (String s: code) {
			if (start) {
				start = false;
			} else {
				str += seperator;
				
			}
			str += s;
		}
		str += "\n";
		return str;
	}
	public static String encode(List<String> code) {
		String str = "";
		boolean start = true;
		for (String s: code) {
			if (start) {
				start = false;
			} else {
				str += seperator;
				
			}
			str += s;
		}
		str += "\n";
		return str;
	}
	public static String[] decode(String code) {
		return code.split(seperator);
	}
}
