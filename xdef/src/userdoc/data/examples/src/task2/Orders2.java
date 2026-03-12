package task2;

import java.io.File;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;

public class Orders2 {
    public static void main(String... args) {
        // ensure the directories task2/output and task2/errors exists
        new File("task2/output").mkdirs();
        new File("task2/errors").mkdirs();

        // Compile X-definition to XDPool
        XDPool xpool = XDFactory.compileXD(null, "src/task2/Orders2.xdef");

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Orders");

        // Set external variables
        xdoc.setVariable("outFile", "task2/output/Orders.xml");
        xdoc.setVariable("errFile", "task2/errors/Orders_err.xml");

        // Prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();

        // Run validation mode (you can also try task2/input/Order_err.xml)
        xdoc.xparse("task2/input/Orders.xml", reporter);

        // Throw an exception if unexpected errors detected
        reporter.checkAndThrowErrors();

        // Check reported errors
        if (xdoc.getVariable("errCount").intValue() != 0) {
            System.err.println("Task2.Orders2 Incorrect input data");
        } else {
            System.out.println("OK, Task2.Order2");
        }
    }
}