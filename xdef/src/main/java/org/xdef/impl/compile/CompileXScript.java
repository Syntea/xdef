package org.xdef.impl.compile;

import java.util.List;
import org.xdef.impl.code.CodeI1;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SUtils;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefString;
import org.xdef.impl.XCodeDescriptor;
import org.xdef.impl.XData;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XOccurrence;
import org.xdef.impl.XSelector;
import org.xdef.impl.XVariable;
import org.xdef.model.XMNode;
import java.util.Map;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_VOID;
import org.xdef.impl.XVariableTable;
import org.xdef.impl.code.CodeS1;
import static org.xdef.impl.code.CodeTable.CALL_OP;
import static org.xdef.impl.code.CodeTable.INIT_NOPARAMS_OP;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.PARSERESULT_MATCH;
import static org.xdef.impl.code.CodeTable.PARSE_OP;
import static org.xdef.impl.code.CodeTable.RETV_OP;
import static org.xdef.impl.code.CodeTable.STOP_OP;
import org.xdef.impl.code.DefBigInteger;
import static org.xdef.impl.compile.XScriptParser.ASSGN_SYM;
import static org.xdef.impl.compile.XScriptParser.BEG_SYM;
import static org.xdef.impl.compile.XScriptParser.COLON_SYM;
import static org.xdef.impl.compile.XScriptParser.COMMA_SYM;
import static org.xdef.impl.compile.XScriptParser.CONSTANT_SYM;
import static org.xdef.impl.compile.XScriptParser.CREATE_SYM;
import static org.xdef.impl.compile.XScriptParser.DEFAULT_SYM;
import static org.xdef.impl.compile.XScriptParser.END_SYM;
import static org.xdef.impl.compile.XScriptParser.EXTERNAL_SYM;
import static org.xdef.impl.compile.XScriptParser.FINALLY_SYM;
import static org.xdef.impl.compile.XScriptParser.FINAL_SYM;
import static org.xdef.impl.compile.XScriptParser.FIXED_SYM;
import static org.xdef.impl.compile.XScriptParser.FORGET_SYM;
import static org.xdef.impl.compile.XScriptParser.IDENTIFIER_SYM;
import static org.xdef.impl.compile.XScriptParser.IMPLEMENTS_SYM;
import static org.xdef.impl.compile.XScriptParser.INIT_SYM;
import static org.xdef.impl.compile.XScriptParser.LPAR_SYM;
import static org.xdef.impl.compile.XScriptParser.MATCH_SYM;
import static org.xdef.impl.compile.XScriptParser.NOT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ABSENCE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_EXCESS_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_FALSE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_ATTR_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_ELEMENT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_ROOT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_TEXT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_START_ELEMENT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_TRUE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_XML_ERROR_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTIONS_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTION_SYM;
import static org.xdef.impl.compile.XScriptParser.REFERENCE_SYM;
import static org.xdef.impl.compile.XScriptParser.REF_SYM;
import static org.xdef.impl.compile.XScriptParser.SCRIPT_SEPARATORS;
import static org.xdef.impl.compile.XScriptParser.SEMICOLON_SYM;
import static org.xdef.impl.compile.XScriptParser.TEMPLATE_SYM;
import static org.xdef.impl.compile.XScriptParser.TYPE_SYM;
import static org.xdef.impl.compile.XScriptParser.UNDEF_SYM;
import static org.xdef.impl.compile.XScriptParser.UNIQUE_SET_SYM;
import static org.xdef.impl.compile.XScriptParser.USES_SYM;
import static org.xdef.impl.compile.XScriptParser.VAR_SYM;
import static org.xdef.impl.compile.XScriptParser.symToName;
import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMTEXT;
import static org.xdef.sys.SParser.NOCHAR;

/** Compiler of XD script of headers, elements and attributes.
 * @author Vaclav Trojan
 */
final class CompileXScript extends CompileStatement {

	/** flag if options was specified in the script. */
	private boolean _options;

	/** Creates a new instance of CompileScript
	 * @param g The code generator.
	 * @param xmlVersion 10 .. "1.0", 11 .. "1.1"
	 * (see org.xdef.impl.XConstants.XMLxx).
	 * @param nsPrefixes array with name space prefixes.
	 * @param clsLoader The Class loader (used for external objects).
	 */
	CompileXScript(final CompileCode g,
		final byte xmlVersion,
		final Map<String, Integer> nsPrefixes,
		final ClassLoader clsLoader) {
		super(g, xmlVersion, CompileBase.NO_MODE, nsPrefixes, clsLoader);
	}

	/** Compile acceptLocal attribute from XDef header. */
	final void compileAcceptLocal(final List<String> locals) {
		for (;;) {
			String idName;
			if (nextSymbol() == IDENTIFIER_SYM) {
				idName = _idName + '#';
			} else if (_sym == '#') {
				idName = "#";
			} else {
				break;
			}
			if (locals.contains(idName)) {
				error(XDEF.XDEF351, idName);//Duplicate declaration of &{0}
			} else {
				locals.add(idName);
			}
			if (nextSymbol() != XScriptParser.COMMA_SYM) {
				break;
			}
		}
		if (_sym != NOCHAR) {
			error(XDEF.XDEF425); //Script error
		}
	}

	/** Compile external methods list from XDef header. */
	final void compileExtMethods() {
		while (nextSymbol() == SEMICOLON_SYM) {}
		while(_sym == IDENTIFIER_SYM) {
			compileExtMethod(false); // not local here!
			// method list separator
			if (_sym != SEMICOLON_SYM) {
				if (_sym == END_SYM || _sym == NOCHAR) {
					break;
				}
				errorAndSkip(XDEF.XDEF410, ";}", ";"); //";" expected
			} else {
				nextSymbol();
			}
		}
	}

	/** Compile script from X-definition header.*/
	final void compileXDHeader(final XDefinition def) {
		_options = false;
		nextSymbol();
		while (_sym != NOCHAR) {
			char sym = _sym;
			nextSymbol();
			switch (sym) {
				case SEMICOLON_SYM:
					continue;
				case OPTION_SYM:
				case OPTIONS_SYM:
					readOptions(def);
					continue;
				case INIT_SYM:
					if (def._init != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					def._init =
						compileSection(CompileBase.ANY_MODE, XD_VOID, sym);
					continue;
				case ON_ILLEGAL_ROOT_SYM:
					if (def._onIllegalRoot != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					def._onIllegalRoot =
						compileSection(CompileBase.ANY_MODE, XD_VOID, sym);
					continue;
				case ON_XML_ERROR_SYM:
					if (def._onXmlError != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					def._onXmlError =
						compileSection(CompileBase.ANY_MODE, XD_VOID, sym);
					continue;
				default:
			}
			if (_sym == NOCHAR) {
				break;
			}
			errorAndSkip(XDEF.XDEF425, SCRIPT_SEPARATORS); //Script error
		}
	}

	/** Generation of template values of attributes, texts, comments, PIs.
	 * @param sc XData model of attribute, text, comment, PI.
	 * @param parent parent model
	 */
	final void genTemplateData(final XData sc, final XNode parent) {
		if (parent != null && parent.getKind() == XMNode.XMELEMENT) {
			XElement p = (XElement) parent;
			sc.copyOptions(p);
		} else {
			sc.clearOptions();
			sc._trimAttr = 'F';
			sc._attrWhiteSpaces = 'F';
		}
		sc.clearActions();
		String result = getSourceBuffer();
		if ("$text".equals(sc.getName()) || "$comment".equals(sc.getName())) {
			if (sc._textWhiteSpaces == 'T') {
				result = SUtils.trimAndRemoveMultipleWhiteSpaces(result);
			} else if (sc._trimText  != 'F') {
				result = result.trim();
			}
			result = sc._textValuesCase == 'T' ? result.toUpperCase() :
				sc._textValuesCase == 'F' ? result.toLowerCase() : result;
		} else {
			if (sc._attrWhiteSpaces == 'T') {
				result = SUtils.trimAndRemoveMultipleWhiteSpaces(result);
			} else if (sc._trimAttr  != 'F') {
				result = result.trim();
			}
			result = sc._attrValuesCase == 'T' ? result.toUpperCase() :
				sc._attrValuesCase == 'F' ? result.toLowerCase() : result;
		}
		if (result.length() == 0 && ("$text".equals(sc.getName())
			|| "$comment".equals(sc.getName())
			|| sc._ignoreEmptyAttributes == 'T')){
			sc.setOccurrence(0, 1);
			sc._check = -1;
		} else {
			sc.setOccurrence(XData.FIXED, Integer.MAX_VALUE);
			sc._check = _g._lastCodeIndex + 1;
			_g._mode = CompileBase.TEXT_MODE;
			DefString defTmp = new DefString(result);
			_g._sp  = -1;
			_g.genLDC(defTmp);
			_g.internalMethod("eq", 1);
			_g.topToBool();
			_g.genStop();
			sc.setValueType(XD_STRING, "eq");
			sc._onAbsence = sc._onFalse = _g._lastCodeIndex + 1;
			_g.genLDC(defTmp);
			if ("$text".equals(sc.getName())) {
				_g.internalMethod("setText", 1);
			} else {
				_g.internalMethod("setComent", 1);
			}
			_g.genStop();
			sc._compose = _g._lastCodeIndex + 1;
			_g.genLDC(defTmp);
			_g.genStop();
			_g._sp  = -1;
		}
	}

	/** Compile type check (a method or expression).
	 * @param sc XData model of data value.
	 */
	private void compileTypeCheck(final XData sc) {
		if (_sym == CONSTANT_SYM && _parsedValue.getItemId() == XD_STRING) {
			sc._check = _g._lastCodeIndex + 1;
			byte gmode = _g._mode;
			_g._sp  = -1;
			_g._mode = CompileBase.TEXT_MODE;
			int dx = addDebugInfo(false);
			_g.genLDC(_parsedValue);
			_g.internalMethod("eq", 1);
			_g.topToBool();
			sc.setValueType(XD_STRING, "eq");
			nextSymbol();
			setDebugEndPosition(dx);
			checkSemicolon(SCRIPT_SEPARATORS);
			_g.genStop();
			_g._sp  = -1;
			_g._mode = gmode;
		} else {
			compileCheckExpression(sc);
		}
	}

	/** Compile Attribute, text, comment or PI script.
	 * @param sc The XData.
	 */
	final void compileDataScript(final XData sc) {
		_options = false;
		nextSymbol();
		_g._mode = CompileBase.TEXT_MODE;
		sc.clearOptions();
		sc.clearActions();
		XOccurrence occ = new XOccurrence(); // undefined occurrence
		sc.setValueType(XD_STRING, "string");
		while (_sym != NOCHAR) {
			if (_sym == SEMICOLON_SYM) {
				nextSymbol();
				continue;
			}
			if (isOccurrence(occ)) {
				if (!occ.isIgnore() && occ.maxOccurs() > 1
					&& !sc.getName().startsWith("$")) {
					//Occurrence of attribute or text value can't be more then 1
					error (XDEF.XDEF262);
				}
				compileTypeCheck(sc);
				continue;
			} else if (_sym == IDENTIFIER_SYM || _sym == LPAR_SYM
				|| _sym == NOT_SYM || (_sym == CONSTANT_SYM
				&& (_parsedValue.getItemId() == XD_STRING
				|| _parsedValue.getItemId() == XD_BOOLEAN))) {
				if (!occ.isSpecified()) {
					occ.setRequired();
				}
				compileTypeCheck(sc);
				continue;
			}
			char sym = _sym;
			nextSymbol();
			switch (sym) {
				case OPTION_SYM:
				case OPTIONS_SYM:
					readOptions(sc);
					continue;
				case FIXED_SYM: {
					if (sc._check < 0 && occ.isSpecified()) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					occ.setFixed();
					int addr = compileFixedMethod(SCRIPT_SEPARATORS);
					_g._sp  = -1;
					if (addr >= 0) {
						int check = sc._check;
						if (check >= 0
							&&_g._code.get(_g._lastCodeIndex).getCode()==STOP_OP
							&&_g._code.get(check).getCode() == 0
							&& _g._code.get(check).getItemId() == XD_PARSER) {
							if (addr + 3 == _g._lastCodeIndex
								&&_g._code.get(addr).getCode()==INIT_NOPARAMS_OP
								&&_g._code.get(addr).getParam() == 0
								&&_g._code.get(addr + 1).getCode() == 0
								&&_g._code.get(addr+1).getItemId() == XD_STRING
								&& _g._code.get(addr + 2).getCode() == RETV_OP
								&& _g._code.get(addr + 3).getCode() == STOP_OP){
								XDValue value = _g._code.get(addr + 1);
								XDParser p = (XDParser) _g._code.get(check);
								XDParseResult r =
									p.check(null,value.toString());
								if (r.errors()) {
									error(XDEF.XDEF481); //Incorrect fixed value
								}
							} else {
								sc.setValueType(XD_STRING, "string");
							}
						}
						sc._check = _g._lastCodeIndex + 1;
						_g.addCode(new CodeI1(XD_STRING, CALL_OP, addr), 1);
						_g.internalMethod("eq",1);
						_g.addCode(new CodeI1(XD_PARSERESULT,PARSE_OP,1),0);
						_g.genStop();
						_g._sp  = -1;
						sc._onFalse = _g._lastCodeIndex + 1;
						_g.genLDC(new DefString("XDEF515")); //Value error
						_g.genLDC(new DefString("Value error"));
						_g.internalMethod("error", 2);
						// let's continue with setting od _g.genStop();
						//continues with setText, which is also onAbsence
						_g._sp  = -1;
						sc._onAbsence = _g._lastCodeIndex + 1;
						_g.addCode(new CodeI1(XD_STRING, CALL_OP, addr), 1);
						_g.internalMethod("setText",1);
						_g.genStop();
						_g._sp  = -1;
					}
					continue;
				}
				case ON_TRUE_SYM:
					if (sc._onTrue != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._onTrue =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case ON_FALSE_SYM:
					if (sc._onFalse != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._onFalse =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case ON_ABSENCE_SYM:
					if (sc._onAbsence != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._onAbsence =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case ON_ILLEGAL_ATTR_SYM:
					if ("$text".equals(sc.getName())) {
						 //Script section{0}{ '}{'} is not allowed here
						errorAndSkip(XDEF.XDEF364,
							SCRIPT_SEPARATORS, "onIllegalAttr");
					} else {
						if (sc._onIllegalAttr != -1) {
							error(XDEF.XDEF422); //Duplicated script section
						}
						sc._onIllegalAttr =
							compileSection(CompileBase.ELEM_MODE,XD_VOID, sym);
					}
					continue;
				case ON_ILLEGAL_TEXT_SYM:
					if (!"$text".equals(sc.getName())) {
						 //Script section{0}{ '}{'} is not allowed here
						errorAndSkip(XDEF.XDEF364,
							SCRIPT_SEPARATORS, "onIllegalText");
					} else {
						if (sc._onIllegalText != -1) {
							error(XDEF.XDEF422); //Duplicated script section
						}
						sc._onIllegalText =
							compileSection(CompileBase.ELEM_MODE,XD_VOID, sym);
					}
					continue;
				case CREATE_SYM:
					if (sc._compose != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._compose =
						compileSection(CompileBase.TEXT_MODE, XD_STRING, sym);
					continue;
				case ON_START_ELEMENT_SYM:
					if (sc._onStartElement != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._onStartElement =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case INIT_SYM:
					if (sc._init != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._init =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case DEFAULT_SYM:
					if (sc._deflt != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					occ.setOptional();
					sc._deflt =
						compileSection(CompileBase.TEXT_MODE, XD_STRING, sym);
					continue;
				case FINALLY_SYM:
					if (sc._finaly != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._finaly =
						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
					continue;
				case MATCH_SYM:
					if (sc._match != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc._match =
						compileSection(CompileBase.TEXT_MODE,XD_BOOLEAN, sym);
					continue;
				case ON_EXCESS_SYM: //only for xd:attrs or text
					if (sc.getName().startsWith("$")) {
						if (sc._onExcess != -1) {
							error(XDEF.XDEF422); //Duplicated script section
						}
						sc._onExcess = compileSection(CompileBase.TEXT_MODE,
							XD_VOID, sym);
						continue;
					}
					errToken(sym);
					break;
				default:
			}
			if (sym == NOCHAR) {
				break;
			}
			errorAndSkip(XDEF.XDEF425, SCRIPT_SEPARATORS); //Script error
		}
		sc.setOccurrence(occ);
		if (!sc.isSpecified()) {
			sc.setOptional();
		} else if (sc.isFixed() && sc._deflt >= 0) {
			//Both actions 'fixed' and 'default' can't be specified
			error(XDEF.XDEF415);
			return;
		}
		if (sc._check == -1) {
			if (sc._onTrue != -1) {
				_g._sp  = -1;
				sc._check = _g._lastCodeIndex + 1;
				_g.genLDC(new DefBoolean(true));
				_g.genStop();
				_g._sp  = -1;
			}
			if (sc._onFalse != -1) {
				if (_g._chkWarnings) {
					warning(XDEF.XDEF114); //Action 'OnFalse' never invoked
				}
			}
		}
	}

	/** Compile check text
	 * @param sc The XCodeDescriptor.
	 */
	private void compileCheckExpression(final XData sc) {
		if (sc._check != -1) {
			error(XDEF.XDEF425); //Script error
		}
		sc._check = -2;
		if (_sym == SEMICOLON_SYM) { //';'
			nextSymbol();
			return;
		}
		if (_sym == NOCHAR) {
			return;
		}
		String typeName = _sym == IDENTIFIER_SYM ? _idName : "declared value";
		CodeS1 checkMethod = compileCheckMethod(SCRIPT_SEPARATORS);
		int check = checkMethod.getParam();
		sc.setRefTypeName(checkMethod.stringValue());
		if (check >= 0) {
			sc._check = check;
			//we try to set type of checked object and the type method name.
			sc.setValueType(CompileBase.getParsedType(typeName), typeName);
			XDParser p = null;
			if (_g._lastCodeIndex > 0 && check < _g._code.size()) {
				XDValue y = _g._code.get(check);
				if (y.getCode() == CALL_OP) {// execute type method
					int i = y.getParam();
					if (i >= 0 && i + 2 < _g._code.size()) {
						y = _g._code.get(i);
						int j = _g._code.get(i+1).getCode();
						if (j == PARSE_OP && y.getCode() == LD_CONST
							&& y.getItemId() == XD_PARSER) {
							j = _g._code.get(i+2).getCode();
							if (j == PARSERESULT_MATCH || j == STOP_OP) {
								p = (XDParser) y;
							}
						}
					}
				} else if (y instanceof XDParser) {
					p = (XDParser) y;
				}
				if (p != null) {
					sc.setValueType(p.parsedType(), p.toString());
				}
				if (_g.getVariable(typeName)==null && typeName.indexOf('.')>0) {
					String s = typeName.substring(0, typeName.lastIndexOf('.'));
					if (_g.getVariable(s) != null) {
						sc.setRefTypeName(s);
					}
				} else if (typeName != null && _g.getVariable(typeName) != null) {
					sc.setRefTypeName(typeName);
				}
			}
		}
	}

	/** Generation of template values of elements.
	 * @param sc XElement object.
	 * @param parent parent model
	 */
	final void genTemplateElement(final XElement sc, final XNode parent) {
		sc.clearActions();
		if (parent != null && parent.getKind() == XMNode.XMELEMENT) {
			XElement p = (XElement) parent;
			sc.copyOptions(p);
			sc._forget = p._forget;
			sc._clearAdoptedForgets = p._clearAdoptedForgets;
		}
		sc.setRequired();
		sc._template = true;
		sc._compose = _g._lastCodeIndex + 1;
		_g._sp  = -1;
		_g.addCode(new DefBoolean(true));
		_g.genStop();
		_g._sp  = -1;
	}

	final void initElementScript(final XElement sc) {
		sc.clearActions();
	}

	private void compileModelVariable() {
		boolean isFinal;
		if (isFinal = _sym == FINAL_SYM) {
			nextSymbol();
		} else if (_sym == EXTERNAL_SYM) {
			//The token '&{0}' is not allowed here
			error(XDEF.XDEF411, "external");
			nextSymbol();
		}
		switch (_sym) {
			case IDENTIFIER_SYM: {
				String name = _idName;
				nextSymbol();
				if (_sym == IDENTIFIER_SYM) { // was a type
					short varType = getTypeCode(name);
					name = _idName;
					SPosition spos = getLastPosition();
					nextSymbol();
					if (_sym == NOCHAR || _sym == SEMICOLON_SYM
						|| _sym == COLON_SYM || _sym == ASSGN_SYM
						|| _sym == COMMA_SYM) {
						//declaration of variable
						varDeclaration(varType,
							name, isFinal, false, (byte) 'X', spos);
						checkSemicolon(String.valueOf(IDENTIFIER_SYM));
					}
				}
				break;
			}
			case TYPE_SYM:
				compileType((byte) 'X', false);
				break;
			case UNIQUE_SET_SYM: {
				compileUniqueset((byte) 'X', false);
				break;
			}
			default:
				//Error in variable declaration
				errorAndSkip(XDEF.XDEF121, String.valueOf(END_SYM));
		}
		while (_sym == SEMICOLON_SYM) {
			nextSymbol();
		}
	}

	/** Compile variables of element.
	 * @param xel XElement object.
	 */
	private void elementVarDeclaration(final XElement xel) {
		int start = _g._lastCodeIndex;
		if (_sym == BEG_SYM) { // variable declaration in block
			nextSymbol();
			while (_sym != END_SYM && _sym != NOCHAR) { // may be more
				compileModelVariable();
			}
			checkSymbol(END_SYM);
		} else { // only one variable
			compileModelVariable();
		}
		String [] xvars = _g._varBlock.getVariableNames();
		for (int i = 0; xvars != null && i < xvars.length; i++) {
			String xvar = xvars[i];
			XVariable v = _g._varBlock.getXVariable(xvar);
			if (!v.isInitialized()) {
				short vtype = v.getType();
				switch (vtype) {
					case XD_BOOLEAN:
						_g.genLDC(new DefBoolean(false));
						break;
					case XD_DOUBLE:
						_g.genLDC(new DefDouble(0));
						break;
					case XD_LONG:
						_g.genLDC(new DefLong(0));
						break;
					case XD_DECIMAL:
						_g.genLDC(new DefDecimal(0));
						break;
					case XD_BIGINTEGER:
						_g.genLDC(new DefBigInteger(0));
						break;
					case CompileBase.X_PARSEITEM:
					default:
						_g.genLDC(DefNull.genNullValue(vtype));
				}
				_g.genST(xvar);
			}
			v.setInitialized(true);
		}
		if (_g._lastCodeIndex > start) {
			xel._varinit = start + 1;
			_g.genStop();
			xel._varsize = _g._varBlock.size();
			if (xel._varsize > 0 && _g._varBlock != null) {
				xel._vartable = new XVariableTable(xel._varsize);
				// copy CCompileVariables to the table as XVariables.
				for (int i = 0; i < xel._varsize; i++) {
					XVariable x = _g._varBlock.getXVariable(i);
					if (x != null) {
						xel._vartable.addVariable(_g._varBlock.getXVariable(i));
					}
				}
			} else {
				xel._vartable = null;
			}
		}
	}

	/** Compile element script.
	 * @param xel XElement object.
	 */
	final void compileElementScript(final XElement xel) {
		nextSymbol();
		_options = false;
		if (_sym == TEMPLATE_SYM) { // template script
			nextSymbol();
			xel._template = true;
			if (_sym == SEMICOLON_SYM) {
				while (_sym == SEMICOLON_SYM) { //';'
					nextSymbol();
				}
				xel._trimText = xel._definition._trimText;
				if (_sym == OPTION_SYM || _sym == OPTIONS_SYM) {
					nextSymbol();
					readOptions(xel);
				}
			}
			if (_sym != NOCHAR) {
				errorAndSkip(XDEF.XDEF425, SCRIPT_SEPARATORS); //Script error
			}
			genTemplateElement(xel, null);
			return;
		}
		initElementScript(xel);
		xel._template = false;
		XOccurrence occ = new XOccurrence();
		boolean wasRef = false, wasImpl = false;
		while (_sym != NOCHAR) {
			if (_sym == SEMICOLON_SYM) {
				nextSymbol();
				continue;
			}
			if (isOccurrence(occ)) {
				checkSemicolon(SCRIPT_SEPARATORS);
				continue;
			}
			char sym = _sym;
			if (_sym == IMPLEMENTS_SYM || _sym == USES_SYM) {
				if (wasImpl) {
					error(XDEF.XDEF422); //Duplicated script section
				} else {
					wasImpl = true;
				}
				skipBlanksAndComments();
				if (!isXModelPosition()) {
					_sym = UNDEF_SYM;
				} else {
					_implList.add(new CompileReference(
						_sym == IMPLEMENTS_SYM ? (short) 1 : (short) 0,
						xel, null, getParsedString(), getPosition()));
				}
				nextSymbol();
				continue;
			}
			nextSymbol();
			switch (sym) {
				case VAR_SYM:
					//"var" section must be declared before a varialbe is
					// refered
					elementVarDeclaration(xel);
					continue;
				case OPTION_SYM:
				case OPTIONS_SYM:
					readOptions(xel);
					continue;
				case ON_ABSENCE_SYM:
					if (xel._onAbsence != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onAbsence =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case CREATE_SYM:
					if (xel._compose != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._compose =
						compileSection(CompileBase.ELEM_MODE, XD_ANY, sym);
					continue;
				case INIT_SYM:
					if (xel._init != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					_g._allowBindSet = true; // allow bindSet method
					xel._init =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					_g._allowBindSet = false;
					continue;
				case ON_EXCESS_SYM:
					if (xel._onExcess != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onExcess =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case ON_START_ELEMENT_SYM:
					if (xel._onStartElement != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onStartElement =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case MATCH_SYM:
					if (xel._match != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._match = compileSection(CompileBase.ELEM_MODE,
						XD_BOOLEAN, sym);
					continue;
				case ON_ILLEGAL_ATTR_SYM:
					if (xel._onIllegalAttr != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onIllegalAttr =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case ON_ILLEGAL_TEXT_SYM:
					if (xel._onIllegalText != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onIllegalText =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case ON_ILLEGAL_ELEMENT_SYM:
					if (xel._onIllegalElement != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._onIllegalElement =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case REF_SYM:
					if (wasRef) {
						error(XDEF.XDEF422); //Duplicated script section
					} else {
						wasRef = true;
					}
					if (_g._varBlock.size() > 0) {
						//In model with reference can't be variable declaration
						error(XDEF.XDEF237);
					}
					if (_sym == REFERENCE_SYM) {
						int ndx = _idName.indexOf('#') + 1;
						int ndx1;
						Integer ns;
						if ((ndx1 = _idName.indexOf(':', ndx)) < 0) {
							ns = _g._nsPrefixes.get("");
						} else {
							String prefix = _idName.substring(ndx, ndx1);
							if ((ns = _g._nsPrefixes.get(prefix)) == null) {
								//Namespace for prefix '&{0}' is undefined
								error(XDEF.XDEF257, prefix);
							}
						}
						String nsUri =
							ns != null ? _g._namespaceURIs.get(ns) : null;
						xel.addNode(
							new CompileReference(CompileReference.XMREFERENCE,
							xel, nsUri, _idName, new SPosition(this)));
						nextSymbol();
					} else {
						error(XDEF.XDEF328); //Reference specification expected
					}
					checkSemicolon(SCRIPT_SEPARATORS);
					continue;
				case FINALLY_SYM:
					if (xel._finaly != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._finaly =
						compileSection(CompileBase.ELEM_MODE, XD_VOID, sym);
					continue;
				case FORGET_SYM:
					if (xel._forget != 0) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					xel._forget = 'T';
					checkSemicolon(SCRIPT_SEPARATORS);
					continue;
				case ON_TRUE_SYM:
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, "onTrue");
					break;
				case ON_FALSE_SYM:
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, "onFalse");
					break;
				default:
			}
			if (sym == NOCHAR) {
				break;
			}
			errorAndSkip(XDEF.XDEF425, SCRIPT_SEPARATORS); //Script error
		}
		xel.setOccurrence(occ);
		if (_sym != NOCHAR) {
			error(XDEF.XDEF425); //Script error
		}
	}

	/** Compile script Section or action.
	 * @param mode mode (ELEM_MODE, TEXT_MODE, ...)
	 * @param returnType expected type of result.
	 * @param section the symbol ID of the section.
	 * @return address of generated code or -2 if an error occurred.
	 */
	final int compileSection(final byte mode,
		final short returnType,
		final char section) {
		if (_sym == NOCHAR) {
			return -2;
		}
		int sp = _g._sp  = -1;
		int start = _g._lastCodeIndex;
		int result = start + 1;
		initCompilation(mode, returnType);
		if (_sym == BEG_SYM) { /*explicite code;*/
			nextSymbol();
			while (_sym != END_SYM && statement()) {}
			if (!wasReturn() && returnType != XD_VOID) {
				error(XDEF.XDEF421); //Missing 'return' statement
				result = -2; //error
			}
			checkSymbol(END_SYM, SCRIPT_SEPARATORS); //'}'
		} else { //expression or a statement expected;
			if (returnType == XD_VOID) {//statement
				int dx = addDebugInfo(false);
				if (!statement()) {
					//Action &{0} expected,
					errorAndSkip(XDEF.XDEF426,
						SCRIPT_SEPARATORS+';',"statement");
					result = -2; //error
				}
				setDebugEndPosition(dx);
			} else {
				int dx = addDebugInfo(false);
				if (!expression()) {//expression
					errorAndSkip(XDEF.XDEF426, //Action &{0} expected,
						SCRIPT_SEPARATORS + ';', symToName(section));
					result = -2; //error
				} else {
					setDebugEndPosition(dx);
					if (returnType != XD_VOID) {
						if (sp == _g._sp) {//we expect value
							error(XDEF.XDEF423,//Value of type '&{0}' expected
								CompileBase.getTypeName(returnType));
							result = -2; //error
						} else {
							short xType = _g._tstack[_g._sp];
							if (xType == CompileBase.X_ATTR_REF) {
								if (returnType == XD_BOOLEAN
									|| returnType == XD_ANY) {
									_g.topToBool();
								} else {
									_g.topToString();
								}
								xType = _g._tstack[_g._sp];
							}
							if (returnType == XD_STRING && xType != XD_STRING) {
								if (mode == CompileBase.TEXT_MODE
									&& section == CREATE_SYM) {
									_g.topToNullOrString();
								} else {
									_g.topToString();
								}
								xType = _g._tstack[_g._sp];
							}
							if (section == CREATE_SYM) {
								if (returnType==XD_ANY) {//group or elelement
									if (xType != XD_ELEMENT
										&& xType != XD_CONTAINER
										&& xType != XD_RESULTSET
										&& xType != XD_LONG
										&& xType != XD_BOOLEAN
										&& xType != XD_STRING
										&& xType != XD_NULL) {
										if (xType != XD_ANY
											&& xType != CompileBase.XD_UNDEF) {
											//Value of type '&{0}' expected
											error(XDEF.XDEF423,
												"\"create element\"");
											result = -2; //error
										}
									}
								}
							} else if (xType == XD_CONTAINER
								&& section == MATCH_SYM) {// only match
								_g.topToBool();
							} else if (xType != returnType) {
								if (xType != XD_ANY
									&& xType != CompileBase.XD_UNDEF) {
									//Value of type '&{0}' expected
									error(XDEF.XDEF423,
										CompileBase.getTypeName(returnType));
									result = -2; //error
								}
							}
						}
					}
				}
				checkSemicolon(SCRIPT_SEPARATORS);
			}
		}
		_g.genStop();
		_g._sp  = -1;
		return result;
	}

	/** Compile group script.
	 * @param sc group selector.
	 * @return reference or null.
	 */
	final SBuffer compileGroupScript(final XSelector sc) {
		nextSymbol();
		SBuffer ref = null;
		XOccurrence occ = new XOccurrence();
		while (_sym != NOCHAR) {
			if (_sym == SEMICOLON_SYM) {
				nextSymbol();
				continue;
			}
			if (isOccurrence(occ)) {
				checkSemicolon(SCRIPT_SEPARATORS);
				continue;
			}
			char sym = _sym;
			nextSymbol();
			switch (sym) {
				case MATCH_SYM:
					if (sc.getMatchCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setMatchCode(compileSection(CompileBase.ELEM_MODE,
						XD_BOOLEAN, sym));
				continue;
				case INIT_SYM:
					if (sc.getInitCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setInitCode(compileSection(CompileBase.ELEM_MODE,
						XD_VOID, sym));
					continue;
				case ON_ABSENCE_SYM:
					if (sc.getOnAbsenceCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setOnAbsenceCode(compileSection(CompileBase.ELEM_MODE,
						XD_VOID, sym));
					continue;
				case ON_EXCESS_SYM:
					if (sc.getOnExcessCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setOnExcessCode(compileSection(CompileBase.ELEM_MODE,
						XD_VOID, sym));
					continue;
				case CREATE_SYM:
					if (sc.getComposeCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setComposeCode(compileSection(CompileBase.ELEM_MODE,
						XD_ANY, sym));
					continue;
				case REF_SYM:
					if (_sym == REFERENCE_SYM) {
						ref = new SBuffer(_idName, new SPosition(this));
						nextSymbol();
					}
					continue;
				case FINALLY_SYM:
					if (sc.getFinallyCode() != -1) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					sc.setFinallyCode(compileSection(CompileBase.ELEM_MODE,
						XD_VOID, sym));
					continue;
				default:
			}
			if (sym == NOCHAR) {
				break;
			}
			errorAndSkip(XDEF.XDEF425, SCRIPT_SEPARATORS); //Script error
		}
		sc.setOccurrence(occ);
		if (_sym != NOCHAR) {
			error(XDEF.XDEF425); //Script error
		}
		return ref;
	}

	/** Report deprecated symbol.
	 * @param symbol deprecated symbol.
	 * @param recommended what should be done.
	 */
	private void reportDeprecated(final String symbol,final String recommended){
		_g.reportDeprecated(symbol, recommended);
	}

	/** Read list of options.
	 * @param result The object with script code.
	 */
	private void readOptions(final XCodeDescriptor result) {
		if (_options) {
			error(XDEF.XDEF430); //'options' was already specified
		}
		_options = true;
		if (_sym != IDENTIFIER_SYM && _sym != FORGET_SYM) {
			error(XDEF.XDEF431); //Option identifier expected
			return;
		}
		short kind = result.getKind();
		//XMNode.XMDEFINITION, XMELEMENT, XMATTRIBUTE, XMTEXT ...
		boolean forget = false;
		boolean clearAdoptedForgets = false;
		boolean attrWhiteSpaces = false;
		boolean textWhiteSpaces = false;
		boolean ignoreComments = false;
		boolean ignoreEmptyAttributes = false;
		boolean setAttrValuesCase = false;
		boolean setTextValuesCase = false;
		boolean trimAttr = false;
		boolean trimText = false;
		boolean moreAttributes = false;
		boolean moreElements = false;
		boolean moreText = false;
		boolean ignoreEntities = false;
		boolean acceptQualifiedAttr = false;
		boolean nillable = false;
		boolean cdata = false;
		while (_sym == IDENTIFIER_SYM || _sym == FORGET_SYM) {
			if (_sym == FORGET_SYM) {
				if (kind != XMELEMENT) {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, symToName(FORGET_SYM));
				} else {
					reportDeprecated("option forget", "forget (action)");
					if (forget) {
						error(XDEF.XDEF432,_idName);//Option &{0} redefinition
					}
					((XElement) result)._forget = 'T';
				}
			} else if ("notForget".equals(_idName)) {
				if (forget) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				if (kind != XMELEMENT) {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, "notForget");
				} else {
					reportDeprecated("option notForget", "forget (action)");
					if (forget) {
						error(XDEF.XDEF432,_idName);//Option &{0} redefinition
					}
				}
			} else if ("clearAdoptedForgets".equals(_idName)) {
				if (kind != XMELEMENT) {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, "clearAdoptedForgets");
				} else {
					if (clearAdoptedForgets) {
						error(XDEF.XDEF422); //Duplicated script section
					}
					((XElement)result)._clearAdoptedForgets = 'T';
				}
				clearAdoptedForgets = true;
			} else if ("copyAttrWhiteSpaces".equals(_idName)
				|| "preserveAttrWhiteSpaces".equals(_idName)
				|| "ignoreAttrWhiteSpaces".equals(_idName)) {
				if("copyAttrWhiteSpaces".equals(_idName)) {
					reportDeprecated(_idName, "preserveAttrWhiteSpaces");
				}
				if (attrWhiteSpaces) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				attrWhiteSpaces = true;
				result._attrWhiteSpaces = (byte)
					("ignoreAttrWhiteSpaces".equals(_idName) ? 'T' : 'F');
			} else if ("copyTextWhiteSpaces".equals(_idName)
				|| "preserveTextWhiteSpaces".equals(_idName)
				|| "ignoreTextWhiteSpaces".equals(_idName)) {
				if("copyTextWhiteSpaces".equals(_idName)) {
					reportDeprecated(_idName, "preserveTextWhiteSpaces");
				}
				if (textWhiteSpaces) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				textWhiteSpaces = true;
				result._textWhiteSpaces = (byte)
					("ignoreTextWhiteSpaces".equals(_idName) ? 'T' : 'F');
			} else if ("ignoreComments".equals(_idName)) {
				if (ignoreComments) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				ignoreComments = true;
				result._ignoreComments = 'T';
			} else if ("copyComments".equals(_idName)
				|| "preserveComments".equals(_idName)) {
				if ("copyComments".equals(_idName)) {
					reportDeprecated(_idName,"preserveComments");
				}
				if (ignoreComments) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				ignoreComments = true;
				result._ignoreComments = 'F';
			} else if ("copyEmptyAttributes".equals(_idName)
				|| "acceptEmptyAttributes".equals(_idName)
				|| "preserveEmptyAttributes".equals(_idName)
				|| "ignoreEmptyAttributes".equals(_idName)) {
				if("copyEmptyAttributes".equals(_idName)) {
					reportDeprecated(_idName, "acceptEmptyAttributes");
				}
				if (ignoreEmptyAttributes) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				ignoreEmptyAttributes = true;
				result._ignoreEmptyAttributes = (byte)
					 ("ignoreEmptyAttributes".equals(_idName) ? 'T' :
					"acceptEmptyAttributes".equals(_idName) ? 'A' :
					"preserveEmptyAttributes".equals(_idName) ? 'P' :
					'F');
			} else if ("setAttrUpperCase".equals(_idName)
				|| "setAttrLowerCase".equals(_idName)
				|| "noSetAttrCase".equals(_idName)
				|| "preserveAttrCase".equals(_idName)) {
				if (setAttrValuesCase) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				setAttrValuesCase = true;
				result._attrValuesCase = (byte)
					("setAttrUpperCase".equals(_idName) ? 'T' :
					("noSetAttrCase".equals(_idName)
					|| "preserveAttrCase".equals(_idName)) ? 'I' : 'F');
			} else if ("setTextUpperCase".equals(_idName)
				|| "setTextLowerCase".equals(_idName)
				|| "noSetTextCase".equals(_idName)
				|| "preserveTextCase".equals(_idName)) {
				if (setTextValuesCase) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				setTextValuesCase = true;
				result._textValuesCase = (byte)
					("setTextUpperCase".equals(_idName) ? 'T' :
					("notSetTextCase".equals(_idName)
					|| "preserveTextCase".equals(_idName)) ? 'I' : 'F');
			} else if ("trimAttr".equals(_idName)
				|| "noTrimAttr".equals(_idName)) {
				if (trimAttr) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				trimAttr = true;
				result._trimAttr =
					(byte) ("trimAttr".equals(_idName) ? 'T' : 'F');
			} else if ("trimText".equals(_idName)
				|| "noTrimText".equals(_idName)) {
				if (trimText) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				trimText = true;
				result._trimText =
					(byte) ("trimText".equals(_idName) ? 'T' : 'F');
			} else if ("moreAttributes".equals(_idName)) {
				if (moreAttributes) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				moreAttributes = true;
				result._moreAttributes = 'T';
			} else if ("moreElements".equals(_idName)) {
				if (moreElements) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				moreElements = true;
				result._moreElements = 'T';
			} else if ("moreText".equals(_idName)) {
				if (moreText) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				moreText = true;
				result._moreText = 'T';
			} else if ("acceptOther".equals(_idName)
				|| "ignoreOther".equals(_idName)) {
				if (moreAttributes | moreElements | moreText) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				moreAttributes = moreElements = moreText = true;
				byte b = "acceptOther".equals(_idName) ? (byte)'T' :(byte) 'I';
				result._moreAttributes=result._moreElements=result._moreText= b;
			} else if ("nillable".equals(_idName)
				|| "noNillable".equals(_idName)) {
				if (nillable) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				} else if (kind != XMELEMENT) {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, _idName);
				}
				nillable = true;
				result._nillable =
					(byte) ("nillable".equals(_idName) ? 'T' : 'F');
			} else if ("ignoreEntities".equals(_idName)
				|| "resolveEntities".equals(_idName)) {
				if (ignoreEntities) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				ignoreEntities = true;
				result._resolveEntities =
					(byte) ("resolveEntities".equals(_idName) ? 'T' : 'F');
			} else if ("acceptQualifiedAttr".equals(_idName)
				|| "notAacceptQualifiedAttr".equals(_idName)) {
				if (acceptQualifiedAttr) {
					error(XDEF.XDEF432,_idName);//Option &{0} redefinition
				}
				acceptQualifiedAttr = true;
				result._acceptQualifiedAttr =
					(byte) ("acceptQualifiedAttr".equals(_idName) ? 'T' : 'F');
			} else if ("cdata".equals(_idName)) {
				if (cdata) {
					error(XDEF.XDEF432); // option redefinition
				}
				cdata = true;
				if (kind == XMTEXT || kind == XMATTRIBUTE
					&& (result.getName().equals("$text")
					|| result.getName().equals("$textcontent"))) {
					result._cdata = 'T';
					if ("textcontent".equals(result.getName())
						&& result.maxOccurs() > 1) {
						if (_g._chkWarnings) {
							//Maximum occurrence of item "&{0}" can not be
							// higher then 1
							error(XDEF.XDEF535, "textcontent");
						}
						result.setOccurrence(result.minOccurs(), 1);
					}
				} else {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, _idName);
				}
			} else if ("clearReports".equals(_idName)
				|| "preserveReports".equals(_idName)) {
				if (kind != XMELEMENT) {
					//The token '&{0}' is not allowed here
					error(XDEF.XDEF411, _idName);
				} else {
					XElement xel = (XElement) result;
					if (xel._clearReports != 0) {
						error(XDEF.XDEF432,_idName);//Option &{0} redefinition
					} else {
						xel._clearReports =
							"clearReports".equals(_idName) ?(byte)'T':(byte)'F';
					}
				}
			} else {
				error(XDEF.XDEF433, _idName); //Unknown option '&{0}'
			}
			if (nextSymbol() != COMMA_SYM) {
				break;
			}
			nextSymbol();
		}
		checkSemicolon(SCRIPT_SEPARATORS);
	}
}
