package builtools;

import org.xdef.sys.FUtils;
import org.xdef.sys.RegisterReportTables;
import java.io.File;

/** Update registered message files.
 * @author Trojan
 */
public class UpdateMessages {

	/** Generate error message files.
	 * @param args not used
	 */
	public static void main(String... args) {
		File dir = new File("src/main/resources/org/xdef/msg/");
		if (!dir.exists() || !dir.isDirectory()) {
			dir = new File("src/org/xdef/msg/");
			if (!dir.exists() || !dir.isDirectory()) {
				throw new RuntimeException(
					"Resources directory is not available");
			}
		}
		File srcDir = new File("src/main/java/org/xdef/msg/");
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			srcDir = new File("src/org/xdef/msg/");
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
			String msgPath = dir.getAbsolutePath();
			msgPath = msgPath.replace('\\', '/');
			if (!msgPath.endsWith("/")) {
				msgPath += '/';
			}
			RegisterReportTables.main(new String[] {
				"-i", msgPath + "*.properties",
				"-c", "UTF-8",
				"-p", "org.xdef.msg",
				"-o", temp.getAbsolutePath(),
				"-r"});
			String msg = 
				FUtils.updateDirectories(temp, srcDir, "java", true, false);
			if (msg.isEmpty()) {
				System.out.println("Nothing changed in report files");
			} else {
				System.out.println(msg); // print changes
			}
			
			FUtils.deleteAll(temp, true); // delete temp directory
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}