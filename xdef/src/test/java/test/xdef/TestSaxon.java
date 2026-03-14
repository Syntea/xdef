package test.xdef;

import java.io.StringWriter;
import org.w3c.dom.Document;
import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.w3c.dom.Element;
import org.xdef.XDFactory;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

/** Test versions with Saxon and without Saxon implementation.
 * @author Vaclav Trojan
 */
public class TestSaxon extends XDTester {

    public TestSaxon() {super();}

    private void testNoSaxon() {putInfo("XQuery library not found; tests skipped.");}

    /** Test binding of a value to the external XQuery varialb;e.
     * @param typ name of value type.
     * @param val value to bind witn the external Variable $doc in the XQuery.
     * @return string created from the variable $doc.
     * @throws Exception
     */
    public String testBind(final String typ, final Object val) throws Exception {
        XDPool xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='X'>\n" +
"  <xd:declaration>\n" +
"    external " + typ + " doc;\n" +
"    external Container result;\n" +
"    String query = 'declare variable $doc external; $doc';\n" +
"  </xd:declaration>\n" +
"  <X xd:script='finally out(result = xquery(query));'/>\n" +
"</xd:def>");
        XDDocument xd = xp.createXDDocument();
        xd.setVariable("doc", val);
        StringWriter swr = new StringWriter();
        xd.setStdOut(XDFactory.createXDOutput(swr, false));
        ArrayReporter reporter = new ArrayReporter();
        parse(xd, "<X/>", reporter);
        assertNoErrors(reporter);
        return swr.toString();
    }

    private void testSaxon() {
        String xdef, xml;
        Document doc;
        Element el;
        StringWriter swr;
        XDDocument xd;
        XDPool xp;
        ArrayReporter reporter = new ArrayReporter();
        try {//xquery in declaration part (without XML context)
            xdef = // test xquery
"<xd:def  xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:declaration>\n" +
"    void testXQuery() {\n" +
"      Container c = xquery(\"let $b := 'abcd'\n"+
"        return (0 to string-length($b)) !(substring($b, 1, string-length($b) - .))\");\n" +
"      for(int i=0;i LT c.getLength();i++) out(c.item(i)+'.');\n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <a xd:script=\"init testXQuery()\"/>\n" +
"</xd:def>";
            xp = compile(xdef);
            xml = "<a/>";
            xd = xp.createXDDocument();
            swr = new StringWriter();
            xd.setStdOut(swr);
            assertEq(xml, parse(xd, xml , reporter));
            assertNoErrorwarnings(reporter);
            assertEq("abcd.abc.ab.a..", swr.toString());
            xdef = // test XPath 2
"<xd:def  xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:declaration>\n" +
"     void testXPath2() {\n" +
"        Container c = xpath(\"serialize(let $b := @b\n"+
"           return (0 to string-length($b)) !('_' || substring($b,1,string-length($b) - .)))\");\n"+
"        for(int i=0; i LT c.getLength(); i++) {\n" +
"           out(c.item(i));\n" +
"        }\n" +
"     }\n" +
"  </xd:declaration>\n" +
"  <a xd:script=\"init testXPath2();\" b=\"string();\"/>\n" +
"</xd:def>";
            xp = compile(xdef);
            xd = xp.createXDDocument();
            swr = new StringWriter();
            xd.setStdOut(swr);
            xml = "<a b=\"abcd\"/>";
            assertEq(xml, parse(xd, xml, reporter));
            assertNoErrorwarnings(reporter);
            assertEq("_abcd _abc _ab _a _", swr.toString());
        } catch (Exception ex) {fail(ex);}
        try {//fromXQ (xquery)
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='root'>\n"+
"  <root>\n"+
"    <xd:sequence xd:script=\"occurs *; create xquery('//a')\">\n"+
"      <a a = \"required string; create xquery('@A')\">\n"+
"         <b xd:script=\"occurs *; create xquery('B')\"\n"+
"             x = \"required string; create xquery('@c')\"\n"+
"             y = \"required string; create xquery('@d')\"/>\n"+
"      </a>\n"+
"    </xd:sequence>\n"+
"  </root>\n"+
"</xd:def>";
            xml =
"<x><a A=\"A\"><B c=\"c\" d=\"d\"/><B c=\"C\" d=\"D\"/></a><a A=\"B\"><B c=\"e\" d=\"f\"/></a></x>";
            assertEq(create(xdef, "", "root", reporter, xml),
"<root><a a=\"A\"><b x=\"c\" y=\"d\"/><b x=\"C\" y=\"D\"/></a><a a=\"B\"><b x=\"e\" y=\"f\"/></a></root>");
            assertNoErrorwarnings(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a'\n"+
"        script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces'\n"+
"        xmlns='N' xmlns:sod='N'>\n"+
"  <a>\n"+
"    <e f=\"optional string; create xpath('\\'\\'')\"/>\n"+
"    <e f=\"string; create xpath('\\'2\\'')\"/>\n"+
"    <f xd:script=\"occurs *; create xpath('../e')\"/>\n"+
"    <x xd:script=\"occurs *; create xpath('../sod:e')\"/>\n"+
"    <g xd:script=\"occurs *; create xpath('../*:e')\"/>\n"+
"    <h xd:script=\"occurs *; create xpath('preceding-sibling::e')\"/>\n"+
"    <i xd:script=\"occurs *; create xpath('preceding-sibling::*:e')\"/>\n"+
"    <j xd:script=\"occurs *; create xpath('preceding-sibling::sod:e')\"/>\n"+
"  </a>\n"+
"</xd:def>";
            assertEq(create(xdef,
                "", "a", reporter, xml), "<a xmlns=\"N\"><e/><e f=\"2\"/><x/><x/><g/><g/><i/><i/><j/><j/></a>");
            assertNoErrorwarnings(reporter);
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a' xmlns='N' xmlns:sod='N'\n"+
"        script='options ignoreAttrWhiteSpaces,ignoreTextWhiteSpaces'>\n"+
"  <a>\n"+
"    <e f=\"fixed '1'\"/>\n"+
"    <e f=\"fixed '2'\"/>\n"+
"    <f xd:script=\"occurs *; create xpath('../sod:e')\"/>\n"+
"    <g xd:script=\"occurs *; create xpath('preceding-sibling::*:e')\"/>\n"+
"    <h xd:script=\"occurs *; create xpath('preceding-sibling::sod:e')\"/>\n"+
"  </a>\n"+
"</xd:def>";
            assertEq(create(xdef,
                "", "a", reporter, xml), "<a xmlns='N'><e f='1'/><e f='2'/><f/><f/><g/><g/><h/><h/></a>");
            assertNoErrorwarnings(reporter);
        } catch (RuntimeException ex) {fail(ex);}
        try {//test of xquery
            xp = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='a' >\n"+
"   <a C='required num(1,9);'>\n"+
"      <O J='optional string(1,36); finally setText(xquery(\".\") + \"d\");' />\n"+
"   </a>\n"+
"</xd:def>");
            xml = "<a C='5'><O J='Abc'/></a>";
            el = parse(xp, "", xml, reporter);
            assertEq("Abcd", ((Element) el.getElementsByTagName("O").
                item(0)).getAttribute("J"));
        } catch(Exception ex) {fail(ex);}
        try {
            xdef = // result of xquery (in boolean expression
"<xd:def xmlns:xd='"+_xdNS+"' root='a|b|c|d'>\n"+
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
"  xd:script=\"finally if (!(xquery('@a') XOR xpath('@b'))) error('EE', '@a, @b must be excluzive');\"/>\n"+
"</xd:def>";
            xp = compile(xdef);
            xml = "<a typ='1'><b/></a>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<a typ='2'><b/></a>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<a typ='2'><c/></a>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<a typ='3'><c/></a>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<a typ='1'><c/></a>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<a typ='4'><c/></a>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);

            xml = "<b typ='1'><b/></b>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<b typ='2'><b/></b>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<b typ='2'><c/></b>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<b typ='1'><c/></b>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<c typ='1'><b/></c>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<c typ='2'><b/></c>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<c typ='2'><c/></c>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<c typ='1'><c/></c>";
            parse(xp, "", xml, reporter);
            assertErrors(reporter);
            xml = "<d a='1'/>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<d b='2'/>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorwarnings(reporter);
            xml = "<d a='1' b='2'/>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertErrors(reporter);
            xml = "<d/>";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertErrors(reporter);
            xdef = // Find anthill with more than two soldiers and use XQuery result for construction of element.
"<xd:def xmlns:xd='"+ _xdNS + "'>\n"+
"<xd:declaration>\n"+
"String x='for $i in(//anthill) return $i[count(//insect[caste/text()=\"soldier\" and @anthill=$i/@name])>1]/@name';\n"+
"</xd:declaration>\n"+
"  <anthill name='string; create xquery(x);' />\n"+
"</xd:def>";
            xd = compile(xdef).createXDDocument();
            xml =
"<forest>\n"+
"  <animals>\n"+
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\"> <genus>ant</genus> <caste>queen</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\"> <genus>ant</genus> <caste>male</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\"> <genus>ant</genus> <caste>male</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\"> <genus>ant</genus> <caste>soldier</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\"> <genus>ant</genus> <caste>queen</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\"> <genus>ant</genus> <caste>soldier</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\"> <genus>ant</genus> <caste>male</caste> </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\"> <genus>ant</genus> <caste>soldier</caste> </insect>\n" +
"  </animals>\n"+
"  <objects>\n"+
"    <anthill name=\"nexToStrawberry\" />\n"+
"    <anthill name=\"underSpruce\" />\n"+
"  </objects>\n"+
"</forest>";
            xd.setXDContext(KXmlUtils.parseXml(xml).getDocumentElement());
            el = create(xd, "anthill", reporter);
            assertNoErrorwarnings(reporter);
            assertTrue("underSpruce".equals(el.getAttribute("name")), el.getAttribute("name"));
        } catch (Exception ex) {fail(ex);}
        try {
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"'>\n" +
"<xd:declaration scope='local'>\n"+
"  external Element source;\n"+
"</xd:declaration>\n"+
"<Persons xd:script=\"create xquery(source, '.')\"\n" +
"         firma=\"create xquery('@name')\">\n" +
"  <Office>\n" +
"    <Kontakt xd:script= \"*; create xquery('Person[Telefon/@typ=\\'office\\']')\">\n" +
"      <Name xd:script=\"create xquery('Name')\">\n" +
"        string; create xquery('text()')\n" +
"      </Name>\n" +
"      <Telefon xd:script=\"create xquery('Telefon')\">\n" +
"        create xquery('text()');\n" +
"      </Telefon>\n" +
"    </Kontakt>\n" +
"  </Office>\n" +
"  <Home>\n" +
"    <Kontakt xd:script=\n" +
"          \"*; create xquery('Person[Telefon/@typ=\\'personal\\']')\">\n" +
"      <Name xd:script=\"create xquery('Name')\" >\n" +
"        create xquery('text()');\n" +
"      </Name>\n" +
"      <Telefon xd:script=\"create xquery('Telefon')\">\n" +
"        create xquery('text()');\n" +
"      </Telefon>\n" +
"    </Kontakt>\n" +
"  </Home>\n" +
"</Persons>\n" +
"</xd:def>";
            xp = compile(xdef);
            xml =
"<Firma name=\"Syntea software group a.s.\">\n" +
"    <Person>\n" +
"      <Name>Josef</Name>\n" +
"      <Telefon typ=\"office\">773102030</Telefon>\n" +
"    </Person>\n" +
"    <Person>\n" +
"      <Name>Petr</Name>\n" +
"      <Telefon typ=\"personal\">755001002</Telefon>\n" +
"    </Person>\n" +
"    <Person>\n" +
"      <Name>Martin</Name>\n" +
"      <Telefon typ=\"office\">775223311</Telefon>\n" +
"    </Person>\n" +
"    <Person>\n" +
"      <Name>Martin</Name>\n" +
"      <Telefon typ=\"personal\">678901002</Telefon>\n" +
"    </Person>\n" +
"    <Person>\n" +
"      <Name>Petr</Name>\n" +
"      <Telefon typ=\"office\">777888999</Telefon>\n" +
"    </Person>\n" +
"</Firma>";
            xd = xp.createXDDocument();
            xd.setVariable("source", xml);
            el = xd.xcreate("Persons", reporter);
            assertNoErrorwarnings(reporter);
            assertEq(3, el.getElementsByTagName("Office").item(0).getChildNodes().getLength());
            assertEq(2, el.getElementsByTagName("Home").item(0).getChildNodes().getLength());
        } catch (RuntimeException ex) {fail(ex);}
        try {
            assertEq("true", testBind("boolean", true));
            assertEq("http://example.com/ns", testBind("XDUri", new java.net.URI("http://example.com/ns")));
            assertEq("myElement", testBind("XDQName", new javax.xml.namespace.QName("myElement")));
            assertEq("ex:myElem",testBind("XDQName",new javax.xml.namespace.QName("http://org.xdef/ns","myElem","ex")));
            assertEq("ISIjeA==", testBind("Bytes", new byte[] {33, 34, 35, 120}));
            assertEq("P2Y3M5DT4H30M", testBind("Duration", new SDuration(("P2Y3M5DT4H30M"))));
            assertEq("2026-03-02T00:39:15",testBind("Datetime", new SDatetime("2026-03-02T00:39:15")));
            assertEq("2026-03-02", testBind("Datetime", new SDatetime("2026-03-02"))); //date
            assertEq("00:39:15", testBind("Datetime", new SDatetime("00:39:15"))); // time
            assertEq("---02", testBind("Datetime", new SDatetime("---02"))); //GDay
            assertEq("--02", testBind("Datetime", new SDatetime("--02"))); //GMonth
            assertEq("--02-01", testBind("Datetime", new SDatetime("--02-01"))); ; //GMonthDay
            assertEq("-2026", testBind("Datetime", new SDatetime("-2026")));  //GYear
            assertEq("-2026-01", testBind("Datetime", new SDatetime("-2026-01")));  //GYearMonth
            // testBind org.w3c.dom (Document, Element, Attr, Text)
            doc = KXmlUtils.parseXml("<root><item a='Hi'>Hello</item></root>");
            assertEq("<root><item a=\"Hi\">Hello</item></root>", testBind("Element", doc)); //document
            assertEq("<root><item a=\"Hi\">Hello</item></root>", testBind("Element",doc.getDocumentElement()));
            assertEq("Hi", testBind("Attr",
                ((Element) doc.getDocumentElement().getChildNodes().item(0)).getAttributeNode("a"))); //Attr
            assertEq("Hello", testBind("Text",
                ((Element) doc.getDocumentElement().getChildNodes().item(0)).getChildNodes().item(0))); //Text
        } catch (Exception ex) {fail(ex);}
    }

    @Override
    public void test() {
        if (XDFactory.isXQuerySupported() && XDFactory.isXPath2Supported()) {
            testSaxon();
        } else {
            testNoSaxon();
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
