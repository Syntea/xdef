package buildtools;

import org.xdef.sys.FUtils;
import org.xdef.sys.RegisterReportTables;
import java.io.File;

/** Update registered message files.
 * @author Trojan
 */
public class GenReportTables {

	/** Generate error message files.
	 * @param args not used
	 */
	public static void main(String... args) {
		String packageName = "org.xdef.msg";
		File resourceDir = new File("src/main/resources/org/xdef/msg/");
		if (!resourceDir.exists() || !resourceDir.isDirectory()) {
			packageName = "cz.syntea.xdef.msg";
			resourceDir = new File("resources/cz/syntea/xdef/msg/");
			if (!resourceDir.exists() || !resourceDir.isDirectory()) {
				throw new RuntimeException(
					"Resources directory is not available");
			}
		}
		File srcDir = new File("src/main/java/org/xdef/msg/");
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			srcDir = new File("src/cz/syntea/xdef/msg/");
			if (!srcDir.exists() || !srcDir.isDirectory()) {
				throw new RuntimeException(
					"Java sources directory is not available");
			}
		}

		File temp = new File("temp");
		temp.mkdir();
		try {
			FUtils.deleteAll(temp, true);
			temp.mkdir();
			String msgPath = resourceDir.getAbsolutePath();
			msgPath = msgPath.replace('\\', '/');
			if (!msgPath.endsWith("/")) {
				msgPath += '/';
			}
			RegisterReportTables.main(new String[] {
				"-i", msgPath + "*.properties",
				"-p", packageName,
				"-c", "UTF-8",
				"-o", temp.getAbsolutePath()});
			String msg = 
				FUtils.updateDirectories(temp, srcDir, "java", true, false);
			System.out.println(// print info about changes
				msg.isEmpty() ? "Nothing changed in report files" : msg); 			
			FUtils.deleteAll(temp, true); // delete temp directory
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}