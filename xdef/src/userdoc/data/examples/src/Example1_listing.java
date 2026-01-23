import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportPrinter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;

/** Validate XML with X-definition, errors are printed as a listing. */
public class Example1_listing {

	public static void main(String[] args) throws Exception {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example1.xdef";
		String xmlData = "./src/Example1_errors.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		// if you want to get correct positions of values of attributes
		// set following property (see XDConstants.XDPROPERTY_LOCATIONDETAILS):
		props.setProperty("xdef_locationsdetails", "true");
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xpool.createXDDocument();

		// 3. Validate and process XML data.
		//reports will be recorded to array reporter.
		ArrayReporter reporter = new ArrayReporter();
		xdoc.xparse(xmlData, reporter); //validate and process data

		// 4. Print listing with errors
		if (xdoc.errors()) {
			System.out.println("Errors printed:");
			ReportPrinter.printListing(System.out,
				xmlData,
				reporter.getReportReader(),
				true);
			System.out.println("OK, Example1_listing with errors printed.");
		} else { // no errors
			System.err.println("Errors not detected in Example1_listing");
		}
	}

}