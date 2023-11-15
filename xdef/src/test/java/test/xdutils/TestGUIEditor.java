package test.xdutils;

import java.io.File;
import org.xdef.proc.XXNode;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.GUIEditor;
import test.XDTester;

/** Test GUI editor
 * @author Vaclav Trojan
 */
public class TestGUIEditor  extends XDTester {

	public TestGUIEditor() {super();}

	public static final String test(final XXNode x) {return "Method test; ";}

	private void test(final String... params) {
		GUIEditor.main(params);
		System.out.println(
			"OK " + params[0] + (params.length > 1 ? " " + params[1] : ""));
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
////////////////////////////////////////////////////////////////////////////////
			File f = new File(clearTempDir(),  "newProject");
			f.mkdirs(); // work directory for created project
			String wdir = f.getCanonicalPath();
////////////////////////////////////////////////////////////////////////////////
//			test("-v", "-workDir", wdir);
//			test("-v", "-format", "JSON");
//			test("-c", "-workDir", wdir);
//			test("-g", "-workDir", wdir);
//			test("-c", getDataDir()+"GUI/test/project.xml", "-workDir", wdir);
//			test("-g", getDataDir()+"GUI/validate/data.xml", "-workDir", wdir);
////////////////////////////////////////////////////////////////////////////////
			test("-p", getDataDir()+"GUI/test/project.xml");
//			test("-p", getDataDir()+"GUI/validate/project.xml");
//			test("-p", getDataDir()+"GUI/json/project.xml");
//			test("-p", getDataDir()+"GUI/construct/project.xml");
		} catch (Exception ex) {fail(ex);}
		clearTempDir(); // clear temporary directory
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}