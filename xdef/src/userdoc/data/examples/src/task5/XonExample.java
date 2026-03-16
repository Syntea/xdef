package task5;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Properties;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xon.XonUtils;

public class XonExample {
    public static void main(String... args) throws IOException {
        // ensure the directories task5/output and task5/errors are clear and exists
        FUtils.deleteAndCreateDir("task5/output");
        FUtils.deleteAndCreateDir("task5/errors");

        // compile the XDPool object from the X-definition source
        Properties props = new Properties();
        XDPool xpool = XDFactory.compileXD(props, "src/task5/xonExample.xdef");
        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("xonExample");
        // prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();
        xdoc.setProperties(props);
        Object xon = xdoc.jparse("task5/input/xonExample.xon", reporter);
        // check errors
        if (reporter.errorWarnings()) {
            // write log file with errors
            PrintStream ps = new PrintStream("task5/errors/xon.txt");
            reporter.printReports(ps); //print errors
            ps.close();
            System.err.println("Task5.XonExample input data error; see task5/errors/xon.txt");
        } else {
            System.out.println("OK, Task5.XonExample, see task5/output/result.xon");
            // Store the parsed result
            Writer out = new OutputStreamWriter(new FileOutputStream("task5/output/result.xon"), "UTF-8");
            out.write(XonUtils.toXonString(xon, true));
            out.close();
        }
    }
}