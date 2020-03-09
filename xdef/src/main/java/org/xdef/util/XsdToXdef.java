package org.xdef.util;

import org.xdef.impl.util.conv.xsd2xd.Convertor;
import org.xdef.sys.FileReportWriter;
import org.xdef.impl.util.conv.xsd2xd.util.Reporter;
import org.xdef.impl.util.conv.xsd2xd.xdef_2_0.Schema_1_0_Processor;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/** Represents XML Schema to X-definition convertor.
 * (see {@link org.xdef.util.XsdToXdef#main(String[])})
 * @author Alexandrov
 */
public class XsdToXdef {

	/** Constant that represents XML Schema version 1.0. */
	private static final byte SCHEMA1_0 = 1;
	/** Prefix of X-definition nodes in output documents. */
	private String _xdefPrefix;
	/** Input XML Schema version. */
	private byte _schemaVersion;
	/** Reporter for reporting warnings and errors. */
	private Reporter _reporter;

	/** Prevent user to instantiate this class. */
	private XsdToXdef() { this(null, null); }

	/** Creates instance of Convertor with default settings. X-definition nodes
	 * prefix as "xd", input XML Schema version as XML Schema 1.0, output
	 * X-definition version as X-definition 2.0, debug mode is set to off and
	 * reporter is set to print reports to System.err print stream.
	 */
	private XsdToXdef(final String xdefPrefix,
		final PrintStream out) {
		_xdefPrefix = xdefPrefix == null ? "xd" : xdefPrefix;
		_schemaVersion = SCHEMA1_0;
		_reporter = new Reporter(new FileReportWriter(
			out == null ? System.out : out, false),false);
	}

	/** Generates collection with X-definitions from root schema at given URL
	 * and saves file with given file name.
	 * @param schemaURL URL of root schema.
	 * @param collectionFileName name of collection of X-definitions file.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 * @throws IllegalArgumentException not a schema at given URL.
	 * @throws RuntimeException cant find X-definition generator implementation.
	 * @throws IOException if cannot create collection file.
	 */
	public static final void genCollection(final URL schemaURL,
		final String collectionFileName,
		final String xdefPrefix,
		final PrintStream out)
		throws RuntimeException, IllegalArgumentException, IOException {
		if (collectionFileName == null || "".equals(collectionFileName)) {
			throw new IllegalArgumentException("Collection file name is empty");
		}
		XsdToXdef c = new XsdToXdef(xdefPrefix, out);
		c.checkSchema(schemaURL);
		c.checkOutputFilePath(collectionFileName);
		c.getXdefGenerator(schemaURL,false).writeCollection(collectionFileName);
	}

	/** Generates collection with X-definitions from root schema at given path
	 * and saves file with given file name.
	 * @param schemaFilePath path to root schema file.
	 * @param collectionFileName name of collection of X-definitions file.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 * @throws MalformedURLException cant create URL from given path.
	 * @throws IllegalArgumentException not a schema at given URL.
	 * @throws RuntimeException cant find X-definition generator implementation.
	 * @throws IOException if cannot create collection file.
	 */
	public static final void genCollection(final String schemaFilePath,
		final String collectionFileName,
		final String xdefPrefix,
		final PrintStream out) throws MalformedURLException, RuntimeException,
		IllegalArgumentException, IOException {
		if (schemaFilePath == null || "".equals(schemaFilePath)) {
			throw new IllegalArgumentException("Schema file path is empty");
		}
		URL url = new URL("file", "", schemaFilePath);
		genCollection(url, collectionFileName, xdefPrefix, out);
	}

	/** Generates collection from a schema at given path and prints it
	 * to standard output.
	 * @param schemaFilePath path to schema file.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 * @throws MalformedURLException url can not be created.
	 */
	public static final void genCollection(final String schemaFilePath,
		final String xdefPrefix,
		final PrintStream out)
		throws MalformedURLException {
		URL url = new URL("file", "", schemaFilePath);
		genCollection(url, xdefPrefix, out);
	}

	/** Generates collection from a schema at given URL and prints it to
	 * standard output.
	 * @param schemaURL url of schema.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 */
	public static final void genCollection(final URL schemaURL,
		final String xdefPrefix,
		final PrintStream out) {
		final XsdToXdef c = new XsdToXdef(xdefPrefix, out);
		c.checkSchema(schemaURL);
		c.getXdefGenerator(schemaURL, false).printCollection();
	}

	/** Generates X-definition file per XML schema file from root schema
	 * at given URL and saves created file to directory with given name.
	 * @param schemaURL URL of root schema.
	 * @param directoryName name of directory with created X-definition files.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 * @throws IllegalArgumentException not a valid schema at given URL.
	 * @throws RuntimeException cant find implementation of X-definition
	 * generator.
	 * @throws IOException if cannot create X-definition files.
	 */
	public static final void genXdefFiles(final URL schemaURL,
		final String directoryName,
		final String xdefPrefix,
		final PrintStream out) throws IOException {
		if (directoryName == null || "".equals(directoryName)) {
			throw new IllegalArgumentException("Directory name is empty");
		}
		XsdToXdef c = new XsdToXdef(xdefPrefix, out);
		c.checkSchema(schemaURL);
		c.checkOutputDirectoryPath(directoryName);
		c.getXdefGenerator(schemaURL, true).writeXdefFiles(directoryName);
	}

	/** Generates X-definition file per XML schema file from root schema
	 * at given path and saves created file to directory with given name.
	 * @param schemaFilePath path to root schema file.
	 * @param directoryName name of directory with created X-definition files.
	 * @param xdefPrefix namespace prefix of X-definitions (if null then "xd").
	 * @param out PrintStream where to print messages (if null then System.out).
	 * @throws MalformedURLException cant create URL from given path.
	 * @throws RuntimeException cant find implementation of X-definition
	 * generator.
	 * @throws IllegalArgumentException not a valid schema at given path.
	 * @throws IOException if cannot create X-definition files.
	 */
	public static final void genXdefFiles(final String schemaFilePath,
		final String directoryName,
		final String xdefPrefix,
		final PrintStream out) throws MalformedURLException, IOException {
		if (schemaFilePath == null || "".equals(schemaFilePath)) {
			throw new IllegalArgumentException("Schema file path is empty");
		}
		URL url = new URL("file", "", schemaFilePath);
		genXdefFiles(url, directoryName, xdefPrefix, out);
	}

	/** Creates and returns proper X-definition generator implementation
	 * according to set parameters.
	 * @param separetely XML Schema file as X-definiiton file.
	 * @throws RuntimeException no proper implementation.
	 */
	private Convertor getXdefGenerator(final URL schemaURL,
		final boolean separately) {
		if (_schemaVersion == SCHEMA1_0) {
			return new Schema_1_0_Processor(
				_xdefPrefix, _reporter, schemaURL, separately);
		}
		throw new RuntimeException("Could not find implementation of "
			+ "X-definition generator according to set parameters");
	}

	/** Checks directory path.
	 * @param path path of directory.
	 * @throws IllegalArgumentException given path is illegal.
	 */
	private void checkOutputDirectoryPath(final String path) {
		File dir = new File(path);
		if (dir.exists() && !dir.isDirectory()) {
			throw new IllegalArgumentException(
				"Given directory path is not valid");
		}
	}

	/** Checks file path.
	 * @param path file path.
	 * @throws IllegalArgumentException file path is illegal.
	 */
	private void checkOutputFilePath(final String path) {
		File f = new File(path);
		if (f.exists() && !f.isFile()) {
			throw new IllegalArgumentException("Given file path is not valid");
		}
	}

	/** Checks if file at given url is valid XML Schema.
	 * @param url path to schema file.
	 * @return Schema validator.
	 * @throws IllegalArgumentException not a valid schema file url.
	 */
	private Validator checkSchema(final URL url) {
		try {
			File schemaFile = new File(url.getPath());
			if (!schemaFile.exists()) {
				throw new IllegalArgumentException(
					"Schema file does not exists");
			}
			SchemaFactory factory = SchemaFactory.newInstance(
				"http://www.w3.org/2001/XMLSchema");
			Schema schema = factory.newSchema(schemaFile);
			return schema.newValidator();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Not valid XML Schema file", ex);
		}
	}

	/** Run class from command line.
	 * @param args array of string with command line arguments:
	 * <ul>
	 * <li>-in, --input
	 * <p> &lt;PATH&gt; input main schema location.</p>
	 * </li>
	 * <li>-out, --output
	 * <p> &lt;PATH&gt; output file or directory.</p>
	 * </li>
	 * <li>-s, --output
	 * <p> each schema generate to the separate X-definition file.</p>
	 * </li>
	 * <li>-p
	 * <p> namespace prefix of X-definitions</p>
	 * </li>
	 * <li>-?, -h, --help help</li>
	 * </ul>
	 */
	public static final void main(final String... args) {
		String info =
"Using XsdToXdef: \n"
+ "-in, --input <PATH> input main schema location\n"
+ "-out, --output <PATH> output file or directory name\n"
+ "-s, --separated every schema to standalone xdefinition file\n"
+ "-p, --xdefPrefix <PREFIX> prefix of X-Definition namespace\n"
+ "-l, --logFile <PATH> log file name\n"
+ "-h, --help help";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Parameters missing!\n" + info);
		}
		final StringBuilder err = new StringBuilder();
		boolean valueGetMode = false;
		String input = null;
		String output = null;
		boolean separated = false;
		String prefix = null;
		String type = null;
		for (int i = 0; i < args.length; i++) {
			String parameter = args[i];
			if (valueGetMode) {
				if (parameter.startsWith("-")) {
					err.append("Parameter value for '")
						.append(type).append("' is missing\n");
					break;
				}
				if ("--input".equals(type)) {
					input = parameter;
				} else if ("--output".equals(type)) {
					output = parameter;
				} else if ("--xdefPrefix".equals(type)) {
					prefix = parameter;
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
				if (parameter.equals("-h") || parameter.equals("-?")
					|| "--help".equals(parameter)) {
					System.out.println(info);
					return;
				} else if (parameter.equals("-in")
					|| parameter.equals("--input")) {
					if (input != null) {
						err.append("Input file is already set\n");
					}
					valueGetMode = true;
					type = "--input";
				} else if ("-out".equals(parameter)
					|| "--output".equals(parameter)) {
					if (output != null) {
						err.append("Output file is already set\n");
					}
					valueGetMode = true;
					type = "--output";
				} else if ("-s".equals(parameter)
					|| "--separated".equals(parameter)) {
					if (separated) {
						err.append("Separated mode already set\n");
					}
					separated = true;
				} else if ("-p".equals(parameter)
					|| "--xdefPrefix".equals(parameter)) {
					if (prefix != null) {
						err.append(
							"Prefix for X-definition elements already set\n");
						return;
					}
					valueGetMode = true;
					type = "--xdefPrefix";
				} else {
					err.append("Unknown parameter: ").append(parameter)
						.append("\n");
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
		if (err.length() > 0) {
			throw new RuntimeException(err + info);
		}
		try {
			if (separated) {
				genXdefFiles(input, output, prefix, System.out);
			} else {
				genCollection(input, output, prefix, System.out);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Exception when converting schema", ex);
		}
	}
}