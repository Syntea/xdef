package bugreports;

import static org.xdef.sys.STester.runTest;
import org.xdef.util.GUIEditor;
import test.XDTester;

/** Test GUI editor.
 * @author Vaclav Trojan
 */
public class TestGUI extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
		String projectDir = "src/test/java/bugreports/data/project/";
		clearTempDir();
//		GUIEditor.main("-g", "-workDir", "temp");
//		GUIEditor.main("-p", projectDir + "json/project.xml");
		GUIEditor.main("-p", projectDir + "validate/project.xml");
	}
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}