package org.xdef.impl.compile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.json.JsonToXml;
import org.xdef.json.JsonUtil;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Create items from xd:json to X-definition.
 * @author Vaclav Trojan
 */
public class XJson extends JsonToXml {

	/** This is the special character used for the X-script specification. */
	public static final String SCRIPT_KEY = "]";

	/** This is the special character used for the xd:choice specification. */
	public static final String ONEOF_KEY = ")";

	/** This keyword used for the X-script specification. */
	public static final String SCRIPT_NAME = "$script";

	/** This keyword used for the xd:choice specification. */
	public static final String ONEOF_NAME = "$oneOf";

	/** Prepare instance of XJSON. */
	private XJson() {super();}

	/** Set attribute to PNode.
	 * @param e PNode where to set an attribute.
	 * @param name name of attribute.
	 * @param val SBuffer with the value of attribute.
	 * @return created PAttr.
	 */
	private PAttr setAttr(final PNode e,
		final String name,
		final SBuffer val) {
		PAttr a = new PAttr(name, val, null, -1);
		for (PAttr att: e._attrs) {
			if (att.getName().equals(name)) {
				e._attrs.remove(att);
				break;
			}
		}
		a._localName = name;
		e._attrs.add(a);
		return a;
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
			for (PAttr att: e._attrs) {
				if (att._nsindex == nsindex && name.equals(att._localName)) {
					return att;
				}
			}
		}
		return null;
	}

	private void addToXDScript(final PNode e, final String s) {
		PAttr attr = getXDAttr(e, "script");
		SBuffer val;
		if (attr != null) {
			val = attr._value;
		} else {
			attr = setXDAttr(e, "script", val = new SBuffer("", e._name));
		}
		if (!val.getString().trim().isEmpty()) {
			if (!val.getString().trim().endsWith(";")) {
				val.addString(";");
			}
			val.addString(" ");
		}
		val.addString(s);
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
		PAttr a = new PAttr(_xdPrefix + ":" + name, val, _xdNamespace, nsindex);
		for (PAttr att: e._attrs) {
			if (att._nsindex == nsindex && name.equals(att._localName)) {
				e._attrs.remove(att);
				break; // attribute will be replaced
			}
		}
		a._localName = name;
		e._attrs.add(a);
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

	/** Parse X-script and return occurrence and executive part in separate
	 * fields.
	 * @param sbuf JValue from which is used the value
	 * @return array with SBuffer items from both parts.
	 */
	private SBuffer[] parseOccurrence(final SBuffer sbuf) {
		if (sbuf != null) {
			setSourceBuffer(sbuf);
			return parseOccurrence();
		}
		return new SBuffer[] {
			new SBuffer("", getPosition()), new SBuffer("", getPosition())};
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
			{"optional", "?", "*", "required", "ignore", "illegal"};
		switch (isOneOfTokens(tokens)) {
			case  0:
			case  1:
				return new XOccurrence(0, 1); // optional
			case  2:
				return new XOccurrence(1, Integer.MAX_VALUE); // unbounded
			case  3:
				return new XOccurrence(1, 1); // required
			case  4:
				return new XOccurrence(
					XOccurrence.IGNORE,Integer.MAX_VALUE); // ignore
			case  5:
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
				error(XDEF.XDEF429);
			}
			return null;
		}
	}

	/** Parse X-script and return occurrence and executive part in separate
	 * fields.
	 * @return array os SBuffer with the occurrence part and remaining part.
	 */
	private SBuffer[] parseOccurrence() {
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
				nsindex = parent._nsPrefixes.size();
				result._nsPrefixes.put(_xdPrefix, nsindex);
			}
		} else {
			nsindex = -1;
			localName = name;
		}
		result._nsindex = nsindex;
		result._nsURI = nsURI;
		result._localName = localName;
		result._level = parent._level + 1;
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
		return genPElement(parent, _jsNamespace, _jsPrefix + ":" + name, spos);
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
// Create X-definition model from xd:json (W3C version)
////////////////////////////////////////////////////////////////////////////////

	/** Update key information to xd:script attribute.
	 * @param e PNode where to update.
	 * @param key value of key.
	 */
	private void updateKeyInfoW3C(final PNode e, final String key) {
		addToXDScript(e, "match @"+ J_KEYATTRW3C + "=='"+key+"';"
			+ (key.isEmpty()
				? " options preserveEmptyAttributes,noTrimAttr;" : ""));
		setAttr(e, J_KEYATTRW3C, new SBuffer(
			"string(%minLength=0,%whiteSpace='preserve');options noTrimAttr;",
				e._name));
	}

	private PNode genJsonMapW3C(final JMap map, final PNode parent) {
		PNode e, ee;
		Object val = map.get(SCRIPT_KEY);
		if (val != null) {
			map.remove(SCRIPT_KEY);
			JValue jv =(JValue) val;
			setSourceBuffer(jv.getSBuffer());
			isSpacesOrComments();
			if (isToken(ONEOF_KEY)) {
				e = genJElement(parent, "map", map.getPosition());
				ee = e;
				ee = genXDElement(e, "choice", getPosition());
				e._childNodes.add(ee);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(ee, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else if (map.size() > 1) {
				e = genJElement(parent, "map", map.getPosition());
				ee = e;
				ee = genXDElement(e, "sequence", map.getPosition());
				e._childNodes.add(ee);
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
//			if (!eos()) {
//				setXDAttr(e, "script",
//					new SBuffer(getUnparsedBufferPart(), getPosition()));
//			}
			ee = genXDElement(e, "mixed", map.getPosition());
			e._childNodes.add(ee);
		} else {
			e = genJElement(parent, "map", map.getPosition());
			ee = e;
//			if (!eos()) {
//				setXDAttr(e, "script",
//					new SBuffer(getUnparsedBufferPart(), getPosition()));
//			}
		}
		for (Map.Entry<String, Object> entry: map.entrySet()) {
			String key = entry.getKey();
			val = entry.getValue();
			PNode ee2 = genJsonModelW3C(val, ee);
			if (_xdNamespace.equals(ee2._nsURI)
				&& "choice".equals(ee2._localName)) {
				for (PNode n : ee2._childNodes) {
					updateKeyInfoW3C(n, key);
				}
			} else {
				updateKeyInfoW3C(ee2, key);
			}
		}
		return e;
	}

	private PNode genJsonListW3C(final JList array, final PNode parent) {
		PNode e = genJElement(parent, "array", array.getPosition());
		Iterator<Object> it = array.iterator();
		if (it.hasNext()) {
			Object jo = it.next();
			Object o = jo == null ? null
				: jo instanceof JValue ? ((JValue) jo).getObject() : jo;
			if (o != null && o instanceof JValue) {
				setSourceBuffer(((JValue) o).getSBuffer());
				isSpacesOrComments();
				if (isToken(ONEOF_KEY)) {
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
			} else {
				genJsonModelW3C(jo, e);
			}
			while(it.hasNext()) {
				genJsonModelW3C(it.next(), e);
			}
		}
		return e;
	}

	private PNode genJsonValueW3C(final JValue jo, final PNode parent) {
		PNode e;
		SBuffer sbf = null, occ = null;
		String itemName;
		if (jo.getObject() != null) {
			String s = jo.toString();
			if (s.trim().isEmpty()) {
				// set default required string()
				sbf = new SBuffer("jvalue()", jo.getPosition());
				occ = new SBuffer("?", jo.getPosition());
				itemName = J_ITEM;
			} else {
				SBuffer[] parsedScript = parseOccurrence(jo.getSBuffer());
				if (!parsedScript[0].getString().isEmpty()) { // occurrence
					occ = parsedScript[0];
				}
				if (eos()) {
					parsedScript[1] = new SBuffer("jvalue()",
						jo.getPosition());
					itemName = J_ITEM; // default
				} else if (isToken("jnull") && isLetter()==NOCHAR) {
					itemName = J_NULL;
				} else {
					if ((isToken("jvalue")) && isLetter()==NOCHAR) {
						itemName = J_ITEM;
					} else if (isOneOfTokens(new String[] {"boolean",
						"jboolean"}) >= 0 && isLetter() == NOCHAR) {
						itemName = J_BOOLEAN;
					} else if (isOneOfTokens(new String[] {
						"unsignedLong","unsignedInt", "unsignedShort",
						"unsignedByte",
						"negativeInteger", "nonNegativeInteger",
						"positiveInteger", "nonPositiveInteger",
						"jnumber", "byte", "short", "int", "long",
						"float", "double", "decimal", "dec", "jnum"}) >= 0
						&& isLetter() == NOCHAR) {
						itemName = J_NUMBER;
					} else {
						itemName = J_STRING;
					}
					sbf = new SBuffer('?' + parsedScript[1].getString(),
						parsedScript[1]);
				}
			}
		} else {
			itemName = J_NULL;
		}
		if (J_ITEM.equals(itemName)) {
			e = genXDElement(parent, "choice", jo.getPosition());
			if (occ != null) { // occurrence
				setXDAttr(e, "script", occ);
			}
			PNode f = genJElement(e, J_NULL, jo.getPosition());
			e._childNodes.add(f);
			f = genJElement(e, J_BOOLEAN, jo.getPosition());
			PNode txt = genXDElement(e, "text", jo.getPosition());
			txt._value = new SBuffer("jboolean");
			f._childNodes.add(txt);
			e._childNodes.add(f);
			f = genJElement(e, J_NUMBER, jo.getPosition());
			txt = genXDElement(e, "text", jo.getPosition());
			txt._value = new SBuffer("jnumber");
			f._childNodes.add(txt);
			e._childNodes.add(f);
			f = genJElement(e, J_STRING, jo.getPosition());
			txt = genXDElement(e, "text", jo.getPosition());
			txt._value = new SBuffer("jstring");
			f._childNodes.add(txt);
			e._childNodes.add(f);
		} else {
			e = genJElement(parent, itemName, jo.getPosition());
			e._value = null;
			if (occ != null) { // occurrence
				setXDAttr(e, "script", occ);
			}
			if (sbf != null) {
				PNode txt = genXDElement(e, "text", jo.getPosition());
				txt._value = sbf;
				e._childNodes.add(txt);
			}
		}
		return e;
	}

	/** Create PNode with JSON model from JSON parsed data.
	 * @param json JSON parsed data.
	 * @param parent parent PNode,
	 * @return created PNode.
	 */
	private PNode genJsonModelW3C(final Object json, final PNode parent) {
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
		_jsPrefix = s;
		_jsNamespace = XDConstants.JSON_NS_URI_W3C;
		PNode e;
		if (json instanceof JMap) {
			e = genJsonMapW3C((JMap) json, parent);
		} else if (json instanceof JList) {
			e = genJsonListW3C((JList) json, parent);
		} else {
			e = genJsonValueW3C((JValue) json, parent);
		}
		parent._childNodes.add(e);
		return e;
	}

	/** Create X-definition model from PNode with JSON description.
	 * @param p PNode with JSON script.
	 * @param jsonMode version of transformation JSON to XML(W3C, X-definition).
	 * @param reporter report writer
	 */
	static final void genXdef(final PNode p,
		final byte jsonMode,
		final ReportWriter reporter) {
		XJson jx = new XJson();
		jx.setGenJObjects();
		try {
			Field field = JsonUtil.class.getDeclaredField("_jdef");
			field.setAccessible(true);
			field.setBoolean(jx, true);
		} catch (Exception ex) {ex.printStackTrace();}
		jx.setReportWriter(reporter);
		if (p._value == null) {
			jx.setSourceBuffer(p._name);
			jx.error(JSON.JSON011); //Not JSON object&{0}
			return;
		}
		jx.setSourceBuffer(p._value);
		Object json = jx.parse();
		if (json != null && (json instanceof JMap || json instanceof JList)) {
			jx.genJsonModelW3C(json, p);
		} else {
			jx.error(JSON.JSON011); //Not JSON object&{0}
		}
		p._value = null;
//System.out.println(org.xdef.xml.KXmlUtils.nodeToString(p._parent.toXML(),true));
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used when JSON is parsed from X-definition compiler.
////////////////////////////////////////////////////////////////////////////////

	public static class JMap extends LinkedHashMap<String, Object>{
		private final SPosition _position; // SPosition of parsed object
		public JMap(final SPosition position) {super(); _position = position;}
		private SPosition getPosition() {return _position;}
	}

	public static class JList extends ArrayList<Object> {
		private final SPosition _position; // SPosition of parsed object
		public JList(final SPosition position) {super(); _position = position;}
		private SPosition getPosition() {return _position;}
	}

	public static class JValue {
		private final SPosition _position; // SPosition of parsed object
		private final Object _o; // parsed object
		public JValue(final SPosition position, final Object val) {
			_position = position;
			_o = val;
		}
		public  Object getObject() {return _o;}
		public SPosition getPosition() {return _position;}
		private String getString() {return _o == null ? "null" : _o.toString();}
		private SBuffer getSBuffer(){return new SBuffer(getString(),_position);}
		@Override
		public String toString() {return _o == null ? "null" : _o.toString();}
	}
}