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
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

/** Test of external utilities for key, keyRef and also sequence in choice.
 * @author Vaclav Trojan
 */
public final class TestKeyAndRef extends XDTester {

	public TestKeyAndRef() {super();}

	public static String boundQName(final XXData data) {
		String s = data.getTextValue();
		byte xmlVersion =
			"1.1".equals(data.getElement().getOwnerDocument().getXmlVersion()) ? (byte) 11 : (byte) 10;
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
		StringWriter swr;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>uniqueSet u {x: int()}</xd:declaration>\n"+
"  <a><b z='u.x.IDREFS'/><c x='u.x.ID' y='u.x.ID'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b z='1 2'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b z='1 3'/><c x='1' y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>type p QName(); uniqueSet u p;</xd:declaration>\n"+
"  <a><b z='SET'> u.IDREFS </b><c x='u.ID' y='u.ID'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b z='x'>a b</b><c x='a' y='b'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b z='x'>a c</b><c x='a' y='b'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xml = "<a><b z='x'>a c</b><c x='b' y='d'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 2 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='local'>uniqueSet u {x: int()}</xd:declaration>\n"+
"  <a><b x='u.x.ID' y='u.x.ID'/><c z='u.x.CHKIDS'/></a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a><b x='1' y='2'/><c z='1 2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b x='1' y='2'/><c z='1 3'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings() && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			// uniqueSet declared as variable of model.
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A>\n"+
"    <a xd:script='var uniqueSet v {x:int()}; occurs *'>\n"+
"      <b x='v.x.ID()' y='v.x.ID()'/>\n"+
"      <c z='v.x.IDREFS();'/>\n"+
"    </a>\n"+
"  </A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 1 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='3'/><c z='2 3'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertEq(2, reporter.getErrorCount());
			assertTrue(reporter.getErrorCount() == 2 && "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()), reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type flt float(1,6);\n"+
"    uniqueSet u {x: flt; y : optional flt;}\n"+
"  </xd:declaration>\n"+
"  <A xd:script='var uniqueSet v {x: u.x}'>\n"+
"    <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"    <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml ="<A><b a='3.1'/><c a='4.1'/></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.getErrorCount() == 2 && "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()), reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>uniqueSet u int();</xd:declaration>\n"+
"  <A>\n"+
"    <a xd:script='?' a='u()'/>\n"+
"    <b xd:script='+' a='u.ID()'/>\n"+
"    <c xd:script='?' a='u.IDREF()'/>\n"+
"    <d xd:script='?' a='u.CHKID()'/>\n"+
"    <e xd:script='?' a='u.SET()'/>\n"+
"  </A>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<A><a a='2'/><b a='1'/><c a='1'/><d a='1'/><e a='3'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><a a='2'/><b a='1'/><c a='2'/><d a='3'/><e a='4'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 2 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef = // test ID in onStartElement
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration scope=\"local\"> uniqueSet tabulka {sloupec: string(1,3)}; </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script=\"*; onStartElement tabulka.ID()\"\n" +
"        b=\"tabulka.sloupec()\"/>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B b='S-A'/><B b='S-B'/><B b='S-D'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><B b='S-A'/><B b='S-B'/><B b='S-A'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF523"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type flt float(1,6);\n"+
"    uniqueSet u {x: flt} \n"+
"  </xd:declaration>\n"+
"  <A xd:script='var uniqueSet v {x: flt()}'>\n"+
"    <b xd:script='+' a='v.x.ID(u.x.ID())'/>\n"+
"    <c xd:script='+' a='v.x.IDREF(u.x.IDREF())'/>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><b a='3.1'/><c a='3.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><b a='3.1'/><b a='3.1'/><c a='4.1'/></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.getErrorCount() == 3, reporter);
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
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><b x='1' y='3'/><c x='1'>2 3</c></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><c x='1'>1 3</c></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings() && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
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
			assertNoErrorwarnings(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1 && "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration scope='local'>uniqueSet v {x: int(1,3)}</xd:declaration>\n"+
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
			xml = "<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>", reporter);
			assertTrue(reporter.getErrorCount()==1 && "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			parse(xp,
				"", "<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>",reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings() && "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
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
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 2'/></a><a><b x='1' y='2'/><c z='1 2'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a></A>";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errorWarnings()&&"XDEF522".equals(reporter.getReport().getMsgID()),reporter);
			xml = "<A><a><b x='1' y='2'/><c z='1 3'/></a><a><b x='1' y='4'/><c z='2 4'/></a></A>";
			parse(xp, "", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			assertTrue(reporter.errorWarnings() && "XDEF522".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF813".equals(reporter.getReport().getMsgID())
				&& "XDEF522".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A xd:script='var {type i int(1,3);uniqueSet v {p:i;q:? string(1,9)};}'>\n"+
"    <a xd:script='occurs *;'>\n"+
"      <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"      <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"    </a>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='3' y='a'/><c x='3' y='a'/></a>"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<A>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+
"<a><b x='1' y='a'/><c x='1' y='a'/></a>"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1 && "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A xd:script='var{type i int(1,3);uniqueSet v{p:i;q:? string(1,9)};}'>\n"+
"    <a xd:script='occurs *;'>\n"+
"      <b xd:script='finally v.ID' x='v.p()' y='v.q()'/>\n"+
"      <c xd:script='finally v.IDREF' x='v.p()' y='v.q()'/>\n"+
"    </a>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><a><b x='1' y='a'/><c x='1' y='a'/></a><a><b x='3' y='a'/><c x='3' y='a'/></a></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<A>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+
"  <a><b x='1' y='a'/><c x='1' y='a'/></a>\n"+ //must be unique
"</A>";
			parse(xp, "", xml, reporter); // NULOVAT??? XDPool ????
			assertTrue(reporter.getErrorCount()==1 && "XDEF523".equals(reporter.getReport().getMsgID()),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type code string(3);\n"+
"    uniqueSet FileCode {parse: code();}\n"+
"    uniqueSet FileParam {Code: FileCode.parse.ID(); Param: string(1,10);}\n"+
"  </xd:declaration>\n"+
"  <A>\n"+
"    <FileType xd:script='+' FileCode='required FileParam.Code()'>\n"+
"      <Param xd:script='+' ParamName='required FileParam.Param.ID()'/>\n"+
"    </FileType>\n"+
"  </A>\n"+
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
			assertNoErrorwarnings(reporter);
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
			assertTrue(reporter.printToString().indexOf("XDEF523") > 0, "Error not reported; " + reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"  <xd:declaration scope='local'>\n"+
"   /* table of compound keys */\n"+
"   uniqueSet addr {country:string;town:string;street:string;house: int()};\n"+
"  </xd:declaration>\n"+
"  <Country name=\"addr.country()\">\n"+
"    <Town xd:script = \"occurs +\" name=\" addr.town()\">\n"+
"      <Street xd:script = \"occurs +\" name=\" addr.street()\">\n"+
"        <House xd:script = \"occurs +\" number=\"addr.house.ID()\"/> \n"+
"      </Street>\n"+
"    </Town>\n"+
"    <Address xd:script = \"occurs *; finally addr.IDREF();\"\n"+
"      Country=\"addr.country()\"\n"+
"      Town=\"addr.town()\"\n"+
"      Street=\"addr.street()\"\n"+
"      House=\"? addr.house()\" />\n"+
"  </Country>\n"+
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
			assertNoErrorwarnings(reporter);
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
			assertTrue(reporter.getErrorCount() == 1 && s.indexOf("/Country/Address[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"  <xd:declaration scope='local'>\n"+
"   /* table of compound keys */\n"+
"   uniqueSet town {country: string; name: string};\n"+
"   uniqueSet addr {country: town.country; town: town.name; street: string; house: ? int()};\n"+
"  </xd:declaration>\n"+
  "<Country name=\"addr.country(town.country())\">\n"+
"    <Town xd:script=\"occurs +\" name=\"addr.town(town.name.SET());\">\n"+
"      <Street xd:script = \"occurs +\" name=\"addr.street.SET()\">\n"+
"        <House xd:script = \"occurs *\" number=\"addr.house.ID()\"/> \n"+
"      </Street>\n"+
"    </Town>\n"+
"    <Address xd:script = \"occurs *; finally addr.IDREF();\"\n"+
"      Country=\"addr.country()\"\n"+
"      Town=\"addr.town()\"\n"+
"      Street=\"addr.street()\"\n"+
"      House=\"? addr.house()\" />\n"+
"    <Locality xd:script=\"occurs *; finally town.IDREF();\"\n"+
"      Country=\"town.country()\"\n"+
"      Town=\"town.name()\"/>\n"+
"  </Country>\n"+
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
			assertNoErrorwarnings(reporter);
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
			assertTrue(reporter.getErrorCount() == 1 && s.indexOf("/Country/Locality[1]") > 1, s);
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
			assertTrue(reporter.getErrorCount() == 1 && s.indexOf("/Country/Address[3]") > 1, s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Country'>\n"+
"  <xd:declaration scope='local'>\n"+
"    /* table of compound keys */\n"+
"    uniqueSet lokalita {country:string; town: ? string;};\n"+
"    uniqueSet addr {country:lokalita.country.SET; town:lokalita.town.SET; street:string; house:int()};\n"+
"  </xd:declaration>\n"+
"  <Country name=\"addr.country()\">\n"+
"    <Town xd:script = 'occurs +' name=' addr.town()'>\n"+
"      <Street xd:script = 'occurs +' name='addr.street()'>\n"+
"        <House xd:script = 'occurs +' number='addr.house.ID()'/>\n"+
"      </Street>\n"+
"    </Town>\n"+
"    <Address xd:script = 'occurs *; finally addr.IDREF();'\n"+
"      Country='addr.country()'\n"+
"      Town='addr.town()'\n"+
"      Street='addr.street()'\n"+
"      House='addr.house()'/>\n"+
"    <Locality xd:script = 'occurs *; finally lokalita.IDREF();'\n"+
"      Country='lokalita.country()'\n"+
"      Town='? lokalita.town()'/>\n"+
"  </Country>\n"+
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
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
		try { // test a.d.NEWKEY()
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally a.ID()' a='a.d(); finally a.d.NEWKEY()' >\n"+
"    <b b='a.d.IDREF();' />\n"+
"  </a>\n"+
"  <xd:declaration>uniqueSet a {d: int()}</xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='5'><b b='5'/></a>";
			parse(xp, "", xml, reporter);
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF522") > 0);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <Town xd:script='occurs 0..*;' name='addr.town()'>\n"+
"        <Street xd:script='occurs 0..*'\n"+
"          name='addr.street()'>\n"+
"          <House xd:script='occurs 0..*' number='addr.house.ID()'/>\n"+
"        </Street>\n"+
"      </Town>\n"+
"      <Village xd:script='occurs 0..*;'\n"+
"        name='addr.town()'>\n"+
"        <House xd:script='occurs 0..*; init addr.street.NEWKEY();' number='addr.house.ID();'/>\n"+
"      </Village>\n"+
"    </xd:mixed>\n"+
"    <Address xd:script='occurs 0..*; finally addr.IDREF()'\n"+
"       town='addr.town()'\n"+
"       street='optional addr.street();onAbsence addr.street.NEWKEY();'\n"+
"       number='addr.house()' />\n"+
"  </a>\n"+
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
			assertNoErrorwarnings(reporter);
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
			assertTrue(reporter.getErrorCount() == 1 && s.indexOf("path=/a/Address[2]") > 0, s);
		} catch (Exception ex) {fail(ex);}
		String oldCodes = getProperty(XDConstants.XDPROPERTY_STRING_CODES);
		try {
			xp = compile(dataDir + "TestKeyAndRef2.xdef");
			assertEq(dataDir + "TestKeyAndRef2.xml", parse(xp, "", dataDir + "TestKeyAndRef2.xml",reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "TestKeyAndRef3.xdef");
			assertEq(dataDir + "TestKeyAndRef3.xml", parse(xp, "", dataDir + "TestKeyAndRef3.xml", reporter));
			assertNoErrorwarnings(reporter);
			assertEq(dataDir+"TestKeyAndRef3_1.xml",parse(xp, "", dataDir+"TestKeyAndRef3_1.xml", reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "TestKeyAndRef4.xdef");
			assertEq(dataDir+"TestKeyAndRef4.xml", parse(xp, "",dataDir + "TestKeyAndRef4.xml", reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", dataDir + "TestKeyAndRef4_1.xml" , reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().indexOf("XDEF522") > 0,
				"Error Not recognized; " + reporter);
			parse(xp, "", dataDir + "TestKeyAndRef4_2.xml", reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().indexOf("XDEF522") > 0,
				"Error Not recognized; " + reporter);
			xp = compile(dataDir + "TestKeyAndRef5.xdef");
			assertEq(dataDir+"TestKeyAndRef5.xml", parse(xp, "", dataDir + "TestKeyAndRef5.xml",reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(dataDir + "TestKeyAndRef6.xdef");
			assertEq(dataDir+"TestKeyAndRef6.xml", parse(xp, "" , dataDir + "TestKeyAndRef6.xml", reporter));
			assertNoErrorwarnings(reporter);
			setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
			setProperty(XDConstants.XDPROPERTY_SPECDATES, null);
			setProperty(XDConstants.XDPROPERTY_STRING_CODES, "UTF-8");
			xp = compile(dataDir + "TestKeyAndRef7.xdef");
			assertEq(dataDir+"TestKeyAndRef7.xml",parse(xp,"Mondial",dataDir+"TestKeyAndRef7.xml", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // test CHIID
"<xd:def xmlns:xd='" + _xdNS + "' root='A' >\n" +
"  <xd:declaration>uniqueSet s int();</xd:declaration>\n" +
"  <A><a xd:script='*' a='s.ID()'/><b xd:script='*' a='s.CHKID()'/></A>\n" +
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
"  <xd:declaration>type a int(); type b string(); uniqueSet s2 {a: a(); b: b()};</xd:declaration>\n" +
"  <A>\n" +
"    <a xd:script='*' a='s2.a()'>\n" +
"      <b xd:script='*; finally s2.ID();' b='s2.b()' /> \n" +
"    </a>\n" +
"    <b xd:script='*' a='s2.a()'>\n" +
"      <c xd:script='*; finally s2.CHKID()' b='s2.b()' />\n" +
"    </b>\n" +
"  </A>\n" +
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
"  <xd:declaration>uniqueSet u {a: string();}</xd:declaration>\n" +
"  <A><a xd:script='*' b='u.a.ID()'/><b xd:script='*' b='u.a.CHKID()'/></A>\n" +
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
				&& (s=reporter.printToString()).contains("/A/b[2]/@b") && s.contains("/A/b[3]/@b"), reporter);
			xdef = //test CHIID-
"<xd:def xmlns:xd='" + _xdNS + "' root='Test'>\n" +
"  <xd:declaration>\n" +
"     type at   int();\n" +
"     type bt   string();\n" +
"     type ct   enum('Y','N');\n" +
"     uniqueSet s3 {a: at(); b: bt(); c: string()};\n" + // c accepts anything
"     boolean x() {return ct() AND s3.c();}\n"+
"  </xd:declaration>\n" +
"  <Test> \n" +
"    <A xd:script=\"*\" a=\"s3.a()\">\n" +
"      <B xd:script=\"*; ref B; finally s3.ID()\"/>\n" +
"    </A>\n" +
"    <uA xd:script=\"*\" a=\"s3.a()\">\n" +
"      <uB xd:script=\"*; finally s3.CHKID()\" b=\"s3.b()\" c=\"x()\"/>\n" +
"    </uA>\n" +
"  </Test>\n" +
"  <B b=\"s3.b()\" c=\"s3.c()\"/>\n" +
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
				&& (s = reporter.printToString()).contains("XDEF522") && s.contains("/Test/uA[1]/uB[2]"),
				reporter);
		} catch (Exception ex) {fail(ex);}
		if (oldCodes != null) {
			setProperty(XDConstants.XDPROPERTY_STRING_CODES, oldCodes);
		}
		try { // check xdType
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"  <xd:declaration>uniqueSet u {a: string();}</xd:declaration>\n" +
"  <A xd:script='var Parser x = null;'>\n" +
"    <B b='xdType(); onTrue x = getParsedValue();'/>\n" +
"    <C c='x()'/>\n" + // parser is x
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<A><B b='int()'/><C c='99'/></A>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<A><B b='int'/><C c='abc'/></A>", reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().contains("XDEF809"), reporter);
			parse(xp, "", "<A><B b='int(1,2)'/><C c='1'/></A>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<A><B b='int(1,2)'/><C c='0'/></A>", reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().contains("XDEF813"), reporter);
			parse(xp, "", "<A><B b='int(1,2)'/><C c='3'/></A>", reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().contains("XDEF813"), reporter);
			parse(xp, "", "<A><B b='ynt(1,2)'/><C c='3'/></A>", reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817") &&  s.contains("XDEF820"), reporter);
			parse(xp, "", "<A><B b='xdatetime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<A><B b='xdatetime(\"dd.MM.yyyy\")'/><C c='01.02'/></A>", reporter);
			assertTrue(reporter.getErrorCount()==1 && reporter.printToString().contains("XDEF809"), reporter);
			parse(xp, "", "<A><B b='xdattime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>", reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817") &&  s.contains("XDEF820"),
				reporter);
			String propwarning = getProperties().getProperty(XDConstants.XDPROPERTY_WARNINGS);
			setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
			 // datetime must be xdatetime
			parse(xp, "", "<A><B b='datetime(\"dd.MM.yyyy\")'/><C c='01.02.1987'/></A>", reporter);
			assertTrue(reporter.getErrorCount() == 2
				&& (s = reporter.printToString()).contains("XDEF817") &&  s.contains("XDEF820"),
				reporter);
			setProperty(XDConstants.XDPROPERTY_WARNINGS, propwarning);
			// test uniqueSet setValue, getValoue and order of attribute processing in Xdefinition
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
"    <Param Type=\"decimal()\" Name=\"xxx\"/>\n" +
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
			assertTrue(reporter.getErrorCount() == 2 && (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524") && s.contains("birthday") && s.contains("name"),
				reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n" +
"  <xd:declaration>uniqueSet u {var Parser x, int y; a: string(); var String z}</xd:declaration>\n" +
"  <a>\n" +
"    <DefParams>\n" +
"       <Param xd:script=\"init out(u.z==null);*;finally out(','+u.z+',')\"\n" +
"          Name=\"u.a.ID(); onTrue u.z='x'; onFalse u.z='y';\"\n" +
"          Type=\"xdType(); onTrue u.x=getParsedValue(); onFalse u.y=99; finally out(u.y)\"/>\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script=\"*;\" Name=\"u.a.CHKID();\" Value=\"u.x; onTrue out(u.x + ','); \"/>\n" +
"    </Params>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"decimal()\" Name=\"Vyska\"/>\n" +
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
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertEq("true,x,false,x,false,x,string,decimal,xdatetime,", swr.toString());
			assertTrue(reporter.getErrorCount() == 2 && (s = reporter.printToString()).contains("XDEF804")
				&& s.contains("XDEF524") && s.contains("DatumNarozeni") && s.contains("Jmeno"),
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
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument(); // parse again
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter); // uniqeue set must be clear!
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter); // even here uniqeue set must be clear!
			// method uniqueSet.toContainer()
			xdef = // explicit variant
"<xd:def xmlns:xd='" + _xdNS + "' root='List'>\n" +
"  <xd:declaration>\n" +
"    Container c;\n" +
"    int i=0;\n" +
"    uniqueSet members {room: string()};\n" +
"  </xd:declaration> \n" +
"  <List xd:script='finally c = members.toContainer();'>\n" +
"    <Member xd:script='occurs +' Name='string()' Room='members.room.SET()'/>\n" +
"  </List>\n" +
"  <School xd:script='var String s;'>\n" +
"    <Group xd:script='*; create c.getLength()'\n" +
"      Room='string(); create s = ((Container)c.item(i++)).getNamedString(\"room\");'>\n" +
"      <Student xd:script=\"+;create xpath('//Member[@Room=\\''+s+'\\']');\" Name='string()'/> \n" +
"    </Group>\n" +
"  </School>\n" +
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
			assertNoErrorwarnings(reporter);
			assertEq(create(xd, "School", reporter), s);
			assertNoErrorwarnings(reporter);
			xdef = // toContainer() - variant with container as context
"<xd:def xmlns:xd='" + _xdNS + "' root='List' >\n" +
"  <xd:declaration>uniqueSet members {room: string()};</xd:declaration>\n" +
"  <List xd:script='finally {returnElement(xcreate(\"School\"))}'>\n" +
"     <Member xd:script='occurs +' Name='string()' Room='members.room.SET()'/>\n" +
"  </List>\n" +
"  <School>\n" +
"    <Group xd:script='*; var String s; create members.toContainer();'\n" +
"      Room='string(); create s=from(\"@room\");'>\n" +
"      <Student xd:script=\"+;create xpath('//Member[@Room=\\''+s+'\\']');\" Name='string()'/> \n" +
"    </Group>\n" +
"  </School>\n" +
"</xd:def>";
			assertEq(parse(compile(xdef).createXDDocument(), xml, reporter), s);
			assertNoErrorwarnings(reporter);
			xdef = // test reporting iof incomplete key items,
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='T'>\n" +
"  <xd:declaration>uniqueSet r {a: string(1,2); b: string(1,2)};</xd:declaration>\n" +
"  <T> <R xd:script='*; finally r.ID()' A='r.a' B='r.b'/> </T>\n" +
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
		try { // test ID(null), SET(null)
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='Town'>\n"+
"  <xd:declaration scope=\"local\">\n" +
"    uniqueSet items {street:string(2,50); house: ? int(1,999);var int x;};\n" +
"    int count = 0;\n" +
"  </xd:declaration>\n" +
"  <Town>\n" +
"    <Street xd:script=\"*; init items.house.ID(null);\" Name=\"items.street.ID();\">\n" +
"      <House xd:script=\"*\"\n" +
"             Number=\"items.house.ID(); finally {items.x=++count; out(items.x + ',');}\"/>\n" +
"    </Street>\n" +
"    <Houses>\n" +
"      <House xd:script=\"*;\" Street=\"items.street()\"\n" +
"         Number=\"items.house.IDREF(); onTrue {out(items.x+',');}\"/>\n" +
"    </Houses>\n" +
"    <Streets xd:script='init items.house.SET(null);'>\n" +
"      <Street xd:script=\"*;\" Name=\"items.street.IDREF()\"/>\n" +
"    </Streets>\n" +
"  </Town>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml =
"<Town>\n" +
"  <Street Name=\"Empty\" />\n" +
"  <Street Name=\"Long\" >\n" +
"    <House Number=\"1\"/><House Number=\"2\"/><House Number=\"3\"/>\n" +
"  </Street>\n" +
"  <Street Name=\"Short\" >\n" +
"    <House Number=\"1\"/><House Number=\"2\"/>\n" +
"  </Street>\n" +
"  <Houses>\n" +
"    <House Street=\"Long\" Number=\"2\"/>\n" +
"    <House Street=\"Short\" Number=\"2\"/>\n" +
"    <House Number=\"1\" Street=\"Long\"/>\n" +
"    <House Number=\"1\" Street=\"Short\"/>\n" +
"    <House Street=\"Long\" Number=\"3\"/>\n" +
"  </Houses>\n" +
"  <Streets>\n" +
"    <Street Name=\"Short\"/><Street Name=\"Long\"/><Street Name=\"Empty\"/>\n"+
"  </Streets>\n" +
"</Town>";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("1,2,3,4,5,2,5,1,4,3,", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
		try { // test of uniqueSetKey and bindSet
			xdef = // uniqueSetKey
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='CodeBook'>\n"+
"  <xd:declaration scope=\"local\">\n" +
"     int  AttrCount;\n" +
"     type attrValue           string(1,511);\n" +
"     type description         string(1,511);\n" +
"     type name                string(1,30);\n" +
"     type version             string(1,20);\n" +
"     type xdate               xdatetime('yyyy-MM-dd');\n" +
"     uniqueSet nodeSet        {Node: name(); var int AttrCount};\n" +
"     uniqueSet attrSet        {Node: name(); Attr: name()};\n" +
"     boolean idNode() {return nodeSet.Node.ID() AAND attrSet.Node();}\n"+
"     boolean chkNode() {return nodeSet.Node.CHKID() AAND attrSet.Node();}\n"+
"  </xd:declaration>\n" +
"  <CodeBook>\n" +
"    <Def xd:script=\"0..1;\"> <!-- Code list definitions -->\n" +
"      <Node  xd:script=\"1..;\" Name=\"idNode();\">\n" +
"        <Attr xd:script=\"1..;\" Name=\"attrSet.Attr.ID()\"/>\n" +
"        <Node xd:script=\"0..; ref CodeBook/Def/Node\"/>\n" +
"      </Node>\n" +
"    </Def>\n" +
"    <Node xd:script=\"var uniqueSetKey x=attrSet.getActualKey(); 0..*;\"\n" +
"          Name=\"chkNode();\">\n" +
"      <xd:sequence xd:script=\"1..*\">\n" +
"        <Row xd:script=\"1..*;\">\n" +
"          <Attr xd:script=\"1..*;\" Name=\"attrSet.Attr.CHKID()\">\n" +
"            attrValue()\n" +
"          </Attr>\n" +
"        </Row>\n" +
"        <Node xd:script=\"0..*;ref CodeBook/Node; finally x.resetKey();\"/>\n"+
"      </xd:sequence>\n" +
"    </Node>\n" +
"  </CodeBook>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<CodeBook>\n" +
"  <Def>\n" +
"    <Node Name=\"Tab_V\">\n" +
"      <Attr Name=\"A\"/><Attr Name=\"B\"/>\n" +
"      <Node Name=\"Tab_m\"><Attr Name=\"c\"/><Attr Name=\"d\"/></Node>\n" +
"    </Node>\n" +
"  </Def>\n" +
"  <Node Name=\"Tab_V\">\n" +
"    <Row><Attr Name=\"A\">A1</Attr><Attr Name=\"B\">B1</Attr></Row>\n" +
"    <Node Name=\"Tab_m\">\n" +
"      <Row><Attr Name=\"c\">1c1</Attr><Attr Name=\"d\">1d1</Attr></Row>\n" +
"      <Row><Attr Name=\"c\">1c2</Attr><Attr Name=\"d\">1d2</Attr></Row>\n" +
"    </Node>\n" +
"    <Row> <Attr Name=\"A\">A2</Attr><Attr Name=\"B\">B2</Attr></Row>\n" +
"    <Node Name=\"Tab_m\">\n" +
"      <Row><Attr Name=\"c\">2c1</Attr><Attr Name=\"d\">2d1</Attr></Row>\n" +
"      <Row><Attr Name=\"c\">2c2</Attr><Attr Name=\"d\">2d2</Attr></Row>\n" +
"    </Node>\n" +
"  </Node>\n" +
"</CodeBook>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = // bindSet
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='CodeBook'>\n"+
"  <xd:declaration scope=\"local\">\n" +
"    int  AttrCount;\n" +
"    type attrValue    string(1,511);\n" +
"    type description  string(1,511);\n" +
"    type name         string(1,30);\n" +
"    type version      string(1,20);\n" +
"    type xdate        xdatetime('yyyy-MM-dd');\n" +
"    uniqueSet nodes   {Node: name(); var int AttrCount};\n" +
"    uniqueSet atts    {Node: name(); Attr: name()};\n" +
"    boolean idNode()  {return nodes.Node.ID() AAND atts.Node();}\n"+
"    boolean chkNode() {return nodes.Node.CHKID() AAND atts.Node();}\n"+
"  </xd:declaration>\n" +
"  <CodeBook>\n" +
"    <!-- Code list definitions -->\n" +
"    <Def>\n" +
"      <Node  xd:script=\"1..*;\" Name=\"idNode();\">\n" +
"        <Attr xd:script=\"1..*;\" Name=\"atts.Attr.ID()\"/>\n" +
"        <Node xd:script=\"0..*; ref CodeBook/Def/Node\"/>\n" +
"      </Node>\n" +
"    </Def>\n" +
"    <!-- Code list values  -->\n" +
"    <Node xd:script=\"0..*;\"\n" +
"          Name=\"chkNode();\">\n" +
"      <xd:sequence xd:script=\"1..*\">\n" +
"        <Row xd:script=\"1..*;\">\n" +
"          <Attr xd:script=\"1..*;\" Name=\"atts.Attr.CHKID()\">\n" +
"            attrValue()\n" +
"          </Attr>\n" +
"        </Row>\n" +
"        <Node xd:script=\"0..*; init bindSet(atts); ref CodeBook/Node;\"/>\n" +
"      </xd:sequence>\n" +
"    </Node>\n" +
"  </CodeBook>\n" +
"</xd:def>";
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
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
