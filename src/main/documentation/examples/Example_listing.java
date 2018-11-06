import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.ReportPrinter;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;

public class Example_listing {	
	public static void main(String[] args) throws Exception {
		XDPool xp = XDFactory.compileXD(null, "Example.xdef");
		XDDocument xd = xp.createXDDocument();
		ArrayReporter reporter = new ArrayReporter();
		xd.xparse("Example.xml", reporter);
		if (reporter.errors()) {
			ReportPrinter.printListing(System.out,
				new java.io.FileReader("Example.xml"),
				reporter.getReportReader(),
				true);
		}
	}
		
}
