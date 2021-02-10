package org.xdef.json;

import java.util.List;
import java.util.Map;

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
}