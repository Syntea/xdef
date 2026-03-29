package test.xdutils;

import java.io.File;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XDEF40_NS_URI;
import static org.xdef.XDConstants.XDEF41_NS_URI;
import static org.xdef.XDConstants.XDEF42_NS_URI;
import org.xdef.XDDocument;
import test.XDTester;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.util.XDefToJSON;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import static test.XDTester._xdNS;

/** Test of conversion of X-definition from XML to JSON and from JSON to XML.
 * @author Vaclav Trojan
 */
public final class TestXdefToJSON extends XDTester {

    public TestXdefToJSON() {super();}

    /** Test the conversion of X-definition from XML to JSON and vice versa.
     * @param xdef string with XML form of X-definition
     * @param data string with data (may be null, XML or JSON).
     * @param xdName name of invoked X-definition.
     * @param display it true, results of conversion are displayed.
     * @return empty string if test is OK, otherwis, returns string witn error message
     */
    private String testXdefJson(final String xdef, final String data, final String xdName, final boolean display) {
        try {
            compile(xdef);
            String xdef_JSON = XDefToJSON.xmlXdefToJson(xdef);
            if (display) {
                System.out.println(xdef_JSON);
            }
            String xdef_XML = XDefToJSON.jsonXdefToXml(xdef_JSON);
            if (display) {
                System.out.println(xdef_XML);
            }
            XDPool xp = compile(xdef_XML);
            if (data != null) {
                ArrayReporter reporter = new ArrayReporter();
                boolean isXML = data.trim().startsWith("<");
                Object o = isXML ? parse(xp, xdName, data, reporter) : jparse(xp, xdName, data, reporter);
                if (reporter.errorWarnings()) {
                    return reporter.toString();
                }
                try {
                    genXComponent(xp);
                } catch (Exception ex) {
                    return ""; // OK, component was not created
                }
                XDDocument xd = xp.createXDDocument(xdName);
                XComponent xc = isXML
                    ? xd.xparseXComponent(data, null, reporter) : xd.jparseXComponent(data, null, reporter);
                if (reporter.errorWarnings()) {
                    return reporter.toString();
                }
                if (isXML) {
                    String s = "" + KXmlUtils.compareElements((Element)o, xc.toXml());
                    return "OK".equals(s) ? "" : ("Error: parsed object in not equal to object from XComponent:" + s);
                } else {
                    Object x = xc.toXon();
                    if (!XonUtils.xonEqual(o, x)) {
                        return "Error: parsed object in not equal to object from XComponent:\n"
                            + XonUtils.toJsonString(o) + "\n" + XonUtils.toJsonString(x);
                    }
                }
            }
            return ""; // OK
        } catch (RuntimeException ex) {
            return STester.printThrowable(ex);
        }
    }

    /** Test the conversion of X-definition from XML to JSON and vice versa.
     * @param xdef file with XML form of X-definition
     * @param data file with data (may be null, XML or JSON).
     * @param xdName name of invoked X-definition.
     * @param display it true, results of conversion are displayed.
     * @return empty string if test is OK, otherwis, returns string witn error message
     */
    private String testXdefJson(final File xdef, final File data, final String xdName, final boolean display) {
        try {
            String xdef_str = FUtils.readString(xdef, "UTF-8");
            String data_str;
            if (data != null && data.isFile()) {
                data_str = FUtils.readString(data, "UTF-8");
            } else {
                data_str = null;
            }
            return testXdefJson(xdef_str, data_str, xdName, display);
        } catch (SException ex) {return STester.printThrowable(ex);}
    }

    @Override
    public void test() {
////////////////////////////////////////////////////////////////////////////////
        boolean T = false; // If the value is false, all tests are run; otherwise, only the first one is run
////////////////////////////////////////////////////////////////////////////////
        String xdef, data;
        try {
            xdef =
"<xd:declaration xmlns:xd='"+_xdNS+"'>\n" +
"  type x string();\n" +
"  type y int();\n" +
"  type z xdatetime('yyyy-MM-dd[THH:mm:ss]'); \n" +
"</xd:declaration>";
            assertEq("", testXdefJson(xdef, null, null, false));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:name = 'Example' xd:root = 'root'>\n" +
"  <xd:declaration scope='local'>\n"+
"     type myType $rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"\n"+
"  <xd:BNFGrammar scope='local' name=\"base\">\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"\n"+
"  <xd:BNFGrammar scope='local' xd:name=\"$rrr\" xd:extends=\"base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"\n"+
"  <xd:json name='root'>\n" +
"{ a: \"required myType()\" }\n"+
"  </xd:json>\n"+
"\n"+
"  <xd:component>\n"+
"    %class "+_package+".TestxTOJson2 %link Example#root;\n"+
"  </xd:component>\n"+
"</xd:def>";
            data = "{ \"a\":\"123, 456, 789\" }";
            assertEq("", testXdefJson(xdef, data, "Example", false));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"Example\" root=\"S2KF\">\n" +
"  <xd:declaration>\n" +
"    type  caseID            long(1,999_999_999); \n" +
"    type  companyName       string(1,100); \n" +
"    type  firstName         string(1,50); \n" +
"    type  ico               string(1,14);\n" +
"    type  lastName          string(1,50); \n" +
"    type  phoneNum          string(1,18);\n" +
"    type  rc                string(1,14);\n" +
"    type  statusCode        enum('X', 'Y', 'Z');\n" +
"    type  subjectType       enum('NP', 'LE', 'SEP');\n" +
"    type  xsDate            xdatetime('yyyy-MM-dd'); \n" +
"    type  xsDateTime        xdatetime('yyyy-MM-dd[THH:mm:ss]'); \n" +
"  </xd:declaration>\n" +
" \n" +
"  <xd:json name=\"S2KF\">\n" +
"{\n" +
"    \"caseID\":             \"  caseID()\",\n" +
"    \"createdTime\":        \"  xsDateTime()\",\n" +
"    \"modifiedTime\":       \"  xsDateTime()\",\n" +
"    \"statusCode\":         \"  statusCode()\",\n" +
"    \"holder\":             {\"%script\":  \"ref Subject\"},\n" +
"    \"owner\":              {\"%script\":  \"?; ref Subject\"},\n" +
"}\n" +
"  </xd:json>\n" +
"\n" +
"  <xd:json name=\"Subject\">\n" +
"{\n" +
"     \"subjectType\":       \"  subjectType()\",\n" +
"     \"firstName\":         \"? firstName()\",\n" +
"     \"lastName\":          \"? lastName()\",\n" +
"     \"companyName\":       \"? companyName()\",\n" +
"     \"birthDate\":         \"? xsDate()\",\n" +
"     \"CIN\":               \"? ico()\",\n" +
"     \"PIN\":               \"? rc()\",\n" +
"     \"contacts\":   {\"%script\": \"ref Contact\"}\n" +
"}\n" +
"  </xd:json>\n" +
"\n" +
"  <xd:json name=\"Contact\">\n" +
"{\n" +
"     \"phoneNum\":  \"? phoneNum()\",\n" +
"     \"emailAddr\": \"? emailAddr()\"\n" +
"}\n" +
"  </xd:json>\n" +
"\n" +
"  <xd:component>\n" +
"    %class "+_package+".TestxTOJson3 %link Example#S2KF;\n" +
"  </xd:component>\n" +
"</xd:def>";
            data =
"{\n" +
"  \"caseID\": 112233,\n" +
"  \"createdTime\": \"2026-01-29\",\n" +
"  \"modifiedTime\": \"2026-02-02\",\n" +
"  \"statusCode\": \"Y\",\n" +
"  \"holder\": {\n" +
"    \"subjectType\": \"NP\",\n" +
"    \"firstName\": \"JOHN\",\n" +
"    \"lastName\": \"BROWN\",\n" +
"    \"PIN\": \"7403160123\",\n" +
"    \"contacts\": {\"phoneNum\": \"9988776655\"}\n" +
"  }\n" +
"}";
            assertEq("", testXdefJson(xdef, data, "Example", false));
            xdef =
"<xd:collection xmlns:xd='"+XDEF40_NS_URI+"'>\n" +
"\n"+
"<xd:def xmlns:xd='"+XDEF41_NS_URI+"' name='Example' root=\"A | B#B\">\n" +
"  <xd:json name=\"A\">\n" +
"{\"array\": [ {\"%script\":  \"2..3; ref B#Element\"}]}\n" +
"  </xd:json>\n" +
"</xd:def>\n"+
"\n"+
"<xd:BNFGrammar xmlns:xd='"+XDEF41_NS_URI+"' name=\"base\">\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"</xd:BNFGrammar>\n"+
"\n"+
"<xd:BNFGrammar xmlns:xd='"+XDEF41_NS_URI+"' xd:name=\"$rrr\" xd:extends=\"base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"</xd:BNFGrammar>\n"+
"\n"+
"<xd:declaration>\n"+
"   type myType base.parse('name');\n"+
"   type name $rrr.parse('fullName');\n"+
"   type list $rrr.parse('intList');\n"+
"</xd:declaration>\n"+
"\n"+
"<xd:def xmlns:xd='"+XDEF41_NS_URI+"' name='B' root='B'>\n" +
"  <xd:json name=\"Element\">\n" +
"{\"element\": \"myType\"}\n" +
"  </xd:json>\n" +
"  <B a='? string()'>\n"+
"    <C>string()</C>\n"+
"  </B>\n"+
"</xd:def>\n"+
"\n"+
"<xd:component xmlns:xd='"+XDEF42_NS_URI+"'>\n"+
"    %class "+_package+".TestxTOJson4A %link Example#A;\n"+
"    %class "+_package+".TestxTOJson4B %link B#B;\n"+
"</xd:component>\n"+
"\n"+
"</xd:collection>";
            data =
"{\"array\": [\n" +
"    {\"element\": \"First\"},\n" +
"    {\"element\": \"Second\"},\n" +
"    {\"element\": \"Third\"}\n" +
"  ]\n" +
"}";
            assertEq("", testXdefJson(xdef, data, "Example", false));
            assertEq("", testXdefJson(xdef, "<B a='John C. Brown'><C>1, 23, 456</C></B>", "B", false));
        } catch (RuntimeException ex) {fail(ex); return;}
if(T) return;
        try { // test all X-definitions from test/xdef/data/json directory
            String dataDir = getDataDir();
            dataDir = dataDir.substring(0, dataDir.indexOf("/test/xdutils/data/"));
            final String xdefDataDir = dataDir + "/test/xdef/data/";
            for (File f : SUtils.getFileGroup(xdefDataDir+"json/Test*.xdef")) {
                String xdname = f.getName();
                xdname = xdname.substring(0, xdname.lastIndexOf('.'));
                File[] files = SUtils.getFileGroup(xdefDataDir+xdname+"*.json");
                String result;
                if (files == null || files.length == 0) {
                    result = testXdefJson(f, null, xdname, false);
                    if (!result.isEmpty()) {
                        fail(xdname + "\n"  + result);
                    }
                } else {
                    for (File g : files) {
                        result = testXdefJson(f, g, xdname, false);
                        if (!result.isEmpty()) {
                            fail(xdname+ "; data: " + g.getName() + "\n" + result );
                        }
                    }
                }
            }
            for (File f : SUtils.getFileGroup(xdefDataDir+"test/Test000_0*.xdef")) {
                String xdname = f.getName();
                if (xdname.contains("Test000_02") || xdname.contains("Test000_05")|| xdname.contains("Test000_06")
                    || xdname.contains("Test000_07") || xdname.contains("Test000_08")) { //multiple XDefs
                    continue;
                }
                xdname = xdname.substring(0, xdname.lastIndexOf('.'));
                File[] file = SUtils.getFileGroup(xdefDataDir+xdname+"*.xml");
                String result;
                if (file == null || file.length == 0) {
                    result = testXdefJson(f, null, xdname, false);
                    if (!result.isEmpty()) {
                        fail(xdname + "\n"  + result);
                    }
                } else {
                    for (File g : file) {
                        result = testXdefJson(f, g, xdname, false);
                        if (!result.isEmpty()) {
                            fail(xdname+ "; data: " + g.getName() + "\n" + result );
                        }
                    }
                }
            }
        } catch (RuntimeException ex) {fail(ex);}

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
