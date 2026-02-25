package task3;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;

public class Order2a {
    public static void main(String[] args) throws IOException {
        // Compile the X-definition source to the XDPool object
        XDPool xpool = XDFactory.compileXD(null, "src/task3/Order2a.xdef");

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Order");

        // Set files with reports
        xdoc.setProperty(XDConstants.XDPROPERTY_MESSAGES + "POBJ", "src/task3/POBJ*.properties");

        // Set the actual language for the reporter (you can also try to set "ces")
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
            try ( // print errors to a file
                PrintStream ps = new PrintStream("task3/errors/Order_err.txt")) {
                reporter.printReports(ps); //print errors
            }
            // print the message to the system console
            Report rep = Report.error("POBJ003", null);
            System.err.println("Error Task3.Order2a; " + rep.toString());
        } else {
            // Write the processed document to the file
            KXmlUtils.writeXml("task3/output/Order.xml", xdoc.getElement());
            // print the message to the system console
            Report rep = Report.info("POBJ004", null);
            System.out.println(rep.toString());
           System.out.println("OK, Task3.Order2a ");
        }
    }
}