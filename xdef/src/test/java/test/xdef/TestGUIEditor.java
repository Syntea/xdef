package test.xdef;

import org.xdef.util.GUIEditor;
import test.XDTester;

/** Tset GUIEditor
 * @author Trojan
 */
public class TestGUIEditor extends XDTester {
	TestGUIEditor() {super();}
	@Override
	/** Run test and display error information. */
	public void test() {
		try {
			GUIEditor.main("-p",
				"src/test/resources/test/xdef/projects/test/project.xml");
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}