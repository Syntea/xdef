package org.xdef.impl.compile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.json.JsonToXml;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;

/** Create X-definition model from xd:json.
 * @author Vaclav Trojan
 */
public class CompileJsonXdef extends JsonToXml {

	/** This is the special character used for the $script specification. */
	public static final String SCRIPT_KEY = "]";
	/** This is the special character used for the $oneOf specification. */
	public static final String ONEOF_KEY = ")";
	/** This keyword used for the $script specification in X-definition. */
	public static final String SCRIPT_NAME = "$script";
	/** This keyword used for the $oneOf specification in X-definition. */
	public static final String ONEOF_NAME = "$oneOf";
	/** This keyword used for $any specification in X-definition. */
	public static final String ANY_NAME = "$any";
	/** This is the special character used for the $any specification. */
	public static final String ANY_KEY = ")";
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
	final PAttr setAttr(final PNode e,
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
	final PAttr getAttr(final PNode e, final String name) {
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
	final PAttr getXDAttr(final PNode e, final String name) {
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
//
//	private void addToXDScript(final PNode e, final String s) {
//		PAttr attr = getXDAttr(e, "script");
//		SBuffer val;
//		if (attr != null) {
//			val = attr._value;
//		} else {
//			attr = setXDAttr(e, "script", val = new SBuffer("", e._name));
//		}
//		if (!val.getString().trim().isEmpty()) {
//			if (!val.getString().trim().endsWith(";")) {
//				val.addString(";");
//			}
//			val.addString(" ");
//		}
//		val.addString(s);
//	}

	/** Set X-def attribute.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @param val SBuffer with value of attribute.
	 * @return created PAttr.
	 */
	final PAttr setXDAttr(final PNode e,
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
	final boolean skipSemiconsBlanksAndComments() {
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
	final XOccurrence readOccurrence(final SBuffer sbuf) {
		setSourceBuffer(sbuf);
		return readOccurrence();
	}

	/** Read occurrence.
	 * Occurrence ::= ("required" | "optional" | "ignore" | "illegal" | "*"
	 *   | "+" | "?" | (("occurs" S)? ("*" | "+" | "?"
	 *   | (IntegerLiteral (S? ".." (S? ("*" | IntegerLiteral))? )? ))))
	 * @return Occurrence object or null.
	 */
	final XOccurrence readOccurrence() {
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
					XOccurrence.IGNORE,Integer.MAX_VALUE); // ignore
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
	final SBuffer[] parseTypeDeclaration(final SBuffer sbuf) {
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
	final SBuffer[] parseTypeDeclaration() {
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
	final PNode genPElement(final PNode parent,
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
		result._xpathPos = _basePos;
		return result;
	}

	/** Create PNode as JSON element with given position,
	 * @param parent parent PNode.
	 * @param name local name of JSON element.
	 * @param spos source position
	 * @return created PNode,
	 */
	final PNode genJElement(final PNode parent,
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
	final PNode genXDElement(final PNode parent,
		final String name,
		final SPosition spos) {
		return genPElement(parent, _xdNamespace, _xdPrefix + ":" + name, spos);
//System.out.println(org.xdef.xml.KXmlUtils.nodeToString(p._parent.toXML(),true));
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
		if (jsonMode == XConstants.JSON_MODE_W3C) {
			CompileJsonXdefW3C.genXdefW3C(p, jsonMode, name, reporter);
		} else {
			CompileJsonXdefXD.genXdefXD(p, jsonMode, name, reporter);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used when JSON is parsed from X-definition compiler.
////////////////////////////////////////////////////////////////////////////////

	public static class JMap extends LinkedHashMap<String, Object>{
		private final SPosition _position; // SPosition of parsed object
		public JMap(final SPosition position) {super(); _position = position;}
		SPosition getPosition() {return _position;}
	}

	public static class JArray extends ArrayList<Object> {
		private final SPosition _position; // SPosition of parsed object
		public JArray(final SPosition position) {super(); _position = position;}
		SPosition getPosition() {return _position;}
	}

	public static class JValue {
		private final SPosition _position; // SPosition of parsed object
		private final Object _o; // parsed object
		public JValue(final SPosition position, final Object val) {
			_position = position;
			_o = val;
		}
		public Object getValue() {return _o;}
		public SPosition getPosition() {return _position;}
		private String getString() {return _o == null ? "null" : _o.toString();}
		public SBuffer getSBuffer(){return new SBuffer(getString(),_position);}
		@Override
		public String toString() {return _o == null ? "null" : _o.toString();}
	}

	public static class JScript {
		private final SPosition _position; // SPosition of parsed object
		private final SBuffer _val; // parsed object
		public JScript(final SPosition position, final JValue val) {
			_position = position;
			_val = val != null ? val.getSBuffer() : null;
		}
		public SBuffer getSBuffer() {return _val;}
		public SPosition getPosition() {return _position;}
		@Override
		public String toString() {return _val==null ? "null" : _val.toString();}
	}

	public static class JOneOf {
		private final SPosition _position; // SPosition of parsed object
		private final SBuffer _val; // parsed object
		public JOneOf(final SPosition position, final JValue val) {
			_position = position;
			_val = val != null ? val.getSBuffer() : null;
		}
		public SBuffer getSBuffer() {return _val;}
		public SPosition getPosition() {return _position;}
		@Override
		public String toString() {return _val==null ? "null" : _val.toString();}
	}

	public static class JAny {
		private final SPosition _position; // SPosition of parsed object
		private final SBuffer _val; // parsed object
		public JAny(final SPosition position, final SBuffer val) {
			_position = position;
			_val = val != null ? val : null;
		}
		public SBuffer getSBuffer() {return _val;}
		public SPosition getPosition() {return _position;}
		@Override
		public String toString() {return _val==null ? "null" : _val.toString();}
	}
}