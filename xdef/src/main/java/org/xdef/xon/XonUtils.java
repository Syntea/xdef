package org.xdef.xon;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;

/** Utilities for XON/JSON/Properties/INI/CSV data (parse source, comparing
 * XON objects, conversion data to XML and from XML, conversion to String.
 * @author Vaclav Trojan
 */
public class XonUtils {

////////////////////////////////////////////////////////////////////////////////
// parsers
////////////////////////////////////////////////////////////////////////////////

	/** Parse input stream with CSV data(value separator is comma).
	 * @param source input stream with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final InputStream source,
		final char separator,
		final boolean skipHeader) {
		return parseCSV(source, separator, skipHeader, null);
	}

	/** Parse input stream with CSV data(value separator is comma).
	 * @param source input stream with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sysid System id.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final InputStream source,
		final char separator,
		final boolean skipHeader,
		final String sysid) {
		return CsvReader.parseCSV(source,
			separator, skipHeader, sysid == null ? "INPUTSTREAM" : sysid);
	}

	/** Parse CSV input reader (value separator is comma).
	 * @param source reader with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final Reader source,
		final char separator,
		final boolean skipHeader) {
		return parseCSV(source, separator, skipHeader, null);
	}

	/** Parse CSV input reader (value separator is comma).
	 * @param source reader with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sysid System id.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final Reader source,
		final char separator,
		final boolean skipHeader,
		final String sysid) {
		return CsvReader.parseCSV(source,
			separator, skipHeader, sysid == null ? "READER" : sysid);
	}

	/** Parse CSV data from a file(value separator is comma).
	 * @param source file with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final File source,
		final char separator,
		final boolean skipHeader) {
		return CsvReader.parseCSV(source, separator, skipHeader);
	}

	/** Parse CSV data from URL(value separator is comma).
	 * @param source URL with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final URL source,
		final char separator,
		final boolean skipHeader) {
		return CsvReader.parseCSV(source, separator, skipHeader);
	}

	/** Parse CSV source data.
	 * @param source CSV source (filename, URL, or string with CSV data).
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return parsed CSV object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static List<Object> parseCSV(final String source,
		final char separator,
		final boolean skipHeader)
		throws SRuntimeException {
		return CsvReader.parseCSV(source, separator, skipHeader);
	}

	/** Parse INI/Properties document from input reader.
	 * @param in reader with INI/Properties source.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final Reader in) {
		return IniReader.parseINI(in, null);
	}

	/** Parse INI/Properties document from input reader.
	 * @param in reader with INI/Properties source.
	 * @param sysid System id.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final Reader in,
		final String sysid) {
		return IniReader.parseINI(in, sysid == null ? "READER" : sysid);
	}

	/** Parse INI/Properties document from input source data.
	 * @param source file pathname or URL or string with INI/Properties source data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final String source)
		throws SRuntimeException {
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return indata._reader != null
			? parseINI(indata._reader, indata._sysId)
			: parseINI(indata._in, indata._sysId);
	}

	/** Parse INI/Properties document from input source data in file.
	 * @param source file with INI data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final File source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return parseINI(indata._in, indata._sysId);
	}

	/** Parse URL data to INI/Properties.
	 * @param source URL with INI/Properties data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Map<String, Object> parseINI(final URL source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return parseINI(indata._in, indata._sysId);
	}

	/** Parse INI/Properties document from InputStream.
	 * @param source input data.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final InputStream source)
		throws SRuntimeException {
		return parseINI(source, null);
	}

	/** Parse INI/Properties document from input source data in InputStream.
	 * @param source input with INI/Properties data.
	 * @param sysid System id.
	 * @return parsed INI/Properties object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Map<String, Object> parseINI(final InputStream source,
		final String sysid) throws SRuntimeException {
		return IniReader.parseINI(
			new InputStreamReader(source,Charset.forName("ISO-8859-1")),
			sysid == null ? "INPUTSTREAM" : sysid);
	}

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final Reader in) {
		return XonReader.parseJSON(in, null);
	}

	/** Parse JSON document from input reader.
	 * @param in reader with JSON source.
	 * @param sysid System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final Reader in, final String sysid) {
		return XonReader.parseJSON(in, sysid == null ? "READER" : sysid);
	}

	/** Parse JSON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param source file pathname or URL or string with JSON source.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final String source)
		throws SRuntimeException {
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return indata._reader != null
			? parseJSON(indata._reader, indata._sysId)
			: parseJSON(indata._in, indata._sysId);
	}

	/** Parse JSON document from input source data in file.
	 * @param source input file.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final File source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return parseJSON(indata._in, indata._sysId);
	}

	/** Parse source URL to JSON.
	 * @param source source URL
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseJSON(final URL source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return parseJSON(indata._in, indata._sysId);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param source input data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final InputStream source)
		throws SRuntimeException {
		return parseJSON(source, null);
	}

	/** Parse JSON document from input source data in InputStream.
	 * @param source input data with JSON source.
	 * @param sysId System id.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseJSON(final InputStream source,
		final String sysId)
		throws SRuntimeException {
		return XonReader.parseJSON(source, sysId==null ? "INPUTSTREAM" : sysId);
	}

	/** Parse XON document from input reader.
	 * @param in reader with XON source.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final Reader in) {
		return XonReader.parseXON(in, null);
	}

	/** Parse XON document from input reader.
	 * @param in reader with XON source.
	 * @param sysid System id.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final Reader in, final String sysid) {
		return XonReader.parseXON(in, sysid == null ? "READER" : sysid);
	}

	/** Parse XON document from input source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param source file pathname or URL or string with XON source.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final String source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return indata._reader != null ? parseXON(indata._reader, indata._sysId)
			: parseXON(indata._in, indata._sysId);
	}

	/** Parse XON document from input source data in file.
	 * @param source input file.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final File source)
		throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(source, null);
		return parseXON(indata._in, indata._sysId);
	}

	/** Parse source URL to XON.
	 * @param url source URL with XON data.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseXON(final URL url) throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(url, null);
		return parseXON(indata._in, indata._sysId);
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
	 * @param source input with XON data.
	 * @param sysId System id.
	 * @return parsed XON object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseXON(final InputStream source,
		final String sysId) throws SRuntimeException {
		return XonReader.parseXON(source, sysId == null ? "INPUTSTREAM" : sysId);
	}

	/** Parse YAML document from input reader.
	 * @param in reader with YAML source.
	 * @param sysid System id.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseYAML(final Reader in, final String sysid) {
		return XonYaml.parseYAML(in);
	}

	/** Parse YAML document from source data.
	 * The source data may be either file pathname or URL or JSON source.
	 * @param s file pathname or URL or string with YAML source.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseYAML(final String s)
		throws SRuntimeException {
		XonTools.InputData indata = XonTools.getInputFromObject(s, null);
		return indata._reader != null
			? parseYAML(indata._reader, indata._sysId)
			: parseYAML(indata._in, indata._sysId);
	}

	/** Parse YAML document from input source data in file.
	 * @param f input file.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseYAML(final File f) throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(f, null);
		return parseYAML(indata._in, indata._sysId);
	}

	/** Parse source URL to YAML data.
	 * @param url source URL with YAML data.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final static Object parseYAML(final URL url)throws SRuntimeException{
		XonTools.InputData indata = XonTools.getInputFromObject(url, null);
		return parseYAML(indata._in, indata._sysId);
	}

	/** Parse YAML document from input source data in InputStream.
	 * @param in input with YAML data.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseYAML(final InputStream in)
		throws SRuntimeException {
		return parseYAML(in, null);
	}

	/** Parse YAML document from InputStream.
	 * @param in input with YAML data.
	 * @param sysId System id.
	 * @return parsed YAML object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static Object parseYAML(final InputStream in,
		final String sysId) throws SRuntimeException {
		return parseYAML(new InputStreamReader(in,
			Charset.forName("UTF-8")), sysId == null ? "INPUTSTREAM" : sysId);
	}

////////////////////////////////////////////////////////////////////////////////
//  to String tools
////////////////////////////////////////////////////////////////////////////////

	/** Create CSV string from CSV object.
	 * @param x CSV object.
	 * @return CSV string created from CSV object.
	 */
	public final static String toCsvString(final List<Object> x) {
		return CsvReader.toCsvString(x);
	}

	/** Create INI/Properties from object.
	 * @param x INI/Properties object.
	 * @return string with INI/Properties format.
	 */
	public final static String toIniString(final Map<String, Object> x) {
		return IniReader.toIniString(x);
	}

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

	/** Create YAML string from object (no indentation).
	 * @param x XON object.
	 * @return string with YAML source format.
	 */
	public final static String toYamlString(final Object x) {
		return XonYaml.toYamlString(x);
	}

////////////////////////////////////////////////////////////////////////////////
// XML to object...
////////////////////////////////////////////////////////////////////////////////

	/** Create XML element with CSV data.
	 * @param x object with CSV data.
	 * @return Element created from CSV data.
	 */
	public final static Element csvToXml(final List x) {
		return CsvReader.csvToXml(x);
	}

	/** Convert XML element to XON object.
	 * @param node XML element or document.
	 * @return JSON object.
	 */
	public final static Object xmlToXon(final Node node) {
		return XonFromXml.toXon(node);
	}

	/** Convert XML document to XON object.
	 * @param source path or string with source of XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToXon(final String source) {
		return xmlToXon(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Convert XML document to XON object.
	 * @param file file with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToXon(final File file) {
		return xmlToXon(KXmlUtils.parseXml(file).getDocumentElement());
	}

	/** Convert XML document to XON object.
	 * @param url URL containing XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToXon(final URL url) {
		return xmlToXon(KXmlUtils.parseXml(url).getDocumentElement());
	}

	/** Convert XML document to XON object.
	 * @param in InputStream with XML document.
	 * @return object with JSON data.
	 */
	public final static Object xmlToXon(final InputStream in) {
		return xmlToXon(KXmlUtils.parseXml(in).getDocumentElement());
	}

////////////////////////////////////////////////////////////////////////////////
// to XML tools
////////////////////////////////////////////////////////////////////////////////

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini path toINI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final String ini) {
		return IniReader.iniToXml(parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini file with INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final File ini) {
		return IniReader.iniToXml(parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini URL where is INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final URL ini) {
		return IniReader.iniToXml(parseINI(ini));
	}

	/** Create XML from INI/Properties object in "W" format.
	 * @param ini Input stream where is INI/Properties source data.
	 * @return XML element created from INI/Properties data.
	 */
	public final static Element iniToXml(final InputStream ini) {
		return IniReader.iniToXml(parseINI(ini));
	}

	/** Create XML from INI/Properties object in X-Definition mode.
	 * @param ini INI/Properties object.
	 * @return XML element created from INI/Properties object.
	 */
	public final static Element iniToXml(final Object ini) {
		return IniReader.iniToXml(ini);
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon path to XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlW(final String xon) {
		return XonToXml.toXmlW(parseXON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon file with XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlW(final File xon) {
		return XonToXml.toXmlW(parseXON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon URL where is XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlW(final URL xon) {
		return XonToXml.toXmlW(parseXON(xon));
	}

	/** Create XML from XON/JSON object in "W" format.
	 * @param xon Input stream where is XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlW(final InputStream xon) {
		return XonToXml.toXmlW(parseXON(xon));
	}

	/** Create XML from XON/JSON object in W-format.
	 * @param xon XON/JSON object.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXmlW(final Object xon) {
		return XonToXml.toXmlW(xon);
	}

	/** Create XML from XON object in X-Definition mode.
	 * @param xon path to XON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final String xon) {
		return XonToXml.toXmlXD(parseXON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon File with XON/JSON source data.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final File xon) {
		return XonToXml.toXmlXD(parseXON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon URL with XON/JSON source data.
	 * @return XML element created from JSON XON/data.
	 */
	public final static Element xonToXml(final URL xon) {
		return XonToXml.toXmlXD(parseXON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon InputStream with XON/JSON source data.
	 * @return XML element created from JSON XON/data.
	 */
	public final static Element xonToXml(final InputStream xon) {
		return XonToXml.toXmlXD(parseXON(xon));
	}

	/** Create XML from XON/JSON object in X-Definition mode.
	 * @param xon XON/JSON object.
	 * @return XML element created from XON/JSON data.
	 */
	public final static Element xonToXml(final Object xon) {
		return XonToXml.toXmlXD(xon);
	}

////////////////////////////////////////////////////////////////////////////////
// Compare two objects (JSON,XON, INI, CSV, ...).
////////////////////////////////////////////////////////////////////////////////

	/** Compare two XON/JSON objects.
	 * @param a first object with XON/JSON data.
	 * @param b second object with XON/JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static boolean xonEqual(final Object a, final Object b) {
		return (a == null && b == null) ||
			(a != null && b != null && XonCompare.equalValue(a,b));
	}

	/** Compare two XON/JSON objects. Return an empty string if both objects
	 * are equal, otherwise, return string with different items.
	 * @param a first object with XON/JSON data.
	 * @param b second object with XON/JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	public final static String xonDiff(final Object a, final Object b) {
		return XonCompare.xonDiff(a, b);
	}

}
