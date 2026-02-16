package task6;

import java.io.File;
import java.io.IOException;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.getClassSource;

/** Example of Xcomponent and %bind command and %with
 * @author trojan
 */
public class BindWith extends STester {
	Integer vin;
	// setVIN is called from the nenerated XComponen TestWith
	public void setVIN(Integer x) { vin = x; }
	public Integer getVIN() { return vin; }
	
	@Override
	public void test() {
		try {
			XDPool xp = XDFactory.compileXD(null, "src/task6/BindWith.xdef");
			XDDocument xd = xp.createXDDocument("");
			File xml = new File("task6/input/Truck1.xml");
			ArrayReporter reporter = new ArrayReporter();

			Element el = xd.xparse(xml, reporter);
			assertEq("1234", el.getAttribute("MaxWeight"));
			assertNoErrorsAndClear(reporter);
			// compile sources with XComponents
			// parse datata with the XComponent
			XComponent xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("1234", xc.toXml().getAttribute("MaxWeight"));
			assertEq(1234, XComponentUtil.get(xc, "VIN")); // try getter created from %bind command.
			XComponentUtil.set(xc, "VIN", 456789); // try setter created from %bind command.
			assertEq(456789, XComponentUtil.get(xc, "VIN")); // try getter created from %bind command.
			assertEq("<Truck MaxWeight = \"456789\" />", xc.toXml());
			XComponentUtil.set(xc, "VIN", null); // try setter created from %bind command.
			assertEq("<Truck />", xc.toXml());
			assertEq(null, XComponentUtil.get(xc, "VIN")); // try getter created from %bind command.
		} catch (RuntimeException ex) {fail(ex);}
	}
	
	/** @param args parameter not used */
	public static void main(String[] args) { runTest(args); }
	
}
