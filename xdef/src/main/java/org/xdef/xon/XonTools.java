package org.xdef.xon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import org.xdef.XDBytes;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDTelephone;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefTelephone;
import org.xdef.impl.code.DefURI;
import org.xdef.impl.parsers.XDParseChar;
import org.xdef.impl.parsers.XDParseCurrency;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Methods used in XON/JSON/Properties/INI/CSV data processing.
 * @author Vaclav Trojan
 */
public class XonTools {
	/** Value of null in XON/JSON objects. */
	public static final JNull JNULL = new JNull();

////////////////////////////////////////////////////////////////////////////////
// methods used in this package
////////////////////////////////////////////////////////////////////////////////
	/** Convert character to representation used in XML names.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	final static String genXmlHexChar(final char c) {return "_x" + Integer.toHexString(c) + '_';}

	/** Check and get hexadecimal digit as integer.
	 * @param ch character with hexadecimal digit.
	 * @return hexadecimal digit as an integer number 0..15 or return -1 if argument is not hexadecimal digit.
	 */
	final static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return (i > 15) ? i - 6 : i;
	}

	final static String genCharAsUTF(final char ch) {
		String s = "\\u";
		for (int j = 12; j >= 0; j -=4) {
			s += "0123456789ABCDEF".charAt((ch >> j) & 0xf);
		}
		return s;
	}

	/** Check if on the position given by index in a string it is hexadecimal character representation.
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
		for (int i = index + 3; i < index + 7 && i < s.length(); i++) { // parse hexdigits
			char ch = s.charAt(i);
			if (hexDigit(ch) < 0) {// not hexadecimal digit.
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
	/** Create string from XON/JSON source string data.
	 * @param s XON/JSON string.
	 * @return string created from XON/JSON string data.
	 */
	public static final String jstringToSource(final String s) {
		StringBuilder sb = new StringBuilder();
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];
			switch (ch) {
				case '\\': sb.append("\\\\"); continue;
				case '"': sb.append("\\\""); continue;
				case '\b': sb.append("\\b"); continue;
				case '\f': sb.append("\\f"); continue;
				case '\n': sb.append("\\n"); continue;
				case '\r': sb.append("\\r"); continue;
				case '\t': sb.append("\\t"); continue;
				default:
					if (ch >= ' ' && Character.isDefined(ch)) {
						sb.append(ch);
					} else { // create \\uxxxx
						sb.append(genCharAsUTF(ch));
					}
			}
		}
		return sb.toString();
	}

	/** Parse XON/JSON array of simple values (string which begins with '[').
	 * @param ar the array where results are stored.
	 * @param p String parser with the string.
	 */
	private static void parseJArray(final List<Object> ar,final StringParser p){
		for (;;) {
			p.isSpaces();
			if (p.isChar('[')) {
				List<Object> ar1 = new ArrayList<>();
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
				char typCh; // character with type specification after a number
				if (p.isSignedInteger() && (typCh=p.isOneOfChars("bsilND"))>=0
					&& (p.eos() || (ch = p.getCurrentChar()) <= ' ' || ch == ']' || ch == ',')) {
					String s = p.getBufferPart(pos, p.getIndex());
					switch(typCh) {
						case 'b': ar.add(Byte.valueOf(s)); break;
						case 's': ar.add(Short.valueOf(s)); break;
						case 'i': ar.add(Integer.valueOf(s)); break;
						case 'N': ar.add(new BigInteger(s)); break;
						case 'D': ar.add(new BigDecimal(s)); break;
						default:
							try {
								ar.add(Long.valueOf(s));
							} catch (NumberFormatException ex) {
								ar.add(new BigInteger(s));
							}
					}
				} else if (p.isSignedFloat() && (typCh=p.isOneOfChars("fdD"))>=0 && (p.eos()
					|| (ch = p.getCurrentChar()) <= ' ' || ch == ']' || ch == ',')) {
					String s = p.getBufferPart(pos, p.getIndex());
					switch(typCh) {
						case 'f': ar.add(Float.valueOf(s)); break;
						case 'D': ar.add(new BigDecimal(s)); break;
						default: ar.add(Double.valueOf(s));
					}
				} else {
					p.setIndex(pos);
					if (p.isChar('"')) {
						ar.add(XonTools.readJString(p));
					} else {
						for(;;) {
							if (p.isChar('\\')) {
								if (p.eos()) {
									throw new RuntimeException("JList error");
								}
								p.nextChar();
							} else if ((ch=p.getCurrentChar()) == ' ' || ch == ',' || ch == ']' || ch == '['){
								String s = p.getBufferPart(pos, p.getIndex());
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
			p.isSpaces();
			if (p.isChar(']')) {
				break;
			}
			if (!p.isChar(',')) {
				throw new RuntimeException("JList error");
			}
		}
	}

	private static XDParseResult chkValue(final String s, final XDParser p) {
		DefParseResult result = new DefParseResult(s);
		p.parseObject(null, result);
		return result;
	}

	/** Get XON/JSON value from string in XML.
	 * @param s string with XON simple value source
	 * @return object with XON value
	 */
	public static final Object xmlToJValue(final String s) {
		if (s == null) {
			return null;
		}
		switch(s) {
			case "": return "";
			case "null": return null;
			case "true": return Boolean.TRUE;
			case "false": return Boolean.FALSE;
		}
		if (s.charAt(0) == '[') {
			List<Object> ar = new ArrayList<>();
			StringParser p = new StringParser(s);
			p.setIndex(1);
			parseJArray(ar, p);
			return ar;
		}
		int len = s.length();
		char ch = s.charAt(0);
		if (len == 1) {
			if (ch>='0' && ch<='9') { // one digit: -> byte
				return ch - '0';
			}
			return s; // one char
		}
		char endChar = s.charAt(len-1);
		if (ch == '"' && endChar == '"') {
			StringParser p = new StringParser(s);
			p.setIndex(1);
			return XonTools.readJString(p);
		}
		int i = 0;
		if (ch == '-') {
			if (len == 1) {
				return "-"; // only minus char -> string
			}
			ch = s.charAt(1);
			i = 1;
		}
		if (ch == '0' && i + 1 < len && s.charAt(i+1) >= '0' && s.charAt(i+1) <= '9') {
			return s; //redundant leading zero, => XON/JSON string
		}
		if (ch >= '0' && ch <= '9') { // not redundant leading zero
			try {
				return Long.valueOf(s);
			} catch (NumberFormatException ex) {}
			try {
				return new BigDecimal(s);
			} catch (Exception ex) {}
		}
		XDParseResult r;
		if (endChar == '"') {
			try {
				switch (ch) {
					case 'T': return new DefTelephone(s);
					case 'e': return new DefEmailAddr(s);
					case 'u': return new DefURI(s);
					case 'C':
						if ((r = chkValue(s, new XDParseCurrency())).matches()){
							return r.getParsedValue().getObject();
						}
						break;
					case 'c':
						if ((r = chkValue(s, new XDParseChar())).matches()) {
							return r.getParsedValue().getObject();
						}
				}
			} catch (Exception ex) {}
		}
		return s; // XON/JSON String
	}

	/** Get XML name created from XON/JSON pair name.
	 * @param s XON/JSON pair name.
	 * @return XML name.
	 */
	public static final String toXmlName(final String s) {
		if (s.isEmpty()) {
			return "_x_"; // empty string
		} else if (("_x_").equals(s)) {
			return "_x5f_x_";
		}
		StringBuilder sb = new StringBuilder();
		char ch = s.charAt(0);
		sb.append(ch == ':' || isJChar(s, 0)
			|| StringParser.getXmlCharType(ch, StringParser.XMLVER1_0) != StringParser.XML_CHAR_NAME_START
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
						|| StringParser.getXmlCharType(ch, StringParser.XMLVER1_0)<StringParser.XML_CHAR_COLON
						? genXmlHexChar(ch) : ch);
				} else {
					i--;
				}
			} else {
				sb.append(isJChar(s,i)
					|| StringParser.getXmlCharType(ch, StringParser.XMLVER1_0) < StringParser.XML_CHAR_COLON
					? genXmlHexChar(ch) : ch);
			}
		}
		return sb.toString();
	}

	/** Create XON/JSON named value from XML name.
	 * @param name XML name.
	 * @return XON/JSON name.
	 */
	public static final String xmlToJName(final String name) {
		if ("_x_".equals(name)) {
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

	/** Read character representation from XON/JSON source.
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
				p.error(JSON.JSON006); // Incorrect escape character in string
				return -1;
			}
		} else if (!p.eos()) {
			return p.peekChar();
		}
		p.error(JSON.JSON007); //Unexpected eof
		return -1;
	}

	/** Create XON/JSON string from source.
	 * @param src XML form of string.
	 * @return XML form of string converted to XON/JSON.
	 */
	public static final String jstringFromSource(final String src) {
		if (src == null || src.isEmpty()) {
			return src;
		}
		// remove starting and ending '"'
		return jstringToSource(src.charAt(0) == '"' && src.endsWith("\"")
			? src.substring(1, src.length() - 1) : src);
	}

	/** Convert a character to XON/JSON representation.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	public static final String charToJSource(final char c) {
		int i = "\"\\/bfnrt".indexOf(c);
		if (i >= 0) {
			return "\\" + "\"\\/bfnrt".charAt(i);
		}
		return i < 0 && StringParser.getXmlCharType(c, StringParser.XMLVER1_0)==StringParser.XML_CHAR_ILLEGAL
			? genCharAsUTF(c) : String.valueOf(c);
	}

	/** Check if the string in argument is a JSON number notation.
	 * @param s the string to be checked.
	 * @return true if argument is a (signed) float number.
	 */
	public static final boolean isNumber(final String s) {
		StringParser p = new StringParser(s);
		boolean minus = p.isChar('-');
		if (p.isInteger() && p.eos()) {
			return true;
		}
		p.setIndex(minus ? 1 : 0);
		return p.isFloat() && p.eos();
	}

	/** Create string from value.
	 * @param s original string value.
	 * @return quoted string if necessary.
	 */
	public static final String genXMLString(final String s) {
		switch (s) {
			case "":
			case "-":
			case "null":
			case "true":
			case "false":
			return '"' + s + '"';
		}
		if (isNumber(s)) {
			return '"' + s + '"';
		}
		boolean addQuot = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c <= ' ' || c == '\\' || c == '"' || !Character.isDefined(c)) {
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
			return s;
		}
	}

	/** Convert simple value to the form XML attribute.
	 * @param x the object to be converted.
	 * @return XML form of the of attribute value created from argument.
	 */
	public static final String genXMLValue(final Object x) {
		if (x == null) {
			return "null";
		} else if (x instanceof String) {
			return genXMLString((String) x);
		} else if (x instanceof Character) {
			return genXMLString(String.valueOf((Character) x));
		} else if (x instanceof XDBytes) {
			XDBytes y = (XDBytes) x;
			return (y.isBase64() ? "b("+y.getBase64() : "x("+y.getHex()) + ")";
		} else if (x instanceof byte[]) {
			return genXMLString(new String(SUtils.encodeBase64((byte[]) x)));
		} else if (x instanceof InetAddress) {
			return genXMLString(x.toString().substring(1));
		} else if (x instanceof Currency) {
			return genXMLString(((Currency) x).getCurrencyCode());
		} else if (x instanceof XDTelephone) {
			return "t\"" + x + '"';
		}
		return x.toString();// Boolean, Number, etc...
	}

	/** Read value of XON/JSON string.
	 * @param p parser where the string is on the actual position.
	 * @return the parsed string.
	 */
	public static final String readJString(final SParser p) {
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
// Interface and classes used when XON/JSON is parsed in X-definition compiler.
////////////////////////////////////////////////////////////////////////////////
	/** Interface of JSON/XON object. */
	public interface JObject {
		public SPosition getPosition();
		public Object getValue();
		public SBuffer getSBuffer();
	}

	/** XON/JSON map. */
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

	/** XON/JSON array. */
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

	/** XON/JSON simple value. */
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

	/** XON/JSON any object. */
	public static class JAny extends JValue {
		public JAny(final SPosition position, final SBuffer val) {
			super(position, val);
		}
		@Override
		public SBuffer getSBuffer() {return (SBuffer) getValue();}
	}

	/** Representation of XON/JSON object "null". */
	public static final class JNull {
		private JNull() {}
		@Override
		public final String toString() {return "null";}
		@Override
		public final int hashCode(){return 0;}
		@Override
		public final boolean equals(final Object o) {return o==null || o instanceof JNull;}
	}

	/** Check if the argument is a simple value. Simple value is null,
	 * Number, Boolean, String or X_Value with object which is simple value.
	 * @param val Object to be tested.
	 * @return true if the argument is a simple value.
	 */
	public static final boolean isSimpleValue(final Object val) {
		Object o;
		return val == null || val instanceof Number || val instanceof Boolean
			|| val instanceof String || val instanceof XonTools.JValue
			&& ((o=((XonTools.JValue) val).getValue()) == null
				|| o instanceof Number || o instanceof Boolean || o instanceof String);
	}

	/** Generate XML form from XON/JSON string value.
	 * @param val Object with value.
	 * @param mode 0 .. text node, 1.. attribute, 2.. array of simple items
	 * @return XML form of string from the argument val,
	 */
	public static final String jstringToXML(final Object val, final int mode) {
		if (val == null) {
			return "null";
		}
		if (val instanceof String) {
			String s = (String) val;
			if (s.isEmpty()) {
				return mode == 1 ? "" : "\"\"";
			}
			if ("true".equals(s) || "false".equals(s) || "null".equals(s)) {
				return '"' + s + '"';
			}
			char ch = s.charAt(0);
			int len = s.length();
			boolean addQuot = mode == 2 || mode == 1 && (ch == '[' || ch == '"')
				|| mode == 0 && (ch <= ' ' || s.charAt(len-1) <= ' ');
			if (!addQuot) {
				int i = 0;
				if (ch == '-' && len > 1) {
					i = 1;
					ch = s.charAt(1);
				}
				if (ch >= '0' && ch <= '9') { //if it is a number => qoute
					if (i + 1 == len) {
						return '"' + s + '"';
					}
					if (ch!='0'||(ch=s.charAt(i+1))=='.'||ch=='E'||ch=='e') {
						// number without redundant leading zeroes
						StringParser p = new StringParser(s);
						if ((p.isFloat() || p.isInteger()) && p.eos()) {
							return '"' + s + '"';
						}
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {
				switch (ch = s.charAt(i)) {
					case '\t':
						if (mode == 1 || addQuot) { // force quote attributes
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\t");
						} else {
							sb.append(ch);
						}
						break;
					case '\n':
						if (mode == 1 || addQuot) {  // force quote attributes
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\n");
						} else {
							sb.append(ch);
						}
						break;
					case '\r':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
							SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\r");
						break;
					case '\f':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
							SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\f");
						break;
					case '\b':
						if (!addQuot) { // force quote
							SUtils.modifyStringBuilder(sb, "\\", "\\\\");
							SUtils.modifyStringBuilder(sb, "\"", "\\\"");
							addQuot = true;
						}
						sb.append("\\b");
						break;
					case '\\':
						if (mode == 2 || addQuot) { // force quote array items
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\\\");
						} else {
							sb.append(ch);
						}
						break;
					case '"':
						if (mode == 2 || addQuot) { // force quote array items
							if (!addQuot) {
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append("\\\"");
						} else {
							sb.append(ch);
						}
						break;
					default:
						if (ch < ' '|| StringParser.getXmlCharType(ch,
							StringParser.XMLVER1_0) == StringParser.XML_CHAR_ILLEGAL) {
							if (!addQuot) { // force quote
								SUtils.modifyStringBuilder(sb, "\\", "\\\\");
								SUtils.modifyStringBuilder(sb, "\"", "\\\"");
								addQuot = true;
							}
							sb.append(genCharAsUTF(ch));
						} else {
							sb.append(ch);
						}
				}
			}
			if (!addQuot) {
				return s;
			}
			return '"' + sb.toString() + '"';
		} else {
			return val instanceof XDBytes ? (((XDBytes) val).isBase64() ? "b(" : "x(") + val + ')'
				: val instanceof InetAddress ? val.toString().substring(1) : val.toString();
		}
	}

	/** Get InputStream or Reader from object.
	 * @param source if it is string check file name, URL or input data otherwise it can be a File,
	 * InputStream or Reader.
1	 * @param sysId System ID or null.
	 * @param charset name or null.
	 * @return InputData object.
	 */
	final static InputData getInputFromObject(final Object source, final String sysId) {
		if (source instanceof String) {
			File f = new File((String) source);
			try { // try if it is URL
				return (f.exists() && f.isFile()) ? getInputFromObject(f, sysId)
					: getInputFromObject(SUtils.getExtendedURL((String)source), sysId);
			} catch (RuntimeException | MalformedURLException ex) {
				//not URL, file name, so create from string a reader
				return new InputData(new StringReader((String) source), sysId==null ? "STRING" : sysId);
			}
		} else if (source instanceof URL) {
			try {
				return new InputData(((URL) source).openStream(),
					sysId==null ? ((URL) source).toString() : sysId);
			} catch (Exception ex) {
				throw new SRuntimeException(SYS.SYS029, source.toString());//Can't read input stream&{0}{; }
			}
		} else if (source instanceof File) {
			try {
				return new InputData(new FileInputStream((File) source),
					sysId==null ? ((File) source).getAbsolutePath() : sysId);
			} catch (Exception ex) {
				throw new SRuntimeException(SYS.SYS028);//Can't read file: &{0}
			}
		} else if (source instanceof InputStream) {
			try {
				return new InputData((InputStream) source, sysId==null ? "INPUTSTREAM" : sysId);
			} catch (Exception ex) {
				throw new SRuntimeException(SYS.SYS029); //Can't read input stream&{0}{; }
			}
		} else if (source instanceof Reader) {
			return new InputData((Reader) source,sysId==null? "READER" : sysId);
		}
		//Unsupported type of argument &{0}: &{1}
		throw new SRuntimeException(SYS.SYS037,"source", source==null ? "null" : source.getClass().getName());
	}

	protected static final class InputData {
		final Reader _reader;
		final InputStream _in;
		final String _sysId;
		protected InputData(final Reader reader, final String sysId) {
			_reader = reader;
			_in = null;
			_sysId=sysId;
		}
		protected InputData(final InputStream in, final String sysId) throws Exception{
			_sysId = sysId;
			_reader = null;
			_in = in;
		}
	}
}