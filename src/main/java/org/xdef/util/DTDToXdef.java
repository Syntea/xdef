package org.xdef.util;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.SReporter;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import org.w3c.dom.Document;

/** Convertor of DTD to X-definition.
 * @author Alexandrov Ilia
 */
public class DTDToXdef {

	/** Reporter for reporting warnings and errors. */
	private SReporter _reporter = new SReporter(new ArrayReporter());

	/** Creates the instance of DTD to X-definition convertor. */
	private DTDToXdef() {}

	/** Sets log file for logging.
	 * @param logFile log file.
	 * @throws IllegalArgumentException if log file is empty or log file name
	 * is illegal.
	 */
	private void setLogFile(String logFile) {
		if (logFile == null || "".equals(logFile)) {
			throw new IllegalArgumentException("Log file name is empty");
		}
		try {
			URL url = new URL("file", "", logFile);
			File log = new File(url.getPath());
			if (!log.exists()) {
				log.createNewFile();
			}
			_reporter = new SReporter(new FileReportWriter(
				new PrintWriter(new FileWriter(log, true)), false));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Log file name is illegal", ex);
		}
	}

	/** Writes given document to file with given name.
	 * @param doc document to write.
	 * @param file file name.
	 * @throws IOException if error occurred during writing file.
	 */
	private void writeFile(Document doc, String file) throws IOException {
		KXmlUtils.writeXml(file, doc);
	}

	/** Generates X-definition file with given name from given DTD file with
	 * given root element.
	 * @param dtd DTD string or URL as string.
	 * @param root root element.
	 * @param outXdef output X-definition file.
	 * @throws IllegalArgumentException if root element is empty of output file
	 * is illegal.
	 * @throws RuntimeException if error occurred during writing files.
	 */
	public void genXdef(String dtd, String root, String outXdef) {
		if (root == null || "".equals(root)) {
			throw new IllegalArgumentException("Root element is empty");
		}
		checkOutputFilePath(outXdef);
		GenDTD2XDEF xdg = new GenDTD2XDEF(dtd);
		Document doc = xdg.genRootXd(root);
		try {
			writeFile(doc, outXdef);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Error during writing X-definition file");
		}
	}

	/** Checks file path.
	 * @param path file path.
	 * @throws IllegalArgumentException file path is illegal.
	 */
	private void checkOutputFilePath(String path) {
		File f = new File(path + ".xdef");
		if (f.exists() && !f.isFile()) {
			throw new IllegalArgumentException("Given file path is not valid");
		}
	}

	/** Calling the program from command line.
	 *
	 * @param args array of parameters.
	 * <p><tt>[-in | --input] file [-out | --output] file [-r | --root] element
	 * [-l | --logFile] file</tt> </p>
	 * <ul>
	 *  <li><tt>[-in | --input] file</tt> - input dtd file</li>
	 *  <li><tt>[-out | --output] file</tt> - output X-definition file</li>
	 *  <li><tt>[-r | --root] element</tt> - root element name</li>
	 *  <li><tt>[-l | --logFile] file</tt> - log file</li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"XsdToXdef parameters:\n" +
"  -in, --input <PATH> input schema file location\n" +
"  -out, --output <PATH> output file or directory name\n" +
"  -r, --root <ROOT> name of root element \n" +
"  -l, --logFile <PATH> log file name \n" +
"  -?, -h, --help help";
		final StringBuilder err = new StringBuilder();

		String INPUT = "--input";
		String OUTPUT = "--output";
		String LOGFILE = "--logFile";
		String ROOT = "--root";
		boolean valueGetMode = false;
		String type = null;
		String input = null;
		String output = null;
		String log = null;
		String root = null;

		for (int i = 0; i < args.length; i++) {
			String parameter = args[i];
			if (valueGetMode) {
				if (parameter.startsWith("-")) {
					err.append("Parameter value for '")
						.append(type).append("' is missing\n");
					break;
				}
				if (INPUT.equals(type)) {
					input = parameter;
				} else if (OUTPUT.equals(type)) {
					output = parameter;
				} else if (LOGFILE.equals(type)) {
					log = parameter;
				} else if (ROOT.equals(type)) {
					root = parameter;
				} else {
					if (parameter.startsWith("-")) {
						err.append("Parameter value for '")
							.append(type).append("' is missing\n");
					} else {
						err.append("Unknown parameter value type '")
							.append(type).append("'\n");
					}
					break;
				}
				valueGetMode = false;
			} else {
				if ("-h".equals(parameter) || "-?".equals(parameter)
					|| "--help".equals(parameter)) {
					System.out.println(info);
					return;
				} else if ("-in".equals(parameter) || INPUT.equals(parameter)) {
					if (input != null) {
						err.append("Input file was already set\n");
					}
					valueGetMode = true;
					type = INPUT;
				} else if ("-out".equals(parameter)||OUTPUT.equals(parameter)) {
					if (output != null) {
						err.append("Output file was already set\n");
					}
					valueGetMode = true;
					type = OUTPUT;
				} else if ("-l".equals(parameter)||LOGFILE.equals(parameter)) {
					if (log != null) {
						err.append("Log file ws already set\n");
					}
					valueGetMode = true;
					type = LOGFILE;
				} else if ("-r".equals(parameter) || ROOT.equals(parameter)) {
					if (root != null) {
						err.append("Root element was already set\n");
					}
					valueGetMode = true;
					type = ROOT;
				} else {
					err.append("Unknown parameter: ")
						.append(parameter)
						.append('\n');
					break;
				}
			}
		}
		// validating input file
		if (input == null || "".equals(input)) {
			err.append("Input file parameter is missing\n");
		}
		//validating output file
		if (output == null || "".equals(output)) {
			err.append("Output file or directory parameter is missing\n");
		}
		//validating output file
		if (root == null || "".equals(root)) {
			err.append("Root element name is missing\n");
		}
		if (err.length() > 0) {
			throw new RuntimeException(err + info);
		}
		//creating convertor
		DTDToXdef dtd2xdef = new DTDToXdef();
		//setting parameters
		try {
			if (log != null) {
				dtd2xdef.setLogFile(log);
			}
		} catch (Exception ex) {
			throw new RuntimeException(
				"Exception during setting parameters:", ex);
		}
		//converting
		try {
			dtd2xdef.genXdef(input, root, output);
		} catch (Exception ex) {
			throw new RuntimeException("Exception during converting dtd", ex);
		}
	}
}