package task4;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import java.util.Properties;

public class Orders1 {
	public static void main(String[] args) throws Exception {
		// compile XDPool from the X-definition
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task4/Orders1.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");

		// set variables "products", "customers" and "output"
		xdoc.setVariable("products", "task4/input/Products.xml");
		xdoc.setVariable("customers", "task4/input/Customers.xml");
		xdoc.setVariable("output",
			XDFactory.createXDXmlOutStream("task4/output/Orders.xml", "UTF-8", true));

		// prepare error reporter
		ArrayReporter reporter = new ArrayReporter();

		// run validation mode (you can also try task4/input/Order_err.xml)
		xdoc.setProperties(props);
		xdoc.xparse("task4/input/Orders.xml", reporter);

		// check errors
		if (reporter.errorWarnings()) {
			// write log file with errors
			PrintStream ps = new PrintStream("task4/errors/Orders_err.txt");
			reporter.printReports(ps); //vytisteni chyb
			ps.close();
			System.err.println("Incorrect input data");
		} else {
			System.out.println("OK");
		}
	}
}