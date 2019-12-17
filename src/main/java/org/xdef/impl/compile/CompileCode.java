package org.xdef.impl.compile;

import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeExtMethod;
import org.xdef.impl.code.CodeI1;
import org.xdef.impl.code.CodeParser;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.StringParser;
import org.xdef.impl.xml.KNamespace;
import org.xdef.XDBNFGrammar;
import org.xdef.XDBNFRule;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefRegex;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.DefXQueryExpr;
import org.xdef.impl.XDebugInfo;
import org.xdef.impl.XVariableTable;
import org.xdef.model.XMVariable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.xdef.XDConstants;
import org.xdef.XDContainer;
import org.xdef.impl.code.DefLocale;
import org.xdef.msg.XML;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.SPosition;

/** Generation of compiler objects - variables, methods etc.
 * @author Trojan
 */
public final class CompileCode extends CompileBase {
	/** Internal stack size. */
	private final static int STACK_SIZE = 512; //20 for tests is enough!

	/** The parser (just for error reporting). */
	XScriptParser _parser;
	/** Mode of compilation:<br/>
	 * NO_MODE ... no mode<br/>
	 * TEXT_MODE ... text events<br/>
	 * ELEMENT_MODE ... element events<br/>
	 * GLOBAL_MODE ... global definitions<br/>
	 * ANY_MODE ... all modes
	 */
	byte _mode;
	/** Internal stack with item types. */
	short[] _tstack;
	/** Internal stack with indexes to code constants
	 * or -1 if stack item is a value, or -2 if optimizing is not allowed). */
	int[] _cstack;
	/** Stack pointer (index to stack or -1). */
	int _sp;
	/** Max. stack pointer. */
	int _spMax;
	/** Index to last code item. */
	int _lastCodeIndex;
	/** Address of code initialization. */
	int _init;
	/** End address of code initialization. */
	private int _initEnd;
	/** Actual index to last local variable. */
	int _localVariablesLastIndex;
	/** Highest reached index to local variables. */
	int _localVariablesMaxIndex;
	/** Switch to ignore unresolved externals */
	boolean _ignoreUnresolvedExternals;
	/** Mode of external method interface: 0 - both modes, 1 - old, 2 - new. */
	private final int _externalMode;
	/** Debug mode. */
	boolean _debugMode;
	/** Array of external classes. */
	Class<?>[] _extClasses;
	/** Compiled code. */
	final ArrayList<XDValue> _code;
	/** Table of local variables. */
	Map<String, CompileVariable> _localVariables;
	/** External methods. */
	private final Map<String, CodeExtMethod> _extMethods;
	/** Array of declared external methods. */
	private final List<ExternalMethod> _declaredMethods;
	/** List of actual NameSpace prefixes. */
	Map<String, Integer> _nsPrefixes;
	/** Table of NameSpace URIs. */
	final List<String> _namespaceURIs;
	/** Table of script methods */
	private final Map<String, ScriptMethod> _scriptMethods;
	/** Table of script global variables. */
	XVariableTable _globalVariables;
	/** Variables block. */
	XVariableTable _varBlock;
	/** Size of size of predefined global variables. */
	private final int _globalPredefSize;
	/** Debug information. */
	XDebugInfo _debugInfo = null;
	/** Components. */
	final Map<String,SBuffer> _components = new LinkedHashMap<String,SBuffer>();
	/** Binds. */
	final Map<String, SBuffer> _binds = new LinkedHashMap<String, SBuffer>();
	/** Enumerations. */
	final Map<String, SBuffer> _enums = new LinkedHashMap<String, SBuffer>();
	/** XDLexicon object (null if not specified). */
	XDLexicon _lexicon = null;
	/** Flag if external method should be searched. */
	private boolean _ignoreExternalMethods;

	/** Creates a new instance of GenCodeObj.
	 * @param extClasses Array of external classes.
	 * @param externalMode id of mode external methods (old=1, new=2, both=0).
	 * @param debugMode debug mode flag.
	 * @param ignoreUnresolvedExternals ignore unresolved externals flag.
	 */
	CompileCode(final Class<?>[] extClasses,
		final int externalMode,
		final boolean debugMode,
		final boolean ignoreUnresolvedExternals) {
//		_spMax = 0; _parser = null; //java makes it
		_ignoreUnresolvedExternals = ignoreUnresolvedExternals;
		_externalMode = externalMode;
		_debugMode = debugMode;
		_tstack = new short[STACK_SIZE];
		_cstack = new int[STACK_SIZE];
		_sp = -1;
		_lastCodeIndex = -1;
		_code = new ArrayList<XDValue>();
		_localVariables = new LinkedHashMap<String, CompileVariable>();
		_extMethods = new LinkedHashMap<String, CodeExtMethod>();
		_declaredMethods = new ArrayList<ExternalMethod>();
		_scriptMethods = new LinkedHashMap<String, ScriptMethod>();
		_namespaceURIs = new ArrayList<String>();
		_globalVariables = _varBlock = new XVariableTable(null, 0);
		_localVariablesLastIndex = -1;
		_localVariablesMaxIndex = -1;
		_mode = NO_MODE;
		setExternals(extClasses); //external classes and/or objects
		_init = _initEnd = -1;
		//predefined global variables
		CompileVariable var = new CompileVariable("$stdOut",
			XD_OUTPUT, _globalVariables.getNextOffset(), (byte) 'G', null);
		var.setInitialized(true);
		_globalVariables.addVariable(var);
		var = new CompileVariable("$stdErr",
			XD_OUTPUT, _globalVariables.getNextOffset(), (byte) 'G', null);
		var.setInitialized(true);
		_globalVariables.addVariable(var);
		var = new CompileVariable("$stdIn",
			XD_INPUT, _globalVariables.getNextOffset(), (byte) 'G', null);
		var.setInitialized(true);
		_globalVariables.addVariable(var);
/*UNS*/
		var = new CompileVariable("$IDParser$", // use only internally
			XD_PARSER, _globalVariables.getNextOffset(), (byte) 'G', null);
		var.setInitialized(true);  // prevent to report errors
		_globalVariables.addVariable(var);
		var = new CompileVariable("$IDuniqueSet$", // use only internally
			CompileBase.UNIQUESET_M_VALUE,
			_globalVariables.getNextOffset(), (byte) 'G', null);
		var.setInitialized(true); // prevent to report errors
//		addCode(new CodeI1(XD_PARSER, LD_GLOBAL, 4));
//		addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1));
//		genStop();
		_globalVariables.addVariable(var);
//		addCode(new CodeI1(XD_PARSER, LD_GLOBAL, 4));
//		addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1));
//		genStop();
/*UNS*/
		_globalPredefSize = _globalVariables.getLastOffset();
	}

	/** Reinitialize fields to prepare the recompilation of code. */
	final void  reInit() {
		clearLocalVariables();
//		clearModelVariables();
		_localVariablesMaxIndex = -1;
		_code.clear();
		_lastCodeIndex = -1;
		_init = _initEnd = -1;
		_sp = -1;
		_spMax = 0;
		_globalVariables.setLastOffset(_globalPredefSize);
		for (ScriptMethod sm : _scriptMethods.values()) {
			sm.setAddr(-1);
		}
		for (XMVariable xmv: _globalVariables.toArray()) {
			CompileVariable v = (CompileVariable) xmv;
			if (v.getOffset() >= _globalPredefSize && !v.isConstant()) {
				//not predeclared
				v.setOffset(-1);
				v.setInitialized(false);
			}
		}
/*UNS*/
//		addCode(new CodeI1(XD_PARSER, LD_GLOBAL, 0));
//		addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1));
//		genStop();
/*UNS*/
	}

	/** Set XScriptParser. */
	final void setParser(final XScriptParser parser) {_parser = parser;}

	/** Clear local variables. */
	final void clearLocalVariables() {
		_localVariables.clear();
		_localVariablesLastIndex = -1;
	}

	final void setIgnoreExternalMethods(final boolean b) {
		_ignoreExternalMethods = b;
	}
	/** Add new variable of given name.
	 * @param name the name of variable.
	 * @param kind the variable kind ('G': global, 'L': local, 'X': XModel).
	 * @param spos source position where the variable was declared.
	 * @return the CompileVariable object.
	 */
	final CompileVariable addVariable(final String name,
		final short type,
		final byte kind,
		final SPosition spos) {
		if (type != PARSEITEM_VALUE && getTypeId(name) >= 0) {
			//Type identifier '&{0}' can't be used here
			_parser.error(XDEF.XDEF463, name);
			return new CompileVariable("?", type, -1, (byte) 'L', spos);
		}
		CompileVariable result = null;
		switch (kind) {
			case 'G':
				result = (CompileVariable) _globalVariables.getVariable(name);
				if (result == null) {
					result = new CompileVariable(name,
						type, _globalVariables.getNextOffset(), kind, spos);
					_globalVariables.addVariable(result);
					return result;
				} else {
					if (result.getOffset() == -1) {
						if (result.resolvePostDef(
							_globalVariables.getNextOffset(), this)) {
							return result;
						}
					}
				}
				break;
			case 'X':
				if (_varBlock.getVariable(name) == null) {
					result = new CompileVariable(name,
						type, _varBlock.getNextOffset(), kind, spos);
					if (_varBlock.addVariable(result)) {
						return result;
					}
				}
				break;
			case 'L':
				result = _localVariables.get(name);
				if (result == null) {
					result = new CompileVariable(name, type,
						++_localVariablesLastIndex, kind, spos);
					_localVariables.put(name, result);
					if (_localVariablesLastIndex > _localVariablesMaxIndex) {
						_localVariablesMaxIndex = _localVariablesLastIndex;
					}
					return result;
				}
				break;
			default:
				//Internal error&{0}{: }
				throw new SRuntimeException(SYS.SYS066, "variable kind: "+kind);
		}
		//Repeated declaration of variable '&{0}'&{#SYS000}&{1}
		//({; (already declared: }{)}
		putRedefinedError(null, XDEF.XDEF450,
			name, result == null ? null : result.getSourcePosition());
		return (result != null && result.getName().equals(name)
			&& result.getType() == type) 
			? result : new CompileVariable("?", type, -1, (byte) 'L', null);
	}

	/** Put error with the first declared item position.
	 * @param actpos the actual source position or null.
	 * @param id message ID.
	 * @param name name of item.
	 * @param sp Source position of declared item or null.
	 */
	final void putRedefinedError(SPosition actpos,
		final long id,
		final String name,
		final SPosition sp) {
		String s = null;
		if (sp != null) {
			s = "line="+ sp.getLineNumber() + "; column=" + sp.getColumnNumber()
				+ "; source=\"" + sp.getSystemId() + "\"";
		}
		//Redefinition of item '&{0}'&{#SYS000}&{1}({; (see: }{)}
		if (actpos == null) {
			_parser.error(id, name, s); //Redefinition of variable '&{0}'
		} else {
			_parser.error(actpos, id, name,s); //Redefinition of variable '&{0}'
		}
	}

	/** Check all unresolved declarations and try to resolve them. */
	final void clearPostdefines() {
		for (ScriptMethod gm: _scriptMethods.values()) {
			gm.clearPostdefs(); //(should be already clear)
		}
		for (XMVariable xmv: _globalVariables.toArray()) {
			((CompileVariable) xmv).clearPostdefs();// (should be already clear)
		}
	}

	/** Create string with list of parameter types from parameter list.
	 * @param paramTypes list of types of parameters.
	 * @return list of parameters converted to characters.
	 */
	private static String typeList(final short[] paramTypes) {
		if (paramTypes.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(paramTypes.length + 1).append('(');
		for (int i = 0; i < paramTypes.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append((char) paramTypes[i] + ' ');
		}
		return sb.append(')').toString();
	}

	/** Create string with list of parameter types from stack.
	 * @param numPar number of parameters.
	 * @return string with characters of parameter types.
	 */
	private String typeList(final int numPar) {
		if (numPar == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(numPar + 1).append('(');
		for (int i = _sp - numPar + 1, j = i; i <= _sp; i++) {
			if (i > j) {
				sb.append(',');
			}
			sb.append((char) _tstack[i] + ' ');
		}
		return sb.append(')').toString();
	}

	/** Get external classes.
	 * @return array of external classes.
	 */
	final Class<?>[] getExternals() {return _extClasses;}

	/** Set external classes.
	 * @param extObjects Array of external classes.
	 */
	final void setExternals(final Class<?>... extObjects) {
		Class<?>[] extObj = extObjects == null ? new Class<?>[0] : extObjects;
		_extClasses = new Class<?>[extObj.length];
		System.arraycopy(extObj, 0, _extClasses, 0, extObj.length);
	}

	/** Check if given identifier refers to a variable.
	 * @param name The identifier of variable.
	 * @return true if identifier refers to a variable.
	 */
	final boolean isVariable(final String name) {
		if (_localVariables.containsKey(name)) {
			return true;
		}
		if (_varBlock == null) {
			return false;
		}
			CompileVariable var = (CompileVariable)_varBlock.getXVariable(name);
			if (var == null || var.getKind() == 'G') {
				CompileVariable v = (CompileVariable) _varBlock.getXVariable(
					_parser._actDefName + '#' + name);
				if (v != null) {
					var = v;
				}
			}
			if (var != null) {
				if (var.getOffset() == -1) {
					return false;
				}
			}
		return var != null;
	}

	/* Get declared variable.
	 * @param name The name of variable.
	 * @return declared variable or null.
	 */
	final CompileVariable getVariable(final String name) {
		CompileVariable var;
		var = _localVariables.get(name);
		if (var == null && _varBlock != null) {
			var = (CompileVariable) _varBlock.getXVariable(name);
			if (var == null || var.getKind() == 'G') {
				CompileVariable v = (CompileVariable) _varBlock.getXVariable(
					_parser._actDefName + '#' + name);
				if (v != null) {
					var = v;
				}
			}
			if (var != null) {
				if (var.getOffset() == -1) {
					return null;
				}
			}
		}
		return var;
	}

	/** Get last code item. */
	final XDValue getLastCodeItem() {return getCodeItem(_lastCodeIndex);}

	/** Remove last code item. */
	final XDValue removeLastCodeItem() {return _code.remove(_lastCodeIndex--);}

	final XDValue getCodeItem(final int addr) {
		return (addr < 0 || addr >= _code.size()) ? null : _code.get(addr);
	}

	/** Replace last code item with new item.
	 * @param item item which will replace the existing one.
	 */
	final void replaceLastCodeItem(final XDValue item) {
		if (_lastCodeIndex >= 0) {
			_code.set(_lastCodeIndex, item);
		}
	}

	/** Set code item on given index of code array.
	 * @param index index to code array.
	 * @param item item which will replace the existing one.
	 */
	final void setCodeItem(final int index, final XDValue item) {
		_code.set(index, item);
	}

	/** Replace last code item with the item given by argument. The code
	 * of the item is changing the type of the top of the stack.
	 * @param item The item which replaces the last code item.
	 */
	final void replaceTop(final XDValue item) {
		_tstack[_sp] = item.getItemId();
		_code.set(_lastCodeIndex, item);
		_cstack[_sp] = (item.getCode() == LD_CONST) ? _lastCodeIndex : -1;
	}

	/** Replace two last code items with the item given by argument. The code
	 * of the item is changing the type of the top of the stack.
	 * @param item The item which replaces the last two code items.
	 */
	final void replaceTwo(final XDValue item) {
		_code.remove(_lastCodeIndex--);
		_sp--;
		replaceTop(item);
	}

	/** Create new undefined method.
	 * @param name The name.
	 * @param numPar Number of parameters.
	 * @return undefined method object.
	 */
	private CodeExtMethod crtUndefMethod(final String name, final int numPar) {
		return new CodeExtMethod(name,
			XD_UNDEF, CompileCode.UNDEF_CODE, numPar, null);
	}

	/** Add code (stack not changed).
	 * @param item The item to be added
	 * throws SRuntimeException if stack overflow error occurs.
	 */
	final void addCode(final XDValue item) {
		_code.add(++_lastCodeIndex, item);
	}

	/** Add code (update stack pointer).
	 * @param item The item to be added
	 * @param stackInc Increment of the stack after execution of this item.
	 * throws SRuntimeException if stack overflow error occurs.
	 */
	final void addCode(final XDValue item, final int stackInc) {
		addCode(item);
		if ((_sp += stackInc) > _spMax) {
			if (_sp >= STACK_SIZE) {
				//Overflow of compiler internal stack
				_parser.error(XDEF.XDEF204);
				_sp -= stackInc;
				return;
			}
			_spMax = _sp;
		}
		short type;
		if ((type = item.getItemId()) != XD_VOID) {//result is not void
			if (_sp >= 0) {
				_tstack[_sp] = type;
				_cstack[_sp] = (item.getCode()==LD_CONST) ? _lastCodeIndex : -1;
			}
		}
	}

	/** Add global method.
	 * @param resultType The result type of method.
	 * @param name The method name.
	 * @param address The code index of method start.
	 * @param params Array of parameters types.
	 * @param mode The mode of method.
	 * @param spos source position where the method was declared.
	 */
	final void addMethod(final short resultType,
		final String name,
		final int address,
		final short[] params,
		final short mode,
		final SPosition spos) {
		String extName = name + typeList(params);
		ScriptMethod gm = _scriptMethods.get(extName);
		if (gm == null) {
			_scriptMethods.put(extName,
				new ScriptMethod(resultType, address, params, mode, spos));
		} else if (!gm.resolvePostDef(address, this)) {
			//Repeated declaration of method '&{0}'&{#SYS000}&{1}
			//({; (already declared: }{)}
			putRedefinedError(null, XDEF.XDEF462,
				name, gm.getSourcePosition());
		}
	}

	final Method addDeclaredMethod(final String name, final Method method) {
		Method m;
		if ((m = getDeclaredMethod(name, method.getParameterTypes())) != null) {
			return m; //ignore repeated declarations
		}
		_declaredMethods.add(new ExternalMethod(name, method));
		return null;
	}

	private Method getDeclaredMethod(final String name,final Class<?>[] params){
		for (ExternalMethod d: _declaredMethods) {
			if (d.isMethod(name, params)) {
				return d.getMethod();
			}
		}
		return null;
	}

	/** Find external method in the class and in super classes.
	 * @param clazz Class to be inspected.
	 * @name name of method.
	 * @name parameters list of classes of parameters.
	 * @return method or null.
	 */
	final Method getExtMethod(final Class<?> clazz,
		final String name,
		final Class<?>[] params) {
		if (clazz != null) {
			Method[] methods = clazz.getMethods();
			for (Method m: methods) {
				if (name.equals(m.getName())
					&& (m.getModifiers() & Modifier.PUBLIC) != 0) {
					Class<?>[] p = m.getParameterTypes();
					if (p.length == params.length) {
						boolean paramsOK = true;
						for (int j = 0; j < p.length; j++) {
							Class<?> param = params[j];
							Class<?> par = p[j];
							if (!param.equals(par)) {
								if (!((param.equals(long.class)
									|| param.equals(Long.class))
									&& (par.equals(Long.class)
									|| par.equals(long.class)
									|| par.equals(Integer.class)
									|| par.equals(int.class)))
									&& !((param.equals(double.class)
									|| param.equals(double.class))
									&& (par.equals(Double.class)
									|| par.equals(double.class)
									|| par.equals(Float.class)
									|| par.equals(float.class)))
									&& !((param.equals(boolean.class)
									|| param.equals(Boolean.class))
									&& (par.equals(boolean.class)
									|| par.equals(Boolean.class)))) {
									paramsOK = false;
									break;
								}
							}
						}
						if (paramsOK) {
							return m;
						}
					}
				}
			}
			return null;
		}
		Method m = getDeclaredMethod(_parser._actDefName + '#' + name, params);
		return (m != null) ? m : getDeclaredMethod(name, params);
	}

	/** Find external method in given class.
	 * @param name The name of method.
	 * @param numPar Number of parameters.
	 * @param clazz The class.
	 * @param obj instantiated object or null.
	 * @return The object with external method call.
	 */
	final CodeExtMethod findExternalMethod(final String name,
		final int numPar,
		final Class<?> clazz,
		final Object obj) {
		int modifiers;
		Method m = null; //static
		Method m1 = null; //not static
		short resultType = XD_UNDEF;
		short code = UNDEF_CODE;
		if (m == null && _externalMode != 1) {//new or both
			// 2.new style, params: type m([p1[,p2[, ... ]]])
			Class<?>[] params = new Class<?>[numPar];
			for (int j = 0; j < numPar; j++) {
				Class<?> c = getTypeClass(
					(short) (_tstack[_sp - numPar+j+1] - XD_INT + 1));
				params[j] = c;
			}
			if ((m = getExtMethod(clazz, name, params)) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
		}
		if (m == null && _externalMode != 1) {//new
			//new style, ChkElement and array: type m(ChkElement, DefItem[])
			if ((m = getExtMethod(clazz, name, new Class<?>[] {
				org.xdef.proc.XXElement.class,
				XDValue[].class})) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD_CHKEL_XDARRAY;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
			if (m == null && (m = getExtMethod(clazz, name, new Class<?>[] {
				XXNode.class,
				XDValue[].class})) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD_XXNODE_XDARRAY;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
			if (m == null && (m = getExtMethod(clazz, name, new Class<?>[] {
				XXData.class, XDValue[].class})) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD_XXNODE_XDARRAY;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC)==0 && obj==null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
			if (m == null && (m = getExtMethod(clazz, name, new Class<?>[] {
				XDValue[].class})) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD_XDARRAY;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
		}
		if (m == null && _externalMode != 1) {//new
			//new style, XXNode and params: type m(XXNode[,p[,...]])
			Class<?>[] params = new Class<?>[numPar + 1];
			params[0] = XXNode.class;
			for (int j = 0; j < numPar; j++) {
				params[j + 1] = getTypeClass(
					(short) (_tstack[_sp-numPar+j+1] - XD_INT + 1));
			}
			if ((m = getExtMethod(clazz, name, params)) != null) {
				modifiers = m.getModifiers();
				if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
					m1 = m;
					m = null;
				} else {
					if ((resultType = getClassTypeID(m.getReturnType())) ==
						XD_UNDEF) {
						code = UNDEF_CODE;
						m = null;
					} else {
						code = EXTMETHOD_XXNODE;
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						}
					}
				}
			}
			if (m == null) {
				params[0] = XXElement.class;
				if ((m = getExtMethod(clazz, name, params)) != null) {
					modifiers = m.getModifiers();
					if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
						m1 = m;
						m = null;
					} else {
						if ((resultType=getClassTypeID(m.getReturnType())) ==
							XD_UNDEF) {
							code = UNDEF_CODE;
							m = null;
						} else {
							code = EXTMETHOD_XXNODE;
							modifiers = m.getModifiers();
							if ((modifiers & Modifier.STATIC)==0 && obj==null) {
								m1 = m;
								m = null;
							}
						}
					}
				} else {
					params[0] = XXData.class;
					if ((m = getExtMethod(clazz, name, params)) != null) {
						modifiers = m.getModifiers();
						if ((modifiers & Modifier.STATIC) == 0 && obj == null) {
							m1 = m;
							m = null;
						} else {
							if ((resultType = getClassTypeID(m.getReturnType()))
								== XD_UNDEF) {
								code = UNDEF_CODE;
								m = null;
							} else {
								code = EXTMETHOD_XXNODE;
								modifiers = m.getModifiers();
								if ((modifiers & Modifier.STATIC) == 0 &&
									obj == null) {
									m1 = m;
									m = null;
								}
							}
						}
					}
				}
			}
		}
		if (m == null) {
			if (m1 != null) {
				//External method '&{0}' must be 'static' and 'public'
				_parser.error(XDEF.XDEF466, m1.getName());
				m = m1;
			}
		}
		if (m != null) {
			return new CodeExtMethod(m.getDeclaringClass().getName()+'.'+name,
				resultType, code, numPar, m);
		}
		return null;
	}

	/** Get NameSpace context of this XElement. */
	final KNamespace getXDNamespaceContext() {
		KNamespace result = new KNamespace();
		for (Map.Entry<String, Integer> item : _nsPrefixes.entrySet()) {
			String key = item.getKey();
			if ("xml".equals(key) || "xmlns".equals(key)
				|| XDConstants.XDEF20_NS_URI.equals(
					_namespaceURIs.get(item.getValue()))
				|| XDConstants.XDEF31_NS_URI.equals(
					_namespaceURIs.get(item.getValue()))
				|| XDConstants.XDEF32_NS_URI.equals(
					_namespaceURIs.get(item.getValue()))) {
				continue;
			}
			result.setPrefix(key, _namespaceURIs.get(item.getValue()));
		}
		return result;
	}

	/** Generate "stop" operation. */
	final void genStop() {addCode(new CodeI1(XD_VOID, STOP_OP));}

	/** Add code for load attribute name.
	 * @param name an attribute name. */
	final void genLDAttr(final String name) {
		addCode(new CodeS1(XD_STRING, ATTR_REF, name));
		if (++_sp > _spMax) {
			if (_sp >= STACK_SIZE) {
				//Overflow of compiler internal stack
				_parser.error(XDEF.XDEF204);
				_sp--;
				return;
			}
			_spMax = _sp;
		}
		_tstack[_sp] = ATTR_REF_VALUE;
		_cstack[_sp] = -1;
	}

	/** Add code for load constant.
	 * @param item The item to be added
	 */
	final void genLDC(final XDValue item) {
		addCode(item);
		if (++_sp > _spMax) {
			if (_sp >= STACK_SIZE) {
				//Overflow of compiler internal stack
				_parser.error(XDEF.XDEF204);
				_sp--;
				return;
			}
			_spMax = _sp;
		}
		_tstack[_sp] = item.getItemId();
		_cstack[_sp] = _lastCodeIndex;
	}

	/** Add code for load variable.
	 * @param name The name of local variable.
	 * @return false if variable not exists.
	 */
	final boolean genLD(final String name) {
		CompileVariable var = getVariable(name);
		return var == null ? false : genLD(var);
	}

	/** Add code for load variable.
	 * @param name The name of local variable.
	 * @return false if variable not exists.
	 */
	final boolean genLD(CompileVariable var) {
		short xType = var.getType();
		if (xType == UNIQUESET_KEY_VALUE) {
			return false;
		}
		if (!var.isInitialized()) {
			if (xType != PARSEITEM_VALUE) {
				//Variable '&{0}' might be not initialized
				_parser.error(XDEF.XDEF464, var.getName());
			}
		}
		short code;
		int addr = var.getOffset();
		byte kind = var.getKind();
		if (var.getCodeAddr() >= 0 && var.isFinal()) {
			code = LD_CODE;
			addr = var.getCodeAddr();
		} else if (kind == 'G') {
			code = LD_GLOBAL;
		} else if (kind == 'X') {
			code = LD_XMODEL;
		} else {
			code = LD_LOCAL;
		}
		if (++_sp > _spMax) {
			if (_sp >= STACK_SIZE) {
				//Overflow of compiler internal stack
				_parser.error(XDEF.XDEF204);
				_sp--;
				return false;
			}
			_spMax = _sp;
		}
		_tstack[_sp] = xType;
		if (xType != CompileBase.UNIQUESET_NAMED_VALUE && var.isConstant()) {
			addCode(var.getValue().cloneItem());
			_cstack[_sp] = _lastCodeIndex;
		} else {
			addCode(new CodeS1(xType, code, addr, var.getName()));
			_cstack[_sp] = -1;
		}
		return true;
	}

	/** Add code for store value to local variable.
	 * @param name The name of local variable.
	 */
	final void genST(final String name) {
		if (_sp >= 0) {
			short xType = _tstack[_sp];
			CompileVariable var = getVariable(name);
			if (var == null) {
				_parser.error(XDEF.XDEF424, name);//Undefined variable &{0}
				var = addVariable(
					CompileBase.genErrId(), xType, (byte) 'L', null);
			}
			if (xType != var.getType()) {
				switch (xType) {
					case XD_FLOAT:
						if (var.getType() == XD_INT) {
							topToFloat();
							break;
						}
						topToString();
						break;
					case XD_STRING:
						topToString();
						break;
					case XD_INT:
						if (var.getType() == XD_DATETIME) {
							topToMillis();
							break;
						}
						break;
					case XD_ANY:
						break;
					case XD_UNDEF:
						return;
					case XD_PARSERESULT:
						convertTopToType(var.getType());
						break;
					default:
						_parser.error(XDEF.XDEF457, //Incompatible types&{0}{: }
							getTypeName(var.getType()) + "," +
							getTypeName(xType));
						return;
				}
			}
			genST(var);
		}
	}

	/** Add code for store value to local variable.
	 * @param var CompileVariable object.
	 */
	final void genST(final CompileVariable var) {
		var.setInitialized(true);
		short code;
		short xType = var.getType();
		switch (var.getKind()) {
			case 'G':
				code = ST_GLOBAL;
				break;
			case 'X':
				code = ST_XMODEL;
				break;
			default:
				code = ST_LOCAL;
				break;
		}
		addCode(new CodeS1(xType, code, var.getOffset(), var.getName()));
		_sp--;
	}

	/** Push  on top of the stack the "undefined" value.*/
	final void setUnDefItem() {
		_tstack[++_sp] = XD_UNDEF;
		_cstack[_sp] = -1;
	}

	/** Add jump to the end of code.
	 * If argument is an unconditional jump return. If it is a conditional jump
	 * then:
	 * <li>1. while the last code item is a logical 'not' operator reverse
	 * jump true/false condition and remove the last 'not' code item.</li>
	 * <li>2. if the last code item is a operator (CMPIEQ .. CMPDGT) then
	 * replace it with the jump item object from argument.
	 * If jump is JMPF operator reverse condition operator to logical
	 * complement (i.e. CMPIEQ change to CMPINE, CMPDGT to CMPDLE etc).
	 * Change code id of jump item from the argument to the last  code item
	 * converted to corresponding condition jump (CMPIEQ to JMPEQ etc) and
	 * replace the last code item with it. Otherwise just add the jump item to
	 * the end of code.
	 * @param jump code object with a conditioned jump.
	 */
	final void addJump(final CodeI1 jump) {
		if (jump != null) {
			short jumpCode;
			if ((jumpCode = jump.getCode()) == JMP_OP) {
				addCode(jump, 0);
				return;
			}
			XDValue lastItem;
			short code;
			while (_lastCodeIndex >= 0 &&
				(getLastCodeItem()).getCode() == NOT_B) {
				code = invertCode(jumpCode);
				if (code < 0) {
					break;
				}
				jump.setCode(jumpCode = code);
				_code.remove(_lastCodeIndex);
				_lastCodeIndex--;
			}
			if (_lastCodeIndex > 0) {
				lastItem = getLastCodeItem();
				CodeI1 codeItem;
				if ((_sp < 0 || _cstack[_sp] != -2) &&
					(code = lastItem.getCode()) >= CMPEQ && code <= CMPGT) {
					codeItem = (CodeI1) lastItem;
					if (jumpCode == JMPF_OP) {
						code = invertCode(codeItem._code);
						codeItem.setCode(code);
					}
					// convert cmpTxx -> jmpTxx
					jump.setCode((short) (code + JMPEQ - CMPEQ));
					//replace last code
					_code.set(_lastCodeIndex, jump); //replace last code
					_sp--;
					return;
				}
				XDValue prevItem = getCodeItem(_lastCodeIndex - 1);
				if ((prevItem.getCode()) == LD_TRUE_AND_SKIP) {
					if (jumpCode == JMPF_OP) {
						codeItem = (CodeI1) prevItem;
						codeItem.setCode(JMP_OP);
						codeItem.setParam(_lastCodeIndex + 1);
						jump.setCode(JMP_OP);
						_code.set(_lastCodeIndex, jump); //replace last code
					} else {
						jump.setCode(JMP_OP);
						_code.remove(_lastCodeIndex--);
						_code.set(_lastCodeIndex, jump); //replace last code
					}
					_sp--;
					return;
				}
			}
			addCode(jump, -1);
		}
	}

	/** If jump vector is not empty generate boolean value from it.
	 * @param jv CompileJumpVector object.
	 */
	final void jumpVectorToBoolValue(final CompileJumpVector jv) {
		if (!jv.isTrueJumpListEmpty()) {
			addJump(jv.addJumpItemToFalseList(JMPF_OP));
			genBoolJumpConvertor();
			jv.resoveTrueJumps(_lastCodeIndex - 1);
			jv.resoveFalseJumps(_lastCodeIndex);
		} else if (!jv.isFalseJumpListEmpty()) {
			addJump(jv.addJumpItemToFalseList(JMPF_OP));
			genBoolJumpConvertor();
			jv.resoveFalseJumps(_lastCodeIndex);
		}
	}

	/** Generate sequence of two operations:
	 * <li> LD_TRUE_AND_SKIP_CODE, LDC_FALSE_CODE.</li>
	 */
	final void genBoolJumpConvertor() {
		addCode(new CodeI1(XD_BOOLEAN, LD_TRUE_AND_SKIP));
		addCode(new DefBoolean(false), 1);
		_cstack[_sp] = -1;
	}

	/** Check if code is an compare operation or conditional jump.
	 * If yes, then the code is inverted to logical negation of given operation
	 * and the method returns inverted code. Otherwise the method returns -1.
	 * @param code inspected code.
	 * @return inverted code or -1.
	 */
	private short invertCode(short code) {
		switch (code) {
			case CMPEQ: // CMPxEQ -> CMPxNE
			case JMPEQ: // JMPxEQ -> JMPxNE
			case CMPGE: // CMPxGE -> CMPxLT
			case JMPGE: // JMPxGE -> JMPxLT
			case JMPF_OP: // JMPF -> JMPT
				return ++code;
			case CMPNE: // CMPxNE -> CMPxEQ
			case JMPNE: // JMPxNE -> JMPxEQ
			case CMPLT: // CMPxLT -> CMPxGE
			case JMPLT: // JMPxLT -> JMPxGE
			case JMPT_OP: // JMPT -> JMPF
				return --code;
			case CMPLE: // CMPxLE -> CMPxGT
			case JMPLE: // JMPxLE -> JMPxGT
				return (short) (code +  3);
			case CMPGT: // CMPxGT -> CMPxLE
			case JMPGT: // JMPxGT -> JMPxLE
				return (short) (code - 3);
			default:
				return -1;
		}
	}

	/** Generate default conversion of the item on the top of stack.
	 * @param resultType required type.
	 */
	final void convertTopToType(final short resultType) {
		short xType;
		if (_sp >= 0 && resultType != XD_UNDEF &&
			resultType != (xType = _tstack[_sp])) {
			if (xType == resultType) {
				return;
			}
			if (xType == XD_UNDEF ||//do not report other error messages!
				resultType == XD_ANY) {
				_tstack[_sp] = resultType;
				return;
			}
			int xValue = _cstack[_sp];
			if (xType == XD_NULL  && xValue >= 0) {
				_code.set(xValue, DefNull.genNullValue(resultType));
				_tstack[_sp] = resultType;
				return;
			}
			if (xType == XD_ANY) {//conversion from AnyValue
				//dynamic type check
				addCode(new CodeI1(resultType, CHECK_TYPE, resultType));
				_tstack[_sp] = resultType;
				return;
			}
			switch (resultType) {
				case XD_STRING:
					topToString();
					return;
				case XD_FLOAT:
					if (xType == XD_INT || xType == XD_DECIMAL) {
						topToFloat();
						return;
					}
					break;
				case XD_INT:
					if (xType == XD_DATETIME) {
						topToMillis();
						return;
					} else if (xType == XD_DECIMAL) {
						addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X, 0));
						_cstack[_sp] = -1;
						return;
					}
					break;
				case XD_DECIMAL:
					if (xType == XD_FLOAT || xType == XD_INT) {
						if (xValue >= 0) {
							_code.set(xValue, new DefDecimal(
								getCodeItem(xValue).decimalValue()));
						} else {
							addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X, 0));
							_cstack[_sp] = -1;
						}
						_tstack[_sp] = XD_DECIMAL;
						return;
					}
					break;
				case XD_BOOLEAN:
					if (xType == ATTR_REF_VALUE || xType == XD_PARSERESULT ||
						xType == XD_PARSER) {
						topToBool();
						return;
					}
					break;
				case XD_CONTAINER:
					addCode(new CodeI1(XD_CONTAINER, NEW_CONTEXT, 1));
					_cstack[_sp] = -1;
					return;
				case XD_DATETIME: {
					break;
				}
				case XD_DURATION: {
					break;
				}
				case XD_ELEMENT:
					break;
				case XD_PARSERESULT:
					switch (xType) {
						case XD_PARSER:
							addCode(new CodeI1(XD_PARSERESULT,PARSE_OP,1),0);
							return;
						case XD_BOOLEAN:
							return;
						case XD_BNFRULE:
							addCode(new CodeI1(XD_PARSERESULT,
								BNFRULE_PARSE, 1), 0);
							_tstack[_sp] = XD_PARSERESULT;
							return;
						default:
							break;
					}
					break;
				default:
			}
			_parser.error(XDEF.XDEF457, //Incompatible types&{0}{: }
				getTypeName(xType) + "," + getTypeName(resultType));
			_tstack[_sp] = XD_UNDEF;
		}
	}

	final void topToBool() {
		short xType;
		if (_sp >= 0 && XD_BOOLEAN != (xType = _tstack[_sp]) &&
			XD_UNDEF != xType) {
			int codesize;
			CodeS1 cs;
			Object o;
			if (xType == ATTR_REF_VALUE &&
				(codesize = _code.size() - 1) >= 0 &&
				(o = getCodeItem(codesize)) instanceof CodeS1 &&
				(cs = (CodeS1) o)._code == ATTR_REF) {
				//attribute
				cs._code = ATTR_EXIST;
				_cstack[_sp] = -1;
				_tstack[_sp] = XD_BOOLEAN;
			} else if (xType == XD_PARSERESULT) {
				addCode(new CodeI1(XD_BOOLEAN, PARSERESULT_MATCH, 1));
				_tstack[_sp] = XD_BOOLEAN;
				_cstack[_sp] = -1;
			} else if (xType == XD_PARSER) {
				addCode(new CodeI1(XD_BOOLEAN, PARSEANDCHECK, 1));
				_tstack[_sp] = XD_BOOLEAN;
				_cstack[_sp] = -1;
			} else if (xType == XD_CONTAINER) {
				addCode(new CodeI1(XD_BOOLEAN, TO_BOOLEAN, 1));
				_tstack[_sp] = XD_BOOLEAN;
				_cstack[_sp] = -1;
			} else {
				//Value of type '&{0}' expected
				_parser.error(XDEF.XDEF423,"boolean");
			}
		}
	}

	/** Conversion of the stack item at top position to float. */
	final void topToFloat() {topXToFloat(0);}

	/** Conversion of stack item under top to float. */
	final void topXToFloat() {topXToFloat(1);}

	/** Conversion of the stack item at top position to null or string. */
	final void topToNullOrString() {
		short xType;
		if (_sp >= 0 && (xType=_tstack[_sp])!=XD_STRING && xType!=XD_UNDEF){
			int xValue;
			if ((xValue = _cstack[_sp]) >= 0) {//constant
				if (xType == ATTR_REF_VALUE) {
					_code.set(xValue,
						new CodeS1(XD_STRING, ATTR_REF,
							getCodeItem(xValue).stringValue()));
					_cstack[_sp] = -1;
				} else {
					//constant
					_code.set(xValue,
						new DefString(getCodeItem(xValue).stringValue()));
				}
			} else {//value
				addCode(new CodeI1(XD_STRING, NULL_OR_TO_STRING));
				_cstack[_sp] = -1;
			}
			_tstack[_sp] = XD_STRING;
		}
	}

	/** Conversion of the stack item at top position to string. */
	final void topToString() {topXToString(0);}

	/** Conversion of stack item under top to string. */
	final void topXToString() {topXToString(1);}

	/** Conversion of the given stack item to float.
	 * @param index relative stack position from top.
	 */
	private void topXToFloat(final int index) {
		short xType;
		int sp = _sp - index;
		if (sp >= 0 && (xType=_tstack[sp])!=XD_FLOAT && xType!=XD_UNDEF){
			switch (xType) {
				case XD_INT:
					if (_cstack[sp] >= 0) { //constant
						_code.set(_cstack[sp], new DefDouble(
							getCodeItem(_cstack[sp]).longValue()));
					} else {
						//value
						addCode(new CodeI1(XD_FLOAT, TO_FLOAT_X, index));
						_cstack[sp] = -1;
					}	break;
				case XD_DECIMAL:
					if (_cstack[sp] >= 0) { //constant
						_code.set(_cstack[sp], new DefDouble(
							getCodeItem(_cstack[sp]).floatValue()));
					} else {
						//value
						addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X, index));
						_cstack[sp] = -1;
					}	break;
				default:
					//Value of type 'int' or 'float' expected
					_parser.error(XDEF.XDEF439);
					_cstack[sp] = -1;
					break;
			}
			_tstack[sp] = XD_FLOAT;
		}
	}

	/** Conversion of the given stack item to string.
	 * @param index relative stack position from top.
	 */
	private void topXToString(final int index) {
		short xType;
		int sp = _sp - index;
		if (sp >= 0 && (xType=_tstack[sp])!=XD_STRING && xType!=XD_UNDEF){
			int xValue;
			if ((xValue = _cstack[sp]) >= 0) {//constant
				if (xType == ATTR_REF_VALUE) {
					_code.set(xValue,
						new CodeS1(XD_STRING, ATTR_REF,
							getCodeItem(xValue).stringValue()));
					_cstack[sp] = -1;
				} else {
					//constant
					_code.set(xValue,
						new DefString(getCodeItem(xValue).stringValue()));
				}
			} else {//value
				addCode(new CodeI1(XD_STRING, TO_STRING));
				_cstack[sp] = -1;
			}
			_tstack[sp] = XD_STRING;
		}
	}

	/** Conversion of the stack item at top position to float. */
	final void topToMillis() {
		short xType;
		if (_sp >= 0 && (xType=_tstack[_sp]) != XD_INT &&
			xType != XD_ANY && xType != XD_UNDEF) {
			if (xType == XD_DATETIME) {
				if (_cstack[_sp] >= 0) { //constant
					_code.set(_cstack[_sp], new DefLong(
						getCodeItem(_cstack[_sp]).datetimeValue().
						getTimeInMillis()));
				} else { //value
					addCode(new CodeI1(XD_INT, TO_MILLIS));
					_cstack[_sp] = -1;
				}
			} else {
				//Value of type 'Datetime' or 'int' expected
				_parser.error(XDEF.XDEF441);
				_tstack[_sp] = XD_INT;
				_cstack[_sp] = -1;
			}
			_tstack[_sp] = XD_INT;
		}
	}

	/** Conversion of stack item under top to millisecond type. */
	final void topXToMillis() {topXToMillis(1);}

	/** Conversion of the given stack item to millisecond type.
	 * @param index relative stack position from top.
	 */
	private void topXToMillis(final int index) {
		short xType;
		int sp = _sp - index;
		if (sp >= 0 && (xType=_tstack[sp])!=XD_INT && xType!=XD_UNDEF) {
			if (xType == XD_DATETIME) {
				if (_cstack[sp] >= 0) { // constant
					_code.set(_cstack[sp],
						new DefLong(getCodeItem(_cstack[sp]).
						datetimeValue().getTimeInMillis()));
				} else { //value
					addCode(new CodeI1(XD_STRING, TO_MILLIS_X, index));
					_cstack[sp] = -1;
				}
			} else if (xType != XD_INT) {
				//Value of type 'Datetime' or 'int' expected
				_parser.error(XDEF.XDEF441);
				_tstack[sp] = -1;
			}
			_tstack[sp] = XD_INT;
		}
	}
	/** Conversion of the part stack to context..
	 * @param index relative stack position from top.
	 */
	private void toContext(final int n) {
		for (int i = 0; i < n; i++) {
			if (_cstack[_sp - i] < 0) { // not all constants
				addCode(new CodeI1(XD_CONTAINER, STACK_TO_CONTEXT, n), -n + 1);
				return;
			}
		}
		XDContainer c = new DefContainer();
		for (int i = n - 1; i >= 0; i--) {
			c.addXDItem(_code.get(_lastCodeIndex));
			_code.remove(_lastCodeIndex--);
		}
		_code.add(c);
		_sp = _sp - n + 1;
		_cstack[_sp] = ++_lastCodeIndex;
		_tstack[_sp] = XD_CONTAINER;
	}

	/** Generate conversion of two top stack items to float. */
	final void operandsToFloat() {
		topXToFloat(1);
		topToFloat();
	}

	/** Conversion of two top stack items to string. */
	final void operandsToString() {
		topXToString(1);
		topToString();
	}

	/** Conversion of two top stack items from date to milliseconds. */
	final void operandsToMillis() {
		topXToMillis(1);
		topToMillis();
	}

	/** Remove code from codeIndex to end and clear associated stack.
	 * @param codeIndex The point from which code is removed.
	 * @param sp The saved stack pointer.
	 * @param spMax The saved max. stack pointer.
	 */
	 final void removeCodeFromIndexAndClearStack(final int codeIndex,
		final int sp,
		final int spMax) {
		for (int i = _lastCodeIndex; i > codeIndex; i--) {
			_code.remove(i);
		}
		_sp = sp;
		_spMax = spMax;
		_lastCodeIndex = codeIndex;
		for (int i = 0; i <= _sp; i++) {
			if (_cstack[i] > codeIndex) {
				_cstack[i] = -1;
			}
		}
	}

	/** Generate pop stack item (throw it away). */
	final void genPop() {
		if (_sp >= 0) {
			addCode(new CodeI1(XD_VOID, POP_OP), -1);
		}
	}

	/** Generate duplicate stack item. */
	final void genDup() {
		if (_sp >= 0) {
			short xType = _tstack[_sp];
			if(xType == XD_INT || xType == XD_BOOLEAN ||
				xType == XD_FLOAT || xType == XD_STRING) {
				addCode(new CodeI1(xType, STACK_DUP), 1);
			} else {
				//Internal error: &{0}
				_parser.error(SYS.SYS066, "Stack copy on type " + xType);
			}
		}
	}

	final boolean scriptMethod(final String name, final int numPar) {
		ScriptMethod lm = _scriptMethods.get(_parser._actDefName + '#' +name);
		if (lm == null) {
			lm = _scriptMethods.get(name);
			if (lm == null) {
				return false;
			}
		}
		short[] pars = lm.getParams();
		if (numPar != pars.length) {
			long errcode = numPar < pars.length
				? XDEF.XDEF460 //More parameters required for method &{0}
				: XDEF.XDEF461;//Too many parameters for method &{0}
			_parser.error(errcode, name);
		} else {
			for (int i = numPar - 1, j = _sp;  i >= 0; i--) {
				if (j < 0 || pars[i] != _tstack[j--]) {
					if (pars[i]!=XD_ANY && _tstack[j+1]!=XD_ANY &&
						pars[i]!=XD_UNDEF &&_tstack[j+1]!=XD_UNDEF){
						_parser.error(XDEF.XDEF467); //Incorrect parameter type
					}
					break;
				}
			}
		}
		CodeI1 operator = new CodeI1(lm.getResultType(), CALL_OP, lm.getAddr());
		int np = numPar;
		if (lm.getResultType() != XD_VOID) {
			np--;
		}
		addCode(operator, -np);
		if (lm.getAddr() == -1) {
			lm.addPostDef(_lastCodeIndex);
		}
		return true;
	}

	final boolean externalMethod(final String name,
		final String extName,
		final int numPar) {
		if (_ignoreExternalMethods){
			return false;
		}
		CodeExtMethod method = findExternalMethod(
			name, numPar, org.xdef.impl.ext.XExtUtils.class, null);
		if (method == null) {
			method = findExternalMethod(name, numPar,java.lang.Math.class,null);
		}
		if (method == null) {
			method = _extMethods.get(extName);
		}
		if (method == null) {
			method = findExternalMethod(name, numPar, null, null);
		}
		if (method == null) {
			//first occurrence
			if (_extClasses != null) {
				for (int i = 0; i < _extClasses.length; i++) {
					method = findExternalMethod(name,
						numPar,
						_extClasses[i],
						null);
//						_extObjects[i]);
					if (method != null) {
						break;
					}
				}
			}
			if (method == null) {
				return false;
			}
			_extMethods.put(extName, method);
		}
		int np = numPar;
		if (method._resultType != XD_VOID) {
			np--;
		}
		addCode(method,	-np);
		return true;
	}

	/** Generate method call.
	 * @param name The name of method.
	 * @param numPar Number of parameters (types of parameters are in stack).
	 * @return name of the unknown method or null.
	 */
	final String genMethod(final String name, final int numPar) {
		String extName = name + typeList(numPar);
		if (extName.indexOf('?') >= 0) {
			//don't process searching of method with undefined parameter
			_sp -= numPar -1;
			_tstack[_sp] = XD_UNDEF;
			_cstack[_sp] = -1;
			return null;
		}
		CompileVariable var = getVariable(name);
		if (var != null && (var.getType() == UNIQUESET_VALUE
			&& var.getCodeAddr() == -1
			|| var.getType() == PARSEITEM_VALUE && numPar == 0)){//check type ID
			//unique type, unique value
			CodeI1 op = new CodeI1(XD_BOOLEAN,
				CALL_OP, var.getParseMethodAddr());
			addCode(op, 1);
			// return null if it is OK, otherwise return the name of method
			return numPar != 0 ? name : null;
		}
		if (scriptMethod(extName, numPar)) {
			return null;
		} else if (numPar > 0 && _tstack[_sp] == XD_CONTAINER) {
			if (scriptMethod(name + typeList(numPar), numPar)) {
				return null;
			}
		}
		if (externalMethod(name, extName, numPar)) {
			return null;
		}
		if (internalMethod(name, numPar)) {
			return null;
		}
		String s = null;
		if (!_ignoreUnresolvedExternals) {
			//create string with the method name and the list of parameter types
			s = name + "(";
			for (int i = _sp - numPar + 1, j = i; i <= _sp; i++) {
				if (i > j) {
					s += ',';
				}
				short type = _tstack[i];
				s += type == ATTR_REF_VALUE ?
					"String" : CompileBase.getTypeName(type);
			}
			s += ')';
		}
		//generate dummy code.
		CodeExtMethod method = crtUndefMethod(name, numPar);
		_extMethods.put(name+extName, method);
		int np = numPar;
		if (method._resultType != XD_VOID) {
			np--;
		}
		addCode(method,	-np);
		return s; // null if it is OK, othrwise it is error message
	}

	/** Put report XDEF998 "{0}" is deprecated. Please use "&{1}" instead.
	 * @param old old text.
	 * @param replace text to be deprecated/
	 */
	final void reportDeprecated(final String old, final String replace) {
		_parser.warning(XDEF.XDEF998,old, replace);
	}

	/** Generation of internal method code.
	 * @param name The name of method.
	 * @param numPar Number of parameters (types of parameters are in stack).
	 */
	final boolean internalMethod(final String name, final int numPar) {
		InternalMethod imethod = getTypeMethod(NOTYPE_VALUE_ID, name);
		if (imethod == null) {
			return false;
		}
		if (imethod.isDeprecated()) {
			reportDeprecated(name, imethod.getRecommendedName());
		}
		genInternalMethod(name, numPar, imethod);
		return true;
	}

	final boolean genConstructor(final short type, final int numPar) {
		if (type == XD_UNDEF) {
			if (_sp >= 0) {
				_cstack[_sp] = -1;
			}
			return true; //do not report multiple errors!
		}
		InternalMethod imethod = getTypeMethod(type, "#");
		if (imethod == null) {
			return false;
		}
		genInternalMethod(getTypeName(type), numPar, imethod);
		return true;
	}

	final boolean genClassMethod(final String name, final int numPar) {
		if (_sp - numPar < 0) {
			return false;
		}
		short xType = _tstack[_sp - numPar];
		if (xType == XD_VOID || xType == XD_UNDEF) {
			if (xType == XD_VOID) {
				_parser.error(XDEF.XDEF438); //Value expected
			}
			return true;
		}
		if ("toString".equals(name)) {
			if (numPar == 0) {
				topToString();
				return true;
			}
		}
		if (xType == ATTR_REF_VALUE && numPar == 0) {
			if ("exists".equals(name)) {
				topToBool();
				return true;
			}
		}
		if (xType < 0 || xType >= NOTYPE_VALUE_ID) {
			return false;
		}
		InternalMethod imethod = getTypeMethod(xType, name);
		if (imethod == null && xType == XD_PARSER) {
			addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1), 0);
			xType = _tstack[_sp - numPar];
			imethod = getTypeMethod(xType, name);
		}
		if (imethod != null) {
			genInternalMethod(name, numPar + 1, imethod);
			return true;
		}
		return false;
	}

	/** This method is called only for GET_XPATH and GET_XPATH_FROM_SOURCE!
	 * @param npar number of parameters.
	 * @param code code to be optimized.
	 * @return true if code was optimized.
	 */
	private boolean optimizeXPath(final int npar, final short code) {
		int sp = code == GET_XPATH ? _sp-npar+1 : _sp;
		int constPar = _cstack[sp];
		if (constPar < 0) {
			return false; // xpath expression is not string constant
		}
		String s = getCodeItem(constPar).toString();
		if (s == null || (s = s.trim()).isEmpty()) {
			//XPath error&{0}{: }
			_parser.error(XML.XML505, "empty argument");
			return false;
		}
		int ix = s.charAt(0) == '@' ? 1 : s.startsWith("self::") ? 6 : 0;
		String name = s.substring(ix);
		ix = StringParser.chkXMLName(name, (byte) 10) ?
			name.indexOf("::") < 0 ? ix : -1 : -1;
		if (ix < 0) {
			return false;
		}
		if (npar == 2) {
			if (code == GET_XPATH) {
				if (constPar + 1 != _lastCodeIndex) {
					return false;
				}
				XDValue v = _code.get(_lastCodeIndex); //element value
				//relace code with string constant
				_code.set(constPar++, v);
				_tstack[_sp - 1] = _tstack[_sp]; //should be element
				_cstack[_sp - 1] = _cstack[_sp];
			}
			sp = --_sp;
		}
		int ndx = name.indexOf(':');
		if (ndx > 0) { // has prefix -> namespaceURI
			String prefix = name.substring(0, ndx);
			String uri = getXDNamespaceContext().getNamespaceURI(prefix);
			name = '{' + uri + '}' + name.substring(ndx+1);
		} else if (ix != 1) { // not attribute -> may have defalut namespace
			String uri = getXDNamespaceContext().getNamespaceURI("");
			if (uri != null) {
				name = '{' + uri + '}' + name;
			}
		}
		switch (ix) {
			case 1:
				//@name->String
				_code.set(constPar,
					new CodeS1(XD_CONTAINER, GETATTR_FROM_CONTEXT, npar,name));
				_tstack[sp] = XD_CONTAINER;
				break;
			case 6:
				// self::name -> element
				_code.set(constPar,
					new CodeS1(XD_CONTAINER, GETELEM_FROM_CONTEXT, npar,name));
				_tstack[sp] = XD_CONTAINER;
				break;
			default:
				// name -> Context
				_code.set(constPar,
					new CodeS1(XD_CONTAINER, GETELEMS_FROM_CONTEXT,npar,name));
				_tstack[sp] = XD_CONTAINER;
				break;
		}
		_cstack[sp] = -1;
		return true;
	}

	/** Generation of internal method code.
	 * @param name The name of method.
	 * @param numPar Number of parameters (types of parameters are in stack).
	 * @param imethod The internal method descriptor.
	 */
	private void genInternalMethod(final String name,
		final int numPar,
		final InternalMethod imethod) {
		short code = imethod.getCode();
		int npar = numPar; //is modified, should be local
		//first parameter type
		short par1typ = npar > 0 ? _tstack[_sp - (npar - 1)] : -1;
		//first parameter value constant pointer or -1
		int par1const = npar > 0 ? _cstack[_sp - (npar - 1)] : -1;
		InternalMethod method = imethod;
		short resultType = method.getResultType();
		XDValue operator = null;
		switch (code) {
			case FROM_ELEMENT://deprecated
				reportDeprecated("fromElement", "from");
				break;
			case LD_CONST: {
				if (resultType != XD_PARSER) {
					//Internal error: &{0}
					_parser.error(XDEF.XDEF309,"const type: "+resultType);
					break; // this should never happen!
				}
				// parsers
				if (npar == 0) { // no parameters
					if ("string".equals(name) || "CDATA".equals(name)) {
						genLDC(CompileBase.getParser("CDATA"));
					} else {
						if (imethod.getMinParams() > 0) {
							//More parameters required for method &{0}
							_parser.error(XDEF.XDEF460, name);
						}
						genLDC(CompileBase.getParser(name));
					}
					return;
				} else if ("list".equals(name) && //deprecated!
					par1typ != XD_CONTAINER && par1typ != XD_PARSER) {
					reportDeprecated("list", "enum");
					genInternalMethod("enum",
						npar, getTypeMethod(NOTYPE_VALUE_ID, "enum"));
					return;
				}
				XDParser p =
					CompileBase.getParser("CDATA".equals(name) ? "string":name);
				KeyParam[] pars = method.getKeyParams();
				String[] sqParamNames = method.getSqParamNames();
				if (npar == 1 && _tstack[_sp] == XD_CONTAINER &&
					_cstack[_sp] == _lastCodeIndex) {// set named parameters
					DefContainer h = (DefContainer) getLastCodeItem();
					int len = h.getXDNamedItemsNumber();
					for (int i = len - 1; i >= 0; i--) {
						String s = h.getXDNamedItemName(i);
						boolean found = false;
						for (int j = 0; j < pars.length; j++) {
							if (pars[j].getName().equals(s)) {
								XDValue[] legal = pars[j].getLegalValues();
								if (legal != null && legal.length > 0) {
									XDValue v = h.getXDNamedItemValue(s);
									boolean found1 = false;
									for (int k = 0; k < legal.length; k++) {
										if (legal[k].equals(v)) {
											if (k == 0) {// default
												h.removeXDNamedItem(s);
												len--;
											}
											found1 = true;
										}
									}
									if (!found1) {
										//Incorrect value of '&{0}'
										_parser.error(XDEF.XDEF809, s);
									}
								}
								found = true;
								break;
							}
						}
						if (!found) {
							//Illegal parameter name '&{0}'
							_parser.error(XDEF.XDEF801, s);
						}
					}
					for (KeyParam par: pars) {
						String parName = par.getName();
						XDValue val = h.getXDNamedItem(parName);
						if (par.isFixed()) {
							if (val == null) {
								h.setXDNamedItem(parName,par.getDefaultValue());
								len++;
							} else if (!val.equals(val)) {
								//Incorrect value of '&{0}'
								_parser.error(XDEF.XDEF809, parName);
							}
						} else if (par.isRequired() && val == null) {
							//Missing required parameter: &{0}
							_parser.error(XDEF.XDEF545, parName);
						}
					}
					if (len > 0) {
						try {
							p.setNamedParams(null, h);
						} catch (Exception ex) {
							if (ex instanceof SThrowable) {
								_parser.putReport(((SThrowable)ex).getReport());
							} else {
								_parser.putReport(
									//Internal error&{0}{: }
									Report.error(SYS.SYS066, ex, ex));
							}
						}
					} else if ("datetime".equals(name)
						|| "xdatetime".equals(name)) {
						//More parameters required for method &{0}
						_parser.error(XDEF.XDEF460, "xdatetime");
					}
					_code.set(_lastCodeIndex, p);
					_tstack[_sp] = resultType;
				} else {
					if (_tstack[_sp] == XD_CONTAINER) {
						if (npar-1 > sqParamNames.length) {
							//Too many parameters for method &{0}
							_parser.error(XDEF.XDEF461, name);
						}
					} else if (npar > method.getMaxParams()) {
						//Too many parameters for method &{0}
						_parser.error(XDEF.XDEF461, name);
					}
					boolean allconst = true;
					for (int i = 0; i < npar; i++) {
						if (_cstack[_sp-i] != _lastCodeIndex - i) {
							allconst = false; // not constant
							break;
						}
					}
					if (_tstack[_sp] != XD_CONTAINER) {
						if ("string".equals(name)) {
							p = new org.xdef.impl.parsers.XDParseCDATA();
						}
						if (npar > sqParamNames.length) {
							if (sqParamNames.length == 1) {
								toContext(npar);
								npar = 1;
								if (allconst) {
									npar = 1;
									XDValue val = removeLastCodeItem();
									_sp--;
									XDContainer d = new DefContainer();
									d.setXDNamedItem(sqParamNames[0], val);
									try {
										p.setNamedParams(null, d);
										genLDC(p);
										return;
									} catch (Exception ex) {} //never happens
								} else {
									addCode(new CodeS1(XD_NAMEDVALUE,
										CREATE_NAMEDVALUE, sqParamNames[0]),0);
								}
							}
						}
					}
					if (allconst) {
						XDValue val = removeLastCodeItem();
						_sp--;
						XDContainer d;
						if (val.getItemId() == XD_CONTAINER) {
							d = (XDContainer) val;
							if (--npar >= 1) {
								val = removeLastCodeItem();
								_sp--;
							}
						} else {
							d = new DefContainer(); //empty map
						}
						if (--npar >= 0) { // sequential parameters
							if (sqParamNames!=null && sqParamNames.length > 0) {
								if (sqParamNames.length - 1 < npar) {
									//Too many of sequential parameters
									_parser.error(XDEF.XDEF465);
								} else if (npar == 0 && sqParamNames.length >=2
									&& (("minInclusive".equals(sqParamNames[0])
									  && "maxInclusive".equals(sqParamNames[1]))
									|| ("minLength".equals(sqParamNames[0])
									  && "maxLength".equals(sqParamNames[1])))){
									// one parameter, expand it to two
									if (!d.hasXDNamedItem(sqParamNames[0])) {
										d.setXDNamedItem(sqParamNames[0], val);
									} else {
										// Conflict of sequential parameter and
										// named parameter: &{0}
										_parser.error(XDEF.XDEF442,
											sqParamNames[0]);
									}
									if (!d.hasXDNamedItem(sqParamNames[1])
										&& !("maxLength".equals(sqParamNames[1])
										&& d.hasXDNamedItem("trimTo"))) {
										d.setXDNamedItem(sqParamNames[1], val);
									}
									npar--;
								} else {
									while (npar > 0) {
										d.setXDNamedItem(
											sqParamNames[npar], val);
										val = removeLastCodeItem();
										_sp--;
										npar--;
									}
									d.setXDNamedItem(sqParamNames[0], val);
								}
							} else {
								//Too many of sequential parameters
								_parser.error(XDEF.XDEF465);
							}
						}
						try {
							if (!d.isEmpty()) {
								XDValue minlen =
									d.getXDNamedItemValue("minLength");
								XDValue maxlen =
									d.getXDNamedItemValue("maxLength");
								if (minlen != null && maxlen != null
									&& minlen.equals(maxlen)) {
									d.removeXDNamedItem("minLength");
									d.removeXDNamedItem("maxLength");
									d.setXDNamedItem("length", minlen);
								}
								p.setNamedParams(null, d);
							}
						} catch (Exception ex) {
							if (ex instanceof SThrowable) {
								_parser.putReport(((SThrowable)ex).getReport());
							} else {
								//Incorrect parameter value
								_parser.error(XDEF.XDEF471);
							}
						}
						genLDC(p);
					} else {
						addCode(new CodeParser(resultType,
							NEW_PARSER, npar, p.parserName(), sqParamNames),
							-npar + 1);
					}
				}
				return;
			}
			case GET_DBQUERY: {
				if (npar < 1) {
					break;
				}
				if (par1typ == XD_SERVICE) {
					if (npar == 1) {
						//The argument with query statement is missing
						_parser.error(XDEF.XDEF111);
					}
				} else if (par1typ != XD_STATEMENT) {
					//The first argument of the method 'dbquery' must be either
					// DBconnection or XIterator
					_parser.error(XDEF.XDEF110);
					break;
				}
				// check modification parameters
				for (int i = _sp - npar + 2; i <= _sp; i++) {
					if (_tstack[i] != XD_STRING) {
						//The argument &{0} must be of &{1} type
						_parser.error(XDEF.XDEF112, i-(_sp-npar)-1, "String");
					}
				}
				break;
			}
			case SET_ATTR:
			case GET_ATTR:
			case HAS_ATTR:
			case DEL_ATTR:
			case CREATE_ELEMENTS:
			case CREATE_ELEMENT: {
				int xpar = code == CREATE_ELEMENTS ? 2 :
					code == CREATE_ELEMENT ? 1 : code == SET_ATTR ? 3 : 2;
				if (npar == xpar && _cstack[_sp] >= 0 &&
					_tstack[_sp] == XD_STRING) {
					String qname = getCodeItem(_cstack[_sp]).toString();
					int i;
					String prefix = (i = qname.indexOf(':')) >= 0 ?
						qname.substring(0, i) : "";
					Integer p = _nsPrefixes.get(prefix);
					if (p == null) {
						if (prefix.length() > 0) {
							//Namespace for prefix '&{0}' isn't defined
							_parser.error(XDEF.XDEF257, prefix);
						}
					} else {
						npar++;
						genLDC(new DefString(_namespaceURIs.get(p)));
					}
				}
				break;
			}
			case GET_XPATH:
				if (npar == 2) {
					short typ2 = _tstack[_sp];
					if (typ2 != XD_CONTAINER && typ2 != XD_ELEMENT) {
						//Parameter type must be XML Node
						_parser.error(XDEF.XDEF113);
					}
					if (par1typ != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
					} else if (par1const >= 0) {
						if (optimizeXPath(npar, code)) {
							return;
						}
						try {
							DefXPathExpr xp = new DefXPathExpr(
								getCodeItem(par1const).toString(),
								getXDNamespaceContext(), null, null);
							xp.setCode(LD_CONST);
							_code.set(par1const, xp);
							_cstack[_sp-1] = -1;
						} catch (SRuntimeException ex) {
							_parser.putReport(ex.getReport());
						}
					}
					break;
				}
			case GET_XPATH_FROM_SOURCE:
				if (npar == 1 || npar == 2) {
					if (_tstack[_sp] != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
					} else if (optimizeXPath(npar, code)) {
						return;
					}
					if (npar == 2) {
						if (par1typ != XD_ELEMENT) {
							//Value of type '&{0}' expected
							_parser.error(XDEF.XDEF423, "Element");
						}
						if (_cstack[_sp] == _lastCodeIndex) {
							// COMPILE_XPATH -> vynechat
							String s = getCodeItem(_lastCodeIndex).toString();
							DefXPathExpr xp = new DefXPathExpr(s,
								getXDNamespaceContext(), null, null);
							if (s.indexOf(':') < 0) {
								xp.setCode(LD_CONST_I);
							}
							_code.set(_lastCodeIndex, xp);
							_tstack[_sp] = XD_XPATH;
							_cstack[_sp] = -1;
						}
						operator = new CodeI1(resultType, code, npar);
						addCode(operator, -1); //npar-1
						return;
					}
				}
				break;
			case GET_XQUERY_FROM_SOURCE:
				if (npar == 1 || npar == 2) {
					int cnstLast = _cstack[_sp];
					if (_tstack[_sp] != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
					} else if (cnstLast >= 0) {
						try {
							_code.set(cnstLast, new DefXQueryExpr(
								getCodeItem(cnstLast).toString()));
						} catch (SRuntimeException ex) {
							_parser.putReport(ex.getReport());
						}
					}
					if (npar == 2) {
						if (par1typ != XD_ELEMENT) {
							//Value of type '&{0}' expected
							_parser.error(XDEF.XDEF423, "Element");
						}
						operator = new CodeI1(resultType, code, npar);
						npar--;
						addCode(operator, -npar);
						return;
					}
				}
				break;
			case GET_NS:
				if (npar == 2) {
					if (par1typ != XD_ELEMENT) {
						_parser.error(XDEF.XDEF467); //Incorrect parameter type
					}
					topToString();
				} else if (npar == 1) {
					if (par1typ != XD_ELEMENT) {
						topToString();
					}
				}
				break;
			case SET_ELEMENT:
				if (npar == 1) {
					if (par1typ != XD_CONTAINER && par1typ != XD_ELEMENT) {
						//Parameter type must be 'Context' or 'Element'
						_parser.error(XDEF.XDEF468);
						break;
					}
				}
				break;
			case GET_ELEMENT:
				if (npar > 0) {//getElement(list, index)
					method = getTypeMethod(NOTYPE_VALUE_ID, "#getElement");
					code = method.getCode();
					resultType = method.getResultType();
				}
				break;
			case CONTEXT_GETTEXT:
				if (npar == 0) {//getText()
					method = getTypeMethod(NOTYPE_VALUE_ID, "#getValue");
					code = method.getCode();
					resultType = method.getResultType();
				}
				break;
			case TO_STRING: //NOTE this case item continues (without break)
				if (npar == 2) {
					topToString();
					switch (_tstack[_sp - 1]) {
						case XD_INT:
							code = INTEGER_FORMAT;
							method.getParamTypes()[0] = INTEGER_FORMAT;
							break;
						case XD_FLOAT:
							code = FLOAT_FORMAT;
							break;
						case XD_DATETIME:
							code = DATE_FORMAT;
							if(_cstack[_sp] >= 0) {
								Report r = StringParser.checkDateFormat(
									getCodeItem(_cstack[_sp]).toString());
								if (r != null) {
									_parser.error(r.getMsgID(),
										r.getText(), r.getModification());
								}
							}
							break;
						case XD_ANY:
							break;
						default:
							//Format mask supported only for numbers or dates
							_parser.error(XDEF.XDEF469);
					}
					method = genInternalMethod(code, XD_STRING,
						method.getRestrictions(),
						2, 2, //min. number and max. number of parameters
						_tstack[_sp - 1], // first parameter type
						XD_STRING); // second parameter type
					break;
				}
				if (npar != 1) {
					if (npar == 0) {
						_parser.error(XDEF.XDEF459); //Parameter is missing
						return;
					}
				}
				topToString();
				return;
			case TO_FLOAT:
			case TO_MILLIS:
				if (npar != 1) {
					if (npar == 0) {
						_parser.error(XDEF.XDEF459); //Parameter is missing
						return;
					}
				}
				if (code == TO_FLOAT) {
					topToFloat();
				} else {
					topToMillis();
				}
				return;
			case OUTLN_STREAM:
			case OUT_STREAM:
				if (npar == 1) {
					topToString();
				}
				break;
			case OUTLN1_STREAM:
			case OUT1_STREAM:
				if (npar == 2) {
					topToString();
				}
				break;
			case PRINTF_STREAM: {
				int maskPar;
				if (_tstack[_sp - npar + 1] == XD_OUTPUT) {
					if (npar == 2) {
						//More parameters required for method &{0}
						_parser.error(XDEF.XDEF460, "printf");
						return;
					}
					maskPar = _sp - npar + 2;
				} else {
					maskPar = _sp - npar + 1;
				}
				if (_tstack[maskPar] == XD_LOCALE) {
					maskPar++;
				}
				if (_tstack[maskPar] != XD_STRING) {
					//Value of type '&{0}' expected
					_parser.error(XDEF.XDEF423,"String");
					return;
				}
				break;
			}
			case FORMAT_STRING: {
					int maskPar = _sp - npar + 1;
					if (_tstack[_sp - npar + 1] == XD_LOCALE) {
						maskPar++;
					}
					if (_tstack[maskPar] != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
						return;
					}
					break;
				}
			case PUT_ERROR:
				if (npar > 0) {
					if (_tstack[_sp + 1 - npar] == XD_OUTPUT) { //first param
						if (npar < 2) {
							//More parameters required for method &{0}
							_parser.error(XDEF.XDEF460, name);
							return;
						} else if (npar > 4) {
							//Too many parameters for method &{0}
							_parser.error(XDEF.XDEF461, name);
							return;
						}
						method = getTypeMethod(XD_OUTPUT, "error");
						code = PUT_ERROR1;
					} else {
						if (npar > 1 && _tstack[_sp] == XD_REPORT) {
							//Too many parameters for method &{0}
							_parser.error(XDEF.XDEF461, name);
							return;
						}
						if (npar > 3) {
							//Too many parameters for method &{0}
							_parser.error(XDEF.XDEF461, name);
							return;
						}
					}
				}
				break;
			case COMPILE_REGEX:
				if (npar == 1) {
					if (_tstack[_sp] != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
						return;
					}
					if (par1const >= 0) {//constant
						try {
							replaceTop(new DefRegex(
								getCodeItem(par1const).toString()));
						} catch (Exception ex) {
							if (ex instanceof SThrowable) {
								_parser.putReport(((SThrowable)ex).getReport());
							} else {
								//Incorrect regular expression
								_parser.error(XDEF.XDEF650, ex);
							}
						}
						return;
					}
				}
				break;
			case PARSE_FLOAT:
			case PARSE_INT:
			case PARSE_DATE:
				if (numPar != 2) {
					topToString();
					break;
				}
				operandsToString();
				int par2const = _cstack[_sp];
				if (par2const >= 0) {//format mask
					Report r = StringParser.checkDateFormat(
						_code.get(par2const).toString());
					if (r != null) {// incorrect mask
						_parser.error(r.getMsgID(),
							r.getText(), r.getModification());
						return;
					}
				}
				if(par1const >= 0 && par2const >= 0) { //both are literals
					StringParser p = new StringParser(
						_code.get(par1const).toString()); //mask
					if (!p.isDatetime(_code.get(par2const).toString())) {
						_parser.error("XDEF499","Incorrect value of date/time");
						return;
					}
					replaceTwo(new DefDate(p.getParsedSDatetime()));
					return;
				}
				break;
			case GET_ATTR_NAME:
				if((_mode & (TEXT_MODE)) == 0) {
					//Method '&{0}' can't be called here
					_parser.error(XDEF.XDEF475, "&getAttrName");
				}
				break;
			case GET_BNFRULE: {
				if (npar == 2 && //BNFGRAMMAR_VALUE, XD_STRING
					par1typ == XD_BNFGRAMMAR && //BNF grammar)
					_cstack[_sp] >= 0 && _tstack[_sp] == XD_STRING) { //rulename
					XDValue v = getCodeItem(_lastCodeIndex - 1);
					if (v.getCode() == LD_GLOBAL) {
						CompileVariable xv = (CompileVariable)
							_globalVariables.getXVariable(v.getParam());
						v = xv.getValue();
					} else if (v.getCode() != LD_CODE) {
						v = null;
					}
					if (v != null && v instanceof XDBNFGrammar) {
						XDBNFGrammar g = (XDBNFGrammar) v;
						String ruleName = getCodeItem(_cstack[_sp]).toString();
						XDBNFRule r = g.getRule(ruleName);
						if (r == null || r.ruleValue() == null) {
							//BNF rule '&{0}' not exists
							_parser.error(XDEF.XDEF108, ruleName);
						}
					}
				}
				break;
			}
			case NEW_BNFGRAMAR: {
				if (npar == 1 && par1const >= 0 && par1typ == XD_STRING) {
					//string constant in BNF contructor
					SBuffer source = new SBuffer(
						getCodeItem(par1const).stringValue(), _parser);
					DefBNFGrammar dd;
					try {
						dd = new DefBNFGrammar(null,
							-1, source, _parser.getReportWriter());
					} catch (SRuntimeException ex) {
						//error was reported to reporter;
						dd = new DefBNFGrammar();
						dd.setParam(-1);
						dd.setSource(source.getString());
					}
					_code.set(_lastCodeIndex, dd);
					_tstack[_sp] = resultType;
					_cstack[_sp] = _lastCodeIndex; //constant!
					return;
				}
				break;
			}
			case NEW_LOCALE: {
				String lang = getCodeItem(par1const).toString();
				if (npar == 1 && par1const >= 0) {
					replaceTop(new DefLocale(lang));
					_tstack[_sp] = XD_LOCALE;
					_cstack[_sp] = _lastCodeIndex; //constant!
					return;
				} else if (npar == 2 && par1const >= 0 && _cstack[_sp] >= 0) {
					String country = getCodeItem(_cstack[_sp]).toString();
					replaceTwo(new DefLocale(lang, country));
					_tstack[_sp] = XD_LOCALE;
					_cstack[_sp] = _lastCodeIndex; //constant!
					return;
				} else if (npar == 3 && par1const >= 0
					&& _cstack[_sp - 1] >= 0 && _cstack[_sp] >= 0) {
					String country = getCodeItem(_cstack[_sp - 1]).toString();
					String variant = getCodeItem(_cstack[_sp]).toString();
					_code.remove(_lastCodeIndex--);
					_code.remove(_lastCodeIndex--);
					_sp -= 2;
					replaceTop(new DefLocale(lang, country, variant));
					return;
				}
			}
			case SET_NAMEDVALUE: {
				if (npar == 2) {
					if (_tstack[_sp] == XD_CONTAINER) {
						//????
					} else if (_tstack[_sp] != XD_NAMEDVALUE) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "NamedValue");
					}
				} else if (npar == 3) {
					if (_tstack[_sp - 1] != XD_STRING) {
						//Value of type '&{0}' expected
						_parser.error(XDEF.XDEF423, "String");
					}
				}
				break;
			}
			default:
		}
		//check parameters
		if ((method.getRestrictions() & _mode) == 0) {
			_parser.error(XDEF.XDEF472, name);//Method '&{0}' not allowed here
			code = UNDEF_CODE;
			resultType = XD_UNDEF;
		} else if (npar < method.getMinParams()) {
			//More parameters required for method &{0}
			_parser.error(XDEF.XDEF460, name);
			code = UNDEF_CODE;
			resultType = XD_UNDEF;
		} else  if (npar > method.getMaxParams()) {
			if (method.getMaxParams() == 0) {
				//Parameters are not allowed for method '&{0}'
				_parser.error(XDEF.XDEF473, name);
			} else {
				//Too many parameters for method &{0}
				_parser.error(XDEF.XDEF461, name);
			}
			code = UNDEF_CODE;
			resultType = XD_UNDEF;
		} else if (method.getParamTypes() != null) {
			short mtype = XD_ANY;
			for (int i = 0; i < npar; i++) {
				if (i < method.getParamTypes().length) {
					mtype = method.getParamTypes()[i];
				}
				int j = _sp + i + 1 - npar;
				short ptype;
				if (mtype != XD_ANY && mtype != (ptype = _tstack[j])) {
					if (ptype != XD_ANY && ptype != XD_UNDEF) {
						if (mtype == XD_FLOAT &&
							ptype == XD_INT) {
							if (npar - 1 - i == 0) {
								topToFloat();
							} else {
								topXToFloat(npar - 1 - i);
							}
						} else {
							//Incorrect parameter type
							_parser.error(XDEF.XDEF467);
							code = UNDEF_CODE;
							resultType = XD_UNDEF;
							break;
						}
					}
				}
			}
		}
		if (operator == null) {
			operator = new CodeI1(resultType, code, npar);
		}
		if (resultType != XD_VOID) {
			npar--;
		}
		addCode(operator, -npar);
	}

////////////////////////////////////////////////////////////////////////////////
// Methods for generation of initialization code.
////////////////////////////////////////////////////////////////////////////////

	/** Return the code item with the last stop of the initialization code
	 * sequence or return <tt>null</tt>.
	 */
	final CodeI1 getLastStop() {
		//prevents to generate redundant jumps.
		if (_lastCodeIndex != -1 && _initEnd >= 0) {
			if (_lastCodeIndex != _initEnd) {
				return (CodeI1) getCodeItem(_initEnd);
			}
			_code.remove(_lastCodeIndex--);
		}
		return null;
	}

	/** Add initialization code. */
	final void addInitCode(final int actAdr, final CodeI1 lastStop) {
		if (actAdr == _lastCodeIndex + 1) {
			genStop();
		} else if (actAdr < _lastCodeIndex) {
			//gen init section
			if (_init == -1) {
				_init = actAdr + 1;
			} else if (lastStop != null) {
				lastStop._code = JMP_OP; //change it to jump to next addr
				lastStop.setParam(actAdr + 1);
			}
			genStop();
			_initEnd = _lastCodeIndex;
		}
	}
}

/** Object describing a script method. */
final class ScriptMethod {
	/** The offset of method. */
	private int _address;
	/** The List of types of parameters. */
	private final short[] _params;
	/** The type of result of method. */
	private final short _resultType;
	/** The compilation mode of method (the purpose). */
	private final short _mode;
	/** List of code code addresses where method was called. */
	private int[] _postdefs;
	/** Source position where the method was declared. */
	private final SPosition _spos;

	/** Create new instance of ScriptMethod.
	 * @param resultType The result type of method.
	 * @param address The code index of method start.
	 * @param params Array of parameters types.
	 * @param mode The mode of method.
	 * @param spos source position where the method was declared.
	 */
	ScriptMethod(final short resultType,
		final int address,
		final short[] params,
		final short mode,
		final SPosition spos) {
		_resultType = resultType;
		_address = address;
		_params = params;
		_mode = mode;
		_postdefs = null;
		_spos = spos;
	}

	/** Add address of post defined item to the list. */
	final void addPostDef(final int codeIndex) {
		if (_postdefs == null) {
			_postdefs = new int[]{codeIndex};
		} else {
			int[] x = _postdefs;
			int len = x.length;
			_postdefs = new int[len + 1];
			System.arraycopy(x, 0, _postdefs, 0, len);
			_postdefs[len] = codeIndex;
		}
	}

	/** Resolves post-definition of a method (set address to all previous
	 * references).
	 * @param address address of method.
	 * @param g code generator.
	 * @return <tt>false</tt> if address was already set(i.e. error),
	 * otherwise return <tt>true</tt> (i.e. OK).
	 */
	final boolean resolvePostDef(final int address,
		final CompileCode g) {
		if (_address != -1) {
			return false;
		}
		_address = address;
		if (_postdefs != null) {
			for (int pc: _postdefs) {
				((CodeI1) g.getCodeItem(pc)).setParam(address);
			}
			_postdefs = null;
		}
		return true;
	}

	/** Get result type of method. */
	final short getResultType() {return _resultType;}

	/** Get address of method. */
	final int getAddr() {return _address;}

	/** Set address of method.
	 * @param address the address.
	 */
	final void setAddr(final int address) {_address = address;}

	/** Get array  with the list of parameter types. */
	final short[] getParams() {return _params;}

	/** Get address of method. */
	final short getMode() {return _mode;}

	/** Clear post-definition info. */
	final void clearPostdefs() {_postdefs = null;}

	/** Get source position where the method was declared.
	 * @return source position where the method was declared.
	 */
	final SPosition getSourcePosition() {return _spos;}

	@Override
	public final String toString() {
		String result = CompileBase.getTypeName(_resultType) + " x(";
		if (_params != null) {
			for (short par : _params) {
				if (!result.endsWith("x(")) {
					result += ", ";
				}
				result += CompileBase.getTypeName(par);
			}
		}
		return result + ')';
	}
}

/** Object containing the external method. */
class ExternalMethod {

	/** Name of method (may be alias). */
	private final String _name;
	/** External method. */
	private final Method _method;
	/** Parameters of method. */
	private final Class<?>[] _mparams;

	/** Constructor. */
	ExternalMethod(final String name, final Method method) {
		_method = method;
		_name  = name;
		_mparams = _method.getParameterTypes();
	}

	/** Check if this is a method. */
	final boolean isMethod(final String name, final Class<?>[] params) {
		if (!_name.equals(name)) {
			return false;
		}
		if (_mparams.length != params.length) {
			return false;
		}
		for (int i = 0; i < _mparams.length; i++) {
			Class<?> x = _mparams[i];
			Class<?> y = params[i];
			if (y.equals(Long.TYPE) &&
					(x.equals(Long.TYPE) || x.equals(Long.class)
					|| x.equals(Integer.TYPE) || x.equals(Integer.class)
					|| x.equals(Short.TYPE) || x.equals(Short.class)
					|| x.equals(Byte.TYPE) || x.equals(Byte.class))
				|| y.equals(Double.TYPE) &&
					(x.equals(Double.TYPE) || x.equals(Double.class)
					|| x.equals(Float.TYPE) || x.equals(Float.class))
				|| y.equals(Boolean.TYPE) &&
					(x.equals(Boolean.TYPE) || x.equals(Boolean.class))) {
				continue;
			}
			if (!x.equals(y)) {
				return false;
			}
		}
		return true;
	}

	/** Get external method.
	 * @return Java external method object.
	 */
	final Method getMethod() { return _method; }

	@Override
	public final String toString() {
		return _name + "; " + _method;
	}
}