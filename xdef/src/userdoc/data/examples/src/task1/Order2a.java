package task1;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.w3c.dom.Element;

public class Order2a {
	public static void main(String... args) throws IOException {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task1/Order2a.xdef");

		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Prepare the error reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order.xml", reporter);

		// Get the variable "errs".
		XDValue errs = xdoc.getVariable("errs");

		// Check if an error was reported
		if (errs != null && !errs.isNull()) {
			// Write errors to the file
			KXmlUtils.writeXml("task1/errors/Order_err.xml", errs.getElement()); 
			System.err.println("Incorrect input data");
		} else {
			// No errors, write parsed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", result);
			System.out.println("OK, Task1.Order2a");
		}
	}
}