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

public class YamlExample {
	public static void main(String... args) throws Exception {
		// compile XDPool from the X-definition
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task5/yamlExample.xdef");
		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("yamlExample");
		// prepare error reporter
		ArrayReporter reporter = new ArrayReporter();
		xdoc.setProperties(props);
		Object xon = xdoc.yparse("task5/input/yamlExample.yaml", reporter);
		// check errors
		if (reporter.errorWarnings()) {
			// write log file with errors
			new File("task5/errors").mkdirs();
			PrintStream ps = new PrintStream("task5/errors/yaml.txt");
			reporter.printReports(ps); //print errors
			ps.close(); 
			System.err.println("Input data error; see task5/errors/yaml.txt");
		} else {
			System.out.println("OK. See task5/output/result.yaml");
			// Store parsed result
			new File("task5/output").mkdirs();
			Writer out = new OutputStreamWriter(
				new FileOutputStream("task5/output/result.yaml"), "UTF-8");
			out.write(XonUtils.toYamlString(xon));
			out.close();
		}
	}
}