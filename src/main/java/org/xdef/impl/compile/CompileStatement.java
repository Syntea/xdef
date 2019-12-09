package org.xdef.impl.compile;

import org.xdef.impl.code.CodeSWTableInt;
import org.xdef.impl.code.CodeXD;
import org.xdef.impl.code.CodeSWTableStr;
import org.xdef.impl.code.CodeOp;
import org.xdef.impl.code.CodeExtMethod;
import org.xdef.impl.code.CodeI2;
import org.xdef.impl.code.CodeI1;
import org.xdef.impl.code.CodeTable;
import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.XVariable;
import org.xdef.impl.code.CodeS1;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.xdef.impl.XConstants;

/** Compiler of statements in script.
 * @author Vaclav Trojan
 */
class CompileStatement extends XScriptParser implements CodeTable {
	/** Operators level 1. */
	private static final String OP_L1 =
		new String(new char[]{MUL_SYM, DIV_SYM, MOD_SYM});
	/** Operators level 2. */
	private static final String OP_L2 =
		new String(new char[]{PLUS_SYM, MINUS_SYM});
	/** Operators level 3. */
	private static final String OP_L3 =
		new String(new char[]{EQ_SYM, NE_SYM, LE_SYM, GE_SYM,
		 LT_SYM, GT_SYM, LSH_SYM, RSH_SYM, RRSH_SYM,});
	/** Operators level 4. */
	private static final String OP_L4 =
		new String(new char[]{AND_SYM, AAND_SYM});
	/** Operators level 5. */
	private static final String OP_L5 =
		new String(new char[]{OR_SYM, XOR_SYM, OOR_SYM});
	/** Unary operators. */
	private static final String OP_UNARY =
		new String(new char[]{NEG_SYM, NOT_SYM, PLUS_SYM, MINUS_SYM});
	/** Assignment operators */
	private static final String OP_ASSGN =
		new String(new char[]{ASSGN_SYM, LSH_EQ_SYM, RSH_EQ_SYM, RRSH_EQ_SYM,
		 MUL_EQ_SYM, DIV_EQ_SYM, MOD_EQ_SYM, AND_EQ_SYM, PLUS_EQ_SYM,
		 MINUS_EQ_SYM, OR_EQ_SYM, XOR_EQ_SYM});

	/** Code generator */
	final CompileCode _g;
	/** Saved block information. */
	private BlockInfo _blockInfo;
	/** true if break command was generated. */
	private boolean _wasBreak;
	/** true if continue command was generated. */
	private boolean _wasContinue;
	/** true if return command was generated. */
	private boolean _wasReturn;
	/** the result type of return value if statements are part of method. */
	private short _returnType;
	/** number of parameters to be popped from stack if statements are part of
	 * method. -1 if statements are part of "direct" code of the action. */
	private int _popNumParams;
	/** Actual catch address */
	private CodeI1 _catchItem;
	/** The ClassLoader to load Java classes. */
	private ClassLoader _classLoader;
	/** List of implements and uses requests */
	final ArrayList<CompileReference> _implList =
		new ArrayList<CompileReference>();

	/** Creates a new instance of CommandCompiler
	 * @param g The code generator.
	 * @param xmlVersion 10 -> ""1.0, 11 -> "1.1".
	 * @param mode The compilation mode.
	 * @param nsPrefixes array with name space prefixes.
	 * @param classLoader The Class loader (used for external objects).
	 */
	CompileStatement(final CompileCode g,
		final byte xmlVersion,
		final byte mode,
		final Map<String, Integer> nsPrefixes,
		final ClassLoader classLoader) {
		super(xmlVersion);
		_g = g;
		_g._nsPrefixes = nsPrefixes;
		_g._sp = -1;
		_g._mode = mode;
		setParser();
		_g.clearLocalVariables();
		initCompilation(mode, XD_VOID);
		_wasContinue = false;
		_wasBreak = false;
		_wasReturn = false;
		_returnType = XD_VOID;
		_popNumParams = -1;
		_classLoader = classLoader;
	}

	final void setParser() {_g.setParser(this);}

	/** Set source end position to debug info item;
	 * @param index index of debug info item.
	 */
	void setDebugEndPosition(final int index) {
		if (_g._debugInfo == null || index < 0) return;
		_g._debugInfo.setEndPosition(index, getLastPosition());
	}

	/** Add debug information.
	 * @param varTable is true the table of variables is added.
	 * @return index of debug info item or -1.
	 */
	int addDebugInfo(final boolean varTable) {
		if (_g._debugInfo == null) return -1;
		XVariable[] vtab = null;
		if (varTable && _g._localVariables != null
			&& !_g._localVariables.isEmpty()) {
			vtab = new XVariable[_g._localVariables.size()];
			_g._localVariables.values().toArray(vtab);
		}
		return _g._debugInfo.addInfo(getLastPosition(),
			_actDefName, _g._lastCodeIndex + 1, vtab);
	}

	/** Set source buffer with code to be compiled.
	 * @param source buffer wit source code.
	 * @param actDefName name of actually processed X-definition.
	 * @param nsPrefixes table with prefixes and name spaces.
	 */
	final void setSource(final SBuffer source,
		final String actDefName,
		final byte xdVersion,
		final Map<String, Integer> nsPrefixes) {
		super.setSource(source, actDefName, xdVersion);
		_g._nsPrefixes = nsPrefixes;
	}

////////////////////////////////////////////////////////////////////////////////
// Compilation of expression.
////////////////////////////////////////////////////////////////////////////////

	/** Compile expression and make value from jump vector (if not empty).
	 * @return false if no expression was compiled.
	 */
	private boolean compileValue() {
		CompileJumpVector jv;
		if ((jv = expr()) == null) {
			return false;
		} else {
			_g.jumpVectorToBoolValue(jv);
			return true;
		}
	}

	/** Compile named value.
	 * NamedValue ::= "%" XmlName "=" Expression
	 * @return false if nothing was compiled.
	 */
	private boolean namedValueConstructor(final ParamList plist) {
		String name;
		if (!isXMLName(_xmlVersion)) {
			errorAndSkip(XDEF.XDEF106, //Keyword parameter expected
				new String(new char[] {RPAR_SYM,
				SEMICOLON_SYM, BEG_SYM, END_SYM, COMMA_SYM, ASSGN_SYM}));
			if (_sym != ASSGN_SYM) {
				return false;
			}
			name = "_UNDEF_";
		} else {
			name = getParsedString();
		}
		if (nextSymbol() != ASSGN_SYM) {
			error(XDEF.XDEF410, "="); //'&{0}' expected
		} else {
			nextSymbol();
		}
		_g.genLDC(new DefString(name));
		int sp = _g._sp;
		if (readParam()) {
			XDValue item =_g.removeLastCodeItem();
			if (plist != null) {
				plist.addKeyPar(name, item.getItemId(), item);
			}
			_g.replaceLastCodeItem(new DefNamedValue(name,item));
			_g._tstack[--_g._sp] = XD_NAMEDVALUE;
			return true;
		} else {
			if (sp + 1 == _g._sp) {
				if (plist != null) {
					plist.addKeyPar(name, _g._tstack[_g._sp], null);
				}
				_g.addCode(new CodeOp(XD_NAMEDVALUE, NEW_NAMEDVALUE), -1);
			}
			return false;
		}
	}

	/** Read value of method parameter and return true if it was a constant.
	 * @return <tt>true</tt> if and only if the parameter is a constant.
	 */
	private boolean readParam() {
		int sp = _g._sp;
		int lastCodeIndex = _g._lastCodeIndex;
		compileValue();
		if (sp == _g._sp) {
			error(XDEF.XDEF434); //Parameter value expected
			return false;
		}
		return lastCodeIndex + 1 == _g._lastCodeIndex && _g._cstack[_g._sp]>=0;
	}

	/** Read list of parameters.
	 * ParameterList ::= ( (KeyParam | Parameters) (S? "," S? KeyParam)* )?
	 * Parameters ::= ("$ChkElement" | Expression) ( S? "," S? Expression )*
	 * KeyParam ::= "%" S? Identifier S? "=" S? KeyValue
	 * KeyValue ::= "(" S? Expression ( S? "," S? Expression )* S? ")"
	 * | Expression
	 * @param name of method.
	 * @return number of parameters, - 1 if method was generated,
	 */
	private ParamList paramList(final String name) {
		ParamList plist = new ParamList();
		nextSymbol();
		if (_sym == RPAR_SYM) { // ')' => empty parameter list
			nextSymbol();
			return plist;
		}
		int sp = _g._sp;
		while (true) {
			if (_sym == MOD_SYM) { //keyword params
				int sp1 = _g._sp;
				int lastCodeIndex = _g._lastCodeIndex;
				int spMax = _g._spMax;
				boolean allConstants = true;
				do {
					if (!namedValueConstructor(plist)) {
						allConstants = false;
					}
					if (_sym == COMMA_SYM) {
						nextSymbol();
						if (_sym != MOD_SYM) {
							error(XDEF.XDEF106); //Keyword parameter expected
						}
					}
				} while(_sym == MOD_SYM);
				int np = _g._sp - sp1;
				if (allConstants) {
					XDValue[] list = new XDValue[np];
					for (int i=0; i < np; i++) {
						list[i] = _g.getCodeItem(lastCodeIndex + i + 1);
					}
					_g.removeCodeFromIndexAndClearStack(lastCodeIndex,
						sp1, spMax);
					_g.genLDC(new DefContainer(list));
				} else {
					_g.genConstructor(XD_CONTAINER, np);
				}
				break;
			} else {
				CompileBase.InternalMethod m;
				String[] sqn;
				// asterisk as maxLength
				if (_sym == MUL_SYM && _g._sp - sp == 1
					&& _g._tstack[_g._sp] == XD_INT
					&& (m = CompileCode.getTypeMethod(
						CompileBase.NOTYPE_VALUE_ID,name)) != null
					&& m.getResultType() == XD_PARSER
					&& m.getParsedResult() == XD_STRING
					&& (sqn = m.getSqParamNames()) != null
					&& sqn.length >= 2 && "maxLength".equals(sqn[1])) {
					nextSymbol();
					_g.addCode(new DefLong(Long.MAX_VALUE));
					_g._tstack[++_g._sp] = XD_INT;
					_g._cstack[_g._sp] = _g._lastCodeIndex;
				} else {
					readParam();
				}
			}
			if (_sym != COMMA_SYM) { // ','
				break;
			}
			nextSymbol();
		}
		checkSymbol(RPAR_SYM); // ')' end of parameter list
		plist.setNumpars(_g._sp - sp); //number of parameters
		return plist;
	}

	/** Parse and compile method reference.
	 * methodName ::= scriptIdentifier
	 * method ::= methodName (s? parameterList)?
	 * parameterList ::= "(" s? ( expression ( s? "," s? expression )* )? s? ")"
	 * @param name Name of method
	 * @param spos source position where the method was declared.
	 * @return true if method was parsed.
	 */
	private boolean method(final String name, final SPosition spos) {
		if (_sym != LPAR_SYM) {
			return false;
		}
		int numPar;
		if (!_g._debugMode && ("trace".equals(name) || "pause".equals(name))) {
			int lastCodeIndex = _g._lastCodeIndex;
			int sp = _g._sp;
			int spMax = _g._spMax;
			paramList(name);
			_g.removeCodeFromIndexAndClearStack(lastCodeIndex, sp, spMax);
			return true;
		} else {
			numPar = paramList(name).getNumpars();
			CompileVariable var;
			if (numPar == 0 && (var =_g.getVariable(name)) != null
				&& var.getType() == XD_PARSER) {
				_g.genLD(name);
				_g.addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1), 0);
			} else {
				String err = _g.genMethod(name, numPar);
				if (err != null) {
					error(XDEF.XDEF443, err); //Unknown method: '&{0}'
				}
			}
			if ("ListOf".equals(name)) {
				warning(XDEF.XDEF998,name, "list(type)");
			}
		}
		classMethod();
		return true;
	}

	/** parse class method. */
	private void classMethod() {
		while (_sym == DOT_SYM) { // TODO - will be separate method
			if (_g._sp < 0) {
				error(XDEF.XDEF438); //Value expected
				//skip identifiers followed by dots
				while(nextSymbol()==IDENTIFIER_SYM && nextSymbol()==DOT_SYM){}
				return;
			}
			short xType;
			if ((xType =_g._tstack[_g._sp]) == XD_INT
				|| xType == XD_FLOAT) {
				error(XDEF.XDEF216); //Unexpected character
				nextSymbol();
				return;
			}
			if (nextSymbol() != IDENTIFIER_SYM) {
				error(XDEF.XDEF417); //Method name expected
			}
			String methodName = _idName;
			nextSymbol();
			if (_sym == LPAR_SYM) { // '(' => method
				int numPar = paramList(methodName).getNumpars();
				if (!_g.genClassMethod(methodName, numPar)) {
					error(XDEF.XDEF443, methodName); //Unknown method: '&{0}'
				}
			} else {
				error(XDEF.XDEF410, "("); //'&{0}' expected
			}
		}
	}

	/** Generate inc/dec operation.
	 * @param op INC_SYM or DEC_SYM.
	 * @param name The name of variable.
	 * @param mode 0..not stack duplicate, 1..prefix (++i), 2..postfix (i++).
	 */
	private void genInc(final char op, final String name, final int mode) {
		CompileVariable var = getVariableAndErr(name);
		if (var != null) {
			_g.genLD(name);
			if (mode == 2) { // e.g. i++
				_g.genDup();
			}
			short itemType;
			if ((itemType = var.getType()) == XD_INT) {
				_g.addCode(new CodeOp(XD_INT,
					op==INC_SYM ? INC_I : DEC_I), 0);
			} else if (itemType == XD_FLOAT) {
				_g.addCode(new CodeOp(XD_FLOAT,
					op==INC_SYM ? INC_R : DEC_R), 0);
			} else {
				if (itemType != CompileBase.XD_UNDEF) {
					//Variable of type 'int' or 'float' expected
					error(XDEF.XDEF435);
				}
				return;
			}
			if (mode == 1) { // e.g. ++i
				_g.genDup();
			}
			_g.genST(name);
		} else {
			_g.setUnDefItem();
		}
	}

	/** Process external fully qualified external method.
	 * @param className
	 */
	private boolean qualifiedMethod(String name, boolean vLoaded) {
		String methodName;
		String cName;
		SPosition spos;
		if (_sym == DOT_SYM) {
			nextSymbol();
			if (_sym != IDENTIFIER_SYM) {
				error(XDEF.XDEF416); //Identifier expected
				_g.setUnDefItem();
				return false;
			}
			cName = name;
			methodName = _idName;
			spos = getLastPosition();
			nextSymbol();
		} else {
			spos = getLastPosition();
			int i;
			if ((i = name.lastIndexOf('.')) <= 0) {
				if (!_g._ignoreUnresolvedExternals) {
					error(XDEF.XDEF424, name); //Undefined variable '&{0}'
				}
				_g.setUnDefItem();
				return false;
			}
			cName = name.substring(0, i);
			methodName = name.substring(i + 1);
		}
		int numPar = _sym == LPAR_SYM ? paramList(methodName).getNumpars() : 0;
		short xType, code;
		CompileVariable var;
		boolean loaded = vLoaded || _g.genLD(cName);
		if (loaded) {
			xType = _g._tstack[_g._sp - numPar];
			short yType = -1;
			if (numPar==1) {
				if ((xType!=CompileBase.UNIQUESET_VALUE
					&& xType!=CompileBase.UNIQUESET_M_VALUE)
					|| ((yType=_g._tstack[_g._sp])!=CompileBase.PARSEITEM_VALUE
					&& yType!=XD_PARSER && yType!=XD_PARSERESULT)) {
					yType = -1;
				}
			}
			code = "ID".equals(methodName) ? UNIQUESET_ID
				: "IDREF".equals(methodName) ? UNIQUESET_IDREF
				: "CHKID".equals(methodName) ? UNIQUESET_CHKID
				: "IDREFS".equals(methodName) ? UNIQUESET_IDREFS
				: "CHKIDS".equals(methodName) ? UNIQUESET_CHKIDS
				: "SET".equals(methodName) ? UNIQUESET_SET : -1;
			if (numPar<=1 && CompileBase.UNIQUESET_VALUE==xType && code >=0) {
				if (yType != -1) {
					_g.genPop();
				}
				_g.addCode(new CodeI2(XD_PARSERESULT, code, 0,
					getVariableAndErr(cName).getParseMethodAddr()));
				_g._tstack[_g._sp] = XD_PARSERESULT;
				return true;
			} else if (numPar<=1 && CompileBase.UNIQUESET_M_VALUE==xType
				&& code < 0
				&& (var = getVariable(cName + "." + methodName)) != null
					&& var.getType() == CompileBase.UNIQUESET_KEY_VALUE) {
				if (yType != -1) {
					_g.genPop();
				}
				_g.addCode(new CodeI2(XD_VOID, UNIQUESET_KEY_SETKEY,
					var.getValue().intValue(), var.getParseMethodAddr()));
				_g._tstack[_g._sp] = CompileBase.XD_PARSERESULT;
				return true;
			} else if (_g.genClassMethod(methodName, numPar)) {
				return true;
			}
			error(spos, XDEF.XDEF443, methodName); //Unknown method: '&{0}'
			return false;
		}
		try {
			Class<?> clazz = Class.forName(name, false, _classLoader);
			CodeExtMethod method =
				_g.findExternalMethod(methodName, numPar, clazz, null);
			if (method._resultType != XD_VOID) {
				numPar--;
			}
			_g.addCode(method,	-numPar);
			return true;
		} catch (Exception ex) {
			int ndx;
			if (numPar <= 1 && (ndx = cName.lastIndexOf('.')) > 0
				&& (code = "ID".equals(methodName) ? UNIQUESET_KEY_ID
					: "IDREF".equals(methodName) ? UNIQUESET_KEY_IDREF
					: "CHKID".equals(methodName) ? UNIQUESET_KEY_CHKID
					: "IDREFS".equals(methodName) ? UNIQUESET_IDREFS
					: "CHKIDS".equals(methodName) ? UNIQUESET_CHKIDS
					: "NEWKEY".equals(methodName) ? UNIQUESET_KEY_NEWKEY
					: "SET".equals(methodName) ? UNIQUESET_KEY_SET : -1) >= 0
				&& (var = getVariable(cName.substring(0, ndx))) != null
				&& ((xType = var.getType()) == CompileBase.UNIQUESET_VALUE
					|| xType == CompileBase.UNIQUESET_M_VALUE
					&& (var = getVariable(cName)) != null
					&& var.getType() == CompileBase.UNIQUESET_KEY_VALUE)) {
				if (numPar==1) {
					short yType;
					if ((yType=_g._tstack[_g._sp])!=XD_PARSER
						&& yType!=CompileBase.PARSEITEM_VALUE
						&& yType!=XD_PARSERESULT&&yType!=XD_BOOLEAN) {
						error(spos, XDEF.XDEF467); //Incorrect parameter type
						return false;
					} else {
						_g.genPop();
					}
				}
				_g.genLD(cName.substring(0, ndx));
				_g.addCode(new CodeI2(XD_VOID,
					code, var.getValue().intValue(), var.getParseMethodAddr()));
				if (code == UNIQUESET_KEY_NEWKEY) {
					_g._tstack[_g._sp--] = -1; //KEY_NEWKEY is void
				} else {
					_g._tstack[_g._sp] = CompileBase.XD_PARSERESULT;
				}
				return true;
			}
			if (_g._ignoreUnresolvedExternals) {
				_g.setUnDefItem();
				return true;
			}
			//Unknown method: '&{0}'
			error(spos, XDEF.XDEF443, cName+'.'+methodName);
			return false;
		}
	}

	/** Parse and compile factor.
	 * Factor ::= ( Cast )? ( UnaryOperator )? ( "(" Expression ")" | Value )
	 * Cast ::= "(" TypeName ")"
	 * UnaryOperator ::= ( "+" | "-" | "!" | "~" )
	 * Value ::= Constant | Method | Constructor | AttrReference |
	 *   Identifier ( "++" | "--" )?
	 * Constructor :: = "new" TypeName "(" ParameterList ")" |
	 *   "[" ContextValues? "]" | NamedValue
	 * ContextValues ::= Value ("," Value)*
	 * NamedValue ::= "%" QName "=" Value
	 * Method ::= MethodName "(" ParameterList ")"
	 * AttrReference ::= "@" QName
	 * ParameterList ::= ( "$ChkElement" | Expression ) ( "," Expression )*
	 *
	 * @return true if factor was parsed.
	 */
	private boolean factor() {
		char unaryoperator;
		if (OP_UNARY.indexOf(_sym) >= 0) { // PLUS, MINUS, NOT, NEG (binary)
			unaryoperator = _sym;
			if (unaryoperator == MINUS_SYM) {
				_unaryMinus = true;
			}
			nextSymbol();
			if (unaryoperator == MINUS_SYM) {
				if (!_unaryMinus) {
					unaryoperator = NOCHAR;
				} else {
					_unaryMinus = false;
				}
			}
		} else {
			unaryoperator = NOCHAR;
		}
		switch (_sym) {
			case LPAR_SYM: {// '('
				nextSymbol();
				short castRequest;
				if (_sym == IDENTIFIER_SYM
					&& (castRequest = CompileBase.getTypeId(_idName)) >= 0) {
					//type casting
					checkNextSymbol(RPAR_SYM);
					if (factor()) {
						switch (castRequest) {
							case XD_BOOLEAN:
								if (_g._tstack[_g._sp] == XD_PARSERESULT) {
									_g.topToBool(); // force conversion!
									break;
								}
							case XD_PARSER:
							case XD_STRING:
							case XD_DECIMAL:
							case XD_INT:
							case XD_FLOAT:
							case XD_CONTAINER:
							case XD_ELEMENT:
							case XD_ANY: {
								_g.convertTopToType(castRequest);
								break;
							}
							default:
								//Incorrect cast request: &{0}
								error(XDEF.XDEF474, _idName);
						}
						return true;
					}
					error(XDEF.XDEF437); //Error in expression
				} else {
					if (!compileValue()) {
						error(XDEF.XDEF437); //Error in expression
					}
					checkSymbol(RPAR_SYM);
				}
				break;
			}
			case NEW_SYM: {// new
				if (nextSymbol() == IDENTIFIER_SYM) {
					String name;
					short type = CompileBase.getTypeId(name = _idName);
					if (type <= 0) {
						error(XDEF.XDEF480, name);//Unknown constructor: '&{0}'
						type = CompileBase.XD_UNDEF;
					}
					if (nextSymbol() == LPAR_SYM) { // '('
						if (!_g.genConstructor(type,
							paramList(name).getNumpars())) {
							//Unknown constructor: '&{0}'
							error(XDEF.XDEF480, name);
						}
					} else {
						error(XDEF.XDEF410, "("); //'&{0}' expected
					}
				}
				break;
			}
			case LSQ_SYM: {// '[' -> Context constructor
				nextSymbol();
				int sp = _g._sp;
				int lastCodeIndex = _g._lastCodeIndex;
				int spMax = _g._spMax + 1;
				boolean allConstants = true;
				while (_sym != RSQ_SYM) { // ']'
					if (_sym == MOD_SYM) { // named param
						if (!namedValueConstructor(null)) {
							allConstants = false;
						}
					} else if (!readParam()) {
						allConstants = false;
					}
					if (_sym != COMMA_SYM) { // ','
						break;
					}
					nextSymbol();
				}
				checkSymbol(RSQ_SYM); // ']' end list
				int np = _g._sp - sp;
				if (allConstants) { // Generate constant
					XDValue[] list = new XDValue[np];
					for (int i=0; i < np; i++) {
						list[i] = _g.getCodeItem(lastCodeIndex + i + 1);
					}
					_g.removeCodeFromIndexAndClearStack(lastCodeIndex,sp,spMax);
					_g.genLDC(new DefContainer(list));
				} else { // Generate constructor
					_g.genConstructor(XD_CONTAINER, np);
				}
				break;
			}
			case MOD_SYM: // NamedValue constructor
				namedValueConstructor(null);
				break;
			case CONSTANT_SYM: // constant literal
				_g.genLDC(_parsedValue); // addCode(_parsedValue, 1);
				nextSymbol();
				break;
			case NULL_SYM: // null
				_g.genLDC(new DefNull());
				nextSymbol();
				break;
			case IDENTIFIER_SYM: {
				String name = _idName;
				SPosition spos = getLastPosition();
				if (LPAR_SYM == nextSymbol()) { // '(' => method
					method(name, spos);
				} else if (_sym == INC_SYM || _sym == DEC_SYM) { // "++" | "--"
					genInc(_sym, name, 2);
					nextSymbol();
				} else if (!assignment(name, true)) {
					CompileVariable var = _g.getVariable(name);
					XDValue x;
					if (var != null
						&& var.getType() == CompileBase.UNIQUESET_NAMED_VALUE) {
						_g.genLD(var);
						_g.addCode(new CodeS1(var.getParseResultType(),
							UNIQUESET_GETVALUEX, var.getValue().toString()), 0);
					// this is very nasty code. If the variable refers to a
					// declared type and the parser is constant we use the
					// code with the parser as a value (and it is constant).
					// TODO if it is not a constant.
					} else if (var != null
						&& var.getType()==CompileBase.PARSEITEM_VALUE
						&& var.getParseMethodAddr() >= 0 // parse method exists
//						&& var.getValue() == null
						&& (x=_g._code.get(var.getParseMethodAddr()))
							.getCode() == 0 // constant
						&& x.getItemId() == XD_PARSER
						&& _g._code.get(var.getParseMethodAddr() + 1)
							.getCode() == PARSE_OP
						&& _g._code.get(var.getParseMethodAddr() + 2)
							.getCode() == STOP_OP) { // declared type
						_g.addCode(x, 1);
						_g._tstack[_g._sp] = XD_PARSER; // it must be parser!
						_g._cstack[_g._sp] = var.getParseMethodAddr();//constant
					} else if (var == null || !_g.genLD(var)) {
						int xsp = _g._sp;
						if (_g.scriptMethod(name, 0)
							|| _g.internalMethod(name, 0)
							|| _g.externalMethod(name, name, 0)) {
							if (xsp == _g._sp) { // method without parameters
								error(XDEF.XDEF438); //Value expected
							}
						} else {
							qualifiedMethod(name, false);
						}
					} else if (_sym == DOT_SYM) {
						qualifiedMethod(name, true);
					}
				}
				break;
			}
			case INC_SYM: // "++"
			case DEC_SYM: { // "--"
				char op = _sym;
				nextSymbol();
				if (_sym != IDENTIFIER_SYM) {
					error(XDEF.XDEF436); //Variable expected
				} else {
					genInc(op, _idName, 1);
					nextSymbol();
				}
				break;
			}
			case ATCHAR_SYM: { // attribute reference
				int i;
				String s;
				if ((i = _idName.indexOf(':')) >= 0) {
					s = _idName.substring(0, i);
					Integer p = _g._nsPrefixes.get(s);
					if (p != null) {
						s = _g._namespaceURIs.get(p);
						s = '{' + s + '}' +_idName.substring(i + 1);
					} else {
						//Namespace for prefix '&{0}' is undefined
						error(XDEF.XDEF257, s);
						s = _idName;
					}
				} else {
					s = _idName;
				}
				_g.genLDAttr(s);
				nextSymbol();
				break;
			}
			default:
				// not value
				if (unaryoperator == NOCHAR) {
					return false;
				}
				error(XDEF.XDEF438); //Value expected
				return true;
		}
		classMethod();
		if (_g._sp < 0) {
			return true; // this prevents internal errors
		}
		short xType = _g._tstack[_g._sp];
		int xValue = _g._cstack[_g._sp];
		switch (unaryoperator) {
			case PLUS_SYM:
			case MINUS_SYM: {
				if (xType != XD_INT && xType != XD_FLOAT
					 && xType != XD_DECIMAL) {
					if (xType != CompileBase.XD_UNDEF) {
						// don't report twice
						 //Value of type int or float expected
						error(XDEF.XDEF439);
					}
					return true;
				}
				if (unaryoperator == MINUS_SYM) { // unary minus (plus is ignored!)
					if (xType == XD_INT) {
						if (xValue >= 0) { // constant
							long i = _g.getCodeItem(xValue).longValue();
							_g._code.set(xValue, new DefLong(- i));
						} else { // not constant
							_g.addCode(new CodeOp(XD_INT, NEG_I), 0);
						}
					} else if (xType == XD_DECIMAL) {
						if (xValue >= 0) { // constant
							BigDecimal d = _g.getCodeItem(xValue).decimalValue();
							d.negate();
							_g.setCodeItem(xValue, new DefDecimal(d));
						} else {
							//Value of type int or float expected
						   error(XDEF.XDEF439);
						}
					} else {
						if (xValue >= 0) { // constant
							double f = _g.getCodeItem(xValue).doubleValue();
							_g._code.set(xValue, new DefDouble(- f));
						} else { // not constant
							_g.addCode(new CodeOp(XD_FLOAT, NEG_R), 0);
						}
					}
				}
				return true;
			}
			case NEG_SYM: {
				if (xType == XD_INT) {
					if (xValue >= 0) { // constant
						long i = _g.getCodeItem(xValue).longValue();
						_g._code.set(xValue, new DefLong(~ i));
					} else { // not constant
						_g.addCode(new CodeOp(XD_BOOLEAN, NEG_BINARY), 0);
					}
				} else {
					if (xType != CompileBase.XD_UNDEF) {
						//Value of type '&{0}' expected
						error(XDEF.XDEF423, "int");
					}
				}
				return true;
			}
			case NOT_SYM: {
				if (xType  == CompileBase.ATTR_REF_VALUE) {
					_g.topToBool();
					xValue = _g._cstack[_g._sp];
				} else if (xType  == XD_PARSERESULT || xType  == XD_PARSER) {
					_g.topToBool();
					xValue = _g._cstack[_g._sp];
				} else if (xType != XD_BOOLEAN) {
					if (xType != CompileBase.XD_UNDEF) {
						//Value of type &{0} expected
						error(XDEF.XDEF423, "boolean");
						xValue = _g._cstack[_g._sp] = -1;
					}
				}
				if (xValue >= 0) {
					boolean b = _g.getCodeItem(xValue).booleanValue();
					_g._code.set(xValue, new DefBoolean(!b));
				} else {
					_g.addCode(new CodeOp(XD_BOOLEAN, NOT_B), 0);
				}
				return true;
			}
			default:
				return true;
		}
	}

	/** Parse and compile term.
	 * multiplication | division || modulo || boolean and
	 * operatorLevel1 ::= ( "*" | "/" | "%" )
	 * term ::= factor ( ( operatorLevel1 factor )*
	 * @return true if term was parsed.
	 */
	private boolean term() {
		int sp = _g._sp;
		if (!factor()) {
			return false;
		}
		boolean firstError = _g._sp <= sp;
		while (OP_L1.indexOf(_sym) >= 0) { // MUL, DIV, MOD
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			sp = _g._sp;
			short xType = _g._tstack[sp];
			int xValue = _g._cstack[sp];
			nextSymbol();
			if (!factor()) {
				error(XDEF.XDEF437); //Error in expression
				continue;
			}
			if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
				if (sp == _g._sp) {
					error(XDEF.XDEF438); //Value expected
				}
				continue;
			}
			short yType = _g._tstack[_g._sp];
			int yValue = _g._cstack[_g._sp];
			if ((operator == MUL_SYM) || (operator == DIV_SYM)
				|| (operator == MOD_SYM)) { // mul | div | modulo
				if (xType == XD_INT && yType == XD_INT) {
					if (xValue >= 0 && yValue >= 0) { // both constants
						long x = _g.getCodeItem(xValue).longValue();
						long y = _g.getCodeItem(yValue).longValue();
						_g.replaceTwo(new DefLong(operator==MUL_SYM
							? x * y : operator==DIV_SYM ? x / y : x % y));
					} else { // not both constants
						_g.addCode(new CodeOp(XD_INT, operator==MUL_SYM
							? MUL_I : operator==DIV_SYM ? DIV_I : MOD_I), -1);
					}
				} else { // not both integer => float
					_g.operandsToFloat();
					yValue = _g._cstack[_g._sp];
					if (xValue >= 0 && yValue >= 0) { // both constants
						double x = _g.getCodeItem(xValue).doubleValue();
						double y = _g.getCodeItem(yValue).doubleValue();
						_g.replaceTwo(new DefDouble(operator==MUL_SYM
							? x * y : operator==DIV_SYM ? x / y : x % y));
					} else { // not both constants
						_g.addCode(new CodeOp(XD_FLOAT, operator==MUL_SYM
							? MUL_R	:operator==DIV_SYM ? DIV_R : MOD_R), -1);
					}
				}
			}
		}
		return true;
	}

	/** Parse and compile simple expression.
	 * add | subtract
	 * operatorLevel2 ::= "+" | "-"
	 * simpleExpression ::= term ( operatorLevel2 term )?
	 * @return true if simple expression was parsed.
	 */
	private boolean simpleExpression() {
		int sp = _g._sp;
		if (!term()) {
			return false;
		}
		boolean firstError = _g._sp <= sp;
		while (OP_L2.indexOf(_sym) >= 0) { // PLUS, MINUS
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			sp = _g._sp;
			short xType = _g._tstack[sp];
			int xValue = _g._cstack[sp];
			nextSymbol();
			if (!term()) {
				error(XDEF.XDEF437); //Error in expression
				continue;
			}
			if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
				if (sp == _g._sp) {
					error(XDEF.XDEF438); //Value expected
				}
				continue;
			}
			short yType = _g._tstack[_g._sp];
			int yValue = _g._cstack[_g._sp];
			if (operator == PLUS_SYM
				&& (xType == XD_STRING || yType == XD_STRING)) {
				// "+" => string concatenation
				_g.operandsToString();
				xValue = _g._cstack[_g._sp - 1];
				yValue = _g._cstack[_g._sp];
				if (xValue >= 0 && yValue >= 0)	{ // constants
					_g.replaceTwo(new DefString(
						_g.getCodeItem(xValue).toString()
						+ _g.getCodeItem(yValue).toString()));
				} else {
					_g.addCode(new CodeOp(XD_STRING, ADD_S), -1);
				}
			} else {// add or subtract numbers
				if (xType == XD_INT && yType == XD_INT) {
					if (xValue >= 0 && yValue >= 0) {
						long x = _g.getCodeItem(xValue).longValue();
						long y = _g.getCodeItem(yValue).longValue();
						_g.replaceTwo(new DefLong(
							operator==PLUS_SYM ? x + y : x - y));
					} else {
						_g.addCode(new CodeOp(XD_INT,
							operator==PLUS_SYM ? ADD_I : SUB_I), -1);
					}
				} else if (xType == XD_FLOAT && yType == XD_FLOAT
					|| xType == XD_FLOAT && yType == XD_INT
					|| xType == XD_INT && yType == XD_FLOAT) {
					_g.operandsToFloat();
					yValue = _g._cstack[_g._sp];
					if (xValue >= 0 && yValue >= 0) {
						double x = _g.getCodeItem(xValue).doubleValue();
						double y = _g.getCodeItem(xValue).doubleValue();
						_g.replaceTwo(new DefDouble(
							operator==PLUS_SYM ? x + y : x - y));
					} else {
						_g.addCode(new CodeOp(XD_FLOAT,
							operator==PLUS_SYM ? ADD_R : SUB_R), -1);
					}
				} else {
					if (!_g._ignoreUnresolvedExternals
						|| (xType != XD_UNDEF && yType != XD_UNDEF)) {
						//Value of type '&{0}' expected
						error(XDEF.XDEF423, "number");
					}
					_g.addCode(new CodeOp(XD_FLOAT, ADD_R), -1);
				}
			}
		}
		return true;
	}

	/** Report if values are incomparable. */
	private void incomparable(final short yType) {
		if (yType != CompileBase.XD_UNDEF) {
			error(XDEF.XDEF444); //Incomparable types of values
		}
	}

	/** Parse and compile relation part of expression.
	 * operatorLevel3 ::= "<" | ">" | "<=" | ">=" | "==" | "!=" | "<<" | ">>" |
	 *   ">>>"
	 * expression ::=
	 *   simpleExpression ( operatorLevel3 simpleExpression )?
	 *
	 * @return true if simple expression was parsed.
	 */
	private boolean relExpression() {
		int sp = _g._sp;
		if (!simpleExpression()) {
			return false;
		}
		boolean firstError = _g._sp <= sp;
		// relational and shift operators:
		while (OP_L3.indexOf(_sym) >= 0) { // <, >, <=, >=, ==, !=, <<, >>, >>>
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			sp = _g._sp;
			short xType = _g._tstack[sp];
			if (xType == CompileBase.ATTR_REF_VALUE) {
				_g.topToString();
				xType = _g._tstack[sp];
			}
			int xValue = _g._cstack[sp];
			nextSymbol();
			if (!simpleExpression()) {
				error(XDEF.XDEF437); //Error in expression
				continue;
			}
			if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
				if (sp == _g._sp) {
					error(XDEF.XDEF438); //Value expected
				}
				continue;
			}
			short yType = _g._tstack[_g._sp];
			if (yType  == CompileBase.ATTR_REF_VALUE) {
				_g.topToString();
				yType = _g._tstack[_g._sp];
			}
			int yValue = _g._cstack[_g._sp];
			if (xValue >= 0 && yValue >= 0) { // both constants, resolve
				int result = 0;
				long i,j;
				double u,v;
				boolean a,b;
				String s,t;
				if (xType == XD_NULL || yType == XD_NULL) {
					// TODO
					incomparable(yType);
					break;
				}
				switch (xType) {
					case XD_INT: {
						i = _g.getCodeItem(xValue).longValue();
						switch (yType) {
							case XD_INT:
								j = _g.getCodeItem(yValue).longValue();
								switch (operator) {
									case LSH_SYM:
										_g.replaceTwo(new DefLong(i << j));
										continue;
									case RSH_SYM:
										_g.replaceTwo(new DefLong(i >> j));
										continue; // while sym is rel. operator
									case RRSH_SYM:
										_g.replaceTwo(new DefLong(i >>> j));
										continue; // while sym is rel. operator
								}
								result = i == j ? 0 : i < j ? - 1 : 1;
								break;
							case XD_FLOAT:
								v = _g.getCodeItem(yValue).doubleValue();
								result = i == v ? 0 : i < v ? - 1 : 1;
								break;
							case CompileBase.XD_UNDEF:
								break;
							case XD_ANY:
								//TODO
							default:
								incomparable(yType);
						}
						break;
					}
					case XD_FLOAT: {
						u = _g.getCodeItem(xValue).doubleValue();
						switch (yType) {
							case XD_INT:
								j = _g.getCodeItem(yValue).longValue();
								result = u == j ? 0 : u < j ? - 1 : 1;
								break;
							case XD_FLOAT:
								v = _g.getCodeItem(yValue).doubleValue();
								result = u == v ? 0 : u < v ? - 1 : 1;
								break;
							case CompileBase.XD_UNDEF:
								break;
							case XD_ANY:
								//TODO
							default:
								incomparable(yType);
						}
						break;
					}
					case XD_BOOLEAN: {
						a = _g.getCodeItem(xValue).booleanValue();
						if (yType == XD_BOOLEAN) {
							b = _g.getCodeItem(yValue).booleanValue();
							result = a == b ? 0 : 1;
							if (operator !=  EQ_SYM && operator !=  NE_SYM) {
								//Values of the type 'boolean' can be compared
								//only for equality
								error(XDEF.XDEF445);
							}
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_STRING: {
						s = _g.getCodeItem(xValue).toString();
						if (yType == XD_STRING) {
							t = _g.getCodeItem(yValue).toString();
							result = s.compareTo(t);
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_DATETIME: {
						switch (yType) {
							case XD_DATETIME:
								try {
									result = _g.getCodeItem(xValue).compareTo(
										_g.getCodeItem(yValue));
								} catch (Exception ex) {
									incomparable(yType);
								}
								break;
							case XD_INT:
								break;
							case CompileBase.XD_UNDEF:
								break;
							case XD_ANY:
								//TODO
							default:
								incomparable(yType);
						}
						break;
					}
					case XD_DURATION: {
						if (yType != XD_DURATION) {
							incomparable(yType);
						} else {
							try {
								result = _g.getCodeItem(xValue).compareTo(
									_g.getCodeItem(yValue));
							} catch (Exception ex) {
								incomparable(yType);
							}
						}
						break;
					}
					case CompileBase.XD_UNDEF:
						break;
					case XD_ANY:
						//TODO
					default:
						incomparable(yType);
				}
				boolean bval = false;
				switch (operator) {
					case EQ_SYM: // '=='
						bval = result == 0;
						break;
					case NE_SYM: // '!=''
						bval = result != 0;
						break;
					case LE_SYM: // '<=''
						bval = result <= 0;
						break;
					case GE_SYM: // '>='
						bval = result >= 0;
						break;
					case LT_SYM: // '<''
						bval = result < 0;
						break;
					case GT_SYM: // '>'
						bval = result > 0;
						break;
					default:
				}
				_g.replaceTwo(new DefBoolean(bval));
			} else if (xType == XD_NULL || yType == XD_NULL) {
				// not both constants, first null
				short op;
				switch (operator) {
					case EQ_SYM:
						op = EQ_NULL;
						break;
					case NE_SYM:
						op = NE_NULL;
						break;
					default:
						op = CompileBase.UNDEF_CODE;
						incomparable(yType);
				}
				_g.addCode(new CodeI1(XD_BOOLEAN, op), -1);
			} else {// not both constants, none null
				short op = CompileBase.UNDEF_CODE;
				switch (xType) {
					case XD_INT: {
						switch (yType) {
							case XD_INT:
								switch (operator) {
									case LSH_SYM:
										_g.addCode(new CodeI1(XD_INT,LSHIFT_I),-1);
										continue; // while sym is rel. operator
									case RSH_SYM:
										_g.addCode(new CodeI1(XD_INT,RSHIFT_I),-1);
										continue; // while sym is rel. operator
									case RRSH_SYM:
										_g.addCode(new CodeI1(XD_INT,
											RRSHIFT_I),-1);
										continue; // while sym is rel. operator
								}
								op = CMPEQ;
								break;
							case XD_FLOAT:
								_g.topXToFloat();
								op = CMPEQ;
								break;
							case XD_DECIMAL:
								_g.addCode(new CodeI1(XD_DECIMAL,
									TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case CompileBase.XD_UNDEF:
								break;
							case XD_ANY:
								//TODO
							default:
								incomparable(yType);
						}
						break;
					}
					case XD_FLOAT: {
						switch (yType) {
							case XD_INT:
								_g.addCode(new CodeOp(XD_FLOAT,TO_FLOAT),0);
								op = CMPEQ;
								break;
							case XD_FLOAT:
								op = CMPEQ;
								break;
							case XD_DECIMAL:
								_g.addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case XD_ANY:
								//TODO
							default:
								incomparable(yType);
						}
						break;
					}
					case XD_BOOLEAN: {
						if (yType != XD_BOOLEAN) {
							_g.topToBool();
							yType = _g._tstack[_g._sp];
						}
						if (yType == XD_BOOLEAN) {
							if (operator !=  EQ_SYM && operator !=  NE_SYM) {
								//Values of the type 'boolean' can be compared
								//only for equality
								error(XDEF.XDEF445);
							} else {
								op = CMPEQ;
							}
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_STRING: {
						if (yType == XD_STRING) {
							op = CMPEQ;
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_DATETIME: {
						if (yType == XD_DATETIME) {
							op = CMPEQ;
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_DURATION: {
						if (yType == XD_DURATION) {
							op = CMPEQ;
						} else {
							incomparable(yType);
						}
						break;
					}
					case XD_DECIMAL: {
						switch (yType) {
							case XD_INT:
							case XD_FLOAT:
								_g.addCode(new CodeI1(XD_DECIMAL,
									TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case XD_DECIMAL:
								op = CMPEQ;
								break;
							default:
								incomparable(yType);
						}
						break;
					}
					case CompileBase.XD_UNDEF:
						break;
					case XD_ANY:
						//TODO
					default:
						incomparable(yType);
				}
				switch (operator) {
					case EQ_SYM: // ==
						break;
					case NE_SYM: // !=
						op++;
						break;
					case LE_SYM: // <=
						op += 2;
						break;
					case GE_SYM: // >=
						op += 3;
						break;
					case LT_SYM: // <
						op += 4;
						break;
					case GT_SYM: // >
						op += 5;
						break;
					default:
				}
				_g.addCode(new CodeI1(XD_BOOLEAN, op), -1);
			}
		}
		return true;
	}

	/** Parse and compile andExpression.
	 * operatorLevel4 ::= "&" | "&&"
	 * andExpression ::=  relExpression ( operatorLevel4 relExpression)*
	 * @return true if andExpression was parsed.
	 */
	private boolean andExpression(final CompileJumpVector jumpVector) {
		int sp = _g._sp;
		if (!relExpression()) {
			return false;
		}
		boolean firstError = _g._sp <= sp;
		while (OP_L4.indexOf(_sym) >= 0) { // AND_SYM, AAND_SYM
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			_g.topToBool();
			int xValue = _g._cstack[_g._sp];
			if (operator == AAND_SYM) {
				_g.addJump(jumpVector.addJumpItemToFalseList(JMPF_OP));
			}
			sp = _g._sp;
			nextSymbol();
			if (!relExpression()) {
				error(XDEF.XDEF437); //Error in expression
				continue;
			}
			if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
				if (sp == _g._sp) {
					error(XDEF.XDEF438); //Value expected
				}
				_g.topToBool(); // resolve ATTR_REF_VALUE etc
				continue;
			}
			if (operator == AND_SYM) {// AND_SYM logical and
				_g.topToBool();
				int yValue = _g._cstack[_g._sp];
				if (xValue >= 0 && yValue >= 0) { // both constants
					boolean a = _g.getCodeItem(xValue).booleanValue();
					boolean b = _g.getCodeItem(yValue).booleanValue();
					_g.replaceTwo(new DefBoolean(a & b));
				} else { // not both constants
					_g.addCode(new CodeI1(XD_BOOLEAN, AND_B), -1);
				}
			}
		}
		return true;
	}

	/** Parse and compile expression.
	 * operatorLevel5 ::= ( "|" | "^" | "||" | "XOR")
	 * expression ::=  andExpression (operatorLevel5  andExpression )*
	 *   ( "?" expression ":"  expression )?
	 * @return CompileJumpVector if expression was parsed, otherwise return null.
	 */
	private CompileJumpVector expr() {
		int sp = _g._sp;
		CompileJumpVector jumpVector = new CompileJumpVector();
		if (!andExpression(jumpVector)) {
			return null;
		}
		boolean firstError = _g._sp <= sp;
		while (OP_L5.indexOf(_sym) >= 0) { // OR_SYM, XOR_SYM, OOR_SYM
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			_g.topToBool();
			short xType = _g._tstack[_g._sp];
			int xValue = _g._cstack[_g._sp];
			if (operator == OOR_SYM) {
				_g.addJump(jumpVector.addJumpItemToTrueList(JMPT_OP));
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			}
			nextSymbol();
			sp = _g._sp;
			if (!andExpression(jumpVector)) {
				error(XDEF.XDEF437); //Error in expression
				continue;
			}
			if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
				if (sp == _g._sp) {
					error(XDEF.XDEF438); //Value expected
				}
				_g.topToBool();  // resolve ATTR_REF_VALUE etc
				continue;
			}
			if (operator == OR_SYM || operator == XOR_SYM) {
				_g.topToBool();
				short yType = _g._tstack[_g._sp];
				int yValue = _g._cstack[_g._sp];
				if (xType == XD_BOOLEAN && yType == XD_BOOLEAN) {
					if (xValue >= 0 && yValue >= 0) { // both constants
						boolean a = _g.getCodeItem(xValue).booleanValue();
						boolean b = _g.getCodeItem(yValue).booleanValue();
						_g.replaceTwo(new DefBoolean(operator==OR_SYM ?
							(a | b) : (a ^ b)));
					} else {
						_g.addCode(new CodeI1(XD_BOOLEAN,
							operator == OR_SYM ? OR_B : XOR_B), -1);
					}
				}
			}
		}
		if (_sym == ASK_SYM) { // '?' - conditional expression
			// conditional expression
			if (sp == _g._sp) {
				error(XDEF.XDEF423, "boolean");//Value of type '&{0}' expected
				_g.genLDC(new DefBoolean(true)); // addCode(_parsedValue, 1);
			} else {
				_g.topToBool();
			}
			short xType = _g._tstack[_g._sp];
			int xValue = _g._cstack[_g._sp];
			if (xType != XD_BOOLEAN) {
				_g._tstack[_g._sp] = XD_BOOLEAN;
				xValue = _g._cstack[_g._sp] = -2; // prevent code optimizing
			}
			nextSymbol();
			// prepare false jump and else jump
			if (xValue >= 0) {
				// the bollean value on top of stack is constant, so we'll
				// ignore the unused branche.
				boolean isTrue = _g.getCodeItem(xValue).booleanValue();
				_g._sp--;
				_g.removeLastCodeItem();
				if (isTrue) { // don't generate false jump
				} else { // gen jump false code as hard jump
					_g.addJump(jumpVector.addJumpItemToTrueList(JMP_OP));
				}
			} else {
				if (jumpVector.isEmpty()) {
					_g.addJump(jumpVector.addJumpItemToFalseList(JMPF_OP));
				} else {
					_g._sp--;
					jumpVector.resoveTrueJumps(_g._lastCodeIndex + 1);
				}
			}
			// save state of stack and code
			int sp0 = sp = _g._sp;
			compileValue();
			if (sp == _g._sp) {
				xType = XD_VOID;
				xValue = -1;
				error(XDEF.XDEF438); //Value expected
			} else {
				xType = _g._tstack[_g._sp];
				xValue = _g._cstack[_g._sp];
			}
			if (!checkSymbol(COLON_SYM)) {//':'
				_g._sp++; // if error just simulate stack value or previous type
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			} else {
				if (_g._sp >= 0) { // set top of stack is not constant!
					_g._cstack[_g._sp] = -1;
				}
				CodeI1 jmp = new CodeI1(XD_VOID, JMP_OP);
				_g.addJump(jmp);
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
				_g._sp = sp; // return stack!
				short yType;
				sp = _g._sp = sp0;
				compileValue();
				if (sp == _g._sp) {
					yType = XD_VOID;
					error(XDEF.XDEF438); //Value expected
				} else {
					yType = _g._tstack[_g._sp];
					if (xType == XD_NULL && xValue >= 0) {
						switch (yType) {
							case XD_STRING:
								_g.setCodeItem(xValue, new DefString());
								xType = _g._tstack[_g._sp] = XD_STRING;
								break;
							case XD_ELEMENT:
								_g.setCodeItem(xValue, new DefElement(null));
								xType =_g._tstack[_g._sp] = XD_ELEMENT;
								break;
							case XD_CONTAINER:
								_g.setCodeItem(xValue,
									new DefContainer((Object) null));
								xType = _g._tstack[_g._sp] = XD_CONTAINER;
								break;
						}
					}
					if (yType == XD_NULL && _g._cstack[_g._sp] >= 0) {
						_g.convertTopToType(xType);
						yType = _g._tstack[_g._sp];
					}
					_g._cstack[_g._sp] = -2; // prevent code optimizing
				}
				if (xType != yType) {
					if (xType != XD_ANY && yType != XD_ANY
						&& xType != CompileBase.XD_UNDEF
						&& yType != CompileBase.XD_UNDEF) {
						error(XDEF.XDEF457, //Incompatible types&{0}{: }
							CompileBase.getTypeName(xType) + "," +
							CompileBase.getTypeName(yType));
					}
				}
				jmp.setParam(_g._lastCodeIndex + 1);
			}
		}
		return jumpVector;
	}

	/** Compile boolean expression.
	 * @param jumpCondition if <tt>true</tt> then result will be generated as
	 * conditional jump sequence.
	 * @return CompileJumpVector object or <tt>null</tt>.
	 */
	private CompileJumpVector boolExpression(final boolean jumpCondition) {
		int sp = _g._sp;
		CompileJumpVector jumpVector = expr();
		if (jumpVector == null) {
			error(XDEF.XDEF423, "boolean");//Value of type '&{0}' expected
			jumpVector = new CompileJumpVector(); // prevent null pointer
		}
		short xType;
		int xValue;
		if (_g._sp > sp) {
			_g.topToBool();
			xType = _g._tstack[_g._sp];
			if (xType != XD_BOOLEAN
				&& xType != XD_ANY && xType != CompileBase.XD_UNDEF) {
				error(XDEF.XDEF423, "boolean");//Value of type '&{0}' expected
				_g._tstack[_g._sp] = CompileBase.XD_UNDEF;
				_g._cstack[_g._sp] = -1;
			}
			xValue = _g._cstack[_g._sp];
		} else {
			_g._tstack[++_g._sp] = XD_BOOLEAN;
			xValue = _g._cstack[_g._sp] = -1;
			error(XDEF.XDEF423, "boolean"); //Value of type '&{0}' expected
		}
		if (xValue >= 0) {
			// the boolean value on top of stack is constant, so we'll ignore
			// the unused branche.
			boolean expResult = _g.getCodeItem(xValue).booleanValue();
			_g._sp--;
			_g.removeLastCodeItem();
			if (expResult != jumpCondition) {
				return null;
			}
			if (jumpCondition) {
				_g.addJump(jumpVector.addJumpItemToTrueList(JMP_OP));
			} else {
				_g.addJump(jumpVector.addJumpItemToFalseList(JMP_OP));
			}
		} else {
			if (jumpCondition) {
				_g.addJump(jumpVector.addJumpItemToTrueList(JMPT_OP));
			} else {
				_g.addJump(jumpVector.addJumpItemToFalseList(JMPF_OP));
			}
		}
		return jumpVector;
	}

	/** Parse expression. Create jump vector and return true, if result parsed
	 * expression was jump boolean vector.
	 * expression ::=  andExpression (operatorLevel5  andExpression )*
	 * operatorLevel5 ::= ( "|" | "^" | "||" )
	 *   ( "?" expression ":"  expression )?
	 * @return return true if parsed expression was compiled, otherwise
	 * return false.
	 */
	final boolean expression() {
		CompileJumpVector jumpVector = expr();
		if (jumpVector != null) {
			_g.jumpVectorToBoolValue(jumpVector);
			return true;
		}
		return false;
	}
	private CompileVariable getVariable(final String name) {
		CompileVariable var;
		if ((var = _g.getVariable(_actDefName + '#' + name)) == null) {
			var = _g.getVariable(name);
		}
		return var;
	}

	private CompileVariable getVariableAndErr(final String name) {
		CompileVariable var = getVariable(name);
		if (var == null) {
			error(XDEF.XDEF424, name); //Undefined variable '&{0}'
		}
		return var;
	}

	/** Compile assignment.
	 * Assignment_operator:: ( '=' | '+=' | '-=" | '*=' | '/=' | || '%='
	 * '|=' | '&=' | '^=' | '<<=' | '>>=' | '>>=')
	 * Assignment::= variable Assignment_operator expression
	 * @param varName Name of variable to be assigned.
	 * @param keepValue if value should remain on the stack.
	 */
	private boolean assignment(final String varName, final boolean keepValue) {
		if (OP_ASSGN.indexOf(_sym) < 0) {
			return false;
		}
		String name = varName;
		CompileVariable var = getVariableAndErr(name);
		char op = _sym;
		nextSymbol();
		int dx = addDebugInfo(true);
		if (op != ASSGN_SYM) {
			_g.genLD(name);
		}
		int sp = _g._sp;
		if (op == ASSGN_SYM && _sym == MOD_SYM) {
			namedValueConstructor(null);
		} else {
			compileValue();
		}
		setDebugEndPosition(dx);
		short yType = var == null ? CompileBase.XD_UNDEF : var.getType();
		if (_g._sp == sp) {
			_g.setUnDefItem();
		}
		short xType = _g._tstack[_g._sp];
		if (xType == XD_VOID) {
			error(XDEF.XDEF438); //Value expected
		} else if (xType == CompileBase.UNIQUESET_VALUE
			|| xType == CompileBase.PARSEITEM_VALUE) {
			//Assignment of value of type &{0} is not allowed
			error(XDEF.XDEF488, CompileBase.getTypeName(xType));
		}
		if (op != ASSGN_SYM && var != null) {
			short code = CompileBase.UNDEF_CODE;
			switch(op) {
				case LSH_EQ_SYM:
				case RSH_EQ_SYM:
				case RRSH_EQ_SYM:
					if (xType == XD_INT && yType == XD_INT) {
						code = op==LSH_EQ_SYM ?
							LSHIFT_I : op==RSH_EQ_SYM ? RSHIFT_I : RRSHIFT_I;
					}
					break;
				case MUL_EQ_SYM:
					if (yType == XD_FLOAT) {
						_g.topToFloat();
						xType = _g._tstack[_g._sp];
					}
					code = xType==XD_INT ? MUL_I : MUL_R;
					break;
				case DIV_EQ_SYM:
					if (yType == XD_FLOAT) {
						_g.topToFloat();
						xType = _g._tstack[_g._sp];
					}
					code = xType == XD_INT ? DIV_I : DIV_R;
					break;
				case MOD_EQ_SYM:
					if (yType == XD_FLOAT) {
						_g.topToFloat();
						xType = _g._tstack[_g._sp];
					}
					code = xType == XD_INT ? MOD_I : MOD_R;
					break;
				case AND_EQ_SYM:
					if (xType == CompileBase.ATTR_REF_VALUE) {
						_g.topToBool();
					}
					if (xType == XD_BOOLEAN) {
						code = AND_B;
					}
					break;
				case PLUS_EQ_SYM:
					if (yType == XD_STRING) {
						_g.topToString();
						xType = _g._tstack[_g._sp];
					} else if (yType == XD_FLOAT) {
						_g.topToFloat();
						xType = _g._tstack[_g._sp];
					}
					code = xType == XD_INT ? ADD_I
						: xType == XD_FLOAT ? ADD_R
						: xType == XD_STRING ? ADD_S : code;
					break;
				case MINUS_EQ_SYM:
					if (yType == XD_FLOAT) {
						_g.topToFloat();
						xType = _g._tstack[_g._sp];
					}
					if (xType == XD_INT) {
						code = SUB_I;
					} else if (xType == XD_FLOAT) {
						code = SUB_R;
					}
					break;
				case OR_EQ_SYM:
					if (xType == CompileBase.ATTR_REF_VALUE) {
						_g.topToBool();
					}
					if (xType == XD_BOOLEAN) {
						code = OR_B;
					}
					break;
				case XOR_EQ_SYM:
					if (xType == CompileBase.ATTR_REF_VALUE) {
						_g.topToBool();
					}
					if (xType == XD_BOOLEAN) {
						code = XOR_B;
					}
					break;
				default:
			}
			if (code == CompileBase.UNDEF_CODE) {
				error(XDEF.XDEF457); //Incompatible types
			} else {
				_g.addCode(new CodeI1(yType, code), -1);
			}
			xType = _g._tstack[_g._sp];
		} else {
			if (yType == XD_BOOLEAN && xType == XD_PARSERESULT) {
				_g.topToBool(); // force conversion to boolean!!!
			} else {
				if (yType == CompileBase.UNIQUESET_NAMED_VALUE) {
					// named value of uniqueSet
					yType = var.getParseResultType(); // type of result
					_g.convertTopToType(yType); // convert to the required type
					if (keepValue) {
						_g.addCode(new CodeOp(yType, STACK_DUP),1);
					}
					_g.genLD(var); // load the uniqueset object
					String s = var.getValue().toString(); // name of variable
					_g.addCode(new CodeS1(yType, UNIQUESET_SETVALUEX, s),-2);
					return true;
				} else {
					_g.convertTopToType(yType);
				}
			}
			if (var != null && _g._sp >= 0 && _g._cstack[_g._sp] >= 0) {
				if (_g._tstack[_g._sp] == XD_BNFGRAMMAR) {
					var.setValue(_g.getCodeItem(_g._cstack[_g._sp]));
					var.setCodeAddr(_g._cstack[_g._sp]);
				}
				var.setInitialized(true);
			}
		}
		if (var == null) {
			name = CompileBase.genErrId(); // "#UNDEF" + _g._lastCodeIndex;
			_g.addVariable(name, xType, (byte) 'L', null);
		} else if (var.isFinal()) {
			//Variable '&{0}' is 'final'; the value can't be assigned
			error(XDEF.XDEF119, name);
		}
		_g.genST(name);
		if (keepValue) {
			_g.genLD(name);
		}
		return true;
	}

	final void initCompilation(final byte mode,
		final short returnType) {
		_g._mode = mode;
		_g.clearLocalVariables();
		_blockInfo = new BlockInfo(false, -1, null, _g);
		_wasContinue = false;
		_wasBreak = false;
		_wasReturn = false;
		_returnType = returnType;
		_catchItem = new CodeI1(XD_VOID, SET_CATCH_EXCEPTION, -1);
	}

////////////////////////////////////////////////////////////////////////////////
// Compilation of Statements.
////////////////////////////////////////////////////////////////////////////////

	/** Get return status.
	 * @return true if return statement occurred in all branches, otherwise
	 * return false.
	 */
	final boolean wasReturn() {return _wasReturn;}

	private void initBlock(final boolean jumps, final int continueAddr) {
		_blockInfo = new BlockInfo(jumps, continueAddr, _blockInfo, _g);
	}

	private void closeBlock() {_blockInfo = _blockInfo.closeBlock(_g);}

	private void genBreakJump(final CodeI1 jmp) {
		if (_blockInfo._breakJumps == null) {
			//'break' command is not inside of switch statement or loop
			error(XDEF.XDEF451);
			return;
		}
		if (jmp != null) {
			_g.addJump(jmp); // gen jump
			_blockInfo._breakJumps.add(jmp);
		}
	}

	private void genContinueJump(final CodeI1 jmp) {
		if (_blockInfo._continueAddr < 0) {
			error(XDEF.XDEF452); //'continue' command is not inside of loop
			return;
		}
		if (jmp != null) {
			jmp.setParam(_blockInfo._continueAddr);
			_g.addJump(jmp); // gen jump false back to loop
		}
	}

	private void labelOrSimplestatement(final boolean isFinal) {
		String name = _idName;
		nextSymbol();
		if (!simpleStatement(name, isFinal)) {
			error(XDEF.XDEF455); //Method invocation expected
		}
//TODO ??? labels; break with label, continue with label...
//		String name = _idName;
//		if (nextSymbol() == COLON_SYM && !isFinal) { // ':'
//			// label
//			Object o =
//				_g._progLabels.put(name, new Integer(_g._lastCodeIndex + 1));
//			if (o != null) {
//				if (o instanceof ArrayList) {
//					ArrayList refs = (ArrayList)o;
//					for (int i = 0; i < refs.size(); i++) {
//						((CodeI1) _g.getCodeItem(
//							((Integer)refs.get(i)).intValue()))._param =
//								_g._lastCodeIndex + 1;
//					}
//				} else {
//					error(XDEF.XDEF456); //Label redefinition
//				}
//			}
//			nextSymbol();
//			statement();
//		} else if (!simpleStatement(name, isFinal)) {
//			error(XDEF.XDEF455); //Method invocation expected
//		}
	}

	/** Parse and compile statement.
	 * ifStatement ::= "if" s? "(" s? expression s? ")" S? statement
	 *		( s? "else" s? statement )?
	 * whileStatement ::= "while" s? "(" s? expression s? ")" s? statement
	 * doStatement ::= "do" S? statement s? "while" s? "(" s? expression s? ")"
	 *		( s? "else" s? statement )?
	 * assignment ::= identifier s? "=" s? expression
	 * statement ::=  ( method | assignment | compoundStatement
	 *                |  ifStatement | whileStatement | doStatement)? s? ";"
	 * compoundStatement ::= "{" s? statement* s? "}"
	 *
	 * @return true if statement was parsed.
	 */
	final boolean statement() {
		boolean isFinal = false;
		switch (_sym) {
			case BEG_SYM: // '{'
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); // Unreachable statement
				}
				// compound statement
				nextSymbol();
				initBlock(false, -1);
				while (statement()){}
				checkSymbol(END_SYM); // '}'
				closeBlock();
				return true;
			case SEMICOLON_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				// empty statement
				nextSymbol();
				return true;
			case INC_SYM:
			case DEC_SYM: {
				int dx = addDebugInfo(false);
				isIncStatement();
				setDebugEndPosition(dx);
				break;
			}
			case EXTERNAL_SYM:
			case FINAL_SYM: {
				boolean wasFinal = false, wasExternal = false;
				for(;;) {
					switch (_sym) {
						case FINAL_SYM:
							if (wasFinal) {
								//Duplicate specification of &{0}
								error(XDEF.XDEF118, "external");
							}
							wasFinal = true;
							nextSymbol();
							continue;
						case EXTERNAL_SYM:
							if (wasExternal) {
								//Duplicate specification of &{0}
								error(XDEF.XDEF118, "external");
							} else {
								//'&{0}' is not allowed here
								error(XDEF.XDEF411, "external");
							}
							wasExternal = true;
							nextSymbol();
							continue;
					}
					break;
				}
				if (_sym != IDENTIFIER_SYM) {
					error(XDEF.XDEF412); //Type identifier expected
				} else {
					if (_wasReturn || _wasContinue || _wasBreak) {
						error(XDEF.XDEF453); //Unreachable statement
					}
					short varType = getVarTypeCode(_idName);
					if (nextSymbol() == IDENTIFIER_SYM) {
						String name = _idName;
						SPosition spos = getLastPosition();
						nextSymbol();
						varDeclaration(varType,
							name, wasFinal, false, (byte)'L', spos);
					} else {
						error(XDEF.XDEF454); //Variable identifier expected
					}
				}
				break;
			}
			case IDENTIFIER_SYM: {
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				int dx = addDebugInfo(false);
				labelOrSimplestatement(isFinal);
				setDebugEndPosition(dx);
				break;
			}
			case IF_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				nextSymbol();
				ifStatement();
				return true;
			case DO_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				nextSymbol();
				doStatement();
				break;
			case WHILE_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				nextSymbol();
				whileStatement();
				return true;
			case FOR_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				nextSymbol();
				forStatement();
				return true;
			case SWITCH_SYM:
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				nextSymbol();
				switchStatement();
				return true;
			case CONTINUE_SYM: {
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				_wasContinue = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				genContinueJump(new CodeI1(XD_VOID, JMP_OP));
				setDebugEndPosition(dx);
				break;
			}
			case BREAK_SYM: {
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				_wasBreak = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				genBreakJump(new CodeI1(XD_VOID, JMP_OP));
				setDebugEndPosition(dx);
				break;
			}
			case RETURN_SYM: {
				if (_wasReturn || _wasContinue || _wasBreak) {
					error(XDEF.XDEF453); //Unreachable statement
				}
				_wasReturn = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				int sp = _g._sp;
				short code = RET_OP;
				short xType = XD_VOID;
				if (_returnType != XD_VOID) {
					expression();
					if (sp + 1 == _g._sp) {
						if ((xType = _g._tstack[_g._sp]) != _returnType) {
							if (xType == XD_CONTAINER
								&& _returnType == XD_STRING) {
								_g.topToString();
							} else {
								_g.convertTopToType(_returnType);
							}
						}
						code = RETV_OP;
					} else {
						error(XDEF.XDEF440); //Return value expected
					}
				}
				setDebugEndPosition(dx);
				_g.addCode(new CodeI1(xType, code, _popNumParams), 0);
				_g._sp = sp;
				break;
			}
			case THROW_SYM:
				throwStatement();
				break;
			case TRY_SYM: // try
				tryStatement();
				return true;
			case CATCH_SYM:
				error(XDEF.XDEF476); // 'catch' whithout 'try'
				statement();
				return true;
			case CASE_SYM:
				error(XDEF.XDEF477); // 'case' whithout 'switch'
				return true;
			case ELSE_SYM:
				error(XDEF.XDEF478); // 'else' whithout 'if'
				statement();
				return true;
			default:
				return false;
		}
		if (_sym != END_SYM && _sym != ELSE_SYM) {
			checkSemicolon(new String(
				new char[] {SEMICOLON_SYM, BEG_SYM, END_SYM}));
		}
		return true;
	}

	private boolean simpleStatement(final String name, final boolean isFinal) {
		if (_sym == IDENTIFIER_SYM) {
			short varType = getVarTypeCode(name);
			String varName = _idName;
			SPosition spos = getLastPosition();
			nextSymbol();
			varDeclaration(varType, varName, isFinal, false, (byte) 'L', spos);
			return true;
		} else {
			if (isFinal) {
				error(XDEF.XDEF412); //Type identifier expected
			}
			return simpleStatement(name);
		}
	}

	/** All simple statements. */
	private boolean simpleStatement() {
		if (_sym == IDENTIFIER_SYM) {
			String name = _idName;
			nextSymbol();
			return simpleStatement(name);
		} else {
			return isIncStatement();
		}
	}

	/**	Compile simple statement.
	 * @param name the name of statement ("if","do","for","while","try" etc.)
	 * @return true if the statement was compiled.
	 */
	private boolean simpleStatement(final String name) {
		SPosition spos = getLastPosition();
		boolean wasVariable;
		switch (_sym) {
			case DOT_SYM: {
				// a) a method can be called on an object defined in X-Skript
				// b) an external method given by fully qualified name is called
				// try to find the defined variable
				if (wasVariable =_g.isVariable(name)) {
					// the object's method "name" has to be probe first
					if (!_g.genLD(name)) {
						if (_sym == DOT_SYM) {
							return qualifiedMethod(name, false);
						}
						error(XDEF.XDEF438); //Value expected
					}
				}
				while (_sym == DOT_SYM) {
					// remember the state of the parser
					int lastPos = getIndex();
					String lastIdName = _idName;
					nextSymbol();
					if (_sym != IDENTIFIER_SYM) {
						error(XDEF.XDEF417); //Method name expected
						return true;
					}
					String cname = _idName;
					nextSymbol();
					int sp = _g._sp;
					int numPar = _sym == LPAR_SYM
						? paramList(cname).getNumpars():-1;
					if (numPar >= 0) { // '(' => method
						boolean wasClassMethod = true;
						// try to find and generate the internal method
						if (!_g.genClassMethod(cname, numPar)) {
							// the method is not internal in X-Skript
							wasClassMethod = false;
						}
						while (sp < _g._sp) {
							_g.genPop();
						}
						if(!wasVariable && !wasClassMethod) {
							// the "name" is not a variable defined in X-Skript
							// and the method is also not defined in X-Skript
							// therefore it could be a path to a fully
							// qualified external method
							// Change the parser to the remembered state:
							// (set the position on the dot (.) between the
							// method name and the path to the method's class)
							setBufIndex(lastPos);
							// set the last symbol (dot) and idName
							_sym = DOT_SYM;
							_idName = lastIdName;
							// Try to find external method and generate its code
							qualifiedMethod(name, false);
						} else {
							// External method was not generated.
							// Check and reports possible errors:
							if(!wasVariable) {
								//Variable was not defined but the class method
								//is defined in X-Skript
								error(XDEF.XDEF233); //Statement error
							}
							if(!wasClassMethod) {
								//Variable is defined but the method was not
								//defined in X-Skript
								error(XDEF.XDEF443, cname);//Unknown method:&{0}
							}
						}
					} else {
						error(XDEF.XDEF410, "("); //'&{0}' expected
					}
				}
				return true;
			}
			case INC_SYM:
			case DEC_SYM: {
				genInc(_sym, name, 0);
				nextSymbol();
				return true;
			}
			case LPAR_SYM: {
				int sp = _g._sp;
				method(name, spos);
				while (sp < _g._sp) {
					_g.genPop();
				}
				return true;
			}
			default: {
				if (assignment(name, false)) {
					return true;
				}
				int ndx;
				int sp = _g._sp;
				if ((ndx=name.indexOf('.')) > 0) {
					String methodName = name.substring(ndx + 1);
					String cName = name.substring(0, ndx);
					if (_g.genLD(cName)) {
						if (!_g.genClassMethod(methodName, 0)) {
							_g.genPop();
							return false;
						}
					}
				} else {
					if (_g.genMethod(name, 0) != null) {
						return false;
					}
				}
				while (sp < _g._sp) {
					_g.genPop();
				}
			}
			return true;
		}
	}

	/** Inc ("++" or "--") statement. */
	private boolean isIncStatement() {
		if(_sym != INC_SYM && _sym != DEC_SYM) {
			return false;
		}
		if (_wasReturn || _wasContinue || _wasBreak) {
			error(XDEF.XDEF453); //Unreachable statement
		}
		char op = _sym;
		int dx = addDebugInfo(true);
		nextSymbol();
		if (_sym != IDENTIFIER_SYM) {
			error(XDEF.XDEF436); //Variable expected
		} else {
			genInc(op, _idName, 0);
		}
		setDebugEndPosition(dx);
		nextSymbol();
		return true;
	}

	/** While statement. */
	private void whileStatement() {
		int dx = addDebugInfo(true);
		initBlock(true, _g._lastCodeIndex + 1);
		CompileJumpVector jumpVector = parBoolExpression(false);
		if (jumpVector != null) {
			jumpVector.resoveTrueJumps(_g._lastCodeIndex + 1);
		}
		setDebugEndPosition(dx);
		statement();
		genContinueJump(new CodeI1(XD_VOID, JMP_OP));
		if (jumpVector != null) {
			jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
		}
		closeBlock();
	}

	/** Do statement. */
	private void doStatement() {
		initBlock(true, _g._lastCodeIndex + 1);
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		int dx = addDebugInfo(true);
		if (checkSymbol(WHILE_SYM, XDEF.XDEF410, "while")) { //'&{0}' expected
			CompileJumpVector jumpVector = parBoolExpression(true);
			if (jumpVector != null) {
				jumpVector.resoveTrueJumps(_blockInfo._continueAddr);
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			}
		}
		setDebugEndPosition(dx);
		closeBlock();
	}

	/** If statement. */
	private void ifStatement() {
		int dx = addDebugInfo(true);
		CompileJumpVector jumpVector =  parBoolExpression(false);
		if (jumpVector != null) {
			jumpVector.resoveTrueJumps(_g._lastCodeIndex + 1);
		}
		setDebugEndPosition(dx);
		boolean wasBreak = _wasBreak;
		boolean wasContinue = _wasContinue;
		boolean wasReturn = _wasReturn;
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		boolean allReturn = _wasReturn;
		boolean allContinue = _wasContinue;
		_wasBreak = wasBreak;
		_wasContinue = wasContinue;
		_wasReturn = wasReturn;
		if (_sym == ELSE_SYM) {
			nextSymbol();
			CodeI1 jmp = new CodeI1(XD_VOID, JMP_OP);
			_g.addJump(jmp);
			if (jumpVector != null) {
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			}
			if (!statement()) {
				error(XDEF.XDEF447); //Statement expected
			}
			jmp.setParam(_g._lastCodeIndex + 1);
			_wasBreak &= allReturn;
			_wasContinue &= allContinue;
			_wasReturn &= allReturn;
		} else {
			if (jumpVector != null) {
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			}
		}
	}

	/** For statement. */
	private void forStatement() {
		int dx = addDebugInfo(true);
		checkSymbol(LPAR_SYM); // '('
		initBlock(false, -1);
		if (!isIncStatement()) {
			boolean isFinal;
			if (isFinal = _sym == FINAL_SYM) {
				nextSymbol();
			}
			if (_sym == IDENTIFIER_SYM) {
				String name = _idName;
				nextSymbol();
				if (!simpleStatement(name, isFinal)) {
					error(XDEF.XDEF414); //Assignment statement expected
				}
			}
		}
		checkSymbol(SEMICOLON_SYM); // ';'
		_blockInfo._breakJumps = new ArrayList<CodeI1>();
		_blockInfo._continueAddr = _g._lastCodeIndex + 1;
		_blockInfo._jumps = true;
		int sp = _g._sp;
		CompileJumpVector jumpVector = null;
		if (_sym != SEMICOLON_SYM
			&& (jumpVector =  boolExpression(false)) != null) {
			jumpVector.resoveTrueJumps(_g._lastCodeIndex + 1);
		}
		checkSymbol(SEMICOLON_SYM); // ';'
		int lastCodeIndex = _g._lastCodeIndex;
		int spMax = _g._spMax;
		if (_sym != RPAR_SYM) { // statement list may be empty!
			if (simpleStatement()) {
				while (_sym == COMMA_SYM) { // ','
					nextSymbol();
					if (!simpleStatement()) {
						error(XDEF.XDEF447); //Statement expected
					}
				}
			} else {
				error(XDEF.XDEF447); //Statement expected
			}
		}
		checkSymbol(RPAR_SYM); // ')'
		setDebugEndPosition(dx);
		ArrayList<XDValue> savedCode;
		if (_g._lastCodeIndex > lastCodeIndex) {
			savedCode = new ArrayList<XDValue>(_g._lastCodeIndex-lastCodeIndex);
			for (int i = lastCodeIndex + 1; i < _g._code.size(); i++) {
				savedCode.add(_g.getCodeItem(i));
			}
			_g.removeCodeFromIndexAndClearStack(lastCodeIndex, sp, spMax);
		} else {
			savedCode = null;
		}
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		if (savedCode != null) {
			// move code to the end
			for (XDValue v: savedCode) {
				_g.addCode(v);
			}
			_g._sp = sp;
			if (spMax > _g._spMax) {
				_g._spMax = spMax;
			}
		}
		genContinueJump(new CodeI1(XD_VOID, JMP_OP));
		if (jumpVector != null) {
			jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
		}
		closeBlock();
	}

	/** Switch statement. */
	private void switchStatement() {
		checkSymbol(LPAR_SYM); // '('
		int sp = _g._sp;
		int dx = addDebugInfo(true);
		expression();
		short xType = _g._sp > sp ? _g._tstack[_g._sp] : XD_VOID;
		if (xType != XD_INT && xType != XD_STRING
			&& xType != CompileBase.XD_UNDEF) {
			xType = CompileBase.XD_UNDEF;
			error(XDEF.XDEF446); //'int' or 'String' value expected
		}
		checkSymbol(RPAR_SYM); // ')'
		setDebugEndPosition(dx);
		XDValue code = xType == XD_STRING ?
			new CodeSWTableStr() : new CodeSWTableInt();
		_g.addCode(code, -1);
		checkSymbol(BEG_SYM);
		initBlock(true, -1);
		boolean wasDefault = false;
		int defaultAddr = -1;
		Map<Object, Integer> ht = new LinkedHashMap<Object, Integer>();
		boolean wasContinue = true;
		boolean wasReturn = true;
		while (_sym != END_SYM) {
			_wasBreak = false;
			_wasContinue = false;
			_wasReturn = false;
			dx = addDebugInfo(true);
			if (_sym == CASE_SYM) {
				nextSymbol();
				sp = _g._sp;
				expression();
				setDebugEndPosition(dx);
				short yType;
				int yValue;
				if (_g._sp > sp) {
					yType = _g._tstack[_g._sp];
					yValue = _g._cstack[_g._sp];
				} else {
					yType = XD_VOID;
					yValue = -1;
				}
				if (yValue < 0) {
					yType = XD_VOID;
				}
				if (yType != xType) {
					if (xType != CompileBase.XD_UNDEF
						&& yType != CompileBase.XD_UNDEF) {
						//Constant expression of type &{0} expected
						error(XDEF.XDEF448,xType==XD_INT ? "int":"String");
					}
				} else if (yType == XD_INT) {
					Long v = _g.getCodeItem(yValue).longValue();
					_g._sp--;
					_g.removeLastCodeItem();
					if (ht.containsKey(v)) {
						error(XDEF.XDEF496, v); //Duplicated case variant '{0}'
					}
					ht.put(v, _g._lastCodeIndex+1);
				} else if (yType == XD_STRING) {
					String v = _g.getCodeItem(yValue).toString();
					_g._sp--;
					_g.removeLastCodeItem();
					if (ht.containsKey(v)) {
						error(XDEF.XDEF496, v); //Duplicated case variant '{0}'
					}
					ht.put(v, _g._lastCodeIndex+1);
				}
			} else if (_sym == DEFAULT_SYM){ //default:
				if (wasDefault) {
					//'default' can't be duplicated in 'switch' statement
					error(XDEF.XDEF495);
				}
				wasDefault = true;
				setDebugEndPosition(dx);
				nextSymbol();
				defaultAddr = _g._lastCodeIndex + 1;
			} else {
				error(XDEF.XDEF449); //'case', 'default' or '}' expected
				while (statement()){}
				if (_sym == CASE_SYM || _sym == DEFAULT_SYM) {
					continue;
				}
				break;
			}
			checkSymbol(COLON_SYM);
			if (_sym == CASE_SYM || _sym == DEFAULT_SYM) {
				continue;
			}
			if (_sym == END_SYM) {
				break;
			}
			if (statement()) {
				while (_sym != CASE_SYM && _sym != DEFAULT_SYM
					&& _sym != END_SYM) {
					if (!statement()) {
						error(XDEF.XDEF447); //Statement expected
						break;
					}
				}
				wasReturn &= _wasReturn;
				wasContinue &= _wasContinue;
			}
		}
		_wasBreak = false;
		if (defaultAddr == -1) {
			defaultAddr = _g._lastCodeIndex + 1;
			_wasReturn = false;
			_wasContinue = false;
		} else {
			_wasReturn = wasReturn;
			_wasContinue = wasContinue;
		}
		if (xType == XD_INT) {
			CodeSWTableInt icode = (CodeSWTableInt) code;
			icode.setParam(defaultAddr);
			icode._adrs = new int[ht.size()];
			icode._list = new long[ht.size()];
			Long[] keys = new Long[ht.keySet().size()];
			ht.keySet().toArray(keys);
			for (int i = 0; i < keys.length; i++) {
				Long key = keys[i];
				icode._list[i] = key;
				icode._adrs[i] = ht.get(key);
			}
		} else if (xType == XD_STRING) {
			CodeSWTableStr scode = (CodeSWTableStr) code;
			scode.setParam(defaultAddr);
			scode._adrs = new int[ht.size()];
			scode._list = new String[ht.size()];
			String[] keys = new String[ht.keySet().size()];
			ht.keySet().toArray(keys);
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				scode._list[i] = key;
				scode._adrs[i] = ht.get(key);
			}
		}
		closeBlock();
		checkSymbol(END_SYM);
	}

	/** Get type ID from the type name. If the name is not a type make
	 * an error report.
	 * @param name the type name.
	 * @return type ID.
	 */
	final short getTypeCode(final String name) {
		short varType = CompileBase.getTypeId(name);
		if (varType < 0) {
			error(XDEF.XDEF412); //Type identifier expected
			varType = CompileBase.XD_UNDEF;
		}
		return varType;
	}

	/** Get type of variable (can not be "void"!).
	 * @param name the type name.
	 * @return type ID.
	 */
	private short getVarTypeCode(final String name) {
		short varType = getTypeCode(name);
		if (varType < 0 || varType > XD_OBJECT) {
			error(XDEF.XDEF458);//Expected type identifier of variable
			varType = CompileBase.XD_UNDEF;
		}
		return varType;
	}

	/** Compile variable declaration statement.
	 * @param type the type of variable (XD_INT, XD_STRING etc).
	 * @param varName the name of variable.
	 * @param isFinal if <tt>true</tt> variable is declared as final.
	 * @param isExternal if <tt>true</tt> variable is declared as external.
	 * @param varKind kind of variable ('G' .. global,'L' local,'X' .. X-model).
	 * @param spos source position where the variable was declared.
	 */
	final void varDeclaration(short type,
		final String varName,
		final boolean isFinal,
		final boolean isExternal,
		final byte varKind,
		final SPosition spos) {
		String name = varName;
		short varType;
		if (type == XD_VOID) {
			error(XDEF.XDEF485); //Variable type can't be 'void'
			varType = CompileBase.XD_UNDEF;
		} else if (type < 0 || type > XD_OBJECT) {
			error(XDEF.XDEF458); //Expected type identifier of variable
			varType = CompileBase.XD_UNDEF;
		} else {
			varType = type;
		}
		for(;;) {
			CompileVariable var = null;
			if (CompileBase.getTypeId(name) >= 0) {
				error(XDEF.XDEF454); //Variable name expected
			} else {
				var = _g.addVariable(name, varType, varKind, spos);
			}
			if (isExternal && var != null) {
				var.setExternal(true);
			}
			if (_sym == ASSGN_SYM) {
				if (isExternal) {
					//External variable '&{0}' can't be assigned in declaration
					error(XDEF.XDEF120, name);
				}
				if (var != null) {
					//set now final flag as false; we'll set it again
					var.setFinal(false);
				}
				assignment(name, false);
			} else if (var == null) {
				error(XDEF.XDEF454); //Variable name expected
			} else {
				if (isFinal && !isExternal) {
					error(XDEF.XDEF414); //Assignment statement expected
				} else if (varKind == 'G') {
					var.setInitialized(true);// default initialization (null...)
//				} else if (varKind == 'X') {
//					var.setInitialized(true);// default initialization (null...)
				}
			}
			if (isFinal && var != null) {
				var.setFinal(true);
			}
			if (_sym != COMMA_SYM) {
				break;
			}
			nextSymbol();
			if (_sym == IDENTIFIER_SYM) {
				name = _idName;
				nextSymbol();
			} else {
				break;
			}
		}
	}

	/** Try statement. */
	private void  tryStatement() {
		nextSymbol();
		CodeI1 catchItem = _catchItem;
		if (_sym != BEG_SYM) {
			error(XDEF.XDEF410, "{"); //'&{0}' expected
		}
		_catchItem = new CodeI1(XD_VOID, SET_CATCH_EXCEPTION);
		_g.addCode(_catchItem,0);
		boolean wasBreak = _wasBreak; // save break flag
		boolean wasContinue = _wasContinue; // save continue flag
		boolean wasReturn = _wasReturn; // save return flag
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		boolean allReturn = _wasReturn;
		boolean allContinue = _wasContinue;
		_wasBreak = wasBreak; // reset break flag
		_wasContinue = wasContinue; // reset continue flag
		_wasReturn = wasReturn; // reset return flag
		_g.addCode(new CodeI1(XD_VOID, RELEASE_CATCH_EXCEPTION), 0);
		initBlock(false, -1);
		CodeI1 jmp = new CodeI1(XD_VOID, JMP_OP);
		_g.addJump(jmp);
		_catchItem.setParam(_g._lastCodeIndex + 1);
		String varName = "_(DUMMY)_";
		SPosition spos = null;
		int dx = addDebugInfo(true);
		if (_sym != CATCH_SYM) {
			error(XDEF.XDEF410, "catch"); //'&{0}' expected
		} else {
			nextSymbol();
			if (_sym != LPAR_SYM) {
				error(XDEF.XDEF410, "("); //'&{0}' expected
			} else {
				if (nextSymbol() != IDENTIFIER_SYM) {
					error(XDEF.XDEF479);//Name of the type of exception expected
				} else {
					if (!"Exception".equals(_idName)) {
						//Name of the type of exception expected
						error(XDEF.XDEF479);
					}
					if (nextSymbol() != IDENTIFIER_SYM) {
						error(XDEF.XDEF454); //Variable name expected
					} else {
						spos = getLastPosition();
						// generate variable with thrown exception.
						varName = _idName;
					}
				}
			}
		}
		_g.addVariable(varName, XD_EXCEPTION, (byte) 'L', spos);
		_g._sp = 0;
		_g._cstack[0] = -1;
		_g._tstack[0] = XD_EXCEPTION;
		_g.genST(varName);
		checkNextSymbol(RPAR_SYM);
		if (_sym != BEG_SYM) {
			error(XDEF.XDEF410, "{"); //'&{0}' expected
		}
		setDebugEndPosition(dx);
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		jmp.setParam(_g._lastCodeIndex + 1);
		_wasBreak &= allReturn;
		_wasContinue &= allContinue;
		_wasReturn &= allReturn;
		closeBlock();
		_catchItem = catchItem;
	}

	/** Throw statement. */
	private void throwStatement() {
		if (_wasReturn | _wasContinue | _wasBreak) {
			error(XDEF.XDEF453); //Unreachable statement
		}
		int dx = addDebugInfo(true);
		nextSymbol();
		int sp = _g._sp;
		expression();
		setDebugEndPosition(dx);
		if (_g._sp > sp && _g._tstack[_g._sp] == XD_EXCEPTION) {
			// generate throw RuntimeException
			_g.addCode(new CodeI1(XD_VOID, THROW_EXCEPTION), -1);
		} else {
			error(XDEF.XDEF423, "Exception");//Value of type '&{0}' expected
		}
		_wasReturn = true;
	}

	/** Parse expression in parenthesis and generate conditioned jump.
	 * If result of expression is a constant value result of the method is
	 * fixed jump or <tt>null</tt>.
	 * <p>parExpression::= "(" expression ")"</p>
	 * @param boolean jumpCondition;
	 * @return conditioned jump, fixed jump, or <tt>null</tt>;
	 */
	private CompileJumpVector parBoolExpression(final boolean jumpCondition) {
		checkSymbol(LPAR_SYM); // '('
		CompileJumpVector jumpVector = boolExpression(jumpCondition);
		checkSymbol(RPAR_SYM); // ')'
		return jumpVector;
	}

	/** Compile method declaration.
	 * @param resultType code of result type of a method.
	 * @param name name of method.
	 * @param spos source position where the method was declared.
	 */
	private void compileMethodDeclaration(final short resultType,
		final String name,
		final SPosition spos) {
		byte mode = (CompileBase.NO_MODE | CompileBase.ANY_MODE);
		initCompilation(mode, resultType);
		CodeI1 initCode = compileMethodParamsDeclaration(resultType, name,spos);
		compileMethodBody(resultType != XD_VOID, initCode);
	}

	/** Compile method parameters.
	 * @param resultType code of result type of a method.
	 * @param name name of method.
	 * @param spos source position where the method was declared.
	 * @return array with parameter names.
	 */
	private CodeI1 compileMethodParamsDeclaration(final short resultType,
		final String name,
		final SPosition spos) {
		ArrayList<String> paramNames = new ArrayList<String>();
		ArrayList<SPosition> spositions = new ArrayList<SPosition>();
		int numPar = 0;
		DefContainer keyParams = null;
		if (checkSymbol(LPAR_SYM) && _sym != RPAR_SYM) {
			while (true) {
				_g._sp = -1;
				_g._spMax = 0;
				short paramType;
				if (_sym == MOD_SYM) { // key paramater
					if (isXMLName((byte) 10)) {
						if (keyParams == null) {
							keyParams = new DefContainer();
						}
						if (nextSymbol() == EQ_SYM) {
							nextSymbol();
						} else {
							error(XDEF.XDEF412); //Type identifier expected
						}
						XDValue val;
						if (_sym == CONSTANT_SYM) {
							val = _parsedValue;
							nextSymbol();
						} else if (_sym == IDENTIFIER_SYM
							&& (paramType = CompileBase.getTypeId(_idName))>0) {
							val = DefNull.genNullValue(paramType);
						} else {
							val = new DefNull();
							error(XDEF.XDEF412); //Type identifier expected
						}
						keyParams.setXDNamedItem(name, val);
					} else {
						error(XDEF.XDEF412); //Type identifier expected
					}
					if (_sym != COMMA_SYM) { // ','
						break;
					}
					nextSymbol();
					continue;
				}
				if (_sym != IDENTIFIER_SYM) {
					error(XDEF.XDEF412); //Type identifier expected
					if (_sym == COMMA_SYM) {
						nextSymbol();
						continue;
					}
					paramType = CompileBase.XD_UNDEF;
				} else if ((paramType = CompileBase.getTypeId(_idName)) <= 0) {
					error(XDEF.XDEF412); //Type identifier expected
					nextSymbol();
					if (_sym != IDENTIFIER_SYM) {
						if (_sym == COMMA_SYM) {
							nextSymbol();
							continue;
						}
					}
					paramType = CompileBase.XD_UNDEF;
				} else {
					nextSymbol();
				}
				if (_sym == IDENTIFIER_SYM) {
					String pname = _idName;
					SPosition sp = getLastPosition();
					if (CompileBase.getTypeMethod(CompileBase.NOTYPE_VALUE_ID,
						pname) != null) {
						error(XDEF.XDEF419); //Name of parameter expected
					} else {
						if (paramNames.indexOf(pname) >= 0) {
							//Parameter redefinition of &{0}
							error(XDEF.XDEF420, pname);
						} else {
							paramNames.add(pname);
							spositions.add(sp);
							_g._tstack[numPar++] = paramType;
						}
					}
					nextSymbol();
				} else {
					error(XDEF.XDEF419); //Name of parameter expected
				}
				if (_sym != COMMA_SYM) { // ','
					break;
				}
				nextSymbol();
			}
		}
		checkSymbol(RPAR_SYM);
		if (keyParams != null) {
			paramNames.add("#");
			_g._tstack[numPar++] = XD_CONTAINER;
		}
		String[] params = new String[numPar];
		paramNames.toArray(params);
		short[] paramTypes = new short[numPar];
		for (int i = 0; i < numPar; i++) {
			paramTypes[i] = _g._tstack[i];
			_g.addVariable(params[i],
				paramTypes[i],(byte)'L',spositions.get(i)).setInitialized(true);
		}
		CodeI1 initCode = paramTypes.length == 0 ?
			new CodeI1(XD_VOID, INIT_NOPARAMS_OP, 0) :
			new CodeI2(XD_VOID, INIT_PARAMS_OP, 0, paramTypes.length);
		_g.addCode(initCode, 0);
		int address = _g._lastCodeIndex;
		_popNumParams = paramTypes.length;
		_g.addMethod(resultType, name, address, paramTypes, _g._mode, spos);
		return initCode;
	}

	/** Compile method body.
	 * @param hasResult if method must return a value.
	 * @param initCode initial code of method (parameters as local variables).
	 */
	private void compileMethodBody(final boolean hasResult,
		final CodeI1 initCode) {
		if (_sym == BEG_SYM) {
			nextSymbol();
			_wasContinue = false;
			_wasBreak = false;
			_wasReturn = false;
			while (_sym != END_SYM) {
				if (!statement()) {
					break;
				}
			}
			checkSymbol(END_SYM); // ';'
			if (!_wasReturn) {
				if (!hasResult) { //add return statement
					_g.addCode(new CodeI1(
						XD_VOID, RET_OP, _popNumParams), 0);
				} else {
					error(XDEF.XDEF421); //Missing 'return' statement
				}
			}
		} else {
			error(XDEF.XDEF410, "{"); //'&{0}' expected
		}
		_popNumParams = -1;
		initCode.setParam(_g._localVariablesMaxIndex+1); // local variables size
		_g._localVariablesMaxIndex = -1;
		_g._localVariables.clear();
	}

	/** Compile lexicon specification
	 * @param source source data.
	 * @param defName the name of X-definition.
	 * @param lang language of lexicon.
	 * @param deflt default language.
	 * @param xp XDPool object.
	 * @param languages List of languages in this lexicon.
	 */
	final void compileLexicon(final SBuffer source,
		final String defName,
		final SBuffer lang,
		final SBuffer deflt,
		final XDPool xp,
		final List<Map<String,String>> languages) {
		setSource(source, defName, XConstants.XD31, null);
		if (lang == null) {
			error(XDEF.XDEF410, "language");//'&{0}' expected
			return;
		}
		String language;
		try {
			language = SUtils.getISO3Language(lang.getString());
		} catch (Exception ex) {
			error(lang, XDEF.XDEF410, "language");//'&{0}' expected
			return;
		}
		Map<String,String> props = null;
		for (Map<String,String> p : languages) {
			if (language.equals(p.get(".language$"))) {
				props = p;
				break;
			}
		}
		if (props == null) {
			props = new LinkedHashMap<String,String>();
			props.put("%{language}", language);
			languages.add(props);
		}
		skipBlanksAndComments();
		if (deflt != null) {
			String s = deflt.getString();
			if (s.equalsIgnoreCase("yes")) {
				if (!eos()) {
					error(deflt, XDEF.XDEF260);//Text value is not allowed here
				}
				props.put("%{default}", "!");
				return;
			}
			if (!s.equalsIgnoreCase("no")) {
				error(deflt, XDEF.XDEF410, "yes or no");//'&{0}' expected
			}
		}
		if (eos()) {
			error(XDEF.XDEF148); //Specification of lexicon values expected
		}
		StringBuilder sb = new StringBuilder();
		for (;;) {
			sb.setLength(0);
			char c = getCurrentChar();
			SPosition spos = getPosition();
			while(c > ' ' && c != '=') {
				sb.append(c); c = nextChar();
			}
			String key = sb.toString().trim();
			if (nextSymbol() == ASSGN_SYM) {
				if (props.containsKey(key)) {
					//The reference alias in lexicon must be unique: &{0}
					error(spos,XDEF.XDEF147, key);
				}
				if (xp.findModel(key) == null) {
					//Invalid reference of lexicon item &{0} for language &{1}
					error(spos, XDEF.XDEF150, key, language);
				}
				if (nextSymbol() == IDENTIFIER_SYM) {
					props.put(key, _idName);
				} else if (_sym == CONSTANT_SYM) {
					if (_parsedValue == null
						|| _parsedValue.getItemId() != XD_STRING) {
						error(XDEF.XDEF216);//Unexpected character
					} else {
						props.put(key, _parsedValue.stringValue());
					}
				}
				if (eos()) {
					break;
				}
			} else {
				break;
			}
			skipBlanksAndComments();
			if (eos()) {
				break;
			}
		}
		if (!eos()) {
			error(XDEF.XDEF216);//Unexpected character
		}
	}

	@SuppressWarnings("deprecation")
	/** Compile BNF grammar.
	 * @param sName SBuffer with name of BNF grammar variable.
	 * @param sExtends SBuffer with name of BNF grammar to be extended.
	 * @param source SBuffer BNF grammar.
	 * @param defName name of X-definition.
	 * @param local true if it is in the declaration part with the local scope.
	 * @param nsPrefixes table of name space prefixes.
	 */
	final void compileBNFGrammar(final SBuffer sName,
		final SBuffer sExtends,
		final SBuffer source,
		final String defName,
		final boolean local,
		final Map<String, Integer> nsPrefixes) { // namespace
		setSource(sName, defName, XConstants.XD20, nsPrefixes);
		String name = sName.getString();
		if (local) {
			name = defName+'#'+name;
		}
		CompileVariable var = _g.getVariable(name);
		if (var == null) { // OK
			var = _g.addVariable(name, XD_BNFGRAMMAR, (byte) 'G', sName);
		} else {// ERROR
			//Repeated declaration of variable '&{0}'&{#SYS000}&{1}
			//({; (already declared: }{)}
			_g.putRedefinedError(sName, XDEF.XDEF450,
				sName.getString(), var.getSourcePosition());
		}
		DefBNFGrammar di = null;
		int extVar = -1;
		boolean isFinal = true;
		if (sExtends != null) { // extension
			setSourceBuffer(sExtends);
			String s = sExtends.getString();
			CompileVariable evar;
			if ((evar = _g.getVariable(s)) == null) {
				//BNF grammar '&{0}' is not available for extension '&{1}'
				error(sExtends, XDEF.XDEF105, s, name);
				return;
			} else if (evar.getType() != XD_BNFGRAMMAR) {
				error(XDEF.XDEF457,//Incompatible types&{0}{: }
					CompileBase.getTypeName(evar.getType()) + ",BNFGrammar");
			} else {
				extVar = evar.getOffset();
				isFinal = evar.isFinal();
				di = (DefBNFGrammar) evar.getValue();
				if (di.grammarValue() == null) {
					//Variable '&{0}' might be not initialized
					error(sExtends, XDEF.XDEF464, s);
					return;
				}
			}
		}
		SBuffer s = source == null ? new SBuffer("") : source;
		int actAdr = _g._lastCodeIndex;
		CodeI1 lastStop = _g.getLastStop();
		DefBNFGrammar dd;
		try {
			dd = new DefBNFGrammar(di, extVar, s, getReportWriter());
		} catch (SRuntimeException ex) {
			Report r = ex.getReport();
			error(sName, r.getMsgID(), r.getText(), r.getModification());
			dd = new DefBNFGrammar();
			dd.setParam(extVar);
			dd.setSource(s.getString());
		}
		var.setValue(dd);
		var.setFinal(isFinal);
		_g.genLDC(dd);
		_g.genST(name);
		_g.addInitCode(actAdr, lastStop);

	}

	/** Compile external method declaration.
	 * @param local true if it is in the declaration part with the local scope.
	 */
	final void compileExtMethod(final boolean local) {
		ClassLoader cl = getClassLoader();
		if (_sym != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF412, ";"); //Type identifier expected
			return;
		}
		String s = _idName;
		// in s we have now the name of type of result or "void"
		if (nextSymbol() == LSQ_SYM) {
			if (nextSymbol() != RSQ_SYM) {
				errorAndSkip(XDEF.XDEF410, ']', ";"); //'&{0}' expected
				return;
			}
			nextSymbol();
			s += "[]";
		}
		boolean wasError = false;
		String rType = s; // result type of method
		short resultType = CompileBase.getClassTypeID(rType, cl);
		if (resultType == CompileBase.XD_UNDEF) {
			error(XDEF.XDEF412);//Type identifier expected
			wasError = true;
		}
		// Now we read the classified name of method.
		// The symbol is an identifier - either class name, or class with
		// method name; class name can contain the used language name
		if (_sym != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF220, ";"); //Qualified method name expected
			return;
		}
		//classified name of metohd
		String cName = _idName; // className
		Class<?> mClass;
		try {
			// the class where is the method
			mClass = Class.forName(cName, false, cl);
		} catch (ClassNotFoundException ex) {
			if (!_g._ignoreUnresolvedExternals) {
				error(XDEF.XDEF228, cName); //Class '&{0}' not exists
				wasError = true;
			}
			mClass = null;
		}
		if (nextSymbol() != DOT_SYM){
			errorAndSkip(XDEF.XDEF220, ";"); //Qualified method name expected
			return;
		}
		if (nextSymbol() != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF220, ";"); //Qualified method name expected
			return;
		}
		String methodName = _idName;
		String aliasName = methodName;//we prepare alias same as method name
		// parse the list of types of parameters
		if (nextSymbol() != LPAR_SYM) {
			errorAndSkip(XDEF.XDEF410, '(', ";"); //'&{0}' expected
			return;
		}
		ArrayList<String> params = new ArrayList<String>();
		String classPar = null;
		// read first parameter
		if (nextSymbol() == IDENTIFIER_SYM) {
			s = _idName;
			nextSymbol();
			if (s.startsWith("org.xdef.proc.")) {
				s = s.substring("org.xdef.proc.".length());
			}
			if (s.startsWith("org.xdef.")) {
				s = s.substring("org.xdef.".length());
			}
			if ("XXElement".equals(s) || "XXData".equals(s)
				|| "XXNode".equals(s)) {
				classPar = "org.xdef.proc." + s;
			} else {
				if ("XDValue".equals(s) && _sym == LSQ_SYM) {
					if (nextSymbol() != RSQ_SYM) {
						errorAndSkip(XDEF.XDEF410, ']', ";"); //'&{0}' expected
						return;
					}
					if (nextSymbol() == COMMA_SYM) {
						//After XDValue[] parameter can't follow
						//other parameter
						error(XDEF.XDEF223);
						wasError = true;
					}
					params.add(null);
				} else {
					short paramType;
					if ("byte".equals(s) && _sym == LSQ_SYM) {
						if (nextSymbol() != RSQ_SYM) {
							 //'&{0}' expected
							errorAndSkip(XDEF.XDEF410, ']', ";");
							return;
						}
						nextSymbol();
						paramType = XD_BYTES;
						s += "[]";
					} else {
						paramType = CompileBase.getClassTypeID(s, cl);
					}
					if (paramType == CompileBase.XD_UNDEF) {
						error(XDEF.XDEF412); //Type identifier expected
						wasError = true;
					}
					params.add(s);
				}
			}
			// read following parameters
			while (_sym == COMMA_SYM) {
				if (nextSymbol() != IDENTIFIER_SYM) {
					errorAndSkip(XDEF.XDEF412, ",);");//Type identifier expected
					wasError = true;
					continue;
				}
				s = _idName;
				nextSymbol();
				if (s.startsWith("org.xdef.proc.")) {
					s = s.substring("org.xdef.proc.".length());
				}
				if (s.startsWith("org.xdef.")) {
					s = s.substring("org.xdef.".length());
				}
				// check first parameter, it can be XXNode
				if (("XDValue".equals(s)) && _sym == LSQ_SYM) {
					if (nextSymbol() != RSQ_SYM) {
						 //'&{0}' expected
						errorAndSkip(XDEF.XDEF410, ']', ";");
						return;
					}
					if (nextSymbol() == COMMA_SYM) {
						//After XDValue[] parameter can't follow
						//other parameter
						error(XDEF.XDEF223);
						wasError = true;
					}
					params.add(null);
				} else {
					short paramType;
					if ("byte".equals(s) && _sym == LSQ_SYM) {
						if (nextSymbol() != RSQ_SYM) {
							 //'&{0}' expected
							errorAndSkip(XDEF.XDEF410, ']', ";");
							return;
						}
						nextSymbol();
						paramType = XD_BYTES;
						s += "[]";
					} else {
						paramType = CompileBase.getClassTypeID(s, cl);
					}
					if (paramType == CompileBase.XD_UNDEF) {
						error(XDEF.XDEF412); //Type identifier expected
					}
					params.add(s);
				}
			}
		}
		if (_sym != RPAR_SYM) {
			errorAndSkip(XDEF.XDEF410, ')', ")"); //'&{0}' expected
			return;
		}
		// check the alias name is specified
		if (nextSymbol() == IDENTIFIER_SYM && "as".equals(_idName)) {
			if (nextSymbol() == IDENTIFIER_SYM) {
				aliasName = _idName;
				nextSymbol();
			} else {
				errorAndSkip(XDEF.XDEF362, ";"); //Alias name expected
				return;
			}
		} else if (_sym != SEMICOLON_SYM && _sym != END_SYM
			&& _sym != NOCHAR) {
			errorAndSkip(XDEF.XDEF410, ";}", ";"); //'&{0}' expected
		}
		if (wasError) {
			return;
		}
		if (mClass != null) {
			Method method = // the Java reflection method object
				getExtMethod(cl, mClass, methodName, classPar, params);
			String mName = rType + ' ' + methodName + "(";
			for (int i = 0; i < params.size(); i++) {
				if (i > 0) {
					mName += ',';
				}
				mName = mName + params.get(i);
			}
			mName += ')';
			if (method != null) {
				Method m;
				int modifiers;
				String localName = local && _actDefName != null ?
					_actDefName + '#' + aliasName : aliasName;
				if (resultType !=
					CompileBase.getClassTypeID(method.getReturnType())) {
					//Result type of the extenal method '&{0}' differs
					error(XDEF.XDEF226, mName);
				} else if (((modifiers = method.getModifiers())
					& Modifier.STATIC)==0 && (modifiers & Modifier.PUBLIC)==0) {
					//External method '&{0}' must be 'static' and 'public'
					error(XDEF.XDEF466, mName);
				} else if ((m=_g.addDeclaredMethod(localName, method))!=null
					&& !m.equals(method)) {
					//Ambiguous redeclaration of the external method&{0}
					error(XDEF.XDEF227, aliasName);
				}
			} else {
				//External method '&{0}' was not found in class '&{1}'
				error(XDEF.XDEF225, mName, cName);
			}
		}
	}

	/** Get class corresponding to class name of parameter of method.
	 * @param paramClassName name of class of parameter.
	 * @param cl actual class loader.
	 * @return class corresponding to class name of parameter of method.
	 */
	private static Class<?> getClassParam(final String paramClassName,
		final ClassLoader cl) {
		if (paramClassName.indexOf('.') >= 0) {
			try {
				return Class.forName(paramClassName, false, cl);
			} catch (ClassNotFoundException ex) {}
		}
		if ("Long".equals(paramClassName)) {
			return Long.class;
		} else if ("int".equals(paramClassName)) {
			return Integer.TYPE;
		} else if ("Integer".equals(paramClassName)) {
			return Integer.class;
		} else if ("short".equals(paramClassName)) {
			return Short.TYPE;
		} else if ("Short".equals(paramClassName)) {
			return Short.class;
		} else if ("byte".equals(paramClassName)) {
			return Byte.TYPE;
		} else if ("Byte".equals(paramClassName)) {
			return Byte.class;
		} else if ("Double".equals(paramClassName)) {
			return Double.class;
		} else if ("float".equals(paramClassName)) {
			return Float.TYPE;
		} else if ("Float".equals(paramClassName)) {
			return Float.class;
		} else if ("boolean".equals(paramClassName)) {
			return Boolean.TYPE;
		} else if ("Boolean".equals(paramClassName)) {
			return Boolean.class;
		} else if ("BigDecimal".equals(paramClassName)) {
			return BigDecimal.class;
		} else if ("String".equals(paramClassName)) {
			return String.class;
		}
		return CompileBase.getTypeClass(
			CompileBase.getClassTypeID(paramClassName, cl));
	}

	/** Get external method item.
	 * @param cl actual ClassLoader.
	 * @param mClass Class where the external method should be.
	 * @param methodName name of method in the class.
	 * @param classPar Class name with 1.st parameter.
	 * @param params parameter names.
	 * @return Method object or null.
	 */
	private Method getExtMethod(final ClassLoader cl,
		final Class<?> mClass,
		final String methodName,
		final String classPar,
		final ArrayList<String> params) {
		Class<?> pars[];
		int numPar = params.size();
		if (classPar == null) {
			pars = new Class<?>[numPar];
			for (int i = 0; i < numPar; i++) {
				String s = params.get(i);
				if (s == null) {//array
					pars[i] = XDValue[].class;
				} else {
					if ((pars[i] = getClassParam(s, cl)) == null) {
						error(XDEF.XDEF228, s); //Class '&{0}' not exists
						return null;
					}
				}
			}
		} else {
			pars = new Class<?>[numPar + 1];
			try {
				pars[0] = Class.forName(classPar, false, cl);
			} catch (ClassNotFoundException ex) {
				error(XDEF.XDEF228, classPar); //Class '&{0}' not exists
				return null;
			}
			for (int i = 0; i < numPar; i++) {
				String s = params.get(i);
				if (s == null) {//array
					pars[i + 1] = XDValue[].class;
				} else {
					if ((pars[i+1] = getClassParam(s, cl)) == null) {
						error(XDEF.XDEF228, s); //Class '&{0}' not exists
						return null;
					}
				}
			}
		}
		return _g.getExtMethod(mClass, methodName, pars);
	}

	/** Compile declaration part - methods, types, variables and
	 * thi init sections.
	 * @param local true if it is a declaration with the local scope.
	 * within a X-definition.
	 */
	final void compileDeclaration(final boolean local) {
		nextSymbol();
		while (_sym != NOCHAR) {
			if (_sym == SEMICOLON_SYM) {
				nextSymbol();
				continue;
			}
			_g._sp = -1;
			boolean isFinal = false, isExternal = false;
			do {
				if (_sym == FINAL_SYM) {
					if (isFinal) {
						//Duplicate specification of &{0}
						error(XDEF.XDEF118,"final");
					}
					isFinal = true;
					nextSymbol();
				} else if (_sym == EXTERNAL_SYM) {
					if (isExternal) {
						//Duplicate specification of &{0}
						error(XDEF.XDEF118, "external");
					}
					isExternal = true;
					nextSymbol();
				}
			} while (_sym == FINAL_SYM || _sym == EXTERNAL_SYM);
			switch(_sym) {
				case IDENTIFIER_SYM: {
					String name = _idName;
					if ("method".equals(name)) {
						if (isFinal) {
							//&{0}' is not allowed here
							lightError(XDEF.XDEF411, "final");
						}
						if (nextSymbol() == BEG_SYM) {
							while (nextSymbol() == SEMICOLON_SYM) {}
							while(_sym != END_SYM && !eos()) {
								compileExtMethod(local);
								// method list separator
								while (_sym == SEMICOLON_SYM) {
									nextSymbol();
								}
							}
							if (_sym != END_SYM) {
								error(XDEF.XDEF410, '}');//'&{0}' expected
							} else {
								nextSymbol();
							}
						} else {
							compileExtMethod(local);
							if (_sym == SEMICOLON_SYM) {
								while (nextSymbol() == SEMICOLON_SYM) {}
							} else if (!eos()) {
								error(XDEF.XDEF410, ';');//'&{0}' expected
							}
						}
						continue;
					}
					char sym = nextSymbol();
					if (sym == IDENTIFIER_SYM) { // previous one was a type
						short varType = getTypeCode(name);
						name = _idName;
						SPosition spos = getLastPosition();
						if (local) {
							name = _actDefName + '#' + name;
						}
						if (nextSymbol() == LPAR_SYM) { // method
							compileMethodDeclaration(varType, name, spos);
							continue;
						} else if (_sym == NOCHAR || _sym == SEMICOLON_SYM
							|| _sym == COLON_SYM || _sym == ASSGN_SYM
							|| _sym == COMMA_SYM) {
							// declaration of variable
							int actAdr = _g._lastCodeIndex;
							CodeI1 lastStop = _g.getLastStop();
							varDeclaration(varType,
								name, isFinal, isExternal, (byte) 'G', spos);
							_g.addInitCode(actAdr, lastStop);
							if(!checkSemicolon(String.valueOf(IDENTIFIER_SYM))){
								nextSymbol();
							}
							continue;
						}
					}
					break;
				}
				case TYPE_SYM:
					// compile type or uniqueSet
				   compileType((byte) 'G', local);
				   continue;
				case UNIQUE_SET_SYM:
					// compile type or uniqueSet
				   compileUniqueset((byte) 'G', local);
				   continue;
			}
			error(XDEF.XDEF487); //Variable or method declaration expected
			while (nextSymbol() != NOCHAR) {// skip to type identifier or EOS
				if (_sym==IDENTIFIER_SYM && CompileBase.getTypeId(_idName)>=0) {
					break;
				} else { // ???
					nextChar(); // ???
				}
			}
		}
	}

	/** Compile validation type as a method.
	 * @param varKind variable kind ('G' .. global or 'X' .. model).
	 * @param local true if it is in the declaration part with the local scope.
	 * @param name the name of declared type.
	 * @param spos source position where the type was declared.
	 */
	final void compileType(final byte varKind,
		final boolean local,
		final String name,
		final SPosition spos) {
		CodeI1 jmp = null;
		int addr;
		short type;
/*XXX*
		int srcPos = getIndex();
/*XXX*/
		switch (_sym) {
			case IDENTIFIER_SYM: { // type method
/*XXX*
				String typeName =
					_sym==IDENTIFIER_SYM ? _idName : symToName(_sym);
//				CodeS1 info = new CodeS1(XD_STRING, TYPEINFO_CODE, 0, "");
//				_g.addCode(info, 0);
/*XXX*/
				CompileVariable rVar =
					(CompileVariable) _g._globalVariables.getXVariable(_idName);
				int dx = addDebugInfo(true);
				if (rVar==null && varKind == 'X') {
					if (!expression() || _g._tstack[_g._sp] != XD_PARSER) {
						//Value of type &{0} expected
						errorAndSkip(XDEF.XDEF423,
							String.valueOf(END_SYM), "Parser");
					} else {
						CompileVariable v = _g.addVariable(name,
							_g._tstack[_g._sp], varKind, spos);
						if (_g._cstack[_g._sp] >= 0) {
							v.setValue(_g._code.get(_g._cstack[_g._sp]));
							v.setFinal(true);
						}
						_g.genST(name);
					}
					return;
				}
				if (varKind == 'X') {
					_g.addJump(jmp = new CodeI1(XD_VOID, JMP_OP));
				}
				addr = compileCheckMethod("").getParam();
				if (rVar!=null && rVar.getType()==CompileBase.PARSEITEM_VALUE) {
					type = rVar.getParseResultType();
				} else {
					type = XD_STRING;
					if (addr + 2 == _g._lastCodeIndex) {
						if (_g._code.get(addr).getCode() == LD_CONST
							&& _g._code.get(addr).getItemId() == XD_PARSER
							&& _g._code.get(addr+1).getCode() == PARSE_OP
							&& _g._code.get(addr+2).getCode() == STOP_OP) {
							XDParser p = (XDParser) _g._code.get(addr);
							type = p.parsedType();
							if (p != null) {// set declared type name
								p.setDeclaredName(name);
							}
						}
					}
				}
				setDebugEndPosition(dx);
				break;
			}
			case BEG_SYM: { // explicit declaration of type method
				skipBlanksAndComments();
				if (isToken("parse")) {  // X-definition version 2.0
					skipBlanksAndComments();
					if (isChar(':')) {
						nextSymbol();
						compileType(varKind, local, name, spos);
						if (_sym == SEMICOLON_SYM) {
							nextSymbol();
						}
						checkSymbol(END_SYM);
						if (_xdVersion >= XConstants.XD31) {
							//Type declaration format "{parse: ...}" is deprecated;
							//please use just validation method call
							if (_xdVersion == XConstants.XD31) {
								warning(XDEF.XDEF997);
							} else {
								error(XDEF.XDEF997);
							}
						}
						return;
					} else {
						_sym = IDENTIFIER_SYM;
						_idName = "parse";
					}
//				} else if (_xdVersion < XConstants.XD31) {//old X-def versions
//					errorAndSkip(XDEF.XDEF410, //'&{0}' expected
//						String.valueOf(END_SYM), "parse:");
//					return;
				}
				if (varKind == 'X') {
					_g.addJump(jmp = new CodeI1(XD_VOID, JMP_OP));
				}
				int dx = addDebugInfo(true);
				addr = compileCheckMethod("").getParam();
				type = XD_STRING;
				setDebugEndPosition(dx);
				break;
			}
			default:
				// Expected declaration 'type' or 'uniqueSet'
				errorAndSkip(XDEF.XDEF489,String.valueOf(END_SYM));
				_g.addVariable(name,
					CompileBase.PARSEITEM_VALUE, varKind, spos);
				return;
		}
		if (varKind == 'X') { // ModelVariable
			jmp.setParam(_g._lastCodeIndex+1);
		}
		CompileVariable var =
			_g.addVariable(name, CompileBase.PARSEITEM_VALUE, varKind, spos);
		var.setParseMethodAddr(addr);
		var.setParseResultType(type);
	}

	/** Compile check type as a method.
	 * @param varKind variable kind ('G' .. global or 'X' .. model).
	 * @param local true if it is in the declaration part with the local scope.
	 */
	final void compileType(final byte varKind, final boolean local) {
		String name;
		SPosition spos;
		if (nextSymbol() != IDENTIFIER_SYM) {
			error(XDEF.XDEF329); //Identifier expected
			name = CompileBase.genErrId(); // "UNDEF$$$";
			spos = null;
		} else {
			name = _idName;
			spos = getLastPosition();
			if (local) {
				name = _actDefName + '#' + name;
			}
		}
		if (varKind != 'X' && _g.getVariable(name) != null) {
			//Repeated declaration of type '&{0}'&{#SYS000}&{1}
			//({; (already declared: }{)}
			_g.putRedefinedError(null, XDEF.XDEF470,
				name, _g.getVariable(name).getSourcePosition());
			name = CompileBase.genErrId(); // "UNDEF$$$";
		}
		nextSymbol();
		compileType(varKind, local, name, spos);
	}

	/** Compile "var" section of the uniqueSet declaration (the named values).
	 * @param uniquesetName the name of the uniqueSet object.
	 * @param varMap the map with named values.
	 * @param keyItems key items (to check if tne name already was used)
	 * @return the map with variables.
	 */
	private void uniquesetVar(final String uniquesetName,
		final Map<String,Short> varMap,
		final List<CodeUniqueset.ParseItem> keyItems) {
		for(;;) {
			short type = (nextSymbol() == IDENTIFIER_SYM) ?
				CompileBase.getTypeId(_idName) : -1;
			if (type <= 0) {
				//Expected type identifier of variable
				errorAndSkip(XDEF.XDEF458,
					String.valueOf(END_SYM) + SEMICOLON_SYM);
				return;
			}
			if (nextSymbol() != IDENTIFIER_SYM) {
				//Name of named item expected
				errorAndSkip(XDEF.XDEF418,
					String.valueOf(END_SYM) + SEMICOLON_SYM);
				return;
			}
			for (CodeUniqueset.ParseItem x: keyItems) {
				if (_idName.equals(x.getParseName())) {
					//Redefinition of the named value specification &{0} in the
					//declaration of uniqueSet &{1}
					error(XDEF.XDEF146, _idName, uniquesetName);
				}
			}
			if (varMap.put(_idName, type) != null) {
				//Redefinition of the named value specification &{0} in the
				//declaration of uniqueSet &{1}
				error(XDEF.XDEF146, _idName, uniquesetName);
			}
			if (nextSymbol() != COMMA_SYM) {
				break;
			}
		}
		if (_sym == SEMICOLON_SYM) {
			nextSymbol();
		}
	}

	/** Compile the key item of the uniqueSet declaration (the named values).
	 * @param uniquesetName name of the uniqueSet object.
	 * @param varMap the map with named values.
	 * @param keyItems key items (to check if the name already was used)
	 * @return true if the key item was compiled.
	 */
	private boolean uniquesetKeyItem(final String uniquesetName,
		final Map<String,Short> varMap,
		final List<CodeUniqueset.ParseItem> keyItems) {
		if (_sym != IDENTIFIER_SYM) {
			//Name of named item expected
			errorAndSkip(XDEF.XDEF418, String.valueOf(END_SYM));
			return false;
		}
		String keyName = _idName;
		int ndx = keyName.lastIndexOf(':');
		if (ndx > 0 || nextSymbol() == COLON_SYM) {
			boolean optional;
			if (_sym == COLON_SYM) {
				nextSymbol();
				if (optional = (_sym==OPTIONAL_SYM || _sym==ASK_SYM)) {
					nextSymbol();
				}
			} else {
				_idName = keyName.substring(ndx + 1);
				if ("optional".equals(_idName)) {
					optional = true;
					nextSymbol();
				} else {
					optional = false;
				}
				keyName = keyName.substring(0, ndx);
			}
			if (nameIsDeclared(keyName, varMap, keyItems)) {
				//Redefinition of section &{0}
				error(XDEF.XDEF324, uniquesetName + "." + keyName);
				return false;
			} else {
				int dx = addDebugInfo(true);
				CodeS1 checkMethod = compileCheckMethod("");
				int addr = checkMethod.getParam();
				setDebugEndPosition(dx);
				keyItems.add(new CodeUniqueset.ParseItem(keyName,
					checkMethod.stringValue(),
					addr, keyItems.size(), XD_STRING, optional));
			}
		}
		return true;
	}

	/** Check if name from the argument name is declared in varMap or keyItems
	 * @param name name to be checked.
	 * @param varMap map with declared named variables of uniqueSet object.
	 * @param keyItems list of declared key items of uniqueSet object.
	 * @return true if the name exists in varMap of keyItems.
	 */
	private boolean nameIsDeclared(final String name,
		final Map<String,Short> varMap,
		final List<CodeUniqueset.ParseItem> keyItems) {
		for (CodeUniqueset.ParseItem item: keyItems) {
			if (name.equals(item.getParseName())) {
				return true;
			}
		}
		return varMap.containsKey(name);
	}

	/** Compile declaration of a uniqueSet.
	 * @param varKind variable kind ('G' .. global or 'X' .. model).
	 * @param local true if it is in the declaration part with the local scope.
	 */
	final void compileUniqueset(final byte varKind, final boolean local) {
		short varType;
		String uniquesetName;
		SPosition spos = null;
		if (nextSymbol() != IDENTIFIER_SYM) {
			error(XDEF.XDEF329); //Identifier expected
			uniquesetName = CompileBase.genErrId(); // "UNDEF$$$";
		} else {
			uniquesetName = _idName;
			spos = getLastPosition();
			if (local) {
				uniquesetName = _actDefName + '#' + uniquesetName;
			}
		}

		if (varKind != 'X' && _g.getVariable(uniquesetName) != null) {
			//Repeated declaration of variable '&{0}'&{#SYS000}&{1}
			//({; (already declared: }{)}
			_g.putRedefinedError(null, XDEF.XDEF450,
				uniquesetName,
				_g.getVariable(uniquesetName).getSourcePosition());
			uniquesetName = CompileBase.genErrId(); // "UNDEF$$$";
		}
		List<CodeUniqueset.ParseItem> keyItems =
			new ArrayList<CodeUniqueset.ParseItem>();
		Map<String,Short> varMap = new HashMap<String, Short>();
		CodeI1 jmp = null;
		if (varKind == 'X') {
			_g.addJump(jmp = new CodeI1(XD_VOID, JMP_OP));
		}
		switch (nextSymbol()) {
			case IDENTIFIER_SYM: { // type method
				varType = CompileBase.UNIQUESET_VALUE;
				int dx = addDebugInfo(true);
				CodeS1 checkMethod = compileCheckMethod("");
				int addr = checkMethod.getParam();
				setDebugEndPosition(dx);
				short type = XD_STRING;
				keyItems.add(new CodeUniqueset.ParseItem("",
					checkMethod.stringValue(),
					addr,
					keyItems.size(),
					type,
					false));
				break;
			}
			case BEG_SYM: { // explicit declaration of type method
				varType = CompileBase.UNIQUESET_M_VALUE;
				nextSymbol();
				while (_sym != END_SYM) {
					if (_sym == VAR_SYM) {
						uniquesetVar(uniquesetName, varMap, keyItems);
					} else if (_sym == IDENTIFIER_SYM) {
						uniquesetKeyItem(uniquesetName, varMap, keyItems);
					} else { // identified
						//In the uniqueSet is expected name of named item or
						// named variable declaration
						errorAndSkip(XDEF.XDEF418, String.valueOf(END_SYM));
						return;
					}
					if (_sym == SEMICOLON_SYM) {
						nextSymbol();
					}
				}
				checkSymbol(END_SYM);
				break;
			}
			default:
				// Expected declaration of uniqueSet
				errorAndSkip(XDEF.XDEF489,
					String.valueOf(END_SYM));
				_g.addVariable(uniquesetName,
					CompileBase.UNIQUESET_M_VALUE, varKind, spos);
				return;
		}
		if (varKind == 'X') { // ModelVariable
			jmp.setParam(_g._lastCodeIndex+1);
		}
		int keySize = keyItems.size();
		if (keySize == 0) {
			return; // an error should be reported in the code above.
		}
		CodeUniqueset.ParseItem[] keys =
			keyItems.toArray(new CodeUniqueset.ParseItem[keySize]);
		boolean namedKey = !keys[0].getParseName().isEmpty();
		CompileVariable var =
			_g.addVariable(uniquesetName, varType, varKind, spos);
		var.setParseMethodAddr(keys[0].getParseMethodAddr());
		var.setParseResultType(keys[0].getParsedType());
		CodeI1 lastStop = varKind == 'G' ? _g.getLastStop() : null;
		CodeUniqueset u = new CodeUniqueset(keys, uniquesetName);
		var.setValue(new DefLong(0));
		if (namedKey) {
			var.setValue(new DefLong(-1));
			var.setParseMethodAddr(-1);
			if (keys.length > 1) {
				var.setParseResultType(CompileBase.XD_UNDEF);
			}
			for (int i = 0; i < keys.length; i++) {
				CodeUniqueset.ParseItem key = keys[i];
				String keyName = key.getParseName();
				CompileVariable x = new CompileVariable(
					uniquesetName + "." + keyName,
					CompileBase.UNIQUESET_KEY_VALUE,
					var.getOffset(), varKind, spos);
				x.setCodeAddr(var.getCodeAddr());
				x.setKeyRefName(key.getDeclaredTypeName());
				if (varKind == 'G') {
					_g._globalVariables.addVariable(x);
				} else {
					_g._varBlock.addVariable(x);
				}
				x.setValue(new DefLong(i));
				x.setFinal(true);
				x.setInitialized(true);
				x.setParseMethodAddr(key.getParseMethodAddr());
				x.setParseResultType(key.getParsedType());
			}
			for (String keyName: varMap.keySet()) {
				CompileVariable x = new CompileVariable(
					uniquesetName + "." + keyName,
					CompileBase.UNIQUESET_NAMED_VALUE,
					var.getOffset(), varKind, spos);
				x.setCodeAddr(var.getCodeAddr());
				if (varKind == 'G') {
					_g._globalVariables.addVariable(x);
				} else {
					_g._varBlock.addVariable(x);
				}
				x.setParseResultType(varMap.get(keyName));
				x.setValue(new DefString(keyName));
				x.setInitialized(true);
				x.setFinal(true);
			}
		}
		int actAdr = _g._lastCodeIndex;
		_g.addCode(new CodeXD(u.getItemId(),
			UNIQUESET_NEWINSTANCE, actAdr, u), 1);
		_g.genST(var);
		if (varKind == 'G') {
			_g.addInitCode(actAdr, lastStop);
		}
	}

	/** Compile a validation method.
	 * @param keywords where to skip if a syntax error is reported.
	 * @return code with the address, type and referred name.
	 */
	final CodeS1 compileCheckMethod(final String keywords) {
		int sp = _g._sp  = -1;
		int start = _g._lastCodeIndex;
		CodeS1 result = new CodeS1(XD_STRING, (short) 0, start + 1, null);
		initCompilation(CompileBase.TEXT_MODE, XD_PARSERESULT);
		if (_sym == BEG_SYM) { // explicite code (method body)
			// generate call of following method
			CodeI1 call = new CodeI1(XD_BOOLEAN, CALL_OP, start + 3);
			_g.addCode(call, 0);
			_g.genStop();
			// generate code of method body
			CodeI1 initCode = new CodeI1(XD_VOID, INIT_NOPARAMS_OP, 0);
			_g.addCode(initCode, 0);
			_popNumParams = 0;
			compileMethodBody(true, initCode);
			result.setItemType(XD_STRING);
		} else { // check expression
			String typeName = _sym==IDENTIFIER_SYM ? _idName : symToName(_sym);
			int dx = addDebugInfo(false);
			if (!expression()) {
				//Action &{0} expected
				errorAndSkip(XDEF.XDEF426, keywords, "validation");
				_g._sp  = -1;
				result.setParam(-1);
				return result;
			}
			if (sp == _g._sp) {
				//Validation method or a value of type ParseResult
				//or boolean expected
				error(XDEF.XDEF107);
				result.setParam(-1);
				return result;
			}
			switch (_g._tstack[_g._sp]) {
				case XD_BNFRULE:
					_g.addCode(new CodeI1(XD_PARSERESULT, BNFRULE_PARSE, 1), 0);
					_g.genStop();
					break;
				case XD_ANY:
				case XD_PARSER: {
					if (getVariable(typeName) != null) {
						result.setParam2(typeName);
					}
					_g.addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1), 0);
					_g.genStop();
					break;
				}
				case XD_PARSERESULT: {
					CompileVariable var = getVariable(typeName);
					if (var != null) {
						String s = var.getKeyRefName();
						result.setParam2(s != null ? s : typeName);
					}
					_g.genStop();
					break;
				}
				case XD_BOOLEAN:
					_g.genStop();
					break;
				case CompileBase.PARSEITEM_VALUE:
					if (_g._lastCodeIndex == start + 1) {
						XDValue val = _g.getLastCodeItem();
						short code;
						if (CompileBase.PARSEITEM_VALUE == val.getItemId()
							&& ((code = val.getCode()) == LD_GLOBAL
							|| code == LD_LOCAL || code == LD_XMODEL)) {
							result.setParam2(typeName);
							_g.removeCodeFromIndexAndClearStack(start,
								sp, _g._spMax);
							String err = _g.genMethod(val.stringValue(), 0);
							if (err != null) {
								error(XDEF.XDEF443, err);//Unknown method:'&{0}'
							}
							_g._sp  = -1;
							return result;
						}
					}
					break;
				case CompileBase.XD_UNDEF:
					break;
				default:
					//Value of type '&{0}' expected
					error(XDEF.XDEF423, "boolean or ParseResult");
			}
			setDebugEndPosition(dx);
			if (_sym != END_SYM && _sym != COMMA_SYM && _sym != NOCHAR) {
				checkSemicolon(keywords);
			}
		}
		_g._sp  = -1;
		return result;
	}

	/** Compile "fixed" validation method.
	 * @param keywords where to skip if a syntax error is reported.
	 * @return address of generated method.
	 */
	final int compileFixedMethod(final String keywords) {
		int sp = _g._sp  = -1;
		int start = _g._lastCodeIndex;
		initCompilation(CompileBase.TEXT_MODE, XD_STRING);
		// generate as code of method
		CodeI1 initCode = new CodeI1(XD_VOID, INIT_NOPARAMS_OP, 0);
		_g.addCode(initCode, 0);
		if (_sym != BEG_SYM) {
			int dx = addDebugInfo(false);
			if (!expression()) {
				//Action &{0} expected
				errorAndSkip(XDEF.XDEF426, keywords, "fixed");
				_g._sp  = -1;
				return -1;
			}
			_g.topToString();
			if (sp == _g._sp || _g._tstack[_g._sp] != XD_STRING) {
				error(XDEF.XDEF423, "String"); //Value of type '&{0}' expected
			}
			_g.addCode(new CodeI1(XD_STRING, RETV_OP, 0), 0);
			_g.genStop();
			checkSemicolon(keywords);
			setDebugEndPosition(dx);
		} else { // method body declaration
			_popNumParams = 0;
			compileMethodBody(true, initCode);
		}
		_g._sp  = -1;
		return start + 1;
	}

	/** Sets the ClassLoader to load Java classes.
	 * @param loader ClassLoader to load Java classes.
	 */
	final void setClassLoader(final ClassLoader loader) {_classLoader = loader;}

	/** Get the ClassLoader used to load Java classes.
	 * @return ClassLoader used to load Java classes.
	 */
	final ClassLoader getClassLoader() {return _classLoader;}

////////////////////////////////////////////////////////////////////////////////
// Private classes
////////////////////////////////////////////////////////////////////////////////

	/** Contanis data assigneg to a block statement. */
	private static final class BlockInfo {
		/** Map with variable names. */
		private Map<String, CompileVariable> _variables;
		/** Array with break jumps. */
		private ArrayList<CodeI1> _breakJumps;
		/** The address where to contimue. */
		private int _continueAddr;
		/** Parent BlockInfo. */
		private BlockInfo _prevInfo;
		/** True if it contains break jumps. */
		private boolean _jumps;
		/** Last index pof variables. */
		private final int _variablesLastIndex;

		/** Create new instance of BlockInfo.
		 * @param jumps True if it contains break jumps.
		 * @param continueAddr The address where to contimue.
		 * @param prevInfo Parent BlockInfo.
		 * @param g link to CompileCode object.
		 */
		BlockInfo(final boolean jumps,
			final int continueAddr,
			final BlockInfo prevInfo,
			final CompileCode g) {
			_variables = g._localVariables;
			_variablesLastIndex = g._localVariablesLastIndex;
			g._localVariables =
				new LinkedHashMap<String, CompileVariable>(_variables);
			_jumps = jumps;
			if (_jumps) {
				_breakJumps = new ArrayList<CodeI1>();
				if (continueAddr == -1) {
					if (prevInfo != null) {
						_continueAddr = prevInfo._continueAddr;
					} else {
						_continueAddr = -1;
					}
				} else {
					_continueAddr = continueAddr;
				}
			} else {
				if (prevInfo != null) {
					_breakJumps = prevInfo._breakJumps;
					_continueAddr = prevInfo._continueAddr;
				} else {
					_breakJumps = null;
					_continueAddr = -1;
				}
			}
			_prevInfo = prevInfo;
		}

		/** Close BlockInfo object.
		 * @param g link to CompileCode object.
		 * @return parent BlockInfo object.
		 */
		final BlockInfo closeBlock(final CompileCode g) {
			// resolve break jumps
			if (_jumps) {
				for (int i = 0, j = _breakJumps.size(); i < j; i++) {
					_breakJumps.get(i).setParam(g._lastCodeIndex + 1);
				}
			}
			_breakJumps = null;
			g._localVariablesLastIndex = _variablesLastIndex;
			g._localVariables = _variables;
			_variables = null;
			BlockInfo result = _prevInfo;
			_prevInfo = null;
			return result;
		}
	}

	/** Description of list of "keyword" parameters (starting with "%"):
	 * to be compared with the parameters template. */
	private static final class ParamList {
		/** Total number of parameters */
		private int _numPars;
		/** Array with keyword parameters. */
		private KeyPar[] _keyParams = new KeyPar[0];

		/** Create instance of key parameters. */
		private ParamList() {}

		/** Get number of parameters.
		 * @return number of parameters.
		 */
		private int getNumpars() {return _numPars;}

		/** Set number of parameters.
		 * @param numPars number of parameters.
		 */
		private void setNumpars(final int numPars) {_numPars = numPars;}

		/** Add key parameter.
		 * @param name name of parameter.
		 * @param type type ID of parameter.
		 * @param value value of parameter.
		 * @return
		 */
		private boolean addKeyPar(final String name,
			final short type, final XDValue value) {
			int len = _keyParams.length;
			KeyPar keypar = new KeyPar(name, type, value);
			if (len == 0) {
				_keyParams = new KeyPar[]{keypar};
			}
			for (KeyPar k: _keyParams) {
				if (name.equals(k._name)) {
					return false; // not found
				}
			}
			KeyPar[] oldParams = _keyParams;
			_keyParams = new KeyPar[len + 1];
			System.arraycopy(oldParams, 0, _keyParams, 0, len);
			_keyParams[len] = keypar;
			return true;
		}

		/** Contains a keyword parameter. */
		private static final class KeyPar {
			private final String _name;
			private final short _type;
			private final XDValue _value;

			private KeyPar(final String name,
				final short type,
				final XDValue value) {
				_name = name;
				_type = type;
				_value = value;
			}
		}
	}
}