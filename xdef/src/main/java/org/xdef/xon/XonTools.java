package org.xdef.xon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Methods used in JSON/XON parsing, processing and conversions.
 * @author Vaclav Trojan
 */
public class XonTools {

	/** Value of null in JSON/XON objects. */
	public static final JNull JNULL = new JNull();

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

	final static String genCharAsUTF(final char ch) {
		String s = "\\u";
		for (int j = 12; j >= 0; j -=4) {
			s += "0123456789abcdef".charAt((ch >> j) & 0xf);
		}
		return s;
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

	/** Create string from JSON/XON source string data.
	 * @param s JSON string.
	 * @return string created from JSON/XON string data.
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

	/** Parse JSON/XON array (the string which begins with '[').
	 * @param ar the array where results are stored.
	 * @param p String parser with the string.
	 */
	private static void parseJArray(final List<Object> ar,final StringParser p){
		for (;;) {
			p.isSpaces();
			if (p.isChar('[')) {
				List<Object> ar1 = new ArrayList<Object>();
				parseJArray(ar1, p);
				ar.add(ar1);
				p.isSpaces();
				if (p.isChar(',')) {
					continue;
				} else if (p.isChar(']')) {
					break;
				}
				throw new RuntimeException("JList error");
			}
			if (p.isChar(']')) {
				break;
			}
			int pos = p.getIndex();
			char ch;
			int i;
			if ((i=p.isOneOfTokens(new String[]{"null","true","false"}))>=0
				&& (p.eos() || (ch=p.getCurrentChar())<=' '||ch==']'||ch==',')){
					ar.add(i == 0 ? null : i==1);
			} else {
				p.setIndex(pos);
				if (p.isSignedInteger()
					&& (p.eos() || (ch = p.getCurrentChar()) <= ' '
					|| ch == ']' || ch == ',')) {
					try {
						ar.add(Long.parseLong(
							p.getBufferPart(pos, p.getIndex())));
					} catch (Exception ex) {
						ar.add(new BigDecimal(
							p.getBufferPart(pos, p.getIndex())));
					}
				} else {
					p.setIndex(pos);
					if (p.isSignedFloat()
						&& (p.eos() || (ch = p.getCurrentChar()) <= ' '
						|| ch == ']' || ch == ',')) {
						String s = p.getBufferPart(pos, p.getIndex());
						if (s.indexOf('.') > 0 || s.indexOf('e') > 0
							|| s.indexOf('E') > 0) {
							ar.add(new BigDecimal(
								p.getBufferPart(pos, p.getIndex())));
						} else {
							try {
								ar.add(Long.parseLong(s));
							} catch (Exception ex) {
								ar.add(new BigInteger(
									p.getBufferPart(pos, p.getIndex())));
							}
						}
					} else {
						p.setIndex(pos);
						if (p.isChar('"')) {
							ar.add(XonTools.readJString(p));
						} else {
							for(;;) {
								if (p.isChar('\\')) {
									if (p.eos()) {
										throw new RuntimeException(
											"JList error");
									}
									p.nextChar();
								} else if ((ch = p.getCurrentChar()) == ' '
									|| ch == ',' || ch == ']' || ch == '[') {
									String s =
										p.getBufferPart(pos, p.getIndex());
									ar.add(xmlToJValue(s));
									break;
								}
								if (p.eos()) {
									throw new RuntimeException("JList error");
								}
								p.nextChar();
							}
						}
					}
				}
			}
			p.isSpaces();
			if (p.isChar(']')) {
				break;
			}
			if (!p.isChar(',')) {
				throw new RuntimeException("JList error");
			}
		}
	}

	/** Get JSON/XON value from string in XML.
	 * @param s string with XON simple value source
	 * @return object with XON value
	 */
	public final static Object xmlToJValue(final String s) {
		if (s.isEmpty()) {
			return "";
		} else if (s.charAt(0) == '[') {
			ArrayList<Object> ar = new ArrayList<Object>();
			StringParser p = new StringParser(s);
			p.setIndex(1);
			parseJArray(ar, p);
			return ar;
		} else if ("null".equals(s)) {
			return null;
		} else if ("true".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equals(s)) {
			return Boolean.FALSE;
		}
		int len = s.length();
		char ch = s.charAt(0);
		if (ch == '"' && s.charAt(len-1) == '"') {
			StringParser p = new StringParser(s);
			p.setIndex(1);
			return XonTools.readJString(p);
		}
		int i = 0;
		if (ch == '-' && len > 0) {
			ch = s.charAt(1);
			i = 1;
		}
		if (ch == '0' && i + 1 < len && s.charAt(i+1) >= '0'
			&& s.charAt(i+1) <= '9') {
			return s; //redundant leading zero, => JSON string
		}
		if (ch >= '0' && ch <= '9') { // not redundant leading zero
			try {
				return Long.parseLong(s);
			} catch (Exception ex) {}
			try {
				return new BigDecimal(s);
			} catch (Exception ex) {}
		}
		return s; // JSON String
	}

	/** Get XML name created from JSON/XON pair name.
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

	/** Create JSON/XON named value from XML name.
	 * @param name XML name.
	 * @return JSON/XON name.
	 */
	public final static String xmlToJName(final String name) {
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

	/** Read character representation from JSON/XON source.
	 * @param p parser where is source.
	 * @return decoded character.
	 */
	public static int readJChar(final SParser p) {
		if (p.isChar('\\')) {
			char c = p.peekChar();
			int i = "u\"\\/bfnrt".indexOf(c);
			if (i == 0) { //u
				int x = 0;
				for (int j = 0; j < 4; j++) {
					int y = hexDigit(p.peekChar());
					if (y < 0) {
						p.error(JSON.JSON005);//hexadecimal digit expected
						return -1;
					}
					x = (x << 4) + y;
				}
				return x;
			} else if (i > 0) { // escaped characters
				return (int) "u\"\\/\b\f\n\r\t".charAt(i);
			} else {
				 // Incorrect escape character in string
				p.error(JSON.JSON006);
				return -1;
			}
		} else if (!p.eos()) {
			return p.peekChar();
		}
		p.error(JSON.JSON007); //Unexpected eof
		return -1;
	}

	/** Create JSON/XON string from source.
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

	/** Convert a character to JSON/XON representation.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	public final static String charToJSource(final char c) {
		int i = "\"\\/bfnrt".indexOf(c);
		if (i >= 0) {
			return "\\" + "\"\\/bfnrt".charAt(i);
		}
		if (i < 0 && StringParser.getXmlCharType(c, StringParser.XMLVER1_0)
			== StringParser.XML_CHAR_ILLEGAL) {
			String s = "\\u";
			for (int j = 12; j >= 0; j -=4) {
				s += "0123456789abcdef".charAt((c >> j) & 0x0f);
			}
			return s;
		} else {
			return String.valueOf(c);
		}
	}

	/** Convert simple value to the form XML attribute.
	 * @param x the object to be converted.
	 * @return XML form of the of attribute value created from argument.
	 */
	public final static String genXMLValue(final Object x) {
		if (x == null) {
			return "null";
		}
		String s;
		if (x instanceof String) {
			s = (String) x;
		} else if (x instanceof Character) {
			s = String.valueOf((Character) x);
		} else if (x instanceof InetAddress) {
			return x.toString().substring(1);
		} else if (x instanceof Currency) {
			return ((Currency) x).getCurrencyCode();
		} else if (x instanceof byte[]) {
			s = new String(SUtils.encodeBase64((byte[]) x));
		} else {
			return x.toString();
		}
		if (s.isEmpty() || "null".equals(s)
			|| "true".equals(s) || "false".equals(s)) {
			return '"' + s + '"';
		}
		boolean addQuot = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c <= ' ' || c == '\\' || c == '"'
				|| !Character.isDefined(c)) {
				addQuot = true;
				break;
			}
		}
		char ch = s.charAt(0);
		if (addQuot) {
			if (s.equals(s.trim()) && ch != '"' && ch != '[' && ch != '\\'){
				// For attributes it is not necessary to add quotes if
				// string does not contain leading or trailing white spaces
				return s;
			} else {
				return '"' + jstringToSource(s) + '"';
			}
		} else {
			if (ch == '-' || ch >= '0' && ch <= '9'
				&& (ch = s.charAt(s.length() - 1)) >= '0' && ch <= '9') {
				StringParser p = new StringParser(s);
				if ((p.isSignedFloat() || p.isSignedInteger()) && p.eos()) {
					return '"' + s + '"'; // value is number, must be quoted
				}
			}
			return s;
		}
	}

	/** Read value of JSON/XON string.
	 * @param p parser where the string is on the actual position.
	 * @return the parsed string.
	 */
	public final static String readJString(final SParser p) {
		StringBuilder sb = new StringBuilder();
		while (!p.eos()) {
			if (p.isChar('"')) {
				return sb.toString();
			} else {
				int i = readJChar(p);
				if (i < 0) {
					return null;
				}
				sb.append((char) i);
			}
		}
		p.error(JSON.JSON001); // end of string ('"') is missing
		return null;
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used when JSON is parsed from X-definition compiler.
////////////////////////////////////////////////////////////////////////////////

	/** Interface of JSON/XON object. */
	public interface JObject {
		public SPosition getPosition();
		public Object getValue();
		public SBuffer getSBuffer();
	}

	/** JSON/XON map. */
	public static class JMap extends LinkedHashMap<Object, Object>
		implements JObject {
		private final SPosition _position; // SPosition of parsed object
		public JMap(final SPosition position) {super(); _position = position;}
		@Override
		public SPosition getPosition() {return _position;}
		@Override
		public Object getValue() {return null;}
		@Override
		public SBuffer getSBuffer() {return null;}
	}

	/** JSON/XON array. */
	public static class JArray extends ArrayList<Object> implements JObject {
		private final SPosition _position; // SPosition of parsed object
		public JArray(final SPosition position) {super(); _position = position;}
		@Override
		public SPosition getPosition() {return _position;}
		@Override
		public Object getValue() {return null;}
		@Override
		public SBuffer getSBuffer() {return null;}
	}

	/** JSON/XON simple value. */
	public static class JValue implements JObject {
		private final SPosition _position; // SPosition of parsed object
		private final Object _o; // parsed object
		public JValue(final SPosition position, final Object val) {
			_position = position;
			_o = val;
		}
		@Override
		public SPosition getPosition() {return _position;}
		@Override
		public Object getValue() {return _o;}
		@Override
		public SBuffer getSBuffer(){return new SBuffer(toString(),_position);}
		@Override
		public String toString() {return _o == null ? "null" : _o.toString();}
	}

	/** JSON/XON any object. */
	public static class JAny extends JValue {
		public JAny(final SPosition position, final SBuffer val) {
			super(position, val);
		}
		@Override
		public SBuffer getSBuffer() {return (SBuffer) getValue();}
	}

	/** Representation of JSON/XON object "null". */
	public static final class JNull {
		private JNull() {}
		@Override
		public final String toString() {return "null";}
		@Override
		public final int hashCode(){return 0;}
		@Override
		public final boolean equals(final Object o) {
			return o==null || o instanceof JNull;
		}
	}

	/** Get reader data from argument.
	 * @param x the object containing XON/JSON data.
	 * @return array with two items: Reader and System ID.
	 */
	static final Object[] getReader(final Object x, final Charset charset) {
		Object[] result = new Object[2];
		if (x instanceof String) {
			String s = (String) x;
			try {
				return getReader(SUtils.getExtendedURL(s), charset);
			} catch (Exception ex) {
				try {
					return getReader(new File(s), charset);
				} catch (Exception exx) {}
			}
			result[0] = new StringReader(s);
			result[1] = "STRING";
		} else if (x instanceof File) {
			File f = (File) x;
			try {
				result[0] = new InputStreamReader(
					new FileInputStream(f),
					charset == null ? Charset.forName("UTF-8") : charset);
				result[1] = f.getCanonicalPath();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (x instanceof URL) {
			URL u = (URL) x;
			try {
				result[0] = new InputStreamReader(u.openStream(),
					charset == null ? Charset.forName("UTF-8") : charset);
				result[1] = u.toExternalForm();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (x instanceof InputStream) {
			result[0] = new InputStreamReader((InputStream) x,
				charset == null ? Charset.forName("UTF-8") : charset);
			result[1] = "INPUT_STREAM";
		} else if (x instanceof Reader) {
			result[0] = (Reader) x;
			result[1] = "READER";
		} else {
			//Program exception &{0}
			throw new SRuntimeException(SYS.SYS036,
				"Incorrect parameter of getReader");
		}
		return result;
	}
}