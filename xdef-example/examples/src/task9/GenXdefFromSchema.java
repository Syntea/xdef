package task9;

import java.io.File;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.FUtils;
import org.xdef.util.XsdToXdef;

/** Generate XML schema from X-definition.*/
public class GenXdefFromSchema {
	
	public static void main(final String... args) throws Exception {
		// 1. if X-definition not exists
		if (!new File("task9/output/main.xs").exists()) { 
			GenSchemaFromXdef.main(); // generate X-definition
		}
		// 2. create XML schema from X-defiontion
		XsdToXdef.main(
			"-i", "task9/output/main.xs", // input XML schema file (fro previous step)
			"-o", "task9/output/main.xdef"); // where to create XML schema

		// 3. check created X-definition with valid XML data
		String xdFile = "task9/output/main.xdef"; // created XML schema
		String s = checkXd(xdFile, "task9/input/Town_valid.xml"); // valid version
		if (!s.isEmpty()) { // reported error
			FUtils.writeString(new File("task9/errors/schemaErr1.txt"), s);
			System.err.println("ERROR, see task9/errors/schemaErr1.txt");
			return;
		}

		// 4. check created X-definition with invalid XML data
		s = checkXd(xdFile, "task9/input/Town_invalid.xml");
		if (s.isEmpty()) { // error not reported
			FUtils.writeString(new File("task9/errors/schemaErr2.txt"),
				"error was not recognized by generated X-definition");
			System.err.println("ERROR, see task9/errors/schemaErr2.txt");
		} else {
			System.out.println(
				"OK, Task9, X-definbition created to task9/output/main.xdef");
		}
	}
	
	/** Check XML data with X-definition.
	 * @param xdName file name with X-definition.
	 * @param xmlName XML data file name.
	 * @return empty string if XML data are valid or return message why XML data
	 * is not valid.
	 */	
	private static String checkXd(final String xdName, final String xmlName) {
		try { //check XML data with X-definition
			XDPool xp = XDFactory.compileXD(null, xdName);
			xp.createXDDocument().xparse(xmlName, null);
			return ""; // OK
		} catch (RuntimeException ex) {
			return ex.getMessage(); // not valid
		}
	}
}