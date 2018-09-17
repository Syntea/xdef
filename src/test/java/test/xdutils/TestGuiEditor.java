package test.xdutils;

import cz.syntea.xdef.proc.XXNode;

/** Test GUI editor
 * @author Vaclav Trojan
 */
public class TestGuiEditor {

	public static String test(XXNode x) {return "Method test; ";}

	public static void main(String[] args) throws Exception {
		cz.syntea.xdef.util.GUIEditor.main(new String[] {
			"-p", "test/test/xdutils/data/GUI/validate/project.xml"});
		System.out.println("validate OK");
//		cz.syntea.xdef.util.GUIEditor.main(new String[] {
//			"-p", "test/test/xdutils/data/GUI/construct/project.xml"});
//		System.out.println("construct OK");
//		cz.syntea.xdef.util.GUIEditor.main(new String[] {
//			"-p", "test/test/xdutils/data/GUI/test/project.xml"});
//		System.out.println("test OK");
	}

}