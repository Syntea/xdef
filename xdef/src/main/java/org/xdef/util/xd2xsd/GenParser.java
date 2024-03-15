package org.xdef.util.xd2xsd;

import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.parsers.XSParseDate;
import org.xdef.impl.parsers.XSParseDatetime;
import org.xdef.impl.parsers.XSParseHexBinary;
import org.xdef.impl.parsers.XSParseList;
import org.xdef.impl.parsers.XSParseString;
import org.xdef.impl.parsers.XSParseTime;
import org.xdef.impl.parsers.XSParseUnion;
import org.xdef.model.XMData;
import org.xdef.sys.SException;

/** Creates from the X-definition parser a parser compatible with XML schema.
 * @author Trojan
 */
class GenParser {

	private final XDParser _xdp;// XDParser object with parameters (schema type)
	private final String _declaredName;// name of X-definition parser
	private final String _info; // text of information about type conversion
	private String _defaultValue; // default value
	private String _fixedValue; // fixed value

	/** Create new instance of ParserInfo with X-definition parser,
	 * declared type name and information text.
	 * @param xdp X-definition parser,
	 * @param declaredName declared type name in X-definition.
	 * @param info information text.
	 */
	private GenParser(final XDParser xdp,
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
	protected XDParser getParser() {return _xdp;}

	/** Get information text (used in xs:documentation).
	 * @return information text (or null).
	 */
	protected String getInfo() {return _info;}

	/** Get name of declared type in X-definition.
	 * @return name of declared type in X-definition (or null).
	 */
	protected String getDeclaredName() {return _declaredName;}

	/** Get default value from data model.
	 * @return default value from data model (or null).
	 */
	protected String getDefault() {return _defaultValue;}

	/** Get fixed value from data model.
	 * @return fixed value from data model (or null).
	 */
	protected String getFixed() {return _fixedValue;}

	/** Create ParserInfo and set to the parser the declared name.
	 * @param xdp the X-definition parser.
	 * @param info information (i.e. original parser name).
	 * @param declaredName name of declared type (or null);.
	 * @param xdc X-container with parser parameters.
	 * @return ParserInfo object.
	 */
	private static GenParser genParserInfo(final XDParser xdp,
		final String info,
		final String declaredName,
		final XDContainer xdc) {
		try {
			GenParser result = new GenParser(xdp,declaredName,info);
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
		return result + ')';
	}

	/** Create ParserInfo object with XML schema compatible parser from XMData.
	 * @param xmd XMData object.
	 * @param genXdOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
	 * @return ParserInfo object with XML schema compatible parser.
	 */
	protected static GenParser genParser(final XMData xmd,
		final boolean genXdOutFormat) {
		GenParser result =
			genParser((XDParser) xmd.getParseMethod(), genXdOutFormat);
		XDValue val = xmd.getFixedValue();
		if (val != null) {
			result._fixedValue = val.toString();
		}
		if ((val = xmd.getDefaultValue()) != null) {
			result._defaultValue = val.toString();
		}
		return result;
	}

	/** Create ParserInfo object with XML schema compatible parser from
	 * X-definition parser.
	 * @param xdp X-definition parser.
	 * @param generator instance of Xd2Xsd.
	 * @param genXdOutFormat if true, from the xdatetime method the outFormat
	 * parameter (the second sequential) is used as mask to validate datetime.
	 * @return ParserInfo object with XML schema compatible parser.
	 */
	private static GenParser genParser(final XDParser xdp,
		final boolean genXdOutFormat) {
		String name = xdp.parserName();
		String declName = xdp.getDeclaredName();
		XDContainer xdc = xdp.getNamedParams();
		String info = "Created from X-definition " + name;
		String s, mask;
		XDValue x;
		switch(name) {
////////////////////////////////////////////////////////////////////////////////
// XML schema types which are implemented in X-definition
////////////////////////////////////////////////////////////////////////////////
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
				return new GenParser(xdp, declName, null);
			case "list": {
				XSParseList xParseList = (XSParseList) xdp;
				XDParser parser = xParseList.getItemParser();
				xParseList.setItemParser(
					genParser(parser, genXdOutFormat).getParser());
				return new GenParser(xdp, declName, null);
			}
			case "union": {
				XDParser[] parsers = ((XSParseUnion) xdp).getParsers();
				for (int i = 0; i < parsers.length; i++) {
					parsers[i] =
						genParser(parsers[i], genXdOutFormat).getParser();
				}
				return new GenParser(xdp, declName, null);
			}
////////////////////////////////////////////////////////////////////////////////
// X-definition types converted to XML schema type
////////////////////////////////////////////////////////////////////////////////
			case "an":
				info += genPars(xdc);
				xdc.setXDNamedItem("pattern", new DefString("[A-Za-z0-9]+"));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "contains":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("pattern", new DefString(
					".*" + GenRegex.escapeCharsInString(s) + ".*"));
				xdc.removeXDNamedItem("argument");
				return genParserInfo(new XSParseString(),
					info + '(' + s + ')', declName, xdc);
			case "containsi":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				mask = GenRegex.genCaseInsensitive(s);
				xdc.setXDNamedItem("pattern", new DefString(".*"+mask+".*"));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "country":
				xdc.setXDNamedItem("pattern", new DefString("[a-ZA-Z]{2,3}"));
				return genParserInfo(new XSParseString(),
					info+"(); see ISO 3166-1 alpha-2,3",declName,xdc);
			case "currency":
				return genParserInfo(
					new XSParseString(), "(); see ISO 4217", declName, xdc);
			case "dateYMDhms":
				xdc.setXDNamedItem("pattern", new DefString(
					"\\d{4}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])"
					+"([0-1]\\d|2[0-3])([0-5]\\d)([0-5]\\d)"));
				return genParserInfo(
					new XSParseString(), info + "()", declName, xdc);
			case "dec":
				info += "(";
				mask = "-?\\d+[.,]\\d+";
				if (xdc.getXDNamedItem("totalDigits") != null) {
					s = xdc.getXDNamedItemAsString("totalDigits");
					xdc.removeXDNamedItem("totalDigits");
					info += s;
					int i = Integer.parseInt(s);
					if (xdc.getXDNamedItem("fractionDigits") != null) {
						s = xdc.getXDNamedItemAsString("fractionDigits");
						xdc.removeXDNamedItem("fractionDigits");
						info +="," + s;
						int j = Integer.parseInt(s);
						mask = (i-j > 1 ? "-?(\\d{0,"+(i-j)+"})" : "\\d")
							+ "([.,]\\d{1,"+j+"})?";
					} else {
						xdc.setXDNamedItem("maxLength",
							new DefString(String.valueOf(i + 1)));
					}
				}
				info += ")";
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "domainAddr":
				xdc.setXDNamedItem("pattern", new DefString(
					"[-0-9a-zA-Z_](\\.[-0-9a-zA-Z_]){0,99}"));
				return genParserInfo(new XSParseString(),
					info + "()", declName, xdc);
			case "emailAddr":
				xdc.setXDNamedItem("pattern", new DefString(
					"([ ]*\\([0-9a-zA-Z_ .-]*\\)[ ]*)*"
					+"(([0-9a-zA-Z_ .-]*<([0-9a-zA-Z_-]+(\\.[0-9a-zA-Z_-]*)*)"
					+ "@([0-9a-zA-Z_-]+(\\.[0-9a-zA-Z_-]*)*>))"
					+ "|(([0-9a-zA-Z_-]+(\\.[0-9a-zA-Z_-]*)*)"
					+ "@([0-9a-zA-Z_-]+(\\.[0-9a-zA-Z_-]*)*)))"
					+ "([ ]*\\([0-9a-zA-Z_ .-]*\\))?"));
				return genParserInfo(new XSParseString(),
					info + "()", declName, xdc);
			case "emailDate":
				xdc.setXDNamedItem("pattern",
					new DefContainer(GenRegex.getRegexes(
						"[EEE, ]d MMM yyyy HH:mm[:ss][ ZZZZZ][ (z)]"
						+ "|[EEE, ]d MMM YY HH:mm[:ss][ ZZZZZ][ (z)]")));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "ends":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("pattern", new DefString(".*"
					+ GenRegex.escapeCharsInString(s)));
				xdc.removeXDNamedItem("argument");
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "endsi":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				mask = GenRegex.genCaseInsensitive(s);
				xdc.setXDNamedItem("pattern", new DefString(".*" + mask));
				return genParserInfo(new XSParseString(),
					info + '(' + s + ')', declName, xdc);
			case "enum":
				x = xdc.getXDNamedItem("argument").getValue();
				xdc.setXDNamedItem("enumeration", x);
				xdc.removeXDNamedItem("argument");
				s = "";
				if (x instanceof XDContainer) {
					XDContainer y = (XDContainer) x;
					for (int i=0; i < y.getXDItemsNumber(); i++) {
						if (i > 0) s += ',';
						s += "" + y.getXDItem(i);
					}
				} else {
					s += "" + x;
				}
				return genParserInfo(new XSParseString(),
					info + '(' + s + ')', declName, xdc);
			case "eq":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("enumeration", new DefString(s));
				xdc.removeXDNamedItem("argument");
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "eqi":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				mask = GenRegex.genCaseInsensitive(s);
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "hex":
				return genParserInfo(new XSParseHexBinary(),info,declName,xdc);
			case "ipAddr":
				s = "((25[0-5])|(2[0-4][0-9])|([0-1]?[0-9][0-9])|([0-9]))";
				mask = "([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){1,7})"
					+"|(" + s + "(\\." + s + "){3})";
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "letters":
				info += genPars(xdc);
				xdc.setXDNamedItem("pattern", new DefString("[A-Za-z]+"));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "MD5":
				xdc.setXDNamedItem("length", new DefLong(16));
				return genParserInfo(new XSParseHexBinary(),
					info + genPars(xdc), declName, xdc);
			case "num":
				info += '(';
				if ((s = xdc.getXDNamedItemAsString("length")) != null) {
					xdc.removeXDNamedItem("length");
					mask = "{" + s + "}";
					info += s;
				} else if ((s = xdc.getXDNamedItemAsString("minLength"))!=null){
					xdc.removeXDNamedItem("minLength");
					mask = "{" + s;
					info += s;
					s = "," + xdc.getXDNamedItemAsString("maxLength");
					xdc.removeXDNamedItem("maxLength");
					mask += s +  "}";
					info += s;
				} else {
					mask = "+";
				}
				info += ")";
				xdc.setXDNamedItem("pattern", new DefString("\\d" + mask));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "pic":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				mask = "";
				for (char c: s.toCharArray()) {
					switch (c) {
						case '9':
							mask += "\\d";
							continue;
						case 'A':
							mask += "[a-zA-Z]";
							continue;
						case 'X':
							mask += "[0-9a-zA-Z]";
							continue;
						default:
							mask += GenRegex.createEscapedChar(c);
					}
				}
				xdc.setXDNamedItem("pattern", new DefString(mask));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "price":
				xdc.setXDNamedItem("pattern",
					new DefString("(0|[1-9]\\d*)(\\.\\d+)?[ ][A-Z]{3}"));
				return genParserInfo(new XSParseString(),
					info + "()", declName, xdc);
			case "printableDate":
				xdc.setXDNamedItem("pattern",
					new DefContainer(GenRegex.getRegexes(
						"[EEE ]MMM d HH:mm[:ss[.S]][ z] y"
						+ "|({L(*)}[EEE ]MMM d HH:mm[:ss[.S]][ z] y)")));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "regex":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				xdc.setXDNamedItem("pattern", new DefString(s));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "starts":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				xdc.setXDNamedItem("pattern", new DefString(
					GenRegex.escapeCharsInString(s)+"*"));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "startsi":
				s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				mask = GenRegex.genCaseInsensitive(s);
				xdc.setXDNamedItem("pattern", new DefString(mask + "*"));
				return genParserInfo(new XSParseString(),
					info + "(" + s + ")", declName, xdc);
			case "SHA1":
				xdc.setXDNamedItem("length", new DefLong(20));
				return genParserInfo(new XSParseHexBinary(),
					info + genPars(xdc), declName, xdc);
			case "telephone":
				xdc.setXDNamedItem("pattern", new DefString(
					"([+]\\d+[ ])?(\\d+([ ]\\d+)*)"));
				return genParserInfo(new XSParseString(), info, declName, xdc);
			case "xdatetime":
				mask = genXdOutFormat
					? xdc.getXDNamedItemAsString("outFormat") : null;
				if (mask == null || mask.isEmpty()) {
					mask = xdc.getXDNamedItemAsString("format");
					if (mask == null || mask.isEmpty()) {
						throw new RuntimeException("xdatetime params missing");
					}
				}
				info += '(' + mask;
				xdc.removeXDNamedItem("outFormat");
				xdc.removeXDNamedItem("format");
				for (XDNamedValue xv: xdc.getXDNamedItems()) {
					info += ",%" + xv.getName() + '=' + xv.getValue();
				}
				info += ')';
				switch (mask) {
					case "yyyy-MM-dd":
					case "yyyy-MM-ddZ":
						return genParserInfo(new XSParseDate(),
							info, declName, xdc);
					case "HH:mm:ss":
					case "HH:mm:ssZ":
					case "HH:mm:ss.S":
					case "HH:mm:ss.SZ":
						return genParserInfo(new XSParseTime(),
							info, declName, xdc);
					case "yyyy-MM-ddTHH:mm:ss":
					case "yyyy-MM-ddTHH:mm:ssZ":
					case "yyyy-MM-ddTHH:mm:ss.S":
					case "yyyy-MM-ddTHH:mm:ss.SZ":
						return genParserInfo(new XSParseDatetime(),
							info, declName, xdc);
					default: // return string type with patterns
						xdc.setXDNamedItem("pattern",
							new DefContainer(GenRegex.getRegexes(mask)));
						return genParserInfo(new XSParseString(),
							info, declName, xdc);
				} // switch mask
////////////////////////////////////////////////////////////////////////////////
// other X-definition types are converted to xs:string
////////////////////////////////////////////////////////////////////////////////
			default:
				// remove xdef parameters
				xdc.removeXDNamedItem("a1");
				xdc.removeXDNamedItem("a2");
				xdc.removeXDNamedItem("argument");
				xdc.removeXDNamedItem("outFormat");
				xdc.removeXDNamedItem("format");
				info += genPars(xdc);
				return genParserInfo(new XSParseString(),
					info, declName, new DefContainer());
		}
	}
}