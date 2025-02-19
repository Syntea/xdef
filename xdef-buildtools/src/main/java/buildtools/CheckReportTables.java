package buildtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** Check message reports tables for not existing or unused messages.
 * @author Vaclav Trojan
 */
public class CheckReportTables {

	public CheckReportTables() {super();}

	static boolean hasField(File f, String name) throws Exception {
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while((line = br.readLine()) != null) {
				if (line.indexOf(name) > 0) {
					br.close();
					return true;
				}
			}
		}
		return false;
	}

	static boolean hasField(String dir, String name) throws Exception {
		File[] files = new File(dir).listFiles();
		if (files != null) {
			for (File f: new File(dir).listFiles()) {
				if (f.getName().endsWith(".java") && !f.getName().endsWith("ScriptCodeTable.java")) {
					if (hasField(f, name)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Check unused messages. */
	private static void chkUnusedMesssges(final String msgdir, final String prefix, final String[] dirs)
		throws Exception {
		System.out.println("Checking " + prefix);
		String fname = msgdir + prefix + ".java";
		ArrayList<String> names = new ArrayList<>(), unused = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fname))) {
			String line;
			String description = prefix + "_DESCRIPTION";
			String language = prefix + "_LANGUAGE";
			while((line = br.readLine()) != null) {
				int i,j;
				if ((i = line.indexOf("long")) > 0 && (j = line.indexOf('=')) > 0) {
					String name = line.substring(i + 5, j).trim();
					if (!description.equals(name) && !language.equals(name)) {
						names.add(name);
					}
				}
			}
		}
		for (String name: names) {
			boolean found = false;
			for (String chkdir: dirs) {
				if (hasField(chkdir, name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				unused.add(name);
			}
		}
		if (!unused.isEmpty()) {
			for (String name: unused) {
				System.out.println("  unused: " + name);
			}
		}
	}

	/** Find directories with Java source files. */
	private static void addDirs(final File f, final ArrayList<String> srcDirs, final String[] exclude) {
		if (!f.isDirectory()) {
			return;
		}
		for (String dir: exclude) {
			if (f.getAbsolutePath().replace('\\', '/').endsWith(dir)) {
				return;
			}
		}
		File[] files = f.listFiles();
		boolean wasJava = false;
		for (File g : files) {
			if (g.isDirectory()) {
				addDirs(g, srcDirs, exclude);
			}
			if (!wasJava && g.getName().endsWith(".java")) {
				wasJava = true;
			}
		}
		if (wasJava) {
			srcDirs.add(f.getAbsolutePath());
		}
	}

	/** Find directories with Java source files. */
	private static String[] getJavaDirectiories(final String[] sourceBases, final String[] exclude) {
		ArrayList<String> srcDirs = new ArrayList<>();
		for (String directory: sourceBases) {
			File f = new File(directory);
			addDirs(f, srcDirs, exclude);
		}
		String[] result = new String[srcDirs.size()];
		return srcDirs.toArray(result);
	}

	/** Run checking reports in org.xdef.
	 * @param args the command line arguments
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception {
		String dir;
		try {
			File baseDir = args == null || args.length == 0 ? new File("../xdef") : new File(args[0]);
			if (!baseDir.exists() || !baseDir.isDirectory()) {
				throw new IOException("Base is not directory.");
			}
			dir = baseDir.getCanonicalPath().replace('\\', '/');
			if (!dir.endsWith("/")) {
				dir += '/';
			}
		} catch (IOException ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		
		dir += "src/main/java/";
		System.out.println("  Directory: " + dir);
		String[] dirs = getJavaDirectiories(new String[] {dir + "org"},	new String[] {"org/xdef/msg"});
		String msgdir = dir+"org/xdef/msg/";
		chkUnusedMesssges(msgdir, "BNF", dirs);
		chkUnusedMesssges(msgdir,"JSON", dirs);
		chkUnusedMesssges(msgdir,"SYS", dirs);
		chkUnusedMesssges(msgdir,"XDEF", dirs);
		chkUnusedMesssges(msgdir,"XML", dirs);
	}
}