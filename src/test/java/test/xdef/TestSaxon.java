package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.impl.code.DefXQueryExpr;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/** Test versions with Saxon and without Saxon implementation.
 * @author Vaclav Trojan
 */
public class TestSaxon extends XDTester {

	public TestSaxon() {super();}

	private void testNoSaxon() {
		String xdef, xml;
		ArrayReporter reporter = new ArrayReporter();
		try {//test binding of XPath variables with XDefinition variables
			xdef = //integer variable x without leading "$"
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int x = 123; </xd:declaration>\n"+
"  <a xd:script = \"create from('*[@a=$x]')\"> string </a>\n"+
"</xd:def>";
			xml = "<w><b a='x'/><b a='123'>zxy</b><b>xx</b></w>";
			assertEq("<a>zxy</a>", create(xdef, "", "a", reporter, xml));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	private void testSaxon() {
		String xdef, xml;
		Element el;
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		try {//fromXQ (xquery)
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root>\n"+
" <xd:sequence xd:script=\"occurs *; create fromXQ('//a')\">\n"+
"  <a a = \"required string; create fromXQ('@A')\">\n"+
"    <b xd:script=\"occurs *; create fromXQ('B')\"\n"+
"       x = \"required string; create fromXQ('@c')\"\n"+
"       y = \"required string; create fromXQ('@d')\"/>\n"+
"  </a>\n"+
" </xd:sequence>\n"+
"</root>\n"+
"</xd:def>";
			xml =
"<x>" +
"<a A=\"A\"><B c=\"c\" d=\"d\"/><B c=\"C\" d=\"D\"/></a>" +
"<a A=\"B\"><B c=\"e\" d=\"f\"/></a>" +
"</x>";
			el = create(xdef, "", "root", reporter, xml);
			assertNoErrors(reporter);
			assertEq(el,
				"<root>" +
				"<a a=\"A\"><b x=\"c\" y=\"d\"/><b x=\"C\" y=\"D\"/></a>" +
				"<a a=\"B\"><b x=\"e\" y=\"f\"/></a>" +
				"</root>");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"    script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces'\n"+
"    xmlns='N' xmlns:sod='N'>\n"+
"<a>\n"+
"    <e f=\"optional string; create xpath('\\'\\'')\"/>\n"+
"    <e f=\"string; create xpath('\\'2\\'')\"/>\n"+
"    <f xd:script=\"occurs *; create xpath('../e')\"/>\n"+
"    <x xd:script=\"occurs *; create xpath('../sod:e')\"/>\n"+
"    <g xd:script=\"occurs *; create xpath('../*:e')\"/>\n"+
"    <h xd:script=\"occurs *; create xpath('preceding-sibling::e')\"/>\n"+
"    <i xd:script=\"occurs *; create xpath('preceding-sibling::*:e')\"/>\n"+
"    <j xd:script=\"occurs *; create xpath('preceding-sibling::sod:e')\"/>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			el = xp.createXDDocument().xcreate(new QName("N", "a"), reporter);
			assertNoErrors(reporter);
			assertEq(
"<a xmlns=\"N\"><e/><e f=\"2\"/><x/><x/><g/><g/><i/><i/><j/><j/></a>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"    xmlns='N' xmlns:sod='N'\n"+
"    script='options ignoreAttrWhiteSpaces,ignoreTextWhiteSpaces'>\n"+
"<a>\n"+
"  <e f=\"fixed '1'\"/>\n"+
"  <e f=\"fixed '2'\"/>\n"+
"  <f xd:script=\"occurs *; create xpath('../sod:e')\"/>\n"+
"  <g xd:script=\"occurs *; create xpath('preceding-sibling::*:e')\"/>\n"+
"  <h xd:script=\"occurs *; create xpath('preceding-sibling::sod:e')\"/>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			el = xp.createXDDocument().xcreate(new QName("N", "a"), reporter);
			assertNoErrors(reporter);
			assertEq("<a xmlns='N'>" +
				"<e f='1'/><e f='2'/><f/><f/><g/><g/><h/><h/></a>", el);
		} catch (Exception ex) {fail(ex);}
		try {//test of xquery
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n"+
"   <a C='required num(1,9);'>\n"+
"      <O J='optional string(1,36);\n"+
"            finally setText(toString(xquery(\".\")) + \"d\");' />\n"+
"   </a>\n"+
"</xd:def>");
			xml = "<a C='5'><O J='Abc'/></a>";
			el = parse(xp, "", xml, reporter);
			assertEq("Abcd", ((Element) el.getElementsByTagName("O").
				item(0)).getAttribute("J"));
		} catch(Exception ex) {fail(ex);}
		try {
			xdef = // result of xquery (in boolean expression
"<xd:def xmlns:xd='" + _xdNS + "' root='a|b|c|d'>\n"+
"<a typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xquery('/a[@typ=1]')\"/>\n"+
"    <c xd:script=\"match xquery('/a[@typ=(2,3)]')\"/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"<b typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xquery('../@typ=&quot;1&quot;')\"/>\n"+
"    <c xd:script=\"match xquery('../@typ=2')\"/>\n"+
"  </xd:choice>\n"+
"</b>\n"+
"<c typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xquery('//c[@typ=&quot;1&quot;]')\"/>\n"+
"    <c xd:script=\"match xquery('//c[@typ=2]')\"/>\n"+
"  </xd:choice>\n"+
"</c>\n"+
"<d\n"+
"  a=\"optional int\"\n"+
"  b=\"optional int\"\n"+
"  xd:script=\"finally if (!(xquery('@a') XOR xpath('@b')))\n"+
"     error('EE', '@a, @b mus be excluzive');\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a typ='1'><b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a typ='2'><b/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='2'><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a typ='3'><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a typ='1'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='4'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<b typ='1'><b/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<b typ='2'><b/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<b typ='2'><c/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<b typ='1'><c/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<c typ='1'><b/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<c typ='2'><b/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<c typ='2'><c/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<c typ='1'><c/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<d a='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<d b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<d a='1' b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xml = "<d/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
		} catch(Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='"+ _xdNS + "'>\n"+
"\n"+
"  <mraveniste jmeno='string; create xquery(&apos;\n"+
"    for $i in (//mraveniste)\n"+
"      return $i[count(//hmyz[kasta/text() = \"vojak\" and\n"+
"                 @mraveniste = $i/@jmeno]) > 2]/@jmeno\n"+
"               &apos;)' />\n"+
"\n"+
"</xd:def>";

			xp = compile(xdef);
			xml =
"<les>\n"+
"\n"+
"  <zvirata>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"vedleJahody\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>kralovna</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"vedleJahody\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>samec</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"vedleJahody\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>samec</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"vedleJahody\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>vojak</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"podSmrkem\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>kralovna</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"podSmrkem\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>vojak</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"podSmrkem\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>vojak</kasta>\n"+
"    </hmyz>\n"+
"    <hmyz celed=\"mravencovití\"\n"+
"      mraveniste=\"podSmrkem\">\n"+
"      <rod>mravenec</rod>\n"+
"      <kasta>vojak</kasta>\n"+
"    </hmyz>\n"+
"  </zvirata>\n"+
"\n"+
"  <objekty>\n"+
"    <mraveniste jmeno=\"vedleJahody\" />\n"+
"    <mraveniste jmeno=\"podSmrkem\" />\n"+
"  </objekty>\n"+
"\n"+
"</les>";
			xd = xp.createXDDocument();
			xd.setXDContext(KXmlUtils.parseXml(xml).getDocumentElement());
			//mraveniste, kde jsou vic nez dva vojaci
			el = xd.xcreate("mraveniste", reporter);
			assertTrue("podSmrkem".equals(el.getAttribute("jmeno")),
				el.getAttribute("jmeno"));
		} catch (Exception ex) {fail(ex);}
	}

	@Override
	public void test() {
		if (!DefXQueryExpr.isXQueryImplementation()) {
			testNoSaxon();
		} else {
			testSaxon();
		}
		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}
