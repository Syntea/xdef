package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.StringParser;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Test of external utilities for key, keyRef and also sequence in choice.
 * @author Vaclav Trojan
 */
public final class TestKeyAndRef extends XDTester {

	public TestKeyAndRef() {super();}

	public static String boundQName(final XXData data) {
		String s = data.getTextValue();
		byte xmlVersion =
			"1.1".equals(data.getElement().getOwnerDocument().getXmlVersion())
			? (byte) 11 : (byte) 10;
		if (!StringParser.chkNCName(s, xmlVersion)) {
			return null;
		}
		Element e = data.getElement();
		for (;;) {
			if (e.hasAttribute("targetNamespace")) {
				return e.getAttribute("targetNamespace");
			}
			Node n = e.getParentNode();
			if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
				break;
			}
			e = (Element) n;
		}
		return null;
	}

	@Override
	public void test() {
		String xdef;
		String s;
		String xml;
		XDDocument xd;
		XDPool xp;
		final String dataDir = getDataDir() + "test/";
		final ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>uniqueSet u {x: int()}</xd:declaration>\n"+
"<a><b z='u.x.IDREFS'/><c x='u.x.ID' y='u.x.ID'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b z='1 2'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b z='1 3'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> type p QName(); uniqueSet u p;</xd:declaration>\n"+
"<a><b z='SET'> u.IDREFS </b><c x='u.ID' y='u.ID'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b z='x'>a b</b><c x='a' y='b'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b z='x'>a c</b><c x='a' y='b'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml = "<a><b z='x'>a c</b><c x='b' y='d'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration scope='local'>uniqueSet u {x: int()}</xd:declaration>\n"+
"<a><b x='u.x.ID' y='u.x.ID'/><c z='u.x.CHKIDS'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b x='1' y='2'/><c z='1 2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b x='1' y='2'/><c z='1 3'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			// uniqueSet declared as variable of model.
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A>\n"+
"  <a xd:script='var uniqueSet v {x:int()}; occurs *'>\n"+
"    <b x='v.x.ID()' y='v.x.ID()'/>\n"+
"    <c z='v.x.IDREFS();'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='3'/><c z='2 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertEq(2, reporter.getErrorCount());
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt; y : optional flt;}\n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: u.x}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml ="<A><b a='3.1'/><c a='4.1'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration> uniqueSet u int(); </xd:declaration>\n"+
"<A>\n"+
"  <a xd:script='?' a='u()'/>\n"+
"  <b xd:script='+' a='u.ID()'/>\n"+
"  <c xd:script='?' a='u.IDREF()'/>\n"+
"  <d xd:script='?' a='u.CHKID()'/>\n"+
"  <e xd:script='?' a='u.SET()'/>\n"+
"</A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A><a a='2'/><b a='1'/><c a='1'/><d a='1'/><e a='3'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a a='2'/><b a='1'/><c a='2'/><d a='3'/><e a='4'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  uniqueSet u{x: int();}\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <a xd:script='?' a='u.x()'/>\n"+
"  <b xd:script='+' a='u.x.ID()'/>\n"+
"  <c xd:script='?' a='u.x.IDREF()'/>\n"+
"  <d xd:script='?' a='u.x.CHKID()'/>\n"+
"  <e xd:script='?' a='u.x.SET()'/>\n"+
"</A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A><a a='2'/><b a='1'/><c a='1'/><d a='1'/><e a='3'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a a='2'/><b a='1'/><c a='2'/><d a='3'/><e a='4'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 2
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef = // test ID in onStartElement
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
" <xd:declaration scope=\"local\">\n" +
"   uniqueSet tabulka {sloupec: string(1,3)}; \n" +
" </xd:declaration>\n" +
" <A>\n" +
"    <B xd:script=\"*; onStartElement tabulka.ID()\"\n" +
"        b=\"tabulka.sloupec()\"/>\n" +
" </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B b='S-A'/><B b='S-B'/><B b='S-D'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><B b='S-A'/><B b='S-B'/><B b='S-A'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF523"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt} \n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: flt()}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><b a='3.1'/><b a='3.1'/><c a='4.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 3, reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type flt float(1,6);\n"+
"  uniqueSet u {x: flt; y : ? flt;}\n"+
"</xd:declaration>\n"+
"<A xd:script='var uniqueSet v {x: flt}'>\n"+
"  <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"  <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><b a='3.1'/><b a='3.1'/><c a='4.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 3, reporter.printToString());

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A><a xd:script='occurs *; ref a;'/></A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3); y: int()}'>\n"+
"    <b xd:script='+; finally v.ID' x='v.x' y='v.y'/>\n"+
"    <c x='v.x'> v.y.IDREFS </c>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><b x='1' y='3'/><c x='1'>2 3</c></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c x='1'>1 3</c></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A xd:script='var {uniqueSet v {p: int(1,3); q: optional string};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID()' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF()' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='3' y='a'/><c x='3' y='a'/></a>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>uniqueSet v {x: int(1,3)}</xd:declaration>\n"+
"  <A><a xd:script='occurs *; ref a;'/></A>\n"+
"  <a xd:script='finally v.CLEAR()'>\n"+
"    <b x='v.x.ID' y='v.x.ID'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A><a xd:script='occurs *; ref a;'/></A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3)}'>\n"+
"    <b x='v.x.ID' y='v.x.ID'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>uniqueSet v {x:int(1,3)}</xd:declaration>\n"+
"  <A><a xd:script='occurs *; ref a;'/></A>\n"+
"  <a xd:script='var uniqueSet v {x: int(1,3)}'>\n"+
"    <b x='v.x.ID' y='v.x.ID()'/>\n"+
"    <c z='v.x.IDREFS'/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xml =
"<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings()
				&& "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A xd:script='var {type i int(1,3);uniqueSet v {p:i;q:? string(1,9)};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='3' y='a'/><c x='3' y='a'/></a>"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<A xd:script='var{type i int(1,3);uniqueSet v{p:i;q:? string(1,9)};}'>\n"+
"  <a xd:script='occurs *;'>\n"+
"    <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"    <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"  </a>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='3' y='a'/><c x='3' y='a'/></a>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1
				&& "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration scope='local'>\n"+
"  type code string(3);\n"+
"  uniqueSet FileCode {parse: code();}\n"+
"  uniqueSet FileParam {Code: FileCode.parse.ID(); Param: string(1,10);}\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <FileType xd:script='occurs 1..'\n"+
"      FileCode='required FileParam.Code()'>\n"+
"    <Param xd:script='occurs 1..'\n"+
"      ParamName='required FileParam.Param.ID()'/>\n"+
"  </FileType>\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n"+
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>\n"+
"  <FileType FileCode='ABC'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<A>\n"+
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v1'/>\n"+
"    <Param ParamName='v2'/>\n"+
"  </FileType>" +
"  <FileType FileCode='XYZ'>\n"+
"    <Param ParamName='v3'/>\n"+
"    <Param ParamName='v4'/>\n"+
"  </FileType>\n"+
"</A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.printToString().indexOf("XDEF523") > 0,
				"Error not reported; " + reporter.printToString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"<xd:declaration scope='local'>\n"+
" /* tabulka slozenych klicu */\n"+
" uniqueSet addr {country:string;town:string;street:string;house: int()};\n"+
"</xd:declaration>\n"+
"<Country name=\"addr.country()\">\n"+
"  <Town xd:script = \"occurs +\" name=\" addr.town()\">\n"+
"    <Street xd:script = \"occurs +\" name=\" addr.street()\">\n"+
"      <House xd:script = \"occurs +\" number=\"addr.house.ID()\"/> \n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address xd:script = \"occurs *; finally addr.IDREF();\"\n"+
"    Country=\"addr.country()\"\n"+
"    Town=\"addr.town()\"\n"+
"    Street=\"addr.street()\"\n"+
"    House=\"? addr.house()\" />\n"+
"</Country>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Country name=\"CS\">\n"+
"  <Town name=\"Praha\">\n"+
"    <Street name=\"Dlouhá\">\n"+
"      <House number=\"1\"/> \n"+
"      <House number=\"3\"/> \n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouhá\" House=\"1\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouhá\" House=\"3\" />\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<Country name=\"CS\">\n"+
"  <Town name=\"Praha\">\n"+
"    <Street name=\"Dlouhá\">\n"+
"      <House number=\"1\"/> \n"+
"      <House number=\"3\"/> \n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouhá\" House=\"1\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouhá\" House=\"3\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouhá\" House=\"5\" />\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Country/Address[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"<xd:declaration scope='local'>\n"+
" /* tabulka slozenych klicu */\n"+
" uniqueSet town {country: string; name: string};\n"+
" uniqueSet addr {country: town.country;\n"+
"					town: town.name;\n"+
"					street: string;\n"+
"					house:?int()};\n"+
"</xd:declaration>\n"+
"<Country name=\"addr.country(town.country())\">\n"+
"  <Town xd:script=\"occurs +\" name=\"addr.town(town.name.SET());\">\n"+
"    <Street xd:script = \"occurs +\" name=\"addr.street.SET()\">\n"+
"      <House xd:script = \"occurs *\" number=\"addr.house.ID()\"/> \n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address xd:script = \"occurs *; finally addr.IDREF();\"\n"+
"    Country=\"addr.country()\"\n"+
"    Town=\"addr.town()\"\n"+
"    Street=\"addr.street()\"\n"+
"    House=\"? addr.house()\" />\n"+
"  <Locality xd:script=\"occurs *; finally town.IDREF();\"\n"+
"    Country=\"town.country()\"\n"+
"    Town=\"town.name()\"/>\n"+
"</Country>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Country name=\"CS\">\n"+
"  <Town name=\"Praha\">\n"+
"    <Street name=\"Dlouha\">\n"+
"      <House number=\"1\"/> \n"+
"      <House number=\"3\"/> \n"+
"    </Street>\n"+
"    <Street name=\"Kratka\"/>\n"+
"  </Town>\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Kratka\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"1\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"3\" />\n"+
"  <Locality Country=\"CS\" Town=\"Praha\" />\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<Country name=\"CS\">\n"+
"  <Town name=\"Praha\">\n"+
"    <Street name=\"Dlouha\">\n"+
"      <House number=\"1\"/> \n"+
"      <House number=\"3\"/> \n"+
"    </Street>\n"+
"    <Street name=\"Kratka\"/>\n"+
"  </Town>\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Kratka\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"1\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"3\" />\n"+
"  <Locality Country=\"CS\" Town=\"Olomouc\" />\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Country/Locality[1]") > 1, s);
			xml =
"<Country name=\"CS\">\n"+
"  <Town name=\"Praha\">\n"+
"    <Street name=\"Dlouha\">\n"+
"      <House number=\"1\"/> \n"+
"      <House number=\"3\"/> \n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"1\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"3\" />\n"+
"  <Address Country=\"CS\" Town=\"Praha\" Street=\"Dlouha\" House=\"5\" />\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1
				&& s.indexOf("/Country/Address[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"<xd:declaration scope='local'>\n"+
"   /* tabulka slozenych klicu */\n"+
"   uniqueSet lokalita {country:string; town: ? string;};\n"+
"   uniqueSet addr {country: lokalita.country.SET;\n"+
"                     town: lokalita.town.SET;\n"+
"                     street:string; house:int()};\n"+
"</xd:declaration>\n"+
"<Country name=\"addr.country()\">\n"+
"  <Town xd:script = \"occurs +\" name=\" addr.town()\">\n"+
"    <Street xd:script = \"occurs +\" name=\" addr.street()\">\n"+
"      <House xd:script = \"occurs +\" number=\"addr.house.ID()\"/>\n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address xd:script = \"occurs *; finally addr.IDREF();\"\n"+
"    Country=\"addr.country()\"\n"+
"    Town=\"addr.town()\"\n"+
"    Street=\"addr.street()\"\n"+
"    House=\"addr.house()\"/>\n"+
"  <Locality xd:script = \"occurs *; finally lokalita.IDREF();\"\n"+
"    Country=\"lokalita.country()\"\n"+
"    Town=\"? lokalita.town()\"/>\n"+
"</Country>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Country name=\"CR\">\n"+
"  <Town name='Praha'>\n"+
"    <Street name='Dlouha'>\n"+
"      <House number='1'/>\n"+
"      <House number='3'/>\n"+
"    </Street>\n"+
"    <Street name='Kratka'>\n"+
"      <House number='2'/>\n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address Country='CR' Town='Praha' Street='Dlouha' House='1'/>\n"+
"  <Address Country='CR' Town='Praha' Street='Dlouha' House='3'/>\n"+
"  <Address Country='CR' Town='Praha' Street='Kratka' House='2'/>\n"+
"  <Locality Country='CR'/>\n"+
"  <Locality Country='CR' Town='Praha'/>\n"+
"</Country>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { // test a.d.NEWKEY()
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally a.ID()' a='a.d(); finally a.d.NEWKEY()' >\n"+
"    <b b='a.d.IDREF();' />\n"+
"  </a>\n"+
"  <xd:declaration>\n"+
"    uniqueSet a {d: int()}\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a a='5'><b b='5'/></a>";
			parse(xp, "", xml, reporter);
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF522") > 0);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <Town xd:script='occurs 0..*;'\n"+
"        name='addr.town()'>\n"+
"        <Street xd:script='occurs 0..*'\n"+
"          name='addr.street()'>\n"+
"          <House xd:script='occurs 0..*'\n"+
"            number='addr.house.ID()'/>\n"+
"        </Street>\n"+
"      </Town>\n"+
"      <Village xd:script='occurs 0..*;'\n"+
"        name='addr.town()'>\n"+
"        <House xd:script='occurs 0..*; init addr.street.NEWKEY();'\n"+
"          number='addr.house.ID();'/>\n"+
"      </Village>\n"+
"    </xd:mixed>\n"+
"    <Address xd:script='occurs 0..*; finally addr.IDREF()'\n"+
"       town='addr.town()'\n"+
"       street='optional addr.street();onAbsence addr.street.NEWKEY();'\n"+
"       number='addr.house()' />\n"+
"  </a>\n"+
"\n"+
"	<xd:declaration scope='local'>\n"+
"		uniqueSet addr {town: string(); street: ? string(); house: int();}\n"+
"	</xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<a>\n"+
"  <Town name='Praha'>\n"+
"    <Street name='Dlouha'>\n"+
"      <House number='1'/>\n"+
"      <House number='3'/>\n"+
"    </Street>\n"+
"    <Street name='Kratka'>\n"+
"      <House number='2'/>\n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Village name='Lhota'>\n"+
"    <House number='1'/>\n"+
"    <House number='2'/>\n"+
"  </Village>\n"+
"  <Address town='Praha' street='Kratka' number='2'/>\n"+
"  <Address town='Praha' street='Dlouha' number='3'/>\n"+
"  <Address town='Praha' street='Dlouha' number='1'/>\n"+
"  <Address town='Lhota' number='2'/>\n"+
"  <Address town='Lhota' number='1'/>\n"+
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<a>\n"+
"  <Town name='Praha'>\n"+
"    <Street name='Kratka'>\n"+
"      <House number='1'/>\n"+
"    </Street>\n"+
"  </Town>\n"+
"  <Address town='Praha' street='Kratka' number='1'/>\n"+
"  <Address town='Praha' street='Kratka' number='2'/>\n"+
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 1 &&
				s.indexOf("xpath=/a/Address[2]") > 0, s);
		} catch (Exception ex) {fail(ex);}
		try {
			xp = compile(dataDir + "TestKeyAndRef2.xdef");
			assertEq(dataDir + "TestKeyAndRef2.xml",
				parse(xp, "", dataDir + "TestKeyAndRef2.xml",reporter));
			assertNoErrors(reporter);
			xp = compile(dataDir + "TestKeyAndRef3.xdef");
			assertEq(dataDir + "TestKeyAndRef3.xml",
				parse(xp, "", dataDir + "TestKeyAndRef3.xml", reporter));
			assertNoErrors(reporter);
			assertEq(dataDir + "TestKeyAndRef3_1.xml",
				parse(xp, "", dataDir + "TestKeyAndRef3_1.xml", reporter));
			assertNoErrors(reporter);
			xp = compile(dataDir + "TestKeyAndRef4.xdef");
			assertEq(dataDir + "TestKeyAndRef4.xml",
				parse(xp, "",dataDir + "TestKeyAndRef4.xml", reporter));
			assertNoErrors(reporter);
			parse(xp, "", dataDir + "TestKeyAndRef4_1.xml" , reporter);
			assertTrue(reporter.getErrorCount()==1
				&& reporter.printToString().indexOf("XDEF522")>0,
				"Error Not recognized; " + reporter.printToString());
			parse(xp, "", dataDir + "TestKeyAndRef4_2.xml", reporter);
			assertTrue(reporter.getErrorCount()==1
				&& reporter.printToString().indexOf("XDEF522")>0,
				"Error Not recognized; " + reporter.printToString());
			xp = compile(dataDir + "TestKeyAndRef5.xdef");
			assertEq(dataDir + "TestKeyAndRef5.xml",
				parse(xp, "", dataDir + "TestKeyAndRef5.xml",reporter));
			assertNoErrors(reporter);
			xp = compile(dataDir + "TestKeyAndRef6.xdef");
			assertEq(dataDir + "TestKeyAndRef6.xml",
				parse(xp, "" , dataDir + "TestKeyAndRef6.xml", reporter));
			assertNoErrors(reporter);
			setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
			setProperty(XDConstants.XDPROPERTY_SPECDATES, null);
			xp = compile(dataDir + "TestKeyAndRef7.xdef");
			assertEq(dataDir + "TestKeyAndRef7.xml",
				parse(xp, "Mondial" , dataDir + "TestKeyAndRef7.xml",reporter));
			assertNoErrors(reporter);
			xdef = // test CHIID
"<xd:def xmlns:xd='" + _xdNS + "' root='A' >\n" +
" <xd:declaration> uniqueSet s int(); </xd:declaration>\n" +
" <A><a xd:script='*' a='s.ID()'/><b xd:script='*' a='s.CHKID()'/></A>\n" +
"</xd:def>";
			xml =
"<A>\n" +
"   <a a='1'/>\n" +
"   <b a='1'/>\n" +
"   <b a='2'/>\n" + // must be error
"   <b a='2'/>\n" + // must be error
"</A>";
			parse(xdef, "", xml, reporter);
			assertEq(2, reporter.getErrorCount(), reporter);
			xdef = // test CHIID
"<xd:def xmlns:xd='" + _xdNS + "' root='A' >\n" +
"<xd:declaration>\n" +
" type a int(); type b string(); uniqueSet s2 {a: a(); b: b()};\n" +
"</xd:declaration>\n" +
" <A>\n" +
"   <a xd:script='*' a='s2.a()'>\n" +
"     <b xd:script='*; finally s2.ID();' b='s2.b()' /> \n" +
"   </a>\n" +
"   <b xd:script='*' a='s2.a()'>\n" +
"     <c xd:script='*; finally s2.CHKID()' b='s2.b()' />\n" +
"   </b>\n" +
" </A>\n" +
"</xd:def>";
			xml =
"<A>\n" +
"   <a a='1'><b b='B1'/></a>\n" +
"   <b a='1'>\n" +
"     <c b='B1'/>\n" +
"     <c b='B3'/>\n" + // must be error
"     <c b='B3'/>\n" + // must be error
"   </b>\n" +
" </A>";
			parse(xdef, "", xml, reporter);
			assertEq(2, reporter.getErrorCount(), reporter);
			xdef = //test CHIID
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<xd:declaration> uniqueSet u {a: string();} </xd:declaration>\n" +
"<A><a xd:script='*' b='u.a.ID()'/><b xd:script='*' b='u.a.CHKID()'/></A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n" +
"  <a b='1'/>\n" +
"  <b b='1'/>\n" +
"  <b b='2'/>\n" + // must be error
"  <b b='2'/>\n" + // must be error
"</A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("/A/b[2]/@b")
				&& s.contains("/A/b[3]/@b"), reporter);
			xdef = //test CHIID-
"<xd:def xmlns:xd='" + _xdNS + "' root='Test'>\n" +
" <xd:declaration>\n" +
"    type at   int();\n" +
"    type bt   string();\n" +
"    type ct   enum('Y','N');\n" +
"    uniqueSet s3 {a: at(); b: bt(); c: string()};\n" + // c accepts anything
"    boolean x() {return ct() AND s3.c();}\n"+
" </xd:declaration>\n" +
" <Test> \n" +
"   <A xd:script=\"*\" a=\"s3.a()\">\n" +
"     <B xd:script=\"*; ref B; finally s3.ID()\"/>\n" +
"   </A>\n" +
"   <uA xd:script=\"*\" a=\"s3.a()\">\n" +
"     <uB xd:script=\"*; finally s3.CHKID()\" \n" +
"          b=\"s3.b()\"\n" +
"          c=\"x()\"/>\n" +
"   </uA>\n" +
" </Test>\n" +
" <B b=\"s3.b()\" c=\"s3.c()\"/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<Test>\n" +
"   <A a=\"1\">\n" +
"     <B b=\"B1\" c=\"Y\"/>\n" +
"     <B b=\"B2\" c=\"N\"/>\n" +
"   </A>\n" +
"   <uA a=\"1\">\n" +
"     <uB b=\"B1\" c=\"Y\"/>\n" +
"     <uB b=\"B2\" c=\"1\"/>\n" + // here is incorrect type (and no reference)
"   </uA>\n" +
" </Test>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF522")
				&& s.contains("/Test/uA[1]/uB[1]")
				&& s.contains("XDEF809") && s.contains("/Test/uA[1]/uB[2]"),
				reporter);
		} catch (Exception ex) {fail(ex);}
		try { // check xdType
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet u {a: string();}\n" +
"  </xd:declaration>\n" +
"  <A xd:script='var Parser x = null;'>\n" +
"    <B b='xdType(); onTrue x = getParsedValue();'/>\n" +
"    <C c='x()'/>\n" + // parser is x
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml ="<A><B b='int()'/><C c='99'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml ="<A><B b='int'/><C c='abc'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 1
				&& reporter.printToString().contains("XDEF809"), reporter);
			xml ="<A><B b='int(1,2)'/><C c='1'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml ="<A><B b='int(1,2)'/><C c='0'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 1
				&& reporter.printToString().contains("XDEF813"), reporter);
			xml ="<A><B b='int(1,2)'/><C c='3'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 1
				&& reporter.printToString().contains("XDEF813"), reporter);
			xml ="<A><B b='ynt(1,2)'/><C c='3'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817")
				&&  s.contains("XDEF820"), reporter);
			xml ="<A><B b='xdatetime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml ="<A><B b='xdatetime(\"dd.MM.yyyy\")'/><C c='01.02'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 1
				&& reporter.printToString().contains("XDEF809"), reporter);
			xml = "<A><B b='xdattime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817")
				&&  s.contains("XDEF820"), reporter);
			String propwarning = getProperties().getProperty(
				XDConstants.XDPROPERTY_WARNINGS);
			setProperty(XDConstants.XDPROPERTY_WARNINGS,
				XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
			xml ="<A><B b='datetime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>";
			parse(xp, "", xml, reporter); // datetime must be xdatetime
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817")
				&&  s.contains("XDEF820"), reporter);
			setProperty(XDConstants.XDPROPERTY_WARNINGS, propwarning);
// test uniqueSet setValue, getValoue
// and order of attribute processing in X-definition
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <xd:declaration>uniqueSet u {a: string(); var Parser x}</xd:declaration>\n" +
"  <A>\n" +
"    <DefParams>\n" +
"       <Param xd:script='*;'\n" +
"          Name='u.a.ID();'\n" +
"          Type='xdType(); onTrue u.x=getParsedValue()' />\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script='*;' Name='u.a.CHKID();' Value='u.x'/>\n" +
"    </Params>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>\n" +
"  <DefParams>\n" +
"    <Param Name=\"name\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"xxx\"/>\n" +
"    <Param Name=\"birthday\" Type=\"xdatetime('dd.MM.yyyy')\"/>\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"name\" Value=\"John\"/>\n" +
"    <Param Name=\"xxx\" Value=\"184.8\"/>\n" +
"    <Param Name=\"birthday\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"1.8a\" Name=\"xxx\"/>\n" +
"  </Params>\n" +
"</A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524")
				&& s.contains("birthday") && s.contains("name"),reporter);
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='a'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet u {var Parser x, int y; a: string(); var String z}\n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <DefParams>\n" +
"       <Param xd:script=\"init out(u.z==null);*;finally out(','+u.z+',')\"\n" +
"          Name=\"u.a.ID(); onTrue u.z='x'; onFalse u.z='y';\"\n" +
"          Type=\"xdType(); onTrue u.x=getParsedValue();\n" +
"                          onFalse u.y=99;\n" +
"                          finally out(u.y)\"/>\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script=\"*;\"\n" +
"              Name=\"u.a.CHKID();\"\n" +
"              Value=\"u.x; onTrue out(u.x + ','); \"/>\n" +
"    </Params>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"xdatetime('dd.MM.yyyy')\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"14.8a\" Name=\"Vyska\"/>\n" +
"  </Params>\n" +
"</a>";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("true,x,false,x,false,x,string,dec,xdatetime,",
				strw.toString());
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524")
				&& s.contains("DatumNarozeni") && s.contains("Jmeno"),
				reporter);
			xdef = // run parse twice
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <xd:declaration>uniqueSet u{s: ? string; e: ? string};</xd:declaration>\n" +
"  <a>\n" +
"    <b N=\"enum('A')\"><E xd:script='ref E' N=\"enum('B')\" /></b>\n" +
"    <b N='u.s'><E xd:script='1..; ref E'/></b>\n" +
"  </a>\n" +
"  <E xd:script='finally u.ID()' N='u.e'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a><b N='A'><E N='B'/></b><b N='x'><E N='T'/></b></a>";
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			xd = xp.createXDDocument(); // parse again
			parse(xd, xml, reporter);
			assertNoErrors(reporter); // uniqeue set must be clear!
			parse(xd, xml, reporter);
			assertNoErrors(reporter); // even here uniqeue set must be clear!
			// method uniqueSet.toContainer()
			xdef = // explicit variant
"<xd:def xmlns:xd='" + _xdNS + "' root='List'>\n" +
"<xd:declaration>\n" +
"   Container c;\n" +
"   int i=0;\n" +
"   uniqueSet members {room: string()};\n" +
"</xd:declaration> \n" +
"<List xd:script='finally c = members.toContainer();'>\n" +
"   <Member xd:script='occurs +'\n" +
"     Name='string()'\n" +
"     Room='members.room.SET()'/>\n" +
"</List>\n" +
"<School xd:script='var String s;'>\n" +
"  <Group xd:script='*; create c.getLength()'\n" +
"    Room='string(); create\n" +
"             s = ((Container)c.item(i++)).getNamedString(\"room\");'>\n" +
"    <Student xd:script=\"+; create xpath('//Member[@Room=\\''+s+'\\']');\"\n" +
"      Name='string()'/> \n" +
"  </Group>\n" +
"</School>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = // source
"<List>\n" +
"   <Member Name='Smith' Room='A'/>\n" +
"   <Member Name='Bush' Room='B'/>\n" +
"   <Member Name='Bloch' Room=\"A\"/>\n" +
"</List>";
			s = // result
"<School>" +
"<Group Room='A'><Student Name='Smith'/><Student Name='Bloch'/></Group>" +
"<Group Room='B'><Student Name='Bush'/></Group>" +
"</School>";
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(create(xd, "School", reporter), s);
			assertNoErrors(reporter);
			xdef = // toContainer() - variant with container as context
"<xd:def xmlns:xd='" + _xdNS + "' root='List' >\n" +
"<xd:declaration>\n" +
"   uniqueSet members {room: string()};\n" +
"</xd:declaration>\n" +
"<List xd:script='finally {returnElement(xcreate(\"School\"))}'>\n" +
"   <Member xd:script='occurs +'\n" +
"     Name='string()'\n" +
"     Room='members.room.SET()'/>\n" +
"</List>\n" +
"<School>\n" +
"  <Group xd:script='*; var String s; create members.toContainer();'\n" +
"    Room='string(); create s=from(\"@room\");'>\n" +
"    <Student xd:script=\"+; create xpath('//Member[@Room=\\''+s+'\\']');\"\n" +
"      Name='string()'/> \n" +
"  </Group>\n" +
"</School>\n" +
"</xd:def>";
			assertEq(parse(compile(xdef).createXDDocument(), xml, reporter), s);
			assertNoErrors(reporter);
			xdef = // test reporting iof incomplete key items,
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' xd:root='T'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet r {a: string(1,2); b: string(1,2)};\n" +
"  </xd:declaration>\n" +
"  <T>\n" +
"    <R xd:script='*; finally r.ID()' A='r.a' B='r.b'/>\n" +
"  </T>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<T>\n" +
"  <R A='xx' B='aaa'/>\n" +
"  <R A='xxx' B='aa'/>\n" +
"  <R A='xxx' B='aaa'/>\n" +
"  <R A='xx' B='aa'/>\n" +
"</T>";
			parse(xd, xml, reporter);
			s = reporter.printToString();
			assertTrue(s.contains(" \"a\")") && s.contains(" \"b\")")
				&& s.contains(" \"a\", \"b\"")&&reporter.getErrorCount()==7,s);
		} catch (Exception ex) {fail(ex);}

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