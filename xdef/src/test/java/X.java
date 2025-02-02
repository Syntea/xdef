import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;

public class X {
	public static void main(String[] args) {
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
	}
}