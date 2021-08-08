package org.xdef;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import org.w3c.dom.Element;
import org.xdef.impl.XBuilder;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefSQLResultSet;
import org.xdef.impl.code.DefSQLService;
import org.xdef.impl.code.DefXmlWriter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SRuntimeException;

/** Collection of methods supporting X-definition programming.
 * @author Vaclav Trojan
 */
public class XDTools {

	/** Get version of this implementation of X-definition.
	 * @return version of this implementation of X-definition.
	 */
	public final static String getXDVersion() {
		return XDConstants.BUILD_VERSION + " (" + XDConstants.BUILD_DATE + ")";
	}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param xmlFormat if true the input data are in XML format,
	 * otherwise in string format.
	 * @return the XDInput object.
	 */
	public final static XDInput createXDInput(final InputStream value,
		final boolean xmlFormat) {
		return new DefInStream(value, xmlFormat);
	}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param xmlFormat if true the input data are in XML format,
	 * otherwise in string format.
	 * @return the XDInput object.
	 */
	public final static XDInput createXDInput(final InputStreamReader value,
		boolean xmlFormat) {
		return new DefInStream(value, xmlFormat);
	}

	/** Creates XDInput from InputStream.
	 * @param value ReportReader.
	 * @return the XDInput object.
	 */
	public final static XDInput createXDInput(final ReportReader value) {
		return new DefInStream(value);
	}

	/** Creates XDOutput from reporter.
	 * @param value the reporter.
	 * @return the XDOutput object.
	 */
	public final static XDOutput createXDOutput(final ReportWriter value) {
		return new DefOutStream(value);
	}

	/** Creates XDOutput from Writer.
	 * @param value Writer object.
	 * @param xmlFormat if true the output will be in XML format,
	 * otherwise in string format.
	 * @return the XDOutput object.
	 */
	public final static XDOutput createXDOutput(final Writer value,
		final boolean xmlFormat) {
		return new DefOutStream(value, xmlFormat);
	}

	/** Creates XDOutput from PrintStream.
	 * @param value PrintStream object.
	 * @return the XDOutput object.
	 */
	public final static XDOutput createXDOutput(final PrintStream value) {
		return new DefOutStream(value);
	}

	/** Creates XDElement from org.w3c.dom.Element.
	 * @param el W3C element.
	 * @return XDElement object.
	 */
	public final static XDElement createXDElement(final Element el) {
		return new DefElement(el);
	}

	/** Creates named value.
	 * @param key the name of named value.
	 * @param value value of named value (may be null)
	 * @return named value.
	 */
	public final static XDNamedValue createXDNamedValue(final String key,
		final Object value) {
		return new DefNamedValue(key,
			value == null || value instanceof XDValue ?
				(XDValue) value : createXDValue(value));
	}

	/** Creates the empty XDContainer.
	 * @return the empty XDContainer.
	 */
	public final static XDContainer createXDContainer() {
		return new DefContainer();
	}

	/** Creates XDContainer from the object.
	 * @param value the object.
	 * @return the XDContainer object with item created from the object.
	 */
	public final static XDContainer createXDContainer(final Object value) {
		return new DefContainer(value);
	}

	/** Creates XDService object with JDBC support.
	 * @param url string with connection URL.
	 * @param user user name.
	 * @param passw password.
	 * @return XDService object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final static XDService createSQLService(final String url,
		final String user,
		final String passw) throws SRuntimeException {
		return new DefSQLService(url, user, passw);
	}

	/** Creates DefSQLService object with JDBC support.
	 * @param conn Database connection.
	 * @return XDService object.
	 */
	public final static XDService createSQLService(final Connection conn)
		throws SRuntimeException {
		return new DefSQLService(conn);
	}

	/** Creates XDResultSet object from java.sql.ResultSet.
	 * @param sqlResultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public final static XDResultSet createXDResultSet(
		final ResultSet sqlResultSet) {
		return new DefSQLResultSet(sqlResultSet);
	}

	/** Creates XDResultSet object from java.sql.ResultSet extracting value of
	 * specified column from each raw.
	 * @param itemName name of column.
	 * @param sqlResultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public final static XDResultSet createXDResultSet(final String itemName,
		final ResultSet sqlResultSet) {
		return new DefSQLResultSet(itemName, sqlResultSet);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param out where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if true then the XML header is
	 * written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDXmlOutStream createXDXmlOutStream(
		final OutputStream out,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		return new DefXmlWriter(out, encoding, writeDocumentHeader);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @deprecated use OutputStream instead of writer.
	 * @param writer where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if true then the XML header is
	 * written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 */
	public final static XDXmlOutStream createXDXmlOutStream(final Writer writer,
		final String encoding,
		final boolean writeDocumentHeader) {
		return new DefXmlWriter(
			writer, encoding, writeDocumentHeader);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param fname where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if true then the XML header is
	 * written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDXmlOutStream createXDXmlOutStream(final String fname,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		return new DefXmlWriter(fname, encoding, writeDocumentHeader);
	}

	/** Creates XDParseResult object.
	 * @param source source which will be set as parsed object.
	 * @return XDParseResult object.
	 */
	public final static XDParseResult createParseResult(final String source) {
		return new DefParseResult(source);
	}

	/** Create XDValue object.
	 * @param obj the object from which XDValue will be created.
	 * It may be one of:
	 * <ul>
	 * <li>XDValue
	 * <li>String
	 * <li>Short, Integer, Long
	 * <li>Float, Double
	 * <li>BigDecimal
	 * <li>BNFGrammar
	 * <li>BNFRule
	 * <li>Boolean
	 * <li>Calendar, SDatetime
	 * <li>SDuration
	 * </ul>
	 * @return new XDValue object.
	 * @throws RuntimeException if the object from argument is not possible
	 * to convert to XDValue object.
	 */
	public final static XDValue createXDValue(final Object obj) {
		return XBuilder.createXDValue(obj);
	}
}
