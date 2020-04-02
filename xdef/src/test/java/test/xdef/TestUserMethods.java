package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;
import java.io.StringWriter;

/** Test of user methods, process mode, create mode, groups.
 * @author Vaclav Trojan
 */
public final class TestUserMethods extends XDTester {

	public TestUserMethods() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		String xdef, xml;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		try { //test methods
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <A xd:script = \"ref A; create from('A')\" a='string'/>;\n"+
"  </a>\n"+
"  <A xd:script=\"init out('A'); finally out('B');\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><A a='x'/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "AB");
			strw = new StringWriter();
			assertEq(xml,
				create(xp,null,"a",reporter,"<X><A a='x'/></X>",strw,null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "AB");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>\n"+
"match {out('m#{p} '); return true;};\n"+
"init out('i#{p} ');finally out('f#{p} ')\n"+
"</xd:macro>\n"+
"<xd:macro name='n' p='?'>\n"+
"${m(p='#{p}')}; onStartElement out('s#{p} ')\n"+
"</xd:macro>\n"+
"  <a xd:script=\"${n(p='a')}\">\n"+
"   <xd:choice xd:script=\"1; ${m(p='CH')}\">\n"+
"      <b xd:script=\"${n(p='b')}\"/>\n"+
"      <x xd:script=\"1\"/>\n"+
"   </xd:choice>\n"+
"   <c xd:script=\"${n(p='c')}\"/>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a><b/><c/></a>";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null,null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mCH iCH mb ib sb fb fCH mc ic sc fc fa ");
			strw = new StringWriter();
			assertEq("<a><b/><c/></a>",
				create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "ia sa iCH ib sb fb fCH ic sc fc fa ");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>\n"+
"match {out('m#{p} '); return true;};\n"+
"init out('i#{p} ');finally out('f#{p} ')\n"+
"</xd:macro>\n"+
"<xd:macro name='n' p='?'>\n"+
"${m(p='#{p}')}; onStartElement out('s#{p} ')\n"+
"</xd:macro>\n"+
"<a xd:script=\"${n(p='a')}\">\n"+
"  <xd:mixed xd:script=\"1; ${m(p='MX')}\">\n"+
"    <b xd:script=\"${n(p='b')}\"/>\n"+
"    <x xd:script=\"?;${n(p='x')}; create from('/a/x')\"/>\n"+
"  </xd:mixed>\n"+
"  <c xd:script=\"${n(p='c')}\"/>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><b/><c/></a>";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mMX iMX mb ib sb fb fMX mc ic sc fc fa ");
			strw = new StringWriter();
			assertEq("<a><b/><c/></a>",
				create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(), "ia sa iMX ib sb fb fMX ic sc fc fa ");
			xml = "<a><b/><x/><c/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mMX iMX mb ib sb fb mx ix sx fx fMX mc ic sc fc fa ");
			strw = new StringWriter();
			assertEq(xml, create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ia sa iMX ib sb fb ix sx fx fMX ic sc fc fa ");

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>\n"+
"match {out('m#{p} '); return true;};\n"+
"init out('i#{p} ');finally out('f#{p} ')\n"+
"</xd:macro>\n"+
"<xd:macro name='n' p='?'>\n"+
"${m(p='#{p}')}; onStartElement out('s#{p} ')\n"+
"</xd:macro>\n"+
"   <a xd:script=\"${n(p='a')}\">\n"+
"   <p xd:script=\"?; ${n(p='p')}\"/>\n"+
"   <xd:choice xd:script=\"1; ${m(p='CH')}\">\n"+
"      <b xd:script=\"${n(p='b')}; create from('/a/b')\"/>\n"+
"      <x xd:script=\"1;${n(p='x')}; create from('/a/x')\"/>\n"+
"   </xd:choice>\n"+
"   <c xd:script=\"${n(p='c')}\"/>\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><x/><c/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw,null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mCH iCH mx ix sx fx fCH mc ic sc fc fa ");
			strw = new StringWriter();
			assertEq("<a><x/><c/></a>",
				create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ia sa iCH ix sx fx fCH ic sc fc fa ");
			xml = "<a><p/><x/><c/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw,null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mp ip sp fp mCH iCH mx ix sx fx fCH mc ic sc fc fa ");
			strw = new StringWriter();
			assertEq("<a><p/><x/><c/></a>",
				create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ia sa ip sp fp iCH ix sx fx fCH ic sc fc fa ");

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n" +
"  <xd:macro name='m' p='?'>\n"+
"    match {out('m#{p} '); return true;};\n"+
"    init out('i#{p} ');finally out('f#{p} ')\n"+
"  </xd:macro>\n"+
"  <xd:macro name='n' p='?'>\n"+
"    ${m(p='#{p}')}; onStartElement out('s#{p} ')\n"+
"  </xd:macro>\n"+
"</xd:declaration>\n" +
" <a xd:script=\"1;${n(p='a')}\">\n"+
"   <xd:sequence xd:script=\"1;${m(p='SQ')}\">\n"+
"      <b xd:script=\"1;${n(p='b')}; create from('/a/b')\"/>\n"+
"      <xd:any xd:script=\"1; ${n(p='x')}; create from('/a/c')\"/>\n"+
"   </xd:sequence>\n"+
"   <d xd:script=\"1;${n(p='d')}; create from('/a/d')\"/>\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xml = "<a><b/><c/><d/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw,null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ma ia sa mSQ iSQ mb ib sb fb mx ix sx fx fSQ md id sd fd fa ");
			strw = new StringWriter();
			assertEq(xml, create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"ia sa iSQ ib sb fb ix sx fx fSQ id sd fd fa ");
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}

}