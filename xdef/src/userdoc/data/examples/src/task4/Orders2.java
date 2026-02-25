package task4;

import java.io.IOException;
import org.xdef.sys.ArrayReporter;
import  org.xdef.XDDocument;
import  org.xdef.XDFactory;
import  org.xdef.XDPool;
import java.io.PrintStream;

public class Orders2 {
    public static void main(String[] args) throws IOException {
        // compile the XDPool from the X-definition source
        XDPool xpool = XDFactory.compileXD(null, "src/task4/Orders2.xdef");

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Orders");

        // set the instance of Orders2ext as user object
        xdoc.setUserObject(new Orders2ext(xpool, // the instance of Orders2ext
          "task4/input/Products.xml", // file with information about commodity items
          "task4/input/Customers.xml", // file with the information about customers
          "task4/output/Orders.xml")); // output file.

        // Prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();

        // run validation mode (you can also try task4/input/Orders_err.xml)
        xdoc.xparse("task4/input/Orders.xml", reporter);

        // Check errors
        if (reporter.errorWarnings()) {
            try ( // write log file with errors
                PrintStream ps = new PrintStream("task4/errors/Orders_err.txt")) {
                reporter.printReports(ps); //print errors
            }
            System.err.println("Task4.Order2 Incorrect input data");
        } else {
            System.out.println("OK, Task4.Order2");
        }
    }
}