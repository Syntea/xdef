package task1;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import org.w3c.dom.Element;

public class Order1 {
	// Compile the X-definition source to the static variable with the XDPool object
	static final XDPool xpool = XDFactory.compileXD(null, "src/task1/Order1.xdef");

	public static void main(String... args) throws IOException {
		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Prepare the error reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order.xml", reporter);

		// Check if an error was reported
		if (reporter.errors()) {
			try ( // Print errors to the file
				PrintStream ps = new PrintStream("task1/errors/Order_err.txt ")) {
				reporter.printReports(ps);
			} 
			System.err.println("Task1.Orders1 Incorrect input data");
		} else {
			// No errors, write the processed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", result);
			System.out.println("OK, Task1.Order1");
		}
	}
}