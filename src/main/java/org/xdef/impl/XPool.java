package org.xdef.impl;

import org.xdef.XDConstants;
import org.xdef.msg.XDEF;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SIOException;
import org.xdef.sys.SManager;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.code.CodeDisplay;
import org.xdef.impl.compile.CompileXDPool;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.model.XMSelector;
import org.xdef.model.XMVariableTable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.xdef.sys.ReportWriter;
import java.io.Serializable;
import org.xdef.proc.XDLexicon;

/** Implementation of the XDPool containing the set of X-definitions.
 * @author Vaclav Trojan
 */
public final class XPool implements XDPool, Serializable {
	/** The namespace URI for X-definition instance (version 2.0; deprecated).*/
	public static final String XDEF20_INSTANCE_NS_URI =
		"http://www.syntea.cz/xdef/2.0/instance";
	/** The namespace URI for X-definition instance (version 3.1; deprecated).*/
	public static final String XDEF31_INSTANCE_NS_URI =
		"http://www.syntea.cz/xdef/instance";

	/** This constant is used in the ObjectStream reader/writer. */
	private static final long serialVersionUID = -4736745770531753457L;
	/** Magic ID.*/
	private static final short XD_MAGIC_ID = 0x7653;
	/** XDPool version.*/
	private static final String XD_VERSION = 
		"XD" + XDConstants.BUILD_VERSION.split("-")[0]; // ignore snapshot
	/** Last compatible version of XDPool.*/
	private static final long XD_MIN_VERSION = 302005000L; // 32.5.0

	/** Flag if warnings should be checked.*/
	private boolean _chkWarnings;
	/** Switch to allow/restrict DOCTYPE in XML.*/
	private boolean _illegalDoctype;
	/** Switch to allow/restrict includes in XML.*/
	private boolean _resolveIncludes;
	/** Debug mode: 0 .. false, 1 .. true, 2 .. showResult.*/
	private byte _debugMode;
	/** flag to be set validation of names references in parsed/created nodes.*/
	private boolean _validate;
	/** Class name of debug editor.*/
	private String _debugEditor;
	/** Class name of X-definition editor.*/
	private String _xdefEditor;
	/** DisplayMode.*/
	private byte _displayMode;
	/** flag unresolved externals to be ignored.*/
	private boolean _ignoreUnresolvedExternals;
	/** Switch if location details will be generated.*/
	private boolean _locationdetails;
	/** External objects.*/
	private Class<?>[] _extClasses;
	/** Properties.*/
	private Properties _props;
	/** Global variable table.*/
	private XVariableTable _variables;
	/** Max. stack length.*/
	private int _stackLen;
	/** Address of code initialization.*/
	private int _init;
	/** Size of global variables table.*/
	private int _globalVariablesSize;
	/** Maximum size of local variables table.*/
	private int _localVariablesMaxSize;
	/** Counter of string sources.*/
	private int _stringItem = 0;
	/** Counter of stream sources.*/
	private int _streamItem = 0;
	/** debug information.*/
	private XDebugInfo _debugInfo;
	/** SQId generator.*/
	private int _sqId = 1;
	/** Generated code of XDPool. */
	private XDValue[] _code;
	/** Components.*/
	private Map<String, String> _components;
	/** Binds.*/
	private Map<String, String> _binds;
	/** Enumerations.*/
	private Map<String, String> _enums;
	/** Lexicon of tag names in different languages.*/
	XDLexicon _lexicon = null;
	/** Reporter writer.*/
	ReportWriter _reporter;
	/** CompileXDPool for definitions.*/
	CompileXDPool _compiler;

	/** Table of definitions.*/
	Map<String, XDefinition> _xdefs;
	/** Table of source objects.*/
	private XDSourceInfo _sourceInfo;
	
	// valid date parameters
	/** Maximal accepted value of the year.*/
	private int _maxYear;
	/** Minimal accepted value of the year.*/
	private int _minYear;
	/** List of dates to be accepted out of interval _minYear.._maxYear.*/
	private SDatetime _specialDates[];

////////////////////////////////////////////////////////////////////////////////
// Constructors
////////////////////////////////////////////////////////////////////////////////

	private XPool() {
		_xdefs = new LinkedHashMap<String, XDefinition>();
		_sourceInfo = new XDSourceInfo();
	}

	/** Creates instance of XDPool with properties, external objects and
	 * reporter.
	 * @param props Properties or <tt>null</tt>.
	 * @param extClasses The array of classes where are available methods
	 * referred from definitions.
	 * @param reporter report writer or <tt>null</tt>.
	 */
	XPool(final Properties props,
		final ReportWriter reporter,
		final Class<?>... extClasses) {
		this();
		_extClasses = extClasses;
		_reporter = reporter;
		_props = props != null ? props : SManager.getProperties();
		// Set values of properties
		//debug mode
		_debugMode = (byte) readProperty(_props, XDConstants.XDPROPERTY_DEBUG,
			new String[] {XDConstants.XDPROPERTYVALUE_DEBUG_FALSE,
				XDConstants.XDPROPERTYVALUE_DEBUG_TRUE,
				XDConstants.XDPROPERTYVALUE_DEBUG_SHOWRESULT},
				XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
		//showErrors display mode
		_displayMode = (byte) readProperty(_props,
			XDConstants.XDPROPERTY_DISPLAY,
			new String[] {XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE,
				XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS,
				XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE},
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);
		_debugEditor = _props.getProperty(XDConstants.XDPROPERTY_DEBUG_EDITOR);
		_xdefEditor = _props.getProperty(XDConstants.XDPROPERTY_XDEF_EDITOR);
		//set DOCTYPE illegal
		_illegalDoctype = readProperty(_props,XDConstants.XDPROPERTY_DOCTYPE,
			new String[] {XDConstants.XDPROPERTYVALUE_DOCTYPE_TRUE,
				XDConstants.XDPROPERTYVALUE_DOCTYPE_FALSE},
			XDConstants.XDPROPERTYVALUE_DOCTYPE_TRUE)== 0;
		//ignore undefined external objects
		_ignoreUnresolvedExternals = readProperty(_props,
			XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			new String[] {XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE,
				XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE},
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_FALSE) == 0;
		// generate detailed location information in XML parser
		_locationdetails =  readProperty(_props,
			XDConstants.XDPROPERTY_LOCATIONDETAILS,
			new String[] {XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_TRUE,
				XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_FALSE},
			XDConstants.XDPROPERTYVALUE_LOCATIONDETAILS_FALSE) == 0;

		// Validation of attr names
		_validate = readProperty(_props, XDConstants.XDPROPERTY_VALIDATE,
			new String[] {XDConstants.XDPROPERTYVALUE_VALIDATE_TRUE,
				XDConstants.XDPROPERTYVALUE_VALIDATE_FALSE},
			XDConstants.XDPROPERTYVALUE_VALIDATE_FALSE) == 0;
		//check warnings
		_chkWarnings = readProperty(_props, XDConstants.XDPROPERTY_WARNINGS,
			new String[] {XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE,
				XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE},
				XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE) == 0;
		_resolveIncludes = readProperty(_props,XDConstants.XDPROPERTY_XINCLUDE,
			new String[] {XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE},
			XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE) == 0;
		_minYear = readPropertyYear(_props, XDConstants.XDPROPERTY_MINYEAR);
		_maxYear = readPropertyYear(_props, XDConstants.XDPROPERTY_MAXYEAR);
		_specialDates = readPropertySpecDates(_props);
		_compiler = new CompileXDPool(this,
			_reporter != null ? _reporter : new ArrayReporter(),
			_extClasses,
			_xdefs);
	}
	
////////////////////////////////////////////////////////////////////////////////

	/** Read MIN_YEAR or MAX_YEAR from properties.
	 * @param props properties
	 * @param key key to be read.
	 * @return year or Integer.MIN_VALUE.
	 */
	private static int readPropertyYear(final Properties props,
		final String key) {
		String s = props == null ? null : props.getProperty(key);
		if (s == null || (s = s.trim()).length() == 0) {
			return Integer.MIN_VALUE;
		} else {
			try {
				return Integer.parseInt(s);
			} catch (Exception ex) {
				//Error of property &{0} = &{1} (it must be &{2}
				throw new SRuntimeException(XDEF.XDEF214, key, s, "integer");
			}
		}
	}

	/** Read list of special dates from properties.
	 * @param props Properties where to read.
	 * @return array with special dates or null.
	 */
	private static SDatetime[] readPropertySpecDates(final Properties props) {
		String s = props == null ?
			null : props.getProperty(XDConstants.XDPROPERTY_SPECDATES);
		if (s == null || (s = s.trim()).length() == 0) {
			return null;
		} else {
			StringTokenizer st = new StringTokenizer(s, ", ");
			if (st.countTokens() == 0) {
				return null;
			}
			SDatetime[] result = new SDatetime[st.countTokens()];
			for (int i = 0; i < result.length; i++) {
				try {
					result[i] = new SDatetime(st.nextToken());
				} catch (Exception ex) {
					//Error of property &{0} = &{1} (it must be &{2}
					throw new SRuntimeException(XDEF.XDEF214,
						XDConstants.XDPROPERTY_SPECDATES,
						s,
						"datetime [, datetime ...]");
				}
			}
			return result;
		}
	}

	/** Read property and return the index of a value from the list.
	 * @param props Properties.
	 * @param key name of property.
	 * @param values array of possible property values.
	 * @param dflt default value.
	 * @return index of value in the array.
	 */
	private int readProperty(final Properties props,
		final String key,
		final String[] values,
		final String dflt) {
		String value = props == null ? dflt : props.getProperty(key, dflt);
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return i;
			}
		}
		//Incorrect property value: &{0}
		_compiler.getReportWriter().error(SYS.SYS082, key + "=" + value);
		return 0; // default value
	}

	/** Add source data of X-definition or collection. If the argument starts
	 * with "&lt;" character then it is interpreted as source X-definition data,
	 * otherwise it can be the pathname of the file or URL. If it is a pathname
	 * format then it may contain also wildcard characters representing a group
	 * of files.
	 * @param source The string with source X-definition.
	 * @param sourceId name of source source data corresponding to
	 * the argument source (may be null) used in reporting.
	 * @throws RuntimeException if source is missing or if an error occurs.
	 */
	final void setSource(final String source, final String sourceId) {
		if (getDisplayMode() == DISPLAY_TRUE && sourceId == null
			&& (source.startsWith("?") || source.length() == 0)) {
			if (source.length() <= 1) {
				setSource(
"<xd:def xmlns:xd='"+ XDConstants.XDEF32_NS_URI + "' root=\"a\" name=\"a\">\n"+
"  <a/>\n"+
"</xd:def>", "String[1]");
				return;
			} else {
				try {
					String sid = "String_" + (++_stringItem);
					String src = null;
					if (source.charAt(1) == '<') { //direct XML
						src = source.substring(1);
					} else if (source.endsWith(".xml") &&
						new File(source.substring(1)).exists()) {
						src = source.substring(1);
						sid = source.substring(1, source.length()-2) + "def";
					}
					if (src != null) { // Generate a X-definition from XML
						src = KXmlUtils.nodeToString(GenXDef.genXdef(src),true);
						setSource(src, sid);
						return;
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		String s = sourceId;
		if (source == null || source.length() == 0) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903, sourceId);
			return;
		}
		try {
			char c;
			if ((c = source.charAt(0)) == '<' || c == '[') {
				if (s == null || (s = sourceId.trim()).length() == 0) {
					s = "String_"+ (++_stringItem);
				}
				_sourceInfo.getMap().put(s, new XDSourceItem(source));
				_compiler.parseString(source, s);
			} else if (source.startsWith("//") ||
				(source.indexOf(":/") > 2 && source.indexOf(":/") < 12)) {
				for (String x: SUtils.getSourceGroup(source)) {
					setSource(SUtils.getExtendedURL(x));
				}
			} else {
				File[] files = SUtils.getFileGroup(source);
				if (files == null || files.length == 0) {
					_sourceInfo.getMap().put(
						source, new XDSourceItem(source));
					//X-definition source is missing or null&{0}{: }
					_compiler.getReportWriter().error(XDEF.XDEF903, source);
					return;
				}
				setSource(files);
			}
		} catch (Exception ex) {
			try {
				_sourceInfo.getMap().put(s, new XDSourceItem(source.trim()));
			} catch (Exception e) {
				ex = e;
			}
			if (ex instanceof SThrowable) {
				_compiler.getReportWriter().putReport(
					((SThrowable) ex).getReport());
			} else {
				//X-definition source is missing or null&{0}{: }
				_compiler.getReportWriter().error(XDEF.XDEF903, source);
			}
		}
	}

	/** Add source data of X-definitions or collections. If an item starts with
	 * "&lt;" character then it is interpreted as source data, otherwise
	 * it can be the pathname of the file or URL. If it is a pathname format,
	 * then it may contain also wildcard characters representing a group
	 * of files.
	 * @param sources The string with sources.
	 * @param sourceIds array of names of source source data corresponding to
	 * the sources argument used in reporting (any item or even this argument
	 * may be <tt>null</tt>).
	 * @throws RuntimeException if source is missing or if an error occurs.
	 */
	final void setSource(final String[] sources, final String[] sourceIds) {
		if (sources == null || sources.length == 0) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		for (int i = 0; i < sources.length; i++) {
			setSource(sources[i], sourceIds == null ?
				null : sourceIds.length > i ? sourceIds[i] : null);
		}
	}

	/** Add file with source data of X-definition or collection.
	 * @param source The file with source.
	 */
	final void setSource(final File source) {
		if (source == null) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		try {
			File f = source.getCanonicalFile();
			String key = f.toURI().toASCIIString();
			if (!_sourceInfo.getMap().containsKey(key)) {
				_sourceInfo.getMap().put(key, new XDSourceItem(f));
				_compiler.parseFile(f);
			}
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				_compiler.getReportWriter().putReport(
					((SThrowable) ex).getReport());
			} else {
				//X-definition source is missing or null&{0}{: }
				_compiler.getReportWriter().error(XDEF.XDEF903, source);
				//Program exception&{0}{: }
				_compiler.getReportWriter().error(SYS.SYS036, ex);
			}
		}
	}

	/** Add files with source data of  X-definitions or collections.
	 * @param sources array of files with sources.
	 */
	final void setSource(final File[] sources) {
		if (sources == null || sources.length == 0) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		for (int i = 0; i < sources.length; i++) {
			setSource(sources[i]);
		}
	}

	/** Add URL with source data of X-definition or collection.
	 * @param source The URL with source.
	 */
	final void setSource(final URL source) {
		if (source == null) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		try {
			_sourceInfo.getMap().put(source.toExternalForm(),
				new XDSourceItem(source));
			_compiler.parseURL(source);
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				_compiler.getReportWriter().putReport(
					((SThrowable) ex).getReport());
			} else {
				//X-definition source is missing or null&{0}{: }
				_compiler.getReportWriter().error(XDEF.XDEF903, source);
				//Program exception&{0}{: }
				_compiler.getReportWriter().error(SYS.SYS036, ex);
			}
		}
	}

	/** Add URLs with source data of X-definitions or collections.
	 * @param sources array of URLs with sources.
	 */
	final void setSource(final URL[] sources) {
		if (sources == null || sources.length == 0) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		for (int i = 0; i < sources.length; i++) {
			setSource(sources[i]);
		}
	}

	/** Add input stream with source data of a X-definition or collection.
	 * @param source The input stream with source.
	 * @param sourceId name of source source data corresponding to
	 * stream (may be null).
	 */
	final void setSource(final InputStream source, final String sourceId) {
		String s = sourceId;
		if (s == null || (s = sourceId.trim()).length() == 0) {
			s = "Stream_" + (++_streamItem);
		}
		if (source == null) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903, s);
			return;
		}
		try {
			XDSourceItem xsi = new XDSourceItem(source);
			_sourceInfo.getMap().put(s, xsi);
			if (xsi._source != null && xsi._source.length() > 0) {
				_compiler.parseString(xsi._source, s);
			} else {
				_compiler.parseStream(source, s);
			}
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				_compiler.getReportWriter().putReport(
					((SThrowable) ex).getReport());
			} else {
				//X-definition source is missing or null&{0}{: }
				_compiler.getReportWriter().error(XDEF.XDEF903, source);
				//Program exception&{0}{: }
				_compiler.getReportWriter().error(SYS.SYS036, ex);
			}
		}
	}

	/** Add input streams with sources data of X-definitions or collections.
	 * @param sources array of input streams with sources.
	 * @param sourceIds array of names of source source data corresponding to
	 * streams (may be null).
	 */
	final void setSource(final InputStream sources[],
		final String sourceIds[]) {
		if (sources == null || sources.length == 0) {
			//X-definition source is missing or null&{0}{: }
			_compiler.getReportWriter().error(XDEF.XDEF903);
			return;
		}
		for (int i = 0; i < sources.length; i++) {
			setSource(sources[i], sourceIds == null
				? null : sourceIds.length > i ? sourceIds[i] : null);
		}
	}

	/** Find XModel (called internally also from CompileRerence).
	 * @param xe XMElement whore to find (in child nodes).
	 * @param path position of XModel in XDPool.
	 * @param begIndex where to start.
	 * @param endIndex  where to finish.
	 * @return found XModel.
	 */
	public static XMNode findXMNode(final XMElement xe,
		final String path,
		final int begIndex,
		final int endIndex) {
		int ndx = path.indexOf('/');
		String p,q;
		if (ndx > 0) {
			p = path.substring(0, ndx);
			q = path.substring(ndx + 1);
		} else {
			ndx = path.indexOf('@');
			if (ndx == 0) {
				return xe.getAttr(path.substring(1));
			} else if (ndx > 0) {
				p = path.substring(0, ndx);
				q = path.substring(ndx);
			} else {
				p = path;
				q = null;
			}
		}
		int index; // first
		ndx = p.indexOf('[');
		if (ndx > 0) {
			if (p.indexOf(']') + 1 < p.length()) {
				return null;
			}
			index = Integer.parseInt(p.substring(ndx + 1, p.length() - 1));
			p = p.substring(0, ndx);
		} else {
			index = 1; // first
		}
		if ("text()".equals(p)) {
			p = "$text";
		}
		XMNode[] nodes = xe.getChildNodeModels();
		int endndx = endIndex == -1 ? nodes.length : endIndex;
		for (int i = begIndex; i < endndx; i++) {
			XMNode xn = nodes[i];
			String name = xn.getName();
			if (p.equals(name)) {
				if (--index <= 0) {
					if (q == null) {
						return xn;
					}
					if ("$choice".equals(p) || "$mixed".equals(p)
						|| "$sequence".equals(p)) {
						return findXMNode(xe,
							q, i + 1, ((XMSelector) xn).getEndIndex() + 1);
					} else {
						return findXMNode((XMElement) xn, q, 0, -1);
					}
				}
			}
			if ("$choice".equals(name) || "$mixed".equals(name)
				|| "$sequence".equals(name)) {
				i = ((XMSelector) xn).getEndIndex();
			}
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Get unique id.
	 * @return  unique id.
	 */
	final int getSqId() {return ++_sqId;}

	final Class<?>[] getExtObjects() {return _extClasses;}

	/** Get default X-definition (no named) from the pool.
	 * @return X-definition or <tt>null</tt> if definition doesn't exist.
	 */
	private XDefinition getDefinition() {
		return _xdefs.size() == 1 ?
			_xdefs.values().iterator().next() : _xdefs.get("");
	}

	/** Set list of XComponent binds.
	 * @param p list of XComponents binds.
	 */
	public final void setXComponentBinds(final Map<String, String> p) {
		_binds = p;
	}

	/** Clear source map.
	 * @param fully if true then all items are deleted.
	 */
	final void clearSourcesMap(boolean fully) {
		_stringItem = 0; _streamItem = 0;
		if (fully) {
			_sourceInfo.getMap().clear();
		} else {
			for (XDSourceItem xsi: _sourceInfo.getMap().values()) {
				xsi._active = false;
				xsi._pos = 0;
				if (xsi._source != null) {
					if (xsi._url != null && !xsi._changed) {
						xsi._source = null;
					}
				}
			}
		}
	}

	/** Set code of interpreter.
	 * @param code array with code.
	 */
	final void setCode(final XDValue[] code) {_code = code;}

	/** Get address of initialization code.
	 * @return address of initialization code.
	 */
	final int getInitAddress() {return _init;}

	/** Get max. size of stack.
	 * @return size of stack.
	 */
	final int getStackSize() {return _stackLen;}

	/** Get size of global variables area.
	 * @return size of global variables area.
	 */
	final int getGlobalVariablesSize() {return _globalVariablesSize;}

	/** Get max. size of local variables area.
	 * @return size of local variables area.
	 */
	final int getLocalVariablesSize() {return _localVariablesMaxSize;}

	/** Set code to ScriptCodeInterpreter.
	 * @param code the array with code.
	 * @param globalVariablesSize the size of global variables table.
	 * @param localVariablesMaxSize the maximum size of local variables table.
	 * @param stackMaxSize the maximum size of stack.
	 * @param init the starting point of init action for the code.
	 * @param xdVersion version ID of X-definition.
	 * @param lexicon XDLexicon or null.
	 */
	public final void setCode(final ArrayList<XDValue> code,
		final int globalVariablesSize,
		final int localVariablesMaxSize,
		final int stackMaxSize,
		final int init,
		final byte xdVersion,
		final XDLexicon lexicon) {
		_globalVariablesSize = globalVariablesSize;
		_localVariablesMaxSize = localVariablesMaxSize;
		_lexicon = lexicon;
		_code = new XDValue[code.size()];
		for (int i = 0; i < _code.length; i++) {
			XDValue item = code.get(i);
			//TODO clone some items...
			_code[i] = item;
		}
		_stackLen = stackMaxSize * 8 + 8; //???? - few recursicve calls
		_init = init;
	}

	public final void setVariables(final XVariableTable v) {_variables = v;}

	/** Get global variable of given name.
	 * @param name name of global variable.
	 * @return XMVariable object of the global variable or <tt>null</tt>.
	 */
	final XVariable getVariable(final String name) {
		return _variables!=null ? (XVariable) _variables.getVariable(name):null;
	}

	/** Get X-definition of given name from the pool or nameless X-definition
	 * if argument is the empty string.
	 * @param key name of definition or empty string.
	 * @return X-definition or <tt>null</tt> if definition doesn't exist.
	 */
	final XDefinition getDefinition(final String key) {
		return (key==null || key.length()==0) ? getDefinition():_xdefs.get(key);
	}

	private static void checkModel(ArrayList<XElement> reflist,
		Set<XElement> refset,
		XElement xe) {
		if (refset.add(xe)) {
			if (xe._childNodes == null) {
				if (xe.isReference() && xe._childNodes == null) {
					XElement xe1 = (XElement)
						xe.getXDPool().findModel(xe.getReferencePos());
					if (xe1 != null && xe1._childNodes != null) {
						xe._attrs = xe1._attrs;
						xe._childNodes = xe1._childNodes;
					} else {
						reflist.add(xe);
						return;
					}
				}
			}
			for (XNode xn: xe._childNodes) {
				if (xn.getKind() == XMNode.XMELEMENT) {
					checkModel(reflist, refset, (XElement) xn);
				}
			}
		}
	}

	/** Set debug information.
	 * @param debugInfo debug information object.
	 */
	public void setDebugInfo(XDebugInfo debugInfo) { _debugInfo = debugInfo; }

	/** Get names of global variables.
	 * @return array of names of global variables.
	 */
	final String[] getVariableNames() {return _variables.getVariableNames();}

	/** Set class loader.
	 * @param loader class loader.
	 */
	final void setClassLoader(final ClassLoader loader) {
		_compiler.setClassLoader(loader);
	}

	/** Set list of XComponents.
	 * @param p list of XComponents.
	 */
	public final void setXComponentEnums(final Map<String, String> p){_enums=p;}

	/** Get array with code of XDPool.
	 * @return array with code of XDPool.
	 */
	public final XDValue[] getCode() {return _code;}

////////////////////////////////////////////////////////////////////////////////
// Interface XDPool
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get version information.
	 * @return version information.
	 */
	public final String getVersionInfo() {return XD_VERSION;}

	@Override
	/** Get array with all XMDefinitions from this XDPool.
	 * @return array with all XMDefinitions from this XDPool.
	 */
	public final XMDefinition[] getXMDefinitions() {
		XMDefinition[] result = new XMDefinition[_xdefs.size()];
		_xdefs.values().toArray(result);
		return result;
	}

	@Override
	/** Get array with all X-definitions from this XDPool.
	 * @return array with all X-definitions from this XDPool.
	 */
	public final String[] getXMDefinitionNames() {
		String[] result = new String[_xdefs.size()];
		_xdefs.keySet().toArray(result);
		return result;
	}

	@Override
	/** Get X-definition from this XDPool.
	 * @param name the name of X-definition.
	 * @return specified X-definition from this XDPool.
	 */
	public final XMDefinition getXMDefinition(final String name) {
		return _xdefs.get(name);
	}

	@Override
	/** Find XModel in XDPool.
	 * @param xdpos position of XModel in XDPool.
	 * @return XMNode representing model or null if model was nod found.
	 */
	public final XMNode findModel(final String xdpos) {
		if (xdpos == null) {
			return null;
		}
		int ndx = xdpos.indexOf('#'), ndx1;
		String xdName, path, s;
		if (ndx >= 0) {
			xdName = xdpos.substring(0, ndx);
			path = xdpos.substring(ndx + 1);
		} else {
			if (xdpos.indexOf('/') < 0) {
				xdName = xdpos; path = ""; // just XDef name
			} else {
				xdName = ""; path = xdpos; // nameless X-definition and path
			}
		}
		XDefinition xd = _xdefs.get(xdName);
		if (xd == null) {
			return null;
		}
		if ("*".equals(path)) {
			return xd._rootSelection!=null ? xd._rootSelection .get("*") : null;
		}
		if (path.length() == 0) {
			return xd;
		}
		ndx = path.indexOf('/');
		if (ndx == 0) {
			path = path.substring(1);
		}
		if ((ndx1=path.indexOf('$')+1) > path.length() + 1
			&& path.charAt(ndx1) == '$') {// e.g. A#M$mixed/$mixed/Bnnjm
			String t = path.substring(0, ndx1); // name
			String u = path.substring(ndx1); // rest
			if (u.startsWith("$choice")) {
				path = t + "choice/" + u;
			} else if (u.startsWith("$mixed")) {
				path = t + "mixed/" + u;
			} else if (u.startsWith("$sequence")) {
				path = t + "sequence/" + u;
			}
		}
		ndx = path.indexOf('/');
		s = ndx >= 0 ? path.substring(0, ndx) : path;
		if (s.endsWith("[1]")) {
			s = s.substring(0, s.length() - 3).trim();
		}
		//search models of X-definition
		for (XMElement xe: xd.getModels()) {
			String x = xe.getName();
			String y = s;
			String z = null;
			if ((ndx1 = x.indexOf('$')) > 0) {
				z = x.substring(ndx1);
				y += z;
			}
			if (y.equals(x)) {
				if ("$any".equals(z)) {
					xe = (XMElement) xe.getChildNodeModels()[0];
					if (ndx == 0) {
						return xe;
					} else if (path.substring(ndx+1).startsWith(xe.getName())) {
						ndx = path.indexOf('/', ndx + 1);
					}
					z = null;
				}
				if (ndx < 0) {
					return xe;
				}
				path = path.substring(ndx + 1);
				if (z != null) {
					path = z + "/" + path;
				}
				return findXMNode(xe, path, 0, -1);
			}
		}
		ndx1 = s.indexOf(':');
		String nsURI = xd._namespaces.get(ndx1 > 0 ? s.substring(0, ndx1) : "");
		XMElement xe = xd.getModel(nsURI, s);
		return (ndx < 0 || xe == null) ?
			xe : findXMNode(xe, path.substring(ndx + 1), 0, -1);
	}

	@Override
	/** Get nameless X-definition from this XDPool.
	 * @return nameless X-definition from this XDPool.
	 */
	public final XMDefinition getXMDefinition() {return getDefinition();}

	@Override
	/** Check if debug mode is set on.
	 * @return value of debug mode.
	 */
	public final boolean isDebugMode() {return _debugMode > 0;}

	@Override
	/** Check if show result mode is set for debug mode.
	 * @return true if show result mode is set.
	 */
	public final boolean isDebugShowResult() {return _debugMode > 1;}

	@Override
	/** Get display mode.
	 * @return display mode.
	 */
	public final byte getDisplayMode(){return _displayMode;}

	@Override
	/** Check if unresolved externals will be ignored.
	 * @return true if unresolved externals will be ignored.
	 */
	public final boolean isIgnoreUnresolvedExternals() {
		return _ignoreUnresolvedExternals;
	}

	@Override
	/** Check if exists the X-definition of given name.
	 * @param name the name of X-definition (or <tt>null</tt>) if
	 * noname X-definition is checked.
	 * @return true if and only if the X-definition of given name exists in
	 * the XDPool.
	 */
	public final boolean exists(final String name) {
		return _xdefs.containsKey(name == null ? "" : name);
	}

	@Override
	/** Get table of global variables.
	 * @return table of global variables.
	 */
	public final XMVariableTable getVariableTable() {return _variables;}

	@Override
	/** Print code to PrintStream.
	 * @param out stream where code is printed.
	 */
	public final void displayCode(final PrintStream out) {
		CodeDisplay.displayCode(_code, out);
	}

	@Override
	/** Print code from XDPool.
	 * @param out PrintStream where pool is printed.
	 */
	public final void display(final PrintStream out) {
		out.println("ScriptCode init = " + getInitAddress());
		CodeDisplay.displayCode(_code, out);
		Set<XNode> processed = new HashSet<XNode>();
		for (String x: _xdefs.keySet()) {
			CodeDisplay.displayDefNode(getDefinition(x), out, processed);
		}
	}

	@Override
	/** Display XDPool on System.out. */
	public final void display() {display(System.out);}

	@Override
	/** Display code of XDPool on System.out. */
	public final void displayCode() {displayCode(System.out);}

	@Override
	/** Display debugging information of XDPool.
	 * @param out PrintStream where pool is printed.
	 */
	public final void displayDebugInfo(final PrintStream out) {
		CodeDisplay.displayDebugInfo(this, out);
	}

	@Override
	/** Display debugging information of XDPool on System.out. */
	public final void displayDebugInfo() {displayDebugInfo(System.out);}

	final Properties getProperties() {return _props;}

	@Override
	/** Create new XDDocument with default X-definition.
	 * @return the XDDocument object.
	 */
	public final XDDocument createXDDocument() {
		return new ChkDocument(getDefinition());
	}

	@Override
	/** Create new XDDocument.
	 * @param id Identifier of X-definition (or <tt>null</tt>).
	 * @return the XDDocument object.
	 * @throws SRuntimeException if X-definition doesn't exist.
	 */
	public final XDDocument createXDDocument(final String id) {
		if (id == null || id.length() == 0) {
			return createXDDocument();
		}
		int ndx = id.indexOf('#');
		if (ndx < 0) {
			XDefinition xd = getDefinition(id);
			if (xd == null) {
				//The X-definition&{0}{ '}{'} is missing
				throw new SRuntimeException(XDEF.XDEF602, id);
			}
			return new ChkDocument(getDefinition(id));
		} else {
			XMNode xn = findModel(id);
			if (xn != null  && xn.getKind() == XMNode.XMELEMENT) {
				return ((XMElement) xn).createXDDocument();
			}
			//'&{0' doesn't point to model of element
			throw new SRuntimeException(XDEF.XDEF603, id);
		}
	}

	@Override
	/** Get debug information or null.
	 * @return debug information object.
	 */
	public final XMDebugInfo getDebugInfo() {return _debugInfo;}

	@Override
	/** Get switch if the parser allows XML XInclude.
	 * @return true if the parser allows XInclude.
	 */
	public final boolean isResolveIncludes() {return _resolveIncludes;}

	@Override
	/** Get the switch if XML parser will generate detailed location reports.
	 * @return the location details switch.
	 */
	public final boolean isLocationsdetails() {return _locationdetails;}

	@Override
	/** Get switch if the parser do not allow DOCTYPE.
	 * @return true if the parser do not allow DOCTYPE or return false
	 * if DOCTYPE is processed.
	 */
	final public boolean isIllegalDoctype() {return _illegalDoctype;}

	@Override
	/** Get switch if the parser will check warnings as errors.
	 * @return true if the parser checks warnings as errors.
	 */
	final public boolean isChkWarnings() {return _chkWarnings;}

	@Override
	/** Get list of XComponents.
	 * @return list of XComponents.
	 */
	public final Map<String, String> getXComponents() {return _components;}

	/** Set list of XComponents.
	 * @param p list of XComponents.
	 */
	public final void setXComponents(Map<String, String> p) {_components=p;}

	@Override
	/** Get list of XComponent binds.
	 * @return list of XComponent binds.
	 */
	public final Map<String, String> getXComponentBinds() {return _binds;}

	@Override
	/** Get list of XComponent enumerations.
	 * @return list of XComponent enumerations.
	 */
	public Map<String, String> getXComponentEnums() {return _enums;}

	@Override
	/** Get minimum valid year of date.
	 * @return minimum valid year (Integer.MIN if not set).
	 */
	public final int getMinYear() {return _minYear;}

	@Override
	/** Get maximum valid year of date (or Integer.MIN if not set).
	 * @return maximum valid year (Integer.MIN if not set).
	 */
	public final int getMaxYear()  {return _maxYear;}

	@Override
	/** Get array of dates to be accepted out of interval minYear..maxYear.
	 * @return array with special values of valid dates.
	 */
	public final SDatetime[] getSpecialDates() {return _specialDates;}

	@Override
	/** Get the object with the map of source items of compiled X-definitions
	 * and with editing information.
	 * @return object with the map of source items of compiled X-definitions
	 * and with editing information.
	 */
	public XDSourceInfo getXDSourceInfo() {return _sourceInfo;}

	@Override
	/** Get debug editor class name.
	 * @return debug editor class name (if null. the default debug editor
	 * will be used).
	 */
	public final String getDebugEditor() {return _debugEditor;}

	@Override
	/** Get class name of the editor of X-definition.
	 * @return class name of the editor of X-definition which
	 * will be used).
	 */
	public final String getXdefEditor() {return _xdefEditor;}

	/** Write this XDPool to stream.
	 * @param out where to write.
	 * @throws IOException if an error occurs.
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		GZIPOutputStream gout = new GZIPOutputStream(out);
		XDWriter xw = new XDWriter(gout);
		xw.writeShort(XD_MAGIC_ID); //XDPool file ID
		xw.writeString(getVersionInfo()); //XD verze
		xw.writeByte(_debugMode);
		xw.writeString(_debugEditor);
		xw.writeString(_xdefEditor);
		xw.writeBoolean(_illegalDoctype);
		xw.writeBoolean(_ignoreUnresolvedExternals);
		xw.writeBoolean(_locationdetails);
		xw.writeBoolean(_validate);
		xw.writeBoolean(_chkWarnings);
		xw.writeBoolean(_resolveIncludes);
		xw.writeByte(_displayMode);
		xw.writeInt(_minYear);
		xw.writeInt(_maxYear);
		int len = _specialDates == null ? 0 : _specialDates.length;
		xw.writeLength(len);
		for (int i = 0; i < len; i++) {
			xw.writeSDatetime(_specialDates[i]);
		}
		len = _extClasses == null ? 0 : _extClasses.length;
		xw.writeLength(len);
		for (int i = 0; i < len; i++) {
			xw.writeString(_extClasses[i].getName());
		}
		if (_lexicon == null) {
			xw.writeLength(0);
		} else {
			String[] languages = _lexicon.getLanguages();
			len = languages.length;
			xw.writeLength(len);
			for (int i = 0; i < len; i++) {
				xw.writeString(languages[i]);
			}
			String[] keys = _lexicon.getKeys();
			len = keys.length;
			xw.writeLength(len);
			for (int i = 0; i < len; i++) {
				String key = keys[i];
				xw.writeString(key);
				String[] words = _lexicon.findTexts(key);
				for (String word: words) {
					xw.writeString(word);
				}
			}
		}
		len = _components == null ? 0 : _components.size();
		xw.writeLength(len);
		if (len > 0) {
			for (String s: _components.keySet()) {
				xw.writeString(s);
				xw.writeString(_components.get(s));
			}
		}
		len = _binds == null ? 0 : _binds.size();
		xw.writeLength(len);
		if (len > 0) {
			for (String s: _binds.keySet()) {
				xw.writeString(s);
				xw.writeString(_binds.get(s));
			}
		}
		len = _enums == null ? 0 : _enums.size();
		xw.writeLength(len);
		if (len > 0) {
			for (String s: _enums.keySet()) {
				xw.writeString(s);
				xw.writeString(_enums.get(s));
			}
		}
		len = _code.length;
		xw.writeLength(len);
		for (int i = 0; i < len; i++) {
			xw.writeXD(_code[i]);
		}
		_variables.writeXD(xw);
		xw.writeLength(_stackLen);
		xw.writeInt(_init);
		xw.writeInt(_globalVariablesSize);
		xw.writeInt(_localVariablesMaxSize);
		len = _xdefs.size();
		xw.writeLength(len);
		ArrayList<XNode> list = new ArrayList<XNode>();
		for(String name: _xdefs.keySet()) {
			xw.writeString(name);
			getDefinition(name).writeXNode(xw, list);
		}
		//we must write root selections after X-definitions are processed!
		for(String name: _xdefs.keySet()) {
			xw.writeString(name);
			XDefinition xd = getDefinition(name);
			len = xd._rootSelection == null ? 0 : xd._rootSelection.size();
			xw.writeLength(len);
			//here are references to models, so we write names and XPositions!
			for (String key: xd._rootSelection.keySet()){
				xw.writeString(key);
				if (!"*".equals(key)) {
					XElement xel = (XElement) xd._rootSelection.get(key);
					xw.writeString(xel._definition.getName());
					xw.writeString(xel.getXDPosition());
				}
			}
		}
		_sourceInfo.writeXDSourceInfo(xw);
		if (_debugInfo == null) {
			xw.writeString(null);
		} else {
			xw.writeString("DebugInfo");
			_debugInfo.writeXD(xw);
		}
		xw.writeShort(XD_MAGIC_ID); //XDPool file ID
		gout.finish();
	}

	/** This method uses the Serializable interface.
	 * @param input from where to read.
	 * @throws IOException if an error occurs.
	 */
	private void readObject(java.io.ObjectInputStream input)
		throws IOException, ClassNotFoundException {
		_xdefs = new LinkedHashMap<String, XDefinition>();
		_sourceInfo = new XDSourceInfo();
		GZIPInputStream in = new GZIPInputStream(input);
		XDReader xr = new XDReader(in);
		if (XD_MAGIC_ID != xr.readShort()) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "Incorrect file format");
		}
		String ver = xr.readString(); //XDPool version
		try {
			// check if version is compatible with this implementation
			String[] verParts = ver.split("\\."); // verion parts
			if (!verParts[0].startsWith("XD")) {
				throw new Exception("Version error");
			}
			verParts[0] = verParts[0].substring(2);
			long x;
			if (verParts.length == 3) {
				x = Integer.parseInt(verParts[0]);
				x = (x / 10) * 100 + (x % 10);
				x = x * 1000 + Integer.parseInt(verParts[1]);
				x = x * 1000 + Integer.parseInt(verParts[2]);
			} else if (verParts.length==4) {
				x = Integer.parseInt(verParts[0]) * 100
					+ Integer.parseInt(verParts[1]);
				x = x * 1000 + Integer.parseInt(verParts[2]);
				x = x * 1000 + Integer.parseInt(verParts[3]);
			} else {
				throw new Exception("Version error");
			}
			if (x < XD_MIN_VERSION) {
				throw new Exception("Version error");
			}
		} catch (Exception ex) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, ex, "Version error: " + ver);
		}
		_debugMode = xr.readByte();
		_debugEditor = xr.readString();
		_xdefEditor = xr.readString();
		_illegalDoctype = xr.readBoolean();
		_ignoreUnresolvedExternals = xr.readBoolean();
		_locationdetails =  xr.readBoolean();
		_validate = xr.readBoolean();
		_chkWarnings = xr.readBoolean();
		_resolveIncludes = xr.readBoolean();
		_displayMode = xr.readByte();
		_minYear = xr.readInt();
		_maxYear = xr.readInt();
		int len = xr.readLength();
		_specialDates = new SDatetime[len];
		for (int i = 0; i < len; i++) {
			_specialDates[i] = xr.readSDatetime();
		}
		len = xr.readLength();
		_extClasses = new Class<?>[len];
		for (int i = 0; i < len; i++) {
			try {
				_extClasses[i] = Class.forName(xr.readString(),
					false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException ex) {
				//SObject reader: incorrect format of data&{0}{: }
				throw new SIOException(SYS.SYS039, ex);
			}
		}
		len = xr.readLength();
		if (len > 0) {
			String[] languages = new String[len];
			for (int i = 0; i < len; i++) {
				languages[i] = xr.readString();
			}
			XLexicon t = new XLexicon(languages);
			len = xr.readLength(); // number of aliases
			for (int i = 0; i < len; i++) {
				String base = xr.readString();
				for (int j = 0; j < languages.length; j++) {
					t.setItem(base, j, xr.readString());
				}
			}
			_lexicon = t;
		}
		len = xr.readLength();
		_components = new LinkedHashMap<String, String>();
		for (int i = 0; i < len; i++) {
			String s = xr.readString();
			_components.put(s, xr.readString());
		}
		len = xr.readLength();
		_binds = new LinkedHashMap<String, String>();
		for (int i = 0; i < len; i++) {
			String s = xr.readString();
			_binds.put(s, xr.readString());
		}
		len = xr.readLength();
		_enums = new LinkedHashMap<String, String>();
		for (int i = 0; i < len; i++) {
			String s = xr.readString();
			_enums.put(s, xr.readString());
		}
		ArrayList<XNode> list = new ArrayList<XNode>();
		len = xr.readLength();
		_code = new XDValue[len];
		for (int i = 0; i < len; i++) {
			try {
				_code[i] = xr.readXD();
			} catch (Exception ex) {
				//SObject reader: incorrect format of data&{0}{: }
				throw new SIOException(SYS.SYS039,
					ex, "code["+i+"]; " + _code[i]);
			}
		}
		_variables = XVariableTable.readXD(xr);
		_stackLen = xr.readLength();
		_init = xr.readInt();
		_globalVariablesSize = xr.readInt();
		_localVariablesMaxSize = xr.readInt();
		_xdefs = new LinkedHashMap<String, XDefinition>();
		len = xr.readLength();
		for(int i = 0; i < len; i++) {
			try {
				_xdefs.put(xr.readString(),
					XDefinition.readXDefinition(xr, this, list));
			} catch (Exception ex) {
				//SObject reader: incorrect format of data&{0}{: }
				throw new SIOException(SYS.SYS039, ex);
			}
		}
		//resolve root selections - references to models!
		for (int i = 0; i < len; i++) {
			String name = xr.readString();
			XDefinition xd = getDefinition(name);
			int len1 = xr.readLength();
			for (int j = 0; j < len1; j++) {
				String key = xr.readString();
				XNode ref;
				if ("*".equals(key)) {
					XElement xe = new XElement("$any", null, xd);
					xe._moreAttributes = 'T';
					xe._moreElements = 'T';
					xe.setOccurrence(0, Integer.MAX_VALUE);
					xe.setXDPosition(xd.getXDPosition() + "*");
					ref = xe;
				} else {
					String refDefName = xr.readString();
					XDefinition refXd = getDefinition(refDefName);
					String refName = xr.readString();
					ref = (XNode) refXd.getXDPool().findModel(refName);
				}
				xd._rootSelection.put(key, ref);
			}
		}
		_sourceInfo.copyFrom(XDSourceInfo.readXDSourceInfo(xr));
		if ("DebugInfo".equals(xr.readString())) {
			_debugInfo = XDebugInfo.readXDebugInfo(xr);
		}
		if (XD_MAGIC_ID != xr.readShort()) {
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "Incorrect file format");
		}
		ArrayList<XElement> reflist = new ArrayList<XElement>();
		Set<XElement> refset = new HashSet<XElement>();
		for(XDefinition xd: _xdefs.values()) {
			for (XMElement xe: xd.getModels()) {
				checkModel(reflist, refset, (XElement) xe);
			}
		}
		refset.clear();
		while(reflist.size() > 0) {
			for (int i = reflist.size() -1; i >= 0; i--) {
				XElement xe = reflist.get(i);
				XMNode xm;
				if (xe.isReference() &&
					(xm=xe.getXDPool().findModel(xe.getReferencePos()))!=null) {
					XElement xe1 = (XElement) xm;
					if (xe1._childNodes != null) {
						xe._childNodes = xe1._childNodes;
						xe._attrs = xe1._attrs;
						reflist.remove(i);
					}
				}
			}
		}
	}
}