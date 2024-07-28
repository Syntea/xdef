package org.xdef.impl;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XDEF42_NS_URI;
import static org.xdef.XDConstants.XDEF_NS_PREFIX;
import org.xdef.XDEmailAddr;
import org.xdef.XDTelephone;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonTools.jstringToSource;

/** Generate X-definition from JSON/XON.
 * @author Vaclav Trojan
 */
public final class GenXDefXON {

	/** The class contains model and occurrence.*/
	private final static class XItem {
		private final XOccurrence _occ; // occurrence of this item
		private final Object _item; // value of this item

		/** Create new XItem from XON object.
		 * @param XON onject.
		 */
		XItem(Object x) {
			_occ = new XOccurrence(1,1);
			_item = x;
		}

		boolean isSimpleType(XItem x) {
			return _item instanceof String && x._item instanceof String;
		}

		/** Check if type of the argument is same as the type of this object.
		 * @param x the object to be checked.
		 * @return true if types are equal.
		 */
		boolean isSameType(XItem x) {
			return isSimpleType(x)
				|| _item instanceof List && x._item instanceof List
				|| _item instanceof Map && x._item instanceof Map;
		}

		/** Check if the this object is compatible with object from argument.
		 * @param x the object to be checke for compatibility.
		 * @return true if object is compatible.
		 */
		boolean isSame(XItem x) {
			if (!isSameType(x) || !_occ.equals(x._occ)) {
				return false;
			}
			if (_item instanceof String) {
				return _item.equals(x._item);
			}
			if (_item instanceof List) {
				List l1 = (List) _item;
				List l2 = (List) x._item;
				if (l1.size() != l2.size()) {
					return false;
				}
				for (int i = 0; i < l1.size()-1; i++) {
					XItem xi1 = (XItem)l1.get(i);
					XItem xi2 = (XItem)l2.get(i+1);
					if (!xi1.isSame(xi2)) {
						return false;
					}
				}
				return true;
			}
			// Map
			Map m1 = (Map) _item;
			Map m2 = (Map) x._item;
			if (m1.size() != m2.size()) {
				return false;
			}
			String[] keys = getKeys(m1);
			for (String key: keys) {
				if (!m2.containsKey(key)) {
					return false;
				}
				XItem xi1 = (XItem)m1.get(key);
				XItem xi2 = (XItem)m2.get(key);
				if (!xi1.isSame(xi2)){
					return false;
				}
			}
			return true;
		}

		/** Create string with occurrence information,
		 * @param isAtt if true the item is attribute or text node.
		 * @return string with occurrence information or an empty string,
		 */
		private String occToString(final boolean isAtt) {
			return _occ.isRequired() ? ""
				: isAtt ? _occ.toString(isAtt)+" " : _occ.toString(isAtt)+";";
		}

		/** Optimize model. */
		@SuppressWarnings("unchecked")
		private void optimize() {
			if (_item instanceof List) {
				boolean allSame = true;
				List<Object> list = (List) _item;
				int len = list.size();
				if (len > 1) {
					XItem xi0 = (XItem) list.get(0);
					int occ = 0;
					for (int i = 1; i < len; i++) {
						XItem xi = (XItem) list.get(i);
						if (!xi0.isSame((XItem) xi)) {
							allSame = false;
							occ = 0;
							break;
						} else {
							occ++;
						}
					}
					if (allSame) {
						XItem xi = (XItem) ((List) _item).get(0);
						if (occ > 0) {
							xi._occ.setMinOccur(occ+1);
							xi._occ.setMaxOccur(occ+1);
							((List) _item).clear();
							((List) _item).add(xi);
						}
					}
				}
				((List) _item).forEach(x -> {((XItem)x).optimize();});
			} else if (_item instanceof Map) {
				Map m = (Map) _item;
				String[] keys = getKeys(m);
				for (String key: keys) {
					((XItem) m.get(key)).optimize();
				}
			}
		}

		/** Create indented source of model.
		 * @param indent indentation or an empty string.
		 * @return indented source of created model.
		 */
		String toXonModel(String indent) {
			if (_item instanceof String) {
				return indent + "\"" + occToString(true) + _item + ";\"";
			}
			StringBuilder sb = new StringBuilder();
			String nIndent = indent.isEmpty() ? "\n  " : indent + "  ";
			boolean first = true;
			if (_item instanceof List) {
				List list = (List) _item;
				int size = list.size();
				sb.append(indent).append("[ ");
				if (_occ.isRequired() && list.isEmpty()) {
					sb.append("]");
				} else {
					if (!_occ.isRequired()) {
						sb.append("%script = \"");
						sb.append(occToString(false)).append("\"");
						size++;
						first = false;
					}
					for (Object o: list) {
						if (first) {
							first = false;
						} else {
							sb.append(",");
						}
						sb.append(((XItem) o).toXonModel(nIndent));
					}
					sb.append(indent.isEmpty() ? "\n" : indent).append(']');
				}
			} else { // map
				Map map = (Map) _item;
				sb.append(indent).append("{ ");
				int size = map.size();
				if (!_occ.isRequired()) {
					sb.append("%script=\"");
					sb.append(occToString(false)).append("\"");
					size++;
					first = false;
				}
				String[] keys = getKeys(map);
				for (String key: keys) {
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append(nIndent);
					if (StringParser.chkNCName(key, StringParser.XMLVER1_0)) {
						sb.append(key);
					} else {
						sb.append('"').append(jstringToSource(key)).append('"');
					}
					XItem xi = (XItem) map.get(key);
					sb.append(": ");
					sb.append(xi.toXonModel(
						xi._item instanceof String ? "" : nIndent + "  "));
				}
				sb.append(indent.isEmpty() ? "\n" : indent).append('}');
			}
			return sb.toString();
		}
	}

	/** Create XDItem from XON object with named values.
	 * @param map Map with XON object with named values.
	 * @return created XDItem object.
	 */
	@SuppressWarnings("unchecked")
	private static XItem genMap(final Map map) {
		Map<String, Object> m = new LinkedHashMap();
		String[] keys = getKeys(map);
		for (String key: keys) {
			m.put(key, genModel(map.get(key)));
		}
		return new XItem(m);
	}

	/** Create XDItem from XON array.
	 * @param list List with XON array.
	 * @return created XDItem object.
	 */
	@SuppressWarnings("unchecked")
	private static XItem genList(final List list) {
		List<Object> l = new ArrayList();
		list.forEach(o -> {l.add(genModel(o));});
		return new XItem(l);
	}

	/** Create type method from XON simple type.
	 * @param x XON simple type
	 * @return name of type method.
	 */
	private static String genItem(final Object x) {
		if (x == null) {
			return "jnull()";
		} else if (x instanceof Boolean) {
			return "jboolean()";
		} else if (x instanceof Number) {
			return "jnumber()";
		} else if (x instanceof Character) {
			return "char()";
		} else if (x instanceof String) {
			return "jstring()";
		} else if (x instanceof URI) {
			return "uri()";
		} else if (x instanceof XDEmailAddr) {
			return "emailAddr()";
		} else if (x instanceof SDatetime) {
			return (x.toString().contains("T")) ? "dateTime()" : "date()";
		} else if (x instanceof GPSPosition) {
			return "gps()";
		} else if (x instanceof Price) {
			return "price()";
		} else if (x instanceof Currency) {
			return "currency()";
		} else if (x instanceof XDTelephone) {
			return "telephone()";
		} else if (x instanceof SDuration) {
			return "duration()";
		} else if (x instanceof InetAddress) {
			return "ipAddr()";
		} else if (x instanceof byte[]) {// byte array
			return "base64Binary()";
		} else {
			return "jvalue()";
		}
	}

	/** Create XDItem from XON data.
	 * @param data XON data.
	 * @return created XDItem object.
	 */
	private static XItem genModel(final Object data) {
		return data instanceof Map ? genMap((Map) data)
			: data instanceof List ? genList((List) data)
			: new XItem(genItem(data));
	}

	private static String[] getKeys(Map m) {
		String[] keys = new String[m.size()];
		int i = 0;
		for (Object o: m.keySet()) {
			keys[i++] = (String) o;
		}
		return keys;
	}

	/** Generate X-definition from input data to given output stream writer.
	 * @param xon JSON/XON data.
	 * @param xdName name XDefinition or null.
	 * @return org.w3c.dom.Document object with X-definition.
	 */
	public static final Element genXdef(final Object xon, final String xdName) {
		Document doc = KXmlUtils.newDocument(XDEF42_NS_URI, "xd:def", null);
		Element xdef = doc.getDocumentElement();
		xdef.setAttribute("xmlns:" + XDEF_NS_PREFIX, XDEF42_NS_URI);
		if (xdName != null && !xdName.isEmpty()) {
			xdef.setAttribute("name", xdName);
		}
		Element xmodel = doc.createElementNS(XDEF42_NS_URI, "xd:xon");
		String modelName = "model";
		xdef.setAttribute("root", modelName);
		xmodel.setAttributeNS(XDEF42_NS_URI, "xd:name", modelName);
		XItem xi = genModel(xon);
		xi.optimize();
		xmodel.appendChild(
			xmodel.getOwnerDocument().createTextNode(xi.toXonModel("")));
		xdef.appendChild(xmodel);
		return xdef;
	}
}