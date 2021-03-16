package org.xdef.json;

import org.xdef.msg.JSON;
import org.xdef.sys.SParser;
import org.xdef.sys.StringParser;

/** Methods used in JSON/XON parsing, processing and conversions.
 * @author Vaclav Trojan
 */
public class JsonTools {

////////////////////////////////////////////////////////////////////////////////
// methods used in this package
////////////////////////////////////////////////////////////////////////////////

	/** Convert character to representation used in XML names.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	final static String genXmlHexChar(final char c) {
		return "_x" + Integer.toHexString(c) + '_';
	}

	/** Check and get hexadecimal digit as integer.
	 * @param ch character with hexadecimal digit.
	 * @return hexadecimal digit as an integer number 0..15
	 * or return -1 if the argument is not hexadecimal digit.
	 */
	final static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return (i > 15) ? i - 6 : i;
	}

	/** Check if on the position given by index in a string it is the
	 * form of hexadecimal character representation.
	 * @param s inspected string.
	 * @param index index where to start inspection.
	 * @return true if index position represents hexadecimal form of character.
	 */
	final static boolean isJChar(final String s, final int index) {
		if (index + 3 > s.length() ||
			s.charAt(index) != '_' || s.charAt(index+1) != 'x' ||
			hexDigit(s.charAt(index+2)) < 0) {
			return false;
		}
		// parse hexdigits
		for (int i = index + 3; i < index + 7 && i < s.length(); i++) {
			char ch = s.charAt(i);
			if (hexDigit(ch) < 0) {
				// not hexadecimal digit.
				return ch == '_'; //if '_' return true otherwise return false
			}
		}
		return false;
	}

	/** Replace colon in XML name with "_x3a_".
	 * @param s raw XML name.
	 * @return name with colon replaced by "_x3a_".
	 */
	final static String replaceColonInXMLName(final String s) {
		int i = s.indexOf(':');
		return i >= 0 ? s.substring(0, i) + "_x3a_" + s.substring(i + 1) : s;
	}

////////////////////////////////////////////////////////////////////////////////
// public methods (used also in X-definition compilation and X-components)
////////////////////////////////////////////////////////////////////////////////

	/** Create string from JSON source string.
	 * @param s JSON string.
	 * @return string created from JSON string.
	 */
	public final static String jstringToSource(final String s) {
		StringBuilder sb = new StringBuilder();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			switch (ch) {
				case '\\':
					sb.append("\\\\");
					continue;
				case '"':
					sb.append("\\\"");
					continue;
				case '\b':
					sb.append("\\b");
					continue;
				case '\f':
					sb.append("\\f");
					continue;
				case '\n':
					sb.append("\\n");
					continue;
				case '\r':
					sb.append("\\r");
					continue;
				case '\t':
					sb.append("\\t");
					continue;
				default:
					if (ch >= ' ' && Character.isDefined(ch)) {
						sb.append(ch);
					} else { // create \\uxxxx
						sb.append("\\u");
						for (int x = 12; x >= 0; x -=4) {
							sb.append("0123456789abcdef"
								.charAt((ch >> x) & 0xf));
						}
					}
			}
		}
		return sb.toString();
	}

	/** Get XML name created from JSOM pair name.
	 * @param s JSOM pair name.
	 * @return XML name.
	 */
	public final static String toXmlName(final String s) {
		if (s.isEmpty()) {
			return "_x00_"; // empty string
		} else if (("_x00_").equals(s)) {
			return "_x5f_x00_";
		}
		StringBuilder sb = new StringBuilder();
		char ch = s.charAt(0);
		sb.append(ch == ':' || isJChar(s, 0)
			|| StringParser.getXmlCharType(ch, StringParser.XMLVER1_0)
			 != StringParser.XML_CHAR_NAME_START
			? genXmlHexChar(ch) : ch);
		byte firstcolon = 0;
		for (int i = 1; i < s.length(); i++) {
			ch = s.charAt(i);
			if (isJChar(s, i)) {
				sb.append(genXmlHexChar(ch));
			} else if (ch == ':' && firstcolon == 0) {
				firstcolon = 1;
				sb.append(':');
				if (i + 1 < s.length()) {
					ch=s.charAt(++i);
					sb.append(isJChar(s,i)
						|| StringParser.getXmlCharType(ch,
							StringParser.XMLVER1_0)
							< StringParser.XML_CHAR_COLON
					? genXmlHexChar(ch) : ch);
				} else {
					i--;
				}
			} else {
				sb.append(isJChar(s,i)
					|| StringParser.getXmlCharType(ch, StringParser.XMLVER1_0)
					< StringParser.XML_CHAR_COLON ? genXmlHexChar(ch) : ch);
			}
		}
		return sb.toString();
	}

	/** Create JSON named value from XML name.
	 * @param name XML name.
	 * @return JSON name.
	 */
	public final static String xmlToJsonName(final String name) {
		if ("_x00_".equals(name)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int len = name.length();
		for (int i = 0; i < len; i++) {
			char ch = name.charAt(i);
			if (ch == '_' && i + 2 < len) {
				if (isJChar(name, i)) {
					int ndx = name.indexOf('_', i+1);
					int x = Integer.parseInt(name.substring(i+2, ndx), 16);
					sb.append((char) x);
					i = ndx;
				} else {
					sb.append('_');
				}
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/** Create JSON string from source string.
	 * @param src XML form of string.
	 * @return XML form of string converted to JSON.
	 */
	public final static String jstringFromSource(final String src) {
		if (src == null || src.isEmpty()) {
			return src;
		}
		// remove starting and ending '"'
		String s = (src.charAt(0)=='"' && src.charAt(src.length() - 1)=='"')
			? src.substring(1, src.length() - 1) : src;
		return jstringToSource(s);
	}

	/** Read value of JSON string.
	 * @param p parser where the string is on the actual position.
	 * @return the parsed string.
	 */
	public final static String readJSONString(final SParser p) {
		StringBuilder sb = new StringBuilder();
		while (!p.eos()) {
			if (p.isChar('"')) {
				return sb.toString();
			} else if (p.isChar('\\')) {
				char c = p.peekChar();
				if (c == 'u') {
					int x = 0;
					for (int j = 0; j < 4; j++) {
						int y = hexDigit(p.peekChar());
						if (y < 0) {
							p.error(JSON.JSON005);//hexadecimal digit expected
							break;
						}
						x = (x << 4) + y;
					}
					sb.append((char) x);
				} else {
					int i = "\"\\/bfnrt".indexOf(c);
					if (i >= 0) {
						sb.append("\"\\/\b\f\n\r\t".charAt(i));
					} else {
						 // Incorrect escape character in string
						p.error(JSON.JSON006);
						return null;
					}
				}
			} else {
				sb.append(p.peekChar());
			}
		}
		p.error(JSON.JSON001); // end of string ('"') is missing
		return null;
	}
}