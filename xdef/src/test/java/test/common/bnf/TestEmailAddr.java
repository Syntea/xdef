package test.common.bnf;

import java.io.ByteArrayOutputStream;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import org.xdef.sys.SUtils;

/** Test of email address.
 * @author Vaclav Trojan
 */
public class TestEmailAddr extends STester {

	public TestEmailAddr() {super();}

	private static String readSrc(final StringParser q,
		final String s,
		final String src) {
		if (q.isToken(s) && q.isSpaces()) {
			if (q.isInteger()) {
				int i = q.getParsedInt();
				q.isSpaces();
				if (q.isInteger()) {
					return src.substring(i, q.getParsedInt());
				}
			}
		}
		return null;
	}

	private static String readBtext(final String s, final String charsetName) {
		try {
			return new String(SUtils.decodeBase64(s), charsetName);
		} catch (Exception ex) {
			return "?";
		}
	}

	private static String readQtext(final String s, final String charsetName) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '=' && i + 2 < s.length()) {
				String hexMask = "0123456789ABCDEF";
				ch = (char) ((hexMask.indexOf(s.charAt(++i)) << 4)
					+ hexMask.indexOf(s.charAt(++i)));
			}
			b.write((byte) ch);
		}
		try {
			return new String(b.toByteArray(), charsetName);
		} catch (Exception ex) {
			return "?";
		}
	}

	private static String readAddress(Object[] code, String s) {
		/** Email source value. */
		String value;
		/** Email domain. */
		String domain;
		/** Email user. */
		String localPart;
		/** Email user name. */
		String userName;
		String charsetName;
		value = s;
		domain = localPart = userName = charsetName = null;
		for (int i = 0; code != null && i < code.length; i++) {
			StringParser q = new StringParser((String) code[i]);
			q.isSpaces();
			String t;
			if ((t = readSrc(q, "emailAddr", s)) != null) {
				int ndx = t.indexOf('@');
				localPart = t.substring(0, ndx);
				domain = t.substring(ndx + 1);
			} else if ((t = readSrc(q, "comment", s)) != null) {
				t = t.substring(1, t.length() -1).trim();
				if (!t.isEmpty()) {
					if (localPart == null) {
						if (userName == null) {
							userName = t;
						} else {
							userName += t;
						}
					} else if (userName == null) {
						userName = t;
					}
				}
			} else if ((t = readSrc(q, "ptext", s)) != null) {
				if (userName == null) {
					userName = t;
				} else {
					userName += t;
				}
			} else if ((t = readSrc(q, "charsetName", s)) != null) {
				charsetName = t;
			} else if ((t = readSrc(q, "qtext", s)) != null) {
				t = readQtext(t, charsetName);
				if (userName == null) {
					userName = t;
				} else {
					userName += t;
				}
				charsetName = null;
			} else if ((t = readSrc(q, "btext", s)) != null) {
				t = readBtext(t, charsetName);
				if (userName == null) {
					userName = t;
				} else {
					userName += t;
				}
				charsetName = null;
			} else {
				System.out.println("CODE: " + code[i]);
			}
		}
		return value + ";" + userName + ";" + localPart + '@' + domain;
	}

	private String parse(BNFGrammar grammar, String name, String s) {
		try {
			StringParser p = new StringParser(s);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				Object[] code = grammar.getParsedObjects();
				readAddress(code, grammar.getParsedString());
				return s;
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			return printThrowable(ex);
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			BNFGrammar G = BNFGrammar.compile(
"S ::= (' ' | #9)+ /* linear white space */\n" +
"comment ::= commentList $rule\n" +
"commentList ::= ( '(' commentPart* ')' (S? '(' commentPart* ')')* ) S?\n" +
"commentPart ::= ($ASCIIChar - [()])+ (S? commentList)?\n" +
"delimiters  ::=  specials | S | comment\n" +
"specials ::=  '(' | ')' | '<' | '>' | '@' | ',' | ';' \n" +
"              | ':' | '\\' | '\"' |  '.' | '[' | ']'\n"+
"atom ::= ($ASCIIChar - $ctlrChar - specials - ' ')+\n" +
"emailAddr ::= localPart domain $rule\n" +
"emailAddr1 ::= '<' emailAddr '>' \n" +
"localPart ::= atom ('.' atom)*\n" +
"domain ::= '@' atom ('.' atom)*\n" +
"email ::= (text? S? emailAddr1 | (comment* emailAddr)) (S? comment)*\n"+
"text ::= ((comment* (textItem | comment)*) | S? ptext)?\n"+
"textItem ::= S? '=?' charsetName ('Q?' qtext | 'B?' btext) '?='\n"+
"charsetName ::= ([a-zA-Z] ('-'? [a-zA-Z0-9]+)*) $rule '?' \n"+
"ptext ::=  (($ASCIIChar - $ctlrChar - [@><()=])+) $rule\n"+
"qtext ::= ((hexOctet | $ASCIIChar - $ctlrChar - [=?])+) $rule /*quoted*/\n"+
"hexOctet ::= '=' [0-9A-F] [0-9A-F]\n"+
"btext ::= ([a-zA-Z0-9+/]+ '='? '='?) $rule /* base64 */");

			String s;
			s = "1@2";
			assertEq(s, parse(G, "email", s));
			s = "<1E.-J@s-e_.z.cz>";
			assertEq(s, parse(G, "email", s));
			s = "(ab)a@b";
			assertEq(s, parse(G, "email", s));
			s = "a@b(ab)";
			assertEq(s, parse(G, "email", s));
			s = "(a (b c) b)a@b (de) (fg)";
			assertEq(s, parse(G, "email", s));
			s = "(a (b c) b)a@b (de) (fg)";
			assertEq(s, parse(G, "email", s));
			s = "El-,Ji. <EJ@sez.cz>";
			assertEq(s, parse(G, "email", s));
			s = "=?UTF-8?B?RZZhIEt1xb5lbG92w6E=?= <ep@e.c>";
			assertEq(s, parse(G, "email", s));
			s = "=?UTF-8?Q?P. B=C3=BDk?= <p@s>";
			assertEq(s, parse(G, "email", s));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}