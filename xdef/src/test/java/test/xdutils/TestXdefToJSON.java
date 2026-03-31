package test.xdutils;

import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XDEF40_NS_URI;
import static org.xdef.XDConstants.XDEF41_NS_URI;
import static org.xdef.XDConstants.XDEF42_NS_URI;
import org.xdef.XDDocument;
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
import test.XDTester;
import static test.XDTester._xdNS;

/** Test of conversion of X-definition from XML to JSON and from JSON to XML.
 * @author Vaclav Trojan
 */
public final class TestXdefToJSON extends XDTester {

    TestXdefToJSON() {super();}

    /** Test the conversion of X-definition from XML to JSON and vice versa.
     * @param xdef string with XML form of X-definition
     * @param xdName name of invoked X-definition.
     * @param display it true, results of conversion are displayed.
     * @param data list with data strings.
     * @return empty string if test is OK, otherwis, returns string witn error message
     */
    private String testXdefJson(final String xdef, final String xdName, final boolean display,final String... dataList){
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
            if (dataList != null && dataList.length > 0) {
                for (String data: dataList) {
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
                        return "OK".equals(s) ? "" : ("Error: parsed object in not equal to object from XComponent:"+s);
                    } else {
                        Object x = xc.toXon();
                        if (!XonUtils.xonEqual(o, x)) {
                            return "Error: parsed object in not equal to object from XComponent:\n"
                                + XonUtils.toJsonString(o) + "\n" + XonUtils.toJsonString(x);
                        }
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
    private String testXdefJson(final File xdef, final String xdName, final boolean display, final File... dataList) {
        try {
            String xdef_str = FUtils.readString(xdef, "UTF-8");
            ArrayList<String> ar = new ArrayList<>();
            if (dataList != null && dataList.length > 0) {
                for (File x: dataList) {
                    if (x != null && x.isFile()) {
                         ar.add(FUtils.readString(x, "UTF-8"));
                    }
                }
            }
            if (ar.isEmpty()) {
                return testXdefJson(xdef_str,xdName, display);
            }
            return testXdefJson(xdef_str,xdName, display, (String[] )ar.toArray());
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
            assertEq("", testXdefJson(xdef, null, false));
            xdef =
"<x:def xmlns:x='"+_xdNS+"' name='a' root='a' script='options ignoreEmptyAttributes'>\n"+
"  <a x:script='ref b'>\n"+
"    <p/>\n"+
"    <q/>\n"+
"    optional int(); default 456\n"+
"  </a>\n"+
"  <b attr=\"optional an(); default 'a123x'\"> <c/> </b>\n"+
"</x:def>\n";
            data = "<a><c/><p/><q/></a>";
            assertEq("", testXdefJson(xdef, null, false, data));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:name = 'Example' xd:root = 'root'>\n" +
"  <xd:declaration scope='local'>\n"+
"     type myType $rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"\n"+
"  <xd:BNFGrammar scope='local' xd:name=\"$rrr\" >\n"+
"    S       ::= [#9#10#13 ]+ /*white spaces*/\n"+
"    integer ::= [0-9]+\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
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
            assertEq("", testXdefJson(xdef, "Example", false, data));
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
            assertEq("", testXdefJson(xdef, "Example", false, data));
            xdef =
"<xd:collection xmlns:xd='"+XDEF40_NS_URI+"'>\n" +
"\n"+
"<xd:def name='Example' root=\"A | B#B\">\n" +
"  <xd:json name=\"A\">\n" +
"{\"array\": [ {\"%script\":  \"2..3; ref B#Item\"}]}\n" +
"  </xd:json>\n" +
"</xd:def>\n"+
"\n"+
"<xd:BNFGrammar xmlns:xd='"+XDEF41_NS_URI+"' name=\"base\">\n"+
"  integer ::= [0-9]+\n"+
"  S       ::= [#9#10#13 ]+ /*white spaces*/\n"+
"  name ::= [A-Z] [a-z]+\n"+
"</xd:BNFGrammar>\n"+
"\n"+
"<xd:BNFGrammar xd:name=\"$rrr\" xd:extends=\"base\" >\n"+
"  intList  ::= integer (S? \",\" S? integer)*\n"+
"  fullName ::= ([A-Z] \".\" S){1,2} name\n"+
"</xd:BNFGrammar>\n"+
"\n"+
"<xd:declaration>\n"+
"  type myType base.parse('name');\n"+
"</xd:declaration>\n"+
"\n"+
"<xd:declaration xmlns:xd='"+XDEF40_NS_URI+"'>\n"+
"  type name $rrr.parse('fullName');\n"+
"  type list $rrr.parse('intList');\n"+
"</xd:declaration>\n"+
"\n"+
"<xd:def name='B' root='B'>\n" +
"  <xd:json name=\"Item\">\n" +
"{\"element\": \"myType\"}\n" +
"  </xd:json>\n" +
"\n"+
"  <B a='? string()'> <C>string()</C> </B>\n"+
"</xd:def>\n"+
"\n"+
"<xd:component xmlns:xd='"+XDEF42_NS_URI+"'>\n"+
"  %class "+_package+".TestxTOJson4A %link Example#A;\n"+
"  %class "+_package+".TestxTOJson4B %link B#B;\n"+
"</xd:component>\n"+
"\n"+
"</xd:collection>";
            String[] dataList = new String[] {
"{ \"array\": [\n"+
"    {\"element\": \"First\"},\n"+
"    {\"element\": \"Second\"},\n"+
"    {\"element\": \"Third\"}\n"+
"  ]\n"+
"}",
"<B a='J. D. Wain'>\n" +
"  <C>1, 23, 456</C>\n" +
"</B>"};
            assertEq("", testXdefJson(xdef, "Example", false, dataList));
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
                String result = files == null || files.length == 0
                    ? testXdefJson(f, xdname, false) : testXdefJson(f, xdname, false, files);
                 if (!result.isEmpty()) {
                     fail(xdname + "\n"  + result);
                 }
            }
            for (File f : SUtils.getFileGroup(xdefDataDir+"test/Test000_0*.xdef")) {
                String xdname = f.getName();
                if (xdname.contains("Test000_02") || xdname.contains("Test000_05")|| xdname.contains("Test000_06")
                    || xdname.contains("Test000_07") || xdname.contains("Test000_08")) { //multiple XDefs
                    continue;
                }
                xdname = xdname.substring(0, xdname.lastIndexOf('.'));
                File[] files = SUtils.getFileGroup(xdefDataDir+xdname+"*.xml");
                String result = files == null || files.length == 0
                    ? testXdefJson(f, xdname, false) : testXdefJson(f, xdname, false, files);
                if (!result.isEmpty()) {
                    fail(xdname + "\n"  + result);
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
