import java.io.StringWriter;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class X extends XDTester {
	public X() {}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("Xdefinition version: " + XDFactory.getXDVersion());
		XDDocument xd = XDFactory.compileXD(null,
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
		StringWriter swr = new StringWriter();
		xd.setStdOut(swr);
		ArrayReporter reporter = new ArrayReporter();
		xd.xparse("<A><B a='USD'/><B a='CZK'/></A>", reporter);
		System.out.println((!"USD\nCZK\n".equals(swr.toString()) || reporter.errorWarnings())
			? "Error: " + reporter + ";\n" + swr : "OK");

		System.out.println("========");
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_STRING_CODES, "Windows-1250,ISO8859-5");
		XDPool xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <A><B xd:script='*;' a='string();'/></A>\n" +
"</xd:def>");
		reporter.clear();
		xp.createXDDocument().xparse(new java.io.File(getSourceDir()+"x1.xml"),reporter);
		System.out.println((reporter.errors() ? reporter.toString() : "OK"));
		reporter.clear();
		xp.createXDDocument().xparse(new java.io.File(getSourceDir()+"x2.xml"),reporter);
		System.out.println((reporter.getErrorCount()!=1 ? reporter.toString() : "OK"));
		reporter.clear();
		xp.createXDDocument().xparse(new java.io.File(getSourceDir()+"x3.xml"),reporter);
		System.out.println((reporter.getErrorCount()!=2 ? reporter.toString() : "OK"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
