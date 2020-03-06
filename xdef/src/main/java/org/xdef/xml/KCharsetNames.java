package org.xdef.xml;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

/** Checking and canonize character set names in Java and XML.
 * @author Vaclav Trojan
 */
public final class KCharsetNames {

	private static final Map<String, String> IANA_TO_JAVA =
		new LinkedHashMap<String, String>();
	private static final Map<String, String> JAVA_TO_IANA =
		new LinkedHashMap<String, String>();
	static {
		IANA_TO_JAVA.put("IBM852", "IBM-852");
		IANA_TO_JAVA.put("CSGB2312","GB2312");
		IANA_TO_JAVA.put("KS_C_5601-1989","KS_C_5601-1987");
		IANA_TO_JAVA.put("KOREAN","KS_C_5601-1987");
		IANA_TO_JAVA.put("CSISO13JISC6220JP","JIS0201");
		IANA_TO_JAVA.put("CSIBM855","CP855");
		IANA_TO_JAVA.put("ISO-IR-149","KS_C_5601-1987");
		IANA_TO_JAVA.put("IBM-858","CP858");
		IANA_TO_JAVA.put("CCSID00924","CP924");
		IANA_TO_JAVA.put("X0208dbiJIS_X0208-1983","JIS0208");
		IANA_TO_JAVA.put("CSIBM290","CP290");
		IANA_TO_JAVA.put("CSIBM1026","CP1026");
		IANA_TO_JAVA.put("ISO-8859-8-I","ISO8859_8");
		IANA_TO_JAVA.put("CSIBM280","CP280");
		IANA_TO_JAVA.put("EBCDIC-CP-BE","CP500");
		IANA_TO_JAVA.put("EBCDIC-CP-NO","CP277");
		IANA_TO_JAVA.put("CSIBM277","CP277");
		IANA_TO_JAVA.put("CSIBM273","CP273");
		IANA_TO_JAVA.put("IBM-1149","Cp1149");
		IANA_TO_JAVA.put("IBM-1148","Cp1148");
		IANA_TO_JAVA.put("IBM-1147","Cp1147");
		IANA_TO_JAVA.put("IBM-1146","Cp1146");
		IANA_TO_JAVA.put("CP290","CP290");
		IANA_TO_JAVA.put("IBM-1145","Cp1145");
		IANA_TO_JAVA.put("IBM-1144","Cp1144");
		IANA_TO_JAVA.put("IBM-1143","Cp1143");
		IANA_TO_JAVA.put("IBM-1142","Cp1142");
		IANA_TO_JAVA.put("IBM-1141","Cp1141");
		IANA_TO_JAVA.put("IBM-1140","Cp1140");
		IANA_TO_JAVA.put("EBCDIC-CP-ES","CP284");
		IANA_TO_JAVA.put("CSPC775BALTIC","CP775");
		IANA_TO_JAVA.put("EBCDIC-CP-FI","CP278");
		IANA_TO_JAVA.put("IBM00924","CP924");
		IANA_TO_JAVA.put("EBCDIC-JP-KANA","CP290");
		IANA_TO_JAVA.put("LATIN-9","ISO8859_15_FDIS");
		IANA_TO_JAVA.put("EBCDIC-CP-DK","CP277");
		IANA_TO_JAVA.put("CSIBM918","CP918");
		IANA_TO_JAVA.put("CSKSC56011987","KS_C_5601-1987");
		IANA_TO_JAVA.put("EBCDIC-CP-IT","CP280");
		IANA_TO_JAVA.put("EBCDIC-LATIN9--EURO","CP924");
		IANA_TO_JAVA.put("IBM-367","ASCII");
		IANA_TO_JAVA.put("IBM-290","CSIBM290");
		IANA_TO_JAVA.put("IBM290","CP290");
		IANA_TO_JAVA.put("CP00924","IBM00924");
		IANA_TO_JAVA.put("IBM-924","CP924");
		IANA_TO_JAVA.put("X-ISO-10646-UCS-4-2143", "X-ISO-10646-UCS-4-2143");
		IANA_TO_JAVA.put("X-ISO-10646-UCS-4-3412", "X-ISO-10646-UCS-4-3412");

		JAVA_TO_IANA.put("UTF8", "UTF-8");
		JAVA_TO_IANA.put("UTF16", "UTF-16");
		JAVA_TO_IANA.put("UTF16LE", "UTF-16LE");
		JAVA_TO_IANA.put("UTF16BE", "UTF-16BE");
		JAVA_TO_IANA.put("UTF32", "UTF-32");
		JAVA_TO_IANA.put("UTF32LE", "UTF-32LE");
		JAVA_TO_IANA.put("UTF32BE", "UTF-16BE");
		JAVA_TO_IANA.put("IBM852", "IBM-852");
		JAVA_TO_IANA.put("CP852", "IBM-852");
		JAVA_TO_IANA.put("CP1250", "WINDOWS-1250");
		JAVA_TO_IANA.put("CP1251", "WINDOWS-1251");
		JAVA_TO_IANA.put("X-ISO-10646-UCS-4-2143", "X-ISO-10646-UCS-4-2143");
		JAVA_TO_IANA.put("X-ISO-10646-UCS-4-3412", "X-ISO-10646-UCS-4-3412");
	}

	/** Don't allow user to create an instance of this class. */
	private KCharsetNames() {}

	/** Get XML encoding name. Return null if encoding name is
	 * unknown. The argument can be either a XML encoding name or character set
	 * name.
	 * @param name the XML encoding name or the character set name.
	 * @return canonized XML encoding name or <tt>null</tt>.
	 */
	public static String getXmlEncodingName(final String name) {
		String result = JAVA_TO_IANA.get(name.toUpperCase(Locale.ENGLISH));
		return result!=null ? result : Charset.isSupported(name) ? name : null;
	}

	/** Get canonized java character set name. The argument can be either
	 * a XML encoding name or a character set name. Returns null if the
	 * character set name is unknown.
	 * @param name The name of character set.
	 * @return canonized character set name or <tt>null</tt>.
	 */
	public static String getJavaCharsetName(final String name) {
		String result = IANA_TO_JAVA.get(name.toUpperCase(Locale.ENGLISH));
		return result!=null ? result : Charset.isSupported(name) ? name : null;
	}

}