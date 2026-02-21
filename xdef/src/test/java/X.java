import java.io.StringWriter;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

public class X extends XDTester {
	public X() {}

	/** Run test and display error info. */
	@Override
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		System.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		XDDocument xd;
		StringWriter swr;
		try {
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_STRING_CODES, "Windows-1250");
			ArrayReporter reporter = new ArrayReporter();
			xd = XDFactory.compileXD(props, //moreAttributes
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <A xd:script='option moreAttributes'/>\n" +
"</xd:def>").createXDDocument();
			parse(xd, "<A a='Таблица' />", reporter);
			assertNoErrorsAndClear(reporter); //for moreAttributes the charset is not checked
			xd = XDFactory.compileXD(props, //moreAttributes
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <A a=';'/>\n" +
"</xd:def>").createXDDocument();
			parse(xd, "<A a='Таблица' />", reporter);
			assertNoErrorsAndClear(reporter); //for moreAttributes the charset is not checked
			xd = XDFactory.compileXD(props, //moreAttributes
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <A a='string();'/>\n" +
"</xd:def>").createXDDocument();
			parse(xd, "<A a='Таблица' />", reporter);
			assertTrue(reporter.toString().contains("XDEF823")); // chaset is checked
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
		try {
			xd = compile(
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A|X|Y|Z'>\n" +
"	/* BNF grammar rules of email address according to RFC 5321. */\n"+
"  <xd:BNFGrammar name='EMAILADDR'>\n" +
"FWS::=           [ #9] /* Folding white space */ \n" +
"ASCIICHAR::=     [ -~] /* Printable ASCII character */ \n" +
"Let_dig::=       [0-9] | $letter\n" +
"Ldh_str::=       '-'+ Let_dig+\n"+
"sub_domain::=    Let_dig+ Ldh_str*\n" +
"Domain::=        sub_domain ( '.' sub_domain )*\n"+
"IPv4::=          Snum ('.'  Snum){3}\n"+
"Snum::=          ( '2' ([0-4] [0-9] | '5' [0..5]) ) | [0-1] [0-9]{2} | [0-9]{1,2}\n"+
"IPv6::=          IPv6_full | IPv6_comp | IPv6v4_full | IPv6v4_comp\n" +
"IPv6_hex::=      [0-9a-fA-F]{1,4}\n" +
"IPv6_full::=     IPv6_hex ( ':' IPv6_hex ){7}\n" +
"IPv6_comp::=     ( IPv6_hex ( ':' IPv6_hex ){0,5} )? '::' ( IPv6_hex (':' IPv6_hex){0,5} )?\n" +
"/* The '::' represents at least 2 16-bit groups of zeros.\n" +
"	No more than 6 groups in addition to the '::' may be present. */\n" +
"IPv6v4_full::=   IPv6_hex ( ':' IPv6_hex ){5} ':' IPv4_addr\n" +
"IPv6v4_comp::=   ( IPv6_hex (':' IPv6_hex){0,3} )? '::'\n" +
"                 ( IPv6_hex (':' IPv6_hex){0,3} ':' )? IPv4_addr\n" +
"/* The '::' represents at least 2 16-bit groups of zeros.  No more than 4 groups in\n" +
"   addition to the '::' and IPv4-address-literal may be present. */\n" +
"IPv4_addr::=     IPv4\n"+
"IPv6_addr::=     'IPv6:' IPv6\n"+
"Std_tag::=       Ldh_str /*Std-tag MUST be specified in a Standards-Track RFC and registered with IANA*/\n" +
"dcontent::=      [!-Z] | [^-~] /* %d33-90 | %d94-126 Printable US-ASCII; excl. [, ', ]*/\n" +
"General_addr::=  Std_tag ':' ( dcontent )+\n" +
"address::=       '[' ( IPv4_addr | IPv6_addr | General_addr ) ']' /* See Section 4.1.3 */\n" +
"atext::=         ($letter | ('\\' ('[' | ']' | [\\&quot;@/ ()&lt;>,;.:])) | [0-9_!#$%&amp;'*+/=?^`{|}~])+\n"+
"Atom::=          atext ('-'+ atext)*\n" +
"Dot_string::=    Atom ('.'  Atom)*\n" +
"qtextSMTP::=     [ !#-Z^-~] | '[' | ']'\n" +
"/* i.e., within a quoted string, any ASCII graphic or space is permitted without\n" +
"   blackslash-quoting except double-quote and the backslash itself. */\n" +
"quoted_pair::=   '\\' ASCIICHAR /* %d92 %d32-126 */\n" +
"/* i.e., backslash followed by any ASCII graphic (including itself) or SPace */\n" +
"QcontentSMTP::=  quoted_pair | qtextSMTP\n" +
"Quoted_string::= '\"' QcontentSMTP* '\"'\n" +
"Local_part::=    Dot_string | Quoted_string /* MAY be case-sensitive */\n" +
"Mailbox::=       Local_part '@' ( address | Domain ) $rule\n"+
"comment::=       ( commentList $rule ) FWS?\n"+
"commentList::=   ( FWS? '(' commentPart* ')' )+\n"+
"commentPart::=   ( (ASCIICHAR - [()]) | $letter )+ ( commentList)? $rule\n"+
"text::=          ( ( comment* (textItem | comment)* ) | comment* ptext )? comment*\n"+
"textItem::=      FWS? '=?' charsetName ( 'Q?' qtext | 'B?' btext ) '?='\n"+
"charsetName::=   ( [a-zA-Z] ('-'? [a-zA-Z0-9]+)* ) $rule '?' \n"+
"ptext::=         FWS? (ASCIICHAR - [@>&lt;()=])+ $rule /* Printable ASCII character without @>&lt;()= */\n"+
"qtext::=         FWS? ( hexOctet | ASCIICHAR - [=?] )+ $rule /* Quoted text */\n" +
"hexOctet::=      '=' [0-9A-F] [0-9A-F]\n"+
"btext::=         [a-zA-Z0-9+/]+ '='? '='? $rule /* Base64 text */ \n" +
"emailAddr::=     ( text? FWS? '&lt;' Mailbox '>' | comment* Mailbox ) comment*\n"+
"  </xd:BNFGrammar>\n" +
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
"    <B xd:script = '*;' a = 'required; myEmail();' />\n" +
"  </A>\n" +
"  <X>\n" +
"    <B xd:script = '*;' a = 'required; price(); onTrue x(getParseResult())'/>\n" +
"  </X>\n" +
"  <Y>\n" +
"    <Z xd:script = '*;'\n" +
"      a=\"required; manufactureDateType(); finally {\n" +
"          outln(getQnamePrefix('x:y') + ', ' + getQnameLocalpart('y')\n" +
"             + ', ' + isLeapYear(now()) + ', ' + easterMonday(now())); \n" +
"        } onTrue {\n" +
"          if ('--'.equals(getText())) getParseResult().setValue(null);\n" +
"            date = getParseResult().datetimeValue();\n" +
"        }\" />\n" +
"  </Y>\n" +
"  <Z> required; string(1); onFalse out('error: ' + getText()); </Z>\n" +
"</xd:def>").createXDDocument();

			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<A><B a='a--b@b--c'/><B a='&lt;a@b>'/><B a='\"a b\"&lt;a@b>'/><B a='JS--N@S.CZ'/></A>",
				null);
			try {
				xd.xparse("<X><B a='0.5 USD'/><B a='1.2 CZK'/></X>", null);
				assertTrue("0.5;USD\n1.2;CZK\n".equals(swr.toString()), swr.toString());
			} catch (RuntimeException ex) {
				if (ex.getMessage() == null || !ex.getMessage().contains("XDEF998")) {
					fail(ex);
				}
			}
			try {
				xd.setStdOut(swr = new StringWriter());
				xd.xparse("<Y> <Z a='--'/> </Y>", null);
				assertTrue(xd.getVariable("date").isNull());
				assertTrue("x, y, false, 2025-04-21\n".equals(swr.toString()), swr.toString());
			} catch (RuntimeException ex) {
				if (ex.getMessage() == null || !ex.getMessage().contains("XDEF998")) {
					fail(ex);
				}
			}
			try {
				xd.xparse("<Y> <Z a='2025-04-03'/> </Y>", null);
				assertEq("2025-04-03", xd.getVariable("date").toString());
			} catch (RuntimeException ex) {
				if (ex.getMessage() == null || !ex.getMessage().contains("XDEF998")) {
					fail(ex);
				}
			}
			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<Z>xx</Z>", null);
			assertEq("error: xx", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test.
	 * @param args the command line arguments.
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}