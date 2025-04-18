import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDValue;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class X extends XDTester {
	public X() {}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		String s;
		XDDocument xd;
		StringWriter swr;
		XDValue val;
		try {
			xd = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A|X|Y|Z'>\n" +
"\n" +
"  <xd:BNFGrammar name='EMAILADDR'>\n" +
"/***  RFC 5321 ***/\n"+
"    FWS           ::= [ #9]+ /* Folding white space */\n" +
"    ASCIICHAR     ::= [ -~]  /* Printable ASCII characters */\n" +
"    Let_dig       ::= [0-9] | $letter\n" +
"    Ldh_str       ::= '-'+ Let_dig+\n" +
"    sub_domain    ::= Let_dig+ Ldh_str*\n" +
"    Domain        ::= sub_domain ( '.' sub_domain )*\n" +
"    IPv4          ::= Snum ('.'  Snum){3}\n" +
"    Snum          ::= ( '2' ([0-4] [0-9] | '5' [0..5]) ) | [0-1] [0-9]{2} | [0-9]{1,2}\n" +
"    IPv6          ::= IPv6_full | IPv6_comp | IPv6v4_full | IPv6v4_comp\n" +
"    IPv6_hex      ::= [0-9a-fA-F]{1,4}\n" +
"    IPv6_full     ::= IPv6_hex ( ':' IPv6_hex ){7}\n" +
"    IPv6_comp     ::= ( IPv6_hex ( ':' IPv6_hex ){0,5} )? '::' ( IPv6_hex (':' IPv6_hex){0,5} )?\n" +
"			       /* The '::' represents at least 2 16-bit groups of zeros. */\n" +
"			       /* No more than 6 groups in addition to the '::' may be present. */\n" +
"    IPv6v4_full   ::= IPv6_hex ( ':' IPv6_hex ){5} ':' IPv4_addr\n" +
"    IPv6v4_comp   ::= ( IPv6_hex (':' IPv6_hex){0,3} )? '::'\n" +
"                  ( IPv6_hex (':' IPv6_hex){0,3} ':' )? IPv4_addr\n" +
"			       /* The '::' represents at least 2 16-bit groups of zeros.  No more than 4 groups in */\n" +
"			       /* addition to the '::' and IPv4-address-literal may be present. */\n" +
"    IPv4_addr     ::= IPv4\n" +
"    IPv6_addr     ::= 'IPv6:' IPv6\n" +
"    Std_tag       ::= Ldh_str\n" +
"			       /* Std-tag MUST be specified in a Standards-Track RFC and registered with IANA*/\n" +
"    dcontent      ::= [!-Z] | [^-~] /* %d33-90 | %d94-126 Printable US-ASCII; excl. [, ', ] */\n" +
"    General_addr  ::= Std_tag ':' ( dcontent )+\n" +
"    address       ::= '[' ( IPv4_addr | IPv6_addr | General_addr ) ']'\n" +
"    atext         ::= ( $letter | ('\\' ('[' | ']' | [\\\"@/ ()&lt;&gt;,;.:]))\n"+
"                  | [0-9_!#$%&amp;'*+/=?^`{|}~] )+\n" +
"    Atom          ::= atext ('-'+ atext)*\n" +
"    Dot_string    ::= Atom ('.'  Atom)*\n" +
"    qtextSMTP     ::= [ !#-Z^-~] | '[' | ']'\n" +
"			       /* i.e., within a quoted string, any ASCII graphic or space is permitted without */\n" +
"			       /* blackslash-quoting except double-quote and the backslash itself. */\n" +
"    quoted_pair   ::= '\\' ASCIICHAR\n" +
"    QcontentSMTP  ::= quoted_pair | qtextSMTP\n" +
"    Quoted_string ::= '\"' QcontentSMTP* '\"'\n" +
"    Local_part    ::= Dot_string | Quoted_string /* MAY be case-sensitive */\n" +
"    Mailbox       ::= Local_part '@' ( address | Domain )\n" +
"    comment       ::= ( commentList ) FWS?\n" +
"    commentList   ::= ( FWS? '(' commentPart* ')' )+\n" +
"    commentPart   ::= ( (ASCIICHAR - [()]) | $letter )+ ( commentList)?\n" +
"    text          ::= ( ( comment* (textItem | comment)* ) | comment* ptext )? comment*\n" +
"    textItem      ::= FWS? '=?' charsetName ( 'Q?' qtext | 'B?' btext ) '?='\n" +
"    charsetName   ::= ( [a-zA-Z] ('-'? [a-zA-Z0-9]+)* )'?' \n" +
"    ptext         ::= FWS? ( ASCIICHAR - [@&lt;&gt;()=] )+\n" +
"			       /* Printable ASCII character without @&lt;&gt;()= */\n" +
"    qtext         ::= FWS? ( hexOctet | ASCIICHAR - [=?] )+ /* Quoted text */\n" +
"    hexOctet      ::= '=' [0-9A-F] [0-9A-F]\n" +
"    btext         ::= [a-zA-Z0-9+/]+ '='? '='? /* Base64 text */\n" +
"    emailAddr     ::= ( text? FWS? '&lt;' Mailbox '&gt;' | comment* Mailbox ) comment*\n" +
"  </xd:BNFGrammar>\n" +
"\n" +
"  <xd:declaration>\n" +
"    external Datetime date;\n" +
"    type manufactureDateType union(%item=[date(), xdatetime('\"--\"')]); /* date or -- */\n" +
"    type myEmail EMAILADDR.rule('emailAddr');\n" +
"    void x(ParseResult val) {\n" +
"      try {\n" +
"        Price price = (Price) val;\n" +
"        outln(price.amount() + ';' + price.currency().currencyCode());\n" +
"      } catch (Exception e) {\n" +
"        outln('Exception: ' + e);\n" +
"      }\n" +
"    }\n" +
"  </xd:declaration>\n" +
"\n" +
"  <A>\n" +
"    <B xd:script = '*;' a = 'required; myEmail();'/>\n" +
"  </A>\n" +
"\n" +
"  <X>\n" +
"    <B xd:script = '*;' a = 'required; price(); onTrue x(getParsedResult())'/>\n" +
"  </X>\n" +
"\n" +
"  <Y>\n" +
"    <Z xd:script = '*;'\n" +
"      a=\"required; manufactureDateType(); finally {\n" +
"          outln(getQnamePrefix('x:y') + ', ' + getQnameLocalpart('y')\n" +
"             + ', ' + isLeapYear(now()) + ', ' + easterMonday(now())); \n" +
"        } onTrue {\n" +
"          if ('--'.equals(getText())) getParsedResult().setValue(null);\n" +
"            date = getParsedResult().datetimeValue();\n" +
"        }\"/>\n" +
"  </Y>\n" +
"  <Z> required; string(1); onFalse out('error: ' + getText()); </Z>\n" +
"</xd:def>").createXDDocument();

			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<A><B a='a@b--c'/><B a='a--b@b'/><B a='JOS--NT@SEZN.CZ'/></A>", null);
			xd.xparse("<X><B a='0.5 USD'/><B a='1.2 CZK'/></X>", null);
			s = swr.toString();
			assertTrue("0.5;USD\n1.2;CZK\n".equals(s), s);

			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<Y> <Z a='--'/> </Y>", null);
			assertTrue(xd.getVariable("date").isNull());
			s = swr.toString();
			assertTrue("x, y, false, 2025-04-21\n".equals(s), s);
			xd.xparse("<Y> <Z a='2025-04-03'/> </Y>", null);
			assertEq("2025-04-03", xd.getVariable("date").toString());
			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<Z>xx</Z>", null);
			assertEq("error: xx", s = swr.toString());
			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<Z/>", new ArrayReporter());
			assertEq("error: null", s = swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
