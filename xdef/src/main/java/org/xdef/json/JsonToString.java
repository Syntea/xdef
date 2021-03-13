package org.xdef.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.impl.XConstants;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Conversion of JSON or XON to string.
 * @author Vaclav Trojan
 */
class JsonToString extends JsonTools {

	/** Add the a string created from JSON or XON simple value to StringBuilder.
	 * @param x object to be converted to String.
	 * @return sb created string.
	 * @param xon if true then XON else if false JOSN source is generated.
	 */
	private static String valueToString(final Object x, final boolean xon) {
		if (x == null) {
			return "null";
		} else if (x instanceof Boolean) {
			return x.toString();
		} else if (x instanceof String) {
			return '"' + jstringToSource((String) x) + '"';
		}
		if (xon) {
			if (x instanceof Number) {
				String result = x.toString();
				if (x instanceof BigDecimal) {
					return result + 'd';
				} else if (x instanceof Float) {
					return result + 'F';
				} else if (x instanceof Double) {
					return result + 'D';
				} else if (x instanceof Short) {
					return result + 'S';
				} else if (x instanceof Integer) {
					return result + 'I';
				} else if (x instanceof Long) {
					return result + 'L';
				} else if (x instanceof BigInteger) {
					return result + 'N';
				}
				return result;
			} else if (x instanceof Character) {
				return '\''+ jstringToSource(String.valueOf(x))+'\'';
			} else if (x instanceof SDatetime) {
				return "d(" + x + ")";
			} else if (x instanceof SDuration) {
				return "p(" + x + ")";
			} else if (x instanceof CurrencyAmount) {
				return "#(" + x + ')';
			} else if (x instanceof GPSPosition) {
				return "g(" + x + ')';
			}
			try { // try byte array
				return "b("+new String(SUtils.encodeBase64((byte[]) x))+")";
			} catch (Exception ex) {}
		}
		return x.toString();
	}

	/** Add the string created from JSON or XON array to StringBuilder.
	 * @param array array to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 * @param xon if true then XON else if false JOSN source is generated.
	 */
	private static void arrayToString(final List array,
		final String indent,
		final StringBuilder sb,
		final boolean xon) {
		if (array.isEmpty()) {
			sb.append("[]");
			return;
		}
		if (indent != null && indent.length() > 0 && array.size() == 1) {
			Object o = array.get(0);
			if (!(o instanceof Map) && !(o instanceof List)) {
				String s = valueToString(o, xon);
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
			objectToString(o, ind, sb, xon);
		}
		if (ind != null
			&&  (array.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)){
			sb.append(indent);
		}
		sb.append(']');
	}

	/** Add the string created from JSON or XON object to StringBuilder.
	 * @param obj object to be converted to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 * @param xon if true then XON else if false JOSN source is generated.
	 */
	final static void objectToString(final Object obj,
		final String indent,
		final StringBuilder sb,
		final boolean xon) {
		if (obj instanceof List) {
			List x = (List) obj;
			arrayToString(x, indent, sb, xon);
		} else if (obj instanceof Map) {
			Map x = (Map) obj;
			mapToString(x, indent, sb, xon);
		} else {
			sb.append(valueToString(obj, xon));
		}
	}

	/** Add the string created from JSON or XON map to StringBuilder.
	 * @param map map to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 * @param xon if true then XON else if false JOSN source is generated,
	 */
	private static void mapToString(final Map map,
		final String indent,
		final StringBuilder sb,
		final boolean xon) {
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
			String key = (String) e.getKey();
			boolean xonKey =
				xon && StringParser.chkNCName(key, XConstants.XML10);
			if (xonKey) {
				key += indent == null ? "=" : " = ";
			} else {
				key = '"' + jstringToSource(key) + '"'
					+ (indent == null ? ":" : " : ");
			}
			if (first) {
				first = false;
				if (map.size() > 1) {
					sb.append(' ');
				}
				sb.append(key);
			} else {
				lastValuePosition = sb.length();
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
				sb.append(key);
			}
			lastValuePosition = sb.length();
			objectToString(e.getValue(), ind, sb, xon);
		}
		if (ind != null
			&&  (map.size() > 1 || sb.lastIndexOf("\n") > lastValuePosition)) {
			sb.append(indent);
		}
		sb.append('}');
	}

////////////////////////////////////////////////////////////////////////////////
// XON to JSON contertor
////////////////////////////////////////////////////////////////////////////////

	/** Convert XON array to JSON.
	 * @param xlist XON array.
	 * @return XON array converted to JSON.
	 */
	private static List xonArraytOJson(final List xlist) {
		List<Object> result = new ArrayList<Object>();
		for (Object x: xlist) {
			result.add(xonToJson(x));
		}
		return result;
	}

	/** Convert XON map to JSON.
	 * @param xmap XON map object
	 * @return XON map converted to JSON.
	 */
	private static Map xonMapToJson(final Map xmap) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Object x: xmap.entrySet()) {
			Map.Entry en = (Map.Entry) x;
			String key = (String) en.getKey();
			key = xmlToJsonName(key);
			Object y = en.getValue();
			result.put(key, xonToJson(y));
		}
		return result;
	}

	/** Convert XON object to JSON.
	 * @param x XON object
	 * @return XON object converted to JSON.
	 */
	final static Object xonToJson(final Object x) {
		if (x == null || x instanceof String || x instanceof Boolean
			|| x instanceof Number) {
			return x;
		} else if (x instanceof Map) {
			return xonMapToJson((Map) x);
		} else if (x instanceof List) {
			return xonArraytOJson((List) x);
		} else if (x instanceof Character) {
			return String.valueOf(x);
		} else if (x instanceof SDatetime) {
			String s = x.toString();
			StringParser p = new StringParser(s);
			if (p.isSignedInteger() && p.eos()) {
				return p.getParsedLong(); // gYear without zone!
			}
			return s;
		} else if (x instanceof SDuration
			|| x instanceof CurrencyAmount || x instanceof GPSPosition) {
			return x.toString();
		} else {
			try { // try byte array
				byte[] b = (byte[]) x;
				return new String(SUtils.encodeBase64(b));
			} catch (Exception ex) {} // not byte array
		}
		return x.toString();
	}
}