package org.xdef.sys;

import org.xdef.XDConstants;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.xml.KNamespace;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides methods for conversion from JSON to XML, conversion from XML
 * to JSON and for comparing of JSON objects.
 * @author Vaclav Trojan
 */
public class JSONUtil implements XDConstants {

	/** JSON map. */
	public static final String J_MAP = "map";
	/** JSON array. */
	public static final String J_ARRAY = "array";
	/** JSON string item. */
	public static final String J_STRING = "string";
	/** JSON number item. */
	public static final String J_NUMBER = "number";
	/** JSON boolean item. */
	public static final String J_BOOLEAN = "boolean";
	/** JSON null item. */
	public static final String J_NULL = "null";

	/** JSON any item with JSON value. */
	public static final String J_ITEM = "item";
	/** Extension of JSON map if named values are map or array. */
	public static final String J_EXTMAP = "mapItems";

	/** This field is internally used. */
	public final KNamespace _ns = new KNamespace();

	public int _n = 0;

	/** This method is internally used. */
	public final void pushContext() {
		_ns.pushContext();
		_n++;
	}

	/** This method is internally used. */
	public final void popContext() {
		_ns.popContext();
		_n--;
		if (_n < 0) {
			// Internal error&{0}{: }
			throw new SRuntimeException(SYS.SYS066,	"Namespace nesting: "+_n);
		}
	}

	/** Create instance of JSONUtil object (used only internally). */
	public JSONUtil() {}

	/** Check if argument is a hexadecimal digit. */
	private static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return i >= 16 ? i - 6 : i;
	}

////////////////////////////////////////////////////////////////////////////////
// parse JSON
////////////////////////////////////////////////////////////////////////////////

	/** JSON parser */
	private static final class JParser {
		/** parser used for parsing of source. */
		private final SParser _p;

		/** Create instance of JSON parser build with reader,
		 * @param p parser of source data,
		 */
		private JParser(final SParser p) {_p = p;}

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
				Map<String, Object> result = new TreeMap<String, Object>();
				_p.isSpaces();
				if (_p.isChar('}')) {
					return result;
				}
				for (;;) {
					Object o = readValue();
					if (o != null && o instanceof String) {
						 // parse JSON named pair
						String name = (String) o;
						_p.isSpaces();
						if (!_p.isChar(':')) {
							// ":" expected
							throw new SRuntimeException(JSON.JSON002, ":",
								genPosMod());
						}
						_p.isSpaces();
						result.put(name, readValue());
						_p.isSpaces();
						if (_p.isChar('}')) {
							_p.isSpaces();
							return result;
						}
						if (_p.isChar(',')) {
							_p.isSpaces();
						}
					} else {
						// String with name of item expected
						throw new SRuntimeException(JSON.JSON004, genPosMod());
					}
				}
			} else if (_p.isChar('[')) {
				List<Object> result = new ArrayList<Object>();
				_p.isSpaces();
				if (_p.isChar(']')) {
					return result;
				}
				for(;;) {
					result.add(readValue());
					_p.isSpaces();
					if (_p.isChar(']')) {
						return result;
					}
					if (_p.isChar(',')) {
						_p.isSpaces();
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
			_p.isSpaces();
			char c = _p.getCurrentChar();
			if (c != '{' && c != '[' ) {
				// JSON object or array expected"
				throw new SRuntimeException(JSON.JSON009, genPosMod());
			}
			Object result = readValue();
			_p.isSpaces();
			if (!_p.eos()) {
				//Text after JSON not allowed
				throw new SRuntimeException(JSON.JSON008, genPosMod());
			}
			return result;
		}
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

	/** Parse JSON document with StringParser.
	 * @param parser StringParser with input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parseJSON(StringParser parser)
		throws SRuntimeException{
		return new JParser(parser).parse();
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
			return new JParser(p).parse();
		} catch (Exception ex) { // never happens
			ex.printStackTrace(System.err);
			return null;
		}
	}

	/** Parse source URL to JSON.
	 * @param in source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public static final Object parseJSON(final URL in) throws SRuntimeException {
		try {
			URLConnection u = in.openConnection();
			InputStream is = u.getInputStream();
			try {
				return parseJSON(is, in.toExternalForm());
			} finally {
				is.close();
			}
		} catch (IOException ex) {
			//URL &{0} error: &{1}{; }
			throw new SRuntimeException(SYS.SYS076,in.toExternalForm(), ex);
		}
	}

	/** Parse source data from reader to JSON.
	 * @param in reader with source data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public static final Object parseJSON(final Reader in)
		throws SRuntimeException {
		return parseJSON(in, null);
	}

	/** Parse source data from reader to JSON.
	 * @param in reader with source data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public static final Object parseJSON(final Reader in,
		final String sysid) throws SRuntimeException {
		StringParser parser = new StringParser(in, null);
		if (sysid != null && !sysid.isEmpty()) {
			parser.setSysId(sysid);
		}
		return parseJSON(parser);
	}

////////////////////////////////////////////////////////////////////////////////
// JSON to XML
////////////////////////////////////////////////////////////////////////////////

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

	/** Get Document from argument. If argument is null then create Document.
	 * @param n node.
	 * @return Document object.
	 */
	private static Document getDoc(final Node n) {
		return (n instanceof Document) ? (Document) n : n.getOwnerDocument();
	}

	/** Get XML name without colon, replace it with "_u3a_".
	 * @param s raw XML name.
	 * @return name with colon replaced by "_u3a_".
	 */
	private static String replaceColonInName(final String s) {
		int i = s.indexOf(':');
		return i >= 0 ? s.substring(0, i) + "_u3a_" + s.substring(i + 1) : s;
	}

	/** Get prefix of XML name.
	 * @param s XML name.
	 * @return prefix of XML name.
	 */
	private static String getNamePrefix(final String s) {
		int i = s.indexOf(':');
		return (i >= 0) ? s.substring(0, i) : "";
	}

	/** Create and append new element and push context.
	 * @param n node to which new element will be appended.
	 * @param namespace name space URI.
	 * @param tagname tag name of element.
	 * @return created element.
	 */
	public final Element appendElem(final Node n,
		final String namespace,
		final String tagname) {
		pushContext();
		String u;
		String prefix = getNamePrefix(tagname);
		u = namespace == null ?	_ns.getNamespaceURI(prefix) : namespace;
		Document doc = getDoc(n);
		Element e = u != null ? doc.createElementNS(u, tagname)
			: doc.createElement(replaceColonInName(tagname));
		if (u != null && _ns.getNamespaceURI(prefix) == null) {
			_ns.setPrefix(prefix, u);
			e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				prefix.length() > 0 ? "xmlns:" + prefix : "xmlns", u);
		}
		n.appendChild(e);
		return e;
	}

	/** Append to node the element with JSON name space.
	 * @param n node where to append new element.
	 * @param name local name of element.
	 * @return created element.
	 */
	public Element appendJSONElem(final Node n, final String name) {
		return appendElem(n, JSON_NS_URI, JSON_NS_PREFIX + ":" + name);
	}

	/** Append text with a value to element.
	 * @param e element where to add value.
	 * @param val value to be added as text.
	 */
	private void addValueAsText(final Element e, final Object val) {
		String s = genSimpleValueToXML(val);
		e.appendChild(getDoc(e).createTextNode(s));
	}

	/** Create child element with text value to node.
	 * @param node node where to create element.
	 * @param val value which will be represented as value of created element.
	 */
	private void addValue(final Node node, final Object val) {
		String name;
		if (val == null) {
			name = J_NULL;
		} else if (val instanceof String) {
			name = J_STRING;
		} else if (val instanceof Number) {
			name = J_NUMBER;
		} else if (val instanceof Boolean) {
			name = J_BOOLEAN;
		} else {
			throw new RuntimeException("Unknown object: " + val);
		}
		Element e = appendJSONElem(node, name);
		if (val != null) {
			addValueAsText(e, val);
		}
		popContext();
	}

	/** Append array of JSON values to node.
	 * @param list list with array of values.
	 * @param parent node where to append array.
	 */
	private void listToXml(final List<?> list, final Node parent) {
		Element e = appendJSONElem(parent, J_ARRAY);
		for (Object x: list) {
			if (x == null) {
				addValue(e, null);
			} else if (x instanceof Map) {
				mapToXml((Map) x, e);
			} else if (x instanceof List) {
				listToXml((List)x,  e);
			} else {
				addValue(e, x);
			}
		}
		popContext();
	}

	/** Check if the value may be represented as unquoted string.
	 * @param val Object to be tested.
	 * @return true the value may be represented as unquoted string.
	 */
	public static final boolean isSimpleValue(final Object val) {
		if (val == null || "".equals(val)
			|| val instanceof Number || val instanceof Boolean) {
			return true;
		}
		if (val instanceof String) {
			String s = (String) val;
			return !"null".equals(s) && !"true".equals(s) && !"false".equals(s)
				&& s.indexOf('\t') < 0 && s.indexOf('\n') < 0
				&& s.indexOf('\r') < 0 && s.indexOf('\f') < 0
				&& s.indexOf('\"') < 0 && s.indexOf('\\') < 0;
		}
		return false;
	}

	/** Generate  value to XML string,
	 * @param val Object with value.
	 * @return string representing the value.
	 */
	public static final String genSimpleValueToXML(final Object val) {
		if (val == null) {
			return "null";
		} else if (val instanceof String) {
			String s = (String) val;
			if (s.length() == 0 ||
				"null".equals(s) || "true".equals(s) || "false".equals(s)) {
				return "\"" + s + "\"";
			} else {
				boolean addQuot = s.indexOf(' ') > 0 || s.indexOf('\t') > 0
					|| s.indexOf('\n') > 0 || s.indexOf('\r') > 0
					|| s.indexOf('\f') > 0 || s.indexOf('\b') > 0;
				if (!addQuot) {
					char ch = s.charAt(0);
					if (ch == '-' || ch >= '0' && ch <= '9') {
						StringParser p = new StringParser(s);
						p.isChar('-');
						addQuot |= p.isFloat() || p.isInteger();
						if (!addQuot) return s;
					}
				}
				return addQuot ? '\"' + s + '\"' : s;
			}
		} else {
			return val.toString();
		}
	}

	/** Set attribute to element,
	 * @param e element where to set attribute.
	 * @param namespace namespace URI of attribute.
	 * @param name name of attribute {if there is colon and namespace is null
	 * then replace colon with "_u3a_"}
	 * @param val string with value of attribute.
	 */
	public static final void setAttr(final Element e,
		final String namespace,
		final String name,
		final String val) {
		if (namespace == null) {
			e.setAttribute(replaceColonInName(name), val);
		} else {
			e.setAttributeNS(namespace, name, val);
		}
	}

	/** Create named item,
	 * @param rawName raw name (JSON string).
	 * @param val Object to be created as named item.
	 * @param parent parent node where the item will be appended.
	 * @return created element.
	 */
	private Element namedItemToXml(final String rawName,
		final Object val,
		final Node parent) {
		String name = toXmlName(rawName);
		String namespace = _ns.getNamespaceURI(getNamePrefix(name));
		if (val == null) {
			return appendElem(parent, namespace, name);
		} else if (val instanceof Map) {
			Map<?, ?> map = (Map) val;
			if (map.isEmpty()) {
				Element e = appendElem(parent, namespace, name);
				appendJSONElem(e, J_MAP);
				popContext(); // appended element js:map
				return e;
			}
			String u = getNamePrefix(name);
			u = u.length() > 0 ? "xmlns:" + u : "xmlns";
			u = (String) map.get(u);
			if (u == null) {
				u = namespace;
			}
			Element e = appendElem(parent, u, name);
			for (Object key: map.keySet()) { // xmlns elements
				String name1 = (String) key;
				Object o = map.get(key);
				if (o == null || o instanceof String) {
					name1 = toXmlName(name1);
					String s = o == null ? "" : o.toString();
					if ("xmlns".equals(name1)) {
						e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							"xmlns", s);
						_ns.setPrefix("", s);
					} else if (name1.startsWith("xmlns:")) {
						e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							name1, s);
						_ns.setPrefix(name1.substring(6), s);
					}
				}
			}
			Element ee = null;
			for (Object key: map.keySet()) {
				String name1 = toXmlName(key.toString());
				Object o = map.get(key);
				if (o != null && (o instanceof Map || o instanceof List)) {
					if (ee == null) {
						ee = appendJSONElem(e, J_EXTMAP);
					}
					namedItemToXml(name1, o, ee);
					popContext();
				} else {
					if (!e.hasAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
						name1)) {
						String uri1 = _ns.getNamespaceURI(getNamePrefix(name1));
						if (isSimpleValue(o) ) { // we set it as attribute
							String s;
							if (o == null) {
								s = "null";
							} else if (o instanceof String) {
								s = (String) o;
								StringParser p = new StringParser(s);
								if ((p.isToken("null") || p.isToken("true")
									|| p.isToken("false") || p.isSignedFloat()
									|| p.isSignedInteger()) && p.eos()) {
									s = genSimpleValueToXML(o);
								}
							} else {
								s = genSimpleValueToXML(o);
							}
							setAttr(e, uri1, name1, s);
						} else {
							if (map.size() == 1) {
								if (o == null) {
									setAttr(e, uri1, name1, "null");
								} else { // string
									String s = (String) o;
									if ("".equals(s)) {
										setAttr(e, uri1, name1, "");
									} else {
										s = genSimpleValueToXML(s);
										if (s.charAt(0) == '"') {
											// we set it as named item
											if (ee == null) {
												ee = appendJSONElem(e,J_EXTMAP);
											}
											namedItemToXml(name1, s, ee);
											popContext();
										} else { // just attribute
											setAttr(e, uri1, name1, s);
										}
									}
								}
								break;
							}
							if (ee == null) {
								ee = appendJSONElem(e, J_EXTMAP);
							}
							namedItemToXml(name1, o, ee);
							popContext();
						}
					}
				}
			}
			if (ee != null) {
				popContext();
			}
			return e;
		} else if (val instanceof List) {
			Element e = appendElem(parent, namespace, name);
			listToXml((List) val, e);
			return e;
		} else {
			Element e = appendElem(parent, namespace, name);
			addValueAsText(e, val);
			return e;
		}
	}

	/** Append map with JSON tuples to node.
	 * @param map map with JSON tuples.
	 * @param parent node where to append map.
	 */
	private void mapToXml(final Map<?, ?> map, final Node parent) {
		int size = map.size();
		if (size == 0) {
			appendJSONElem(parent, J_MAP);
		} else if (size == 1) {
			String key = (String) map.keySet().iterator().next();
			namedItemToXml(key, map.get(key), parent);
		} else {
			Element el = appendJSONElem(parent, J_MAP);
			for (Object key: map.keySet()) {
				namedItemToXml(key.toString(), map.get(key), el);
				popContext();
			}
		}
		popContext();
	}

	/** Create child nodes with JSON object to node.
	 * @param json JSON object.
	 * @param parent where to create child nodes.
	 */
	private void json2xml(final Object json, final Node parent) {
		if (json != null) {
			if (json instanceof Map) {
				mapToXml((Map) json, parent);
				return;
			}
			if (json instanceof List) {
				listToXml((List) json, parent);
				return;
			}
		}
		throw new SRuntimeException(JSON.JSON011, json); //Not JSON object&{0}
	}

	/** Convert object with JSON data to XML element.
	 * @param json object with JSON data.
	 * @return XML element.
	 */
	public final static Element jsonToXml(final Object json) {
		try {
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = df.newDocumentBuilder();
			Document doc = db.newDocument();
			JSONUtil jsp = new JSONUtil();
			jsp.json2xml(json, doc);
			if (jsp._n != 0) {
				// Internal error&{0}{: }
				throw new SRuntimeException(SYS.SYS066,
					"Namespace nesting: "+jsp._n);
			}
			return doc.getDocumentElement();
		} catch (ParserConfigurationException ex) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// XML to JSON
////////////////////////////////////////////////////////////////////////////////

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

	public final static Object getValue(final String s) {
		if (s.length() == 0) {
			return "";
		}
		if ("null".equals(s)) {
			return null;
		} else if ("true".equals(s)) {
			return Boolean.TRUE;
		} else if ("false".equals(s)) {
			return Boolean.FALSE;
		}
		StringParser p = new StringParser(s);
		if (p.isSignedFloat()) {
			return new BigDecimal(s);
		} else if (p.isSignedInteger()) {
			return new BigInteger(s);
		}
		String t = s.charAt(0) == '"' ? s.substring(1, s.length()-1) : s;
		t = SUtils.modifyString(t, "\\\\", "\\");
		return t;
	}

	private static Map<String, Object> createAttrs(final Element xml) {
		if (xml.hasAttributes()) {
			Map<String, Object> attrs = new TreeMap<String, Object>();
			NamedNodeMap nnm = xml.getAttributes();
			for (int i = nnm.getLength() - 1; i >= 0; i--) {
				Node attr = nnm.item(i);
				String key = toJsonName(attr.getNodeName());
				Object value = getValue(attr.getNodeValue());
				attrs.put(key, value);
			}
			return attrs;
		}
		return null;
	}

	private static void valuesToList(final List<Object> list, final String s) {
		StringParser p = new StringParser(s.trim());
		while ((p.isSpaces() || true) && !p.eos()) {
			if (p.isToken("null")) {
				list.add(null);
			} else if (p.isToken("false")) {
				list.add(Boolean.FALSE);
			} else if (p.isToken("true")) {
				list.add(Boolean.TRUE);
			} else if (p.isSignedFloat()) {
				list.add(new BigDecimal(p.getParsedString()));
			} else if (p.isSignedInteger()) {
				list.add(new BigInteger(p.getParsedString()));
			} else {
				if (p.isChar('"')) { // quoted string
					StringBuilder sb = new StringBuilder();
					for (;;) {
						if (p.eos()) {
							throw new RuntimeException("Unclosed string");
						}
						if (p.isToken("\"\"")) {
							sb.append('"');
						} else if (p.isChar('"')) {
							list.add(sb.toString());
							break;
						} else {
							sb.append(p.peekChar());
						}
					}
				} else { //not quoed string
					int pos = p.getIndex();
					while (!p.isSpace() && !p.eos()) {
						p.nextChar();
					}
					list.add(p.getParsedBufferPartFrom(pos).trim());
				}
			}
		}
	}

	private static List<Object> createList(final NodeList nl) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.TEXT_NODE
				|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
				String s = n.getNodeValue();
				if (s == null) {
					s = "";
				}
				while (i+1 < nl.getLength()
					&& ((n = nl.item(i+1)).getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE)) {
					s += n.getNodeValue();
					i++;
				}
				valuesToList(list, s);
			} else {
				list.add(createItem(n));
			}
		}
		return list;
	}

	private static Map<?,?> createMap(final NodeList nl) {
		Map<String, Object> map = new TreeMap<String, Object>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String key = toJsonName(n.getNodeName());
				List<Object> list = createList(n.getChildNodes());
				NamedNodeMap nnm = n.getAttributes();
				if (nnm != null && nnm.getLength() > 0) {
					Map<String, Object> attrs = new TreeMap<String, Object>();
					for (int j = 0; j < nnm.getLength(); j++) {
						Node att = nnm.item(j);
						attrs.put(toJsonName(att.getNodeName()),
							getValue(att.getNodeValue()));
					}
					list.add(0, attrs);
				}
				map.put(key, list.size() == 1 ? list.get(0) : list);
			} else {
				System.err.println("ERROR: " + n);
			}
		}
		return map;
	}

	private static Map<String, Object> namedItems(final Node n,
		final Map<String, Object> attrs) {
		Map<String, Object> atrs =
			attrs == null ? new TreeMap<String, Object>() : attrs;
		NodeList nl = n.getChildNodes();
		if (nl.getLength() > 0) {
			for (int j = 0; j < nl.getLength(); j++) {
				Object o = createItem(nl.item(j));
				if (o instanceof Map) {
					Object k = ((Map) o).keySet().iterator().next();
					atrs.put(k.toString(), ((Map) o).get(k));
				} else if (o instanceof List && ((List) o).size() >= 1 &&
					((List) o).get(0) instanceof Map) {
					Iterator<?> x =
						((Map)((List)o).get(0)).entrySet().iterator();
					while (x.hasNext()) {
						Map.Entry<?, ?> y = (Map.Entry) x.next();
						List<?> list = (List) o;
						if (list.size() == 1) {
							atrs.put(y.getKey().toString(), y.getValue());
						} else {
							List<Object> z= new ArrayList<Object>();
							for (int k = 1; k <  list.size(); k++) {
								z.add(list.get(k));
							}
							atrs.put(y.getKey().toString(), z);
						}
					}
				} else {
					atrs.put(toJsonName(nl.item(j).getNodeName()), o);
				}
			}
		}
		return atrs;
	}

	private static Object createItem(final Node n) {
		switch (n.getNodeType()) {
			case Node.ELEMENT_NODE: {
				if (JSON_NS_URI.equals(n.getNamespaceURI())) {
					String name = n.getLocalName();
					if (J_ARRAY.equals(name)) {
						return createList(n.getChildNodes());
					}
					if (J_MAP.equals(name)) {
						return createMap(n.getChildNodes());
					}
					if (J_EXTMAP.equals(name)) {
						return namedItems(n, null);
					}
					if (J_NULL.equals(name)) {
						return null;
					} else if (J_STRING.equals(name)
						|| J_NUMBER.equals(name)
						|| J_BOOLEAN.equals(name)) {
						String s = ((Element) n).getTextContent();
						return getValue(s);
					}
					throw new RuntimeException(
						"Incorrect node: " + n.getNodeName());
				} else {
					return xmlToJson((Element) n);
				}
			}
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				List<Object> list = new ArrayList<Object>();
				String s = n.getNodeValue();
				if (s == null) {
					s = "";
				}
				valuesToList(list, s);
				if (list.size() == 1) {
					return list.get(0);
				}
				return list;
		}
		return "?";
	}

	/** Convert XML element to object with JSON data.
	 * @param xml XML element.
	 * @return object with JSON data.
	 */
	public static final Object xmlToJson(final Element xml) {
		NodeList nl = xml.getChildNodes();
		if (JSON_NS_URI.equals(xml.getNamespaceURI())) {
			if (J_ARRAY.equals(xml.getLocalName())) {
				return createList(nl);
			}
			if (J_MAP.equals(xml.getLocalName())) {
				return createMap(nl);
			}
			//Incorrect XML node name with JSON namespace &{0}{: }
			throw new SRuntimeException(JSON.JSON013, xml.getNodeName());
		}
		Map<String, Object> map = new TreeMap<String, Object>();
		String key = toJsonName(xml.getNodeName());
		if (xml.getChildNodes().getLength() == 0) {
			map.put(key, createAttrs(xml));
			return map;
		} else {
			Map<String, Object> attrs = createAttrs(xml);
			int i = 0;
			int len = nl.getLength();
			if (len >= 1) {
				Node n = nl.item(0);
				if (JSON_NS_URI.equals(n.getNamespaceURI())
					&& J_EXTMAP.equals(n.getLocalName())) {
					i = 1;
					NodeList nl1 = n.getChildNodes();
					if (nl1.getLength() > 0) {
						if (attrs == null) {
							attrs = new TreeMap<String, Object>();
						}
						for (int j = 0; j < nl1.getLength(); j++) {
							Object o = createItem(nl1.item(j));
							if (o instanceof Map) {
								Map<?,?> m = (Map) o;
								Object k = m.keySet().iterator().next();
								attrs.put(k.toString(), m.get(k));
							} else if (o instanceof List &&
								((List) o).size() >= 1 &&
								((List) o).get(0) instanceof Map) {
								Iterator<?> x = ((Map)((List)o).get(0))
									.entrySet().iterator();
								while (x.hasNext()) {
									Map.Entry<?, ?> y = (Map.Entry) x.next();
									List<?> list = (List) o;
									if (list.size() == 1) {
										attrs.put(y.getKey().toString(),
											y.getValue());
									} else {
										List<Object> z= new ArrayList<Object>();
										for (int k = 1; k <  list.size(); k++) {
											z.add(list.get(k));
										}
										attrs.put(y.getKey().toString(), z);
									}
								}
							}
						}
					}
				} else if (attrs == null && len == 1) {
					map.put(key, createItem(nl.item(0)));
					return map;
				}
			}
			if (len == 1 && i == 1 &&
				!JSON_NS_URI.equals(xml.getNamespaceURI()) &&
				!J_ARRAY.equals(xml.getLocalName())) {
				// no js:array and no child nodes (just an element)
				map.put(key, attrs);
				return map;
			}
			Object result;
			List<Object> list = new ArrayList<Object>();
			if (attrs == null || attrs.isEmpty()) {
				// no attributes: we set list to value and return map
				map.put(key, list);
				result = map;
			} else {
				// attributes exists: we set return list with map
				map.put(key, attrs);
				list.add(map);
				result = list;
			}
			for (; i < len; i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
					String s = n.getNodeValue();
					if (s == null) {
						s = "";
					}
					while (i+1 < nl.getLength()
						&& ((n = nl.item(i+1)).getNodeType() == Node.TEXT_NODE
						|| n.getNodeType() == Node.CDATA_SECTION_NODE)) {
						s += n.getNodeValue();
						i++;
					}
					valuesToList(list, s);
				} else {
					list.add(createItem(n));
				}
			}
			return result;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// JSON to string
////////////////////////////////////////////////////////////////////////////////

	private static void objToJSONString(final Object obj,
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
			listToJSONString(x, indent, sb);
		} else if (obj instanceof Map) {
			Map<?,?> x = (Map) obj;
			mapToJSONString(x, indent, sb);
		} else {
			throw new SRuntimeException(JSON.JSON011, obj);//Not JSON object&{0}
		}
	}

	private static void listToJSONString (final List<?> list,
		final String indent,
		final StringBuilder sb) {
		sb.append('[');
		if (list.isEmpty()) {
			sb.append(']');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Object o: list) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			if (ind != null) {
				sb.append(ind);
			}
			objToJSONString(o, ind, sb);
		}
		if (indent != null) {
			sb.append(indent);
		}
		sb.append(']');
	}

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
			objToJSONString(e.getKey(), ind, sb);
			sb.append(':');
			objToJSONString(e.getValue(), ind, sb);
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

	/** Create JSON string from object. Indentation depends on
	 argument.
	 * @param obj JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	public final static String toJSONString(final Object obj, boolean indent) {
		StringBuilder sb = new StringBuilder();
		String ind = indent ? "\n" : null;
		if (obj instanceof List) {
			listToJSONString((List) obj, ind, sb);
		} else if (obj instanceof Map) {
			mapToJSONString((Map) obj, ind, sb);
		} else {
			//Not JSON object &{0}{: }
			throw new SRuntimeException(JSON.JSON011, obj);
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON objects.
////////////////////////////////////////////////////////////////////////////////

	private static boolean equalList(final List<?> l1, final List<?> l2) {
		if (l1.size() == l2.size()) {
			for (int i = 0; i < l1.size(); i++) {
				if (!equalValue(l1.get(i), l2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

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

	private static boolean equalValue(final Object o1, final Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else if (o2 == null) {
			return false;
		}
		if (o1 instanceof Map) {
			return o2 instanceof Map ? equalMap((Map)o1, (Map)o2) : false;
		}
		if (o1 instanceof List) {
			return o2 instanceof List ? equalList((List) o1, (List) o2) : false;
		}
		if (o1 instanceof String) {
			return ((String) o1).equals(o2);
		}
		if (o1 instanceof Number) {
			if (o1 instanceof BigDecimal && o2 instanceof BigInteger) {
				BigInteger i = (BigInteger) o2;
				return o1.equals(new BigDecimal(i));
			} else if (o1 instanceof BigInteger && o2 instanceof BigDecimal) {
				BigInteger i = (BigInteger) o1;
				return new BigDecimal(i).equals(o2);
			} else {
				return ((Number) o1).equals(o2);
			}
		}
		if (o1 instanceof Boolean) {
			return ((Boolean) o1).equals(o2);
		}
		// Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			o1.getClass().getName(), o2.getClass().getName());
	}

	/** Compare two JSON objects.
	 * @param j1 first object with JSON data.
	 * @param j2 second object with JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public static final boolean jsonEqual(final Object j1, final Object j2) {
		if (j1 != null && j2 != null
			&& (j1 instanceof Map || j1 instanceof List)
			&& (j2 instanceof Map || j2 instanceof List)) {
			return equalValue(j1, j2);
		}
		// Uncomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			j1 == null ? "null" : j1.getClass().getName(),
			j2 == null ? "null" : j2.getClass().getName());
	}
}