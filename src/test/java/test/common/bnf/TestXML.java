package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import java.io.File;
import builtools.STester;

/** Simple XML grammar test.
 * @author Vaclav Trojan
 */
public class TestXML extends STester {

	public TestXML() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			if (grammar.parse(new StringParser(source), name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (grammar.getParser().eos()?
					"eos" : grammar.getParser().getPosition().toString()) +"; ";
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
			String s;

////////////////////////////////////////////////////////////////////////////////
			grammar = BNFGrammar.compile(null,
				new File(getDataDir() + "TestXML.bnf"), null);
//			grammar.display(System.out, true);

////////////////////////////////////////////////////////////////////////////////
			s = "<!--c-o-m-m-e-n-t-->";
			assertEq(s, parse(grammar, "Comment", s));
			s = "xx]] >";
			assertEq(s, parse(grammar, "CData", s));
			s = "<![CDATA[x]<x>]] >x]]>";
			assertEq(s, parse(grammar, "CDSect", s));
			s = "<A/>";
			assertEq(s, parse(grammar, "document", s));
			s = "<A></A>";
			assertEq(s, parse(grammar, "document", s));
			s = "<A>test<B/></A>";
			assertEq(s, parse(grammar, "document", s));
			s = "<A x=\"x\"  \n y = 'y' >test<B  /></A>";
			assertEq(s, parse(grammar, "document", s));
			s =
"<?xml version = \"1.0\" encoding='UTF-8'?>\n"+
"<!-- comment -->\n"+
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/2.0' root='Misto|Udaj|Base'>\n"+
"<xd:declaration>\n"+
"<![CDATA[\n"+
"external Element base; /*V tomto elementu je nase databaze. */\n"+
"\n"+
"/*Pridame do databaze novy element “Misto”pokud jeste neexistuje*/\n"+
"void addMisto(String misto) {\n"+
"  if (xpath('Misto[@name=\"' + misto + '\"]',  base).getLength() == 0) {\n"+
"    Element e = new Element('Misto');\n"+
"    e.setAttribute('name', misto);\n"+
"    base.addElement(e);\n"+
"  } else {\n"+
"     outln('Misto ' + misto + ' jiz bylo definovane!');\n"+
"  }\n"+
"}\n"+
"\n"+
"/*Pridame do databaze do prislusneho elementu Misto novy element “Udaj”.\n"+
"   Pokud misto neexistuje nebo udaj jiz existuje , ohlasime chybu.*/\n"+
"void addUdaj(String misto, String od) {\n"+
"   Element e = getElement();\n"+
"   Element e1 = xpath('Misto[@name=\"' + misto + '\"]',base).getElement(0);\n"+
"   if (e1 == null) {\n"+
"     outln('Misto ' + misto + ' neni definovane!'); return;\n"+
"   }\n"+
"   if (xpath('Udaj[@od=\"' + od + '\"]', e1).getLength() != 0) {\n"+
"     outln('Udaj ' + misto + '/' + od + ' jiz existuje!'); return;\n"+
"   }\n"+
"   Element e2 = new Element('Udaj');\n"+
"   e2.setAttribute('hodnota', e.getAttribute('hodnota'));\n"+
"   e2.setAttribute('od', e.getAttribute('od'));\n"+
"   e2.setAttribute('do', e.getAttribute('do'));\n"+
"   e1.addElement(e2);\n"+
"}\n"+
"]]>\n"+
"</xd:declaration>\n"+
"\n"+
"<Misto xd:script='finally addMisto((String) @name)' name='string'/>\n"+
"<Udaj xd:script='finally addUdaj((String) @misto, (String) @od)'\n"+
"   misto='string'\n"+
"   hodnota='xs:float'\n"+
"   od='xs:dateTime'\n"+
"   do='xs:dateTime'/>\n"+
"\n"+
"<Base>\n"+
"  <Misto xd:script='*' name='string'>\n"+
"    <Udaj xd:script='*'\n"+
"      hodnota='xs:float'\n"+
"      od='xs:dateTime'\n"+
"      do='xs:dateTime'/>\n"+
"  </Misto>\n"+
"</Base>\n"+
"</xd:def>";
			assertEq(s, parse(grammar, "document", s));
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
