package org.xdef.impl.compile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.json.JsonToXml;
import org.xdef.json.JsonUtil;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;

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


	/** Get X-def attribute.
	 * @param e PNode where to set attribute.
	 * @param name local name of attribute.
	 * @return X-def PAttr.
	 */
	private PAttr getXDAttr(final PNode e, final String name) {
		int nsindex;
		if (e._nsPrefixes.containsKey(_xdPrefix)) {
			nsindex = e._nsPrefixes.get(_xdPrefix);
		} else {
			nsindex = e._nsPrefixes.size();
			e._nsPrefixes.put(_xdPrefix, nsindex);
		}
		return e.getAttrNS(name, nsindex);
	}

	private SBuffer getScriptValue(final Object obj) {
		Object o;
		if (obj == null || !(obj instanceof JValue)
			|| (o = ((JValue) obj).getObject()) == null
			|| !(o instanceof JValue)) {
			return null;
		}
		return ((JValue) o).getSBuffer();
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
		if (isToken("occurs")) {
			if (!isSpacesOrComments()) {
				// here it is an error
			}
		}
		final String[] tokens =
			{"optional", "?", "*", "required", "ignore", "illegal"};
		int ndx = isOneOfTokens(tokens);
		if (ndx >= 0) {
			switch (ndx) {
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
				default:
					return new XOccurrence(XOccurrence.ILLEGAL, 0); // illegal
			}
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
			return null;
		}
	}

	/** Parse X-script and return occurrence and executive part in separate
	 * fields.
	 * @return array with SBuffer items from both parts.
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
		int nsindex;
		if (e._nsPrefixes.containsKey(_xdPrefix)) {
			nsindex = e._nsPrefixes.get(_xdPrefix);
		} else {
			nsindex = e._nsPrefixes.size();
			e._nsPrefixes.put(_xdPrefix, nsindex);
		}
		PAttr attr = e.getAttrNS("script", nsindex);
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
		val.addString("match @"+ J_KEYATTRW3C + "=='"+key+"';"
			+ (key.isEmpty()
				? " options preserveEmptyAttributes,noTrimAttr;" : ""));
		setAttr(e, J_KEYATTRW3C,
			new SBuffer(
			"string(%minLength=0,%whiteSpace='preserve');options noTrimAttr;",
				e._name));
	}

	private PNode genJsonMapW3C(final JMap map, final PNode parent) {
		PNode e = genJElement(parent, "map", map.getPosition());
		PNode ee = e;
		Object val = map.get(SCRIPT_KEY);
		if (val != null) {
			map.remove(SCRIPT_KEY);
			JValue jv =(JValue) val;
			setSourceBuffer(jv.getSBuffer());
			isSpacesOrComments();
			if (isToken(ONEOF_KEY)) {
				ee = genXDElement(e, "choice", getPosition());
				e._childNodes.add(ee);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(ee, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else if (map.size() > 1) {
				ee = genXDElement(e, "mixed", map.getPosition());
				e._childNodes.add(ee);
				if (!eos()) {
					setXDAttr(e, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			}
		} else if (map.size() > 1) {
			ee = genXDElement(e, "mixed", map.getPosition());
				e._childNodes.add(ee);
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
					skipSemiconsBlanksAndComments();
					e = genXDElement(parent,
						"choice", ((JValue) jo).getPosition());
				}
				String s = getUnparsedBufferPart().trim();
				if (!s.isEmpty()) {
					setXDAttr(e, "script",
						new SBuffer(s, ((JValue) jo).getSBuffer()));
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
		setPrefix(parent,
			XDConstants.JSON_NS_URI_W3C, "js", XPreCompiler.NS_JSON_W3C_INDEX);
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

////////////////////////////////////////////////////////////////////////////////
// Create X-definition model from xd:json (Xdef version)
////////////////////////////////////////////////////////////////////////////////

	/** Get PAttr from PNode.
	 * @param e PNode where this attribute can be.
	 * @param namespace namespace URI of attribute.
	 * @param localname local name of attribute.
	 * @return PAttr or null;
	 */
	private PAttr getAttr(final PNode e,
		final String namespace,
		final String localname) {
		for (PAttr att: e._attrs) {
			if (localname.equals(att._localName) &&
				(att._nsURI == null && namespace == null
				|| att._nsURI != null && att._nsURI.equals(namespace))) {
				return att;
			}
		}
		return null;
	}

	/** Add named value to the parent node.
	 * @param rawName JSON name.
	 * @param val value of named value.
	 * @param parent the node where to to add named value.
	 * @return created node.
	 */
	private PNode namedItemToXD(final String rawName,
		final Object val,
		final PNode parent) {
		String name = toXmlName(rawName);
		String nsURI = null;
		PNode e;
		if (val instanceof JMap) {
			JMap map = (JMap) val;
			if (map.isEmpty()) {
				e = genJElement(parent, J_MAP, map.getPosition());
				parent._childNodes.add(e);
				return e;
			}
			String prefix = getNamePrefix(name);
			String uri = (String) map.get(
				prefix.length() > 0 ? "xmlns:" + prefix : "xmlns");
			JMap attrs = new JMap(map.getPosition());
			JMap items = new JMap(map.getPosition());
			boolean choice = false;
			String simpleChoiceName = null;
			SBuffer xscript = null;
			SPosition choicePosition = null;
			for (String key: map.keySet()) {
				Object o = map.get(key);
				if (o instanceof JValue && SCRIPT_KEY.equals(key)) {
					setSourceBuffer(((JValue) o).getSBuffer());
					isSpacesOrComments();
					choicePosition = getPosition();
					if (isToken(ONEOF_KEY)) {
						choice = true;
						skipSemiconsBlanksAndComments();
						if (!eos()) {
							xscript = new SBuffer(getUnparsedBufferPart(),
								getPosition());
						}
					} else {
						choicePosition = null;
					}
					continue;
				}
				if (o == null) {
					attrs.put(key, "jnull()");
				} else if (isSimpleValue(o)) {
					if (!"xmlns".equals(key) && !key.startsWith("xmlns:")
						|| !StringParser.chkXMLName(key, (byte) 10)) {
						if (choice) {
							simpleChoiceName = key;
						}
					}
					attrs.put(key, o);
				} else {
					items.put(toXmlName(key), o);
				}
			}
			PNode ee;
			if (choice) {
				if (items.size() > 1) {
					e = genXDElement(parent, "choice", choicePosition);
					parent._childNodes.add(e);
					if (!xscript.getString().trim().isEmpty()) {
						setXDAttr(e, "script", xscript);
					}
				} else {
					e = parent;
				}
			} else {
				e = parent;
			}
			if (items.isEmpty()) {
				ee = genPElement(e, uri, name, null);
				e._childNodes.add(ee);
				setAttrsFromMapXD(ee, attrs);
			} else {
				Iterator<Entry<String,Object>> it = items.entrySet().iterator();
				ee = genPElement(e, uri, name, null);
				e._childNodes.add(ee);
				if (choice) {
					if (simpleChoiceName != null) {
						JValue x = (JValue) attrs.get(simpleChoiceName);
						setAttr(ee, simpleChoiceName,
							new SBuffer('?' + x.getString(), x.getPosition()));
						attrs.remove(simpleChoiceName);
					}
					if (xscript != null) {
						PAttr att = getAttr(ee, _xdNamespace, "script");
						if (att != null) {
							if (!att._value.getString().trim().isEmpty()
								&&!att._value.getString().trim().endsWith(":")){
								att._value. addString("; ");
							}
							att._value.addString(xscript.getString()); //s
						} else {
							setXDAttr(ee, "script", xscript);
						}
					}
				}
				setAttrsFromMapXD(ee, attrs);
				if (items.size() == 1) {
					Entry<String, Object> entry = it.next();
					String n = entry.getKey();
					Object o = entry.getValue();
					PNode eee = namedItemToXD(n, o, ee);
					if (choice) {
						PAttr att = getAttr(eee, _xdNamespace, "script");
						if (att != null) {
							att._value.addString(";optional");
						} else {
							setXDAttr(eee, "script",
								new SBuffer("?", eee.getName()));//pos
						}
					}
				} else {
					PNode eee = genJElement(ee, J_MAP, map.getPosition());
					ee._childNodes.add(eee);
					while (it.hasNext()) {
						Entry<String, Object> entry = it.next();
						String n = entry.getKey();
						Object o = entry.getValue();
						namedItemToXD(n, o, eee);
					}
				}
			}
			return ee;
		} else if (val instanceof JList) {
			JList list = (JList) val;
			if (!list.isEmpty() && list.size() > 1
				&& list.get(0) instanceof JValue) {
				SBuffer sbf = getScriptValue((JValue) list.get(0));
				if (sbf != null){
					setSourceBuffer(sbf);
					isSpacesOrComments();
					if (isToken(ONEOF_KEY)) {
						PNode ee =
							genPElement(parent, nsURI, name,list.getPosition());
						parent._childNodes.add(ee);
						skipSemiconsBlanksAndComments();
						if (!eos()) {
							setXDAttr(ee, "script",
								new SBuffer(getUnparsedBufferPart(),
									getPosition()));
						}
						e = genXDElement(ee, "choice", sbf);
						ee._childNodes.add(e);
						ee = e;
						// all item names must be unique
						HashSet<String> set = new HashSet<String>();
						for (int i = 1; i < list.size(); i++) {
							Object o = list.get(i);
							list.remove(i--);
							boolean added = false;
							if (o instanceof JValue) {
								e = genXDElement(parent, "text", sbf);
								e._value = ((JValue) o).getSBuffer();
								if (added = set.add("\0value")) {
									ee._childNodes.add(e);
								}
							} else if (o instanceof JMap) {
								e = mapToXD((JMap) o,  ee);
								added = set.add(e.getName().getString());
							} else {
								e = listToXD((JList) o,  ee);
								added = set.add(e.getName().getString());
							}
							if (!added) {
								//Only one JSON value is allowed
								error(XDEF.XDEF311);
							}
						}
						return ee;
					}
				}
			}
			e = genPElement(parent, nsURI, name, list.getPosition());
			parent._childNodes.add(e);
			listToXD(list, e);
			return e;
		} else {
			JValue jv = (JValue) val;
			SBuffer[] parsedScript = parseOccurrence(jv.getSBuffer());
			e = genPElement(parent, nsURI, name, jv.getPosition());
			parent._childNodes.add(e);
			isSpacesOrComments();
			if (!parsedScript[0].getString().trim().isEmpty()) { // occurrence
				setXDAttr(e, "script", parsedScript[0]);
			}
			String s;
			if ((s = parsedScript[1].getString().trim()).isEmpty()) {
				parsedScript[1].addString("jvalue();");
			} else if (!s.endsWith(";")) {
				parsedScript[1].addString(";");
			}
			PNode ee = genXDElement(e, "text", parsedScript[1]);
			e._childNodes.add(ee);
			ee._value = new SBuffer('?' + s, parsedScript[1]);
			return e;
		}
	}

	private void setAttrFromJValuetXD(final PNode e,
		final String key,
		final JValue val) {
		if (SCRIPT_KEY.equals(key) && val.getObject() != null
			&& val.getObject() instanceof String) {
			setXDAttr(e, "script", val.getSBuffer());
		} else {
			if (isSimpleValue(val) &&
				!"xmlns".equals(key) && !key.startsWith("xmlns:")
				|| !StringParser.chkXMLName(key, (byte) 10)) {
				setAttr(e, toXmlName(key), val.getSBuffer());
			}
		}
	}

	/** Set attributes to PNode created form simple named values of JMap,
	 * @param e PNode where to add attributes.
	 * @param map inspected map.
	 */
	private void setAttrsFromMapXD(final PNode e, final JMap map) {
		ArrayList<String> notAttrNames = new ArrayList<String>();
		for (String key: map.keySet()) {
			Object o = map.get(key);
			if (o instanceof JValue) {
				setAttrFromJValuetXD(e, key, (JValue) o);
			} else if (o instanceof JList) {
				for (Object p : (JList) o) {
					// find just string
					boolean wasString = false;
					if (p != null && p instanceof JValue) {
						JValue jv = (JValue) p;
						if (((JValue) p).getObject() instanceof String) {
							SBuffer[] parsed = parseOccurrence(jv.getSBuffer());
							jv = new JValue(parsed[0],
								"? " + parsed[1].getString());
							if (wasString) {
error("XXXX", key + "=" + jv.getString()); //TODO ?????
							} else {
								setAttrFromJValuetXD(e, key, jv);
								wasString = true;
							}
						}
					}
				}
			} else {
				notAttrNames.add(key);
			}
		}
		for (String key: notAttrNames) {
			map.remove(key);
		}
	}

	/** Create JMap with items from JMAP which are map or array.
	 * @param map the inspected map.
	 * @return e JMap with not simple items.
	 */
	private JMap getNotSimpleValuesXD(final JMap map) {
		JMap notSimpleItems = new JMap(map.getPosition());
		for (String key: map.keySet()) {
			Object o = map.get(key);
			if (!isSimpleValue(o)) {
				notSimpleItems.put(key, o);
			}
		}
		return notSimpleItems;
	}

	private SBuffer setXDScriptOptional(final SBuffer sb) {
		SBuffer[] sbfs = parseOccurrence(sb);
		if (!sbfs[1].getString().isEmpty()) {
			return new SBuffer("? " + sbfs[1].getString(), sbfs[1]);
		} else {
			return new SBuffer("?", sb);
		}
	}

	private void createMapItemXD(final JMap map,
		final JMap notSimpleItems,
		final String key,
		final PNode e) {
		Object item = notSimpleItems.get(key);
		PNode ee = namedItemToXD(key, item, e);
		JList jl = null;
		if (item instanceof JList) {
			jl = (JList) item;
			if (!isSimpleValue(jl.get(0))) {
				SBuffer sbf = getScriptValue(jl.get(0));
				if (sbf == null) {
					return;
				} else {
					if (!map.containsKey(key)) {
						return;
					}
				}
			}
		}
		if (jl == null) {
			return;
		}
		for (int i = 0; i < ee._childNodes.size(); i++) {
			PNode x = ee._childNodes.get(i);
			if (x._name.getString().equals(_xdPrefix + ":text")) {
				ee._childNodes.remove(i);
				PAttr pa = getXDAttr(ee._parent, "script");
				if (pa == null) {
					setXDAttr(ee._parent, "script", new SBuffer("?"));
				} else {
					SBuffer sbf = setXDScriptOptional(pa._value);
					setXDAttr(ee._parent, "script", sbf);
				}
				break;
			}
		}
		for (int i = 0; i < jl.size(); i++) {
			Object o = jl.get(i);
			if (!isSimpleValue(o)) {
				continue;
			}
			for (PAttr x: e.getAttrs()) {
				if (key.equals(x._name)) {
					SBuffer sbf = setXDScriptOptional(((JValue)o).getSBuffer());
					setAttr(e, key, sbf);
					for (PNode y: e.getChildNodes()) {
						if (key.equals(y.getName().getString())) {
							PAttr pa = getXDAttr(y, "script");
							if (pa == null) {
								setXDAttr(y, "script", new SBuffer("?"));
							} else {
								sbf = setXDScriptOptional(pa._value);
								setXDAttr(y, "script", sbf);
							}
							String s = "match !@" + key + "; ?";
							pa = getXDAttr(y, key);
							if (pa != null) {
							   pa._value.addString(s);
							} else {
								sbf = setXDScriptOptional(pa._value);
								setXDAttr(y, key, sbf);
							}
								return;
						}
					}
					break;
				}
			}
		}
	}

	/** Append map with JSON tuples to node.
	 * @param map map with JSON tuples.
	 * @param parent node where to append map.
	 * @return created element.
	 */
	private PNode mapToXD(final JMap map, final PNode parent) {
		PNode ee = parent;
		PNode e;
		Object o;
		SBuffer xscript = null;
		if ((o = map.get(SCRIPT_KEY)) != null
			&& o instanceof JValue) {
			JValue jx = (JValue) o;
			if ((o = jx.getObject()) != null && o instanceof String) {
				setSourceBuffer(jx.getSBuffer());
				isSpacesOrComments();
				map.remove(SCRIPT_KEY);
				if (isToken(ONEOF_KEY)) {
					SBuffer[] parsedScript = parseOccurrence();
					ee = genXDElement(parent, "choice", jx.getPosition());
					parent._childNodes.add(ee);
					if (!eos()) {
						setXDAttr(ee, "script",
							new SBuffer(getUnparsedBufferPart(),getPosition()));
					}
					for (String key: map.keySet()) {
						e = namedItemToXD(key, map.get(key), ee);
					}
					if (!parsedScript[0].getString().isEmpty()) {
						setSourceBuffer(parsedScript[0]);
						XOccurrence occ = readOccurrence();
						if (occ != null && occ.minOccurs() == 0) {
							e = genJElement(ee, J_MAP, map.getPosition());
							ee._childNodes.add(e);
						}
					}
					return ee;
				}
				if (!eos()) {
					xscript = jx.getSBuffer();
					ee = genXDElement(parent, "sequence", jx.getPosition());
					parent._childNodes.add(ee);
					setXDAttr(ee, "script", xscript);
					if (map.size() == 1) {
						String key = map.keySet().iterator().next();
						e = namedItemToXD(key, map.get(key), ee);
						ee._childNodes.add(e);
						return ee;
					} else {
						e = genJElement(ee, J_MAP, map.getPosition());
						ee._childNodes.add(e);
						JMap notSimpleItems = getNotSimpleValuesXD(map);
						setAttrsFromMapXD(e, map);
						for (String key : notSimpleItems.keySet()) {
							createMapItemXD(map, notSimpleItems,key, e);
						}
					}
					return e;
				}
			}
		}
		if (map.size() == 1) {
			String key = map.keySet().iterator().next();
			return namedItemToXD(key, map.get(key), ee);
		} else {
			e = genJElement(ee, J_MAP, map.getPosition());
			ee._childNodes.add(e);
			if (xscript != null) {
				setXDAttr(e, "script", xscript);
			}
			JMap notSimpleItems = getNotSimpleValuesXD(map);
			setAttrsFromMapXD(e, map);
			if (!notSimpleItems.isEmpty()) {
				for (String key: notSimpleItems.keySet()) {
					namedItemToXD(key, notSimpleItems.get(key), e);
				}
			}
			return e;
		}
	}

	/** Add an item from JSON list.
	 * @param x item from JSON list.
	 * @param e PNode with SON list.
	 */
	private void listItemToXD(final Object x, final PNode e) {
		PNode ee = null;
		if (x instanceof JMap) {
			mapToXD((JMap) x, e);
		} else if (x instanceof JList) {
			listToXD((JList)x,  e);
		} else { // JValue
			JValue jv = (JValue) x;
			if (!isSimpleValue(jv)) {
				setXDAttr(e, "script", getScriptValue(jv));
			} else {
				SBuffer[] parsedScript = parseOccurrence(jv.getSBuffer());
				ee = genJElement(e, J_ITEM, parsedScript[1]);
				e._childNodes.add(ee);
				PNode eee = genXDElement(ee, "text", parsedScript[1]);
				eee._value = new SBuffer(
					'?' + parsedScript[1].getString(), parsedScript[1]);
				ee._childNodes.add(eee);
				if (!parsedScript[0].getString().isEmpty()) {
					setXDAttr(ee, "script", parsedScript[0]);
				}
			}
		}
	}

	/** Append array of JSON values to node.
	 * @param array JSON array of values.
	 * @param parent node where to append array.
	 */
	private PNode listToXD(final JList array, final PNode parent) {
		int i = 0;
		SBuffer xscript = null;
		PNode e = null;
		if (!array.isEmpty() && array.get(0) instanceof JValue) {
			SBuffer sbf = getScriptValue((JValue) array.get(0));
			if (sbf != null) { // xd:script item
				setSourceBuffer(sbf);
				i = 1;
				isSpacesOrComments();
				if (isToken(ONEOF_KEY)) {
					e = genXDElement(parent, "choice", getPosition());
					parent._childNodes.add(e);
					skipSemiconsBlanksAndComments();
					if (!eos()) {
						xscript = new SBuffer(getUnparsedBufferPart(),
							getPosition());
					}
					for (; i < array.size(); i++) {
						listItemToXD(array.get(i), e);
					}
					return e;
				}
				xscript = sbf;
			}
		}
		if (e == null) {
			e = genJElement(parent, J_ARRAY, null);
			parent._childNodes.add(e);
		}
		if (xscript != null && !xscript.getString().trim().isEmpty()) {
			setXDAttr(e, "script", xscript);
		}
		for (; i < array.size(); i++) {
			listItemToXD(array.get(i), e);
		}
		return e;
	}

	/** Create child nodes with JSON object to node.
	 * @param json JSON object.
	 * @param parent where to create child nodes.
	 */
	private PNode genJsonModelXD(final Object json, final PNode parent) {
		setPrefix(parent,
			XDConstants.JSON_NS_URI, "js", XPreCompiler.NS_JSON_INDEX);
		if (json instanceof JMap) {
			return mapToXD((JMap) json, parent);
		} else {
			return listToXD((JList) json, parent);
		}
	}

////////////////////////////////////////////////////////////////////////////////

	private void setPrefix(final PNode p, String nsURI,
		String prefix, Integer nsindex) {
		String s = prefix;
		for (int i = 1;; i++) {
			Integer x;
			if ((x=p._nsPrefixes.get(s)) == null) {
				p._nsPrefixes.put(s, nsindex);
				break;
			} else if (x.equals(nsindex)) {
				break;
			} else {
				s = prefix + i;
			}
		}
		_jsPrefix = s;
		_jsNamespace = nsURI;
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
			if (jsonMode == XConstants.JSON_W3C) {
				jx.genJsonModelW3C(json, p);
			} else if (jsonMode == XConstants.JSON_XD){
				jx.genJsonModelXD(json, p);
			} else {//never should happen!
				//Internal error&{0}{: }
				throw new SRuntimeException(SYS.SYS066, "Incorrect jsonMode");
			}
		} else {
			jx.error(JSON.JSON011); //Not JSON object&{0}
		}
		p._value = null;
//System.out.println(cz.syntea.xdef.xml.KXmlUtils.nodeToString(p._parent.toXML(),true));
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