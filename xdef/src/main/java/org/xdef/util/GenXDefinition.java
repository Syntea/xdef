package org.xdef.util;

import org.xdef.impl.GenXDef;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;

/** Generate X-definition from XML.
 * @author Vaclav Trojan
 */
public class GenXDefinition {

	/** Prevent create an instance of this class.*/
	private GenXDefinition() {}

	/** Generate X-definition from XML data to X-definition.
	 * @param obj object with XML data (File, InputStream, URL, pathname,
	 * or string with XML document).
	 * @return org.w3c.dom.Element object with X-definition.
	 */
	public static final Element genXdef(final Object obj) {
		Element el = null;
		if (obj instanceof Element) {
			el = (Element) obj;
		} else if (obj instanceof Document) {
			el = ((Document) obj).getDocumentElement();
		} else if (obj instanceof String) {
			el = KXmlUtils.parseXml(((String) obj).trim()).getDocumentElement();
		} else if (obj instanceof URL) {
			el = KXmlUtils.parseXml((URL) obj).getDocumentElement();
		} else if (obj instanceof File) {
			el = KXmlUtils.parseXml((File) obj).getDocumentElement();
		} else if (obj instanceof InputStream) {
			el = KXmlUtils.parseXml((InputStream) obj).getDocumentElement();
		}
		if (el != null) {
			return GenXDef.genXdef(el);
		}
		//XDEF883=Incorrect type of input data
		throw new SRuntimeException(XDEF.XDEF883);
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
		KXmlUtils.writeXml(outFile,
			encoding,
			GenXDef.genXdef(KXmlUtils.parseXml(source).getDocumentElement()),
			true,
			true);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param source input file with source XML.
	 * @param outFile output file.
	 * @param encoding name of character encoding.
	 * @throws IOException if an error occurs.
	 */
	public static final void genXdef(final File source,
		final File outFile, final String encoding) throws IOException {
		KXmlUtils.writeXml(outFile,
			encoding,
			GenXDef.genXdef(KXmlUtils.parseXml(source).getDocumentElement()),
			true,
			true);
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
"Generate X-definition from XML.\n" +
"\n"+
"Command line arguments:\n"+
"   -i input_file -o output file [-e encoding]\n"+
"Where:\n"+
"  -i input    file intput file\n" +
"  -o output   file\n" +
"  -e encoding name of character set encoding";
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