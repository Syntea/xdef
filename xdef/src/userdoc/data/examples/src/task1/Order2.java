package task1;

import java.io.IOException;
import java.io.PrintStream;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Order2 {
	public static void main(String... args) throws IOException {
		// Compile the X-definition source to variable
		XDPool xpool = XDFactory.compileXD(null, "src/task1/Order2.xdef");

		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Prepare the error reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order_err.xml", reporter);

		// Get Container with errors from the variable "errors" in X-definition.
		XDContainer errors = (XDContainer) xdoc.getVariable("errors");

		// Check if an error was reported
		if (errors.getXDItemsNumber() > 0) { // errors reported
			// Run the construction mode to create document with errors.
			// The context is in the variable "errors"
			result = xdoc.xcreate("Errors", null);
			// write errors to the file
			KXmlUtils.writeXml("task1/errors/Order_err.xml", result); 
			String s = xdoc.getVariable("errors").toString();
			try (PrintStream ps = new PrintStream("task1/errors/Order_err.txt ")) {
				ps.print(s);
			} 
			System.out.println(s);
			System.out.println("OK, Task1.Order2");
		} else {
			System.err.println("Task1.Order2 errors not detected");
		}
	}
}