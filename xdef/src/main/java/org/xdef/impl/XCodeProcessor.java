package org.xdef.impl;

import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXQueryExpr;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.DefRegex;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefObject;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefReport;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SError;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.StringParser;
import org.xdef.proc.XXException;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDCallItem;
import org.xdef.XDDebug;
import org.xdef.XDElement;
import org.xdef.XDException;
import org.xdef.XDInput;
import org.xdef.XDNamedValue;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDRegex;
import org.xdef.XDRegexResult;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDXmlOutStream;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeSWTableInt;
import org.xdef.impl.code.CodeSWTableStr;
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.code.CodeTable;
import org.xdef.impl.code.CodeXD;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMDefinition;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SManager;
import org.xdef.XDContainer;
import org.xdef.XDPool;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import org.xdef.impl.code.DefLocale;
import org.xdef.impl.debug.ChkGUIDebug;
import java.lang.reflect.Constructor;
import java.util.Locale;
import org.xdef.XDConstants;
import org.xdef.XDUniqueSetKey;

/** Provides processor engine of script code.
 * @author Vaclav Trojan
 */
public final class XCodeProcessor implements XDValueID, CodeTable {

	/** This identifier is created if it is undefined. */
	private static final String UNDEF_ID = "__UNDEF_ID__";
	/** Switch to allow/restrict DOCTYPE in XML. */
	/** Program code */
	private XDValue[] _code;
	/** Address of code initialization. */
	private int _init;
	/** Processor stack. */
	private XDValue[] _stack;
	/** Local variables. */
	private XDValue[] _localVariables;
	/** Local string parser */
	private StringParser _textParser;
	/** Call list - must be implementation, not interface! */
	private CallItem _callList;
	/** Catch list. */
	private CatchItem _catchItem;
	/** Properties. */
	private Properties _props;
	/** True if and only if version of XML document is 1.0.*/
	private boolean _xmlVersion1;
	/** Switch to debug mode. */
	private boolean _debug = false; //debug switch
	/** X-definition from which processor was created. */
	private XDefinition _xd;
	/** Global variables:<p>
	 * _globalVariables[i]; i=0 stdOut, 1 stdErr, 2 stdIn.
	 * 3 $IDParser$, 4 $IDuniqueSet$
	 * </p>
	 * Follows global variables from DefPool.
	 */
	private XDValue[] _globalVariables;
	/** Temporary  reporter */
	ArrayReporter _reporter;
	/** XML writer for default output. */
	private XDXmlOutStream _outWriter;
	/** Stream writer for output of XML result. */
	boolean _flushed;
	/** flag 'init1' processed. */
	private boolean _initialized1;
	/** flag global variables initialized. */
	private boolean _initialized2;
	/** debugger */
	private XDDebug _debugger;
	/** debug information. */
	private XMDebugInfo _debugInfo;
	/** List of items to be managed at the end of process. */
	private ArrayList<XDValue> _finalList;
	/** Map of named user objects. */
	private final Map<String, Object> _userObjects =
		new LinkedHashMap<String, Object>();
	/** XPath function resolver. */
	final XPathFunctionResolver _functionResolver = new XDFunctionResolver();
	/** XPath variable resolver. */
	final XPathVariableResolver _variableResolver = new XDVariableResolver();

	/** Creates a new instance of ScriptCodeProcessor
	 * @param xd XDefinition.
	 * @param reporter container for error messages.
	 * @param stdOut standard output stream (if null then java.lang.System.out).
	 * @param stdIn standard input stream (if null then java.lang.System.in).
	 * @param userObj Assigned user's object.
	 */
	XCodeProcessor(final XDefinition xd,
		final SReporter reporter,
		final XDOutput stdOut,
		final XDInput stdIn) {
		init(xd, null);
		init1(xd, reporter.getReportWriter(), stdOut, stdIn);
	}

	/** Creates new instance of ScriptCodeProcessor. This constructor is called
	 * only internally from ChkComposer.
	 * @param xd XDefinition.
	 * @param ce ChkElement from which the object is created.
	 */
	XCodeProcessor(final XDefinition xd, final ChkElement ce) {
		init(xd, ce._scp._props);
		_globalVariables = ce._scp._globalVariables;
		_debugger = ce._scp.getDebugger();
		_reporter = ce.getTemporaryReporter();
		_reporter.clear();
		_textParser.setReportWriter(_reporter);
		_globalVariables = ce._scp._globalVariables;
		_code = ce._scp._code;
		_init = ce._scp._init;
		_initialized1 = true;
		_initialized2 = true;
	}

	/** Get root XDefinition. */
	final XDefinition getXDefinition() {return _xd;}

	/** Set properties.
	 * @param props properties used by processor.
	 */
	final void setProperties(final Properties props) {
		if (props == null) {
			_props = new Properties();
		} else {
			_props = props;
			_debug = XDConstants.XDPROPERTYVALUE_DEBUG_TRUE.equals(
				SManager.getProperty(_props, XDConstants.XDPROPERTY_DEBUG));
			SManager.setProperties(props);
		}
	}

	/** Set X-definition property to SManager. If properties are null
	 * the new properties  will be created.
	 * @param key name of X-definition property.
	 * @param value value of property or null. If the value is null the property
	 * is removed from properties.
	 */
	public final void setProperty(final String key, final String value) {
		String newKey = key.startsWith("xdef.") ? key.replace('.', '_') : key;
		if (XDConstants.XDPROPERTY_DEBUG.equals(newKey)) {
			_debug = XDConstants.XDPROPERTYVALUE_DEBUG_TRUE.equals(value);
		}
		if (_props == null) {
			if (value == null) {
				return;
			}
			_props = new Properties();
		}
		_props.remove(key);
		if (value != null) {
			_props.setProperty(newKey, value);
		} else {
			_props.remove(newKey);
		}
		if (newKey.startsWith(XDConstants.XDPROPERTY_MESSAGES)
			|| newKey.startsWith(XDConstants.XDPROPERTY_MSGLANGUAGE)) {
			SManager.setProperty(newKey, value);
		}
	}

	/** Get assigned properties.
	 * @return assigned properties.
	 */
	final Properties getProperties() {return _props;}

	/** Get array with global variables.
	 * @return array with global variables.
	 */
	final XDValue[] getGlobalVariables() {return _globalVariables;}

	/** Set XML version  "1.0" or "1.1".
	 * @param version1 if true the version of XML document is set to "1.1".
	 */
	final void setXMLVersion1(final boolean version1) {_xmlVersion1 = version1;}

	/** Check if version of XML document is "1.1".
	 * @return true if and only if version of XML document is "1.1".
	 */
	final boolean isXMLVersion1() {return _xmlVersion1;}

	/** Get assigned standard output stream.
	 * @return assigned standard output stream.
	 */
	final XDOutput getStdOut() {return (XDOutput) _globalVariables[0];}

	/** Get assigned standard error stream.
	 * @return assigned standard error stream.
	 */
	final XDOutput getStdErr() {return (XDOutput) _globalVariables[1];}

	/** Get assigned standard input stream.
	 * @return assigned standard input stream.
	 */
	final XDInput getStdIn() {return (XDInput) _globalVariables[2];}

	/** Set standard output stream.
	 * @param out standard output stream.
	 */
	final void setStdOut(final XDOutput out) {_globalVariables[0] = out;}

	/** Set standard error stream.
	 * @param err standard error stream.
	 */
	final void setStdErr(final XDOutput err) {_globalVariables[1] = err;}

	/** Set standard input stream.
	 * @param in standard input stream.
	 */
	final void setStdIn(final XDInput in) {_globalVariables[2] = in;}

	/** Set debugger.
	 * @param debugger the debugger.
	 */
	final void setDebugger(final XDDebug debugger) {_debugger = debugger;}

	/** Get debugger.
	 * @return the debugger.
	 */
	final XDDebug getDebugger() {return _debugger;}

	/** Set debugging mode.
	 * @param debug debugging mode.
	 */
	final void setDebug(final boolean debug) { _debug = debug; }

	/** Check debugging mode is set ON.
	 * @return value of debugging mode.
	 */
	final boolean isDebugMode() { return _debug && _debugger != null; }

	/** Base initialization of code processor engine. */
	private void init(final XDefinition xd, final Properties props) {
		_xd = xd;
		XPool xp = (XPool) xd.getXDPool();
		_xmlVersion1 = false;
		_init = xp.getInitAddress();
		_stack = new XDValue[xp.getStackSize()];
		_localVariables = new XDValue[xp.getLocalVariablesSize()];
		_textParser = new StringParser(""); //create string parser
		if (props != null) {
			_props = props;
		}
		_debug = xp.isDebugMode() || _props != null &&
			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE.equals(
				SManager.getProperty(_props, XDConstants.XDPROPERTY_DEBUG));
		if (_debug) {
			if (_debugger == null) {
				String debugEditor = xp.getDebugEditor();
				if (debugEditor == null) {
				} else {
					try {
						Class<?> cls = Class.forName(debugEditor);
						Constructor<?> c = cls.getDeclaredConstructor(
							Properties.class, XDPool.class);
						_debugger = (XDDebug) c.newInstance(null, xp);
					} catch (Exception ex) {
						_debugger = null; // will be used the default debugger
						// Class with the external debug editor &{0}{"}{"}
						// is not available.
						throw new SRuntimeException(
							XDEF.XDEF850, ex, debugEditor);
					}
				}
				if (_debugger == null) {
					//default debugger
					_debugger = new ChkGUIDebug(getProperties(), xp);
				}
			}
			_debugInfo = xp.getDebugInfo();
		}
	}

	/** Extended initialization of code processor engine. */
	private void init1(final XDefinition xd,
		final ReportWriter rw,
		final XDOutput out,
		final XDInput in) {
		if (!_initialized1) {
			_reporter = new ArrayReporter(); //create temporary reporter.
			_textParser.setReportWriter(_reporter); // set reporter
			XPool xp = (XPool) xd.getXDPool();
			XDValue[] code = xp.getCode();
			_code = new XDValue[code.length];
			for (int i = 0; i < code.length; i++) {
				XDValue x = code[i];
				// to assure reeentrancy of XDPool create clones of constants.
				_code[i] = x.getCode() != LD_CONST ? x : x.cloneItem();
			}
			_globalVariables = new XDValue[xp.getGlobalVariablesSize()];
			_globalVariables[0] = out == null ?
				new DefOutStream(System.out) : out;
			_globalVariables[1] = rw == null ?
				new DefOutStream(System.err) : new DefOutStream(rw);
			_globalVariables[2] = in == null ?
					new DefInStream(System.in, false) : in;
			_globalVariables[3] = null; // "QName" parser
			_globalVariables[4] = null; // CodeUniqueset for ID,IDREF, ...
			_initialized1 = true;
		}
		_initialized2 = false; //initialize global variables at execution
	}

	/** Add the object to final list.
	 * @param x the object to be added.
	 */
	void addToFinalList(final ChkNode chkNode, final XDValue x) {
		if (chkNode != null && chkNode._parent != null) {
			//probably in initialization of XDDocument
			chkNode._parent.addToFinalList(x);
		} else  {
			if (_finalList == null) {
				_finalList = new ArrayList<XDValue>();
			}
			_finalList.add(x);
		}
	}


	/** Check if the value from argument is assigned to a global variable.
	 * @param xv the value to be checked.
	 * @return true if variable is assigned to a global variable.
	 */
	private boolean isInInGlobals(final XDValue xv) {
		for (int i = 2; i < _globalVariables.length; i++) {
			if (xv == _globalVariables[i]) {
				return true;
			}
		}
		return false;
	}

	/** Close ResultSet object (a database result).
	 * @param x ResultSet object.
	 */
	final void closeResultSet(final XDResultSet x) {
		if (x != null && !x.isClosed() && !isInInGlobals(x)) {
			x.close();
		}
	}

	/** Close items in final list (if present).
	 * @param finalList array of items in final list or null.
	 */
	final void closeFinalList(final ArrayList<XDValue> finalList) {
		// if final list not exists, do nothing
		if (finalList == null) {
			return;
		}
		// 1) close all ResultSet objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_RESULTSET) {
				closeResultSet((XDResultSet) xv);
			}
		}
		// 2) close all Statement objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_STATEMENT && !isInInGlobals(xv)) {
				((XDResultSet) xv).close();
			}
		}
		//3) close all Service objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_SERVICE && !isInInGlobals(xv)) {
				((XDService) xv).close();
			}
		}
	}

	/** Check all lists of unresolved references and close XD objects.
	 * @return true if no unresolved references were found.
	 */
	final boolean endXDProcessing() {
		// close debugger
		XDDebug debuger = getDebugger();
		if (_debug && debuger != null) { // close debugger
			debuger.closeDebugger();
		}
		closeFinalList(_finalList);
		boolean result = true;
		if (_globalVariables != null) {
			XVariableTable vartab =
				(XVariableTable) _xd.getXDPool().getVariableTable();
			for (int i = 0; i < _globalVariables.length; i++) {
				XDValue val = _globalVariables[i];
				XVariable xvar = vartab.getXVariable(i);
				if (val != null && !val.isNull()) {
					short itemId = val.getItemId();
					switch (itemId) {
						case CompileBase.UNIQUESET_VALUE:
						case CompileBase.UNIQUESET_M_VALUE: {
							// pending references
							CodeUniqueset pt = (CodeUniqueset) val;
							result &= pt.checkAndClear(_reporter);
							break;
						}
						case CompileBase.XD_SERVICE:
						case CompileBase.XD_STATEMENT:
						case CompileBase.XD_RESULTSET:
							// close all not external database objects
							if (xvar != null && !xvar.isExternal()) {
								if (itemId == XD_SERVICE) {
									((XDService) val).close();
								} else if (itemId == XD_STATEMENT) {
									((XDStatement) val).close();
								} else {
									((XDResultSet) val).close();
								}
							}
							break;
						case CompileBase.XD_INPUT: //close input streams
							if (xvar != null && !xvar.isExternal()
								&& !xvar.getName().equals("$stdIn")) {
								// close if not $stdIn and not external
								((DefInStream) val).close();
							}
							break;
						case CompileBase.XD_OUTPUT: //close out streams
							if (xvar != null) {
								DefOutStream out = (DefOutStream) val;
								if (xvar.isExternal()
									|| xvar.getName().equals("$stdOut")
									|| xvar.getName().equals("$stdErr")) {
									// external, stdOut and stdErr just flush
									out.flush();
								} else {
									out.close(); // other streams close
								}
							}
							break;
					}
				}
			}
		}
		return result;
	}

	/** Get value of variable from global variables.
	 * @param index index to variable block.
	 * @return XDValue object or <ttt>null</tt>.
	 */
	final XDValue getVariable(final String name) {
		if (_xd != null) {
			XVariable xv = _xd.findVariable(name);
			if (xv != null) {
				int addr = xv.getOffset();
				if (addr >= 0) {
					return _globalVariables[addr];
				}
			}
		}
		return null;
	}

	/** Set global value of variable from variables.
	 * @param value XDValue object to be set.
	 * @param xvar global XVariable.
	 */
	final void setVariable(final XVariable xvar, final XDValue value) {
		_globalVariables[xvar.getOffset()] = value;
	}

	/** Get temporary reporter.
	 * @return ArrayReporter used as temporary reporter.
	 */
	final ArrayReporter getTemporaryReporter() {return _reporter;}

	/** Set temporary reporter.
	 * @param reporter ArrayReporter to be set as temporary reporter.
	 */
	final void setTemporaryReporter(final ArrayReporter reporter) {
		_reporter = reporter;
	}

	/** Set XML output stream.
	 *  @param stream XML output stream to be set.
	 */
	final void setXmlStreamWriter(final XDXmlOutStream stream) {
		_outWriter = stream;
	}

	/** Get XML output stream.
	 *  @return the XML output stream.
	 */
	final XDXmlOutStream getXmlStreamWriter() {return _outWriter;}

	/** Clear temporary reporter. */
	final void clearReports() {_reporter.clear();}

	/** Remove given report from temporary reporter. */
	final void removeReport(final Report rep) {_reporter.removeReport(rep);}

	/** Get the default uniqueSet (used for ID, IDREF etc).
	 * @return the default uniqueSet object.
	 */
	final CodeUniqueset getIdRefTable() {
		if (_globalVariables[4] == null) {
			 CodeUniqueset.ParseItem[] parseItems =
				new CodeUniqueset.ParseItem[] {
					new CodeUniqueset.ParseItem("", // no key name
						null, // refName
						-1, // chkAddr,
						0, // itemIndex,
						XD_STRING, // parsedType,
						true) // required item
				}; // optional
			_globalVariables[3] = CompileBase.getParser("QName");
			_globalVariables[4] =
				new CodeUniqueset(parseItems, new String[0], "");
		}
		return (CodeUniqueset) _globalVariables[4];
	}

	final StringParser getStringParser() {return _textParser;}

	/** Initialize script variables and methods. */
	final void initscript() {
		if (!_initialized2) { //not initialized.
			XVariableTable vt =
				(XVariableTable) _xd.getDefPool().getVariableTable();
			for (int i = 0; i < vt.size(); i++) {
				//set DefNull(type) to all not initialized global variables.
				XVariable xv = vt.getXVariable(i);
				if (xv == null) {
					continue;
				}
				if (_globalVariables[i] == null) {
					_globalVariables[i] = DefNull.genNullValue(xv.getType());
				}
			}
			_initialized2 = true; //set initialized
			if (_init >= 0) { // initFrame code not yet called.
				exec(_init, null); //call initFrame code
			}
		}
	}
	private void putError(final ChkNode chkNode, final long id) {
		putReport(chkNode, Report.error(id));
	}
	private void putError(final ChkNode chkNode,final long id,final String mod){
		putReport(chkNode, Report.error(id, mod));
	}
	private void putReport(final ChkNode chkNode, final Report rep) {
		updateReport(rep, chkNode);
		_reporter.putReport(rep);
	}

	/** Execute script code starting from given address.
	 * @param start The index of starting code address.
	 * @param chkNode The actual ChkElement object.
	 * @return The XDValue return object or null.
	 */
	final XDValue exec(final int start, final ChkElement chkNode) {
		_callList = null; // list of call objects
		_catchItem = null; // catch item
		int pc = start; //program counter
		int sp = -1; //stack index (pointer)
		int step = XDDebug.NOSTEP;
		XDValue item; //actual instruction
		while (true) {
			int code;
			try {
			if (_debug && (_debugger.hasStopAddr(pc) || step!=XDDebug.NOSTEP)) {
				step = _debugger.debug(chkNode, _code, pc, sp, _stack,
					_localVariables, _debugInfo, _callList, step);
				if (step == XDDebug.KILL) {
					throw new SError(XDEF.XDEF906); //X-definition canceled
				}
			}
			switch (code =(item = _code[pc++]).getCode()) {
				case LD_CONST:  //load clone of value (constant)
					_stack[++sp] = item.cloneItem();
					continue;
				case LD_CONST_I: //load copy of value (constant)
					_stack[++sp] = item.cloneItem();
					continue;
				case LD_CODE:
					_stack[++sp] = _code[item.getParam()].cloneItem();
					continue;
				case LD_TRUE_AND_SKIP:
					// Push true on the top of stack and skip next item.
					_stack[++sp] = new DefBoolean(true);
					pc++;
					continue;
				case LD_FALSE_AND_SKIP:
					// Push false on the top of stack and skip next item.
					_stack[++sp] = new DefBoolean(false);
					pc++;
					continue;
				case LD_LOCAL:{ // load local variable
					XDValue v = _localVariables[item.getParam()];
					_stack[++sp] = v != null ? v : new DefNull();
					continue;
				}
				case LD_XMODEL: { // load xmodel variable
					XDValue v = chkNode.loadModelVariable(item.stringValue());
					_stack[++sp] = v != null ? v : new DefNull();
					continue;
				}
				case LD_GLOBAL: { // load global variable
					XDValue val = _globalVariables[item.getParam()];
					if (val == null) {
						XVariable xv = ((XVariableTable)_xd.getDefPool().
							getVariableTable()).getXVariable(item.getParam());
						if (xv != null && xv.isExternal()) {
							//Null value of &{0}
							putError(chkNode, XDEF.XDEF573, "external " +
								CompileBase.getTypeName(xv.getType())
								+ " " + xv.getName());
						}
						_stack[++sp] = new DefNull();
					} else {
						_stack[++sp] = val;
					}
					continue;
				}
				case ST_LOCAL: //store local variable
					_localVariables[item.getParam()] = _stack[sp--];
					continue;
				case ST_XMODEL: {//store xmodel variable
					chkNode.storeModelVariable(item.stringValue(),_stack[sp--]);
					continue;
				}
				case ST_GLOBAL: //store global variable
					_globalVariables[item.getParam()] = _stack[sp--];
					continue;
////////////////////////////////////////////////////////////////////////////////
				case NO_OP: //No operation
					continue;
////////////////////////////////////////////////////////////////////////////////
//stack operations
////////////////////////////////////////////////////////////////////////////////
				case STACK_DUP: //Duplicate top of stack
					sp++;
					_stack[sp] = _stack[sp - 1];
					continue;
				case POP_OP:
					_stack[sp--] = null;
					continue;
				case STACK_SWAP: {
					XDValue x = _stack[sp];
					_stack[sp] = _stack[sp - 1];
					_stack[sp - 1] = x;
					continue;
				}
////////////////////////////////////////////////////////////////////////////////
//unary operators
////////////////////////////////////////////////////////////////////////////////
				case NEG_I:
					_stack[sp] = new DefLong(- _stack[sp].longValue());
					continue;
				case NEG_R:
					_stack[sp] = new DefDouble(- _stack[sp].doubleValue());
					continue;
				case NOT_B:
					_stack[sp] = new DefBoolean(!_stack[sp].booleanValue());
					continue;
				case NEG_BINARY:
					_stack[sp] = new DefLong(~ _stack[sp].longValue());
					continue;
				case INC_I: //Increase integer by one
					_stack[sp] = new DefLong(_stack[sp].longValue() + 1);
					continue;
				case INC_R: //Decrease real number by one
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() + 1);
					continue;
				case DEC_I: //Decrease integer by one
					_stack[sp] = new DefLong(_stack[sp].longValue() - 1);
					continue;
				case DEC_R: //Decrease real by one
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() - 1);
					continue;
////////////////////////////////////////////////////////////////////////////////
//conversions
////////////////////////////////////////////////////////////////////////////////
				case TO_DECIMAL_X: {
					int i = item.getParam();
					_stack[sp - i] =
						new DefDecimal(_stack[sp - i].decimalValue());
					continue;
				}
				case TO_FLOAT:
					_stack[sp] = new DefDouble(_stack[sp].doubleValue());
					continue;
				case TO_FLOAT_X: {
					int i = item.getParam();
					_stack[sp - i] = new DefDouble(_stack[sp - i].doubleValue());
					continue;
				}
				case NULL_OR_TO_STRING:
					if (_stack[sp] == null || _stack[sp].isNull()) {
						_stack[sp] = new DefString();
						continue;
					}
				case TO_STRING:
					if (_stack[sp] == null) {
						_stack[sp] = new DefString(_stack[sp].toString());
					} else if (_stack[sp].getItemId() != XD_STRING) {
						_stack[sp] = new DefString(_stack[sp].toString());
					}
					continue;
				case TO_STRING_X:
					_stack[sp - item.getParam()] =
						new DefString(_stack[sp - item.getParam()].toString());
					continue;
				case TO_MILLIS:
					_stack[sp] = new DefLong(
						_stack[sp].datetimeValue().getTimeInMillis());
					continue;
				case TO_MILLIS_X: {
					long millis = _stack[sp - item.getParam()]
						.datetimeValue().getTimeInMillis();
					_stack[sp - item.getParam()] = new DefLong(millis);
					continue;
				}
				case TO_BOOLEAN:
					_stack[sp] = new DefBoolean(_stack[sp].booleanValue());
					continue;
				case STACK_TO_CONTEXT: { // create context from stack values
					int n = item.getParam(); // number of stack items
					sp = sp - n + 1;
					_stack[sp] = new DefContainer(_stack, sp, sp + n - 1);
					for (int i = 1; i < n; i++) { //clear used stack items
						_stack[sp + i] = null;
					}
					continue;
				}
				case CREATE_NAMEDVALUE: // create named value
					_stack[sp] = new DefNamedValue(
						((CodeS1)item).stringValue(), _stack[sp]);
					continue;
////////////////////////////////////////////////////////////////////////////////
//binary operators
////////////////////////////////////////////////////////////////////////////////
				case AND_B:
					sp--;
					_stack[sp] = new DefBoolean(_stack[sp].booleanValue() &
						_stack[sp + 1].booleanValue());
					continue;
				case MUL_I:
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() *
						_stack[sp + 1].longValue());
					continue;
				case MUL_R:
					sp--;
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() *
						_stack[sp + 1].doubleValue());
					continue;
				case DIV_I:
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() /
						_stack[sp + 1].longValue());
					continue;
				case DIV_R:
					sp--;
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() /
						_stack[sp + 1].doubleValue());
					continue;
				case ADD_I:
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() +
						_stack[sp + 1].longValue());
					continue;
				case ADD_R:
					sp--;
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() +
						_stack[sp + 1].doubleValue());
					continue;
				case ADD_S: //string concatenation
					sp--;
					_stack[sp] = new DefString(_stack[sp].toString() +
						_stack[sp + 1].toString());
					continue;
				case SUB_I:
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() -
						_stack[sp + 1].longValue());
					continue;
				case SUB_R:
					sp--;
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() -
						_stack[sp + 1].doubleValue());
					continue;
				case OR_B:
					sp--;
					_stack[sp] = new DefBoolean(_stack[sp].booleanValue() |
						_stack[sp + 1].booleanValue());
					continue;
				case XOR_B:
					sp--;
					_stack[sp] = new DefBoolean(_stack[sp].booleanValue() ^
						_stack[sp + 1].booleanValue());
					continue;
				case MOD_I:
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() %
						_stack[sp + 1].longValue());
					continue;
				case MOD_R:
					sp--;
					_stack[sp] = new DefDouble(_stack[sp].doubleValue() %
						_stack[sp + 1].doubleValue());
					continue;
				case LSHIFT_I:/** Left bit shift. */
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() <<
						_stack[sp + 1].intValue());
					continue;
				case RSHIFT_I:/** Right bit shift. */
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() >>
						_stack[sp + 1].intValue());
					continue;
				case RRSHIFT_I:/** Right bit shift unsigned.*/
					sp--;
					_stack[sp] = new DefLong(_stack[sp].longValue() >>>
						_stack[sp + 1].intValue());
					continue;
////////////////////////////////////////////////////////////////////////////////
//comparings
////////////////////////////////////////////////////////////////////////////////
				case CMPEQ:
					sp--;
					_stack[sp] =
						new DefBoolean((_stack[sp]).equals(_stack[sp + 1]));
					continue;
				case CMPNE:
					sp--;
					_stack[sp] = new DefBoolean(
						!(_stack[sp]).equals(_stack[sp + 1]));
					continue;
				case CMPLE:
					sp--;
					_stack[sp] = new DefBoolean(
						(_stack[sp]).compareTo(_stack[sp + 1]) <= 0);
					continue;
				case CMPGE:
					sp--;
					_stack[sp] = new DefBoolean(
						(_stack[sp]).compareTo(_stack[sp + 1]) >= 0);
					continue;
				case CMPLT:
					sp--;
					_stack[sp] = new DefBoolean(
						(_stack[sp]).compareTo(_stack[sp + 1]) < 0);
					continue;
				case CMPGT:
					sp--;
					_stack[sp] = new DefBoolean(
						(_stack[sp]).compareTo(_stack[sp + 1]) > 0);
					continue;
////////////////////////////////////////////////////////////////////////////////
// conditional jumps
////////////////////////////////////////////////////////////////////////////////
				case JMPEQ:
					if (_stack[sp - 1].equals(_stack[sp])) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPNE:
					if (!_stack[sp - 1].equals(_stack[sp])) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPLE:
					if (_stack[sp - 1].compareTo(_stack[sp]) <= 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPGE:
					if (_stack[sp - 1].compareTo(_stack[sp]) >= 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPLT:
					if (_stack[sp - 1].compareTo(_stack[sp]) < 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPGT:
					if (_stack[sp - 1].compareTo(_stack[sp]) > 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
////////////////////////////////////////////////////////////////////////////////
// jumps and program controls
////////////////////////////////////////////////////////////////////////////////
				case STOP_OP: {
					XDValue result = sp == -1 ? null : _stack[sp];
					Arrays.fill(_stack, null); // clear stack
					return result;
				}
				case JMP_OP:
					pc = item.getParam();
					continue;
				case JMPF_OP:
					if (!_stack[sp--].booleanValue()) {
						pc = item.getParam();
					}
					continue;
				case JMPT_OP:
					if (_stack[sp--].booleanValue()) {
						pc = item.getParam();
					}
					continue;
				case CALL_OP:
					_callList = new CallItem(pc, _callList, step);
					pc = item.getParam();
					if (step != XDDebug.NOSTEP) {
						step--;
					}
					continue;
				case RET_OP:
					if (_callList == null) {
						Arrays.fill(_stack, null); // clear stack
						return null;
					}
					pc = _callList._returnAddr;
					_localVariables = _callList._parentLocalVariables;
					step = _callList._step;
					_callList = _callList._parent;
					continue;
				case RETV_OP:
					if (_callList == null) {
						XDValue result = _stack[sp];
						Arrays.fill(_stack, null); // clear stack
						return result;
					}
					pc = _callList._returnAddr;
					_localVariables = _callList._parentLocalVariables;
					step = _callList._step;
					_callList = _callList._parent;
					continue;
				case SWITCH_I: // switch(int)
					pc = ((CodeSWTableInt) item).getTabAddr(
						_stack[sp--].longValue());
					continue;
				case SWITCH_S: // switch(String)
					pc = ((CodeSWTableStr) item).getTabAddr(
						_stack[sp--].stringValue());
					continue;
				case INIT_NOPARAMS_OP: //init method - no parameters
					_localVariables =
						_callList.init(item.getParam(), _localVariables);
					continue;
				case INIT_PARAMS_OP: {//init method - parameters
					_localVariables = //item.getParam() == local variables size
						_callList.init(item.getParam(), _localVariables);
					int numpars = item.intValue();// number of parameters
					sp -= numpars;
					System.arraycopy(_stack, sp+1, _localVariables, 0, numpars);
					continue;
				}
////////////////////////////////////////////////////////////////////////////////
// exceptions try/catch clause
////////////////////////////////////////////////////////////////////////////////
				case SET_CATCH_EXCEPTION:
					_catchItem = new CatchItem(item.getParam(),
						_localVariables, _catchItem);
					continue;
				case RELEASE_CATCH_EXCEPTION:
					_catchItem = _catchItem.release();
					continue;
				case THROW_EXCEPTION: {
					if (_catchItem != null) {
						pc = _catchItem.getCatchAddr();
						_stack[0] = _stack[sp];
						sp = 0;
						_localVariables = _catchItem.getVariables();
						_catchItem = _catchItem.getPrevItem();
						continue;
					}
					//X-definition script exception, PC=&{0}&{1}{; }
					Report rep = Report.error(XDEF.XDEF905, pc - 1,
						((XDException) _stack[sp--]).toString());
					updateReport(rep, chkNode);
					throw new XXException(rep);
				}
////////////////////////////////////////////////////////////////////////////////
// get values
////////////////////////////////////////////////////////////////////////////////
				case GET_TEXTVALUE:
					_stack[++sp] = new DefString(chkNode.getTextValue());
					continue;
				case ELEMENT_GETTEXT: {
					Element el = (item.getParam() == 1) ?
						_stack[sp--].getElement() :
						chkNode != null ? chkNode.getElement() : null;
					String s = el == null ? null : KXmlUtils.getTextValue(el);
					_stack[++sp] = new DefString(s);
					continue;
				}
				case GET_ELEMENT:
					_stack[++sp] = new DefElement(chkNode.getElement());
					continue;
				case GET_ROOTELEMENT: {
					Element el = item.getParam() == 0 ?
						chkNode.getElemValue() : _stack[sp--].getElement();
					if (el != null) {
						el = el.getOwnerDocument().getDocumentElement();
					}
					_stack[++sp] = new DefElement(el);
					continue;
				}
////////////////////////////////////////////////////////////////////////////////
// compile
////////////////////////////////////////////////////////////////////////////////
				case COMPILE_BNF: { //Compile BNF grammar
					DefBNFGrammar x = (DefBNFGrammar) item;
					int extndx = x.getParam(); //extension
					String source = x.stringValue();
					try {
						x = new DefBNFGrammar(extndx == -1 ? null :
							(DefBNFGrammar) _globalVariables[extndx],
							extndx, new SBuffer(source), null);
					} catch (SRuntimeException ex) {
						putReport(chkNode, ex.getReport());
					}
					x.setCode(LD_CONST_I);
					_stack[++sp] = _code[pc - 1] = x;
					continue;
				}
				case COMPILE_XPATH: { //load value (constant)
					DefXPathExpr xp = (DefXPathExpr) item;
					//we MUST recompile this with actual resolvers!!!
					xp = new DefXPathExpr(xp.sourceValue(),
						chkNode.getXXNamespaceContext(),
						_functionResolver, _variableResolver);
					xp.setCode(LD_CONST_I); //However, we do it just first time!
					_code[pc - 1] = _stack[++sp] = _code[pc-1] = xp;
					continue;
				}
				case GET_XPATH: {
					Node node;
					if (item.getParam() == 1) {
						if (chkNode.getItemId() != XX_ELEMENT) {
							if ((node = chkNode._node) == null) {
								node = chkNode.getElement();
							}
						} else {
							node = chkNode.getElement();
						}
					} else {// params == 2
						if (_stack[sp].getItemId() == XD_ELEMENT) {
							node = _stack[sp--].getElement();
						} else {
							XDContainer dc = ((XDContainer) _stack[sp--]);
							node = (dc.getXDItemsNumber() > 0 &&
								dc.getXDItem(0).getItemId() == XD_ELEMENT)
								? dc.getXDElement(0) : null;
						}
					}
					try {
						DefXPathExpr x;
						if (_stack[sp].getItemId() == XD_XPATH) {
							x = ((DefXPathExpr) _stack[sp]);
						} else {
							x = new DefXPathExpr(_stack[sp].toString(),
								chkNode.getXXNamespaceContext(),
								_functionResolver,
								_variableResolver);
						}
						_stack[sp] = x.exec(node);
					} catch (SRuntimeException ex) {
						putReport(chkNode, ex.getReport());
						_stack[sp] = new DefContainer();
					}
					continue;
				}
				case GET_XPATH_FROM_SOURCE: { // optimized XPath expression
					Element e;
					if (item.getParam() == 2) {
						e = _stack[--sp].getElement();
						_stack[sp] = _stack[sp + 1];
						_stack[sp + 1] = null;
					} else {
						Object obj = chkNode.getCreateContext();
						e = (obj != null && (obj instanceof Element))
							? (Element) obj : chkNode.getElemValue();
					}
					if (item.getParam() == 0) {
						if (e == null) {
							_stack[sp++] = new DefContainer();
							continue;
						}
						switch (chkNode.getItemId()) {
							case XX_ATTR: {
								int ndx = chkNode._xPos.lastIndexOf("/@");
								String s = null;
								if (ndx > 0) {
									Node n = e.getAttributeNode(
										chkNode._xPos.substring(ndx + 1));
									s = n == null ? null : n.getNodeValue();
								}
								_stack[++sp] = new DefString(s);
								continue;
							}
							case XX_TEXT:
								_stack[++sp] =
									 new DefString(KXmlUtils.getTextValue(e));
								continue;
							default: {
								String name= chkNode.getXXElement().getXXName();
								String uri= chkNode.getXXElement().getXXNSURI();
								NodeList nl;
								if (uri == null || uri.length() == 0) {
									int ndx = name.indexOf(':');
									nl = KXmlUtils.getChildElementsNS(e, uri,
										ndx < 0 ? name : name.substring(ndx+1));
								} else {
									nl= KXmlUtils.getChildElementsNS(e,"",name);
								}
								DefContainer c = new DefContainer(nl);
								if (nl.getLength() == 0) {
									nl = KXmlUtils.getChildElements(e);
									if (nl.getLength() != 0) {
										c.addXDItem(
											KXmlUtils.firstElementChild(e));
									}
								}
								_stack[++sp] =  c;
								continue;
							}
						}
					} else {
						try {
							DefXPathExpr x;
							if (_stack[sp].getItemId() == XD_XPATH) {
								x = (DefXPathExpr) _stack[sp];
								x.setNamespaceContext(
									chkNode.getXXNamespaceContext());
								x.setFunctionResolver(_functionResolver);
								x.setVariableResolver(_variableResolver);
							} else {
								x = new DefXPathExpr(_stack[sp].toString(),
									chkNode.getXXNamespaceContext(),
									_functionResolver,
									_variableResolver);
								if (_code[pc-2].equals(_stack[sp])) {
									x.setCode(LD_CONST_I);
									_code[pc-2] = x;
								}
							}
							_stack[sp] = x.exec(e);
						} catch (SRuntimeException ex) {
							putReport(chkNode, ex.getReport());
							_stack[sp] = new DefContainer();
						}
					}
					continue;
				}
				case GETATTR_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkNode.getCreateContext();
						el = obj != null && (obj instanceof Element)
							? (Element) obj : chkNode.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					Node node;
					if ((node = el) != null) {
						String s;
						if ((s = item.stringValue()).charAt(0) == '{') {
							// namespace
							int ndx = s.lastIndexOf('}');
							node = el.getAttributeNodeNS(s.substring(1, ndx),
								s.substring(ndx+1));
						} else { // no namespace
							node = el.getAttributeNode(s);
						}
					}
					_stack[++sp] = new DefContainer(node);
					continue;
				}
				case GETELEM_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkNode.getCreateContext();
						el = obj != null && (obj instanceof Element) ?
							(Element) obj : chkNode.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					Node node;
					if ((node = el) != null) {
						String s;
						int ndx = (s = item.stringValue()).lastIndexOf('}');
						if (ndx < 0) { // no namespaceURI
							node = el.getNodeName().equals(s)
								&& el.getNamespaceURI() == null ? el : null;
						} else { // namespaceURI
							node = el.getLocalName().equals(
								s.substring(ndx + 1)) &&
								s.substring(1, ndx).equals(el.getNamespaceURI())
								? el : null;
						}
					}
					_stack[++sp] = new DefContainer(node);
					continue;
				}
				case GETELEMS_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkNode.getCreateContext();
						el = obj != null && (obj instanceof Element) ?
							(Element) obj : chkNode.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					if (el == null) {
						_stack[++sp] = new DefContainer();
					} else {
						String s;
						int ndx = (s = item.stringValue()).lastIndexOf('}');
						NodeList nl = ndx < 0
							? KXmlUtils.getChildElementsNS(el,"",s)
							: KXmlUtils.getChildElementsNS(el,
								s.substring(1, ndx), s.substring(ndx + 1));
						_stack[++sp] = new DefContainer(nl);
					}
					continue;
				}
				case COMPILE_REGEX: //string to regex
					_stack[sp] = new DefRegex(_stack[sp].toString());
					continue;
				case CONTAINS: {
					String s = _stack[sp--].stringValue();
					String t = _stack[sp].stringValue();
					_stack[sp] = (s == null || t == null)
						? new DefBoolean(false)
						: new DefBoolean(s.indexOf(t) >= 0);
					continue;
				}
				case CONTAINSI: {
					String s = _stack[sp--].stringValue();
					String t = _stack[sp].stringValue();
					_stack[sp] = (s == null || t == null)
						? new DefBoolean(false)
						: new DefBoolean(
							s.toLowerCase().indexOf(t.toLowerCase()) >= 0);
					continue;
				}
				case IS_NUM: {
					boolean result = true;
					String s = _stack[sp].toString();
					for (int i = 0; i < s.length(); i++) {
						if (!Character.isDigit(s.charAt(i))) {
							result = false;
							break;
						}
					}
					_stack[sp] = new DefBoolean(result);
					continue;
				}
				case IS_INT:
					try {
						String s = _stack[sp].toString();
						if (s.startsWith("+")) {
							Long.parseLong(s.substring(1));
						} else {
							Long.parseLong(s);
						}
						_stack[sp] = new DefBoolean(true);
						continue;
					} catch (NumberFormatException ex) {
						_stack[sp] = new DefBoolean(false);
						continue;
					}
				case IS_FLOAT:
					try {
						Double.parseDouble(_stack[sp].toString());
						_stack[sp] = new DefBoolean(true);
						continue;
					} catch (Exception ex) {
						_stack[sp] = new DefBoolean(false);
						continue;
					}
				case IS_DATETIME:
					if (item.getParam() == 1) {
						_textParser.setSourceBuffer(_stack[sp].stringValue());
						int i;
						_stack[sp] = new DefBoolean(
							_textParser.isISO8601Datetime()&&
							_textParser.eos() &&
							_textParser.testParsedDatetime() &&
							((i=_textParser.getParsedSDatetime().getYear()) ==
								Integer.MIN_VALUE || i > 1800 && i <= 3000));
					} else {
						sp--;
						_textParser.setSourceBuffer(_stack[sp].stringValue());
						int i;
						_stack[sp] = new DefBoolean(_textParser.isDatetime(
							_stack[sp+1].stringValue()) &&
							_textParser.eos() &&
							_textParser.testParsedDatetime() &&
							((i=_textParser.getParsedSDatetime().getYear()) ==
								Integer.MIN_VALUE || i > 1800 && i <= 3000));
					}
					continue;
				case EQUALSI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? false : t.equalsIgnoreCase(s));
					continue;
				}
				case STARTSWITH: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? false : t.startsWith(s));
					continue;
				}
				case STARTSWITHI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null || s.length() > t.length() ? false
						: s.equalsIgnoreCase(t.substring(0, s.length())));
					continue;
				}
				case ENDSWITH: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? false : t.endsWith(s));
					continue;
				}
				case ENDSWITHI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null || s.length() > t.length() ? false
						: s.equalsIgnoreCase(
							t.substring(t.length() - s.length())));
					continue;
				}
				case CHK_GT: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? false : t.compareTo(s) > 0);
					continue;
				}
				case CHK_LT: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? true : t.compareTo(s) < 0);
					continue;
				}
				case CHK_GE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? false : t.compareTo(s) >= 0);
					continue;
				}
				case CHK_LE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? true : t.compareTo(s) <= 0);
					continue;
				}
				case CHK_NE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? s != null : !t.equals(s));
					continue;
				}
				case CHK_NEI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(
						t == null ? s != null : !s.equalsIgnoreCase(t));
					continue;
				}
				case GET_USEROBJECT:
					sp++;
					_stack[sp] = new DefObject(chkNode.getUserObject());
					continue;
				case SET_USEROBJECT:
					chkNode.setUserObject(_stack[sp].getObject());
					_stack[sp--] = null;
					continue;
				case UNIQUESET_NEWINSTANCE: {
					CodeXD x = (CodeXD) item;
					CodeUniqueset u = (CodeUniqueset) x.getParam2().cloneItem();
					_stack[++sp] = new CodeUniqueset(u.getParsedItems(),
						u.getVarNames(), u.getName());
					continue;
				}
				case UNIQUESET_M_NEWKEY: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp--];
					for (CodeUniqueset.ParseItem i : u.getParsedItems()) {
						i.setParsedObject(null);
					}
					continue;
				}
				case UNIQUESET_KEY_LOAD:
					((CodeUniqueset) _stack[sp]).setKeyIndex(item.getParam());
					continue;
				case UNIQUESET_ID:
				case UNIQUESET_SET:
				case UNIQUESET_IDREF:
				case UNIQUESET_CHKID:
				case UNIQUESET_M_ID:
				case UNIQUESET_M_SET:
				case UNIQUESET_M_IDREF:
				case UNIQUESET_M_CHKID:
				case UNIQUESET_KEY_ID:
				case UNIQUESET_KEY_SET:
				case UNIQUESET_KEY_IDREF:
				case UNIQUESET_KEY_CHKID:
				case UNIQUESET_KEY_SETKEY: {
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					if (code != UNIQUESET_ID && code != UNIQUESET_IDREF
						&& code != UNIQUESET_CHKID && code != UNIQUESET_SET) {
						dt.setKeyIndex(item.getParam());
					}
					if (code != UNIQUESET_M_CHKID && code != UNIQUESET_M_IDREF
						&& code != UNIQUESET_M_SET && code != UNIQUESET_M_ID) {
						CodeUniqueset assumed = execUniqueParser(dt,sp,chkNode);
						_stack[sp] = chkNode._parseResult;
						if (code == UNIQUESET_KEY_SETKEY ||
							chkNode._parseResult.errors()) {
							continue; // just set key
						}
						if (assumed != null) {
							execUniqueOperation(assumed, chkNode, code);
						}
					}
					execUniqueOperation(dt, chkNode, code);
					continue;
				}
				case UNIQUESET_M_SIZE:
					_stack[sp] = new DefLong(
						((CodeUniqueset) _stack[sp]).size());
					continue;
				case UNIQUESET_M_TOCONTAINER:
					_stack[sp]=((CodeUniqueset) _stack[sp]).getUniqueSetItems();
					continue;
				case UNIQUESET_GET_ACTUAL_KEY:
					_stack[sp]=((CodeUniqueset)_stack[sp]).getActualKey();
					if (_stack[sp] == null || _stack[sp].isNull()) {
						//The key is not in the uniqueSet
						Report rep = Report.error(XDEF.XDEF538, pc - 1,
							((XDException) _stack[sp--]).toString());
						updateReport(rep, chkNode);
						_stack[sp] = DefNull.NULL_VALUE;
					}
					continue;
				case UNIQUESET_KEY_RESET: {
					XDUniqueSetKey usk = (XDUniqueSetKey)_stack[sp--];
					if (usk == null || usk.isNull() || !usk.resetKey()) {
						Report rep = Report.error(XDEF.XDEF540, pc - 1,
							((XDException) _stack[sp--]).toString());
						updateReport(rep, chkNode);
					}
					continue;
				}
				case UNIQUESET_IDREFS:
				case UNIQUESET_CHKIDS: {
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					dt.setKeyIndex(item.getParam());
					String s = chkNode.getTextValue();
					StringTokenizer st = new StringTokenizer(s = s.trim());
					DefContainer val = new DefContainer();
					ArrayReporter reporter = new ArrayReporter();
					while (st.hasMoreTokens()) {
						String t = st.nextToken();
						chkNode.setTextValue(t);
						execUniqueParser(dt, sp, chkNode);
						XDValue v = chkNode._parseResult;
						val.addXDItem(v);
						if (chkNode._parseResult.errors()) {
							reporter.addAll(chkNode._parseResult.getReporter());
						} else {
							ArrayReporter list = dt.chkId();
							if (list != null) {
								//Unique value "&{0}" was not set
								Report rep = Report.error(XDEF.XDEF522,
									(dt.getName() != null ?
										dt.getName() + " " : "")+v);
								updateReport(rep, chkNode);
								if (code == UNIQUESET_IDREFS) {
									list.putReport(rep);
								} else {
									reporter.putReport(rep);
								}
							}
						}
					}
					chkNode.setTextValue(s);
					DefParseResult p = new DefParseResult(s);
					p.setParsedValue(val);
					p.addReports(reporter);
					_stack[sp] = chkNode._parseResult = p;
					continue;
				}
				case UNIQUESET_KEY_NEWKEY: {
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					_stack[sp--] = null;
					CodeUniqueset.ParseItem[] o = dt.getParsedItems();
					o[item.getParam()].setParsedObject(null);
					continue;
				}
				case UNIQUESET_CLOSE:
					/** Report unresolved Id references and clear list. */
					_stack[sp] = new DefBoolean(
						((CodeUniqueset) _stack[sp]).checkAndClear(_reporter));
					continue;
				case UNIQUESET_CHEKUNREF: {
					CodeUniqueset x = (CodeUniqueset) _stack[sp--];
					x.setMarker(chkNode);
					chkNode._markedUniqueSets.add(x);
					continue;
				}
				case UNIQUESET_SETVALUEX: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp--];
					String s = ((CodeS1)item).stringValue();
					if (!u.setNamedValue(s, _stack[sp--])) {
						//The uniqueSet item not exists, value "{0}" was not set
						putError(chkNode, XDEF.XDEF537, s);
					}
					continue;
				}
				case UNIQUESET_GETVALUEX: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp];
					_stack[sp] = u.getNamedValue(((CodeS1)item).stringValue());
					continue;
				}
				case DEFAULT_ERROR: //DEFAULT_ERROR_CODE puts message
					putError(chkNode, XDEF.XDEF515);//Value error
					_stack[++sp] = new DefBoolean(false); //returns always false
					continue;
				case ATTR_EXIST:
					if (chkNode.getElemValue() == null) {
						_stack[++sp] = new DefBoolean(false);
					} else {
						String s = item.stringValue();
						if (s.charAt(0) == '{'){
							int i = s.lastIndexOf('}');
							_stack[++sp] = new DefBoolean(
								chkNode.getElemValue().hasAttributeNS(
									s.substring(1, i), s.substring(i + 1)));
						} else {
							_stack[++sp] = new DefBoolean(
								chkNode.getElemValue().hasAttribute(s));
						}
					}
					continue;
				case ATTR_REF:
					if (chkNode.getElemValue() == null) {
						_stack[++sp] = DefNull.genNullValue(XD_STRING);
					} else {
						String s;
						Node n;
						if ((s = item.stringValue()).charAt(0) == '{'){
							int i = s.lastIndexOf('}');
							n = chkNode.getElemValue().getAttributeNodeNS(
								s.substring(1, i), s.substring(i + 1));
						} else {
							n = chkNode.getElemValue().getAttributeNode(s);
						}
						_stack[++sp] = n == null ?
							DefNull.genNullValue(XD_STRING) :
							new DefString(n.getNodeValue());
					}
					continue;
////////////////////////////////////////////////////////////////////////////////
//other methods
////////////////////////////////////////////////////////////////////////////////
				case OUT_STREAM:
				case OUTLN_STREAM: {
					XDOutput out = (XDOutput) _globalVariables[0];
					if (item.getParam() == 1) {
						if (out != null) {
							out.writeString(_stack[sp--].toString());
							if (code == OUTLN_STREAM){
								out.writeString("\n");
							}
						} else {
							sp--;
						}
					} else if (out != null) {
						out.writeString("\n");
					}
					continue;
				}
				case OUT1_STREAM:
				case OUTLN1_STREAM: {
					if (item.getParam() == 2) {
						XDOutput out = (XDOutput) _stack[sp - 1];
						out.writeString(_stack[sp].toString());
						if (code == OUTLN1_STREAM) {
							out.writeString("\n");
						}
						sp -= 2;
					} else {
						((XDOutput)_stack[sp--]).writeString("\n");
					}
					continue;
				}
				case FORMAT_STRING: {
					int npar = item.getParam();
					XDValue v =  _stack[sp - npar + 1];
					int ndx;
					Locale loc;
					String mask;
					if (v.getItemId() == XD_LOCALE) {
						loc = ((DefLocale) v).getLocale();
						mask = _stack[sp - npar + 2].toString();
						ndx = 1;
					} else {
						loc = Locale.US;
						mask = v.toString();
						ndx = 0;
					}
					Object[] pars = new Object[npar - 1 - ndx];
					for (int i = 0; i < npar - 1 - ndx; i++) {
						v = _stack[sp - npar + 2 + i + ndx];
						if (v == null || v.isNull()) {
							pars[i] = null;
						} else {
							switch (v.getItemId()) {
								case XD_INT:
									pars[i] = v.longValue();
									break;
								case XD_FLOAT:
									pars[i] = v.doubleValue();
									break;
								case XD_DATETIME:
									pars[i] = v.datetimeValue().getCalendar();
									break;
								default:
									pars[i] = v.stringValue();
							}
						}
					}
					_stack[sp = sp - npar + 1] =
						new DefString(String.format(loc, mask, pars));
					continue;
				}
				case PRINTF_STREAM: {
					int npar = item.getParam();
					XDValue v = _stack[sp - npar + 1];
					int ndx;
					XDOutput out;
					if (v.getItemId() != XD_OUTPUT) {
						out = (XDOutput) _globalVariables[0];
						ndx = 1;
					} else {
						out = (XDOutput) _stack[sp - npar + 1];
						ndx = 2;
					}
					Locale loc;
					if (_stack[sp - npar + ndx].getItemId() == XD_LOCALE) {
						loc = ((DefLocale) _stack[sp - npar + ndx]).getLocale();
						ndx++;
					} else {
						loc = Locale.US;
					}
					String mask = _stack[sp - npar + ndx].toString();
					Object[] pars = new Object[npar - ndx];
					for (int i = 0; i < npar - ndx; i++) {
						v = _stack[ndx + i];
						if (v == null || v.isNull()) {
							pars[i] = null;
						} else {
							switch (v.getItemId()) {
								case XD_INT:
									pars[i] = v.longValue();
									break;
								case XD_FLOAT:
									pars[i] = v.doubleValue();
									break;
								case XD_DATETIME:
									pars[i] = v.datetimeValue().getCalendar();
									break;
								default:
									pars[i] = v.stringValue();
							}
						}
					}
					sp -= npar;
					out.writeString(String.format(loc, mask, pars));
					continue;
				}
				case SET_ATTR: {//set attribute
					String ns =
						item.getParam() == 3 ? _stack[sp--].toString() : null;
					String s = _stack[sp--].stringValue();
					Element e;
					if ((e = chkNode.getElemValue()) == null) {
						sp--;
					} else {
						String name = _stack[sp--].toString();
						if (ns == null) {
							if (chkNode._node != null &&
								chkNode._node.getNodeType()==
								Node.ATTRIBUTE_NODE &&
								name.equals(chkNode._node.getNodeName())) {
								chkNode.setTextValue(s);
							} else {
								e.setAttribute(name, s);
							}
						} else {
							if (chkNode._node != null &&
								chkNode._node.getNodeType()==
								Node.ATTRIBUTE_NODE &&
								name.equals(chkNode._node.getLocalName()) &&
								ns.equals(chkNode._node.getNamespaceURI())) {
								chkNode.setTextValue(s);
							} else {
								e.setAttributeNS(ns, name, s);
							}
						}
					}
					continue;
				}
				case HAS_ATTR: {//check attribute exists
					String ns =
						item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e;
					_stack[sp] = (e = chkNode.getElemValue()) == null ?
						new DefBoolean(false) :
						new DefBoolean(ns == null ?
							e.hasAttribute(_stack[sp].stringValue()) :
							e.hasAttributeNS(ns, _stack[sp].stringValue()));
					continue;
				}
				case DEL_ATTR: {//delete attribute
					String ns =
						item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e;
					if ((e = chkNode.getElemValue()) == null) {
						sp--;
					} else {
						String name = _stack[sp--].toString();
						if (ns == null) {
							if (chkNode._node != null &&
								chkNode._node.getNodeType()==
								Node.ATTRIBUTE_NODE &&
								name.equals(chkNode._node.getNodeName())) {
								chkNode.setTextValue(null);
							} else {
								e.removeAttribute(name);
							}
						} else {
							if (chkNode._node != null &&
								chkNode._node.getNodeType()==
								Node.ATTRIBUTE_NODE &&
								name.equals(chkNode._node.getLocalName()) &&
								ns.equals(chkNode._node.getNamespaceURI())) {
								chkNode.setTextValue(null);
							} else {
								e.removeAttributeNS(ns, name);
							}
						}
					}
					continue;
				}
				case GET_OCCURRENCE:
					_stack[++sp] = new DefLong(chkNode.getOccurrence());
					continue;
				case GET_IMPLROPERTY: {
					String s;
					if (item.getParam() == 1) {
						s = _xd.getImplProperty(_stack[sp].toString());
					} else {
						s = _stack[sp--].toString();
						XDefinition xd =
							(XDefinition) _xd.getXDPool().getXMDefinition(
								_stack[sp].toString());
						s = xd == null ? null : xd.getImplProperty(s);
					}
					_stack[sp] = s==null ? new DefString("") : new DefString(s);
					continue;
				}
				case DEBUG_TRACE:
				case DEBUG_PAUSE: {
					if (_debug && _debugger != null) {
						step = _debugger.debug(chkNode, _code,
							pc - 1, // pc is already increased
							sp, _stack, _localVariables,
							_debugInfo, _callList, step);
					}
					sp -= item.getParam();
					continue;
				}
				case PUT_ERROR: //error message to stdErr.
					switch (item.getParam()) {
						case 1:
							if (_stack[sp].getItemId() == XD_REPORT) {
								Report r =
									((DefReport)_stack[sp]).reportValue();
								_reporter.putReport(Report.error(r.getMsgID(),
									r.getText(), r.getModification()));
							} else {
								_reporter.putReport(Report.error(null,
									_stack[sp].stringValue()));
							}
							break;
						case 2:
							putReport(chkNode,
								Report.error(_stack[sp-1].stringValue(),
								_stack[sp].stringValue()));
							sp--;
							break;
						default:
							putReport(chkNode,
								Report.error(_stack[sp-2].stringValue(),
								_stack[sp-1].stringValue(),
								_stack[sp].stringValue()));
							sp -= 2;
					}
					_stack[sp] = new DefBoolean(false);
					continue;
				case PUT_ERROR1: //Put error message to stdErr.
					switch (item.getParam()) {
						case 2:
							((XDOutput) _stack[sp-1]).putReport(
								Report.error(null, _stack[sp].stringValue()));
							sp--;
							break;
						case 3:
							((XDOutput) _stack[sp-2]).putReport(
								Report.error(_stack[sp-1].stringValue(),
									_stack[sp].stringValue()));
							sp -= 2;
							break;
						default:
							((XDOutput)_stack[sp-3]).putReport(
								Report.error(_stack[sp-2].stringValue(),
									_stack[sp-1].stringValue(),
									_stack[sp].stringValue()));
							sp -= 3;
					}
					_stack[sp] = new DefBoolean(false);
					continue;
				case GET_NUMOFERRORS: //Get number of errors.
					_stack[++sp] = new DefLong(
						chkNode.getReporter().getErrorCount()+
						_reporter.getErrorCount() - chkNode._errCount);
					continue;
				case GET_NUMOFERRORWARNINGS:
					_stack[++sp] = new DefLong(
						chkNode.getReporter().getErrorCount() +
						_reporter.getErrorCount() - chkNode._errCount +
						chkNode.getReporter().getWarningCount() +
						_reporter.getWarningCount());
					continue;
				case CLEAR_REPORTS: //clear temp reports
					_reporter.clear();
					continue;
				case SET_TEXT: {//set string value
					XDValue x =_stack[sp--];
					String s = x == null || x.isNull() ? null : x.toString();
					chkNode.setTextValue(s);
					continue;
				}
				case SET_ELEMENT: {//set actual element
					Element el;
					if (_stack[sp].getItemId() == XD_ELEMENT){
						el = ((DefElement) _stack[sp--]).getElement();
					} else {
						XDValue x = _stack[sp--];
						if (x == null || x.isNull()) {
							el = null;
						} else {
							XDContainer c = (XDContainer) x;
							el = (c.getXDItemsNumber() > 0
								&& c.getXDItem(0).getItemId() == XD_ELEMENT)
								? c.getXDItem(0).getElement() : null;
						}
					}
					if (el == null) {
						//Required element is missing in setElement method
						putError(chkNode, XDEF.XDEF529);
						el = chkNode.getDocument().createElementNS(null,
							UNDEF_ID);
					} else {
						if (el.getOwnerDocument() != chkNode.getDocument()) {
							el = (Element) chkNode.getDocument().importNode(
								el, true);
						}
					}
					chkNode.getChkElement().updateElement(el);
					chkNode.setElemValue(el);
					continue;
				}
				case REMOVE_TEXT:
					//Remove actual text node or attribute
					chkNode.setTextValue(null);
					continue;
				case GET_NOW: //set actual date and time
					_stack[++sp] = new DefDate(
						new SDatetime(new GregorianCalendar()));
					continue;
				case GET_QNPREFIX:{ //getQnamePrefix()
					String s = _stack[sp].toString();
					int ndx;
					if ((ndx = s.indexOf(':')) > 0) {
						_stack[sp] = new DefString(s.substring(0, ndx));
					} else {
						_stack[sp] = new DefString("");
					}
					continue;
				}
				case GET_QNLOCALPART:{//getQnameLocalpart
					String s = _stack[sp].toString();
					int ndx;
					if ((ndx = s.indexOf(':')) > 0) {
						_stack[sp] = new DefString(s.substring(ndx + 1));
					} else {
						_stack[sp] = new DefString(s);
					}
					continue;
				}
				case CONTEXT_GETELEMENTS: //getElements(container)
					if (item.getParam() == 2) {
						sp--;
						_stack[sp] = ((XDContainer) _stack[sp]).getXDElements(
							_stack[sp + 1].stringValue());
					} else {
						_stack[sp] = ((XDContainer) _stack[sp]).getXDElements();
					}
					continue;
				case CONTEXT_GETELEMENT_X: { //getElement(container, index)
					int i = item.getParam() == 2 ? _stack[sp--].intValue() : 0;
					Element elem = ((XDContainer) _stack[sp]).getXDElement(i);
					if (elem == null) {
						elem = chkNode._rootChkDocument._doc.createElementNS(
							null, UNDEF_ID);
					}
					_stack[sp] = new DefElement(elem);
					continue;
				}
				case CONTEXT_GETTEXT: //getText(container)
					if (item.getParam() == 2) {
						int i = _stack[sp--].intValue();
						_stack[sp] = new DefString(
							((XDContainer) _stack[sp]).getXDTextItem(i));
					} else {
						_stack[sp] =
							new DefString(((XDContainer) _stack[sp]).getXDText());
					}
					continue;
				case CONTEXT_GETLENGTH: //getLength(container)
					_stack[sp] = new DefLong(
						((XDContainer) _stack[sp]).getXDItemsNumber());
					continue;
				case CONTEXT_SORT: //container.sort()
					switch (item.getParam()) {
						case 2:
							sp--;
							((XDContainer) _stack[sp]).sortXD(
								_stack[sp+1].toString(), true);
							continue;
						case 3:
							sp -= 2;
							((XDContainer) _stack[sp]).sortXD(
								_stack[sp+1].toString(),
								_stack[sp+2].booleanValue());
							continue;
						default:
							((XDContainer) _stack[sp]).sortXD(null, true);
					}
					continue;
				case CONTEXT_ADDITEM: //container.add(obj);
					sp -= 2;
					((XDContainer) _stack[sp + 1]).addXDItem(_stack[sp + 2]);
					continue;
				case CONTEXT_REMOVEITEM: //container.remove(index);
					sp--;
					_stack[sp] = ((XDContainer) _stack[sp]).removeXDItem(
						_stack[sp + 1].intValue());
					continue;
				case CONTEXT_ITEM: { //container.getXDItem(index)
					sp--;
					XDValue x = ((XDContainer) _stack[sp]).getXDItem(
						_stack[sp + 1].intValue());
					_stack[sp] = (x == null) ?  new DefNull() : x;
					continue;
				}
				case CONTEXT_REPLACEITEM: //container.rreplace(index, value)
					sp -= 2;
					_stack[sp] = ((XDContainer) _stack[sp]).replaceXDItem(
						_stack[sp + 1].intValue(),
						_stack[sp + 2]);
					continue;
				case CONTEXT_TO_ELEMENT: {
					String uri = null;
					String name = null;
					if (item.getParam() >= 2) {
						name = _stack[sp--].stringValue();
						if (item.getParam() == 3) {
							uri = _stack[sp--].stringValue();
						}
					}
					XDContainer x = (XDContainer) _stack[sp];
					_stack[sp] = new DefElement(x.toElement(uri, name));
					continue;
				}
				case CONTEXT_ITEMTYPE:{ //container.temType(index)
					int index = _stack[sp--].intValue();
					_stack[sp] = new DefLong(
						((XDContainer)_stack[sp]).getXDItem(index).getItemId());
					continue;
				}
				case GET_RESULTSET_ITEM: {
					String s = _stack[sp--].stringValue();
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkNode);
					}
					_stack[sp] = new DefString(it.itemAsString(s));
					continue;
				}
				case HAS_RESULTSET_ITEM: {
					String s = _stack[sp--].stringValue();
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkNode);
					}
					_stack[sp] = new DefBoolean(it.itemAsString(s) != null);
					continue;
				}
				case HAS_RESULTSET_NEXT: {
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkNode);
					}
					_stack[sp] = new DefBoolean(it.lastXDItem() != null);
					continue;
				}
				case RESULTSET_NEXT: {
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkNode);
					}
					_stack[sp] = new DefBoolean(it.nextXDItem(chkNode) != null);
					continue;
				}
				case GET_RESULTSET_COUNT:
					_stack[sp] = new DefLong(
						((XDResultSet) _stack[sp]).getCount());
					continue;
				case GET_XPOS://get actual xpath position
					_stack[++sp] = new DefString(chkNode.getXPos());
					continue;
				case GET_XQUERY: {//execute xquery
					Node node;
					if (item.getParam() == 1) {
						switch (chkNode.getItemId()) {
							case XX_ATTR:
							case XX_TEXT:
								if ((node = chkNode._node) == null) {
									node = chkNode.getElemValue();
								}
								break;
							default:
								node = chkNode.getElemValue();
						}
					} else {// params == 2
						if (_stack[sp].getItemId() == XD_ELEMENT) {
							node = _stack[sp--].getElement();
						} else {
							XDContainer dc = ((XDContainer) _stack[sp--]);
							if (dc.getXDItemsNumber() > 0 &&
								dc.getXDItem(0).getItemId() == XD_ELEMENT){
								node = dc.getXDElement(0);
							} else {
								_stack[sp]= new DefContainer();
								continue;
							}
						}
					}
					try {
						DefXQueryExpr x;
						if (_stack[sp].getItemId() == XD_XQUERY) {
							x = (DefXQueryExpr) _stack[sp];
						} else {
							x = new DefXQueryExpr(_stack[sp].toString());
						}
						_stack[sp] = x != null ? x.exec(node, chkNode)
							: new DefContainer();
					} catch (SRuntimeException ex) {
						chkNode.putReport(ex.getReport());
						_stack[sp] = new DefContainer();
					}
					continue;
				}
				case DB_PREPARESTATEMENT: {
					String query = _stack[sp--].stringValue();
					XDValue dv = _stack[sp];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						throw new SRuntimeException(XDEF.XDEF573, "Service");
					}
					_stack[sp] = ((XDService) dv).prepareStatement(query);
					addToFinalList(chkNode, _stack[sp]);
					continue;
				}
				case GET_DBQUERY: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv = _stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						Report r = Report.error(XDEF.XDEF573, "Service");
						throw new SRuntimeException(updateReport(r, chkNode));
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDResultSet result;
					switch (xtype) {
						case XD_SERVICE: {
							String query = _stack[sp--].stringValue();
							XDService conn = (XDService) dv;
							result = conn.query(query, params);
							break;
						}
						case XD_STATEMENT: {
							XDStatement ds = (XDStatement) dv;
							result = ds.query(params);
							break;
						}
						case XD_STRING: { // ???
							XDStatement ds = (XDStatement) dv;
							result = ds.query(params);
							break;
						}
						default:
							//XQuery expression error
							throw new SRuntimeException(updateReport(
								Report.error(XDEF.XDEF561),chkNode));
					}
					_stack[sp] = result;
					addToFinalList(chkNode, result);
					continue;
				}
				case GET_DBQUERY_ITEM: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						Report r = Report.error(XDEF.XDEF573,
							(dv == null || dv.getItemId() != XD_STATEMENT ?
							"Service" : "Statement"));
						throw new SRuntimeException(updateReport(r, chkNode));
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 3 : 2);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDResultSet result;
					switch (xtype) {
						case XD_SERVICE: {
							String itemName = _stack[sp--].stringValue();
							String query = _stack[sp--].stringValue();
							XDService conn = (XDService) dv;
							result = conn.queryItems(query, itemName, params);
							break;
						}
						case XD_STATEMENT: {
							String itemName = _stack[sp--].stringValue();
							XDStatement ds = (XDStatement) dv;
							result = ds.queryItems(itemName, params);
							break;
						}
						default:
							//XQuery expression error
							throw new SRuntimeException(updateReport(
								Report.error(XDEF.XDEF561), chkNode));

					}
					_stack[sp] = result;
					addToFinalList(chkNode, result);
					continue;
				}
				case HAS_DBITEM: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						Report r = Report.error(XDEF.XDEF573,
							dv == null || dv.getItemId() != XD_STATEMENT ?
							"Service" : "Statement");
						throw new SRuntimeException(updateReport(r, chkNode));
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDResultSet di;
					switch (xtype) {
						case XD_SERVICE: {
							String query = _stack[sp--].stringValue();
							XDService conn = (XDService) dv;
							di = conn.query(query, params);
							break;
						}
						case XD_STATEMENT:
							di = ((XDStatement) dv).query(params);
							break;
						case XD_RESULTSET: {
							di = (XDResultSet) dv;
							break;
						}
						default: //???? this never happens
							//XQuery expression error
							throw new SRuntimeException(updateReport(
								Report.error(XDEF.XDEF561), chkNode));
					}
					_stack[sp] = new DefBoolean(di.nextXDItem(chkNode) != null);
					di.close();
					continue;
				}
				case DB_EXEC: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						Report r = Report.error(XDEF.XDEF573,
							dv == null || dv.getItemId()!=XD_STATEMENT ?
							"Service" : "Statement");
						throw new SRuntimeException(updateReport(r, chkNode));
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDStatement ds;
					switch (xtype) {
						case XD_SERVICE: {
							String query = _stack[sp--].stringValue();
							ds = ((XDService) dv).prepareStatement(query);
							break;
						}
						case XD_STATEMENT:
							ds = (XDStatement) dv;
							break;
						default:
							//Null value of &{0}
							throw new SRuntimeException(updateReport(
								Report.error(XDEF.XDEF573, "Service"),chkNode));
					}
					_stack[sp] = ds.execute(params);
					addToFinalList(chkNode, _stack[sp]);
					continue;
				}
				case DB_CLOSE: {
					XDValue dv =_stack[sp--];
					if (dv != null && !dv.isNull()) {
						switch (dv.getItemId()) {
							case XD_SERVICE:
								((XDService) dv).close();
								continue;
							case XD_STATEMENT:
								((XDStatement) dv).close();
								continue;
							case XD_RESULTSET:
								((XDResultSet) dv).close();
								continue;
						}
					}
					continue;
				}
				case DB_ISCLOSED: {
					XDValue dv =_stack[sp];
					boolean b = true;
					if (dv != null && !dv.isNull()) {
						switch (dv.getItemId()) {
							case XD_SERVICE:
								b = ((XDService) dv).isClosed();
								break;
							case XD_STATEMENT:
								b = ((XDStatement) dv).isClosed();
								break;
							case XD_RESULTSET:
								b = ((XDResultSet) dv).isClosed();
								break;
						}
					}
					_stack[sp] = new DefBoolean(b);
					continue;
				}
				case DB_CLOSESTATEMENT:
					((XDResultSet)_stack[sp--]).closeStatement();
					continue;
				case DB_COMMIT: {
					XDValue dv = _stack[sp--];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						throw new SRuntimeException(updateReport(
							Report.error(XDEF.XDEF573, "Service"), chkNode));
					}
					((XDService) dv).commit();
					continue;
				}
				case DB_ROLLBACK: {
					XDValue dv = _stack[sp--];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						throw new SRuntimeException(updateReport(
							Report.error(XDEF.XDEF573, "Service"), chkNode));
					}
					((XDService) dv).rollback();
					continue;
				}
				case DB_SETPROPERTY: {
					String value = _stack[sp--].stringValue();
					String name = _stack[sp--].stringValue();
					XDValue dv = _stack[sp];
					if (dv == null || dv.isNull()) {
						//Null value of &{0}
						throw new SRuntimeException(updateReport(
							Report.error(XDEF.XDEF573, "Service"), chkNode));
					}
					try {
						((XDService) dv).setProperty(name, value);
						_stack[sp] = new DefBoolean(true);
					} catch (Exception ex) {
						_stack[sp] = new DefBoolean(false);
					}
					continue;
				}
				case COMPOSE_OP: {
					Element oldContext = chkNode._sourceElem;
					if (item.getParam() == 2) {
						Element context = _stack[sp--].getElement();
						if (context != null) {
							chkNode._sourceElem = context;
						}
					}
					String rootName = _stack[sp].toString();
					XMDefinition xdef;
					int i;
					if ((i = rootName.indexOf('#')) >= 0) {
						if (i > 0) {
							xdef = _xd.getXDPool().getXMDefinition(
								rootName.substring(0,i));
							if (xdef == null) {
								//Missing X-definition &{0}
								chkNode.fatal(XDEF.XDEF530, rootName);
								_stack[sp] =
									new DefElement(chkNode.getElemValue());
								chkNode._sourceElem = oldContext;
								continue;
							}
						} else {
							xdef = _xd;
						}
						rootName = rootName.substring(i + 1);
					} else {
						xdef = _xd;
					}
					Map<Integer, CodeUniqueset> idrefTables =
						new LinkedHashMap<Integer, CodeUniqueset>();
					// save and clear all unique
					for (int j = 3; j < _globalVariables.length; j++) {
						XDValue xv;
						if ((xv = _globalVariables[j]) != null &&
							xv.getItemId() == CompileBase.UNIQUESET_VALUE) {
							CodeUniqueset x = (CodeUniqueset)xv;
							idrefTables.put(j, x);
							_globalVariables[j] = new CodeUniqueset(
								x.getParsedItems(),x.getVarNames(),x.getName());
						}
					}
					Element elem = ChkComposer.compose(_reporter,
						(XDefinition) xdef, rootName, chkNode.getChkElement());
					// restore all unique
					for (Integer j : idrefTables.keySet()) {
						CodeUniqueset x = idrefTables.get(j);
						_globalVariables[j] = x;
					}
					if (elem == null) {
						//Required element is missing in setElement method
						chkNode.error(XDEF.XDEF529);
						//we create dumy element
						elem = chkNode.getDocument().createElementNS(
							null, UNDEF_ID);
					} else if (elem.getOwnerDocument() !=
						chkNode.getDocument()) {
						elem = (Element) chkNode.getDocument().importNode(
							elem, true);
					}
					chkNode._sourceElem = oldContext;
					_stack[sp] = new DefElement(elem);
					continue;
				}
				case FROM_ELEMENT: {
					_stack[sp] = new DefContainer(_stack[sp]);
					continue;
				}
				case GET_ITEM: {
					Object obj = chkNode.getCreateContext();
					Element e = (obj != null && (obj instanceof Element)) ?
						(Element) obj : chkNode.getElemValue();
					String t;
					if (e == null) {
						t = null;
					} else {
						String s = _stack[sp].toString();
						if (s.startsWith("@")) {
							s = s.substring(1);
						}
						t = e.getAttribute(s);
						if (t.length() == 0 && !e.hasAttribute(s)) {
							t = null;
						}
					}
					_stack[sp] = new DefString(t);
					continue;
				}
				case GET_ATTR: {//get attribute value
					String ns =
						item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e = chkNode.getElemValue();
					_stack[sp] = new DefString(e == null ? ""
						: ns == null ? e.getAttribute(_stack[sp].toString())
						: e.getAttributeNS(ns, _stack[sp].toString()));
					continue;
				}
				case CREATE_ELEMENT: {
					String ns;
					String name;
					if (item.getParam() == 0) {
						ns = chkNode._xElement.getNSUri();
						name = chkNode._xElement.getName();
					} else {
						ns = item.getParam() == 2 ?
							_stack[sp--].toString() : null;
						name = _stack[sp--].toString();
					}
					_stack[++sp] =
						new DefElement(chkNode._rootChkDocument._doc, ns, name);
					continue;
				}
				case CREATE_ELEMENTS: {
					String ns;
					String name;
					if (item.getParam() == 1) {
						ns = chkNode._xElement.getNSUri();
						name = chkNode._xElement.getName();
					} else {
						ns = item.getParam() == 3 ?
							_stack[sp--].toString() : null;
						name = _stack[sp--].toString();
					}
					int i = _stack[sp].intValue();
					DefElement[] values = new DefElement[i];
					for (int j = 0; j < i; j++) {
						 values[j] = new DefElement(
							chkNode._rootChkDocument._doc, ns, name);
					}
					_stack[sp] = new DefContainer(values);
					continue;
				}
				case PARSE_XML: {
					String s; //name of xdef
					if (item.getParam() == 1 ||
						(s = _stack[sp--].stringValue()) == null) {
						//no xdef, just parse
						Document d = KXmlUtils.parseXml(_stack[sp].toString());
						_stack[sp] = new DefElement(d.getDocumentElement());
						continue;
					}
					XMDefinition xdef;
					int ndx;
					if ((ndx = s.indexOf('#')) >= 0) {
						if (ndx > 0) {
							xdef = _xd.getXDPool().getXMDefinition(
								s.substring(0,ndx));
							if (xdef == null) {
								//Missing X-definition &{0}
								chkNode.fatal(XDEF.XDEF530, s);
								_stack[sp] =
									new DefElement(chkNode.getElemValue());
								continue;
							}
						} else {
							xdef = _xd;
						}
						s = s.substring(ndx + 1);
					} else {
						xdef = _xd;
					}
					//parse element with X-definition
					ChkDocument x = (ChkDocument) ("*".equals(s) ?
						xdef.getXDPool().createXDDocument() :
						xdef.getXDPool().createXDDocument(s));
					//set our global variables to parser!!!
					x._rootChkDocument._scp._initialized1 = true;
					x._rootChkDocument._scp._initialized2 = true;
					x._scp._code = _code;
					x._rootChkDocument._scp._globalVariables = _globalVariables;
					x._rootChkDocument._scp._textParser = _textParser;
					_stack[sp] = new DefElement(x.xparse(_stack[sp].toString(),
						_reporter));
					_globalVariables = x._rootChkDocument._scp._globalVariables;
					continue;
				}
				case GET_COUNTER: {
					int result = -1;
					if (chkNode != null) {
						if (chkNode.getItemId() == XX_ELEMENT) {
							result = chkNode.getOccurrence();
						} else if (chkNode.getItemId() == XX_TEXT) {
							result = chkNode.getRefNum();
						}
					}
					_stack[++sp] = new DefLong(result);
					continue;
				}
				case GET_ELEMENT_NAME:
					_stack[++sp] = new DefString(chkNode._element == null
						? "" : chkNode._element.getNodeName());
					continue;
				case GET_ELEMENT_LOCALNAME: {
					String s;
					if (chkNode._element != null) {
						s = chkNode._element.getLocalName();
						if (s == null) {
							s = chkNode._element.getNodeName();
						}
					} else {
						s = "";
					}
					_stack[++sp] = new DefString(s);
					continue;
				}
				case GET_ATTR_NAME:
					_stack[++sp] = new DefString(chkNode.getItemId() == XX_ATTR
						? chkNode.getNodeName() : null);
					continue;
				case GET_REGEX_RESULT:
					_stack[sp - 1] = ((XDRegex) _stack[sp - 1]).getRegexResult(
						_stack[sp].stringValue());
					sp -= 1;
					continue;
				case MATCHES_REGEX: {
					XDRegexResult rr;
					if (item.getParam() == 2) {
						rr = ((XDRegex) _stack[sp - 1]).getRegexResult(
							_stack[sp].stringValue());
						_stack[sp] = null;
						sp--;
					} else {
						rr = (XDRegexResult) _stack[sp];
					}
					_stack[sp] = new DefBoolean(rr.matches());
					continue;
				}
				case GET_REGEX_GROUP:
					sp--;
					_stack[sp] =
						new DefString(((XDRegexResult) _stack[sp]).group(
							_stack[sp + 1].intValue()));
					continue;
				case GET_REGEX_GROUP_NUM:
					_stack[sp] = new DefLong(
						((XDRegexResult) _stack[sp]).groupCount());
					continue;
				case GET_REGEX_GROUP_START:
					sp--;
					_stack[sp] = new DefLong(
						((XDRegexResult) _stack[sp]).groupStart(
							_stack[sp + 1].intValue()));
					continue;
				case GET_REGEX_GROUP_END:
					sp--;
					_stack[sp] = new DefLong(
						((XDRegexResult) _stack[sp]).groupEnd(
							_stack[sp + 1].intValue()));
					continue;
				case STREAM_READLN: {
					XDInput in;
					if (item.getParam() == 1) {
						in = (XDInput) _stack[sp];
					} else {
						in = (XDInput) _globalVariables[2];
						sp++;
					}
					_stack[sp] = new DefString(in.readString());
					continue;
				}
				case STREAM_EOF: {
					XDInput in;
					if (item.getParam() == 1) {
						in = (XDInput) _stack[sp];
					} else {
						in = (XDInput) _globalVariables[2];
						sp++;
					}
					_stack[sp] = new DefBoolean(in.isOpened());
					continue;
				}
				case GET_MESSAGE:
					_stack[sp] = new DefString(_stack[sp].toString());
					continue;
				case GET_BNFRULE: {
					String s = _stack[sp--].toString();
					DefBNFRule br =  ((DefBNFGrammar) _stack[sp]).getRule(s);
					if (br == null || br.isNull()) {
						//BNF rule '&{0}' not exists
						Report r = Report.error(XDEF.XDEF572, s);
						throw new SRuntimeException(updateReport(r, chkNode));
					}
					_stack[sp] = br;
					continue;
				}
				case BNF_PARSE: {
					String s = item.getParam() == 2
						? chkNode.getTextValue() : _stack[sp--].toString();
					String ruleName = _stack[sp--].stringValue();
					DefBNFRule r =
						((DefBNFGrammar) _stack[sp]).getRule(ruleName);
					if (r.ruleValue() == null) {
						DefParseResult pr = new DefParseResult(s);
						//Script error: BNF rule '&{0}' not exists
						pr.error(XDEF.XDEF567, ruleName);
						_stack[sp] = pr;
					} else {
						_stack[sp] = r.perform(s);
					}
					continue;
				}
				case BNFRULE_PARSE: {
					String s = item.getParam() == 1
						? chkNode.getTextValue() : _stack[sp--].toString();
					_stack[sp] = ((DefBNFRule) _stack[sp]).perform(s);
					continue;
				}
				case PARSE_OP: {
					String s = item.getParam() == 1
						? chkNode.getTextValue() : _stack[sp--].toString();
					XDParseResult result;
					if (_stack[sp]==null || !(_stack[sp] instanceof XDParser)) {
						result = new DefParseResult(s);
						//Value of type "Parser" expected&{0}{, found: }
						result.error(XDEF.XDEF820, _stack[sp]);
					} else {
						result = ((XDParser) _stack[sp]).check(chkNode, s);
					}
					if (result.matches()) {
						if (item.getParam() == 1) {
							chkNode.setTextValue(result.getSourceBuffer());
						}
					}
					if (chkNode != null) {
						chkNode._parseResult = result;
					}
					_stack[sp] = result;
					continue;
				}
				case PARSEANDCHECK: {
					String s = item.getParam() == 1 ?
						chkNode.getTextValue() : _stack[sp--].toString();
					_stack[sp] = new DefBoolean(
						((XDParser) _stack[sp]).check(chkNode, s).matches());
					continue;
				}
				case PARSE_STRING: {
					String s = _stack[sp--].toString();
					XDParseResult result =
						((XDParser) _stack[sp]).check(chkNode, s);
					_stack[sp] = result.matches()
						? result.getParsedValue()
						: DefNull.genNullValue(XD_ANY);
					continue;
				}
				case PARSERESULT_MATCH:
					_stack[sp] =
						new DefBoolean(((XDParseResult)_stack[sp]).matches());
					continue;
				case SET_PARSED_ERROR: {//result.setError()
					String txt, id, modif;
					switch (item.getParam()) {
						case 4:
							modif = _stack[sp--].toString();
							txt = _stack[sp--].toString();
							id = _stack[sp--].toString();
							break;
						case 3:
							txt = _stack[sp--].toString();
							id = _stack[sp--].toString();
							modif = null;
							break;
						default:
							txt = _stack[sp--].toString();
							id = modif = null;
					}
					((DefParseResult) _stack[sp]).error(id, txt, modif);
					continue;
				}
				case GET_PARSED_ERROR: {//result.getError()
					XDParseResult x = (XDParseResult) _stack[sp];
					_stack[sp] = x.matches() ? new DefReport() :
						new DefReport(x.getReporter().getLastErrorReport());
					continue;
				}
				case SET_PARSED_STRING: //result.setSourceString
					((XDParseResult) _stack[sp]).setSourceBuffer(
						_stack[sp--].toString());
					continue;
				case SET_PARSED_VALUE: {//result.setParsedValue
					XDValue v = _stack[sp--];
					XDParseResult pr = item.getParam() == 2
						? (XDParseResult) _stack[sp--] : chkNode._parseResult;
					pr.setParsedValue(v);
					continue;
				}
				case GET_PARSED_VALUE: {//result.setSourceString
					XDParseResult pr = (item.getParam() == 1)
						? (XDParseResult) _stack[sp--]
						: chkNode._parseResult;
					_stack[++sp] = pr.getParsedValue();
					continue;
				}
				case SET_NAMEDVALUE: {
					if (item.getParam() == 2) {
						XDValue v = _stack[sp--];
						XDNamedValue x;
						if (v.getItemId() == XD_CONTAINER) {
							XDContainer c = (XDContainer) v;
							x = c.getXDNamedItemsNumber() == 0
								? new DefNamedValue("", new DefString())
								: c.getXDNamedItem(c.getXDNamedItemName(0));
						} else {
							x = (XDNamedValue) v;
						}
						((XDContainer)_stack[sp--]).setXDNamedItem(x);
					} else {//3
						XDValue val = _stack[sp--];
						String key = _stack[sp--].stringValue();
						((XDContainer)_stack[sp--]).setXDNamedItem(key, val);
					}
					continue;
				}
				case GET_NAMEDVALUE: {//get named item from context
					String name = (item.getParam() == 1) ?
						item.stringValue() : _stack[sp--].toString();
					XDValue v =
						((XDContainer)_stack[sp]).getXDNamedItemValue(name);
					_stack[sp] = v == null ? new DefString() : v;
					continue;
				}
				case HAS_NAMEDVALUE: {//has named item in context
					String name = (item.getParam() == 1)
						? item.stringValue() : _stack[sp--].toString();
					_stack[sp] = new DefBoolean(
						((XDContainer)_stack[sp]).hasXDNamedItem(name));
					continue;
				}
				case REMOVE_NAMEDVALUE: {//get named item from context
					String name = (item.getParam() == 1)
						? item.stringValue() : _stack[sp--].toString();
					XDValue v =
						((XDContainer)_stack[sp]).removeXDNamedItem(name);
					_stack[sp] = v == null ? new DefNull() : v;
					continue;
				}
				case GET_NAMED_AS_STRING: {//named item from context as string
					String name = (item.getParam() == 1)
						? item.stringValue() : _stack[sp--].toString();
					XDValue v =
						((XDContainer)_stack[sp]).getXDNamedItemValue(name);
					_stack[sp] = v == null
						? new DefString() : new DefString(v.stringValue());
					continue;
				}
				case NAMEDVALUE_GET:
					_stack[sp] = ((XDNamedValue) _stack[sp]).getValue();
					continue;
				case NAMEDVALUE_SET:
					sp--;
					_stack[sp] =
						((XDNamedValue) _stack[sp]).setValue(_stack[sp + 1]);
					continue;
				case NAMEDVALUE_NAME:
					_stack[sp] =
						new DefString(((XDNamedValue) _stack[sp]).getName());
					continue;
				case EQ_NULL:
					sp--;
					 _stack[sp] = new DefBoolean(
						 (_stack[sp] == null || _stack[sp].isNull()) &&
						 (_stack[sp+1] == null || _stack[sp+1].isNull()));
					continue;
				case NE_NULL:
					sp--;
					 _stack[sp] = new DefBoolean(
						 !((_stack[sp] == null ||_stack[sp].isNull()) &&
						 (_stack[sp+1] == null ||_stack[sp+1].isNull())));
					continue;
				case IS_EMPTY: {
					XDValue v = _stack[sp];
					boolean result = true;
					if (_stack[sp] != null && !_stack[sp].isNull()) {
						switch (v.getItemId()) {
							case XD_STRING:
								result = v.stringValue().isEmpty();
								break;
							case XD_ELEMENT:
								result = ((XDElement)v).isEmpty();
								break;
							case XD_CONTAINER:
								result = ((DefContainer)v).isEmpty();
						}
					}
					_stack[sp] = new DefBoolean(result);
					continue;
				}
				case CLOSE_XMLWRITER: // Close XML writer.
					((XDXmlOutStream) _stack[sp--]).closeStream();
					continue;
				case NEW_PARSERESULT:
					_stack[sp] = new DefParseResult(_stack[sp].toString());
					continue;
				case NEW_PARSER: {
					XDParser p = CompileBase.getParser(item.stringValue());
					int np = item.getParam();
					if (np > 0) { //number of parameters >= 1
						XDContainer d;
						XDValue val = _stack[sp];
						if (np == 1 && val.getItemId() == XD_NAMEDVALUE) {
							XDNamedValue ni = (XDNamedValue) val;
							d = new DefContainer();
							d.setXDNamedItem(ni.getName(), ni.getValue());
						} else {
							if (val.getItemId() == XD_CONTAINER) {
								d = new DefContainer((XDContainer) val);
								np--;
								sp--;
							} else {
								d = new DefContainer();
							}
							String [] sqParamNames =
								((CodeParser) item).getSqParamNames();
							for (int i = np-1; i >= 0; i--) {
							   d.setXDNamedItem(sqParamNames[i], _stack[sp--]);
							}
						}
						p.setNamedParams(chkNode, d);
					}
					_stack[++sp] = (XDValue) p;
					continue;
				}
				case GET_BYTES_FROM_STRING: {
					String s = _stack[sp].toString();
					if (item.getParam() == 1) {
						_stack[sp] = new DefBytes(s.getBytes());
					} else {
						sp--;
						_stack[sp] = new DefBytes(
							_stack[sp].toString().getBytes(s));
					}
					continue;
				}
			//Codes implemented in XCodeImplMethods
				case GET_TYPEID: //get type of a value (as integer type id)
				case GET_TYPENAME: // get name of type of a value
				case CHECK_TYPE:
			//Bytes
				case BYTES_CLEAR:
				case BYTES_SIZE: //size of byte array
				case BYTES_TO_BASE64:
				case BYTES_TO_HEX:
			//Duration
				case PARSE_DURATION:
				case DURATION_GETYEARS:
				case DURATION_GETMONTHS:
				case DURATION_GETDAYS:
				case DURATION_GETHOURS:
				case DURATION_GETMINUTES:
				case DURATION_GETSECONDS:
				case DURATION_GETRECURRENCE:
				case DURATION_GETFRACTION:
				case DURATION_GETSTART:
				case DURATION_GETEND:
				case DURATION_GETNEXTTIME:
			//Element
				case ELEMENT_CHILDNODES:
				case ELEMENT_NAME:
				case ELEMENT_NSURI:
			//ParseResult
				case GET_PARSED_STRING:
			//Datetime
				case GET_DAY:
				case GET_WEEKDAY:
				case GET_MONTH:
				case GET_YEAR:
				case GET_HOUR:
				case GET_MINUTE:
				case GET_SECOND:
				case GET_MILLIS:
				case GET_NANOS:
				case GET_FRACTIONSECOND:
				case GET_EASTERMONDAY:
				case GET_LASTDAYOFMONTH:
				case GET_DAYTIMEMILLIS:
				case GET_ZONEOFFSET:
				case GET_ZONEID:
				case IS_LEAPYEAR:
			//String
				case LOWERCASE:
				case UPPERCASE:
				case TRIM_S:
				case GET_STRING_LENGTH: //s.length()
				case WHITESPACES_S:
			//Report
				case GET_REPORT:
					_stack[sp] = XCodeProcessorExt.perform1v(item, _stack[sp]);
					continue;
			//Element
				case ELEMENT_ADDELEMENT:
				case ELEMENT_ADDTEXT:
			//Bytes
				case BYTES_ADDBYTE: //Add byte
			//Report
				case PUT_REPORT:
			//XmlWriter
				case SET_XMLWRITER_INDENTING: // Set writer indenting.
				case WRITE_TEXTNODE: // Write text node.
					XCodeProcessorExt.perform2(item, _stack[sp-1], _stack[sp]);
					sp-=2;
					continue;
			//formating to string
				case INTEGER_FORMAT:
				case FLOAT_FORMAT:
			//Bytes
				case BYTES_GETAT: //Get byte at position
			//Date
				case DATE_FORMAT:
				case ADD_DAY:
				case ADD_MONTH:
				case ADD_YEAR:
				case ADD_HOUR:
				case ADD_MINUTE:
				case ADD_SECOND:
				case ADD_MILLIS:
				case ADD_NANOS:
				case SET_DAY:
				case SET_MONTH:
				case SET_YEAR:
				case SET_HOUR:
				case SET_MINUTE:
				case SET_SECOND:
				case SET_MILLIS:
				case SET_NANOS:
				case SET_FRACTIONSECOND:
				case SET_DAYTIMEMILLIS:
				case SET_ZONEOFFSET:
				case SET_ZONEID:
			//String
				case GET_STRING_TAIL: //tail(s,i);
				case CUT_STRING: //cut(s,i);
			//Report
				case REPORT_TOSTRING:
				case NEW_NAMEDVALUE:
					sp--;
					_stack[sp] = XCodeProcessorExt.perform2v(item,_stack[sp],
						_stack[sp+1]);
					continue;
			//Bytes
				case BYTES_INSERT: //Insert byte before
				case BYTES_REMOVE: //remove byte(s)
				case BYTES_SETAT: //set byte at position
			//Element
				case ELEMENT_TOSTRING:
				case ELEMENT_GETATTR:
				case ELEMENT_HASATTR:
				case ELEMENT_SETATTR:
				case ELEMENT_TOCONTEXT:
			//Datetime
				case PARSE_DATE:
			//String
				case TRANSLATE_S:
				case REPLACEFIRST_S:
				case REPLACE_S:
				case GET_SUBSTRING: //s.substring(i[,j]);
				case GET_INDEXOFSTRING:
				case GET_LASTINDEXOFSTRING:
			//Report
				case REPORT_SETPARAM:
				case REPORT_SETTYPE: //set report type ('E', 'W', 'F', ...)
			//Constructors
				case NEW_CONTEXT:
				case NEW_ELEMENT:
				case NEW_BYTES:
				case NEW_INSTREAM:
				case NEW_OUTSTREAM:
				case NEW_BNFGRAMAR:
				case NEW_SERVICE:
				case NEW_XMLWRITER:
				case NEW_REPORT:
				case NEW_LOCALE:
					sp = XCodeProcessorExt.perform(this, item, sp, _stack);
					continue;
				//Other codes (implemented in XCodeProcessorExt)
				default:
					sp = XCodeProcessorExt.performX(this,
						item, chkNode, sp, _stack, pc);
				}//switch
			} catch (SRuntimeException ex) {
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkNode);
					sp = 0;
					continue;
				}
				Report report = ex.getReport();
				throw new XXException(updateReport(report, chkNode), ex);
			} catch (InvocationTargetException ex) {
				Throwable thr;
				thr = ex.getCause();
				if (thr instanceof Error) {
					throw (Error) thr;
				}
				if (_catchItem != null) {
					pc = genDefException(pc, thr, chkNode);
					sp = 0;
					continue;
				}
				throwError(pc, sp, thr, chkNode);
			} catch (Exception ex) {
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkNode);
					sp = 0;
					continue;
				}
				if (ex instanceof XXException) {
					XXException exx = (XXException) ex;
					if ("XDEF900".equals(exx.getMsgID())) {
						throw exx;
					}
				}
				throwError(pc, sp, ex, chkNode);
			} catch (SError ex) {
				if ("XDEF569".equals(ex.getMsgID())) {
					throw ex;
				}
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkNode);
					sp = 0;
					continue;
				}
				throwError(pc, sp, ex, chkNode);
			}
		} //end of while statement
	}

	private static Report updateReport(final Report rep, final ChkNode chkNode){
		if (rep != null && chkNode != null) {
			chkNode.ensurePosInfo(rep);
		}
		return rep;
	}

	/** Generate script exception.
	 * @param pc program counter.
	 * @param ex exception object
	 * @param chkNode processed XXNode.
	 * @return new program counter of catch block.
	 */
	private int genDefException(final int pc,
		final Throwable ex,
		final XXNode xNode) {
		int result = _catchItem.getCatchAddr();
		_localVariables = _catchItem.getVariables();
		Report report;
		if (ex instanceof SThrowable) {
			report = ((SThrowable) ex).getReport();
		} else {
			report = Report.error(null, ex.toString());
		}
		_stack[0] = new DefException(report,
			xNode != null ? xNode.getXPos() : null, pc);
		_catchItem = _catchItem.getPrevItem();
		return result;
	}

	private CodeUniqueset execUniqueParser(final CodeUniqueset dt,
		final int sp,
		final ChkElement chkElem) {
		XDValue[] stack = new XDValue[sp];
		System.arraycopy(_stack, 0, stack, 0, sp);
		XDValue x = exec(dt.getParseMethod(), chkElem);
		XDParseResult y;
		CodeUniqueset result = null;
		switch (x.getItemId()) {
			case XD_BOOLEAN:
				y =	new DefParseResult(chkElem.getTextValue());
				if (!x.booleanValue()) {
					y.putDefaultParseError(); //XDEF515 value error&{0}{ :}
				}
				break;
			case XD_PARSERESULT:
				y = (XDParseResult) x;
				break;
			case CompileBase.UNIQUESET_VALUE:
			case CompileBase.UNIQUESET_M_VALUE:
				result = (CodeUniqueset) x;
			default:
				y = chkElem._parseResult;
				if (x instanceof CodeUniqueset) {
					CodeUniqueset z = (CodeUniqueset) x;
					if (z != dt) {
						z.getParsedItems()[z.getKeyItemIndex()].setParsedObject(
							y.getParsedValue());
					}
				}
		}
		chkElem._parseResult = y;
		dt.getParseKeyItem(
			dt.getKeyItemIndex()).setParsedObject(y.getParsedValue());
		System.arraycopy(stack, 0, _stack, 0, sp);
		return result;
	}

	private void execUniqueOperation(final CodeUniqueset dt,
		final ChkNode chkNode,
		final int code) {
		if (code==UNIQUESET_ID || code==UNIQUESET_SET
			|| code==UNIQUESET_KEY_ID || code==UNIQUESET_KEY_SET
			|| code==UNIQUESET_M_ID || code==UNIQUESET_M_SET) {
			Report rep = updateReport(dt.setId(), chkNode);
			if (rep != null && code!=UNIQUESET_SET && code != UNIQUESET_KEY_SET
				&& code != UNIQUESET_M_SET) {
				if (chkNode._parseResult == null) {
					putReport(chkNode, rep);
				} else {
					chkNode._parseResult.putReport(rep);
				}
			}
		} else if (code == UNIQUESET_CHKID || code == UNIQUESET_M_CHKID
			|| code == UNIQUESET_KEY_CHKID) {
			if (!dt.hasId()) {
				String modif = (dt.getName() != null ? dt.getName()+" " : "")
					+ dt.printActualKey();
				//Unique value "&{0}" was not set
				Report rep = Report.error(XDEF.XDEF522, modif);
				updateReport(rep, chkNode);
				if (chkNode._parseResult == null) {
					_reporter.putReport(rep);
				} else {
					chkNode._parseResult.putReport(rep);
				}
			}
		} else {
			ArrayReporter list = dt.chkId();
			if (list != null) {
				String modif = (dt.getName()!=null ? dt.getName()+" " : "")
					+ dt.printActualKey();
				//Unique value "&{0}" was not set
				Report rep = Report.error(XDEF.XDEF522, modif);
				updateReport(rep, chkNode);
				switch (code) {
					case UNIQUESET_KEY_IDREF:
					case UNIQUESET_M_IDREF:
					case UNIQUESET_IDREF:
						list.putReport(rep);
						break;
					default :
						if (chkNode._parseResult == null) {
							_reporter.putReport(rep);
						} else {
							chkNode._parseResult.putReport(rep);
						}
				}
			}
		}
	}

	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <tt>null</tt>.
	 */
	final Object setUserObject(final String id, final Object obj) {
		return _userObjects.put(id, obj);
	}

	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	final Object removeUserObject(final String id) {
		return _userObjects.remove(id);
	}

	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public final Object getUserObject(final String id) {
		return _userObjects.get(id);
	}

	/** This method is revoked if an exception is thrown when x-script
	 * is processed and process X-definition is to be finished with fatal error.
	 * @param pc program counter.
	 * @param sp stack pointer.
	 * @param ex exception.
	 * @param xNode processed node.
	 * @throws SError this SError is thrown.
	 */
	private void throwError(final int pc,
		final int sp,
		final Throwable ex,
		final XXNode xNode) throws SError {
		String s = ex.getMessage();
		StackTraceElement[] ste = ex.getStackTrace();
		s = (xNode != null ? "&{xpath}" + xNode.getXPos() + "\n": "") +
			ex.getClass().getName() + (s != null ? ": " + s : "") + "\n" +
			(ste != null && ste.length > 0 ? "at " + ste[0] + "\n" : "") +
			"PC = " + (pc - 1) + "; " +
			(xNode != null ? "XPOS: " + xNode.getXPos() : "INIT section") +"\n";
		if (sp >= 0) {
			s += "STACK:\n";
			for (int i = sp; i >= sp; i--) {
				if (i >= _stack.length) {
					s += "STACK OVERFLOW: [" + i + "]" + "\n";
				} else {
					s += "[" + i + "]: " + _stack[i] + "\n";
				}
			}
		} else {
			s += "STACK: empty\n";
		}
		if (_globalVariables != null && _globalVariables.length > 0) {
			s += "GLOBAL VARIABLES:\n";
			for (String name:
				_xd.getXDPool().getVariableTable().getVariableNames()) {
				s += name + ": " + getVariable(name) + "\n";
			}
		}
		if (_localVariables != null && _localVariables.length > 0) {
			s += "LOCAL VARIABLES BLOCK:\n";
			for (int i = 0; i < _localVariables.length; i++) {
				s += "[" + i + "]: " + _localVariables[i] + "\n";
			}
		}
/*#if DEBUG*#/
		java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
		java.io.PrintStream ps = new java.io.PrintStream(bs);
		org.xdef.impl.code.CodeDisplay.displayCode(_code, ps);
		ps.close();
		s += "\nCODE:\n" + bs.toString() + "\n";
/*#end*/
		//put error to reporter.
		if (xNode != null) {
			_reporter.error(XDEF.XDEF569, s); //Fatal error&{0}{: }
			xNode.copyTemporaryReports();
		} else if (getStdErr() != null) {
			//Fatal error&{0}{: }
			getStdErr().putReport(Report.error(XDEF.XDEF569, s));
		}
		//throw fatal error.
		//Fatal error&{0}{: }
		SError err = new SError(Report.fatal(XDEF.XDEF569, s), ex);
		err.setStackTrace(ex.getStackTrace());
		throw err;
	}

	public class XDFunctionResolver implements XPathFunctionResolver {
		@Override
		public XPathFunction resolveFunction(final QName functionName,
			final int arity) { //TODO
			return null;
		}
	}

	public class XDVariableResolver implements XPathVariableResolver {
		public final boolean XPATH2 = DefXPathExpr.isXPath2();
		public boolean convertToString;
		@Override
		public Object resolveVariable(final QName qname) {
			String name = qname.toString();
			String uri;
			if ((uri = qname.getNamespaceURI()) == null || uri.length() == 0) {
				XVariable xv = _xd.findVariable('$' + name);
				if (xv == null) {
					xv = _xd.findVariable(name);
				}
				if (xv != null) {
					XDValue value = _globalVariables[xv.getOffset()];
					if (XPATH2 && convertToString) {
						//Xpath2 ???? bind??? this is a nasted trick!!
						convertToString = false;
						return  value.stringValue();
					}
					switch (value.getItemId()) {
						case XD_DECIMAL:
							return value.decimalValue();
						case XD_BOOLEAN:
							return value.booleanValue() ?
								Boolean.TRUE : Boolean.FALSE;
						case XD_INT:
							return value.longValue();
						case XD_FLOAT:
							return value.doubleValue();
						case XD_ELEMENT:
							return value.getElement();
						default:
							return value.stringValue();
					}
				}
			}
			convertToString = false;
			return null;
		}
	}

	/** try/catch block {throws link}. */
	private static final class CatchItem {
		private XDValue[] _variables;
		private final int _catchAddr;
		private CatchItem _prevItem;

		/** Create the instance of catch item.
		 * @param catchAddr address of catch block.
		 * @param variables local variables.
		 * @param prevCatchItem link to previous catch item.
		 */
		CatchItem(final int catchAddr,
			final XDValue[] variables,
			CatchItem prevCatchItem) {
			_variables = variables;
			_catchAddr = catchAddr;
			_prevItem = prevCatchItem;
		}

		/** Release catch item. */
		private CatchItem release() {
			CatchItem result = _prevItem;
			_prevItem = null;
			_variables = null; //let GC do the job
			return result;
		}

		//getters and setters.
		private int getCatchAddr() {return _catchAddr;}
		private CatchItem getPrevItem() {return _prevItem;}
		private XDValue[] getVariables() {return _variables;}
	}

	/** Call method block. */
	private static final class CallItem extends XDValueAbstract
		implements XDCallItem {

		/** Stack (local variables) of parent node. */
		private XDValue[] _parentLocalVariables;
		/** Parent call item. */
		private final CallItem _parent;
		/** Return address, */
		private final int _returnAddr;
		/** Debug step mode. */
		private final int _step;
		/** Counter of nesting. */
		private int _nestCount;

		/** Create the instance of call method block.
		 * @param returnAddr return address.
		 * @param parent parent call block.
		 * @param step debug step mode.
		 */
		private CallItem(final int returnAddr, CallItem parent, int step) {
			if (parent != null) {
				if ((_nestCount = parent._nestCount+1) > 999999) {
					// Too many of recursive call: &{0}
					throw new XXException(XDEF.XDEF553, _nestCount);
				}
			}
			_parent = parent;
			_parentLocalVariables = null;
			_returnAddr = returnAddr;
			_step = step;
		}

		/** Initialize call block.
		 * @param localVariablesSize size of local variables.
		 * @param localVariables local variables o f parent.
		 * @return new local variables.
		 */
		private XDValue[] init(final int localVariablesSize,
			final XDValue[] localVariables) {
			_parentLocalVariables = localVariables;
			return localVariablesSize > 0 ?
				new XDValue[localVariablesSize] : null;
		}

		////////////////////////////////////////////////////////////////////////
		// XDValue methods
		////////////////////////////////////////////////////////////////////////
		@Override
		public final short getItemId() {return XDValueID.XD_ANY;}
		@Override
		public final XDValueType getItemType() {return XDValueType.OBJECT;}

		////////////////////////////////////////////////////////////////////////
		// XDCallItem methods
		////////////////////////////////////////////////////////////////////////
		@Override
		public final XDCallItem getParentCallItem() {return _parent;}
		@Override
		public final int getDebugMode() {return _step;}
		@Override
		public final int getReturnAddr() {return _returnAddr;}
	}
}