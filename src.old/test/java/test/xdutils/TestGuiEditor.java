package test.xdutils;

import cz.syntea.xdef.proc.XXNode;
import test.utils.XDTester;

/** Test GUI editor
 * @author Vaclav Trojan
 */
public class TestGuiEditor  extends XDTester {

	public TestGuiEditor() {super();}

	public static final String test(final XXNode x) {return "Method test; ";}

	private void test(final String... params) {
		cz.syntea.xdef.util.GUIEditor.main(params);
		System.out.println("OK " + params[0] +
			(params.length > 1 ? " " + params[1] : ""));
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String dataDir = getDataDir() + "GUI/";
////////////////////////////////////////////////////////////////////////////////
//		test(new String[] {"-p", dataDir + "validate/project.xml"});
//		test(new String[] {"-p", dataDir + "construct/project.xml"});
//		test(new String[] {"-p", dataDir + "test/project.xml"});
		test(new String[] {"-v"});
//		test(new String[] {"-p", "C:/temp/project.xml"});
//		test(new String[] {"-c"});
//		test(new String[] {
//			"-v",
//			"-xdef", dataDir + "validate/xdef.xml",
//			"-data", dataDir + "validate/data.xml",
//			"-debug",
////			"-editInput",
//			"-displayResult",
//		});
//		test(new String[] {
//			"-c",
//			"-xdef", dataDir + "construct/xdef.xml",
//			"-data", dataDir + "construct/data.xml",
//			"-debug",
//			"-editInput",
//			"-displayResult"
//		});
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}

}