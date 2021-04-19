package org.xdef.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;

/** JSON utility (parse JSON source to JSON instance, compare JSON instances,
 * and create string with JSON source from JSON object.
 * @author Vaclav Trojan
 */
public class JsonUtil {

////////////////////////////////////////////////////////////////////////////////
// JSON parser
////////////////////////////////////////////////////////////////////////////////

	private static List<Object> getReader(Object x) {
		List<Object> result = new ArrayList<Object>();
		Reader reader = null;
		String sysId = null;
		if (x instanceof String) {
			String s = (String) x;
			try {
				URL url = SUtils.getExtendedURL(s);
				reader = new InputStreamReader(url.openStream(), "UTF-8");
				sysId = s;
			} catch (Exception ex) {
				File f = new File(s);
				if (f.exists()) {
				}
			}
			if (reader == null) {
				reader = new StringReader(s);
				sysId = "STRING";
			}
		} else if (x instanceof File) {
			File f = (File) x;
			try {
				reader = new InputStreamReader(
				new FileInputStream(f), "UTF-8");
				sysId = f.getCanonicalPath();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (x instanceof URL) {
			URL u = (URL) x;
			try {
				reader = new InputStreamReader(u.openStream(), "UTF-8");
				sysId = u.toExternalForm();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else {
			//Program exception &{0}
			throw new SRuntimeException(SYS.SYS036,
				"Incorrect parameter of getReader");
		}
		result.add(reader);
		result.add(sysId);
		return result;
	}

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final Reader in, final String sysid) {
		return XONReader.parseJSON(in, sysid);
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final String s) throws SRuntimeException {
		List<Object> x = getReader(s);
		return parse((Reader) x.get(0), (String) x.get(1));
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final File f) throws SRuntimeException {
		List<Object> x = getReader(f);
		return parse((Reader) x.get(0), (String) x.get(1));
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in)
		throws SRuntimeException {
		return parse(in, null);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysId System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parse(final InputStream in, final String sysId)
		throws SRuntimeException {
		try {
			return parse(new InputStreamReader(in, "UTF-8"), sysId);
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	/** Parse source URL to JSON.
	 * @param url source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parse(final URL url) throws SRuntimeException {
		List<Object> x = getReader(url);
		return parse((Reader) x.get(0), (String)x.get(1));
	}
////////////////////////////////////////////////////////////////////////////////

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final Reader in, final String sysid) {
		return XONReader.parseXON(in, sysid);
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final String s)throws SRuntimeException{
		List<Object> x = getReader(s);
		return parseXON((Reader) x.get(0), (String) x.get(1));
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final File f) throws SRuntimeException{
		List<Object> x = getReader(f);
		return parseXON((Reader) x.get(0), (String) x.get(1));
	}

	/** Parse source URL to JSON.
	 * @param url source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseXON(final URL url) throws SRuntimeException{
		List<Object> x = getReader(url);
		return parseXON((Reader) x.get(0), (String) x.get(1));
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in)
		throws SRuntimeException {
		return parseXON(in, null);
	}

	/** Parse XON document from input source data in InputStream.
	 * @param in input data.
	 * @param sysId System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in,final String sysId)
		throws SRuntimeException {
		try {
			return parseXON(new InputStreamReader(in, "UTF-8"), sysId);
		} catch (Exception ex) {
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}
////////////////////////////////////////////////////////////////////////////////
// XON parser
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

	/** Create string with XON object.
	 * @param x the XON object.
	 * @return string with XCN source data.
	 */
	public static final String toXonString(final Object x){
		StringBuilder sb = new StringBuilder();
		JsonToString.objectToString(x, null, sb, true);
		return sb.toString();
	}

	/** Create JSON object form XON object.
	 * @param x  XON object
	 * @return JSON object.
	 */
	public static final Object xonToJson(Object x) {
		return JsonToString.xonToJson(x);
	}

////////////////////////////////////////////////////////////////////////////////
//  String from JSON/XON
////////////////////////////////////////////////////////////////////////////////
	/** Create JSON string from object (no indentation).
	 * @param x JSON object.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object x) {
		return toJsonString(x, false);
	}

	/** Create JSON string from object. Indentation depends on argument.
	 * @param x JSON object.
	 * @param indent if true then result will be indented.
	 * @return string with JSON source format.
	 */
	public final static String toJsonString(final Object x, boolean indent) {
		StringBuilder sb = new StringBuilder();
		JsonToString.objectToString(x, indent ? "\n" : null, sb, false);
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