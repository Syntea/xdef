package task5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import org.xdef.xon.XonUtils;

public class CsvExample {
    public static void main(String... args) throws IOException {
        // compile the XDPool object from the X-definition source
        Properties props = new Properties();
        XDPool xpool = XDFactory.compileXD(props, "src/task5/csvExample.xdef");
        // Create an instance of the XDDocument object (from XDPool)
        XDDocument xdoc = xpool.createXDDocument("csvExample");
        // prepare the error reporter
        ArrayReporter reporter = new ArrayReporter();
        xdoc.setProperties(props);
        List<Object> csv;
        try (Reader in = new FileReader("task5/input/csvExample.csv")) {
            csv = xdoc.cparse(in, null, reporter);
        }
        // check errors
        if (reporter.errorWarnings()) {
            // write log file with errors
            new File("task5/errors").mkdirs();
            try (PrintStream ps = new PrintStream("task5/errors/csv.txt")) {
                reporter.printReports(ps); //print errors
            }
            System.err.println("Task5.CsvExample, input data error; see task5/errors/csv.txt");
        } else {
            System.out.println("OK, Task5.CsvExample, see task5/output/result.csv");
            // Store the parsed result
            new File("task5/output").mkdirs();
            Writer out =
                new OutputStreamWriter(new FileOutputStream("task5/output/result.csv"), "ISO8859-2");
            out.write(XonUtils.toCsvString(csv));
            out.close();
        }
    }
}