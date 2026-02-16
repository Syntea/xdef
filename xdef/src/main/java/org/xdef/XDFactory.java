package org.xdef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import org.xdef.impl.XBuilder;
import org.xdef.msg.XDEF;
import org.xdef.sys.FUtils;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXpathExpr;
import org.xdef.xml.KXqueryExpr;

/** Provides generation of {@link org.xdef.XDPool} from sources of X-definition. You can modify properties of
 * compilation by parameters from properties (see {@link org.xdef.XDConstants}). In most of cases you can get
 * {@link org.xdef.XDPool} directly by using of static methods of {@link org.xdef.XDFactory} class. You can also create
 * a XDBuilder when you have to compile XDPool from different sources of X-definition.
 * <p>The external methods must be static. The list of external classes with the external methods can be passed as
 * a parameter containing array of classes. If relevant method is not found in the list of classes then the generator
 * of XDPool is searching the the method in the system class path.
 * <p>Typical use of XDFactory:
 * <pre><code>
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
 * </code></pre>
 * @author Vaclav Trojan
 */
public final class XDFactory extends XDTools {

	/** Creates instance of XDBuilder with properties.
	 * @param props Properties or null - see {@link org.xdef.XDConstants}.
	 * @return created XDBuilder.
	 */
	public static final XDBuilder getXDBuilder(final Properties props) {return getXDBuilder(null, props);}

	/** Creates instance of XDBuilder with properties.
	 * @param reporter the ReportWriter to be used for error reporting.
	 * @param props Properties or null - see {@link org.xdef.XDConstants}.
	 * @return created XDBuilder.
	 */
	public static final XDBuilder getXDBuilder(final ReportWriter reporter, final Properties props) {
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
			for (Object[] y: x) {
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
		} else if (param instanceof String[]) {
			String[] x = (String[]) param;
			for (String s: x) {
				b.setSource(s);
			}
		} else if (param instanceof String) {
			b.setSource((String) param);
		} else if (param instanceof File[]) {
			File[] x = (File[]) param;
			for (File f: x) {
				b.setSource(f);
			}
		} else if (param instanceof File) {
			b.setSource((File) param);
		} else if (param instanceof URL[]) {
			URL[] x = (URL[]) param;
			for (URL u: x) {
				b.setSource(u);
			}
		} else if (param instanceof URL) {
			b.setSource((URL) param);
		} else if ((param instanceof InputStream[])) {
			InputStream[] x = (InputStream[]) param;
			for (InputStream i: x) {
				b.setSource(i, null);
			}
		} else if ((param instanceof InputStream)) {
			b.setSource((InputStream) param, null);
		} else if ((param instanceof Object[])) {
			Object[] x = (Object[]) param;
			if (x.length == 2 && (x[0] instanceof InputStream) && x[1] instanceof String) {
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
	 * @param props Properties or null.
	 * @param pars list of strings with X-definition file names.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final String... pars) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, pars);
		return builder.compileXD();
	}

	/** Compile XDPool from URLs.
	 * @param props Properties or null.
	 * @param pars list of URLs with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final URL... pars) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, pars);
		return builder.compileXD();
	}

	/** Compile XDPool from files.
	 * @param props Properties or null.
	 * @param pars list of files with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final File... pars) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, pars);
		return builder.compileXD();
	}

	/** Compile XDPool from InputStreams.
	 * @param props Properties or null.
	 * @param pars list of files with X-definition sources.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final InputStream... pars) throws SRuntimeException {
		XDBuilder builder = getXDBuilder(props);
		setParam(builder, pars);
		return builder.compileXD();
	}

	/** Compile XDPool from sources and assign the sourceId to each source.
	 * @param props Properties or null.
	 * @param sources array with source data with X-definition sources. (The type of items can only be either
	 * an InputStreams or a String containing an XML document).
	 * @param srcIds array with sourceIds (corresponding to the items in the argument sources).
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final Object[] sources, final String[] srcIds)
		throws SRuntimeException {
		XDBuilder builder = XDFactory.getXDBuilder(props);
		setParam(builder, new Object[] {sources, srcIds});
		return builder.compileXD();
	}

	/** Compile XDPool from source.
	 * @param props Properties or null.
	 * @param pars list of sources, source pairs or external classes.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final Properties props, final Object... pars) throws SRuntimeException {
		return compileXD((ReportWriter) null, props, pars);
	}

	/** Compile XDPool from source.
	 * @param r the ReportWriter to be used for error reporting.
	 * @param props Properties or null.
	 * @param pars list of sources, source pairs or external classes.
	 * @return generated XDPool.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDPool compileXD(final ReportWriter r, final Properties props, final Object... pars)
		throws SRuntimeException {
		if (pars == null || pars.length == 0) {
			throw new SRuntimeException(XDEF.XDEF903); //X-definition source is missing or incorrect&{0}{: }
		}
		XDBuilder builder = getXDBuilder(r, props);
		setParam(builder, pars);
		return builder.compileXD();
	}

	/** Parse XML with X-definition declared in source input stream.
	 * @param source where to read XML.
	 * @param r used for error messages or null.
	 * @return created XDDocument object.
	 * @throws RuntimeException if an error occurs.
	 */
	public static final XDDocument xparse(final InputStream source, final ReportWriter r) throws SRuntimeException {
		return XBuilder.xparse(source, r);
	}

	/** Parse XML with X-definition declared in source.
	 * @param source URL, pathname direct to XML or direct XML.
	 * @param r used for error messages or null.
	 * @return created XDDocument object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final XDDocument xparse(final String source, final ReportWriter r) throws SRuntimeException{
		return XBuilder.xparse(source, r);
	}

	/** Write the XDPool to output stream.
	 * @param out output stream where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeXDPool(final OutputStream out, final XDPool xp) throws IOException {
		try (ObjectOutputStream oout = new ObjectOutputStream(out)) {
			oout.writeObject(xp);
		}
	}

	/** Write the XDPool to output stream.
	 * @param file file where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeXDPool(final File file, final XDPool xp) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		writeXDPool(fos, xp);
	}

	/** Write the XDPool to output stream.
	 * @param fname pathname where to write XDPool.
	 * @param xp XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeXDPool(final String fname, final XDPool xp) throws IOException {
		FileOutputStream fos = new FileOutputStream(fname);
		writeXDPool(fos, xp);
	}

	/** Read the XDPool from the input stream.
	 * @param in input stream with X-definition.
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final XDPool readXDPool(final InputStream in) throws IOException {
		try {
			try (ObjectInputStream x = new ObjectInputStream(in)) {
				return (XDPool) x.readObject();
			}
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
	public static final XDPool readXDPool(final File file) throws IOException {
		return readXDPool(new FileInputStream(file));
	}

	/** Read the XDPool from the input stream.
	 * @param fname pathname of file or string with URL with X-definition (it may be also "classpath://...").
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final XDPool readXDPool(final String fname) throws IOException {
		return !new File(fname).exists() && fname.indexOf("://") > 0
			? readXDPool(FUtils.getExtendedURL(fname).openStream()) : readXDPool(new FileInputStream(fname));
	}

	/** Read the XDPool from the input stream.
	 * @param u URL where is data with XDPool.
	 * @return XDPool object.
	 * @throws IOException if an error occurs.
	 */
	public static final XDPool readXDPool(final URL u) throws IOException {return readXDPool(u.openStream());}

	/** Check if XQuery implementation is available.
	 * @return true if XQuery implementation is available.
	 */
	public static final boolean isXQuerySupported() {return KXqueryExpr.isXQueryImplementation();}

	/** Check if XPath2 implementation is available.
	 * @return true if XPath2 implementation is available.
	 */
	public static final boolean isXPath2Supported() {return KXpathExpr.isXPath2();}
}
