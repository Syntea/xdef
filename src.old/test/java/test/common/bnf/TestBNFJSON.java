package test.common.bnf;

import cz.syntea.xdef.sys.BNFGrammar;
import cz.syntea.xdef.sys.SParser;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import test.utils.STester;

/** Test of BNF of JSON.
 * @author Vaclav Trojan
 */
public class TestBNFJSON extends STester {

	public TestBNFJSON() {super();}

	Document _doc;
	StringParser _p;
	StringBuffer _sb = new StringBuffer();

	/** pair ::= string S (":" | "=>") S value
	 * @param e element where to add value.
	 * @return true if source fits to rule.
	 */
	boolean isStringOrPair(Element e, boolean pair) {
		if (!isString()) {
			return false;
		}
		String s = _sb.toString();
		_p.isSpaces();
		if (_p.isChar(':')) {
			_p.isSpaces();
			Element e1 = _doc.createElement("pair");
			e1.appendChild(_doc.createTextNode(s));
			if (isValue(e1)) {
				e.appendChild(e1);
			} else {
				_p.error("JSON001", "Value expected");
			}
		} else {
			Element e1 = _doc.createElement("string");
			e1.appendChild(_doc.createTextNode(s));
			e.appendChild(e1);
			if (pair) {
				_p.error("JSON002", "Pair expected");
			}
		}
		return true;
	}

	/** string ::= ('"' ([^\"] | control-character)* '"' |
	 *     "'" ([^\'] | control-character)* "'")
	 * @param e element where to add value.
	 * @return true if source fits to rule.
	 */
	boolean isString() {
		char delimiter;
		if ((delimiter = _p.isOneOfChars("'\"")) == SParser.NOCHAR) {
			return false;
		}
		char c;
		_sb.setLength(0);
		while ((c = _p.notChar(delimiter)) != SParser.NOCHAR) {
			if (c == '\\') {
				if (_p.isChar(delimiter)) {
					_sb.append(delimiter);
				} else if (_p.isChar('t')) {
					_sb.append('\t');
				} else if (_p.isChar('r')) {
					_sb.append('\r');
				} else if (_p.isChar('n')) {
					_sb.append('\n');
				} else if (_p.isChar('b')) {
					_sb.append('\b');
				} else if (_p.isChar('f')) {
					_sb.append('\f');
				} else if (_p.isChar('u')) {
					int u = 0;
					final String hexa = "0123456789abcdefABCDEF";
					for (int i = 0; i < 4; i++) {
						int j = hexa.indexOf(_p.getCurrentChar());
						if (j < 0) {
							_p.error("JSON003", "Hexadecimal digit expected");
							break;
						}
						_p.nextChar();
						if (j >= 16) {
							j -= 6;
						}
						u = u*16 + j;
					}
					_sb.append((char) u);
				}
			} else {
				_sb.append(c);
			}
		}
		if (!_p.isChar(delimiter)) {
			_p.error("JSON004", "Ending string delimiter is missing");
		}
		return true;
	}

	/** Parse value
	 * value ::= pair | "null" | boolean | number | string | array | object
	 * @param e element where to add value.
	 * @return true if source fits to rule.
	 */
	boolean isValue(Element e) {
		if (isStringOrPair(e, false)) {
			return true;
		} else if (_p.isToken("true")) {
			Element e1 = _doc.createElement("boolean");
			e1.appendChild(_doc.createTextNode("true"));
			e.appendChild(e1);
			return true;
		} else if (_p.isToken("false")) {
			Element e1 = _doc.createElement("boolean");
			e1.appendChild(_doc.createTextNode("false"));
			e.appendChild(e1);
			return true;
		} else if (_p.isToken("null")) {
			e.appendChild(_doc.createElement("null"));
			return true;
		} else if (_p.isSignedFloat() || _p.isSignedInteger()) {
			Element e1 = _doc.createElement("number");
			e1.appendChild(_doc.createTextNode(_p.getParsedString()));
			e.appendChild(e1);
			return true;
		} else if (_p.isChar('[')) {
			Element e1 = _doc.createElement("array");
			e.appendChild(e1);
			_p.isSpaces();
			if (isValue(e1)) {
				_p.isSpaces();
				while (_p.isChar(',')) {
					_p.isSpaces();
					if (isValue(e1)) {
						_p.isSpaces();
					} else {
						_p.error("JSON001", "Value expected");
					}
				}
			}
			_p.isSpaces();
			if (!_p.isChar(']')) {
				_p.error("JSON005", "']' expected");
			}
			return true;
		} else if (_p.isChar('{')) {
			_p.isSpaces();
			Element e1 = _doc.createElement("object");
			if (isStringOrPair(e1, true)) {
				_p.isSpaces();
				while (_p.isChar(',')) {
					_p.isSpaces();
					if (!isStringOrPair(e1, true)) {
						_p.error("JSON006", "Pair expected");
					}
				}
			}
			_p.isSpaces();
			if (!_p.isChar('}')) {
				_p.error("JSON007", "'}' expected");
			}
			e.appendChild(e1);
			return true;
		}
		return false;
	}

	/** Convert value sourve JSON to element.
	 * @param source source with JSON.
	 * @return converted element.
	 */
	Element JsonToXML(String source) {
		 _doc = KXmlUtils.newDocument(null, "x", null);
		 Element root = _doc.getDocumentElement();
		 _p = new StringParser(source);
		 _p.isSpaces();
		 if (!isValue(root)) {
			 _p.error("JSON001", "Value expected");
		 }
		 _p.isSpaces();
		 if (!_p.eos()){
			 _p.error("JSON008", "Characters after document");
		 }
		 if (_p.errors()) {
			 _p.checkAndThrowErrors();
		 }
		 return (Element) root.getChildNodes().item(0);
	}

	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			BNFGrammar grammar;
			grammar = BNFGrammar.compile(null,
				new File(getDataDir() + "TestJSON.bnf"), null);
//			grammar.display(System.out, true);

////////////////////////////////////////////////////////////////////////////////
			String s;
			s = "{}";
			assertEq(s, parse(grammar, "value", s));
			s ="1";
			assertEq(s, parse(grammar, "value", s));
			s ="-2";
			assertEq(s, parse(grammar, "value", s));
			s ="3.333";
			assertEq(s, parse(grammar, "value", s));
			s ="4e17";
			assertEq(s, parse(grammar, "value", s));
			s ="\"abc\"";
			assertEq(s, parse(grammar, "value", s));
			s ="\"x\\n\"";
			assertEq(s, parse(grammar, "value", s));
			s ="null";
			assertEq(s, parse(grammar, "value", s));
			s ="true";
			assertEq(s, parse(grammar, "value", s));
			s ="false";
			assertEq(s, parse(grammar, "value", s));
			s = "[]";
			assertEq(s, parse(grammar, "value", s));
			s = "[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null]";
			assertEq(s, parse(grammar, "value", s));
			s = "[1, -2, 3.333, 4e17, \"abc\", \"x\\n\", null," +
				"[2.1, 2.2, [\"2.2.1\"]], \"key\"=>\"value\"]";
			assertEq(s, parse(grammar, "value", s));
			s = "'a': 'b'";
			assertEq(s, parse(grammar, "value", s));
			s = "{}";
			assertEq(s, parse(grammar, "value", s));
			s = "{ 'a': [1, null, 'a': 'b', {'c': []}]}";
			assertEq(s, parse(grammar, "value", s));

			s = "[ 1, -2,3.333, 4e17, \"abc\", \"x\\n\", null,"+
				"[2.1, 2.2, [\"2.2.1\" ]], false, true,\"\","+
				"\"key\": \"value\", 'abc\"def' : [ ], [],"+
				"{'x':{'a':'b'}}, {}]";
			assertEq(s, parse(grammar, "value", s));

			s = "array ( 1, -2,3.333, 4e17, \"abc\", \"x\\n\", null,"+
				"array(2.1, 2.2, array(\"2.2.1\" ) ), false,true,\"\","+
				"\"key\" => \"value\", 'abc\"def' => array( ), array(),"+
				"{'x':{'a':'b'}}, {})";
			assertEq(s, parse(grammar, "value", s));
			s = "[1,-2,3.333,4.0e+17,\"abc\","+
				"\"\\u00e1\\n\",null,[2.1,2.2,[\"2.2.1\"]],false,true,\"\","+
				"\"key\":\"value\",\"abc\\\"def\":[]]";
			assertEq(s, parse(grammar, "value", s));
			s = "{\"0\":1,\"1\":-2,\"2\":3.333,\"3\":4.0e+17,\"4\":\"abc\","+
				"\"5\":\"\\u00e1\\n\",\"6\":null,\"7\":[2.1,2.2,[\"2.2.1\"]],"+
				"\"8\":false,\"9\":true,\"10\":\"\",\"key\":\"value\","+
				"\"abc\\\"def\":[]}";
			assertEq(s, parse(grammar, "value", s));
//			s =
//"\"root\": {\n"+
//"  \"glossary\": {\n"+
//"     \"title\": \"example glossary\",\n"+
//"     \"GlossDiv\": {\n"+
//"        \"title\": \"S\",\n"+
//"        \"GlossList\": {\n"+
//"           \"GlossEntry\": {\n"+
//"              \"ID\": \"SGML\",\n"+
//"              \"SortAs\": \"SGML\",\n"+
//"              \"GlossTerm\": \"Standard Generalized Markup Language\",\n"+
//"              \"Acronym\": \"SGML\",\n"+
//"              \"Abbrev\": \"ISO 8879:1986\",\n"+
//"              \"GlossDef\": {\n"+
//"                 \"para\": \"A meta-markup used to create DocBook.\",\n"+
//"                 \"GlossSeeAlso\": [\n"+
//"                    \"GML\",\n"+
//"                    \"XML\"\n"+
//"                 ]\n"+
//"              },\n"+
//"              \"GlossSee\": \"markup\"\n"+
//"           }\n"+
//"        }\n"+
//"     }\n"+
//"  }\n"+
//"}";
//			System.out.println(s);
//			System.out.println(KXmlUtils.nodeToString(JsonToXML(s), true);

			//http://www.json.org/example.html
//			s =
//"[\n"
//+ "\t1,\n"
//+ "\t-2,\n"
//+ "\t3.333,\n"
//+ "\t4.0e+17,\n"
//+ "\t\"abc\",\n"
//+ "\t\"\\u00e1\\n\",\n"
//+ "\tnull,\n"
//+ "\t[\n"
//+ "\t\t2.1,2.2,\n"
//+ "\t\t[\"2.2.1\"]\n"
//+ "\t],\n"
//+ "\tfalse,\n"
//+ "\ttrue,\n"
//+ "\t\"\",\n"
//+ "\t\"key\":\"value\",\n"
//+ "\t\"abc\\\"def\":[]\n"
//+ "]\n";
//
//			System.out.println(s);
//			Element el = JsonToXML(s);
//			s = KXmlUtils.nodeToString(el, true);
//			System.out.println(s);

		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
