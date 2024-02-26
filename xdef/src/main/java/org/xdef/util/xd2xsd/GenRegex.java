package org.xdef.util.xd2xsd;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.xdef.sys.StringParser;

/** Generator of regular expression patterns (e.g. from xdatetime mask).
 * @author Trojan
 */
class GenRegex {
	/** String with characters which must be escaped, */
	private static final String ESCAPEDCHARS = "\\|.-?*+{}(){}^";

	/** Get regex string created form a part of xdatetime mask.
	 * @param mask part of xdatetime mask.
	 * @return regex string.
	 */
	private static String getRegex(final String mask) {
		StringBuilder ret = new StringBuilder();
		StringParser p = new StringParser(mask);
		while (!p.eos()) {
			if (p.isToken("DDD")) { // Day in year with leading zeros
				ret.append(
				"(00[1-9]|0[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-6])");
			} else if (p.isChar('D')) { // Day in year without leading zeros
				ret.append(
				"([1-9]|[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])");
			} else if (p.isChar('d')) { // Day in month
				ret.append(p.isChar('d')
					? "(0[1-9]|[1-2][0-9]|3[0-1])" // with leading zero
					: "([1-9]|[1-2][0-9]|3[0-1])"); // without leading zero
			} else if (p.isChar('H')) { // HOUR 0..24
				 ret.append((p.isChar('H'))
					 ? "(0[0-9]|1[0-9]|2[0-3])" //with leading zero
					 : "([0-9]|1[0-9]|2[0-3])"); //without leading zero
			} else if (p.isChar('h')) { // HOUR 0..12
				ret.append("([1-9]|1[0-2])");
			} else if (p.isChar('K')) { // Hour from 0-11
				ret.append(p.isChar('K')
					? "(0[0-9]|1[0-1])" //with leading zero
					: "([0-9]|1[0-1])"); //without leading zero
			} else if (p.isChar('M')) { // Month
				if (!p.isChar('M')) { // Month without leading zero (M)
					ret.append("([1-9]|1[0-2])");
				} else if (!p.isChar('M')) { // Month with leading zeros. (MM)
					ret.append("(0[1-9]|1[0-2])");
				} else if (!p.isChar('M')) { //Short month name (MMM)
					ret.append("[A-Za-z][a-z]{2}");
				} else { // Month full name. (MMMM)
					while (p.isChar('M')){}
					ret.append("[A-Za-z][a-z]*");
				}
			} else if (p.isChar('G')) { // era
				while (p.isChar('G')){}
				ret.append("[a-zA-Z]*(\\s[a-zA-Z])*");
			} else if (p.isChar('m')) { // minute 0..59
				ret.append(p.isChar('m')
					? "[0-5][0-9]" // with leading zero
					: "([0-9]|[1-5][0-9])"); // without leading zero
			} else if (p.isToken("s")) { // seconds 0..59
				ret.append(p.isChar('s')
					? "[0-5][0-9]" // with leading zero
					: "([1-9]|[1-5][0-9])"); // without leading zero
			} else if (p.isChar('S')) { //Seconds fraction
				while (p.isChar('S')) {}
				ret.append("\\d+");
			} else if (p.isChar('k')) { //Hour in day from interval 1-24
				ret.append(p.isChar('k')
					? "(0[1-9]|1[0-9]|2[0-4])" // with leading zero
					: "([1-9]|1[0-9]|2[0-4])"); // without leading zero
			} else if (p.isChar('y')) { // Year
				int i = 1;
				while (p.isChar('y')) i++;
				switch (i) {
					case 1:// Year (as ISO standard)
						ret.append("(-)?(0*)?\\d{4}"); break;
					case 2: // Year as 2 digits.
						ret.append("\\d{2}"); break;
					default: ret.append("\\d{4}\\d*"); // four and more digits.
				}
			} else if (p.isChar('Y')) { //Year
				int i = 1;
				while (p.isChar('Y')) i++;
				ret.append(i == 1
					? "(-)?(0*)?\\d{4}" // 4digits
					: "\\d{2}"); // two digits
			} else if (p.isToken("RR")) { //Year from 0 to 99 with leading zero
				ret.append("[0-9]{2}");
			} else if (p.isChar('z')) { //zone name
				int i = 1;
				while (p.isChar('z')) i++;
				ret.append(i == 1
					? "[a-zA-Z0-9]{1,5}" // short zone name
					: "([A-Z][a-z]*(\\s[A-Z][a-z]*)*)"); // full zone name
			} else if (p.isChar('Z')) { // zone base format
				int i = 1;
				while (p.isChar('Z')) i++;
				switch (i) {
					case 2: //+/-HHmm format
					ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3])[0-5][0-9]");
					break;
					case 3:
					case 4:
					case 5: //+/-HHmm format
						ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3])[0-5][0-9]");
						break;
					default: //+/-HH:mm format
					ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]");
				}
			} else if (p.isChar('a')) { //Information about day part (am, pm).
				while (p.isChar('a')){}
				ret.append("[a-zA-Z]+");
			} else if (p.isChar('{')) {
				p.findCharAndSkip('}');
			} else if (p.isChar('E')) { //Day in week
				int i = 1;
				while (p.isChar('E')) i++;
				ret.append(i < 4
					? "[a-zA-z]+" // short name
					: "[a-zA-z]+"); // full name
			} else if (p.isChar('e')) { //Day in week as number
				ret.append("[1-7]");
			} else if (p.isChar('[')) {
				ret.append("(");
			} else if (p.isChar(']')) {
				ret.append(")?");
			} else if (p.isChar('\'')) {
				StringBuilder constBuffer = new StringBuilder();
				while (!p.isChar('\'')) {
					constBuffer.append(p.peekChar());
				}
				ret.append(genEscapeChars(constBuffer.toString()));
			} else {
				ret.append(genEscapeChars(String.valueOf(p.peekChar())));
			}
		}
		return ret.toString();
	}

	/** Return string with special character escaped.
	 * @param c character.
	 * @return string with character or with special character escaped.
	 */
	protected static String genEscapedChar(final char c) {
		if (ESCAPEDCHARS.indexOf(c) >= 0) {
			return "\\"+ c;
		} else if (' ' == c) {
			return "\\s";
		} else {
			return String.valueOf(c);
		}
	}

	/** Return string with special characters escaped.
	 * @param string string to modify.
	 * @return modified string with special characters escaped.
	 */
	protected static String genEscapeChars(String string) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			ret.append(genEscapedChar(string.charAt(i)));
		}
		return ret.toString();
	}

	/** Return regex pattern of the string to be case insensitive.
	 * @param string string to be case insensitive.
	 * @return regex pattern of the string value to be case insensitive.
	 */
	public static String genCaseInsensitive(final String s) {
		String mask = "";
		for (char c: s.toCharArray()) {
			mask += Character.isLetter(c)
				? "[" + Character.toLowerCase(c)
					+ Character.toUpperCase(c) + "]"
				: GenRegex.genEscapedChar(c);
		}
		return mask;
	}

	/** Get set with regex strings created form xdatetime mask.
	 * @param mask xdatetime mask.
	 * @return set with regex strings.
	 */
	protected static String[] getRegexes(String mask) {
		Set<String> ret = new HashSet<>();
		StringTokenizer st = new StringTokenizer(mask, "|");
		while (st.hasMoreTokens()) {
			String maskPart = st.nextToken();
			String regex = getRegex(maskPart);
			regex = regex.trim();
			if (regex.length() > 0) {
				ret.add(regex);
			}
		}
		return ret.toArray(new String[0]);
	}
}