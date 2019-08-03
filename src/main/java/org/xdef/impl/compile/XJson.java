package org.xdef.impl.compile;

import org.xdef.XDConstants;
import org.xdef.json.JsonToXml;
import org.xdef.msg.JSON;
import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SParser;
import org.xdef.sys.SPosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/** JSON/X-definition utility.
 * @author Vaclav Trojan
 */
public class XJson extends JsonToXml {

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

	/** Parse X-script and return occurrence and executive part in separate fields.
	 * @return array with SBuffer items from both parts.
	 */
	private SBuffer[] parseOccurrence() {
		skipBlanksAndComments();
		SBuffer[] result = new SBuffer[]{new SBuffer("", getPosition()),
			new SBuffer("", getPosition())};
		int pos = getIndex();
//Occurrence ::= ("required" | "optional" | "ignore" | "illegal" | "*"
//  | "+" | "?" | (("occurs" S)? ("*" | "+" | "?" |
//  (IntegerLiteral (S? ".." (S? ("*" | IntegerLiteral))? )? )))) $rule
		// parse occurrence
		SPosition spos = getPosition();
		String[] tokens = {"required","optional", "ignore","illegal"};
		int ndx = isOneOfTokens(tokens);
		if (ndx >= 0) {
			result[0] = new SBuffer(tokens[ndx], spos);
		} else {
			spos = getPosition();
			if (isToken("occurs")) {
				skipBlanksAndComments();
			}
			pos = getIndex();
			char ch = isOneOfChars("*+?");
			if (ch == SParser.NOCHAR) {
				skipBlanksAndComments();
				if (isInteger()) {
					skipBlanksAndComments();
					if (isToken("..")) {
						skipBlanksAndComments();
						if (!isInteger()) {
							isChar('*');
						}
					}
				}
			}
			result[0] = new SBuffer(getParsedBufferPartFrom(pos), spos);
			if (!result[0].getString().isEmpty()) {
				result[0].addString(";");
			}
			isChar(';');
			skipBlanksAndComments();
		}
		result[1] = new SBuffer(getUnparsedBufferPart(), getPosition());
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
		PNode result = new PNode(name,
			spos, parent, parent._xdVersion, parent._xmlVersion);
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
				"string(%minLength=0,%whiteSpace='preserve');options noTrimAttr;", e._name));
	}

	/** Create PNode with JSON model from JSON parsed data.
	 * @param json JSON parsed data.
	 * @param parent parent PNode,
	 * @return created PNode.
	 */
	private PNode genJsonModelW3C(final Object json, final PNode parent) {
		PNode e;
		if (json instanceof JMap) {
			JMap map = (JMap) json;
			e = genJElement(parent, "map", map.getPosition());
			PNode ee = e;
			Object val = map.get(_xdPrefix + ":script");
			if (val != null && val instanceof JValue
				&& ((JValue) val).getObject() != null) {
				JValue jv =(JValue) val;
				SBuffer s = jv.getSBuffer();
				setSourceBuffer(s);
				if (isToken(_xdPrefix + ':' + "choice")) {
					ee = genXDElement(e, "choice", getPosition());
					e._childNodes.add(ee);
					skipSpaces();
					if (isChar(';')) {
						skipSpaces();
						if (!eos()) {
							s = new SBuffer(
								getUnparsedBufferPart(), getPosition());
						}
					}
					if (!s.getString().trim().isEmpty()) {
						setXDAttr(ee, "script", s);
					}
				} else if (map.size() > 1) {
					ee = genXDElement(e, "mixed", map.getPosition());
					e._childNodes.add(ee);
					if (!s.getString().trim().isEmpty()) {
						setXDAttr(e, "script", s);
					}
				}
			} else if (map.size() > 1) {
				ee = genXDElement(e, "mixed", map.getPosition());
					e._childNodes.add(ee);
			}
			for (Map.Entry<String, Object> entry: map.entrySet()) {
				String key = entry.getKey();
				val = entry.getValue();
				if (key.equals(_xdPrefix + ":script")) {
					continue;
				}
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
		} else if (json instanceof JList) {
			JList array = (JList) json;
			e = genJElement(parent, "array", array.getPosition());
			Iterator<Object> it = array.iterator();
			if (it.hasNext()) {
				Object jo = it.next();
				Object o = jo == null ? null
					: jo instanceof JValue ? ((JValue) jo).getObject() : jo;
				if (o != null && o instanceof String) {
					String s = (String) o;
					setSourceBuffer(s);
					skipBlanksAndComments();
					if (isToken(_xdPrefix + ":script")) {
						skipBlanksAndComments();
						if (isChar('=')) {
							skipBlanksAndComments();
							if (isToken(_xdPrefix + ":choice")) {
								skipBlanksAndComments();
								if (isChar(';')) {
									skipBlanksAndComments();
								}
								e = genXDElement(parent,
									"choice", ((JValue) jo).getPosition());
							}
							s = getUnparsedBufferPart().trim();
							if (!s.isEmpty()) {
								setXDAttr(e, "script",
									new SBuffer(s, ((JValue) jo).getSBuffer()));
							}
						} else {
							//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
							error(JSON.JSON002, "=");
						}
					} else {
						genJsonModelW3C(jo, e);
					}
				} else {
					genJsonModelW3C(jo, e);
				}
				while(it.hasNext()) {
					genJsonModelW3C(it.next(), e);
				}
			}
		} else {
			JValue jo = (JValue) json;
			SBuffer sbf = null, occ = null;
			String itemName;
			if (jo.getObject() != null) {
				String s = jo.toString();
				if (s.isEmpty()) {
					itemName = J_STRING;
					// set default required string()
					sbf = new SBuffer("optional string();", jo.getPosition());
				} else {
					setSourceBuffer(jo.getSBuffer());
					SBuffer[] parsedScript = parseOccurrence();
					if (!parsedScript[0].getString().isEmpty()) { // occurrence
						occ = parsedScript[0];
					}
					if (parsedScript[1].getString().isEmpty()) {
						parsedScript[1] = new SBuffer("string();",
							jo.getPosition());
						itemName = J_STRING;
					} else if (s.contains("jnull")) {
						itemName = J_NULL;
					} else {
						if (s.contains("int") || s.contains("long")
							|| s.contains("float") || s.contains("double")
							|| s.contains("dec") || s.contains("jnum")) {
							itemName = J_NUMBER;
						} else if (s.contains("boolean")) {
							itemName = J_BOOLEAN;
						} else {
							itemName = J_STRING;
						}
						if (!s.endsWith(";")) {
							parsedScript[1].addString(";");
						}
						sbf = new SBuffer("optional "
							+ parsedScript[1].getString(), parsedScript[1]);
					}
				}
			} else {
				itemName = J_NULL;
			}
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
				if (o instanceof JValue && (_xdPrefix + ":script").equals(key)){
					setSourceBuffer(((JValue) o).getSBuffer());
					skipBlanksAndComments();
					choicePosition = getPosition();
					if (!isToken(_xdPrefix + ':' + "choice")) {
						choicePosition = null;
					} else {
						choice = true;
						skipBlanksAndComments();
						if (isChar(';')) {
							skipBlanksAndComments();
							if (!eos()) {
								xscript = new SBuffer(getUnparsedBufferPart(),
									getPosition());
							}
						}
					}
					continue;
				}
				String name1 = toXmlName(key);
				if (o == null) {
					attrs.put(name1, "jnull()");
				} else if (isSimpleValue(o)) {
					if (!"xmlns".equals(name1) && !name1.startsWith("xmlns:")) {
						if (choice) {
							simpleChoiceName = name1;
						}
					}
					attrs.put(name1, o);
				} else {
					items.put(name1, o);
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
							new SBuffer("optional "
								+ x.getString(), x.getPosition()));
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
								new SBuffer("optional;", eee.getName()));//pos
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
				setSourceBuffer(new SBuffer(list.get(0).toString(),
					((JValue) list.get(0)).getPosition()));
				skipBlanksAndComments();
				if (isToken(_xdPrefix + ':' + "script")) {
					skipBlanksAndComments();
					if (isChar('=')) {
						skipBlanksAndComments();
						if (isToken(_xdPrefix + ':' + "choice")) {
							// 1. find simpe value
							int simpleValue = -1;
							JList items = new JList(list.getPosition());
							for (int i = 1; i < list.size(); i++) {
								if (list.get(i) instanceof JValue) {
									if (simpleValue > 0) {
										//Only one JSON value is allowed
										error(XDEF.XDEF311);
										list.remove(i--);
										continue;
									}
									simpleValue = i;
									SBuffer s =
										((JValue)list.get(i)).getSBuffer();
									s = new SBuffer("optional " + s.getString(),
										s); // Sposition
									setAttr(parent, name, s);
								} else {
									items.add(list.get(i));
								}
							}
							if (list.size() - 1 - simpleValue == 1) {
								e = namedItemToXD(rawName,list.get(2),parent);
								setXDAttr(e, "script", new SBuffer("optional"));
								list.remove(2);
								return e;
							}
						}
					}
				}
			}
			e = genPElement(parent, nsURI, name, list.getPosition());
			parent._childNodes.add(e);
			listToXD(list, e);
			return e;
		} else {
			JValue jv = (JValue) val;
			setSourceBuffer(jv.getSBuffer());
			SBuffer[] parsedScript = parseOccurrence();
			e = genPElement(parent, nsURI, name, jv.getPosition());
			parent._childNodes.add(e);
			skipBlanksAndComments();
			if (!parsedScript[0].getString().trim().isEmpty()) { // occurrence
				setXDAttr(e, "script", parsedScript[0]);
			}
			String s;
			if ((s = parsedScript[1].getString().trim()).isEmpty()) {
				parsedScript[1].addString("string();");
			} else if (!s.endsWith(";")) {
				parsedScript[1].addString(";");
			}
			PNode ee = genXDElement(e, "text", parsedScript[1]);
			e._childNodes.add(ee);
			ee._value = new SBuffer("optional " + s, parsedScript[1]);
			return e;
		}
	}

	/** Set attributes to PNode created form simple named values of JMap,
	 * @param e PNode where to add attributes.
	 * @param map inspected map.
	 */
	private void setAttrsFromMapXD(final PNode e, final JMap map) {
		for (String key: map.keySet()) {
			Object o = map.get(key);
			if (o instanceof JValue) {
				JValue val = (JValue) o;
				if ((_xdPrefix + ":script").equals(key)) {
					setXDAttr(e, "script", val.getSBuffer());
				} else {
					if (isSimpleValue(o)) {
						if (!"xmlns".equals(key) && !key.startsWith("xmlns:")) {
							setAttr(e, toXmlName(key), val.getSBuffer());
						}
					}
				}
			} else if (o instanceof String) { // ???
				setAttr(e, toXmlName(key), new SBuffer((String) o));
			}
		}
	}

	/** Create JMap with items from JMAP which are map or array.
	 * @param map the inspected map.
	 * @return e JMap with not simple items.
	 */
	private JMap getNotSimpleValuesXD(final JMap map) {
		JMap notSimpleItems = new JMap(map.getPosition());
		for (String key: map.keySet()) {
			Object val = map.get(key);
			if (!isSimpleValue(val)) {
				notSimpleItems.put(key, val);
			}
		}
		return notSimpleItems;

	}

	/** Append map with JSON tuples to node.
	 * @param map map with JSON tuples.
	 * @param parent node where to append map.
	 * @return created element.
	 */
	private PNode mapToXD(final JMap map, final PNode parent) {
		PNode e;
		if (map.size() == 1) {
			String key = map.keySet().iterator().next();
			e = namedItemToXD(key, map.get(key), parent);
		} else {
			e = genJElement(parent, J_MAP, map.getPosition());
			parent._childNodes.add(e);
			JMap notSimpleItems = getNotSimpleValuesXD(map);
			setAttrsFromMapXD(e, map);
			if (!notSimpleItems.isEmpty()) {
				for (String key: notSimpleItems.keySet()) {
					namedItemToXD(key, notSimpleItems.get(key), e);
				}
			}
		}
		return e;
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
			setSourceBuffer(jv.getSBuffer());
			skipBlanksAndComments();
			SBuffer[] parsedScript;
			if (isToken(_xdPrefix + ":script")) {
				skipBlanksAndComments();
				if (isChar('=')) {
					skipBlanksAndComments();
					if (!eos()) {
						setXDAttr(e, "script", jv.getSBuffer());
					}
				}
			}
			parsedScript = parseOccurrence();
			ee = genJElement(e, J_ITEM, parsedScript[1]);
			e._childNodes.add(ee);
			PNode eee = genXDElement(ee, "text", parsedScript[1]);
			eee._value = new SBuffer(
				"optional " + parsedScript[1].getString(), parsedScript[1]);
			ee._childNodes.add(eee);
			if (!parsedScript[0].getString().isEmpty()) {
				setXDAttr(ee, "script", parsedScript[0]);
			}
		}
	}

	/** Append array of JSON values to node.
	 * @param list list with array of values.
	 * @param parent node where to append array.
	 */
	private PNode listToXD(final JList list, final PNode parent) {
		int i = 0;
		SBuffer xscript = new SBuffer("");
		PNode e = null;
		if (!list.isEmpty() && list.get(0) instanceof JValue) {
			JValue x = (JValue) list.get(0);
			setSourceBuffer(new SBuffer(x.toString(), ((JValue)x).getPosition()));
			skipBlanksAndComments();
			if (isToken(_xdPrefix + ":script")) {
				skipBlanksAndComments();
				if (isChar('=')) {
					i = 1;
					skipBlanksAndComments();
					if (isToken(_xdPrefix + ':' + "choice")) {
						e = genXDElement(parent, "choice", getPosition());
						parent._childNodes.add(e);
						skipSpaces();
						if (isChar(';')) {
							skipSpaces();
							if (!eos()) {
								xscript = new SBuffer(getUnparsedBufferPart(),
									getPosition());
							}
						}
						for (; i < list.size(); i++) {
							listItemToXD(list.get(i), e);
						}
						return e;
					}
				}
			}
		}
		if (e == null) {
			e = genJElement(parent, J_ARRAY, null);
			parent._childNodes.add(e);
		}
		if (!xscript.getString().isEmpty()) {
			setXDAttr(e, "script", xscript);
		}
		for (; i < list.size(); i++) {
			listItemToXD(list.get(i), e);
		}
		return e;
	}

	/** Create child nodes with JSON object to node.
	 * @param json JSON object.
	 * @param parent where to create child nodes.
	 */
	private PNode genJsonModelXD(final Object json, final PNode parent) {
		if (json instanceof JMap) {
			return mapToXD((JMap) json, parent);
		} else {
			return listToXD((JList) json, parent);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Create X-definition form of model from xd:json
////////////////////////////////////////////////////////////////////////////////

	/** Create X-definition model from PNode created from json XML element.
	 * @param p PNode with JSON script.
	 * @param reporter report writer
	 */
	final static void genXdef(final PNode p, final ReportWriter reporter) {
		XJson jx = new XJson();
		jx.setGenJObjects();
		jx.setReportWriter(reporter);
		if (p._value == null) {
			jx.setSourceBuffer(p._name);
			jx.error(JSON.JSON011); //Not JSON object&{0}
			return;
		}
		jx.setSourceBuffer(p._value);
		Object json = jx.parse();
		if (json != null && (json instanceof JMap || json instanceof JList)) {
			jx._jsNamespace = p._nsURI;
			jx._jsPrefix = p.getPrefix();
			if (XDConstants.JSON_NS_URI_W3C.equals(p._nsURI)) {
				jx.genJsonModelW3C(json, p);
			} else if (XDConstants.JSON_NS_URI.equals(p._nsURI)) {
				jx.genJsonModelXD(json, p);
			}
		} else {
			jx.error(JSON.JSON011); //Not JSON object&{0}
		}
		p._value = null;
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used when JSON is parsed from X-definition compiler.
////////////////////////////////////////////////////////////////////////////////

	public static class JMap extends LinkedHashMap<String, Object>{
		private final SPosition _position;

		public JMap(final SPosition position) {super(); _position = position;}

		public SPosition getPosition() {return _position;}
		public Object getObject() {return this;}
	}

	public static class JList extends ArrayList<Object> {
		private final SPosition _position;
		public JList(final SPosition position) {super(); _position = position;}
		public SPosition getPosition() {return _position;}
		public Object getObject() {return this;}
	}

	public static class JValue {
		private final SPosition _position;
		final Object o;
		public JValue(final SPosition position, final Object val) {
			_position = position;
			o = val;
		}
		public SPosition getPosition() {return _position;}
		public Object getObject() {return o;}
		public String getString() {return o == null ? "null" : o.toString();}
		public SBuffer getSBuffer() {return new SBuffer(getString(),_position);}
		@Override
		public String toString() {return o == null ? "null" : o.toString();}
	}
}