package task1;

import java.io.IOException;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;

public class Order3 {

    public static void main(String... args) throws IOException {
        task1.Order3ext.class.getClass(); // just force to compile Order3ext
        // ensure the directories task1/output and task1/errors are clear and exists
        FUtils.deleteAndCreateDir("task1/output");
        FUtils.deleteAndCreateDir("task1/errors");

        // Create an instance of the XDDocument object (from XDPool)
        // (external method "err" is called from the X-script; see below)
        XDPool xpool = XDFactory.compileXD(null, "src/task1/Order3.xdef");

        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("Order");

        // Prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();

        // Prepare the XML element used to record errors
        Element errors = KXmlUtils.newDocument(null, "Errors", null).getDocumentElement();
        xdoc.setUserObject(errors);

        // Run validation mode (you can also try task1/input/Order_err.xml)
        xdoc.xparse("task1/input/Order.xml", reporter);

        // Check errors
        if (errors.getChildNodes().getLength() > 0) {
            // Write error information to the file
            KXmlUtils.writeXml("task1/errors/Order_err.xml", errors);
            System.err.println("Task1.Order3 Incorrect input data");
        } else {
            // No errors, write the processed document to the file
            KXmlUtils.writeXml("task1/output/Order.xml", xdoc.getElement());
            System.out.println("OK, Task1.Order3");
        }
    }
}