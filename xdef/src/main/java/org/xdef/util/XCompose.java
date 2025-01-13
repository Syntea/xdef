package org.xdef.util;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.NullReportWriter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.SRuntimeException;

/** Process the XML file with the X-definition in the construction mode. Also provides main method for calling
 * the program from command line. (see {@link org.xdef.util.XCompose#main(String[])})
 * @author  Vaclav Trojan
 */
public class XCompose {

	/** Compose xml file from source with given definition.
	 * @param sourceFile The file with source XML.
	 * @param outFile The file where the result will be created.
	 * @param encoding The charset name of output encoding.
	 * @param repw Report writer.
	 * @return true if result was composed.
	 */
	public static boolean compose(File sourceFile, File outFile, String encoding, ReportWriter repw) {
		return compose(sourceFile, null, null, null, null, outFile, encoding, repw);
	}

	/** Compose xml file with given definition.
	 * @param sourceFile The file with XML.
	 * @param xdefFiles Array of files with definitions.
	 * @param xDefName Name of definition.
	 * @param rootNS namespace of result root element or <i>null</i>.
	 * @param rootName qualified name of result root element.
	 * @param outFile The file where the result will be created.
	 * @param encoding The charset name of output encoding.
	 * @param repw Report writer.
	 * @return true if result was composed.
	 */
	public static boolean compose(
		File sourceFile,
		File[] xdefFiles,
		String xDefName,
		String rootNS,
		String rootName,
		File outFile,
		String encoding,
		ReportWriter repw) {
		Element sourceElem = null;
		try {
			Document sourceDoc = KXmlUtils.parseXml(sourceFile);
			sourceElem = repw.errors() ? null : sourceDoc != null ? sourceDoc.getDocumentElement() : null;
			if (encoding == null || encoding.isEmpty()) {
				encoding = sourceDoc.getXmlEncoding();
			}
			if (encoding == null || encoding.isEmpty()) {
				 encoding = "UTF-8";
			}
		} catch (Exception ex) {}
		if (sourceElem == null) {
			repw.error(null, "Incorrect source XML document");
			return false;
		}
		if (xDefName == null) {
			xDefName = sourceElem.getTagName();
		}
		if (rootName == null) {
			rootName = xDefName;
		}
		try {
			XDPool defPool = XDFactory.compileXD(new Properties(), (Object[]) xdefFiles);
			XDDocument chkDoc;
			if (xDefName != null && xDefName.length() > 0) {
				if (!defPool.exists(xDefName)) {
					System.err.println("Can't find definition '"+xDefName+"'");
					return false;
				}
				chkDoc = defPool.createXDDocument(xDefName);
			} else {
				chkDoc = defPool.createXDDocument();
			}
			chkDoc.setXDContext(sourceElem);
			chkDoc.xcreate(new QName(rootNS, rootName), repw);
			Element result = chkDoc.getElement();
			if (result == null) {
				System.err.println("Can't create result");
				return false;
			} else {
				KXmlUtils.writeXml(outFile.getAbsolutePath(), encoding, result, true, true);
			}
		} catch (IOException | SRuntimeException ex) {
			repw.error(null, "Unexpected error: " + ex);
		}
		return !repw.errors();
	}

	/** String with invocation information. */
	private final static String INFO =
"Process the XML file with the X-definition in construction mode.\n"+
"\n"+
"Command line arguments:\n"+
" [-d defList] [-x xDefName] [-l logFile] [-e encoding] -o outFile\n" +
" [-n rootNameSpace] [-r rootName] -i xmlFile\n"+
"(Items in the defList are separated by path separators.)";

	/** Validation of pool ox X-definitions. This method is possible to invoke from command line.
	 * @param args Array of strings containing command line arguments.
	 * <p>
	 * <i>[-d defList] [-x xDefName] [-l logFile] [-e encoding] -o outFile
	 * [-n rootNameSpace] [-r rootName] -i xmlFile</i>
	 * <ul>
	 *  <li><i>-d defList</i> - list of X-definition files
	 *  <li><i>-x xDefName</i> - X-definition name
	 *  <li><i>-l logFile</i> - log file
	 *  <li><i>-e encoding</i> - output file encoding
	 *  <li><i>-o outFile</i> - output file
	 *  <li><i>-n rootNameSpace</i> - root namespace
	 *  <li><i>-r rootName</i> - root name
	 *  <li><i>-i xmlFile</i> - input XML file
	 * </ul>
	 */
	public static void main(String... args) {
		if (args.length == 0) {
			throw new RuntimeException("Parameters missing\n" + INFO);
		}
		File[] xdefFiles = null;
		String xdefName = null;
		String rootNS = null;
		String rootName = null;
		ReportWriter repw = null;
		File sourceFile = null;
		String encoding = null;
		File outFile = null;
		int i = 0;
		while (i < args.length && args[i].startsWith("-")) {
			int swNum = i + 1;
			if (args[i].length() == 1) {
				throw new RuntimeException("Incorrect parameter [" + swNum + "]: '-'" + args[i] +"\n" + INFO);
			}
			char c = args[i].charAt(1);
			switch (c) {
				case 'h':
				case '?':
					System.out.println(INFO);
					return;
			}
			String s;
			if (args[i].length() == 2) {
				i++;
				if (i >= args.length) {
					throw new RuntimeException("Parameter [" + swNum + "], \"" + args[swNum-1]
						+ "\": missing following argument\n" + INFO);
				}
				s = args[i];
			} else {
				s = args[i].substring(2);
			}
			i++;
			switch (c) {
				case 'i':
					if (sourceFile != null) {
						throw new RuntimeException("Redefinition of input file\n" + INFO);
					}
					sourceFile = new File(s);
					if (!sourceFile.exists()) {
						throw new RuntimeException("File " + sourceFile.getAbsolutePath()
							+ " doesn't exist\n" + INFO);
					}
					if (!sourceFile.canRead()) {
						throw new RuntimeException("Can't read file "
							+ sourceFile.getAbsolutePath() + "\n" + INFO);
					}
					continue;
				case 'd':
					if (xdefFiles == null) {
						xdefFiles = SUtils.getFileGroup(s);
					} else {
						throw new RuntimeException("Parameter [" + swNum +	"], \"" + args[swNum-1]
							+ "\": redefinition\n" + INFO);
					}
					continue;
				case 'e':
					if (encoding == null) {
						encoding = s;
					} else {
						throw new RuntimeException("Parameter [" + swNum + "], \"" + args[swNum-1]
							+ "\": redefinition\n" + INFO);
					}
					continue;
				case 'o':
					if (outFile == null) {
						outFile = new File(s);
						try {
							if (outFile.exists()) {
								outFile.delete();
							}
							if (!outFile.createNewFile()) {
								throw new RuntimeException("Can't write to file " + s + "\n" + INFO);
							}
						} catch (IOException | RuntimeException ex) {
							throw new RuntimeException("Can't write to file " + s + "\n" + INFO);
						}
					} else {
						throw new RuntimeException("Parameter [" + swNum + "], \"" + args[swNum-1]
							+ "\": redefinition\n" + INFO);
					}
					continue;
				case 'l':
					if (repw == null) {
						if ("null".equals(s)) {
							repw = new NullReportWriter(false);
						} else {
							try {
								repw = new FileReportWriter(s);
							} catch (Exception ex) {
								throw new RuntimeException("Can't create refort writer from [" + swNum
									+ "]: \"" + args[swNum-1] + "\"\n" + INFO);
							}
						}
					} else {
						throw new RuntimeException("Parameter [" + swNum
							+ "]: \"" + args[swNum-1] + "\": redefinition\n" + INFO);
					}
					continue;
				case 'n':
					if (rootNS == null) {
						rootNS = s;
					} else {
						throw new RuntimeException("Parameter [" + swNum
							+ "]: \"" + args[swNum-1] + "\": redefinition\n" + INFO);
					}
					continue;
				case 'r':
					if (rootName == null) {
						rootName = s;
					} else {
						throw new RuntimeException("Parameter [" + swNum
							+ "]: \"" + args[swNum-1] + "\": redefinition\n" + INFO);
					}
					continue;
				case 'x':
					if (xdefName == null) {
						xdefName = s;
					} else {
						throw new RuntimeException("Parameter [" + swNum
							+ "]: \"" + args[swNum-1] + "\": redefinition\n" + INFO);
					}
					continue;
				default:
					throw new RuntimeException("Parameter [" + swNum
						+ "]: \"" + args[swNum-1] + "\": unknown switch\n"+INFO);
			}
		}
		if (sourceFile == null) {
			throw new RuntimeException("No source XML file\n" + INFO);
		}
		if (i < args.length) {
			throw new RuntimeException("Too many parameters\n" + INFO);
		}
		if (repw == null) {
			repw = new ArrayReporter();
		}
		repw.clearCounters();
		if (compose(
			sourceFile,
			xdefFiles,
			xdefName,
			rootNS,
			rootName,
			outFile,
			encoding,
			repw)) {
			System.out.println("File OK: " + outFile.getAbsolutePath());
			if (repw.getWarningCount() > 0) {
				System.err.println("Warnings reported");
			}
		}
		if (repw.errorWarnings()) {
			throw new RuntimeException(repw.getReportReader().printToString());
		}
	}
}
