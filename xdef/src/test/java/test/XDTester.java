package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.compile.PNode;
import org.xdef.impl.compile.XPreCompiler;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.getClassSource;
import static org.xdef.sys.STester.printThrowable;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Support of tests.
 * @author Vaclav Trojan
 */
public abstract class XDTester extends STester {
	public static String _xdNS = XDConstants.XDEF42_NS_URI;
	public static XDPool _xdOfxd;
	public static boolean _fulltestMode;
	private final Properties _props = new Properties();
	private boolean _convertXD;
	private boolean _chkSyntax;

	/** Creates a new instance of TestAbstract */
	public XDTester() {
		super();
		resetTester();
		_chkSyntax = _fulltestMode;
		_convertXD = _fulltestMode;
	}

	/** Reset tester properties */
	public final void resetProperties() {
		Report.setLanguage("en"); //localize
		setProperty(XDConstants.XDPROPERTY_DOCTYPE, XDConstants.XDPROPERTYVALUE_DOCTYPE_TRUE);
		if (_fulltestMode) {
			setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
				XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);
		} else {
			setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
				XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_FALSE);
		}
		setProperty(XDConstants.XDPROPERTY_XINCLUDE, XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		setProperty(XDConstants.XDPROPERTY_DEBUG, XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
		setProperty(XDConstants.XDPROPERTY_DEBUG_OUT, null);
		setProperty(XDConstants.XDPROPERTY_DEBUG_IN, null);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);
		setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
		setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
		setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE);
		setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, null);
	}
	/** Reset tester parameters */
	public final void resetTester() {
		_props.clear();
		_chkSyntax = _fulltestMode;
		_convertXD = _fulltestMode;
		setChkSyntax(false);
		resetProperties();
	}
	/** Get tester properties.
	 * @return tester properties.
	 */
	public final Properties getProperties() {return _props;}
	/** Set mode to check X-definition syntax.
	 * @param x if rue them check syntax, otherwise skip checking.
	 */
	public final void setChkSyntax(final boolean x) {_chkSyntax = x;}
	/** Get mode to check X-definition syntax.
	 * @return check X-definition syntax mode.
	 */
	public final boolean getChkSyntax() {return _chkSyntax;}
	/** Get value of full test mode.
	 * @return x value of full test mode.
	 */
	public final static boolean getFulltestMode() {return _fulltestMode;}
	/** Set mode of full test.
	 * @param x full test mode.
	 */
	public final static void setFulltestMode(boolean x) {_fulltestMode = x;}
	/** Compile X-definition od X-definitions.
	 * @throws RuntimeException if compilation fails.
	 */
	private void genXdOfXd() {
		if (_xdOfxd == null) {// if _xdOfxd is null create it
			_xdOfxd= XDFactory.compileXD(null,"classpath://org.xdef.impl.compile.XdefOfXdef*.xdef");
		}
	}
	/** Set tester property value.
	 * @param key name of property.
	 * @param value value of property.
	 * @return original value of property
	 */
	public final String setProperty(final String key, final String value) {
		String newKey = key.replace('.', '_');
		Object result = _props.remove(key);
		if (value != null) {
			_props.setProperty(newKey, value);
		}
		return (String) result;
	}
	/** Get value of tester property.
	 * @param key name of property.
	 * @return value of property.
	 */
	public final String getProperty(final String key) {
		return _props.getProperty(key.replace('.', '_'));
	}
	/** Set mode get compiled XDPool form converted binary data.
	 * @param genObj if true compiled XDPool read form converted binary data.
	 */
	public final void setGenObjFile(final boolean genObj) {_convertXD = genObj;}
	/** Get mode get compiled XDPool form converted binary data.
	 * @return mode get compiled XDPool form converted binary data.
	 */
	public final boolean getGenObjFile() {return _convertXD;}
	/** Remove macros from the element.
	 * @param el element from which remove macros.
	 */
	private static void removeMacros(final Element el) {
		NodeList nl = el.getElementsByTagNameNS(el.getNamespaceURI(), "macro");
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Node n = nl.item(i);
			n.getParentNode().removeChild(n); // remove macros
		}
	}
	/** Check X-definitions.
	 * @param xdefs array with strins with sources of X-definitions.
	 * @return reporter with error/warnings.
	 */
	private ArrayReporter chkSyntax(final String... xdefs) {return chkSyntax((Object[]) xdefs);}
	/** Check X-definitions.
	 * @param xdefs array with source files X-definitions.
	 * @return reporter with error/warnings.
	 */
	private ArrayReporter chkSyntax(final File... xdefs) {return chkSyntax((Object[]) xdefs);}
	/** Check X-definitions.
	 * @param xdefs array with X-definitions.
	 * @return reporter with error/warnings.
	 */
	public final ArrayReporter chkSyntax(final Object... xdefs) {
		ArrayReporter reporter = new ArrayReporter();
		if (!_chkSyntax) {
			return reporter;
		}
		genXdOfXd();
		XPreCompiler xpc = new XPreCompiler(reporter, null, (byte) 0, false, false, true);
		for (int i = 0; i < xdefs.length; i++) {
			Object x = xdefs[i];
			if (x instanceof String) {
				String s = (String) x;
				if (s.startsWith("<")) {
					xpc.parseString(s);
				} else if (s.startsWith("//") || (s.indexOf(":/") > 2 && s.indexOf(":/") < 12)) {
					try {
						for (String y: SUtils.getSourceGroup(s)) {
							xpc.parseURL(SUtils.getExtendedURL(y));
						}
					} catch (Exception ex) {}
				} else {
					for (File xf1 : SUtils.getFileGroup(s)) {
						xpc.parseFile(xf1);
					}
				}
			} else if (x instanceof File) {
				xpc.parseFile((File) x);
			} else if (x instanceof URL) {
				xpc.parseURL((URL) x);
			} else if (x instanceof InputStream) {
				String s = "STREEM_"+1;
				if (i+1 < xdefs.length && xdefs[i+1] instanceof String) {
					String t = (String) xdefs[i+1];
					if (t.charAt(0) != '<') {
						s = t;
						i++;
					}
				}
				xpc.parseStream((InputStream) x, s);
				try {
					((InputStream) x).reset();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			} else if (x instanceof String[]) {
				for (String x1 : (String[]) x) {
					xpc.parseString(x1);
				}
			} else if (x instanceof File[]) {
				for (File xf1 : (File[]) x) {
					xpc.parseFile(xf1);
				}
			} else {
				throw new RuntimeException("Incorrect parameter type: " + x);
			}
		}
		xpc.prepareMacros();
		List<PNode> x;
		x = xpc.getPDeclarations();
		for (PNode y: x) {
			Element el = y.toXML();
			removeMacros(el);
			String s = KXmlUtils.nodeToString(el, true);
			_xdOfxd.createXDDocument("").xparse(s, reporter);
		}
		x = xpc.getPCollections();
		for (PNode y: x) {
			Element el = y.toXML();
			removeMacros(el);
			String s = KXmlUtils.nodeToString(el, true);
			_xdOfxd.createXDDocument("").xparse(s, reporter);
		}
		x = xpc.getPXDefs();
		for (PNode y: x) {
			Element el = y.toXML();
			removeMacros(el);
			String s = KXmlUtils.nodeToString(el, true);
			_xdOfxd.createXDDocument("").xparse(s, reporter);
		}
		return reporter;
	}
	/** Returns the available model represented by given name or null if model is not available.
	 * @param xdef XDefinition.
	 * @param key The name of model.
	 * @return The required model or null.
	 */
	private static XMElement getXElement(final XMDefinition xdef, final String key) {
		int ndx = key.lastIndexOf('#');
		String lockey;
		XMDefinition def;
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = key;
			def = xdef;
		} else {
			def = xdef.getXDPool().getXMDefinition(key.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = key.substring(ndx + 1);
		}
		for (XMElement xel : def.getModels()) {
			if (lockey.equals(xel.getName())) {
				return xel;
			}
		}
		return null;
	}
	/** Construct XML element from X-definition.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName name of X-definition or null.
	 * @param reporter ArrayReporter or null.
	 * @param el XML element used as context or null.
	 * @param stdout output stream used as stdout.
	 * @return constructed XML element.
	 */
	private Element createElement(final XDPool xp,
		final String defName,
		final ReportWriter reporter,
		final Element el,
		final DefOutStream stdout) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		xd.setStdOut(stdout);
		String qname;
		String nsURI;
		if (el != null) {
			xd.setXDContext(el);
			nsURI = el.getNamespaceURI();
			qname = el.getTagName();
		} else {
			XMElement xe;
			XMDefinition xdf = xp.getXMDefinition(defName);
			if ((xe = getXElement(xdf, (qname = xdf.getName()))) == null) {
				//Model of element '&{0}' is missing in XDefinition&{1}{ }
				throw new SRuntimeException(XDEF.XDEF601, qname,xdf.getName());
			}
			nsURI = xe.getNSUri();
		}
		xd.xcreate(new QName(nsURI, qname), reporter);
		return xd.getElement();
	}
	/** Check XDPool conversion to and return XDPool created from converted stream (if _genObj
	 * is false the conversion is skipped).
	 * @param xp XDPool to be checked
	 * @return XDPool read from converted stream.
	 * @throws RuntimeException if an error occurs,
	 */
	public final XDPool checkExtObjects(final XDPool xp) {
		if (!_convertXD) {return xp;}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XDFactory.writeXDPool(baos, xp);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			XDPool xp1 = XDFactory.readXDPool(bais);
			baos = new ByteArrayOutputStream();
			XDFactory.writeXDPool(baos, xp1);
			bais = new ByteArrayInputStream(baos.toByteArray());
			xp1 =  XDFactory.readXDPool(bais);
			return xp1;
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch(Error e) {
			throw new RuntimeException(e);
		}
	}

	/** Process XML data with X-definition.
	 * @param xdefs X-definition sources.
	 * @param xml XML data (in create mode it may be null).
	 * @param defName name of X-definition or null.
	 * @param out stream used as stdout (may be null).
	 * @param reporter ArrayReporter or null.
	 * @param mode 'P' => parse, 'C' => create
	 * @return xml element from processed X-definition.
	 */
	final public Element test(final String[] xdefs,
		final String xml,
		final String defName,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode) {
		System.out.flush();
		System.err.flush();
		try {
			if (reporter != null) {
				reporter.clear();
			}
			chkSyntax(xdefs).checkAndThrowErrors();
			XDBuilder xb = XDFactory.getXDBuilder(_props);
			xb.setSource(xdefs, null);
			XDPool xp = xb.compileXD();
			xp = checkExtObjects(xp);
			if (out != null) {
				out.flush();
			}
			if (xdefs == null) {
				throw new Exception("XDefinition " + defName + " doesn't exist");
			}
			DefOutStream stdout;
			if (out == null) {
				stdout = null;
			} else {
				stdout =new DefOutStream(new OutputStreamWriter(out), false);
			}
			if (mode == 'C') {
				Element el = null;
				if (xml != null) {
					el = KXmlUtils.parseXml(xml, true).getDocumentElement();
				}
				return createElement(xp, defName, reporter, el, stdout);
			} else {
				XDDocument xd = xp.createXDDocument(defName);
				xd.setProperties(_props);
				if (stdout != null) {
					xd.setStdOut(stdout);
				}
				return xd.xparse(xml, "", reporter);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return null;
	}
	/** Process XML data with X-definition.
	 * @param xdef X-definition source.
	 * @param xml XML data (in create mode it may be null).
	 * @param defName name of X-definition or null.
	 * @param out stream used as stdout (may be null).
	 * @param reporter ArrayReporter or null.
	 * @param mode if 'C' then run construction mode.
	 * @return xml element from processed X-definition.
	 */
	final public Element test(final String xdef,
		final String xml,
		final String defName,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode) {
		return test(new String[] {xdef}, xml, defName, out, reporter, mode);
	}
	/** Process XML data with X-definition.
	 * @param xdefs X-definition sources.
	 * @param xml stream with XML data (in create mode it may be null).
	 * @param defName name of X-definition or null.
	 * @param out stream used as stdout (may be null).
	 * @param reporter ArrayReporter or null.
	 * @param mode 'P' => parse, 'C' => create.
	 * @return xml element from processed X-definition.
	 */
	@SuppressWarnings("deprecation")
	public final Element test(final File[] xdefs,
		final InputStream xml,
		final String defName,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode) {
		try {
			chkSyntax(xdefs).checkAndThrowErrors();
			if (reporter != null) {
				reporter.clear();
			}
			XDBuilder xb = XDFactory.getXDBuilder(_props);
			xb.setSource(xdefs);
			XDPool xp = xb.compileXD();
			xp = checkExtObjects(xp);
			if (out != null) {
				out.flush();
			}
			if (xdefs == null) {
				throw new Exception("XDefinition " + defName + " doesn't exist");
			}
			DefOutStream stdout;
			if (out == null) {
				stdout = null;
			} else {
				stdout = new DefOutStream(new OutputStreamWriter(out), false);
			}
			if (mode == 'C') {
				Element el = null;
				if (xml != null) {
					el = KXmlUtils.parseXml(xml, true).getDocumentElement();
				}
				return createElement(xp, defName , reporter, el, stdout);
			} else {
				XDDocument xd = xp.createXDDocument(defName);
				xd.setProperties(_props);
				if (stdout != null) {
					xd.setStdOut(stdout);
				}
				return xd.xparse(xml, "", reporter);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		return null;
	}

	/** Process XML data with X-definition.
	 * @param xdef X-definition source.
	 * @param xml stream with XML data (in create mode it may be null).
	 * @param defName name of X-definition or null.
	 * @param out stream used as stdout (may be null).
	 * @param reporter ArrayReporter or null.
	 * @return xml element from processed X-definition.
	 */
	final public Element test(final File xdef,
		final InputStream xml,
		final String defName,
		final OutputStream out,
		final ReportWriter reporter) {
		// if reporter is not null skipp checking of result of data processing*/
		return test(new File[]{xdef}, xml, defName, out, reporter);
	}
	/** Process XML data with X-definition.
	 * @param xdefs X-definition sources.
	 * @param xml stream with XML data (in create mode it may be null).
	 * @param defName name of X-definition or null.
	 * @param out stream used as stdout (may be null).
	 * @param reporter ArrayReporter or null.
	 * @return xml element from processed X-definition.
	 */
	final public Element test(final File[] xdefs,
		final InputStream xml,
		final String defName,
		final OutputStream out,
		final ReportWriter reporter) {
		try {
			ReportWriter myreporter = reporter;
			if (reporter == null) {
				myreporter = new ArrayReporter();
			}
			Element result = test(xdefs, xml, defName, out, reporter, 'P');
			if (reporter == null) {
				if (myreporter.errors()) {
					StringWriter sw = new StringWriter();
					xml.reset();
					ReportReader rri = myreporter.getReportReader();
					ReportPrinter.printListing(sw, new java.io.InputStreamReader(xml), rri, true);
					fail(sw.toString());
				}
				if (result == null) {
					fail("got null result");
				}
			}
			return result;
		} catch (IOException ex) {
			fail(ex);
		}
		return null;
	}
	/** Process XML data with X-definition.
	 * @param xdef X-definition source.
	 * @param xml string with XML data (in create mode it may be null).
	 * @param name name of X-definition or null.
	 * @param mode 'P' => parse, 'C' => create.
	 * @return xml element from processed X-definition.
	 */
	final public boolean test(final String xdef,final String xml,final String name,final char mode){
		return test(xdef, xml, name, mode, xml, "");
	}
	/** Process XML data with X-definition.
	 * @param xdef X-definition source.
	 * @param xml string with XML data (in create mode it may be null).
	 * @param name name of X-definition or null.
	 * @param mode 'P' => parse, 'C' => create.
	 * @param result ecxpected result.
	 * @param stdout ecxpected stdout.
	 * @return xml element from processed X-definition.
	 */
	final public boolean test(final String xdef,
		final String xml,
		final String name,
		final char mode,  // 'P' => parse, 'C' => create
		final String result,
		final String stdout) {
		return test(new String[]{xdef}, xml, name, mode, result, stdout);
	}
	/** Process XML data with X-definition.
	 * @param xdefs X-definition sources.
	 * @param xml string with XML data (in create mode it may be null).
	 * @param name name of X-definition or null.
	 * @param mode 'P' => parse, 'C' => create.
	 * @param result expected result.
	 * @param stdout expected stdout.
	 * @return xml element from processed X-definition.
	 */
	final public boolean test(final String[] xdefs,
		final String xml,
		final String name,
		final char mode,  // 'P' => parse, 'C' => create
		final String result,
		final String stdout) {
		boolean error = false;
		System.err.flush();
		System.out.flush();
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ArrayReporter reporter = new ArrayReporter();
			Element el = test(xdefs, xml, name, bos, reporter, mode);
			if (reporter.errors()) {
				error = true;
				ReportPrinter.printListing(System.out, xml, reporter, true);
			}
			if (result == null) {
				if (el != null) {
					error = true;
					System.err.println("Fails expected null, got:");
					System.err.println(KXmlUtils.nodeToString(el, true));
				}
			} else {
				if (el == null) {
					error = true;
					System.err.println("Fails! Expected:\n" + result + "\n got null");
				} else {
					Element expected = KXmlUtils.parseXml(result).getDocumentElement();
					ReportWriter rw = KXmlUtils.compareElements(el, expected);
					if (rw.errorWarnings()) {
						error = true;
						rw.getReportReader().printReports(System.err);
						System.err.println("Fails! Expected:\n" + result + "\n got:\n"
							+ KXmlUtils.nodeToString(el, false));
					}
				}
			}
			String s = bos.toString(getEncoding());
			if (!s.equals(stdout)) {
				error = true;
				System.err.println("========== Standard output ===========\n");
				System.err.println("Len expected: " + stdout.length() + ", len returned: " + s.length());
				System.err.println("Incorrect Output! Expected:\n" + stdout + "\ngot:\n" + s);
			}
		} catch (UnsupportedEncodingException ex) {
			error = true;
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return error;
	}
	final public XDPool compile(final URL[] source, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, source, obj));
	}
	final public XDPool compile(final URL url, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(	_props, url, obj));
	}
	final public XDPool compile(final File[] files, final Class... obj) {
		chkSyntax(files).checkAndThrowErrors();
		return checkExtObjects(XDFactory.compileXD(_props, files, obj));
	}
	final public XDPool compile(final File file, final Class... obj) {
		chkSyntax(file).checkAndThrowErrors();
		return checkExtObjects(XDFactory.compileXD(_props, file, obj));
	}
	final public XDPool compile(final String xdef, final Class<?>... obj) {
		chkSyntax(xdef).checkAndThrowErrors();
		return checkExtObjects(XDFactory.compileXD(_props, xdef, obj));
	}
	final public XDPool compile(String[] xdefs, final Class<?>... obj) {
		chkSyntax(xdefs).checkAndThrowErrors();
		return checkExtObjects(XDFactory.compileXD(_props, xdefs, obj));
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final ReportWriter reporter,
		final String xml) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element el = KXmlUtils.parseXml(xml).getDocumentElement();
		xd.setXDContext(el);
		return xd.xcreate(new QName(el.getNamespaceURI(),
			el.getNodeName()), reporter);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param name element name of the constructed XML.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final String name,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xcreate(name, reporter);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param qname QName of model to be created.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final QName qname,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xcreate(qname, reporter);
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef X-definition source.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param qname QName of model to be created.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final QName qname,
		final ReportWriter reporter) {
		return create(compile(xdef), defName, qname, reporter);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as XDocument context or null.
	 * @param name element name of the constructed XML.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final Element el,
		final String name) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (el != null) {
			xd.setXDContext(el);
		}
		return (el != null && (name == null || name.length() == 0))
			? xd.xcreate(new QName(el.getNamespaceURI(), el.getTagName()), null) : xd.xcreate(name, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef X-definition source.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as XDocument context or null.
	 * @param name element name of the constructed XML.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef, final String defName, final Element el, final String name){
		return create(compile(xdef), defName, el, name);
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef the X-Definition as source for data construction.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as XDocument context or null.
	 * @param name element name of the constructed XML.
	 * @param param global parameter in X-Script or null.
	 * @param obj the value of the global parameter or null.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final Element el,
		final String name,
		final String param,
		final Object obj,
		final ArrayReporter reporter) {
		return create(compile(xdef), defName, el, name, param, obj, reporter);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null.
	 * @param el Element as XDocument context or null.
	 * @param name element name of the constructed XML.
	 * @param param global parameter in X-Script or null.
	 * @param obj the value of the global parameter or null.
	 * @param reporter ArrayReporter or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName, // xdefinition name
		final Element el, // context
		final String name, // name of model
		final String param, // name of vatiable
		final Object obj, // value of variable
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (el != null) {
			xd.setXDContext(el);
		}
		if(param != null) {
			xd.setVariable(param, obj);
		}
		return (el != null && (name == null || name.length() == 0) && reporter != null)
			? xd.xcreate(new QName(el.getNamespaceURI(), el.getTagName()), reporter) : xd.xcreate(name, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null.
	 * @param qname QName of model to be created.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return create(xd, qname, reporter, xml, swr, userObj);
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param name name of model.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDOutput out = null;
		if (swr != null) {
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
		}
		if (xml != null && xml.length() > 0) {
			xd.setXDContext(xml);
		}
		if (userObj != null) {
			xd.setUserObject(userObj);
		}
		Element result = xd.xcreate(name, reporter);
		if (out != null) {
			out.close();
		}
		return result;
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param qname QName of model.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDOutput out = null;
		if (swr != null) {
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
		}
		if (xml != null && xml.length() > 0) {
			xd.setXDContext(xml);
		}
		if (userObj != null) {
			xd.setUserObject(userObj);
		}
		Element result = xd.xcreate(qname, reporter);
		if (out != null) {
			out.close();
		}
		return result;
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef XDefinition source.
	 * @param defName X-Definition name, or null.
	 * @param qname QName of model to be created.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		return create(compile(xdef), defName, qname, reporter, xml,swr,userObj);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null.
	 * @param name element name of the constructed XML.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(defName);
		return create(xd, name, reporter, xml, swr, userObj);
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef source with XDefinition.
	 * @param defName X-Definition name, or null.
	 * @param name element name of the constructed XML.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final String xml, // contest
		final StringWriter swr,
		final Object userObj) {
		return create(compile(xdef), defName, name, reporter, xml, swr, userObj);
	}
	/** Construct a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null.
	 * @param name element name of the constructed XML.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xp, defName, name, reporter, xml, null, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param name element name of the constructed XML.
	 * @param reporter ArrayReporter or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter) {
		return create(xd, name, reporter, null, null, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param qname QName of element to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter) {
		return create(xd, qname, reporter, null, null, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, name, reporter, xml, null, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xdef source with X-definition.
	 * @param defName name of X-definition or null.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final String xdef,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xdef, defName, name, reporter, xml, null, null);
	}
	/** Construct a new XML document from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param qname QName of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param xml context (source of XML) or null.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, qname, reporter, xml, null, null);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xd XDocument created from XDefinitions.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final Object obj,
		final StringWriter swr,
		final Object userObj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDOutput out = null;
		if (swr != null) {
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
		}
		if (obj != null) {
			xd.setXONContext(obj);
		}
		if (userObj != null) {
			xd.setUserObject(userObj);
		}
		Object result = xd.jcreate(name, reporter);
		if (out != null) {
			out.close();
		}
		return result;
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xdef source with X-definition.
	 * @param defName name of X-definition or null.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final String xdef,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final Object obj,
		final StringWriter swr,
		final Object userObj) {
		return jcreate(compile(xdef), defName, name, reporter, obj, swr,userObj);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xp XDPool containing X-definitions.
	 * @param defName X-Definition name, or null.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @param swr writer used as stdout or null.
	 * @param userObj user object or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final XDPool xp,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final Object obj,
		final StringWriter swr,
		final Object userObj) {
		return jcreate(xp.createXDDocument(defName), name, reporter, obj, swr, userObj);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xp XDPool containing X-definitions.
	 * @param defName X-Definition name, or null.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final XDPool xp,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final Object obj) {
		return jcreate(xp, defName, name, reporter, obj, null, null);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xd XDocument created from X-definitions.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final XDDocument xd, final String name, final ArrayReporter reporter) {
		return jcreate(xd, name, reporter, null, null, null);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xd XDocument created from X-definitions.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final Object obj) {
		return jcreate(xd, name, reporter, obj, null, null);
	}
	/** Construct a new XON/JSON object from the specified data.
	 * @param xdef source with X-definitions.
	 * @param defName name of X-definition or null.
	 * @param name name of model to be constructed.
	 * @param reporter ArrayReporter or null.
	 * @param obj context (object) or null.
	 * @return created XON/JSON object.
	 */
	final public Object jcreate(final String xdef,
		final String defName,
		final String name,
		final ArrayReporter reporter,
		final Object obj) {
		return jcreate(xdef, defName, name, reporter, obj, null, null);
	}
	/** Validate and process XML data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(xml, reporter);
	}
	/** Validate and process XML data.
	 * @param xdef X-definition source.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, xml, reporter);
	}
	/** Validate and process XML data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param el XML element to be processed.
	 * @param reporter ArrayReporter or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDPool xp,
		final String defName,
		final Element el,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(el, reporter);
	}
	/** Validate and process XML data.
	 * @param xdef X-definition source.
	 * @param defName X-definition name, or null.
	 * @param el XML element to be processed.
	 * @param reporter ArrayReporter or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final String xdef,
		final String defName,
		final Element el,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, el, reporter);
	}
	/** Validate and process XML data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param el XML element to be processed.
	 * @return XML element with processed data.
	 * @throws RuntimeException if an error occurs.
	 */
	final public Element parse(final XDPool xp,
		final String defName,
		final Element el) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(el, null);
	}
	/** Validate and process XML data.
	 * @param xdef X-definition source.
	 * @param defName X-definition name, or null.
	 * @param el XML element to be processed.
	 * @return XML element with processed data.
	 * @throws RuntimeException if an error occurs.
	 */
	final public Element parse(final String xdef, final String defName, final Element el) {
		return parse(compile(xdef), defName, el);
	}
	/** Validate and process XML data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @return XML element with processed data.
	 * @throws RuntimeException if an error occurs.
	 */
	final public Element parse(final XDPool xp, final String defName, final String xml) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(xml, null);
	}
	/** Validate and process XML data.
	 * @param xdef X-definition source.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @return XML element with processed data.
	 * @throws RuntimeException if an error occurs.
	 */
	final public Element parse(final String xdef, final String defName, final String xml) {
		return parse(compile(xdef), defName, xml);
	}
	/** Validate and process XML data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (obj != null) {
			xd.setUserObject(obj);
		}
		return xd.xparse(xml, reporter);
	}
	/** Validate and process XML data.
	 * @param xdef XDefinition source.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final String xdef,
		final String defName,
		final ArrayReporter reporter,
		final String xml,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, obj);
	}
	/** Validate and process XML data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDPool xp,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return parse(xd, xml, reporter, swr, input, obj);
	}
	/** Validate and process XML data.
	 * @param xd XDocument created from an X-definition.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDDocument xd, final String xml, final ArrayReporter reporter){
		return parse(xd, xml, reporter, null, null, null);
	}
	/** Validate and process XML data.
	 * @param xd XDocument created from an X-definition.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr) {
		return parse(xd, xml, reporter, swr, null, null);
	}
	/** Validate and process XML data.
	 * @param xd XDocument created from an X-definition.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object obj) {
		return parse(xd, xml, reporter, swr, null, obj);
	}
	/** Validate and process XML data.
	 * @param xd XDocument created from an X-definition.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		if (input != null) {
			if (input instanceof String) {
				xd.setStdIn(XDFactory.createXDInput(
					new ByteArrayInputStream(((String)input).getBytes()), false));
			} else if (input instanceof InputStreamReader) {
				xd.setStdIn(XDFactory.createXDInput((InputStreamReader) input, false));
			} else if (input instanceof InputStream) {
				xd.setStdIn(XDFactory.createXDInput((InputStream) input, false));
			}
		}
		if (swr != null) {
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
		}
		if (obj != null) {
			xd.setUserObject(obj);
		}
		xd.xparse(xml, reporter);
		if (swr != null) {
			try {
				swr.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		return xd.getElement();
	}
	/** Validate and process XML data.
	 * @param xdef X-definition source.
	 * @param defName X-Definition name, or null.
	 * @param xml XML data to be processed.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return XML element with processed data.
	 */
	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, swr, input, obj);
	}

	/** Validate and process JSON/XON data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param json input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDPool xp,
		final String defName,
		final String json,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.jparse(json, reporter);
	}
	/** Validate and process JSON/XON data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param json input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDPool xp,
		final String defName,
		final Object json,
		final ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.jvalidate(json, reporter);
	}
	/** Validate and process JSON/XON data.
	 * @param xdef X-definition source.
	 * @param defName X-definition name, or null.
	 * @param json object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final String xdef,
		final String defName,
		final Object json,
		final ReportWriter reporter) {
		return jparse(compile(xdef), defName, json, reporter);
	}
	/** Validate and process JSON/XON data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param json object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDPool xp,
		final String defName,
		final Object json,
		final ArrayReporter reporter,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (obj != null) {
			xd.setUserObject(obj);
		}
		return xd.jvalidate(json, reporter);
	}
	/** Validate and process JSON/XON data.
	 * @param xdef X-definition source.
	 * @param defName X-Definition name, or null.
	 * @param json input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final String xdef,
		final String defName,
		final ArrayReporter reporter,
		final Object json,
		final Object obj) {
		return jparse(compile(xdef), defName, json, reporter, obj);
	}
	/** Validate and process JSON/XON data.
	 * @param xp XDPool with compiled X-definitions.
	 * @param defName X-definition name, or null.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDPool xp,
		final String defName,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		if (swr != null) {
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
		}
		return jparse(xd, json, reporter, swr, input, obj);
	}
	/** Validate and process JSON/XON data.
	 * @param xd XDocument created from an X-definition.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDDocument xd,final Object json,final ArrayReporter reporter) {
		return jparse(xd, json, reporter, null, null, null);
	}
	/** Validate and process JSON/XON data.
	 * @param xd XDocument created from an X-definition.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr) {
		return jparse(xd, json, reporter, swr, null, null);
	}
	/** Validate and process JSON/XON data.
	 * @param xd XDocument created from an X-definition.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object obj) {
		return jparse(xd, json, reporter, swr, null, obj);
	}
	/** Validate and process JSON/XON data.
	 * @param xd XDocument created from an X-definition.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		if (reporter != null) {
			reporter.clear();
		}
		if (input != null) {
			if (input instanceof String) {
				xd.setStdIn(
					XDFactory.createXDInput(new ByteArrayInputStream(((String)input).getBytes()), false));
			} else if (input instanceof InputStreamReader) {
				xd.setStdIn(XDFactory.createXDInput((InputStreamReader) input, false));
			} else if (input instanceof InputStream) {
				xd.setStdIn(XDFactory.createXDInput((InputStream) input, false));
			}
		}
		if (obj != null) {
			xd.setUserObject(obj);
		}
		Object o = json == null
			? null
			: json instanceof String ? xd.jparse((String) json,reporter) : xd.jparse((String) json,reporter);
		if (swr != null) {
			try {
				swr.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		return o;
	}
	/** Validate and process JSON/XON data.
	 * @param xdef X-definition source.
	 * @param defName X-Definition name, or null.
	 * @param json Object with input JSON data.
	 * @param reporter ArrayReporter or null.
	 * @param swr writer used as stdout or null.
	 * @param input stream used as stdin or null.
	 * @param obj user object or null.
	 * @return processed JSON/XON data.
	 */
	final public Object jparse(final String xdef,
		final String defName,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		return jparse(compile(xdef), defName, json, reporter, swr, input, obj);
	}

	/** Create listing with reports from reporter.
	 * @param r reporter with reports.
	 * @param data source data.
	 * @return listing of source data and reports.
	 */
	final public String getListing(final ReportReader r, final String data) {
		if (data.charAt(0) == '<') {
			return ReportPrinter.printListing(data, r);
		} else {
			try {
				return "File: "+ data +"\n"+ ReportPrinter.printListing(FUtils.readString(new File(data)), r);
			} catch (SException ex) {
				return "File: " + data + "\n" + ex;
			}
		}
	}
	/** Create listing with reports from reporter.
	 * @param data String with source data.
	 * @param reporter reporter with reports.
	 * @return string with created listing.
	 */
	final public String createListnig(final String data, final ArrayReporter reporter) {
		if (!reporter.errorWarnings()) {
			return "";
		}
		StringWriter sw = new StringWriter();
		try (PrintWriter out = new PrintWriter(sw)) {
			ReportPrinter.printListing(out, data, reporter, true);
		}
		return sw.toString();
	}
	/** Print listing with reports from reporter.
	 * @param reporter reporter with reports.
	 * @param data String with source data.
	 */
	final public void printReports(final ReportReader reporter, final String data) {
		System.out.flush();
		if (data.charAt(0) == '<') {
			ReportPrinter.printListing(System.err, data, reporter, true);
		} else {
			try {
				ReportPrinter.printListing(System.err, new FileReader(data), reporter, true);
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		System.err.flush();
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse data and return created X-component.
	 * @param xp XDPool with X-definitions
	 * @param defName name of X-definition or null.
	 * @param xml input XML data.
	 * @param clazz XComponent class (if null, class is searched in XDPool).
	 * @param reporter ArrayReporter.
	 * @return created X-component.
	 */
	public final XComponent parseXC(final XDPool xp,
		final String defName,
		final String xml,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(defName), xml, clazz, reporter);
	}
	/** Parse data and return created X-component.
	 * @param xp XDPool with X-definitions
	 * @param defName name of X-definition or null.
	 * @param el input XML element.
	 * @param clazz XComponent class (if null, class is searched in XDPool).
	 * @param reporter ArrayReporter.
	 * @return created X-component.
	 */
	public final XComponent parseXC(final XDPool xp,
		final String defName,
		final Element el,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(defName), el, clazz, reporter);
	}
	/** Parse data and return created X-component.
	 * @param xd XDDocument created from X-definitions.
	 * @param xml input XML data.
	 * @param clazz XComponent class (if null, class is searched in XDPool).
	 * @param reporter ArrayReporter.
	 * @return created X-component.
	 */
	public final XComponent parseXC(final XDDocument xd,
		final String xml,
		final Class clazz,
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		xd.setProperties(_props);
		return xd.xparseXComponent(xml, clazz, reporter);
	}
	/** Parse data and return created X-component.
	 * @param xd XDDocument created from X-definitions.
	 * @param el input XML element.
	 * @param clazz XComponent class (if null, class is searched in XDPool).
	 * @param reporter ArrayReporter.
	 * @return created X-component.
	 */
	public final static XComponent parseXC(final XDDocument xd,
		final Element el,
		final Class clazz,
		final ArrayReporter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		return xd.xparseXComponent(el, clazz, reporter);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Create X-components from the XDPool object to temporary directory.
	 * @param xp XDPool from which the X-components created.
	 * @return ArrayReporter with reports from generator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public final ArrayReporter genXComponent(final XDPool xp) {
		return genXComponent(xp, clearTempDir());
	}

	/** Create X-components from the XDPool object to given directory.
	 * @param xp XDPool from which the X-components created.
	 * @param dir path to directory where to generate Java sources.
	 * @return ArrayReporter with reports from generator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public ArrayReporter genXComponent(final XDPool xp, final String dir) {
		return genXComponent(xp, new File(dir));
	}

	/** Create X-components from the XDPool object to given directory.
	 * @param xp XDPool from which the X-components created.
	 * @param dir directory where to generate Java sources.
	 * @return ArrayReporter with reports from generator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public final ArrayReporter genXComponent(final XDPool xp, final File dir) {
		if (!dir.exists() && !dir.isDirectory()) {
			//Directory doesn't exist or isn't accessible: &{0}
			throw new SRuntimeException(SYS.SYS025, dir.getAbsolutePath());
		}
		try {
			ArrayReporter result = xp.genXComponent(dir, getEncoding(), false, true);
			result.checkAndThrowErrors(); // throw exception if error reported
			compileSources(dir);
			return result;
		} catch (IOException ex) {
			throw new SRuntimeException(ex);
		}
	}

	/** Compile sources from a directory to actual class directory. Use actual classpath. */
	public final void compileSources(final File dir) {
			// create classpath item with org.xdef directory
		String classpath = getClassSource(XDConstants.class);
		String classDir = getClassSource(XDTester.class);
		compileSources(classpath, classDir, dir);
	}

	/** Simple type test in the Array
	 * @param type type method.
	 * @param xon XON/JSON data to be tested.
	 * @return string with errors or null.
	 */
	public final String testA(final String type, final String xon) {
		return testX(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:json name='A'> [\"* " + type + "()\"] </xd:json>\n"+
"  <xd:component> %class test.TestGJ" + type + " %link #A; </xd:component>\n"+
"</xd:def>", "", xon);
	}

	/** Simple type test in the Map.
	 * @param type type method.
	 * @param xon XON/JSON data to be tested.
	 * @return string with errors or null.
	 */
	public final String testM(final String type, final String xon) {
		return testX(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:json name='A'>\n" +
"{a:\"? " + type + "();\",b:\"? " + type + "();\",c:\"? " + type + "();\"}\n" +
"</xd:json>\n" +
"<xd:component>%class test.TestGM" + type + " %link A</xd:component>\n"+
"</xd:def>", "", xon);
	}

	/** Testing the entered data using X-definition.
	 * @param xdef X-definition source.
	 * @param xname name of X-definition.
	 * @param src data to be tested.
	 * @return null or string with error.
	 */
	public final String testX(final String xdef, final String xname, final String src) {
		try {
			XDPool xp = compile(xdef);
			genXComponent(xp);
			return testX(xp, xname, src);
		} catch (RuntimeException ex) {return printThrowable(ex);}
	}

	/** Testing the entered data using XDPool.
	 * @param xp compiled XDPool.
	 * @param xname name of X-definition.
	 * @param src data to be tested.
	 * @return empty string or error.
	 */
	public final String testX(final XDPool xp, final String xname, final String src) {
		return testX(xp, xname, null, src, null);
	}

	/** Testing the entered data using XDPool.
	 * @param xp compiled XDPool.
	 * @param xname name of X-definition.
	 * @param cls XComponent class name or null.
	 * @param src data to be tested.
	 * @return empty string or error.
	 */
	public final String testX(final XDPool xp,final String xname,final String cls,final String src) {
		return testX(xp, xname, cls, src, null);
	}

	/** Testing the entered data using XDPool.
	 * @param xp compiled XDPool.
	 * @param xname name of X-definition.
	 * @param cls XComponent class name or null.
	 * @param src data to be tested.
	 * @param outResult expected result of out stream or null.
	 * @return empty string or error.
	 */
	public final String testX(final XDPool xp,
		final String xname,
		final String cls,
		final String src,
		final String outResult){
		String result = "";
		try {
			ArrayReporter reporter = new ArrayReporter();
			Object o = XonUtils.parseXON(src);
			XDDocument xd = xp.createXDDocument(xname);
			StringWriter swr;
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			} else {
				swr = null;
			}
			Object x = xd.jparse(src, reporter);
			if (reporter.errorWarnings()) {
				result += "** 1\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 2\n" + XonUtils.toXonString(o, true) + "\n"
					+ XonUtils.toXonString(x, true) + "\n";
			}
			if (outResult != null && swr != null) {
				if (!outResult.equals(swr.toString())) {
					result += "** 3 '"+outResult+"', '"+swr.toString()+"'\n";
				}
			}
			xd = xp.createXDDocument(xname);
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			}
			XComponent xc = xd.jparseXComponent(src, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 4\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (xc == null) {
				return result + "** 5\n X-component is null\n";
			}
			if (outResult != null && swr != null) {
				if (!outResult.equals(swr.toString())) {
					result +="** 6 '"+outResult+"', '"+swr.toString()+"'\n";
				}
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 7\n" + XonUtils.toXonString(x, true) + "\n";
			}
			xd = xp.createXDDocument(xname);
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			}
			x = XonUtils.toXonString(x);
			xc = xd.jparseXComponent(x, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 8\n" + x + "\n"  + reporter.printToString() + "\n";
				reporter.clear();
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 9\n" + XonUtils.toXonString(x, true) + "\n";
			}
			if (outResult != null && swr != null) {
				if (!outResult.equals(swr.toString())) {
					result += "** 10 '"+outResult+"', '" + swr.toString()+"'\n";
				}
			}
			if (cls != null) {
				Class<?> clazz = Class.forName(cls);
				xd = xp.createXDDocument(xname);
				if (outResult != null) {
					xd.setStdOut(XDFactory.createXDOutput(swr=new StringWriter(),false));
				}
				xc = xd.jparseXComponent(src, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 11\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 12\n"+XonUtils.toXonString(x, true)+"\n";
				}
				if (outResult != null && swr != null) {
					if (!outResult.equals(swr.toString())) {
						result +="** 13 '"+outResult+"', '" + swr.toString()+"'\n";
					}
				}
				xd = xp.createXDDocument(xname);
				if (outResult != null && swr != null) {
					xd.setStdOut(XDFactory.createXDOutput(swr=new StringWriter(),false));
				}
				x = XonUtils.toXonString(x);
				xc = xd.jparseXComponent(x, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 14\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 15\n" + XonUtils.toXonString(x,true)+"\n";
				}
				if (outResult != null && swr != null) {
					if (!outResult.equals(swr.toString())) {
						result +="** 16 '"+outResult+"', '" + swr.toString()+"'\n";
					}
				}
			}
		} catch (ClassNotFoundException | SRuntimeException ex) {
			result += printThrowable(ex) + "\n";
		}
		return result.isEmpty() ? null : '~' + src + "~\n" + result;
	}
}
