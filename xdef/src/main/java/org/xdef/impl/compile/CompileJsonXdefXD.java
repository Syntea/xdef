package org.xdef.impl.compile;

import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.impl.XOccurrence;
import org.xdef.json.JsonUtil;
import org.xdef.msg.JSON;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SUtils;

/** Create X-definition model from xd:json in the format according
 * to X-definition specification.
 * @author Vaclav Trojan
 */
public class CompileJsonXdefXD extends CompileJsonXdef {
	private String _jsNamespace = XDConstants.JSON_NS_URI_XD;

	/** Prepare instance of XJSON. */
	private CompileJsonXdefXD() {super();}

////////////////////////////////////////////////////////////////////////////////
// Create X-definition model from xd:json (W3C transformation)
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
		String s = SUtils.modifyString(SUtils.modifyString(
			JsonUtil.jstringToSource(key), "\\", "\\\\"), "'", "\\'") ;
		addMatchExpression(e, '@' + JsonUtil.J_KEYATTR + "=='"+ s +"'");
		setAttr(e, JsonUtil.J_KEYATTR, new SBuffer("fixed('"+ s +"');", e._name));
	}

	private PNode genJsonMap(final JMap map, final PNode parent) {
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
				e.addChildNode(ee);
				skipSemiconsBlanksAndComments();
				if (!eos()) {
					setXDAttr(ee, "script",
						new SBuffer(getUnparsedBufferPart(), getPosition()));
				}
			} else if (map.size() > 1) {
				e = genJElement(parent, "map", map.getPosition());
				ee = e;
				if (map.size() > 2) {
					ee = genXDElement(e, "mixed", map.getPosition());
					e.addChildNode(ee);
				}
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
				index = 1;
			}
			for(; index < len; index++) {
				PNode ee = genJsonModel(array.get(index), e);
				PAttr val;
				// if it is not the last and it has xd:script attribute where
				// the min occurrence differs from max occurrence
				// and it has the attribute with a value description
				if (JsonUtil.J_ITEM.equals(ee._localName)
					&& _jsNamespace.equals(ee._nsURI)
					&& (val = getAttr(ee, JsonUtil.J_VALUEATTR)) != null) {
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
								+ JsonUtil.J_VALUEATTR + ").matches()");
					}
				}
			}
		}
		return e;
	}

	private PNode genJsonValue(final JValue jo, final PNode parent) {
		SBuffer sbf, occ = null;
		PNode e = genJElement(parent, JsonUtil.J_ITEM, jo.getPosition());
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
			setAttr(e, JsonUtil.J_VALUEATTR, sbf);
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
		_jsNamespace = XDConstants.JSON_NS_URI_W3C;
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
	static final void genXdefXD(final PNode p,
		final byte jsonMode,
		final SBuffer name,
		final ReportWriter reporter) {
		CompileJsonXdefXD jx = new CompileJsonXdefXD();
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
		Object json = jx.parse();
		if (json != null && (json instanceof JMap
			|| json instanceof JArray
			|| json instanceof JValue
			|| (json instanceof JValue &&
			((JValue) json).getValue() instanceof String))) {
			jx.genJsonModel(json, p);
		} else {
			jx.error(JSON.JSON011); //Not JSON object&{0}
		}
		p._value = null;
	}
}