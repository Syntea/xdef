package task3;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;

public class Order2a {
	public static void main(String[] args) throws Exception {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task3/Order2a.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Set files with reports
		xdoc.setProperty(XDConstants.XDPROPERTY_MESSAGES + "POBJ",
			"src/task3/POBJ*.properties");

		// Set the actual language for reporter (you can also try to set "ces")
		xdoc.setProperty(XDConstants.XDPROPERTY_MSGLANGUAGE, "eng"); // English

		// Set external variables "products" and "customers"
		xdoc.setVariable("products", "task3/input/Products.xml");
		xdoc.setVariable("customers", "task3/input/Customers.xml");

		// Prepare error reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run the validation mode (you can also try task3/input/Order_err.xml)
		xdoc.xparse("task3/input/Order.xml", reporter);

		// Check errors
		if (reporter.errorWarnings()) {
			// print errors to the file
			PrintStream ps = new PrintStream("task3/errors/Order_err.txt");
			reporter.printReports(ps); //vytisteni chyb
			ps.close();
			// print the message to the system consloe
			Report rep = Report.error("POBJ003", null);
			System.err.println(rep.toString());
		} else {
			// Write processed document to the file
			KXmlUtils.writeXml("task3/output/Order.xml", xdoc.getElement());
			// print the message to the system consloe
			Report rep = Report.info("POBJ004", null);
			System.out.println(rep.toString());
		}
	}
}