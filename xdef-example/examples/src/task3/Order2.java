package task3;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;

public class Order2 {
	public static void main(String[] args) throws Exception {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task3/Order2.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// set variables "products" and "customers"
		xdoc.setVariable("products", "task3/input/Products.xml");
		xdoc.setVariable("customers", "task3/input/Customers.xml");

		// Prepare error reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task3/input/Order_err.xml)
		xdoc.xparse("task3/input/Order.xml", reporter);

		// Check errors
		if (reporter.errorWarnings()) {
			// Print errors to file
			PrintStream ps = new PrintStream("task3/errors/Order_err.txt");
			reporter.printReports(ps);
			ps.close();
			System.err.println("Incorrect input data");
		} else {
			// Write processed document to the file
			KXmlUtils.writeXml("task3/output/Order.xml", xdoc.getElement());
		   System.out.println("OK");
		}
	}
}