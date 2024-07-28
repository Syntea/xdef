package buildtools;

import java.io.File;
import java.io.IOException;

/** Canonize sources.
 * <p>1. Remove all white spaces after last non-blank character
 * at the end of line and replace leading spaces by tabs.
 * <p>2. Check and generate message report classes.
 * @author  Vaclav Trojan
 */
public class Canonize {
	/** Charset of sources. */
	static final String JAVA_SOURCE_CHARSET = "UTF-8";

	/** just prevent user to create an instance of this class. */
	private Canonize() {}

	/** Canonize sources. Remove all trailing white spaces on all lines and
	 * handle with leading spaces an all lines according to argument
	 * <code>tabs</code>.
	 * Insert or update header or _tail information to sources
	 * according to value of arguments <code>_hdr</code> and <code>_tail</code>.
	 * If the argument <code>dirTree</code> is true, do it with all specified
	 * files in child directories.
	 * @param projectBase file name of project directory.
	 * @param filename name of file (wildcards are possible).
	 * @param dirTree If <code>true<code> then dirTree process in child
	 * subdirectories.
	 * @param hdr If <code>true</code> then leading standard copyright
	 * information is inserted before the first line of Java source or it
	 * replaces the existing one. The template for the copyright information is
	 * taken from the file <code>hdrinfo.txt</code>the root directory
	 * <code>java</code> (under which are projects).If the argument's value
	 * is <code>false</code> then the top of source remains unchanged.
	 * @param tail If <code>true</code> then log information is added after the
	 * last line of Java source or it replaces the existing one.The template
	 * used for the log information is taken from the file
	 * <code>tailinfo.txt</code> in the root directory
	 * <code>java</code> (under which are projects). If the value of this
	 * argument is <code>false</code> then the end source remains unchanged.
	 */
	private static void doSources(final String projectBase,
		final String filename,
		final boolean dirTree) {
		try {
			File f = new File(projectBase, filename).getCanonicalFile();
			if (!f.exists() || !f.isDirectory()) {
				System.err.println("[ERROR] " + f.getAbsolutePath()
					+ " not exists or it is not directory");
				return;
			}
			String home = f.getAbsolutePath().replace('\\', '/');
			if (!home.endsWith("/")) {
				home += '/';
			}
			if (home.endsWith("/data/")) {
				return; //do not process data directories
			}

			String hdrTemplate = null;
			String tailTemplate = null;
			System.out.println("Directory: " + home);
			CanonizeSource.canonize(home + "*.java",
				dirTree,
				true,
//				false,
				4,
				hdrTemplate, tailTemplate, JAVA_SOURCE_CHARSET, true);
//			CanonizeSource.canonize(home + "*.xml",
//				dirTree,
//				false,
//				-1,
//				null, null, JAVA_SOURCE_CHARSET, true);
//			CanonizeSource.canonize(home + "*.html",
//				dirTree,
//				false,
//				-1,
//				null, null, JAVA_SOURCE_CHARSET, true);
//			CanonizeSource.canonize(home + "*.xdef",
//				dirTree,
//				false,
//				-1,
//				null, null, JAVA_SOURCE_CHARSET, true);
			CanonizeSource.canonize(home + "*.properties",
				dirTree,
				false,
				-1,
				null, null, JAVA_SOURCE_CHARSET, true);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Canonize sources.
	 * @param args array with command line parameters (no parameters used).
	 */
	public static void main(String... args) {
		String projectBase;
		try {
			File baseDir = args == null || args.length == 0
				? new File("../xdef") : new File(args[0]);
			if (!baseDir.exists() || !baseDir.isDirectory()) {
				throw new IOException("Base is not directory.");
			}
			projectBase = baseDir.getCanonicalPath().replace('\\', '/');
		} catch (IOException ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		ResetPreprocessorSwitches.main(projectBase);
		int i = projectBase.lastIndexOf('/');
		if (i < 0) {
			throw new RuntimeException("Unknown build structure");
		}
		// Canonize sources: replace leading spaces with tabs and remove
		// trailing white spaces.
		doSources(projectBase, "/src/main/java", true);
		doSources(projectBase, "/src/main/resources/org", true);
		doSources(projectBase, "/src/test/java", true);

		// register report messages
		GenReportTables.main(projectBase);

//		// update date in files changelog.md and in pom.xml
//		String date = String.format("%tF", new Date()); // actual date
//		updateDateInChangeLog(projectBase, date);

//		updateDateInPomXml(date);
	}
}