import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;

/** Validate XML with X-definition - script commands; input data with errors. */
public class Example2_errors {

    public static void main(String[] args) throws Exception {
        // Prepare path to X-definition and XML data.
        String xdef = "./src/Example2.xdef";
        String xmlData = "./src/Example2_errors.xml";

        // 1. Create XDPool.
        Properties props = System.getProperties();
        XDPool xpool = XDFactory.compileXD(props, xdef);

        // 2. Create XDDocument.
        XDDocument xdoc = xpool.createXDDocument();

        // 3. Validate and process XML data.
        //validate and process data. Errors are recorded to reporter.
        Element el = xdoc.xparse(xmlData, new ArrayReporter());

        // 4. test if an error was reported
        if (xdoc.errorWarnings()) {
            System.out.println("Detected errors:");
            // print errors
            xdoc.printReports(System.out);
            System.out.println("Example2_errors OK; errors printed");
        } else { // no errors => error
            System.err.println("Error not detected in Example2_errors");
        }
    }

}