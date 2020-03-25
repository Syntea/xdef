package org.xdef.util;

import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.NullReportWriter;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefOutStream;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;

/** Validation of XML document with given X-definition.
 * <p>
 * Also provides main method for calling the program from command line.
 * (see {@link org.xdef.util.XValidate#main(String[])})</p>
 * @author  Vaclav Trojan
 */
public class XValidate {

	/** Creates a new instance of XValidate.
	 * (just to prevents user to instantiate this object).
	 */
	private XValidate() {}

	/** Validate xml file with given definition.
	 * @param props properties to be set to X-definitions.
	 * @param xmlFile The file with XML.
	 * @param repw Report writer.
	 * @return The XDDocument object or <tt>null</tt> if the XDDocument was
	 * not created.
	 */
	public static XDDocument validate(final Properties props,
		final File xmlFile,
		final ReportWriter repw) {
		return validate(props,
			xmlFile,
			null, //xdefFile
			null, //xdefFiles
			null, //rootDefFile
			repw);
	}

	/** Validate xml file with given definition.
	 * @param props properties to be set to X-definitions.
	 * @param xmlFile The file with XML.
	 * @param xdefFiles Array of files with definitions.
	 * @param rootDefName Name of root definition.
	 * @param repw Report writer.
	 * @return The XDDocument object or <tt>null</tt> if the XDDocument was
	 * not created.
	 */
	public static XDDocument validate(final Properties props,
		final File xmlFile,
		final File[] xdefFiles,
		final String rootDefName,
		final ReportWriter repw) {
		//just back compatibility (without poolFile).
		return validate(props,
			null,
			null,
			null,
			null,
			xmlFile,
			null,
			xdefFiles,
			rootDefName,
			repw);
	}

	/** Validate XML file with given definition.
	 * @param props properties to be set to X-definitions.
	 * @param xmlFile The file with XML.
	 * @param poolFile The file with XDPool or null.
	 * @param xdefFiles Array of files with definitions.
	 * @param rootDefName Name of root definition.
	 * @param repw Report writer.
	 * @return The XDDocument object or <tt>null</tt> if the XDDocument was
	 * not created.
	 */
	public static XDDocument validate(final Properties props,
		final File xmlFile,
		final File poolFile,
		final File[] xdefFiles,
		final String rootDefName,
		final ReportWriter repw) {
		//just back compatibility (without poolFile).
		return validate(props,
			null, // ext
			null, // out
			null, // in
			null, // userObj
			xmlFile,
			poolFile,
			xdefFiles,
			rootDefName,
			repw);
	}

	/** Validate XML file with given definition.
	 * @param props properties to be set to X-definitions.
	 * @param ext The array of external classes with external methods
	 * or <tt>null</tt>.
	 * @param out The DefOutStream used as standard output or <tt>null</tt>.
	 * @param in The DefInStream used as standard input or <tt>null</tt>.
	 * @param userObj The user object or <tt>null</tt>.
	 * @param xmlFile The file with XML.
	 * @param xdefFiles Array of files with definitions or null.
	 * @param rootDefName Name of root definition.
	 * @param repw Report writer; if <tt>null</tt> then an RuntimeEexception
	 * is thrown.
	 * @return XDDocument object or <tt>null</tt> if the XDDocument was
	 * not created.
	 */
	public static XDDocument validate(final Properties props,
		final Class<?>[] ext,
		final DefOutStream out,
		final DefInStream in,
		final Object userObj,
		final File xmlFile,
		final File[] xdefFiles,
		final String rootDefName,
		final ReportWriter repw) {
		//just back compatibility (without poolFile).
		return validate(props,
			ext,
			out,
			in,
			userObj,
			xmlFile,
			null, //poolFile
			xdefFiles,
			rootDefName,
			repw);
	}

	/** Validate XML file with given definition.
	 * @param props properties to be set to X-definitions.
	 * @param ext The array of external classes with external methods
	 * or <tt>null</tt>.
	 * @param out The DefOutStream used as standard output or <tt>null</tt>.
	 * @param in The DefInStream used as standard input or <tt>null</tt>.
	 * @param userObj The user object or <tt>null</tt>.
	 * @param xmlFile The file with XML.
	 * @param poolFile The file with serialized XDPool or null.
	 * @param xdefFiles Array of files with definitions or null.
	 * @param rootDefName Name of root definition.
	 * @param repw Report writer; if <tt>null</tt> then an RuntimeEexception
	 * is thrown.
	 * @return XDDocument object or <tt>null</tt> if the XDDocument was
	 * not created.
	 */
	public static XDDocument validate(final Properties props,
		final Class<?>[] ext,
		final DefOutStream out,
		final DefInStream in,
		final Object userObj,
		final File xmlFile,
		final File poolFile,
		final File[] xdefFiles,
		final String rootDefName,
		final ReportWriter repw) {
		XDPool xp = null;
		if (xdefFiles != null && xdefFiles.length > 0) {
			try {
				xp = XDFactory.compileXD(props, xdefFiles, ext);
			} catch (Exception ex) {
				if (ex instanceof SThrowable) {
					repw.putReport(((SThrowable)ex).getReport());
				} else {
					repw.fatal(SYS.SYS036, ex); //Program exception &{0}
				}
				return null;
			}
		} else if (poolFile != null) {
			try {
				xp = XDFactory.readXDPool(poolFile);
			} catch (Exception ex) {
				if (ex instanceof SThrowable) {
					repw.putReport(((SThrowable)ex).getReport());
				} else {
					repw.fatal(SYS.SYS036, ex); //Program exception &{0}
				}
				return null;
			}
		}
		if (xp != null) {
			try {
				if (rootDefName != null && rootDefName.length() > 0
					&& xp.getXMDefinition(rootDefName) == null) {
					//X-definition '&{0}' doesn't exist
					repw.fatal(XDEF.XDEF269, rootDefName);
				} else {
					XDDocument xd = xp.createXDDocument(rootDefName);
					xd.setProperties(props);
					parse(xd, repw, out, in, userObj, xmlFile);
					return xd;
				}
			} catch (Exception ex) {
				if (ex instanceof SThrowable) {
					repw.putReport(((SThrowable)ex).getReport());
				} else {
					repw.fatal(SYS.SYS036, ex); //Program exception &{0}
				}
				return null;
			}
		}
		try {
			return XDFactory.xparse(xmlFile.getCanonicalPath(), repw);
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				repw.putReport(((SThrowable)ex).getReport());
			} else {
				repw.fatal(SYS.SYS036, ex); //Program exception &{0}
			}
			return null;
		}
	}

	private static void parse(final XDDocument chkdoc,
		final ReportWriter repw,
		final DefOutStream out,
		final DefInStream in,
		final Object userObj,
		final File xmlFile) {
		if (out != null) {
			chkdoc.setStdOut(out);
		}
		if (in != null) {
			chkdoc.setStdIn(in);
		}
		if (userObj != null) {
			chkdoc.setUserObject(userObj);
		}
		chkdoc.xparse(xmlFile, repw);
	}

	/** Validation of pool ox X-definitions. This method is possible to invoke
	 * from command line.
	 * @param args Array of strings containing command line arguments.
		 * <p>Arguments:</p>
		 * <ul>
		 *  <li><tt>-i file</tt> - input XML file to validate</li>
		 *  <li><tt>-d file</tt> - input X-definition file(s)</li>
		 *  <li><tt>-p file</tt> - input XDPool file</li>
		 *  <li><tt>-l file</tt> - log file</li>
		 *  <li><tt>-x name</tt> - name of X-definition</li>
		 *  <li><tt>-h | -?</tt> - print help</li>
		 * </ul>
	 */
	public static void main(final String... args) {
		final String info =
"XValidate: validation of XML document with X-definition.\n"+
"usage: [-d defList | -p XDPool ] [-x xDefName] [-l logFile] -i xmlFile\n"+
"    Items in the defList parameter are separated by path separators and may\n"+
"    contain wildcard charaters in file names.\n"+
"    Both -d and -p parameters can't be specified.";
		if (args.length == 0) {
			throw new RuntimeException("Parameters missing\n" + info);
		}
		File[] xdefFiles = null;
		String xdefName = null;
		ReportWriter repw = null;
		File xmlFile = null;
		File poolFile = null;
		int i = 0;
		while (i < args.length && args[i].startsWith("-")) {
			int swNum = i + 1;
			if (args[i].length() == 1) {
				throw new RuntimeException(
					"Incorrect parameter ["+swNum+"]: \""+args[i]+"\"\n"+info);
			}
			char c = args[i].charAt(1);
			switch (c) {
				case 'h':
				case '?':
					System.out.println(info);
					return;
			}
			String s;
			if (args[i].length() == 2) {
				i++;
				if (i >= args.length) {
					throw new RuntimeException("Parameter [" + swNum +
						"], \"" + args[swNum-1]
						+ "\": missing following argument\n" + info);
				}
				s = args[i];
			} else {
				s = args[i].substring(2);
			}
			i++;
			switch (c) {
				case 'i': {
					if (xmlFile != null) {
						throw new RuntimeException("Parameter ["+swNum+
							"], \""+args[swNum-1]+"\": redefinition\n"+info);
					}
					xmlFile = new File(s);
					if (!xmlFile.exists()) {
						throw new RuntimeException("File "
							+ xmlFile.getAbsolutePath()+" doesn't exist.");
					}
					if (!xmlFile.canRead()) {
						throw new RuntimeException(
							"Can't read file " + xmlFile.getAbsolutePath());
					}
					continue;
				}
				case 'd':
					if (xdefFiles == null) {
						HashSet<File> fileTab = new HashSet<File>();
						StringTokenizer st = new StringTokenizer(s, ";");
						while (st.hasMoreTokens()) {
							try {
								File[] files =
									SUtils.getFileGroup(st.nextToken());
								for (int j = 0; j < files.length; j++) {
									fileTab.add(files[j].getCanonicalFile());
								}
							} catch (Exception ex) {
								throw new RuntimeException(
									"Can't open file: " + args[i]);
							}
						}
						xdefFiles = new File[fileTab.size()];
						fileTab.toArray(xdefFiles);
					} else {
						throw new RuntimeException("Parameter ["+swNum+
							"], \""+args[swNum-1]+"\": redefinition\n"+info);
					}
					continue;
				case 'p': {
					if (poolFile != null || xdefFiles != null) {
						throw new RuntimeException("Parameter ["+swNum+
							"], \""+args[swNum-1]+"\": redefinition\n"+info);
					}
					poolFile = new File(s);
					if (!poolFile.exists()) {
						throw new RuntimeException(
							"File "+poolFile.getAbsolutePath()
							+" doesn't exist\n" + info);
					}
					if (!poolFile.canRead()) {
						throw new RuntimeException(
							"Can't read file " + poolFile.getAbsolutePath()
							+"\n" + info);
					}
					continue;
				}
				case 'l':
					if (repw == null) {
						if ("null".equals(s)) {
							repw = new NullReportWriter(false);
						} else {
							try {
								repw = new FileReportWriter(s);
							} catch (Exception ex) {
								throw new RuntimeException(
									"Can't create report writer from ["+swNum+
									"], \"" + args[swNum-1] + "\"\n" + info);
							}
						}
					} else {
						throw new RuntimeException("Parameter ["+swNum+
							"], \""+args[swNum-1]+"\": redefinition\n"+info);
					}
					continue;
				case 'x':
					if (xdefName == null) {
						xdefName = s;
					} else {
						throw new RuntimeException("Parameter ["+swNum+
							"], \""+args[swNum-1]+"\": redefinition\n"+info);
					}
					continue;
				default:
					throw new RuntimeException("Parameter [" + swNum +
						"], \"" + args[swNum-1] + "\": unknown switch\n"+info);
			}
		}
		if (xmlFile == null) {
			throw new RuntimeException("No XML file to validate\n"+info);
		}
		//reading of xml files
		if (i < args.length) {
			throw new RuntimeException("Error in parameters\n"+info);
		}
		if (repw == null) {
			repw = new ArrayReporter();
		}

		try {
			XDDocument chkDoc = validate(System.getProperties(),
				xmlFile, poolFile, xdefFiles, xdefName, repw);
			int errors = repw.getErrorCount() + repw.getFatalErrorCount() +
				repw.getWarningCount();
			String fname = xmlFile.getCanonicalPath();
			if (repw instanceof ArrayReporter) {
				if (errors != 0) {
					System.out.println("File OK: " + fname);
				}
			} else {
				if (chkDoc != null && !chkDoc.errors()) {
					repw.info("", "File OK: " + fname);
				}
				repw.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected exception", ex);
		}
	}
}