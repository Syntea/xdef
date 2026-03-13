package task3;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import org.xdef.sys.FUtils;

public class Order1 {
    public static void main(String... args) throws IOException {
        // ensure the directories task3/output and task3/errors are clear and exists
        FUtils.deleteAndCreateDir("task3/output");
        FUtils.deleteAndCreateDir("task3/errors");

        // Compile the X-definition source to the XDPool object
        XDPool xpool = XDFactory.compileXD(null, "src/task3/Order1.xdef");

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Order");

        // set variables "products" and "customers"
        xdoc.setVariable("products", "task3/input/Products.xml");
        xdoc.setVariable("customers", "task3/input/Customers.xml");

        // Prepare error reporter
        ArrayReporter reporter = new ArrayReporter();

        // Run validation mode (you can also try task3/input/Order_err.xml)
        xdoc.xparse("task3/input/Order.xml", reporter);

        // Check if an error was reported
        if (reporter.errorWarnings()) {
            System.err.println("Task3.Order1 Incorrect input data");
            try ( // Print errors to the file
                PrintStream ps = new PrintStream("task3/errors/Order_err.txt")) {
                reporter.printReports(ps);
            }
        } else {
            // write processed document
            KXmlUtils.writeXml("task3/output/Order_123.xml", xdoc.getElement());
            System.out.println("OK, Task3.Order1");
        }
    }
}