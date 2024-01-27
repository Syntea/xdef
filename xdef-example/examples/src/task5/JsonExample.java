package task5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Properties;
import org.xdef.xon.XonUtils;

public class JsonExample {
	public static void main(String... args) throws Exception {
		// compile the XDPool object from the X-definition source
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task5/jsonExample.xdef");
		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("jsonExample");
		// prepare then error reporter
		ArrayReporter reporter = new ArrayReporter();
		// run validation mode (you can also try task5/input/Order_err.xml)
		xdoc.setProperties(props);
		Object xon = xdoc.jparse("task5/input/jsonExample.json", reporter);
		// check errors
		if (reporter.errorWarnings()) {
			new File("task5/errors").mkdirs();
			// write log file with errors
			PrintStream ps = new PrintStream("task5/errors/json.txt");
			reporter.printReports(ps); //print errors
			ps.close(); 
			System.err.println("Input data error; see task5/errors/json.txt");
		} else {
			System.out.println("OK. See task5/output/result.json");
			new File("task5/output").mkdirs();
			// Store the parsed result
			Writer out = new OutputStreamWriter(
				new FileOutputStream("task5/output/result.json"), "UTF-8");
			out.write(XonUtils.toJsonString(xon, true));
			out.close();
		}
	}
}