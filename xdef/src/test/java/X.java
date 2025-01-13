import java.io.StringWriter;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

public class X {
	public static void main(String[] args) {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		String s, xdef, xml;
		XDDocument xd;
		XDPool xp;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"<xd:declaration>\n" +
"  void x(String s) {\n" +
"    Currency c = new Currency(s);\n" +
"    outln(c.currencyCode());\n" +
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
			if (!"CZK\n".equals(s = swr.toString()) || reporter.errorWarnings()) {
				System.out.println("Error: " + s + reporter);
			} else {
				System.out.println("OK");
			}
		} catch (Exception ex) {ex.printStackTrace(System.out);}
	}
}
