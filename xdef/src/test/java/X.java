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

		XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <xd:BNFGrammar name='EMAILADDR'>\n" +
"    Atom          ::= atext ('-'+ atext)*\n" +
"    FWS           ::= [ #9]+\n"+ // Folding white spaces
"    ASCIICHAR     ::= [ -~]\n"+ // Printable ASCII characters
"    Domain        ::= sub_domain ( '.' sub_domain )*\n"+
"    sub_domain    ::= Let_dig+ Ldh_str*\n" +
"    Let_dig       ::= [0-9] | $letter\n" +
"    Ldh_str       ::= '-'+ Let_dig+\n"+
"    General_addr  ::= Std_tag ':' ( dcontent )+\n" +
"    Std_tag       ::= Ldh_str\n"+ // Std-tag MUST be specified Standards-Track RFC and registered with IANA
"    dcontent      ::= [!-Z] | [^-~]\n" + // %d33-90 | %d94-126 Printable US-ASCII; excl. [, ', ]
"    IPv4          ::= Snum ('.'  Snum){3}\n"+
"    Snum          ::= ( '2' ([0-4] [0-9] | '5' [0..5]) ) | [0-1] [0-9]{2} | [0-9]{1,2}\n"+
"    IPv6          ::= IPv6_full | IPv6_comp | IPv6v4_full | IPv6v4_comp\n" +
"    IPv6_hex      ::= [0-9a-fA-F]{1,4}\n" +
"    IPv6_full     ::= IPv6_hex ( ':' IPv6_hex ){7}\n" +
"    IPv6_comp     ::= ( IPv6_hex ( ':' IPv6_hex ){0,5} )? '::' ( IPv6_hex (':' IPv6_hex){0,5} )?\n" +
"    IPv6v4_full   ::= IPv6_hex ( ':' IPv6_hex ){5} ':' IPv4_addr\n" +
"    IPv6v4_comp   ::= ( IPv6_hex (':' IPv6_hex){0,3} )? '::'\n" +
"                  ( IPv6_hex (':' IPv6_hex){0,3} ':' )? IPv4_addr\n" +
"    IPv4_addr     ::= IPv4\n"+
"    IPv6_addr     ::= 'IPv6:' IPv6\n"+
"    address       ::= '[' ( IPv4_addr | IPv6_addr | General_addr ) ']'\n" +
"    quoted_pair   ::= '\\' ASCIICHAR\n" + // %d92 %d32-126
"    qtextSMTP     ::= [ !#-Z^-~] | '[' | ']'\n" +
"    QcontentSMTP  ::= quoted_pair | qtextSMTP\n" +
"    Quoted_string ::= '\"' QcontentSMTP* '\"'\n" +
"    atext         ::= ( $letter | ('\\' ('[' | ']' | [\\\"@/ ()&lt;&gt;,;.:]))\n"+
"                  | [0-9_!#$%&amp;'*+/=?^`{|}~] )+\n"+
"    Local_part    ::= Dot_string | Quoted_string\n" + // MAY be case-sensitive
"    Mailbox       ::= Local_part '@' ( address | Domain ) $rule\n"+
"    Dot_string    ::= Atom ('.'  Atom)*\n" +
"    comment       ::= ( commentList $rule ) FWS?\n"+
"    commentList   ::= ( FWS? '(' commentPart* ')' )+\n"+
"    commentPart   ::= ( (ASCIICHAR - [()]) | $letter )+ ( commentList)? $rule\n"+
"    text          ::= ( ( comment* (textItem | comment)* ) | comment* ptext )? comment*\n"+
"    textItem      ::= FWS? '=?' charsetName ( 'Q?' qtext | 'B?' btext ) '?='\n"+
"    charsetName   ::= ( [a-zA-Z] ('-'? [a-zA-Z0-9]+)* ) $rule '?' \n"+
"    ptext         ::= FWS? ( ASCIICHAR - [@&gt;&lt;()=] )+ $rule\n"+ // Printable ASCII character without @><()=
"    qtext         ::= FWS? ( hexOctet | ASCIICHAR - [=?] )+ $rule \n"+ // Quoted text
"    hexOctet      ::= '=' [0-9A-F] [0-9A-F]\n"+
"    btext         ::= [a-zA-Z0-9+/]+ '='? '='? $rule\n"+ // Base64 text
"    emailAddr     ::= ( text? FWS? '&lt;' Mailbox '&gt;' | comment* Mailbox ) comment*\n"+
"  </xd:BNFGrammar>\n" +
"  <xd:declaration> type myEmail EMAILADDR.rule('emailAddr'); </xd:declaration>\n" +
"  <A><B xd:script='*;' a='myEmail();'/></A>\n" +
"</xd:def>").createXDDocument().xparse("<A><B a='a@b--c'/><B a='a--b@b'/><B a='JOS--NT@SEZN.CZ'/></A>", null);

		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;

		xd = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <xd:declaration>\n" +
"    void x(String s) {\n" +
"      try {\n" +
"        outln(new Currency(s).currencyCode());\n" +
"      } catch (Exception e) {\n" +
"        outln('Exception: ' + e);\n" +
"      }\n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <A><B xd:script='*; finally x((String) @a);' a='currency();'/></A>\n" +
"</xd:def>").createXDDocument();
		xd.setStdOut(swr = new StringWriter());
		xd.xparse("<A><B a='USD'/><B a='CZK'/></A>", reporter);
		assertTrue("USD\nCZK\n".equals(swr.toString()) && !reporter.errors(), reporter);
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
