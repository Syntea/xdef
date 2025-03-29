import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class X extends XDTester {
	public X() {}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;
		try {
			xd = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A|X'>\n" +
"\n" +
"  <xd:BNFGrammar name='EMAILADDR'>\n" +
"    FWS           ::= [ #9]+\n" +
"    ASCIICHAR     ::= [ -~]\n" +
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
"    IPv6v4_full   ::= IPv6_hex ( ':' IPv6_hex ){5} ':' IPv4_addr\n" +
"    IPv6v4_comp   ::= ( IPv6_hex (':' IPv6_hex){0,3} )? '::'\n" +
"                  ( IPv6_hex (':' IPv6_hex){0,3} ':' )? IPv4_addr\n" +
"    IPv4_addr     ::= IPv4\n" +
"    IPv6_addr     ::= 'IPv6:' IPv6\n" +
"    Std_tag       ::= Ldh_str\n" +
"    dcontent      ::= [!-Z] | [^-~]\n" +
"    General_addr  ::= Std_tag ':' ( dcontent )+\n" +
"    address       ::= '[' ( IPv4_addr | IPv6_addr | General_addr ) ']'\n" +
"    atext         ::= ( $letter | ('\\' ('[' | ']' | [\\\"@/ ()&lt;&gt;,;.:]))\n"+
"                  | [0-9_!#$%&amp;'*+/=?^`{|}~] )+\n" +
"    Atom          ::= atext ('-'+ atext)*\n" +
"    Dot_string    ::= Atom ('.'  Atom)*\n" +
"    qtextSMTP     ::= [ !#-Z^-~] | '[' | ']'\n" +
"    quoted_pair   ::= '\\' ASCIICHAR\n" +
"    QcontentSMTP  ::= quoted_pair | qtextSMTP\n" +
"    Quoted_string ::= '\"' QcontentSMTP* '\"'\n" +
"    Local_part    ::= Dot_string | Quoted_string\n" +
"    Mailbox       ::= Local_part '@' ( address | Domain ) $rule\n" +
"    comment       ::= ( commentList $rule ) FWS?\n" +
"    commentList   ::= ( FWS? '(' commentPart* ')' )+\n" +
"    commentPart   ::= ( (ASCIICHAR - [()]) | $letter )+ ( commentList)? $rule\n" +
"    text          ::= ( ( comment* (textItem | comment)* ) | comment* ptext )? comment*\n" +
"    textItem      ::= FWS? '=?' charsetName ( 'Q?' qtext | 'B?' btext ) '?='\n" +
"    charsetName   ::= ( [a-zA-Z] ('-'? [a-zA-Z0-9]+)* ) $rule '?' \n" +
"    ptext         ::= FWS? ( ASCIICHAR - [@&lt;&gt;()=] )+ $rule\n" +
"    qtext         ::= FWS? ( hexOctet | ASCIICHAR - [=?] )+ $rule \n" +
"    hexOctet      ::= '=' [0-9A-F] [0-9A-F]\n" +
"    btext         ::= [a-zA-Z0-9+/]+ '='? '='? $rule\n" +
"    emailAddr     ::= ( text? FWS? '&lt;' Mailbox '&gt;' | comment* Mailbox ) comment*\n" +
"  </xd:BNFGrammar>\n" +
"\n" +
"  <xd:declaration>\n" +
"    type myEmail EMAILADDR.rule('emailAddr');\n" +
"    void x(String s) {\n" +
"      try {\n" +
"        outln(new Currency(s).currencyCode());\n" +
"      } catch (Exception e) {\n" +
"        outln('Exception: ' + e);\n" +
"      }\n" +
"    }\n" +
"  </xd:declaration>\n" +
"\n" +
"  <A><B xd:script='*;' a='myEmail();'/></A>\n" +
"\n" +
"  <X><B xd:script='*;' a='currency();'/></X>\n" +
"\n" +
"</xd:def>").createXDDocument();

			xd.xparse("<A><B a='a@b--c'/><B a='a--b@b'/><B a='JOS--NT@SEZN.CZ'/></A>", null);

			xd.setStdOut(swr = new StringWriter());
			xd.xparse("<X><B a='USD'/><B a='CZK'/></X>", null);
			assertTrue("USD\nCZK\n".equals(swr.toString()) && !reporter.errors());
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
