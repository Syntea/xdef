package org.xdef.xon;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.XDEmailAddr;
import org.xdef.XDTelephone;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonTools.charToJSource;
import static org.xdef.xon.XonTools.jstringToSource;

/** Conversion of XON/JSON to string.
 * @author Vaclav Trojan
 */
class XonToString extends XonTools {

	/** Add the string created from XON/JSON simple value to StringBuilder.
	 * @param x object to be converted to String.
	 * @return sb created string.
	 * @param xon if true then XON else if false JOSN source is generated.
	 */
	private static String valueToString(final Object x, final boolean xon) {
		if (x == null || x instanceof XonTools.JNull) {
			return "null";
		} else if (x instanceof Boolean) {
			return x.toString();
		} else if (x instanceof String) {
			return '"' + jstringToSource((String) x) + '"';
		}
		if (x instanceof byte[]) {// byte array
			return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
		}
		if (xon) {
			if (x instanceof Number) {
				String num = x.toString();
				if (x instanceof Long) {
					return num;
				} else if (x instanceof Double) {
					return ((Double) x).isInfinite()
						? num.charAt(0) == '-' ? "-INF" : "INF" : num;
				} else if (x instanceof Float) {
					return ((Float) x).isInfinite()
						? num.charAt(0) == '-' ? "-INFF" : "INFF" : num + 'F';
				} else if (x instanceof Byte) {
					return num + 'B';
				} else if (x instanceof Short) {
					return num + 'S';
				} else if (x instanceof Integer) {
					return num + 'I';
				} else if (x instanceof BigInteger) {
					return num + 'N';
				} else if (x instanceof BigDecimal) {
					return num + 'd';
				}
			} else if (x instanceof Character) {
				return "c\"" + charToJSource((Character) x) + '"';
			} else if (x instanceof URI) {
				return "u\"" + jstringToSource(((URI) x).toASCIIString()) + '"';
			} else if (x instanceof XDEmailAddr) {
				return "e\""
					+ jstringToSource(((XDEmailAddr) x).getEmailAddr()) + '"';
			} else if (x instanceof SDatetime) {
				return "D" + x;
			} else if (x instanceof GPSPosition) {
				return "g(" + x + ')';
			} else if (x instanceof Price) {
				return "p(" + x + ')';
			} else if (x instanceof Currency) {
				return "C(" + ((Currency) x).getCurrencyCode() + ')';
			} else if (x instanceof XDTelephone) {
				return "T\"" + x + '"';
			} else if (x instanceof SDuration || x instanceof InetAddress) {
				return x.toString();
			}
		}
		String result;
		if (x instanceof File) {// file
			result = ((File) x).getAbsolutePath();
		} else if (x instanceof InetAddress) {
			result = x.toString().substring(1);
		} else if (x instanceof Currency) {
			result = ((Currency) x).getCurrencyCode();
		} else {
			result = x.toString();
			if (x instanceof Number) {
				return result.equals("NaN") || result.equals("Infinity")
					|| result.equals("-Infinity") ? '"' + result + '"' : result;
			}
		}
		return '"' + jstringToSource(result) + '"';
	}

	/** Write array item to array or Map.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 * @param sb1 StringBuilder from which append the created string
	 * @param notFirst true if this item is not the first one.
	 * @param oneLine true if all items are on one line.
	 */
	private static void writeItem(final String indent,
		final StringBuilder sb,
		final StringBuilder sb1,
		final boolean notFirst,
		final boolean oneLine) {
		if (notFirst) {
			sb.append(',');
			if (indent != null) {
				sb.append(oneLine ? " " : indent);
			}
		}
		sb.append(sb1);
	}

	/** Add the string created from XON/JSON array to StringBuilder.
	 * @param array array to be created to String.
	 * @param indent indentation of result,
	 * @param sb StringBuilder where to append the created string.
	 * @param xon if true then XON else if false JOSN source is generated.
	 */
	private static void arrayToString(final List array,
		final String indent,
		final StringBuilder sb,
		final boolean xon) {
		sb.append('[');
		if (array.isEmpty()) {
			sb.append(']');
			return;
		}
		int pos = sb.length();
		String ind = (indent != null) ? indent + "  " : null;
		int lineLen = sb.length() - sb.lastIndexOf("\n"), itemsLen = 0;
		List<StringBuilder> items = new ArrayList<StringBuilder>();
		boolean notFirst = false;
		for (Object o: array) {
			StringBuilder sb1 = new StringBuilder();
			objectToString(o, ind, sb1, xon);
			if (items != null && ind != null
				&& (sb1.indexOf("\n") >= 0
				|| (itemsLen += sb1.length() + 1) + lineLen > 74)) {
				items.add(sb1);
				for (StringBuilder x : items) {
					writeItem(ind, sb, x, notFirst, false);
					notFirst = true;
				}
				items = null;
			} else {
				if (items == null) {
					writeItem(ind, sb, sb1, notFirst, false);
					notFirst = true;
				} else {
					items.add(sb1);
				}
			}
		}
		if (items != null) {
			for (StringBuilder x : items) {
				writeItem(ind, sb, x, notFirst, true);
				notFirst = true;
			}
		}
		if (indent != null && sb.lastIndexOf("\n") > pos) {
			sb.append(indent).insert(pos, ind);
		}
		sb.append(']');
	}

	/** Add the string created from XON/JSON object to StringBuilder.
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

	/** Add named item to StringBuilder.
	 * @param en named item.
	 * @param indent indentation of result or null.
	 * @param xon if true then XON else if false JSON source is generated.
	 * @return StringBuilder with created item.
	 */
	private static StringBuilder createNamedItem(final Map.Entry en,
		final String ind,
		final boolean xon) {
		Object y = en.getKey();
		String key;
		if (y instanceof String) {
			key = (String) y;
		} else {
			try {
				key = new String((byte[]) y, "UTF-8");
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		char separator = '=';
		if (!xon || !StringParser.chkXMLName(key, StringParser.XMLVER1_0)) {
			key = '"' + jstringToSource(key) + '"';
			separator = ':';
		}
		StringBuilder sb = new StringBuilder(
			ind != null ? key + " " + separator + " " : (key + separator));
		objectToString(en.getValue(), ind, sb, xon);
		return sb;
	}

	/** Add the string created from XON/JSON map to StringBuilder.
	 * @param map map to be created to String.
	 * @param indent indentation of result or null.
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
		int pos = sb.length();
		String ind = (indent != null) ? indent + "  " : null;
		boolean notFirst = false;
		int lineLen = sb.length() - sb.lastIndexOf("\n"), itemsLen = 0;
		List<StringBuilder> items = new ArrayList<StringBuilder>();
		for (Object o: map.entrySet()) {
			StringBuilder sb1 = createNamedItem((Map.Entry) o, ind, xon);
			if (items != null && ind != null &&
				(sb1.indexOf("\n") >= 0
				|| (itemsLen += sb1.length() + 1) + lineLen >= 74)) {
				items.add(sb1);
				sb.append(' ');
				for (StringBuilder x : items) {
					writeItem(ind, sb, x, notFirst, false);
					notFirst = true;
				}
				items = null;
			} else {
				if (items == null) {
					writeItem(ind, sb, sb1, notFirst, false);
					notFirst = true;
				} else {
					items.add(sb1);
				}
			}
		}
		if (items != null) {
			for (StringBuilder x : items) {
				writeItem(ind, sb, x, notFirst, true);
				notFirst = true;
			}
		}
		if (indent != null && sb.lastIndexOf("\n") > pos) {
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
	private static List xonArrayToJson(final List xlist) {
		List<Object> result = new ArrayList<Object>();
		for (Object x: xlist) {
			result.add(xonToJson(x));
		}
		return result;
	}

	/** Convert XON map to JSON (all values except of Number, String, Boolean
	 * or null are converted to strings).
	 * @param xmap XON map object
	 * @return XON map converted to JSON.
	 */
	private static Map xonMapToJson(final Map xmap) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Object x: xmap.entrySet()) {
			Map.Entry en = (Map.Entry) x;
			Object o = en.getKey();
			String key = o instanceof byte[] // this is because of YAML???
				? new String((byte[]) o) : (String) o;
			result.put(key, xonToJson(en.getValue()));
		}
		return result;
	}

	/** Convert XON object to JSON.
	 * @param x XON object
	 * @return XON object converted to JSON.
	 */
	final static Object xonToJson(final Object x) {
		if (x == null || x instanceof String || x instanceof Boolean
			|| x instanceof Number || x instanceof XonTools.JNull) {
			return x;
		} else if (x instanceof Map) {
			return xonMapToJson((Map) x);
		} else if (x instanceof List) {
			return xonArrayToJson((List) x);
		} else if (x instanceof byte[]) {
			return new String(SUtils.encodeBase64((byte[]) x));
		} else if (x instanceof File) {
			return ((File) x).getAbsolutePath();
		} else if (x instanceof InetAddress) {
			return x.toString().substring(1);
		} else if (x instanceof Currency) {
			return ((Currency) x).getCurrencyCode();
		} else if (x instanceof XDTelephone) {
			return "T\"" + x + "\"";
		}
		return x.toString();
	}
}