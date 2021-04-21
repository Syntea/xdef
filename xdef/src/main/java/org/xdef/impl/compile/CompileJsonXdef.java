package org.xdef.impl.compile;

import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.json.JsonNames;
import org.xdef.json.JsonParser;
import org.xdef.json.JsonTools;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;

/** Create X-definition model from xd:json.
 * @author Vaclav Trojan
 */
public class CompileJsonXdef extends JsonParser {
	/** Prefix of X-definition namespace. */
	String _xdPrefix;
	/** Index of X-definition namespace. */
	int _xdIndex;
	/** Namespace of X-definition.*/
	String _xdNamespace;
	/** XPath position of JSON description.*/
	String _basePos;

	/** Prepare instance of XJSON. */
	CompileJsonXdef() {super();}

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

	/** get attribute without namespace.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return PAttr or null.
	 */
	private PAttr getAttr(final PNode e, final String name) {
		for (PAttr patt: e.getAttrs()) {
			if (patt._nsindex == -1 && name.equals(patt._localName)) {
				return patt;
			}
		}
		return null;
	}

	/** get X-def attribute.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return PAttr or null.
	 */
	private PAttr getXDAttr(final PNode e, final String name) {
		int nsindex;
		if (e._nsPrefixes.containsKey(_xdPrefix)) {
			nsindex = e._nsPrefixes.get(_xdPrefix);
			for (PAttr att: e.getAttrs()) {
				if (att._nsindex == nsindex && name.equals(att._localName)) {
					return att;
				}
			}
		}
		return null;
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
		for (PAttr patt: e.getAttrs()) {
			if (patt._nsindex == nsindex && name.equals(patt._localName)) {
				e.removeAttr(patt);
				break; // attribute will be replaced
			}
		}
		a._localName = name;
		e.setAttr(a);
		a._xpathPos = _basePos;
		return a;
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
	private XOccurrence readOccurrence(final SBuffer sbuf) {
		setSourceBuffer(sbuf);
		return readOccurrence();
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
			new SBuffer("", getPosition()), new SBuffer("", getPosition())};
		int pos = getIndex();
		SPosition spos = getPosition();
		XOccurrence occ = readOccurrence();
		if (occ != null) {
			result[0] = new SBuffer(getParsedBufferPartFrom(pos), spos);
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
		result._level = parent._level + 1;
		result._xpathPos = _basePos;
		return result;
	}

	/** Create PNode as JSON element with given position,
	 * @param parent parent PNode.
	 * @param name local name of JSON element.
	 * @param spos source position
	 * @return created PNode,
	 */
	private PNode genJElement(final PNode parent,
		final String name,
		final SPosition spos) {
		return genPElement(parent,
			XDConstants.JSON_NS_URI_W3C, "js:" + name, spos);
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
// Create X-definition model from xd:json (use W3C format)
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
		String s = JsonTools.toXmlName(key);
		addMatchExpression(e, '@' + JsonNames.J_KEYATTR + "=='"+ s +"'");
		setAttr(e, JsonNames.J_KEYATTR, new SBuffer("fixed('"+s+"');",e._name));
	}

	private PNode genJsonMap(final JMap map, final PNode parent) {
		PNode e, ee;
		Object val = map.get(JsonParser.SCRIPT_KEY);
		if (val != null) {
			map.remove(JsonParser.SCRIPT_KEY);
			JValue jv =(JValue) val;
			setSourceBuffer(jv.getSBuffer());
			isSpacesOrComments();
			if (isToken(JsonParser.ONEOF_KEY)) {
				e = genJElement(parent, "map", map.getPosition());
				ee = genXDElement(e, "choice", getPosition());
				e.addChildNode(ee);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(ee, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else if (map.size() > 1) {
				e = genJElement(parent, "map", map.getPosition());
				ee = genXDElement(e, "mixed", map.getPosition());
				e.addChildNode(ee);
				if (!eos()) {
					setXDAttr(e, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else {
				e = genJElement(parent, "map", map.getPosition());
				ee = e;
				if (!eos()) {
					setXDAttr(e, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			}
		} else if (map.size() > 1) {
			e = genJElement(parent, "map", map.getPosition());
			ee = genXDElement(e, "mixed", map.getPosition());
			e.addChildNode(ee);
		} else {
			e = genJElement(parent, "map", map.getPosition());
			ee = e;
		}
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			String key = entry.getKey();
			val = entry.getValue();
			PNode ee2 = genJsonModel(val, ee);
			if (_xdNamespace.equals(ee2._nsURI)
				&& "choice".equals(ee2._localName)) {
				for (PNode n : ee2.getChildNodes()) {
					updateKeyInfo(n, key);
				}
			} else {
				updateKeyInfo(ee2, key);
			}
		}
		return e;
	}

	private PNode genJsonArray(final JArray array, final PNode parent) {
		PNode e = genJElement(parent, "array", array.getPosition());
		int index = 0;
		int len = array.size();
		if (len > 0) {
			Object jo = array.get(0);
			Object o = jo == null ? null
				: jo instanceof JValue ? ((JValue) jo).getValue() : jo;
			if (o != null && o instanceof JValue) {
				setSourceBuffer(((JValue) o).getSBuffer());
				isSpacesOrComments();
				if (isToken(JsonParser.ONEOF_KEY)) {
					e = genXDElement(parent,
						"choice", ((JValue) jo).getPosition());
					skipSemiconsBlanksAndComments();
					String s = getUnparsedBufferPart().trim();
					if (!s.isEmpty()) {
						setXDAttr(parent, "script",
							new SBuffer(s + ';', ((JValue) jo).getSBuffer()));
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
				PNode ee = genJsonModel(array.get(index), e);
				PAttr val;
				// if it is not the last and it has xd:script attribute where
				// the min occurrence differs from max occurrence
				// and it has the attribute with a value description
				if (JsonNames.J_ITEM.equals(ee._localName)
					&& XDConstants.JSON_NS_URI_W3C.equals(ee._nsURI)
					&& (val = getAttr(ee, JsonNames.J_VALUEATTR)) != null) {
					PAttr script = getXDAttr(ee, "script");
					XOccurrence occ = null;
					if (script != null) {
						SBuffer[] sbs = parseTypeDeclaration(script.getValue());
						occ = readOccurrence(sbs[0]);
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
								+ JsonNames.J_VALUEATTR + ").matches()");
					}
				}
			}
		}
		return e;
	}

	private PNode genJsonValue(final JValue jo, final PNode parent) {
		SBuffer sbf, occ = null;
		PNode e = genJElement(parent, JsonNames.J_ITEM, jo.getPosition());
		if (jo.getValue() == null) {
			sbf = new SBuffer("jnull()");
		} else {
			if (jo.toString().trim().isEmpty()) {
				sbf = new SBuffer("jvalue()", jo.getPosition()); // => any value
				occ = new SBuffer("?", jo.getPosition());
			} else {
				SBuffer[] parsedScript = parseTypeDeclaration(jo.getSBuffer());
				if (!parsedScript[0].getString().isEmpty()) { // occurrence
					occ = parsedScript[0];
				}
				if (eos()) {
					parsedScript[1] = new SBuffer("jvalue()", jo.getPosition());
				}
				sbf = parsedScript[1];
			}
			if (occ != null) { // occurrence
				setXDAttr(e, "script", occ);
			}
			setAttr(e, JsonNames.J_VALUEATTR, sbf);
		}
		return e;
	}

	/** Create PNode with JSON model from JSON parsed data.
	 * @param json JSON parsed data.
	 * @param parent parent PNode,
	 * @return created PNode.
	 */
	private PNode genJsonModel(final Object json, final PNode parent) {
		// set fields _jsprefix and _jsNamespace
		String s = XDConstants.JSON_NS_PREFIX; // default namespace prefix
		for (int i = 1; ;i++) {
			Integer x;
			if ((x = parent._nsPrefixes.get(s)) == null) {
				parent._nsPrefixes.put(s, XPreCompiler.NS_JSON_INDEX);
				break;
			} else if (x.equals(XPreCompiler.NS_JSON_INDEX)) {
				break; // prefix is already set
			} else { // the prefix is already used
				s = XDConstants.JSON_NS_PREFIX + i; // change prefix
			}
		}
		PNode e;
		if (json instanceof JMap) {
			e = genJsonMap((JMap) json, parent);
		} else if (json instanceof JArray) {
			e = genJsonArray((JArray) json, parent);
		} else if (json instanceof JValue
			&& ((JValue) json).getValue() instanceof String) {
			e = genJsonValue((JValue) json, parent);
		} else {
			error(JSON.JSON011); //Not JSON object&{0}
			return parent;
		}
		parent.addChildNode(e);
		return e;
	}

	/** Create X-definition model from PNode with JSON description.
	 * @param p PNode with JSON script.
	 * @param jsonMode version of transformation JSON to XML).
	 * @param name name of json model in X-definition.
	 * @param reporter report writer
	 */
	static final void genXdef(final PNode p,
		final byte jsonMode,
		final SBuffer name,
		final ReportWriter reporter) {
		if (jsonMode != XConstants.JSON_MODE_W3C) {
			//Internal error&{0}{: }
			throw new SRuntimeException(SYS.SYS066, "Namespace W3C expected");
		}
		CompileJsonXdef jx = new CompileJsonXdef();
		jx._xdNamespace = p._nsURI;
		jx._xdPrefix = p.getPrefix();
		jx._xdIndex = p._nsPrefixes.get(jx._xdPrefix);
		p._name = name;
		p._nsURI = null; // set no namespace
		p._nsindex = -1;
		jx.setXdefMode();
		jx.setReportWriter(reporter);
		if (p._value == null) {
			jx.setSourceBuffer(p._name);
			jx.error(JSON.JSON011); //Not JSON object&{0}
			return;
		}
		jx.setSourceBuffer(p._value);
		jx._basePos = p._xpathPos + "/text()";
		Object json = jx.parse(); //////
		if (json != null && (json instanceof JMap
			|| json instanceof JArray
			|| json instanceof JValue
			|| (json instanceof JValue
				&& ((JValue) json).getValue() instanceof String))) {
			jx.genJsonModel(json, p);
		} else {
			jx.error(JSON.JSON011); //Not JSON object&{0}
		}
		p._value = null;
//System.out.println(org.xdef.xml.KXmlUtils.nodeToString(p.toXML(),true));
	}

}