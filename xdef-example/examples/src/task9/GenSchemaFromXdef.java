package task9;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xdef.sys.FUtils;
import org.xdef.util.XdefToXsd;
import org.xml.sax.SAXException;

/** Generate XML schema from X-definition.*/
public class GenSchemaFromXdef {
	
	public static void main(final String... args) throws Exception {

		// 1. create XML schema from X-defiontion
		XdefToXsd.main(
			"-i", "task9/input/Town.xdef", // input file with X-definition
			"-o", "task9/output", // directory where to create XML schema
			"-m", "Town", // name of model in X-definition
			"-x", "main"); // name of X-definition

		// 2. check created XML schema with valid XML data
		String xsFile = "task9/output/main.xs"; // created XML schema
		String s = checkXML(xsFile, "task9/input/Town_valid.xml"); // valid version
		if (!s.isEmpty()) { // reported error
			FUtils.writeString(new File("task9/errors/schemaErr1.txt"), s);
			System.err.println("ERROR, see task9/errors/schemaErr1.txt");
			return;
		}

		// 3. check created XML schema with invalid XML data
		s = checkXML(xsFile, "task9/input/Town_invalid.xml");
		if (s.isEmpty()) { // error not reported
			FUtils.writeString(new File("task9/errors/schemaErr2.txt"),
				"error was not recognized by generated schema");
			System.err.println("ERROR, see task9/errors/schemaErr2.txt");
		} else {
			System.out.println("OK, Task9, XML schema created to task9/output/main.xs");
		}
	}
	
	/** Check XML data with XML schema.
	 * @param xsName XML schema file name.
	 * @param xmlName XML data file name.
	 * @return empty string if XML data are valid or return message why XML data
	 * is not valid.
	 */
	private static String checkXML(final String xsName, final String xmlName) {
		try { //check XML data with X-definition
			Source source = new StreamSource(new File(xmlName));
			SchemaFactory xsdFactory =
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = xsdFactory.newSchema(new File(xsName));
			schema.newValidator().validate(source);
			return ""; // OK
		} catch (IOException | SAXException ex) {
			return ex.getMessage(); // not valid
		}
	}
}