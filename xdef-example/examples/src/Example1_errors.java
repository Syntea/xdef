import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;

/** Validate XML with X-definition (print errors from input data) */
public class Example1_errors {

	public static void main(String... args) throws RuntimeException {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example1.xdef";
		String xmlData = "./src/Example1_errors.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		XDPool xp = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xp.createXDDocument();

		// 3. Validate and process XML data.
		//reports will be recorded to the ArrayReporter.
		xdoc.xparse(xmlData, new ArrayReporter());//validate and process data

		// 4. test if an error was reported
		if (xdoc.errors()) {
			System.out.println("Errors detected:");
			// print error messages
			xdoc.printReports(System.err);
		} else { // no errors
			System.err.println("Errors not detected!");
		}
	}

}