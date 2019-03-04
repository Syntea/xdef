import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;

/** Validate XML with X-definition - demonstration of script commands. */
public class Example2 {

	public static void main(String[] args) throws Exception {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example2.xdef";
		String xmlData = "./src/Example2.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xpool.createXDDocument();

		// 3. Validate and process XML data with XDefinition.
		// parse data with XDefinition. Note if argument with reporter is null
		// then a runtime exception will be thrown if an error occurs.
		// The result of parsing is given as result of parsing.
		Element el = xdoc.xparse(xmlData, null);

		// 4. print results from XDDocument
		System.out.println("Company name: " + el.getAttribute("name"));
		System.out.println("Employees: " + xdoc.getVariable("count"));
		System.out.println("Total salary: " + xdoc.getVariable("salary"));
		System.out.println("Average: " + xdoc.getVariable("average"));
	}

}