package mytest;

import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import test.utils.XDTester;

public class JSONTest1 extends XDTester {

	public JSONTest1() {super();}
	
	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		Element el;
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		props.setProperty(XDConstants.XDPROPERTY_DISPLAY,
			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE);
		props.setProperty(XDConstants.XDPROPERTY_DEBUG,
			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE);
		xp = XDFactory.compileXD(props, "src/test/java/mytest/JSONTest1.xdef");
		xd = xp.createXDDocument();
		ArrayReporter reporter = new ArrayReporter();
		el = xd.xparse("src/test/java/mytest/JSONTest1.xml", reporter);
		if (reporter.errorWarnings()) {
			System.out.println("ERRORS:\n" + reporter.printToString());
		} else {
			//TODO el -> JSON
			System.out.println(el);
		}
		xd = xp.createXDDocument("xdjson");
		reporter.clear();
		el = xd.xparse("src/test/java/mytest/JSONTest1_1.xml", reporter);
		if (reporter.errorWarnings()) {
			System.out.println("ERRORS:\n" + reporter.printToString());
		} else {
			//TODO el -> JSON
			System.out.println(el);
		}
	}
	
	public static void main(String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}