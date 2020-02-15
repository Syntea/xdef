package mytest.xdef;

import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

public class JSONTest1 {

	public JSONTest1() {super();}
	
	/** Run JSON test. */
	static void test(String xdef, String xml) {
        try {
			XDPool xp;
			XDDocument xd;
			Element el;
			Properties props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
			props.setProperty(XDConstants.XDPROPERTY_DISPLAY,
				XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);
//				XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE);
			props.setProperty(XDConstants.XDPROPERTY_DEBUG,
				XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
//				XDConstants.XDPROPERTYVALUE_DEBUG_TRUE);
			xp = XDFactory.compileXD(props, xdef);
			xd = xp.createXDDocument();
			ArrayReporter reporter = new ArrayReporter();
			el = xd.xparse(xml, reporter);
			// check errors
			if (reporter.errorWarnings()) {
				System.err.println("ERRORS in "+ xml +" (xdef: "+ xdef +"):\n");
				System.err.println(reporter.printToString());
			} else {
				System.out.println("OK "+ xml +" (xdef: "+ xdef +")");
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	public static void main(String[] args) {
		test("src/test/java/mytest/JSONTest1_base.xdef", 
			"src/test/java/mytest/JSONTest1_data1.xml");
		test("src/test/java/mytest/JSONTest1_data1.xdef.xml", 
			"src/test/java/mytest/JSONTest1_data1.xml");
		test("src/test/java/mytest/JSONTest1_base.xdef", 
			"src/test/java/mytest/JSONTest1_data2.xml");
		test("src/test/java/mytest/JSONTest1_data2.xdef.xml", 
			"src/test/java/mytest/JSONTest1_data2.xml");

		test("src/test/java/mytest/JSONTest1_base.xdef", 
			"src/test/java/mytest/XML.txt");
		test("src/test/java/mytest/Xdef.txt", "src/test/java/mytest/XML.txt");

	}
}