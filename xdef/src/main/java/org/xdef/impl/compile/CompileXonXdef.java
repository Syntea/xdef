package org.xdef.impl.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import static org.xdef.impl.compile.XScriptParser.ASK_SYM;
import static org.xdef.impl.compile.XScriptParser.BEG_SYM;
import static org.xdef.impl.compile.XScriptParser.CONSTANT_SYM;
import static org.xdef.impl.compile.XScriptParser.CREATE_SYM;
import static org.xdef.impl.compile.XScriptParser.DDOT_SYM;
import static org.xdef.impl.compile.XScriptParser.DEFAULT_SYM;
import static org.xdef.impl.compile.XScriptParser.END_SYM;
import static org.xdef.impl.compile.XScriptParser.FINALLY_SYM;
import static org.xdef.impl.compile.XScriptParser.FIXED_SYM;
import static org.xdef.impl.compile.XScriptParser.FORGET_SYM;
import static org.xdef.impl.compile.XScriptParser.IGNORE_SYM;
import static org.xdef.impl.compile.XScriptParser.ILLEGAL_SYM;
import static org.xdef.impl.compile.XScriptParser.INIT_SYM;
import static org.xdef.impl.compile.XScriptParser.MATCH_SYM;
import static org.xdef.impl.compile.XScriptParser.MUL_SYM;
import static org.xdef.impl.compile.XScriptParser.OCCURS_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ABSENCE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_EXCESS_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_FALSE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_ATTR_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_START_ELEMENT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_TRUE_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTIONAL_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTIONS_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTION_SYM;
import static org.xdef.impl.compile.XScriptParser.PLUS_SYM;
import static org.xdef.impl.compile.XScriptParser.REF_SYM;
import static org.xdef.impl.compile.XScriptParser.REQUIRED_SYM;
import static org.xdef.impl.compile.XScriptParser.SEMICOLON_SYM;
import static org.xdef.impl.compile.XScriptParser.UNDEF_SYM;
import static org.xdef.impl.compile.XScriptParser.VAR_SYM;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import org.xdef.xon.IniReader;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;
import org.xdef.xon.XonParser;
import org.xdef.xon.XonParsers;
import org.xdef.xon.XonReader;
import static org.xdef.xon.XonReader.X_ONEOF_DIRECTIVE;
import static org.xdef.xon.XonReader.X_SCRIPT_DIRECTIVE;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonTools.JAny;
import org.xdef.xon.XonTools.JArray;
import org.xdef.xon.XonTools.JMap;
import org.xdef.xon.XonTools.JObject;
import org.xdef.xon.XonTools.JValue;

/** Create X-definition model from xd:json/xon element.
 * @author Vaclav Trojan
 */
public final class CompileXonXdef extends XScriptParser {

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
	CompileXonXdef(final PNode pn, final SBuffer name, final ReportWriter reporter) {
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
	private PAttr setAttr(final PNode pn, final String name, final SBuffer val) {
		PAttr pa = new PAttr(name, val, null, -1);
		pa._localName = name;
		pn.setAttr(pa);
		pa._xpathPos = _basePos;
		return pa;
	}

	/** Get Xdef attribute.
	 * @param pn PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return PAttr or null.
	 */
	private PAttr getXDAttr(final PNode pn, final String name) {
		return pn.getAttrNS(name, pn._nsPrefixes.get(_xdPrefix));
	}

	/** Set Xdef attribute.
	 * @param pn PNode where to set attribute.
	 * @param name local name of attribute.
	 * @param val SBuffer with value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setXDAttr(final PNode pn, final String name, final SBuffer val) {
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
	 */
	private void skipSpacesAndComments() {
		isSpaces();
		while(isToken("/*") ) {
			if (!findTokenAndSkip("*/")) {
				error(JSON.JSON015); //Unclosed comment
				setEos();
				return;
			}
			isSpaces();
		}
	}

	/** Skip all blanks, comments and semicolons. */
	private void skipSemiconsBlanksAndComments() {
		for(;;) {
			skipSpacesAndComments();
			if (!isChar(';')) {
				return;
			}
		}
	}

	/** Parse X-script and return occurrence and executive part
	 * (type declaration) in separate fields.
	 * @param sbuf source text with X-script
	 * @return array with SBuffer items (item 0 is occurrence specification)
	 * and item 1 is composed form remaining X-script parts).
	 */
	private SBuffer[] parseTypeDeclaration(final SBuffer sbuf) {
		SBuffer[] result = new SBuffer[] {null, new SBuffer("")};
		if (sbuf == null) {
			return result;
		}
		List<Object> sectionList = parseXscript(sbuf);
		SBuffer occ = removeSection("occurs", sectionList);
		if (occ != null) {
			result[0] = occ;
		}
		result[1] = xsToString(sectionList);
		setPosition((SBuffer) result[1]);
		return result;
	}

	/** Create PNode.
	 * @param parent parent node.
	 * @param nsURI namespace URI.
	 * @param name qualified name of PNode.
	 * @param pos source position.
	 * @return created PNode.
	 */
	private PNode genPElement(final PNode parent, final String nsURI, final String name, final SPosition pos){
		PNode result = new PNode(name, pos, parent, parent._xdVersion, parent._xmlVersion);
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
	 * @param pos source position
	 * @return created PNode,
	 */
	private PNode genJElement(final PNode parent, final String name, final SPosition pos) {
		return genPElement(parent, XDConstants.XON_NS_URI_W, XDConstants.XON_NS_PREFIX + ":" + name, pos);
	}

	/** Create PNode as XDef element with given position,
	 * @param parent parent PNode.
	 * @param name local name of XDef element.
	 * @param spos pos position
	 * @return created PNode,
	 */
	private PNode genXDElement(final PNode parent, final String name, final SPosition pos) {
		return genPElement(parent, _xdNamespace, _xdPrefix + ":" + name, pos);
	}

////////////////////////////////////////////////////////////////////////////////
// Create X-definition model from xd:json/xon (use W3C format)
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
			List<Object> sectionList = parseXscript(val);
			SBuffer matchItem = findSection("match", sectionList);
			if (matchItem == null) {
				matchItem =
					new SBuffer(matchexpr, sectionList.isEmpty() ? pn._name : (SBuffer) sectionList.get(1));
				sectionList.add("match");
				sectionList.add(matchItem);
			} else {
				String s = matchItem.getString();
				matchItem.setString(s.startsWith("{")
					? "{if (!(" +matchexpr+ ")) return false;" + s.substring(1) : matchexpr + " AAND " + s);
			}
			val = xsToString(sectionList);
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

	private PNode genXonValue(final String name, final JValue jo, final PNode parent) {
		SBuffer sbf, sbocc;
		PNode pn = genJElement(parent, X_VALUE, jo.getPosition());
		if (jo.getValue() != null) {
			if (jo.toString().trim().isEmpty()) {
				sbf = new SBuffer("jvalue()", jo.getPosition());
				sbocc = new SBuffer("?", jo.getPosition());
			} else {
				SBuffer[] parsedScript = parseTypeDeclaration(jo.getSBuffer());
				if ((sbocc = parsedScript[0]) != null) { // occurrence
					if (!ANY_NAME.equals(name) && !X_ARRAY.equals(parent.getLocalName())) {
						setSourceBuffer(sbocc); // read occurrence
						nextSymbol();
						XOccurrence xocc = new XOccurrence();
						isOccurrence(xocc);
						if (xocc.maxOccurs() != 1) {
							//Maximum occurrence of item "&{0}" can not be higher then 1
							error(XDEF.XDEF535, XonTools.xmlToJName(name));
						}
					}
				}
				if (parsedScript[1].getString().isEmpty()) {//no validation etc
					parsedScript[1] = new SBuffer("jvalue()", jo.getPosition());
				}
				sbf = parsedScript[1];
			}
			if (sbocc != null) { // occurrence
				setXDAttr(pn, "script", sbocc);
			}
			setAttr(pn, X_VALATTR, sbf);
		}
		if (name != null) {
			if (pn._nsPrefixes.containsKey(_xdPrefix) && "choice".equals(pn.getLocalName())) {
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
			setAttr(pn, X_KEYATTR, new SBuffer("string();", pn._name));
		} else {
			addMatchExpression(pn, '@' + X_KEYATTR + "=='"+ name +"'");
			setAttr(pn, X_KEYATTR, new SBuffer("fixed('"+name+ "');", pn._name));
		}
	}

	private PNode genXonMap(final JMap map, final PNode parent) {
		PNode pn1, pn2;
		Object val = map.get(X_SCRIPT_DIRECTIVE);
		if (val != null && val instanceof JValue) {
			map.remove(X_SCRIPT_DIRECTIVE);
			JValue jv = (JValue) val;
			setSourceBuffer(jv.getSBuffer());
			skipSpacesAndComments();
			if (isToken(X_ONEOF_DIRECTIVE)) {
				pn1 = genJElement(parent, X_MAP, map.getPosition());
				pn2 = genXDElement(pn1, "choice", getPosition());
				pn1.addChildNode(pn2);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(pn2, "script", new SBuffer(getUnparsedBufferPart(), getPosition()));
					List<Object> sectionList = parseXscript();
					SBuffer item = removeSection("occurs", sectionList);
					if (item != null) {
						XOccurrence occ = new XOccurrence();
						setSourceBuffer(item);
						setSourceBuffer(item);
						nextSymbol();
						isOccurrence(occ);
						if (occ.maxOccurs() > 1) {
							//Specification of occurence of &{0} group can not be higher then 1
							error(XDEF.XDEF252, ONEOF_DIRECTIVE);
						}
					}
				}
			} else if (map.size() > 1) {
				pn1 = genJElement(parent, X_MAP, map.getPosition());
				pn2 = genXDElement(pn1, "mixed", map.getPosition());
				pn1.addChildNode(pn2);
				if (!eos()) {
					setXDAttr(pn1, "script", new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else {
				pn2 = pn1 = genJElement(parent, X_MAP, map.getPosition());
				if (!eos()) {
					setXDAttr(pn1, "script", new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			}
		} else if (map.size() > 1) {
			pn1 = genJElement(parent, X_MAP, map.getPosition());
			pn2 = genXDElement(pn1, "mixed", map.getPosition());
			pn1.addChildNode(pn2);
		} else {
			pn2 = pn1 = genJElement(parent, X_MAP, map.getPosition());
		}
		Object anyItem = null;
		for (Map.Entry<Object, Object> entry: map.entrySet()) {
			String key = (String) entry.getKey();
			Object o = entry.getValue();
			if (key == null) { // ANY_NAME ... || ANY_NAME.equals(key)
				anyItem = o;
			} else {
				String keyXmlName = XonTools.toXmlName(key);
				if (o != null && (o instanceof Map || o instanceof List)) {
					PNode pn3 = genXonModel(o, pn2);
					PAttr xscr = getXDAttr(pn3, "script");
					if (xscr != null) {
						SBuffer sbf = removeSection("occurs", parseXscript(xscr._value));
						if (sbf != null) {
							XOccurrence occ = new XOccurrence();
							setSourceBuffer(sbf);
							nextSymbol();
							isOccurrence(occ);
							if (occ.maxOccurs() > 1) {
								//Maximum occurrence of item "&{0}" can not be higher then 1
								error(XDEF.XDEF535, key);
							}
						}
					}
					if (_xdNamespace.equals(pn3._nsURI) && "choice".equals(pn3._localName)) {
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
				if (_xdNamespace.equals(pn3._nsURI) && "choice".equals(pn3._localName)) {
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
					setAttr(pn3, X_KEYATTR, new SBuffer("string()", getPosition()));
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
			Object o = jo == null ? null : jo instanceof JValue ? ((JValue) jo).getValue() : jo;
			if (o != null && o instanceof JValue) {
				setSourceBuffer(((JValue) o).getSBuffer());
				skipSpacesAndComments();
				if (isToken(ONEOF_DIRECTIVE)) {
					String s = getUnparsedBufferPart().trim();
					pn = genXDElement(parent, "choice", ((JValue) jo).getPosition());
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
						SPosition spos = getPosition();
						List<Object> sectionList = parseXscript(new SBuffer(s));
						SBuffer item = removeSection("occurs", sectionList);
						if (item != null) {
							XOccurrence occ = new XOccurrence();
							setSourceBuffer(item);
							nextSymbol();
							isOccurrence(occ);
							if (occ.minOccurs() == 0 && X_MAP.equals(parent.getLocalName())) {
								setXDAttr(pn, "script",new SBuffer("?", spos));
							}
						}
						if (!s.isEmpty()) {
							setXDAttr(parent, "script", new SBuffer(s, spos));
						}
					}
				} else {
					String s = getUnparsedBufferPart().trim();
					if (!s.isEmpty()) {
						setXDAttr(pn, "script", new SBuffer(s + ';', ((JValue) jo).getSBuffer()));
					}
				}
				index = 1;
			}
			for(; index < len; index++) {
				PNode pn1 = genXonModel(array.get(index), pn);
				PAttr val;
				// if it is not the last and it has xd:script attribute where the min occurrence differs from
				// max occurrence and it has the attribute with a value description
				if (X_VALUE.equals(pn1._localName) && XDConstants.XON_NS_URI_W.equals(pn1._nsURI)
					&& (val = pn1.getAttrNS(X_VALATTR, -1)) != null) {
					PAttr script = getXDAttr(pn1, "script");
					XOccurrence occ = null;
					if (script != null) {
						SBuffer[] sbs = parseTypeDeclaration(script.getValue());
						if (sbs[0] != null) {
							occ = new XOccurrence();
							setSourceBuffer(sbs[0]);
							isOccurrence(occ);
						}
					}
					if (index < len-1 && pn.getNSIndex() == _xdIndex //xdef
						&& ("mixed".equals(pn.getLocalName()) // mixed or choice
							|| "choice".equals(pn.getLocalName()))
						|| occ != null && occ.minOccurs() != occ.maxOccurs()) {
						SBuffer[] sbs = parseTypeDeclaration(val.getValue());
						String s = sbs[1].getString();
						int i;
						while ((i = s.indexOf("/*")) >= 0) { // remove comments!
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
						addMatchExpression(pn1, s + ".parse((String)@" + X_VALATTR + ").matches()");
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
		} else if (xon instanceof JValue && ((JValue) xon).getValue() instanceof String) {
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
			_anyXPos = _xonModel._parent._xdef.getName() + "#" + XConstants.JSON_ANYOBJECT;
		}
		PNode pn, pn1, pn2;
		pn = genXDElement(parent, "choice", parent.getName());
		pn._xonMode = XConstants.XON_MODE_W;
		parent.addChildNode(pn);
		SBuffer val = jo != null && jo.getSBuffer() != null ? jo.getSBuffer() : new SBuffer("", parent._name);
		String s = val.getString().trim();
		val = s.isEmpty() ? null : new SBuffer(s, parent.getName());
		if (val != null) {
			setXDAttr(pn, "script", val);
		}
		SPosition spos = parent._name;
		pn1 = genJElement(pn, X_VALUE, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		boolean isMap = X_MAP.equals(parent._localName) && XDConstants.XON_NS_URI_W.equals(parent._nsURI);
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
		setXDAttr(pn2, "script", new SBuffer("*; ref " + _anyXPos, spos));
		pn1.addChildNode(pn2);
		pn.addChildNode(pn1);
		pn1 = genJElement(pn, X_MAP, spos);
		pn1._xonMode = XConstants.XON_MODE_W;
		if (isMap) {
			setAttr(pn1, X_KEYATTR, new SBuffer("string();", spos));
		}
		pn2 = genXDElement(pn1, "choice", spos);
		pn2._xonMode = XConstants.XON_MODE_W;
		setXDAttr(pn2, "script", new SBuffer("*; ref " + _anyXPos, spos));
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
		pn1 = genJElement(pn, X_VALUE, spos);
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
		if (_dbgSwitches.contains(XConstants.XDPROPERTYVALUE_DBG_SHOWXON)) {
			System.out.flush();
			System.err.flush();
			System.out.println("*** xdef: \"" + (pn._parent._xdef != null
				? pn._parent._xdef.getName() : "???") +'"');
			System.out.println("* "+org.xdef.xml.KXmlUtils.nodeToString(pn.toXML(),true)+" *");
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
	final String genXdef(final PNode pn, final String format, final SBuffer name,final ReportWriter reporter){
		XonModelParser jp = new XonModelParser(this);
		XonParsers xp = format.equals("xon")
			? new XonReader(pn._value, jp) : new IniReader(pn._value, jp);
		xp.setReportWriter(reporter);
		xp.setXdefMode();
		xp.parse();
		genXonModel(jp.getResult(), pn);
		pn._value = null;
/*#if DEBUG*#/
		displayModel(pn); // remove this code in future
/*#end*/
		return _anyXPos;
	}

////////////////////////////////////////////////////////////////////////////////
// X-script parser
////////////////////////////////////////////////////////////////////////////////
	/** Check if id of parsed section name is a section name.
	 * @param sym ID of parsed section name.
	 * @return true if it is a section name.
	 */
	private static boolean isSectionCommand(final char sym) {
		return sym==VAR_SYM||sym==FINALLY_SYM||sym==CREATE_SYM||sym==ON_TRUE_SYM||sym==ON_FALSE_SYM
			||sym==ON_ABSENCE_SYM||sym==ON_ILLEGAL_ATTR_SYM||sym==CREATE_SYM||sym==MATCH_SYM
			||sym==ON_START_ELEMENT_SYM||sym==FINALLY_SYM||sym==FORGET_SYM||sym==INIT_SYM||sym==DEFAULT_SYM
			||sym==FIXED_SYM||sym==REF_SYM||sym==ON_EXCESS_SYM||sym==OPTION_SYM||sym==OPTIONS_SYM;
	}

	/** Parse command which follows section.
	 * @return true if section command was parsed.
	 */
	private boolean readSectionCommand() {
		if (_sym == BEG_SYM) {
			int n = 1;
			do {
				if (nextSymbol() == END_SYM) {
					if (--n == 0) {
						return true;
					}
				} else if (_sym == BEG_SYM) {
					n++;
				}
			} while(!eos());
		} else if (_sym != SEMICOLON_SYM && _sym!= NOCHAR) {
			if (nextSymbol() == UNDEF_SYM) {
				setEos();
				return false;
			}
			while(_sym != SEMICOLON_SYM && _sym!= NOCHAR){
				if (nextSymbol() == UNDEF_SYM) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/** Check if it is an occurrence specification.
	 * @return SPosition of parsed occurrence specification or null
	 */
	private SPosition isOccurrence() {
		SPosition spos = getLastPosition();
		if ((_sym == OCCURS_SYM)) {
			nextSymbol();
		}
		switch (_sym) {
			case MUL_SYM:
			case PLUS_SYM:
			case REQUIRED_SYM:
			case ASK_SYM:
			case OPTIONAL_SYM:
			case IGNORE_SYM:
			case ILLEGAL_SYM:
				return spos;
			case CONSTANT_SYM:
				int pos = getIndex();
				char sym = _sym;
				if (nextSymbol() != DDOT_SYM) {
					setIndex(pos); // reset position
					_sym = sym;
				} else {
					sym = _sym;
					pos = getIndex();
					if (nextSymbol() != CONSTANT_SYM && _sym != MUL_SYM) {
						setIndex(pos);  // reset position
						_sym = sym;
					}
				}
				return spos;
			default:
				return null;
		}
	}

	/** Add section item to the list.
	 * @param sectionName section name,
	 * @param sectionList where to add.
	 * @param pos SPosition of the section.
	 */
	private void addSection(final String sectionName, final List<Object> sectionList, final SPosition pos) {
		sectionList.add(sectionName);
		String s = getParsedBufferPartFrom(pos.getIndex()).trim();
		while (s.endsWith(";")) {
			s = s.substring(0, s.length() - 1).trim();
		}
		sectionList.add(new SBuffer(s, pos));
	}

	/** Parse X-script and return the section list.
	 * @param source Source text with X-script.
	 * @return section list. Each section is composed of two items: the first
	 * item is id of section (a character) and the following item is a SBuffer
	 * with the source of the section command.
	 */
	private List<Object> parseXscript(SBuffer source) {
		setSourceBuffer(source);
		return parseXscript();
	}

	/** Parse X-script and return the section list.
	 * @return section list. Each section is composed of two items: the first
	 * item is id of section (a character) and the following item is a SBuffer
	 * with the source of the section command.
	 */
	private List<Object> parseXscript() {
		List<Object> sectionList = new ArrayList<>();
		SPosition spos;
		nextSymbol();
		char sym;
		for (;;) {
			while (_sym == SEMICOLON_SYM || _sym == END_SYM) {
				nextSymbol();
			}
			if (_sym == NOCHAR) {
				break;
			}
			if ((spos = isOccurrence()) != null) {
				addSection("occurs", sectionList, spos);
				if (_sym == SEMICOLON_SYM) {
					continue;
				}
				if (!isSectionCommand(_sym)) {
					spos = getPosition();
					if (readSectionCommand()) {
						String s = getParsedBufferPartFrom(spos.getIndex());
						if (!s.trim().equals(";")) { //it is not only ";"!
							addSection("", sectionList, spos);
						}
					}
				}
			} else if (!isSectionCommand(sym = _sym)) {
				spos = getLastPosition();
				if (!readSectionCommand()) {// this never should not happeh
					error(XDEF.XDEF425); //Script error&{#SYS000}
					break; // do not continue parsing, return sectionList;
				}
				addSection("", sectionList, spos);
			} else {
				spos = getPosition();
				String sectionName = getParsedString();
				nextSymbol();
				if (sym != FORGET_SYM && readSectionCommand()) {
					addSection(sectionName, sectionList, spos);
				} else {  // here should be only "forget"
					addSection(sectionName, sectionList, getPosition());
				}
			}
		}
		return sectionList;
	}

	/** Create X-script string from the list of sections.
	 * @param sectionList list of sections.
	 * @return string with X-script source.
	 */
	private static SBuffer xsToString(final List<Object> sectionList) {
		String result = "";
		boolean wasOccurs = false;
		for (int i = 0; i < sectionList.size(); i++) {
			Object o = sectionList.get(i);
			if (o instanceof String) {
				String sectionName = (String) o;
				if (++i >= sectionList.size()) {
					result += sectionName;
					break;
				}
				o = sectionList.get(i);
				if ("occurs".equals(sectionName)) {
					if (!result.isEmpty() && !result.endsWith(";") && !result.endsWith("}")) {
						result += ';';
					}
					result += ((SBuffer) o).getString();
					wasOccurs = true;
				} else if (sectionName.isEmpty()) { // type validation
					if (wasOccurs) {
						if (!result.isEmpty()) {
							result += ' ';
						}
						result += ((SBuffer) o).getString();
					} else {
						if (!result.isEmpty() && !result.endsWith(";") && !result.endsWith("}")) {
							result += ';';
						}
						result += ((SBuffer) o).getString();
					}
					wasOccurs = false;
				} else {
					if (!result.isEmpty() && !result.endsWith(";") && !result.endsWith("}")) {
						result += ';';
					}
					String s = ((SBuffer) o).getString();
					if (s.isEmpty()) {
						result += sectionName;
					} else {
						result += sectionName + ' ' + s;
					}
					wasOccurs = false;
				}
			}
		}
		if (result.isEmpty()) {
			return new SBuffer("");
		}
		if (!result.endsWith(";")) {
			result += ';';
		}
		return new SBuffer(result, (SBuffer) sectionList.get(1));
	}

	/** Find given section in section list.
	 * @param name name of section or emptyString if it is validation method.
	 * @param list list of section.
	 * @return SBuffer with the section or null.
	 */
	private static SBuffer findSection(final String name, final List<Object> list) {
		for (int i = 0; i < list.size(); i+=2) {
			if (name.equals(list.get(i))) {
				return (SBuffer) list.get(i + 1);
			}
		}
		return null;
	}

	/** Remove given section from section list.
	 * @param name name of section (or emptyString for a validation method).
	 * @param list list of section.
	 * @return removed section.
	 */
	private static SBuffer removeSection(final String name, final List<Object> list) {
		for (int i = 0; i < list.size(); i+=2) {
			if (name.equals(list.get(i))) {
				list.remove(i);
				SBuffer result = (SBuffer) list.get(i);
				list.remove(i);
				return result;
			}
		}
		return null;
	}

////////////////////////////////////////////////////////////////////////////////
// XonModelParser - implementation of XonParser
////////////////////////////////////////////////////////////////////////////////

	/** This class provides parsing of XON/JSON source and creates the XON
	 * structure composed from JObjets used for compilation of XON/JSON model
	 * in X-definition.
	 */
	private final static class XonModelParser implements XonParser {
		/** kind = value */
		private final int VALUE = 0;
		/** kind = array */
		private final int ARRAY = 1;
		/** kind = map */
		private final int MAP = 2;

		/** stack with kinds of nested items. */
		private final Stack<Integer> _kinds = new Stack<>();
		/** stack with kinds of arrays. */
		private final Stack<JArray> _arrays = new Stack<>();
		/** stack with kinds of maps. */
		private final Stack<JMap> _maps = new Stack<>();
		/** stack of names in map. */
		private final Stack<SBuffer> _names = new Stack<>();
		/** actual kind (VALUE, ARRAY or MAP). */
		private int _kind; // ARRAY, MAP, or VALUE
		/** parsed value. */
		private JObject _value;

		/** Create new instance of XonModelParser. */
		XonModelParser(final CompileXonXdef jx) {_kinds.push(_kind = VALUE);}

		////////////////////////////////////////////////////////////////////////
		// Implementation of JParser interface
		////////////////////////////////////////////////////////////////////////

		/** Put value to result.
		 * @param value JValue to be added to result object.
		 */
		@Override
		public final void putValue(final JValue value) {
			switch (_kind) {
				case ARRAY: _arrays.peek().add(value);
					return;
				case MAP:
					SBuffer name = _names.pop();
					_maps.peek().put(name.getString(), value);
					return;
			}
			_value = value; // it is VALUE
		}

		/** Set name of value pair.
		 * @param name value name.
		 * @return true if the name of pair already exists.
		 */
		@Override
		public final boolean namedValue(final SBuffer name) {
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

		/** Array started.
		 * @param pos source position.
		 */
		@Override
		public final void arrayStart(final SPosition pos) {
			 //add ARRAY to kins stack and set it to kind
			_kinds.push(_kind = ARRAY);
			_arrays.push(new JArray(pos));  // new item to array stack
		}

		/** Array ended.
		 * @param pos source position.
		 */
		@Override
		public final void arrayEnd(final SPosition pos) {
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

		/** Map started.
		 * @param pos source position.
		 */
		@Override
		public final void mapStart(final SPosition pos) {
			//add MAP to kins stack and set it to kind
			_kinds.push(_kind = MAP);
			_maps.push(new JMap(pos)); // new item to map stack
		}

		/** Map ended.
		 * @param pos source position.
		 */
		@Override
		public final void mapEnd(final SPosition pos) {
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

		/** Processed comment.
		 * @param value SBuffer with the value of comment.
		 */
		@Override
		public final void comment(final SBuffer value){/*we ingore it here*/}

		/** X-script item parsed, not used methods for JSON/XON parsing
		 * (used in X-definition compiler).
		 * @param name name of item.
		 * @param value value of item.
		 */
		@Override
		public final void xdScript(final SBuffer name, final SBuffer value) {
			String s = name.getString();
			if (s == null) { // ANY_NAME!!!
				namedValue(new SBuffer(null, name));
				return;
			}
			SPosition spos = value == null ? name : value;
			JValue jv;
			switch (name.getString()) {
				case ANY_OBJ:
					putValue(new JAny((SPosition)name, value));
					return;
				case X_ONEOF_DIRECTIVE:
					jv =  new JValue(
						name, new JValue(spos, X_ONEOF_DIRECTIVE + (value == null? "" : value.getString())));
					break;
				case ONEOF_DIRECTIVE:
					if (_kind == 1) {
						jv =  new JValue(
							name, new JValue(spos, ONEOF_DIRECTIVE + (value==null? "" : value.getString())));
						break;
					}
				default:
					jv = new JValue(name, new JValue(spos, value == null ? "" : value.getString()));
			}
			if (_kind == 1) { // array
				_arrays.peek().add(jv);
			} else if (_kind == 2) { // map
				_maps.peek().put(X_SCRIPT_DIRECTIVE, jv);
			}
		}

		/** Get result of parser.
		 * @return parsed object.
		 */
		@Override
		public final Object getResult() {return _value;}
	}
}
