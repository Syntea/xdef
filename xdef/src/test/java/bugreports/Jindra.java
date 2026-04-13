package bugreports;

import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class Jindra extends XDTester {

    public Jindra() {super();}

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
        Element el;
        String json, s, xdef;
        XDPool xp;
        ArrayReporter reporter = new ArrayReporter();
        try {
            xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:name=\"P5R_json\" xd:root=\"P5R\">\n" +
"    <xd:json name=\"P5R\">\n" +
"        {\n" +
"            \"stavPoistenia\": \"jnumber()\"\n" +
"            \"obj\"          : { \"%script\": \"ref MyObj\" },\n" +
"        }\n" +
"    </xd:json>\n" +
"    <xd:json name = \"MyObj\">\n" +
"        { \"a\": \"jstring()\" }\n" +
"    </xd:json>\n" +
"</xd:def>";
            xp = XDFactory.compileXD(null, xdef);
            json = "{ \"stavPoistenia\": 123, \"obj\": { \"a\": \"ABC\" } }";
            jparse(xp, "", json, reporter);
            assertNoErrorsAndClear(reporter);
            System.out.println("OK");
        } catch (RuntimeException ex) {fail(ex);}
if(T)return;
        try {
            xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='P5R'>\n" +
"  <xd:json name=\"P5R\"> { \"stavPoistenia\": \"int(); option illegalJsonNull\" } </xd:json>\n" +
"</xd:def>";
            xp = compile(xdef);
            json = "{ \"stavPoistenia\": null }";
            jparse(xp, "", json, reporter);
            if (!reporter.errors()) {
                fail("Error not reported");
            } else {
                assertTrue(reporter.printToString().contains("XDEF809"));
            }
            xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='P5R' script='option illegalJsonNull' >\n" +
"  <xd:json name=\"P5R\"> { \"stavPoistenia\": \"int();\" } </xd:json>\n" +
"</xd:def>";
            xp = compile(xdef);
            json = "{ \"stavPoistenia\": null }";
            jparse(xp, "", json, reporter);
            if (!reporter.errors()) {
                fail("Error not reported");
            } else {
                assertTrue(reporter.printToString().contains("XDEF809"));
            }
            xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='P5R' script='option illegalJsonNull' >\n" +
"  <xd:json name=\"P5R\"> { \"stavPoistenia\": \"int(); option acceptJsonNull\" } </xd:json>\n" +
"</xd:def>";
            xp = compile(xdef);
            json = "{ \"stavPoistenia\": null }";
            jparse(xp, "", json, reporter);
            assertNoErrorwarnings(reporter);
            System.out.println("OKOK");
        } catch (RuntimeException ex) {fail(ex);}
if(T)return;

        clearTempDir(); // delete temporary files.
    }

    /** Run test
     * @param args the command line arguments
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {
            System.exit(1);
        }
    }
}
