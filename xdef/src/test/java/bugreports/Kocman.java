package bugreports;

import java.io.StringWriter;
import java.util.Map;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDConstants;
import org.xdef.impl.XConstants;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;

public class Kocman extends XDTester {
    /** Run test and display error information. */
    @Override
    public void test() {
////////////////////////////////////////////////////////////////////////////////
        boolean T = false; // If the value is false, all tests are run; otherwise, only the first one is run
////////////////////////////////////////////////////////////////////////////////
        System.out.println("X-definition version: " + XDFactory.getXDVersion());
        System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
        setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
        setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
        Object o, x, j;
        String json, s, xdef;
        XDPool xp;
        StringWriter swr;
        ArrayReporter reporter = new ArrayReporter();
        try {
            xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:name=\"Example\" xd:root=\"test\">\n" +
"    <xd:json name = \"test\">\n" +
"      {\n" +
"          \"code\": \"num(4)\"\n" +
"      }\n" +
"    </xd:json>" +
"</xd:def>";
            json = "{\"code\" : \"4120\"}";
            j = XonUtils.parseJSON(json);
            xp = compile(xdef);
            o = jparse(xp, "Example", json, reporter, null, null, null);
            assertNoErrorsAndClear(reporter);
            x = XonUtils.parseJSON(XonUtils.toJsonString(o));
            System.out.println(x);
            Map m = (Map) x;
            assertTrue(XonUtils.xonEqual(x, j));
        } catch (RuntimeException ex) {fail(ex);}
        clearTempDir(); // delete temporary files.
    }

    public static void main(String[] args) {
        XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {
            System.exit(1);
        }
    }
}
