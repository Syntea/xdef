import java.io.StringWriter;
import java.util.Properties;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;

public class X {
	public static void main(String[] args) throws Exception {
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

		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_STRING_CODE, "Windows-1250");
		XDPool xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='" + XDConstants.XDEF42_NS_URI + "' root='A'>\n" +
"  <A><B xd:script='*;' a='string();'/></A>\n" +
"</xd:def>");
		reporter.clear();
		xp.createXDDocument().xparse("<A><B a='áé'/><B a='ÁÉ'/></A>",reporter);
		System.out.println((reporter.errors() ? reporter.toString() : "OK"));
		reporter.clear();
		xp.createXDDocument().xparse("<A><B a='والنشر6ت'/><B a='Таблица аски'/></A>",reporter);
		System.out.println((reporter.errors() ? reporter.toString() : "OK"));
	}
}
