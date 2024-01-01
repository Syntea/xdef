package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.sys.SUtils;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import static org.xdef.sys.STester.getClassSource;
import org.xdef.xml.KXmlUtils;

/** Support of tests.
 * @author Vaclav Trojan
 */
public abstract class XDTester extends STester {
	public static String _xdNS = XDConstants.XDEF42_NS_URI;
	public static XDPool _xdOfxd;
	public static boolean _fulltestMode;
	private final Properties _props = new Properties();
	private boolean _genObj;
	private boolean _chkSyntax;

	/** Creates a new instance of TestAbstract */
	public XDTester() {
		super();
		resetTester();
		_chkSyntax = _fulltestMode;
		_genObj = _fulltestMode;
	}

	public final void resetProperties() {
		Report.setLanguage("en"); //localize
		setProperty(XDConstants.XDPROPERTY_DOCTYPE,
			XDConstants.XDPROPERTYVALUE_DOCTYPE_TRUE);
		if (_fulltestMode) {
			setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
				XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE);
		} else {
			setProperty(XDConstants.XDPROPERTY_LOCATIONDETAILS,
				XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_FALSE);
		}
		setProperty(XDConstants.XDPROPERTY_XINCLUDE,
			XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
		setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		setProperty(XDConstants.XDPROPERTY_DEBUG,
			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
		setProperty(XDConstants.XDPROPERTY_DEBUG_OUT, null);
		setProperty(XDConstants.XDPROPERTY_DEBUG_IN, null);
		setProperty(XDConstants.XDPROPERTY_DISPLAY,
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);
		setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
		setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
		setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE);
	}
	public final void resetTester() {
		_props.clear();
		_chkSyntax = _fulltestMode;
		_genObj = _fulltestMode;
		setChkSyntax(false);
		resetProperties();
	}
	public final Properties getProperties() {return _props;}
	public final void setChkSyntax(final boolean x) {_chkSyntax = x;}
	public final boolean getChkSyntax() {return _chkSyntax;}
	public final static boolean getFulltestMode() {return _fulltestMode;}
	public final static void setFulltestMode(boolean fulltest) {
		_fulltestMode = fulltest;
	}
	private ArrayReporter chkSyntax(final String[] xdefs) {
		return chkSyntax((Object[]) xdefs);
	}
	private ArrayReporter chkSyntax(final File[] xdefs) {
		return chkSyntax((Object[]) xdefs);
	}
	private static void removeMacros(final Element el) {
		NodeList nl = el.getElementsByTagNameNS(el.getNamespaceURI(), "macro");
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Node n = nl.item(i);
			n.getParentNode().removeChild(n); // remove macros
		}
	}
	private void genXdOfXd() {
		if (_xdOfxd == null) {// _xdOfxd not created, create it
			try {
				_xdOfxd = XDFactory.compileXD(null,
					"classpath://org.xdef.impl.compile.XdefOfXdef*.xdef");
			} catch (Exception ex) {
				ex.printStackTrace();
				new RuntimeException("XdefOfXdef is not available", ex);
			}
		}
	}
	public final ArrayReporter chkSyntax(final Object... xdefs) {
		ArrayReporter reporter = new ArrayReporter();
		if (!_chkSyntax) {
			return reporter;
		}
		genXdOfXd();
		XPreCompiler xpc =
			new XPreCompiler(reporter, null, (byte) 0, false, false, true);
		for (int i = 0; i < xdefs.length; i++) {
			Object x = xdefs[i];
			if (x instanceof String) {
				String s = (String) x;
				if (s.startsWith("<")) {
					xpc.parseString(s);
				} else if (s.startsWith("//")
					|| (s.indexOf(":/") > 2 && s.indexOf(":/") < 12)) {
					try {
						for (String y: SUtils.getSourceGroup(s)) {
							xpc.parseURL(SUtils.getExtendedURL(y));
						}
					} catch (Exception ex) {}
				} else {
					File[] xf = SUtils.getFileGroup(s);
					for (int j=0; j < xf.length; j++) {
						xpc.parseFile(xf[j]);
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
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			} else if (x instanceof String[]) {
				String[] xs = (String[]) x;
				for (int j=0; j < xs.length; j++) {
					xpc.parseString(xs[j]);
				}
			} else if (x instanceof File[]) {
				File[] xf = (File[]) x;
				for (int j=0; j < xf.length; j++) {
					xpc.parseFile(xf[j]);
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
	public final void setProperty(final String key, final String value) {
		String newKey = key.replace('.', '_');
		_props.remove(key);
		if (value == null) {
			_props.remove(newKey);
		} else {
			_props.setProperty(newKey, value);
		}
	}
	public final String getProperty(final String key) {
		return	_props.getProperty(key.replace('.', '_'));
	}
	public final void setGenObjFile(final boolean genObj) {_genObj = genObj;}
	public final boolean getGenObjFile() {return _genObj;}
	/** Returns the available element model represented by given name or
	 * <i>null</i> if definition item is not available.
	 * @param xdef XDefinition.
	 * @param key The name of definition item used for search.
	 * @return The required XElement or null.
	 */
	private static XMElement getXElement(final XMDefinition xdef,
		final String key) {
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
		XMElement[] elems = def.getModels();
		for (int i = 0; i < elems.length; i++) {
			XMElement xel  = elems[i];
			if (lockey.equals(xel.getName())) {
				return xel;
			}
		}
		return null;
	}
	private Element createElement(final XDPool xp,
		final String xdname,
		final ReportWriter reporter,
		final Element el,
		final DefOutStream stdout) {
		XDDocument xd = xp.createXDDocument(xdname);
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
			XMDefinition xdf = xp.getXMDefinition(xdname);
			if ((xe = getXElement(xdf, (qname = xdf.getName()))) == null) {
				//Model of element '&{0}' is missing in XDefinition&{1}{ }
				throw new SRuntimeException(XDEF.XDEF601,
					qname,xdf.getName());
			}
			nsURI = xe.getNSUri();
		}
		xd.xcreate(new QName(nsURI, qname), reporter);
		return xd.getElement();
	}
	public final XDPool checkExtObjects(final XDPool xp) {
		if (!_genObj) {return xp;}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			XDFactory.writeXDPool(baos, xp);
			ByteArrayInputStream bais =
				new ByteArrayInputStream(baos.toByteArray());
			XDPool xp1 = XDFactory.readXDPool(bais);
			baos = new ByteArrayOutputStream();
			XDFactory.writeXDPool(baos, xp1);
			bais = new ByteArrayInputStream(baos.toByteArray());
			xp1 =  XDFactory.readXDPool(bais);
			return xp1;
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch(Error e) {
			throw new RuntimeException(e);
		}
	}
	final public void checkResult(final Element el, final String expected) {
		if (expected == null) {
			return;
		}
		checkResult(el, KXmlUtils.parseXml(expected).getDocumentElement());
	}
	final public void checkResult(final Element el, final Element expected) {
		ReportWriter rw = KXmlUtils.compareElements(el, expected);
		if (!rw.errorWarnings()) {
			return;
		}
		System.err.flush();
		System.out.flush();
		StringWriter swr = new StringWriter();
		Report rep;
		ReportReader rri = rw.getReportReader();
		while((rep = rri.getReport()) != null) {
			swr.write(rep.toString() + '\n');
		}
		fail(swr.toString());
	}
	final public Element test(final String[] xdefs,
		final String data,
		final String name,
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
				throw new Exception("XDefinition " + name + " doesn't exist");
			}
			DefOutStream stdout;
			if (out == null) {
				stdout = null;
			} else {
				stdout =new DefOutStream(new OutputStreamWriter(out), false);
			}
			if (mode == 'C') {
				Element el = null;
				if (data != null) {
					el = KXmlUtils.parseXml(data, true).getDocumentElement();
				}
				return createElement(xp, name , reporter, el, stdout);
			} else {
				XDDocument xd = xp.createXDDocument(name);
				xd.setProperties(_props);
				if (stdout != null) {
					xd.setStdOut(stdout);
				}
				return xd.xparse(data, "", reporter);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return null;
	}
	final public Element test(final String xdef,
		final String data,
		final String name,
		final OutputStream out,
		final ReportWriter reporter,
		final char mode) {
		return test(new String[] {xdef}, data, name, out, reporter, mode);
	}
	@SuppressWarnings("deprecation")
	public final Element test(final File[] xdefs,
		final InputStream data,
		final String name,
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
				throw new Exception("XDefinition " + name + " doesn't exist");
			}
			DefOutStream stdout;
			if (out == null) {
				stdout = null;
			} else {
				stdout =new DefOutStream(new OutputStreamWriter(out), false);
			}
			if (mode == 'C') {
				Element el = null;
				if (data != null) {
					el = KXmlUtils.parseXml(data, true).getDocumentElement();
				}
				return createElement(xp, name , reporter, el, stdout);
			} else {
				XDDocument xd = xp.createXDDocument(name);
				xd.setProperties(_props);
				if (stdout != null) {
					xd.setStdOut(stdout);
				}
				return xd.xparse(data, "", reporter);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		return null;
	}
	/* if reporter is not null skipp checking of result of data processing*/
	final public Element test(final File xn,
		final InputStream data,
		final String name,
		final OutputStream os,
		final ReportWriter reporter) {
		return test(new File[]{xn}, data, name, os, reporter);
	}
	// if reporter is not null skipp checking of result of data proecessing
	final public Element test(final File[] xdefs,
		final InputStream data,
		final String name,
		final OutputStream os,
		final ReportWriter reporter) {
		try {
			ReportWriter myreporter = reporter;
			if (reporter == null) {
				myreporter = new ArrayReporter();
			}
			Element result = test(xdefs, data, name, os, reporter, 'P');
			if (reporter == null) {
				if (myreporter.errors()) {
					StringWriter sw = new StringWriter();
					data.reset();
					ReportReader rri = myreporter.getReportReader();
					ReportPrinter.printListing(sw,
						new java.io.InputStreamReader(data), rri, true);
					fail(sw.toString());
				}
				if (result == null) {
					fail("got null result");
				}
			}
			return result;
		} catch (Exception ex) {
			fail(ex);
		}
		return null;
	}
	final public boolean test(final String xdef,
		final String data,
		final String name,
		final char mode) {  // 'P' => parse, 'C' => create
		return test(xdef, data, name, mode, data, "");
	}
	final public boolean test(final String xdef,
		final String data,
		final String name,
		final char mode,  // 'P' => parse, 'C' => create
		final String result,
		final String stdout) {
		return test(new String[]{xdef}, data, name, mode, result, stdout);
	}
	final public boolean test(final String[] xdefs,
		final String data,
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
			Element el = test(xdefs, data, name, bos, reporter, mode);
			if (reporter.errors()) {
				error = true;
				ReportPrinter.printListing(System.out, data, reporter, true);
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
					System.err.println("Fails! Expected:\n"
						+ result + "\n got null");
				} else {
					Element expected =
						KXmlUtils.parseXml(result).getDocumentElement();
					ReportWriter rw =
						KXmlUtils.compareElements(el, expected);
					if (rw.errorWarnings()) {
						error = true;
						rw.getReportReader().printReports(System.err);
						System.err.println("Fails! Expected:\n"
							+ result + "\n got:\n"
							+ KXmlUtils.nodeToString(el, false));
					}
				}
			}
			String s = bos.toString(getEncoding());
			if (!s.equals(stdout)) {
				error = true;
				System.err.println("========== Standard output ===========\n");
				System.err.println("Len expected: " + stdout.length()
					 + ", len returned: " + s.length());
				System.err.println("Incorrect Output! Expected:\n" + stdout
					+ "\ngot:\n" + s);
			}
		} catch (Exception ex) {
			error = true;
			fail(ex);
		}
		System.err.flush();
		System.out.flush();
		return error;
	}
	final public XDPool compile(final InputStream source,
		final String path,
		final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, source, path, obj));
	}
	final public XDPool compile(final InputStream[] sources,
		final String[] path,
		final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, sources, path, obj));
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
	final public Element create(final String xdef,
		final String defName,
		final QName qname,
		final ReportWriter reporter) {
		return create(compile(xdef), defName, qname, reporter);
	}
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
			? xd.xcreate(new QName(el.getNamespaceURI(), el.getTagName()), null)
			: xd.xcreate(name, null);
	}
	final public Element create(final String xdef,
		final String defName,
		final Element el,
		final String name) {
		return create(compile(xdef), defName, el, name);
	}
	/**
	 Compose a new XML document from the specified data.
	 * @param xdef the X-Definition as source for data construction.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as X-Definition context or null.
	 * @param name root element name of the constructed XML document.
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
	/**
	 Compose a new XML document from the specified data.
	 * @param xp XDPool containing XDefinitions.
	 * @param defName X-Definition name, or null if it is not specified.
	 * @param el Element as X-Definition context or null.
	 * @param name root element name of the constructed XML document.
	 * @param param global parameter in X-Script or null.
	 * @param obj the value of the global parameter or null.
	 * @param reporter ArrayReporter.
	 * @return root element of the created XML document.
	 */
	final public Element create(final XDPool xp,
		final String defName,
		final Element el,
		final String name,
		final String param,
		final Object obj,
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
		return (el != null &&
			(name == null || name.length() == 0) && reporter != null)
			? xd.xcreate(
				new QName(el.getNamespaceURI(), el.getTagName()), reporter)
			: xd.xcreate(name, null);
	}
	final public Element create(final XDPool xp,
		final String xdName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(xdName);
		xd.setProperties(_props);
		return create(xd, qname, reporter, xml, swr, userObj);
	}
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
	final public Element create(final String xdef,
		final String xdName,
		final QName qname,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		return create(compile(xdef), xdName, qname, reporter, xml,swr,userObj);
	}
	final public Element create(final XDPool xp,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(xdName);
		return create(xd, name, reporter, xml, swr, userObj);
	}
	final public Element create(final String xdef,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml,
		final StringWriter swr,
		final Object userObj) {
		return create(compile(xdef), xdName, name, reporter, xml, swr, userObj);
	}
	final public Element create(final XDPool xp,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xp, xdName, name, reporter, xml, null, null);
	}
	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter) {
		return create(xd, name, reporter, null, null, null);
	}
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter) {
		return create(xd, qname, reporter, null, null, null);
	}
	final public Element create(final XDDocument xd,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, name, reporter, xml, null, null);
	}
	final public Element create(final String xdef,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final String xml) {
		return create(xdef, xdName, name, reporter, xml, null, null);
	}
	final public Element create(final XDDocument xd,
		final QName qname,
		final ArrayReporter reporter,
		final String xml) {
		return create(xd, qname, reporter, xml, null, null);
	}
	final public Object jcreate(final XDDocument xd,
		final String modelName,
		final ArrayReporter reporter,
		final Object json,
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
		if (json != null) {
			xd.setXONContext(json);
		}
		if (userObj != null) {
			xd.setUserObject(userObj);
		}
		Object result = xd.jcreate(modelName, reporter);
		if (out != null) {
			out.close();
		}
		return result;
	}
	final public Object jcreate(final String xdef,
		final String xdName,
		final String name,
		final ArrayReporter reporter,
		final Object xml,
		final StringWriter swr,
		final Object userObj) {
		return jcreate(compile(xdef), xdName, name, reporter, xml, swr,userObj);
	}
	final public Object jcreate(final XDPool xp,
		final String xdName,
		final String modelName,
		final ArrayReporter reporter,
		final Object json,
		final StringWriter swr,
		final Object userObj) {
		XDDocument xd = xp.createXDDocument(xdName);
		return jcreate(xd, modelName, reporter, json, swr, userObj);
	}
	final public Object jcreate(final XDPool xp,
		final String xdName,
		final String modeName,
		final ArrayReporter reporter,
		final Object json) {
		return jcreate(xp, xdName, modeName, reporter, json, null, null);
	}
	final public Object jcreate(final XDDocument xd,
		final String name,
		final ArrayReporter reporter) {
		return jcreate(xd, name, reporter, null, null, null);
	}
	final public Object jcreate(final XDDocument xd,
		final String modelName,
		final ArrayReporter reporter,
		final Object json) {
		return jcreate(xd, modelName, reporter, json, null, null);
	}
	final public Object jcreate(final String xdef,
		final String xdName,
		final String modelName,
		final ArrayReporter reporter,
		final Object json) {
		return jcreate(xdef, xdName, modelName, reporter, json, null, null);
	}
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
	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, xml, reporter);
	}
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
	final public Element parse(final String xdef,
		final String defName,
		final Element el,
		final ReportWriter reporter) {
		return parse(compile(xdef), defName, el, reporter);
	}
	final public Element parse(final XDPool xp,
		final String defName,
		final Element el) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(el, null);
	}
	final public Element parse(final String xdef,
		final String defName,
		final Element el) {
		return parse(compile(xdef), defName, el);
	}
	final public Element parse(final XDPool xp,
		final String defName,
		final String xml) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		return xd.xparse(xml, null);
	}
	final public Element parse(final String xdef,
		final String defName,
		final String xml) {
		return parse(compile(xdef), defName, xml);
	}
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
	final public Element parse(final String xdef,
		final String defName,
		final ArrayReporter reporter,
		final String xml,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, obj);
	}
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
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter) {
		return parse(xd, xml, reporter, null, null, null);
	}
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr) {
		return parse(xd, xml, reporter, swr, null, null);
	}
	final public Element parse(final XDDocument xd,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object obj) {
		return parse(xd, xml, reporter, swr, null, obj);
	}
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
					new ByteArrayInputStream(((String)input).getBytes()),
					false));
			} else if (input instanceof InputStreamReader) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStreamReader) input, false));
			} else if (input instanceof InputStream) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStream) input, false));
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
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return xd.getElement();
	}
	final public Element parse(final String xdef,
		final String defName,
		final String xml,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		return parse(compile(xdef), defName, xml, reporter, swr, input, obj);
	}
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
	final public Object jparse(final String xdef,
		final String defName,
		final Object json,
		final ReportWriter reporter) {
		return jparse(compile(xdef), defName, json, reporter);
	}
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
	final public Object jparse(final String xdef,
		final String defName,
		final ArrayReporter reporter,
		final Object json,
		final Object obj) {
		return jparse(compile(xdef), defName, json, reporter, obj);
	}
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
	final public Object jparse(final XDDocument xd,
		final String json,
		final ArrayReporter reporter) {
		return jparse(xd, json, reporter, null, null, null);
	}
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter) {
		return jparse(xd, json, reporter, null, null, null);
	}
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr) {
		return jparse(xd, json, reporter, swr, null, null);
	}
	final public Object jparse(final XDDocument xd,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object obj) {
		return jparse(xd, json, reporter, swr, null, obj);
	}
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
				xd.setStdIn(XDFactory.createXDInput(
					new ByteArrayInputStream(((String)input).getBytes()),
					false));
			} else if (input instanceof InputStreamReader) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStreamReader) input, false));
			} else if (input instanceof InputStream) {
				xd.setStdIn(XDFactory.createXDInput(
					(InputStream) input, false));
			}
		}
		if (obj != null) {
			xd.setUserObject(obj);
		}
		Object o = json == null ? null
			: json instanceof String ? xd.jparse((String) json, reporter)
			: xd.jparse((String) json, reporter);
		if (swr != null) {
			try {
				swr.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return o;
	}
	final public Object jparse(final String xdef,
		final String defName,
		final Object json,
		final ArrayReporter reporter,
		final StringWriter swr,
		final Object input,
		final Object obj) {
		return jparse(compile(xdef), defName, json, reporter, swr, input, obj);
	}
	final public String getListing(final ReportReader r,
		final String xdef) {
		if (xdef.charAt(0) == '<') {
			return ReportPrinter.printListing(xdef, r);
		} else {
			try {
				return "File: " + xdef + "\n" + ReportPrinter.printListing(
					FUtils.readString(new File(xdef)), r);
			} catch (Exception ex) {
				return "File: " + xdef + "\n" + ex;
			}
		}
	}

	final public String createListnig(final String data,
		final ArrayReporter reporter) {
		if (!reporter.errorWarnings()) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		ReportPrinter.printListing(out, data, reporter, true);
		out.close();
		return sw.toString();
	}

	final public void printReports(final ReportReader reporter,
		final String data) {
		System.out.flush();
		if (data.charAt(0) == '<') {
			ReportPrinter.printListing(System.err, data, reporter, true);
		} else {
			try {
				ReportPrinter.printListing(System.err,
					new FileReader(data), reporter, true);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		System.err.flush();
	}

////////////////////////////////////////////////////////////////////////////////

	public final XComponent parseXC(final XDPool xp,
		final String name,
		final String xml,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(name), xml, clazz, reporter);
	}
	public final XComponent parseXC(final XDPool xp,
		final String name,
		final Element el,
		final Class clazz,
		final ArrayReporter reporter) {
		return parseXC(xp.createXDDocument(name), el, clazz, reporter);
	}
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

	/** Create X-components from the XDPool object to given directory.
	 * @param xp XDPool from which the X-components created.
	 * @param dir path to directory where to generate Java sources.
	 * @return ArrayReporter with reports from ganerator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public ArrayReporter genXComponent(final XDPool xp, final String dir) {
		return genXComponent(xp, new File(dir));
	}

	/** Create X-components from the XDPool object to temporary directory.
	 * @param xp XDPool from which the X-components created.
	 * @return ArrayReporter with reports from ganerator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public final ArrayReporter genXComponent(final XDPool xp) {
		return genXComponent(xp, clearTempDir());
	}
	
	/** Create X-components from the XDPool object to given directory.
	 * @param xp XDPool from which the X-components created.
	 * @param dir directory where to generate Java sources.
	 * @return ArrayReporter with reports from ganerator of XComponents.
	 * @throws RuntimeException if an error occurs.
	 */
	public final ArrayReporter genXComponent(final XDPool xp, final File dir) {
		if (!dir.exists() && !dir.isDirectory()) {
			//Directory doesn't exist or isn't accessible: &{0}
			throw new SRuntimeException(SYS.SYS025, dir.getAbsolutePath());
		}
		try {
			ArrayReporter result =
				xp.genXComponent(dir, getEncoding(), false, true);
			result.checkAndThrowErrors(); // throw exception if error reported
			// create classpath item with org.xdef directory
			String classpath = getClassSource(XDConstants.class);
			String classDir = getClassSource(XDTester.class);
			compileSources(classpath, classDir, dir);
			return result;
		} catch (Exception ex) {
			throw new SRuntimeException(ex.toString(), ex);
		}
	}
}