package org.xdef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.FUtils;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SRuntimeException;

/** Provides generation of {@link org.xdef.XDPool} from source
 * X-definitions. You can modify properties of compilation by parameters from
 * properties (see {@link org.xdef.XDConstants}). In most of cases you can
 * get {@link org.xdef.XDPool} directly by using of static methods of
 * {@link org.xdef.XDFactory} class. You can also create a XDBuilder when
 * you have to compile XDPool from different sources of X-definitions.
 * <p>The external methods must be static. The list of external classes with
 * the external methods can be passed as a parameter containing array of
 * classes. If relevant method is not found in the list of classes then the
 * generator of XDPool is searching the the method in the system class path.</p>
 *
 * Typical use of XDFactory:
 * <pre><tt>
 * // 1. Create XDPool from one source and no properties:
 * File xdef = ...
 * XDPool xd = XDFactory.compileXD(null, xdef);
 *  ...
 * // 2. Create XDPool from more sources and with properties:
 * File[] xdefs = ...
 * Properties props = new Properties();
 * props.setProperty(key, value); //see {@link org.xdef.XDConstants}
 * XDPool xd = XDFactory.compileXD(props, xdefs);
 * ...
 * </tt></pre>
 * @author Vaclav Trojan
 */
public final class XDFactory {

	/** Prevent user to instantiate this class.*/
	private XDFactory() {
		//Internal error&{0}{: }
		throw new SRuntimeException(SYS.SYS066, "Instantiate not accepted");
	}

	/** Get version of this implementation of X-definition.
	 * @return version of this implementation of X-definition.
	 */
	public static String getXDVersion() {
		return XDConstants.BUILD_VERSION + " (" + XDConstants.BUILD_DATE + "]";
	}

	/** Creates instance of XDBuilder with properties.
	 * @param props Properties or <tt>null</tt> -
	 * see {@link org.xdef.XDConstants}.
	 * @return created XDBuilder.
	 */
	public static XDBuilder getXDBuilder(final Properties props) {
		return getXDBuilder(null, props);
	}

	/** Creates instance of XDBuilder with properties.
	 * @param reporter the ReportWriter to be used for error reporting.
	 * @param props Properties or <tt>null</tt> -
	 * see {@link org.xdef.XDConstants}.
	 * @return created XDBuilder.
	 */
	public static XDBuilder getXDBuilder(final ReportWriter reporter,
		final Properties props) {
		XDBuilder result = new org.xdef.impl.XBuilder(reporter, props);
		return result;
	}

	@SuppressWarnings("deprecation")
	/** Set object from parameter to be prepared for compiling.
	 * @param b Instance of XDBuilder.
	 * @param param Object to be analyzed for compiling.
	 */
	private static void setParam(final XDBuilder b, final Object param) {
		if (param == null) {
			return;
		}
		if (param instanceof Object[][]) {
			Object[][] x = (Object[][]) param;
			for (Object[] y : x) {
				setParam(b, y);
			}
			return;
		}
		if (param instanceof Object[]) {
			Object[] x = (Object[]) param;
			if (x.length >  0 && x[0] instanceof Object[][]) {
				setParam(b, (Object[][]) x[0]);
				for (int i = 1; i < x.length; i++) {
					setParam(b, x[i]);
				}
				return;
			}
			if (x.length == 2) {
				if (x[0] instanceof InputStream && x[1] instanceof String) {
					b.setSource((InputStream) x[0], (String) x[1]);
					return;
				}
				if (x[0] instanceof Object[] && x[1] instanceof String[]) {
					Object[] x1 = (Object[]) x[0];
					String[] x2 = (String[]) x[1];
					boolean ids = true;
					for (int i = 0; i < x1.length; i++) {
						Object y =  x1[i];
						if (y instanceof String) {
							 if (((String) y).charAt(0) != '<') {
								ids = false;
								break;
							}
						} else if (!(y instanceof InputStream)) {
							ids = false;
							break;
						}
						String s =  x2[i];
						if (s == null || s.charAt(0) == '<') {
							ids = false;
							break;
						}
					}
					if (ids) {
						// input data and source names
						for (int i = 0; i < x1.length; i++) {
							if (x1[i] instanceof String) {
								b.setSource((String) x1[i], x2[i]);
							} else {
								b.setSource((InputStream) x1[i], x2[i]);
							}
						}
						return;
					}
				}
			}
			for (Object o : x) {
				setParam(b, o);
			}
		} else if (param instanceof Class) {
			b.setExternals((Class) param);
		} else if (param instanceof Class[]) {
			Class[] x = (Class[]) param;
			for (Class c : x) {
				b.setExternals(c);
			}
		} else if (param instanceof String[]) {
			String[] x = (String[]) param;
			for (String s : x) {
				b.setSource(s);
			}
		} else if (param instanceof String) {
			b.setSource((String) param);
		} else if (param instanceof File[]) {
			File[] x = (File[]) param;
			for (File f : x) {
				b.setSource(f);
			}
		} else if (param instanceof File) {
			b.setSource((File) param);
		} else if (param instanceof URL[]) {
			URL[] x = (URL[]) param;
			for (URL u : x) {
				b.setSource(u);
			}
		} else if (param instanceof URL) {
			b.setSource((URL) param);
		} else if ((param instanceof InputStream[])) {
			InputStream[] x = (InputStream[]) param;
			for (InputStream i : x) {
				b.setSource(i, null);
			}
		} else if ((param instanceof InputStream)) {
			b.setSource((InputStream) param, null);
		} else if ((param instanceof Object[])) {
			Object[] x = (Object[]) param;
			if (x.length == 2 && (x[0] instanceof InputStream)
				&& x[1] instanceof String) {
				b.setSource((InputStream) x[0], (String) x[1]);
			} else {
				//Incorrect parameter of compiler of X-definitions&{0}{: }
				throw new SRuntimeException(XDEF.XDEF904, param.getClass());
			}
		} else {
			//Incorrect parameter of compiler of X-definitions&{0}{: }
			throw new SRuntimeException(XDEF.XDEF904, param.getClass());
		}
	}

	/** Compile XDPool from sources.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of strings with X-definition file names.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final String[] params) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Compile XDPool from URLs.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of URLs with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final URL[] params) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Compile XDPool from files.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of files with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final File[] params) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Compile XDPool from InputStreams.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of files with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final InputStream[] params) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Compile XDPool from sources and assign the sourceId to each source.
	 * @param props Properties or <tt>null</tt>.
	 * @param sources array with source data with X-definitions source data.
	 * (The type of items can only be either the InputStreams or the String
	 * containing an XML document).
	 * @param sourceIds array with sourceIds (corresponding to the items
	 * in the argument sources).
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final Object[] sources,
		final String[] sourceIds) throws SRuntimeException {
		XDBuilder builder = XDFactory.getXDBuilder(props);
		setParam(builder, new Object[] {sources, sourceIds});
		return builder.compileXD();
	}

	/** Compile XDPool from source.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of sources, source pairs or external classes.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final Properties props,
		final Object... params)	throws SRuntimeException {
		return compileXD((ReportWriter) null, props, params);
	}

	/** Compile XDPool from source.
	 * @param reporter the ReportWriter to be used for error reporting.
	 * @param props Properties or <tt>null</tt>.
	 * @param params list of sources, source pairs or external classes.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDPool compileXD(final ReportWriter reporter,
		final Properties props,
		final Object... params)	throws SRuntimeException {
		if (params == null || params.length == 0) {
			throw new SRuntimeException(XDEF.XDEF903);
		}
		XDBuilder builder = getXDBuilder(reporter, props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Parse XML with X-definition declared in source input stream.
	 * @param source where to read XML.
	 * @param reporter used for error messages or <tt>null</tt>.
	 * @return created XDDocument object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDDocument xparse(final InputStream source,
		final ReportWriter reporter) throws SRuntimeException {
		return org.xdef.impl.XBuilder.xparse(source, reporter);
	}

	/** Parse XML with X-definition declared in source.
	 * @param source URL, pathname direct to XML or direct XML.
	 * @param reporter used for error messages or <tt>null</tt>.
	 * @return created XDDocument object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDDocument xparse(final String source,
		final ReportWriter reporter) throws SRuntimeException {
		return org.xdef.impl.XBuilder.xparse(source, reporter);
	}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param xmlFormat if <tt>true</tt> the input data are in XML format,
	 * otherwise in string format.
	 * @return the XDInput object.
	 */
	public static XDInput createXDInput(final InputStream value,
		final boolean xmlFormat) {
		return new org.xdef.impl.code.DefInStream(value, xmlFormat);
	}

	/** Creates XDInput from InputStream.
	 * @param value the stream.
	 * @param xmlFormat if <tt>true</tt> the input data are in XML format,
	 * otherwise in string format.
	 * @return the XDInput object.
	 */
	public static XDInput createXDInput(final InputStreamReader value,
		boolean xmlFormat) {
		return new org.xdef.impl.code.DefInStream(value, xmlFormat);
	}

	/** Creates XDInput from InputStream.
	 * @param value ReportReader.
	 * @return the XDInput object.
	 */
	public static XDInput createXDInput(final ReportReader value) {
		return new org.xdef.impl.code.DefInStream(value);
	}

	/** Creates XDOutput from reporter.
	 * @param value the reporter.
	 * @return the XDOutput object.
	 */
	public static XDOutput createXDOutput(final ReportWriter value) {
		return new org.xdef.impl.code.DefOutStream(value);
	}

	/** Creates XDOutput from Writer.
	 * @param value Writer object.
	 * @param xmlFormat if <tt>true</tt> the output will be in XML format,
	 * otherwise in string format.
	 * @return the XDOutput object.
	 */
	public static XDOutput createXDOutput(final Writer value,
		final boolean xmlFormat) {
		return new org.xdef.impl.code.DefOutStream(value, xmlFormat);
	}

	/** Creates XDOutput from PrintStream.
	 * @param value PrintStream object.
	 * @return the XDOutput object.
	 */
	public static XDOutput createXDOutput(final PrintStream value) {
		return new org.xdef.impl.code.DefOutStream(value);
	}

	/** Creates XDElement from org.w3c.dom.Element.
	 * @param el W3C element.
	 * @return XDElement object.
	 */
	public static XDElement createXDElement(Element el) {
		return new org.xdef.impl.code.DefElement(el);
	}

	/** Creates named value.
	 * @param key the name of named value.
	 * @param value value of named value (may be null)
	 * @return named value.
	 */
	public static XDNamedValue createXDNamedValue(String key, Object value) {
		return new org.xdef.impl.code.DefNamedValue(key,
			value == null || value instanceof XDValue ?
				(XDValue) value : createXDValue(value));
	}

	/** Creates the empty XDContainer.
	 * @return the empty XDContainer.
	 */
	public static XDContainer createXDContainer() {
		return new org.xdef.impl.code.DefContainer();
	}

	/** Creates XDContainer from the object.
	 * @param value the object.
	 * @return the XDContainer object with item created from the object.
	 */
	public static XDContainer createXDContainer(Object value) {
		return new org.xdef.impl.code.DefContainer(value);
	}

	/** Creates XDService object with JDBC support.
	 * @param url string with connection URL.
	 * @param user user name.
	 * @param passw password.
	 * @return XDService object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDService createSQLService(final String url,
		final String user,
		final String passw) throws SRuntimeException {
		return new org.xdef.impl.code.DefSQLService(url, user, passw);
	}

	/** Creates DefSQLService object with JDBC support.
	 * @param conn Database connection.
	 * @return XDService object.
	 */
	public static XDService createSQLService(final Connection conn)
		throws SRuntimeException {
		return new org.xdef.impl.code.DefSQLService(conn);
	}

	/** Creates XDResultSet object from java.sql.ResultSet.
	 * @param resultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static XDResultSet createXDResultSet(
		final java.sql.ResultSet resultSet) {
		return new org.xdef.impl.code.DefSQLResultSet(resultSet);
	}

	/** Creates XDResultSet object from java.sql.ResultSet extracting value of
	 * specified column from each raw.
	 * @param itemName name of column.
	 * @param resultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static XDResultSet createXDResultSet(final String itemName,
		final java.sql.ResultSet resultSet) {
		return new org.xdef.impl.code.DefSQLResultSet(itemName,resultSet);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param writer where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 */
	public static XDXmlOutStream createXDXmlOutStream(final Writer writer,
		final String encoding,
		final boolean writeDocumentHeader) {
		return new org.xdef.impl.code.DefXmlWriter(
			writer, encoding,writeDocumentHeader);
	}

	/** Creates XDXmlStream writer object from java.io.Writer.
	 * @param filename where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @return XDXmlOutStream object.
	 * @throws IOException if an error occurs.
	 */
	public static XDXmlOutStream createXDXmlOutStream(final String filename,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		return new org.xdef.impl.code.DefXmlWriter(
			filename, encoding, writeDocumentHeader);
	}

	/** Creates XDParseResult object.
	 * @param source source which will be set as parsed object.
	 * @return XDParseResult object.
	 */
	public static XDParseResult createParseResult(final String source) {
		return new org.xdef.impl.code.DefParseResult(source);
	}

	/** Create XDValue object.
	 * @param obj the object from which XDValue will be created.
	 * It may be one of:
	 * <ul>
	 * <li>XDValue</li>
	 * <li>String</li>
	 * <li>Short, Integer, Long</li>
	 * <li>Float, Double</li>
	 * <li>BigDecimal</li>
	 * <li>BNFGrammar</li>
	 * <li>BNFRule</li>
	 * <li>Boolean</li>
	 * <li>Calendar, SDatetime</li>
	 * <li>SDuration</li>
	 * </ul>
	 * @return new XDValue object.
	 * @throws RuntimeException if the object from argument is not possible
	 * to convert to XDValue object.
	 */
	public static XDValue createXDValue(final Object obj) {
		return org.xdef.impl.XBuilder.createXDValue(obj);
	}

	/** Write the XDPool to output stream.
	 * @param out output stream where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static void writeXDPool(final OutputStream out,final XDPool xp)
		throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(xp);
		oout.close();
	}

	/** Write the XDPool to output stream.
	 * @param file file where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static void writeXDPool(final File file, final XDPool xp)
		throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		writeXDPool(fos, xp);
	}

	/** Write the XDPool to output stream.
	 * @param fname pathname where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static void writeXDPool(final String fname, final XDPool xp)
		throws IOException {
		FileOutputStream fos = new FileOutputStream(fname);
		writeXDPool(fos, xp);
	}

	/** Read the XDPool from the input stream.
	 * @param in input stream with X-definition.
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(final InputStream in)
		throws IOException {
		try {
			ObjectInputStream oin = new ObjectInputStream(in);
			XDPool result = (XDPool) oin.readObject();
			oin.close();
			return result;
		} catch (ClassNotFoundException ex) {
			in.close();
			throw new IOException(ex);
		}
	}

	/** Read the XDPool from the input stream.
	 * @param file file with X-definition.
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(final File file) throws IOException {
		return readXDPool(new FileInputStream(file));
	}

	/** Read the XDPool from the input stream.
	 * @param fname pathname of file or string with URL with X-definition (it
	 * may be also "classpath://.....").
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(final String fname)throws IOException{
		if (!new File(fname).exists() && fname.indexOf("://") > 0) {
			return readXDPool(FUtils.getExtendedURL(fname).openStream());
		} else {
			return readXDPool(new FileInputStream(fname));
		}
	}

	/** Read the XDPool from the input stream.
	 * @param url URL where is data with XDPool.
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public final static XDPool readXDPool(final URL url) throws IOException {
		return readXDPool(url.openStream());
	}

}