package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;
import java.io.StringWriter;

/** Test of try/catch.
 * @author Vaclav Trojan
 *
 */
public final class TestTryCatch extends XDTester {

	public TestTryCatch() {super();}

	/** Run test and print error information. */
	@Override
	public void test() {
		XDPool xp;
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;
		String s;

		try {
			xdef =
"<xd:def xd:name='test' root='a' xmlns:xd='"+_xdNS+"'>\n"+
"  <xd:declaration>\n"+
"    boolean mytype() {\n"+
"        try {\n"+
"          if (false) throw new Exception('Hello');\n"+
"            try {\n"+
"              Exception ex = new Exception('Hi');\n"+
"              if (true) throw ex;\n"+
"              return true;\n"+
"           } catch (Exception ex) {\n"+
"             throw ex;\n"+
"           }\n"+
"        } catch (Exception ex) {\n"+
"          out(ex.getMessage());\n"+
"          return true;\n"+
"        }\n"+
"    }\n"+
"  </xd:declaration>\n"+
"  <a att='required mytype'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a att='xx'/>";
			swr = new StringWriter();
			parse(xp, "test", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertTrue((s = swr.toString()).contains("E: Hi"), s);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}

}