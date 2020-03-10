package task1;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import org.w3c.dom.Element;

public class Order1a {
	public static void main(String[] args) throws Exception {
		// Get XDPool object from the file
		ObjectInputStream in = new ObjectInputStream(
			new FileInputStream("src/task1/Order1a.xp"));
		XDPool xpool = (XDPool) in.readObject();
		in.close();

		// Create instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Order");

		// Create reporter
		ArrayReporter reporter = new ArrayReporter();

		// Run validation mode (you can also try task1/input/Order_err.xml)
		Element result = xdoc.xparse("task1/input/Order.xml", reporter);

		// Check if an error was reported
		if (reporter.errorWarnings()) {
			// Print errors to the file
			PrintStream ps = new PrintStream("task1/errors/Order_err.txt");
			reporter.printReports(ps);
			ps.close();
			System.err.println("Incorrect input data");
		} else {
			// No errors, write the processed document to the file
			KXmlUtils.writeXml("task1/output/Order.xml", result);
			System.out.println("OK");
		}
	}
}