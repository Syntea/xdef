package org.xdef.util;

import java.io.File;
import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.XDFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Check the source with the X-definition for errors.
 * @author Vaclav Trojan
 */
public class CheckXdef {

	/** Prevent user to instantiate this class. */
	private CheckXdef() {}

	@SuppressWarnings("deprecation")
	/** Check X-definitions.
	 * @param sources Array of file names or URLs with X-definitions.
	 * @return true if no errors are detected.
	 */
	public static boolean checkXdef(final String... sources) {
		ReportWriter reporter = new ArrayReporter();
		try {
			Properties props = new Properties();
			XDFactory.getXDBuilder(reporter, props).setSource(sources).compileXD();
			if (reporter.errorWarnings()) {
				reporter.getReportReader().printReports(System.err);
				return false;
			}
			return true;
		} catch (Exception ex) {
			if (reporter.errorWarnings()) {
				reporter.getReportReader().printReports(System.err);
			}
			System.err.println(ex);
			return false;
		}
	}

	/** Check file with X-definition.
	 * @param args The array of arguments:
	 * <p>FileName [FileName ... ]
	 * <p>where:
	 * <ul>
	 * <li>FileName the path and name of the source X-definition.
	 * Note the file names may contain the "wildcards" as '*' or '?' to specify the group of source files.
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"Check if X-definition is correct.\n" +
"Command line arguments:\n"+
"   FileName [FileName1] [ ... ]\n" +
"Note: wildcard chars '*' or '?' in the file specification are accepted.";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Parameters missing\n" + info);
		}
		final StringBuilder err = new StringBuilder();
		List<String> ar = new ArrayList<>();
		for (String arg : args) {
			java.io.File[] files = SUtils.getFileGroup(arg);
			if (files == null || files.length == 0) {
				throw new RuntimeException("\"No available file: " + arg + "\n" + info);
			} else {
				for (File file : files) {
					if (file.exists() && file.canRead()) {
						try {
							String s = file.getCanonicalPath().intern();
							if (!ar.contains(s)) {
								ar.add(s);
							}
						} catch (IOException ex) {}
					}
				}
			}
		}
		if (ar.isEmpty()) {
			err.append("No available input files\n");
		}
		if (err.length() > 0) {
			throw new RuntimeException(err + info);
		}
		try {
			if (checkXdef(ar.toArray(new String[0]))) {
				System.out.println("OK");
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}