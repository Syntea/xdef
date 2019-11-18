package buildtools;

import java.io.File;
import org.xdef.sys.SUtils;

/** Clean source files with XComponents.
 * @author Vaclav Trojan
 */
public class XComponentsClean {

	/** Clean source files with XComponents.
	 * @param args not used.
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception {
		File f = new File("test");
		if (!f.exists() || !f.isDirectory()) {
			f = new File("src/test/java");
			if (!f.isDirectory()) {
				throw new Exception("Test directory is not available!");
			}
		}		
		if ((f=new File(f, "test/xdef/component")).exists() && f.isDirectory()){
			SUtils.deleteAll(f, true);
			System.out.println("All XComponents deleted.");
		} else {
			System.out.println("No XComponents to be deleted.");
		}
	}
}
