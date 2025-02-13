package org.xdef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.BUILD_DATETIME;
import static org.xdef.XDConstants.BUILD_VERSION;
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
import org.xdef.sys.SUtils;

/** Collection of methods supporting Xdefinition programming.
 * @author Vaclav Trojan
 */
public class XDTools {

	/** Get version of this implementation of Xdefinition.
	 * @return version of this implementation of Xdefinition.
	 */
	public static final String getXDVersion() {	return BUILD_VERSION + " (" + BUILD_DATETIME + ")";}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param xmlFormat if true the input data are in XML format, otherwise in string format.
	 * @return the XDInput object.
	 */
	public static final XDInput createXDInput(final InputStream value, final boolean xmlFormat) {
		return new DefInStream(value, xmlFormat);
	}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param isxml if true the input data are in XML format, otherwise in string format.
	 * @return the XDInput object.
	 */
	public static final XDInput createXDInput(final InputStreamReader value, final boolean isxml) {
		return new DefInStream(value, isxml);
	}

	/** Creates XDInput from InputStream.
	 * @param rr ReportReader.
	 * @return the XDInput object.
	 */
	public static final XDInput createXDInput(final ReportReader rr) {return new DefInStream(rr);}

	/** Creates XDOutput from reporter.
	 * @param r the reporter.
	 * @return the XDOutput object.
	 */
	public static final XDOutput createXDOutput(final ReportWriter r) {return new DefOutStream(r);}

	/** Creates XDOutput from Writer.
	 * @param w Writer object.
	 * @param isxml if true the output will be in XML format, otherwise in string format.
	 * @return the XDOutput object.
	 */
	public static final XDOutput createXDOutput(final Writer w, final boolean isxml) {
		return new DefOutStream(w, isxml);
	}

	/** Creates XDOutput from PrintStream.
	 * @param ps PrintStream object.
	 * @return the XDOutput object.
	 */
	public static final XDOutput createXDOutput(final PrintStream ps) {return new DefOutStream(ps);}

	/** Creates XDElement from org.w3c.dom.Element.
	 * @param el W3C element.
	 * @return XDElement object.
	 */
	public static final XDElement createXDElement(final Element el) {return new DefElement(el);}

	/** Creates named value.
	 * @param key the name of named value.
	 * @param x value of named value (may be null)
	 * @return named value.
	 */
	public static final XDNamedValue createXDNamedValue(final String key, final Object x) {
		return new DefNamedValue(key, x == null || x instanceof XDValue ? (XDValue) x : createXDValue(x));
	}

	/** Creates the empty XDContainer.
	 * @return the empty XDContainer.
	 */
	public static final XDContainer createXDContainer() {return new DefContainer();}

	/** Creates XDContainer from the object.
	 * @param o the object.
	 * @return the XDContainer object with item created from the object.
	 */
	public static final XDContainer createXDContainer(final Object o) {return new DefContainer(o);}

	/** Creates XDService object with JDBC support.
	 * @param url string with connection URL.
	 * @param user user name.
	 * @param passw password.
	 * @return XDService object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDService createSQLService(final String url, final String user, final String passw) {
		return new DefSQLService(url, user, passw);
	}

	/** Creates DefSQLService object with JDBC support.
	 * @param conn Database connection.
	 * @return XDService object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDService createSQLService(final Connection conn) {return new DefSQLService(conn);}

	/** Creates XDResultSet object from java.sql.ResultSet.
	 * @param x the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static final XDResultSet createXDResultSet(final ResultSet x) {return new DefSQLResultSet(x);}

	/** Creates XDResultSet object from java.sql.ResultSet extracting value of
	 * specified column from each raw.
	 * @param itemName name of column.
	 * @param sqlResultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static final XDResultSet createXDResultSet(final String itemName, final ResultSet sqlResultSet) {
		return new DefSQLResultSet(itemName, sqlResultSet);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param out where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if true then the XML header is written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 * @throws IOException if an error occurs.
	 */
	public static final XDXmlOutStream createXDXmlOutStream(final OutputStream out,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		return new DefXmlWriter(out, encoding, writeDocumentHeader);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param fname where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if true then the XML header is written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 * @throws IOException if an error occurs.
	 */
	public static final XDXmlOutStream createXDXmlOutStream(final String fname,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		return new DefXmlWriter(fname, encoding, writeDocumentHeader);
	}

	/** Creates XDParseResult object.
	 * @param src source which will be set as parsed object.
	 * @return XDParseResult object.
	 */
	public static final XDParseResult createParseResult(final String src) {return new DefParseResult(src);}

	/** Create XDValue object.
	 * @param o the object from which XDValue will be created.
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
	public static final XDValue createXDValue(final Object o) {return XBuilder.createXDValue(o);}

	private static final int BUFLEN = 20000;
	/** Create source Java code with given XDPool.
	 * @param w where to write.
	 * @param cls name of created class.
	 * @param pckg package of created class.
	 * @param xp XDPool to be written to class.
	 */
	public static void writeXDPoolClass(final Writer w, final String cls, final String pckg, final XDPool xp){
		PrintWriter pw = new PrintWriter(w);
		if (pckg != null && !pckg.trim().isEmpty()) {
			pw.println("package " + pckg + ";");
		}
		pw.println("public final class " + cls + " extends org.xdef.XDPoolFromClass {");
		pw.println("\tpublic static final org.xdef.XDPool getXDPool() {return getXDPool("
			+ (!pckg.trim().isEmpty()? pckg + '.' : "") + cls + ".class);}");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(xp);
			out.close();
			byte[] bytes = baos.toByteArray();
			int len = bytes.length;
			int pos = 0;
			for (int i = 1; ; i++) {
				int dif = len - pos;
				if (dif <= 0) {
					break;
				}
				int bufLen = dif > BUFLEN ? BUFLEN : dif;
				byte[] buf = new byte[bufLen];
				System.arraycopy(bytes, pos, buf, 0, bufLen);
				pos += bufLen;
				pw.println("\tprivate static final class B" + i + " {");
				String b = new String(SUtils.encodeBase64(buf));
				pw.println("\t\tprivate static final String b = \"" + b + "\";");
				pw.println("\t}");
			}
			pw.print("}");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}