package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.io.StringWriter;
import org.w3c.dom.Element;

/** Test of groups (repeated items, nesting).
 * @author Vaclav Trojan
 */
public final class TestGroups extends XDTester {

	public TestGroups() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xdef;
		String xml;
		String s;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		XDPool xp;
		XDDocument xd;
		StringWriter strw;
		String dataDir = getDataDir() + "test/";
////////////////////////////////////////////////////////////////////////////////
// sequence
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef = // Test choice in sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><p/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><q/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertErrors(reporter);
			xdef = // create sequence
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"<a>\n"+
"    <xd:sequence script=\"*; create from('//b/d/e'); ref s\"/>\n"+
"    <c/>\n"+
"    <xd:sequence script=\"*; create from('//b/d/e'); ref s\"/>\n"+
"</a>\n"+
"  <xd:sequence name='s'>\n"+
"	 create from('./text()');\n"+
"    <b/>\n"+
"   </xd:sequence>\n"+
"</xd:def>";
			xml =
"<a>\n"+
"  <b>\n"+
"    <c>1</c>\n"+
"    <d><e>3</e><e>4</e><e>5</e><e>6</e></d>\n"+
"  </b>\n"+
"  <b>\n"+
"    <c>2</c>\n"+
"    <d><e>7</e></d>\n"+
"  </b>\n"+
"</a>";
			xp = compile(xdef);
			el = create(xp, "a", "a", reporter, xml);
			s = "<a>3<b/>4<b/>5<b/>6<b/>7<b/><c/>3<b/>4<b/>5<b/>6<b/>7<b/></a>";
			assertEq(s, el);
			el = parse(xp, "a", s, reporter);
			assertNoErrors(reporter);
			assertEq(s, el);
			xdef = // test text in repeated sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script='8'>\n"+
"    <xd:choice>\n"+
"      <b />\n"+
"      <c />\n"+
"      int\n"+
"    </xd:choice>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b/><c/>1<b/><c/>2</a>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xdef = // Test mixed in sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><p/><q/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><p/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			//nemělo by hlasit dvakrat
			assertTrue(reporter.printToString().contains("XDEF520"),
				reporter.printToString());
			parse(xp, null, "<a><p/><p/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><q/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><p/><p/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			xdef = // Test sequence in sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><p/><q/><b/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/><p/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><p/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><q/><p/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/><p/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration> int i = 0; </xd:declaration>\n"+
"  <A xd:script='create true'>\n"+
"    <xd:sequence xd:script='5; create 5; finally out(i)'> \n"+
"     <B xd:script='?; create i++ == 0'/>\n"+
"       ? string; create i++ == 1 ? 'Text' : null;\n"+
"     <C xd:script= \"?; create i++ == 2\" />\n"+
"    </xd:sequence> \n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B/>Text<C/></A>";
			strw = new StringWriter();
			el = parse(xp, null, xml, reporter, strw, null, null);
			assertTrue(reporter.errors(),"Error not reported");
			assertEq("<A><B/>Text<C/></A>", el);
			assertEq(strw.toString(), "0");
			xml = null;
			strw = new StringWriter();
			el = create(xp, null, "A", reporter, xml, strw, null);
			assertTrue(reporter.errors(),"Error not reported");
			assertEq("<A><B/>Text<C/></A>", el);
			assertEq(strw.toString(), "15");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration> int i = 0; </xd:declaration>\n"+
"  <A xd:script='create true'>\n"+
"    <xd:sequence xd:script='*; create 2; finally i++'> \n"+
"      <xd:choice xd:script='2;'> \n"+
"         <B xd:script='create i == 0'/>\n"+
"         <C/>\n"+
"      </xd:choice> \n"+
"    </xd:sequence> \n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<A><B/><C/></A>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<A><B/></A>", reporter);
			assertTrue(reporter.errors());
			el = create(xp, null, "A", reporter, null);
			assertNoErrors(reporter);
			assertEq("<A><B/><B/><C/><C/></A>", el);
		} catch (Exception ex) {fail(ex);}

////////////////////////////////////////////////////////////////////////////////
// mixed
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"       <b t=\"fixed 't'\"/>\n"+
"       <c t=\"fixed 't'\"/>\n"+
"       <d t=\"fixed 't'\"/>\n"+
"       <xd:any xd:script=\"match @t=='t'; occurs 0..\" t=\"fixed 't'\"/>\n"+
"    </xd:mixed>\n"+
" </a>\n"+
"</xd:def>";
			xml =
"<a><x t='t'/><y t='t'/><d t='t'/><c t='t'/><z t='t'/><b t='t'/></a>";
			parse(xdef, null, xml, reporter);
			assertNoErrors(reporter);
			xdef = //test any in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:mixed script='optional'>\n"+
"    <b/>\n"+
"    <c/>\n"+
"    <xd:any xd:script='options moreAttributes,moreElements,moreText'/>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b/><c/><x/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef = //test empty blok and repeated items
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:mixed script='optional'>\n"+
"    <c xd:script='1..3;'/>\n"+
"    <d xd:script='1..3;'/>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a/>";
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><d/><c/><c/><d/><d/><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><d/><c/><c/><d/><d/><c/><d/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings(), "Error not recognozed");
			xdef = // text in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"      <xd:mixed>\n"+
"        <b />\n"+
"        <c />\n"+
"        int\n"+
"      </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b/>1<c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b/>1<c/>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			xdef = // mixed in repeated sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script='2'>\n"+
"      <xd:mixed>\n"+
"        <b />\n"+
"        <c />\n"+
"        int\n"+
"      </xd:mixed>\n"+
"      <d/>\n"+
"    </xd:sequence>\n"+
"    <e/>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b/><c/>1<d/><b/><c/>2<d/><e/></a>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(el, xml);
			xdef = // Test choice in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertErrors(reporter);
			xdef = // Test sequence in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef = // Test mixed in mixed
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <b/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/><c/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><p/><q/><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><c/><p/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><p/><b/><q/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd ='" + _xdNS + "' root='A'>\n"+
"<xd:declaration> int i = 0; </xd:declaration>\n"+
"  <A xd:script= 'create true'>\n"+
"    <xd:mixed xd:script='create 5; finally out(i)'> \n"+
"     <B xd:script='?; create i++ == 0'/>\n"+
"       ? string; create i++ == 1 ? 'Text' : null;\n"+
"     <C xd:script='?; create i++ == 2'/>\n"+
"    </xd:mixed> \n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B/>Text<C/></A>";
			strw = new StringWriter();
			assertEq("<A><B/>Text<C/></A>",
				parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "0");
			xml = null;
			strw = new StringWriter();
			assertEq("<A><B/>Text<C/></A>",
				create(xp, null, "A", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "3");
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"<A xd:script= \"create 1\">\n" +
"  <xd:mixed xd:script= \"create 5\"> \n" +
"    <B xd:script= \"occurs *; create 2\" />\n" +
"      required string; create \"Text\";\n" +
"    <C xd:script= \"create 1\" />\n" +
"    </xd:mixed> \n" +
"</A>\n" +
"</xd:def>");
			assertEq("<A><B/><B/>Text<C/></A>", create(xp, "", "A", reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

////////////////////////////////////////////////////////////////////////////////
// choice
////////////////////////////////////////////////////////////////////////////////
		try {// Test any in choice
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"       <xd:any xd:script=\"options moreAttributes,moreElements,moreText\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><x/></a>", reporter);
			assertNoErrors(reporter);
			// Test choice in choice
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:choice>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:choice>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef = // Test sequence in choice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:sequence>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:sequence>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
// Test m mixed in choice
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"      <c/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:mixed>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/><p/><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a><q/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			xdef = //check missing contents in choice
"<xdef:def xmlns:xdef='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b/>\n"+
"    <xdef:choice>\n"+
"      <c xdef:script= \"onAbsence out('4281')\"/>\n"+
"      <d xdef:script= \"onAbsence out('4281')\"/>\n"+
"    </xdef:choice>\n"+
"    <e/>\n"+
"  </a>\n"+
"</xdef:def>";
			xp = compile(xdef);
			parse(xp, null, "<a><b/></a>", reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				rep = reporter.getReport();
				if (!"XDEF555".equals(rep.getMsgID())) {
					fail(rep);
					if ((rep = reporter.getReport()) == null) {
						fail();
						if ((reporter.getReport()) != null) {
							fail();
						}
					} else if (!"XDEF539".equals(rep.getMsgID())) {
						fail(rep);
					}
				}
			}
			parse(xp, null, "<a><b/><e/></a>", reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				rep = reporter.getReport();
				if (!"XDEF555".equals(rep.getMsgID())) {
					fail(rep);
					if ((reporter.getReport()) != null) {
						fail();
					}
				}
			}
			xdef =
"<xdef:def xmlns:xdef='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b/>\n"+
"    <xdef:choice>\n"+
"       <c xdef:script=\"onAbsence out('4281')\"/>\n"+
"       <d xdef:script=\"onAbsence out('4281')\"/>\n"+
"    </xdef:choice>\n"+
"  </a>\n"+
"</xdef:def>";
			parse(xdef, null, "<a><b/></a>", reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				rep = reporter.getReport();
				if (!"XDEF555".equals(rep.getMsgID())) {
					fail(rep);
					if ((reporter.getReport()) != null) {
						fail();
					}
				}
			}
//test choice
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <a/>\n"+
"      <b xd:script='occurs 3'/>\n"+
"      optional string()\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a>\n<a/>\n</a>", reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			parse(xp, null, "<a>\n<b/><b/><b/>\n</a>", reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			parse(xp, null, "<a>\ntext\n</a>", reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			parse(xp, null, "<a/>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a>\n<a/><a/>\n</a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a>\n<b/><b/><b/><b/>\n</a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a>\n<b/><b/>\n</a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a>\n<b/>\n</a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration> int i = 0; </xd:declaration>\n"+
"  <A xd:script='create true'>\n"+
"    <xd:choice xd:script= \"5; create 5; finally out(i)\"> \n"+
"     <B xd:script='create i++ == 0'/>\n"+
"       string; create i++ == 2 ? 'Text' : null;\n"+
"     <C xd:script='create i++ == 5'/>\n"+
"    </xd:choice> \n"+
"  </A>\n"+
"</xd:def>";
			xml = "<A><B/>Text<C/></A>";
			strw = new StringWriter();
			assertEq(xml, parse(xdef, null, xml, reporter, strw, null, null));
			assertTrue(reporter.errors(),"Error not reported");
			assertEq(strw.toString(), "0");
			strw = new StringWriter();
			// null input XML!!!
			assertEq(xml, create(xdef, null, "A", reporter, null, strw, null));
			assertTrue(reporter.errors(),"Error not reported");
			assertEq(strw.toString(), "12");
			xdef = //reference to other XDefinition
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='A'>\n"+
"  <xd:mixed name='m'><b x='string'/><c y='string'/></xd:mixed>\n"+
"</xd:def>\n"+
"<xd:def name='B' root='a'>\n"+
"  <a><xd:mixed xd:script='ref A#m'/></a>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument("B");
			xml = "<a><c y='y'/><b x='x'/></a>\n";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xdef = //reference to other XDefinition
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name='B' root='a'>\n"+
"  <a><xd:mixed xd:ref='A#m'/></a>\n"+
"</xd:def>\n"+
"<xd:def name='A'>\n"+
"  <xd:mixed xd:name='m'><b x='string'/><c y='string'/></xd:mixed>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument("B");
			xml = "<a><c y='y'/><b x='x'/></a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:choice>\n"+
"    <xd:choice ref='d1' />\n"+
"    <d/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"<xd:choice name='d1'>\n"+
"  <b/>\n"+
"  <c/>\n"+
"</xd:choice>\n"+
"</xd:def>";
			xml = "<a><b/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xml = "<a><c/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xml = "<a><d/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:sequence ref='d1' />\n"+
"  <d/>\n"+
"</a>\n"+
"\n"+
"<xd:sequence name='d1'>\n"+
"  <b/>\n"+
"  <c/>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			xml = "<a><b/><c/><d/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n"+
"<a>\n"+
"  <d/>\n"+
"  <xd:sequence ref='d1'/>\n"+
"</a>\n"+
"<xd:sequence name='d1'>\n"+
"  optional string()\n"+
"  <b xd:script='occurs ?'/>\n"+
"  <c xd:script='occurs ?'/>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			xml = "<a><d/>text</a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x:mixed>\n"+
"     <x:sequence ref='n'/>\n"+
"  </x:mixed>\n"+
"</a>\n"+
"<x:sequence name='n'>\n"+
"  <b x:script='occurs *'/>\n"+
"  <c x:script='occurs *'/>\n"+
"  <d x:script='occurs *'/>\n"+
"</x:sequence>\n"+
"</x:def>\n";
			xml = "<a><c/><c/><b/><c/><c/><b/><b/><b/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x:choice ref='n'/>\n"+
"</a>\n"+
"<x:choice name='n'>\n"+
"  <b x:script='occurs +'/>\n"+
"  <c x:script='occurs +'/>\n"+
"  <d x:script='occurs +'/>\n"+
"</x:choice>\n"+
"</x:def>\n";
			xml = "<a><c/><c/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x/>\n"+
"  <x:choice ref='n'/>\n"+
"  <y/>\n"+
"</a>\n"+
"\n"+
"<x:choice name='n'>\n"+
"  <b x:script='occurs +'/>\n"+
"  <c x:script='occurs +'/>\n"+
"  <d x:script='occurs +'/>\n"+
"</x:choice>\n"+
"\n"+
"</x:def>\n";
			xml = "<a><x/><c/><c/><y/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x/>\n"+
"  <x:mixed ref='n'/>\n"+
"  <y/>\n"+
"</a>\n"+
"\n"+
"<x:mixed name='n'>\n"+
"  <b x:script='occurs 0..1'/>\n"+
"  <c x:script='occurs 0..2'/>\n"+
"  <d x:script='occurs 0..1'/>\n"+
"</x:mixed>\n"+
"\n"+
"</x:def>\n";
			xml = "<a><x/><d/><c/><c/><b/><y/></a>";
			assertFalse(test(xdef, xml, null, 'P',xml, ""));
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x/>\n"+
"  <x:choice ref='n' occurs='+'/>\n"+
"  <y/>\n"+
"</a>\n"+
"\n"+
"<x:choice name='n'>\n"+
"  <b x:script='occurs 0..1'/>\n"+
"  <c x:script='occurs 0..1'/>\n"+
"  <d x:script='occurs 0..1'/>\n"+
"</x:choice>\n"+
"\n"+
"</x:def>\n";
			xml = "<a><x/><d/><y/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			parse(xdef, null, "<a><x/><y/></a>", reporter);
			if (reporter.errors()) {
				assertEq("XDEF555", reporter.getReport().getMsgID());
			} else {
				fail("Error not reported");
			}
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x/>\n"+
"  <x:choice ref='n'/>\n"+
"  <y/>\n"+
"</a>\n"+
"\n"+
"<x:choice name='n'>\n"+
"  <b x:script='occurs 0..2'/>\n"+
"  <c x:script='occurs 0..2'/>\n"+
"  <d x:script='occurs 0..2'/>\n"+
"</x:choice>\n"+
"\n"+
"</x:def>\n";
			xml = "<a><x/><b/><b/><y/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xml = "<a><x/><b/><y/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			parse(xdef, null, "<a><x/><y/></a>", reporter);
			if (reporter.errors()) {
				assertEq("XDEF555", reporter.getReport().getMsgID());
			} else {
				fail("Error not reported");
			}
			xdef = //test recursive reference
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:choice ref='command'/>\n"+
"</a>\n"+
"<xd:choice name='command'>\n"+
"  <if>\n"+
"    <c/>\n"+
"    <xd:choice ref='command'/>\n"+
"    <else xd:script='occurs ?'>\n"+
"      <xd:choice ref='command'/>\n"+
"    </else>\n"+
"  </if>\n"+
"  <assgn/>\n"+
"</xd:choice>\n"+
"</xd:def>";
			xml = "<a><assgn/></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xml = "<a><if><c/><assgn/></if></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			xml = "<a><if><c/><assgn/><else><if>" +
				"<c/><assgn/></if></else></if></a>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
//test error if first group is not prezent
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:sequence><b/><c/></xd:sequence>\n"+
"  <x/>\n"+
"</a>\n"+
"</xd:def>";
			parse(xdef, "", "<a><x/></a>", reporter);
			assertTrue(reporter.errorWarnings(),"errors not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:choice><b/><c/></xd:choice>\n"+
"  <x/>\n"+
"</a>\n"+
"</xd:def>";
			parse(xdef, "", "<a><x/></a>", reporter);
			assertTrue(reporter.errorWarnings(),"errors not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:mixed><b/><c/></xd:mixed>\n"+
"  <x/>\n"+
"</a>\n"+
"</xd:def>";
			parse(xdef, "", "<a><x/></a>", reporter);
			assertTrue(reporter.errorWarnings(),"errors not recognized");
// group is not required and items of the group are missing
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"    <a>\n"+
"        <xd:choice xd:script='?'>\n"+
"            <d xd:script='1; create from(\"/a/d\")'/>\n"+
"        </xd:choice>\n"+
"    </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, null, xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, null, "a", reporter, xml, null, null));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"    <a>\n"+
"        <xd:mixed xd:script='?'>\n"+
"            <d xd:script='1; create from(\"/a/d\")'/>\n"+
"        </xd:mixed>\n"+
"    </a>\n"+
"</xd:def>";
			xml = "<a/>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			assertFalse(test(xdef, xml, "",'C', xml, ""));
//test empty optional groups
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a>\n"+
"      <xd:sequence xd:script=\"optional\">\n"+
"         <b xd:script='required; create from(\"/a/b\")'/>\n"+
"         <c xd:script='required; create from(\"/a/c\")'/>\n"+
"         <d xd:script='required; create from(\"/a/d\")'/>\n"+
"      </xd:sequence>\n"+
"   </a>\n"+
"</xd:def>";
			xml = "<a/>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a>\n"+
"      <xd:choice xd:script=\"optional\">\n"+
"         <b xd:script='required; create from(\"/a/b\")'/>\n"+
"         <c xd:script='required; create from(\"/a/c\")'/>\n"+
"         <d xd:script='required; create from(\"/a/d\")'/>\n"+
"      </xd:choice>\n"+
"   </a>\n"+
"</xd:def>";
			xml = "<a/>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			assertFalse(test(xdef, xml, "",'C', xml, ""));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a>\n"+
"      <xd:mixed xd:script=\"optional\">\n"+
"         <b xd:script='required; create from(\"/a/b\")'/>\n"+
"         <c xd:script='required; create from(\"/a/c\")'/>\n"+
"         <d xd:script='required; create from(\"/a/d\")'/>\n"+
"      </xd:mixed>\n"+
"   </a>\n"+
"</xd:def>";
			xml = "<a/>";
			assertFalse(test(xdef, xml, null, 'P', xml, ""));
			assertFalse(test(xdef, xml, "",'C', xml, ""));
			xdef = //test reference to group
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script='2..3; ref a'/>\n"+
"  </a>\n"+
"  <xd:sequence name='a'>\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><x/></a>", reporter);
			assertTrue(reporter.errorWarnings(),"errors not recognized");
			xml = "<a><b/><c/><b/><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			parse(xp, "", "<a/>", reporter); //should be error
			assertErrors(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><b/><c/><b/><c/><b/><c/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script='ref a'/>\n"+
"  </a>\n"+
"  <xd:sequence name='a' xd:script='2..3;'>\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b/><c/><b/><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			parse(xp, "", "<a/>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><b/><c/><b/><c/><b/><c/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a' name='a'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script='2..3; ref b#a'/>\n"+
"  </a>\n"+
"</xd:def>\n"+
"<xd:def name='b'>\n"+
"  <xd:sequence name='a'>\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<a><b/><c/><b/><c/></a>";
			assertEq(xml, parse(xp, "a", xml, reporter));
			assertNoErrors(reporter);
			parse(xp, "a", "<a/>", reporter);
			assertErrors(reporter);
			parse(xp, "a", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "a", "<a><b/><c/><b/><c/><b/><c/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a' name='a'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script='ref b#a'/>\n"+
"  </a>\n"+
"</xd:def>\n"+
"<xd:def name='b'>\n"+
"  <xd:sequence name='a' xd:script='2..3;'>\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<a><b/><c/><b/><c/></a>";
			assertEq(xml, parse(xp, "a", xml, reporter));
			assertNoErrors(reporter);
			parse(xp, "a", "<a/>", reporter);
			assertErrors(reporter);
			parse(xp, "a", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "a", "<a><b/><c/><b/><c/><b/><c/><b/><c/></a>", reporter);
			assertErrors(reporter);
//misc tests
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a>\n"+
"  <xd:sequence xd:script='1'>\n"+
"    <c xd:script='1; create from(\"/a/c\")'/>\n"+
"    <e xd:script='?; create from(\"/a/e\")'/>\n"+
"  </xd:sequence>\n"+
"  <xd:choice xd:script='1'>\n"+
"    <d xd:script='1; create from(\"/a/d\")'/>\n"+
"    <e xd:script='1; create from(\"/a/e\")'/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><c/><d/></a>";
			assertEq(xml, create(xdef, null, "a", reporter, xml));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a>\n"+
"  <b/>\n"+
"  <xd:choice>\n"+
"    <c xd:script = 'create from(\"c\")' a = 'optional string();'/>\n"+
"    <d xd:script = 'create from(\"d\")' a = 'optional string();'/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><b/><d a = 'y'/><c a = 'x'/></a>";
			assertEq(create(xdef, null, "a", reporter, xml),
				"<a><b/><c a='x'/></a>");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a>\n"+
"  <b/>\n"+
"  <xd:choice>\n"+
"     <c xd:script = 'create from(\"//d\")' a = 'optional string();'/>\n"+
"     <d xd:script = 'create from(\"//c\")' a = 'optional string();'/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><b><d a = 'y'/></b></a>";
			assertEq(create(xdef, null, "a", reporter, xml),
				"<a><b/><c a='y'/></a>");
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<a>\n"+
"  <b/>\n"+
"  <xd:choice xd:script= 'create from(\"//c\")'>\n"+
"    <c a = 'optional string();'/>\n"+
"    <d a = 'optional string();'/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq(create(xp, null, "a",reporter,"<a><b><c a='x'/></b></a>"),
				"<a><b/><c a='x'/></a>");
			assertNoErrors(reporter);
			assertEq(create(xp, null, reporter, "<a><b><d a='y'/></b></a>"),
				"<a><b/></a>");
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"<![CDATA[Element s=xparse(\"<X><Y><b a='b'/></Y><Y><c a='x'/></Y></X>\");]]>\n"+
"</xd:declaration>  \n"+
"<a xd:script=\"create 1\">\n"+
"  <xd:choice xd:script=\"occurs *; create xpath('//Y',s)\"> \n"+
"    <b a=\"string\" />\n"+
"    <c a=\"string\" />\n"+
"  </xd:choice>\n"+
"</a>	\n"+
"</xd:def>";
			assertEq("<a><b a='b'/><c a='x'/></a>",
				create(xdef, "", "a", reporter, null));
			assertNoErrors(reporter);
			String expected = //Expected result of following tests
"<Envelope>" +
  "<Header>" +
	"<Request IdentZpravy=\"123\" RemotePartner=\"x\"/>" +
	  "<Answer RequestPending=\"x\" RequestAcepted=\"x\"/>" +
  "</Header>" +
  "<Body>" +
	"<Fault>" +
	  "<Detail>" +
		"<Chyba KodChyby=\"4253\" Hodnota=\"73547386\"" +
		  " Xpath=\"Get_PSP/Get_PSP@CisloSmlouvy\"" +
		  " PopisChyby=\"Smlouva neexistuje (CisloSmlouvy, PoradiVozidla)\"/>" +
	  "</Detail>" +
	"</Fault>" +
  "</Body>" +
"</Envelope>";

////////////////////////////////////////////////////////////////////////////////
//1. primy popis
////////////////////////////////////////////////////////////////////////////////
			xml=
"<Fault_ IdPrace=\"137\">\n"+
"  <RequestId IdentZpravy=\"123\" IdPartner=\"2\"/>\n"+
"  <AnswerId Status=\"Fault\" Program=\"WBM\"/>\n"+
"  <Fault>\n"+
"    <Detail>\n"+
"      <Chyba Hodnota=\"73547386\" KodChyby=\"4253\" " +
		 "Xpath=\"Get_PSP/Get_PSP@CisloSmlouvy\" " +
		 "PopisChyby=\"Smlouva neexistuje (CisloSmlouvy, PoradiVozidla)\"/>\n"+
"    </Detail>\n"+
"  </Fault>\n"+
"</Fault_>";
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"\n"+
"   <xd:macro name=\"moreAll\">\n"+
"      options moreAttributes, moreElements, moreText\n"+
"   </xd:macro>\n"+
"\n"+
"  <Envelope         xd:script=\"occurs 1; create from ('/Fault_')\">\n"+
"    <Header         xd:script=\"occurs 1; create newElement()\">\n"+
"      <Request      xd:script=\"occurs 1; create from ('//RequestId')\"\n"+
"         RemotePartner     =\"required; onTrue setText('x');" +
			" create from('@IdPartner')\"\n"+
"         IdentZpravy       =\"required\"\n"+
"         RefMsgID          =\"optional\"\n"+
"      />\n"+
"      <Answer              xd:script=\"occurs 1; create newElement()\"\n"+
"         RequestAcepted    =\"required; create 'x'\"\n"+
"         RequestPending    =\"optional; create 'x'\"\n"+
"      />\n"+
"    </Header>\n"+
"    <Body                  xd:script=\"occurs 1; create newElement()\">\n"+
"      <NositelPSP       xd:script=\"occurs 0..1; ref NositelPSP;" +
				" create from ('/NositelPSP_/NositelPSP')\"/>\n"+
"      <Fault            xd:script=\"occurs 0..1;  ref Fault;" +
				" create from ('/Fault_/Fault')\" />\n"+
"      <PingFlow         xd:script=\"occurs 0..; ref PingFlow;" +
				" create from ('/PingFlow_/PingStatus')\"/>\n"+
"      <PSP              xd:script=\"occurs 0..1; ref PSP;" +
				" create from ('/PSP_/PSP')\"/>\n"+
"      <SmlouvyPSP       xd:script=\"occurs 0..1; ref SmlouvyPSP;" +
				" create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"      <VolnePSP         xd:script=\"occurs 0..1; ref VolnePSP;" +
				" create from ('/VolnePSP_/VolnePSP')\"/>\n"+
"    </Body>\n"+
"  </Envelope>\n"+
"  <Fault>\n"+
"    <Detail>\n"+
"      <Chyba Hodnota=\"required int()\"\n"+
"             KodChyby=\"required int()\"\n"+
"             Xpath=\"required string()\"\n"+
"             PopisChyby=\"required string()\"/>\n"+
"    </Detail>\n"+
"  </Fault>\n"+
"  <NositelPSP xd:script=\"occurs 1;" +
			 " create from('/NositelPSP_/NositelPSP')\"/>\n"+
"  <PingFlow xd:script=\"occurs 1..;" +
		   " create from ('/PingFlow_/PingStatus')\"/>\n"+
"  <PSP     xd:script=\"occurs 1; create from ('/PSP_/PSP')\"/>\n"+
"  <SmlouvyPSP xd:script=\"occurs 1;" +
			 " create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"  <VolnePSP xd:script=\"occurs 1; create from('/VolnePSP_/VolnePSP')\"/>\n"+
"</xd:def>";
			assertEq(create(xdef, null, "Envelope", reporter, xml), expected);
			assertNoErrors(reporter);


////////////////////////////////////////////////////////////////////////////////
//2. sekvence
////////////////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"\n"+
"   <xd:macro name=\"moreAll\">" +
				"options moreAttributes, moreElements, moreText</xd:macro>\n"+
"\n"+
"  <Envelope         xd:script=\"occurs 1; create from ('/Fault_')\">\n"+
"    <Header         xd:script=\"occurs 1; create newElement()\">\n"+
"      <Request         xd:script=\"occurs 1; create from ('//RequestId')\"\n"+
"         RemotePartner     =\"required; onTrue setText('x');" +
				" create from('@IdPartner')\"\n"+
"         IdentZpravy       =\"required\"\n"+
"         RefMsgID          =\"optional\"\n"+
"      />\n"+
"      <Answer              xd:script=\"occurs 1; create newElement()\"\n"+
"         RequestAcepted    =\"required; create 'x'\"\n"+
"         RequestPending    =\"optional; create 'x'\"\n"+
"      />\n"+
"    </Header>\n"+
"    <Body                  xd:script=\"occurs 1; create newElement()\">\n"+
"      <xd:sequence xd:script=\"occurs 0..1;\" >\n"+
"         <Fault            xd:script=\"occurs 0..1;  ref Fault;" +
				" create from ('/Fault_/Fault')\" />\n"+
"         <NositelPSP       xd:script=\"occurs 0..1;" +
				" create from ('/NositelPSP_/NositelPSP')\"/>\n"+
"         <PingFlow         xd:script=\"occurs 0..1;" +
				" create from ('/PingFlow_/PingStatus')\"/>\n"+
"         <PSP              xd:script=\"occurs 0..1;" +
				" create from ('/PSP_/PSP')\"/>\n"+
"         <SmlouvyPSP       xd:script=\"occurs 0..1;" +
				" create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"         <VolnePSP         xd:script=\"occurs 0..1;" +
				" create from ('/VolnePSP_/VolnePSP')\"/>\n"+
"      </xd:sequence>\n"+
"    </Body>\n"+
"  </Envelope>\n"+
"  <Fault>\n"+
"    <Detail>\n"+
"      <Chyba Hodnota=\"required int()\"\n"+
"             KodChyby=\"required int()\"\n"+
"             Xpath=\"required string()\"\n"+
"             PopisChyby=\"required string()\"/>\n"+
"    </Detail>\n"+
"  </Fault>\n"+
"  <NositelPSP xd:script=\"occurs 1;" +
			 " create from('/NositelPSP_/NositelPSP')\"/>\n"+
"  <PingFlow xd:script=\"occurs 1..;" +
		   " create from ('/PingFlow_/PingStatus')\"/>\n"+
"  <PSP     xd:script=\"occurs 1; create from ('/PSP_/PSP')\"/>\n"+
"  <SmlouvyPSP xd:script=\"occurs 1;" +
			 " create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"  <VolnePSP xd:script=\"occurs 1; create from('/VolnePSP_/VolnePSP')\"/>\n"+
"</xd:def>";
			assertEq(expected, create(xdef, null, "Envelope", reporter, xml));
			assertNoErrors(reporter);

////////////////////////////////////////////////////////////////////////////////
//3. mixed
////////////////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"\n"+
"   <xd:macro name=\"moreAll\">" +
				"options moreAttributes, moreElements, moreText</xd:macro>\n"+
"\n"+
"  <Envelope           xd:script=\"occurs 1; create from ('/Fault_')\">\n"+
"    <Header           xd:script=\"occurs 1; create newElement()\">\n"+
"      <Request        xd:script=\"occurs 1; create from ('//RequestId')\"\n"+
"         RemotePartner     =\"required; onTrue setText('x');" +
				" create from('@IdPartner')\"\n"+
"         IdentZpravy       =\"required\"\n"+
"         RefMsgID          =\"optional\"\n"+
"      />\n"+
"      <Answer              xd:script=\"occurs 1; create newElement()\"\n"+
"         RequestAcepted    =\"required; create 'x'\"\n"+
"         RequestPending    =\"optional; create 'x'\"\n"+
"      />\n"+
"    </Header>\n"+
"    <Body                  xd:script=\"occurs 1; create newElement()\">\n"+
"      <xd:mixed>\n"+
"         <NositelPSP       xd:script=\"occurs 0..1;" +
				" create from ('/NositelPSP_/NositelPSP')\"/>\n"+
"         <PingFlow         xd:script=\"occurs 0..1;" +
				" create from ('/PingFlow_/PingStatus')\"/>\n"+
"         <PSP              xd:script=\"occurs 0..1;" +
				" create from ('/PSP_/PSP')\"/>\n"+
"         <SmlouvyPSP       xd:script=\"occurs 0..1;" +
				" create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"         <VolnePSP         xd:script=\"occurs 0..1;" +
				" create from ('/VolnePSP_/VolnePSP')\"/>\n"+
"         <Fault            xd:script=\"occurs 0..1;  ref Fault;" +
				" create from ('/Fault_/Fault')\" />\n"+
"      </xd:mixed>\n"+
"    </Body>\n"+
"  </Envelope>\n"+
"  <Fault>\n"+
"    <Detail>\n"+
"      <Chyba Hodnota=\"required int()\"\n"+
"             KodChyby=\"required int()\"\n"+
"             Xpath=\"required string()\"\n"+
"             PopisChyby=\"required string()\"/>\n"+
"    </Detail>\n"+
"  </Fault>\n"+
"  <NositelPSP xd:script=\"occurs 1;" +
			 " create from('/NositelPSP_/NositelPSP')\"/>\n"+
"  <PingFlow xd:script=\"occurs 1..;" +
		   " create from ('/PingFlow_/PingStatus')\"/>\n"+
"  <PSP     xd:script=\"occurs 1; create from ('/PSP_/PSP')\"/>\n"+
"  <SmlouvyPSP xd:script=\"occurs 1;" +
			 " create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"  <VolnePSP xd:script=\"occurs 1; create from('/VolnePSP_/VolnePSP')\"/>\n"+
"</xd:def>";
			assertEq(expected, create(xdef, null, "Envelope", reporter, xml));
			assertNoErrors(reporter);

////////////////////////////////////////////////////////////////////////////////
//4. choice
////////////////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:macro name=\"moreAll\">" +
"options moreAttributes, moreElements, moreText"+
"</xd:macro>\n"+
"  <Envelope           xd:script=\"occurs 1; create from ('/Fault_')\">\n"+
"    <Header           xd:script=\"occurs 1; create newElement()\">\n"+
"      <Request        xd:script=\"occurs 1; create from ('//RequestId')\"\n"+
"         RemotePartner     =\"required; onTrue setText('x');" +
				" create from('@IdPartner')\"\n"+
"         IdentZpravy       =\"required\"\n"+
"         RefMsgID          =\"optional\"\n"+
"      />\n"+
"      <Answer              xd:script=\"occurs 1; create newElement()\"\n"+
"         RequestAcepted    =\"required; create 'x'\"\n"+
"         RequestPending    =\"optional; create 'x'\"\n"+
"      />\n"+
"    </Header>\n"+
"    <Body                  xd:script=\"occurs 1; create newElement()\">\n"+
"      <xd:choice occurs=\"?\">\n"+
"         <NositelPSP       xd:script=\"occurs 1;" +
				" create from ('/NositelPSP_/NositelPSP')\"/>\n"+
"         <PingFlow         xd:script=\"occurs 1;" +
				" create from ('/PingFlow_/PingStatus')\"/>\n"+
"         <PSP              xd:script=\"occurs 1;" +
				" create from ('/PSP_/PSP')\"/>\n"+
"         <SmlouvyPSP       xd:script=\"occurs 1;" +
				" create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"         <VolnePSP         xd:script=\"occurs 1;" +
				" create from ('/VolnePSP_/VolnePSP')\"/>\n"+
"         <Fault            xd:script=\"occurs 1;  ref Fault;" +
				" create from ('/Fault_/Fault')\" />\n"+
"      </xd:choice>\n"+
"    </Body>\n"+
"  </Envelope>\n"+
"  <Fault>\n"+
"    <Detail>\n"+
"      <Chyba Hodnota=\"required int()\"\n"+
"             KodChyby=\"required int()\"\n"+
"             Xpath=\"required string()\"\n"+
"             PopisChyby=\"required string()\"/>\n"+
"    </Detail>\n"+
"  </Fault>\n"+
"  <NositelPSP xd:script=\"occurs 1;" +
			 " create from('/NositelPSP_/NositelPSP')\"/>\n"+
"  <PingFlow xd:script=\"occurs 1..;" +
		   " create from ('/PingFlow_/PingStatus')\"/>\n"+
"  <PSP     xd:script=\"occurs 1; create from ('/PSP_/PSP')\"/>\n"+
"  <SmlouvyPSP xd:script=\"occurs 1;" +
			 " create from ('/SmlouvyPSP_/SmlouvyPSP')\"/>\n"+
"  <VolnePSP xd:script=\"occurs 1; create from('/VolnePSP_/VolnePSP')\"/>\n"+
"</xd:def>";
			assertEq(create(xdef, null, "Envelope", reporter, xml), expected);
			assertNoErrors(reporter);
			xdef = //sequence
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x:mixed>\n"+
"    <p x:script = \"occurs 1..\" />\n"+
"    <x:sequence>\n"+
"      <q x:script = \"occurs 1\" />\n"+
"      required int()\n"+
"      <q x:script = \"occurs 1\" />\n"+
"    </x:sequence>\n"+
"  </x:mixed>\n"+
"</a>\n"+
"</x:def>";
			parse(xdef, null, "<a><p/><q/>123<q/><p/></a>", reporter);
			assertNoErrors(reporter);
			xdef = //test mixed as last in sequence
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:choice>\n"+
"        <xd:sequence>\n"+
"          <b/>\n"+
"          <d xd:script='occurs 0..1'/>\n"+
"        </xd:sequence>\n"+
"        <xd:sequence>\n"+
"          <c/>\n"+
"          <d xd:script='occurs 0..1'/>\n"+
"        </xd:sequence>\n"+
"      </xd:choice>\n"+
"      <xd:mixed xd:script='occurs 1'>\n"+
"        <p/>\n"+
"        <q/>\n"+
"      </xd:mixed>\n"+
"    </xd:sequence>\n"+
"    <z xd:script='occurs 0..1'/>\n"+
"  </a>\n"+
"</xd:def>";
			parse(xdef, null, "<a><c/><d/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing p,q");
			xdef = //test reference of groups.
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a>\n"+
"   <xd:choice>\n"+
"     <a/>\n"+
"     <b/>\n"+
"   </xd:choice>\n"+
"   <xd:mixed ref='a'/>\n"+
" </a>\n"+
" <xd:mixed empty='false' name='a'>\n"+
"   <p/>\n"+
"   <q/>\n"+
" </xd:mixed>\n"+
"</xd:def>";
			parse(xdef, null, "<a><b/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing p,q");
			xdef = //test sequence references witn nested group
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <b xd:script='occurs *; ref b'/>\n"+
"</a>\n"+
"<b>\n"+
"  <xd:sequence xd:script='optional; ref sq'/>\n"+
"</b>\n"+
"<xd:sequence xd:name='sq'>\n"+
"  <xd:choice xd:script='occurs *'>\n"+
"    string()\n"+
"    <c>\n"+
"      <xd:sequence xd:script='occurs +; ref sq'/>\n"+
"    </c>\n"+
"    <b xd:script='ref b'/>\n"+
"  </xd:choice>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a/>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b>x<c/></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><b/><b><b/><b/></b></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x<c/></b></a>", reporter);
			assertNoErrors(reporter);
			xdef = //set choice "+" instead of "*"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <b xd:script='optional; ref b'/>\n"+
"</a>\n"+
"<b>\n"+
"  <xd:sequence xd:script='optional; ref sq'/>\n"+
"</b>\n"+
"<xd:sequence xd:name='sq'>\n"+
"  <xd:choice xd:script='occurs +'>\n"+
"    string()\n"+
"    <c>\n"+
"      <xd:sequence xd:script='occurs *; ref sq'/>\n"+
"    </c>\n"+
"    <b xd:script='ref b'/>\n"+
"  </xd:choice>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a/>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b>x<c/></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><b/><b><b/><b/></b></b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x</b></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><b><c/>x<c/></b></a>", reporter);
			assertNoErrors(reporter);
			xdef = // check mixed, include 1
"<x:collection xmlns:x='" + _xdNS + "'>\n"+
"<xd:def name='a' xd:root='a' xmlns:xd='" + _xdNS + "'\n"+
" script='options ignoreEmptyAttributes' >\n"+
"<a xd:script='ref b'>\n"+
"    <xd:sequence ref='dummy1'/>\n"+
"    required string()\n"+
"    <xd:sequence ref='dummy2'/>\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"<xd:sequence name='dummy1'>\n"+
"  <xd:mixed>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:sequence>\n"+
"<xd:sequence name='dummy2'>\n"+
"  <xd:choice>\n"+
"    <r/>\n"+
"    <s/>\n"+
"  </xd:choice>\n"+
"</xd:sequence>\n"+
"</xd:def>\n"+
"</x:collection>";
			parse(xdef, "a", "<a attr='a'><c/><q/><p/>text<s/></a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" script='options ignoreEmptyAttributes'>\n"+
"<a xd:script = 'ref b'>\n"+
"  <xd:sequence ref='dummy1'/>\n"+
"  required\n"+
"  <xd:sequence ref='dummy2'/>\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"<xd:sequence name='dummy1'>\n"+
"  <xd:mixed>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:sequence>\n"+
"<xd:sequence name='dummy2'>\n"+
"  <xd:choice>\n"+
"    <r/>\n"+
"    <s/>\n"+
"  </xd:choice>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			parse(xdef, null, "<a attr='a'><c/><p/><q/>text<s/></a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        script='options ignoreEmptyAttributes' >\n"+
"<a xd:script = 'ref b'>\n"+
"  <xd:mixed xd:ref='dummy1'/>\n"+
"  required string(4)\n"+
"  <xd:choice ref='dummy2'/>\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"\n"+
"<xd:mixed name='dummy1'>\n"+
"  <p/>\n"+
"  <q/>\n"+
"</xd:mixed>\n"+
"\n"+
"<xd:choice name='dummy2'>\n"+
"  <r/>\n"+
"  <s/>\n"+
"</xd:choice>\n"+
"</xd:def>";
			parse(xdef, null, "<a attr='a'><c/><p/><q/>text<s/></a>", reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" script= 'options ignoreEmptyAttributes' >\n"+
"<a xd:script = 'ref b'>\n"+
"  <xd:sequence xd:ref='dummy1'/>\n"+
"  required\n"+
"  <xd:sequence ref='dummy2'/>\n"+
"</a>\n"+
"<b attr='required'>\n"+
"  <c/>\n"+
"</b>\n"+
"<xd:sequence name = \"dummy1\">\n"+
"  <xd:mixed>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:sequence>\n"+
"<xd:sequence name='dummy2'>\n"+
"  <xd:choice>\n"+
"    <r/>\n"+
"    <s/>\n"+
"  </xd:choice>\n"+
"</xd:sequence>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a attr='a'><c/><p/><q/>text<s/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><q/><p/>text<s/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><q/><p/>text<r/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a attr='a'><c/>text<r/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><p/>text<r/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><q/>text<r/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><p/><q/><r/></a>", reporter);
			assertErrors(reporter);
			parse(xp, null, "<a attr='a'><c/><p/><q/>text</a>", reporter);
			assertErrors(reporter);
			xdef = // check mixed, include
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='a' root='a' script='options ignoreEmptyAttributes' >\n"+
"<a>\n"+
"  <p p = \"required xdatetime('ddMMyyyy')\"/>\n"+
"  <xd:sequence ref='b#dummy'/>\n"+
"  <z/>\n"+
"</a>\n"+
"</xd:def>\n"+
"<xd:def name='b' script='options ignoreEmptyAttributes'>\n"+
"<xd:sequence name='dummy'>\n"+
"  <xd:mixed>\n"+
"    <b/>\n"+
"    <c xd:script='occurs 0..' />\n"+
"    <d/>\n"+
"    <xd:sequence ref='dummy1'/>\n"+
"  </xd:mixed>\n"+
"</xd:sequence>\n"+
"<xd:sequence name='dummy1'>\n"+
"    <xd:sequence ref='dummy2'/>\n"+
"    <y/>\n"+
"</xd:sequence>\n"+
"<xd:sequence name='dummy2'>\n"+
"    <e xd:script='occurs 0..'/>\n"+
"    <f xd:script='occurs 0..'/>\n"+
"</xd:sequence>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml = "<a>\n\n<p p='31122004'/>\n\n<y/>\n"+
				"<b/>\n<d/>\n<e/>\n<f/>\n<z/>\n</a>";
			parse(xdef, "a", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {// check mixed, include
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:mixed empty='false'>\n"+
"    <b xd:script = 'occurs 0..' />\n"+
"    optional string()\n"+
"    <c xd:script = 'occurs 0..'/>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, null, "<a>t1<b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a>t1<b/>t2</a>\n", reporter);
			assertTrue(reporter.errorWarnings());
			parse(xp, null, "<a/>", reporter);
			rep = reporter.getReport();
			assertTrue(rep != null && ("XDEF520".equals(rep.getMsgID())),
				reporter.printToString());
		} catch (Exception ex) {fail(ex);}
/*xx*/
		try {// check mixed in sequence
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script='2'>\n"+
"      <xd:mixed>\n"+
"        <b />\n"+
"        <c />\n"+
"        int\n"+
"      </xd:mixed>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b/><c/>1<b/><c/>2</a>";
			assertEq(parse(xp, null, xml, reporter), xml);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
/*xx*/
		try {//test choice
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x:choice>\n"+
"  <b/>\n"+
"  <c/>\n"+
"  </x:choice>\n"+
"</a>\n"+
"</x:def>";
			xp = compile(xdef);
			parse(xp, null, "<a/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, null, "<a><b/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, null, "<a><c/><b/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, null, "<a><b/><c/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:choice script='occurs 0..1;'>\n"+
"      <O xd:script='occurs 0..2;'/>\n"+
"      <A xd:script='occurs 1;'/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><O/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><O/><O/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><A/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a><O/><O/><O/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
			parse(xp, "", "<a><O/><A/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not reported");
		} catch (Exception ex) {fail(ex);}
		try {//text checking inside sequence
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <xd:sequence>\n"+
"    <c/>\n"+
"    <b/>\n"+
"    required string();\n"+
"  </xd:sequence>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a>\n  ahoj\n  <b/>\n  \n  <b/>\n  \n  <c/>\n</a>";
			parse(xdef, null, xml, reporter);
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {//test reference of groups among Xdefinitions.
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name='a' xd:root='a'>\n"+
"  <a>\n"+
"    <xd:sequence xd:script='ref x#a'/>\n"+
"  </a>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name='x'>\n"+
"  <xd:sequence name='a'>\n"+
"    <xd:choice xd:script='ref x#a'/>\n"+
"    <xd:mixed xd:script='ref x#a'/>\n"+
"  </xd:sequence>\n"+
"  <xd:choice occurs='?' name='a'>\n"+
"    <xd:sequence xd:script='ref b#a'/>\n"+
"    <xd:sequence xd:script='ref b#b'/>\n"+
"    <xd:sequence xd:script='ref b#c'/>\n"+
"  </xd:choice>\n"+
"  <xd:mixed name='a' ref='b#a'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name='b'>\n"+
"  <xd:sequence name='a'>\n"+
"    <b/>\n"+
"    <d xd:script='occurs 0..1'/>\n"+
"  </xd:sequence>\n"+
"  <xd:sequence name='b'>\n"+
"    <c/>\n"+
"    <d xd:script='occurs 0..1'/>\n"+
"  </xd:sequence>\n"+
"  <xd:sequence name='c'>\n"+
"    <d/>\n"+
"  </xd:sequence>\n"+
"  <xd:mixed name='a' empty='false'>\n"+
"    <p/>\n"+
"    <q/>\n"+
"  </xd:mixed>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			parse(xp, "a", "<a><c/><d/><p/><q/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "a", "<a><c/><d/><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "a", "<a><d/><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "a", "<a><q/><p/></a>", reporter);
			assertNoErrors(reporter);
			parse(xp, "a", "<a><c/><d/><p/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing q");
			parse(xp, "a", "<a><c/><d/><q/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing p");
			parse(xp, "a", "<a><c/><d/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing p,q");
			parse(xp, "a", "<a/>", reporter);
			assertTrue(reporter.errorWarnings(), "Not reported missing p,q");
		} catch(Exception ex) {
			fail(ex);
		}
//script methods
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>\n"+
"  init out('i#{p} ');finally out('f#{p} ')\n"+
"</xd:macro>\n"+
"  <a xd:script=\"1; ${m(p='a')};\">\n"+
"    <xd:choice>\n"+
"      <xd:sequence>\n"+
"        <c xd:script=\"1; ${m(p='c1')}; create from('/a/c')\"/>\n"+
"        <d xd:script=\"?; ${m(p='d1')}; create from('//a/d')\"/>\n"+
"        <e xd:script=\"?; ${m(p='e1')}\"/>\n"+
"      </xd:sequence>\n"+
"      <xd:sequence>\n"+
"        <d/>\n"+
"        <c xd:script=\"occurs ?; ${m(p='c2')}\"/>\n"+
"        <e xd:script=\"occurs *; ${m(p='e2')}\"/>\n"+
"      </xd:sequence>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><c/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("ia ic1 fc1 fa ", strw.toString());
			xml = "<a><d/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null ,null));
			assertNoErrors(reporter);
			assertEq("ia fa ", strw.toString());
			xml = "<a><d/><c/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("ia ic2 fc2 fa ", strw.toString());
			xml = "<a><d/><e/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("ia ie2 fe2 fa ", strw.toString());
			xml = "<a><c/><d/><d/><e/><e/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("ia ic1 fc1 id1 fd1 ie2 fe2 ie2 fe2 fa ", strw.toString());
			xml = "<a/>"; /*error*/
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertTrue(reporter.getErrorCount() == 1, reporter);
			assertEq("ia fa ", strw.toString());
			xml = "<a><c/><d/><e/><e/></a>"; /*error:  e*/
			strw = new StringWriter();
			assertEq("<a><c/><d/><e/></a>",
				parse(xp, null, xml, reporter, strw, null, null));
			assertTrue(reporter.getErrorCount() == 1, reporter);
			assertEq("ia ic1 fc1 id1 fd1 ie1 fe1 fa ", strw.toString());

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>\n"+
"  init out('i#{p} ');finally out('f#{p} ')\n"+
"</xd:macro>\n"+
"<a xd:script=\"${m(p='a')}\">\n"+
"    <b xd:script=\"${m(p='b1')}\">required string();${m(p='T1')};</b>\n"+
"    <xd:sequence script=\"*;${m(p='SQ1')}\">\n"+
"      required string();${m(p='T2')}\n"+
"      <c xd:script=\"${m(p='c1')}\"/>\n"+
"    </xd:sequence>\n"+
"    <b xd:script=\"${m(p='b2')}\">required string();${m(p='T3')}</b>\n"+
"    <xd:sequence script=\"*;${m(p='SQ2')}\">\n"+
"	   required string();${m(p='T4')}\n"+
"      <c xd:script=\"${m(p='c2')}\"/>\n"+
"    </xd:sequence>\n"+
"</a>\n"+
"</xd:def>\n";
			xml = "<a><b>0</b>1<c/>2<c/><b>3</b>4<c/>5<c/></a>";
			strw = new StringWriter();
			assertEq(parse(xdef, null, xml, reporter, strw, null, null), xml);
			assertNoErrors(reporter);
			assertEq("ia ib1 iT1 fT1 fb1 iSQ1 iT2 fT2 ic1 fc1 fSQ1 iT2 fT2 ic1"
				+ " fc1 fSQ1 fSQ1 fSQ1 ib2 iT3 fT3 fb2 iSQ2 iT4 fT4 ic2 fc2"
				+ " fSQ2 iT4 fT4 ic2 fc2 fSQ2 fa ", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {//Sisma
			xml = dataDir + "TestGroups01.xml";
			xdef = dataDir + "TestGroups01_1.xdef";
			parse(xdef, "SODContainer_Template", xml, reporter);
			assertNoErrors(reporter);
			xdef = dataDir + "TestGroups01_2.xdef";
			parse(xdef, "SODContainer_Template", xml, reporter);
			assertNoErrors(reporter);
			xdef = dataDir + "TestGroups01_3.xdef";
			parse(xdef, "SODContainer_Template", xml, reporter);
			assertNoErrors(reporter);
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