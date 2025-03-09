package mytests.xon;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** XON test
 * @author Vaclav Trojan
 */
public class TestXXON1 extends XDTester {
	public TestXXON1() {super();}

	static boolean eq(final Object x, final Object y) {
		if (x == null) {
			return y == null;
		} else if (x instanceof Text) {
			return y instanceof Text
				&& ((Text) x).getNodeValue().equals(((Text) y).getNodeValue());
		} else if (x instanceof Attr) {
			return y instanceof Attr
				&& ((Attr) x).getNodeValue().equals(((Attr) y).getNodeValue())
				&& ((Attr) x).getNodeName().equals(((Attr) y).getNodeName());
		} else if (x instanceof Element && y instanceof Element) {
			return !KXmlUtils.compareElements(
				(Element) x, (Element) y, true).errors();
		} else if (x instanceof NodeList && y instanceof NodeList) {
			NodeList nl1 = (NodeList) x; NodeList nl2 = (NodeList) y;
			if (nl1.getLength() != nl2.getLength()) {
				return false;
			}
			for (int i = 0; i < nl1.getLength(); i++) {
				if (!eq(nl1.item(i), nl2.item(i))) {
					return false;
				}
			}
			return true;
		} else if (x instanceof NamedNodeMap && y instanceof NamedNodeMap) {
			NamedNodeMap nl1=(NamedNodeMap)x; NamedNodeMap nl2=(NamedNodeMap)y;
			if (nl1.getLength() != nl2.getLength()) {
				return false;
			}
			for (int i = 0; i < nl1.getLength(); i++) {
				if (!eq(nl1.item(i), nl2.item(i))) {
					return false;
				}
			}
			return true;
		} else {
			return x.equals(y);
		}
	}

	private void testXon(String xdef, String xdName, String json) {
		XDPool xp;
		XDDocument xd;
		ArrayReporter reporter = new ArrayReporter();
		Object o1, o2;
		try {
			xp = compile(xdef);
			xd = xp.createXDDocument(xdName);
			xd.jparse(json, reporter);
			o1 = xd.getXon();
			assertNoErrors(reporter);
			if (!XonUtils.xonEqual(o1, o2 = XonUtils.parseXON(json))) {
				fail(o1 + "\n"
					+ KXmlUtils.nodeToString(XonUtils.xonToXml(o1), true)+ "\n"
					+ o2 + "\n"
					+ KXmlUtils.nodeToString(XonUtils.xonToXml(o2),true));
			}
		} catch (Exception ex) {fail(ex);}
	}
	@Override
	/** Run test and display error information. */
	public void test() {
		String s =
			"{\"a\":[1,null,-1.23E+4,[true,5],\"\\\\a\\\"\"],\"b\":null}";
		System.out.println(XonUtils.parseJSON(s));
/**/
		testXon(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"  [ \"int\", \"boolean\", \"string\", \"jnull\", [[],{}], {} ]\n" +
"</xd:json>\n" +
"</xd:def>",
			"",
			"[123, false, \"ab cd\", null, [[],{}], {}]");
/**/
		testXon(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"  { a=\"int\" }\n" +
"</xd:json>\n" +
"</xd:def>",
			"",
			"{a=123}");
/**/
		testXon(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"  { a = [\"int\"],\n" +
"    b = \"boolean\",\n" +
"    c = {},\n" +
"    d = \"int\",\n" +
"    e = \"jnull\"\n" +
"  }\n" +
"</xd:json>\n" +
"</xd:def>",
			"",
			"{a=[123] b= true c = {} d=1 e=null}");
/**/
		testXon(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' root='a'>\n"+
"<xd:json name=\"a\">\n" +
"  { a = [\"int\"],\n" +
"    b = \"boolean\",\n" +
"    c = {},\n" +
"    d = \"int\",\n" +
"    e = \"jnull\",\n" +
"    Towns = [\n" +
"      \"* gps()\"\n" +
"    ]\n" +
"  }\n" +
"</xd:json>\n" +
"</xd:def>",
			"",
"{a=[123] b= true c = {} d=1 e=null,\n" +
"    Towns = [ # array with GPS locations of towns\n" +
"      g(48.2, 16.37, 151, Wien),     # GPS\n" +
"      g(51.52, -0.09, 0, London),    # GPS\n" +
"      g(50.08, 14.42, 399, \"Praha (centrum)\") # GPS\n" +
"    ],\n" +
"  }");
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}