package test.xdutils;

import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import org.xdef.util.PrettyXdef;
import java.io.File;
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
		return KXmlUtils.compareXML(
			params[1], params[params.length - 1], true);
	}

	@Override
	public void test() {
		String dataDir = getDataDir();
		String tempDir = getTempDir();
		File f = new File(tempDir);
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			assertNoErrors(chkPrettyXDef(
				"-o", tempDir + "TestValidate.xdef",
				dataDir + "test/TestValidate.xdef"));
			assertNoErrors(chkPrettyXDef(
				"-o", tempDir + "Igor02_xd.xml",
				"-i", "8",
				"-e", "UTF-8",
				dataDir + "test/Igor02_xd.xml"));
			assertNoErrors(chkPrettyXDef(
				"-o", tempDir + "Matej2_L1_common.xdef",
				"-i", "0",
				dataDir + "test/Matej2_L1_common.def"));
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			FUtils.deleteAll(tempDir, true);
		} catch (SException ex) {
			throw new RuntimeException("Could not delete temporary files!", ex);
		}
	}

	/** Run test
	 * @param args ignored
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}