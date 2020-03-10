package org.xdef.impl.util.conv.type;

import org.xdef.sys.StringParser;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/** Analyzer of X-definition <code>datetime</code> type masks.
 * @author Ilia Alexandrov
 */
public class DatetimeMaskAnalyzer {

	/** Day in year with leading zeros. */
	private static final String DDD =
		"(00[1-9]|0[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-6])";
	/** Day in year without leading zeros. */
	private static final String D =
		"([1-9]|[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])";
	/** Day in month with leading zeros. */
	private static final String dd = "(0[1-9]|[1-2][0-9]|3[0-1])";
	/** Day in month without leading zeros. */
	private static final String d = "([1-9]|[1-2][0-9]|3[0-1])";
	/** Hour in day from interval 0-23 with leading zeros. */
	private static final String HH = "(0[0-9]|1[0-9]|2[0-3])";
	/** Hour in day from interval 0-23 without leading zeros. */
	private static final String H = "([0-9]|1[0-9]|2[0-3])";
	/** Hour in day from interval 1-12 with leading zeros. */
	private static final String hh = "(0[1-9]|1[0-2])";
	/** Hour in day from interval 1-12 without leading zeros. */
	private static final String h = "([1-9]|1[0-2])";
	/** Hour in day from interval 0-11 with leading zeros. */
	private static final String KK = "(0[0-9]|1[0-1])";
	/** Hour in day from interval 0-11 without leading zeros. */
	private static final String K = "([0-9]|1[0-1])";
	/** Full month name. */
	private static final String MMMM = "[A-Za-z][a-z]*";
	/** Short month name. */
	private static final String MMM = "[A-Za-z][a-z]{2}";
	/** Month in year with leading zeros. */
	private static final String MM = "(0[1-9]|1[0-2])";
	/** Month in year without leading zeros. */
	private static final String M = "([1-9]|1[0-2])";
	/** Minutes in hour with leading zeros. */
	private static final String mm = "[0-5][0-9]";
	/** Minutes in hour without leading zeros. */
	private static final String m = "([0-9]|[1-5][0-9])";
	/** Seconds in minute with leading zeros. */
	private static final String ss = "[0-5][0-9]";
	/** Seconds in minute without leading zeros. */
	private static final String s = "([1-9]|[1-5][0-9])";
	/** Seconds fraction. */
	private static final String S = "\\d+";
	/** Hour in day from interval 1-24 with leading zeros. */
	private static final String kk = "(0[1-9]|1[0-9]|2[0-4])";
	/** Hour in day from interval 1-24 without leading zeros. */
	private static final String k = "([1-9]|1[0-9]|2[0-4])";
	/** Year with four and more digits. */
	private static final String yyyy = "\\d{4}\\d*";
	/** Year between 1901 and 2000 with leading zeros. */
	private static final String yy = "\\d{2}";
	/** Year as ISO standard. */
	private static final String Y = "(-)?(0*)?\\d{4}";
	/** Year as 2 digits. */
	private static final String YY = "\\d{2}";
	/** Year from 0 to 99 with leading zeros. */
	private static final String RR = "[0-9]{2}";
	/** Full zone name. */
	private static final String zz = "([A-Z][a-z]*(\\s[A-Z][a-z]*)*)";
	/** Short zone name. */
	private static final String z = "[a-zA-Z0-9]{1,5}";
	/** Zone in +/-HH:mm format. */
	private static final String ZZZZZZ = "(\\+|\\-)" + HH + ":" + mm;
	/** Zone in +/-HHmm format. */
	private static final String ZZZZZ = "(\\+|\\-)" + HH + mm;
	/** Zone in +/-Hm format. */
	private static final String ZZ = "(\\+|\\-)" + H + m;
	/** Zone in +/-HH:mm format. */
	private static final String Z = "(\\+|\\-)" + HH + ":" + mm;
	/** Information about day part (am, pm). */
	private static final String a = "[a-zA-Z]+";
	/** Era. */
	private static final String G = "[a-zA-Z]*(\\s[a-zA-Z])*";
	/** Day in week short name. */
	private static final String E = "[a-zA-z]+";
	/** Day in week full name. */
	private static final String EEEE = "[a-zA-z]+";
	/** Day in week as number. */
	private static final String e = "[1-7]";

	public static Set<String> getRegexes(String mask) {
		Set<String> ret = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(mask, "|");
		while (st.hasMoreTokens()) {
			String maskPart = st.nextToken();
			String regex = getRegex(maskPart);
			regex = regex.trim();
			if (regex.length() > 0) {
				ret.add(regex);
			}
		}
		return ret;
	}

	private static String getRegex(String mask) {
		if (mask == null) {
			throw new NullPointerException("Given mask is null");
		}
		if (mask.length() == 0) {
			throw new IllegalArgumentException("Given mask is empty");
		}
		StringBuilder ret = new StringBuilder();
		StringParser p = new StringParser(mask);
		while (!p.eos()) {
			if (p.isToken("DDD")) {
				ret.append(DDD);
			} else if (p.isToken("D")) {
				ret.append(D);
			} else if (p.isToken("dd")) {
				ret.append(dd);
			} else if (p.isToken("d")) {
				ret.append(d);
			} else if (p.isToken("HH")) {
				ret.append(HH);
			} else if (p.isToken("H")) {
				ret.append(H);
			} else if (p.isToken("hh")) {
				ret.append(hh);
			} else if (p.isToken("h")) {
				ret.append(h);
			} else if (p.isToken("KK")) {
				ret.append(KK);
			} else if (p.isToken("K")) {
				ret.append(K);
			} else if (p.isChar('M')) {
				int i = 1;
				while (p.isChar('M')) {
					i++;
				}
				if (i == 1) {
					ret.append(M);
				} else if (i == 2) {
					ret.append(MM);
				} else if (i == 3) {
					ret.append(MMM);
				} else {
					ret.append(MMMM);
				}
			} else if (p.isChar('G')) {
				int i = 1;
				while (p.isChar('G')) {
					i++;
				}
				ret.append(G);
			} else if (p.isToken("mm")) {
				ret.append(mm);
			} else if (p.isToken("m")) {
				ret.append(m);
			} else if (p.isToken("ss")) {
				ret.append(ss);
			} else if (p.isToken("s")) {
				ret.append(s);
			} else if (p.isToken("S")) {
				ret.append(S);
			} else if (p.isToken("kk")) {
				ret.append(kk);
			} else if (p.isToken("k")) {
				ret.append(k);
			} else if (p.isChar('y')) {
				int i = 1;
				while (p.isChar('y')) {
					i++;
				}
				if (i == 2) {
					ret.append(yy);
				} else {
					ret.append(yyyy);
				}
			} else if (p.isChar('Y')) {
				int i = 1;
				while (p.isChar('Y')) {
					i++;
				}
				if (i == 1) {
					ret.append(Y);
				} else {
					ret.append(YY);
				}
			} else if (p.isToken("RR")) {
				ret.append(RR);
			} else if (p.isChar('z')) {
				int i = 1;
				while (p.isChar('z')) {
					i++;
				}
				if (i == 1) {
					ret.append(z);
				} else {
					ret.append(zz);
				}
			} else if (p.isChar('Z')) {
				int i = 1;
				while (p.isChar('Z')) {
					i++;
				}
				if (i == 1) {
					ret.append(Z);
				} else if (i == 2) {
					ret.append(ZZ);
				} else if (i == 5) {
					ret.append(ZZZZZ);
				} else {
					ret.append(ZZZZZZ);
				}
			} else if (p.isChar('a')) {
				int i = 1;
				while (p.isChar('a')) {
					i++;
				}
				ret.append(a);
			} else if (p.isChar('{')) {
				p.findCharAndSkip('}');
			} else if (p.isChar('E')) {
				int i = 1;
				while (p.isChar('E')) {
					i++;
				}
				if (i < 4) {
					ret.append(E);
				} else {
					ret.append(EEEE);
				}
			} else if (p.isChar('e')) {
				ret.append(e);
			} else if (p.isChar('[')) {
				ret.append("(");
			} else if (p.isChar(']')) {
				ret.append(")?");
			} else if (p.isChar('\'')) {
				StringBuilder constBuffer = new StringBuilder();
				while (!p.isChar('\'')) {
					constBuffer.append(p.peekChar());
				}
				ret.append(escapeChars(constBuffer.toString()));
			} else {
				ret.append(escapeChars(String.valueOf(p.peekChar())));
			}
		}
		return ret.toString();
	}

	/** Returns string as constant for regular expression with escaped special
	 * characters.
	 * @param string string to modify.
	 * @return modified string with special characters escaped.
	 */
	private static String escapeChars(String string) {
		StringBuilder ret = new StringBuilder();
		String escapedChars = "\\|.-?*+{}(){}^";
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (escapedChars.indexOf(ch) >= 0) {
				ret.append("\\").append(ch);
			} else if (' ' == ch) {
				ret.append("\\s");
			} else {
				ret.append(ch);
			}
		}
		return ret.toString();
	}
}