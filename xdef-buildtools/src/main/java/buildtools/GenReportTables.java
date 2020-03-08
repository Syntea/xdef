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
		File resourceDir =
			new File("../xdef/src/main/resources/org/xdef/msg/");
		if (!resourceDir.exists() || !resourceDir.isDirectory()) {
			throw new RuntimeException("Resources directory is not available: "
				+ resourceDir.getAbsolutePath());
		}
		File srcDir = new File("../xdef/src/main/java/org/xdef/msg/");
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			throw new RuntimeException("Java msg directory is not available: "
				+ srcDir.getAbsolutePath());
		}
		try {
			resourceDir = resourceDir.getCanonicalFile();
			srcDir = srcDir.getCanonicalFile();
			File temp = new File("temp").getCanonicalFile();
			temp.mkdir();
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
				"-o", temp.getAbsolutePath().replace('\\', '/')});
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