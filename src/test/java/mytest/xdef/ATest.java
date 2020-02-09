package mytest.xdef;

import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.xml.KXmlUtils;

/**
 * @author Vaclav Trojan
 */
public class ATest {

	public static void main(String... args) throws Exception {
		String xdef, xml;
		XDPool xp;
		XDDocument xd;
		Properties props = new Properties();
		Element el;
		xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' root='A'>\n" +
"  <A xd:script='ref a;'/>\n"+
"  <a xd:script='var uniqueSet v {x: int (1,3); y: int()}' a='string'>\n"+
"    <b xd:script='*; finally v.ID' x='v.x' y='v.y'/>\n"+
"    <c xd:script='?;' x='v.x'> v.y.IDREFS </c>\n"+
"    <d xd:script='?;' a='u.a (); onTrue u.x = null; finally u.ID' />\n" +
"  </a>\n"+
"  <xd:declaration>\n"+
"    uniqueSet u{a: string(); var Parser x}\n" +
"    external method void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		xp = XDFactory.compileXD(props, xdef);
		xd = xp.createXDDocument();
		xml = "<A a='x'/>";
		el = xd.xparse(xml, null);
		System.out.println(KXmlUtils.compareElements(xml, el));
	}
}