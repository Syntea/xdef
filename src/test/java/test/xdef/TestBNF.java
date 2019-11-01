package test.xdef;

import builtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;

/** Test BNF.*/
public final class TestBNF extends XDTester {

	public TestBNF() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name = 'Example' xd:root = 'root'>\n"+
"  <root> required myType() </root>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration>\n"+
"     type myType $rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"  <xd:BNFGrammar name = \"$base\">\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar name = \"$rrr\" extends = \"$base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<root>123, 456, 789</root>";
			assertEq(xml, parse(xp, "Example", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name = 'Example' xd:root = 'root'>\n"+
"  <root> required myType; </root>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration>\n"+
"     final String myInt = \"intList\"; /* modify as intList or fullName */\n"+
"     BNFRule $r = $rrr.rule(myInt);\n"+
"     type myType $r.check();\n"+
"  </xd:declaration>\n"+
"  <xd:BNFGrammar name = \"$base\">\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar name = \"$rrr\" extends = \"$base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<root>123, 456, 789</root>";
			assertEq(xml, parse(xp, "Example", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= [0-9]+\n"+
"      S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      intList ::= integer (S? \",\" S? integer)*\n"+
"    ');\n"+
"  type intList rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"<a>required intList()</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= [0-9]+\n"+
"      S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      intList ::= integer (S? \",\" S? integer)*\n"+
"    ');\n"+
"  type intList rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"<a>required intList()</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= [0-9]+\n"+
"      S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      intList ::= integer (S? \",\" S? integer)*\n"+
"    ');\n"+
"  type intList rrr.check('intList');\n"+
"  </xd:declaration>\n"+
"<a>required intList()</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { // BNF
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name = 'test' root='a'>\n"+
"  <a>required myType()</a>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      S        ::= $whitespace+ /*skipped white spaces*/\n"+
"      intList  ::= $integer (S? \",\" S? $integer)*\n"+
"      name     ::= $uppercaseLetter $lowercaseLetter+\n"+
"      fullName ::= name S ($uppercaseLetter \".\" S)? name\n"+
"      nameList ::= fullName (S? \",\" S? fullName)*\n"+
"      list     ::= intList | nameList\n"+
"    ');\n"+
"    type myType rrr.check('list');\n"+
//"    type myType{parse: {return BNF(rrr, 'list');}}\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<a>123</a>";
			assertEq(xml, parse(xp, "test", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xp, "test", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a>Arthur C. Clark, Jack London</a>";
			assertEq(xml, parse(xp, "test", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a>Arthur C Clark, Jack London</a>";
			parse(xp, "test", xml, reporter);
			assertErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:root = 'root'>\n"+
"  <root> required myType() </root>\n"+
"</xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration>\n"+
"     String x = \"list\";\n"+
"     BNFGrammar rrr = new BNFGrammar('\n"+
"        integer ::= [0-9]+\n"+
"        S       ::= [9#10#13 ]*\n"+
"        list ::= (integer | fullName) (S \",\" S (integer | fullName))*\n"+
"        name ::= [A-Z] [a-z]+\n"+
"        fullName ::= name S ([A-Z] \".\"){0,2} S name ');\n"+
"     type myType rrr.rule(x);\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<root>4, 5, Arhur C. Klark, Jan Novak, 55</root>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:BNFGrammar name=\"rr\">\n"+
"    M      ::= [#9#10#13 ]*   /*skip white spaces*/\n"+
"    OD     ::= M \",\" M        /*separator of values*/\n"+
"    LnPrd  ::= [1-9] | [1-4] [0-9]\n"+
"    Prd    ::= \"Y\" | \"(Pojistnik)\" | \"(Provozovatel)\" | \"(Vlastnik)\"\n"+
"               | \"(ZeleneKarta)\" | \"(ZmenaVozidla)\"\n"+
"    Month  ::= [1-9] | [1] [0-2]\n"+
"    Months ::= Month ( OD Month )*\n"+
"    YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n"+
"    MDay   ::= [1-9] | [1-2] [0-9] | [3] [0-1] | \"-1\"\n"+
"    MDays  ::= MDay (OD MDay)*\n"+
"    MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n"+
"    WDay   ::= [0-7] | \"-1\"\n"+
"    WDays  ::= WDay (OD WDay)*\n"+
"    WPrd   ::= LnPrd? \"W\" \"(\" WDays \")\"\n"+
"    TimeH  ::= [0-1] [0-9] | [2] [0-3]\n"+
"    TimeM  ::= [0-5] [0-9]\n"+
"    Time   ::= TimeH \":\" TimeM\n"+
"    Times  ::= Time (OD Time)*\n"+
"    DPrd   ::= LnPrd? \"D\" \"(\" Times \")\"\n"+
"    HPrd   ::= LnPrd \"H\"\n"+
"    MinPrd ::= LnPrd \"Min\"\n"+
"    reccur ::= (MinPrd1 | HPrd1 | DPrd1 | WPrd1 | MPrd1 | YPrd?)?\n"+
"    MinPrd1::= MinPrd (OD (HPrd1 | DPrd1 | WPrd1 | MPrd1 | YPrd))?\n"+
"    HPrd1  ::= HPrd (OD (DPrd1 | WPrd1 | MPrd1 | YPrd))?\n"+
"    DPrd1  ::= DPrd (OD (WPrd1 | MPrd1 | YPrd))?\n"+
"    WPrd1  ::= WPrd (OD (MPrd1 | YPrd))?\n"+
"    MPrd1  ::= MPrd (OD YPrd)?\n"+
"  </xd:BNFGrammar>\n"+
"  <a> \n"+
"    <y xd:script=\"+\" a=\"rr.rule('reccur')\">? rr.rule('reccur')</y>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a>\n"+
"  <y a=''> </y>\n"+
"  <y a=' 2H '> 2H </y>\n"+
"  <y a='D(09:00)'>D(09:00)</y>\n"+
"  <y a='D(10:00),W(1,2,3,4,5,6)'>D(10:00),W(1,2,3,4,5,6)</y>\n"+
"  <y a='D(14:45,20:00),W(1,2,3,4,5)'>D(14:45,20:00),W(1,2,3,4,5)</y>\n"+
"  <y a='D(11:00),W(1)'>D(11:00),W(1)</y>\n"+
"  <y a='1W(2)'>1W(2)</y>\n"+
"  <y a='D(12:00),W(-1)'>D(12:00),W(-1)</y>\n"+
"  <y a='D(23:59),M(-1)'>D(23:59),M(-1)</y>\n"+
"  <y a='D(07:00),M(20)'>D(07:00),M(20)</y>\n"+
"  <y a='49Min'>49Min</y>\n"+
"  <y a='4Min'>4Min</y>\n"+
"  <y a='M(3)'>M(9)</y>\n"+
"  <y a='M(1)'>M(31)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml =
"<a>\n"+
"  <y a='M(32)'>M(0)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings() && reporter.getErrorCount()==2);
			//BNF - final variable
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"  final BNFGrammar rr = new BNFGrammar('\n"+
"    M      ::= [#9#10#13 ]*  /*skip white spaces*/\n"+
"    OD     ::= M \",\" M     /*separator of values*/\n"+
"    LnPrd  ::= [1-9] | [1-4] [0-9]\n"+
"    Prd    ::= \"Y\" | \"(Pojistnik)\" | \"(Provozovatel)\" | \"(Vlastnik)\"\n"+
"               | \"(ZeleneKarta)\" | \"(ZmenaVozidla)\"\n"+
"    Month  ::= [1-9] | [1] [0-2]\n"+
"    Months ::= Month ( OD Month )*\n"+
"    YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n"+
"    MDay   ::= [1-9] | [1-2] [0-9] | [3] [0-1] | \"-1\"\n"+
"    MDays  ::= MDay (OD MDay)*\n"+
"    MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n"+
"    WDay   ::= [0-7] | \"-1\"\n"+
"    WDays  ::= WDay (OD WDay)*\n"+
"    WPrd   ::= LnPrd? \"W\" \"(\" WDays \")\"\n"+
"    TimeH  ::= [0-1] [0-9] | [2] [0-3]\n"+
"    TimeM  ::= [0-5] [0-9]\n"+
"    Time   ::= TimeH \":\" TimeM\n"+
"    Times  ::= Time (OD Time)*\n"+
"    DPrd   ::= LnPrd? \"D\" \"(\" Times \")\"\n"+
"    HPrd   ::= LnPrd \"H\"\n"+
"    MinPrd ::= LnPrd \"Min\"\n"+
"    reccur ::= MinPrd? HPrd? DPrd? WPrd? MPrd? YPrd?');\n"+
"  </xd:declaration>\n"+
"  <a> \n"+
"    <y xd:script='+'>? rr.rule('reccur');</y>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a>\n"+
"  <y></y>\n"+
"  <y>2H</y>\n"+
"  <y>D(09:00)</y>\n"+
"  <y>D(10:00)W(1,2,3,4,5,6)</y>\n"+
"  <y>D(14:45,20:00)W(1,2,3,4,5)</y>\n"+
"  <y>D(11:00)W(1)</y>\n"+
"  <y>1W(2)</y>\n"+
"  <y>D(12:00)W(-1)</y>\n"+
"  <y>D(23:59)M(-1)</y>\n"+
"  <y>D(07:00)M(20)</y>\n"+
"  <y>49Min</y>\n"+
"  <y>4Min</y>\n"+
"  <y>M(1)</y>\n"+
"  <y>M(9)</y>\n"+
"  <y>M(31)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml =
"<a>\n"+
"  <y>M(32)</y>\n"+
"  <y>M(0)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings() && reporter.getErrorCount()==2);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"  final BNFGrammar rr = new BNFGrammar('\n"+
"    M      ::= [#9#10#13 ]*   /*skip white spaces*/\n"+
"    OD     ::= M \",\" M        /*separator of values*/\n"+
"    LnPrd  ::= [1-9] | [1-4] [0-9]\n"+
"    Prd    ::= \"Y\" | \"(Pojistnik)\" | \"(Provozovatel)\" | \"(Vlastnik)\"\n"+
"               | \"(ZeleneKarta)\" | \"(ZmenaVozidla)\"\n"+
"    Month  ::= [1-9] | [1] [0-2]\n"+
"    Months ::= Month ( OD Month )*\n"+
"    YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n"+
"    MDay   ::= [1-9] | [1-2] [0-9] | [3] [0-1] | \"-1\"\n"+
"    MDays  ::= MDay (OD MDay)*\n"+
"    MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n"+
"    WDay   ::= [0-7] | \"-1\"\n"+
"    WDays  ::= WDay (OD WDay)*\n"+
"    WPrd   ::= LnPrd? \"W\" \"(\" WDays \")\"\n"+
"    TimeH  ::= [0-1] [0-9] | [2] [0-3]\n"+
"    TimeM  ::= [0-5] [0-9]\n"+
"    Time   ::= TimeH \":\" TimeM\n"+
"    Times  ::= Time (OD Time)*\n"+
"    DPrd   ::= LnPrd? \"D\" \"(\" Times \")\"\n"+
"    HPrd   ::= LnPrd \"H\"\n"+
"    MinPrd ::= LnPrd \"Min\"\n"+
"    reccur ::= MinPrd? HPrd? DPrd? WPrd? MPrd? YPrd?');\n"+
"  </xd:declaration>\n"+
"  <a> \n"+
"    <y xd:script='+'>? BNF(rr, 'reccur');</y>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a>\n"+
"  <y></y>\n"+
"  <y>2H</y>\n"+
"  <y>D(09:00)</y>\n"+
"  <y>D(10:00)W(1,2,3,4,5,6)</y>\n"+
"  <y>D(14:45,20:00)W(1,2,3,4,5)</y>\n"+
"  <y>D(11:00)W(1)</y>\n"+
"  <y>1W(2)</y>\n"+
"  <y>D(12:00)W(-1)</y>\n"+
"  <y>D(23:59)M(-1)</y>\n"+
"  <y>D(07:00)M(20)</y>\n"+
"  <y>49Min</y>\n"+
"  <y>4Min</y>\n"+
"  <y>M(1)</y>\n"+
"  <y>M(9)</y>\n"+
"  <y>M(31)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml =
"<a>\n"+
"  <y>M(32)</y>\n"+
"  <y>M(0)</y>\n"+
"</a>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings() && reporter.getErrorCount()==2);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}
