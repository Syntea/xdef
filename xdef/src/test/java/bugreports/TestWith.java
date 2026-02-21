package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class TestWith extends XDTester {
    Integer vin;
    public void setMaxWeight(Integer x) {vin = x; }
    public Integer getMaxWeight() { return vin; }
    public void setVIN(Integer x) { vin = x; }
    public Integer getVIN() { return vin; }

    /** Run test and display error information. */
    @Override
    public void test() {
        System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
        System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
        setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
        setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
        Object o;
        String json, xdef, xml;
        XDDocument xd;
        XDPool xp;
        XComponent xc;
        ArrayReporter reporter = new ArrayReporter();
/**/
        try {
            xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" xd:root=\"Truck\">\n" +
"    <Truck MaxWeight = \"required int()\" />\n" +
"    <xd:component>\n" +
"        %bind VIN %with bugreports.MyTestWith1 %link #Truck/@MaxWeight;\n" +
"        %class bugreports.TestWith1 extends bugreports.TestWith %link #Truck;\n" +
"    </xd:component>\n" +
"</xd:def>";
            xp = compile(xdef);
            genXComponent(xp);
            xd = xp.createXDDocument("");
            xml = "<Truck MaxWeight = \"1234\" />";
            assertEq(xml, parse(xp, "", xml, reporter));
            assertNoErrorsAndClear(reporter);
            xc = xd.xparseXComponent(xml, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq(xml, xc.toXml());
            assertEq(1234, XComponentUtil.get(xc, "VIN"));
        } catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**/
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