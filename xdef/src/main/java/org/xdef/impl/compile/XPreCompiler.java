package org.xdef.impl.compile;

import org.xdef.impl.xml.XInputStream;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.XDConstants;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import javax.xml.XMLConstants;
import org.xdef.sys.STester;

/** Reads source X-definitions and prepares the list of PNodes created
 * from source data.
 * @author Trojan
 */
public class XPreCompiler implements PreCompiler {
	/** index of NameSpace of X-definitions. */
	static final int NS_XDEF_INDEX = 0;								//0
	/** index of NameSpace of XML. */
	static final int NS_XML_INDEX = NS_XDEF_INDEX + 1;				//1
	/** index of NameSpace of XML NameSpace. */
	static final int NS_XMLNS_INDEX = NS_XML_INDEX + 1;				//2
	/** index of NameSpace of XLink. */
	static final int NS_XLINK_INDEX = NS_XMLNS_INDEX + 1;			//3
	/** index of NameSpace of XInclude. */
	static final int NS_XINCLUDE_INDEX = NS_XLINK_INDEX + 1;		//4
	/** index of NameSpace of XML Schema (XSD). */
	static final int NS_XMLSCHEMA_INDEX = NS_XINCLUDE_INDEX + 1;	//5
	/** index of NameSpace of XON/JSON (W3C). */
	static final int NS_XON_INDEX = NS_XMLSCHEMA_INDEX + 1;		//6
	/** Table of NameSpace prefixes. */
	static final Map<String, Integer> DEFINED_PREFIXES = new LinkedHashMap<>();
	/** PNodes with parsed source items. */
	private final List<PNode> _xdefPNodes = new ArrayList<>();
	/** Source files table - to prevent to doParse the source twice. */
	private final List<Object> _sources = new ArrayList<>();
	/** Array of lexicon sources item. */
	private final List<PNode> _lexicons = new ArrayList<>();
	/** Array of BNF sources. */
	private final List<PNode> _listBNF = new ArrayList<>();
	/** Array of declaration source items. */
	private final List<PNode> _listDecl = new ArrayList<>();
	/** Array of collection source items. */
	private final List<PNode> _listCollection = new ArrayList<>();
	/** Array of component sources. */
	private final List<PNode> _listComponent = new ArrayList<>();
	/** Code generator. */
	private final CompileCode _g;

	/** Display mode */
	private final byte _displayMode;
	/** The nesting level of XML node. */
	private boolean _macrosProcessed;
	/** List of included sources. */
	private final List<Object> _includeList = new ArrayList<>();
	/** List of macro definitions. */
	private final Map<String, XScriptMacro> _macros = new LinkedHashMap<>();
	/** Reader of X-definitions in the form of XML. */
	private final PreReaderXML _xmlReader;
	/** Reporter used for error messages. */
	private ReportWriter _reporter;

	/** Creates a new instance of XDefCompiler
	 * @param reporter The reporter.
	 * @param extClasses The array with external classes declared by user.
	 * @param displayMode display mode: 0 .. false, 1 .. true, 2 .. errors
	 * @param chkWarnings if false warnings are generated as error.
	 * @param debugMode debug mode flag.
	 * @param ignoreUnresolvedExternals ignore unresolved externals flag.
	 */
	public XPreCompiler(final ReportWriter reporter,
		final Class<?>[] extClasses,
		final byte displayMode,
		final boolean chkWarnings,
		final boolean debugMode,
		final boolean ignoreUnresolvedExternals) {
		_displayMode = displayMode;

		DEFINED_PREFIXES.put(XMLConstants.XML_NS_PREFIX, NS_XML_INDEX); //"xml"

		DEFINED_PREFIXES.put(XMLConstants.XMLNS_ATTRIBUTE, NS_XMLNS_INDEX); //"xmlns",
		_g = new CompileCode(extClasses, 2, chkWarnings, debugMode, ignoreUnresolvedExternals);
		_g._namespaceURIs.add("."); //dummy namespace
		_g._namespaceURIs.add(XMLConstants.XML_NS_URI);
		_g._namespaceURIs.add(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		_g._namespaceURIs.add(XDConstants.XLINK_NS_URI);
		_g._namespaceURIs.add(XDConstants.XINCLUDE_NS_URI);
		_g._namespaceURIs.add(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		_g._namespaceURIs.add(XDConstants.XON_NS_URI_W); //XON/JSON
		_macrosProcessed = false;
		_reporter = reporter == null ? new ArrayReporter() : reporter;
		_xmlReader = new PreReaderXML(this);
	}

	/** Get "name" (or "prefix:name") of node. If the argument required is set to true put error message that
	 * required attribute is missing.
	 * @param pnode PNode where the attribute "name" is required.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the name or null.
	 */
	final String getNameAttr(final PNode pnode, final boolean required, final boolean remove) {
		PAttr pa = getXdefAttr(pnode, "name", required, remove);
		if (pa == null) {
			// if required do not return null!
			return required ? ("_UNKNOWN_REQUIRED_NAME_") : null;
		}
		String name = pa._value.getString().trim();
		if (name.isEmpty()) {
			error(pa._value, XDEF.XDEF258); //Incorrect name
			return "__UNKNOWN_ATTRIBUTE_NAME_";
		}
		if (!XPreCompiler.chkDefName(name, pnode._xmlVersion)) {
			error(pa._value, XDEF.XDEF258); //Incorrect name
			return "__UNKNOWN_INCORRECT_NAME_";
		}
		return name;
	}

	/** Process include list from header of X-definition. */
	void processIncludeList(PNode pnode) {
		PAttr pa = getXdefAttr(pnode, "include", false, true);
		if (pa == null) {
			return; // Attribute "include" is not declared in X-definition
		}
		/* Process list of file specifications and/or URLs. Result of list is added to the includeList
		 * (if the includeList already contains an item the item it is skipped. If reporter is null then
		 * SRuntimeException is thrown.*/
		SBuffer include = pa._value;
		String actPath = pnode._name.getSysId();
		ReportWriter reporter = getReportWriter();
		ReportWriter myreporter = reporter == null ? new ArrayReporter() : reporter;
		StringTokenizer st = new StringTokenizer(include.getString(), " \t\n\r\f,;");
		while (st.hasMoreTokens()) {
			String sid = st.nextToken(); // system ID
			URL u;
			if (sid.startsWith("//") || (sid.indexOf(":/") > 2 && sid.indexOf(":/") < 12)) { // is URL
				try {
					for (String x : SUtils.getSourceGroup(sid)) {
						if (!_includeList.contains(u=SUtils.getExtendedURL(x))) {
							_includeList.add(u); // file is not in list
						}
					}
				} catch (Exception ex) {
					myreporter.error(SYS.SYS024, sid);//File doesn't exist: &{0}
				}
			} else {
				if (sid.indexOf(':') < 0 && !sid.startsWith("/") && !sid.startsWith("\\")) {//no path
					if (actPath != null) {//take path from sysId
						try {
							u = SUtils.getExtendedURL(actPath);
							if (!"file".equals(u.getProtocol())) {
								String v = u.toExternalForm().replace('\\','/');
								int i = v.lastIndexOf('/');
								if (i >= 0) {
									v = v.substring(0, i + 1);
								}
								u = SUtils.getExtendedURL(v + sid);
								if (!_includeList.contains(u)) {
									_includeList.add(u); // file is not in list
								}
								continue;
							} else {
								String p = new File(u.getFile()).getCanonicalPath().replace('\\', '/');
								int i = p.lastIndexOf('/');
								sid = i>0 ? p.substring(0, i+1)+sid : ('/'+sid);
							}
						} catch (IOException ex) {
							myreporter.error(SYS.SYS024, sid); //File doesn't exist: &{0}
							sid = ""; // no file
						}
					}
				}
				File[] list = SUtils.getFileGroup(sid);
				if (list.length == 0) {
					myreporter.error(SYS.SYS024, sid);//File doesn't exist: &{0}
				} else {
					for (File f: list) {
						if (f.canRead()) {
							if (!_includeList.contains(f)) {
								_includeList.add(f); // file is not in list
							}
							continue;
						}
						myreporter.error(SYS.SYS024, sid); //File doesn't exist: &{0}
					}
				}
			}
		}
		if (reporter == null && myreporter.errors()) {
			myreporter.checkAndThrowErrors();
		}
	}

	/** Check if the name of X-definition is OK.
	 * @param name name of X-definition
	 * @return true if the name of X-definition is OK.
	 */
	final static boolean chkDefName(final String name, byte xmlVersion) {
		if (name.length() == 0) {
			return true; //nameless is also name
		}
		if (StringParser.getXmlCharType(name.charAt(0),  xmlVersion) !=
			StringParser.XML_CHAR_NAME_START) {
			return false;
		}
		char c;
		boolean wasColon = false;
		for (int i = 1; i < name.length(); i++) {
			if (StringParser.getXmlCharType(c = name.charAt(i),  xmlVersion) !=
				StringParser.XML_CHAR_NAME_START && (c  < '0' && c > '9')) {
				if (!wasColon && c == ':') { // we allow one colon inside name
					wasColon = true;
					if (i + 1 < name.length() && StringParser.getXmlCharType(name.charAt(++i), xmlVersion)
						!= StringParser.XML_CHAR_NAME_START) { //must follow name
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	/** Get map with macros.
	 * @return map with macros.
	 */
	public Map<String, XScriptMacro> getMacros() {return _macros;}

	/** Get display mode.
	 * @return display mode (see XDPool.DISPLAY_FALSE,
	 * XPool.DISPLAY_TRUE, DISPLAY_ERRORS).
	 */
	public final byte getDispalyMode() {return _displayMode;}

	private void setMacros(final List<PNode> macros) {
		for (PNode macro : macros) {
			chkNestedElements(macro);
			Map<String, String> params = new LinkedHashMap<>();
			String def = null;
			for (PAttr patt : macro.getAttrs()) {
				if ("#def".equals(patt._name)) {
					def = patt.getValue().getString();
					macro.removeAttr(patt);
					break;
				}
			}
			for (PAttr val : macro.getAttrs()) {
				params.put(val._name, val._value.getString());
			}
			XScriptMacro m = new XScriptMacro(getNameAttr(macro, true, true),
				def, params, macro._value, getReportWriter());
			if (_macros.containsKey(m.getName())) {
				Report rep = Report.error(XDEF.XDEF482, m.getName()); //Macro '&{0}' redefinition
				macro._name.putReport(rep, getReportWriter());
			} else {
				_macros.put(m.getName(), m);
			}
		}
	}

	/** Get namespace URI on given position.
	 * @param i position
	 * @return uri on this position or null.
	 */
	@Override
	public String getNSURI(final int i) {return _g._namespaceURIs.get(i);}

	/** Get namespace URI index of given uri.
	 * @param uri uri to he found.
	 * @return index of uri from argument.
	 */
	@Override
	public int getNSURIIndex(final String uri) {return _g._namespaceURIs.indexOf(uri);}

	/** Set URI. If the URI already exists just return the index
	 * @param uri URI to be set.
	 * @return index of uri.
	 */
	@Override
	public int setNSURI(final String uri) {
		int i = _g._namespaceURIs.indexOf(uri);
		if (i >= 0) {
			return i;
		}
		_g._namespaceURIs.add(uri);
		return _g._namespaceURIs.size() - 1;
	}

	/** Set URI on given index.
	 * @param i index where to set.
	 * @param uri URI to set.
	 * @return original URI or null.
	 */
	@Override
	public String setURIOnIndex(final int i, final String uri) {return _g._namespaceURIs.set(i, uri);}

	/** Report not legal attributes. All allowed attributes should be
	 * processed and removed. Not legal attributes generates an error message.
	 * @param pnode node to be checked.
	 */
	@Override
	public final void reportNotAllowedAttrs(final PNode pnode) {
		for (PAttr attr: pnode.getAttrs()) {
			error(attr._value, XDEF.XDEF254, attr._name); //Attribute '&{0}' not allowed here
		}
		pnode.getAttrs().clear();
	}

	/** Get attribute of given name with or without namespace prefix from node. The attribute is removed from
	 * the list. If the argument required is set to true put error message that required attribute is missing.
	 * @param pnode where to find attribute.
	 * @param localName The local name of attribute.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return PAttr object or null.
	 */
	@Override
	public final PAttr getXdefAttr(final PNode pnode,
		final String localName,
		final boolean required,
		final boolean remove) {
		PAttr attr = null;
		PAttr xattr = null;
		for (PAttr a : pnode.getAttrs()) {
			if (localName.equals(a._localName) && a._nsindex <= 0) {
				if (a._nsindex == 0) {
					xattr = a;
				} else {
					attr = a;
				}
			}
		}
		if (xattr != null && attr != null) {
			//The attribute '&{0}' can't be specified simultanously with and without namespace
			error(attr._value, XDEF.XDEF230, localName);
		} else if (xattr == null) {
			xattr = attr;
		}
		if (xattr == null) {
			if (required) {
				error(pnode._name, XDEF.XDEF323, "xd:"+localName);//Required attribute '&{0}' is missing
			}
			return null;
		} else {
			if (remove) {
				pnode.removeAttr(xattr);
			}
			return xattr;
		}
	}

	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 */
	@Override
	public final void parseString(final String source) {parseString(source, null);}

	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 */
	@Override
	public final void parseString(final String source, final String srcName) {
		File f = new File(source);
		if (!f.exists()) {
			String s = source.trim();
			if (s.length() > 3 && s.charAt(0) == '<') {
				_xmlReader.parseStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), srcName);
				f = null;
			}
		}
		if (f != null) {
			parseFile(f);
		}
	}

	/** Parse file with source X-definition and addAttr it to the set of definitions.
	 * @param fileName pathname of file with with X-definitions.
	 */
	@Override
	public final void parseFile(final String fileName) {parseFile(new File(fileName));}

	/** Parse file with source X-definition and addAttr it to the set of definitions.
	 * @param file The file with with X-definitions.
	 */
	@Override
	public final void parseFile(final File file) {
		try {
			URL url = file.getCanonicalFile().toURI().toURL();
			for (Object o: getSources()) {
				if (o instanceof URL && url.equals(o)) {
					return; //found in list
				}
			}
			getSources().add(url);
			InputStream in = new FileInputStream(file);
			parseStream(in, url.toExternalForm());
		} catch (IOException | RuntimeException ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new SRuntimeException(SYS.SYS036, STester.printThrowable(ex)); //Program exception &{0}
		}
	}

	/** Parse InputStream source X-definition and addAttr it to the set of definitions.
	 * @param in input stream with the X-definition.
	 * @param srcName name of source data used in reporting (SysId) or null.
	 */
	@Override
	public final void parseStream(final InputStream in, final String srcName) {
		try {
			XInputStream myInputStream = new XInputStream(in);
			byte[] buf = myInputStream.getparsedBytes();
			for (byte x: buf) {
				if (x != 0) { //skip zeroes
					_xmlReader.parseStream(myInputStream, srcName);
				}
			}
			in.close();
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport(), ex);
			}
			throw new SRuntimeException(SYS.SYS036, STester.printThrowable(ex)); //Program exception &{0}
		}
	}

	/** Parse data with source X-definition given by URL and addAttr it
	 * to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 */
	@Override
	public final void parseURL(final URL url) {
		try {
			parseStream(url.openStream(), url.toExternalForm());
		} catch (IOException | RuntimeException ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport(), ex);
			}
			throw new SRuntimeException(SYS.SYS036, STester.printThrowable(ex)); //Program exception &{0}
		}
	}

	/** Check if the node has no nested child nodes.
	 * @param pnode PNode to be tested.
	 */
	@Override
	public final void chkNestedElements(final PNode pnode) {
		for (PNode p: pnode.getChildNodes()) {
			if (pnode._nsindex != 0 || !"declaration".equals(pnode._localName)
				|| !"macro".equals(p._localName)) {
				error(p._name, XDEF.XDEF219); //Nested child elements are not allowed here
			}
		}
	}

	/** Prepare list of declared macros and expand macro references. */
	@Override
	public final void prepareMacros() {
		if (_macrosProcessed) {
			return;
		}
		// doParse of definitions from include list
		for (int i = 0; i < _includeList.size(); i++) {
			Object o = _includeList.get(i);
			if (o instanceof URL) {
				parseURL((URL) o);
			} else {
				parseFile((File) o);
			}
		}
		List<PNode> macros = new ArrayList<>();
		for (PNode xd: _listDecl) {
			for (PNode pnode : xd.getChildNodes()) {
				if (pnode._localName.equals("macro")) {
					macros.add(pnode);
				}
			}
		}
		for (PNode xd: _xdefPNodes) {
			String defName = xd._xdef.getName();
			for (PNode pnode : xd.getChildNodes()) {
				if (pnode._nsindex == 0) {
					if (pnode._localName.equals("macro")) {
						PAttr defAttr =
							new PAttr("#def", new SBuffer(defName), "", -1);
						defAttr._localName = "#def";
						pnode.setAttr(defAttr);
						macros.add(pnode);
					} else if (pnode._localName.equals("declaration")) {
						boolean local = false;
						for (PAttr pa : pnode.getAttrs()) {
							if ("scope".equals(pa._localName)) {
								local = "local".equals(pa._value.getString());
							} else {
								//Attribute '&{0}' not allowed here
								error(pa._value, XDEF.XDEF254, pa._localName);
							}
						}
						for (PNode x : pnode.getChildNodes()) {
							if (x._nsindex==0 && "macro".equals(x._localName)) {
								if (local) {
									// local mactro
									PAttr defAttr = new PAttr("#def", new SBuffer(defName), "", -1);
									defAttr._localName = "#def";
									x.setAttr(defAttr);
								}
								macros.add(x);
							}
						}
					}
				}
			}
			setMacros(macros);
			macros.clear();
		}
		// expand macros
		ReportWriter reporter = getReportWriter();
		for (PNode p: _xdefPNodes) {
			p.expandMacros(reporter, p._xdef.getName(), _macros);
		}
		for (PNode p: _lexicons) {
			p.expandMacros(reporter, null, _macros);
		}
		for (PNode p: _listBNF) {
			p.expandMacros(reporter, null, _macros);
		}
		for (PNode p: _listDecl) {
			p.expandMacros(reporter, null, _macros);
		}
		for (PNode p: _listComponent) {
			p.expandMacros(reporter, null, _macros);
		}
		_macrosProcessed = true;
	}

	/** Set System ID for error reporting.
	 * @param sysId System id.
	 */
	@Override
	public final void setSystemId(final String sysId) {_xmlReader._sysId=sysId;}

	/** Get code generator.
	 * @return the code generator.
	 */
	@Override
	public CompileCode getCodeGenerator() {return _g;}

	/** Get sources of X-definitions.
	 * @return array with sources of X-definitions.
	 */
	@Override
	public List<Object> getSources() {return _sources;}

	/** Get list with included sources of X-definitions (URL or File).
	 * @return list with included sources of X-definitions (URL or File).
	 */
	@Override
	public List<Object> getIncluded() {return _includeList;}

	/** Get prepared sources (PNodes) of X-definition items.
	 * @return array with PNodes with X-definitions.
	 */
	@Override
	public List<PNode> getPXDefs() {return _xdefPNodes;}

	/** Get prepared sources (PNodes) of lexicon items.
	 * @return array with PNodes.
	 */
	@Override
	public final List<PNode> getPLexiconList() {return _lexicons;}

	/** Get prepared sources (PNodes) of collection items.
	 * @return array with PNodes.
	 */
	@Override
	public final List<PNode> getPCollections() {return _listCollection;}

	/** Get prepared sources (PNodes) of declaration items.
	 * @return array with PNodes.
	 */
	@Override
	public final List<PNode> getPDeclarations() {return _listDecl;}

	/** Get prepared sources (PNodes) of components items.
	 * @return array with PNodes.
	 */
	@Override
	public final List<PNode> getPComponents() {return _listComponent;}

	/** Get prepared sources (PNodes) of BNF Grammar items.
	 * @return array with PNodes.
	 */
	@Override
	public final List<PNode> getPBNFs() {return _listBNF;}

	/** Get report writer.
	 * @return the report writer.
	 */
	@Override
	public final ReportWriter getReportWriter() {return _reporter;}

	/** Set report writer.
	 * @param x the report writer to be set.
	 */
	@Override
	public final void setReportWriter(final ReportWriter x) {_reporter = x;}

	/** Get switch if the parser will check warnings as errors.
	 * @return true if the parser checks warnings as errors.
	 */
	@Override
	final public boolean isChkWarnings() {return _g._chkWarnings;}

	/** Put fatal error message.
	 * @param pos SPosition.
	 * @param registeredID registered report id.
	 * @param mod Array with message modification parameters.
	 */
	@Override
	public void fatal(final SPosition pos, final long registeredID, final Object... mod) {
		putReport(pos, Report.fatal(registeredID, mod));
	}

	/** Put error message.
	 * @param pos SPosition.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void error(final SPosition pos, final long registeredID, final Object... mod) {
		putReport(pos, Report.error(registeredID, mod));
	}

	/** Put ligthError message.
	 * @param pos SPosition.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void lightError(final SPosition pos, final long registeredID, final Object... mod) {
		putReport(pos, Report.lightError(registeredID, mod));
	}

	/** Put warning message ( or error if warning flag is false).
	 * @param pos SPosition.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void warning(final SPosition pos, final long registeredID, final Object... mod) {
		putReport(pos, Report.warning(registeredID, mod));
	}

	/** Put report to reporter.
	 * @param pos SPosition.
	 * @param rep Report.
	 */
	@Override
	public final void putReport(final SPosition pos, final Report rep) {
		pos.putReport(rep, getReportWriter());
	}

	/** Put error to compiler reporter.
	 * @param ID registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void error(final long ID, final Object... mod) {getReportWriter().error(ID, mod);}

	/** Put report to compiler reporter.
	 * @param rep Report.
	 */
	@Override
	public final void putReport(final Report rep) {getReportWriter().putReport(rep);}
}
