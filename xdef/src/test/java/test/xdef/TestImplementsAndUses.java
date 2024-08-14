package test.xdef;

import test.XDTester;
import java.io.StringWriter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;

/** Test "implements" and "uses".
 * @author Vaclav Trojan
 */
public final class TestImplementsAndUses extends XDTester {

	public TestImplementsAndUses() {super();}

	final public static boolean x() {return true;}
	final public static boolean y() {return true;}

	@Override
	/** Run test and print error information.*/
	public void test() {
		String xdef;
		String xml;
		String s;
		XDPool xp;
		XDDocument xd;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
		boolean	chkSyntax = getChkSyntax();
		setChkSyntax(false);
// test error reporting
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <B a='int(1,2);'/>\n"+
"  <A xd:script=\"implements B\" a='float'></A>\n"+
"</xd:def>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !s.contains("XDEF289") ||
				!s.contains("XDEF285") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <B a='int(1,2);'/>\n"+
"  <A xd:script=\"implements B\" a='xxx'></A>\n"+ // this is error
"</xd:def>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XDEF229")) { // comparing skipped
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='b'>\n"+
"  <A a=\"fixed 'a'\"/>\n"+
"</xd:def>\n"+
"<xd:def name='a'>\n"+
"  <A xd:script=\"implements b#A\" a=\"fixed 'b'\"></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF286") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration>\n"+
"    external method {\n"+
"      boolean test.xdef.TestImplementsAndUses.x();\n"+
"      boolean test.xdef.TestImplementsAndUses.y();\n"+
"     }\n"+
"  </xd:declaration>\n"+
"<xd:def name='X'>\n"+
"  <A a='x();'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='y(); finally outln()'/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF285") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration>\n"+
"    external method {\n"+
"      boolean test.xdef.TestImplementsAndUses.x();\n"+
"      boolean test.xdef.TestImplementsAndUses.y();\n"+
"    }\n"+
"  </xd:declaration>\n"+
"<xd:def name='X'>\n"+
"  <A>x();</A>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\">y(); finally outln()</A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XDEF285") < 0 || s.indexOf("XDEF282") < 0) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <A a='int(1,2);'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script='implements #A' a='float'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XDEF285") < 0 || s.indexOf("XDEF282") < 0) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <xd:declaration scope='global'>\n"+
"    boolean a(int i) {return true}\n"+
"  </xd:declaration>\n"+
"  <A a='a(1)'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='a(2)'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XDEF285") < 0 || s.indexOf("XDEF282") < 0) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A a='int(1,2);'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='int(1,2); finally outln()'><B/></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF283") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try { //check "implements" XDefinifion
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A a='int(1,2);'><B/></A>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='int(1,2); finally outln()'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF283") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A a='int(1,2);'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='int(1,3); finally outln()'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF285") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='b'>\n"+
"  <A a=\"int; default 'a'\"/>\n"+
"</xd:def>\n"+
"<xd:def name='a'>\n"+
"  <A xd:script=\"implements b#A\" a=\"int; default 'b'\"></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF286") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
		try {
			xdef = //this is a question
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <xd:declaration scope='global'>\n"+
"    boolean a() {return true}\n"+
"    boolean b() {return true}\n"+
"  </xd:declaration>\n"+
"  <A a='a()'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='b()'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			fail("error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (!s.contains("XDEF285") || !s.contains("XDEF282")) {
				fail(ex);
			}
		}
// test no errors
		setChkSyntax(chkSyntax);
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='b'>\n"+
"  <A a=\"int; default '123'\"/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements b#A\" a=\"int; default '123'\"></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='1'/>";
			assertEq(xml, parse(xp, "Y", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<A a='123'/>",parse(xp, "Y", "<A/>", reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A a='x'/>";
			assertEq(xml, parse(xp, "Y", xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='b'>\n"+
"  <A a=\"fixed 'a'\"/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements b#A\" a=\"fixed 'a'\"></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='a'/>";
			assertEq(xml, parse(xp, "Y", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, parse(xp, "Y", "<A/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, parse(xp, "Y", "<A a='x'/>", reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A a='int(1,2);'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"implements X#A\" a='int(1,2); finally outln()'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <xd:declaration scope='global'>\n"+
 "   boolean a(int i) {return true;}\n"+
"  </xd:declaration>\n"+
"  <A a='a(1)'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script='implements #A' a='a(01)'></A>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A xd:script='ref X' a='int(1,2); onTrue outln()'/>\n"+
"  <X xd:script=\"create [%a='a','b'].toElement()\" a='enum(1,2)'>\n"+
"     <B/>\n"+
"     enum('A', 'B')\n"+
"     <C/>\n"+
"   </X>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
"  <A xd:script=\"finally outln('x'); implements X#A\"\n"+
"     a='int(1,2)'><B/> enum('A', 'B')  <C/></A>\n"+
"  <X xd:script='ref X#X' a='enum(2,3)'/>\n"+
"  <root>\n"+
"    <A xd:script=\"finally outln('x'); implements X#A\"\n"+
"      a='int(1,2)'><B/> enum('A', 'B') <C/></A>\n"+
"  </root>\n"+
"  <boot xd:script=\"finally outln('y'); uses Y#root; ref root\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='X'>\n"+
"  <A xd:script='ref X' a='int(1,2); onTrue out(1);'/>\n"+
"  <X xd:script=\"create [%a='a','b'].toElement()\"\n"+
"    a='enum(1,2)'> <B/> enum('A', 'B') <C/></X>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A|Z'>\n"+
"  <A xd:script=\"finally out('a'); implements X#A\"\n"+
"    a='int(1,2)'><B/> enum('A', 'B') <C/></A>\n"+
"  <X xd:script='ref X#X' a='enum(2,3)'/>\n"+
"  <root>\n"+
"    <A xd:script=\"finally out('x'); implements X#A\"\n"+
"      a='int(1,2)'> <B/> enum('A', 'B') <C/></A>\n"+
"  </root>\n"+
"  <Y>\n"+
"   <P xd:script=\"*; finally out('y'); uses X#A\"\n"+
"     a='required '> <B/> required <C/></P>\n"+
"  </Y>\n"+
"  <Z xd:script=\"finally out('z'); uses Y#root; ref root\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xd = xp.createXDDocument("Y");
			xml = "<A a='1'><B/>A<C/></A>";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("a", swr.toString());
			xd = xp.createXDDocument("Y");
			xml = "<Z><A a='2'><B/>A<C/></A></Z>";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("xz", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='P'>\n"+
"  <P xd:script='uses A' a='required '><B/>required <C xd:script='+'/></P>\n"+
"  <A a='required int'> <B/> required float <C xd:script='+'/> </A>\n"+
"</xd:def>";
			compile(xdef);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"  <P a='required int'>\n"+
"    <B/>\n"+
"    required float\n"+
"    <C xd:script='+'/>\n"+
"  </P>\n"+
"  <root>\n"+
"    <A xd:script='uses P' a='required'>\n"+
"      <B/>\n"+
"      required\n"+
"      <C xd:script='+'/>\n"+
"    </A>\n"+
"  </root>\n"+
"</xd:def>";
			compile(xdef);
// external method
			setChkSyntax(false); // follows comparing of external method
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
"  external method boolean test.xdef.TestImplementsAndUses.x();\n"+
"</xd:declaration>\n"+
"<xd:def name='X'>\n"+
"  <A a='x();'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y' root='A'>\n"+
  "<A xd:script=\"implements X#A\"\n"+
"    a='x(); finally outln()'/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			setChkSyntax(chkSyntax);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration>\n"+
"    external method boolean test.xdef.TestImplementsAndUses.x();\n"+
"  </xd:declaration>\n"+
  "<B a='x();'/>\n"+
"  <A xd:script=\"uses B; finally outln()\"\n"+
"     a='required; finally outln()'/>\n"+
"</xd:def>";
			compile(xdef);
//REGEX
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='A'>\n"+
"  <X a=\"required string(%pattern='[A-Z]');\" />\n"+
"</xd:def>\n"+
"<xd:def xd:name='B' xd:root='X'>\n"+
"  <X xd:script='uses A#X' a=\"required string(%pattern='[A-Z]')\" />\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <X xd:script='uses Y' a=\"required string(%pattern='[A-Z]')\" />\n"+
"  <Y a=\"required string(%pattern='[A-Z]');\" />\n"+
"</xd:def>";
			compile(xdef);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <X xd:script='uses Y' a=\"required\" />\n"+
"  <Y a=\"required string(%pattern=' [A-Z]\n ');\" />\n"+
"</xd:def>";
			compile(xdef);
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def xd:name = \"A\" > \n"+
"  <Firma xd:script = \"implements B#Firma\" \n"+
"     ulice = \"required string(1,30)\"\n"+
"     cp = \"optional int()\"\n"+
"     mesto = \"required string(1,30)\"\n"+
"     PSC = \"optional num(5)\"\n"+
"     stat = \"required string(2)\">\n"+
"	<Nazev> required string(1,30) </Nazev>\n"+
"  </Firma>\n"+
"</xd:def>\n"+
"<xd:def xd:name = \"B\" >\n"+
"  <Firma xd:script = \"ref Adresa \">\n"+
"	<Nazev> required string(1,30) </Nazev>\n"+
"  </Firma>\n"+
"  <Adresa ulice = \"required string(1,30)\"\n"+
"          cp = \"optional int()\"\n"+
"          mesto = \"required string(1,30)\"\n"+
"          PSC = \"optional num(5)\"\n"+
"          stat = \"required string(2)\"\n"+
"  />\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "' >\n"+
"<xd:def xd:name = \"A\" > \n"+
"  <Firma xd:script = \"uses B#Object\" \n"+
"     ulice = \"required\"\n"+
"     cp = \"optional\"\n"+
"     mesto = \"required\"\n"+
"     PSC = \"optional\"\n"+
"     stat = \"required\">\n"+
"	<Nazev> required </Nazev>\n"+
"  </Firma>\n"+
"</xd:def>\n"+
"<xd:def xd:name = \"B\" >\n"+
"  <Object xd:script = \"ref Adresa \">\n"+
"	<Nazev> required string(1,30) </Nazev>\n"+
"  </Object>\n"+
"  <Adresa ulice = \"required string(1,30)\"\n"+
"          cp = \"optional int()\"\n"+
"          mesto = \"required string(1,30)\"\n"+
"          PSC = \"optional num(5)\"\n"+
"          stat = \"required string(2)\"\n"+
"  />\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
//BNF
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <B a=\"x.parse('X');\"/>\n"+
"  <A xd:script=\"uses B\"\n"+
"     a=\"x.parse('X')\"/>\n"+
"  <xd:BNFGrammar name = 'x'>\n"+
"    X::= '/*' ( [^*]+ | '*' [^/] )* '*/'\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
		} catch (Exception ex) {fail(ex);}
		try {
			String xdef1 =
"<xd:def  xmlns:xd='"+_xdNS+"' xmlns:s='a.b' name='A' root='s:A'>\n" +
"  <s:A><B/><C/></s:A>\n" +
"</xd:def>";
			String xdef2 =
"<xd:def  xmlns:xd='"+_xdNS+"' xmlns:s='a.b' name='B' root='s:A'>\n" +
"  <s:A xd:script='implements A#s:A'><B/><C/></s:A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, new String[]{xdef1, xdef2});
		} catch (Exception ex) {fail(ex);}
		try {
			String xdef1 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:s='a.b' name='A' root='s:A'>\n" +
"  <s:A><B/><C/></s:A>\n" +
"</xd:def>";
			String xdef2 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:t='a.b' name='B' root='t:A'>\n" +
"  <t:A xd:script='implements A#t:A'><B/><C/></t:A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, new String[]{xdef1, xdef2});
		} catch (Exception ex) {fail(ex);}
		try {
			String xdef1 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:s='a.b' name='A' root='s:A'>\n" +
"  <s:A><B/><C/></s:A>\n" +
"</xd:def>";
			String xdef2 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:s='b.c' name='B' root='s:A'>\n" +
"  <s:A xd:script='implements A#s:A'><B/><C/></s:A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, new String[]{xdef1, xdef2});
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("XDEF122") < 0) { // comparing skipped
				fail(ex);
			}
		}
		try {
			String xdef1 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:s='a.b' name='A' root='s:A'>\n" +
"  <s:A><B/><C/></s:A>\n" +
"</xd:def>";
			String xdef2 =
"<xd:def xmlns:xd='"+_xdNS+"' xmlns:s='b.c' name='B' root='s:A'\n" +
"        xmlns:t='a.b'>\n" +
"  <s:A xd:script='implements A#t:A'><B/><C/></s:A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, new String[]{xdef1, xdef2});
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("XDEF122") < 0) { // comparing skipped
				fail(ex);
			}
		}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}