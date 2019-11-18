package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;
import java.io.StringWriter;

/** Test of try/catch.
 * @author Vaclav Trojan
 *
 */
public final class TestTryCatch extends XDTester {

	public TestTryCatch() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		String s;

		try {
			xdef =
"<xd:def xd:name='test' root='a' xmlns:xd='" + _xdNS + "'>\n"+
"\n"+
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
"\n"+
"  <a att='required mytype'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a att='xx'/>";
			strw = new StringWriter();
			parse(xp, "test", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertTrue((s = strw.toString()).indexOf("E: Hi") >= 0, s);
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