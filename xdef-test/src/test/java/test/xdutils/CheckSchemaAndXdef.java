package test.xdutils;

import org.xdef.sys.SUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.util.XdefToXsd;
import org.xdef.util.XsdToXdef;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/** Generate XML schema from X-definition and check validation of given
 * XML document with generated Schema. If the property "xdef.testing"
 * is set to "genReverse", it is also generated X-definition from generated
 * schema and the XML document is checked also with it.
 * @author Vaclav Trojan
 */
public class CheckSchemaAndXdef {

	/** Prevent create an instance of this class.*/
	private CheckSchemaAndXdef() {}

	private static boolean chkReverseOption() {
		return "genReverse".equalsIgnoreCase(System.getProperty("xdef.testing"));
	}

	/** Validate XML data parsed with X-definition and with
	 * XML Schema generated from X-definition.
	 * @param schemaDir directory where XML schema files will be generated.
	 * @param xdFilename X-definition file name.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is valid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is not valid.
	 */
	public static String valid(String schemaDir,
		String xdFilename,
		String xdName,
		String model,
		String xml) {
		File[] xdefs = new File[] {new File(xdFilename)};
		return valid(new File(schemaDir), xdefs, xdName, model, new File(xml));
	}

	/** Validate XML data parsed with X-definitions and with
	 * XML Schema generated from X-definitions.
	 * @param schemaDir directory where XML schema files will be generated.
	 * @param xdFilenames array with X-definition file names.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is valid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is not valid.
	 */
	public static String valid(String schemaDir,
		String[] xdFilenames,
		String xdName,
		String model,
		String xml) {
		File[] xdefs = new File[xdFilenames.length];
		for (int i = 0; i < xdefs.length; i++) {
			xdefs[i] = new File(xdFilenames[i]);
		}
		return valid(new File(schemaDir), xdefs, xdName, model, new File(xml));
	}

	/** Validate XML data parsed with X-definitions and with
	 * XML Schema generated from X-definitions.
	 * @param schemaDir the directory where XML schema files will be generated.
	 * @param xdefs array with X-definition files.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is valid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is not valid.
	 */
	public static String valid(File schemaDir,
		File[] xdefs,
		String xdName,
		String model,
		File xml) {
		String result = " (" + xdName + "," + xml.getName() + ")";
		XDDocument xd;
		try {
			XDPool xp = XDFactory.compileXD(null, (Object[]) xdefs);
			xd = xp.createXDDocument(xdName);
		} catch (Exception ex) {
			xd = null;
			System.err.println("X-definition can't be executed due to\n"
				+ ex.getMessage());
		}
		if (xd != null) {
			try {
				xd.xparse(xml, null);
			} catch (Exception ex) {
				result = "XDEF ERROR" + result + "\n" + ex.getMessage();
			}
		}
		try {
			XdefToXsd.genSchema(xdefs,
				schemaDir.getAbsolutePath(),
				xdName,
				model,
				null,
				null,
				null);
		} catch (Exception ex) {
			throw new RuntimeException("SCHEMA GENERATION ERROR" + result, ex);
		}
		File schema = new File(schemaDir, xdName + ".xsd");
		if (!schema.exists() || !schema.isFile()) {
			throw new RuntimeException("SCHEMA FILE NOT EXISTS: "
				+ schema.getAbsolutePath() + result);
		}
		try {
			SchemaFactory schemaFactory =
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schemaGrammar = schemaFactory.newSchema(schema);
			Validator schemaValidator = schemaGrammar.newValidator();
			schemaValidator.validate(new StreamSource(xml));
		} catch (Exception ex) {
			result = result.contains(" ERROR")
				? result + "\nSCHEMA ERROR\n" + ex.getMessage()
				: ("SCHEMA ERROR" + result + "\n" + ex.getMessage());
		}
		if (chkReverseOption()) {
			try {
				XsdToXdef.main(
					"-in", schemaDir.getAbsolutePath() + "/" + xdName + ".xsd",
					"-out", schemaDir.getAbsolutePath() + "/" + xdName + ".xd");
				XDPool xp = XDFactory.compileXD(null, (Object[])
					SUtils.getFileGroup(schemaDir.getAbsolutePath()+ "/*.xd"));
				xd = xp.createXDDocument(xdName);
			} catch (Exception ex) {
				throw new RuntimeException(result.contains(" ERROR")
					? result + "\nREVERSE XDEFFINITION ERROR\n"
					: ("REVERSE XDEFFINITION ERROR\n"+result), ex);
			}
			try {
				xd.xparse(xml, null);
			} catch (Exception ex) {
				result = result.contains(" ERROR")
					? result + "\nREVERSE XDEF ERROR\n" + ex.getMessage()
					: ("REVERSE XDEF ERROR" + result + "\n" + ex.getMessage());
			}
		}
		return result.contains(" ERROR") ? result : "OK" + result;
	}

	/** Test if XML data are found invalid when parsed with X-definition
	 * and with XML Schema generated from X-definition.
	 * @param schemaDir directory where XML schema files will be generated.
	 * @param xdFilename X-definition file name.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is invalid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is valid.
	 */
	public static String invalid(String schemaDir,
		String xdFilename,
		String xdName,
		String model,
		String xml) {
		File[] xdefs = new File[] {new File(xdFilename)};
		return invalid(new File(schemaDir), xdefs, xdName, model,new File(xml));
	}

	/** Test if XML data are found invalid when parsed with X-definition
	 * and with XML Schema generated from X-definition.
	 * @param schemaDir directory where XML schema files will be generated.
	 * @param xdFilenames array of X-definition file names.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is invalid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is valid.
	 */
	public static String invalid(String schemaDir,
		String[] xdFilenames,
		String xdName,
		String model,
		String xml) {
		File[] xdefs = new File[xdFilenames.length];
		for (int i = 0; i < xdefs.length; i++) {
			xdefs[i] = new File(xdFilenames[i]);
		}
		return invalid(new File(schemaDir), xdefs, xdName, model, new File(xml));
	}

	/** Test if XML data are found invalid when parsed with X-definition
	 * and with XML Schema generated from X-definition.
	 * @param schemaDir directory where XML schema files will be generated.
	 * @param xdefs array of X-definition files.
	 * @param xdName name of X-definition. May be null, then the X-definition
	 * without name or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param xml XML data to be tested.
	 * @return the string started with "OK" if XML is invalid for both
	 * X-definition and XML schema or return the string starting with "ERROR"
	 * if XML is valid.
	 */
	public static String invalid(File schemaDir,
		File[] xdefs,
		String xdName,
		String model,
		File xml) {
		String result = " (" + xdName + "," + xml.getName() + ")";
		XDDocument xd;
		try {
			XDPool xp = XDFactory.compileXD(null, (Object[]) xdefs);
			xd = xp.createXDDocument(xdName);
		} catch (Exception ex) {
			xd = null;
			System.err.println("X-definition can't be executed due to\n"
				+ ex.getMessage());
		}
		if (xd != null) {
			try {
				xd.xparse(xml, null);
				result = "XDEF ERROR not recognized:" + result;
			} catch (Exception ex) {}
		}
		try {
			XdefToXsd.genSchema(xdefs,
				schemaDir.getAbsolutePath(),
				xdName,
				model,
				null,
				null,
				null);
		} catch (Exception ex) {
			throw new RuntimeException("SCHEMA GENERATION ERROR" + result, ex);
		}
		File schema = new File(schemaDir, xdName + ".xsd");
		if (!schema.exists() || !schema.isFile()) {
			throw new RuntimeException("SCHEMA FILE NOT EXISTS: "
				+ schema.getAbsolutePath() + result);
		}
		try {
			SchemaFactory schemaFactory =
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schemaGrammar = schemaFactory.newSchema(schema);
			Validator schemaValidator = schemaGrammar.newValidator();
			schemaValidator.validate(new StreamSource(xml));
			result += "SCHEMA ERROR not recognized" +
				(result.startsWith("XDEF ERROR") ? " and " : "")
				+ result;
		} catch (Exception ex) {}
		if (chkReverseOption()) {
			try {
				XsdToXdef.main(
					"-in", schemaDir.getAbsolutePath() + "/" + xdName + ".xsd",
					"-out", schemaDir.getAbsolutePath() + "/" + xdName + ".xd");
				XDPool xp = XDFactory.compileXD(null, (Object[])
					SUtils.getFileGroup(schemaDir.getAbsolutePath()+ "/*.xd"));
				xd = xp.createXDDocument(xdName);
			} catch (Exception ex) {
				throw new RuntimeException(result.contains(" ERROR")
					? result + "\nREVERSE XDEFFINITION ERROR\n"
					: ("REVERSE XDEFFINITION ERROR\n"+result), ex);
			}
			try {
				xd.xparse(xml, null);
				result = result.contains(" ERROR")
					? result + "\nREVERSE XDEF ERROR NOT RECOGNIZED\n"
					: ("REVERSE XDEF ERROR NOT RECOGNIZED" + result + "\n");
			} catch (Exception ex) {}
		}
		return result.contains(" ERROR") ? result : "OK" + result;
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

	/** Validation of X-definitions and XML schema. This method is possible to
	 * invoke from command line.
	 * @param args Array of strings containing command line arguments.
	 * <ul>
	 * <li><tt>-i</tt> - test will be for invalidity (if not specified then for
	 * validity). Optional.</li>
	 * <li><tt>-v</tt> - test will be for validity. Optional.
	 * NOTE both parameters "-i" and "-v" are not allowed!</li>
	 * <li><tt>-x xDefName</tt> - X-definition name. Optional.</li>
	 * <li><tt>-m modelName</tt> - Model Name. Optional.</li>
	 * <li><tt>-o outDir</tt> - Output directory</li>
	 * <li><tt>files</tt> - list of files with X-definitions (whitecards
	 * accepted)
	 * </li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"Parameters usage: switches X-definition file [X-definition file]... \n"+
"Note the specification of X-definition file may contain wildcard characters.\n"+
"Switches:\n"+
"  [-v | -i]      test will be for validity(-v) or invalidity (-i).\n"+
"                 Optional switch. If not specified then\n"+
"                 it will be used the test for validity\n"+
"  [-m modelName] name of model in X-definition (optional switch)\n"+
"  [-n xDefName]  name of X-definition (optionals switch)\n"+
"  -x xml         xml file\n"+
"  -o outDir      output directory where XML Schema files are created\n)";

		if (args.length == 0) {
			throw new RuntimeException("Parameters missing\n"+ info);
		}
		final ArrayList<File> files = new ArrayList<File>();
		String xdName = null;
		String model = null;
		File base = null;
		File xmlFile = null;
		char validate = 0;
		int i = 0;
		final StringBuilder err = new StringBuilder();
		while (i < args.length) {
			if (args[i] == null || args[i].length() == 1) {
				errMsg(err, args, i+1, "Incorrect parameter");
				i++;
				continue;
			}
			String s;
			char c;
			if (args[i].length() == 2 && args[i].charAt(0) == '-') {
				c = args[i].charAt(1);
				i++;
				switch (c) {
					case 'h':
					case '?':
						System.out.println(info);
						return;
					case 'i':
						if (validate != 0) {
							errMsg(err, args, i, "redefinition");
						}
						validate = c;
						continue;
					case 'v':
						if (validate != 0) {
							errMsg(err, args, i, "redefinition");
						}
						validate = c;
						continue;
				}
				if (i >= args.length) {
					errMsg(err, args, i, "a parameter must follow");
					break;
				}
				s = args[i];
			} else {
				while (i < args.length) {
					s = args[i];
					if (s != null) {
						File[] ff = SUtils.getFileGroup(s);
						if (ff == null || ff.length == 0) {
							errMsg(err, args, i+1, "not a file");
						} else {
							files.addAll(Arrays.asList(ff));
						}
					}
					i++;
				}
				break;
			}
			switch (c) {
				case 'm':
					if (model == null) {
						model = s;
					} else {
						errMsg(err, args, i, "redefinition");
					}
					i++;
					continue;
				case 'n':
					if (xdName == null) {
						xdName = s;
					} else {
						errMsg(err, args, i, "redefinition");
					}
					i++;
					continue;
				case 'o': {
					if (base == null) {
						base = new File(s);
						if (!base.exists()) {
							if (!base.mkdirs()) {
								errMsg(err, args, i, "can't create directory");
							}
						} else if (!base.isDirectory()) {
							errMsg(err, args, i, "directory expected");
						}
					} else {
						errMsg(err, args, i, "redefinition");
					}
					i++;
					continue;
				}
				case 'x': {
					if (xmlFile == null) {
						xmlFile = new File(s);
						if (!xmlFile.exists() || !xmlFile.isFile()) {
							errMsg(err, args, i, "Incorrect XML file");
						}
					} else {
						errMsg(err, args, i, "redefinition");
					}
					i++;
					continue;
				}
				default: {
					errMsg(err, args, i, "unknown switch");
				}
				i++;
			}
		}
		if (err.length() > 0) {
			throw new RuntimeException(err + info);
		}
		if (base == null) {
			throw new RuntimeException("Output directory is not available\n"
				+ info);
		}
		if (xmlFile == null) {
			throw new RuntimeException("XML file is missing\n"+ info);
		}
		File[] xdefs = new File[files.size()];
		xdefs = files.toArray(xdefs);
		String result;
		if (validate == 'i') {
			result = invalid(base,xdefs, xdName, model, xmlFile);
		} else {
			result = valid(base,xdefs, xdName, model, xmlFile);
		}
		if (result.startsWith("OK")) {
			System.out.println(result);
		} else {
			System.err.println(result);
		}
	}
}