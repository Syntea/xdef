package task6;

import java.io.File;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import static task5.Network.getItemCounter;

/** Example of Xcomponent and %bind command and %with
 * @author trojan
 */
public class BindWith {
	Integer vin;
	// setVIN is called from the nenerated XComponen TestWith
	public void setVIN(Integer x) { vin = x; }
	public Integer getVIN() { return vin; }
	
	private static boolean test() {
		XDPool xp = XDFactory.compileXD(null, "src/task6/BindWith.xdef");
		XDDocument xd = xp.createXDDocument("");
		File xml = new File("task6/input/Truck1.xml");
		ArrayReporter reporter = new ArrayReporter();

		Element el = xd.xparse(xml, reporter);
		String s = el.getAttribute("MaxWeight");
		if (!"1234".equals(s)) {
			System.err.println("ERROR MaxWeight: " + s);
		}
		if (reporter.errorWarnings()) {
			System.err.println(reporter);
			return false;
		}
		// compile sources with XComponents
		// parse datata with the XComponent
		XComponent xc = xd.xparseXComponent(xml, null, reporter);
		if (reporter.errorWarnings()) {
			System.err.println("ERROR xparseXComponent:\n" +reporter);
			return false;
		}
		s = xc.toXml().getAttribute("MaxWeight");
		if (!"1234".equals(s)) {
			System.err.println("ERROR MaxWeight: " + s);
			return false;
		}
		if (1234 != ((Integer) XComponentUtil.get(xc, "VIN"))) {
			System.err.println("ERROR MaxWeight: " + s);
			return false;
		}
		XComponentUtil.set(xc, "VIN", 456789); // try setter created from %bind command.
		if (456789 != ((Integer) XComponentUtil.get(xc, "VIN"))) {
			System.err.println("ERROR MaxWeight (set new value): " + s);
			return false;
		}
		XComponentUtil.set(xc, "VIN", null); // try setter created from %bind command.
		if (XComponentUtil.get(xc, "VIN") != null) {
			System.err.println("Expected null!");
			return false;
		}
		return true;
	}
	
	/** @param args parameter not used */
	public static void main(String[] args) {
		if (test()) {
			System.out.println("OK, Task6.BindWith");			
		} else {
			System.err.println("ERROR Task6.BindWith");			
		}
	}
	
}
