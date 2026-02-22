package task6;

import java.io.File;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;

/** Example of XComponent and %bind command containing %with */
public class BindWith {

    /** @param args parameter not used */
    public static void main(String[] args) {
        XDPool xp = XDFactory.compileXD(null, "src/task6/BindWith.xdef");
        XDDocument xd = xp.createXDDocument("");
        ArrayReporter reporter = new ArrayReporter();

        // parse data with the XComponent
        XComponent xc = xd.xparseXComponent(new File("task6/input/Truck1.xml"), null, reporter);
        if (reporter.errorWarnings()) {
            System.err.println("ERROR xparseXComponent:\n" +reporter);
            return;
        }

        String s = xc.toXml().getAttribute("MaxWeight");  // value from XML data
        if (!"1234".equals(s)) {
            System.err.println("ERROR MaxWeight: " + s);
            return;
        }
        
        if (1234 != ((Integer) XComponentUtil.get(xc, "Mass"))) { // value from Xcomponenet from getter
            System.err.println("ERROR Mass: " + s);
            return;
        }

        XComponentUtil.set(xc, "Mass", 456789); // use setter to set value
        if (456789 != ((Integer) XComponentUtil.get(xc, "Mass"))) {
            System.err.println("ERROR MaxWeight (set new value): " + s);
            return;
        }

        XComponentUtil.set(xc, "Mass", null); // set null.
        if (XComponentUtil.get(xc, "Mass") != null) {
            System.err.println("Expected null!");
            return;
        }        
        if (xc.toXml().getAttributeNode("MaxWeight") != null) {
            System.err.println("ERROR MaxWeight sould be null");
        }
        
        System.out.println("OK, Task6.BindWith");
    }

}