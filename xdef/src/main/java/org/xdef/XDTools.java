package org.xdef;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.Writer;
//import java.net.URL;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import org.w3c.dom.Element;
//import org.xdef.sys.FUtils;
//import org.xdef.sys.ReportReader;
//import org.xdef.sys.ReportWriter;
//import org.xdef.sys.SRuntimeException;
//
///** Collection of methods used in X-definition.
// * @author Vaclav Trojan
// */
//public class XDTools {
//
//	/** Parse XML with X-definition declared in source input stream.
//	 * @param source where to read XML.
//	 * @param reporter used for error messages or null.
//	 * @return created XDDocument object.
//	 * @throws SRuntimeException if an error occurs.
//	 */
//	public final static XDDocument xparse(final InputStream source,
//		final ReportWriter reporter) throws SRuntimeException {
//		return org.xdef.impl.XBuilder.xparse(source, reporter);
//	}
//
//	/** Parse XML with X-definition declared in source.
//	 * @param source URL, pathname direct to XML or direct XML.
//	 * @param reporter used for error messages or null.
//	 * @return created XDDocument object.
//	 * @throws SRuntimeException if an error occurs.
//	 */
//	public final static XDDocument xparse(final String source,
//		final ReportWriter reporter) throws SRuntimeException {
//		return org.xdef.impl.XBuilder.xparse(source, reporter);
//	}
//
//	/** Creates XDInput from InputStream.
//	 * @param value the stream.
//	 * @param xmlFormat if true the input data are in XML format,
//	 * otherwise in string format.
//	 * @return the XDInput object.
//	 */
//	public final static XDInput createXDInput(final InputStream value,
//		final boolean xmlFormat) {
//		return new org.xdef.impl.code.DefInStream(value, xmlFormat);
//	}
//
//	/** Creates XDInput from InputStream.
//	 * @param value the stream.
//	 * @param xmlFormat if true the input data are in XML format,
//	 * otherwise in string format.
//	 * @return the XDInput object.
//	 */
//	public final static XDInput createXDInput(final InputStreamReader value,
//		boolean xmlFormat) {
//		return new org.xdef.impl.code.DefInStream(value, xmlFormat);
//	}
//
//	/** Creates XDInput from InputStream.
//	 * @param value ReportReader.
//	 * @return the XDInput object.
//	 */
//	public final static XDInput createXDInput(final ReportReader value) {
//		return new org.xdef.impl.code.DefInStream(value);
//	}
//
//	/** Creates XDOutput from reporter.
//	 * @param value the reporter.
//	 * @return the XDOutput object.
//	 */
//	public final static XDOutput createXDOutput(final ReportWriter value) {
//		return new org.xdef.impl.code.DefOutStream(value);
//	}
//
//	/** Creates XDOutput from Writer.
//	 * @param value Writer object.
//	 * @param xmlFormat if true the output will be in XML format,
//	 * otherwise in string format.
//	 * @return the XDOutput object.
//	 */
//	public final static XDOutput createXDOutput(final Writer value,
//		final boolean xmlFormat) {
//		return new org.xdef.impl.code.DefOutStream(value, xmlFormat);
//	}
//
//	/** Creates XDOutput from PrintStream.
//	 * @param value PrintStream object.
//	 * @return the XDOutput object.
//	 */
//	public final static XDOutput createXDOutput(final PrintStream value) {
//		return new org.xdef.impl.code.DefOutStream(value);
//	}
//
//	/** Creates XDElement from org.w3c.dom.Element.
//	 * @param el W3C element.
//	 * @return XDElement object.
//	 */
//	public final static XDElement createXDElement(Element el) {
//		return new org.xdef.impl.code.DefElement(el);
//	}
//
//	/** Creates named value.
//	 * @param key the name of named value.
//	 * @param val value of named value (may be null)
//	 * @return named value.
//	 */
//	public final static XDNamedValue createXDNamedValue(String key, Object val){
//		return new org.xdef.impl.code.DefNamedValue(key,
//			val == null || val instanceof XDValue ?
//				(XDValue) val : createXDValue(val));
//	}
//
//	/** Creates the empty XDContainer.
//	 * @return the empty XDContainer.
//	 */
//	public final static XDContainer createXDContainer() {
//		return new org.xdef.impl.code.DefContainer();
//	}
//
//	/** Creates XDContainer from the object.
//	 * @param value the object.
//	 * @return the XDContainer object with item created from the object.
//	 */
//	public final static XDContainer createXDContainer(Object value) {
//		return new org.xdef.impl.code.DefContainer(value);
//	}
//
//	/** Creates XDService object with JDBC support.
//	 * @param url string with connection URL.
//	 * @param user user name.
//	 * @param passw password.
//	 * @return XDService object.
//	 * @throws SRuntimeException if an error occurs.
//	 */
//	public final static XDService createSQLService(final String url,
//		final String user,
//		final String passw) throws SRuntimeException {
//		return new org.xdef.impl.code.DefSQLService(url, user, passw);
//	}
//
//	/** Creates DefSQLService object with JDBC support.
//	 * @param conn Database connection.
//	 * @return XDService object.
//	 */
//	public final static XDService createSQLService(final Connection conn)
//		throws SRuntimeException {
//		return new org.xdef.impl.code.DefSQLService(conn);
//	}
//
//	/** Creates XDResultSet object from java.sql.ResultSet.
//	 * @param resultSet the ResultSet object.
//	 * @return XDResultSet object.
//	 */
//	public final static XDResultSet createXDResultSet(final ResultSet resultSet) {
//		return new org.xdef.impl.code.DefSQLResultSet(resultSet);
//	}
//
//	/** Creates XDResultSet object from java.sql.ResultSet extracting value of
//	 * specified column from each raw.
//	 * @param itemName name of column.
//	 * @param resultSet the ResultSet object.
//	 * @return XDResultSet object.
//	 */
//	public static XDResultSet createXDResultSet(final String itemName,
//		final java.sql.ResultSet resultSet) {
//		return new org.xdef.impl.code.DefSQLResultSet(itemName,resultSet);
//	}
//
//	/** Creates XDXmlStream writer object from java.io.Writer.
//	 * @param writer where to write XML.
//	 * @param encoding encoding of XML stream.
//	 * @param writeDocumentHeader if true then the XML header is
//	 * written, otherwise no XML header is written.
//	 * @return XDXmlOutStream object.
//	 */
//	public static XDXmlOutStream createXDXmlOutStream(final Writer writer,
//		final String encoding,
//		final boolean writeDocumentHeader) {
//		return new org.xdef.impl.code.DefXmlWriter(
//			writer, encoding,writeDocumentHeader);
//	}
//
//	/** Creates XDXmlStream writer object from java.io.Writer.
//	 * @param fname where to write XML.
//	 * @param encoding encoding of XML stream.
//	 * @param writeDocumentHeader if true then the XML header is
//	 * written, otherwise no XML header is written.
//	 * @return XDXmlOutStream object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static XDXmlOutStream createXDXmlOutStream(final String fname,
//		final String encoding,
//		final boolean writeDocumentHeader) throws IOException {
//		return new org.xdef.impl.code.DefXmlWriter(
//			fname, encoding, writeDocumentHeader);
//	}
//
//	/** Creates XDParseResult object.
//	 * @param source source which will be set as parsed object.
//	 * @return XDParseResult object.
//	 */
//	public final static XDParseResult createParseResult(final String source) {
//		return new org.xdef.impl.code.DefParseResult(source);
//	}
//
//	/** Create XDValue object.
//	 * @param obj the object from which XDValue will be created.
//	 * It may be one of:
//	 * <ul>
//	 * <li>XDValue
//	 * <li>String
//	 * <li>Short, Integer, Long
//	 * <li>Float, Double
//	 * <li>BigDecimal
//	 * <li>BNFGrammar
//	 * <li>BNFRule
//	 * <li>Boolean
//	 * <li>Calendar, SDatetime
//	 * <li>SDuration
//	 * </ul>
//	 * @return new XDValue object.
//	 * @throws RuntimeException if the object from argument is not possible
//	 * to convert to XDValue object.
//	 */
//	public final static XDValue createXDValue(final Object obj) {
//		return org.xdef.impl.XBuilder.createXDValue(obj);
//	}
//
//	/** Write the XDPool to output stream.
//	 * @param out output stream where to write XDPool.
//	 * @param xp XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static void writeXDPool(final OutputStream out,final XDPool xp)
//		throws IOException {
//		ObjectOutputStream oout = new ObjectOutputStream(out);
//		oout.writeObject(xp);
//		oout.close();
//	}
//
//	/** Write the XDPool to output stream.
//	 * @param file file where to write XDPool.
//	 * @param xp XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static void writeXDPool(final File file, final XDPool xp)
//		throws IOException {
//		FileOutputStream fos = new FileOutputStream(file);
//		writeXDPool(fos, xp);
//	}
//
//	/** Write the XDPool to output stream.
//	 * @param fname pathname where to write XDPool.
//	 * @param xp XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static void writeXDPool(final String fname, final XDPool xp)
//		throws IOException {
//		FileOutputStream fos = new FileOutputStream(fname);
//		writeXDPool(fos, xp);
//	}
//
//	/** Read the XDPool from the input stream.
//	 * @param in input stream with X-definition.
//	 * @return XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static XDPool readXDPool(final InputStream in)
//		throws IOException {
//		try {
//			ObjectInputStream oin = new ObjectInputStream(in);
//			XDPool result = (XDPool) oin.readObject();
//			oin.close();
//			return result;
//		} catch (ClassNotFoundException ex) {
//			in.close();
//			throw new IOException(ex);
//		}
//	}
//
//	/** Read the XDPool from the input stream.
//	 * @param file file with X-definition.
//	 * @return XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static XDPool readXDPool(final File file) throws IOException {
//		return readXDPool(new FileInputStream(file));
//	}
//
//	/** Read the XDPool from the input stream.
//	 * @param fname pathname of file or string with URL with X-definition (it
//	 * may be also "classpath://.....").
//	 * @return XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static XDPool readXDPool(final String fname)throws IOException{
//		if (!new File(fname).exists() && fname.indexOf("://") > 0) {
//			return readXDPool(FUtils.getExtendedURL(fname).openStream());
//		} else {
//			return readXDPool(new FileInputStream(fname));
//		}
//	}
//
//	/** Read the XDPool from the input stream.
//	 * @param url URL where is data with XDPool.
//	 * @return XDPool object.
//	 * @throws IOException if an error occurs.
//	 */
//	public final static XDPool readXDPool(final URL url) throws IOException {
//		return readXDPool(url.openStream());
//	}
//
//}
