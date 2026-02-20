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

        String s = xc.toXml().getAttribute("MaxWeight");
        if (!"1234".equals(s)) {
            System.err.println("ERROR MaxWeight: " + s);
            return;
        }
        if (1234 != ((Integer) XComponentUtil.get(xc, "VIN"))) {
            System.err.println("ERROR MaxWeight: " + s);
            return;
        }
        XComponentUtil.set(xc, "VIN", 456789); // try setter created from %bind command.
        if (456789 != ((Integer) XComponentUtil.get(xc, "VIN"))) {
            System.err.println("ERROR MaxWeight (set new value): " + s);
            return;
        }
        XComponentUtil.set(xc, "VIN", null); // try setter created from %bind command.
        if (XComponentUtil.get(xc, "VIN") != null) {
            System.err.println("Expected null!");
            return;
        }
        System.out.println("OK, Task6.BindWith");
    }

}