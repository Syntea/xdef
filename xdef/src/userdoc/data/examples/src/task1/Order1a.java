package task1;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import org.w3c.dom.Element;

public class Order1a {
    public static void main(String... args) {
        try {
            // Reasd the XDPool object from the file
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("task1/output/Order1a.xp"));
            XDPool xpool = (XDPool) in.readObject();
            in.close();

            // Create an instance of the XDDocument object (from XDPool)
            XDDocument xdoc = xpool.createXDDocument("Order");

            // Prepare the error reporter
            ArrayReporter reporter = new ArrayReporter();

            // Run validation mode (you can also try task1/input/Order_err.xml)
            Element result = xdoc.xparse("task1/input/Order.xml", reporter);

            // Check if an error was reported
            if (reporter.errorWarnings()) {
                try ( // Print errors to the file
                    PrintStream ps = new PrintStream("task1/errors/Order_err.txt")) {
                    reporter.printReports(ps);
                }
                System.err.println("Task1.Orders1a Incorrect input data");
            } else {
                // No errors, write the processed document to the file
                KXmlUtils.writeXml("task1/output/Order.xml", result);
                System.out.println("OK, Task1.Order1a");
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Error in Task1.Order1a\n" + ex); // unexpected exception
        }
    }
}