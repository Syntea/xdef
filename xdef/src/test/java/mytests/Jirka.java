package mytests;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.code.DefContainer;
import org.xdef.sys.SDatetime;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Tests.
 * @author Vaclav Trojan
 */
public class Jirka extends XDTester {
	public Jirka() {super();}

	/** Get SDatetime from the string according to mask.
	 * @param mask datetime mask.
	 * @param s string data.
	 * @return parsed SDatetime object.
	 */
	private static SDatetime readTime(String mask, String s) {
		StringParser p = new StringParser(s);
		return p.isDatetime(mask) ? p.getParsedSDatetime() : new SDatetime();
	}

	/** Test if the element f can be joined with the element e.
	 * @param e first element.
	 * @param f second element.
	 * @param a name of attribute with datetime "start"
	 * @param b name of attribute with datetime "end"
	 * @param mask description of datetime format.
	 * @return true if and only if f can be joined with e.
	 */
	private static boolean join(Element e, Element f, String a, String b, String mask) {
		NamedNodeMap eatrs = e.getAttributes();
		NamedNodeMap fatrs = f.getAttributes();
		if (!e.hasAttribute(a) || !f.hasAttribute(a)
			|| !e.hasAttribute(b) || !f.hasAttribute(b)
			|| eatrs.getLength() != fatrs.getLength()) {
			return false;
		}
		SDatetime t1 = readTime(mask, e.getAttribute(b)); // e.do
		if (mask.endsWith("d")) {
			t1.addDay(1);
		} else if (mask.endsWith("H")) {
			t1.addHour(1);
		} else if (mask.endsWith("s")) {
			t1.addSecond(1);
		} else {
			return false;
		}
		if (!t1.equals(readTime(mask, f.getAttribute(a)))) { // f.od
			return false;
		}
		for (int i = 0; i < eatrs.getLength(); i++) {
			String name = eatrs.item(i).getNodeName();
			if (!a.equals(name) && !b.equals(name)
				 && !e.getAttribute(name).equals(f.getAttribute(name))) {
				return false;
			}
		}
		return true;
	}

	/** Create list of joined elements.
	 * @param x container with elements.
	 * @param a name of attribute with datetime "start"
	 * @param b name of attribute with datetime "end"
	 * @param mask description of datetime format.
	 * @return Container with joined elements.
	 */
	public static XDContainer x(XDContainer x, String a, String b, String mask) {
		XDContainer y = new DefContainer();
		if (!x.isEmpty()) {
			Element e = x.getXDElement(0);
			y.addXDItem(e);
			for (int i = 1; i < x.getXDItemsNumber(); i++) {
				Element f = x.getXDElement(i);
				if (join(e, f, a, b, mask)) {
					e.setAttribute(b, f.getAttribute(b));
				} else {
					y.addXDItem(e = f);
				}
			}
		}
		return y;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef, xml, expected;
		XDDocument xd;
		XDPool xp;
		xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"   external method XDContainer mytests.Jirka.x(XDContainer x, String a, String b, String mask);\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <B xd:script=\"occurs *; create x(from('//B'), 'x', 'y', 'yyyy-MM-dd');\"\n"+
"  a='string' b='string'\n"+
"     x=\"xdatetime('yyyy-MM-dd')\" y=\"xdatetime('yyyy-MM-dd')\"/>\n"+
"</A>\n"+
"</xd:def>";
		xp = XDFactory.compileXD(null, xdef);
		xd = xp.createXDDocument();
		expected = xml = "<A/>";
		xd.setXDContext(xml);
		assertEq(xd.xcreate("A", null), expected);
		xd = xp.createXDDocument();
		expected = xml = "<A><B a='a' b='b' x='2024-09-08' y='2024-09-09'/></A>";
		xd.setXDContext(xml);
		assertEq(xd.xcreate("A", null), expected);
		xml =
"<A>\n"+
"  <B a='a' b='b' x='2023-12-08' y='2023-12-31'/>\n"+
"  <B a='a' b='b' x='2024-01-01' y='2024-09-11'/>\n"+
"  <B a='a' b='b' x='2024-09-12' y='2024-09-13'/>\n"+
"  <B a='a' b='b' x='2024-09-20' y='2024-09-30'/>\n"+
"  <B a='a' b='b' x='2024-10-01' y='2024-10-02'/>\n"+
"</A>";
		xd = xp.createXDDocument();
		xd.setXDContext(xml);
		expected =
"<A>\n" +
"  <B a=\"a\" b=\"b\" x=\"2023-12-08\" y=\"2024-09-13\"/>\n" +
"  <B a=\"a\" b=\"b\" x=\"2024-09-20\" y=\"2024-10-02\"/>\n" +
"</A>";
		xml = KXmlUtils.nodeToString(xd.xcreate("A", null), true);
		KXmlUtils.compareElements(xml,expected, true).checkAndThrowErrorWarnings();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}