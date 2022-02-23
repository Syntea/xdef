package bugreports;

import static org.xdef.sys.STester.runTest;
import org.xdef.util.GUIEditor;
import test.XDTester;

/**
 * @author Vaclav Trojan
 */
public class TestGUI extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
//		GUIEditor.main("-h");
//		GUIEditor.main("-g");
		GUIEditor.main("-g", "-tempDir", "temp");
		GUIEditor.main("-p", "temp/project.xml") ;
//		GUIEditor.main("-p",
//			"src/test/java/mytests/projects/json/project.xml"
////			"-workDir", "temp"
//		);
	}
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}