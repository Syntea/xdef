package org.xdef.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.xdef.msg.JSON;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SRuntimeException;

/** Provides comparing of JSON objects
 * @author Vaclav Trojan
 */
class JsonCompare {
	/** Check if JSON arrays from arguments are equal.
	 * @param a1 first array.
	 * @param a2 second array.
	 * @return true if and only if both arrays are equal.
	 */
	private static boolean equalArray(final List a1, final List a2) {
		if (a1.size() == a2.size()) {
			for (int i = 0; i < a1.size(); i++) {
				if (!equalValue(a1.get(i), a2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/** Check if JSON maps from arguments are equal.
	 * @param m1 first map.
	 * @param m2 second map.
	 * @return true if and only if both maps are equal.
	 */
	private static boolean equalMap(final Map m1, final Map m2) {
		if (m1.size() != m2.size()) {
			return false;
		}
		for (Object k: m1.keySet()) {
			if (!m2.containsKey(k)) {
				return false;
			}
			if (!equalValue(m1.get(k), m2.get(k))) {
				return false;
			}
		}
		return true;
	}

	/** Check if JSON numbers from arguments are equal.
	 * @param n1 first number.
	 * @param n2 second number.
	 * @return true if and only if both numbers are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	private static boolean equalNumber(final Number n1, final Number n2) {
		if (n1 instanceof BigDecimal) {
			if (n2 instanceof BigDecimal) {
				return ((BigDecimal) n1).compareTo((BigDecimal) n2) == 0;
			} else if (n2 instanceof BigInteger) {
				return equalNumber(n1, new BigDecimal((BigInteger) n2));
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return equalNumber(n1, new BigDecimal(n2.longValue()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				//this is real equality, decimal can't be exactly converted!
				return n1.doubleValue() == n2.doubleValue();
			}
		} else if (n1 instanceof BigInteger) {
			if (n2 instanceof BigInteger) {
				return ((BigInteger) n1).compareTo((BigInteger) n2) == 0;
			} else if (n2 instanceof BigDecimal || n2 instanceof BigInteger) {
				return equalNumber(new BigDecimal((BigInteger)n1), n2);
			} else if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return equalNumber(n1, new BigInteger(n2.toString()));
			} else if (n2 instanceof Double || n2 instanceof Float) {
				return equalNumber(new BigDecimal((BigInteger)n1), n2);
			}
		} else if (n1 instanceof Long || n1 instanceof Integer
			|| n1 instanceof Short || n1 instanceof Byte) {
			if (n2 instanceof Long || n2 instanceof Integer
				|| n2 instanceof Short || n2 instanceof Byte) {
				return n1.longValue() == n2.longValue();
			} else if (n2 instanceof Double || n2 instanceof Float
				|| n2 instanceof BigInteger || n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			}
		} else if (n1 instanceof Double || n1 instanceof Float) {
			if (n2 instanceof BigInteger || n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			}
			return n1.doubleValue() == n2.doubleValue();
		}
		//Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			n1.getClass().getName(), n2.getClass().getName());
	}

	/** Check if JSON values from arguments are equal.
	 * @param o1 first value.
	 * @param o2 second value.
	 * @return true if and only if both values are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	final static boolean equalValue(final Object o1, final Object o2) {
		if (o1 == null || o2 instanceof JNull) {
			return o2 == null || o2 instanceof JNull;
		} else if (o2 == null || o1 instanceof JNull) {
			return o1 == null || o1 instanceof JNull;
		}
		if (o1 instanceof Map) {
			return o2 instanceof Map ? equalMap((Map)o1, (Map)o2) : false;
		}
		if (o1 instanceof List) {
			return o2 instanceof List ? equalArray((List) o1, (List) o2) :false;
		}
		if (o1 instanceof String) {
			return ((String) o1).equals(o2);
		}
		if (o1 instanceof Number) {
			return (o2 instanceof Number)
				? equalNumber((Number) o1, (Number) o2) : false;
		}
		if (o1 instanceof Boolean) {
			return ((Boolean) o1).equals(o2);
		}
		if (o1 instanceof Character) {
			return ((Character) o1).equals(o2);
		}
		if (o1 instanceof SDatetime) {
			return ((SDatetime) o1).equals(o2);
		}
		if (o1 instanceof SDuration) {
			return ((SDuration) o1).equals(o2);
		}
		if (o1 instanceof GPSPosition) {
			return ((GPSPosition) o1).equals(o2);
		}
		if (o1 instanceof CurrencyAmount) {
			return ((CurrencyAmount) o1).equals(o2);
		}
		try {
			byte[] b1 = (byte[]) o1;
			byte[] b2 = (byte[]) o2;
			return Arrays.equals(b2, b1);
		} catch (Exception ex) {}
		// Incomparable objects &{0} and &{1}
		throw new SRuntimeException(JSON.JSON012,
			o1.getClass().getName(), o2.getClass().getName());
	}
}