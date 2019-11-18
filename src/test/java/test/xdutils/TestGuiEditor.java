package test.xdutils;

import org.xdef.proc.XXNode;
import org.xdef.util.GUIEditor;
import buildtools.XDTester;

/** Test GUI editor
 * @author Vaclav Trojan
 */
public class TestGuiEditor  extends XDTester {

	public TestGuiEditor() {super();}

	public static final String test(final XXNode x) {return "Method test; ";}

	private void test(final String... params) {
		GUIEditor.main(params);
		System.out.println(
			"OK " + params[0] + (params.length > 1 ? " " + params[1] : ""));
	}

	@Override
	/** Run test and print error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
//		test(new String[] {"-v"});
//		test(new String[] {"-c"});
//		test(new String[] {"-g"});
//		test(new String[] {"-p", getDataDir() + "GUI/test/project.xml"});
		test(new String[] {"-p", getDataDir() + "GUI/validate/project.xml"});
//		test(new String[] {"-p", getDataDir() + "GUI/construct/project.xml"});
//		test(new String[] {"-g", getDataDir() + "GUI/validate/data.xml"});
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}

}