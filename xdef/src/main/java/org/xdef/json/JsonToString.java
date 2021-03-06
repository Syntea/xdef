package org.xdef.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SUtils;

/** Provides conversion of JSON to string.
 * @author Vaclav Trojan
 */
class JsonToString {

	/** Add the string created from JSON jvalue object to StringBuilder.
	 * @param obj JSON object to be created to String.
	 * @return sb created string.
	 */
	final static String jvalueToString(final Object obj){
		if (obj == null) {
			return "null";
		} else if (obj instanceof String) {
			return '"' + JsonUtil.jstringToSource((String) obj) + '"';
		}
		return obj.toString();
	}

	/** Add the string created from JSON object to StringBuilder.
	 * @param obj JSON object to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	private static void objToJsonString(final Object obj,
		final String indent,
		final StringBuilder sb) {
		if (obj instanceof List) {
			List x = (List) obj;
			arrayToJsonString(x, indent, sb);
		} else if (obj instanceof Map) {
			Map x = (Map) obj;
			mapToJsonString(x, indent, sb);
		} else {
			sb.append(jvalueToString(obj));
		}
	}

	/** Add the string created from JSON array to StringBuilder.
	 * @param array JSON array to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	final static void arrayToJsonString(final List array,
		final String indent,
		final StringBuilder sb) {
		if (array.isEmpty()) {
			sb.append("[]");
			return;
		}
		if (indent != null && indent.length() > 0 && array.size() == 1) {
			Object o = array.get(0);
			if (!(o instanceof Map) && !(o instanceof List)) {
				String s = jvalueToString(o);
				if (s.length() + indent.length() < 72) {
					sb.append('[').append(s).append(']');
					return;
				}
			}
		}
		int lastValuePosition = sb.length();
		sb.append('[');
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		for (Object o: array) {
			if (first) {
				first = false;
				if (ind != null && array.size() > 1) {
					sb.append(ind);
				}
			} else {
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
			}
			objToJsonString(o, ind, sb);
		}
		if (ind != null
			&&  (array.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)){
			sb.append(indent);
		}
		sb.append(']');
	}

	/** Add the string created from JSON map to StringBuilder.
	 * @param map JSON map to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 */
	final static void mapToJsonString(final Map map,
		final String indent,
		final StringBuilder sb) {
		sb.append('{');
		if (map.isEmpty()) {
			sb.append('}');
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		int lastValuePosition = sb.length();
		for (Object x: map.entrySet()) {
			Map.Entry e = (Map.Entry) x;
			if (first) {
				first = false;
				if (map.size() > 1) {
					sb.append(' ');
				}
				objToJsonString(e.getKey(), "", sb);
			} else {
				lastValuePosition = sb.length();
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
				objToJsonString(e.getKey(), ind, sb);
			}
			lastValuePosition = sb.length();
			sb.append(':');
			objToJsonString(e.getValue(), ind, sb);
		}
		if (ind != null
			&&  (map.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)) {
			sb.append(indent);
		}
		sb.append('}');
	}

////////////////////////////////////////////////////////////////////////////////
// JSON/XON contertor
////////////////////////////////////////////////////////////////////////////////

	private static List xlistToJlist(final List xlist) {
		List<Object> result = new ArrayList<Object>();
		for (Object x: xlist) {
			result.add(xobjectToJobject(x));
		}
		return result;
	}

	private static Map xmapToJmap(final Map xmap) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Object x: xmap.entrySet()) {
			Map.Entry en = (Map.Entry) x;
			String key = (String) en.getKey();
			key = JsonUtil.xmlToJsonName(key);
			Object y = en.getValue();
			result.put(key, xobjectToJobject(y));
		}
		return result;
	}

	final static Object xobjectToJobject(final Object x) {
		if (x == null || x instanceof String || x instanceof Boolean
			|| x instanceof Number) {
			return x;
		} else if (x instanceof Map) {
			return xmapToJmap((Map) x);
		} else if (x instanceof List) {
			return xlistToJlist((List) x);
		} else if (x instanceof Character) {
			return String.valueOf(x);
		} else if (x instanceof SDatetime) {
			return x.toString();
		} else if (x instanceof SDuration) {
			return x.toString();
		} else if (x instanceof CurrencyAmount) {
			return "#" + x;
		} else if (x instanceof GPSPosition) {
			return "gps" +  x;
		}
		try { // try byte array
			return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
		} catch (Exception ex) {}
		return x.toString();
	}

	private static String mapToXon(final Map map, final String indent) {
		String newIndent = indent != null ? indent + "  " : null;
		String result = "{";
		boolean wasFirst = false;
		for (Object x: map.entrySet()) {
			Map.Entry en = (Map.Entry) x;
			if (!wasFirst) {
				wasFirst = true;
			} else {
				result += ',';
			}
			if (indent != null) {
				result += "\n" + newIndent;
			}
			String key = (String) en.getKey();
			String value = objectToXon(en.getValue(), newIndent);
			result += key + (indent == null ? "=" :" = ") + value;
		}
		if (wasFirst && indent != null) {
			result += "\n" + indent;
		}
		return result + "}";
	}

	private static String listToXon(final List list, final String indent) {
		String newIndent = indent != null ? indent + "  " : null;
		String result = "[";
		boolean wasFirst = false;
		for (Object x: list) {
			if (!wasFirst) {
				wasFirst = true;
			} else {
				result += ',';
			}
			if (indent != null) {
				result += "\n" + newIndent;
			}
			result += objectToXon(x, newIndent);
		}
		if (wasFirst && indent != null) {
			result += "\n" + indent;
		}
		return result + "]";
	}

	final static String objectToXon(final Object x, final String indent) {
		if (x == null || x instanceof Boolean) {
			return "" + x;
		} else if (x instanceof Map) {
			return mapToXon((Map) x, indent);
		} else if (x instanceof List) {
			return listToXon((List) x, indent);
		} else {
			if (x instanceof Number) {
				String result = x.toString();
				if (x instanceof BigDecimal) {
					return result + 'D';
				} else if (x instanceof Double) {
					return result + 'F';
				} else if (x instanceof BigInteger) {
					return result + 'N';
				} else if (x instanceof Short) {
					return result + 'S';
				} else if (x instanceof Integer) {
					return result + 'I';
				}
				return result;
			} else if (x instanceof String) {
				return '"' + JsonUtil.jstringToSource((String) x) + '"';
			} else if (x instanceof Character) {
				return '"' + JsonUtil.jstringToSource(String.valueOf(x)) + '"';
			} else if (x instanceof SDatetime) {
				x.toString();
				return "d(" + x + ")";
			} else if (x instanceof SDuration) {
				return "p(" + x + ")";
			} else if (x instanceof CurrencyAmount) {
				return "#" + x;
			} else if (x instanceof GPSPosition) {
				return "gps" + x;
			}
			try { // try byte array
				return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
			} catch (Exception ex) {}
			return x.toString();
		}
	}
}