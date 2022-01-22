package buildtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;

/** Canonize sources.
 * <p>1. Remove all white spaces after last non-blank character
 * at the end of line and replace leading spaces by tabs.
 * <p>2. Check and generate message report classes.
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
	 * argument <tt>dirTree</tt> is true, do it with all specified files in
	 * child directories.
	 * @param filename The name of file (wildcards are possible).
	 * @param dirTree If <tt>true<tt> then dirTree process in child
	 * subdirectories.
	 * @param hdr If true then leading standard copyright information
	 * is inserted before the first line of Java source or it replaces the
	 * existing one. The template for the copyright information is taken from
	 * the file <tt>hdrinfo.txt</tt>the root directory <tt>java</tt> (under
	 * which are projects).If the argument's value is false then
	 * the top of source remains unchanged.
	 * @param tail If true then log information is added after the last
	 * line of Java source or it replaces the existing one.The template used for
	 * the log information is taken from the file <tt>tailinfo.txt</tt> in the
	 * root directory <tt>java</tt> (under which are projects). If the value
	 * of this argument is false then the end source remains unchanged.
	 */
	private static void doSources(final String filename,
		final boolean dirTree) {
		try {
			File f = new File(filename).getCanonicalFile();
			if (!f.exists() || !f.isDirectory()) {
				System.err.println(f.getAbsolutePath()
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
				4,
				hdrTemplate, tailTemplate, Constants.JAVA_SOURCE_CHARSET);
//			CanonizeSource.canonize(home + "*.xml",
//				dirTree,
//				false,
//				-1,
//				null, null, Constants.JAVA_SOURCE_CHARSET);
//			CanonizeSource.canonize(home + "*.html",
//				dirTree,
//				false,
//				-1,
//				null, null, Constants.JAVA_SOURCE_CHARSET);
//			CanonizeSource.canonize(home + "*.xdef",
//				dirTree,
//				false,
//				-1,
//				null, null, Constants.JAVA_SOURCE_CHARSET);
			CanonizeSource.canonize(home + "*.properties",
				dirTree,
				false,
				-1,
				null, null, Constants.JAVA_SOURCE_CHARSET);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Update release date in the file changelog.md.
	 * @param date actual date.
	 */
	private static void updateDateInChangeLog(final String date) {
		try {
			File f = new File("../xdef/changelog.md");
			Reader fr = new InputStreamReader(new FileInputStream(f),
				Charset.forName("UTF-8"));
			BufferedReader bufrdr = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String line;
			boolean wasVer = false;
			boolean changed = false;
			while ((line = bufrdr.readLine()) != null) {
				int ndx;
				if (!wasVer && line.indexOf('$') < 0
					&& line.startsWith("# Version ")
					&& (ndx = line.indexOf(" release-date")) > 0) {
					String s =
						line.substring(0, ndx) + " release-date " + date;
					changed = !s.equals(line);
					line = s;
					wasVer = true;
				}
				sb.append(line).append('\n');
			}
			bufrdr.close();
			if (changed) {
				Writer wr = new OutputStreamWriter(new FileOutputStream(f),
					Charset.forName("UTF-8"));
				wr.write(sb.toString());
				wr.close();
				System.out.println("Updated date in changelog.md");
			} else {
				System.out.println("Date in changelog.md not changed");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Find first element with given name.
	 * @param s string with XML data
	 * @param name name of element to be found.
	 * @return index to the element.
	 */
	private static int findElement(final String s, final String name) {
		int ndx;
		int ndx0 = 0;
		while ((ndx = s.indexOf('<', ndx0)) >= 0) {
			if (s.startsWith("<!--", ndx)) {
				ndx0 = s.indexOf("-->", ndx + 4);
				if (ndx0 < 0) {
					return -1;
				}
				ndx0 += 3;
			} else if (s.startsWith('<' + name, ndx)) {
				ndx0 += name.length() + 1;
				ndx = s.indexOf(">", ndx0);
				if (ndx < 0) {
					return -1;
				}
				if (s.charAt(ndx - 1) == '/') {
					return -1;
				}
				return ndx + 1;
			} else {
				ndx0 = s.indexOf('>', ndx);
				if (ndx0 < 0) {
					return -1;
				}
				ndx0++;
			}
		}
		return -1;
	}

	/** Update release date in the file pom.xml.
	 * @param date actual date.
	 */
	private static void updateDateInPomXml(final String date) {
		try {
			File f = new File("../pom.xml");
			Reader fr = new InputStreamReader(new FileInputStream(f),
				Charset.forName("UTF-8"));
			StringBuilder sb = new StringBuilder();
			int i;
			while ((i = fr.read()) >= 0) {
				sb.append((char) i);
			}
			fr.close();
			String s = sb.toString();
			int ndx = findElement(s, "release.date");
			int ndx1 = ndx > 0 ? s.indexOf("</release.date") : -1;
			if (ndx1 > 0 &&
				!s.equals(s = s.substring(0, ndx) + date + s.substring(ndx1))) {
				Writer wr = new OutputStreamWriter(new FileOutputStream(f),
					Charset.forName("UTF-8"));
				wr.write(s);
				wr.close();
				System.out.println("Updated <release.date> in pom.xml");
			} else {
				System.out.println("<release.date> in pom.xml not changed");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/** Canonize sources.
	 * @param args array with command line parameters (no parameters used).
	 */
	public static void main(String... args) {
		_hdr = false;
		_tail = false;
		String projectBase;
		try {
			projectBase = new File(".").getCanonicalPath().replace('\\', '/');
		} catch (Exception ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		int i = projectBase.lastIndexOf('/');
		if (i < 0) {
			throw new RuntimeException("Unknown build structure");
		}
		// Canonize sources: replace leading spaces with tabs and remove
		// trailing white spaces.
		doSources("../xdef/src/main/java", true);
		doSources("../xdef/src/main/resources/org", true);
		doSources("../xdef/src/test/java", true);

		doSources("src/main/java/buildtools", true); //this project

		// register report messages
		GenReportTables.main();

		// update date in files changelog.md and in pom.xml
		String date = String.format("%tF", new Date()); // actual date
		updateDateInChangeLog(date);
//		updateDateInPomXml(date);
	}
}