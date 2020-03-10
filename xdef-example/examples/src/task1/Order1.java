package task1;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import org.w3c.dom.Element;

public class Order1 {
	// Compile X-definitions to XDPool
	static final XDPool xpool = XDFactory.compileXD(null, "src/task1/Order1.xdef");

	public static void main(String[] args) throws Exception {
		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Create reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order.xml", reporter);

		// Check if an error was reported
		if (reporter.errors()) {
			// Print errors to the file
			PrintStream ps = new PrintStream("task1/errors/Order_err.txt ");
			reporter.printReports(ps);
			ps.close();
			System.err.println("Incorrect input data");
		} else {
			// No errors, write the processed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", result);
			System.out.println("OK");
		}
	}
}