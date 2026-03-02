package bugreports;

import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_BYTES;
import org.xdef.impl.XConstants;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

public class SaxonBindDomExample2 extends XDTester {

    public SaxonBindDomExample2() {
        super();
        setChkSyntax(false); // here it MUST be false!
    }

    public String test(final String typ, final Object o) throws Exception {
        String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A'>\n" +
"  <xd:declaration>\n" +
"    external " + typ + " doc;\n" +
"    external Container result;\n" +
"    String query = 'declare variable $doc external; $doc';\n" +
"  </xd:declaration>\n" +
"  <A xd:script='finally out(result = xquery(query));'/>\n" +
"</xd:def>";
        XDPool xp = compile(xdef);
        XDDocument xd = xp.createXDDocument();
        xd.setVariable("doc", o);
//        System.out.println(xd.getVariable("doc"));
        StringWriter swr;
        xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
        parse(xd, "<A/>", null);
        DefContainer c = (DefContainer) xd.getVariable("result");
        XDValue v = c.getXDItem(0);
        if (v.getItemId() == XD_BYTES) {
            String s = "";
            byte[] b = c.getXDItem(0).getBytes();
            for (byte x: b) {
                s += " " + x;
            }
            return s;
        }
        return swr.toString();
    }

    /** Run test and display error information. */
    @Override
    public void test() {
        System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
        System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
        setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//        setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
        setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
        Element el;
        String xdef, xml;
        XDDocument xd;
        XDPool xp;
        Document doc;
        try {
            doc = KXmlUtils.parseXml("<root><item a='Hi'>Hello</item></root>");
//            assertEq("-12345678", test("int", -12345678));
//            assertEq("", test("Element", new DefElement(doc.getDocumentElement())));
//if(true)return;
            assertEq("true", test("boolean", true));
            assertEq("http://example.com/ns", test("URI", new java.net.URI("http://example.com/ns")));
            assertEq("myElement", test("QName", new javax.xml.namespace.QName("myElement")));
            assertEq("ex:myElement",
                test("QName", new javax.xml.namespace.QName("http://example.com/ns","myElement","ex")));
            assertEq(" 33 34 35", test("Bytes", new byte[] {(byte) 33, (byte) 34, (byte) 35}));
            assertEq("P2Y3M5DT4H30M", test("Duration", new SDuration(("P2Y3M5DT4H30M"))));
            assertEq("2026-03-02T00:39:15",test("Datetime", new SDatetime("2026-03-02T00:39:15")));
            assertEq("2026-03-02", test("Datetime", new SDatetime("2026-03-02"))); //date
            assertEq("00:39:15", test("Datetime", new SDatetime("00:39:15"))); // time
            assertEq("---02", test("Datetime", new SDatetime("---02"))); //GDay
            assertEq("--02", test("Datetime", new SDatetime("--02"))); //GMonth
            assertEq("--02-01", test("Datetime", new SDatetime("--02-01"))); ; //GMonthDay
            assertEq("-2026", test("Datetime", new SDatetime("-2026")));  //GYear
            assertEq("-2026-01", test("Datetime", new SDatetime("-2026-01")));  //GYearMonth
//if(true)return;
            xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2'>\n" +
"  <anthill name='string; create xquery(&apos;\n" +
"    for $i in (//anthill)\n" +
"      return $i[count(//insect[caste/text() = \"soldier\" and @anthill = $i/@name]) > 2]/@name &apos;)' />\n" +
"</xd:def>";
            xml =
"<forest>\n" +
"  <animals>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>queen</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>samec</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>samec</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>kralovna</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"  </animals>\n" +
"  <objects>\n" +
"    <anthill name=\"nexToStrawberry\" />\n" +
"    <anthill name=\"underSpruce\" />\n" +
"  </objects>\n" +
"</forest>";
            xp = compile(xdef);
            xd = xp.createXDDocument();
            el = KXmlUtils.parseXml(xml).getDocumentElement();
            xd.setXDContext(el);
            assertEq("<anthill name='underSpruce'/>", xd.xcreate("anthill", null));

/**
            javax.xml.namespace.QName v = new javax.xml.namespace.QName("$doc");
            //Prepare a sample DOM Node
            String xml = ;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true); // Important for XQuery
            org.w3c.dom.Document domDoc = dbf.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xml)));
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc",
                v, domDoc.getDocumentElement(), null));
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc/item/string()",
                v, domDoc.getDocumentElement(), null));
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc/item/@a/string()",
                v, domDoc.getDocumentElement(), null));
            org.w3c.dom.Element el = (org.w3c.dom.Element) domDoc.getDocumentElement().getChildNodes().item(0);
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc/string()",
                v, el.getAttributeNode("a"), null));
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc",
                v, el.getAttributeNode("a"), null));
            System.out.println("XQuery result:\n" + testXQ("declare variable $doc external; $doc",
                v, new javax.xml.namespace.QName("x.y.z", "w:abc"), null));
            String query =
"for $i in (//anthill) return $i[count(//insect[caste/text() = \"soldier\" and @anthill = $i/@name]) > 2]/@name";
            org.w3c.dom.Document doc = KXmlUtils.parseXml(
"<forest>\n" +
"  <animals>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>queen</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>samec</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>samec</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"nexToStrawberry\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>kralovna</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"    <insect family=\"anteatersí\" anthill=\"underSpruce\">\n" +
"      <genus>ant</genus>\n" +
"      <caste>soldier</caste>\n" +
"    </insect>\n" +
"  </animals>\n" +
"  <objects>\n" +
"    <anthill name=\"nexToStrawberry\" />\n" +
"    <anthill name=\"underSpruce\" />\n" +
"  </objects>\n" +
"</forest>");
            System.out.println("XQuery result:\n" + testXQ(query, null, null, doc.getDocumentElement()));
            try {
                System.out.println("XQuery result:\n" + testXQ("declare variable $docx external; $docx", v, 1234, null));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            try {
                System.out.println("XQuery result:\n"+testXQ("declare variable $doc external; $doc",v,new ArrayList(), null));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
/**/
        } catch (Exception ex) {fail(ex);}
     }

    public static void main(String[] args) {
         XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {
            System.exit(1);
        }
   }
}
