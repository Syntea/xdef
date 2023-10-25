package org.xdef.util;

import org.xdef.impl.GenXDef;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Element;

/** Generate X-definition from XML, JSON/XON, or YAML data.
 * @author Vaclav Trojan
 */
public class GenXDefinition {

	/** Prevent create an instance of this class.*/
	private GenXDefinition() {}

	/** Generate X-definition from XML data to X-definition.
	 * @param obj XML, JSON/XON, YAML input data (or path to source data)
	 * @return org.w3c.dom.Element object with X-definition.
	 */
	public static final Element genXdef(final Object obj) {
		return genXdef(obj, null);
	}

	/** Generate X-definition from XML data to X-definition.
	 * @param obj XML, JSON/XON, YAML input data (or path to source data)
	 * @param xdName name of XDefinition or null.
	 * @return org.w3c.dom.Element object with X-definition.
	 */
	public static final Element genXdef(final Object obj, final String xdName) {
		return GenXDef.genXdef(obj, xdName);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj XML, JSON/XON, YAML input data (or path to source datae)
	 * @param outFile output file or pathname.
	 * @param encoding name of character encoding.
	 * @throws IOException if an error occurs.
	 */
	public static final void genXdef(final Object obj,
		final Object outFile,
		final String encoding) throws IOException {
		genXdef(obj, outFile, encoding, null);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj XML, JSON/XON, YAML input data (or path to source datae)
	 * @param outFile output file or pathname.
	 * @param encoding name of character encoding.
	 * @param xdName name of XDefinition or null.
	 * @throws IOException if an error occurs.
	 */
	public static final void genXdef(final Object obj,
		final Object outFile,
		final String encoding,
		final String xdName) throws IOException {
		if (outFile instanceof String) {
			KXmlUtils.writeXml((String) outFile,
				encoding,
				GenXDef.genXdef(obj, xdName),
				true,
				true);
		} else if (outFile instanceof File) {
			KXmlUtils.writeXml((File) outFile,
				encoding,
				GenXDef.genXdef(obj, xdName),
				true,
				true);
		} else {
			throw new IOException("Incorrect type of output file: " +
				(null==outFile? "null" : outFile.getClass().getName()));
		}
	}

	/** Generate X-definition from XML (command line parameters).
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><i><b>-i file</b> ....... </i>XML, JSON/XON, or YAML file  pathname.
	 * <li><i><b>-o file</b> ....... </i>X-definition output file pathname.
	 * <li><i><b>-e encoding</b> ... </i>character set name of output.
	 * </ul>
	 */
	public static final void main(String... args) {
		final String info =
"Generate X-definition from XML.\n" +
"\n"+
"Command line arguments:\n"+
"   -i input_file -o output file [-e encoding]\n"+
"Where:\n"+
"  -i input    file intput file (XML, JSON/XON, YAML)\n" +
"  -o output   file\n" +
"  -e encoding name of character set encoding\n" +
"  -x name of X-definition";
		if (args.length < 2) {
			if (args.length == 1
				&& ("-h".equals(args[0]) || "/h".equals(args[0]))) {
				System.out.println(info);
				return;
			}
			throw new RuntimeException(info + "Incorrect parameters");
		}
		File inFile = null,	outFile = null;
		String encoding = null;
		String xdName = null;
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg == null || arg.isEmpty()) {
				throw new RuntimeException(info
					+ "Empty parameter " + (i + 1));
			}
			if ("-i".equals(arg)) {
				if (inFile != null) {
					throw new RuntimeException(info
						+ "Redefinition of input file '-i'");
				}
				if (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					inFile = new File(arg);
					if (!inFile.exists() || !inFile.canRead()) {
						throw new RuntimeException(info
							+ "Can't read intput file : " + arg);
					}
					i++;
					continue;
				} else {
					throw new RuntimeException(info
						+ "After parameter '-i' is expected an input file");
				}
			}
			if ("-o".equals(arg)) {
				if (outFile != null) {
					throw new RuntimeException(info
						+ "Redefinition of output file '-o'");
				}
				if (++i < args.length && (arg = args[i]) != null
					&& !arg.startsWith("-")) {
					outFile = new File(arg);
					i++;
					continue;
				} else {
					throw new RuntimeException(info
						+ "After parameter '-o' is expected an output file");
				}
			}
			if ("-e".equals(arg)) {
				if (encoding != null) {
					throw new RuntimeException(info
						+ "Redefinition encoding '-e'");
				}
				if (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					encoding = arg;
					i++;
					continue;
				} else {
					throw new RuntimeException(info
						+ "After parameter '-e' is expected an encoding name");
				}
			}
			if ("-x".equals(arg)) {
				if (xdName != null) {
					throw new RuntimeException(info
						+ "Redefinition X-definition name '-x'");
				}
				if (++i < args.length && (arg = args[i]) != null &&
					!arg.startsWith("-")) {
					xdName = arg;
					i++;
					continue;
				} else {
					throw new RuntimeException(info
						+ "After parameter '-e' is expected an encoding name");
				}
			}
			throw new RuntimeException(info + "Incorrect parameter: " + arg);
		}
		if (inFile == null) {
			throw new RuntimeException(info + "No source input specified");
		}
		if (outFile == null) {
			throw new RuntimeException(info + "No output file specified");
		}
		if (encoding == null) {
			encoding = "UTF-8";
		}
		try {
			 genXdef(inFile, outFile.getCanonicalPath(), encoding, xdName);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}