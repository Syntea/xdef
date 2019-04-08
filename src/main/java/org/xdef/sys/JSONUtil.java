package org.xdef.sys;

import org.xdef.XDConstants;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.msg.XDEF;
import org.xdef.xml.KXmlUtils;

/** Provides tools for conversion of JSON to XML, conversion of XML
 * to JSON and comparing of JSON objects.
 * @author Vaclav Trojan
 */
public class JSONUtil implements XDConstants {

	private JSONUtil(String jsprefix) {
		if (jsprefix == null) {
			_jsprefix = "js";
		} else {
			_jsprefix = jsprefix;
		}
	}

	/** Create instance of JSON parser build with reader,
	 * @param p parser of source data,
	 */
	private JSONUtil(final SParser p) {_p = p;}

	/** Prefix of JSON namespace URI. */
	private String _jsprefix = "js";
	/** Switch to allow comments in JSON source. */
	private boolean _allowComments = false;
//	/** Switch to save source positions of JSOB objects. */
//	private boolean _setPositions = false;

	/** Internal parser. */
	private SParser _p;

	/** Set switch to allow comments in JSOU source.
	 * @param x if true the comments are allowed.
	 */
	public void allowComments(boolean x) {_allowComments = x;}
//
//	/** Set switch to save JSOU source positions of JSOB objects.
//	 * @param x if true the source positions are saved.
//	 */
//	public void setPositions(boolean x) {_setPositions = x;}

	/** Check if argument is a hexadecimal digit. */
	private static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return i >= 16 ? i - 6 : i;
	}

	/** Convert character to representation used in XML names. */
	private static String toXmlChar(final char c) {
		return "_u" + Integer.toHexString(c) + '_';
	}

	/** Check if on position in the string given by index if is JSON special
	 * char representation in XML format.
	 * @param s inspected string.
	 * @param index index where to start inspection.
	 * @return true if on
	 */
	private static boolean isJChar(final String s, final int index) {
		if (index + 3 > s.length() ||
			s.charAt(index) != '_' || s.charAt(index+1) != 'u' ||
			hexDigit(s.charAt(index+2)) < 0) {
			return false;
		}
		// is hexDigit
		for (int i = index + 3; i < index + 7 && i < s.length(); i++) {
			char ch = s.charAt(i);
			if (hexDigit(ch) < 0) {
				//if not hexadecimal digit and follows '_' return true
				return ch == '_'; // if
			}
		}
		return false;
	}

	/** Get XML name created from JSOM pair name.
	 * @param s JSOM pair name.
	 * @return XML name.
	 */
	public final static String toXmlName(final String s) {
		if (s.length() == 0) {
			return "_u0_"; // empty string
		}
		StringBuilder sb = new StringBuilder();
		char ch = s.charAt(0);
		sb.append(isJChar(s, 0)
			|| StringParser.getXmlCharType(ch, (byte) 10)
				!= StringParser.XML_CHAR_NAME_START ? toXmlChar(ch) : ch);
		boolean firstcolon = true;
		for (int i = 1; i < s.length(); i++) {
			ch = s.charAt(i);
			if (isJChar(s, i)) {
				sb.append(toXmlChar(ch));
			} else if (ch == ':' && firstcolon) {
				firstcolon = false;
				sb.append(':');
			} else if (StringParser.getXmlCharType(ch, (byte) 10) >
				StringParser.XML_CHAR_COLON) {
				sb.append(ch);
			} else {
				firstcolon = false;
				sb.append(toXmlChar(ch));
			}
		}
		return sb.toString();
	}

	/** Create JSON name from XML name.
	 * @param name XML name.
	 * @return JSON name.
	 */
	private static String toJsonName(final String name) {
		if ("_u0_".equals(name)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (ch == '_' && i + 2 < name.length()) {
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

////////////////////////////////////////////////////////////////////////////////
// Parse JSON
////////////////////////////////////////////////////////////////////////////////

	/** Create modification string with source position.
	 * @return modification string with source position.
	 */
	private String genPosMod() {
		if (_p instanceof StringParser) {
			StringParser p = (StringParser) _p;
			return "&{line}" + p.getLineNumber()
				+ "&{column}" + p.getColumnNumber()
				+ "&{sysId}" + p.getSysId();
		} else {
			return null;
		}
	}

	/** Check white space or comment.
	 * @return true if and only if a white space or comment was found.
	 */
	private boolean isWhitespace() {
		boolean result = _p.isSpaces();
		if (_allowComments) {
			while (_p.isToken("/*")) {
				result = true;
				if (!_p.findToken("*/")) {
					//Unclosed comment in the script
					throw new SRuntimeException(XDEF.XDEF401);
				}
				 _p.isSpaces();
			}
		}
		return result;
	}

	/** Read JSON value.
	 * @return parsed value: List, Map, String, Number, Boolean or null.
	 * @throws SRuntimeException is an error occurs.
	 */
	private Object readValue() throws SRuntimeException {
		if (_p.eos()) {
			//unexpected eof
			throw new SRuntimeException(JSON.JSON007, genPosMod());
		}
		if (_p.isChar('{')) { // Map
			Map<String,Object> result = new LinkedHashMap<String,Object>();
			isWhitespace();
			if (_p.isChar('}')) {
				return result;
			}
			for (;;) {
				Object o = readValue();
				if (o != null && o instanceof String) {
					 // parse JSON named pair
					String name = (String) o;
					isWhitespace();
					if (!_p.isChar(':')) {
						throw new SRuntimeException(// ":" expected
							JSON.JSON002, ":", genPosMod());
					}
					isWhitespace();
					result.put(name, readValue());
					isWhitespace();
					if (_p.isChar('}')) {
						isWhitespace();
						return result;
					}
					if (_p.isChar(',')) {
						isWhitespace();
					} else {
						// JSON002="&{0}" expected&{#SYS000}
						throw new SRuntimeException(
							JSON.JSON002, ",", genPosMod());
					}
				} else {
					// String with name of item expected
					throw new SRuntimeException(JSON.JSON004, genPosMod());
				}
			}
		} else if (_p.isChar('[')) {
			List<Object> result = new ArrayList<Object>();
			isWhitespace();
			if (_p.isChar(']')) {
				return result;
			}
			for(;;) {
				result.add(readValue());
				isWhitespace();
				if (_p.isChar(']')) {
					return result;
				}
				if (_p.isChar(',')) {
					isWhitespace();
				} else {
					// JSON002="&{0}" expected&{#SYS000}
					throw new SRuntimeException(JSON.JSON002, ",", genPosMod());
				}
			}
		} else if (_p.isChar('"')) { // string
			StringBuilder sb = new StringBuilder();
			while (!_p.eos()) {
				if (_p.isChar('"')) {
					return sb.toString();
				} else if (_p.isChar('\\')) {
					char c = _p.peekChar();
					if (c == 'u') {
						int x = 0;
						for (int j = 1; j < 4; j++) {
							int y = hexDigit(_p.peekChar());
							if (y < 0) {
								// hexadecimal digit expected
								throw new SRuntimeException(JSON.JSON005,
									genPosMod());
							}
							x = (x << 4) + y;
						}
						sb.append((char) x);
					} else {
						int i = "\"\\/bfnrt".indexOf(c);
						if (i >= 0) {
							sb.append("\"\\/\b\f\n\r\t".charAt(i));
						} else {
							 // Incorrect control character in string
							throw new SRuntimeException(JSON.JSON006,
								genPosMod());
						}
					}
				} else {
					sb.append(_p.peekChar());
				}
			}
			// end of string ('"') is missing
			throw new SRuntimeException(JSON.JSON001, genPosMod());
		} else if (_p.isToken("null")) {
			return null;
		} else if (_p.isToken("true")) {
			return Boolean.TRUE;
		} else if (_p.isToken("false")) {
			return Boolean.FALSE;
		} else {
			boolean minus = _p.isChar('-');
			int pos = _p.getIndex();
			Number n;
			String s;
			if (_p.isFloat()) {
				s = _p.getBufferPart(pos, _p.getIndex());
				n = new BigDecimal((minus ? "-" : "") + s);
			} else if (_p.isInteger()) {
				s = _p.getBufferPart(pos, _p.getIndex());
				n = new BigInteger((minus ? "-" : "") + s);
			} else {
				if (minus) {
					// number expected
					throw new SRuntimeException(JSON.JSON003, genPosMod());
				} else {
					//JSON value expected
					throw new SRuntimeException(JSON.JSON010, genPosMod());
				}
			}
			if (s.charAt(0) == '0' && s.length() > 1 &&
				Character.isDigit(s.charAt(1))) {
					// Illegal leading zero in number
					throw new SRuntimeException(JSON.JSON014, genPosMod());
			}
			return n;
		}
	}

	/** Parse source data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	private Object parse() throws SRuntimeException {
		isWhitespace();
		char c = _p.getCurrentChar();
		if (c != '{' && c != '[' ) {
			// JSON object or array expected"
			throw new SRuntimeException(JSON.JSON009, genPosMod());
		}
		Object result = readValue();
		isWhitespace();
		if (!_p.eos()) {
			//Text after JSON not allowed
			throw new SRuntimeException(JSON.JSON008, genPosMod());
		}
		return result;
	}

	/** Parse JSON document with StringParser.
	 * @param parser StringParser with input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parseJSON(StringParser parser)
		throws SRuntimeException{
		return new JSONUtil(parser).parse();
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param source file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parseJSON(final String source)
		throws SRuntimeException{
		if (source.charAt(0) == '{' || source.charAt(0) == '[') {
			return parseJSON(new StringParser(source));
		}
		InputStream in;
		try {
			URL url = new URL(URLDecoder.decode(source,
				System.getProperties().getProperty("file.encoding")));
			in = url.openStream();
		} catch (Exception ex) {
			try {
				in = new FileInputStream(
					FUtils.checkFile(new File(source), false));
			} catch (Exception x) {
				/*IO error detected on &{0}&{1}{, reason: }*/
				throw new SRuntimeException(SYS.SYS034, source, x);
			}
		}
		Object result = parseJSON(in);
		try {
			in.close();
		} catch (IOException x) {
			/*IO error detected on &{0}&{1}{, reason: }*/
			throw new SRuntimeException(SYS.SYS034, source, x);
		}
		return result;
	}

	/** Parse JSON document from input source data.
	 * The input data are in InputStream. The input stream is instanced as
	 * character reader reading data from UTF-8 character set.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parseJSON(final InputStream in)
		throws SRuntimeException {
		return parseJSON(in, null);
	}

	/** Parse JSON document from input source data.
	 * The input data are in InputStream. The input stream is instanced as
	 * character reader reading data from UTF-8 character set.
	 * @param in input data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parseJSON(final InputStream in,
		final String sysid) throws SRuntimeException{
		try {
			int i = in.read(); //1st byte from input stream
			int j = in.read(); //2nd byte from input stream
			if (j < 0) {//EOF
				// JSON object or array expected"
				throw new SRuntimeException(JSON.JSON009, "&{line}1&{column}1");
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
					// JSON object or array expected"
					throw new SRuntimeException(JSON.JSON009,
						"&{line}1&{column}1");
				}
				if (i == 0 && j == 0 && k == 0) {
					if (l == 0) {//EOF
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
			StringParser p = new StringParser();
			p.setSourceReader(reader, 0L, s);
			if (sysid != null && !sysid.isEmpty()) {
				p.setSysId(sysid);
			}
			return new JSONUtil(p).parse();
		} catch (Exception ex) { // never happens
			ex.printStackTrace(System.err);
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// JSON to string
////////////////////////////////////////////////////////////////////////////////

	/** Create source form of JSON value.
	 * @param obj JSON value.
	 * @param indent indentation.
	 * @param sb where the source form is recorded.
	 */
	private static void valueToJSONString(final Object obj,
		final String indent,
		final StringBuilder sb) {
		if (obj == null) {
			sb.append("null");
		} else if (obj instanceof String) {
			String s = (String) obj;
			sb.append('"');
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (Character.isDefined(c)) {
					switch (c) {
						case '\n':
							sb.append("\\n");
							break;
						case '\r':
							sb.append("\\r");
							break;
						case '\b':
							sb.append("\\b");
							break;
						case '\t':
							sb.append("\\t");
							break;
						case '\f':
							sb.append("\\f");
							break;
						case '\\':
							sb.append("\\\\");
							break;
						case '"':
							sb.append("\\\"");
							break;
						default:
							sb.append(c);
					}
				} else {
					sb.append("\\u");
					int x = c;
					for (int j=12; j >= 0; j-= 4) {
						sb.append("0123456789ABCDEF".charAt((x >> j) & 15));
					}
				}
			}
			sb.append('"');
		} else if (obj instanceof Boolean || obj instanceof Number) {
			sb.append(obj.toString());
		} else if (obj instanceof List) {
			List<?> x = (List) obj;
			arrayToJSONString(x, indent, sb);
		} else if (obj instanceof Map) {
			Map<?,?> x = (Map) obj;
			mapToJSONString(x, indent, sb);
		} else {
			throw new SRuntimeException(JSON.JSON011, obj);//Not JSON object&{0}
		}
	}

	/** Create source form of JSON array.
	 * @param array JSON array.
	 * @param indent indentation.
	 * @param sb where the source form is recorded.
	 */
	private static void arrayToJSONString (final List<?> array,
		final String indent,
		final StringBuilder sb) {
		sb.append('[');
		if (array.isEmpty()) {
			sb.append(']');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Object o: array) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			if (ind != null) {
				sb.append(ind);
			}
			valueToJSONString(o, ind, sb);
		}
		if (indent != null) {
			sb.append(indent);
		}
		sb.append(']');
	}

	/** Create source form of JSON map.
	 * @param map JSON map.
	 * @param indent indentation.
	 * @param sb where the source form is recorded.
	 */
	private static void mapToJSONString(final Map<?,?> map,
		final String indent,
		final StringBuilder sb) {
		sb.append('{');
		if (map.isEmpty()) {
			sb.append('}');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Map.Entry<?,?> e: map.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			if (ind != null) {
				sb.append(ind);
			}
			valueToJSONString(e.getKey(), ind, sb);
			sb.append(':');
			valueToJSONString(e.getValue(), ind, sb);
		}
		if (ind != null) {
			sb.append(indent);
		}
		sb.append('}');
	}

	/** Create JSON string from object (no indentation).
	 * @param obj JSON object.
	 * @return string with JSON source format.
	 */
	public final static String toJSONString(final Object obj) {
		return toJSONString(obj, false);
	}

	/** Create JSON string from object. Indentation depends on argument.
	 * @param obj JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	public final static String toJSONString(final Object obj, boolean indent) {
		StringBuilder sb = new StringBuilder();
		String ind = indent ? "\n" : null;
		if (obj instanceof List) {
			arrayToJSONString((List) obj, ind, sb);
		} else if (obj instanceof Map) {
			mapToJSONString((Map) obj, ind, sb);
		} else {
			//Not JSON object &{0}{: }
			throw new SRuntimeException(JSON.JSON011, obj);
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Create JSON from XML.
////////////////////////////////////////////////////////////////////////////////

	/** Get JSON value from string.
	 * @param source string with JSON simple value
	 * @return
	 */
	private static Object getValue(final String source) {
		String s;
		if (source == null || "null".equals(s = source.trim())) {
			return null;
		}
		if (s.isEmpty()) {
			return "";
		}
		if ("true".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equals(s)) {
			return Boolean.FALSE;
		}
		switch (s.charAt(0)) {
			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				try {
					if (s.indexOf('.') > 0
						|| s.indexOf('e') > 0 || s.indexOf('E') > 0) {
						return new BigDecimal(s);
					} else {
						return new BigInteger(s);
					}
				} catch (Exception ex) {
					return s;
				}
			case '"': // JSON string
				if (s.length() > 1 && s.charAt(s.length() - 1) == '"'
					&& s.charAt(s.length() - 2) != '\\') {
					s = s.substring(1, s.length() - 1);
					StringBuilder sb = new StringBuilder();
					for (int i = 0;  i < s.length(); i++) {
						char ch = s.charAt(i);
						if (ch == '\\') {
							if (++i >= s.length()) {
								return s; // error
							}
							switch (ch = s.charAt(i)) {
								case 'b':
									ch = '\b';
									break;
								case 'f':
									ch = '\f';
									break;
								case 'n':
									ch = '\n';
									break;
								case 'r':
									ch = '\r';
									break;
								case 't':
									ch = '\t';
									break;
								case '/':
									ch = '/';
									break;
								case '"':
									ch = '"';
									break;
								case 'u':
									try {
										ch = (char) Short.parseShort(
											s.substring(i+1, i+5), 16);
										i += 4;
										break;
									} catch (Exception ex) {
										return s;
									}
								default:
									return s; // error???
							}
						}
						sb.append(ch);
					}
					return sb.toString();
				}
			default: return s;
		}
	}

	/** Check if the node is a value.
	 * @param n the node to be checked.
	 * @return true if and only if the node represents a JSON value.
	 */
	private static boolean isValue(final Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			String uri = n.getNamespaceURI();
			if (uri != null) {
				return false;
			}
			NamedNodeMap attrs = n.getAttributes();
			if (attrs == null || attrs.getLength() == 0) {
				NodeList nl = n.getChildNodes();
				if (nl == null || nl.getLength() == 0) {
					return true;
				}
				if (nl.getLength() == 1
					&& nl.item(0).getNodeType() == Node.TEXT_NODE) {
					return true;
				}
			}
		}
		return false;
	}

	/** Get items from NodeList.
	 * @param nl NodeList with items.
	 * @param array the array where to save items.
	 */
	private static void getArrayItems(NodeList nl, List<Object> array) {
		int len = nl == null ? null : nl.getLength();
		if (len > 0) {
			if (len == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
				String s = nl.item(0).getNodeValue().trim();
				// ignore empty strings (assumed as indentation)
				if (!s.isEmpty()) {
					array.add(getValue(s));
				}
				return;
			}
			for (int i = 0; nl != null && i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (isValue(n)) {
					array.add(getValue(n.getNodeValue()));
				} else
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element) n;
					if (JSON_NS_URI.equals(el.getNamespaceURI())) {
						if ("array".equals(el.getLocalName())) {
							List<Object> array1 = new ArrayList<Object>();
							getArrayItems(el.getChildNodes(), array1);
							array.add(array1);
						} else if ("map".equals(el.getLocalName())) {
							Map<String, Object> map =
								new LinkedHashMap<String, Object>();
							genMapItems(el, map);
							array.add(map);
						} else if ("null".equals(el.getLocalName())) {
							array.add(null);
						} else if ("string".equals(el.getLocalName())) {
							array.add(el.getTextContent());
						} else {
							array.add(getValue(el.getTextContent()));
						}
						continue;
					}
					Map<String, Object> map =
						new LinkedHashMap<String, Object>();
					genMapItems(el, map);
					Map<String, Object> map1 =
						new LinkedHashMap<String, Object>();
					map1.put(el.getTagName(), map);
					array.add(map1);
				} else if (n.getNodeType() == Node.TEXT_NODE) {
					String s = n.getNodeValue().trim();
					if (!s.isEmpty()) {
						array.add(getValue(s));
					}
				}
			}
		}
	}

	/** Get map items from Element.
	 * @param el Element with map items.
	 * @param map the map where to save map items.
	 */
	private static void genMapItems(final Element el,
		final Map<String, Object> map) {
		NamedNodeMap attrs = el.getAttributes();
		for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
			Node n = attrs.item(i);
			if (!(n.getNodeName().startsWith("xmlns")
				&& JSON_NS_URI.equals(n.getNodeValue()))) {
				map.put(toJsonName(n.getNodeName()),
					getValue(n.getNodeValue()));
			}
		}
		NodeList nl = el.getChildNodes();
		for (int i = 0; nl != null && i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (isValue(n)) {
				map.put(toJsonName(n.getNodeName()),
					getValue(n.getTextContent()));
			} else if (n.getNodeType() == Node.ELEMENT_NODE) {
				Map<String, Object> map1 = new LinkedHashMap<String, Object>();
				genMapItems((Element) n, map1);
				map.put(toJsonName(n.getNodeName()), map1);
			}
		}
	}

	/** Create JSON object from XML element.
	 * @param el XML element
	 * @return JSON object.
	 */
	public final static Object xmlToJson(final Element el) {
		if (JSON_NS_URI.equals(el.getNamespaceURI())) {
			if ("map".equals(el.getLocalName())) {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				genMapItems(el, map);
				return map;
			} else if ("array".equals(el.getLocalName())) {
				List<Object> array = new ArrayList<Object>();
				getArrayItems(el.getChildNodes(), array);
				return array;
			}
			throw new RuntimeException("Incorrect root JSON element: "
				+ el.getLocalName());
		}
		NodeList nl = el.getChildNodes();
		int len = nl == null ? 0 : nl.getLength();
		int j = 0;
		for (int i = 0; i < len; i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				j++;
			} else if (n.getNodeType() == Node.TEXT_NODE) {
				if (!n.getNodeValue().trim().isEmpty()) {
					j++;
				}
			}
		}
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		if (j <= 1) {
			if (j == 1) {
				Map<String, Object> map1 =
					new LinkedHashMap<String, Object>();
				genMapItems(el, map1);
				map.put(toJsonName(el.getNodeName()), map1);
			} else {
				genMapItems(el, map);
			}
			return map;
		} else {
			genMapItems((Element) el.cloneNode(false), map);
			List<Object> array = new ArrayList<Object>();
			getArrayItems(el.getChildNodes(), array);
			map.put(toJsonName(el.getNodeName()), array);
			return array;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Create XML from JSON.
////////////////////////////////////////////////////////////////////////////////

	/** Create Element from JSON map item.
	 * @param xmlns map of namespaces.
	 * @param doc document from which the element will be created.
	 * @param key the name of item.
	 * @return Element created from JSON map item.
	 */
	private static Element createElement(final Map<String, String> xmlns,
		final Document doc,
		final String key){
		int ndx = key.indexOf(':');
		String uri = ndx < 0
			? xmlns.get("xmlns") : xmlns.get("xmlns:" + key.substring(0, ndx));
		return uri==null ? doc.createElement(key):doc.createElementNS(uri, key);
	}

	@SuppressWarnings("unchecked")
	/** Generate XML from a map.
	 * @param map JSON map from which an Element will be created.
	 * @param parent parent element.
	 */
	private void genJMap(final Map<String, Object> map, final Element parent) {
		Map<String, String> xmlns = new LinkedHashMap<String, String>();
		for (Map.Entry<String, Object> e: map.entrySet()) {
			if (e.getKey().startsWith("xmlns")
				&& JSON_NS_URI.equals(e.getValue())) {
				xmlns.put(e.getKey(), (String) e.getValue());
			}
		}
		Document doc = parent.getOwnerDocument();
		for (Map.Entry<String, Object> e: map.entrySet()) {
			String key = toXmlName(e.getKey());
			Object val = e.getValue();
			if (val == null) {
				if (JSON_NS_URI.equals(parent.getNamespaceURI())
					&& "map".equals(parent.getLocalName())) {
					Element el1 = createElement(xmlns, doc, key);
					el1.appendChild(doc.createTextNode("null"));
					parent.appendChild(el1);
				} else {
					parent.setAttribute(key, "null");
				}
			} else {
				if (val instanceof Map) {
					Element el1 = createElement(xmlns, doc, key);
					parent.appendChild(el1);
					genJMap((Map<String, Object>) val, el1);
				} else if (val instanceof List) {
					List<Object> array = (List<Object>) val;
					Element el1 = createElement(xmlns, doc, key);
					parent.appendChild(el1);
					if (array.isEmpty()) {
						el1.appendChild(createElement(xmlns, doc, "js:array"));
					} else {
						genArray((List<Object>) val, el1);
					}
				} else {
					if (!(key.startsWith("xmlns")&& JSON_NS_URI.equals(val))) {
						parent.setAttribute(key, val.toString());
					} else {
						if (JSON_NS_URI.equals(parent.getNamespaceURI())
							&& "map".equals(parent.getLocalName())) {
							Element el1 = createElement(xmlns, doc, key);
							el1.appendChild(doc.createTextNode("" + val));
							parent.appendChild(el1);
						} else {
							parent.setAttribute(key, "" + val);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	/** Generate XML from an JSON array.
	 * @param array JSON array which will be converted to XML.
	 * @param parent parent element.
	 */
	private void genArray(final List<Object> array, final Element parent) {
		parent.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
			"xmlns:" + _jsprefix , JSON_NS_URI);
		for (Object o: array) {
			Element el1;
			if (o == null) {
				el1 = parent.getOwnerDocument().createElementNS(
					JSON_NS_URI, _jsprefix + ":item");
				el1.setTextContent("null");
				parent.appendChild(el1);
			} else {
				if (o instanceof Map) {
					el1 = parent.getOwnerDocument().createElementNS(
						JSON_NS_URI, _jsprefix + ":map");
					genJMap((Map<String, Object>) o, el1);
					parent.appendChild(el1);
				} else if (o instanceof List) {
					el1 = parent.getOwnerDocument().createElementNS(
						JSON_NS_URI, _jsprefix + ":array");
					genArray((List<Object>) o, el1);
					parent.appendChild(el1);
				} else {
					el1 = parent.getOwnerDocument().createElementNS(
						JSON_NS_URI, _jsprefix + ":item");
					el1.setTextContent(o.toString());
					parent.appendChild(el1);
				}
			}
		}
	}

	/** Create XML element from JSON.
	 * @param xd XDDocument.
	 * @param el input data.
	 * @return XML element created from JSON.
	 */
	@SuppressWarnings("unchecked")
	private Element jsToXML(final Object json) {
		Document doc;
		if (json instanceof Map) {
			Map <String, Object> map = (Map<String, Object>) json;
			doc = KXmlUtils.newDocument(
				JSON_NS_URI, _jsprefix + ":map", null);
			genJMap(map, doc.getDocumentElement());
		} else if (json instanceof List) {
			List<Object> array = (List<Object>) json;
			doc = KXmlUtils.newDocument(
				JSON_NS_URI, _jsprefix + ":array", null);
			genArray(array, doc.getDocumentElement());
		} else {
			throw new RuntimeException("Unknown instance of JSON");
		}
		return doc.getDocumentElement();
	}

	public static final Element jsonToXml(final Object json) {
		return jsonToXml(json, null);
	}

	public static final Element jsonToXml(final Object json,
		final String prefix) {
		return new JSONUtil(prefix).jsToXML(json);
	}

////////////////////////////////////////////////////////////////////////////////
// Create JSON from XML element according to XDDocument.
////////////////////////////////////////////////////////////////////////////////
//
//	/** Create JSON from XML element according to XDDocument.
//	 * @param xd XDDocument.
//	 * @param el input data.
//	 * @return JSON object created from Element.
//	 */
//	public static Element xmlToJson(XDDocument xd, Element el) {
//		return null;
//	}


////////////////////////////////////////////////////////////////////////////////
// Create XML element from JSON according to XDDocument.
////////////////////////////////////////////////////////////////////////////////
//
//	/** Create XML element from JSON according to XDDocument.
//	 * @param xd XDDocument.
//	 * @param json json object.
//	 * @return the element in the form of JSON object.
//	 */
//	public static Element jsonToXml(XDDocument xd, Object json) {
//		return null;
//	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Check if JSON arrays from arguments are equal.
	 * @param a1 first array.
	 * @param a2 second array.
	 * @return true if and only if both arrays are equal.
	 */
	private static boolean equalArray(final List<?> a1, final List<?> a2) {
		if (a1.size() == a2.size()) {
			for (int i = 0; i < a1.size(); i++) {
				if (!equalValue(a1.get(i), a2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/** Check if JSON maps from arguments are equal.
	 * @param m1 first map.
	 * @param m2 second map.
	 * @return true if and only if both maps are equal.
	 */
	private static boolean equalMap(final Map<?,?> m1, final Map<?, ?>m2) {
		if (m1.size() == m2.size()) {
			for (Object k: m1.keySet()) {
				if (!equalValue(m1.get(k), m2.get(k))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/** Check if JSON numbers from arguments are equal.
	 * @param n1 first number.
	 * @param n2 second number.
	 * @return true if and only if both numbers are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	private static boolean equalNumber(final Number n1, final Number n2) {
		if (n1 instanceof BigDecimal) {
			if (n2 instanceof BigDecimal) {
				return n1.equals(n2);
			} else if (n2 instanceof BigInteger) {
				return n1.equals(new BigDecimal((BigInteger) n2));
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return n1.equals(new BigDecimal(n2.longValue()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				return n1.equals(new BigDecimal(n2.doubleValue()));
			}
		} else if (n1 instanceof BigInteger) {
			if (n2 instanceof BigInteger) {
				return n1.equals(n2);
			}
			if (n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return n1.equals(new BigInteger(n2.toString()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				return n1.equals(
					new BigInteger(String.valueOf(n2.longValue())));
			}
		} else if (n1 instanceof Long || n1 instanceof Integer
			|| n1 instanceof Short || n1 instanceof Byte) {
			if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return n1.longValue() == n2.longValue();
			} else if (n2 instanceof Double || n2 instanceof Float) {
				return n1.longValue() == n2.longValue();
			}
		} else if (n1 instanceof Double || n1 instanceof Float) {
			return n1.doubleValue() == n2.doubleValue();
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			n1.getClass().getName(), n2.getClass().getName());
	}

	/** Check if JSON values from arguments are equal.
	 * @param n1 first value.
	 * @param n2 second value.
	 * @return true if and only if both values are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	private static boolean equalValue(final Object v1, final Object v2) {
		if (v1 == null) {
			return v2 == null;
		} else if (v2 == null) {
			return false;
		}
		if (v1 instanceof Map) {
			return v2 instanceof Map ? equalMap((Map)v1, (Map)v2) : false;
		}
		if (v1 instanceof List) {
			return v2 instanceof List ? equalArray((List) v1, (List) v2) : false;
		}
		if (v1 instanceof String) {
			return ((String) v1).equals(v2);
		}
		if (v1 instanceof Number) {
			return (v2 instanceof Number)
				? equalNumber((Number) v1, (Number) v2) : false;
		}
		if (v1 instanceof Boolean) {
			return ((Boolean) v1).equals(v2);
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			v1.getClass().getName(), v2.getClass().getName());
	}

	/** Compare two JSON objects.
	 * @param j1 first object with JSON.
	 * @param j2 second object with JSON.
	 * @return true if and only if both objects contains equal data.
	 * @throws SRuntimeException if objects are incomparable
	 */
	public static final boolean jsonEqual(final Object j1, final Object j2) {
		if (j1 != null && j2 != null
			&& (j1 instanceof Map || j1 instanceof List)
			&& (j2 instanceof Map || j2 instanceof List)) {
			return equalValue(j1, j2);
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			j1 == null ? "null" : j1.getClass().getName(),
			j2 == null ? "null" : j2.getClass().getName());
	}

}