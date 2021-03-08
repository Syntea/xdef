package org.xdef.json;

import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.SUtils;

/** JSON utility (parse JSON source to JSON instance, compare JSON instances,
 * and create string with JSON source from JSON object.
 * @author Vaclav Trojan
 */
public class JsonUtil extends JsonTools {

////////////////////////////////////////////////////////////////////////////////
// JSON parser
////////////////////////////////////////////////////////////////////////////////

	private static JsonParser initParser(final InputStream in, final String id) {
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
			JsonParser jx = new JsonParser();
			if (id != null) {
				jx.setSysId(id);
			}
			jx.setSourceReader(reader, 0L, s);
			return jx;
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	private static JsonParser getParser(final Object src, final String id) {
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
				JsonParser jx = new JsonParser((Reader) obj);
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
	private static Object parse(final JsonParser jx) {
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
	private static JsonParser getXONParser(final Object src, final String id) {
		JsonParser xx = getParser(src, id);
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
// XON parder
////////////////////////////////////////////////////////////////////////////////

	/** Create string with XON object.
	 * @param x the XON object.
	 * @param indent if true the result will be indented.
	 * @return string with XCN source data.
	 */
	public static final String toXonString(final Object x,final boolean indent){
		StringBuilder sb = new StringBuilder();
		JsonToString.objectToString(x, indent ? "\n" : null, sb, true);
		return sb.toString();
	}

	/** Create JSON object form XON object.
	 * @param xon  XON object
	 * @return JSON object.
	 */
	public static final Object xonToJson(Object xon) {
		return JsonToString.xonToJson(xon);
	}

////////////////////////////////////////////////////////////////////////////////
//  String from JSON/XON
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
		JsonToString.objectToString(obj, indent ? "\n" : null, sb, false);
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two JSON/XON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Compare two JSON or XON objects.
	 * @param j1 first object with JSON or XON data.
	 * @param j2 second object with JSON or XON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static boolean jsonEqual(final Object j1, final Object j2) {
		return (j1 == null && j2 == null) ||
			(j1 != null && j2 != null && JsonCompare.equalValue(j1,j2));
	}

////////////////////////////////////////////////////////////////////////////////
// XML to JSON
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

////////////////////////////////////////////////////////////////////////////////
// JSON to XML
////////////////////////////////////////////////////////////////////////////////

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
}