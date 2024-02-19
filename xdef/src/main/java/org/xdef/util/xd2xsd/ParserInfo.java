package org.xdef.util.xd2xsd;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.parsers.XSParseDate;
import org.xdef.impl.parsers.XSParseDatetime;
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.impl.parsers.XSParseHexBinary;
import org.xdef.impl.parsers.XSParseString;
import org.xdef.impl.parsers.XSParseTime;
import org.xdef.model.XMData;
import org.xdef.sys.SException;
import org.xdef.sys.StringParser;

/** Creates from the X-definition parser a parser compatible with XML schema.
 * @author Trojan
 */
public class ParserInfo {

	private final XDParser _xdp;
	private final String _info;
	private final String _declaredName;

	/** Create new instace of ParserInfo with X-definition parser,
	 * declared type name and information text.
	 * @param xdp X-definition parser,
	 * @param declaredName declared type name in X-defiontion.
	 * @param info information text.
	 */
	private ParserInfo(final XDParser xdp,
		final String declaredName,
		final String info) {
		_xdp = xdp;
		if (declaredName != null && !declaredName.isEmpty()) {
			_xdp.setDeclaredName((_declaredName = declaredName));
		} else {
			_declaredName = null;
		}
		_info = info != null && info.isEmpty() ? null : info;
	}

	/** Get X-definition parser.
	 * @return X-definition parser.
	 */
	public XDParser getParser() {return _xdp;}

	/** Get information text (used in xs:documentation).
	 * @return information text.
	 */
	public String getInfo() {return _info;}

	/** Get name of declared type in X-definition.
	 * @return name of declared type in X-definition or null.
	 */
	public String getDeclaredName() {return _declaredName;}

	/** Get name of parser type.
	 * @return name of parser type.
	 */
	public String getSchemaTypeName() {
		return Xd2Xsd.XSCHEMA_PFX + _xdp.parserName();
	}

	/** Set to the parser the declared name.
	 * @param xdp the X-definition parser.
	 * @param info information (i.e. original parser name).
	 * @param declaredName name of delared type (or null);.
	 * @param xdc X-container with parser parameters.
	 * @return ParserInfo object.
	 */
	private static ParserInfo createNewParserInfo(final XDParser xdp,
		final String info,
		final String declaredName,
		final XDContainer xdc) {
		try {
			ParserInfo result = new ParserInfo(xdp, declaredName, info);
			result._xdp.setNamedParams(null, xdc);
			return result;
		} catch (SException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	/** Generate parameters of validation method from XDContainer
	 * @param xdc container with named parameters.
	 * @return string with parameter list.
	 */
	private static String genPars(XDContainer xdc) {
		String result = "(";
		for (XDNamedValue x: xdc.getXDNamedItems()) {
			if (result.length() > 1) {
				result += ",";
			}
			 result += '%' + x.getName() + '=' + x.getValue();
		}
		return result + ']';
	}

	/** Create ParserInfo object with XML schema compatible parser from XMData.
	 * @param xmd XMData object.
	 * @return ParserInfo object with XML schema compatible parser.
	 */
	public static ParserInfo genParser(final XMData xmd) {
		XDParser xdp = (XDParser) xmd.getParseMethod();
		String name = xdp.parserName();
		String declName = xdp.getDeclaredName();
		XDContainer xdc = xdp.getNamedParams();
		String info = "Created from X-definition " + name;
		switch(name) {
			case "an":
				xdc.setXDNamedItem("pattern", new DefString("[A-Za-z0-9]+"));
				return createNewParserInfo(
					new XSParseString(), info + genPars(xdc), declName, xdc);
			case "contains": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("pattern",
					new DefString(".*" + genEscapeChars(s) + ".*"));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + '(' + s + ')', declName, xdc);
			}
			case "containsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = "";
				for (char c: s.toCharArray()) {
					if (Character.isLetter(c)) {
						mask += '[' + Character.toLowerCase(c)
							+ Character.toUpperCase(c) + ']';
					} else {
						mask += genEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(".*"+mask+".*"));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "country":
				xdc.setXDNamedItem("pattern", new DefString("[a-ZA-Z]{2,3}"));
				return createNewParserInfo(
					new XSParseString(), info + "()", declName, xdc);
			case "currency":
				return createNewParserInfo(
					new XSParseString(), "(); see ISO 4217", declName, xdc);
			case "ends": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("pattern", new DefString(".*"
					+ genEscapeChars(s)));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "endsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = "";
				for (char c: s.toCharArray()) {
					if (Character.isLetter(c)) {
						mask += '[' + Character.toLowerCase(c)
							+ Character.toUpperCase(c) + ']';
					} else {
						mask += genEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(".*" + mask));
				return createNewParserInfo(
					new XSParseString(), info + '(' + s + ')', declName, xdc);
			}
			case "enum": {
				XDValue x = xdc.getXDNamedItem("argument").getValue();
				xdc.setXDNamedItem("enumeration", x);
				xdc.removeXDNamedItem("argument");
				String s = "";
				if (x instanceof XDContainer) {
					XDContainer y = (XDContainer) x;
					for (int i=0; i < y.getXDItemsNumber(); i++) {
						if (i > 0) s += ',';
						s += "" + y.getXDItem(i);
					}
				} else {
					s += "" + x;
				}
				return createNewParserInfo(
					new XSParseString(), info + '(' + s + ')', declName, xdc);
			}
			case "eq": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("enumeration",
					xdc.getXDNamedItem("argument"));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "eqi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = "";
				for (char c: s.toCharArray()) {
					if (Character.isLetter(c)) {
						mask += '[' + Character.toLowerCase(c)
							+ Character.toUpperCase(c) + ']';
					} else {
						mask += genEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "MD5":
				xdc.setXDNamedItem("length", new DefLong(16));
				return createNewParserInfo(
					new XSParseHexBinary(), info + genPars(xdc), declName, xdc);
			case "num": {
				info += genPars(xdc);
				xdc.setXDNamedItem("pattern", new DefString("\\d+"));
				return createNewParserInfo(
					new XSParseString(), info, declName, xdc);
			}
			case "pic": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = "";
				for (char c: s.toCharArray()) {
					switch (c) {
						case '9':
							mask += "[0-9]";
							continue;
						case 'A':
							mask += "[a-zA-Z]";
							continue;
						case 'X':
							mask += "[0-9a-zA-Z]";
							continue;
						default:
							mask += genEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "hex":
				return createNewParserInfo(
					new XSParseHexBinary(), info, declName, xdc);
			case "dec":
				return createNewParserInfo(
					new XSParseDecimal(), info, declName, xdc);
			case "starts": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				xdc.setXDNamedItem("pattern", new DefString(
					genEscapeChars(s)+"*"));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "startsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = "";
				for (char c: s.toCharArray()) {
					if (Character.isLetter(c)) {
						mask += '[' + Character.toLowerCase(c)
							+ Character.toUpperCase(c) + ']';
					} else {
						mask += genEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(mask + "*"));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "xdatetime": {
				String mask = xdc.getXDNamedItemAsString("outFormat");
				if (mask == null || mask.isEmpty()) {
					mask = xdc.getXDNamedItemAsString("format");
					if (mask == null || mask.isEmpty()) {
						throw new RuntimeException("xdatetime params missing");
					}
				}
				info += '(' + mask;
				xdc.removeXDNamedItem("outFormat");
				xdc.removeXDNamedItem("format");
				for (XDNamedValue x: xdc.getXDNamedItems()) {
					info += ",%" + x.getName() + '=' + x.getValue();
				}
				info += ']';
				switch (mask) {
					case "yyyy-MM-dd":
					case "yyyy-MM-ddZ":
						return createNewParserInfo(
							new XSParseDate(), info, declName,xdc);
					case "HH:mm":
					case "HH:mm:ss":
					case "HH:mm:ssZ":
					case "HH:mm:ss.S":
					case "HH:mm:ss.SZ":
						return createNewParserInfo(
							new XSParseTime(), info, declName,xdc);
					case "dd-MM-yyyyTHH:mm:ss":
					case "dd-MM-yyyyTHH:mm:ss.S":
					case "dd-MM-yyyyTHH:mm:ssZ":
					case "dd-MM-yyyyTHH:mm:ss.SZ":
						return createNewParserInfo(
							new XSParseDatetime(), info, declName, xdc);
					default: // return string type with patterns
						String[] patterns = getRegexes(mask);
						XDContainer x = new DefContainer();
						for (String y: patterns) {
							x.addXDItem(y);
						}
						xdc.setXDNamedItem("pattern", x);
						return createNewParserInfo(
							new XSParseString(), info, declName, xdc);
				}
			}
			case "anyURI":
			case "base64Binary":
			case "boolean":
			case "byte":
			case "date":
			case "dateTime":
			case "decimal":
			case "double":
			case "duration":
			case "float":
			case "gDay":
			case "gMonth":
			case "gMonthDay":
			case "gYear":
			case "gYearMonth":
			case "hexBinary":
			case "int":
			case "integer":
			case "language":
			case "long":
			case "Name":
			case "NCName":
			case "negativeInteger":
			case "NMTOKEN":
			case "nonNegativeInteger":
			case "nonPositiveInteger":
			case "normalizedString":
			case "positiveInteger":
			case "short":
			case "string":
			case "time":
			case "token":
			case "unsignedByte":
			case "unsignedInt":
			case "unsignedLong":
			case "unsignedShort":
				ParserInfo result = new ParserInfo(xdp, null, declName);
				return result;
			case "list":
				// TODO ????
				return createNewParserInfo(new XSParseString(),
					info + "()", declName, new DefContainer());
			case "union":
				// TODO ????
				return createNewParserInfo(new XSParseString(),
					info + "()", declName, new DefContainer());
			default: // other uncenvertible X-definition types
				return createNewParserInfo(new XSParseString(),
					info + "()", declName, new DefContainer());
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Generator of regular expression patterns created from xdatetime mask.
////////////////////////////////////////////////////////////////////////////////

	private static final String ESCAPEDCHARS = "\\|.-?*+{}(){}^";

	/** Get regex string created form a part of xdatetime mask.
	 * @param mask part of xdatetime mask.
	 * @return regex string.
	 */
	private static String getRegex(final String mask) {
		StringBuilder ret = new StringBuilder();
		StringParser p = new StringParser(mask);
		while (!p.eos()) {
			if (p.isToken("DDD")) { // Day in year with leading zeros
				ret.append(
				"(00[1-9]|0[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-6])");
			} else if (p.isChar('D')) { // Day in year without leading zeros
				ret.append(
				"([1-9]|[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])");
			} else if (p.isChar('d')) { // Day in month
				ret.append(p.isChar('d')
					? "(0[1-9]|[1-2][0-9]|3[0-1])" // with leading zero
					: "([1-9]|[1-2][0-9]|3[0-1])"); // without leading zero
			} else if (p.isChar('H')) { // HOUR 0..24
				 ret.append((p.isChar('H'))
					 ? "(0[0-9]|1[0-9]|2[0-3])" //with leading zero
					 : "([0-9]|1[0-9]|2[0-3])"); //without leading zero
			} else if (p.isChar('h')) { // HOUR 0..12
				ret.append("([1-9]|1[0-2])");
			} else if (p.isChar('K')) { // Hour from 0-11
				ret.append(p.isChar('K')
					? "(0[0-9]|1[0-1])" //with leading zero
					: "([0-9]|1[0-1])"); //without leading zero
			} else if (p.isChar('M')) { // Month
				if (!p.isChar('M')) { // Month without leading zero (M)
					ret.append("([1-9]|1[0-2])");
				} else if (!p.isChar('M')) { // Month with leading zeros. (MM)
					ret.append("(0[1-9]|1[0-2])");
				} else if (!p.isChar('M')) { //Short month name (MMM)
					ret.append("[A-Za-z][a-z]{2}");
				} else { // Month full name. (MMMM)
					while (p.isChar('M')){}
					ret.append("[A-Za-z][a-z]*");
				}
			} else if (p.isChar('G')) { // era
				while (p.isChar('G')){}
				ret.append("[a-zA-Z]*(\\s[a-zA-Z])*");
			} else if (p.isChar('m')) { // minute 0..59
				ret.append(p.isChar('m')
					? "([0-9]|[1-5][0-9])" // with leading zero
					: "[0-5][0-9]"); // without leading zero
			} else if (p.isToken("s")) { // seconds 0..59
				ret.append(p.isChar('m')
					? "[0-5][0-9]" // with leading zero
					: "([1-9]|[1-5][0-9])"); // without leading zero
			} else if (p.isChar('S')) { //Seconds fraction
				while (p.isChar('S')) {}
				ret.append("\\d+");
			} else if (p.isChar('k')) { //Hour in day from interval 1-24
				ret.append(p.isChar('k')
					? "(0[1-9]|1[0-9]|2[0-4])" // with leading zero
					: "([1-9]|1[0-9]|2[0-4])"); // without leading zero
			} else if (p.isChar('y')) { // Year
				int i = 1;
				while (p.isChar('y')) i++;
				switch (i) {
					case 1:// Year (as ISO standard)
						ret.append("(-)?(0*)?\\d{4}"); break;
					case 2: // Year as 2 digits.
						ret.append("\\d{2}"); break;
					default: ret.append("\\d{4}\\d*"); // four and more digits.
				}
			} else if (p.isChar('Y')) { //Year
				int i = 1;
				while (p.isChar('Y')) i++;
				ret.append(i == 1
					? "(-)?(0*)?\\d{4}" // 4digits
					: "\\d{2}"); // two digits
			} else if (p.isToken("RR")) { //Year from 0 to 99 with leading zero
				ret.append("[0-9]{2}");
			} else if (p.isChar('z')) { //zone name
				int i = 1;
				while (p.isChar('z')) i++;
				ret.append(i == 1
					? "[0-9]{2}" // short zone name
					: "([A-Z][a-z]*(\\s[A-Z][a-z]*)*)"); // full zone name
			} else if (p.isChar('Z')) { // zone base format
				int i = 1;
				while (p.isChar('Z')) i++;
				switch (i) {
					case 2: //+/-HHmm format
					ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3])[0-5][0-9]");
					break;
					case 3:
					case 4:
					case 5: //+/-HHmm format
						ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3])[0-5][0-9]");
						break;
					default: //+/-HH:mm format
					ret.append("(\\+|\\-)(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]");
				}
			} else if (p.isChar('a')) { //Information about day part (am, pm).
				while (p.isChar('a')){}
				ret.append("[a-zA-Z]+");
			} else if (p.isChar('{')) {
				p.findCharAndSkip('}');
			} else if (p.isChar('E')) { //Day in week
				int i = 1;
				while (p.isChar('E')) i++;
				ret.append(i < 4
					? "[a-zA-z]+" // short name
					: "[a-zA-z]+"); // full name
			} else if (p.isChar('e')) { //Day in week as number
				ret.append("[1-7]");
			} else if (p.isChar('[')) {
				ret.append("(");
			} else if (p.isChar(']')) {
				ret.append(")?");
			} else if (p.isChar('\'')) {
				StringBuilder constBuffer = new StringBuilder();
				while (!p.isChar('\'')) {
					constBuffer.append(p.peekChar());
				}
				ret.append(genEscapeChars(constBuffer.toString()));
			} else {
				ret.append(genEscapeChars(String.valueOf(p.peekChar())));
			}
		}
		return ret.toString();
	}

	/** Return string with special character escaped.
	 * @param c character.
	 * @return string with character or with special character escaped.
	 */
	private static String genEscapedChar(final char c) {
		if (ESCAPEDCHARS.indexOf(c) >= 0) {
			return "\\"+ c;
		} else if (' ' == c) {
			return "\\s";
		} else {
			return String.valueOf(c);
		}
	}

	/** Return string with special characters escaped.
	 * @param string string to modify.
	 * @return modified string with special characters escaped.
	 */
	public static String genEscapeChars(String string) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			ret.append(genEscapedChar(string.charAt(i)));
		}
		return ret.toString();
	}

	/** Get set with regex strings created form xdatetime mask.
	 * @param mask xdatetime mask.
	 * @return set with regex strings.
	 */
	private static String[] getRegexes(String mask) {
		Set<String> ret = new HashSet<>();
		StringTokenizer st = new StringTokenizer(mask, "|");
		while (st.hasMoreTokens()) {
			String maskPart = st.nextToken();
			String regex = getRegex(maskPart);
			regex = regex.trim();
			if (regex.length() > 0) {
				ret.add(regex);
			}
		}
		return ret.toArray(new String[0]);
	}
}
