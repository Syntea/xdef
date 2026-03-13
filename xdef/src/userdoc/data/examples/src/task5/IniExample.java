package task5;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.xon.XonUtils;

public class IniExample {

    public static void main(String... args) throws IOException {
        // ensure the directories task5/output and task5/errors are clear and exists
        FUtils.deleteAndCreateDir("task5/output");
        FUtils.deleteAndCreateDir("task5/errors");

        // compile the XDPool object from the X-definition source
        Properties props = new Properties();
        XDPool xpool = XDFactory.compileXD(props, "src/task5/iniExample.xdef");
        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("iniExample");
        // prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();
        xdoc.setProperties(props);
        Map<String, Object> ini =
            xdoc.iparse("task5/input/iniExample.ini", reporter);
        // check errors
        if (reporter.errorWarnings()) {
            System.err.println("Task5.IniExample input data error; see task5/errors/ini.txt");
            try (PrintStream ps = new PrintStream("task5/errors/ini.txt")) {
                reporter.printReports(ps); //print errors
            }
        } else {
            System.out.println("OK, Task5.IniExample, see task5/output/result.ini");
            // Store the parsed result
            try (Writer out = new OutputStreamWriter(
                new FileOutputStream("task5/output/result.ini"), "ISO8859-2")) {
                out.write(XonUtils.toIniString(ini));
            }
        }
    }
}