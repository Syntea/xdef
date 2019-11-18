package test.xdutils;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.Report;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.util.XValidate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import org.xdef.sys.ReportReader;
import buildtools.XDTester;

/** TestValidate.
 * @author Vaclav Trojan
 */
public class TestValidate extends XDTester {

	public TestValidate() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String dataDir = getDataDir() + "test/";
		if (dataDir == null) {
			fail("Data directory is missing, test canceled");
			return;
		}
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
			XValidate.main(new String[] {"-i", dataDir + "TestValidate.xml",
				"-l",  getTempDir() + "TestValidate.log"});
			repr = new FileReportReader(getTempDir() + "TestValidate.log");
			assertEq(((rep = repr.getReport()) == null ?
				null: rep.toString()).indexOf("I: File OK: "),
				0, rep);
			assertEq(null, rep = repr.getReport(), rep);
			repr.close();
			new File(getTempDir() + "TestValidate.log").delete();
			new File(getTempDir()).delete();
			XValidate.main(new String[] {"-i", dataDir + "TestValidate1.xml",
				"-l",  getTempDir() + "TestValidate1.log"});
			repr = new FileReportReader(getTempDir() + "TestValidate1.log");
			assertEq(((rep = repr.getReport()) == null ?
				null: rep.toString()).indexOf("E "), 0, rep);
			assertEq(((rep = repr.getReport()) == null ?
				null: rep.toString()).indexOf("E "), 0, rep);
			assertEq(null, rep = repr.getReport(), rep);
			repr.close();
			new File(getTempDir() + "TestValidate1.log").delete();
			new File(getTempDir()).delete();
			XValidate.main(new String[] {"-i", dataDir + "TestValidate2.xml",
				"-d", dataDir + "TestValidate.xdef",
				"-x", "Test1",
				"-l",  getTempDir() + "TestValidate2.log"});
			repr = new FileReportReader(getTempDir() + "TestValidate2.log");
			assertEq(((rep = repr.getReport()) == null ?
				null: rep.toString()).indexOf("I: File OK: "),
				0, rep);
			assertEq(null, rep = repr.getReport(), rep);
			repr.close();
			new File(getTempDir() + "TestValidate2.log").delete();
			new File(getTempDir()).delete();
			XDPool xp =	XDFactory.compileXD(null, dataDir+"TestValidate.xdef");
			ObjectOutputStream outpool = new ObjectOutputStream(
				new FileOutputStream(getTempDir() + "TestValidate3.xp"));
			outpool.writeObject(xp);
			outpool.close();
			XValidate.main(new String[] {"-i", dataDir + "TestValidate2.xml",
				"-p", getTempDir() + "TestValidate3.xp",
				"-x", "Test1",
				"-l", getTempDir() + "TestValidate3.log"});
			repr = new FileReportReader(getTempDir() + "TestValidate3.log");
			assertEq(((rep = repr.getReport()) == null ?
				null: rep.toString()).indexOf("I: File OK: "),
				0, rep);
			assertEq(null, rep = repr.getReport(), rep);
			repr.close();
		} catch (Exception ex) {
			fail(ex);
		}
		new File(getTempDir() + "TestValidate3.xp").delete();
		new File(getTempDir() + "TestValidate3.log").delete();
		new File(getTempDir()).delete();
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}

}