package construction;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

/** Create new XML document with XDefinition from source XML data; element with
 * source data is parsed by command in the XDefinition.
 *
 * @author Vaclav Trojan
 */
public class Create_01 {

    /** Start test from command line. Print results on system output.
     * @param args No arguments or files with XDefinition and XML data.
     */
    public static void main(String[] args) {

        // Prepare source path to XDefinition and XML data.
        String xdef = args.length == 0 ? "src/construction/Create_01.xdef" : args[0];
        String xmlData = args.length < 2 ? "src/construction/Create_01.xml" : args[1];
        // 1. Create XDPool and XDDocument
        XDPool xpool = XDFactory.compileXD(System.getProperties(), xdef);
        XDDocument xdoc = xpool.createXDDocument();

        // 2. set the name of the input file to the variable "source"
        xdoc.setVariable("zdroj", xmlData);

        // 3. create result
        ArrayReporter reporter = new ArrayReporter();
        Element element = xdoc.xcreate("Adresar", reporter);

        // 4. print results
        if (xdoc.errors()) {
            reporter.printReports(System.err);
        } else {
            System.out.println(KXmlUtils.nodeToString(element, true));
        }

    }
}