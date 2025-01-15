import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

public class X {
	public static void main(String[] args) {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		String xdef, xml;
		XDDocument xd;
		XDPool xp;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"<xd:declaration>\n" +
"  void x(String s) {\n" +
"    try {\n" +
"      Currency c = new Currency(s);\n" +
"      outln(c.currencyCode());\n" +
"    } catch (Exception e) {\n" +
"      outln('Exception: ' + e);\n" +
"    }\n" +
"  }\n" +
"</xd:declaration>\n"+
"  <A xd:script='finally x((String) @a);' a='currency();' />\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(swr);
			xml = "<A a='CZK'/>";
			xd.xparse(xml, reporter);
			System.out.println((!"CZK\n".equals(swr.toString()) || reporter.errorWarnings())
				? "Error: " + reporter + ";\n" + swr : "OK");
		} catch (RuntimeException ex) {ex.printStackTrace(System.out);}
	}
}