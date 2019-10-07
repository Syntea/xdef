package org.xdef.json;

import org.xdef.impl.compile.XJson;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.sys.SUtils;

/** JSON utility (parse JSON source to JSON instance, compare JSON instances,
 * and create string with JSON source from JSON object.
 * @author Vaclav Trojan
 */
public class JsonUtil extends StringParser {

	/** Create instance of JsonUtil. */
	JsonUtil() {}

	/** Flag to accept comments in JSON. */
	private boolean _acceptComments = false; // default value
	/** Flag to generate SPositions when parsing JSON. */
	private boolean _genJObjects = false; // default value
	/** Position of processed item.`*/
	private SPosition _sPosition;

////////////////////////////////////////////////////////////////////////////////
// JSON parser
////////////////////////////////////////////////////////////////////////////////

	/** Set genJObjects flag (and the _acceptComments flag is also set). */
	public final void setGenJObjects() {
		_genJObjects = true;
		_acceptComments = true;
	}

	/** Create modification string with source position.
	 * @return modification string with source position.
	 */
	public final String genPosMod() {
		return "&{line}" + getLineNumber()
			+ "&{column}" + getColumnNumber()
			+ "&{sysId}" + getSysId();
	}

	/** Skip white spaces and comments. */
	public final void skipBlanksAndComments() {
		isSpaces();
		if (_acceptComments) { // comments accepted
			boolean b = false;
			while(isToken("/*") || (b=isToken("//"))) {
				if (b) {
					skipToNextLine();
				} else {
					if (!findTokenAndSkip("*/")) {
						error(JSON.JSON015); //Unclosed comment
						setEos();
						return;
					}
				}
				b = false;
				isSpaces();
			}
		}
	}

	/** Check and get hexadecimal digit as integer.
	 * @param ch character with hexadecimal digit.
	 * @return hexadecimal digit as an integer number 0..15
	 * or return -1 if the argument is not hexadecimal digit.
	 */
	final static int hexDigit(final char ch) {
		int i = "0123456789abcdefABCDEF".indexOf(ch);
		return i >= 16 ? i - 6 : i;
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

	/** Read JSON value.
	 * @return parsed value: List, Map, String, Number, Boolean or null.
	 * @throws SRuntimeException is an error occurs.
	 */
	private Object readValue() throws SRuntimeException {
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
			return _genJObjects ? new XJson.JValue(_sPosition, null) : null;
		}
		if (_genJObjects) {
			_sPosition = getPosition();
		}
		if (isChar('{')) { // Map
			Map<String, Object> result;
			if (_genJObjects) {
				result = new XJson.JMap(_sPosition);
			} else {
				result = new LinkedHashMap<String,Object>();
			}
			skipBlanksAndComments();
			if (isChar('}')) {
				return result;
			}
			while(!eos()) {
				Object o = readValue();
				if (o != null && (o instanceof String ||
					(_genJObjects && o instanceof XJson.JValue)
					&& ((XJson.JValue) o).getObject() instanceof String)) {
					 // parse JSON named pair
					String name = _genJObjects ? o.toString() : (String) o;
					skipBlanksAndComments();
					if (!isChar(':')) {
						//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
						error(JSON.JSON002, ":");
					}
					skipBlanksAndComments();
					result.put(name, readValue());
					skipBlanksAndComments();
					if (isChar('}')) {
						skipBlanksAndComments();
						return result;
					}
					if (isChar(',')) {
						skipBlanksAndComments();
					} else {
						if (eos()) {
							break;
						}
						//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
						error(JSON.JSON002, ",", "}");
					}
				} else {
					// String with name of item expected
					fatal(JSON.JSON004);
					return result;
				}
			}
			//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
			fatal(JSON.JSON002, "}");
			return result;
		} else if (isChar('[')) {
			List<Object> result;
			if (_genJObjects) {
				result = new XJson.JList(_sPosition);
			} else {
				result = new ArrayList<Object>();
			}
			skipBlanksAndComments();
			if (isChar(']')) {
				return result;
			}
			while(!eos()) {
				result.add(readValue());
				skipBlanksAndComments();
				if (isChar(']')) {
					return result;
				}
				if (isChar(',')) {
					skipBlanksAndComments();
				} else {
					if (eos()) {
						break;
					}
					 //"&{0}"&{1}{ or "}{"} expected&{#SYS000}
					error(JSON.JSON002, ",", "]");
				}
			}
			 //"&{0}"&{1}{ or "}{"} expected&{#SYS000}
			error(JSON.JSON002, "]");
			return result;
		} else if (isChar('"')) { // string
			StringBuilder sb = new StringBuilder();
			while (!eos()) {
				if (isChar('"')) {
					return _genJObjects
						? new XJson.JValue(_sPosition, sb.toString())
						: sb.toString();
				} else if (isChar('\\')) {
					char c = peekChar();
					if (c == 'u') {
						int x = 0;
						for (int j = 1; j < 4; j++) {
							int y = hexDigit(peekChar());
							if (y < 0) {
								error(JSON.JSON005);//hexadecimal digit expected
								return _genJObjects
									? new XJson.JValue(_sPosition,sb.toString())
									: sb.toString();
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
							error(JSON.JSON006);
						}
					}
				} else {
					sb.append(peekChar());
				}
			}
			// end of string ('"') is missing
			fatal(JSON.JSON001);
			return _genJObjects
				? new XJson.JValue(_sPosition, sb.toString()) : sb.toString();
		} else if (isToken("null")) {
			return _genJObjects ? new XJson.JValue(_sPosition, null) : null;
		} else if (isToken("true")) {
			return _genJObjects ? new XJson.JValue(_sPosition, true) : true;
		} else if (isToken("false")) {
			return _genJObjects ? new XJson.JValue(_sPosition, false) : false;
		} else {
			boolean minus = isChar('-');
			boolean plus = !minus && isChar('+');
			Number number;
			String s;
			if (isFloat()) {
				s = getParsedString();
				number = new BigDecimal((minus ? "-" : "") + s);
			} else if (isInteger()) {
				s = getParsedString();
				number = new BigInteger((minus ? "-" : "") + s);
			} else {
				if (minus) {
					// number expected
					error(JSON.JSON003);
				} else {
					//JSON value expected
					error(JSON.JSON010);
				}
				if (plus) {
					error(JSON.JSON017, "+");//Not allowed character '&{0}'
				}
				return _genJObjects ? new XJson.JValue(_sPosition, null) : null;
			}
			if (s.charAt(0) == '0' && s.length() > 1 &&
				Character.isDigit(s.charAt(1))) {
					// Illegal leading zero in number
					warning(JSON.JSON014);
			}
			return _genJObjects ? new XJson.JValue(_sPosition,number) : number;
		}
	}

	/** Parse JSON source data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public Object parse() throws SRuntimeException {
		skipBlanksAndComments();
		char c = getCurrentChar();
		if (c != '{' && c != '[' ) {
			error(JSON.JSON009); // JSON object or array expected"
			return _genJObjects ? new XJson.JValue(_sPosition, null) : null;
		}
		Object result = readValue();
		skipBlanksAndComments();
		_sPosition = getPosition();
		if (!eos()) {
			error(JSON.JSON008, genPosMod()); //Text after JSON not allowed
		}
		return result;
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param source file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parse(final String source)
		throws SRuntimeException{
		Object result;
		if (source.charAt(0) == '{' && source.endsWith("}")
			|| source.charAt(0) == '[' && source.endsWith("]")) {
			JsonUtil jx = new JsonUtil();
			jx.setSourceBuffer(source);
			result = jx.parse();
			jx.getReportWriter().checkAndThrowErrors();
			return result;
		}
		InputStream in = null;
		String sysId = null;
		try {
			URL url = SUtils.getExtendedURL(source);
			in = url.openStream();
			sysId = url.toExternalForm();
			result = JsonUtil.parse(in, sysId);
		} catch (Exception ex) {
			try {
				result = JsonUtil.parse(new File(source));
			} catch (Exception x) {
				/*IO error detected on &{0}&{1}{, reason: }*/
				throw new SRuntimeException(SYS.SYS034, source, x);
			}
		}
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException x) {
			/*IO error detected on &{0}&{1}{, reason: }*/
			throw new SRuntimeException(SYS.SYS034, source, x);
		}
		return result;
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parse(final File f)
		throws SRuntimeException {
		try {
			FileInputStream in = new FileInputStream(f);
			return JsonUtil.parse(in, f.getCanonicalPath());
		} catch (Exception ex) {
			/*IO error detected on &{0}&{1}{, reason: }*/
			throw new SRuntimeException(SYS.SYS034, f, ex);
		}
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parse(final InputStream in)
		throws SRuntimeException {
		return JsonUtil.parse(in, null);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Object parse(final InputStream in,
		final String sysid) throws SRuntimeException{
		try {
			int i = in.read(); //1st byte from input stream
			int j = in.read(); //2nd byte from input stream
			if (j < 0) {//EOF
				// JSON object or array expected"
				throw new SRuntimeException(JSON.JSON009,
					"&{line}1&{column}1");
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
			JsonUtil jx = new JsonUtil();
			jx.setSourceReader(reader, 0L, s);
			if (sysid != null && !sysid.isEmpty()) {
				jx.setSysId(sysid);
			}
			Object result = jx.parse();
			jx.getReportWriter().checkAndThrowErrors();
			return result;
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
	public static final Object parse(final URL in) throws SRuntimeException{
		try {
			URLConnection u = in.openConnection();
			InputStream is = u.getInputStream();
			try {
				return JsonUtil.parse(is, in.toExternalForm());
			} finally {
				is.close();
			}
		} catch (IOException ex) {
			//URL &{0} error: &{1}{; }
			throw new SRuntimeException(SYS.SYS076,in.toExternalForm(), ex);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// JSON to string
////////////////////////////////////////////////////////////////////////////////

	/** Add the string created from JSON object to StringBuilder.
	 * @param obj JSON object to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	@SuppressWarnings("unchecked")
	private static void objToJsonString(final Object obj,
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
			List<Object> x = (List) obj;
			arrayToJsonString(x, indent, sb);
		} else if (obj instanceof Map) {
			Map<String, Object> x = (Map) obj;
			mapToJsonString(x, indent, sb);
		} else {
			throw new SRuntimeException(JSON.JSON011, obj);//Not JSON object&{0}
		}
	}

	/** Add the string created from JSON array to StringBuilder.
	 * @param array JSON array to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void arrayToJsonString (final List<Object> array,
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
			objToJsonString(o, ind, sb);
		}
		if (indent != null) {
			sb.append(indent);
		}
		sb.append(']');
	}

	/** Add the string created from JSON map to StringBuilder.
	 * @param map JSON map to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void mapToJsonString(final Map<String, Object> map,
		final String indent,
		final StringBuilder sb) {
		sb.append('{');
		if (map.isEmpty()) {
			sb.append('}');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Map.Entry<String, Object> e: map.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			if (ind != null) {
				sb.append(ind);
			}
			objToJsonString(e.getKey(), ind, sb);
			sb.append(':');
			objToJsonString(e.getValue(), ind, sb);
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
	public final static String toJsonString(final Object obj) {
		return JsonUtil.toJsonString(obj, false);
	}

	/** Create JSON string from object. Indentation depends on argument.
	 * @param obj JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	@SuppressWarnings("unchecked")
	public final static String toJsonString(final Object obj, boolean indent) {
		StringBuilder sb = new StringBuilder();
		String ind = indent ? "\n" : null;
		if (obj instanceof List) {
			arrayToJsonString((List) obj, ind, sb);
		} else if (obj instanceof Map) {
			mapToJsonString((Map) obj, ind, sb);
		} else {
			//Not JSON object &{0}{: }
			throw new SRuntimeException(JSON.JSON011, obj);
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Check if JSON arrays from arguments are equal.
	 * @param a1 first array.
	 * @param a2 second array.
	 * @return true if and only if both arrays are equal.
	 */
	private static boolean equalArray(final List<Object> a1,
		final List<Object> a2) {
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
	private static boolean equalMap(final Map<String, Object> m1,
		final Map<String, Object>m2) {
		if (m1.size() != m2.size()) {
			return false;
		}
		for (Object k: m1.keySet()) {
			if (!m2.containsKey(k)) {
				return false;
			}
			if (!equalValue(m1.get(k), m2.get(k))) {
				return false;
			}
		}
		return true;
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
				return ((BigDecimal) n1).compareTo(
					new BigDecimal(n2.doubleValue())) == 0;
			}
		} else if (n1 instanceof BigInteger) {
			if (n2 instanceof BigDecimal || n2 instanceof BigInteger) {
				return n1.equals(n2);
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
			} else if (n2 instanceof BigInteger) {
				return equalNumber(n2, n1);
			}
		} else if (n1 instanceof Double || n1 instanceof Float) {
			return n1.doubleValue() == n2.doubleValue();
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			n1.getClass().getName(), n2.getClass().getName());
	}

	/** Check if JSON values from arguments are equal.
	 * @param o1 first value.
	 * @param o2 second value.
	 * @return true if and only if both values are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	@SuppressWarnings("unchecked")
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
			return o2 instanceof List ? equalArray((List) o1, (List) o2) :false;
		}
		if (o1 instanceof String) {
			return ((String) o1).equals(o2);
		}
		if (o1 instanceof Number) {
			return (o2 instanceof Number)
				? equalNumber((Number) o1, (Number) o2) : false;
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

////////////////////////////////////////////////////////////////////////////////

	/** Convert XML element to JSON object.
	 * @param e XML element.
	 * @return JSON object.
	 */
	public static final Object xmlToJson(final Element e) {
		return new XmlToJson().toJson(e);
	}

	/** Convert XML document to JSON object.
	 * @param source path or string with source of XML document.
	 * @return object with JSON data.
	 */
	public static final Object xmlToJson(final String source) {
		return xmlToJson(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param file file with XML document.
	 * @return object with JSON data.
	 */
	public static final Object xmlToJson(final File file) {
		return xmlToJson(KXmlUtils.parseXml(file).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param url URL containing XML document.
	 * @return object with JSON data.
	 */
	public static final Object xmlToJson(final URL url) {
		return xmlToJson(KXmlUtils.parseXml(url).getDocumentElement());
	}

	/** Convert XML document to JSON object.
	 * @param in InputStream with XML document.
	 * @return object with JSON data.
	 */
	public static final Object xmlToJson(final InputStream in) {
		return xmlToJson(KXmlUtils.parseXml(in).getDocumentElement());
	}

	/** Create XML from JSON object according to W3C recommendation.
	 * @param json object with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXmlW3C(final Object json) {
		return new JsonToXml().toXmlW3C(json);
	}

	/** Create XML from JSON object according to W3C recommendation.
	 * @param json path or string with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXmlW3C(final String json) {
		return new JsonToXml().toXmlW3C(parse(json));
	}

	/** Create XML from JSON object according to W3C recommendation.
	 * @param file file with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXmlW3C(final File file) {
		return new JsonToXml().toXmlW3C(parse(file));
	}

	/** Create XML from JSON object according to W3C recommendation.
	 * @param url URL with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXmlW3C(final URL url) {
		return new JsonToXml().toXmlW3C(parse(url));
	}

	/** Create XML from JSON object according (XDefinition form).
	 * @param json object with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXml(final Object json) {
		return new JsonToXml().toXmlXD(json);
	}

	/** Create XML from JSON object according (XDefinition form).
	 * @param json path or string with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXml(final String json) {
		return new JsonToXml().toXmlXD(parse(json));
	}

	/** Create XML from JSON object according (XDefinition form).
	 * @param file file JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXml(final File file) {
		return new JsonToXml().toXmlXD(parse(file));
	}

	/** Create XML from JSON object according (XDefinition form).
	 * @param url URL with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXml(final URL url) {
		return new JsonToXml().toXmlXD(parse(url));
	}

	/** Create XML from JSON object according (XDefinition form).
	 * @param in InputStream with JSON data.
	 * @return XML element created from JSON data.
	 */
	public static final Element jsonToXml(final InputStream in) {
		return new JsonToXml().toXmlXD(parse(in));
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create JSON string from XML data.
	 * @param source XML form of string.
	 * @return XML form of string converted to JSON.
	 */
	public static final String jstringFromXML(final String source) {
		if (source == null || source.isEmpty()) {
			return source;
		}
		String src = source.trim();
		if (src != null && src.length() > 1
			&& src.charAt(0)=='"' && src.charAt(src.length() - 1)=='"'
			&& src.charAt(src.length() - 2) != '\\') {
			String s = src.substring(1,  src.length() - 1); // remove '"'
			StringBuilder sb = new StringBuilder();
			for (int i = 0;  i < s.length(); i++) {
				char ch = s.charAt(i);
				if (ch == '\\') {
					if (++i >= s.length()) {
						return s; // missing escape char (error)
					}
					switch (ch = s.charAt(i)) {
						case '"':
							ch = '"';
							break;
						case '\\':
							ch = '\\';
							break;
						case '/':
							ch = '/';
							break;
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
						case 'u':
							try {
								ch = (char) Short.parseShort(
									s.substring(i+1, i+5), 16);
								i += 4;
								break;
							} catch (Exception ex) {
								return s; // incorrect UTF-8 char
							}
						default: return s; // illegal escape char (error)
					}
				}
				sb.append(ch);
			}
			return sb.toString();
		}
		return src;
	}

	/** Create XML string created from JSON string.
	 * @param source JSON form of string.
	 * @param isAttr if true then it is used in attribute, otherwise it will be
	 * used in a text node.
	 * @return XML string converted to JSON string.
	 */
	public static final String jstringToXML(final String source,
		final boolean isAttr) {
		if (source.length() == 0 || "null".equals(source)
			|| "true".equals(source) || "false".equals(source)) {
			return "\"" + source + "\"";
		}
		boolean addQuot = source.indexOf(' ') >= 0 || source.indexOf('\t') >= 0
			|| source.indexOf('\n') >= 0 || source.indexOf('\r') >= 0
			|| source.indexOf('\f') >= 0 || source.indexOf('\b') >= 0;
		if (!addQuot) {
			char ch = source.charAt(0);
			if (ch == '-' || ch >= '0' && ch <= '9') {
				StringParser p = new StringParser(source);
				if ((p.isSignedFloat() || p.isSignedInteger())
					&& p.eos()) {
					return '"' + source + '"'; // value is number
				}
			}
		}
		if (addQuot) {
			StringBuilder sb = new StringBuilder();
			for (char ch: source.toCharArray()) {
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
							for (int i = 12; i >= 0; i -=4) {
								sb.append("0123456789abcdef"
									.charAt((ch >> i) & 0xf));
							}
						}
				}
			}
			String s = sb.toString();
			if (isAttr && s.equals(s.trim())) {
				// For attributes it is not necessary to add quotes if the data
				// does not contain leading or trailing white spaces,
				return s;
			} else {
				return '\"' + s + '\"';
			}
		} else {
			return source;
		}
	}

	/** Get XML name created from JSOM pair name.
	 * @param s JSOM pair name.
	 * @return XML name.
	 */
	public final static String toXmlName(final String s) {
		if (s.length() == 0) {
			return "_"; // empty string
		} else if (("_").equals(s)) {
			return "_x5f_";
		}
		StringBuilder sb = new StringBuilder();
		char ch = s.charAt(0);
		sb.append(isJChar(s, 0) || !Character.isJavaIdentifierStart(ch)
			? genXmlHexChar(ch) : ch);
//			|1 StringParser.getXmlCharType(ch, (byte) 10)
//				!= StringParser.XML_CHAR_NAME_START ? genXmlHexChar(ch) : ch);
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
					sb.append(isJChar(s,i)||!Character.isJavaIdentifierStart(ch)
					? genXmlHexChar(ch) : ch);
				} else {
					i--;
				}
			} else if (Character.isJavaIdentifierPart(ch)) {
				sb.append(ch);
			} else {
				sb.append(genXmlHexChar(ch));
			}
		}
		return sb.toString();
	}

	/** Convert character to representation used in XML names. */
	private static String genXmlHexChar(final char c) {
		return "_x" + Integer.toHexString(c) + '_';
	}

	/** Create JSON name from XML name.
	 * @param name XML name.
	 * @return JSON name.
	 */
	public static String toJsonName(final String name) {
		if ("_".equals(name)) {
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
}