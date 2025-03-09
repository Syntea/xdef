package mytests;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.xml.KXmlUtils;
import java.util.Properties;
import static test.XDTester._xdNS;

public class RefElement {
	static String xdef = "" +
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def root=\"A\" name=\"def1\">\n" +
"   <A a=\"string(); default 'A'\">\n" +
"       <B xd:script=\"2;ref def2#B;\" />\n" +
"   </A>\n" +
"</xd:def>\n" +
"<xd:def name=\"def2\">\n" +
"   <B a=\"string(); default 'B'\" />\n" +
"</xd:def>\n" +
"</xd:collection>";

	public static void main(String[] args) {
		XDDocument xd;
		Element el;
		String xml;
		Properties props = System.getProperties();

		xd = XDFactory.compileXD(props, // no create section
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"   <A a=\"string()\">\n" +
"      <A xd:script=\"0..; ref A\"/>\n" +
"   </A>\n" +
"</xd:def>").createXDDocument();
		xml = "<A a=\"a1\"><A a=\"a2\"/><A a=\"a3\"/></A>";
		xd.setXDContext(xml);
		el = xd.xcreate("A", null);
		System.out.println(KXmlUtils.nodeToString(el, true));

		XDPool xdpool = XDFactory.compileXD(props, xdef);

		xd = xdpool.createXDDocument("def1");
		el = xd.xcreate("A", null);
		System.out.println(KXmlUtils.nodeToString(el, true));
	}
}