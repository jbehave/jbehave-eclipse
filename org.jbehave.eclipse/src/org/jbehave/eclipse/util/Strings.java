package org.jbehave.eclipse.util;

import static org.jbehave.eclipse.util.IO.CR;
import static org.jbehave.eclipse.util.IO.LF;

import java.util.Collection;
import java.util.regex.Pattern;

public class Strings {

	public static String[] array(String... args) {
		return args;
	}

	public static String substringUntilOffset(String text, int offset) {
		String analyzedPart = text
				.substring(0, Math.min(offset, text.length()));
		String[] lines = analyzedPart.split("[\\n\\r]+");
		int lineNo = lines.length - 1;
		return lines[lineNo];
	}

	public static String escapeNL(String token) {
		return token.replace("\n", "\\n").replace("\r", "\\r");
	}

	public static String[] toArray(Collection<String> values) {
		if (values == null)
			return new String[0];
		return values.toArray(new String[values.size()]);
	}

	public static boolean startsWithSame(CharSequence sequence,
			CharSequence prefix) {
		int len = Math.min(sequence.length(), prefix.length());
		for (int i = 0; i < len; i++) {
			if (sequence.charAt(i) != prefix.charAt(i))
				return false;
		}

		// still there
		return true;
	}

	public static boolean startsWith(CharSequence sequence, CharSequence prefix) {
		int slen = sequence.length();
		int plen = prefix.length();
		if (slen < plen)
			return false;
		return startsWithSame(sequence, prefix);
	}

	public static String removeLeadingSpaces(String string) {
		if (string == null)
			return null;
		int len = string.length();
		int st = 0;

		while ((st < len) && (string.charAt(st) == ' ')) {
			st++;
		}
		return (st > 0) ? string.substring(st) : string;
	}

	public static String removeTrailingNewlines(String string) {
		if (string == null)
			return null;

		int index = string.length();
		while (index > 0) {
			index--;
			char c = string.charAt(index);
			if (c == CR || c == LF)
				continue;
			else {
				index++;
				break;
			}
		}
		if (index < string.length())
			return string.substring(0, index);
		else
			return string;
	}

	public static String times(int count, String what) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append(what);
		}
		return builder.toString();
	}

	public static Pattern convertGlobToPattern(String line) {
		return Pattern.compile(convertGlobToRegex(line));
	}

	public static String convertGlobToRegex(String line) {
		line = line.trim();
		int strLen = line.length();
		StringBuilder sb = new StringBuilder(strLen);

		boolean escaping = false;
		int inCurlies = 0;
		for (char currentChar : line.toCharArray()) {
			switch (currentChar) {
			case '*':
				if (escaping)
					sb.append("\\*");
				else
					sb.append(".*");
				escaping = false;
				break;
			case '?':
				if (escaping)
					sb.append("\\?");
				else
					sb.append('.');
				escaping = false;
				break;
			case '.':
			case '(':
			case ')':
			case '+':
			case '|':
			case '^':
			case '$':
			case '@':
			case '%':
				sb.append('\\');
				sb.append(currentChar);
				escaping = false;
				break;
			case '\\':
				if (escaping) {
					sb.append("\\\\");
					escaping = false;
				} else
					escaping = true;
				break;
			case '{':
				if (escaping) {
					sb.append("\\{");
				} else {
					sb.append('(');
					inCurlies++;
				}
				escaping = false;
				break;
			case '}':
				if (inCurlies > 0 && !escaping) {
					sb.append(')');
					inCurlies--;
				} else if (escaping)
					sb.append("\\}");
				else
					sb.append("}");
				escaping = false;
				break;
			case ',':
				if (inCurlies > 0 && !escaping) {
					sb.append('|');
				} else if (escaping)
					sb.append("\\,");
				else
					sb.append(",");
				break;
			default:
				escaping = false;
				sb.append(currentChar);
			}
		}
		return sb.toString();
	}

	public static int indexOf(CharSequence sequence, char c) {
		final int len = sequence.length();
		for (int i = 0; i < len; i++) {
			if (sequence.charAt(i) == c)
				return i;
		}
		return -1;
	}
}
