package buildtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/** Check message reports tables for not existing or unused messages.
 * @author Vaclav Trojan
 */
public class CheckReportTables  extends STester {
	
	public CheckReportTables() {super();}

	static boolean hasField(File f, String name) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			if (line.indexOf(name) > 0) {
				br.close();
				return true;
			}
		}
		br.close();
		return false;
	}

	static boolean hasField(String dir, String name)
		throws Exception {
		File[] files = new File(dir).listFiles();
		if (files != null) {
			for (File f: new File(dir).listFiles()) {
				if (f.getName().endsWith(".java") &&
					!f.getName().endsWith("ScriptCodeTable.java")) {
					if (hasField(f, name)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Check unused messages. */
	private static void chkUnusedMesssges(final String msgdir,
		final String prefix,
		final String[] dirs) throws Exception {
		String fname = msgdir + prefix + ".java";
		BufferedReader br = new BufferedReader(new FileReader(fname));
		String line;
		String description = prefix + "_DESCRIPTION";
		String language = prefix + "_LANGUAGE";
		ArrayList<String> names = new ArrayList<String>();
		while((line = br.readLine()) != null) {
			int i,j;
			if ((i = line.indexOf("long")) > 0 &&
				(j = line.indexOf('=')) > 0) {
				String name = line.substring(i + 5, j).trim();
				if (!description.equals(name) && !language.equals(name)) {
					names.add(name);
				}
			}
		}
		ArrayList<String> unused = new ArrayList<String>();
		br.close();
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
				System.out.println("	unused: " + name);
			}
		}
	}

	/** Find directories with Java source files. */
	private static void addDirs(final File f,
		final ArrayList<String> srcDirs,
		final String[] exclude) {
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
	private static String[] getJavaDirectiories(final String[] sourceBases,
		final String[] exclude) {
		ArrayList<String> srcDirs = new ArrayList<String>();
		for (String directory: sourceBases) {
			File f = new File(directory);
			addDirs(f, srcDirs, exclude);
		}
		String[] result = new String[srcDirs.size()];
		return srcDirs.toArray(result);
	}

	@Override
	public void test() {
		String dir = super.getHomeDir() + "/src/main/java/";
		System.out.println(dir);
		try {
			String[] dirs = getJavaDirectiories(
				new String[] {dir + "org"},
				new String[] {"org/xdef/msg"});
			String msgdir = dir+"org/xdef/msg/";
			chkUnusedMesssges(msgdir, "BNF", dirs);
			System.out.println("Checking " + "JSON");
			chkUnusedMesssges(msgdir,"JSON", dirs);
			System.out.println("Checking " + "SYS");
			chkUnusedMesssges(msgdir,"SYS", dirs);
			System.out.println("Checking " + "XDEF");
			chkUnusedMesssges(msgdir,"XDEF", dirs);
			System.out.println("Checking " + "XML");
			chkUnusedMesssges(msgdir,"XML", dirs);
		} catch (Exception ex) {fail(ex);}
	}
	
	/** Run checking reports.
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}