package test.xdutils;

import java.io.File;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import org.xdef.util.PrettyXdef;
import org.xdef.sys.ReportWriter;
import test.XDTester;

/** Test pretty indentation of XDefinition.
 * @author Trojan
 */
public class TestPrettyXdef extends XDTester {

    public TestPrettyXdef() {super();}

    private ReportWriter chkPrettyXDef(final String... params) throws Exception{
        PrettyXdef.main(params);
        KDOMBuilder kd = new KDOMBuilder();
        kd.setNamespaceAware(true);
        kd.parse(params[1]); //just check XML
        return KXmlUtils.compareElements(
            params[1], params[params.length - 1], true);
    }

    @Override
    public void test() {
        String dataDir = getDataDir();
        File tempDir = clearTempDir();
        try {
            assertNoErrorwarnings(chkPrettyXDef("-o",
                new File(tempDir, "TestValidate.xdef").getCanonicalPath(),
                dataDir + "test/TestValidate.xdef"));
            assertNoErrorwarnings(chkPrettyXDef("-o",
                new File(tempDir, "Igor02_xd.xml").getCanonicalPath(),
                "-i", "8",
                "-e", "UTF-8",
                dataDir + "test/Igor02_xd.xml"));
            assertNoErrorwarnings(chkPrettyXDef("-o",
                new File(tempDir, "Matej2_L1_common.xdef").getCanonicalPath(),
                "-i", "0",
                dataDir + "test/Matej2_L1_common.def"));

            new File(tempDir, "TestValidate.xdef").delete();
            new File(tempDir, "Igor02_xd.xml").delete();
            new File(tempDir, "Matej2_L1_common.xdef").delete();
        } catch (Exception ex) {fail(ex);}

        clearTempDir(); // delete temporary files.
    }

    /** Run test
     * @param args ignored
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        runTest();
    }
}