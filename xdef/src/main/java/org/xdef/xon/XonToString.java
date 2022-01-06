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
		if (x == null) {
			return "null";
		} else if (x instanceof Boolean) {
			return x.toString();
		} else if (x instanceof String) {
			return '"' + jstringToSource((String) x) + '"';
		}
		if (x instanceof byte[]) {// byte array
			return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
		}
		String result;
		if (xon) {
			result = x.toString();
			if (x instanceof Number) {
				if (x instanceof BigDecimal) {
					return result + 'd';
				} else if (x instanceof Float) {
					return ((Float) x).isInfinite()
						? result.charAt(0) == '-' ? "-INFF" : "INFF"
						: result + 'F';
				} else if (x instanceof Double) {
					return ((Double) x).isInfinite()
						? result.charAt(0) == '-' ? "-INF" : "INF"
						: result;
				} else if (x instanceof Byte) {
					return result + 'B';
				} else if (x instanceof Short) {
					return result + 'S';
				} else if (x instanceof Integer) {
					return result + 'I';
				} else if (x instanceof Long) {
					return result;
				} else if (x instanceof BigInteger) {
					return result + 'N';
				}
				return result;
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
			} else if (x instanceof SDuration || x instanceof InetAddress) {
				return x.toString();
			}
		}
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
		if (array.isEmpty()) {
			sb.append("[]");
			return;
		}
		String ind = (indent != null) ? indent + "  " : null;
		if (indent != null && indent.length() > 0) {
			StringBuilder sb1 = new StringBuilder();
			for (Object o: array) {
				if (sb1.length() != 0) {
					sb1.append(", ");
				}
				objectToString(o, ind, sb1, xon);
				if (sb1.indexOf("\n") >= 0) {
					sb1 = null;
					break;
				}
			}
			if (sb1!=null&&sb1.length()+sb.length()-sb.lastIndexOf("\n") < 74) {
				sb.append(ind != null ? "[ " : "[" ).append(sb1).append("]");
				return;
			}
		}
		sb.append('[');
		int pos = sb.length();
		int lastValuePosition = sb.length();
		boolean first = true;
		for (Object o: array) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
			}
			objectToString(o, ind, sb, xon);
		}
		if (ind != null) {
			if (sb.lastIndexOf("\n") > lastValuePosition) {
				sb.insert(pos, indent);
				sb.append(indent);
			} else {
				sb.insert(pos, ' ');
				sb.append(' ');
			}
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

	/** Create string from named item.
	 * @param en named item.
	 * @param indent indentation of result or null.
	 * @param xon if true then XON else if false JOSN source is generated,
	 * @return string created from named item.
	 */
	private static String entryToString(final Map.Entry en,
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
		key = ind != null ? key+" "+separator+" " : (key + separator);
		StringBuilder sb1 = new StringBuilder();
		sb1.append(key);
		objectToString(en.getValue(), ind, sb1, xon);
		return sb1.toString();
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
		String ind = (indent != null) ? indent + "  " : null;
		boolean first = true;
		if (map.size() <= 2) {
			String s = "";
			for (Object x: map.entrySet()) {
				if (first) {
					first = false;
				} else {
					s += ind != null ? ", " : ",";
				}
				s += entryToString((Map.Entry) x, ind, xon);
			}
			if (s.indexOf('\n') < 0
				&& sb.length() - sb.lastIndexOf("\n") + s.length() < 80) {
				sb.append(s).append("}");
				return;
			}
		}
		int lastValuePosition = sb.length();
		first = true;
		for (Object x: map.entrySet()) {
			String s = entryToString((Map.Entry) x, ind, xon);
			lastValuePosition = sb.length();
			if (first) {
				first = false;
				if (ind != null) {
					sb.append(' ');
				}
				sb.append(s);
			} else {
				sb.append(',');
				if (ind != null) {
					sb.append(ind);
				}
				sb.append(s);
			}
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
			Object o = en.getKey();
			String key;
			if (o instanceof byte[]) { // this is because of YAML
				key = new String((byte[])o);
			} else {
				key = (String) o;
			}
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
			|| x instanceof Number || x instanceof XonTools.JNull) {
			return x;
		} else if (x instanceof Map) {
			return xonMapToJson((Map) x);
		} else if (x instanceof List) {
			return xonArraytOJson((List) x);
		} else if (x instanceof byte[]) {
			return new String(SUtils.encodeBase64((byte[]) x));
		} else if (x instanceof File) {
			return ((File) x).getAbsolutePath();
		} else if (x instanceof InetAddress) {
			return x.toString().substring(1);
		} else if (x instanceof InetAddress) {
			return ((Currency) x).getCurrencyCode();
		}
		return x.toString();
	}
}