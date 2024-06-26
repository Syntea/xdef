package org.xdef.util;

import org.xdef.util.xsd2xd.Convertor;
import org.xdef.sys.FileReportWriter;
import org.xdef.util.xsd2xd.xd.Schema_1_0_Processor;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xdef.sys.SReporter;
import org.xml.sax.SAXException;

/** Represents XML Schema to X-definition convertor.
 * (see {@link org.xdef.util.XsdToXdef#main(String[])})
 * @author Ilia Alexandrov
 */
public class XsdToXdef {

	/** Constant that represents XML Schema version 1.0. */
	private static final byte SCHEMA1_0 = 1;
	/** Prefix of X-definition nodes in output documents. */
	private final String _xdefPrefix;
	/** Input XML Schema version. */
	private final byte _schemaVersion;
	/** Reporter for reporting warnings and errors. */
	private final SReporter _reporter;

	/** Creates instance of Convertor with default settings. X-definition nodes
	 * prefix as "xd", input XML Schema version as XML Schema 1.0, output
	 * X-definition version as X-definition 2.0, debug mode is set to off and
	 * reporter is set to print reports to System.err print stream.
	 */
	private XsdToXdef(String xdefPrefix,
		PrintStream out) {
		_xdefPrefix = xdefPrefix == null ? "xd" : xdefPrefix;
		_schemaVersion = SCHEMA1_0;
		_reporter = new SReporter(new FileReportWriter(
			out == null ? System.out : out, false));
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
	public static void genCollection(URL schemaURL,
		String collectionFileName,
		String xdefPrefix,
		PrintStream out)
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
	public static void genCollection(String schemaFilePath,
		String collectionFileName,
		String xdefPrefix,
		PrintStream out) throws MalformedURLException, RuntimeException,
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
	public static void genCollection(String schemaFilePath,
		String xdefPrefix,
		PrintStream out)
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
	public static void genCollection(URL schemaURL,
		String xdefPrefix,
		PrintStream out) {
		XsdToXdef c = new XsdToXdef(xdefPrefix, out);
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
	public static void genXdefFiles(URL schemaURL,
		String directoryName,
		String xdefPrefix,
		PrintStream out) throws IOException {
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
	public static void genXdefFiles(String schemaFilePath,
		String directoryName,
		String xdefPrefix,
		PrintStream out) throws MalformedURLException, IOException {
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
	private Convertor getXdefGenerator(URL schemaURL, boolean separately) {
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
	private void checkOutputDirectoryPath(String path) {
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
	private void checkOutputFilePath(String path) {
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
	private Validator checkSchema(URL url) {
		try {
			File schemaFile = new File(url.getPath());
			if (!schemaFile.exists()) {
				throw new IllegalArgumentException(
					"Schema file does not exists");
			}
			SchemaFactory factory = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(schemaFile);
			return schema.newValidator();
		} catch (IllegalArgumentException | SAXException ex) {
			throw new IllegalArgumentException("Not valid XML Schema file", ex);
		}
	}

	/** Run class from command line.
	 * @param args array of string with command line arguments:
	 * <ul>
	 * <li>-i, --input
	 * <p> &lt;PATH&gt; input main schema location.
	 * <li>-o, --output
	 * <p> &lt;PATH&gt; output file or directory.
	 * <li>-s, --output
	 * <p> each schema generate to the separate X-definition file.
	 * <li>-p
	 * <p> namespace prefix of X-definitions
	 * <li>-?, -h, --help help
	 * </ul>
	 */
	public static void main(String... args) {
		String info =
"Using XsdToXdef: \n"
+ "-i, --input <PATH> input main schema location \n"
+ "-o, --output <PATH> output file or directory name \n"
+ "-s, --separated every schema to standalone xdefinition file \n"
+ "-p, --xdefPrefix <PREFIX> prefix for xdefinition nodes \n"
+ "-l, --logFile <PATH> log file name \n"
+ "-?, -h, --help help";
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
		OUTER:
		for (String parameter : args) {
			if (valueGetMode) {
				if (parameter.startsWith("-")) {
					err.append("Parameter value for '")
						.append(type).append("' is missing\n");
					break;
				}
				if (null == type) {
					if (parameter.startsWith("-")) {
						err.append("Parameter value for '")
							.append(type).append("' is missing\n");
					} else {
						err.append("Unknown parameter value type '")
							.append(type).append("'\n");
					}
					break;
				} else {
					switch (type) {
						case "--input":
							input = parameter;
							break;
						case "--output":
							output = parameter;
							break;
						case "--xdefPrefix":
							prefix = parameter;
							break;
						default:
							if (parameter.startsWith("-")) {
								err.append("Parameter value for '")
									.append(type).append("' is missing\n");
							} else {
								err.append("Unknown parameter value type '")
									.append(type).append("'\n");
							}
							break;
					}
				}
				valueGetMode = false;
			} else {
				switch (parameter) {
					case "-h":
					case "-?":
					case "--help":
						System.out.println(info);
						return;
					case "-i":
					case "--input":
						if (input != null) {
							err.append("Input file is already set\n");
						}	valueGetMode = true;
						type = "--input";
						break;
					case "-o":
					case "--output":
						if (output != null) {
							err.append("Output file is already set\n");
						}	valueGetMode = true;
						type = "--output";
						break;
					case "-s":
					case "--separated":
						if (separated) {
							err.append("Separated mode already set\n");
						}	separated = true;
						break;
					case "-p":
					case "--xdefPrefix":
						if (prefix != null) {
							err.append(
								"Prefix for X-definition elements already set\n");
							return;
						}	valueGetMode = true;
						type = "--xdefPrefix";
						break;
					default:
						err.append("Unknown parameter: ").append(parameter)
							.append("\n");
						break OUTER;
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
		} catch (IOException | RuntimeException ex) {
			throw new RuntimeException("Exception when converting schema", ex);
		}
	}
}