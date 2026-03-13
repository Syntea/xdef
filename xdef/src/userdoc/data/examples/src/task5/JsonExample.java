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

public class JsonExample {

    public static void main(String... args) throws IOException {
        // ensure the directories task5/output and task5/errors are clear and exists
        FUtils.deleteAndCreateDir("task5/output");
        FUtils.deleteAndCreateDir("task5/errors");

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
            System.err.println("Task5.JsonExample input data error; see task5/errors/json.txt");
            try ( // write log file with errors
                PrintStream ps = new PrintStream("task5/errors/json.txt")) {
                reporter.printReports(ps); //print errors
            } //print errors
        } else {
            System.out.println("OK, Task5.JsonExample, see task5/output/result.json");
            try (Writer out = new OutputStreamWriter(new FileOutputStream("task5/output/result.json"), "UTF-8")) {
                out.write(XonUtils.toJsonString(xon, true)); // Store the parsed result
            }
        }
    }
}