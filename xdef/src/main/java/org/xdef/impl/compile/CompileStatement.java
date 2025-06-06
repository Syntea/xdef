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
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.XDValueID.XD_BNFRULE;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTES;
import static org.xdef.XDValueID.XD_CHAR;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_EXCEPTION;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_OBJECT;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_UNDEF;
import static org.xdef.XDValueID.XD_VOID;
import static org.xdef.XDValueID.X_ATTR_REF;
import static org.xdef.XDValueID.X_NOTYPE_VALUE;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_KEY;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import static org.xdef.XDValueID.X_UNIQUESET_NAMED;
import org.xdef.impl.XConstants;
import org.xdef.impl.XDefinition;
import static org.xdef.impl.code.CodeTable.ADD_I;
import static org.xdef.impl.code.CodeTable.ADD_R;
import static org.xdef.impl.code.CodeTable.ADD_S;
import static org.xdef.impl.code.CodeTable.AND_B;
import static org.xdef.impl.code.CodeTable.BNFRULE_PARSE;
import static org.xdef.impl.code.CodeTable.CALL_OP;
import static org.xdef.impl.code.CodeTable.CMPEQ;
import static org.xdef.impl.code.CodeTable.DEC_I;
import static org.xdef.impl.code.CodeTable.DEC_R;
import static org.xdef.impl.code.CodeTable.DIV_I;
import static org.xdef.impl.code.CodeTable.DIV_R;
import static org.xdef.impl.code.CodeTable.EQ_NULL;
import static org.xdef.impl.code.CodeTable.INC_I;
import static org.xdef.impl.code.CodeTable.INC_R;
import static org.xdef.impl.code.CodeTable.INIT_NOPARAMS_OP;
import static org.xdef.impl.code.CodeTable.INIT_PARAMS_OP;
import static org.xdef.impl.code.CodeTable.JMPF_OP;
import static org.xdef.impl.code.CodeTable.JMPT_OP;
import static org.xdef.impl.code.CodeTable.JMP_OP;
import static org.xdef.impl.code.CodeTable.LD_CODE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.LD_GLOBAL;
import static org.xdef.impl.code.CodeTable.LD_LOCAL;
import static org.xdef.impl.code.CodeTable.LD_XMODEL;
import static org.xdef.impl.code.CodeTable.LSHIFT_I;
import static org.xdef.impl.code.CodeTable.MOD_I;
import static org.xdef.impl.code.CodeTable.MOD_R;
import static org.xdef.impl.code.CodeTable.MUL_I;
import static org.xdef.impl.code.CodeTable.MUL_R;
import static org.xdef.impl.code.CodeTable.NEG_BINARY;
import static org.xdef.impl.code.CodeTable.NEG_I;
import static org.xdef.impl.code.CodeTable.NEG_R;
import static org.xdef.impl.code.CodeTable.NEW_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.NE_NULL;
import static org.xdef.impl.code.CodeTable.NOT_B;
import static org.xdef.impl.code.CodeTable.OR_B;
import static org.xdef.impl.code.CodeTable.PARSERESULT_MATCH;
import static org.xdef.impl.code.CodeTable.PARSE_OP;
import static org.xdef.impl.code.CodeTable.RELEASE_CATCH_EXCEPTION;
import static org.xdef.impl.code.CodeTable.RETV_OP;
import static org.xdef.impl.code.CodeTable.RET_OP;
import static org.xdef.impl.code.CodeTable.RRSHIFT_I;
import static org.xdef.impl.code.CodeTable.RSHIFT_I;
import static org.xdef.impl.code.CodeTable.SET_CATCH_EXCEPTION;
import static org.xdef.impl.code.CodeTable.SET_PARSED_ERROR;
import static org.xdef.impl.code.CodeTable.STACK_DUP;
import static org.xdef.impl.code.CodeTable.STOP_OP;
import static org.xdef.impl.code.CodeTable.SUB_I;
import static org.xdef.impl.code.CodeTable.SUB_R;
import static org.xdef.impl.code.CodeTable.THROW_EXCEPTION;
import static org.xdef.impl.code.CodeTable.TO_DECIMAL_X;
import static org.xdef.impl.code.CodeTable.TO_FLOAT;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKIDS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_GETVALUEX;
import static org.xdef.impl.code.CodeTable.UNIQUESET_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREFS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_NEWKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SETKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_NEWINSTANCE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_SETVALUEX;
import static org.xdef.impl.code.CodeTable.XOR_B;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.ParseItem;
import static org.xdef.impl.compile.CompileBase.ANY_MODE;
import org.xdef.impl.compile.CompileBase.InternalMethod;
import static org.xdef.impl.compile.CompileBase.NO_MODE;
import static org.xdef.impl.compile.CompileBase.TEXT_MODE;
import static org.xdef.impl.compile.CompileBase.UNDEF_CODE;
import static org.xdef.impl.compile.CompileBase.getClassTypeID;
import static org.xdef.impl.compile.CompileBase.getTypeClass;
import static org.xdef.impl.compile.CompileBase.getTypeId;
import static org.xdef.impl.compile.CompileBase.getTypeMethod;
import static org.xdef.impl.compile.CompileBase.getTypeName;
import static org.xdef.impl.compile.XScriptParser.AAND_SYM;
import static org.xdef.impl.compile.XScriptParser.AND_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.AND_SYM;
import static org.xdef.impl.compile.XScriptParser.ASK_SYM;
import static org.xdef.impl.compile.XScriptParser.ASSGN_SYM;
import static org.xdef.impl.compile.XScriptParser.ATCHAR_SYM;
import static org.xdef.impl.compile.XScriptParser.BEG_SYM;
import static org.xdef.impl.compile.XScriptParser.BREAK_SYM;
import static org.xdef.impl.compile.XScriptParser.CASE_SYM;
import static org.xdef.impl.compile.XScriptParser.CATCH_SYM;
import static org.xdef.impl.compile.XScriptParser.CHECK_SYM;
import static org.xdef.impl.compile.XScriptParser.COLON_SYM;
import static org.xdef.impl.compile.XScriptParser.COMMA_SYM;
import static org.xdef.impl.compile.XScriptParser.CONSTANT_SYM;
import static org.xdef.impl.compile.XScriptParser.CONTINUE_SYM;
import static org.xdef.impl.compile.XScriptParser.DEC_SYM;
import static org.xdef.impl.compile.XScriptParser.DEFAULT_SYM;
import static org.xdef.impl.compile.XScriptParser.DIV_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.DIV_SYM;
import static org.xdef.impl.compile.XScriptParser.DOT_SYM;
import static org.xdef.impl.compile.XScriptParser.DO_SYM;
import static org.xdef.impl.compile.XScriptParser.ELSE_SYM;
import static org.xdef.impl.compile.XScriptParser.END_SYM;
import static org.xdef.impl.compile.XScriptParser.EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.EXTERNAL_SYM;
import static org.xdef.impl.compile.XScriptParser.FINAL_SYM;
import static org.xdef.impl.compile.XScriptParser.FOR_SYM;
import static org.xdef.impl.compile.XScriptParser.GE_SYM;
import static org.xdef.impl.compile.XScriptParser.GT_SYM;
import static org.xdef.impl.compile.XScriptParser.IDENTIFIER_SYM;
import static org.xdef.impl.compile.XScriptParser.IF_SYM;
import static org.xdef.impl.compile.XScriptParser.INC_SYM;
import static org.xdef.impl.compile.XScriptParser.LE_SYM;
import static org.xdef.impl.compile.XScriptParser.LPAR_SYM;
import static org.xdef.impl.compile.XScriptParser.LSH_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.LSH_SYM;
import static org.xdef.impl.compile.XScriptParser.LSQ_SYM;
import static org.xdef.impl.compile.XScriptParser.LT_SYM;
import static org.xdef.impl.compile.XScriptParser.MINUS_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.MINUS_SYM;
import static org.xdef.impl.compile.XScriptParser.MOD_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.MOD_SYM;
import static org.xdef.impl.compile.XScriptParser.MUL_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.MUL_SYM;
import static org.xdef.impl.compile.XScriptParser.NEG_SYM;
import static org.xdef.impl.compile.XScriptParser.NEW_SYM;
import static org.xdef.impl.compile.XScriptParser.NE_SYM;
import static org.xdef.impl.compile.XScriptParser.NOT_SYM;
import static org.xdef.impl.compile.XScriptParser.NULL_SYM;
import static org.xdef.impl.compile.XScriptParser.OOR_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTIONAL_SYM;
import static org.xdef.impl.compile.XScriptParser.OR_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.OR_SYM;
import static org.xdef.impl.compile.XScriptParser.PLUS_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.PLUS_SYM;
import static org.xdef.impl.compile.XScriptParser.RETURN_SYM;
import static org.xdef.impl.compile.XScriptParser.RPAR_SYM;
import static org.xdef.impl.compile.XScriptParser.RRSH_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.RRSH_SYM;
import static org.xdef.impl.compile.XScriptParser.RSH_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.RSH_SYM;
import static org.xdef.impl.compile.XScriptParser.RSQ_SYM;
import static org.xdef.impl.compile.XScriptParser.SEMICOLON_SYM;
import static org.xdef.impl.compile.XScriptParser.SWITCH_SYM;
import static org.xdef.impl.compile.XScriptParser.THROW_SYM;
import static org.xdef.impl.compile.XScriptParser.TRY_SYM;
import static org.xdef.impl.compile.XScriptParser.TYPE_SYM;
import static org.xdef.impl.compile.XScriptParser.UNIQUE_SET_SYM;
import static org.xdef.impl.compile.XScriptParser.VAR_SYM;
import static org.xdef.impl.compile.XScriptParser.WHILE_SYM;
import static org.xdef.impl.compile.XScriptParser.XOR_EQ_SYM;
import static org.xdef.impl.compile.XScriptParser.XOR_SYM;
import static org.xdef.impl.compile.XScriptParser.symToName;
import static org.xdef.sys.SParser.NOCHAR;
import static org.xdef.sys.StringParser.chkJavaName;

/** Compiler of statements in script.
 * @author Vaclav Trojan
 */
class CompileStatement extends XScriptParser implements CodeTable {
	/** Operators level 1. */
	private static final String OP_L1 = new String(new char[]{MUL_SYM, DIV_SYM, MOD_SYM});
	/** Operators level 2. */
	private static final String OP_L2 = new String(new char[]{PLUS_SYM, MINUS_SYM});
	/** Operators level 3. */
	private static final String OP_L3 = new String(new char[]{EQ_SYM, NE_SYM, LE_SYM, GE_SYM,
		 LT_SYM, GT_SYM, LSH_SYM, RSH_SYM, RRSH_SYM, CHECK_SYM});
	/** Operators level 4. */
	private static final String OP_L4 = new String(new char[]{AND_SYM, AAND_SYM});
	/** Operators level 5. */
	private static final String OP_L5 = new String(new char[]{OR_SYM, XOR_SYM, OOR_SYM});
	/** Unary operators. */
	private static final String OP_UNARY = new String(new char[]{NEG_SYM, NOT_SYM, PLUS_SYM, MINUS_SYM});
	/** Assignment operators */
	private static final String OP_ASSGN = new String(new char[] {ASSGN_SYM, LSH_EQ_SYM, RSH_EQ_SYM,
		RRSH_EQ_SYM, MUL_EQ_SYM, DIV_EQ_SYM, MOD_EQ_SYM, AND_EQ_SYM, PLUS_EQ_SYM, MINUS_EQ_SYM, OR_EQ_SYM,
		XOR_EQ_SYM});

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
	/** number of parameters to be popped from stack if statements are part of method.
	 * -1 if statements are part of "direct" code of the action. */
	private int _popNumParams;
	/** Actual catch address */
	private CodeI1 _catchItem;
	/** The ClassLoader to load Java classes. */
	private ClassLoader _classLoader;
	/** List of implements and uses requests */
	final List<CompileReference> _implList = new ArrayList<>();

	/** Creates a new instance of CommandCompiler
	 * @param g The code generator.
	 * @param xmlVersion 10 -> ""1.0, 11 -> "1.1".
	 * @param mode The compilation mode.
	 * @param nsPrefixes array with namespace prefixes.
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
		if (varTable && _g._localVariables != null && !_g._localVariables.isEmpty()) {
			vtab = new XVariable[_g._localVariables.size()];
			_g._localVariables.values().toArray(vtab);
		}
		return _g._debugInfo.addInfo(getLastPosition(), _actDefName, _g._lastCodeIndex + 1, vtab);
	}

	/** Set source buffer with code to be compiled.
	 * @param source buffer wit source code.
	 * @param actDefName name of actually processed X-definition.
	 * @param xdVersion version of X-definition.
	 * @param nsPrefixes table with prefixes and namespaces.
	 */
	final void setSource(final SBuffer source,
		final String actDefName,
		final XDefinition xdef,
		final byte xdVersion,
		final Map<String, Integer> nsPrefixes,
		final String xpath) {
		String[] importLocal = xdef != null ? xdef._importLocal : null;
		setSource(source, actDefName, importLocal, xdVersion, xpath);
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
				new String(new char[] {RPAR_SYM, SEMICOLON_SYM, BEG_SYM, END_SYM, COMMA_SYM, ASSGN_SYM}));
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
			XDValue xv =_g.removeLastCodeItem();
			if (plist != null) {
				if (!plist.addKeyPar(name, xv.getItemId(), xv)) {
					error(XDEF.XDEF420, name); //Parameter redefinition of &{0}
				}
			}
			_g.replaceLastCodeItem(new DefNamedValue(name, xv));
			_g._tstack[--_g._sp] = XD_NAMEDVALUE;
			return true;
		} else {
			if (sp + 1 == _g._sp) {
				if (plist != null) {
					if (!plist.addKeyPar(name, _g._tstack[_g._sp], null)) {
						error(XDEF.XDEF420, name);//Parameter redefinition &{0}
					}
				}
				_g.addCode(new CodeOp(XD_NAMEDVALUE, NEW_NAMEDVALUE), -1);
			}
			return false;
		}
	}

	/** Read value of method parameter and return true if it was a constant.
	 * @return true if and only if the parameter is a constant.
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
	 * KeyValue ::= "(" S? Expression ( S? "," S? Expression )* S? ")" | Expression
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
				InternalMethod m;
				String[] sqnames;
				if (_sym == MUL_SYM) {
					if (_g._sp - sp < 2 && (m = CompileCode.getTypeMethod(X_NOTYPE_VALUE,name)) != null
						&& m.getResultType() == XD_PARSER && (sqnames = m.getSqParamNames()) != null
						&& sqnames.length == 2 && (sqnames[0].startsWith("minLength")
						&& sqnames[1].startsWith("maxLength")
						|| sqnames[0].startsWith("minInclusive") && sqnames[1].startsWith("maxInclusive"))) {
						_g.addCode(DefNull.NULL_VALUE); // asterisk can be as minxxx or maxxxx
						_g._tstack[++_g._sp] = XD_ANY;
						_g._cstack[_g._sp] = _g._lastCodeIndex;
					} else {
						error(XDEF.XDEF216, "*"); //Unexpected character&{0}{: '}{'}
					}
					nextSymbol();
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
		plist._numPars = _g._sp - sp; //number of parameters
		return plist;
	}

	/** Parse and compile method reference.
	 * methodName ::= scriptIdentifier
	 * method ::= methodName (s? parameterList)?
	 * parameterList ::= "(" s? ( expression ( s? "," s? expression )* )? s? ")"
	 * @param name Name of method
	 * @return true if method was parsed.
	 */
	private boolean method(final String name) {
		if (_sym != LPAR_SYM) {
			return false;
		}
		if (!_g._debugMode && ("trace".equals(name) || "pause".equals(name))) {
			int lastCodeIndex = _g._lastCodeIndex;
			int sp = _g._sp;
			int spMax = _g._spMax;
			paramList(name);
			_g.removeCodeFromIndexAndClearStack(lastCodeIndex, sp, spMax);
			return true;
		} else {
			int numPar = paramList(name)._numPars;
			CompileVariable var;
			int addr;
			if ((var =_g.getVariable(name)) != null && numPar == 0 && var.getType() == XD_PARSER) {
				_g.genLD(name);
				if (_sym != DOT_SYM) {
					_g.addCode(new CodeI1(XD_PARSERESULT, PARSE_OP, 1), 0);
				}
			} else if (numPar == 0 && var != null && var.getType() == X_PARSEITEM
				&& (addr = var.getParseMethodAddr()) >= 0 && _sym == DOT_SYM
				&& _g._code.get(addr).getItemId()== XD_PARSER) {
				 // it is  parser and follows a class method so we load just the parser
				short code;
				if (var.getKind() == 'G'
					&& _g._code.get(addr).getCode() == LD_CONST) {
					code = LD_CODE;
					_g._cstack[++_g._sp] = addr;
					_g.addCode(new CodeS1(XD_PARSER, code, addr, name));
					_g._tstack[_g._sp] = XD_PARSER;
				} else {
					_g.genLD(name);
				}
			} else {
				String err = _g.genMethod(name, numPar);
				if (err != null) {
					error(XDEF.XDEF443, err); //Unknown method: '&{0}'
				}
			}
			if ("ListOf".equals(name)) {
				_g.reportDeprecated(name,"list(type)");
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
				while(nextSymbol()==IDENTIFIER_SYM && nextSymbol()==DOT_SYM) {}
				return;
			}
			short xTyp;
			if ((xTyp =_g._tstack[_g._sp]) == XD_LONG || xTyp == XD_DOUBLE) {
				error(XDEF.XDEF216);//Unexpected character&{0}{: '}{'}
				nextSymbol();
				return;
			}
			if (nextSymbol() != IDENTIFIER_SYM) {
				error(XDEF.XDEF417); //Method name expected
			}
			String methodName = _idName;
			nextSymbol();
			if (_sym == LPAR_SYM) { // '(' => method
				if (!_g.genClassMethod(methodName, paramList(methodName)._numPars)) {
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
			if ((itemType = var.getType()) == XD_LONG) {
				_g.addCode(new CodeOp(XD_LONG, op==INC_SYM ? INC_I : DEC_I), 0);
			} else if (itemType == XD_DOUBLE) {
				_g.addCode(new CodeOp(XD_DOUBLE, op==INC_SYM ? INC_R : DEC_R), 0);
			} else {
				if (itemType != XD_UNDEF) {
					error(XDEF.XDEF435); //Variable of type 'int' or 'float' expected
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
		if (vLoaded && _g._tstack[_g._sp] == X_PARSEITEM) {
			_g._tstack[_g._sp] = XD_PARSER;
		}
		int numPar = _sym == LPAR_SYM ? paramList(methodName)._numPars : 0;
		short xTyp, code;
		CompileVariable var;
		boolean loaded = vLoaded || _g.genLD(cName);
		if (loaded) {
			xTyp = _g._tstack[_g._sp - numPar];
			short yTyp = -1;
			if (numPar==1) {
				if ((xTyp!=X_UNIQUESET	&& xTyp!=X_UNIQUESET_M) || ((yTyp=_g._tstack[_g._sp]) != X_PARSEITEM
					&& yTyp!=XD_PARSER && yTyp!=XD_PARSERESULT)) {
					yTyp = -1;
				}
			}
			switch(methodName) {
				case "ID": code = UNIQUESET_ID; break;
				case "IDREF": code = UNIQUESET_IDREF; break;
				case "CHKID": code = UNIQUESET_CHKID; break;
				case "IDREFS": code = UNIQUESET_IDREFS; break;
				case "CHKIDS": code = UNIQUESET_CHKIDS; break;
				case "SET": code = UNIQUESET_SET; break;
				default: code = -1;
			}
			if (numPar<=1 && X_UNIQUESET==xTyp && code >=0) {
				if (yTyp != -1) {
					_g.genPop();
				}
				_g.addCode(new CodeI2(XD_PARSERESULT, code,0,getVariableAndErr(cName).getParseMethodAddr()));
				_g._tstack[_g._sp] = XD_PARSERESULT;
				return true;
			} else if (numPar<=1 && X_UNIQUESET_M==xTyp && code < 0
				&& (var = getVariable(cName + "." + methodName)) != null && var.getType() == X_UNIQUESET_KEY){
				if (yTyp != -1) {
					_g.genPop();
				}
				_g.addCode(new CodeI2(XD_VOID, UNIQUESET_KEY_SETKEY,
					var.getValue().intValue(), var.getParseMethodAddr()));
				_g._tstack[_g._sp] = XD_PARSERESULT;
				return true;
			} else if (_g.genClassMethod(methodName, numPar)) {
				return true;
			}
			error(spos, XDEF.XDEF443, methodName); //Unknown method: '&{0}'
			return false;
		}
		try {
			Class<?> clazz = Class.forName(name, false, _classLoader);
			CodeExtMethod method = _g.findExternalMethod(methodName, numPar, clazz, null);
			if (method._resultType != XD_VOID) {
				numPar--;
			}
			_g.addCode(method,	-numPar);
			return true;
		} catch (ClassNotFoundException ex) {
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
				&& ((xTyp = var.getType()) == X_UNIQUESET || xTyp == X_UNIQUESET_M
					&& (var = getVariable(cName)) != null && var.getType() == X_UNIQUESET_KEY)) {
				if (numPar==1) {
					short yTyp=_g._tstack[_g._sp];
					if (yTyp == XD_PARSER || yTyp == X_PARSEITEM
						|| yTyp == XD_PARSERESULT || yTyp == XD_BOOLEAN || yTyp == XD_NULL) {
						_g.genPop();
					} else {
						error(spos, XDEF.XDEF467); //Incorrect parameter type
						return false;
					}
				}
				_g.genLD(cName.substring(0, ndx));
				_g.addCode(new CodeI2(XD_VOID,
					code, var.getValue().intValue(), var.getParseMethodAddr()));
				if (code == UNIQUESET_KEY_NEWKEY) {
					_g._tstack[_g._sp--] = -1; //KEY_NEWKEY is void
				} else {
					_g._tstack[_g._sp] = XD_PARSERESULT;
				}
				return true;
			}
			if (_g._ignoreUnresolvedExternals) {
				_g.setUnDefItem();
				return true;
			}
			error(spos, XDEF.XDEF443, cName+'.'+methodName); //Unknown method: '&{0}'
			return false;
		}
	}

	/** Parse and compile factor.
	 * Factor ::= ( Cast )? ( UnaryOperator )? ( "(" Expression ")" | Value )
	 * Cast ::= "(" TypeName ")"
	 * UnaryOperator ::= ( "+" | "-" | "!" | "~" )
	 * Value ::= Constant | Method | Constructor | AttrReference | Identifier ( "++" | "--" )?
	 * Constructor :: = "new" TypeName "(" ParameterList ")" | "[" ContextValues? "]" | NamedValue
	 * ContextValues ::= Value ("," Value)*
	 * NamedValue ::= "%" QName "=" Value
	 * Method ::= MethodName "(" ParameterList ")"
	 * AttrReference ::= "@" QName
	 * ParameterList ::= ( "$ChkElement" | Expression ) ( "," Expression )*
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
				if (_sym == IDENTIFIER_SYM && (castRequest = getTypeId(_idName)) >= 0) {
					//type casting
					checkNextSymbol(RPAR_SYM);
					if (factor()) {
						_g.convertTopToType(castRequest);
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
					short type = getTypeId(name = _idName);
					if (type <= 0) {
						error(XDEF.XDEF480, name);//Unknown constructor: '&{0}'
						type = XD_UNDEF;
					}
					if (nextSymbol() == LPAR_SYM) { // '('
						if (!_g.genConstructor(type, paramList(name)._numPars)) {
							error(XDEF.XDEF480, name); //Unknown constructor: '&{0}'
						}
					} else {
						error(XDEF.XDEF410, "("); //'&{0}' expected
					}
				}
				break;
			}
			case LSQ_SYM: {// '[' => Container constructor
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
				separateMethodNameFromIdentifier();
				String name = _idName;
				if (LPAR_SYM == nextSymbol()) { // '(' => method
					method(name);
				} else if (_sym == INC_SYM || _sym == DEC_SYM) { // "++" | "--"
					genInc(_sym, name, 2);
					nextSymbol();
				} else if (!assignment(name, true)) {
					CompileVariable var = _g.getVariable(name);
					XDValue xv;
					if (var != null) {
						if (var.getType() == X_UNIQUESET_NAMED) {
							// this is very nasty code. If the variable refers to a declared type and
							// the parser is constant we use the code with the parser as a value (and it
							// is a constant). TODO if it is not a constant.
							_g.genLD(var);
							_g.addCode(new CodeS1(var.getParseResultType(), UNIQUESET_GETVALUEX,
								var.getValue().toString()), 0);
							break;
						} else if (var.getParseMethodAddr()>=0 && var.getParseMethodAddr()>_g._lastCodeIndex){
							break; // ??? error, probably unknown type or method
						} else if (var.getType() == X_PARSEITEM
							&& var.getParseMethodAddr() >= 0 //parse method exist
							&& (xv = _g._code.get(var.getParseMethodAddr())).getCode() == LD_CONST // constant
							&& xv.getItemId() == XD_PARSER
							&& _g._code.get(var.getParseMethodAddr() + 1).getCode() == PARSE_OP
							&& _g._code.get(var.getParseMethodAddr() + 2).getCode() == STOP_OP) {
							_g.addCode(xv, 1);// declared type
							_g._tstack[_g._sp] = XD_PARSER; //it is parser!
							_g._cstack[_g._sp] = var.getParseMethodAddr();
							break;
						}
					}
					if (var == null || !_g.genLD(var)) {
						int xsp = _g._sp;
						if (_g.scriptMethod(name, 0) || _g.internalMethod(name, 0)
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
						error(XDEF.XDEF257, s); //Namespace for prefix '&{0}' is undefined
						s = _idName;
					}
				} else {
					s = _idName;
				}
				_g.genLDAttr(s);
				nextSymbol();
				break;
			}
			default: // not value
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
		short xTyp = _g._tstack[_g._sp];
		int xVal = _g._cstack[_g._sp];
		switch (unaryoperator) {
			case PLUS_SYM:
			case MINUS_SYM: {
				if (xTyp != XD_LONG && xTyp != XD_DOUBLE && xTyp != XD_DECIMAL && xTyp != XD_BIGINTEGER) {
					if (xTyp != XD_UNDEF) { // don't report twice
						error(XDEF.XDEF439); //Value of type int or float expected
					}
					return true;
				}
				if (unaryoperator == MINUS_SYM) { //unary minus, plus is ignored!
					switch (xTyp) {
						case XD_LONG:
							if (xVal >= 0) { // constant
								long i = _g.getCodeItem(xVal).longValue();
								_g._code.set(xVal, new DefLong(- i));
							} else { // not constant
								_g.addCode(new CodeOp(XD_LONG, NEG_I), 0);
							}
							break;
						case XD_DECIMAL:
							if (xVal >= 0) { // constant
								_g.setCodeItem(xVal, new DefDecimal(_g.getCodeItem(xVal).decimalValue()));
							} else {
								error(XDEF.XDEF439); //Value of type int or float expected
							}
							break;
						case XD_BIGINTEGER:
							if (xVal >= 0) { // constant
								_g.setCodeItem(xVal, new DefBigInteger(_g.getCodeItem(xVal).integerValue()));
							} else {
								error(XDEF.XDEF439); //Value of type int or float expected
							}
							break;
						default:
							if (xVal >= 0) { // constant
								double f = _g.getCodeItem(xVal).doubleValue();
								_g._code.set(xVal, new DefDouble(- f));
							} else { // not constant
								_g.addCode(new CodeOp(XD_DOUBLE, NEG_R), 0);
							}
					}
				}
				return true;
			}
			case NEG_SYM: {
				if (xTyp == XD_LONG) {
					if (xVal >= 0) { // constant
						long i = _g.getCodeItem(xVal).longValue();
						_g._code.set(xVal, new DefLong(~ i));
					} else { // not constant
						_g.addCode(new CodeOp(XD_BOOLEAN, NEG_BINARY), 0);
					}
				} else if (xTyp != XD_UNDEF) {
					error(XDEF.XDEF423, "int"); //Value of type '&{0}' expected
				}
				return true;
			}
			case NOT_SYM: {
				if (xTyp  == X_ATTR_REF) {
					_g.topToBool();
					xVal = _g._cstack[_g._sp];
				} else if (xTyp  == XD_PARSERESULT || xTyp  == XD_PARSER) {
					_g.topToBool();
					xVal = _g._cstack[_g._sp];
				} else if (xTyp != XD_BOOLEAN) {
					if (xTyp != XD_UNDEF) {
						error(XDEF.XDEF423, "boolean"); //Value of type &{0} expected
						xVal = _g._cstack[_g._sp] = -1;
					}
				}
				if (xVal >= 0) {
					boolean b = _g.getCodeItem(xVal).booleanValue();
					_g._code.set(xVal, new DefBoolean(!b));
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
			short xTyp = _g._tstack[sp];
			int xVal = _g._cstack[sp];
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
			short yTyp = _g._tstack[_g._sp];
			int yVal = _g._cstack[_g._sp];
			if ((operator == MUL_SYM) || (operator == DIV_SYM) || (operator == MOD_SYM)) {//mul | div | modulo
				if (xTyp == XD_LONG && yTyp == XD_LONG) {
					if (xVal >= 0 && yVal >= 0) { // both constants
						long x = _g.getCodeItem(xVal).longValue();
						long y = _g.getCodeItem(yVal).longValue();
						_g.replaceTwo(new DefLong(operator==MUL_SYM ? x*y : operator==DIV_SYM ? x/y : x%y));
					} else { // not both constants
						_g.addCode(new CodeOp(XD_LONG,
							operator==MUL_SYM ? MUL_I : operator==DIV_SYM ? DIV_I : MOD_I), -1);
					}
				} else { // not both integer => float
					_g.operandsToFloat();
					yVal = _g._cstack[_g._sp];
					if (xVal >= 0 && yVal >= 0) { // both constants
						double x = _g.getCodeItem(xVal).doubleValue();
						double y = _g.getCodeItem(yVal).doubleValue();
						_g.replaceTwo(new DefDouble(operator==MUL_SYM ? x*y : operator==DIV_SYM ? x/y : x%y));
					} else { // not both constants
						_g.addCode(new CodeOp(XD_DOUBLE,
							operator==MUL_SYM ? MUL_R : operator==DIV_SYM ? DIV_R : MOD_R), -1);
					}
				}
			}
		}
		return true;
	}

	/** Parse and compile simple expression (add or subtract).
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
			short xTyp = _g._tstack[sp];
			int xVal = _g._cstack[sp];
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
			short yTyp = _g._tstack[_g._sp];
			int yVal = _g._cstack[_g._sp];
			if (operator == PLUS_SYM && (xTyp == XD_STRING || yTyp == XD_STRING)) {
				_g.operandsToString(); // "+" => string concatenation
				xVal = _g._cstack[_g._sp - 1];
				yVal = _g._cstack[_g._sp];
				if (xVal >= 0 && yVal >= 0)	{ // constants
					_g.replaceTwo(
						new DefString(_g.getCodeItem(xVal).toString() + _g.getCodeItem(yVal).toString()));
				} else {
					_g.addCode(new CodeOp(XD_STRING, ADD_S), -1);
				}
			} else {// add or subtract numbers
				if (xTyp == XD_LONG && yTyp == XD_CHAR) {
					_g.topXToInt(0);
					yTyp = XD_LONG;
				} else if (xTyp == XD_CHAR && yTyp == XD_LONG) {
					_g.topXToInt(1);
					xTyp = XD_LONG;
				}
				if (xTyp == XD_LONG && yTyp == XD_LONG) {
					if (xVal >= 0 && yVal >= 0) {
						long x = _g.getCodeItem(xVal).longValue();
						long y = _g.getCodeItem(yVal).longValue();
						_g.replaceTwo(new DefLong(operator==PLUS_SYM ? x + y : x - y));
					} else {
						_g.addCode(new CodeOp(XD_LONG, operator==PLUS_SYM ? ADD_I : SUB_I), -1);
					}
				} else if (xTyp == XD_DOUBLE && yTyp == XD_DOUBLE || xTyp == XD_DOUBLE && yTyp == XD_LONG
					|| xTyp == XD_LONG && yTyp == XD_DOUBLE) {
					_g.operandsToFloat();
					yVal = _g._cstack[_g._sp];
					if (xVal >= 0 && yVal >= 0) {
						double x = _g.getCodeItem(xVal).doubleValue();
						double y = _g.getCodeItem(xVal).doubleValue();
						_g.replaceTwo(new DefDouble(operator==PLUS_SYM ? x + y : x - y));
					} else {
						_g.addCode(new CodeOp(XD_DOUBLE, operator==PLUS_SYM ? ADD_R : SUB_R), -1);
					}
				} else {
					if (!_g._ignoreUnresolvedExternals || (xTyp != XD_UNDEF && yTyp != XD_UNDEF)) {
						error(XDEF.XDEF423, "number"); //Value of type '&{0}' expected
					}
					_g.addCode(new CodeOp(XD_DOUBLE, ADD_R), -1);
				}
			}
		}
		return true;
	}

	/** Report if values are incomparable. */
	private void incomparable(final short yTyp) {
		if (yTyp != XD_UNDEF) {
			error(XDEF.XDEF444); //Incomparable types of values
		}
	}

	/** Parse and compile relation part of expression.
	 * operatorLevel3 ::= "<" | ">" | "<=" | ">=" | "==" | "!=" | "<<" | ">>" | ">>>" | "CHECK"
	 * expression ::= simpleExpression ( operatorLevel3 simpleExpression )?
	 * @return true if simple expression was parsed.
	 */
	private boolean relExpression() {
		int sp = _g._sp;
		if (!simpleExpression()) {
			return false;
		}
		boolean firstError = _g._sp <= sp;
		// relational and shift operators:
		while (OP_L3.indexOf(_sym) >= 0) { //<,>,<=,>=,==,!=,<<,>>,>>>,CHECK
			char operator = _sym;
			if (firstError) {
				_g.setUnDefItem();
				firstError = false;
				error(XDEF.XDEF438); //Value expected
			}
			sp = _g._sp;
			short xTyp = _g._tstack[sp];
			if (xTyp == X_ATTR_REF) {
				_g.topToString();
				xTyp = _g._tstack[sp];
			}
			int xVal = _g._cstack[sp];
			nextSymbol();
			if (operator == CHECK_SYM) {
				if (xTyp == XD_PARSER) {
					_g.addCode(new CodeI1(XD_PARSERESULT,PARSE_OP, 1), 0);
				} else if (xTyp != XD_PARSERESULT) {
					error(XDEF.XDEF423, "ParseResult");//Value of type '&{0}' expected
					incomparable(xTyp);
					continue;
				}
				_g.addCode(new CodeI1(XD_PARSERESULT,STACK_DUP, 1), 1);
				_g.addCode(new CodeI1(XD_BOOLEAN, PARSERESULT_MATCH, -1), 0);
				CodeI1 jmpf = new CodeI1(XD_PARSERESULT, JMPF_OP, -1);
				_g.addCode(jmpf, 0); // skip CHECK code if an error occurs.
				if (!simpleExpression()) { // compile the second argument
					error(XDEF.XDEF437); //Error in expression
					break;
				}
				if (_g._sp < 0 || sp < 0 || sp == _g._sp) {
					if (sp == _g._sp) {
						error(XDEF.XDEF438); //Value expected
					}
					continue;
				}
				short yTyp = _g._tstack[_g._sp];
				if (yTyp != XD_BOOLEAN) {
					_g.topToBool(); // result must be boolean
					yTyp = _g._tstack[_g._sp];
					if (yTyp != XD_BOOLEAN) {
						incomparable(yTyp);
					}
				}
				CodeI1 jmpt = new CodeI1(XD_PARSERESULT, JMPT_OP, -1);
				_g.addCode(jmpt, -1); //skip if CHECK expression result is true
				_g.addCode(new DefString("XDEF822"), 1); // set error
				_g.addCode(new DefString(""), 1);
				_g.addCode(new CodeI1(XD_PARSERESULT, SET_PARSED_ERROR, 3), -2);
				jmpt.setParam(_g._lastCodeIndex + 1);// jump true addr is here
				jmpf.setParam(_g._lastCodeIndex + 1);// jump false addr is here
				continue;
			}
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
			short yTyp = _g._tstack[_g._sp];
			if (yTyp  == X_ATTR_REF) {
				_g.topToString();
				yTyp = _g._tstack[_g._sp];
			}
			int yVal = _g._cstack[_g._sp];
			if (xVal >= 0 && yVal >= 0) { // both constants, resolve
				int result = 0;
				long i,j;
				double u,v;
				boolean a,b;
				String s,t;
				if (xTyp == XD_NULL || yTyp == XD_NULL) {
					if (xTyp == yTyp) {
						result = 0;
					} else {
						// TODO
						incomparable(yTyp);
					}
					break;
				}
				switch (xTyp) {
					case XD_LONG: {
						i = _g.getCodeItem(xVal).longValue();
						switch (yTyp) {
							case XD_LONG:
								j = _g.getCodeItem(yVal).longValue();
								switch (operator) { // continue while sym is rel. operator
									case LSH_SYM: _g.replaceTwo(new DefLong(i << j)); continue;
									case RSH_SYM: _g.replaceTwo(new DefLong(i >> j)); continue;
									case RRSH_SYM: _g.replaceTwo(new DefLong(i >>> j)); continue;
								}
								result = i == j ? 0 : i < j ? - 1 : 1;
								break;
							case XD_DOUBLE:
								v = _g.getCodeItem(yVal).doubleValue();
								result = i == v ? 0 : i < v ? - 1 : 1;
								break;
							case XD_UNDEF: break;
							case XD_ANY: //TODO
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_DOUBLE: {
						u = _g.getCodeItem(xVal).doubleValue();
						switch (yTyp) {
							case XD_LONG:
								j = _g.getCodeItem(yVal).longValue();
								result = u == j ? 0 : u < j ? - 1 : 1;
								break;
							case XD_DOUBLE:
								v = _g.getCodeItem(yVal).doubleValue();
								result = u == v ? 0 : u < v ? - 1 : 1;
								break;
							case XD_UNDEF: break;
							case XD_ANY: //TODO
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_BOOLEAN: {
						a = _g.getCodeItem(xVal).booleanValue();
						if (yTyp == XD_BOOLEAN) {
							b = _g.getCodeItem(yVal).booleanValue();
							result = a == b ? 0 : 1;
							if (operator !=  EQ_SYM && operator !=  NE_SYM) {
								//Values of the type 'boolean' can be compared only for equality
								error(XDEF.XDEF445);
							}
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_STRING: {
						s = _g.getCodeItem(xVal).toString();
						if (yTyp == XD_STRING) {
							t = _g.getCodeItem(yVal).toString();
							result = s.compareTo(t);
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_DATETIME: {
						switch (yTyp) {
							case XD_DATETIME:
								try {
									result = _g.getCodeItem(xVal).compareTo(_g.getCodeItem(yVal));
								} catch (IllegalArgumentException ex) {
									incomparable(yTyp);
								}
								break;

							case XD_LONG: break;
							case XD_UNDEF: break;
							case XD_ANY: //TODO
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_DURATION: {
						if (yTyp != XD_DURATION) {
							incomparable(yTyp);
						} else {
							try {
								result = _g.getCodeItem(xVal).compareTo(_g.getCodeItem(yVal));
							} catch (IllegalArgumentException ex) {
								incomparable(yTyp);
							}
						}
						break;
					}
					case XD_UNDEF: break;
					case XD_ANY: //TODO
					default: incomparable(yTyp);
				}
				boolean bval = false;
				switch (operator) {
					case EQ_SYM: bval = result == 0; break;// '=='
					case NE_SYM: bval = result != 0; break;// '!=''
					case LE_SYM: bval = result <= 0; break;// '<=''
					case GE_SYM: bval = result >= 0; break;// '>='
					case LT_SYM: bval = result < 0; break;// '<''
					case GT_SYM: bval = result > 0; break;// '>'
				}
				_g.replaceTwo(new DefBoolean(bval));
			} else if (xTyp == XD_NULL || yTyp == XD_NULL) {
				// not both constants, first null
				short op;
				switch (operator) {
					case EQ_SYM: op = EQ_NULL; break;
					case NE_SYM: op = NE_NULL; break;
					default: op = UNDEF_CODE; incomparable(yTyp);
				}
				_g.addCode(new CodeI1(XD_BOOLEAN, op), -1);
			} else {// not both constants, none null
				short op = UNDEF_CODE;
				switch (xTyp) {
					case XD_LONG: {
						switch (yTyp) {
							case XD_LONG:
								switch (operator) {
									case LSH_SYM: _g.addCode(new CodeI1(XD_LONG, LSHIFT_I),-1); continue;
									case RSH_SYM:
										_g.addCode(new CodeI1(XD_LONG, RSHIFT_I), -1); continue;
									case RRSH_SYM: _g.addCode(new CodeI1(XD_LONG, RRSHIFT_I),-1); continue;
								}
								op = CMPEQ;
								break;
							case XD_DOUBLE: _g.topXToFloat(); op = CMPEQ; break;
							case XD_DECIMAL:
								_g.addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case XD_UNDEF: break;
							case XD_ANY: //TODO
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_DOUBLE: {
						switch (yTyp) {
							case XD_LONG:
								_g.addCode(new CodeOp(XD_DOUBLE,TO_FLOAT),0);
								op = CMPEQ;
								break;
							case XD_DOUBLE: op = CMPEQ; break;
							case XD_DECIMAL:
								_g.addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case XD_ANY: //TODO
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_BOOLEAN: {
						if (yTyp != XD_BOOLEAN) {
							_g.topToBool();
							yTyp = _g._tstack[_g._sp];
						}
						if (yTyp == XD_BOOLEAN) {
							if (operator !=  EQ_SYM && operator !=  NE_SYM) {
								//Values of the type 'boolean' can be compared only for equality
								error(XDEF.XDEF445);
							} else {
								op = CMPEQ;
							}
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_STRING: {
						if (yTyp == XD_STRING) {
							op = CMPEQ;
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_DATETIME: {
						if (yTyp == XD_DATETIME) {
							op = CMPEQ;
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_DURATION: {
						if (yTyp == XD_DURATION) {
							op = CMPEQ;
						} else {
							incomparable(yTyp);
						}
						break;
					}
					case XD_DECIMAL: {
						switch (yTyp) {
							case XD_LONG:
							case XD_DOUBLE:
								_g.addCode(new CodeI1(XD_DECIMAL, TO_DECIMAL_X), 0);
								op = CMPEQ;
								break;
							case XD_DECIMAL: op = CMPEQ; break;
							default: incomparable(yTyp);
						}
						break;
					}
					case XD_UNDEF: break;
					case XD_ANY: //TODO
					default: incomparable(yTyp);
				}
				switch (operator) {
					case EQ_SYM: break;// ==
					case NE_SYM: op++; break;// !=
					case LE_SYM: op += 2; break;// <=
					case GE_SYM: op += 3; break;// >=
					case LT_SYM: op += 4; break;// <
					case GT_SYM: op += 5; break;// >
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
			int xVal = _g._cstack[_g._sp];
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
				_g.topToBool(); // resolve X_ATTR_REF etc
				continue;
			}
			if (operator == AND_SYM) {// AND_SYM logical and
				_g.topToBool();
				int yVal = _g._cstack[_g._sp];
				if (xVal >= 0 && yVal >= 0) { // both constants
					boolean a = _g.getCodeItem(xVal).booleanValue();
					boolean b = _g.getCodeItem(yVal).booleanValue();
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
	 * expression ::=  andExpression (operatorLevel5  andExpression )* ( "?" expression ":"  expression )?
	 * @return CompileJumpVector if expression was parsed or return null.
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
			short xTyp = _g._tstack[_g._sp];
			int xVal = _g._cstack[_g._sp];
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
				_g.topToBool();  // resolve X_ATTR_REF etc
				continue;
			}
			if (operator == OR_SYM || operator == XOR_SYM) {
				_g.topToBool();
				short yTyp = _g._tstack[_g._sp];
				int yVal = _g._cstack[_g._sp];
				if (xTyp == XD_BOOLEAN && yTyp == XD_BOOLEAN) {
					if (xVal >= 0 && yVal >= 0) { // both constants
						boolean a = _g.getCodeItem(xVal).booleanValue();
						boolean b = _g.getCodeItem(yVal).booleanValue();
						_g.replaceTwo(new DefBoolean(operator==OR_SYM ? (a | b) : (a ^ b)));
					} else {
						_g.addCode(new CodeI1(XD_BOOLEAN, operator == OR_SYM ? OR_B : XOR_B), -1);
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
			short xTyp = _g._tstack[_g._sp];
			int xVal = _g._cstack[_g._sp];
			if (xTyp != XD_BOOLEAN) {
				_g._tstack[_g._sp] = XD_BOOLEAN;
				xVal = _g._cstack[_g._sp] = -2; // prevent code optimizing
			}
			nextSymbol();
			// prepare false jump and else jump
			if (xVal >= 0) {
				// the bollean value on top of stack is constant, so ignore the unused branche.
				boolean isTrue = _g.getCodeItem(xVal).booleanValue();
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
				xTyp = XD_VOID;
				xVal = -1;
				error(XDEF.XDEF438); //Value expected
			} else {
				xTyp = _g._tstack[_g._sp];
				xVal = _g._cstack[_g._sp];
			}
			if (!checkSymbol(COLON_SYM)) {//':'
				_g._sp++; //if error just simulate stack value or previous type
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
			} else {
				if (_g._sp >= 0) { // set top of stack is not constant!
					_g._cstack[_g._sp] = -1;
				}
				CodeI1 jmp = new CodeI1(XD_VOID, JMP_OP);
				_g.addJump(jmp);
				jumpVector.resoveFalseJumps(_g._lastCodeIndex + 1);
				_g._sp = sp; // return stack!
				short yTyp;
				sp = _g._sp = sp0;
				compileValue();
				if (sp == _g._sp) {
					yTyp = XD_VOID;
					error(XDEF.XDEF438); //Value expected
				} else {
					yTyp = _g._tstack[_g._sp];
					if (xTyp == XD_NULL && xVal >= 0) {
						switch (yTyp) {
							case XD_STRING:
								_g.setCodeItem(xVal, new DefString());
								xTyp = _g._tstack[_g._sp] = XD_STRING;
								break;
							case XD_ELEMENT:
								_g.setCodeItem(xVal, new DefElement(null));
								xTyp =_g._tstack[_g._sp] = XD_ELEMENT;
								break;
							case XD_CONTAINER:
								_g.setCodeItem(xVal, new DefContainer((Object) null));
								xTyp = _g._tstack[_g._sp] = XD_CONTAINER;
								break;
						}
					}
					if (yTyp == XD_NULL && _g._cstack[_g._sp] >= 0) {
						_g.convertTopToType(xTyp);
						yTyp = _g._tstack[_g._sp];
					}
					_g._cstack[_g._sp] = -2; // prevent code optimizing
				}
				if (xTyp != yTyp) {
					if (xTyp!=XD_NULL && xTyp!=XD_ANY && xTyp!=XD_UNDEF && yTyp!=XD_ANY && yTyp!=XD_UNDEF) {
						//Incompatible types&{0}{: }
						error(XDEF.XDEF457, getTypeName(xTyp) + "," + getTypeName(yTyp));
					}
				}
				jmp.setParam(_g._lastCodeIndex + 1);
			}
		}
		return jumpVector;
	}

	/** Compile Boolean expression.
	 * @param jumpCondition if true then result will be generated as conditional jump sequence.
	 * @return CompileJumpVector object or null.
	 */
	private CompileJumpVector boolExpression(final boolean jumpCondition) {
		int sp = _g._sp;
		CompileJumpVector jumpVector = expr();
		if (jumpVector == null) {
			error(XDEF.XDEF423, "boolean");//Value of type '&{0}' expected
			jumpVector = new CompileJumpVector(); // prevent null pointer
		}
		short xTyp;
		int xVal;
		if (_g._sp > sp) {
			_g.topToBool();
			xTyp = _g._tstack[_g._sp];
			if (xTyp != XD_BOOLEAN && xTyp != XD_ANY && xTyp != XD_UNDEF) {
				error(XDEF.XDEF423, "boolean");//Value of type '&{0}' expected
				_g._tstack[_g._sp] = XD_UNDEF;
				_g._cstack[_g._sp] = -1;
			}
			xVal = _g._cstack[_g._sp];
		} else {
			_g._tstack[++_g._sp] = XD_BOOLEAN;
			xVal = _g._cstack[_g._sp] = -1;
			error(XDEF.XDEF423, "boolean"); //Value of type '&{0}' expected
		}
		if (xVal >= 0) {
			// the boolean value on top of stack is constant, so we'll ignore the unused branche.
			boolean expResult = _g.getCodeItem(xVal).booleanValue();
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

	/** Parse expression. Create jump vector. Return true if result of parse expression is jump vector.
	 * expression ::=  andExpression (operatorLevel5  andExpression )* operatorLevel5 ::= ( "|" | "^" | "||" )
	 *   ( "?" expression ":"  expression )?
	 * @return return true if parsed expression was compiled, otherwise return false.
	 */
	final boolean expression() {
		CompileJumpVector jumpVector = expr();
		if (jumpVector != null) {
			_g.jumpVectorToBoolValue(jumpVector);
			return true;
		}
		return false;
	}

	/** Get variable.
	 * @param name name of variable.
	 * @return CompileVariable object or null if variable was not found.
	 */
	private CompileVariable getVariable(final String name) {
		for (String s: _importLocals) {
			CompileVariable var = _g.getVariable(s + name);
			if (var != null) {
				return var;
			}
		}
		return _g.getVariable(name);
	}

	/** Get variable and report error if variable was not found.
	 * @param name name of variable.
	 * @return CompileVariable object or null if variable was not found.
	 */
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
		short yTyp = var == null ? XD_UNDEF : var.getType();
		if (_g._sp == sp) {
			_g.setUnDefItem();
		}
		short xTyp = _g._tstack[_g._sp];
		if (xTyp == XD_VOID) {
			error(XDEF.XDEF438); //Value expected
		} else if (xTyp == X_UNIQUESET || xTyp == X_PARSEITEM) {
			//Assignment of value of type &{0} is not allowed
			error(XDEF.XDEF488, getTypeName(xTyp));
		}
		if (op != ASSGN_SYM && var != null) {
			short code = UNDEF_CODE;
			switch (op) {
				case LSH_EQ_SYM:
				case RSH_EQ_SYM:
				case RRSH_EQ_SYM:
					if (xTyp == XD_LONG && yTyp == XD_LONG) {
						code = op==LSH_EQ_SYM ? LSHIFT_I : op==RSH_EQ_SYM ? RSHIFT_I : RRSHIFT_I;
					}
					break;
				case MUL_EQ_SYM:
					if (yTyp == XD_DOUBLE) {
						_g.topToFloat();
						xTyp = _g._tstack[_g._sp];
					}
					code = xTyp==XD_LONG ? MUL_I : MUL_R;
					break;
				case DIV_EQ_SYM:
					if (yTyp == XD_DOUBLE) {
						_g.topToFloat();
						xTyp = _g._tstack[_g._sp];
					}
					code = xTyp == XD_LONG ? DIV_I : DIV_R;
					break;
				case MOD_EQ_SYM:
					if (yTyp == XD_DOUBLE) {
						_g.topToFloat();
						xTyp = _g._tstack[_g._sp];
					}
					code = xTyp == XD_LONG ? MOD_I : MOD_R;
					break;
				case AND_EQ_SYM:
					if (xTyp == X_ATTR_REF) {
						_g.topToBool();
					}
					if (xTyp == XD_BOOLEAN) {
						code = AND_B;
					}
					break;
				case PLUS_EQ_SYM:
					if (yTyp == XD_STRING) {
						_g.topToString();
						xTyp = _g._tstack[_g._sp];
					} else if (yTyp == XD_DOUBLE) {
						_g.topToFloat();
						xTyp = _g._tstack[_g._sp];
					}
					code = xTyp==XD_LONG ? ADD_I : xTyp==XD_DOUBLE ? ADD_R : xTyp==XD_STRING ? ADD_S : code;
					break;
				case MINUS_EQ_SYM:
					if (yTyp == XD_DOUBLE) {
						_g.topToFloat();
						xTyp = _g._tstack[_g._sp];
					}
					if (xTyp == XD_LONG) {
						code = SUB_I;
					} else if (xTyp == XD_DOUBLE) {
						code = SUB_R;
					}
					break;
				case OR_EQ_SYM:
					if (xTyp == X_ATTR_REF) {
						_g.topToBool();
					}
					if (xTyp == XD_BOOLEAN) {
						code = OR_B;
					}
					break;
				case XOR_EQ_SYM:
					if (xTyp == X_ATTR_REF) {
						_g.topToBool();
					}
					if (xTyp == XD_BOOLEAN) {
						code = XOR_B;
					}
					break;
				default:
			}
			if (code == UNDEF_CODE) {
				error(XDEF.XDEF457); //Incompatible types
			} else {
				_g.addCode(new CodeI1(yTyp, code), -1);
			}
			xTyp = _g._tstack[_g._sp];
		} else {
			if (yTyp == XD_BOOLEAN && xTyp == XD_PARSERESULT) {
				_g.topToBool(); // force conversion to boolean!!!
			} else {
				if (yTyp == X_UNIQUESET_NAMED && var != null) { // named value of uniqueSet
					yTyp = var.getParseResultType(); // type of result
					_g.convertTopToType(yTyp); // convert to the required type
					if (keepValue) {
						_g.addCode(new CodeOp(yTyp, STACK_DUP),1);
					}
					_g.genLD(var); // load the uniqueset object
					String s = var.getValue().toString(); // name of variable
					_g.addCode(new CodeS1(yTyp, UNIQUESET_SETVALUEX, s),-2);
					return true;
				} else {
					_g.convertTopToType(yTyp);
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
			name = _g.genErrId(); // "#UNDEF" + _g._lastCodeIndex;
			_g.addVariable(name, xTyp, (byte) 'L', null);
		} else if (var.isFinal()) {
			error(XDEF.XDEF119, name); //Variable '&{0}' is 'final'; the value can't be assigned
		}
		_g.genST(name);
		if (keepValue) {
			_g.genLD(name);
		}
		return true;
	}

	final void initCompilation(final byte mode, final short returnType) {
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
			error(XDEF.XDEF451); //'break' command is not inside of switch statement or loop
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
			if (_blockInfo._continueJumps != null) {
				_blockInfo._continueJumps.add(jmp);
			}
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
//				_g._progLabels.put(name, Integer.parseInt(_g._lastCodeIndex+1));
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

	/** Check if statement is unreachable. */
	private void checkUnreachable() {
		if (_wasReturn || _wasContinue || _wasBreak) {
			error(XDEF.XDEF453); // Unreachable statement
		}
	}

	/** Parse and compile statement.
	 * ifStatement ::= "if" s? "(" s? expression s? ")" S? statement ( s? "else" s? statement )?
	 * whileStatement ::= "while" s? "(" s? expression s? ")" s? statement
	 * doStatement ::= "do" S? statement s? "while" s? "(" s? expression s? ")" ( s? "else" s? statement )?
	 * assignment ::= identifier s? "=" s? expression
	 * statement ::=  ( method | assignment | compoundStatement
	 *                |  ifStatement | whileStatement | doStatement)? s? ";"
	 * compoundStatement ::= "{" s? statement* s? "}"
	 * @return true if statement was parsed.
	 */
	final boolean statement() {
		boolean isFinal = false;
		switch (_sym) {
			case BEG_SYM: // '{' compound statement
				checkUnreachable();
				nextSymbol();
				initBlock(false, -1);
				while (statement()){}
				checkSymbol(END_SYM); // '}'
				closeBlock();
				return true;
			case SEMICOLON_SYM: // empty statement
				nextSymbol();
				return true;
			case INC_SYM:
			case DEC_SYM: {
				checkUnreachable();
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
								error(XDEF.XDEF118, "external"); //Duplicate specification of &{0}
							}
							wasFinal = true;
							nextSymbol();
							continue;
						case EXTERNAL_SYM:
							if (wasExternal) {
								error(XDEF.XDEF118, "external"); //Duplicate specification of &{0}
							} else {
								error(XDEF.XDEF411, "external"); //'&{0}' is not allowed here
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
					checkUnreachable();
					short varType = getVarTypeCode(_idName);
					if (nextSymbol() == IDENTIFIER_SYM) {
						String name = _idName;
						SPosition spos = getLastPosition();
						nextSymbol();
						varDeclaration(varType, name, wasFinal, false, (byte)'L', spos);
					} else {
						error(XDEF.XDEF454); //Variable identifier expected
					}
				}
				break;
			}
			case IDENTIFIER_SYM: {
				separateMethodNameFromIdentifier();
				int dx = addDebugInfo(false);
				labelOrSimplestatement(isFinal);
				setDebugEndPosition(dx);
				break;
			}
			case IF_SYM:
				checkUnreachable();
				nextSymbol();
				ifStatement();
				return true;
			case DO_SYM:
				checkUnreachable();
				nextSymbol();
				doStatement();
				break;
			case WHILE_SYM:
				checkUnreachable();
				nextSymbol();
				whileStatement();
				return true;
			case FOR_SYM:
				checkUnreachable();
				nextSymbol();
				forStatement();
				return true;
			case SWITCH_SYM:
				checkUnreachable();
				nextSymbol();
				switchStatement();
				return true;
			case CONTINUE_SYM: {
				checkUnreachable();
				_wasContinue = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				genContinueJump(new CodeI1(XD_VOID, JMP_OP));
				setDebugEndPosition(dx);
				break;
			}
			case BREAK_SYM: {
				checkUnreachable();
				_wasBreak = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				genBreakJump(new CodeI1(XD_VOID, JMP_OP));
				setDebugEndPosition(dx);
				break;
			}
			case RETURN_SYM: {
				checkUnreachable();
				_wasReturn = true;
				int dx = addDebugInfo(true);
				nextSymbol();
				int sp = _g._sp;
				short code = RET_OP;
				short xTyp = XD_VOID;
				if (_returnType != XD_VOID) {
					expression();
					if (sp + 1 == _g._sp) {
						if ((xTyp = _g._tstack[_g._sp]) != _returnType) {
							if (xTyp == XD_CONTAINER && _returnType == XD_STRING) {
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
				_g.addCode(new CodeI1(xTyp, code, _popNumParams), 0);
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
				return false;
			case CASE_SYM:
				error(XDEF.XDEF477); // 'case' whithout 'switch'
				return false;
			case ELSE_SYM:
				error(XDEF.XDEF478); // 'else' whithout 'if'
			default:
				return false;
		}
		if (_sym != END_SYM && _sym != ELSE_SYM) {
			checkSemicolon(new String(new char[] {SEMICOLON_SYM, BEG_SYM, END_SYM}));
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
		boolean wasVariable;
		switch (_sym) {
			case DOT_SYM: {
				// a) a method can be called on an object defined in X-Skript
				// b) an external method given by fully qualified name is called
				// try to find the defined variable
				if (wasVariable =_g.isVariable(name)) {// the object's method "name" has to be probe first
					if (!_g.genLD(name)) {
						if (_sym == DOT_SYM) {
							return qualifiedMethod(name, false);
						}
						error(XDEF.XDEF438); //Value expected
					}
				}
				while (_sym == DOT_SYM) {
					int lastPos = getIndex(); // remember the state of the parser
					String lastIdName = _idName;
					nextSymbol();
					if (_sym != IDENTIFIER_SYM) {
						error(XDEF.XDEF417); //Method name expected
						return true;
					}
					String cname = _idName;
					nextSymbol();
					int sp = _g._sp;
					int numPar = _sym == LPAR_SYM ? paramList(cname)._numPars : -1;
					if (numPar >= 0) { // '(' => method
						boolean wasClassMethod = true;
						//try to find and generate the internal method
						if (!_g.genClassMethod(cname, numPar)) {
							wasClassMethod = false; // the method is not internal in X-Skript
						}
						while (sp < _g._sp) {
							_g.genPop();
						}
						if(!wasVariable && !wasClassMethod) {
							// the "name" is not a variable defined in X-Skript and the method is also not
							// defined in X-Skript therefore it could be a path to a fully qualified external
							// method. Change the parser to the remembered state: (set the position on the
							// dot (.) between the method name and the path to the method's class)
							setIndex(lastPos);
							// set the last symbol (dot) and idName
							_sym = DOT_SYM;
							_idName = lastIdName;
							// Try to find external method and generate its code
							qualifiedMethod(name, false);
						} else {
							// External method was not generated.
							// Check and reports possible errors:
							if(!wasVariable) {
								//Variable was not defined but the class method is defined in X-Skript
								error(XDEF.XDEF233); //Statement error
							}
							if(!wasClassMethod) {
								//Variable is defined but the method was not defined in X-Skript
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
				method(name);
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
		checkUnreachable();
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
		_blockInfo._breakJumps = new ArrayList<>();
		int loopAddr = _blockInfo._continueAddr = _g._lastCodeIndex + 1;
		_blockInfo._jumps = true;
		int sp = _g._sp;
		CompileJumpVector jumpVector = null;
		if (_sym != SEMICOLON_SYM && (jumpVector = boolExpression(false)) != null) {
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
		List<XDValue> savedCode;
		if (_g._lastCodeIndex > lastCodeIndex) {
			savedCode = new ArrayList<>(_g._lastCodeIndex-lastCodeIndex);
			for (int i = lastCodeIndex + 1; i < _g._code.size(); i++) {
				savedCode.add(_g.getCodeItem(i));
			}
			if (!savedCode.isEmpty()) {
				_blockInfo._continueJumps = new ArrayList<>();
			}
			_g.removeCodeFromIndexAndClearStack(lastCodeIndex, sp, spMax);
		} else {
			savedCode = null;
		}
		if (!statement()) {
			error(XDEF.XDEF447); //Statement expected
		}
		 _wasBreak = _wasContinue = false;
		if (savedCode != null && !savedCode.isEmpty()) {
			_blockInfo._continueAddr = _g._lastCodeIndex + 1;
			// move code to the end of loop
			for (XDValue xv: savedCode) {
				_g.addCode(xv);
			}
			_g._sp = sp;
			if (spMax > _g._spMax) {
				_g._spMax = spMax;
			}
		}
		_g.addJump(new CodeI1(XD_VOID, JMP_OP, loopAddr)); // jump back to loop
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
		short xTyp = _g._sp > sp ? _g._tstack[_g._sp] : XD_VOID;
		if (xTyp != XD_LONG && xTyp != XD_STRING && xTyp != XD_UNDEF) {
			xTyp = XD_UNDEF;
			error(XDEF.XDEF446); //'int' or 'String' value expected
		}
		checkSymbol(RPAR_SYM); // ')'
		setDebugEndPosition(dx);
		XDValue xv = xTyp == XD_STRING ?
			new CodeSWTableStr() : new CodeSWTableInt();
		_g.addCode(xv, -1);
		checkSymbol(BEG_SYM);
		initBlock(true, -1);
		boolean wasDefault = false;
		int defaultAddr = -1;
		Map<Object, Integer> ht = new LinkedHashMap<>();
		boolean wasContinue = true;
		boolean wasReturn = true;
		OUTER:
		while (_sym != END_SYM) {
			_wasBreak = false;
			_wasContinue = false;
			_wasReturn = false;
			dx = addDebugInfo(true);
			switch (_sym) {
				case CASE_SYM:
					nextSymbol();
					sp = _g._sp;
					expression();
					setDebugEndPosition(dx);
					short yTyp;
					int yVal;
					if (_g._sp > sp) {
						yTyp = _g._tstack[_g._sp];
						yVal = _g._cstack[_g._sp];
					} else {
						yTyp = XD_VOID;
						yVal = -1;
					}	if (yVal < 0) {
						yTyp = XD_VOID;
					}	if (yTyp != xTyp) {
						if (xTyp != XD_UNDEF
							&& yTyp != XD_UNDEF) {
							//Constant expression of type &{0} expected
							error(XDEF.XDEF448,xTyp==XD_LONG ? "int":"String");
						}
					} else if (yTyp == XD_LONG) {
						Long v = _g.getCodeItem(yVal).longValue();
						_g._sp--;
						_g.removeLastCodeItem();
						if (ht.containsKey(v)) {
							error(XDEF.XDEF496, v); //Duplicated case variant '{0}'
						}
						ht.put(v, _g._lastCodeIndex+1);
					} else if (yTyp == XD_STRING) {
						String v = _g.getCodeItem(yVal).toString();
						_g._sp--;
						_g.removeLastCodeItem();
						if (ht.containsKey(v)) {
							error(XDEF.XDEF496, v); //Duplicated case variant '{0}'
						}
						ht.put(v, _g._lastCodeIndex+1);
					}
					break;
				case DEFAULT_SYM:
					//default:
					if (wasDefault) {
						error(XDEF.XDEF495); //'default' can't be duplicated in 'switch' statement
					}
					wasDefault = true;
					setDebugEndPosition(dx);
					nextSymbol();
					defaultAddr = _g._lastCodeIndex + 1;
					break;
				default:
					error(XDEF.XDEF449); //'case', 'default' or '}' expected
					while (statement()){}
					if (_sym == CASE_SYM || _sym == DEFAULT_SYM) {
						continue;
					}
					break OUTER;
			}
			checkSymbol(COLON_SYM);
			if (_sym == CASE_SYM || _sym == DEFAULT_SYM) {
				continue;
			}
			if (_sym == END_SYM) {
				break;
			}
			if (statement()) {
				while (_sym != CASE_SYM && _sym != DEFAULT_SYM && _sym != END_SYM) {
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
		if (xTyp == XD_LONG) {
			CodeSWTableInt icode = (CodeSWTableInt) xv;
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
		} else if (xTyp == XD_STRING) {
			CodeSWTableStr scode = (CodeSWTableStr) xv;
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
		short varType = getTypeId(name);
		if (varType < 0) {
			error(XDEF.XDEF412); //Type identifier expected
			varType = XD_UNDEF;
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
			varType = XD_UNDEF;
		}
		return varType;
	}

	/** Compile variable declaration statement.
	 * @param type the type of variable (XD_LONG, XD_STRING etc).
	 * @param varName the name of variable.
	 * @param isFinal if true variable is declared as final.
	 * @param isExternal if true variable is declared as external.
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
			varType = XD_UNDEF;
		} else if (type < 0 || type > XD_OBJECT) {
			error(XDEF.XDEF458); //Expected type identifier of variable
			varType = XD_UNDEF;
		} else {
			varType = type;
		}
		for(;;) {
			CompileVariable var = null;
			if (getTypeId(name) >= 0) {
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
					var.setFinal(false); //set now final flag as false; we'll set it again
				}
				assignment(name, false);
			} else if (var == null) {
				error(XDEF.XDEF454); //Variable name expected
			} else {
				if (isFinal && !isExternal) {
					error(XDEF.XDEF414); //Assignment statement expected
				} else if (varKind == 'G') {
					var.setInitialized(true);// default initialization (null...)
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
			_g.addCode(new CodeI1(XD_VOID, THROW_EXCEPTION), -1); // generate throw RuntimeException
		} else {
			error(XDEF.XDEF423, "Exception");//Value of type '&{0}' expected
		}
		_wasReturn = true;
	}

	/** Parse expression in parenthesis and generate conditioned jump.
	 * If result of expression is a constant value result of the method is
	 * fixed jump or null.
	 * <p>parExpression::= "(" expression ")"
	 * @param boolean jumpCondition;
	 * @return conditioned jump, fixed jump, or null;
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
		byte mode = (NO_MODE | ANY_MODE);
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
		List<String> paramNames = new ArrayList<>();
		List<SPosition> spositions = new ArrayList<>();
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
						XDValue xv;
						if (_sym == CONSTANT_SYM) {
							xv = _parsedValue;
							nextSymbol();
						} else if (_sym == IDENTIFIER_SYM && (paramType = getTypeId(_idName)) > 0) {
							xv = DefNull.genNullValue(paramType);
						} else {
							xv = new DefNull();
							error(XDEF.XDEF412); //Type identifier expected
						}
						keyParams.setXDNamedItem(name, xv);
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
					paramType = XD_UNDEF;
				} else if ((paramType = getTypeId(_idName)) <= 0) {
					error(XDEF.XDEF412); //Type identifier expected
					nextSymbol();
					if (_sym != IDENTIFIER_SYM) {
						if (_sym == COMMA_SYM) {
							nextSymbol();
							continue;
						}
					}
					paramType = XD_UNDEF;
				} else {
					nextSymbol();
				}
				if (_sym == IDENTIFIER_SYM) {
					String pname = _idName;
					SPosition sp = getLastPosition();
					if (getTypeMethod(X_NOTYPE_VALUE, pname) != null) {
						error(XDEF.XDEF419); //Name of parameter expected
					} else {
						if (paramNames.indexOf(pname) >= 0) {
							error(XDEF.XDEF420, pname); //Parameter redefinition of &{0}
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
			_g.addVariable(params[i], paramTypes[i],(byte)'L',spositions.get(i)).setInitialized(true);
		}
		CodeI1 initCode = paramTypes.length == 0 ?
			new CodeI1(XD_VOID,INIT_NOPARAMS_OP,0) : new CodeI2(XD_VOID,INIT_PARAMS_OP,0,paramTypes.length);
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
					_g.addCode(new CodeI1(XD_VOID, RET_OP, _popNumParams), 0);
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
	 * @param pnode PNode with source data.
	 * @param lang language of lexicon.
	 * @param deflt default language.
	 * @param xp XDPool object.
	 * @param languages List of languages in this lexicon.
	 */
	final void compileLexicon(final PNode pnode,
		final SBuffer lang,
		final SBuffer deflt,
		final XDPool xp,
		final List<Map<String,String>> languages) {
		setSource(pnode._value,
			pnode._xdef==null ? "": pnode._xdef.getName(),
			pnode._xdef,
			pnode._xdVersion,
			pnode._nsPrefixes,pnode._xpathPos);
		String language;
		if (lang == null || (language = lang.getString().trim()).isEmpty() || !chkJavaName(language)) {
			error(XDEF.XDEF410, "language name");//'&{0}' expected
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
			props = new LinkedHashMap<>();
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
					error(spos,XDEF.XDEF147, key); //The reference alias in lexicon must be unique: &{0}
				}
				if (xp.findModel(key) == null) {
					//Invalid reference of lexicon item &{0} for language &{1}
					error(spos, XDEF.XDEF150, key, language);
				}
				if (nextSymbol() == IDENTIFIER_SYM) {
					props.put(key, _idName);
				} else if (_sym == CONSTANT_SYM) {
					if (_parsedValue == null || _parsedValue.getItemId() != XD_STRING) {
						error(XDEF.XDEF216);//Unexpected character&{0}{: '}{'}
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
			error(XDEF.XDEF216);//Unexpected character&{0}{: '}{'}
		}
	}

	/** Compile BNF grammar.
	 * @param sName SBuffer with name of BNF grammar variable.
	 * @param sExtends SBuffer with name of BNF grammar to be extended.
	 * @param pnode PNode with BNF grammar.
	 * @param local true if it is in the declaration part with the local scope.
	 */
	final void compileBNFGrammar(final SBuffer sName,
		final SBuffer sExtends,
		final PNode pnode,
		final boolean local) { // namespace
		String defName = pnode._xdef == null ? null : pnode._xdef.getName();
		setSource(sName, defName, pnode._xdef, pnode._xdVersion, pnode._nsPrefixes, pnode._xpathPos);
		String name = sName.getString();
		if (local) {
			name = defName+'#'+name;
		}
		CompileVariable var = _g.getVariable(name);
		if (var == null) { // OK
			var = _g.addVariable(name, XD_BNFGRAMMAR, (byte) 'G', sName);
		} else {// ERROR
			//Repeated declaration of variable '&{0}'&{#SYS000}&{1}({; (already declared: }{)}
			_g.putRedefinedError(sName, XDEF.XDEF450, sName.getString(), var.getSourcePosition());
		}
		DefBNFGrammar di = null;
		int extVar = -1;
		boolean isFinal = true;
		if (sExtends != null) { // extension
			setSourceBuffer(sExtends);
			String s = sExtends.getString();
			CompileVariable evar;
			if ((evar = _g.getVariable(s)) == null) {
				error(sExtends,XDEF.XDEF105,s,name);//BNF grammar '&{0}' is not available for extension '&{1}'
				return;
			} else if (evar.getType() != XD_BNFGRAMMAR) {
				error(XDEF.XDEF457, getTypeName(evar.getType()) + ",BNFGrammar");//Incompatible types&{0}{: }
			} else {
				extVar = evar.getOffset();
				isFinal = evar.isFinal();
				di = (DefBNFGrammar) evar.getValue();
				if (di.grammarValue() == null) {
					error(sExtends, XDEF.XDEF464, s);//Variable '&{0}' might be not initialized
					return;
				}
			}
		}
		SBuffer s = pnode._value == null ? new SBuffer("") : pnode._value;
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

	/** Remove redundant part of type name.
	 * @param typeName source type name.
	 * @return type name without redundant part.
	 */
	private static String canonizeTypeName(final String typeName) {
		if (typeName.startsWith("java.lang.") && typeName.indexOf('.', "java.lang.".length()) < 0){
			return typeName.substring("java.lang.".length());
		} else if (typeName.startsWith("java.math.BigDecimal")) {
			return "BigDecimal";
		} else if (typeName.startsWith("java.math.BigInteger")) {
			return "BigInteger";
		} else if (typeName.startsWith("org.xdef.proc.")
			&& typeName.indexOf('.', "org.xdef.proc.".length()) < 0) {
			return typeName.substring("org.xdef.proc.".length());
		} else if (typeName.startsWith("org.xdef.model.")
			&& typeName.indexOf('.', "org.xdef.model.".length()) < 0) {
			return typeName.substring("org.xdef.model.".length());
		} else if (typeName.startsWith("org.xdef.sys.") && typeName.indexOf('.', "org.xdef.sys.".length())<0){
			return typeName.substring("org.xdef.sys.".length());
		} else if (typeName.startsWith("org.xdef.") && typeName.indexOf('.', "org.xdef.".length()) < 0) {
			return typeName.substring("org.xdef.".length());
		}
		return typeName;
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
		String cName;
		Class<?> mClass;
		boolean wasError = false;
		String rType = canonizeTypeName(_idName); // result type of method
		// we have now the name of type of result or "void"
		if (nextSymbol() == LSQ_SYM) {
			if (nextSymbol() != RSQ_SYM) {
				errorAndSkip(XDEF.XDEF410, ']', ";"); //'&{0}' expected
				return;
			}
			nextSymbol();
			rType += "[]";
		}
		short resultType = getClassTypeID(rType, cl);
		if (resultType == XD_UNDEF) {
			error(XDEF.XDEF412);//Type identifier expected
			wasError = true;
		}
		// Now we read the classified name of method. The symbol is an identifier - either class name,
		// or class with method name; class name can contain the used language name
		if (_sym != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF220, ";");//Qualified method name expected
			return;
		}
		//classified name of method
		separateMethodNameFromIdentifier();
		cName = _idName; // className
		try {
			mClass = Class.forName(_idName, false, cl);
		} catch (ClassNotFoundException ex) {
			if (!_g._ignoreUnresolvedExternals) {
				error(XDEF.XDEF228, _idName); //Class '&{0}' not exists
			}
			mClass = null;
			wasError = true;
		}
		if (nextSymbol() != DOT_SYM){
			errorAndSkip(XDEF.XDEF220, ";");//Qualified method name expected
			return;
		}
		if (nextSymbol() != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF220, ";");//Qualified method name expected
			return;
		}
		String methodName = _idName;
		String aliasName = methodName;//we prepare alias same as method name
		// parse the list of types of parameters
		if (nextSymbol() != LPAR_SYM) {
			errorAndSkip(XDEF.XDEF410, '(', ";"); //'&{0}' expected
			return;
		}
		List<String> params = new ArrayList<>();
		String classPar = null;
		// read first parameter
		if (nextSymbol() == IDENTIFIER_SYM) {
			String s = canonizeTypeName(_idName);
			nextSymbol();
			if ("XXElement".equals(s) || "XXData".equals(s) || "XXNode".equals(s)) {
				classPar = "org.xdef.proc." + s;
			} else {
				if ("XDValue".equals(s) && _sym == LSQ_SYM) {
					if (nextSymbol() != RSQ_SYM) {
						errorAndSkip(XDEF.XDEF410, ']', ";"); //'&{0}' expected
						return;
					}
					if (nextSymbol() == COMMA_SYM) {
						error(XDEF.XDEF223); //After XDValue[] parameter can't follow other parameter
						wasError = true;
					}
					params.add(null);
				} else {
					short paramType;
					if ("byte".equals(s) && _sym == LSQ_SYM) {
						if (nextSymbol() != RSQ_SYM) {
							errorAndSkip(XDEF.XDEF410, ']', ";");//'&{0}' expected
							return;
						}
						nextSymbol();
						paramType = XD_BYTES;
						s += "[]";
					} else {
						paramType = getClassTypeID(s, cl);
					}
					if (paramType == XD_UNDEF) {
						error(XDEF.XDEF412); //Type identifier expected
						wasError = true;
					}
					params.add(s);
				}
			}
			if (_sym == IDENTIFIER_SYM) { // the name of parameter we ignore
				nextSymbol(); // just read it
			}
			// read following parameters
			while (_sym == COMMA_SYM) {
				if (nextSymbol() != IDENTIFIER_SYM) {
					errorAndSkip(XDEF.XDEF412, ",);");//Type identifier expected
					wasError = true;
					continue;
				}
				s = canonizeTypeName(_idName);
				nextSymbol();
				// check first parameter, it can be XXNode
				if (("XDValue".equals(s)) && _sym == LSQ_SYM) {
					if (nextSymbol() != RSQ_SYM) {
						errorAndSkip(XDEF.XDEF410, ']', ";");//'&{0}' expected
						return;
					}
					if (nextSymbol() == COMMA_SYM) {
						error(XDEF.XDEF223); //After XDValue[] parameter can't follow other parameter
						wasError = true;
					}
					params.add(null);
				} else {
					short paramType;
					if ("byte".equals(s) && _sym == LSQ_SYM) {
						if (nextSymbol() != RSQ_SYM) {
							errorAndSkip(XDEF.XDEF410, ']', ";");//'&{0}' expected
							return;
						}
						nextSymbol();
						paramType = XD_BYTES;
						s += "[]";
					} else {
						paramType = getClassTypeID(s, cl);
					}
					if (paramType == XD_UNDEF) {
						error(XDEF.XDEF412); //Type identifier expected
					}
					params.add(s);
				}
				if (_sym == IDENTIFIER_SYM) {
					nextSymbol();
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
		} else if (_sym != SEMICOLON_SYM && _sym != END_SYM && _sym != NOCHAR) {
			errorAndSkip(XDEF.XDEF410, ";}", ";"); //'&{0}' expected
		}
		if (wasError) {
			return;
		}
		if (mClass != null) { // the Java reflection method object
			Method method = getExtMethod(cl, mClass, methodName, classPar, params);
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
				String localName = local && _actDefName != null ? _actDefName + '#' + aliasName : aliasName;
				if (resultType != getClassTypeID(method.getReturnType())) {
					error(XDEF.XDEF226, mName);//Result type of the extenal method '&{0}' differs
				} else if (((modifiers = method.getModifiers()) & Modifier.STATIC) == 0
					 && (modifiers & Modifier.PUBLIC) == 0) {
					error(XDEF.XDEF466, mName); //External method '&{0}' must be 'static' and 'public'
				} else if ((m=_g.addDeclaredMethod(localName, method))!=null && !m.equals(method)) {
					error(XDEF.XDEF227, aliasName); //Ambiguous redeclaration of the external method&{0}
				}
			} else {
				error(XDEF.XDEF225, mName, cName); //External method '&{0}' was not found in class '&{1}'
			}
		}
	}

	/** Get class corresponding to class name of parameter of method.
	 * @param clsName name of class of parameter.
	 * @param cl actual class loader.
	 * @return class corresponding to class name of parameter of method.
	 */
	private static Class<?> getClassParam(final String clsName, final ClassLoader cl) {
		if (clsName.indexOf('.') >= 0) {
			try {
				return Class.forName(clsName, false, cl);
			} catch (ClassNotFoundException ex) {}
		}
		switch (clsName) { // must be Java 1.6, so we must use if
			case "Long": return Long.class;
			case "int": return Integer.TYPE;
			case "Integer": return Integer.class;
			case "short": return Short.TYPE;
			case "Short": return Short.class;
			case "byte": return Byte.TYPE;
			case "Byte": return Byte.class;
			case "Double": return Double.class;
			case "float": return Float.TYPE;
			case "Float": return Float.class;
			case "boolean": return Boolean.TYPE;
			case "Boolean": return Boolean.class;
			case "BigDecimal": return BigDecimal.class;
			case "char": return Character.TYPE;
			case "Character": return Character.class;
			case "String": return String.class;
		}
		return getTypeClass(getClassTypeID(clsName,cl));
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
		final List<String> params) {
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

	/** Compile declaration part - methods, types, variables and init sections.
	 * @param local true if it is a declaration with the local scope within a X-definition.
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
						error(XDEF.XDEF118,"final"); //Duplicate specification of &{0}
					}
					isFinal = true;
					nextSymbol();
				} else if (_sym == EXTERNAL_SYM) {
					if (isExternal) {
						error(XDEF.XDEF118, "external"); //Duplicate specification of &{0}
					}
					isExternal = true;
					nextSymbol();
				}
			} while (_sym == FINAL_SYM || _sym == EXTERNAL_SYM);
			switch (_sym) {
				case IDENTIFIER_SYM: {
					String name = _idName;
					if ("method".equals(name)) {
						if (isFinal) {
							lightError(XDEF.XDEF411, "final"); //&{0}' is not allowed here
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
						} else if (_sym == NOCHAR || _sym == SEMICOLON_SYM || _sym == COLON_SYM
							|| _sym == ASSGN_SYM || _sym == COMMA_SYM) {
							// declaration of variable
							int actAdr = _g._lastCodeIndex;
							CodeI1 lastStop = _g.getLastStop();
							varDeclaration(varType, name, isFinal, isExternal, (byte) 'G', spos);
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
				if (_sym==IDENTIFIER_SYM && getTypeId(_idName)>=0) {
					break;
				} else { // ???
					nextChar(); // ???
				}
			}
		}
	}

	/** Compile type declaration.
	 * @param varKind variable kind ('G' .. global or 'X' .. model).
	 * @param local true if it is in the declaration part with the local scope.
	 */
	final void compileType(final byte varKind, final boolean local) {
		if (nextSymbol() != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF416, ';');//Identifier expected&
			return;
		}
		SPosition spos = getLastPosition();
		String name, gName;
		name = gName = _idName;
		if (local) {
			name = _actDefName + '#' + name;
		}
		if (varKind != 'X' && _g.getVariable(name) != null) {
			//Repeated declaration of type '&{0}'&{#SYS000}&{1}({; (already declared: }{)}
			_g.putRedefinedError(null, XDEF.XDEF470, name, _g.getVariable(name).getSourcePosition());
			name = _g.genErrId(); // "UNDEF$$$";
		}
		nextSymbol();
		if (_sym != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF416, ';');//Identifier expected&
			return;
		}
		String rName = (local) ? _actDefName + '#' + _idName : _idName;
		if (name.equals(_idName) || gName.equals(_idName) || name.equals(rName)) {
			error(XDEF.XDEF385); //The type name must not match the name of the referenced type
		}
		CompileVariable rVar =
			(CompileVariable) _g._globalVariables.getXVariable(rName);
		if (rVar == null && local) { // if local not found try global
			rName = _idName;
			rVar = (CompileVariable) _g._globalVariables.getXVariable(rName);
		}
		CompileVariable var;
		if (rVar!=null&&varKind==rVar.getKind() && rVar.getKeyIndex()==-1 && rVar.getParseMethodAddr()!=-1
			&& X_PARSEITEM==rVar.getType() && !rName.equals(name) &&!"Qname".equals(name)
			&& !"NCname".equals(name) && !"tokens".equals(name)) {
			SPosition spos1 = getPosition();
			if (nextSymbol() != CHECK_SYM) {
				// it is a reference to an other declared type
				var = _g.addVariable(name, X_PARSEITEM, varKind, spos);
				var.setKeyRefName(rName); // name of referenced type
				var.setParseMethodAddr(rVar.getParseMethodAddr());
				var.setCodeAddr(rVar.getCodeAddr());
				var.setParseResultType(rVar.getParseResultType());
				if (_sym == LPAR_SYM) {
					if (nextSymbol() == RPAR_SYM) {
						nextSymbol();
					} else {//parameter list must be empty (if declared)
						error(XDEF.XDEF384); //Parameters not allowed here
					}
				}
				if (_sym == SEMICOLON_SYM) {
					nextSymbol();
				} else if(_sym != END_SYM && !eos()) {
					error(XDEF.XDEF410, ';');//'&{0}' expected
				}
				return;//copy of referred CompileVariable added to table
			} else {
				setPosition(spos1);
				_sym = IDENTIFIER_SYM;
			}
		}
		int dx = addDebugInfo(true);
		if (rVar == null && varKind == 'X') {
			if (!expression() || _g._tstack[_g._sp] != XD_PARSER) {
				//Value of type &{0} expected
				errorAndSkip(XDEF.XDEF423, String.valueOf(END_SYM), "Parser");
			} else {
				var = _g.addVariable(name, _g._tstack[_g._sp], varKind, spos);
				if (_g._cstack[_g._sp] >= 0) {
					var.setValue(_g._code.get(_g._cstack[_g._sp]));
					var.setFinal(true);
				}
				_g.genST(name);
			}
			return;
		}
		CodeI1 jmp = null;
		if (varKind == 'X') {
			_g.addJump(jmp = new CodeI1(XD_VOID, JMP_OP));
		}
		int addr = compileCheckMethod("").getParam();
		short type;
		if (rVar!=null && rVar.getType()==X_PARSEITEM) {
			type = rVar.getParseResultType();
		} else {
			type = XD_STRING;
			if (addr + 2 == _g._lastCodeIndex) {
				if (_g._code.get(addr).getCode()==LD_CONST && _g._code.get(addr).getItemId()==XD_PARSER
					&& _g._code.get(addr+1).getCode()==PARSE_OP && _g._code.get(addr+2).getCode()==STOP_OP) {
					XDParser p = (XDParser) _g._code.get(addr);
					type = p.parsedType();
					String s = p.getDeclaredName();
					s = (s!=null && !s.isEmpty() ? s + ";" : "") + name;
					p.setDeclaredName(s);
				}
			}
		}
		setDebugEndPosition(dx);
		if (jmp != null) { // Model variable
			jmp.setParam(_g._lastCodeIndex + 1); // update jump target
		}
		var = _g.addVariable(name, X_PARSEITEM, varKind, spos);
		var.setParseMethodAddr(addr);
		var.setParseResultType(type);
	}

	/** Compile "var" section of the uniqueSet declaration (the named values).
	 * @param uniquesetName the name of the uniqueSet object.
	 * @param varMap the map with named values.
	 * @param keyItems key items (to check if the name already was used)
	 * @return the map with variables.
	 */
	private void uniquesetVar(final String uniquesetName,
		final Map<String,Short> varMap,
		final List<ParseItem> keyItems) {
		for(;;) {
			short type = (nextSymbol() == IDENTIFIER_SYM) ? getTypeId(_idName) : -1;
			if (type <= 0) {
				errorAndSkip(XDEF.XDEF458, //Expected type identifier of variable
					String.valueOf(END_SYM) + SEMICOLON_SYM);
				return;
			}
			if (nextSymbol() != IDENTIFIER_SYM) {
				errorAndSkip(XDEF.XDEF418,//Name of named item expected
					String.valueOf(END_SYM) + SEMICOLON_SYM);
				return;
			}
			for (ParseItem x: keyItems) {
				if (_idName.equals(x.getParseName())) {
					//Redefinition of the named value specification &{0} in the declaration of uniqueSet &{1}
					error(XDEF.XDEF146, _idName, uniquesetName);
				}
			}
			if (varMap.put(_idName, type) != null) {
				//Redefinition of the named value specification &{0} in the declaration of uniqueSet &{1}
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
		final List<ParseItem> keyItems) {
		if (_sym != IDENTIFIER_SYM) {
			errorAndSkip(XDEF.XDEF418, String.valueOf(END_SYM)); //Name of named item expected
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
				if (optional = "optional".equals(_idName)) {
					nextSymbol();
				}
				keyName = keyName.substring(0, ndx);
			}
			if (nameIsDeclared(keyName, varMap, keyItems)) {
				error(XDEF.XDEF324, uniquesetName + "." + keyName);//Redefinition of uniqueSet key &{0}
				return false;
			} else {
				int dx = addDebugInfo(true);
				CodeS1 checkMethod = compileCheckMethod("");
				int addr = checkMethod.getParam();
				setDebugEndPosition(dx);
				keyItems.add(new ParseItem(keyName,
					checkMethod.stringValue(), addr, keyItems.size(), XD_STRING, optional));
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
		final List<ParseItem> keyItems) {
		for (ParseItem item: keyItems) {
			if (name.equals(item.getParseName())) {
				return true;
			}
		}
		return varMap.containsKey(name);
	}

	/** Compile declaration of a uniqueSet.
	 * @param varKind variable kind ('G' .. global or 'X' .. model).
	 * @param local true if it is in the declaration part with the local scope.
	 * @param spos source position where the variable was declared.
	 */
	final void compileUniqueset(final byte varKind, final boolean local) {
		short varType;
		String uniquesetName;
		SPosition spos = null;
		if (nextSymbol() != IDENTIFIER_SYM) {
			error(XDEF.XDEF329); //Expected specification of set elements types
			uniquesetName = _g.genErrId(); // "UNDEF$$$";
		} else {
			uniquesetName = _idName;
			spos = getLastPosition();
			if (local) {
				uniquesetName = _actDefName + '#' + uniquesetName;
			}
		}
		if (varKind != 'X' && _g.getVariable(uniquesetName) != null) {
			//Repeated declaration of variable '&{0}'&{#SYS000}&{1}({; (already declared: }{)}
			_g.putRedefinedError(null, XDEF.XDEF450,
				uniquesetName, _g.getVariable(uniquesetName).getSourcePosition());
			uniquesetName = _g.genErrId(); // "UNDEF$$$";
		}
		List<ParseItem> keyItems = new ArrayList<>();
		Map<String,Short> varMap = new LinkedHashMap<>();
		CodeI1 jmp = null;
		if (varKind == 'X') {
			_g.addJump(jmp = new CodeI1(XD_VOID, JMP_OP));
		}
		switch (nextSymbol()) {
			case IDENTIFIER_SYM: { // type method
				varType = X_UNIQUESET;
				int dx = addDebugInfo(true);
				CodeS1 checkMethod = compileCheckMethod("");
				int addr = checkMethod.getParam();
				setDebugEndPosition(dx);
				short type = XD_STRING;
				keyItems.add(new ParseItem("", checkMethod.stringValue(), addr, keyItems.size(), type,false));
				break;
			}
			case BEG_SYM: { // explicit declaration of type method
				varType = X_UNIQUESET_M;
				nextSymbol();
				while (_sym != END_SYM) {
					switch (_sym) {
						case VAR_SYM:
							uniquesetVar(uniquesetName, varMap, keyItems);
							break;
						case IDENTIFIER_SYM:
							uniquesetKeyItem(uniquesetName, varMap, keyItems);
							break;
						default: // identified
							//In the uniqueSet is expected name of named item or named variable declaration
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
				errorAndSkip(XDEF.XDEF489, String.valueOf(END_SYM)); // Expected declaration of uniqueSet
				_g.addVariable(uniquesetName, X_UNIQUESET_M, varKind, spos);
				return;
		}
		if (jmp != null && varKind == 'X') { // Model variable
			jmp.setParam(_g._lastCodeIndex + 1);  // update jump target
		}
		int keySize = keyItems.size();
		if (keySize == 0) {
			return; // an error should be reported in the code above.
		}
		ParseItem[] keys = keyItems.toArray(new ParseItem[keySize]);
		boolean namedKey = !keys[0].getParseName().isEmpty();
		CompileVariable var = _g.addVariable(uniquesetName, varType, varKind, spos);
		var.setParseMethodAddr(keys[0].getParseMethodAddr());
		var.setParseResultType(keys[0].getParsedType());
		CodeI1 lastStop = varKind == 'G' ? _g.getLastStop() : null;
		var.setValue(new DefLong(0));
		String[] assinedValueNames = new String[0];
		if (namedKey) {
			var.setValue(new DefLong(-1));
			var.setParseMethodAddr(-1);
			if (keys.length > 1) {
				var.setParseResultType(XD_UNDEF);
			}
			for (int i = 0; i < keys.length; i++) {
				ParseItem key = keys[i];
				String keyName = key.getParseName();
				CompileVariable x = _g.getVariable(uniquesetName+"."+keyName);
				if (x == null) {
					x = _g.addVariable(uniquesetName+"."+keyName, X_UNIQUESET_KEY, varKind, spos);
					x.setOffset(var.getOffset());
				}
				x.setParseMethodAddr(key.getParseMethodAddr());
				x.setParseResultType(key.getParsedType());
				x.setKeyRefName(key.getDeclaredTypeName());
				x.setKeyIndex(i);
				x.setCodeAddr(x.getCodeAddr());
				x.setValue(new DefLong(i));
				x.setFinal(true);
				x.setInitialized(true);
			}
			int varNumber = varMap.size(); // number of assigned variables
			if (varNumber > 0) {
				assinedValueNames = new String[varNumber];
				int i = 0;
				for (String keyName: varMap.keySet()) {
					assinedValueNames[i++] = keyName;
					CompileVariable x = new CompileVariable(uniquesetName + "." + keyName,
						X_UNIQUESET_NAMED, var.getOffset(), varKind, spos);
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
		}
		int actAdr = _g._lastCodeIndex;
		CodeUniqueset u = new CodeUniqueset(keys, assinedValueNames, uniquesetName);
		_g.addCode(new CodeXD(u.getItemId(), UNIQUESET_NEWINSTANCE, actAdr, u), 1);
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
		initCompilation(TEXT_MODE, XD_PARSERESULT);
		if (_sym == BEG_SYM) { // explicite code (method body)
			if (_xdVersion >= XConstants.XD31) {
				//&{0}" is deprecated. Please use "&{1}" instead
				_g.reportDeprecated("explicit validation code", "declaration of validation type");
			}
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
				errorAndSkip(XDEF.XDEF426, keywords, "validation"); //Action &{0} expected
				_g._sp  = -1;
				result.setParam(-1);
				return result;
			}
			if (sp == _g._sp) {
				error(XDEF.XDEF107); //Validation method or a value of type ParseResult or boolean expected
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
					if ((start += 2) == _g._lastCodeIndex && _g._code.get(start).getCode() == PARSE_OP
						&& _g._code.get(start).getParam() == 1) {
						XDValue xv = _g._code.get(start - 1);
						if (xv.getCode() == LD_CODE) {
							xv = _g._code.get(xv.getParam());
						}
						if (xv.getCode() == LD_CONST && xv.getItemId() == XD_PARSER) {
							_g._code.set(start - 1, new CodeI1(XD_PARSERESULT, PARSEANDSTOP));
							_g._code.set(start, xv);
							break;
						}
					}
					_g.genStop();
					break;
				}
				case XD_BOOLEAN:
					_g.genStop();
					break;
				case X_PARSEITEM:
					if (_g._lastCodeIndex == start + 1) {
						XDValue xv = _g.getLastCodeItem();
						short code;
						if (X_PARSEITEM == xv.getItemId() && ((code = xv.getCode()) == LD_GLOBAL
							|| code == LD_LOCAL || code == LD_XMODEL)) {
							result.setParam2(typeName);
							_g.removeCodeFromIndexAndClearStack(start, sp, _g._spMax);
							String err = _g.genMethod(xv.stringValue(), 0);
							if (err != null) {
								error(XDEF.XDEF443, err);//Unknown method:'&{0}'
							}
							_g._sp  = -1;
							return result;
						}
					}
					break;
				case XD_UNDEF:
					break;
				default:
					error(XDEF.XDEF423, "boolean or ParseResult"); //Value of type '&{0}' expected
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
		initCompilation(TEXT_MODE, XD_STRING);
		// generate as code of method
		CodeI1 initCode = new CodeI1(XD_VOID, INIT_NOPARAMS_OP, 0);
		_g.addCode(initCode, 0);
		if (_sym != BEG_SYM) {
			int dx = addDebugInfo(false);
			if (!expression()) {
				errorAndSkip(XDEF.XDEF426, keywords, "fixed"); //Action &{0} expected
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

	/** Contains data assigned to a block statement. */
	private final class BlockInfo {
		/** Map with variable names. */
		private Map<String, CompileVariable> _variables;
		/** Array with "break" jumps. */
		private List<CodeI1> _breakJumps;
		/** Array with "continue" jumps. */
		private List<CodeI1> _continueJumps;
		/** The address where to contimue. */
		private int _continueAddr;
		/** Parent BlockInfo. */
		private BlockInfo _prevInfo;
		/** True if it contains break jumps. */
		private boolean _jumps;
		/** Last index of variables. */
		private final int _variablesLastIndex;
		/** Saved information if break or continue jump was compiled. */
		private final boolean _wasBreak_, _wasContinue_;

		/** Create new instance of BlockInfo.
		 * @param jumps True if it contains break jumps.
		 * @param continueAddr The address where to continue.
		 * @param prevInfo Parent BlockInfo.
		 * @param g link to CompileCode object.
		 */
		BlockInfo(final boolean jumps, final int continueAddr, final BlockInfo prevInfo, final CompileCode g){
			_variables = g._localVariables;
			_variablesLastIndex = g._localVariablesLastIndex;
			g._localVariables = new LinkedHashMap<>(_variables);
			_wasBreak_ = _wasBreak; // save value
			_wasContinue_ = _wasContinue; // save value
			_continueJumps = null;
			_jumps = jumps;
			if (_jumps) {
				_breakJumps = new ArrayList<>();
				if (continueAddr == -1) {
					if (prevInfo != null) {
						_continueAddr = prevInfo._continueAddr;
						_continueJumps = prevInfo._continueJumps;
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
					_continueJumps = prevInfo._continueJumps;
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
				if (_continueJumps != null) {
					for (int i = 0, j = _continueJumps.size(); i < j; i++) {
						_continueJumps.get(i).setParam(g._lastCodeIndex + 1);
					}
				}
			}
			_breakJumps = null;
			_continueJumps = null;
			g._localVariablesLastIndex = _variablesLastIndex;
			g._localVariables = _variables;
			BlockInfo result = _prevInfo;
			_wasBreak = _wasBreak_; // reset value
			_wasContinue = _wasContinue_; // reset value
			_variables = null;
			_prevInfo = null;
			return result;
		}
	}

	/** Description of list of "keyword" parameters (starting with "%"):
	 * to be compared with the parameters template. */
	private static final class ParamList {
		/** Total number of parameters */
		private int _numPars = 0;
		/** Array with keyword parameters. */
		private KeyPar[] _keyParams = new KeyPar[0];

		/** Create instance of key parameters. */
		private ParamList() {}

		/** Add key parameter.
		 * @param name name of parameter.
		 * @param type type ID of parameter.
		 * @param xv value of parameter.
		 * @return true if parameter was added or if the same parameter already exists. Return false
		 * if parameter exists with different key parameters.
		 */
		private boolean addKeyPar(final String name, final short type, final XDValue xv) {
			int len = _keyParams.length;
			KeyPar keyPar = new KeyPar(name, type, xv);
			if (len == 0) {
				_keyParams = new KeyPar[]{keyPar};
			}
			for (KeyPar k: _keyParams) {
				if (name.equals(k._name)) {
					return keyPar.eq(k);
				}
			}
			KeyPar[] oldParams = _keyParams;
			_keyParams = new KeyPar[len + 1];
			System.arraycopy(oldParams, 0, _keyParams, 0, len);
			_keyParams[len] = keyPar;
			return true;
		}

		/** This class contains a keyword parameter. */
		private static final class KeyPar {
			private final String _name;
			private final short _type;
			private final XDValue _xv;

			/** Create instance of KeyPar. */
			private KeyPar(final String name,final short type,final XDValue xv){
				_name = name; _type = type; _xv = xv;
			}

			/** Return true if the argument is same as this KeyPar instance. */
			private boolean eq(KeyPar k) {
				return _name.equals(k._name) && _type == k._type
					&& (_xv != null && _xv.equals(k._xv) || _xv == k._xv);
			}
		}
	}
}
