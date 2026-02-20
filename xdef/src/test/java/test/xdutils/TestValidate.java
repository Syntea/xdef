package test.xdutils;

import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.Report;
import org.xdef.sys.ReportReader;
import org.xdef.util.XValidate;
import java.io.File;
import java.io.IOException;
import org.xdef.sys.SException;
import test.XDTester;

/** TestValidate.
 * @author Vaclav Trojan
 */
public class TestValidate extends XDTester {

    public TestValidate() {super();}

    /** Run test and print error information. */
    @Override
    public void test() {
        String dataDir = getDataDir() + "test/";
        if (dataDir == null) {
            fail("Data directory is missing, test canceled");
            return;
        }
        File tempDir = clearTempDir();
        File xmlFile;
        XDDocument xd;
        ArrayReporter reporter = new ArrayReporter();
        Report rep;
        ReportReader repr;
        try {
            xmlFile = new File(dataDir + "TestValidate.xml");
            reporter.clear();
            xd = XValidate.validate(null, xmlFile, reporter);
            if (xd == null || reporter.errorWarnings()) {
                if (reporter.getErrorCount() != 0) {
                    fail("Errors: " + reporter.printToString()); //2
                }
            }
            xmlFile = new File(dataDir + "TestValidate1.xml");
            reporter.clear();
            xd = XValidate.validate(null, xmlFile, reporter);
            if (xd == null || reporter.errors()) {
                if (reporter.getErrorCount() != 2) {
                    fail("Errors: " + reporter.printToString()); //2
                }
            }
            XValidate.main(new String[] {"-i",
                dataDir + "TestValidate.xml",
                "-l",
                new File(tempDir, "TestValidate.log").getCanonicalPath()});
            repr = new FileReportReader(new File(tempDir, "TestValidate.log"));
            assertEq(((rep = repr.getReport()) == null ?
                null: rep.toString()).indexOf("I: File OK: "),
                0, rep);
            assertEq(null, rep = repr.getReport(), rep);
            repr.close();
            new File(tempDir, "TestValidate.log").delete();
            XValidate.main(new String[] {"-i", dataDir + "TestValidate1.xml",
                "-l",
                new File(tempDir, "TestValidate1.log").getCanonicalPath()});
            repr = new FileReportReader(new File(tempDir, "TestValidate1.log"));
            assertEq(((rep = repr.getReport()) == null
                ? 999 : rep.toString().indexOf("E ")), 0, rep);
            assertEq(((rep = repr.getReport()) == null
                ? 999 : rep.toString().indexOf("E ")), 0, rep);
            assertEq(null, rep = repr.getReport(), rep);
            repr.close();
            new File(tempDir, "TestValidate1.log").delete();
            XValidate.main(new String[] {"-i", dataDir + "TestValidate2.xml",
                "-d", dataDir + "TestValidate.xdef",
                "-x", "Test1",
                "-l",
                new File(tempDir, "TestValidate2.log").getCanonicalPath()});
            repr = new FileReportReader(new File(tempDir, "TestValidate2.log"));
            assertEq(((rep = repr.getReport()) == null ? null: rep.toString()).indexOf("I: File OK: "),0,rep);
            assertNull(rep = repr.getReport(), rep);
            repr.close();
            new File(tempDir, "TestValidate2.log").delete();
            XDPool xp =	compile(dataDir+"TestValidate.xdef");
            XDFactory.writeXDPool(new File(tempDir, "TestValidate3.xp"), xp);
            XValidate.main(new String[] {"-i", dataDir + "TestValidate2.xml",
                "-p", new File(tempDir, "TestValidate3.xp").getCanonicalPath(),
                "-x", "Test1",
                "-l",
                new File(tempDir, "TestValidate3.log").getCanonicalPath()});
            repr = new FileReportReader(new File(tempDir, "TestValidate3.log"));
            assertEq(((rep = repr.getReport()) == null ? 999 : rep.toString().indexOf("I: File OK: ")),0,rep);
            assertNull(rep = repr.getReport(), rep);
            repr.close();

            new File(tempDir, "TestValidate3.xp").delete();
            new File(tempDir, "TestValidate3.log").delete();
        } catch (IOException | SException | RuntimeException ex) {fail(ex);}

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