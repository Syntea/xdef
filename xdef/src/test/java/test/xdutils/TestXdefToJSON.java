package test.xdutils;

import java.io.File;
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
import static test.XDTester._xdNS;

/** Test of conversion of X-definition from XML to JSON.
 * @author Vaclav Trojan
 */
public final class TestXdefToJSON extends XDTester {

    public TestXdefToJSON() {super();}

    private String testXdefJson(final String xdef, final String data, final String xdName) {
        try {
            compile(xdef);
            String xdef_JSON = XDefToJSON.xmlXdefToJson(xdef);
            if ("-Example".equals(xdName)) {
                System.out.println(xdef);
                System.out.println(xdef_JSON);
            }
            String xdef_XML = XDefToJSON.jsonXdefToXml(xdef_JSON);
            if ("-Example".equals(xdName)) {
                System.out.println(xdef_XML);
            }
            XDPool xp = compile(xdef_XML);
            if (data != null) {
                ArrayReporter reporter = new ArrayReporter();
                jparse(xp, xdName, data, reporter);
                if (reporter.errorWarnings()) {
                    return reporter.toString();
                }
                boolean component = false;
                try {
                    genXComponent(xp);
                    component = true;
                } catch (Exception ex) {}
                if (component) {
                    XDDocument xd = xp.createXDDocument(xdName);
                    XComponent xc = xd.jparseXComponent(data, null, reporter);
                    if (reporter.errorWarnings()) {
                        return reporter.toString();
                    }
                }
            }
            return "";
        } catch (Exception ex) {return org.xdef.sys.STester.printThrowable(ex);}
    }

    private String testXdefJson(final File xdef, final File data, final String xdName) {
        try {
            String xdef_str = FUtils.readString(xdef, "UTF-8");
            String data_str;
            if (data != null && data.isFile()) {
                data_str = FUtils.readString(data, "UTF-8");
            } else {
                data_str = null;
            }
            return testXdefJson(xdef_str, data_str, xdName);
        } catch (SException ex) {return STester.printThrowable(ex);}
    }

    @Override
    public void test() {
        String xdef, xml, json;
        final String dataDir = getDataDir();
        final String jsonDataDir = dataDir.substring(0, dataDir.indexOf("/xdutils/")) + "/xdef/data/json/";
        XComponent xc;
        XDPool xp;
        XDDocument xd;
        ArrayReporter reporter = new ArrayReporter();
        try {
            xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name='Example' root=\"test\">\n" +
"  <xd:json name=\"test\"> {\"pole\": [ {\"%script\":  \"2..3; ref Prvek\"}]} </xd:json>\n" +
"  <xd:json name=\"Prvek\"> {\"prvek\": \"string()\"} </xd:json>\n" +
"  <xd:component> %class "+_package+".TestxTOJson1 %link Example#test; </xd:component>\n"+
"</xd:def>";
            xp = compile(xdef);
            json = "{\"pole\": [ {\"prvek\": \"prvni\"}, {\"prvek\": \"druhy\"}, {\"prvek\": \"treti\"} ] }";
            jparse(xp, "Example", json, reporter);
            assertNoErrorsAndClear(reporter);
            xd = xp.createXDDocument("Example");
            genXComponent(xp);
            xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq("", testXdefJson(xdef, json, "Example"));
            xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:name = 'Example' xd:root = 'root'>\n" +
"  <xd:declaration scope='local'>\n"+
"     type myType $rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"  <xd:BNFGrammar scope='local' name=\"base\">\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar scope='local' xd:name=\"$rrr\" xd:extends=\"base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:json name='root'>\n" +
"{ a: \"required myType()\" }\n"+
"  </xd:json>\n"+
"  <xd:component>\n"+
"  %class "+_package+".TestxTOJson2 %link Example#root;\n"+
"  </xd:component>\n"+
"</xd:def>";
            xp = compile(xdef);
            json = "{ \"a\":\"123, 456, 789\" }";
            jparse(xp, "Example", json, reporter);
            assertNoErrorsAndClear(reporter);
            xd = xp.createXDDocument("Example");
            genXComponent(xp);
            xd.jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq("", testXdefJson(xdef, json, "Example"));
        } catch (Exception ex) {fail(ex);}
//if(true) return;
        try {
            for (File f : SUtils.getFileGroup(jsonDataDir+"Test*.xdef")) {
                String xdname = f.getName();
                xdname = xdname.substring(0, xdname.lastIndexOf('.'));
                File[] data = SUtils.getFileGroup(jsonDataDir+xdname+"*.json");
                String result;
                if (data == null || data.length == 0) {
                    result = testXdefJson(f, null, xdname);
                    if (!result.isEmpty()) {
                        fail(xdname + "\n"  + result);
                    }
                } else {
                    for (File g : data) {
                        result = testXdefJson(f, g, xdname);
                        if (!result.isEmpty()) {
                            fail(xdname+ "; data: " + g.getName() + "\n" + result );
                        }
                    }
                }
            }
        } catch (Exception ex) {fail(ex);}

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
