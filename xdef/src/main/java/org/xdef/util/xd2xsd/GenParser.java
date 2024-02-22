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
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.impl.parsers.XSParseHexBinary;
import org.xdef.impl.parsers.XSParseString;
import org.xdef.impl.parsers.XSParseTime;
import org.xdef.model.XMData;
import org.xdef.sys.SException;

/** Creates from the X-definition parser a parser compatible with XML schema.
 * @author Trojan
 */
class GenParser {

	private final XDParser _xdp;
	private final String _info;
	private final String _declaredName;

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
	public XDParser getParser() {return _xdp;}

	/** Get information text (used in xs:documentation).
	 * @return information text.
	 */
	public String getInfo() {return _info;}

	/** Get name of declared type in X-definition.
	 * @return name of declared type in X-definition or null.
	 */
	public String getDeclaredName() {return _declaredName;}

	/** Set to the parser the declared name.
	 * @param xdp the X-definition parser.
	 * @param info information (i.e. original parser name).
	 * @param declaredName name of declared type (or null);.
	 * @param xdc X-container with parser parameters.
	 * @return ParserInfo object.
	 */
	private static GenParser createNewParserInfo(final XDParser xdp,
		final String info,
		final String declaredName,
		final XDContainer xdc) {
		try {
			GenParser result = new GenParser(xdp, declaredName, info);
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
	 * @return ParserInfo object with XML schema compatible parser.
	 */
	public static GenParser genParser(final XMData xmd) {
		XDParser xdp = (XDParser) xmd.getParseMethod();
		String name = xdp.parserName();
		String declName = xdp.getDeclaredName();
		XDContainer xdc = xdp.getNamedParams();
		String info = "Created from X-definition " + name;
		switch(name) {
			case "an":
				info += genPars(xdc);
				xdc.setXDNamedItem("pattern", new DefString("[A-Za-z0-9]+"));
				return createNewParserInfo(
					new XSParseString(), info, declName, xdc);
			case "contains": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.setXDNamedItem("pattern",
					new DefString(".*" + GenRegex.genEscapeChars(s) + ".*"));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + '(' + s + ')', declName, xdc);
			}
			case "containsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = GenRegex.genCaseInsensitive(s);
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
					+ GenRegex.genEscapeChars(s)));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "endsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = GenRegex.genCaseInsensitive(s);
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
				xdc.setXDNamedItem("enumeration", new DefString(s));
				xdc.removeXDNamedItem("argument");
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "eqi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = GenRegex.genCaseInsensitive(s);
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
							mask += GenRegex.genEscapedChar(c);
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
					GenRegex.genEscapeChars(s)+"*"));
				return createNewParserInfo(
					new XSParseString(), info + "(" + s + ")", declName, xdc);
			}
			case "startsi": {
				String s = xdc.getXDNamedItemAsString("argument");
				xdc.removeXDNamedItem("argument");
				String mask = GenRegex.genCaseInsensitive(s);
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
				info += ')';
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
						String[] patterns = GenRegex.getRegexes(mask);
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
				GenParser result = new GenParser(xdp, declName, null);
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
}