package org.xdef.impl.compile;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import org.xdef.xon.IniReader;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_ITEM;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonParsers;
import org.xdef.xon.XonReader;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonTools.JAny;
import org.xdef.xon.XonTools.JArray;
import org.xdef.xon.XonTools.JMap;
import org.xdef.xon.XonTools.JObject;
import org.xdef.xon.XonTools.JValue;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;

/** Create X-definition model from xd:xon element.
 * @author Vaclav Trojan
 */
//public final class CompileXonXdef extends StringParser {
public final class CompileXonXdef extends XScriptParser {

//	/** XPosition of %any model.*/
//	private final byte _xonMode;
	/** Prefix of X-definition namespace. */
	private final String _xdPrefix;
	/** Index of X-definition namespace. */
	private final int _xdIndex;
	/** Namespace of X-definition.*/
	private final String _xdNamespace;
	/** XPath position of XON/JSON description.*/
	private final String _basePos;
	/** PNode with generated model.*/
	private final PNode _xonModel;
	/** X-position of generated %any model.*/
	private String _anyXPos;
/*#if DEBUG*#/
	/** debugging switches; from properties. *#/
	private final String _dbgSwitches; // remove this code in future
/*#end*/

	/** Prepare instance of CompileXonXdef. */
	CompileXonXdef(PNode pn,
		final SBuffer name,
		final ReportWriter reporter) {
		super(StringParser.XMLVER1_0);
		_xdNamespace = pn._nsURI;
		_xdPrefix = pn.getPrefix();
		_xdIndex = pn._nsPrefixes.get(_xdPrefix);
		_basePos = pn._xpathPos + "/text()";
		setReportWriter(reporter);
		_xonModel = pn;
		_anyXPos = null;
/*#if DEBUG*#/
		// read swithes from properties. Remove this code in future.
		String s = System.getProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES);
		_dbgSwitches = s == null ? "" : s.trim();
/*#end*/
	}

	/** Set attribute to PNode.
	 * @param pn PNode where to set an attribute.
	 * @param name name of attribute.
	 * @param val SBuffer with the value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setAttr(final PNode pn,
		final String name,
		final SBuffer val) {
		PAttr pa = new PAttr(name, val, null, -1);
		pa._localName = name;
		pn.setAttr(pa);
		pa._xpathPos = _basePos;
		return pa;
	}

	/** Get X-def attribute.
	 * @param pn PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return PAttr or null.
	 */
	private PAttr getXDAttr(final PNode pn, final String name) {
		return pn.getAttrNS(name, pn._nsPrefixes.get(_xdPrefix));
	}

	/** Set X-def attribute.
	 * @param pn PNode where to set attribute.
	 * @param name local name of attribute.
	 * @param val SBuffer with value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setXDAttr(final PNode pn,
		final String name,
		final SBuffer val) {
		int nsindex;
		if (pn._nsPrefixes.containsKey(_xdPrefix)) {
			nsindex = pn._nsPrefixes.get(_xdPrefix);
		} else {
			nsindex = pn._nsPrefixes.size();
			pn._nsPrefixes.put(_xdPrefix, nsindex);
		}
		PAttr pa = new PAttr(_xdPrefix+":"+name, val, _xdNamespace, nsindex);
		pa._localName = name;
		pn.removeAttr(pa._name);
		pn.setAttr(pa);
		pa._xpathPos = _basePos;
		return pa;
	}

	/** Skip white space separators and comments. Note: line comments are not
	 * allowed in X-script.
	 * @return true if a space or comment was found.
	 */
	public final boolean isSpacesOrComments() {
		boolean result = isSpaces();
		while(isToken("/*") ) {
			result = true;
			if (!findTokenAndSkip("*/")) {
				error(JSON.JSON015); //Unclosed comment
				setEos();
				return result;
			}
			isSpaces();
		}
		return result;
	}

	/** Skip all blanks, comments and semicolons.
	 * @return true if a semicolon was found.
	 */
	private boolean skipSemiconsBlanksAndComments() {
		boolean result = false;
		for(;;) {
			isSpacesOrComments();
			if (eos() || !isChar(';')) {
				break;
			}
			result = true;
		}
		return result;
	}

	/** Read occurrence.
	 * Occurrence ::= ("required" | "optional" | "ignore" | "illegal" | "*"
	 *   | "+" | "?" | (("occurs" S)? ("*" | "+" | "?"
	 *   | (IntegerLiteral (S? ".." (S? ("*" | IntegerLiteral))? )? ))))
	 * @return Occurrence object or null.
	 */
	private XOccurrence readOccurrence() {
		boolean wasOccurs;
		if (wasOccurs = isToken("occurs")) {
			if (!isSpacesOrComments()) {}
		}
		final String[] tokens =
			{"optional", "?", "*", "+", "required", "ignore", "illegal"};
		switch (isOneOfTokens(tokens)) {
			case  0:
			case  1:
				return new XOccurrence(0, 1); // optional
			case  2:
				return new XOccurrence(0, Integer.MAX_VALUE); // unbounded
			case  3:
				return new XOccurrence(1, Integer.MAX_VALUE); // one or more
			case  4:
				return new XOccurrence(1, 1); // required
			case  5:
				return new XOccurrence(
					XOccurrence.IGNORE, Integer.MAX_VALUE); // ignore
			case  6:
				return new XOccurrence(XOccurrence.ILLEGAL, 0); // illegal
		}
		if (isInteger()) {
			int min = getParsedInt(), max = Integer.MAX_VALUE;
			isSpacesOrComments();
			if (isToken("..")) {
				isSpacesOrComments();
				if (isInteger()) {
					max = getParsedInt();
				} else {
					isChar('*');
				}
			}
			return new XOccurrence(min, max);
		} else {
			if (wasOccurs) {
				error(XDEF.XDEF429);//After 'occurs' is expected the interval
			}
			return null;
		}
	}

	/** Parse X-script and return occurrence and executive part
	 * (type declaration) in separate fields.
	 * @param sbuf JValue from which is used the value
	 * @return array with SBuffer items from both parts.
	 */
	private SBuffer[] parseTypeDeclaration(final SBuffer sbuf) {
		SBuffer[] result = new SBuffer[] {
			new SBuffer("",getPosition()),new SBuffer("",getPosition()),null};
		if (sbuf == null) {
			return result;
		}
		setSourceBuffer(sbuf);
//		nextSymbol();
//		XOccurrence occ = new XOccurrence(); // undefined occurrence
//		while (_sym != NOCHAR) {
//			if (_sym == SEMICOLON_SYM) {
//				nextSymbol();
//				continue;
//			}
//			if (isOccurrence(occ)) {
//				compileTypeCheck(sc);
//				continue;
//			} else if (_sym == IDENTIFIER_SYM || _sym == LPAR_SYM
//				|| _sym == NOT_SYM || (_sym == CONSTANT_SYM
//				&& (_parsedValue.getItemId() == XD_STRING
//				|| _parsedValue.getItemId() == XD_BOOLEAN))) {
//				if (!occ.isSpecified()) {
//					occ.setRequired();
//				}
//				compileTypeCheck(sc);
//				continue;
//			}
//			char sym = _sym;
//			nextSymbol();
//			switch (sym) {
//				case OPTION_SYM:
//				case OPTIONS_SYM:
//					readOptions(sc);
//					continue;
//				case FIXED_SYM: {
//					if (sc._check < 0 && occ.isSpecified()) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					occ.setFixed();
//					int addr = compileFixedMethod(SCRIPT_SEPARATORS);
//					_g._sp  = -1;
//					if (addr >= 0) {
//						int check = sc._check;
//						if (check >= 0
//							&&_g._code.get(_g._lastCodeIndex).getCode()==STOP_OP
//							&&_g._code.get(check).getCode() == 0
//							&& _g._code.get(check).getItemId() == XD_PARSER) {
//							if (addr + 3 == _g._lastCodeIndex
//								&&_g._code.get(addr).getCode()==INIT_NOPARAMS_OP
//								&&_g._code.get(addr).getParam() == 0
//								&&_g._code.get(addr + 1).getCode() == 0
//								&&_g._code.get(addr+1).getItemId() == XD_STRING
//								&& _g._code.get(addr + 2).getCode() == RETV_OP
//								&& _g._code.get(addr + 3).getCode() == STOP_OP){
//								XDValue value = _g._code.get(addr + 1);
//								XDParser p = (XDParser) _g._code.get(check);
//								XDParseResult r =
//									p.check(null,value.toString());
//								if (r.errors()) {
//									error(XDEF.XDEF481); //Incorrect fixed value
//								}
//							} else {
//								sc.setValueType(XD_STRING, "string");
//							}
//						}
//						sc._check = _g._lastCodeIndex + 1;
//						_g.addCode(new CodeI1(XD_STRING, CALL_OP, addr), 1);
//						_g.internalMethod("eq",1);
//						_g.addCode(new CodeI1(XD_PARSERESULT,PARSE_OP,1),0);
//						_g.genStop();
//						_g._sp  = -1;
//						sc._onFalse = _g._lastCodeIndex + 1;
//						_g.genLDC(new DefString("XDEF515")); //Value error
//						_g.genLDC(new DefString("Value error"));
//						_g.internalMethod("error", 2);
//						// let's continue with setting od _g.genStop();
//						//continues with setText, which is also onAbsence
//						_g._sp  = -1;
//						sc._onAbsence = _g._lastCodeIndex + 1;
//						_g.addCode(new CodeI1(XD_STRING, CALL_OP, addr), 1);
//						_g.internalMethod("setText",1);
//						_g.genStop();
//						_g._sp  = -1;
//					}
//					continue;
//				}
//				case ON_TRUE_SYM:
//					if (sc._onTrue != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._onTrue =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case ON_FALSE_SYM:
//					if (sc._onFalse != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._onFalse =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case ON_ABSENCE_SYM:
//					if (sc._onAbsence != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._onAbsence =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case ON_ILLEGAL_ATTR_SYM:
//					if (sc._onIllegalAttr != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._onIllegalAttr =
//						compileSection(CompileBase.ELEMENT_MODE,XD_VOID, sym);
//					continue;
//				case CREATE_SYM:
//					if (sc._compose != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._compose =
//						compileSection(CompileBase.TEXT_MODE, XD_STRING, sym);
//					continue;
//				case ON_START_ELEMENT_SYM:
//					if (sc._onStartElement != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._onStartElement =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case INIT_SYM:
//					if (sc._init != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._init =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case DEFAULT_SYM:
//					if (sc._deflt != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					occ.setOptional();
//					sc._deflt =
//						compileSection(CompileBase.TEXT_MODE, XD_STRING, sym);
//					continue;
//				case FINALLY_SYM:
//					if (sc._finaly != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._finaly =
//						compileSection(CompileBase.TEXT_MODE, XD_VOID, sym);
//					continue;
//				case MATCH_SYM:
//					if (sc._match != -1) {
//						error(XDEF.XDEF422); //Duplicated script section
//					}
//					sc._match =
//						compileSection(CompileBase.TEXT_MODE,XD_BOOLEAN, sym);
//					continue;
//				case ON_EXCESS_SYM: //only for xd:attrs or text
//					if (sc.getName().startsWith("$")) {
//						if (sc._onExcess != -1) {
//							error(XDEF.XDEF422); //Duplicated script section
//						}
//						sc._onExcess = compileSection(CompileBase.TEXT_MODE,
//							XD_VOID, sym);
//						continue;
//					}
//					errToken(sym);
//					break;
//				default:
//			}
//			if (sym == NOCHAR) {
//				break;
//			}
		skipSemiconsBlanksAndComments();
		int pos = getIndex();
		SPosition spos = getPosition();
		XOccurrence occ = readOccurrence();
		if (occ != null) {
			result[0] = new SBuffer(getParsedBufferPartFrom(pos), spos);
			result[2] = new SBuffer(String.valueOf(occ.maxOccurs()));
		}
		skipSemiconsBlanksAndComments();
		if (!eos()) {
			result[1] = new SBuffer(getUnparsedBufferPart(), getPosition());
		}
		return result;
	}

	/** Create PNode.
	 * @param parent parent node.
	 * @param nsURI namespace URI.
	 * @param name qualified name of PNode.
	 * @param spos source position.
	 * @return created PNode.
	 */
	private PNode genPElement(final PNode parent,
		final String nsURI,
		final String name,
		final SPosition spos) {
		PNode result = new PNode(
			name, spos, parent, parent._xdVersion, parent._xmlVersion);
		int nsindex;
		String localName;
		if (nsURI != null) {
			int ndx = name.indexOf(':');
			String prefix = ndx >= 0 ? name.substring(0, ndx) : "";
			localName = ndx >= 0 ? name.substring(ndx + 1) : name;
			if (result._nsPrefixes.containsKey(prefix)) {
				nsindex = result._nsPrefixes.get(prefix);
			} else {
				nsindex = parent._nsPrefixes.size();// add namespace to the list
				result._nsPrefixes.put(_xdPrefix, nsindex);
			}
		} else {
			nsindex = -1; // no namespace
			localName = name;
		}
		result._nsindex = nsindex;
		result._nsURI = nsURI;
		result._localName = localName;
		result._xpathPos = _basePos;
		return result;
	}

	/** Create PNode as XON/JSON element with given position,
	 * @param parent parent PNode.
	 * @param name local name of XON/JSON element.
	 * @param spos source position
	 * @return created PNode,
	 */
	private PNode genJElement(final PNode parent,
		final String name,
		final SPosition spos) {
		return genPElement(parent,
			XDConstants.XON_NS_URI_W,
			XDConstants.XON_NS_PREFIX + ":" + name,
			spos);
	}

	/** Create PNode as XDef element with given position,
	 * @param parent parent PNode.
	 * @param name local name of XDef element.
	 * @param spos source position
	 * @return created PNode,
	 */
	private PNode genXDElement(final PNode parent,
		final String name,
		final SPosition spos) {
		return genPElement(parent, _xdNamespace, _xdPrefix + ":" + name, spos);
	}

////////////////////////////////////////////////////////////////////////////////
// Create X-definition model from xd:xon (use W3C format)
////////////////////////////////////////////////////////////////////////////////

	/** Add match section to xd:script attribute. If match section already
	 * in this attribute exists then add the argument to the expression with
	 * the operator "AAND".
	 * @param pn PNode where to set or update the xd:script attribute.
	 * @param matchexpr the match expression.
	 */
	private void addMatchExpression(final PNode pn, final String matchexpr) {
		PAttr attr = getXDAttr(pn, "script");
		SBuffer val;
		if (attr != null) {
			val = attr._value;
			String s = val.getString().trim();
			int ndx;
			if ((ndx = s.indexOf("match ")) < 0) {
				if (!s.isEmpty() && !s.endsWith(";")) {
					s += ';';
				}
				s += "match " + matchexpr + ';';
			} else {
				s = s.substring(0, ndx + 6) + matchexpr
					+ " AAND " + s.substring(ndx + 6);
			}
			val = new SBuffer(s, val);
		} else {
			val = new SBuffer("match " + matchexpr + ';', pn._name);
		}
		setXDAttr(pn, "script", val);
	}

	/** Update key information to xd:script attribute.
	 * @param pn PNode where to update.
	 * @param key value of key.
	 */
	private void updateKeyInfo(final PNode pn, final String key) {
		if (ANY_NAME.equals(key)) {
			setAttr(pn,X_KEYATTR, new SBuffer("string(0,*);", pn._name));
		} else { // ANY name
			addMatchExpression(pn, '@' + X_KEYATTR + "=='"+ key +"'");
			setAttr(pn, X_KEYATTR, new SBuffer("fixed('" + key + "');",pn._name));
		}
	}

	private PNode genXonValue(final String name,
		final JValue jo,
		final PNode parent) {
		SBuffer sbf, occ = null;
		PNode pn = genJElement(parent, X_ITEM, jo.getPosition());
		if (jo.getValue() == null) {
			sbf = new SBuffer("jnull()");
		} else {
			if (jo.toString().trim().isEmpty()) {
				sbf = new SBuffer("jvalue()", jo.getPosition());
				occ = new SBuffer("?", jo.getPosition());
			} else {
				SBuffer[] parsedScript = parseTypeDeclaration(jo.getSBuffer());
				if (!parsedScript[0].getString().isEmpty()) { // occurrence
					occ = parsedScript[0];
					if (!ANY_NAME.equals(name)
						&& !X_ARRAY.equals(parent.getLocalName())
						&& !"1".equals(parsedScript[2].getString())) {
						//Occurrence maximum of attribute or text value can't
						// be higher then one
						error(XDEF.XDEF262);
					}
				}
				if (eos()) {
					parsedScript[1] = new SBuffer("jvalue()", jo.getPosition());
				}
				sbf = parsedScript[1];
			}
			if (occ != null) { // occurrence
				setXDAttr(pn, "script", occ);
			}
			setAttr(pn, X_VALATTR, sbf);
		}
		if (name != null) {
			if (pn._nsPrefixes.containsKey(_xdPrefix)
				&& "choice".equals(pn.getLocalName())) {
				for (PNode p: pn.getChildNodes()) {
					setName(p, name);
				}
			} else {
				setName(pn, name);
			}
		}
		return pn;
	}

	private void setName(final PNode pn, final String name) {
		if (ANY_NAME.equals(name)) {
			setAttr(pn,X_KEYATTR, new SBuffer("string();", pn._name));
		} else {
			addMatchExpression(pn, '@' + X_KEYATTR + "=='"+ name +"'");
			setAttr(pn,X_KEYATTR,new SBuffer("fixed('"+name+ "');",pn._name));
		}
	}

	private PNode genXonMap(final JMap map, final PNode parent) {
		PNode pn1, pn2;
		Object val = map.get(SCRIPT_DIRECTIVE);
		if (val != null && val instanceof JValue) {
			map.remove(SCRIPT_DIRECTIVE);
			JValue jv = (JValue) val;
			setSourceBuffer(jv.getSBuffer());
			isSpacesOrComments();
			if (isToken(ONEOF_DIRECTIVE)) {
				pn1 = genJElement(parent, X_MAP, map.getPosition());
				pn2 = genXDElement(pn1, "choice", getPosition());
				pn1.addChildNode(pn2);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(pn2, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
					XOccurrence x = readOccurrence();
					if (x != null && x.maxOccurs() > 1) {
						//Specification of occurence of &{0} group
						// can not be higher then 1
						error(XDEF.XDEF252, ONEOF_DIRECTIVE);
					}
				}
			} else if (map.size() > 1) {
				pn1 = genJElement(parent, X_MAP, map.getPosition());
				pn2 = genXDElement(pn1, "mixed", map.getPosition());
				pn1.addChildNode(pn2);
				if (!eos()) {
					setXDAttr(pn1, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else {
				pn1 = genJElement(parent, X_MAP, map.getPosition());
				pn2 = pn1;
				if (!eos()) {
					setXDAttr(pn1, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			}
		} else if (map.size() > 1) {
			pn1 = genJElement(parent, X_MAP, map.getPosition());
			pn2 = genXDElement(pn1, "mixed", map.getPosition());
			pn1.addChildNode(pn2);
		} else {
			pn1 = genJElement(parent, X_MAP, map.getPosition());
			pn2 = pn1;
		}
		Object anyItem = null;
		for (Map.Entry<Object, Object> entry: map.entrySet()) {
			String key = (String) entry.getKey();
			Object o = entry.getValue();
			if (key == null || ANY_NAME.equals(key)) {
				anyItem = o;
			} else {
				String keyXmlName = XonTools.toXmlName(key);
				if (o != null && (o instanceof Map || o instanceof List)) {
					PNode pn3 = genXonModel(o, pn2);
					if (_xdNamespace.equals(pn3._nsURI)
						&& "choice".equals(pn3._localName)) {
						for (PNode pn : pn3.getChildNodes()) {
							updateKeyInfo(pn, keyXmlName);
						}
					} else {
						updateKeyInfo(pn3, keyXmlName);
					}
				} else {
					pn2.addChildNode(genXonValue(keyXmlName, (JValue) o, pn2));
				}
			}
		}
		if (anyItem != null) {
			if (anyItem instanceof Map || anyItem instanceof List) {
				PNode pn3 = genXonModel(anyItem, pn2);
				if (_xdNamespace.equals(pn3._nsURI)
					&& "choice".equals(pn3._localName)) {
					for (PNode pn : pn3.getChildNodes()) {
						updateKeyInfo(pn, ANY_NAME);
					}
				} else {
					updateKeyInfo(pn3, ANY_NAME);
				}
				setAnyOccurrence(pn3);
			} else {
				if (anyItem instanceof JAny) {
					genXonAny((JAny) anyItem, pn2);
				} else  {
					PNode pn3 = genXonValue(ANY_NAME, (JValue) anyItem,pn2);
					setAttr(pn3,
						X_KEYATTR, new SBuffer("string()", getPosition()));
					pn2.addChildNode(pn3);
				}
			}
		}
		return pn1;
	}

	private void setAnyOccurrence(final PNode pn) {
		PAttr patt = getXDAttr(pn, "script");
		SBuffer val;
		if (patt != null) {
			SBuffer[] sbx = parseTypeDeclaration(patt.getValue());
			String s = sbx[1] == null ? "" : "+;" + sbx[1].getString();
			val = new SBuffer(s, patt._value);
		} else {
			val = new SBuffer("*");
		}
		setXDAttr(pn, "script", val);
	}

	private PNode genXonArray(final JArray array, final PNode parent) {
		PNode pn = genJElement(parent, X_ARRAY, array.getPosition());
		int index = 0;
		int len = array.size();
		if (len > 0) {
			Object jo = array.get(0);
			Object o = jo == null
				? null : jo instanceof JValue ? ((JValue) jo).getValue() : jo;
			if (o != null && o instanceof JValue) {
				setSourceBuffer(((JValue) o).getSBuffer());
				isSpacesOrComments();
				if (isToken(ONEOF_DIRECTIVE)) {
					String s = getUnparsedBufferPart().trim();
					pn = genXDElement(
						parent, "choice", ((JValue) jo).getPosition());
					skipSemiconsBlanksAndComments();
					if (!s.isEmpty()) {
						int ndx = s.indexOf("ref ");
						if (ndx >= 0) {
							setXDAttr(pn,"script",new SBuffer(s,getPosition()));
							s = s.substring(0, ndx).trim();
						}
					}
					if (!s.isEmpty()) {
						if (!s.endsWith(";")) {
							s += ";";
						}
						setSourceBuffer(new SBuffer(s));
						XOccurrence x = readOccurrence();
						if (x != null && x.minOccurs() == 0
							&& X_MAP.equals(parent.getLocalName())) {
							setXDAttr(pn,
								"script",new SBuffer("?", getPosition()));
						}
						if (!s.isEmpty()) {
							setXDAttr(parent,
								"script", new SBuffer(s,getPosition()));
						}
					}
				} else {
					String s = getUnparsedBufferPart().trim();
					if (!s.isEmpty()) {
						setXDAttr(pn, "script",
							new SBuffer(s + ';', ((JValue) jo).getSBuffer()));
					}
				}
				index = 1;
			}
			for(; index < len; index++) {
				PNode pn1 = genXonModel(array.get(index), pn);
				PAttr val;
				// if it is not the last and it has xd:script attribute where
				// the min occurrence differs from max occurrence
				// and it has the attribute with a value description
				if (X_ITEM.equals(pn1._localName)
					&& XDConstants.XON_NS_URI_W.equals(pn1._nsURI)
					&& (val = pn1.getAttrNS(X_VALATTR, -1)) != null) {
					PAttr script = getXDAttr(pn1, "script");
					XOccurrence occ = null;
					if (script != null) {
						SBuffer[] sbs = parseTypeDeclaration(script.getValue());
						setSourceBuffer(sbs[0]);
						occ = readOccurrence();
					}
					if (index < len-1 && pn.getNSIndex() == _xdIndex //xdef
						&& ("mixed".equals(pn.getLocalName()) // mixed or choice
							|| "choice".equals(pn.getLocalName()))
						|| occ != null && occ.minOccurs() != occ.maxOccurs()) {
						SBuffer[] sbs = parseTypeDeclaration(val.getValue());
						String s = sbs[1].getString();
						int i;
						// remove comments!
						while ((i = s.indexOf("/*")) >= 0) {
							int j = s.indexOf("*/", i);
							if (j > i) {
								s = s.substring(0, i) + s.substring(j+2) + ' ';
							}
						}
						if ((i = s.indexOf(';')) > 0) { // remove ";" at end
							s = s.substring(0, i);
						}
						s = s.trim();
						if (s.isEmpty()) { //type not specified
							s = "jvalue()";
						} else if (!s.endsWith(")")) {
							s += "()"; // add brackets
						}
						addMatchExpression(pn1,
							s + ".parse((String)@" + X_VALATTR + ").matches()");
					}
				}
			}
		}
		return pn;
	}

	/** Create PNode with XON/JSON model from XON/JSON parsed data.
	 * @param xon XON/JSON parsed data.
	 * @param parent parent PNode,
	 * @return created PNode.
	 */
	private PNode genXonModel(final Object xon, final PNode parent) {
		// set fields _jsprefix and _jsNamespace
		String s = XDConstants.XON_NS_PREFIX; // default namespace prefix
		for (int i = 1; ;i++) {
			Integer x;
			if ((x = parent._nsPrefixes.get(s)) == null) {
				parent._nsPrefixes.put(s, XPreCompiler.NS_XON_INDEX);
				break;
			} else if (x.equals(XPreCompiler.NS_XON_INDEX)) {
				break; // prefix is already set
			} else { // the prefix is already used
				s = XDConstants.XON_NS_PREFIX + i; // change prefix
			}
		}
		PNode pn;
		if (xon instanceof JMap) {
			pn = genXonMap((JMap) xon, parent);
		} else if (xon instanceof JArray) {
			pn = genXonArray((JArray) xon, parent);
		} else if (xon instanceof JValue
			&& ((JValue) xon).getValue() instanceof String) {
			pn = genXonValue(null, (JValue) xon, parent);
		} else if (xon instanceof JAny) {
			genXonAny((JAny) xon, parent);
			return parent;
		} else {
			error(JSON.JSON011); //Not XON/JSON object&{0}
			return parent;
		}
		parent.addChildNode(pn);
		return pn;
	}

	/** Create PNode for %any.
	 * @param xon XON/JSON parsed data.
	 * @param parent parent PNode,
	 */
	private void genXonAny(final JAny jo, final PNode parent) {
		if (_anyXPos == null) {
			_anyXPos = _xonModel._parent._xdef.getName()
				+ "#" + XConstants.JSON_ANYOBJECT;
		}
		PNode pn, pn1, pn2;
		pn = genXDElement(parent, "choice", parent.getName());
		pn._xonMode = XConstants.XON_MODE_W;
		parent.addChildNode(pn);
		SBuffer val = jo != null && jo.getSBuffer() != null
			? jo.getSBuffer() : new SBuffer("", parent._name);
		String s = val.getString().trim();
		val = s.isEmpty() ? null : new SBuffer(s, parent.getName());
		if (val != null) {
			setXDAttr(pn, "script", val);
		}
		SPosition spos = parent._name;
		pn1 = genJElement(pn, X_ITEM, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		boolean isMap = X_MAP.equals(parent._localName)
			&& XDConstants.XON_NS_URI_W.equals(parent._nsURI);
		if (isMap) {
			setAttr(pn1, X_KEYATTR, new SBuffer("string();", spos));
		}
		setAttr(pn1, X_VALATTR, new SBuffer("jvalue();", spos));
		pn.addChildNode(pn1);
		pn1 = genJElement(pn, X_ARRAY, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		if (isMap) {
			setAttr(pn1, X_KEYATTR, new SBuffer("string();", spos));
		}
		pn2 = genXDElement(pn1, "choice", spos);
		pn2._xonMode = XConstants.XON_MODE_W;
		setXDAttr(pn2, "script", new SBuffer("*; ref " +_anyXPos, spos));
		pn1.addChildNode(pn2);
		pn.addChildNode(pn1);
		pn1 = genJElement(pn, X_MAP, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		if (isMap) {
			setAttr(pn1, X_KEYATTR, new SBuffer("string();", spos));
		}
		pn2 = genXDElement(pn1, "choice", spos);
		pn2._xonMode = XConstants.XON_MODE_W;
		setXDAttr(pn2, "script", new SBuffer("*; ref " +_anyXPos, spos));
		pn1.addChildNode(pn2);
		pn.addChildNode(pn1);
	}

	/** Generate models for %anyObj.
	 * @param actNode actual PNode.
	 * @param anyName name used for models of %anyObj.
	 */
	void genXonAnyModels(final PNode actNode, final String anyName) {
		SPosition spos = actNode._name; // just to get position
		PNode pn, pn1, pn2;
		pn = genXDElement(actNode._parent, "choice", spos);
		pn._nsPrefixes.put(XDConstants.XON_NS_PREFIX,XPreCompiler.NS_XON_INDEX);
		pn._xonMode = XConstants.XON_MODE_W;
		setXDAttr(pn, "name", new SBuffer(anyName, spos));
		actNode._parent.addChildNode(pn);
		pn1 = genJElement(pn, X_ITEM, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		setAttr(pn1, X_KEYATTR, new SBuffer("? string();", spos));
		setAttr(pn1, X_VALATTR, new SBuffer("jvalue();", spos));
		pn.addChildNode(pn1);
		pn1 = genJElement(pn, X_ARRAY, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		setAttr(pn1, X_KEYATTR, new SBuffer("? string();",spos));
		pn2 = genXDElement(pn1, "choice", spos);
		setXDAttr(pn2, "script", new SBuffer("*; ref " + anyName, spos));
		pn1.addChildNode(pn2);
		pn.addChildNode(pn1);
		pn1 = genJElement(pn, X_MAP, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		setAttr(pn1, X_KEYATTR, new SBuffer("? string();", spos));
		pn2 = genXDElement(pn1, "choice", spos);
		setXDAttr(pn2, "script", new SBuffer("*; ref " + anyName, spos));
		pn1.addChildNode(pn2);
		pn.addChildNode(pn1);
/*#if DEBUG*#/
		displayModel(pn); // remove this code in future
/*#end*/
	}

/*#if DEBUG*#/
	/** Display the compiled model in debug mode. Remove this code method
	 * in the future.
	 * @param pn model to be displayed
	 *#/
	private void displayModel(final PNode pn) {
		if (_dbgSwitches.contains(XConstants.XDPROPERTYVALUE_DBG_SHOWXON)){
			System.out.flush();
			System.err.flush();
			System.out.println("*** xdef: \"" + (pn._parent._xdef != null
				? pn._parent._xdef.getName() : "???") +'"');
			System.out.println(
				"* "+org.xdef.xml.KXmlUtils.nodeToString(pn.toXML(),true)+" *");
			System.out.flush();
		}
	}
/*#end*/

	/** Create X-definition model from PNode with XON/JSON description.
	 * @param pn PNode with XON/JSON script.
	 * @param format "xon" or "ini".
	 * @param name name of XON/JSON model in X-definition.
	 * @param reporter report writer
	 */
	final String genXdef(final PNode pn,
		final String format,
		final SBuffer name,
		final ReportWriter reporter) {
		XonModelParser jp = new XonModelParser(this);
		XonParsers xp = format.equals("xon")
			? new XonReader(pn._value, jp) : new IniReader(pn._value, jp);
		xp.setReportWriter(reporter);
		xp.setXdefMode();
		xp.parse();
		genXonModel(jp.getResult(), pn);
		xp = null;
		jp = null;
		pn._value = null;
/*#if DEBUG*#/
		displayModel(pn); // remove this code in future
/*#end*/
		return _anyXPos;
	}

	/** This class provides parsing of XON/JSON source and creates the XON
	 * structure composed from JObjets used for compilation of XON/JSON model
	 * in X-definition.
	 */
	private static class XonModelParser implements XonParser {
		/** kind value */
		private final int VALUE = 0;
		/** kind array */
		private final int ARRAY = 1;
		/** kind map */
		private final int MAP = 2;

		/** stack with kinds of nested items. */
		private final Stack<Integer> _kinds = new Stack<Integer>();
		/** stack with kinds of arrays. */
		private final Stack<JArray> _arrays = new Stack<JArray>();
		/** stack with kinds of maps. */
		private final Stack<JMap> _maps = new Stack<JMap>();
		/** stack of names in map. */
		private final Stack<SBuffer> _names = new Stack<SBuffer>();
		/** actual kind (VALUE, ARRAY or MAP). */
		private int _kind; // ARRAY, MAP, or VALUE
		/** parsed value. */
		private JObject _value;

		/** Create new instance of XonModelParser. */
		XonModelParser(CompileXonXdef jx) {_kinds.push(_kind = VALUE);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of JParser interface
////////////////////////////////////////////////////////////////////////////////

		@Override
		/** Put value to result.
		 * @param value JValue to be added to result object.
		 */
		public void putValue(JValue value) {
			if (_kind == ARRAY) {
				_arrays.peek().add(value);
			} else if (_kind == MAP) {
				SBuffer name = _names.pop();
				_maps.peek().put(name.getString(), value);
			} else { // must be now VALUE
				_value = value;
			}
		}
		@Override
		/** Set name of value pair.
		 * @param name value name.
		 * @return true if the name of pair already exists.
		 */
		public boolean namedValue(SBuffer name) {
			String s = name.getString();
			boolean result = false;
			for (SBuffer x: _names) {
				if (s != null && s.equals(x.getString())
					|| s == null && x.getString() == null) {
					result = true;
					break;
				}
			}
			_names.push(name);
			return result;
		}
		@Override
		/** Array started.
		 * @param pos source position.
		 */
		public void arrayStart(SPosition pos) {
			 //add ARRAY to kins stack and set it to kind
			_kinds.push(_kind = ARRAY);
			_arrays.push(new JArray(pos));  // new item to array stack
		}
		@Override
		/** Array ended.
		 * @param pos source position.
		 */
		public void arrayEnd(SPosition pos) {
			_kinds.pop();
			_kind = _kinds.peek();
			_value = _arrays.peek();
			_arrays.pop();
			if (_kind == MAP) {
				_maps.peek().put(_names.pop().getString(), _value);
			} else if (_kind == ARRAY) {
				_arrays.peek().add(_value);
			} // else it is VALUE
		}
		@Override
		/** Map started.
		 * @param pos source position.
		 */
		public void mapStart(SPosition pos) {
			//add MAP to kins stack and set it to kind
			_kinds.push(_kind = MAP);
			_maps.push(new JMap(pos)); // new item to map stack
		}
		@Override
		/** Map ended.
		 * @param pos source position.
		 */
		public void mapEnd(SPosition pos) {
			_kinds.pop();
			_kind = _kinds.peek();
			_value = (JObject)_maps.peek();
			_maps.pop();
			if (_kind == MAP) { // parent is map
				_maps.peek().put(_names.pop().getString(), _value);
			} else if (_kind == ARRAY) { // parent array
				_arrays.peek().add(_value);
			} // parent is value
		}
		@Override
		/** Processed comment.
		 * @param value SBuffer with the value of comment.
		 */
		public void comment(SBuffer value){/*we ingore it here*/}
		@Override
		/** X-script item parsed, not used methods for JSON/XON parsing
		 * (used in X-definition compiler).
		 * @param name name of item.
		 * @param value value of item.
		 */
		public void xdScript(SBuffer name, SBuffer value) {
			SPosition spos = value == null ? name : value;
			if (ANY_NAME.equals(name.getString())) {
				namedValue(new SBuffer(null, name));
			} else if (ANY_OBJ.equals(name.getString())) {
				putValue(new JAny((SPosition)name, value));
			} else {
				JValue jv;
				if (ONEOF_DIRECTIVE.equals(name.getString())) {
					jv =  new JValue(name, new JValue(spos,
						ONEOF_DIRECTIVE+(value==null? "" : value.getString())));
				} else {
					jv = new JValue(name, new JValue(spos,
						value == null ? "" : value.getString()));
				}
				if (_kind == 1) { // array
					_arrays.peek().add(jv);
				} else if (_kind == 2) { // map
					_maps.peek().put(SCRIPT_DIRECTIVE, jv);
				}
			}
		}
		@Override
		/** Get result of parser.
		 * @return parsed object.
		 */
		public final Object getResult() {return _value;}
	}
}
