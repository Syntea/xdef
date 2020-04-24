package org.xdef.util;

import org.xdef.impl.GenXDef;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Generate X-definition from XML.
 * @author Vaclav Trojan
 */
public class GenXDefinition {

	/** Prevent create an instance of this class.*/
	private GenXDefinition() {}

	/** Creates a new instance of GenX-definition.
	 * @param data Object from which we'll create a X-definition.
	 * @return element with X-definition.
	 */
	public static final Element genXDefinition(final Element data) {
		return GenXDef.genXDefinition(data);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param source XML element.
	 * @return String with X-definition.
	 * @throws Exception if an error occurs.
	 */
	public static final Element genXdef(final String source) throws Exception {
		return genXdef(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param elem XML element.
	 * @return org.w3c.dom.Document object with X-definition.
	 */
	public static final Element genXdef(final Element elem) {
		return GenXDef.genXdef(elem);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param source name of input file or string with source XML.
	 * @param outFile name of output file.
	 * @param encoding name of character encoding.
	 * @throws IOException if an error occurs.
	 */
	public static final void genXdef(final String source,
		final String outFile,
		final String encoding) throws IOException {
		final KDOMBuilder kb = new KDOMBuilder();
		final Document doc = kb.parse(source);
		KXmlUtils.writeXml(outFile,
			encoding, genXdef(doc.getDocumentElement()), true, true);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param source input file with source XML.
	 * @param outFile output file.
	 * @param encoding name of character encoding.
	 * @throws IOException if an error occurs.
	 */
	public static final void genXdef(final File source,
		final File outFile, final String encoding) throws IOException {
		final KDOMBuilder kb = new KDOMBuilder();
		final Document doc = kb.parse(source);
		KXmlUtils.writeXml(outFile,
			encoding, genXdef(doc.getDocumentElement()), true, true);
	}

	/** Generate X-definition from XML (command line parameters).
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><tt><b>-i file</b> ....... </tt>XML input file pathname</li>
	 * <li><tt><b>-o file</b> ....... </tt>X-definition output file pathname</li>
	 * <li><tt><b>-e encoding</b> ... </tt>character set name of output</li>
	 * </ul>
	 */
	public static final void main(String... args) {
		final String info =
"GenXDefinition - generate X-definitions from given XML.\n" +
"Usage: -i input_file -o output file [-e encoding]\n"+
"  -i input    file intput file\n" +
"  -o output   file\n" +
"  -e encoding name of character set encoding\n"+
"\n(c)2008 Syntea Software group\n";
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
			 genXdef(inFile, outFile, encoding);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}