package buildtools;

import java.io.File;

/** Canonize sources.
 * <p>1. Remove all white spaces after last nonblank character
 * at the end of line and replace leading spaces by tabs.</p>
 * <p>2. Check and generate message report classes.</p>
 * @author  Vaclav Trojan
 */
public class Canonize {

	private Canonize() {}

	/** if true the header (copyright) info text is generated from the file. */
	static boolean _hdr = false;
	/** if true the _tail (modification) info text is generated from the file. */
	static boolean _tail = false;

	/** Canonize sources. Remove all trailing white spaces on all lines and
	 * handle with leading spaces an all lines according to argument
	 * <tt>tabs</tt>.
	 * Insert or update header or _tail information to sources
	 * according to value of arguments <tt>_hdr</tt> and <tt>_tail</tt>. If the
	 * argument <tt>recurse</tt> is true, do it with all specified files in
	 * child directories.
	 * @param filename The name of file (wildcards are possible).
	 * @param recurse If <tt>true<tt> then recurse process in child
	 * subdirectories.
	 * @param tabs If <tt>true</tt> then leading spaces are replaced
	 * by the tabelator (4 spaces for 1 tab).
	 * @param hdr If <tt>true</tt> then leading standard copyright information
	 * is inserted before the first line of Java source or it replaces the
	 * existing one. The template for the copyright information is taken from
	 * the file <tt>hdrinfo.txt</tt>the root directory <tt>java</tt> (under
	 * which are projects).If the argument's value is <tt>false</tt> then
	 * the top of source remains unchanged.
	 * @param tail If <tt>true</tt> then log information is added after the last
	 * line of Java source or it replaces the existing one.The template used for
	 * the log information is taken from the file <tt>tailinfo.txt</tt> in the
	 * root directory <tt>java</tt> (under which are projects). If the value
	 * of this argument is <tt>false</tt> then the end source remains unchanged.
	 */
	private static void doSources(
		String filename,
		boolean recurse,
		boolean tabs) {
		if (filename.endsWith("/data")) {
			return;
		}
		String home = Preproc.getProjectHomeDir(Canonize.class);
		if (home.endsWith("/")) {
			home = home.substring(0, home.length() - 1);
		}
		int i = home.lastIndexOf('/');
		if (i < 0) {
			throw new RuntimeException("Unknown build structure");
		}
		String hdrTemplate = null;
		String tailTemplate = null;
		if (recurse) {
			System.out.println("Directory: " + home + "/" + filename);
		} else {
			System.out.println("Directories: " + home + "/" + filename);
		}
		CanonizeSource.canonize(home + "/" + filename + "/*.java",
			recurse,
			tabs,
			tabs ? 4 : 2,
			hdrTemplate, tailTemplate, GenConstants.JAVA_SOURCE_CHARSET);
		CanonizeSource.canonize(home + "/" + filename + "/*.xml",
			recurse,
			false,
			tabs ? 4 : 2,
			null, null, GenConstants.JAVA_SOURCE_CHARSET);
		CanonizeSource.canonize(home + "/" + filename + "/*.html",
			recurse,
			false,
			tabs ? 4 : 2,
			null, null, GenConstants.JAVA_SOURCE_CHARSET);
		CanonizeSource.canonize(home + "/" + filename + "/*.xdef",
			recurse,
			false,
			tabs ? 4 : 2,
			null, null, GenConstants.JAVA_SOURCE_CHARSET);
	}

	/** Canonize sources.
	 * @param args array with command line parameters.
	 */
	public static void main(String[] args) {
		_hdr = false;
		_tail = false;
		String projectBase;
		try {
			projectBase = new File(".").getCanonicalPath().replace('\\', '/');
		} catch (Exception ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		if (projectBase.endsWith("/")) {
			projectBase = projectBase.substring(0, projectBase.length() - 1);
		}
		int i = projectBase.lastIndexOf('/');
		if (i < 0) {
			throw new RuntimeException("Unknown build structure");
		}
		// Java source files: recurse directories, the second parameter is true.
		doSources("src/main/java/org", true, true);
		doSources("src/test/java/test/common", false, true);
		doSources("src/test/java/test/utils", false, true);
		doSources("src/test/java/test/xdef", false, true);
		doSources("src/test/java/test/xdutils", false, true);
		doSources("src/test/java/mytest", false, true);
		doSources("src/test/java/buildtools", true, true); //this directory
		GenReportTables.main();
	}

}