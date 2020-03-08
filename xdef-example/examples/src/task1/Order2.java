package task1;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Order2 {
	public static void main(String[] args) throws Exception {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task1/Order2.xdef");

		// Create instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Create reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order.xml", reporter);

		// Get Container with errors from the variable "errors" in X-definition.
		XDContainer errors = (XDContainer) xdoc.getVariable("errors");

		// Check if an error was reported
		if (errors.getXDItemsNumber() > 0) { // errors reported
			// Run the construction mode to create document with errors.
			// The context is in the variable "errors"
			result = xdoc.xcreate("Errors", null);
			// write errors to the file
			KXmlUtils.writeXml("task1/errors/Order_err.xml", result);
			System.err.println("Incorrect input data");
		} else {
			// No errors, write the processed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", result);
			System.out.println("OK");
		}
	}
}