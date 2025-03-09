package mytests;

import org.xdef.XDDocument;
import org.xdef.sys.ArrayReporter;
import test.XDTester;
import static test.XDTester._xdNS;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTestK extends XDTester {
	public MyTestK() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef;
		String xml;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef = // conainer to root, named values with maps
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
" int i = 0;\n"+
" Container c = [[%a = 'a', %b = 'b'], [%a = 'c', %b = 'd']];\n"+
"</xd:declaration>\n"+
"<a xd:script='create c'>\n"+
"  <b xd:script='occurs +;' a='string' b='string'/>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><b a='a' b='b'/><b a='c' b='d'/></a>";
			assertEq(xml, create(compile(xdef), "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // conainer to root, maps is child items
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
" int i = 0;\n"+
" Container c = [[%b=[%a = 'a', %b = 'b']], [%b=[%a = 'c', %b = 'd']]];\n"+
"</xd:declaration>\n"+
"<a xd:script='create c;'>\n"+
"  <b xd:script='occurs +;' a='string' b='string'/>\n"+
"</a>\n"+
"</xd:def>";
			assertEq(xml, create(compile(xdef), "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"<xd:declaration>Container c=[[%a='A',[%b='B','C'],'D']];</xd:declaration>\n" +
"<A xd:script='create c' a='string'><B b='string'>string</B>string</A>\n" +
"</xd:def>";
			assertEq("<A a=\"A\"><B b=\"B\">C</B>D</A>",
				create(compile(xdef), "", "A", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // array with one item array
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' xd:root='A'>\n" +
"  <xd:declaration scope='local'>\n" +
"    Container c = [ [ ['2007-01-01'], ['2007-01-02'], ['abc'] ] ];\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script='create c'>\n" +
"      <C xd:script='occurs 1..*;'> <D>optional date();</D> </C>\n" +
"    </B>\n" +
"  </A>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			assertEq("<A><B><C><D>2007-01-01</D></C></B></A>",
				xd.xcreate("A", reporter));
			assertNoErrorsAndClear(reporter);
			xdef =  // array with two items array
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' xd:root='A'>\n" +
"  <xd:declaration scope='local'>\n" +
"    Container c = [\n" +
"      [ ['2007-01-01'], ['2007-01-02'], ['abc'] ],\n" +
"      [ ['2007-01-03'], ['2007-01-04'], ['def'] ]\n" +
"    ];\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script='create c'>\n" +
"      <C xd:script='occurs 1..*;'> <D>optional date();</D> </C>\n" +
"    </B>\n" +
"  </A>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			assertEq(xd.xcreate("A", reporter),
				"<A><B><C><D>2007-01-01</D></C><C><D>2007-01-03</D></C></B></A>");
		} catch (RuntimeException ex) {fail(ex);}

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
