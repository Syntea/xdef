package task5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import org.xdef.xon.XonUtils;

public class PropsExample {
	public static void main(String... args) throws IOException {
		// compile the XDPool object from the X-definition source
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props,"src/task5/propsExample.xdef");
		// Create an instance of the XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("propsExample");
		// prepare the error reporter
		ArrayReporter reporter = new ArrayReporter();
		xdoc.setProperties(props);
		Map<String, Object> ini = xdoc.iparse("task5/input/propsExample.properties", reporter);
		// check errors
		if (reporter.errorWarnings()) {
			// write log file with errors
			new File("task5/errors").mkdirs();
			try (PrintStream ps = new PrintStream("task5/errors/properties.txt")) {
				reporter.printReports(ps); //print errors
			}
			System.err.println("Task5.PropsExample input data error; see task5/errors/props.txt");
		} else {
			System.out.println("OK, Task5.PropsExample, see task5/output/result.props");
			// Store the parsed result
			new File("task5/output").mkdirs();
			try (Writer out = new OutputStreamWriter(
				new FileOutputStream("task5/output/result.properties"), "ASCII")) {
				out.write(XonUtils.toIniString(ini));
			}
		}
	}
}