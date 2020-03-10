package task2;

import org.xdef.sys.FileReportWriter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Orders1 {
	public static void main(String[] args) throws Exception {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task2/Orders1.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");

		// The file where to rite result
		File vystup = new File("task2/output/Orders.xml");
		OutputStream out = new FileOutputStream(vystup);
		xdoc.setStreamWriter(out, "UTF-8", true);

		// The file with errors
		File chyby = new File("task2/errors/Orders_err.txt");

		// Prepasre the error reporter (write error directly to the file)
		FileReportWriter reporter = new FileReportWriter(chyby);

		// Run validation mode (you can also try task2/input/Order_err.xml)
		xdoc.xparse("task2/input/Orders.xml", reporter);

		// close the output stream.
		out.close();
		reporter.close();

		// Check if errors reported
		if (reporter.errorWarnings()) {
			System.err.println("Incorrect input data");
		} else {
			System.out.println("OK");
		}
	}
}