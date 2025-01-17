import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.sys.ArrayReporter;

public class X {
	public static void main(String[] args) {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		try {
			XDDocument xd = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <xd:declaration>\n" +
"    void x(String s) {\n" +
"      try {\n" +
"        Currency c = new Currency(s);\n" +
"        outln(c.currencyCode());\n" +
"      } catch (Exception e) {\n" +
"        outln('Exception: ' + e);\n" +
"      }\n" +
"    }\n" +
"  </xd:declaration>\n" +
"  <A>\n" +
"    <B xd:script='*; finally x((String) @a);' a='currency();'/>\n" +
"  </A>\n" +
"</xd:def>").createXDDocument();
			StringWriter swr = new StringWriter();
			xd.setStdOut(swr);
			ArrayReporter reporter = new ArrayReporter();
			xd.xparse("<A>\n  <B a='USD'/>\n  <B a='CZKx'/>\n</A>", reporter);
			System.out.println((!"USD\nCZK\n".equals(swr.toString()) || reporter.errorWarnings())
				? "Error: " + reporter + ";\n" + swr : "OK");
		} catch (RuntimeException ex) {ex.printStackTrace(System.out);}
	}
}