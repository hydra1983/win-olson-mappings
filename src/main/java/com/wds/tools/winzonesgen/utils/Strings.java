package com.wds.tools.winzonesgen.utils;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public final class Strings {
	public static byte[] toUTF8ByteArray(String value) {
		byte[] result = null;
		try {
			result = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	public static String fromUTF8ByteArray(byte[] bytes) {
		String result = null;
		try {
			result = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	public static boolean isDigitsOnly(String value) {
		if (value == null || value == "") {
			return false;
		}

		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// StringUtils.replace("Hello {0}!", "Edison") => "Hello Edison!";
	public static String substitute(String pattern, Object... args) {
		if (args == null || args.length == 0) {
			return pattern;
		}

		String result = pattern;
		for (int i = 0; i < args.length; i++) {
			String key = String.valueOf(i);
			String value = String.valueOf(args[i]);
			String toBeReplaced = "{" + key + "}";
			while (result.contains(toBeReplaced)) {
				result = result.replace(toBeReplaced, value);
			}
		}

		return result;
	}

	// Map<String,Object> props = ...;
	// props.put("name","Edison");
	// StringUtils.replace("hello ${name}!",props) => "Hello Edison!";
	public static String substitute(String pattern, Map<String, Object> map) {
		if (map == null || map.size() == 0) {
			return pattern;
		}

		String result = pattern;

		for (String key : map.keySet()) {
			String value = String.valueOf(map.get(key));
			result = result.replaceAll("\\$\\{" + key + "?\\}", value);
		}

		return result;
	}

	public static StringConcator concat(String string) {
		return new StringConcatorImpl(string);
	}

	public static String trimLeft(String string) {
		if (string == null || string == "") {
			return string;
		}
		return string.replaceAll("^\\s+", "");
	}

	public static String trimRight(String string) {
		if (string == null || string == "") {
			return string;
		}
		return string.replaceAll("\\s+$", "");
	}

	public static interface StringConcator {
		StringConcator concat(String string);

		String toString();
	}

	private static class StringConcatorImpl implements StringConcator {
		public StringConcatorImpl(String string) {
			this.builder = new StringBuilder();
			if (string != null) {
				builder.append(string);
			}
		}

		private final StringBuilder builder;

		@Override
		public StringConcator concat(String string) {
			this.builder.append(string);
			return this;
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}
	}

	public static String capitalize(String string) {
		return StringUtils.capitalize(string);
	}

	public static Boolean isEmpty(final CharSequence cs) {
		return StringUtils.isEmpty(cs);
	}

	public static String stripPrefix(String string, String prefix) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(prefix) && string.startsWith(prefix)) {
			result = string.substring(prefix.length());
		}
		return result;
	}

	public static String stripSuffix(String string, String suffix) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(suffix) && string.endsWith(suffix)) {
			result = string.substring(0, string.length() - suffix.length());
		}
		return result;
	}

	public static String stripFromFirstMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(string.indexOf(match));
		}
		return result;
	}

	public static String stripFromFirstMatchWithoutMatch(String string,
			String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(string.indexOf(match) + match.length());
		}
		return result;
	}

	public static String stripFromLastMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(string.lastIndexOf(match));
		}
		return result;
	}

	public static String stripFromLastMatchWithoutMatch(String string,
			String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(string.lastIndexOf(match)
					+ match.length());
		}
		return result;
	}

	public static String stripToFirstMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(0, string.indexOf(match) + 1);
		}
		return result;
	}

	public static String stripToFirstMatchWithMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string
					.substring(0, string.indexOf(match) + match.length());
		}
		return result;
	}

	public static String stripToLastMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(0, string.lastIndexOf(match) + 1);
		}
		return result;
	}

	public static String stripToLastMatchWithMatch(String string, String match) {
		String result = string;
		if (!isEmpty(string) && !isEmpty(match) && string.contains(match)) {
			result = string.substring(0,
					string.lastIndexOf(match) + match.length());
		}
		return result;
	}
}
