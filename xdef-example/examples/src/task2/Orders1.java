package task2;

import org.xdef.sys.FileReportWriter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Orders1 {
	public static void main(String... args) throws Exception {
		// Compile the X-definition source to the XDPool object
		XDPool xpool = XDFactory.compileXD(null, "src/task2/Orders1.xdef");

		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");
		
		// The file where to write the result
		OutputStream out = new FileOutputStream("task2/output/Orders.xml");
		xdoc.setStreamWriter(out, "UTF-8", true);

		// The file with errors
		File errors = new File("task2/errors/Orders_err.txt");

		// Prepare the error reporter
		FileReportWriter reporter = new FileReportWriter(errors);
		
		// Run validation mode (you can also try task2/input/Order_err.xml)
		xdoc.xparse("task2/input/Orders.xml", reporter);
		
		// close the output stream.
		out.close();
		reporter.close();
		
		// Check if an error was reported
		if (reporter.errorWarnings()) {
			System.err.println("Incorrect input data");
		} else {
			System.out.println("OK");
		}
	}
}