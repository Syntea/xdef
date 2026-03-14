package task1;

import java.io.File;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import org.w3c.dom.Element;
import org.xdef.sys.FUtils;

public class Order1a {

    public static void main(String... args) throws IOException, ClassNotFoundException{
        // Create the file task1/output/Order1a.xp with compiled X-definition
        Order1a_gen.main();

        XDPool xpool;
        try ( // Reasd the XDPool object from the file
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("task1/output/Order1a.xp"))) {
            xpool = (XDPool) in.readObject();
        }

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Order");

        // Prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();

        // Run validation mode (you can also try task1/input/Order_err.xml)
        Element result = xdoc.xparse("task1/input/Order.xml", reporter);

        // Check if an error was reported
        if (reporter.errorWarnings()) {
            FUtils.deleteAndCreateDir("task1/errors"); // ensure the directory task1/errors exists
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
    }
}