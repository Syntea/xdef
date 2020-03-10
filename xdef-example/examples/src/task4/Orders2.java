package task4;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;

public class Orders2 {
	public static void main(String[] args) throws Exception {
		// compile XDPool from the X-definition
		XDPool xpool = XDFactory.compileXD(null, "src/task4/Orders2.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");

		// set the instance of Orders2ext as user object
		xdoc.setUserObject(new Orders2ext(xpool, // the instance of Orders2ext
		  "task4/input/Products.xml", // file with the infromation obout commodity items
		  "task4/input/Customers.xml", // file with the information abou customers
		  "task4/output/Orders.xml")); // output file.

		// Prepare error reporter
		ArrayReporter reporter = new ArrayReporter();

		// run validation mode (you can also try task4/input/Order_err.xml)
		xdoc.xparse("task4/input/Orders.xml", reporter);

		// Check errors
		if (reporter.errorWarnings()) {
			// write log file with errors
			PrintStream ps = new PrintStream("task4/errors/Orders_err.txt");
			reporter.printReports(ps);
			ps.close();
			System.err.println("Incorrect input data");
		} else {
			System.out.println("OK");
		}
	}
}