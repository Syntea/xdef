import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class X extends XDTester {
	public X() {}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("Xdefinition version: " + XDFactory.getXDVersion());
		XDDocument xd;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();

		xd = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <xd:declaration>\n" +
"    void x(String s) {\n" +
"      try {\n" +
"        outln(new Currency(s).currencyCode());\n" +
"      } catch (Exception e) {\n" +
"        outln('Exception: ' + e);\n" +
"      }\n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <A><B xd:script='*; finally x((String) @a);' a='currency();'/></A>\n" +
"</xd:def>").createXDDocument();
		xd.setStdOut(swr = new StringWriter());
		xd.xparse("<A><B a='USD'/><B a='CZK'/></A>", reporter);
		System.out.println((!"USD\nCZK\n".equals(swr.toString()) || reporter.errorWarnings())
			? "Error: " + reporter + ";\n" + swr : "OK");
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
