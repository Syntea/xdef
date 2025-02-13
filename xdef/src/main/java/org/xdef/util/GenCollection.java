package org.xdef.util;

import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.gencollection.XDGenCollection;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/** Generates a xml document with collection of all given Xdefinitions.
 * @author  Vaclav Trojan
 */
public class GenCollection {

	/** Prevent user to instantiate this class. */
	private GenCollection(){};

	/** Create collection element from sources.
	 * @param sources array of source paths, wildcards are permitted.
	 * @param resolvemacros if true then macros are resolved.
	 * @param removeActions if true all irrelevant actions are removed.
	 * @param genModelVariants if true generate alternate models if in the reference there exists an attribute
	 * redefining type or occurrence (important for XML schema (XSD) generation).
	 * @return element with collection of Xdefinitions.
	 * @throws java.lang.Exception if an error occurs.
	 */
	public static Element genCollection(final String[] sources,
		final boolean resolvemacros,
		final boolean removeActions,
		final boolean genModelVariants) throws Exception {
		return XDGenCollection.genCollection(sources, resolvemacros, removeActions, genModelVariants);
	}

	/** Create collection element from sources.
	 * @param files array of source files.
	 * @param resolvemacros if true then macros are resolved.
	 * @param removeActions if true all irrelevant actions are removed.
	 * @param genModelVariants if true generate alternate models if in the reference there exists an attribute
	 * redefining type or occurrence (important for XML schema (XSD) generation).
	 * @return element with collection of Xdefinitions.
	 * @throws java.lang.Exception if an error occurs.
	 */
	public static Element genCollection(final File[] files,
		final boolean resolvemacros,
		final boolean removeActions,
		final boolean genModelVariants) throws Exception {
		return XDGenCollection.genCollection(files, resolvemacros, removeActions, genModelVariants);
	}

	/** Create collection element from sources.
	 * @param urls array of source urls.
	 * @param resolvemacros if true then macros are resolved.
	 * @param removeActions if true all irrelevant actions are removed.
	 * @param genModelVariants if true generate alternate models if in the reference there exists an attribute
	 * redefining type or occurrence (important for XML schema (XSD) generation).
	 * @return element with collection of Xdefinitions.
	 * @throws java.lang.Exception if an error occurs.
	 */
	public static Element genCollection(URL[] urls,
		boolean resolvemacros,
		final boolean removeActions,
		final boolean genModelVariants) throws Exception {
		return XDGenCollection.genCollection(urls, resolvemacros, removeActions, genModelVariants);
	}

	/** String with command line information. */
	private static final String INFO =
"Generate collection from Xdefinitions and/or collections.\n"+
"Command line arguments:\n"+
" [-m] [-s] [-g] [-e encoding] -o output file -i input_file [input_file1...]\n"+
" -e encoding ...... name of charset\n" +
" -m macros will be expanded\n" +
" -s all actions are removed from the script\n" +
" -g generate alternate models if in the reference there exists an attribute\n"+
"    redefininig type or occurrence (important for XML schema generation)\n" +
" -o output file\n" +
" -i file [file1 ... [filen] ] list of intput files (wildcards supported)";

	/** Call generation of a collection of Xdefinitions from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><i>-o file .......... </i>output file
	 * <li><i>[-m .............. </i>switch macros will be expanded]
	 * <li><i>-e encoding ...... </i>name of character set
	 * <li><i>-i file [file1 ... [filen] ]</i> list of input files (wildcards are supported)
	 * </ul>
	 */
	public static void main(String... args) {
		if (args == null || args.length == 0) {
			throw new RuntimeException("Missing parameters\n" + INFO);
		}
		if (args.length < 4) {
			if (args.length == 1 &&
				("-h".equals(args[0]) || "-?".equals(args[0]))) {
				System.out.println(INFO);
				return;
			} else {
				throw new RuntimeException("Incorrect parameters\n" + INFO);
			}
		}
		List<String> sources = new ArrayList<>();
		File outFile = null;
		boolean resolveMacros = false;
		boolean removeActions = false;
		boolean genModelVariants = false;
		String encoding = null;
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg == null || arg.isEmpty()) {
				throw new RuntimeException("Incorrect parameter: " + arg + "\n" + INFO);
			}
			if ("-m".equals(arg)) {
				if (resolveMacros) {
					throw new RuntimeException("Redefinition of switch \"-m\"\n" + INFO);
				}
				resolveMacros = true;
				i++;
				continue;
			}
			if ("-g".equals(arg)) {
				if (genModelVariants) {
					throw new RuntimeException("Redefinition of switch \"-g\"\n" + INFO);
				}
				genModelVariants = true;
				i++;
				continue;
			}
			if ("-s".equals(arg)) {
				if (removeActions) {
					throw new RuntimeException("Redefinition of switch \"-s\"\n" + INFO);
				}
				removeActions = true;
				i++;
				continue;
			}
			if ("-i".equals(arg)) {
				while (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					sources.add(arg);
				}
				continue;
			}
			if ("-o".equals(arg)) {
				if (outFile != null) {
					throw new RuntimeException("Redefinition of switch \"-o\"\n" + INFO);
				}
				if (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					outFile = new File(arg);
					i++;
					continue;
				} else {
					throw new RuntimeException("After parameter '-o' is expected an output file\n" + INFO);
				}
			}
			if ("-e".equals(arg)) {
				if (encoding != null) {
					throw new RuntimeException("Redefinition of switch \"-e\"\n" + INFO);
				}
				if (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					encoding = arg;
					i++;
					continue;
				} else {
					throw new RuntimeException("After parameter '-e' is expected an encoding name\n" + INFO);
				}
			}
			throw new RuntimeException("Incorrect parameter on position " + (i+1) + "\n" + INFO);
		}
		if (sources.isEmpty()) {
			throw new RuntimeException("No source specified\n" + INFO);
		}
		if (outFile == null) {
			throw new RuntimeException("No output file specified\n" + INFO);
		}
		if (encoding == null) {
			encoding = "UTF-8";
		}
		try {
			String[] srcs = new String[sources.size()];
			sources.toArray(srcs);
			Element collection = genCollection(srcs,
				resolveMacros, removeActions, genModelVariants);
			if (collection == null) {
				throw new RuntimeException("No collection generated\n" + INFO);
			}
			KXmlUtils.writeXml(outFile, encoding, collection, true, true);
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected exception", ex);
		}
	}
}
