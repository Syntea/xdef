package org.xdef.xon;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.XDEmailAddr;

/** Conversion of JSON or XON to string.
 * @author Vaclav Trojan
 */
class XonToString extends XonTools {

	/** Create string representation of character.
	 * @param ch character to be converted.
	 * @return string representation of character.
	 */
	private static String genChar(final char ch) {
		int i = "\"\\\n\b\r\t\f".indexOf(ch);
		if (i < 0) {
			if (StringParser.getXmlCharType(ch, StringParser.XMLVER1_0)
				== StringParser.XML_CHAR_ILLEGAL) {
				String s = "c\"\\u";
				for (int j = 12; j >= 0; j -=4) {
					s += "0123456789abcdef".charAt((ch >> j) & 0xf);
				}
				return s + '"';
			}
			return "c\"" + String.valueOf(ch) + '"';
		}
		return "c\"\\" + "\"\\nbrtf".charAt(i) + "\"";
	}

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
		String result;
		if (xon) {
			if (x instanceof byte[]) {// byte array
				return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
			}
			result = x.toString();
			if (x instanceof Number) {
				if (x instanceof BigDecimal) {
					return result.charAt(0) == '-'
						? "-0d" + result.substring(1) : ("0d" + result);
				} else if (x instanceof Float) {
					if (((Float) x).isInfinite()) {
						return result.charAt(0) == '-' ? "-INFF" : "INFF";
					}
					return result + 'F';
				} else if (x instanceof Double) {
					if (((Double) x).isInfinite()) {
						return result.charAt(0) == '-' ? "-INF" : "INF";
					}
					return result;
				} else if (x instanceof Byte) {
					return result + 'B';
				} else if (x instanceof Short) {
					return result + 'S';
				} else if (x instanceof Integer) {
					return result + 'I';
				} else if (x instanceof Long) {
					return result;
				} else if (x instanceof BigInteger) {
					return result.charAt(0) == '-'
						? "-0i" + result.substring(1) : ("0i" + result);
				}
				return result;
			} else if (x instanceof Character) {
				return genChar((Character) x);
			} else if (x instanceof URI) {
				return "u\"" + jstringToSource(((URI) x).toASCIIString()) + '"';
			} else if (x instanceof XDEmailAddr) {
				return "e\""+jstringToSource(((XDEmailAddr) x).getEmailAddr())+'"';
			} else if (x instanceof SDatetime) {
				return "D" + x;
			} else if (x instanceof SDuration || x instanceof Price
				|| x instanceof GPSPosition) {
				return x.toString();
			}
		}
		if (x instanceof byte[]) {// byte array
			return "b(" + new String(SUtils.encodeBase64((byte[]) x)) + ")";
		} else if (x instanceof File) {// file
			result = ((File) x).getAbsolutePath();
		} else {
			result = x.toString();
		}
		if (x instanceof Number) {
			if (result.equals("NaN") || result.equals("Infinity")
				|| result.equals("-Infinity")) {
				return '"' + result + '"';
			}
			return result;
		}
		return '"' + jstringToSource(result) + '"';
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
			Map.Entry en = (Map.Entry) x;
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
			key += indent != null
				?  " " + separator + " " : String.valueOf(separator);
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
			objectToString(en.getValue(), ind, sb, xon);
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
		}
		return x.toString();
	}
}