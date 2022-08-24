package org.xdef.impl.compile;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.xon.IniReader;
import org.xdef.xon.XonNames;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ONEOF_CMD;
import static org.xdef.xon.XonNames.SCRIPT_CMD;
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


/** Create X-definition model from xd:xon element.
 * @author Vaclav Trojan
 */
public final class CompileXonXdef extends StringParser {

	/** XPosition of $:any model.*/
	private final byte _xonMode;
	/** Prefix of X-definition namespace. */
	private String _xdPrefix;
	/** Index of X-definition namespace. */
	private int _xdIndex;
	/** Namespace of X-definition.*/
	private String _xdNamespace;
	/** XPath position of XON/JSON description.*/
	private String _basePos;
	/** PNode with generated model.*/
	private final PNode _xonModel;
	private final String _anyName;
	/** X-position of generated $:any model.*/
	private String _anyXPos;

	/** Prepare instance of CompileXonXdef. */
	CompileXonXdef(PNode p,
		final byte xonMode,
		final SBuffer name,
		final ReportWriter reporter) {
		super();
		_xonMode = xonMode;
		_xdNamespace = p._nsURI;
		_xdPrefix = p.getPrefix();
		_xdIndex = p._nsPrefixes.get(_xdPrefix);
		_basePos = p._xpathPos + "/text()";
		setReportWriter(reporter);
		_xonModel = p;
		_anyName = name.getString() + "_ANY";
		_anyXPos = null;
	}

	/** Set attribute to PNode.
	 * @param e PNode where to set an attribute.
	 * @param name name of attribute.
	 * @param val SBuffer with the value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setAttr(final PNode e,
		final String name,
		final SBuffer val) {
		PAttr patt = new PAttr(name, val, null, -1);
		patt._localName = name;
		e.setAttr(patt);
		patt._xpathPos = _basePos;
		return patt;
	}

	/** Get X-def attribute.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return PAttr or null.
	 */
	private PAttr getXDAttr(final PNode e, final String name) {
		return e.getAttrNS(name, e._nsPrefixes.get(_xdPrefix));
	}

	/** Set X-def attribute.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @param val SBuffer with value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setXDAttr(final PNode e,
		final String name,
		final SBuffer val) {
		int nsindex;
		if (e._nsPrefixes.containsKey(_xdPrefix)) {
			nsindex = e._nsPrefixes.get(_xdPrefix);
		} else {
			nsindex = e._nsPrefixes.size();
			e._nsPrefixes.put(_xdPrefix, nsindex);
		}
		PAttr a = new PAttr(_xdPrefix+":"+name, val, _xdNamespace, nsindex);
		a._localName = name;
		e.removeAttr(a._name);
		e.setAttr(a);
		a._xpathPos = _basePos;
		return a;
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
		if (sbuf != null) {
			setSourceBuffer(sbuf);
			return parseTypeDeclaration();
		}
		return new SBuffer[] {
			new SBuffer("", getPosition()), new SBuffer("", getPosition())};
	}

	/** Parse X-script and return occurrence and executive part
	 * (type declaration) in separate fields.
	 * @return array os SBuffer with the occurrence part and remaining part.
	 */
	private SBuffer[] parseTypeDeclaration() {
		skipSemiconsBlanksAndComments();
		SBuffer[] result = new SBuffer[] {
			new SBuffer("",getPosition()),new SBuffer("",getPosition()),null};
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
	 * @param e PNode where to set or update the xd:script attribute.
	 * @param matchexpr the match expression.
	 */
	private void addMatchExpression(final PNode e, final String matchexpr) {
		PAttr attr = getXDAttr(e, "script");
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
			val = new SBuffer("match " + matchexpr + ';', e._name);
		}
		setXDAttr(e, "script", val);
	}

	/** Update key information to xd:script attribute.
	 * @param e PNode where to update.
	 * @param key value of key.
	 */
	private void updateKeyInfo(final PNode e, final String key) {
		if (ANY_NAME.equals(key)) {
			setAttr(e,X_KEYATTR, new SBuffer("string(0,*);", e._name));
		} else { // ANY name
			addMatchExpression(e, '@' + X_KEYATTR + "=='"+ key +"'");
			setAttr(e, X_KEYATTR, new SBuffer("fixed('" + key + "');",e._name));
		}
	}

	private PNode genXonValue(final String name,
		final JValue jo,
		final PNode parent) {
		SBuffer sbf, occ = null;
		PNode e;
		if (jo instanceof JAny) {
			e = genXonAny((JAny) jo, parent);
		} else {
			e = genJElement(parent, X_ITEM, jo.getPosition());
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
					setXDAttr(e, "script", occ);
				}
				setAttr(e, X_VALATTR, sbf);
			}
		}
		if (name != null) {
			if (e._nsPrefixes.containsKey(_xdPrefix)
				&& "choice".equals(e.getLocalName())) {
				for (PNode p: e.getChildNodes()) {
					setName(p, name, occ);
				}
			} else {
				setName(e, name, occ);
			}
		}
		return e;
	}

	private void setName(PNode e, String name, SBuffer occ) {
		if (ANY_NAME.equals(name)) {
			setAttr(e,X_KEYATTR, new SBuffer("string();", e._name));
		} else {
			addMatchExpression(e, '@' + X_KEYATTR + "=='"+ name +"'");
			setAttr(e,X_KEYATTR,new SBuffer("fixed('"+name+ "');",e._name));
		}
	}

	private PNode genXonMap(final JMap map, final PNode parent) {
		PNode e, ee;
		Object val = map.get(SCRIPT_CMD);
		if (val != null && val instanceof JValue) {
			map.remove(SCRIPT_CMD);
			JValue jv = (JValue) val;
			setSourceBuffer(jv.getSBuffer());
			isSpacesOrComments();
			if (isToken(ONEOF_CMD)) {
				e = genJElement(parent, X_MAP, map.getPosition());
				ee = genXDElement(e, "choice", getPosition());
				e.addChildNode(ee);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(ee, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
					XOccurrence x = readOccurrence();
					if (x != null && x.maxOccurs() > 1) {
						//Specification of occurence of &{0} group
						// can not be higher then 1
						error(XDEF.XDEF252, ONEOF_CMD);
					}
				}
			} else if (map.size() > 1) {
				e = genJElement(parent, X_MAP, map.getPosition());
				ee = genXDElement(e, "mixed", map.getPosition());
				e.addChildNode(ee);
				if (!eos()) {
					setXDAttr(e, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else {
				e = genJElement(parent, X_MAP, map.getPosition());
				ee = e;
				if (!eos()) {
					setXDAttr(e, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			}
		} else if (map.size() > 1) {
			e = genJElement(parent, X_MAP, map.getPosition());
			ee = genXDElement(e, "mixed", map.getPosition());
			e.addChildNode(ee);
		} else {
			e = genJElement(parent, X_MAP, map.getPosition());
			ee = e;
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
					PNode eee = genXonModel(o, ee);
					if (_xdNamespace.equals(eee._nsURI)
						&& "choice".equals(eee._localName)) {
						for (PNode n : eee.getChildNodes()) {
							updateKeyInfo(n, keyXmlName);
						}
					} else {
						updateKeyInfo(eee, keyXmlName);
					}
				} else {
					ee.addChildNode(genXonValue(keyXmlName, (JValue) o, ee));
				}
			}
		}
		if (anyItem != null) {
			if (anyItem instanceof Map || anyItem instanceof List) {
				PNode eee = genXonModel(anyItem, ee);
				if (_xdNamespace.equals(eee._nsURI)
					&& "choice".equals(eee._localName)) {
					for (PNode n : eee.getChildNodes()) {
						updateKeyInfo(n, ANY_NAME);
					}
				} else {
					updateKeyInfo(eee, ANY_NAME);
				}
				setAnyOccurrence(eee);
			} else {
				PNode eee = genXonValue(ANY_NAME, (JValue) anyItem,ee);
				ee.addChildNode(eee);
//				setAnyOccurrence(eee);
			}
		}
		return e;
	}

	private void setAnyOccurrence(final PNode e) {
		PAttr patt = getXDAttr(e, "script");
		SBuffer val;
		if (patt != null) {
			SBuffer[] sbx = parseTypeDeclaration(patt.getValue());
			String s = sbx[1] == null ? "" : "+;" + sbx[1].getString();
			val = new SBuffer(s, patt._value);
		} else {
			val = new SBuffer("*");
		}
		setXDAttr(e, "script", val);
	}

	private PNode genXonArray(final JArray array, final PNode parent) {
		PNode e = genJElement(parent, X_ARRAY, array.getPosition());
		int index = 0;
		int len = array.size();
		if (len > 0) {
			Object jo = array.get(0);
			Object o = jo == null
				? null : jo instanceof JValue ? ((JValue) jo).getValue() : jo;
			if (o != null && o instanceof JValue) {
				setSourceBuffer(((JValue) o).getSBuffer());
				isSpacesOrComments();
				if (isToken(ONEOF_CMD)) {
					e = genXDElement(
						parent, "choice", ((JValue) jo).getPosition());
					skipSemiconsBlanksAndComments();
					String s = getUnparsedBufferPart().trim();
					if (!s.isEmpty()) {
						if (!s.endsWith(";")) {
							s += ";";
						}
						setXDAttr(parent,"script",new SBuffer(s,getPosition()));
						setSourceBuffer(new SBuffer(s));
						XOccurrence x = readOccurrence();
						if (x != null && x.minOccurs() == 0
							&& X_MAP.equals(parent.getLocalName())) {
							setXDAttr(e, "script",
								new SBuffer("?", getPosition()));
						}
					}
				} else {
					String s = getUnparsedBufferPart().trim();
					if (!s.isEmpty()) {
						setXDAttr(e, "script",
							new SBuffer(s + ';', ((JValue) jo).getSBuffer()));
					}
				}
				index = 1;
			}
			for(; index < len; index++) {
				PNode ee = genXonModel(array.get(index), e);
				PAttr val;
				// if it is not the last and it has xd:script attribute where
				// the min occurrence differs from max occurrence
				// and it has the attribute with a value description
				if (X_ITEM.equals(ee._localName)
					&& XDConstants.XON_NS_URI_W.equals(ee._nsURI)
					&& (val = ee.getAttrNS(X_VALATTR, -1)) != null) {
					PAttr script = getXDAttr(ee, "script");
					XOccurrence occ = null;
					if (script != null) {
						SBuffer[] sbs = parseTypeDeclaration(script.getValue());
						setSourceBuffer(sbs[0]);
						occ = readOccurrence();
					}
					if (index < len-1 && e.getNSIndex() == _xdIndex //xdef
						&& ("mixed".equals(e.getLocalName()) // mixed or choice
							|| "choice".equals(e.getLocalName()))
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
						addMatchExpression(ee,
							s + ".parse((String)@"
								+ X_VALATTR + ").matches()");
					}
				}
			}
		}
		return e;
	}

	/** Create PNode for $:any.
	 * @param xon XON/JSON parsed data.
	 * @param parent parent PNode,
	 * @return created PNode.
	 */
	private PNode genXonAny(final JAny jo, final PNode parent) {
		if (_anyXPos == null) {
			_anyXPos = _xonModel._parent._xdef.getName() + "#" + _anyName;
		}
		PNode e = genXDElement(parent, "choice", jo.getPosition());
		setXDAttr(e, "ref", new SBuffer(_anyXPos, jo.getPosition()));
		parent.addChildNode(e);
		return e;
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
		PNode e;
		if (xon instanceof JMap) {
			e = genXonMap((JMap) xon, parent);
		} else if (xon instanceof JArray) {
			e = genXonArray((JArray) xon, parent);
		} else if (xon instanceof JValue
			&& ((JValue) xon).getValue() instanceof String) {
			e = genXonValue(null, (JValue) xon, parent);
		} else if (xon instanceof JAny) {
			e = genXonAny((JAny) xon, parent);
		} else {
			error(JSON.JSON011); //Not XON/JSON object&{0}
			return parent;
		}
		parent.addChildNode(e);
		return e;
	}

	PNode genXonAnyModels() {
		PNode e, ee, eee;
		SPosition spos = _xonModel._name;
//"<xd:choice xd:name=\"XON_ANY_\">\n" +
		e = genXDElement(_xonModel._parent, "choice", spos);
		setXDAttr(e, "name", new SBuffer(_anyName, spos));
//"  <jx:item key=\"? string();\" val=\"jvalue();\"/>\n" +
		ee = genJElement(e, X_ITEM, spos);
		setAttr(ee, X_KEYATTR, new SBuffer("? string();", spos));
		setAttr(ee, X_VALATTR, new SBuffer("jvalue();", spos));
		e.addChildNode(ee);
//"  <jx:array key=\"? string();\">\n" +
		ee = genJElement(e, X_ARRAY, spos);
		setAttr(ee, X_KEYATTR, new SBuffer("? string();", spos));
//"    <xd:choice xd:script=\"occurs *; ref #XON_ANY_\"/>\n" +
		eee = genXDElement(ee, "choice", spos);
		setXDAttr(eee, "script", new SBuffer("*; ref "+_anyName, spos));
		ee.addChildNode(eee);
//"  </jx:array>\n" +
		e.addChildNode(ee);
//"  <jx:map key=\"? string();\">\n" +
		ee = genJElement(e, X_MAP, spos);
		setAttr(ee, X_KEYATTR, new SBuffer("? string();", spos));
//"    <xd:choice xd:script=\"occurs *; ref #XON_ANY_\"/>\n" +
		eee = genXDElement(ee, "choice", spos);
		setXDAttr(eee, "script", new SBuffer("*; ref "+_anyName, spos));
		ee.addChildNode(eee);
//"  </jx:map>\n" +
		e.addChildNode(ee);
//		_xonModel._parent.addChildNode(e);
		return e;
	}

	/** Create X-definition model from PNode with XON/JSON description.
	 * @param p PNode with XON/JSON script.
	 * @param xonMode version of transformation XON/JSON to XML).
	 * @param format "xon" or "ini".
	 * @param name name of XON/JSON model in X-definition.
	 * @param reporter report writer
	 */
	static final PNode genXdef(final PNode p,
		final byte xonMode,
		final String format,
		final SBuffer name,
		final ReportWriter reporter) {
		if (xonMode != XConstants.XON_MODE_W) {
			//Internal error&{0}{: }
			throw new SRuntimeException(SYS.SYS066, "Namespace W3C expected");
		}
		CompileXonXdef jx = new CompileXonXdef(p, xonMode, name, reporter);
		p._name = name;
		p._nsURI = null; // set no namespace
		p._nsindex = -1;
		XonModelParser jp = new XonModelParser(jx);
		XonParsers pp = format.equals("xon")
			? new XonReader(p._value, jp) : new IniReader(p._value, jp);
		pp.setReportWriter(reporter);
		pp.setXdefMode();
		pp.parse();
		jx.genXonModel(jp.getResult(), p);
		pp = null;
		jp = null;
		p._value = null;
/*#if DEBUG*#/
		String dbgSwitches = System.getProperty("xdef-xon_debug");
		if (dbgSwitches != null && dbgSwitches.contains("showModel")) {
			// display created model
			System.err.flush();
			System.out.println(
				org.xdef.xml.KXmlUtils.nodeToString(p.toXML(),true));
			System.out.flush();
		}
/*#end*/
		if (jx._anyXPos == null) {
			return null;
		}
		return jx.genXonAnyModels();
	}

	/** This class provides parsing of XON/JSON source and creates the XON
	 * structure composed from JObjets used for compilation of XON/JSON model
	 * in X-definition.
	 */
	private static class XonModelParser implements XonParser {
		/** kind undefined */
		private final int UNDEFINED = 0;
		/** kind array */
		private final int ARRAY = 1;
		/** kind map */
		private final int MAP = 2;
		/** kind simple value */
		private final int VALUE = 3;

		/** stack with kinds of nested items. */
		private final Stack<Integer> _kinds = new Stack<Integer>();
		/** stack with kinds of arrays. */
		private final Stack<JArray> _arrays = new Stack<JArray>();
		/** stack with kinds of maps. */
		private final Stack<JMap> _maps = new Stack<JMap>();
		/** stack of names in map. */
		private final Stack<SBuffer> _names = new Stack<SBuffer>();
		/** actual kind (VALUE, ARRAY or MAP). */
		private int _kind; // ARRAY, MAP, VALUE or UNDEFINED
		/** parsed value. */
		private JObject _value;

		/** Create new instance of XonModelParser. */
		XonModelParser(CompileXonXdef jx) {_kinds.push(_kind = UNDEFINED);}

////////////////////////////////////////////////////////////////////////////////
// JParser interface
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
			} else { // must be now ITEM
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
			String s;
			if (ANY_NAME.equals(name.getString())) {
				namedValue(new SBuffer(null, name));
			} else if (XonNames.ANY_OBJECT.equals(name.getString())) {
				putValue(new JAny((SPosition)name, value));
			} else {
				s = ONEOF_CMD.equals(name.getString()) ? ONEOF_CMD : "";
				s += value == null ? "" : value.getString();
				JValue jv = new JValue(name, new JValue(spos, s));
				if (_kind == 1) { // array
					_arrays.peek().add(jv);
				} else if (_kind == 2) { // map
					_maps.peek().put(SCRIPT_CMD, jv);
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
