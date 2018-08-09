/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: XPreCompiler.java, created 2018-07-21.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.impl.xml.XInputStream;
import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.SThrowable;
import cz.syntea.xdef.sys.SUtils;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KXmlConstants;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.xml.XMLConstants;

/** Reads source X-definitions (XML or JSON) and prepares the list of PNodes
 * with X-definitions created from source data.
 * @author Trojan
 */
public class XPreCompiler implements PreCompiler {
	/** index of NameSpace of X-definitions. */
	static final int NS_XDEF_INDEX = 0;
	/** index of NameSpace of XML. */
	static final int NS_XML_INDEX = NS_XDEF_INDEX + 1; //1
	/** index of NameSpace of XML NameSpace. */
	static final int NS_XMLNS_INDEX = NS_XML_INDEX + 1; //2
	/** index of NameSpace of XLink. */
	static final int NS_XLINK_INDEX = NS_XMLNS_INDEX + 1; //3
	/** index of NameSpace of XInclude. */
	static final int NS_XINCLUDE_INDEX = NS_XLINK_INDEX + 1; //4
	/** index of NameSpace of XML Schema. */
	static final int NS_XMLSCHEMA_INDEX = NS_XINCLUDE_INDEX + 1; //5
	/** Table of NameSpace prefixes. */
	static final Map<String, Integer> PREDEFINED_PREFIXES =
		new TreeMap<String, Integer>();

	/** PNodes with parsed source items. */
	private final ArrayList<PNode> _xdefPNodes = new ArrayList<PNode>();
	/** Source files table - to prevent to doParse the source twice. */
	private final ArrayList<Object> _sources = new ArrayList<Object>();
	/** Array of thesaurus sources item. */
	private final ArrayList<PNode> _thesaurus = new ArrayList<PNode>();
	/** Array of BNF sources. */
	private final ArrayList<PNode> _listBNF = new ArrayList<PNode>();
	/** Array of declaration source items. */
	private final ArrayList<PNode> _listDecl = new ArrayList<PNode>();
	/** Array of collection source items. */
	private final ArrayList<PNode> _listCollection = new ArrayList<PNode>();
	/** Array of component sources. */
	private final ArrayList<PNode> _listComponent = new ArrayList<PNode>();
	/** Code generator. */
	private final CompileCode _codeGenerator;

	/** Display mode */
	private final byte _displayMode;
	/** The nesting level of XML node. */
	private boolean _macrosProcessed;
	/** Include list of URL's. */
	private final ArrayList<Object> _includeList = new ArrayList<Object>();
	/** List of macro definitions. */
	private final Map<String, XScriptMacro> _macros =
		new TreeMap<String, XScriptMacro>();
	/** Reader of X-definitions in the form of XML. */
	private final PreReaderXML _xmlReader;
	/** Reader of X-definitions in the form of JSON. */
	private final PreReaderJSON _jsonReader;
	/** Reporter used for error messages. */
	private ReportWriter _reporter;

	/** Creates a new instance of XDefCompiler
	 * @param reporter The reporter.
	 * @param extClasses The array with external classes declared by user.
	 * @param displayMode display mode: 0 .. false, 1 .. true, 2 .. errors
	 * @param debugMode debug mode flag.
	 * @param ignoreUnresolvedExternals ignore unresolved externals flag.
	 */
	public XPreCompiler(final ReportWriter reporter,
		final Class<?>[] extClasses,
		final byte displayMode,
		final boolean debugMode,
		final boolean ignoreUnresolvedExternals) {
		_displayMode = displayMode;
		 //"xml"
		PREDEFINED_PREFIXES.put(XMLConstants.XML_NS_PREFIX, NS_XML_INDEX);
		//"xmlns",
		PREDEFINED_PREFIXES.put(XMLConstants.XMLNS_ATTRIBUTE, NS_XMLNS_INDEX);
		_codeGenerator = new CompileCode(extClasses,
			2, debugMode, ignoreUnresolvedExternals);
		_codeGenerator._namespaceURIs.add("."); //dummy namespace
		_codeGenerator._namespaceURIs.add(XMLConstants.XML_NS_URI);
		_codeGenerator._namespaceURIs.add(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		_codeGenerator._namespaceURIs.add(KXmlConstants.XLINK_NS_URI);
		_codeGenerator._namespaceURIs.add(KXmlConstants.XINCLUDE_NS_URI);
		_codeGenerator._namespaceURIs.add(//schema
			XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		_macrosProcessed = false;
		_reporter = reporter == null ? new ArrayReporter() : reporter;
		_xmlReader = new PreReaderXML(this);
		_jsonReader = new PreReaderJSON(this);
	}

	@Override
	/** Get namespace URI on given position.
	 * @param i position
	 * @return uri on this position or null.
	 */
	public String getNSURI(final int i) {
		return _codeGenerator._namespaceURIs.get(i);
	}

	@Override
	/** Get namespace URI on given position.
	 * @param i position
	 * @return uri on this position or null.
	 */
	public int getNSURIIndex(final String uri) {
		return _codeGenerator._namespaceURIs.indexOf(uri);
	}

	@Override
	/** Set URI. If the URI already exists just return the index
	 * @param uri URI to be set.
	 * @return index of uri.
	 */
	public int setNSURI(final String uri) {
		int i = _codeGenerator._namespaceURIs.indexOf(uri);
		if (i >= 0) {
			return i;
		}
		_codeGenerator._namespaceURIs.add(uri);
		return _codeGenerator._namespaceURIs.size() - 1;
	}

	@Override
	/** Set URI on given index.
	 * @param i index where to set.
	 * @param uri URI to set.
	 * @return original URI or null.
	 */
	public String setURIOnIndex(final int i, final String uri) {
		return _codeGenerator._namespaceURIs.set(i, uri);
	}

	@Override
	/** Report not legal attributes. All allowed attributes should be
	 * processed and removed. Not legal attributes generates an error message.
	 * @param pnode node to be checked.
	 */
	public final void reportNotAllowedAttrs(final PNode pnode) {
		for (PAttr attr: pnode._attrs) {
			 //Attribute '&{0}' not allowed here
			error(attr._value, XDEF.XDEF254, attr._name);
		}
		pnode._attrs.clear();
	}

	@Override
	/** Get attribute of given name with or without name space prefix from
	 * node. The attribute is removed from the list. If the argument
	 * required is set to true put error message that required attribute
	 * is missing.
	 * @param pnode where to find attribute.
	 * @param localName The local name of attribute.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the object SParsedData with the attribute value or null.
	 */
	public final SBuffer getXdefAttr(final PNode pnode,
		final String localName,
		final boolean required,
		final boolean remove) {
		PAttr attr = null;
		PAttr xattr = null;
		for (PAttr a : pnode._attrs) {
			if (localName.equals(a._localName) && a._nsindex <= 0) {
				if (a._nsindex == 0) {
					xattr = a;
				} else {
					attr = a;
				}
			}
		}
		if (xattr != null && attr != null) {
			//The attribute '&{0}' can't be specified simultanously
			//with and without namespace
			error(attr._value, XDEF.XDEF230, localName);
		} else if (xattr == null) {
			xattr = attr;
		}
		if (xattr == null) {
			if (required) {
				//Required attribute '&{0}' is missing
				error(pnode._name, XDEF.XDEF323, "xd:"+localName);
			}
			return null;
		} else {
			if (remove) {
				pnode._attrs.remove(xattr);
			}
			return xattr._value;
		}
	}

	/** Get "name" (or "prefix:name") of node.
	 * If the argument required is set to true put error message that
	 * required attribute is missing.
	 * @param pnode PNode where the attribute "name" is required.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the name or null.
	 */
	final String getNameAttr(final PNode pnode,
		final boolean required,
		final boolean remove) {
		SBuffer sval = getXdefAttr(pnode, "name", required, remove);
		if (sval == null) {
			// if required do not return null!
			return required ? ("_UNKNOWN_REQUIRED_NAME_") : null;
		}
		String name = sval.getString();
		if (name.length() == 0) {
			//Incorrect name
			error(sval, XDEF.XDEF258);
			return "__UNKNOWN_ATTRIBUTE_NAME_";
		}
		if (!XPreCompiler.chkDefName(name, pnode._xmlVersion)) {
			error(sval, XDEF.XDEF258); //Incorrect name
			return "__UNKNOWN_INCORRECT_NAME_";
		}
		return name;
	}

	@Override
	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseString(final String source) {
		parseString(source, null);
	}

	@Override
	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseString(final String source, final String srcName) {
		File f = new File(source);
		if (source.length() > 0 && !f.exists()
			&& (source.charAt(0) == '[' || source.charAt(0) == '{')) {
			ByteArrayInputStream baos = new ByteArrayInputStream(
				source.getBytes(Charset.forName("UTF-8")));
			_jsonReader.parseStream(baos, srcName);
		} else if (source.length() > 0 && !f.exists()
			&& source.charAt(0) == '<') {
			ByteArrayInputStream baos = new ByteArrayInputStream(
				source.getBytes(Charset.forName("UTF-8")));
			_xmlReader.parseStream(baos, srcName);
		} else {
			parseFile(f);
		}
	}

	@Override
	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param fileName pathname of file with with X-definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseFile(final String fileName) {
		parseFile(new File(fileName));
	}

	@Override
	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param file The file with with X-definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseFile(final File file) {
		try {
			URL url = file.toURI().toURL();
			for (Object o: getSources()) {
				if (o instanceof URL && url.equals(o)) {
					return; //found in list
				}
			}
			getSources().add(url);
			InputStream in = new FileInputStream(file);
			parseStream(in, url.toExternalForm());
			in.close();
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport());
			}
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	@Override
	/** Parse InputStream source X-definition and addAttr it to the set
	 * of definitions.
	 * @param in input stream with the X-definition.
	 * @param srcName name of source data used in reporting (SysId) or
	 * <tt>null</tt>.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseStream(final InputStream in, final String srcName) {
		try {
			XInputStream myInputStream = new XInputStream(in);
			byte[] buf = myInputStream.getparsedBytes();
			for (byte x: buf) {
				if (x != 0) {
					if ('[' == (char) x || '{' == (char) x) {
						_jsonReader.parseStream(myInputStream, srcName);
					} else {
						_xmlReader.parseStream(myInputStream, srcName);
					}
				}
			}
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport());
			}
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	@Override
	/** Parse data with source X-definition given by URL and addAttr it
	 * to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseURL(final URL url) {
		try {
			parseStream(url.openStream(), url.toExternalForm());
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport());
			}
			throw new SRuntimeException(SYS.SYS036, ex);//Program exception &{0}
		}
	}

	/** Process include list from header of X-definition. */
	void processIncludeList(PNode pnode) {
		/** let's check some attributes of X-definition.*/
		SBuffer include = getXdefAttr(pnode, "include", false, true);
		processIncludeList(include, _includeList,
			pnode._name.getSysId(), getReportWriter());
	}

	/** Process list of file specifications and/or URLs. Result of list is added
	 * to the includeList (if the includeList already contains an item the
	 * item is skipped. If the argument reporter is not <tt>null</tt> and an
	 * error occurs then the error is written to reporter. If reporter is
	 * <tt>null</tt> then an SRuntimeException is thrown.
	 * @param include SBuffer with list of items, separator is ",". Wildcard
	 * characters are permitted.
	 * @param includeArray ArrayList with items.
	 * @param sysId actual path.
	 * @param reporter report writer or <tt>null</tt>.
	 * @throws SRuntimeException if list contains error and reporter is null.
	 */
	private static void processIncludeList(final SBuffer include,
		final ArrayList<Object> includeArray,
		final String sysId,
		final ReportWriter reporter) {
		if (include == null) {
			return;
		}
		ReportWriter myreporter =
			reporter == null ? new ArrayReporter() : reporter;
		StringTokenizer st =
			new StringTokenizer(include.getString(), " \t\n\r\f,");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.startsWith("https:") || s.startsWith("http:") ||
				s.startsWith("ftp:") || s.startsWith("file:")) {
				try {
					URL u = new URL(URLDecoder.decode(s, "UTF-8"));
					if (includeArray.contains(u)) {
						continue;
					}
					includeArray.add(u);
				} catch (Exception ex) {
					myreporter.error(SYS.SYS024, s); //File doesn't exist: &{0}
				}
			} else {
				if (s.indexOf(':') < 0 &&
					!s.startsWith("/") && !s.startsWith("\\")) {//no path
					if (sysId != null) {//take path from sysId
						try {
							URL u = new URL(URLDecoder.decode(sysId, "UTF-8"));
							if (!"file".equals(u.getProtocol())) {
								String v =u.toExternalForm().replace('\\', '/');
								int i = v.lastIndexOf('/');
								if (i >= 0) {
									v = v.substring(0, i + 1);
								}
								u = new URL(URLDecoder.decode(v + s, "UTF-8"));
								if (includeArray.contains(u)) {
									continue;
								}
								includeArray.add(u);
								continue;
							} else {
								String p = new File(u.getFile()).
									getCanonicalPath().replace('\\', '/');
								int i = p.lastIndexOf('/');
								s = i>0 ? p.substring(0, i + 1) + s : ('/' + s);
							}
						} catch (Exception ex) {
							s = ""; // no file
						}
					}
				}
				File[] list = SUtils.getFileGroup(s);
				if (list.length == 0) {
					myreporter.error(SYS.SYS024, s); //File doesn't exist: &{0}
				} else {
					for (File f: list) {
						try {
							if (f.canRead()) {
								if (includeArray.contains(
									f.getCanonicalPath())) {
									continue; //file already exists
								}
								includeArray.add(f);
								continue;
							}
						} catch (IOException ex) {}
						//File doesn't exist: &{0}
						myreporter.error(SYS.SYS024, s);
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
					if (i + 1 < name.length()
						&& StringParser.getXmlCharType(
							name.charAt(++i), xmlVersion)
						!= StringParser.XML_CHAR_NAME_START){//must follow name
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	@Override
	/** Check if the node has no nested child nodes.
	 * @param pnode PNode to be tested.
	 */
	public final void chkNestedElements(final PNode pnode) {
		for (PNode p: pnode._childNodes) {
			//Nested child elements are not allowed here
			error(p._name, XDEF.XDEF219);
		}
	}

	@Override
	/** Prepare list of declared macros and expand macro references. */
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
		for (PNode xd: _xdefPNodes) {
			String defName = xd._xdef.getName();
			List<PNode> macros = new ArrayList<PNode>();
			for (PNode node : xd._childNodes) {
				if (node._nsindex == 0 && node._localName.equals("macro")) {
					macros.add(node);
				}
			}
			for (PNode macro : macros) {
				chkNestedElements(macro);
				Map<String, String> params = new TreeMap<String, String>();
				for (PAttr val : macro._attrs) {
					params.put(val._name, val._value.getString());
				}
				XScriptMacro m = new XScriptMacro(
					getNameAttr(macro, true, true),
					defName,
					params,
					macro._value,
					getReportWriter());
				if (_macros.containsKey(m.getName())) {
					//Macro '&{0}' redefinition
					Report rep = Report.error(XDEF.XDEF482, m.getName());
					macro._name.putReport(rep, getReportWriter());
				} else {
					_macros.put(m.getName(), m);
				}
			}
		}
		// expand macros
		ReportWriter reporter = getReportWriter();
		for (PNode p: _xdefPNodes) {
			p.expandMacros(reporter, p._xdef.getName(), _macros);
		}
		for (PNode p: _thesaurus) {
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

	@Override
	/** Set System ID for error reporting.
	 * @param sysId System id.
	 */
	public final void setSystemId(final String sysId) {
		_xmlReader._sysId = sysId;
	}

	@Override
	/** Get code generator.
	 * @return the code generator.
	 */
	public CompileCode getCodeGenerator() {return _codeGenerator;}

	@Override
	/** Get sources of X-definitions.
	 * @return array with sources of X-definitions.
	 */
	public List<Object> getSources() {return _sources;}

	@Override
	/** Get precompiled sources (PNodes) of X-definition items.
	 * @return array with PNodes with X-definitions.
	 */
	public List<PNode> getPXDefs() {return _xdefPNodes;}

	@Override
	/** Get precompiled sources (PNodes) of Thesaurus items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPThesaurusList() {return _thesaurus;}

	@Override
	/** Get precompiled sources (PNodes) of collection items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPCollections() {return _listCollection;}

	@Override
	/** Get precompiled sources (PNodes) of declaration items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPDeclarations() {return _listDecl;}

	@Override
	/** Get precompiled sources (PNodes) of components items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPComponents() {return _listComponent;}

	@Override
	/** Get precompiled sources (PNodes) of BNF Grammar items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPBNFs() {return _listBNF;}

	@Override
	/** Get report writer.
	 * @return the report writer.
	 */
	public final ReportWriter getReportWriter() {return _reporter;}

	@Override
	/** Set report writer.
	 * @param x the report writer to be set.
	 */
	public final void setReportWriter(final ReportWriter x) {_reporter = x;}

	/** Get display mode.
	 * @return display mode (see XDPool.DISPLAY_FALSE,
	 * XPool.DISPLAY_TRUE, DISPLAY_ERRORS).
	 */
	final byte getDispalyMode() {return _displayMode;}

	@Override
	/** Put fatal error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.fatal(registeredID, mod));
	}

	@Override
	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void error(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.error(registeredID, mod));
	}

	@Override
	/** Put ligthError message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void lightError(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.lightError(registeredID, mod));
	}

	@Override
	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.warning(registeredID, mod));
	}

	@Override
	/** Put report to reporter.
	 * @param pos SPosition
	 * @param rep Report.
	 */
	public final void putReport(final SPosition pos, final Report rep) {
		pos.putReport(rep, getReportWriter());
	}

	@Override
	/** Put error to compiler reporter.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void error(final long registeredID, final Object... mod) {
		getReportWriter().error(registeredID, mod);
	}

	@Override
	/** Put report to compiler reporter.
	 * @param rep Report.
	 */
	public final void putReport(final Report rep) {
		getReportWriter().putReport(rep);
	}

}