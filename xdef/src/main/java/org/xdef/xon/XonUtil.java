package org.xdef.xon;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.msg.SYS;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;

/** JSON/XON utility (parseJSON JSON source to JSON/XON instance, compare JSON/XON
 objects, and create string with JSON/XON source from JSON/XON object.
 * @author Vaclav Trojan
 */
public class XonUtil {

////////////////////////////////////////////////////////////////////////////////
// XON parser
////////////////////////////////////////////////////////////////////////////////

	/** Get XON reader data from argument.
	 * @param x the object containing XON/JSON data.
	 * @return array with two items: Reader and System ID.
	 */
	private static Object[] getReader(Object x) {
		Object[] result = new Object[2];
		if (x instanceof String) {
			String s = (String) x;
			try {
				return getReader(SUtils.getExtendedURL(s));
			} catch (Exception ex) {
				try {
					return getReader(new File(s));
				} catch (Exception exx) {}
			}
			result[0] = new StringReader(s);
			result[1] = "STRING";
		} else if (x instanceof File) {
			File f = (File) x;
			try {
				result[0] = new InputStreamReader(
					new FileInputStream(f), Charset.forName("UTF-8"));
				result[1] = f.getCanonicalPath();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (x instanceof URL) {
			URL u = (URL) x;
			try {
				result[0] = new InputStreamReader(
					u.openStream(), Charset.forName("UTF-8"));
				result[1] = u.toExternalForm();
			} catch (Exception ex) {
				//Program exception &{0}
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (x instanceof InputStream) {
			result[0] = new InputStreamReader(
				(InputStream) x, Charset.forName("UTF-8"));
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

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final Reader in, final String sysid) {
		return XonReader.parseJSON(in, sysid);
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final String s)
		throws SRuntimeException {
		Object[] x = getReader(s);
		return XonUtil.parseJSON((Reader) x[0], (String) x[1]);
	}

	/** Parse JSON document from input source data in file.
	 * @param f input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final File f) throws SRuntimeException{
		Object[] x = getReader(f);
		return XonUtil.parseJSON((Reader) x[0], (String) x[1]);
	}

	/** Parse source URL to JSON.
	 * @param url source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseJSON(final URL url) throws SRuntimeException {
		Object[] x = getReader(url);
		return XonUtil.parseJSON((Reader) x[0], (String) x[1]);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final InputStream in)
		throws SRuntimeException {
		return XonUtil.parseJSON(in, null);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param in input data with JSON source.
	 * @param sysId System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final InputStream in, final String sysId)
		throws SRuntimeException {
		return XonUtil.parseJSON(
			new InputStreamReader(in, Charset.forName("UTF-8")), sysId);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse XON document from input reader.
	 * @param in reader with XON source.
	 * @param sysid System id.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final Reader in, final String sysid) {
		return XonReader.parseXON(in, sysid);
	}

	/** Parse XON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with XON source.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final String s)throws SRuntimeException{
		Object[] x = getReader(s);
		return parseXON((Reader) x[0], (String) x[1]);
	}

	/** Parse XON document from input source data in file.
	 * @param f input file.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final File f) throws SRuntimeException{
		Object[] x = getReader(f);
		return parseXON((Reader) x[0], (String) x[1]);
	}

	/** Parse source URL to XON.
	 * @param url source URL with XON data.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseXON(final URL url) throws SRuntimeException{
		Object[] x = getReader(url);
		return parseXON((Reader) x[0], (String) x[1]);
	}

	/** Parse XON document from input source data in InputStream.
	 * @param in input with XON data.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in)
		throws SRuntimeException {
		return parseXON(in, null);
	}

	/** Parse XON document from InputStream.
	 * @param in input with XON data.
	 * @param sysId System id.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream in,final String sysId)
		throws SRuntimeException {
		return parseXON(new InputStreamReader(in,
			Charset.forName("UTF-8")), sysId);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse INI/Properties document from input reader.
	 * @param in reader with INI/Properties source.
	 * @param sysid System id.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final Reader in,
		final String sysid) {
		return IniReader.parseINI(in, sysid);
	}

	/** Parse INI/Properties document from input source data.
	 * @param s file pathname or URL or string with INI/Properties source data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final String s)
		throws SRuntimeException {
		Object[] x = getReader(s);
		return parseINI((Reader) x[0], (String) x[1]);
	}

	/** Parse INI/Properties document from input source data in file.
	 * @param f input file.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final File f)
		throws SRuntimeException{
		Object[] x = getReader(f);
		return parseINI((Reader) x[0], (String) x[1]);
	}

	/** Parse URL data to INI/Properties.
	 * @param url URL with INI/Properties data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Map<String, Object> parseINI(final URL url)
		throws SRuntimeException{
		Object[] x = getReader(url);
		return parseINI((Reader) x[0], (String) x[1]);
	}

	/** Parse INI/Properties document from InputStream.
	 * @param in input data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final InputStream in)
		throws SRuntimeException {
		return parseINI(in, null);
	}

	/** Parse INI/Properties document from input source data in InputStream.
	 * @param in input with INI/Properties data.
	 * @param sysId System id.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final InputStream in,
		final String sysId) throws SRuntimeException {
		return parseINI(
			new InputStreamReader(in, Charset.forName("UTF-8")), sysId);
	}

////////////////////////////////////////////////////////////////////////////////
//  XON to String
////////////////////////////////////////////////////////////////////////////////

	/** Create string with XON object.
	 * @param x the XON object.
	 * @param indent if true the result will be indented.
	 * @return string with XCN source data.
	 */
	public static final String toXonString(final Object x,final boolean indent){
		StringBuilder sb = new StringBuilder();
		XonToString.objectToString(x, indent ? "\n" : null, sb, true);
		return sb.toString();
	}

	/** Create string with XON object.
	 * @param x the XON object.
	 * @return string with XCN source data.
	 */
	public static final String toXonString(final Object x){
		StringBuilder sb = new StringBuilder();
		XonToString.objectToString(x, null, sb, true);
		return sb.toString();
	}

	/** Create JSON object form XON object.
	 * @param x  XON object
	 * @return JSON object.
	 */
	public static final Object xonToJson(Object x) {
		return XonToString.xonToJson(x);
	}

////////////////////////////////////////////////////////////////////////////////
//  JSON to String
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
		XonToString.objectToString(x, indent ? "\n" : null, sb, false);
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
//  INI to String
////////////////////////////////////////////////////////////////////////////////
	/** Create INI/Properties from object.
	 * @param x INI/Properties object.
	 * @return string with INI/Properties format.
	 */
	public final static String toIniString(final Map<String, Object> x) {
		return IniReader.toIniString(x);
	}

////////////////////////////////////////////////////////////////////////////////
// XML to JSON
////////////////////////////////////////////////////////////////////////////////

	/** Convert XML element to JSON object.
	 * @param node XML element or document.
	 * @return JSON object.
	 */
	public final static Object xmlToJson(final Node node) {
		return XonFromXml.toJson(node);
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
// XON/JSON to XML
////////////////////////////////////////////////////////////////////////////////

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon path to XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final String xon) {
		return XonToXml.toXmlW(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon file with XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final File xon) {
		return XonToXml.toXmlW(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon URL where is XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final URL xon) {
		return XonToXml.toXmlW(parseJSON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon Input stream where is XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final InputStream xon) {
		return XonToXml.toXmlW(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon XON/JSON object.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final Object xon) {
		return XonToXml.toXmlW(xon);
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon path to XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlXD(final String xon) {
		return XonToXml.toXmlXD(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon File with XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlXD(final File xon) {
		return XonToXml.toXmlXD(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon URL with XON/JSON source data.
	 * @return XML element created from JSON XON/data.
	 */
	public final static Element xonToXmlXD(final URL xon) {
		return XonToXml.toXmlXD(parseJSON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon InputStream with XON/JSON source data.
	 * @return XML element created from JSON XON/data.
	 */
	public final static Element xonToXmlXD(final InputStream xon) {
		return XonToXml.toXmlXD(XonUtil.parseJSON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon XON/JSON object.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlXD(final Object xon) {
		return XonToXml.toXmlXD(xon);
	}

////////////////////////////////////////////////////////////////////////////////
// INI/Properties to XML
////////////////////////////////////////////////////////////////////////////////

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini path toINI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final String ini) {
		return IniReader.iniToXml(XonUtil.parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini file with INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final File ini) {
		return IniReader.iniToXml(XonUtil.parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini URL where is INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final URL ini) {
		return IniReader.iniToXml(XonUtil.parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini Input stream where is INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final InputStream ini) {
		return IniReader.iniToXml(XonUtil.parseINI(ini));
	}

	/** Create XML from INI/Properties object in X-Definition mode.
	 * @param ini INI/Properties object.
	 * @return XML element created from INI/Properties object.
	 */
	public final static Element iniToXml(final Object ini) {
		return IniReader.iniToXml(ini);
	}


////////////////////////////////////////////////////////////////////////////////
// Compare two JSON/XON objects.
////////////////////////////////////////////////////////////////////////////////

	/** Compare two JSON or XON objects.
	 * @param j1 first object with JSON or XON data.
	 * @param j2 second object with JSON or XON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static boolean xonEqual(final Object j1, final Object j2) {
		return (j1 == null && j2 == null) ||
			(j1 != null && j2 != null && XonCompare.equalValue(j1,j2));
	}
}