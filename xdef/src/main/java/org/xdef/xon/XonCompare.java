package org.xdef.xon;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xdef.XDValue;
import org.xdef.sys.SRuntimeException;

/** Provides comparing of XON objects
 * @author Vaclav Trojan
 */
final class XonCompare {

	private static String objDiff(final Object a, final Object b) {
		if (XonUtils.xonEqual(a, b)) {
			return "";
		}
		if (a == null) {
			return "A: null, B: " + b.getClass() + "\n";
		} else if (b == null) {
			return "A: " + a.getClass() +", B: null\n";
		} else {
			if (!a.getClass().equals(b.getClass())) {
				return "A: "+a.getClass()+", B: "+b.getClass()+"\n";
			} else if (a instanceof List) {
				return listDiff((List) a, (List) b);
			} else if (a instanceof Map) {
				return mapDiff((Map) a, (Map) b);
			} else {
				return "A: "+a+", B: "+b+"\n";
			}
		}
	}

	private static String mapDiff(final Map a, final Map b) {
		if (XonUtils.xonEqual(a, b)) {
			return "";
		}
		String s = "{\n";
		int sizea = a.size();
		int sizeb = b.size();
		if (sizea != sizeb) {
			s += "A size=" + sizea + ", B size=" + sizeb;
		} else {
			Set ae = a.keySet();
			Set be = b.keySet();
			boolean keydiff = false;
			for (Object akey: ae) {
				if (!b.containsKey(akey)) {
					s += "A: " + akey + ", B missing\n";
					keydiff = true;
				}
			}
			for (Object bkey: be) {
				if (!a.containsKey(bkey)) {
					s += "B: " + bkey + ", A missing\n";
					keydiff = true;
				}
			}
			if (!keydiff) {
				for (Object key: ae) {
					Object ao = a.get(key);
					Object bo = b.get(key);
					try {
						if (!XonUtils.xonEqual(ao, bo)) {
							s += "key "+ key + "; " + objDiff(ao, bo);
						}
					} catch (Exception ex) {
						s += "key "+ key + "; " + ex;
					}
				}
			}
		}
		return s + "}\n";
	}

	private static String listDiff(final List a, final List b) {
		if (XonUtils.xonEqual(a, b)) {
			return "";
		}
		String s = "[\n";
		int sizea = a.size();
		int sizeb = b.size();
		if (sizea != sizeb) {
			s += "A size=" + sizea + ", B size=" + sizeb;
		} else {
			for (int i = 0; i < sizea; i++) {
				Object oa = a.get(i), ob = b.get(i);
				if (!XonUtils.xonEqual(oa, ob)) {
					s += "[" + i + "]: " + objDiff(oa, ob);
				}
			}
		}
		return s + "]\n";
	}

	/** Compare two XON/JSON objects. Return an empty string if both objects are equal.
	 * Otherwise, return string with different items.
	 * @param a first object with XON/JSON data.
	 * @param b second object with XON/JSON data.
	 * @return true if and only if both objects contains equal data.
	 */
	static final String xonDiff(final Object a, final Object b) {
		if (XonUtils.xonEqual(a, b)) {
			return "";
		}
		if (a != null && b != null) {
			if (a instanceof List && b instanceof List) {
				return listDiff((List) a, (List) b);
			} else if (a instanceof Map && b instanceof Map) {
				return mapDiff((Map) a, (Map) b);
			}
		}
		return objDiff(a,b);
	}

	/** Check if XON/JSON arrays from arguments are equal.
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

	/** Check if XON/JSON maps from arguments are equal.
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

	/** Check if XON/JSON numbers from arguments are equal.
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
			} else if (n2 instanceof Double) {
				//this is real equality, decimal can't be exactly converted!
				return n1.doubleValue() == n2.doubleValue();
			} else if (n2 instanceof Float) {
				//this is real equality, decimal can't be exactly converted!
				return n1.floatValue() == n2.floatValue();
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
		} else if (n1 instanceof Long || n1 instanceof Integer || n1 instanceof Short || n1 instanceof Byte) {
			if (n2 instanceof Long || n2 instanceof Integer || n2 instanceof Short || n2 instanceof Byte) {
				return n1.longValue() == n2.longValue();
			} else if (n2 instanceof Double || n2 instanceof Float
				|| n2 instanceof BigInteger || n2 instanceof BigDecimal) {
				return equalNumber(n2, n1);
			}
		} else if (n2 instanceof BigInteger || n2 instanceof BigDecimal) {
			return equalNumber(n2, n1);
		} else if (n1 instanceof Double) {
			if (n2 instanceof Double) {
				return n1.doubleValue() == n2.doubleValue();
			}
		}
		return n1.floatValue() == n2.floatValue();
	}

	/** Check if XON/JSON values from arguments are equal.
	 * @param o1 first value.
	 * @param o2 second value.
	 * @return true if and only if both values are equal.
	 * @throws SRuntimeException if objects are incomparable
	 */
	final static boolean equalValue(final Object o1, final Object o2) {
		if (o1 instanceof Map) {
			return o2 instanceof Map ? equalMap((Map)o1, (Map)o2) : false;
		} else if (o1 instanceof List) {
			return o2 instanceof List ? equalArray((List) o1,(List) o2) : false;
		} else if (o1 instanceof Number) {
			return o2 instanceof Number ? equalNumber((Number) o1, (Number) o2) : false;
		} else if (o1 instanceof byte[]) {
			if (o2 instanceof byte[]) {
				return Arrays.equals((byte[]) o1, (byte[]) o2);
			} else if (o2 instanceof XDValue) {
				return o2.equals(o1);
			} else {
				return false;
			}
		} else if (o1==null || o1 instanceof XDValue&&((XDValue) o1).isNull()) {
			return o2==null || o2 instanceof XDValue&&((XDValue) o2).isNull();
		}
		try {
			return o1.equals(o2);
		} catch (Exception ex) {
			return false;
		}
	}
}
