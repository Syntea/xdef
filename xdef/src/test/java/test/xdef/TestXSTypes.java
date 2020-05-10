package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.compile.XScriptParser;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SUtils;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xdef.impl.XConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Test XML schema and X-definition types.
 * @author Vaclav Trojan
 */
public final class TestXSTypes extends XDTester {

	public TestXSTypes() {super();}

	private String _msg = "";
	private XDPool _xd;
	private String _xml = "";
	private String _xdef = "";
	private boolean _result = false;
	private String _params;

	private DocumentBuilderFactory _builderFactory;
	private DocumentBuilder _builder;
	private Validator _validator;
	private String _schema = "";
	private ErrorHandler _errHandler = new ErrorHandler() {
		@Override
		final public void warning(final SAXParseException x)
			throws SAXException{setMesage(x);}
		@Override
		final public void error(final SAXParseException x)
			throws SAXException {setMesage(x);}
		@Override
		final public void fatalError(final SAXParseException x)
			throws SAXException {setMesage(x);}
	};
	private final EntityResolver _entityResolver =  new EntityResolver() {
		@Override
		final public InputSource resolveEntity(final String publicId,
			final String systemId) throws SAXException, IOException {
			return new InputSource(new StringReader(_schema));
		}
	};

	private boolean chkSchema(final String result) {
		if (_schema == null || _schema.isEmpty()) {
			return true; //do not check schema if it is not available
		}
		try {
			Document doc = null;
			if (_xml != null) {
				doc = _builder.parse(new InputSource(new StringReader(_xml)));
				doc.getDocumentElement();
			}
			if (_xml != null) {
				_validator.validate(new DOMSource(doc));
			}
			if (result != null) {
				String s = doc.getDocumentElement().getAttribute("a");
				if (_msg.length() == 0 && !result.equals(s)) {
					if (_msg.length() > 0) {
						_msg += "\n";
					}
					_msg += "SCHEMA: attr result differs: '" + s +
						"'; expected: '" + result +  "'";
				}
				Node n = doc.getDocumentElement().getChildNodes().item(0);
				s = (n == null) ? null : n.getNodeValue();
				if (_msg.length() == 0 && !result.equals(s)) {
					if (s != null || result.length() > 0) {
						if (_msg.length() > 0) {
							_msg += "\n";
						}
						_msg += "SCHEMA: text result differs: '" + s +
							"'; expected: '" + result +  "'";
					}
				}
				if (_msg.length() != 0) {
					_result = false;
				}
			} else {
				if (_msg.length() == 0) {
					if (_msg.length() > 0) {
						_msg += "\n";
					}
					_msg += "SCHEMA: error not reported";
					_result = false;
				}
			}
		} catch (Exception ex) {
			if (result != null) {
				if (_msg.length() > 0) {
					_msg += "\n";
				}
				_msg += "SCHEMA: " + ex;
				_result = false;
			}
		}
		return _result;
	}

	private void setMesage(SAXParseException x) {
		String s = "SCHEMA: " + x.getMessage() + "\n";
		if (_msg.indexOf(s) < 0) {//if it was not yet reported
			_msg += s;
		}
	}

	private void init() throws Exception {
		if (XDTester.getFulltestMode()) {
			_builderFactory = DocumentBuilderFactory.newInstance();
			_builderFactory.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
			_builderFactory.setCoalescing(true);
			_builderFactory.setNamespaceAware(true);
			_builderFactory.setExpandEntityReferences(true);
			_builderFactory.setIgnoringComments(true);
			_builderFactory.setIgnoringElementContentWhitespace(true);
			_builderFactory.setValidating(true);
			_builder = _builderFactory.newDocumentBuilder();
			_builder.setErrorHandler(_errHandler);
			_builder.setEntityResolver(_entityResolver);
		}
	}

	private static void readString(final XScriptParser p,
		final StringBuffer sb) {
		if (p._sym == XScriptParser.MINUS_SYM) {
			sb.append('-');
			p.nextSymbol();
		}
		if (p._sym != XScriptParser.CONSTANT_SYM) {
			throw new RuntimeException("Value expected");
		}
		sb.append(p._parsedValue.toString());
		p.nextSymbol();
	}

	private static void addRestriction(final XScriptParser p,
		final String paramName,
		final String indent,
		final StringBuffer sb) {
		sb.append(indent).append("<xs:").append(paramName).append(" value='");
		readString(p, sb);
		sb.append("'/>\n");
	}

	private static boolean isSymbol(final XScriptParser p, final char symbol) {
		if (p._sym == symbol) {
			p.nextSymbol();
			return true;
		}
		return false;
	}

	private static String parseKeyParam(final XScriptParser p) {
		if (p._sym != XScriptParser.MOD_SYM) {
			return null;
		}
		if (!p.isNCName((byte) 10)) {
			throw new RuntimeException("Key identifier expected");
		}
		String result = p.getParsedString();
		if (p.nextSymbol() != XScriptParser.ASSGN_SYM) {
			throw new RuntimeException("= expected");
		}
		p.nextSymbol();
		return result;
	}

	private static String readTypeName(final XScriptParser p) {
		if (p._sym != XScriptParser.IDENTIFIER_SYM) {
//		if (p._sym != XScriptParser.IDENTIFIER_SYM ||
//			p._idName.indexOf(':') != 2 && p._idName.indexOf('_') != 2) {
			throw new RuntimeException("type name expected");
		}
		String result = p._idName.replace('_',':');
		if (!result.startsWith("xs:")) {
			result = "xs:" + result;
		}
		p.nextSymbol();
		return result;
	}

	private static boolean parseRestriction(final XScriptParser p,
		final String indent,
		final StringBuffer sb) {
		String[] names = new String[] {
			"pattern",
			"enumeration",
			"whiteSpace",
			"maxInclusive",
			"maxExclusive",
			"minInclusive",
			"minExclusive",
			"totalDigits",
			"fractionDigits",
			"length",
			"maxLength",
			"minLength"
		};
		String param = parseKeyParam(p);
		if (param == null) {
			return false;
		}
		int i = 0;
		for (; i < names.length; i++) {
			if (names[i].equals(param)) {
				break;
			}
		}
		if (i == names.length) {
			throw new RuntimeException("Unknown key parameter" + param);
		}
		if (i <= 1) {//pattern, enumeration => list
			if (!isSymbol(p, XScriptParser.LSQ_SYM)) {
				throw new RuntimeException("'[' expected");
			}
			if (!isSymbol(p, XScriptParser.RSQ_SYM)) {//not empty list
				for (;;) {
					addRestriction(p, param, indent, sb);
					if (isSymbol(p, XScriptParser.RSQ_SYM)) {
						break;
					}
					if (!isSymbol(p, XScriptParser.COMMA_SYM)) {
						throw new RuntimeException("Error in key parameters");
					}
				}
			}
		} else {
			addRestriction(p, param, indent, sb);
		}
		return true;
	}

	private static void genSimpleType(final String type,
		final XScriptParser p,
		final String indent,
		final StringBuffer sb) {
		sb.append(indent).append("<xs:simpleType>\n");
		sb.append(indent).append("  <xs:restriction base='");
		sb.append(type);
		if (isSymbol(p, XScriptParser.LPAR_SYM)) {
			sb.append("'>\n");
			if (!isSymbol(p, XScriptParser.RPAR_SYM)) { //not empty list
				for(;;) {
					parseRestriction(p, indent + "    ", sb);
					if (isSymbol(p, XScriptParser.RPAR_SYM)) {
						break;
					}
					if (!isSymbol(p, XScriptParser.COMMA_SYM)) {
						throw new RuntimeException(
							"Error in key parameter sequence");
					}
				}
			}
			sb.append(indent).append("  </xs:restriction>\n");
		} else {
			sb.append("'/>\n");
		}
		sb.append(indent).append("</xs:simpleType>\n");
	}

	private static void genUnionItem(final XScriptParser p,
		final String indent,
		final StringBuffer sb) {
		if (!"item".equals(parseKeyParam(p))) {
			throw new RuntimeException(
				"'%item' expexted as first parameter of union");
		}
		if (!isSymbol(p, XScriptParser.LSQ_SYM)) {
			throw new RuntimeException("'[' expected after %item of union");
		}
		sb.append(indent).append("  <xs:simpleType>\n");
		sb.append(indent).append("    <xs:union>\n");
		if (!isSymbol(p, XScriptParser.RSQ_SYM)) {//not empty list
			for (;;) {
				String type = readTypeName(p);
				if ("xs:list".equals(type)) {
					if (isSymbol(p, XScriptParser.LPAR_SYM)) {
						sb.append(indent).append("      <xs:simpleType>\n");
						genListItem(p, indent + "        ", sb);
						sb.append(indent).append("      </xs:simpleType>\n");
						if (!isSymbol(p, XScriptParser.RPAR_SYM)) {
							throw new RuntimeException("')' missing:" +
								p.getParsedBufferPartFrom(0));
						}
					}
				} else {
					genSimpleType(type, p, indent + "      ", sb);
				}
				if (isSymbol(p, XScriptParser.RSQ_SYM)) {
					break;
				}
				if (!isSymbol(p, XScriptParser.COMMA_SYM)) {
					throw new RuntimeException("Error on union %item list");
				}
			}
		}
		sb.append(indent).append("    </xs:union>\n");
		sb.append(indent).append("  </xs:simpleType>\n");
	}

	private static void genListItem(final XScriptParser p,
		final String indent,
		final StringBuffer sb) {
		if (!"item".equals(parseKeyParam(p))) {
			throw new RuntimeException("'%item' expexted as first parameter");
		}
		String type = readTypeName(p);
		sb.append(indent).append("<xs:restriction>\n");
		sb.append(indent).append("  <xs:simpleType>\n");
		sb.append(indent).append("    <xs:list>\n");
		if ("xs:union".equals(type)) {
			if (isSymbol(p, XScriptParser.LPAR_SYM)) {
				genUnionItem(p, indent + "      ", sb);
				if (!isSymbol(p, XScriptParser.RPAR_SYM)) {
					throw new RuntimeException("')' missing after union list");
				}
			} else {
				throw new RuntimeException("'(' missing after union");
			}
		} else {
			genSimpleType(type, p, indent + "      ", sb);
		}
		sb.append(indent).append("    </xs:list>\n");
		sb.append(indent).append("  </xs:simpleType>\n");
		while(isSymbol(p, XScriptParser.COMMA_SYM)) {
			parseRestriction(p, indent + "  ", sb);
		}
		sb.append(indent).append("</xs:restriction>\n");
	}

	private static void genSchemaType(final XScriptParser p,
		final String indent,
		final StringBuffer sb) {
		String type = readTypeName(p);
		if (type == null) {
			throw new RuntimeException("name of type expected");
		}
		boolean wasLpar = isSymbol(p, XScriptParser.LPAR_SYM);
		if ("xs:union".equals(type)) {
			if (!wasLpar) {
				throw new RuntimeException("'(' expected");
			}
			sb.append(indent).append("<xs:restriction>\n");
			genUnionItem(p, indent + "  ", sb);
			while(isSymbol(p, XScriptParser.COMMA_SYM)) {
				parseRestriction(p, "  ", sb);
			}
			sb.append(indent).append("</xs:restriction>\n");
		} else if ("xs:list".equals(type)) {
			if (!wasLpar) {
				throw new RuntimeException("'(' expected");
			}
			genListItem(p, indent, sb);
		} else {
			sb.append(indent).append("<xs:restriction base=\"");
			if (wasLpar) {
				sb.append(type).append("\">\n");
				if (parseRestriction(p, indent + "  ", sb)) {
					while(isSymbol(p, XScriptParser.COMMA_SYM)) {
						if (!parseRestriction(p, indent + "  ", sb)) {
							throw new RuntimeException("Restriction expected");
						}
					}
				}
				sb.append(indent).append("</xs:restriction>\n");
			} else {
				sb.append(type).append("\"/>\n");
			}
		}
		if (wasLpar) {
			if (!isSymbol(p, XScriptParser.RPAR_SYM)) {
				throw new RuntimeException("')' expected");
			}
		}
	}

	private static String genSchema(final String params) {
		StringBuffer sb = new StringBuffer(
"<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"+
"<xs:simpleType name='mytype'>\n");
		XScriptParser p = new XScriptParser((byte) 10);
		p.setSource(new SBuffer(params), null, null, XConstants.XD31, null);
		p.nextSymbol();
		genSchemaType(p, "  ", sb);
		sb.append(
"</xs:simpleType>\n"+
"<xs:element name='a'>\n"+
"  <xs:complexType>\n"+
"    <xs:simpleContent>\n"+
"      <xs:extension base=\"mytype\">\n"+
"        <xs:attribute name='a' type='mytype' use='required'/>\n"+
"      </xs:extension>\n"+
"    </xs:simpleContent>\n"+
"  </xs:complexType>\n"+
"</xs:element>\n"+
"</xs:schema>");
		return sb.toString();
	}

	void genXDef() {
		_xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"  script='options noTrimAttr,preserveEmptyAttributes,\n"+
"    preserveAttrWhiteSpaces, noTrimText,preserveTextWhiteSpaces'>\n"+
"  <a a=\"" + _params + "\">\n"+
"    optional " + _params + "\n"+
"  </a>\n"+
"</xd:def>";
	}

	private XDPool genXDPool(final String xdef) {
		return compile(xdef);
	}

	private boolean chkXDef(final String result) {
		boolean fits = true;
		try {
			if (_xml != null) {
				ArrayReporter reporter = new ArrayReporter();
				Element el = _xd.createXDDocument().xparse(_xml, reporter);
				if (result != null) {
					if (reporter.errorWarnings()) {
						if (_msg.length() > 0) {
							_msg += "\n";
						}
						_msg += reporter.printToString();
						fits = false;
					}
					String s = el.getAttribute("a");
					if (fits && !result.equals(s)) {
						if (_msg.length() > 0) {
							_msg += "\n";
						}
						_msg += "XDEF: attr result differs: '" + s +
							"'; expected: '" + result + "'";
						fits = false;
					}
					Node n = el.getChildNodes().item(0);
					if (n == null) {
						s = null;
					} else {
						s = n.getNodeValue();
					}
					if (fits && !result.equals(s)) {
						if (s != null || result.length() > 0) {
							if (_msg.length() > 0) {
								_msg += "\n";
							}
							_msg += "XDEF: text result differs: '" + s +
								"'; expexted: '" + result + "'";
							fits = false;
						}
					}
				} else {
					if (!reporter.errorWarnings()) {
						if (_msg.length() > 0) {
							_msg += "\n";
						}
						_msg += "XDEF: Error not reported";
						fits = false;
					}
				}
			}
		} catch (Exception ex) {
			if (result != null) {
				if (_msg.length() > 0) {
					_msg += "\n";
				}
				_msg += "" + ex;
				fits = false;
			}
		}
		return fits;
	}

	private boolean checkFail(final String params) {
		if (prepare(params)) {
			_msg = "Neither Schema nor XDef recognize this error";
			return false;
		}
		boolean result = true;
		if (XDTester.getFulltestMode()) {
			if (_msg.indexOf("SCHEMA: ") < 0) {
				_msg = "Not recognized by SCHEMA:\n"+ _msg;
				result = false;
			}
			if (_msg.indexOf("XDEF: ") < 0) {
				_msg = "Not recognized by XDEF:\n"+ _msg;
				result = false;
			}
		} else {
			if (_msg.indexOf("XDEF: ") < 0) {
				_msg = "Not recognized by XDEF:\n"+ _msg;
				result = false;
			}
		}
		return result;
	}

	private boolean prepare(final String params) {
		_params = params;
		_msg = "";
		_schema = "";
		boolean result = true;
		if (XDTester.getFulltestMode()) {
			try {
				_schema = genSchema(_params);
				SchemaFactory factory = SchemaFactory.newInstance(
					"http://www.w3.org/2001/XMLSchema");
				Schema schema = factory.newSchema(
					new StreamSource(new StringReader(_schema)));
				factory.setErrorHandler(_errHandler);
				_validator = schema.newValidator();
				_validator.setErrorHandler(_errHandler);
			} catch (Exception ex) {
				result = false;
				if (_msg.length() > 0) {
					_msg += "\n";
				}
				_msg += "SCHEMA: " + ex.getMessage();
			}
		}
		try {
			genXDef();
			_xd = genXDPool(_xdef);
		} catch (Exception ex) {
			result = false;
			if (_msg.length() > 0) {
				_msg += "\n";
			}
			if (ex.getMessage() == null) {
				java.io.StringWriter sw = new java.io.StringWriter();
				java.io.PrintWriter pw = new java.io.PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				_msg += "XDEF exception:\n"+ sw.toString();
			} else {
				_msg += "XDEF: " + ex.getMessage();
			}

		}
		return result;
	}

	private boolean parse(final String data, final String result) {
		_msg = "";
		_result = true;
		_xml = data == null ? null :
("<a xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
" xsi:noNamespaceSchemaLocation=\"dummy\" a=\""+data+"\"\n>"+data+"</a>");
		return XDTester.getFulltestMode()
			? chkSchema(result) & chkXDef(result) : chkXDef(result);
	}

	private boolean parse(final String data) {
		String s = data.trim(); //this is collapsed string
		int i = 0;
		while ((i = s.indexOf("\n\r", i)) >= 0) {
			if (i > 0) {
				s = s.substring(0, i) + " " + s.substring(i + 2);
			} else {
				s = " " + s.substring(2);
			}
		}
		s = s.replace('\n', ' ');
		s = s.replace('\r', ' ');
		s = s.replace('\t', ' ');
		i = 0;
		while ((i = s.indexOf("  ", i)) >= 0) {
			if (i > 0) {
				s = s.substring(0, i) + s.substring(i + 1);
			} else {
				s = s.substring(1);
			}
		}
		return parse(data, s);
	}

	private boolean parseFail(final String data) {
		return parse(data, null);
	}

	@Override
	public void test() {
		try {
			init();
		} catch (Exception ex) {
			fail(ex);
			resetTester();
			return;
		}
		boolean chkSyntax = getChkSyntax();
		setProperty("xdef.minyear", null);
		setProperty("xdef.maxyear", null);

//------------------------------------------------------------------------------
//                          TESTING string
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("string(%maxInclusive='asdf')"), _msg);
		assertTrue(checkFail("string(%maxExclusive='qwer')"), _msg);
		assertTrue(checkFail("string(%minInclusive='zxcv')"), _msg);
		assertTrue(checkFail("string(%minExclusive='tyui')"), _msg);
		assertTrue(checkFail("string(%fractionDigits='3')"), _msg);
		assertTrue(checkFail("string(%totalDigits='6')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing correct values
		assertTrue(prepare("string()"), _msg);
		assertTrue(parse("Hello World!"), _msg);

		// testing errors
		if (!XDTester.getFulltestMode()) {
			assertFalse(parse(""), _msg); //schema accepts empty string!!!
		}

		// testing facets
		assertTrue(prepare("string(%enumeration=['Hello', 'world'])"), _msg);
		assertTrue(parse("Hello"), _msg);
		assertTrue(parse("world"), _msg);
		assertTrue(parseFail("hello"), _msg);
		assertTrue(parseFail("World"), _msg);
		assertTrue(parseFail(" world"), _msg);
		assertTrue(prepare("string(%enumeration=['Hello world'])"), _msg);
		assertTrue(parse("Hello world"), _msg);
		assertTrue(parseFail("Hello   world"), _msg);
		assertTrue(prepare("string(%enumeration=['Hello   world'])"), _msg);
		assertTrue(parse("Hello   world", "Hello   world"), _msg);
		assertTrue(parseFail(" Hello   world"), _msg);
		assertTrue(parseFail("Hello world"), _msg);

		assertTrue(prepare("string(%enumeration=['Hello world'],"
			+ "%whiteSpace='collapse')"), _msg);
		assertTrue(parse("  \nHello \n  world \n ", "Hello world"), _msg);
		assertTrue(parseFail("Helloworld"), _msg);

		assertTrue(prepare("string(%whiteSpace='replace')"), _msg);
		assertTrue(parse(" \nHello \n world \t ", "  Hello   world   "), _msg);

		assertTrue(prepare("string(%whiteSpace='preserve')"), _msg);
		assertTrue(parse("   Hello   world  ", "   Hello   world  "), _msg);

		assertTrue(prepare("string(%pattern=['[A-Z][a-z]{3}'])"), _msg);
		assertTrue(parse("Fork"), _msg);
		assertTrue(parse("Enum"), _msg);
		assertTrue(parseFail("dark"), _msg);
		assertTrue(parseFail("Egg"), _msg);
		assertTrue(parseFail("9asd"), _msg);
		assertTrue(parseFail(" Fork"), _msg);

		assertTrue(prepare("string(%length='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parse(" sdf "," sdf "), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("asdf"), _msg);

		assertTrue(prepare("string(%minLength='3')"), _msg);
		assertTrue(parse("asdfgh"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("as"), _msg);

		assertTrue(prepare("string(%maxLength='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("as dfg"), _msg);

		assertTrue(prepare("string(%whiteSpace='collapse')"), _msg);
		assertTrue(parse("   Hello   world!  "), _msg);
		if (XDTester.getFulltestMode()) {
			assertTrue(parse("     ", ""), _msg);
		}

		assertTrue(prepare(
			"string(%whiteSpace='collapse',%minLength='1')"), _msg);
		assertTrue(parse("  a  "), _msg);
		assertTrue(parseFail("    "), _msg);

		assertTrue(prepare(
			"string(%whiteSpace='collapse',%length='0')"), _msg);
		if (XDTester.getFulltestMode()) {
			assertTrue(parse(""), _msg);
			assertTrue(parse("    "), _msg);//should return empty string
		}
		assertTrue(parseFail("a"), _msg);

		assertTrue(prepare(
			"string(%whiteSpace='collapse',%length='1')"), _msg);
		assertTrue(parse("  a  "), _msg);
		assertTrue(parseFail("  ab  "), _msg);
		assertTrue(parseFail("    "), _msg);

		if (XDTester.getFulltestMode()) {
			assertTrue(prepare(
				"string(%whiteSpace='collapse',%minLength='0')"), _msg);
			assertTrue(parse("    ", ""), _msg); //should return text value null
		}

		assertTrue(prepare("string(%whiteSpace='replace')"), _msg);
		assertTrue(parse("\r\t Hello  world! \n", "   Hello  world!  "), _msg);


//------------------------------------------------------------------------------
//                          TESTING normalizedstring
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("normalizedString(%maxInclusive='asdf')"),_msg);
		assertTrue(checkFail("normalizedString(%maxExclusive='qwer')"),_msg);
		assertTrue(checkFail("normalizedString(%minInclusive='zxcv')"),_msg);
		assertTrue(checkFail("normalizedString(%minExclusive='tyui')"),_msg);
		assertTrue(checkFail("normalizedString(%fractionDigits='3')"), _msg);
		assertTrue(checkFail("normalizedString(%totalDigits='6')"), _msg);
		assertTrue(checkFail("normalizedString(%whiteSpace='preserve')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing correct values
		assertTrue(prepare("normalizedString(%whiteSpace='collapse')"),_msg);
		assertTrue(parse("Hello World!"), _msg);
		assertTrue(prepare("normalizedString(%whiteSpace='replace')"),_msg);
		assertTrue(parse("Hello World!"), _msg);

		if (!XDTester.getFulltestMode()) {
//			assertTrue(parseFail(""), _msg); //schema accepts empty string???
		}

		// testing facets
		assertTrue(prepare(
			"normalizedString(%enumeration=['Hello', 'world'])"), _msg);
		assertTrue(parse("Hello"), _msg);
		assertTrue(parse("world"), _msg);
		assertTrue(parseFail("hello"), _msg);
		assertTrue(parseFail("World"), _msg);
		assertTrue(prepare(
			"normalizedString(%enumeration=['Hello world'])"), _msg);
		assertTrue(parse("Hello world"), _msg);
		assertTrue(parseFail("Hello   world"), _msg);
		assertTrue(prepare(
			"normalizedString(%enumeration=['Hello   world'])"), _msg);
		assertTrue(parse("Hello   world", "Hello   world"), _msg);
		assertTrue(parseFail(" Hello   world"), _msg);
		assertTrue(parseFail("Hello world"), _msg);

		assertTrue(prepare("normalizedString(%enumeration=['Hello world'],"
			+ "%whiteSpace='collapse')"), _msg);
		assertTrue(parse("  \nHello \n  world \n ", "Hello world"), _msg);
		assertTrue(parseFail("Helloworld"), _msg);

		assertTrue(prepare("normalizedString(%whiteSpace='replace')"), _msg);
		assertTrue(parse("  \nHello \n  world \t ", "   Hello    world   "),
			_msg);

		assertTrue(prepare("normalizedString(%whiteSpace='replace',"
			+ "%enumeration=['\n Being a Dog Is \n a Full-Time Job\n',"
			+ " '\nBla\n'])"), _msg);
		assertTrue(parse("\n Being a Dog Is \n a Full-Time Job\n",
			"  Being a Dog Is   a Full-Time Job "), _msg);
		assertTrue(parse("\nBla\n", " Bla "), _msg);

		assertTrue(prepare("normalizedString(%pattern=['[A-Z][a-z]{3}'])"),
			_msg);
		assertTrue(parse("Fork"), _msg);
		assertTrue(parse("Enum"), _msg);
		assertTrue(parseFail("dark"), _msg);
		assertTrue(parseFail("Egg"), _msg);
		assertTrue(parseFail("9asd"), _msg);
		assertTrue(parseFail(" Fork"), _msg);
		assertTrue(parseFail("\nFork"), _msg);

		assertTrue(prepare("normalizedString(%length='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parse("\nsdf\n"," sdf "), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("asdf"), _msg);

		assertTrue(prepare("normalizedString(%minLength='3')"), _msg);
		assertTrue(parse("asdfgh"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("as"), _msg);

		assertTrue(prepare("normalizedString(%maxLength='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("as dfg"), _msg);

		assertTrue(prepare("normalizedString(%whiteSpace='collapse')"), _msg);
		assertTrue(parse("   Hello   world!  "), _msg);
		if (XDTester.getFulltestMode()) {
			assertTrue(parse("     ", ""), _msg);
		}

		assertTrue(prepare(
			"normalizedString(%whiteSpace='collapse',%minLength='1')"),_msg);
		assertTrue(parse("  a  "), _msg);
		assertTrue(parseFail("    "), _msg);

		assertTrue(prepare(
			"normalizedString(%whiteSpace='collapse',%length='0')"), _msg);
		if (XDTester.getFulltestMode()) {
			assertTrue(parse(""), _msg);
			assertTrue(parse("    "), _msg);//should return empty string
		}
		assertTrue(parseFail("a"), _msg);

		assertTrue(prepare(
			"normalizedString(%whiteSpace='collapse',%length='1')"), _msg);
		assertTrue(parse("  a  "), _msg);
		assertTrue(parseFail("  ab  "), _msg);
		assertTrue(parseFail("    "), _msg);

		if (XDTester.getFulltestMode()) {
			assertTrue(prepare(
				"normalizedString(%whiteSpace='collapse',%minLength='0')"),
				_msg);
			assertTrue(parse("    ", ""), _msg); //should return text value null
		}

//------------------------------------------------------------------------------
//                          TESTING anyURI
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("anyURI(%minInclusive='1')"), _msg);
		assertTrue(checkFail("anyURI(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("anyURI(%minExclusive='1')"), _msg);
		assertTrue(checkFail("anyURI(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("anyURI(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("anyURI(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("anyURI(%totalDigits='2')"), _msg);
		assertTrue(checkFail("anyURI(%fractionDigits='2')"), _msg);
		assertTrue(prepare("anyURI(%whiteSpace='collapse')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing correct values
		assertTrue(prepare("anyURI"), _msg);
		assertTrue(parse("http://www.syntea.cz"), _msg);
		assertTrue(parse("www.syntea.cz"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse("0123456789abcdef"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("1234"), _msg);
		assertTrue(parse("00"), _msg);
		assertTrue(parse("/"), _msg);
		assertTrue(parse("  \t\n\r0000000000000000000000\t\n\r  "), _msg);

		// testing errors
		//This is not recobnized by Java 1.3
		assertTrue(parseFail(":"), _msg);
		assertTrue(parseFail(":a"), _msg);
		assertTrue(parseFail("a:"), _msg);
		// testing facets
		assertTrue(prepare("anyURI(%length='3')"), _msg);
		assertTrue(parse("w.a"), _msg);
//		assertTrue(parseFail("w a"), _msg); //schema not recognizes

		assertTrue(prepare("anyURI(%enumeration=['www.a.cz','www.b.cz'])"),
			_msg);
		assertTrue(parse("www.a.cz"), _msg);
		assertTrue(parse("www.b.cz"), _msg);
		assertTrue(parse(" \n www.a.cz\t ", "www.a.cz"), _msg);
		assertTrue(parseFail("www.c.cz"), _msg);

		assertTrue(prepare(
			"anyURI(%pattern=['ffff','www\\\\.[a-z]+\\\\.cz'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("www.b.cz"), _msg);
		assertTrue(parseFail("www.a.cz www.b.cz"), _msg);
		assertTrue(parseFail("a.b.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING boolean
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("boolean(%enumeration=['1', 'false']"), _msg);
		assertTrue(checkFail("boolean(%length='1')"), _msg);
		assertTrue(checkFail("boolean(%minLength='1')"), _msg);
		assertTrue(checkFail("boolean(%maxLength='1')"), _msg);
		assertTrue(checkFail("boolean(%minInclusive='1')"), _msg);
		assertTrue(checkFail("boolean(%minExclusive='1')"), _msg);
		assertTrue(checkFail("boolean(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("boolean(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("boolean(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("boolean(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("boolean(%totalDigits='2')"), _msg);
		assertTrue(checkFail("boolean(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking
		// testing fixed facets
		assertTrue(prepare("boolean(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("boolean"), _msg);
		assertTrue(parse("true"), _msg);
		assertTrue(parse("  false "), _msg);
		assertTrue(parse("  false "), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse(" 1 "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail(" 2 "), _msg);
		assertTrue(parseFail("00"), _msg);
		assertTrue(parseFail("+1"), _msg);
		assertTrue(parseFail("-1"), _msg);
		assertTrue(parseFail("1.5"), _msg);
		assertTrue(parseFail(" x "), _msg);
		assertTrue(parseFail(" TRUE "), _msg);

		// testing facets
		assertTrue(prepare("boolean(%pattern=['1','true'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  true  "), _msg);
		assertTrue(parseFail("false"), _msg);

		assertTrue(prepare("boolean"), _msg);
		assertTrue(parse("0"), _msg);

//------------------------------------------------------------------------------
//                          TESTING decimal
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("decimal(%length='1')"), _msg);
		assertTrue(checkFail("decimal(%minLength='1')"), _msg);
		assertTrue(checkFail("decimal(%maxLength='1')"), _msg);
		assertTrue(checkFail("decimal(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("decimal(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"decimal(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"decimal(%minInclusive='1',%maxInclusive='0')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("decimal(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("decimal"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse(".00"), _msg);
		assertTrue(parse("-0.1"), _msg);
		assertTrue(parse("-1.23"), _msg);
		assertTrue(parse(".123"), _msg);
		assertTrue(parse("123."), _msg);
		assertTrue(parse(" 5. "), _msg);
		assertTrue(parse(" -5. "), _msg);
		assertTrue(parse("-.5"), _msg);
		assertTrue(parse("126789675432336472147483647"), _msg);
		assertTrue(parse("+1000002147483647.14748364721474836472"), _msg);
		assertTrue(parse("-214748364821474836482147483648"), _msg);
		assertTrue(parse("-214748364821474836482.147483648"), _msg);
		assertTrue(parse("\n 210 \n\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("..5"), _msg);
		assertTrue(parseFail("+..5"), _msg);

		// testing facets
		assertTrue(prepare("decimal(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(prepare("decimal(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare("decimal(%minInclusive='1',%maxInclusive='2')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("decimal(%minExclusive='0',%maxExclusive='3')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("decimal(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("decimal(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("decimal(%pattern=['\\\\d'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("33"), _msg);

		assertTrue(prepare("decimal(%totalDigits='2')"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("+11"), _msg);
		assertTrue(parse("-11"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("111"), _msg);

		assertTrue(prepare("decimal(%fractionDigits='2')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("1.23"), _msg);
		assertTrue(parseFail("1.234"), _msg);

		assertTrue(prepare("decimal(%totalDigits='2',%fractionDigits='2')"),
			_msg);
		assertTrue(parse("0.23"), _msg);
		assertTrue(parseFail("1.23"), _msg);

		assertTrue(prepare("decimal"), _msg);
		assertTrue(parse("   123   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING integer
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("integer(%length='1')"), _msg);
		assertTrue(checkFail("integer(%minLength='1')"), _msg);
		assertTrue(checkFail("integer(%maxLength='1')"), _msg);
		assertTrue(checkFail("integer(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("integer(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"integer(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"integer(%minInclusive='1',%maxInclusive='0')"), _msg);
// This is not recognized by SCHEMA
//		assertTrue(checkFail("integer(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("integer(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("integer"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("214748364721474836472147483647"), _msg);
		assertTrue(parse("-214748364821474836482147483648"), _msg);
		assertTrue(parse("\n 127 \n\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("0.5"), _msg);

		// testing facets
		assertTrue(prepare("integer(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("integer(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare("integer(%minInclusive='1',%maxInclusive='2')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("integer(%minExclusive='0',%maxExclusive='3')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("integer(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("integer(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("integer(%pattern=['\\\\d'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("33"), _msg);

		assertTrue(prepare("integer(%totalDigits='2')"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("+00000000011"), _msg);
		assertTrue(parse("-00000000011"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("111"), _msg);

//------------------------------------------------------------------------------
//                          TESTING negativeInteger
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("negativeInteger(%length='1')"), _msg);
		assertTrue(checkFail("negativeInteger(%minLength='1')"), _msg);
		assertTrue(checkFail("negativeInteger(%maxLength='1')"), _msg);
		assertTrue(checkFail("negativeInteger(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("negativeInteger(%whiteSpace='replace')"),_msg);
		assertTrue(checkFail(
			"negativeInteger(%minInclusive='-1',%minExclusive='-1')"), _msg);
		assertTrue(checkFail(
			"negativeInteger(%minInclusive='-1',%maxInclusive='-2')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("negativeInteger(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("negativeInteger(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("negativeInteger"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("-214748364821474836482147483648"), _msg);
		assertTrue(parse("\n -127 \n\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("1"), _msg);
		assertTrue(parseFail("+1"), _msg);
		assertTrue(parseFail("+0"), _msg);
		assertTrue(parseFail("-0"), _msg);
		assertTrue(parseFail("0.5"), _msg);
		assertTrue(parseFail("214748364721474836472147483647"), _msg);

		// testing facets
		assertTrue(prepare("negativeInteger(%minInclusive='-1')"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parseFail("-2"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("negativeInteger(%minExclusive='-1')"), _msg);
		assertTrue(parseFail("-1"), _msg);

		assertTrue(prepare(
			"negativeInteger(%minInclusive='-2',%maxInclusive='-1')"),_msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("-2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("-3"), _msg);

		assertTrue(prepare(
			"negativeInteger(%minExclusive='-3',%maxExclusive='-1')"),_msg);
		assertTrue(parse("-2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("-3"), _msg);

		assertTrue(prepare("negativeInteger(%enumeration=['-1','-3'])"),
			_msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("-3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("negativeInteger(%pattern=['-1','-2'])"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("  -2  "), _msg);
		assertTrue(parseFail("-3"), _msg);

		assertTrue(prepare("negativeInteger(%totalDigits='2')"), _msg);
		assertTrue(parse("-11"), _msg);
		assertTrue(parse("-00000000011"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parseFail("-111"), _msg);

//------------------------------------------------------------------------------
//                          TESTING byte
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("byte(%length='1')"), _msg);
		assertTrue(checkFail("byte(%minLength='1')"), _msg);
		assertTrue(checkFail("byte(%maxLength='1')"), _msg);
		assertTrue(checkFail("byte(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("byte(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"byte(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"byte(%minInclusive='1',%maxInclusive='0')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("byte(%fractionDigits='0')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("byte(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("byte"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("127"), _msg);
		assertTrue(parse("-128"), _msg);
		assertTrue(parse("\n 127 \n\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("0x0"), _msg);
		assertTrue(parseFail("128"), _msg);
		assertTrue(parseFail("-129"), _msg);

		// testing facets
		assertTrue(prepare("byte(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("byte(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare("byte(%minInclusive='1',%maxInclusive='2')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("byte(%minExclusive='0',%maxExclusive='3')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("byte(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("byte(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("byte(%pattern=['\\\\d'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("33"), _msg);

		assertTrue(prepare("byte(%totalDigits='2')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("+00000000011"), _msg);
		assertTrue(parse("-00000000011"), _msg);
		assertTrue(parseFail("111"), _msg);

//		assertTrue(prepare("byte(%fractionDigits='0')"),
//			_msg); //Schema ignors!!!
//
//		assertTrue(prepare("byte(%whiteSpace='collapse')"), _msg);
//		assertTrue(parse("   123   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING short
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("short(%length='1')"), _msg);
		assertTrue(checkFail("short(%minLength='1')"), _msg);
		assertTrue(checkFail("short(%maxLength='1')"), _msg);
		assertTrue(checkFail("short(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("short(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"short(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"short(%minInclusive='1',%maxInclusive='0')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("short(%fractionDigits='0')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("short(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("short"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("32767"), _msg);
		assertTrue(parse("-32768"), _msg);
		assertTrue(parse("\n 127 \n\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("32768"), _msg);
		assertTrue(parseFail("-32769"), _msg);

		// testing facets
		assertTrue(prepare("short(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("short(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare("short(%minInclusive='1',%maxInclusive='2')"),
			_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("short(%minExclusive='0',%maxExclusive='3')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("short(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("short(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("short(%totalDigits='2')"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("+00000000011"), _msg);
		assertTrue(parse("-00000000011"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("111"), _msg);

//------------------------------------------------------------------------------
//                          TESTING int
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("int(%length='1')"), _msg);
		assertTrue(checkFail("int(%minLength='1')"), _msg);
		assertTrue(checkFail("int(%maxLength='1')"), _msg);
		assertTrue(checkFail("int(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("int(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"int(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"int(%minInclusive='1',%maxInclusive='0')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("int(%fractionDigits='0')"), _msg);

		// testing fixed facets
		assertTrue(prepare("int(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("int"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-1"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("2147483647"), _msg);
		assertTrue(parse("-2147483648"), _msg);
		assertTrue(parse("\n 127 \n\t "), _msg);
		assertTrue(parse("   123   "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("2147483648"), _msg); //higher then max
		assertTrue(parseFail("-2147483649"), _msg);  //lower then max

		// testing facets
		assertTrue(prepare("int(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("int(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare("int(%minInclusive='1',%maxInclusive='2')"),_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("int(%minExclusive='0',%maxExclusive='3')"),_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("int(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("int(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("int(%totalDigits='2')"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("000000000011"), _msg);
		assertTrue(parse("+000000000011"), _msg);
		assertTrue(parse("-000000000011"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("111"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

//------------------------------------------------------------------------------
//                          TESTING unsignedInt
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("unsignedInt(%length='1')"), _msg);
		assertTrue(checkFail("unsignedInt(%minLength='1')"), _msg);
		assertTrue(checkFail("unsignedInt(%maxLength='1')"), _msg);
		assertTrue(checkFail("unsignedInt(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("unsignedInt(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"unsignedInt(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"unsignedInt(%minInclusive='1',%maxInclusive='0')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("unsignedInt(%fractionDigits='0')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("unsignedInt(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("unsignedInt"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("+0"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("4294967295"), _msg);
		assertTrue(parse("+4294967295"), _msg);
		assertTrue(parse("\n 127 \n\t "), _msg);
		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("-1"), _msg);
		assertTrue(parseFail("-2147483649"), _msg);
		assertTrue(parseFail("4294967296"), _msg);

		// testing facets
		assertTrue(prepare("unsignedInt(%minInclusive='1')"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("unsignedInt(%minExclusive='1')"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("1"), _msg);

		assertTrue(prepare(
			"unsignedInt(%minInclusive='1',%maxInclusive='2')"),_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare(
			"unsignedInt(%minExclusive='0',%maxExclusive='3')"),_msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("2"), _msg);
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("unsignedInt(%enumeration=['0','3'])"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("3"), _msg);
		assertTrue(parseFail("2"), _msg);

		assertTrue(prepare("unsignedInt(%pattern=['1','2'])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("  2  "), _msg);
		assertTrue(parseFail("3"), _msg);

		assertTrue(prepare("unsignedInt(%totalDigits='2')"), _msg);
		assertTrue(parse("11"), _msg);
		assertTrue(parse("000000000011"), _msg);
		assertTrue(parse("+000000000011"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parseFail("111"), _msg);

//------------------------------------------------------------------------------
//                          TESTING long
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("long(%length='1')"), _msg);
		assertTrue(checkFail("long(%minLength='1')"), _msg);
		assertTrue(checkFail("long(%maxLength='1')"), _msg);
		assertTrue(checkFail("long(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("long(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"long(%minInclusive='1',%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"long(%minInclusive='1',%maxInclusive='0')"), _msg);
// not recognized by SCHEMA
//		assertTrue(checkFail("long(%fractionDigits='0')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("long(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("long"), _msg);
		assertTrue(parse("000000"), _msg);
		assertTrue(parse("000001"), _msg);
		assertTrue(parse("12345"), _msg);
		assertTrue(parse("+12346"), _msg);
		assertTrue(parse("-5"), _msg);
		assertTrue(parse("9223372036854775807"), _msg);
		assertTrue(parse("-9223372036854775808"), _msg);

		// testing errors
		assertTrue(parseFail("123.456"), _msg);
		assertTrue(parseFail("-124E24"), _msg);
		assertTrue(parseFail("0x1"), _msg);
		assertTrue(parseFail("2346f125"), _msg);
		assertTrue(parseFail("9223372036854775808"), _msg);
		assertTrue(parseFail("-9223372036854775809"), _msg);
		assertTrue(parseFail("126789675432336472147483647"), _msg);

		// testing facets
		assertTrue(prepare("long(%enumeration=[0001, '+296547'])"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("000296547"), _msg);
		assertTrue(parseFail("-1"), _msg);
		assertTrue(parseFail("296546"), _msg);
		assertTrue(parseFail("296548"), _msg);

		assertTrue(prepare("long(%pattern=['\\\\d{3}0{2}'])"), _msg);
		assertTrue(parse("00000"), _msg);
		assertTrue(parse("13200"), _msg);
		assertTrue(parseFail("00002"), _msg);
		assertTrue(parseFail("1200"), _msg);

		assertTrue(prepare("long(%maxInclusive='145589')"), _msg);
		assertTrue(parse("145589"), _msg);
		assertTrue(parseFail("145590"), _msg);

		assertTrue(prepare("long(%maxInclusive='-22578')"), _msg);
		assertTrue(parse("-22578"), _msg);
		assertTrue(parseFail("-22577"), _msg);

		assertTrue(prepare("long(%maxExclusive='22634')"), _msg);
		assertTrue(parse("22633"), _msg);
		assertTrue(parseFail("22634"), _msg);

		assertTrue(prepare("long(%maxExclusive='-5986')"), _msg);
		assertTrue(parse("-5987"), _msg);
		assertTrue(parseFail("-5986"), _msg);

		assertTrue(prepare("long(%minInclusive=13557)"), _msg);
		assertTrue(parse("13557"), _msg);
		assertTrue(parseFail("13556"), _msg);

		assertTrue(prepare("long(%minInclusive=-35894)"), _msg);
		assertTrue(parse("-35894"), _msg);
		assertTrue(parseFail("-35895"), _msg);

		assertTrue(prepare("long(%minExclusive=95784)"), _msg);
		assertTrue(parse("95785"), _msg);
		assertTrue(parseFail("95784"), _msg);

		assertTrue(prepare("long(%minExclusive='-9542')"), _msg);
		assertTrue(parse("-9541"), _msg);
		assertTrue(parseFail("-9542"), _msg);

		assertTrue(prepare("long(%totalDigits='4')"), _msg);
		assertTrue(parse("00001234"), _msg);
		assertTrue(parse("-3154"), _msg);
		assertTrue(parseFail("25636"), _msg);
		assertTrue(parseFail("-15989"), _msg);

		assertTrue(prepare("long"), _msg);
		assertTrue(parse("  00457812   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING float
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("float(%length='1')"), _msg);
		assertTrue(checkFail("float(%minLength='1')"), _msg);
		assertTrue(checkFail("float(%maxLength='1')"), _msg);
		assertTrue(checkFail("float(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("float(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("float(%fractionDigits='1')"), _msg);
		assertTrue(checkFail("float(%totalDigits='1')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal facets
		assertTrue(prepare("float(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("float"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("0000"), _msg);
		assertTrue(parse("000.000"), _msg);
		assertTrue(parse("1E000"), _msg);
		assertTrue(parse("12"), _msg);
		assertTrue(parse("0E2"), _msg);
		assertTrue(parse("1E1"), _msg);
		assertTrue(parse("1e1"), _msg);
		assertTrue(parse("1E0"), _msg);
		assertTrue(parse("0.123E1"), _msg);
		assertTrue(parse("-0.123E1"), _msg);
		assertTrue(parse("+0.123E1"), _msg);
		assertTrue(parse("0.123E+1"), _msg);
		assertTrue(parse("0.123E-1"), _msg);
		assertTrue(parse("+.123E-1"), _msg);
		assertTrue(parse("-.123E1"), _msg);
		assertTrue(parse("-.1"), _msg);
		assertTrue(parse(".1"), _msg);
		assertTrue(parse("12."), _msg);
		assertTrue(parse("-1.e-3"), _msg);
		assertTrue(parse("16777216E1"), _msg);
		assertTrue(parse("-16777216E1"), _msg);
		assertTrue(parse("+1E104"), _msg);
		assertTrue(parse("1E-149"), _msg);
		assertTrue(parse("NaN"), _msg);
		assertTrue(parse("INF"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-0.0"), _msg);
		assertTrue(parse("+0.0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("0.0E0"), _msg);

		// testing errors
		assertTrue(parseFail("1E1.12"), _msg);
		assertTrue(parseFail("1E"), _msg);
		assertTrue(parseFail("-NaN"), _msg);
		assertTrue(parseFail("+NaN"), _msg);
		assertTrue(parseFail("+INF"), _msg);

		//outer limits
		assertTrue(parse("16777217E1"), _msg);
		assertTrue(parse("-16777217E1"), _msg);
		assertTrue(parse("16777217E-150"), _msg);
		assertTrue(parse("16777217E104"), _msg);

		// testing facets
		assertTrue(prepare("float(%enumeration=['NaN'])"), _msg);
		assertTrue(parse("NaN"), _msg);
		assertTrue(parseFail("0.123"), _msg);

		assertTrue(prepare("float(%enumeration=[001.1500E0, 0])"), _msg);
		assertTrue(parse("1.15"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("0.0"), _msg);
		assertTrue(parse("0.0E0"), _msg);
		assertTrue(parseFail("1E0"), _msg);
		assertTrue(parseFail("1.12E1"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(parseFail("INF"), _msg);

		assertTrue(prepare(
			"float(%pattern=['\\\\d{2}.\\\\d{2}', '\\\\d{1}E\\\\d'])"),_msg);
		assertTrue(parse("11.15"), _msg);
		assertTrue(parse("5E3"), _msg);
		assertTrue(parseFail("0012.12"), _msg);
		assertTrue(parseFail("-1E1"), _msg);

		assertTrue(prepare("float(%maxInclusive=1E1)"), _msg);
		assertTrue(parse("10.000"), _msg);
		assertTrue(parse("-10.000"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parseFail("11.0"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(prepare("float(%maxInclusive='INF')"), _msg);
		assertTrue(parse("9.999E99"), _msg);
		assertTrue(parse("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);

		assertTrue(prepare("float(%maxExclusive=1E1)"), _msg);
		assertTrue(parse("9.99999"), _msg);
		assertTrue(parse("-9.99999"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parseFail("10.000"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(prepare("float(%maxExclusive='INF')"), _msg);
		assertTrue(parse("9.999E9"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);

		assertTrue(prepare("float(%minInclusive=1E1)"), _msg);
		assertTrue(parse("10.0"), _msg);
		assertTrue(parseFail("9.0"), _msg);

		assertTrue(prepare("float(%minExclusive=1E1)"), _msg);
		assertTrue(parse("10.1"), _msg);
		assertTrue(parseFail("10.0"), _msg);

		assertTrue(prepare("float"), _msg);
		assertTrue(parse("  -9.9   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING double
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("double(%length='1')"), _msg);
		assertTrue(checkFail("double(%minLength='1')"), _msg);
		assertTrue(checkFail("double(%maxLength='1')"), _msg);
		assertTrue(checkFail("double(%fractionDigits='1')"), _msg);
		assertTrue(checkFail("double(%totalDigits='1')"), _msg);
		assertTrue(checkFail("double(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("double(%whiteSpace='replace')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal facets
		assertTrue(prepare("double(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("double"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("0000"), _msg);
		assertTrue(parse("000.000"), _msg);
		assertTrue(parse("1E000"), _msg);
		assertTrue(parse("12"), _msg);
		assertTrue(parse("0E2"), _msg);
		assertTrue(parse("1E1"), _msg);
		assertTrue(parse("1e1"), _msg);
		assertTrue(parse("1E0"), _msg);
		assertTrue(parse("0.123E1"), _msg);
		assertTrue(parse("-0.123E1"), _msg);
		assertTrue(parse("+0.123E1"), _msg);
		assertTrue(parse("0.123E+1"), _msg);
		assertTrue(parse("0.123E-1"), _msg);
		assertTrue(parse("+.123E-1"), _msg);
		assertTrue(parse("-.123E1"), _msg);
		assertTrue(parse("-.1"), _msg);
		assertTrue(parse(".1"), _msg);
		assertTrue(parse("12."), _msg);
		assertTrue(parse("-.1e-3"), _msg);
		assertTrue(parse("-1.e-3"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("16777216E1"), _msg);
		assertTrue(parse("-16777216E1"), _msg);
		assertTrue(parse("1E104"), _msg);
		assertTrue(parse("1E-149"), _msg);
		assertTrue(parse("NaN"), _msg);
		assertTrue(parse("INF"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("+1"), _msg);
		assertTrue(parse("-0.0"), _msg);
		assertTrue(parse("+0.0"), _msg);
		assertTrue(parse("-0"), _msg);
		assertTrue(parse("0.0E0"), _msg);

		// testing errors
		assertTrue(parseFail("1E1.12"), _msg);
		assertTrue(parseFail("1E"), _msg);
		assertTrue(parseFail("-NaN"), _msg);
		assertTrue(parseFail("+NaN"), _msg);
		assertTrue(parseFail("+INF"), _msg);

		//testing enumeration
		assertTrue(prepare("double(%enumeration=['NaN','INF','-INF'])"), _msg);
		assertTrue(parse("NaN"), _msg);
		assertTrue(parse("INF"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parseFail("0.123"), _msg);

		assertTrue(prepare("double(%enumeration=['001.1500E0', '0'])"),
			_msg);
		assertTrue(parse("1.15"), _msg);
		assertTrue(parse("0"), _msg);
		assertTrue(parse("0.0"), _msg);
		assertTrue(parse("0.0E0"), _msg);
		assertTrue(parseFail("1E0"), _msg);
		assertTrue(parseFail("1.12E1"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(parseFail("INF"), _msg);

		//testing pattern
		assertTrue(prepare(
			"double(%pattern=['\\\\d{2}.\\\\d{2}', '\\\\d{1}E\\\\d'])"), _msg);
		assertTrue(parse("11.15"), _msg);
		assertTrue(parse("5E3"), _msg);
		assertTrue(parseFail("0012.12"), _msg);
		assertTrue(parseFail("-1E1"), _msg);

		//testing maxInclusive
		assertTrue(prepare("double(%maxInclusive=1E1)"), _msg);
		assertTrue(parse("10.000"), _msg);
		assertTrue(parse("-10.000"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parseFail("11.0"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(prepare("double(%maxInclusive='INF')"), _msg);
		assertTrue(parse("9.999E99"), _msg);
		assertTrue(parse("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);

		//testing maxExclusive
		assertTrue(prepare("double(%maxExclusive='1E1')"), _msg);
		assertTrue(parse("9.99999"), _msg);
		assertTrue(parse("-9.99999"), _msg);
		assertTrue(parse("-INF"), _msg);
		assertTrue(parseFail("10.000"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);
		assertTrue(prepare("double(%maxExclusive='INF')"), _msg);
		assertTrue(parse("9.999E9"), _msg);
		assertTrue(parseFail("INF"), _msg);
		assertTrue(parseFail("NaN"), _msg);

		//testing minInclusive
		assertTrue(prepare("double(%minInclusive=1E1)"), _msg);
		assertTrue(parse("10.0"), _msg);
		assertTrue(parseFail("9.0"), _msg);

		//testing minExclusive
		assertTrue(prepare("double(%minExclusive=1E1)"), _msg);
		assertTrue(parse("10.1"), _msg);
		assertTrue(parseFail("10.0"), _msg);

		//testing whiteSpace
		assertTrue(prepare("double"), _msg);
		assertTrue(parse("  -9.9   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING date
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("date(%length='1')"), _msg);
		assertTrue(checkFail("date(%minLength='1')"), _msg);
		assertTrue(checkFail("date(%maxLength='1')"), _msg);
		assertTrue(checkFail("date(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("date(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("date(%totalDigits='2')"), _msg);
		assertTrue(checkFail("date(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal default
		assertTrue(prepare("date(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("date"), _msg);
		assertTrue(parse("0001-01-01"), _msg);
		assertTrue(parse(" \n\t 0001-01-01 \n\t "), _msg);
		assertTrue(parse("12345-01-01"), _msg);
		assertTrue(parse("2015-11-15-05:00"), _msg);
		assertTrue(parse("2015-11-15+05:59"), _msg);
		assertTrue(parse("12345-01-01"), _msg);
		assertTrue(parse("2000-01-01+13:59"), _msg);
		assertTrue(parse("2000-01-01-13:59"), _msg);
		assertTrue(parse("2000-01-01Z"), _msg);
		assertTrue(parse("9999-12-01"), _msg);
		assertTrue(parse("-9999-01-31"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("1"), _msg);
		assertTrue(parseFail("02010-01-01"), _msg);
		assertTrue(parseFail("2010-01-01z"), _msg);
		assertTrue(parseFail("123-01-01"), _msg);
		assertTrue(parseFail("2000-13-01"), _msg);
		assertTrue(parseFail("2000-00-01"), _msg);
		assertTrue(parseFail("2000-01-32"), _msg);
//		assertTrue(parseFail("2000-02-29"), _msg); //schema not fails!
		assertTrue(parseFail("2000-01-00"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("2000-01-01 +10:00"), _msg); //schema not fails
			assertTrue(parseFail("2000-01-01 -10:00"), _msg); //schema not fails
			assertTrue(parseFail("2000-01-01-14:50"), _msg); //schema not fails
			assertTrue(parseFail("2000-01-01+14:50"), _msg); //schema not fails
		}
		assertTrue(parseFail("2000-01-01-10:61"), _msg);
		assertTrue(parseFail("2000-01-01-15:00"), _msg);
		assertTrue(parseFail("2000-01-01+15:00"), _msg);
		assertTrue(parseFail("2000-01-01+1"), _msg);
		assertTrue(parseFail("2000-011-01"), _msg);
		assertTrue(parseFail("2000-01-013"), _msg);
		assertTrue(parseFail("2000.01.01"), _msg);
		assertTrue(parseFail("01-01-2000"), _msg);
		assertTrue(parseFail("2000-1-1"), _msg);
		assertTrue(parseFail("1.1.2000"), _msg);
		assertTrue(parseFail("20000101"), _msg);
		assertTrue(parseFail("2015-11-15-05:60"), _msg);
		assertTrue(parseFail("2015-11-15-0500"), _msg);
		assertTrue(parseFail("2015-11-15-05"), _msg);

		// testing facets
		assertTrue(prepare(
			"date(%enumeration=['1999-12-01', '2015-11-15-05:00'])"), _msg);
		assertTrue(parse("1999-12-01"), _msg);
		assertTrue(parse("2015-11-15-05:00"), _msg);
		assertTrue(parseFail("2015-11-15"), _msg);
		assertTrue(parseFail("1999-12-01Z"), _msg);
		assertTrue(parseFail("1999-12-01+00:00"), _msg);
		assertTrue(parseFail("2015-11-15Z"), _msg);

		assertTrue(prepare(
			"date(%enumeration=['1999-12-01', '2015-11-15-13:59'])"), _msg);
		assertTrue(parse("1999-12-01"), _msg);
		assertTrue(parse("2015-11-15-13:59"), _msg);
		assertTrue(parse("2015-11-16+10:01"), _msg);
		assertTrue(parseFail("1999-12-01Z"), _msg);
		assertTrue(parseFail("2015-11-15"), _msg);

		assertTrue(prepare(
			"date(%pattern=['199\\\\d-\\\\d{2}-\\\\d{2}'])"), _msg);
		assertTrue(parse("1999-12-01"), _msg);
		assertTrue(parse("1999-11-15"), _msg);
		assertTrue(parseFail("2015-11-15"), _msg);
		assertTrue(parseFail("999-11-15+05:00"), _msg);

		assertTrue(prepare("date(%minInclusive='2000-01-01+01:00')"), _msg);
		assertTrue(parse("2000-01-01+01:00"), _msg);
		assertTrue(parse("2000-01-02"), _msg);
		assertTrue(parseFail("2000-01-01+01:01"), _msg);
		assertTrue(parseFail("1999-12-31Z"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);

		assertTrue(prepare("date(%minInclusive='2000-01-01')"), _msg);
		assertTrue(parse("2000-01-01"), _msg);
		assertTrue(parseFail("2000-01-01Z"), _msg);
		assertTrue(parseFail("1999-12-31-14:00"), _msg);

		assertTrue(prepare("date(%minExclusive='1112-07-03+11:00')"), _msg);
		assertTrue(parse("1112-07-03+10:59"), _msg);
		assertTrue(parse("1112-07-02-13:01"), _msg);
		assertTrue(parse("1112-07-04"), _msg);
		assertTrue(parseFail("1112-07-03+11:00"), _msg);
		assertTrue(parseFail("1112-07-02-13:00"), _msg);
		assertTrue(parseFail("1112-07-03"), _msg);

		assertTrue(prepare("date(%minExclusive='2000-01-01+01:00')"), _msg);
		assertTrue(parse("2000-01-01-01:00"), _msg);
		assertTrue(parse("2000-01-01+00:59"), _msg);
		assertTrue(parseFail("2000-01-01+01:00"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg); //xdOK

		assertTrue(prepare("date(%minExclusive='2000-01-01')"), _msg);
		assertTrue(parse("2000-01-02"), _msg);
		assertTrue(parse("2000-01-02+09:59"), _msg);
		assertTrue(parseFail("2000-01-02+10:00"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);

		assertTrue(prepare("date(%maxInclusive='2000-01-01')"), _msg);
		assertTrue(parse("1999-12-31"), _msg);
		assertTrue(parse("2000-01-01"), _msg);
		assertTrue(parseFail("2000-01-01Z"), _msg);
		assertTrue(parseFail("2000-01-02"), _msg);

		assertTrue(prepare("date(%maxInclusive='2000-01-01Z')"), _msg);
		assertTrue(parse("2000-01-01Z"), _msg);
		assertTrue(parse("2000-01-01+00:00"), _msg);
		assertTrue(parse("1999-12-31"), _msg);
		assertTrue(parse("2000-01-01+01:00"), _msg);
		assertTrue(parseFail("2000-01-01-01:00"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);

		assertTrue(prepare("date(%maxInclusive='2000-01-01+13:59')"), _msg);
		assertTrue(parse("2000-01-01+13:59"), _msg);
		assertTrue(parse("1999-12-31-10:01"), _msg);
		assertTrue(parse("1999-12-30"), _msg);
		assertTrue(parseFail("1999-12-31"), _msg);
		assertTrue(parseFail("2000-01-01+13:58"), _msg);
		assertTrue(parseFail("1999-12-31-11:00"), _msg); //????

		assertTrue(prepare("date(%maxExclusive='2000-01-01')"), _msg);
		assertTrue(parse("1999-12-31"), _msg);
		assertTrue(parse("1999-12-31-09:59"), _msg);
		assertTrue(parseFail("2000-01-01+01:00"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);
		assertTrue(parseFail("2000-01-02"), _msg);
		assertTrue(parseFail("1999-12-31-10:00"), _msg);

		assertTrue(prepare("date(%maxExclusive='2000-01-01Z')"), _msg);
		assertTrue(parse("1999-12-31"), _msg);
		assertTrue(parse("1999-12-31Z"), _msg);
		assertTrue(parse("1999-12-31-12:00"), _msg);
		assertTrue(parse("1999-12-31-13:59"), _msg);
		assertTrue(parse("2000-01-01+00:01"), _msg);
		assertTrue(parseFail("2000-01-01Z"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);

		assertTrue(prepare("date"), _msg);
		assertTrue(parse("   2000-01-01   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING time
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("time(%length='1')"), _msg);
		assertTrue(checkFail("time(%minLength='1')"), _msg);
		assertTrue(checkFail("time(%maxLength='1')"), _msg);
		assertTrue(checkFail("time(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("time(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("time(%totalDigits='2')"), _msg);
		assertTrue(checkFail("time(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("time(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("time"), _msg);
		assertTrue(parse("00:00:00.00000+00:00"), _msg);
		assertTrue(parse(" 00:00:00.00000+14:00 "),_msg);
		assertTrue(parse(" 00:00:00.00000-14:00 "),_msg);
		assertTrue(parse("00:00:00"), _msg);
		assertTrue(parse("24:00:00"), _msg);
		assertTrue(parse("00:59:00"), _msg);
		assertTrue(parse("00:00:00.999999"), _msg);
		assertTrue(parse("24:00:00"), _msg); //leap second?

		// testing errors
		assertTrue(parseFail("0:00:00"), _msg);
		assertTrue(parseFail("00:0:00"), _msg);
		assertTrue(parseFail("00:00:0"), _msg);
		assertTrue(parseFail(":00:00"), _msg);
		assertTrue(parseFail("00::00"), _msg);
		assertTrue(parseFail("00:00:"), _msg);
		assertTrue(parseFail("000:0:00"), _msg);
		assertTrue(parseFail("00:000:00"), _msg);
		assertTrue(parseFail("00:00:000"), _msg);
		assertTrue(parseFail("00:00:00."), _msg);
		assertTrue(parseFail("00:00:61"), _msg);
		assertTrue(parseFail("00:60:00"), _msg);
		assertTrue(parseFail("25:00:00"), _msg);
		assertTrue(parseFail("00:60:00"), _msg);
		assertTrue(parseFail("00:00:00,1"), _msg);
		assertTrue(parseFail("00:00:00+0100"), _msg);
		assertTrue(parseFail("00:00:00+01:0"), _msg);
		assertTrue(parseFail("00:00:00+1:00"), _msg);
		assertTrue(parseFail("00:00:00+15:30"), _msg);

		// testing facets
		assertTrue(prepare(
			"time(%enumeration=['02:10:35-04:00', '15:24:57+08:16'])"),_msg);
		assertTrue(parse("02:10:35-04:00"), _msg);
		assertTrue(parse("06:10:35Z"), _msg);
		assertTrue(parse("15:24:57+08:16"), _msg);
		assertTrue(parse("07:08:57Z"), _msg);
		assertTrue(parseFail("06:10:35"), _msg);
		assertTrue(parseFail("07:08:57"), _msg);

		assertTrue(prepare("time(%pattern=['\\\\d{2}:20:\\\\d{2}'])"), _msg);
		assertTrue(parse("02:20:59"), _msg);
		assertTrue(parse("23:20:00"), _msg);
		assertTrue(parseFail("02:20:59Z"), _msg);
		assertTrue(parseFail("02:10:35"), _msg);

		assertTrue(prepare("time(%maxInclusive='18:39:12+02:12')"), _msg);
		assertTrue(parse("18:39:12+02:12"), _msg);
		assertTrue(parse("16:27:12Z"), _msg);
		assertTrue(parse("02:27:11"), _msg);
		assertTrue(parseFail("18:39:12+02:11"), _msg);
		assertTrue(parseFail("16:27:13Z"), _msg);
		assertTrue(parseFail("02:27:12"), _msg);

		assertTrue(prepare("time(%maxInclusive='21:05:45')"), _msg);
		assertTrue(parse("21:05:45"), _msg);
		assertTrue(parse("07:05:44Z"), _msg);
		assertTrue(parseFail("21:05:46"), _msg);
		assertTrue(parseFail("07:05:45Z"), _msg);

		assertTrue(prepare("time(%maxExclusive='02:56:27-12:08')"), _msg);
		assertTrue(parse("02:56:27-12:07"), _msg);
		assertTrue(parse("15:04:26Z"), _msg);
		assertTrue(parse("01:04:26"), _msg);
		assertTrue(parseFail("02:56:27-12:08"), _msg);
		assertTrue(parseFail("15:04:27Z"), _msg);
		assertTrue(parseFail("01:04:27"), _msg);

		assertTrue(prepare("time(%maxExclusive='18:01:40')"), _msg);
		assertTrue(parse("18:01:39"), _msg);
		assertTrue(parse("04:01:39Z"), _msg);
		assertTrue(parseFail("18:01:40"), _msg);
		assertTrue(parseFail("04:01:40Z"), _msg);

		assertTrue(prepare("time(%minInclusive='17:27:59+12:20')"), _msg);
		assertTrue(parse("17:27:59+12:20"), _msg);
		assertTrue(parse("05:07:59Z"), _msg);
		assertTrue(parse("19:07:59.0001"), _msg);
		assertTrue(parseFail("17:27:59+12:21"), _msg);
		assertTrue(parseFail("05:07:58.999Z"), _msg);
		assertTrue(parseFail("19:07:59"), _msg);

		assertTrue(prepare("time(%minInclusive='05:03:30')"), _msg);
		assertTrue(parse("05:03:30"), _msg);
		assertTrue(parse("19:03:30.001Z"), _msg);
		assertTrue(parseFail("05:03:29.999"), _msg);
		assertTrue(parseFail("19:03:30Z"), _msg);

		assertTrue(prepare("time(%minExclusive='09:54:56+03:59')"), _msg);
		assertTrue(parse("09:54:59+03:58"), _msg);
		assertTrue(parse("05:55:57Z"), _msg);
		assertTrue(parse("19:55:58"), _msg);
		assertTrue(parseFail("09:54:56+03:59"), _msg);
		assertTrue(parseFail("05:55:56Z"), _msg);
		assertTrue(parseFail("19:55:55"), _msg);

		assertTrue(prepare("time(%minExclusive='08:12:14')"), _msg);
		assertTrue(parse("08:12:15"), _msg);
		assertTrue(parse("22:12:15Z"), _msg);
		assertTrue(parseFail("08:12:14"), _msg);
		assertTrue(parseFail("22:12:14Z"), _msg);

		assertTrue(prepare("time"), _msg);
		assertTrue(parse("   04:05:20Z   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING dateTime
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("dateTime(%length='1')"), _msg);
		assertTrue(checkFail("dateTime(%minLength='1')"), _msg);
		assertTrue(checkFail("dateTime(%maxLength='1')"), _msg);
		assertTrue(checkFail("dateTime(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("dateTime(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("dateTime(%totalDigits='2')"), _msg);
		assertTrue(checkFail("dateTime(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal facets
		assertTrue(prepare("dateTime(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("dateTime"), _msg);
		assertTrue(parse("0001-01-01T00:00:00.00000+00:00"), _msg);
		assertTrue(parse("  0001-01-01T00:00:00.000Z  "), _msg);
		assertTrue(parse("12345-01-01T00:00:00"), _msg);
		assertTrue(parse("2000-01-01T24:00:00"), _msg);
		assertTrue(parse("2000-12-01T00:00:00"), _msg);
		assertTrue(parse("2000-01-31T00:00:00"), _msg);
		assertTrue(parse("2000-01-01T00:59:00"), _msg);
		assertTrue(parse("2000-01-01T00:00:59"), _msg);
		assertTrue(parse("2000-01-01T00:00:00.999999"), _msg);
		assertTrue(parse("2000-01-01T00:00:00.00005"), _msg);
		assertTrue(parse("2000-01-01T00:00:00+13:59"), _msg);
		assertTrue(parse("2000-01-01T00:00:00-13:59"), _msg);
		assertTrue(parse("-2000-01-01T00:00:00"), _msg);
		assertTrue(parse("2000-01-01T00:00:00"), _msg);
		assertTrue(parse("2000-01-01T24:00:00"), _msg); //leap second ?
		assertTrue(parse("2000-01-01T24:00:00+01:00"), _msg);

		// testing errors
		assertTrue(parseFail("02010-01-01T00:00:00"), _msg);
		assertTrue(parseFail("123-01-01T00:00:00"), _msg);
		assertTrue(parseFail("2000-13-01T00:00:00"), _msg);
		assertTrue(parseFail("2000-00-01T00:00:00"), _msg);
		assertTrue(parseFail("2000-01-32T00:00:00"), _msg);
		assertTrue(parseFail("2000-01-00T00:00:00"), _msg);
		assertTrue(parseFail("2000-01-01T24:00"), _msg);
		assertTrue(parseFail("2000-01-01T24"), _msg);
		assertTrue(parseFail("2000-01-01"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00-15:00"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00+15:00"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("2000-01-01T00:00:00-14:01"), _msg); //schema
			assertTrue(parseFail("2000-01-01T00:00:00+14:01"), _msg); //schema
			assertTrue(parseFail("2000-01-01 T00:00:00+14:00"), _msg);//schema
			assertTrue(parseFail("2000-01-013T00:00:00"), _msg);//schema
		}
		assertTrue(parseFail("2000-01-01T00:00:00+01"), _msg);
		assertTrue(parseFail("2000-011-01T00:00:00"), _msg);
		assertTrue(parseFail("2000-01-13"), _msg);
		assertTrue(parseFail("2000.01.01T00:00:00"), _msg);
		assertTrue(parseFail("01-01-2000T00:00:00"), _msg);
		assertTrue(parseFail("2000-1-1T00:00:00"), _msg);
		assertTrue(parseFail("1.1.2000T00:00:00"), _msg);
		assertTrue(parseFail("20000101T000000"), _msg);
		assertTrue(parseFail("2000-01-01T24:00:00.001"), _msg);
		assertTrue(parseFail("2000-01-01T2:0:0"), _msg);
		assertTrue(parseFail("2000-01-01T00:60:00"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:61"), _msg);
		assertTrue(parseFail("2000-01-01T24:00:00.00001"), _msg);
		assertTrue(parseFail("2000-01-01T24:01:00"), _msg);
		assertTrue(parseFail("2000-01-01T24:01:00+01:00"), _msg);
		assertTrue(parseFail("2000-01-01T00:3:00"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:5"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:05z"), _msg);
		assertTrue(parseFail("2000-01-01T 00:00:00+14:00"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00 +14:00"), _msg);

		// testing facets
		assertTrue(prepare(
			"dateTime(%pattern=['\\\\d{4}-\\\\d{2}-\\\\d{2}T00:00:00'])"),_msg);
		assertTrue(parse("1999-01-01T00:00:00"), _msg);
		assertTrue(parse("2010-08-02T00:00:00"), _msg);
		assertTrue(parseFail("1999-01-01T00:00:00Z"), _msg);
		assertTrue(parseFail("-0001-01-01T00:00:00"), _msg);

		assertTrue(prepare(
			"dateTime(%enumeration=['2002-10-10T12:00:00+05:00'])"), _msg);
		assertTrue(parse("2002-10-10T07:00:00Z"), _msg);
		assertTrue(prepare(
			"dateTime(%enumeration=['2002-10-10T00:00:00+05:00'])"), _msg);
		assertTrue(parse("2002-10-09T19:00:00Z"), _msg);

		assertTrue(prepare(
			"dateTime(%enumeration=['2010-01-01T05:00:00.123+01:00'," +
			" '2010-12-31T24:00:00Z'])"), _msg);
		assertTrue(parse("2010-01-01T05:00:00.123+01:00"), _msg);
		assertTrue(parse("2010-01-01T04:00:00.123Z"), _msg);
		assertTrue(parse("2010-12-31T24:00:00Z"), _msg);
		assertTrue(parse("2011-01-01T00:00:00Z"), _msg);
		assertTrue(parseFail("2010-12-31T24:00:00"), _msg);
		assertTrue(parseFail("2010-01-01T05:00:00.123"), _msg);

		assertTrue(prepare(
			"dateTime(%maxInclusive='2000-01-01T12:00:00+01:00')"), _msg);
		assertTrue(parse("1999-12-31T12:00:00"), _msg);
		assertTrue(parse("1999-12-31T24:00:00-11:00"), _msg);
		assertTrue(parseFail("1999-12-31T24:00:00-11:01"), _msg);
		assertTrue(parseFail("2000-01-01T12:00:00+00:59"), _msg);
		assertTrue(parseFail("2000-01-01T12:00:00"), _msg);

		assertTrue(prepare(
			"dateTime(%maxInclusive='2000-01-01T00:00:00')"), _msg);
		assertTrue(parse("1999-12-31T09:59:59.999Z"), _msg);
		assertTrue(parse("2000-01-01T00:00:00"), _msg);
		assertTrue(parseFail("1999-12-31T10:00:00Z"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00Z"), _msg);

		assertTrue(prepare(
			"dateTime(%maxInclusive='2000-01-01T12:00:00+01:30')"), _msg);
		assertTrue(parse("2000-01-01T12:00:00+01:30"), _msg);
		assertTrue(parse("2000-01-01T10:30:00Z"), _msg);
		assertTrue(parse("1999-12-31T20:29:59.999"), _msg);
		assertTrue(parseFail("2000-01-01T12:00:00+01:29"), _msg);
		assertTrue(parseFail("2000-01-01T11:30:00.001Z"), _msg);
		assertTrue(parseFail("1999-12-31T20:30:00"), _msg);

		assertTrue(prepare(
			"dateTime(%maxInclusive='2000-01-01T00:00:00-04:00')"), _msg);
		assertTrue(parse("2000-01-01T00:00:00-04:00"), _msg);
		assertTrue(parse("1999-12-31T13:59:59.999"), _msg);
		assertTrue(parse("1999-12-31T13:59:59.99999"), _msg);
		assertTrue(parse("2000-01-01T00:00:00-03:59"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00"), _msg);

		assertTrue(prepare(
			"dateTime(%maxInclusive='2000-01-01T12:00:00')"), _msg);
		assertTrue(parse("2000-01-01T11:59:59.9999"), _msg);
		assertTrue(parse("2000-01-01T12:00:00"), _msg);
		assertTrue(parse("1999-12-31T21:59:59.99999Z"), _msg);
		assertTrue(parse("1999-12-31T21:59:59.999Z"), _msg);
		assertTrue(parseFail("1999-12-31T22:00:00Z"), _msg);

		assertTrue(prepare(
			"dateTime(%minInclusive='2000-01-01T00:00:00-01:00')"), _msg);
		assertTrue(parse("2000-01-01T00:00:00-01:00"), _msg);
		assertTrue(parse("2000-01-01T01:00:00Z"), _msg);
		assertTrue(parse("2000-01-01T15:00:00.00001"), _msg);
		assertTrue(parseFail("2000-01-01T00:00:00-00:59"), _msg);
		assertTrue(parseFail("2000-01-01T00:59:59.999Z"), _msg);
		assertTrue(parseFail("2000-01-01T00:59:59.99999Z"), _msg);
		assertTrue(parse("2000-01-01T15:00:00.001"), _msg);
		assertTrue(parseFail("2000-01-01T15:00:00"), _msg);

		assertTrue(prepare(
			"dateTime(%minInclusive='2000-01-01T10:00:00')"), _msg);
		assertTrue(parse("2000-01-01T10:00:00"), _msg);
		assertTrue(parse("2000-01-02T00:00:00.001Z"), _msg);
		assertTrue(parse("2000-01-02T00:00:00.00001Z"), _msg);
		assertTrue(parseFail("2000-01-01T09:59:59.999"), _msg);
		assertTrue(parseFail("2000-01-01T24:00:00Z"), _msg);

		assertTrue(prepare(
			"dateTime(%minExclusive='2000-01-01T15:00:00+05:00')"), _msg);
		assertTrue(parse("2000-01-01T10:00:00.001Z"), _msg);
		assertTrue(parse("2000-01-01T10:00:00.00001Z"), _msg);
		assertTrue(parse("2000-01-02T00:00:00.001"), _msg);
		assertTrue(parseFail("2000-01-01T10:00:00Z"), _msg);
		assertTrue(parseFail("2000-01-01T24:00:00"), _msg);

		assertTrue(prepare(
			"dateTime(%minExclusive='2000-01-01T02:00:00')"), _msg);
		assertTrue(parse("2000-01-01T02:00:00.001"), _msg);
		assertTrue(parse("2000-01-01T02:00:00.00001"), _msg);
		assertTrue(parse("2000-01-01T16:00:00.001Z"), _msg);
		assertTrue(parse("2000-01-01T02:00:00.00001"), _msg);
		assertTrue(parse("2000-01-01T16:00:00.00001Z"), _msg);
		assertTrue(parseFail("2000-01-01T02:00:00"), _msg);
		assertTrue(parseFail("2000-01-01T16:00:00Z"), _msg);

		assertTrue(prepare("dateTime"), _msg);
		assertTrue(parse("   2000-01-01T02:00:00   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING gDay
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("gDay(%length='1')"), _msg);
		assertTrue(checkFail("gDay(%minLength='1')"), _msg);
		assertTrue(checkFail("gDay(%maxLength='1')"), _msg);
		assertTrue(checkFail("gDay(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("gDay(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("gDay(%totalDigits='2')"), _msg);
		assertTrue(checkFail("gDay(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal default
		assertTrue(prepare("gDay(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("gDay"), _msg);
		assertTrue(parse("---01"), _msg);
		assertTrue(parse("  ---31Z  "), _msg);
		assertTrue(parse("---10+13:59"), _msg);
		assertTrue(parse("---08-13:59"), _msg);

		// testing errors
		assertTrue(parseFail("---1"), _msg);
		assertTrue(parseFail("01"), _msg);
		assertTrue(parseFail("---001"), _msg);
		assertTrue(parseFail("32"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("---18+14:01"), _msg); //schema
			assertTrue(parseFail("---12-14:01"), _msg); //schema
		}
		assertTrue(parseFail("---12z"), _msg);

		// testing facets
		assertTrue(prepare("gDay(%pattern=['---2\\\\d'])"), _msg);
		assertTrue(parse("---20"), _msg);
		assertTrue(parse("---29"), _msg);
		assertTrue(parseFail("---21-12:00"), _msg);
		assertTrue(parseFail("---21+01:00"), _msg);
		assertTrue(parseFail("---01"), _msg);

		assertTrue(prepare("gDay(%enumeration=['---01Z','---27'])"), _msg);
		assertTrue(parse("---01Z"), _msg);
		assertTrue(parse("---27"), _msg);
		assertTrue(parseFail("---01"), _msg);
		assertTrue(parseFail("---27Z"), _msg);

		assertTrue(prepare(
			"gDay(%enumeration=['---24', '---10+10:00'])"), _msg);
		assertTrue(parse("---24"), _msg);
		assertTrue(parse("---10+10:00"), _msg);
		assertTrue(parseFail("---24Z"), _msg);
		assertTrue(parseFail("---10"), _msg);

		assertTrue(prepare("gDay(%maxInclusive='---10-05:00')"), _msg);
		assertTrue(parse("---09"), _msg);
		assertTrue(parse("---10Z"), _msg);
		assertTrue(parse("---10-05:00"), _msg);
		assertTrue(parseFail("---10-05:01"), _msg);
		assertTrue(parseFail("---10"), _msg);
		assertTrue(parseFail("---11"), _msg);

		assertTrue(prepare("gDay(%maxInclusive='---20+05:00')"), _msg);
		assertTrue(parse("---20+05:00"), _msg);
		assertTrue(parse("---19"), _msg);
		assertTrue(parseFail("---20+04:59"), _msg);
		assertTrue(parseFail("---20"), _msg);

		assertTrue(prepare("gDay(%maxInclusive='---15')"), _msg);
		assertTrue(parse("---15"), _msg);
		assertTrue(parse("---14-09:59"), _msg);
		assertTrue(parseFail("---16"), _msg);
		assertTrue(parseFail("---15Z"), _msg);
		assertTrue(parseFail("---14-10:00"), _msg);

		assertTrue(prepare("gDay(%maxExclusive='---15')"), _msg);
		assertTrue(parse("---14"), _msg);
		assertTrue(parse("---14-09:59"), _msg);
		assertTrue(parseFail("---15"), _msg);
		assertTrue(parseFail("---14-10:00"), _msg);

		assertTrue(prepare("gDay(%maxExclusive='---18+13:59')"), _msg);
		assertTrue(parse("---17Z"), _msg);
		assertTrue(parse("---17-09:59"), _msg);
		assertTrue(parse("---16"), _msg);
		assertTrue(parseFail("---17"), _msg);
		assertTrue(parseFail("---17-10:01"), _msg);
		assertTrue(parseFail("---18+13:59"), _msg);

		assertTrue(prepare("gDay(%maxExclusive='---08')"), _msg);
		assertTrue(parse("---07"), _msg);
		assertTrue(parse("---07-09:59"), _msg);
		assertTrue(parseFail("---08"), _msg);
		assertTrue(parseFail("---07-10:00"), _msg);

		assertTrue(prepare("gDay(%minInclusive='---15Z')"), _msg);
		assertTrue(parse("---16"), _msg);
		assertTrue(parse("---15Z"), _msg);
		assertTrue(parseFail("'---15"), _msg);
		assertTrue(parseFail("---15+00:01"), _msg);

		assertTrue(prepare("gDay(%minInclusive='---13')"), _msg);
		assertTrue(parse("---13"), _msg);
		assertTrue(parse("---14+09:59"), _msg);
		assertTrue(parseFail("---12"), _msg);
		assertTrue(parseFail("---14+10:00"), _msg);

		assertTrue(prepare("gDay(%minInclusive='---10-11:00')"), _msg);
		assertTrue(parse("---10-11:00"), _msg);
		assertTrue(parse("---12"), _msg);
		assertTrue(parseFail("---10-10:59"), _msg);
		assertTrue(parseFail("---11"), _msg);

		assertTrue(prepare("gDay(%minExclusive='---20-10:00')"), _msg);
		assertTrue(parse("---20-10:01"), _msg);
		assertTrue(parse("---22"), _msg);
		assertTrue(parseFail("---20-10:00"), _msg);
		assertTrue(parseFail("---20"), _msg);
		assertTrue(parseFail("---21"), _msg);

		assertTrue(prepare("gDay(%minExclusive='---08')"), _msg);
		assertTrue(parse("---09"), _msg);
		assertTrue(parse("---09+09:59"), _msg);
		assertTrue(parseFail("---08"), _msg);
		assertTrue(parseFail("---09+10:00"), _msg);

		assertTrue(prepare("gDay"), _msg);
		assertTrue(parse("   ---01   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING gMonth
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("gMonth(%length='1')"), _msg);
		assertTrue(checkFail("gMonth(%minLength='1')"), _msg);
		assertTrue(checkFail("gMonth(%maxLength='1')"), _msg);
		assertTrue(checkFail("gMonth(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("gMonth(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("gMonth(%totalDigits='2')"), _msg);
		assertTrue(checkFail("gMonth(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing correct values
		assertTrue(prepare("gMonth"), _msg);
		assertTrue(parse("--01"), _msg);
		assertTrue(parse("--12"), _msg);
		assertTrue(parse("--08Z"), _msg);
		assertTrue(parse("--10+13:59"), _msg);
		assertTrue(parse("--08-13:59"), _msg);

		// testing errors
		assertTrue(parseFail("--13"), _msg);
		assertTrue(parseFail("--00"), _msg);
		assertTrue(parseFail("--1"), _msg);
		assertTrue(parseFail("--001"), _msg);
		assertTrue(parseFail("--01+15:00"), _msg);
		assertTrue(parseFail("--13"), _msg);
		assertTrue(parseFail("--05+05"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("--12-14:01"), _msg); //schema
			assertTrue(parseFail("--12+14:01"), _msg); //schema
		}
		assertTrue(parseFail("--12z"), _msg);

		// testing facets
		assertTrue(prepare("gMonth(%pattern=['--0\\\\d'])"), _msg);
		assertTrue(parse("--01"), _msg);
		assertTrue(parse("--09"), _msg);
		assertTrue(parseFail("--01Z"), _msg);
		assertTrue(parseFail("--11"), _msg);

		assertTrue(prepare("gMonth(%enumeration=['--01Z','--12'])"), _msg);
		assertTrue(parse("--01Z"), _msg);
		assertTrue(parse("--12"), _msg);
		assertTrue(parseFail("--01"), _msg);
		assertTrue(parseFail("--01+01"), _msg);
		assertTrue(parseFail("--12Z"), _msg);

		assertTrue(prepare("gMonth(%minInclusive='--10Z')"), _msg);
		assertTrue(parse("--10Z"), _msg);
		assertTrue(parse("--11"), _msg);
		assertTrue(parse("--10-01:00"), _msg);
		assertTrue(parseFail("--10+01:00"), _msg);
		assertTrue(parseFail("--10"), _msg);
		assertTrue(parseFail("--09Z"), _msg);

		assertTrue(prepare("gMonth(%minExclusive='--10Z')"), _msg);
		assertTrue(parse("--11"), _msg);
		assertTrue(parse("--11Z"), _msg);
		assertTrue(parse("--11+13:59"), _msg);
		assertTrue(parse("--11-13:59"), _msg);
		assertTrue(parse("--10-00:01"), _msg);
		assertTrue(parseFail("--10+00:01"), _msg);
		assertTrue(parseFail("--10Z"), _msg);
		assertTrue(parseFail("--10"), _msg);
		assertTrue(parseFail("--09Z"), _msg);

		assertTrue(prepare("gMonth(%maxInclusive='--10Z')"), _msg);
		assertTrue(parse("--10Z"), _msg);
		assertTrue(parse("--09"), _msg);
		assertTrue(parseFail("--10"), _msg);
		assertTrue(parseFail("--11Z"), _msg);

		assertTrue(prepare("gMonth(%maxExclusive='--10Z')"), _msg);
		assertTrue(parse("--09"), _msg);
		assertTrue(parseFail("--10Z"), _msg);
		assertTrue(parseFail("--10"), _msg);
		assertTrue(parseFail("--11"), _msg);

		assertTrue(prepare("gMonth"), _msg);
		assertTrue(parse("   --01   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING gMonthDay
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("gMonthDay(%length='1')"), _msg);
		assertTrue(checkFail("gMonthDay(%minLength='1')"), _msg);
		assertTrue(checkFail("gMonthDay(%maxLength='1')"), _msg);
		assertTrue(checkFail("gMonthDay(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("gMonthDay(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("gMonthDay(%totalDigits='2')"), _msg);
		assertTrue(checkFail("gMonthDay(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal default
		assertTrue(prepare("gMonthDay(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("gMonthDay"), _msg);
		assertTrue(parse("--01-01"), _msg);
		assertTrue(parse("--01-01Z"), _msg);
		assertTrue(parse("--01-31"), _msg);
		assertTrue(parse("--12-31"), _msg);
		assertTrue(parse("--02-29"), _msg);
		assertTrue(parse("--04-30"), _msg);
		assertTrue(parse("--01-01+13:59"), _msg);
		assertTrue(parse("--01-01-13:59"), _msg);

		// testing errors
		assertTrue(parseFail("--01-32"), _msg);
		assertTrue(parseFail("--01-00"), _msg);
		assertTrue(parseFail("--04-31"), _msg);
		assertTrue(parseFail("--00-01"), _msg);
		assertTrue(parseFail("--13-01"), _msg);
		assertTrue(parseFail("--99-01"), _msg);
		assertTrue(parseFail("--01-1"), _msg);
		assertTrue(parseFail("--1-01"), _msg);
		assertTrue(parseFail("--02-30"), _msg);
		assertTrue(parseFail("--01-01+15:00"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("--12-18+14:01"), _msg); //schema
			assertTrue(parseFail("--12-01-14:01"), _msg); //schema
		}
		assertTrue(parseFail("--12-01z"), _msg);
		assertTrue(parseFail("---12Z"), _msg);

		// testing facets
		assertTrue(prepare("gMonthDay(%pattern=['--1\\\\d-\\\\d0'])"), _msg);
		assertTrue(parse("--11-30"), _msg);
		assertTrue(parse("--10-10"), _msg);
		assertTrue(parseFail("--01-10"), _msg);
		assertTrue(parseFail("--10-10Z"), _msg);

		assertTrue(prepare(
			"gMonthDay(%enumeration=['--01-01Z','--12-31'])"), _msg);
		assertTrue(parse("--01-01Z"), _msg);
		assertTrue(parse("--12-31"), _msg);
		assertTrue(parseFail("--12-31Z"), _msg);
		assertTrue(parseFail("--01-01"), _msg);

		assertTrue(prepare("gMonthDay(%minInclusive='--10-01Z')"), _msg);
		assertTrue(parse("--10-01Z"), _msg);
		assertTrue(parse("--11-01"), _msg);
		assertTrue(parse("--10-01-00:01"), _msg);
		assertTrue(parseFail("--10-01+00:01"), _msg);
		assertTrue(parseFail("--10-01"), _msg);
		assertTrue(parseFail("--09-01Z"), _msg);

		assertTrue(prepare("gMonthDay(%minExclusive='--10-01Z')"), _msg);
		assertTrue(parse("--11-01"), _msg);
		assertTrue(parse("--10-01-00:01"), _msg);
		assertTrue(parseFail("--10-01+00:01"), _msg);
		assertTrue(parseFail("--10-01Z"), _msg);
		assertTrue(parseFail("--10-01"), _msg);
		assertTrue(parseFail("--09-01Z"), _msg);

		assertTrue(prepare("gMonthDay(%maxInclusive='--10-01Z')"), _msg);
		assertTrue(parse("--10-01Z"), _msg);
		assertTrue(parse("--09-01"), _msg);
		assertTrue(parseFail("--10-01"), _msg);
		assertTrue(parseFail("--11-01Z"), _msg);

		assertTrue(prepare("gMonthDay(%maxExclusive='--10-01Z')"), _msg);
		assertTrue(parse("--09-01"), _msg);
		assertTrue(parseFail("--10-01Z"), _msg);
		assertTrue(parseFail("--10-01"), _msg);
		assertTrue(parseFail("--11-01"), _msg);

		assertTrue(prepare("gMonthDay(%minInclusive='--01-05+10:01')"), _msg);
		assertTrue(parse("--01-05+10:00"), _msg);
		assertTrue(parse("--01-04-13:59"), _msg);
		assertTrue(parse("--01-06"), _msg);
		assertTrue(parseFail("--01-05+10:02"), _msg);
		assertTrue(parseFail("--01-05"), _msg);

		assertTrue(prepare("gMonthDay(%minInclusive='--05-10')"), _msg);
		assertTrue(parse("--05-10"), _msg);
		assertTrue(parse("--05-11Z"), _msg);
		assertTrue(parse("--05-11+09:59"), _msg);
		assertTrue(parseFail("--05-09"), _msg);
		assertTrue(parseFail("--05-11+10:00"), _msg);

		assertTrue(prepare("gMonthDay(%minExclusive='--02-16+11:00')"),_msg);
		assertTrue(parse("--02-16+10:59"), _msg);
		assertTrue(parse("--02-15-13:01"), _msg);
		assertTrue(parse("--02-17"), _msg);
		assertTrue(parseFail("--02-16+11:00"), _msg);
		assertTrue(parseFail("--02-15-13:00"), _msg);
		assertTrue(parseFail("--02-16"), _msg);

		assertTrue(prepare("gMonthDay(%minExclusive='--03-21')"), _msg);
		assertTrue(parse("--03-22"), _msg);
		assertTrue(parse("--03-22+09:59"), _msg);
		assertTrue(parseFail("--03-21"), _msg);
		assertTrue(parseFail("--03-22+10:00"), _msg);

		assertTrue(prepare("gMonthDay"), _msg);
		assertTrue(parse("   --01-01   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING gYear
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("gYear(%length='1')"), _msg);
		assertTrue(checkFail("gYear(%minLength='1')"), _msg);
		assertTrue(checkFail("gYear(%maxLength='1')"), _msg);
		assertTrue(checkFail("gYear(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("gYear(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("gYear(%totalDigits='2')"), _msg);
		assertTrue(checkFail("gYear(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal default
		assertTrue(prepare("gYear(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("gYear"), _msg);
		assertTrue(parse("2010"), _msg);
		assertTrue(parse("2010Z"), _msg);
		assertTrue(parse("0001"), _msg);
		assertTrue(parse("123475"), _msg);
		assertTrue(parse("-0001"), _msg);
		assertTrue(parse("0001+11:00"), _msg);
		assertTrue(parse("-0001+11:00"), _msg);
		assertTrue(parse("9999-13:00"), _msg);
//		assertTrue(parse("1"), _msg); //????
		assertTrue(parse("0999"), _msg);

		// testing errors
		assertTrue(parseFail("0000"), _msg);
		assertTrue(parseFail("-0"), _msg);
		assertTrue(parseFail("+1234"), _msg);
		assertTrue(parseFail("+2000"), _msg);
		assertTrue(parseFail("2000z"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("2000-14:01"), _msg); //schema
			assertTrue(parseFail("2000+14:01"), _msg); //schema
		}
		assertTrue(parseFail("2001+15:00"), _msg);

		// testing facets
		assertTrue(prepare("gYear(%pattern=['200\\\\d'])"), _msg);
		assertTrue(parse("2000"), _msg);
		assertTrue(parse("2001"), _msg);
		assertTrue(parseFail("2005Z"), _msg);
		assertTrue(parseFail("1999"), _msg);

		assertTrue(prepare("gYear(%enumeration=['2000Z','0001'])"), _msg);
		assertTrue(parse("2000Z"), _msg);
		assertTrue(parse("0001"), _msg);
		assertTrue(parseFail("-1"), _msg);
		assertTrue(parseFail("2000+01"), _msg);
		assertTrue(parseFail("0001Z"), _msg);

		assertTrue(prepare("gYear(%minInclusive='2000Z')"), _msg);
		assertTrue(parse("2000Z"), _msg);
		assertTrue(parse("2000+00:00"), _msg);
		assertTrue(parse("2000-00:01"), _msg);
		assertTrue(parse("2001"), _msg);
		assertTrue(parseFail("2000+10:00"), _msg);
		assertTrue(parseFail("1999"), _msg);
		assertTrue(parseFail("1999Z"), _msg);

		assertTrue(prepare("gYear(%minExclusive='2010Z')"), _msg);
		assertTrue(parse("2011"), _msg);
		assertTrue(parse("2010-00:01"), _msg);
		assertTrue(parseFail("2010Z"), _msg);
		assertTrue(parseFail("2000+00:00"), _msg);
		assertTrue(parseFail("2010+00:01"), _msg);
		assertTrue(parseFail("2010"), _msg);
		assertTrue(parseFail("2009Z"), _msg);

		assertTrue(prepare("gYear(%maxInclusive='2010Z')"), _msg);
		assertTrue(parse("2010Z"), _msg);
		assertTrue(parse("2010+10:00"), _msg);
		assertTrue(parse("2009"), _msg);
		assertTrue(parseFail("2010-00:01"), _msg);
		assertTrue(parseFail("2010"), _msg);
		assertTrue(parseFail("2011Z"), _msg);

		assertTrue(prepare("gYear(%maxExclusive='2010Z')"), _msg);
		assertTrue(parse("2009"), _msg);
		assertTrue(parseFail("2010Z"), _msg);
		assertTrue(parseFail("2010"), _msg);
		assertTrue(parseFail("2011"), _msg);

		assertTrue(prepare("gYear"), _msg);
		assertTrue(parse("   2009   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING gYearMonth
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("gYearMonth(%length='1')"), _msg);
		assertTrue(checkFail("gYearMonth(%minLength='1')"), _msg);
		assertTrue(checkFail("gYearMonth(%maxLength='1')"), _msg);
		assertTrue(checkFail("gYearMonth(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("gYearMonth(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("gYearMonth(%totalDigits='2')"), _msg);
		assertTrue(checkFail("gYearMonth(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// test legal default
		assertTrue(prepare("gYearMonth(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("gYearMonth"), _msg);
		assertTrue(parse("2010-01"), _msg);
		assertTrue(parse("2010-01Z"), _msg);
		assertTrue(parse("0001-12+11:00"), _msg);
		assertTrue(parse("0001-12+11:00"), _msg);
		assertTrue(parse("-0001-11+13:59"), _msg);
		assertTrue(parse("12345-12-13:59"), _msg);

		// testing errors
		assertTrue(parseFail("0000-01"), _msg);
		assertTrue(parseFail("2000-00"), _msg);
		assertTrue(parseFail("2000-1"), _msg);
		assertTrue(parseFail("2000-1Z"), _msg);
		assertTrue(parseFail("00124-01"), _msg);
		assertTrue(parseFail("+2000-01"), _msg);
		assertTrue(parseFail("1-12+11:00"), _msg);
		assertTrue(parseFail("-0-01"), _msg);
		assertTrue(parseFail("+1234-12"), _msg);
		assertTrue(parseFail("2001-01+15:00"), _msg);
		if (SUtils.JAVA_RUNTIME_VERSION_ID > 106) {
			assertTrue(parseFail("2000-01-14:01"), _msg); //schema
			assertTrue(parseFail("2000-01+14:01"), _msg); //schema
		}
		assertTrue(parseFail("2000-01z"), _msg);

		// testing facets
		assertTrue(prepare("gYearMonth(%pattern=['2000-0\\\\d'])"), _msg);
		assertTrue(parse("2000-01"), _msg);
		assertTrue(parse("2000-09"), _msg);
		assertTrue(parseFail("2000-12"), _msg);
		assertTrue(parseFail("2000-01+14:00"), _msg);

		assertTrue(prepare(
			"gYearMonth(%enumeration=['2000-01Z','0001-12'])"), _msg);
		assertTrue(parse("2000-01Z"), _msg);
		assertTrue(parse("0001-12"), _msg);
		assertTrue(parseFail("-0001-01"), _msg);
		assertTrue(parseFail("20001-12+01"), _msg);
		assertTrue(parseFail("0001-12Z"), _msg);

		assertTrue(prepare("gYearMonth(%minInclusive='2000-01Z')"), _msg);
		assertTrue(parse("2000-01Z"), _msg);
		assertTrue(parse("2001-01"), _msg);
		assertTrue(parse("2000-01-00:01"), _msg);
		assertTrue(parseFail("2000-01+00:01"), _msg);
		assertTrue(parseFail("1999-12"), _msg);
		assertTrue(parseFail("1999-12Z"), _msg);

		assertTrue(prepare("gYearMonth(%minExclusive='2010-01Z')"), _msg);
		assertTrue(parse("2011-01"), _msg);
		assertTrue(parse("2010-01-00:01"), _msg);
		assertTrue(parseFail("2010-01+00:01"), _msg);
		assertTrue(parseFail("2010-01Z"), _msg);
		assertTrue(parseFail("2010-01"), _msg);
		assertTrue(parseFail("2009-01Z"), _msg);

		assertTrue(prepare("gYearMonth(%maxInclusive='2010-01Z')"), _msg);
		assertTrue(parse("2010-01Z"), _msg);
		assertTrue(parse("2009-01"), _msg);
		assertTrue(parseFail("2010-01"), _msg);
		assertTrue(parseFail("2011-01Z"), _msg);

		assertTrue(prepare("gYearMonth(%maxExclusive='2010-01Z')"), _msg);
		assertTrue(parse("2009-01"), _msg);
		assertTrue(parseFail("2010-01Z"), _msg);
		assertTrue(parseFail("2010-01"), _msg);
		assertTrue(parseFail("2011-01"), _msg);

		assertTrue(prepare("gYearMonth"), _msg);
		assertTrue(parse("   2009-01   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING duration
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("duration(%length='1')"), _msg);
		assertTrue(checkFail("duration(%minLength='10')"), _msg);
		assertTrue(checkFail("duration(%maxLength='20')"), _msg);
		assertTrue(checkFail("duration(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("duration(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("duration(%fractionDigits='5')"), _msg);
		assertTrue(checkFail("duration(%totalDigits='10')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("duration(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("duration"), _msg);
		assertTrue(parse("P1Y2M3DT10H30M123.123456S"), _msg);
		assertTrue(parse("P1Y"), _msg);
		assertTrue(parse("P0Y"), _msg);
		assertTrue(parse("P0000Y"), _msg);
		assertTrue(parse("P0001Y"), _msg);
		assertTrue(parse("P9999Y"), _msg);
		assertTrue(parse("P1M"), _msg);
		assertTrue(parse("P0M"), _msg);
		assertTrue(parse("P1M"), _msg);
		assertTrue(parse("P0M"), _msg);
		assertTrue(parse("P0000M"), _msg);
		assertTrue(parse("P0001M"), _msg);
		assertTrue(parse("P9999M"), _msg);
		assertTrue(parse("P1D"), _msg);
		assertTrue(parse("P0D"), _msg);
		assertTrue(parse("P0000D"), _msg);
		assertTrue(parse("P0001D"), _msg);
		assertTrue(parse("P9999D"), _msg);
		assertTrue(parse("PT1H"), _msg);
		assertTrue(parse("PT0H"), _msg);
		assertTrue(parse("PT0000H"), _msg);
		assertTrue(parse("PT0001H"), _msg);
		assertTrue(parse("PT9999H"), _msg);
		assertTrue(parse("PT1M"), _msg);
		assertTrue(parse("PT0M"), _msg);
		assertTrue(parse("PT0000M"), _msg);
		assertTrue(parse("PT0001M"), _msg);
		assertTrue(parse("PT9999M"), _msg);
		assertTrue(parse("PT1S"), _msg);
		assertTrue(parse("PT0S"), _msg);
		assertTrue(parse("PT0000S"), _msg);
		assertTrue(parse("PT0001S"), _msg);
		assertTrue(parse("PT9999S"), _msg);
		assertTrue(parse("PT123.456S"), _msg);
		assertTrue(parse("PT123.000S"), _msg);
		assertTrue(parse("PT0.456S"), _msg);
		assertTrue(parse("P1M"), _msg);
		assertTrue(parse("P1Y1M1DT1H1M1.1234567S"), _msg);
		assertTrue(parse("-P1Y"), _msg);

		// testing errors
		assertTrue(parseFail("P"), _msg);
		assertTrue(parseFail("P T1M"), _msg);
		assertTrue(parseFail("PT 1M"), _msg);
		assertTrue(parseFail("PT1 M"), _msg);
		assertTrue(parseFail("P0"), _msg);
		assertTrue(parseFail("+P1Y"), _msg);
		assertTrue(parseFail("p1Y"), _msg);
		assertTrue(parseFail("P+1Y"), _msg);
		assertTrue(parseFail("P-1Y"), _msg);
		assertTrue(parseFail("P1.1Y"), _msg);
		assertTrue(parseFail("P1y"), _msg);
		assertTrue(parseFail("P+1M"), _msg);
		assertTrue(parseFail("P-1M"), _msg);
		assertTrue(parseFail("P1.2M"), _msg);
		assertTrue(parseFail("P1m"), _msg);
		assertTrue(parseFail("P1M1Y"), _msg);
		assertTrue(parseFail("P+1D"), _msg);
		assertTrue(parseFail("P-1D"), _msg);
		assertTrue(parseFail("P1.3D"), _msg);
		assertTrue(parseFail("P1d"), _msg);
		assertTrue(parseFail("P1D1M"), _msg);
		assertTrue(parseFail("P1H"), _msg);
		assertTrue(parseFail("PT+1H"), _msg);
		assertTrue(parseFail("PT-1H"), _msg);
		assertTrue(parseFail("PT1.4H"), _msg);
		assertTrue(parseFail("PT1h"), _msg);
		assertTrue(parseFail("P1D1H"), _msg);
		assertTrue(parseFail("P1H1D"), _msg);
		assertTrue(parseFail("PT+1M"), _msg);
		assertTrue(parseFail("PT-1M"), _msg);
		assertTrue(parseFail("PT1.5M"), _msg);
		assertTrue(parseFail("PT1m"), _msg);
		assertTrue(parseFail("PT1M1H"), _msg);
		assertTrue(parseFail("P1S"), _msg);
		assertTrue(parseFail("PT+1S"), _msg);
		assertTrue(parseFail("PT-1S"), _msg);
		assertTrue(parseFail("PT1.S"), _msg);
		assertTrue(parseFail("PT1s"), _msg);
		assertTrue(parseFail("PT1S1M"), _msg);
		assertTrue(parseFail("P1YT"), _msg);
		assertTrue(parseFail("1Y1M"), _msg);
		assertTrue(parseFail("R5/P1Y"), _msg);
//		assertTrue(parseFail("PT.456S"), _msg);//schema not fails
		assertTrue(parseFail("P1MT"), _msg);
		assertTrue(parseFail("P1W"), _msg);

		// testing facets
		assertTrue(prepare("duration(%enumeration=['-P12M', 'P1M'])"), _msg);
		assertTrue(parse("-P12M"), _msg);
		assertTrue(parse("-P1Y"), _msg);
		assertTrue(parse("P1M"), _msg);
		assertTrue(parseFail("P1Y"), _msg);
		assertTrue(parseFail("-P364D"), _msg);
		assertTrue(parseFail("-P365D"), _msg);
		assertTrue(parseFail("-P366D"), _msg);
		assertTrue(parseFail("-P367D"), _msg);
		assertTrue(parseFail("PD"), _msg);
		assertTrue(parseFail("P28D"), _msg);
		assertTrue(parseFail("P29D"), _msg);
		assertTrue(parseFail("P30D"), _msg);
		assertTrue(parseFail("P31D"), _msg);

		assertTrue(prepare(
			"duration(%pattern=['P\\\\d{4}Y\\\\d{2}M'])"), _msg);
		assertTrue(parse("P0001Y01M"), _msg);
		assertTrue(parse("P2010Y07M"), _msg);
		assertTrue(parseFail("P1Y1M"), _msg);
		assertTrue(parseFail("-P2000Y01M"), _msg);

		assertTrue(prepare("duration(%maxInclusive='P1Y')"), _msg);
		assertTrue(parse("P364D"), _msg);
		assertTrue(parseFail("P365D"), _msg);
		assertTrue(parseFail("P366D"), _msg);
		assertTrue(parseFail("P367D"), _msg);

		assertTrue(prepare("duration(%maxInclusive='P1Y2M')"), _msg);
		assertTrue(parse("P0001Y0002M"), _msg);
		assertTrue(parse("P14M"), _msg);
		assertTrue(parse("P423D"), _msg);
		assertTrue(parse("PT10175H"), _msg);
		assertTrue(parseFail("P424D"), _msg);

		assertTrue(prepare("duration(%maxExclusive='P547D')"), _msg);
		assertTrue(parse("P546D"), _msg);
		assertTrue(parse("P17M"), _msg);
		assertTrue(parse("PT13127H"), _msg);
		assertTrue(parseFail("P18M"), _msg);
		assertTrue(parseFail("P547D"), _msg);
		assertTrue(parseFail("PT13128H"), _msg);

		assertTrue(prepare("duration(%maxExclusive='P1Y')"), _msg);
		assertTrue(parse("P364D"), _msg);
		assertTrue(parseFail("P365D"), _msg);

		assertTrue(prepare("duration(%minInclusive='P24M')"), _msg);
		assertTrue(parse("P24M"), _msg);
		assertTrue(parse("P2Y"), _msg);
		assertTrue(parse("P732D"), _msg);
		assertTrue(parseFail("P730D"), _msg);
		assertTrue(parseFail("P728D"), _msg);
		assertTrue(parse("PT17545H"), _msg);
		assertTrue(parseFail("P23M"), _msg);
		assertTrue(parseFail("P731D"), _msg); //XDef
		assertTrue(parseFail("PT17544H"), _msg); //Xdef

		assertTrue(prepare("duration(%minExclusive='P3Y')"), _msg);
		assertTrue(parse("P37M"), _msg);
		assertTrue(parse("P1097D"), _msg);
		assertTrue(parse("PT26305H"), _msg);
		assertTrue(parseFail("P1095D"), _msg);
		assertTrue(parseFail("P1092D"), _msg);
		assertTrue(parseFail("P36M"), _msg);
		assertTrue(parseFail("P1096D"), _msg);
		assertTrue(parseFail("PT26304H"), _msg);

		assertTrue(prepare("duration"), _msg);
		assertTrue(parse("   P37M   "), _msg);

//------------------------------------------------------------------------------
//                          TESTING NOTATION
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("NOTATION(%minInclusive='1')"), _msg);
		assertTrue(checkFail("NOTATION(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("NOTATION(%minExclusive='1')"), _msg);
		assertTrue(checkFail("NOTATION(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("NOTATION(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("NOTATION(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("NOTATION(%totalDigits='2')"), _msg);
		assertTrue(checkFail("NOTATION(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking


//------------------------------------------------------------------------------
//                          TESTING token
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("token(%maxInclusive='asdf')"), _msg);
		assertTrue(checkFail("token(%maxExclusive='qwer')"), _msg);
		assertTrue(checkFail("token(%minInclusive='zxcv')"), _msg);
		assertTrue(checkFail("token(%minExclusive='tyui')"), _msg);
		assertTrue(checkFail("token(%fractionDigits='3')"), _msg);
		assertTrue(checkFail("token(%totalDigits='6')"), _msg);
		assertTrue(checkFail("token(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("token(%whiteSpace='replace')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("token(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("token()"), _msg);
		assertTrue(parse("Hello World!"), _msg);
		assertTrue(parse(" Hello World!", "Hello World!"), _msg);
		assertTrue(parse("Hello World! ", "Hello World!"), _msg);
		assertTrue(parse("\nx\t\r "), _msg);

		// testing facets
		assertTrue(prepare("token(%enumeration=['Hello', 'world'])"), _msg);
		assertTrue(parse("Hello"), _msg);
		assertTrue(parse("world"), _msg);
		assertTrue(parse(" world"), _msg);
		assertTrue(parseFail("hello"), _msg);
		assertTrue(parseFail("World"), _msg);

		assertTrue(prepare("token(%enumeration=['Hello world!'])"), _msg);
		assertTrue(parse("Hello world!"), _msg);
		assertTrue(parse("   Hello   world!   ", "Hello world!"), _msg);
		assertTrue(parseFail("hello"), _msg);
		assertTrue(parseFail("World"), _msg);

		assertTrue(prepare("token(%pattern=['[A-Z][a-z]{3}'])"), _msg);
		assertTrue(parse("Fork"), _msg);
		assertTrue(parse("Enum"), _msg);
		assertTrue(parseFail("dark"), _msg);
		assertTrue(parseFail("Egg"), _msg);
		assertTrue(parseFail("9asd"), _msg);

		assertTrue(prepare("token(%length='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parseFail(" sdf "), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("asdf"), _msg);

		assertTrue(prepare("token(%minLength='3')"), _msg);
		assertTrue(parse("asdfgh"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parse("  asd  "), _msg);
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("as"), _msg);
		assertTrue(parseFail(" as "), _msg);

		assertTrue(prepare("token(%maxLength='5')"), _msg);
		assertTrue(parse("asdfg"), _msg);
		assertTrue(parse("asd"), _msg);
		assertTrue(parseFail("asdfgh"), _msg);
		assertTrue(parseFail("as dfg"), _msg);

		assertTrue(prepare("token"), _msg);
		assertTrue(parse("   Hello world!  "), _msg);

//------------------------------------------------------------------------------
//                          TESTING language
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("language(%minInclusive='1')"), _msg);
		assertTrue(checkFail("language(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("language(%minExclusive='1')"), _msg);
		assertTrue(checkFail("language(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("language(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("language(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("language(%totalDigits='2')"), _msg);
		assertTrue(checkFail("language(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("language(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("language"), _msg);
		assertTrue(parse("cz"), _msg);
		assertTrue(parse("ces"), _msg);
		assertTrue(parse("en"), _msg);
		assertTrue(parse("eng"), _msg);
		assertTrue(parse("cz-Czech-old"), _msg);
		assertTrue(parse("ces-Czech-old"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse(" aB\t"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail(" "), _msg);
		assertTrue(parseFail("a b"), _msg);
		assertTrue(parseFail("a?b"), _msg);

		// testing facets
		assertTrue(prepare("language(%length='2')"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("abc"), _msg);

		assertTrue(prepare("language(%minLength='2',%maxLength='3')"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parse(" abc "), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("abcd"), _msg);


		assertTrue(prepare("language(%enumeration=['ac','bcz'])"), _msg);
		assertTrue(parse("ac"), _msg);
		assertTrue(parse("bcz"), _msg);
		assertTrue(parse("\nac\t"), _msg);
		assertTrue(parseFail("cc"), _msg);

		assertTrue(prepare("language(%pattern=['[a-z]+'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("bc"), _msg);
		assertTrue(parseFail("acz bcz"), _msg);
		assertTrue(parseFail("ab.cz"), _msg);
		assertTrue(parseFail("ab.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING NMTOKEN
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("NMTOKEN(%minInclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKEN(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKEN(%minExclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKEN(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKEN(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("NMTOKEN(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("NMTOKEN(%totalDigits='2')"), _msg);
		assertTrue(checkFail("NMTOKEN(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("NMTOKEN(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("NMTOKEN"), _msg);
		assertTrue(parse("www.syntea.cz"), _msg);
		assertTrue(parse("a_b-c"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse("0123456789abcdef"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("1234"), _msg);
		assertTrue(parse("00"), _msg);
		assertTrue(parse("  \t\n\r0000000000000000000000\t\n\r  "), _msg);
		assertTrue(parse("a:b"), _msg);
		assertTrue(parse("a:b:c"), _msg);
		assertTrue(parse(":"), _msg);
		assertTrue(parse(":::"), _msg);
		assertTrue(parse("_"), _msg);
		assertTrue(parse("-"), _msg);
		assertTrue(parse("."), _msg);
		assertTrue(parse("_a_"), _msg);
		assertTrue(parse(".ab"), _msg);
		assertTrue(parse("-ab"), _msg);
		assertTrue(parse("ab."), _msg);
		assertTrue(parse("ab-"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail(" "), _msg);
		assertTrue(parseFail("a b"), _msg);
		assertTrue(parseFail("a?b"), _msg);
		assertTrue(parseFail("a=b"), _msg);
		assertTrue(parseFail("$"), _msg);
		assertTrue(parseFail("$a"), _msg);
		assertTrue(parseFail("a$"), _msg);

		// testing facets
		assertTrue(prepare("NMTOKEN(%length='2')"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("abc"), _msg);

		assertTrue(prepare("NMTOKEN(%enumeration=['a.cz','b.cz'])"), _msg);
		assertTrue(parse("a.cz"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parse("\na.cz\t"), _msg);
		assertTrue(parseFail("c.cz"), _msg);

		assertTrue(prepare("NMTOKEN(%pattern=['ffff','[a-z]+\\\\.cz'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parseFail("a.cz b.cz"), _msg);
		assertTrue(parseFail("a.b.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING NMTOKENS
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("NMTOKENS(%minInclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKENS(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKENS(%minExclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKENS(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("NMTOKENS(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("NMTOKENS(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("NMTOKENS(%totalDigits='2')"), _msg);
		assertTrue(checkFail("NMTOKENS(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("NMTOKENS(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("NMTOKENS"), _msg);
		assertTrue(parse("www.syntea.cz xdf.cz"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse("0123456789abcdef"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("1234"), _msg);
		assertTrue(parse("00"), _msg);
		assertTrue(parse("  \t\n\r0000000000000000000000\t\n\r  "), _msg);
		assertTrue(parse("a:b"), _msg);
		assertTrue(parse("a:b:c"), _msg);
		assertTrue(parse(":"), _msg);
		assertTrue(parse(":::"), _msg);
		assertTrue(parse("_"), _msg);
		assertTrue(parse("-"), _msg);
		assertTrue(parse("."), _msg);
		assertTrue(parse("_A_"), _msg);
		assertTrue(parse(".ab"), _msg);
		assertTrue(parse("-ab"), _msg);
		assertTrue(parse("ab."), _msg);
		assertTrue(parse("ab-"), _msg);
		assertTrue(parse("a_B-c.d \nAB 0123456789abcdef   a:b   aB\t "), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail(" "), _msg);
		assertTrue(parseFail("a?b"), _msg);
		assertTrue(parseFail("a=b"), _msg);
		assertTrue(parseFail("$"), _msg);
		assertTrue(parseFail("$a"), _msg);
		assertTrue(parseFail("a$"), _msg);
		assertTrue(parseFail("a a$"), _msg);

		// testing facets
		assertTrue(prepare("NMTOKENS(%length='2')"), _msg);
		assertTrue(parse("a b"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("a b c d"), _msg);

		assertTrue(prepare("NMTOKENS(%minLength='2',%maxLength='3')"), _msg);
		assertTrue(parse("a b"), _msg);
		assertTrue(parse("a b c"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("a b c d"), _msg);

		assertTrue(prepare("NMTOKENS(%enumeration=['a.cz b.cz'])"), _msg);
		assertTrue(parse("a.cz b.cz"), _msg);
		assertTrue(parse("\na.cz\t   b.cz  "), _msg);
		assertTrue(parseFail("b.cz a.cz"), _msg);
		assertTrue(parseFail("b.cz"), _msg);
		assertTrue(parseFail("c.cz"), _msg);

		assertTrue(prepare("NMTOKENS(%pattern=['ffff','[a-z]+\\\\.cz'])"),_msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parseFail("ffff b.cz"), _msg);
		assertTrue(parseFail("a.cz b.cz"), _msg);
		assertTrue(parseFail("a.b.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING Name
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("Name(%minInclusive='1')"), _msg);
		assertTrue(checkFail("Name(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("Name(%minExclusive='1')"), _msg);
		assertTrue(checkFail("Name(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("Name(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("Name(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("Name(%totalDigits='2')"), _msg);
		assertTrue(checkFail("Name(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("Name(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("Name"), _msg);
		assertTrue(parse("www.syntea.cz"), _msg);
		assertTrue(parse("a_b-c"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("a:b"), _msg);
		assertTrue(parse("a:b:c"), _msg);
		assertTrue(parse(":"), _msg);
		assertTrue(parse("a:"), _msg);
		assertTrue(parse(":::"), _msg);
		assertTrue(parse("a:::"), _msg);
		assertTrue(parse("_"), _msg);
		assertTrue(parse("_a_"), _msg);
		assertTrue(parse("ab."), _msg);
		assertTrue(parse("ab-"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail(" "), _msg);
		assertTrue(parseFail("a b"), _msg);
		assertTrue(parseFail("a?b"), _msg);
		assertTrue(parseFail("a=b"), _msg);
		assertTrue(parseFail("$"), _msg);
		assertTrue(parseFail("$a"), _msg);
		assertTrue(parseFail("a$"), _msg);
		assertTrue(parseFail("1234"), _msg);
		assertTrue(parseFail("."), _msg);
		assertTrue(parseFail(".ab"), _msg);
		assertTrue(parseFail("-"), _msg);
		assertTrue(parseFail("-ab"), _msg);

		// testing facets
		assertTrue(prepare("Name(%length='2')"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("abc"), _msg);

		assertTrue(prepare("Name(%enumeration=['a.cz','b.cz'])"), _msg);
		assertTrue(parse("a.cz"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parse("\na.cz\t"), _msg);
		assertTrue(parseFail("c.cz"), _msg);

		assertTrue(prepare("Name(%pattern=['ffff','[a-z]+\\\\.cz'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parseFail("a.cz b.cz"), _msg);
		assertTrue(parseFail("a.b.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING NCName
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("NCName(%minInclusive='1')"), _msg);
		assertTrue(checkFail("NCName(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("NCName(%minExclusive='1')"), _msg);
		assertTrue(checkFail("NCName(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("NCName(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("NCName(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("NCName(%totalDigits='2')"), _msg);
		assertTrue(checkFail("NCName(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("NCName(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("NCName"), _msg);
		assertTrue(parse("www.syntea.cz"), _msg);
		assertTrue(parse("a_b-c"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("_"), _msg);
		assertTrue(parse("_a_"), _msg);
		assertTrue(parse("ab."), _msg);
		assertTrue(parse("ab-"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail(" "), _msg);
		assertTrue(parseFail("a b"), _msg);
		assertTrue(parseFail("a?b"), _msg);
		assertTrue(parseFail("a=b"), _msg);
		assertTrue(parseFail("$"), _msg);
		assertTrue(parseFail("$a"), _msg);
		assertTrue(parseFail("a$"), _msg);
		assertTrue(parseFail("1234"), _msg);
		assertTrue(parseFail("."), _msg);
		assertTrue(parseFail(".ab"), _msg);
		assertTrue(parseFail("-"), _msg);
		assertTrue(parseFail("-ab"), _msg);
		assertTrue(parseFail(":"), _msg);
		assertTrue(parseFail("a:b"), _msg);

		// testing facets
		assertTrue(prepare("NCName(%length=2)"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("abc"), _msg);

		assertTrue(prepare("NCName(%enumeration=['a.cz','b.cz'])"), _msg);
		assertTrue(parse("a.cz"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parse("\na.cz\t"), _msg);
		assertTrue(parseFail("c.cz"), _msg);

		assertTrue(prepare("NCName(%pattern=['ffff','[a-z]+\\\\.cz'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("b.cz"), _msg);
		assertTrue(parseFail("a.cz b.cz"), _msg);
		assertTrue(parseFail("a.b.cz"), _msg);

//------------------------------------------------------------------------------
//                          TESTING base64Binary
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("base64Binary(%minInclusive='1')"), _msg);
		assertTrue(checkFail("base64Binary(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("base64Binary(%minExclusive='1')"), _msg);
		assertTrue(checkFail("base64Binary(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("base64Binary(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("base64Binary(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("base64Binary(%totalDigits='2')"), _msg);
		assertTrue(checkFail("base64Binary(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("base64Binary(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("base64Binary"), _msg);
		assertTrue(parse("abcd"), _msg);
		assertTrue(parse("abcd efgh"), _msg);
		assertTrue(parse("abcd\tefgh"), _msg);
		assertTrue(parse("abcd\nefgh"), _msg);
		assertTrue(parse("abcd\refgh"), _msg);
		assertTrue(parse("ab cd\nef\tg\rh"), _msg);
		assertTrue(parse("HbRBHbRBHQw="), _msg);
		assertTrue(parse("HbRBHbRBHQw ="), _msg);
		assertTrue(parse("HbRBHw=="), _msg);
		assertTrue(parse("H\nb\nR\nB\nH\nw\n=\n="), _msg);
		assertTrue(parse("HQ=="), _msg);
//		assertTrue(parse("  abcd\n\t\refgh  "), _msg);//schema makes replace

		// testing errors
		assertTrue(parseFail("abcd="), _msg);
		assertTrue(parseFail("abcd=="), _msg);
		assertTrue(parseFail("HQ"), _msg);
		assertTrue(parseFail("HQ="), _msg);
		assertTrue(parseFail("HbRBHbRBHQw"), _msg);
		assertTrue(parseFail("HbRBHbRBHQw=="), _msg);

		// testing facets
		assertTrue(prepare("base64Binary(%length='4')"), _msg);
		assertTrue(parse("HbRBHw=="), _msg);
		assertTrue(parseFail("HQ=="), _msg);
		assertTrue(parseFail("HbRBHbRBHQw="), _msg);
		assertTrue(parseFail("bRBHw=="), _msg);

		assertTrue(prepare("base64Binary(%minLength='3',%maxLength='4')"),_msg);
		assertTrue(parse("HbRBHw=="), _msg);
		assertTrue(parseFail("HQ=="), _msg);
		assertTrue(parseFail("HbRBHbRBHQw="), _msg);
		assertTrue(parseFail("bRBHw=="), _msg);

		assertTrue(prepare("base64Binary(%enumeration=['HQ==','abcd'])"), _msg);
		assertTrue(parse("HQ=="), _msg);
		assertTrue(parse("abcd"), _msg);
		assertTrue(parse("ab cd"), _msg);
		assertTrue(parseFail("HbRBHw=="), _msg);

		assertTrue(prepare(
			"base64Binary(%pattern=['HQ==','[a-d]*'])"), _msg);
		assertTrue(parse("HQ=="), _msg);
		assertTrue(parse("abcd"), _msg);
		assertTrue(parseFail("ab cd"), _msg);
		assertTrue(parseFail("HbRBHw=="), _msg);

//------------------------------------------------------------------------------
//                          TESTING hexBinary
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("hexBinary(%minInclusive='1')"), _msg);
		assertTrue(checkFail("hexBinary(%maxInclusive='1')"), _msg);
		assertTrue(checkFail("hexBinary(%minExclusive='1')"), _msg);
		assertTrue(checkFail("hexBinary(%maxExclusive='1')"), _msg);
		assertTrue(checkFail("hexBinary(%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("hexBinary(%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("hexBinary(%totalDigits='2')"), _msg);
		assertTrue(checkFail("hexBinary(%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("hexBinary(%whiteSpace='collapse')"), _msg);

		// testing correct values
		assertTrue(prepare("hexBinary"), _msg);
		assertTrue(parse("ab"), _msg);
		assertTrue(parse("AB"), _msg);
		assertTrue(parse("0123456789abcdef"), _msg);
		assertTrue(parse("0123456789ABCDEF"), _msg);
		assertTrue(parse(" aB\t"), _msg);
		assertTrue(parse("1234"), _msg);
		assertTrue(parse("00"), _msg);
		assertTrue(parse("  \t\n\r0000000000000000000000\t\n\r  "), _msg);

		// testing errors
		assertTrue(parseFail("0"), _msg);
		assertTrue(parseFail("123"), _msg);
		assertTrue(parseFail("12 34"), _msg);

		// testing facets
		assertTrue(prepare("hexBinary(%length='4')"), _msg);
		assertTrue(parse("00000000"), _msg);
		assertTrue(parse("  12345678  "), _msg);
		assertTrue(parse("\n\tffffffff\n"), _msg);
		assertTrue(parseFail("ffffff"), _msg);
		assertTrue(parseFail("ffffffffff"), _msg);

		assertTrue(prepare("hexBinary(%minLength='3',%maxLength='4')"),_msg);
		assertTrue(parse("123456"), _msg);
		assertTrue(parse("12345678"), _msg);
		assertTrue(parseFail("1234"), _msg);
		assertTrue(parseFail("1234567890"), _msg);

		assertTrue(prepare("hexBinary(%enumeration=['00ff','abcd'])"), _msg);
		assertTrue(parse("00ff"), _msg);
		assertTrue(parse("abcd"), _msg);
		assertTrue(parse("\nabcd\t"), _msg);
		assertTrue(parseFail("000f"), _msg);

		assertTrue(prepare("hexBinary(%pattern=['ffff','[a-d]*'])"), _msg);
		assertTrue(parse("ffff"), _msg);
		assertTrue(parse("abcd"), _msg);
		assertTrue(parseFail("ab cd"), _msg);
		assertTrue(parseFail("ff00"), _msg);

//------------------------------------------------------------------------------
//                          TESTING union
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail(
			"union(%item=[boolean, short],%minInclusive='1')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%maxInclusive='1')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%minExclusive='1')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%maxExclusive='1')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%whiteSpace='collapse')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%whiteSpace='replace')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean, short],%totalDigits='2')"), _msg);
		assertTrue(checkFail(
			"union(%item=[boolean,short],%fractionDigits='2')"), _msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing correct values
		assertTrue(prepare("union(%item=[boolean, short])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("true"), _msg);
		assertTrue(parse(" false "), _msg);
		assertTrue(parse(" -1 "), _msg);
		assertTrue(parse("234"), _msg);

		// testing errors
		assertTrue(parseFail(""), _msg);
		assertTrue(parseFail("  "), _msg);
		assertTrue(parseFail("x"), _msg);
		assertTrue(parseFail("1 true"), _msg);
		assertTrue(parseFail("1 true"), _msg);

		// testing facets
		assertTrue(prepare("union(%item=[boolean, short]," +
			"%pattern=['true|1'],"+
			"%enumeration=[true,1])"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("true"), _msg);
		assertTrue(parseFail("x"), _msg);
		assertTrue(parseFail("false"), _msg);
		assertTrue(parseFail("0"), _msg);

		assertTrue(prepare("union("+
			"%item=[decimal(%maxInclusive=5), boolean])"), _msg);
		assertTrue(parse("true"), _msg);
		assertTrue(parse("5 "), _msg);
		assertTrue(parseFail("6"), _msg);
		assertTrue(parseFail("5 6"), _msg);
		assertTrue(parseFail("x"), _msg);

		assertTrue(prepare("union(%item=[decimal(%maxInclusive=5),"
			+ "boolean(%pattern=['false'])])"), _msg);
		assertTrue(parse("false"), _msg);
		assertTrue(parse("5 "), _msg);
		assertTrue(parseFail("6"), _msg);
		assertTrue(parseFail("true"), _msg);

		assertTrue(prepare(
			"union(%item=[int,date,dateTime,string(%length=1)])"), _msg);
		assertTrue(parse("x"), _msg);
		assertTrue(parse("1999"), _msg);
		assertTrue(parse("1999-10-11"), _msg);
		assertTrue(parse("1999-10-11T10:11:12"), _msg);

		assertTrue(prepare(
			"union(%item=[string(%length=1),dateTime,date,int])"), _msg);
		assertTrue(parse("x"), _msg);
		assertTrue(parse("1999"), _msg);
		assertTrue(parse("1999-10-11"), _msg);
		assertTrue(parse("1999-10-11T10:11:12"), _msg);

//------------------------------------------------------------------------------
//                          TESTING list
//------------------------------------------------------------------------------
		// testing illegal facets
		setChkSyntax(false); // do not check syntax
		assertTrue(checkFail("list(%item=short,%minInclusive='1')"),_msg);
		assertTrue(checkFail("list(%item=short,%maxInclusive='1')"),_msg);
		assertTrue(checkFail("list(%item=short,%minExclusive='1')"),_msg);
		assertTrue(checkFail("list(%item=short,%maxExclusive='1')"),_msg);
		assertTrue(checkFail("list(%item=short,%whiteSpace='replace')"), _msg);
		assertTrue(checkFail("list(%item=short,%whiteSpace='preserve')"), _msg);
		assertTrue(checkFail("list(%item=short,%totalDigits='2')"), _msg);
		assertTrue(checkFail("list(%item=short,%fractionDigits=2)"),_msg);
		setChkSyntax(chkSyntax); // reset syntax  checking

		// testing fixed facets
		assertTrue(prepare("list(%item=short,%whiteSpace='collapse')"),_msg);

		// testing correct values
		assertTrue(prepare("list(%item=short)"), _msg);
		assertTrue(parse("1"), _msg);
		assertTrue(parse("123 456"), _msg);
		assertTrue(parse("   123 456  -9999  "), _msg);

		// testing errors
//		assertTrue(parseFail(""), _msg); //schema not recognize
//		assertTrue(parseFail("  "), _msg); //schema not recognize
		assertTrue(parseFail("a"), _msg);
		assertTrue(parseFail("   123 456  -99999  "), _msg);

		// testing facets
		assertTrue(prepare("list(%item=short,%enumeration=['123   -4560 0'])"),
			_msg);
		assertTrue(parse("123 -4560 0"), _msg);
		assertTrue(parse(" 123   -4560  0  "), _msg);
		assertTrue(parseFail("123 4560  0"), _msg);

		assertTrue(prepare("list(%item=short," +
			"%pattern=['\\\\s*\\\\d*\\\\s+-\\\\d*\\\\s+\\\\d*\\\\s*'])"), _msg);
		assertTrue(parse("123 -456 0"), _msg);
		assertTrue(parse(" 123   -456  0  "), _msg);
		assertTrue(parseFail("123 456  0"), _msg);

		assertTrue(prepare("list(%item=short,%minLength=3)"), _msg);
		assertTrue(parse("123 -456 0"), _msg);
		assertTrue(parse(" 123   -456  0 99 "), _msg);
		assertTrue(parseFail("123 456"), _msg);

		assertTrue(prepare("list(%item=short,%maxLength=4,%minLength=2)"), _msg);
		assertTrue(parse("3 -4 0"), _msg);
		assertTrue(parse(" -9   9  0 3 "), _msg);
		assertTrue(parseFail(""), _msg); //schema
		assertTrue(parseFail("  "), _msg); //schema
		assertTrue(parseFail("1"), _msg);
		assertTrue(parseFail("1 4 9 123 5"), _msg);

		assertTrue(prepare("list(%item=string(),%length=2)"), _msg);
		assertTrue(parse("1 2"), _msg);
		assertTrue(parse(" bac\n\tzyx"), _msg);
		assertTrue(parseFail(" bac\n\t"), _msg);
		assertTrue(parseFail("1 2 3"), _msg);

		assertTrue(prepare("list(%item=short("+
			"%minInclusive=-9,%maxExclusive=10),%length=3)"), _msg);
		assertTrue(parse("-9 0 9"), _msg);
		assertTrue(parse("1 2 3"), _msg);
		assertTrue(parseFail("-9 0 9 1"), _msg);
		assertTrue(parseFail("-99 0 9"), _msg);
		assertTrue(parseFail("-9 0 99"), _msg);
		assertTrue(parseFail("-9 10 9"), _msg);
		assertTrue(parseFail("1 2"), _msg);
		assertTrue(parseFail("1"), _msg);
		assertTrue(parseFail(""), _msg);

//------------------------------------------------------------------------------
//                          TESTING list and union
//------------------------------------------------------------------------------
		//list contains union
		assertTrue(prepare("list("+
			"%item=union(%item=[short(%minInclusive=-9),boolean]),"+
			"%length=3)"), _msg);
		assertTrue(parse("-9 0 9"), _msg);
		assertTrue(parse("-9 true 9"), _msg);
		assertTrue(parse("false 2 3"), _msg);
		assertTrue(parseFail("1 2"), _msg);

		// union contains list
		assertTrue(prepare("union("+
			"%item=[list(%item=short(%minInclusive=-9),%length=3),"+
			"boolean])"), _msg);
		assertTrue(parse("-9 0 9"), _msg);
		assertTrue(parse("true"), _msg);
		assertTrue(parseFail("false 2 3"), _msg);
		assertTrue(parseFail("1 2"), _msg);

		assertTrue(prepare(
			"list(%item=union(%item=[int,date,dateTime,string(%length=1)]))"),
			_msg);
		assertTrue(parse("x 1999 1999-10-11 1999-10-11T10:11:12"), _msg);
		assertTrue(prepare(
			"list(%item=union(%item=[string(%length=1),dateTime,date,int]))"),
			_msg);
		assertTrue(parse("x 1999 1999-10-11 1999-10-11T10:11:12"), _msg);

////////////////////////////////////////////////////////////////////////////////

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}