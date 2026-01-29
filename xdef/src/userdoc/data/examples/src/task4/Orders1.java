package task4;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import java.util.Properties;

public class Orders1 {
	public static void main(String... args) throws IOException {
		// compile XDPool from the X-definition source
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task4/Orders1.xdef");

		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");

		// set variables "products", "customers" and "output"
		xdoc.setVariable("products", "task4/input/Products.xml");
		xdoc.setVariable("customers", "task4/input/Customers.xml");
		xdoc.setVariable("output", 
			XDFactory.createXDXmlOutStream("task4/output/Orders.xml", "UTF-8", true));

		// prepare the error reporter
		ArrayReporter reporter = new ArrayReporter();

		// run validation mode (you can also try task4/input/Order_err.xml)
		xdoc.setProperties(props);
		xdoc.xparse("task4/input/Orders.xml", reporter);

		// check errors
		if (reporter.errorWarnings()) {
			try ( // write log file with errors
				PrintStream ps = new PrintStream("task4/errors/Orders_err.txt")) {
				reporter.printReports(ps); //print errors
			} //print errors
			System.err.println("Task4.Order1 Incorrect input data");
		} else {
			System.out.println("OK, Task4.Order1"); 
		}
	}
}