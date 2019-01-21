package org.xdef;

import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import java.nio.charset.Charset;

/** *  Provides generation of {@link org.xdef.XDPool} from source
 * X-definitions. You can modify properties of compilation by parameters from
 * properties (see {@link org.xdef.XDConstants}). In most of cases you can
 * get {@link org.xdef.XDPool} directly by using of static methods of
 * {@link org.xdef.XDFactory} class. You can also create a XDBuilder when
 * you have to compile XDPool from different sources of X-definitions.
 * <p>The external methods must be static methods with compliant parameters
 * to X-definition technology. The list of external classes with external
 * methods can be passed as a parameter containing array of classes. If relevant
 * method is not found in the list of classes then the generator of XDPool
 * is searching the the method in the system class path.</p>
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
		final String[] params) {
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
		final URL[] params) {
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
		final File[] params) {
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
		final InputStream[] params) {
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
		final String[] sourceIds) {
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
		final Object... params)
		throws SRuntimeException {
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
		final Object... params)
		throws SRuntimeException {
		if (params == null || params.length == 0) {
			throw new SRuntimeException(XDEF.XDEF903);
		}
		XDBuilder builder = getXDBuilder(reporter, props);
		setParam(builder, params);
		return builder.compileXD();
	}

	/** Read the serialized XDPool from the input stream.
	 * @param stream input stream with X-definition.
	 * @return created XPool object.
	 * @throws IOException if an error occurs.
	 */
	public static XDPool readXDPool(InputStream stream) throws IOException {
		return org.xdef.impl.XPool.readXDPool(stream);
	}

	/** Read the serialized XDPool from the file.
	 * @param f File with saved XDPool.
	 * @return created XPool object.
	 * @throws IOException if an error occurs.
	 */
	public static XDPool readXDPool(File f) throws IOException {
		InputStream is = new FileInputStream(f);
		XDPool xp = readXDPool(is);
		is.close();
		return xp;
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
	public static XDService createSQLService(String url,
		String user,
		String passw) throws SRuntimeException {
		return new org.xdef.impl.code.DefSQLService(url, user, passw);
	}

	/** Creates DefSQLService object with JDBC support.
	 * @param conn Database connection.
	 * @return XDService object.
	 */
	public static XDService createSQLService(Connection conn)
		throws SRuntimeException {
		return new org.xdef.impl.code.DefSQLService(conn);
	}

	/** Creates XDResultSet object from java.sql.ResultSet.
	 * @param resultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static XDResultSet createXDResultSet(java.sql.ResultSet resultSet) {
		return new org.xdef.impl.code.DefSQLResultSet(resultSet);
	}

	/** Creates XDResultSet object from java.sql.ResultSet extracting value of
	 * specified column from each raw.
	 * @param itemName name of column.
	 * @param resultSet the ResultSet object.
	 * @return XDResultSet object.
	 */
	public static XDResultSet createXDResultSet(String itemName,
		java.sql.ResultSet resultSet) {
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

	/** Generate Java source class containing XDPool.
	 * You can get stored data if you invoke static method getXDPool() in
	 * generated class.
	 * @param xp XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code.
	 * @param className class name (file name will be "className.java").
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @throws IOException if an error occurs.
	 */
	public static void genXDPoolClass(final XDPool xp,
		final String dir,
		final String className,
		final String charset) throws IOException {
		final int BLOCKLEN = 24576; //Size of base64 string <= 32768 chars
		String pkgdir = dir;
		String clsName = className;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		xp.writeXDPool(baos);
		baos.close();
		final byte[] data = baos.toByteArray();
		final int codeLen = data.length;
		File f = new File(pkgdir);
		if (f.isDirectory()) {
			if (!pkgdir.endsWith("/")) {
				pkgdir += "/";
			}
		} else {
			throw new RuntimeException("The argument \""
				+ pkgdir + "\" must be a directory!");
		}
		String pckg;
		int ndx;
		if ((ndx = clsName.lastIndexOf('.')) > 0) {
			pckg = clsName.substring(0, ndx);
			clsName = clsName.substring(ndx + 1);
		} else {
			pckg = "";
		}
		f = pckg != null && pckg.length() > 0 ?
			new File(f, pckg.replace('.', '/')) : f;
		f.mkdirs();
		String name = clsName.endsWith(".java") ?
			clsName.substring(0, clsName.length() - 5) : clsName;
		FileOutputStream fos = new FileOutputStream(new File(f, name+".java"));
		OutputStreamWriter w = charset == null || charset.length() == 0 ?
			new OutputStreamWriter(fos) : new OutputStreamWriter(fos, charset);
		w.write(
"/* NOTE: this source code was generated by org.xdef.GenXComponent.\n"+
" * DO NOT MAKE ANY MODIFICATION!\n"+
" */\n");
		if (pckg.length() > 0) {
			w.write("package " + pckg +";\n\n");
		}
		w.write(
"import org.xdef.sys.SUtils;\n"+
"import org.xdef.XDFactory;\n"+
"import org.xdef.XDPool;\n"+
"import java.io.ByteArrayInputStream;\n"+
"\n"+
"/** This class contains encoded data generated from XDPool object.\n"+
" * See the static method getXDPool().\n"+
" */\n"+
"public final class " + name + " {\n"+
"\n"+
"\t/** Get XDPool from encoded data.\n"+
"\t * @return XDPool object.\n"+
"\t */\n"+
"\tpublic static final XDPool getXDPool() {\n"+
"\t\tif (xdp != null) return xdp;\n"+
"\t\ttry {\n");
		if (codeLen <= BLOCKLEN) {
			w.append(
"\t\t\treturn xdp = XDFactory.readXDPool(new ByteArrayInputStream(\n"+
"\t\t\t\tSUtils.decodeBase64(\"")
					.append(new String(SUtils.encodeBase64(data,false),
						Charset.forName("UTF-8")))
					.append("\")));\n"+
"\t\t} catch (Exception ex) {\n"+
"\t\t\tthrow new RuntimeException(ex);\n"+
"\t\t}\n"+
"\t}\n");
		} else {
			w.append(
"\t\t\tbyte[] b = new byte["+codeLen+"];\n"+
"\t\t\tSystem.arraycopy(SUtils.decodeBase64(\"")
				.append(new String(SUtils.encodeBase64(data,
					0, BLOCKLEN, false), Charset.forName("UTF-8")))
				.append("\"), 0, b, 0, "+BLOCKLEN+");\n");
			int offset = BLOCKLEN;
			for (int i = 1; offset < codeLen; i++, offset += BLOCKLEN) {
				int len = codeLen-offset>=BLOCKLEN ? BLOCKLEN : codeLen-offset;
				w.write("\t\t\tSystem.arraycopy(SUtils.decodeBase64(C"
					+i+".x()), 0, b, "+offset+", "+len+ ");\n");
			}
			w.write(
"\t\t\treturn xdp = XDFactory.readXDPool(new ByteArrayInputStream(b));\n"+
"\t\t} catch (Exception ex) {\n"+
"\t\t\tthrow new RuntimeException(ex);\n"+
"\t\t}\n"+
"\t}\n"+
"\n");
			int subclassIndex = 1;
			offset = BLOCKLEN;
			while (offset < codeLen) {
				String s =
"\tprivate static final class C" + subclassIndex + "{"+
"private static String x(){return\"";
				if (BLOCKLEN + offset < codeLen) {
					w.append(s)
						.append(new String(SUtils.encodeBase64(data,
							offset, BLOCKLEN, false), Charset.forName("UTF-8")))
						.append("\";}");
					if (subclassIndex > 0) {
						w.write('}');
					}
					w.write('\n');
					subclassIndex++;
					offset += BLOCKLEN;
				} else if (offset < codeLen) {
					w.append(s)
						.append(new String(SUtils.encodeBase64(data,
							offset, codeLen-offset, false),
							Charset.forName("UTF-8")))
						.append("\";}");
					offset = codeLen;
					if (subclassIndex > 0) {
						w.write('}');
					}
					w.write('\n');
					subclassIndex++;
				}
			}
		}
		w.append("\tprivate static XDPool xdp = null;\n}")
			.close();
	}
}