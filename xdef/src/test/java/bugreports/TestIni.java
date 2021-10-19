package bugreports;

import java.util.Map;
import java.util.Map.Entry;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import org.xdef.xon.XonUtil;
import test.XDTester;

/** Tests used for development..
 * @author Vaclav Trojan
 */
public class TestIni extends XDTester {

	public TestIni() {super();}

	private static Element iniToXml(Map<String, Object> ini) {
		Document doc = KXmlUtils.newDocument(XDConstants.JSON_NS_URI_W3C,
			"js:"+XonNames.X_MAP, null);
		Element el = doc.getDocumentElement();
		iniToXml(ini, el);
		return el;
	}

	private static void iniToXml(Map<String, Object> ini, Element el) {
		for (Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (!(o instanceof Map)) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.JSON_NS_URI_W3C, "js:" + XonNames.X_ITEM);
				item.setAttribute(XonNames.X_KEYATTR, name);
				item.setAttribute(XonNames.X_VALUEATTR, o.toString());
				el.appendChild(item);
			}
		}
		for (Entry<String, Object> x: ini.entrySet()) {
			String name = x.getKey();
			Object o = x.getValue();
			if (o instanceof Map) {
				Element item = el.getOwnerDocument().createElementNS(
					XDConstants.JSON_NS_URI_W3C, "js:" + XonNames.X_MAP);
				item.setAttribute(XonNames.X_KEYATTR, name);
				iniToXml((Map<String, Object>) o, item);
				el.appendChild(item);
			}
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String ini;
		Object o;
		Map<String, Object> map;
		String xdef;
		Element el;
		Properties props = new Properties();
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:ini name='a'>\n"+
"   A=?string()\n" +
"   B=int()\n" +
"   C=date()\n" +
"   D=decimal()\n" +
"   [E]\n" +
"     x = ?int()\n" +
//"   }\n"+
" </xd:ini>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			ini = "A=a\n B = 1\n C=2121-10-19\n D=2.121\n[E]\nx=123";
			map = XonUtil.parseINI(ini);
			el = iniToXml(map);
			System.out.println(KXmlUtils.nodeToString(el, true));
			xd.xparse(el, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}