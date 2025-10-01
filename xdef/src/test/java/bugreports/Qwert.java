package bugreports;

import java.io.StringWriter;
import java.util.List;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 *
 * @author Trojan
 */
public class Qwert extends XDTester {

	/** Run test and display error information. */
	@Override
	public void test() {
//		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		String xml, xdef;
		XDPool xp;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='test'>\n" +
"   <xd:json name = \"test\">\n" +
"[\n" +
"   { \"adresa\": \"%script= \\\"ref adr;\\\"\"  }\n" +
"]\n" +
"   </xd:json>" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
		} catch (RuntimeException ex) {fail(ex);}
	}
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}

}
