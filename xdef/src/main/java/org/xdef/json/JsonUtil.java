package org.xdef.json;

import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.XConstants;
import org.xdef.sys.SParser;
import org.xdef.sys.SUtils;

/** JSON utility (parse JSON source to JSON instance, compare JSON instances,
 * and create string with JSON source from JSON object.
 * @author Vaclav Trojan
 */
public class JsonUtil {
	/** JSON map local name. */
	public final static String J_MAP = "map";
	/** JSON array local name. */
	public final static String J_ARRAY = "array";
	/** JSON any item local name (with JSON value. */
	public final static String J_ITEM = "item";
	/** JSON string item. */
	public final static String J_STRING = "string";
	/** JSON number item. */
	public final static String J_NUMBER = "number";
	/** JSON boolean item. */
	public final static String J_BOOLEAN = "boolean";
	/** JSON null item. */
	public final static String J_NULL = "null";
	/** JSON map key attribute name. */
	public final static String J_KEYATTR = "key";
	/** JSON value attribute name. */
	public final static String J_VALUEATTR = "value";

////////////////////////////////////////////////////////////////////////////////
// Common public mtethods
////////////////////////////////////////////////////////////////////////////////

	/** Convert character to representation used in XML names.
	 * @param c character to be converted.
	 * @return string with converted character.
	 */
	final static String genXmlHexChar(final char c) {
		return "_x" + Integer.toHexString(c) + '_';
	}

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

	/** Replace colon in XML name with "_x3a_".
	 * @param s raw XML name.
	 * @return name with colon replaced by "_x3a_".
	 */
	final static String replaceColonInXMLName(final String s) {
		int i = s.indexOf(':');
		return i >= 0 ? s.substring(0, i) + "_x3a_" + s.substring(i + 1) : s;
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
			|| StringParser.getXmlCharType(ch, XConstants.XML10)
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
						|| StringParser.getXmlCharType(ch, XConstants.XML10)
							< StringParser.XML_CHAR_COLON
					? genXmlHexChar(ch) : ch);
				} else {
					i--;
				}
			} else {
				sb.append(isJChar(s,i)
					|| StringParser.getXmlCharType(ch, XConstants.XML10)
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

	/** Check and get hexadecimal digit as integer.
	 * @param ch character with hexadecimal digit.
	 * @return hexadecimal digit as an integer number 0..15
	 * or return -1 if the argument is not hexadecimal digit.
	 */
	public final static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		if (i > 15) {
			return i - 6;
		}
		return i;
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
					for (int j = 1; j < 4; j++) {
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

////////////////////////////////////////////////////////////////////////////////
// JSON parser
////////////////////////////////////////////////////////////////////////////////

	private static XonParser initParser(final InputStream in, final String id) {
		try {
			int i = in.read(); //1st byte from input stream
			int j = in.read(); //2nd byte from input stream
			if (i < 0 || i == 0 && j < 0) {//EOF
				//Unexpected eof&{#SYS000}
				throw new SRuntimeException(JSON.JSON007, "&{line}1&{column}1");
			}
			String s;
			Reader reader;
			if (i > 0 && j > 0) {
				// xx xx xx xx  UTF-8
				s = String.valueOf((char) i) + (char)j;
				reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			} else {
				int k = in.read();
				int l = in.read();
				if (l < 0) {//EOF
					//Unexpected eof&{#SYS000}
					throw new SRuntimeException(JSON.JSON007,
						"&{line}1&{column}1");
				}
				if (i == 0 && j == 0 && k == 0) {
					if (l == 0) {// not a character
						// JSON object or array expected"
						throw new SRuntimeException(JSON.JSON009,
							"&{line}1&{column}1");
					}
					// 00 00 00 xx  UTF-32BE
					s = String.valueOf((char) l);
					reader = new InputStreamReader(in, "UTF-32BE");
				} else if (i == 0 && k == 0) {// 00 xx 00 xx  UTF-16BE
					s = String.valueOf((char) j) + (char) l;
					reader = new InputStreamReader(in, "UTF-16BE");
				} else if (k != 0) { // xx 00 xx 00  UTF-16LE
					s = String.valueOf((char) i) + (char) k;
					reader = new InputStreamReader(in, "UTF-16LE");
				} else { // xx 00 00 00  UTF-32LE
					s = String.valueOf((char) i);
					reader = new InputStreamReader(in, "UTF-32LE");
				}
			}
			XonParser jx = new XonParser();
			if (id != null) {
				jx.setSysId(id);
			}
			jx.setSourceReader(reader, 0L, s);
			return jx;
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	private static XonParser getParser(final Object src, final String id) {
		if (src instanceof InputStream) {
			return initParser((InputStream) src, null);
		}
		InputStream in;
		try {
			Object obj = src;
			if (obj instanceof String) {
				String s = (String) obj;
				try {
					obj = SUtils.getExtendedURL(s);
				} catch (Exception ex) {
					File f = new File(s);
					obj = f;
					if (!f.exists()) {
						obj = new StringReader(s);
					}
				}
			}
			if (obj instanceof URL) {
				URL u = (URL) obj;
				in = u.openStream();
				return initParser(in, id == null ? u.toExternalForm() : id);
			}
			if (obj instanceof File) {
				File f = (File) obj;
				return initParser(new FileInputStream(f), f.getCanonicalPath());
			}
			if (obj instanceof Reader) {
				XonParser jx = new XonParser((Reader) obj);
				if (id != null) {
					jx.setSysId(id);
				}
				return jx;
			}
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
		throw new SRuntimeException(SYS.SYS036, "input: " + src.getClass());
	}

	/** Parse JSON data with prepared parser.
	 * @param jx prepared parser.
	 * @return parsed object;
	 */
	private static Object parse(final XonParser jx) {
		Object result = jx.parse();
		jx.getReportWriter().checkAndThrowErrors();
		return result;
	}

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
		 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final Reader in, final String sysid) {
		return parse(getParser(in, sysid));
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final String s) throws SRuntimeException {
		return parse(getParser(s, null));
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final File f) throws SRuntimeException {
		return parse(getParser(f, null));
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in)
		throws SRuntimeException {
		return parse(getParser(in, null));
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in, final String sysid)
	 throws SRuntimeException {
		return parse(getParser(in, sysid));
	}

	/** Parse source URL to JSON.
	 * @param url source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parse(final URL url) throws SRuntimeException {
		return parse(getParser(url, null));
	}
////////////////////////////////////////////////////////////////////////////////
	private static XonParser getXONParser(final Object src, final String id) {
		XonParser xx = getParser(src, id);
		xx.setXonMode();
		return xx;
	}

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final Reader in, final String sysid) {
		return parse(getXONParser(in, sysid));
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final String s)throws SRuntimeException{
		return parse(getXONParser(s, null));
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final File f) throws SRuntimeException{
		return parse(getXONParser(f, null));
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in)
		throws SRuntimeException {
		return parse(getXONParser(in, null));
	}

	/** Parse XON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in,final String sysid)
	 throws SRuntimeException {
		return parse(getXONParser(in, sysid));
	}

	/** Parse source URL to JSON.
	 * @param url source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseXON(final URL url) throws SRuntimeException{
		return parse(getXONParser(url, null));
	}

////////////////////////////////////////////////////////////////////////////////
	/** Create JSON string from object (no indentation).
	 * @param obj JSON object.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object obj) {
		return toJsonString(obj, false);
	}

	/** Create JSON string from object. Indentation depends on argument.
	 * @param obj JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object obj, boolean indent) {
		StringBuilder sb = new StringBuilder();
		String indt = indent ? "\n" : null;
		if (obj instanceof List) {
			JsonToString.arrayToJsonString((List) obj, indt, sb);
		} else if (obj instanceof Map) {
			JsonToString.mapToJsonString((Map) obj, indt, sb);
		} else {
			return JsonToString.jvalueToString(obj);
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Compare two JSON objects.
	 * @param j1 first object with JSON data.
	 * @param j2 second object with JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static boolean jsonEqual(final Object j1, final Object j2) {
		return (j1 == null && j2 == null) ||
			(j1 != null && j2 != null && JsonCompare.equalValue(j1,j2));
	}

////////////////////////////////////////////////////////////////////////////////

	/** Convert XML element to JSON object.
	 * @param node XML element or document.
	 * @return JSON object.
	 */
	public final static Object xmlToJson(final Node node) {
		return JsonFromXml.toJson(node);
	}

	/** Convert XML document to JSON object.
	 * @param source path or string with source of XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final String source) {
		return xmlToJson(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param file file with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final File file) {
		return xmlToJson(KXmlUtils.parseXml(file).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param url URL containing XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final URL url) {
		return xmlToJson(KXmlUtils.parseXml(url).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param in InputStream with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToJson(final InputStream in) {
		return xmlToJson(KXmlUtils.parseXml(in).getDocumentElement());
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json path to JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final String json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json file with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final File json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json URL where is JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final URL json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in W3C mode.
	 * @param json Input stream where is JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final InputStream json) {
		return JsonToXml.toXmlW3C(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json JSON object.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXml(final Object json) {
		return JsonToXml.toXmlW3C(json);
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json path to JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final String json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json File with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final File json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json URL with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final URL json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json InputStream with JSON source data.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final InputStream json) {
		return JsonToXml.toXmlXD(parse(json));
	}

	/** Create XML from JSON object in X-Definition mode.
	 * @param json JSON object.
	 * @return XML element created from JSON data.
	 */
	public final static Element jsonToXmlXD(final Object json) {
		return JsonToXml.toXmlXD(json);
	}
////////////////////////////////////////////////////////////////////////////////

	/** Create string with XON object.
	 * @param x the XON object.
	 * @param indent if true the result will be indented.
	 * @return string with XCN source data.
	 */
	public static final String toXonString(final Object x,final boolean indent){
		return JsonToString.objectToXon(x, indent ? "" : null);
	}

	/** Create JSON object form XON object.
	 * @param xon  XON object
	 * @return JSON object.
	 */
	public static final Object xonToJson(Object xon) {
		return JsonToString.xobjectToJobject(xon);
	}
}