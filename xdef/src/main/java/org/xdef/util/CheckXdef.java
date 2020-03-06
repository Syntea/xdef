package org.xdef.util;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.XDFactory;
import java.util.ArrayList;
import java.util.Properties;

/** Check the source with the X-definition for errors.
 * @author Vaclav Trojan
 */
public class CheckXdef {

	/** Prevent user to instantiate this class. */
	private CheckXdef() {}

	/** Check X-definitions.
	 * @param ext Array of classes with external procedures.
	 * @param sources Array of file names or URLs with X-definitions.
	 * @return true if no errors are detected.
	 */
	public static boolean checkXdef(final Class<?>[] ext,
		final String... sources) {
		ReportWriter reporter = new ArrayReporter();
		try {
			Properties props = new Properties();
			XDFactory.getXDBuilder(reporter, props).setExternals(ext)
				.setSource(sources).compileXD();
			if (reporter.errorWarnings()) {
				reporter.getReportReader().printReports(System.err);
				return false;
			}
			return true;
		} catch (Exception ex) {
			if (reporter != null && reporter.errorWarnings()) {
				reporter.getReportReader().printReports(System.err);
			}
			System.err.println(ex);
			return false;
		}
	}

	/** Add an error message to the list of messages.
	 * @param err StringBuffer with error messages.
	 * @param args list of arguments.
	 * @param index index of argument item.
	 * @param msg message text.
	 */
	private static void errMsg(StringBuilder err,
		String[] args,
		int index,
		String msg) {
		err.append("Parameter [").append(String.valueOf(index)).append("]: \"");
		err.append(args[index - 1]).append("\": ").append(msg).append('\n');
	}

	/** Check file with X-definition.
	 * @param args The array of arguments:
	 * <p>FileName [FileName ... ]</p>
	 * <p>where:</p>
	 * <ul>
	 * <li>FileName the path and name of the source X-definition.
	 * <i>Note the file names may contain the "wildcards" as '*' or '?' to
	 * specify the group of source files.</i></li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"CheckXdef - Check if X-definition is correct.\n" +
"Usage: FileName [FileName1] [ ... ]\n" +
"Note: wildcard chars '*' or '?' in the file specification are accepted.\n" +
"(c)2007 Syntea Software Group";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Parameters missing\n" + info);
		}
		final StringBuilder err = new StringBuilder();
		ArrayList<String> ar = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			java.io.File[] files = SUtils.getFileGroup(args[i]);
			if (files == null || files.length == 0) {
				throw new RuntimeException(
					"\"No available file: "+args[i] + "\n" + info);
			} else {
				for (int j = 0; j < files.length; j++) {
					if (files[j].exists() && files[j].canRead()) {
						try {
							String s = files[j].getCanonicalPath().intern();
							if (!ar.contains(s)) {
								ar.add(s);
							}
						} catch (Exception ex) {}
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
			if (checkXdef(null, ar.toArray(new String[ar.size()]))) {
				System.out.println("OK");
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}
}