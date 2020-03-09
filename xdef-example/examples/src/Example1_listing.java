import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportPrinter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;

/** Validate XML with X-definition, errors are printed as listing. */
public class Example1_listing {

	public static void main(String[] args) throws Exception {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example1.xdef";
		String xmlData = "./src/Example1_errors.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xpool.createXDDocument();

		// 3. Validate and process XML data.
		//reports will be recorded to array reporter.
		ArrayReporter reporter = new ArrayReporter();
		xdoc.xparse(xmlData, reporter); //validate and process data

		// 4. Print listing with errors
		if (xdoc.errors()) {
			ReportPrinter.printListing(System.out,
				xmlData,
				reporter.getReportReader(),
				true);
		} else { // no errors
			System.err.println("Errors not detected!");
		}
	}

}