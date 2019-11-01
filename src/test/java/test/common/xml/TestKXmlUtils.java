package test.common.xml;

import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.ReportWriter;
import builtools.STester;

/** TestKXmlUtils
 * @author Vaclav Trojan
 */
public class TestKXmlUtils extends STester {

	public TestKXmlUtils() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		String s;
		String source;
		Document doc;
		Node node;
		Attr attr;
		Element el, root, child;
		ReportWriter cmpResult;
		try { //toXmlText
			s = KXmlUtils.toXmlText("<&'\"", '<', false);
			assertEq("&lt;&amp;'\"", s);
			s = KXmlUtils.toXmlText("]>", '<', false);
			assertEq("]>", s);
			s = KXmlUtils.toXmlText("abc]>",'<', false);
			assertEq("abc]>", s);
			s = KXmlUtils.toXmlText("]]>", '<', false);
			assertEq("]]&gt;", s);
			s = KXmlUtils.toXmlText("]]>x", '<', false);
			assertEq("]]&gt;x", s);
			s = KXmlUtils.toXmlText("abc]]>", '<', false);
			assertEq("abc]]&gt;", s);
			s = KXmlUtils.toXmlText("abc]]><", '<', false);
			assertEq("abc]]&gt;&lt;", s);
			s = KXmlUtils.toXmlText("<&'\"", '"', false);
			assertEq("&lt;&amp;'&quot;", s);
			s = KXmlUtils.toXmlText("<&'\"", '\'', false);
			assertEq("&lt;&amp;&apos;\"", s);
			s = KXmlUtils.toXmlText("abc]]><x", '"', false);
			assertEq("abc]]>&lt;x", s);
			s = KXmlUtils.toXmlText("abc]]><x&", '"', false);
			assertEq("abc]]>&lt;x&amp;", s);
			doc = KXmlUtils.parseXml("<a>]]&gt;</a>");
			s = doc.getDocumentElement().getChildNodes().item(0).getNodeValue();
			assertEq("]]>", s);
			s = KXmlUtils.nodeToString(doc);
			assertEq("<?xml version=\"1.0\"?>\n<a>]]&gt;</a>", s);
			try {
				KXmlUtils.parseXml("<a>]]></a>");
				fail("error not reported");
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("XML053") < 0 &&
					ex.getMessage().indexOf("XML403") < 0) {
					fail(ex);
				}
			}
			source = "<!-- xx --><root>&lt;&amp;&quot;&apos;&gt;</root>"
				+ "<!-- xx -->";
			doc = KXmlUtils.parseXml(source);
			s = KXmlUtils.nodeToString(doc,true,false,false);
			if (KXmlUtils.compareElements(doc.getDocumentElement(),
				KXmlUtils.parseXml(s).getDocumentElement()).errorWarnings()) {
				fail(s);
			}
		} catch (Exception ex) {fail(ex);}
		try { // namespace
			source = "<root atr=\"atr1\" xmlns=\"a\">\n"+
				"  <child xmlns:u=\"t\" childAtr1='atr1' u:childAtr2='atr3'\n"+
				"         v:childAtr2=\"atr2\" xmlns:v=\"t\">\n"+
				"    This is text1...\n"+
				"  </child>\n"+
				" This is text2...\n"+
				"</root>\n";
			try {
				//warning "XML025"
				root = KXmlUtils.parseXml(source).getDocumentElement();
				child = (Element) root.getElementsByTagName("child").item(0);
				assertTrue(child.hasAttribute("u:childAtr2"),
					"Missing u:childAtr2");
				assertTrue(child.hasAttribute("v:childAtr2"),
					"Missing v:childAtr2");
			} catch (Exception ex) {
				// duplicity of attributes u:childAtr2 and v:childAtr2
				if (ex.getMessage().indexOf("XML025") < 0 &&
					ex.getMessage().indexOf("XML404") < 0) {
					fail(ex);
				}
			}
			source = "<root atr=\"atr1\" xmlns=\"a\">\n" +
				"  <child xmlns:u=\"uu\" childAtr1='atr1' u:childAtr2='atr3'\n"+
				"         v:childAtr2=\"atr2\" xmlns:v=\"vv\">\n"+
				"    This is text1...\n"+
				"  </child>\n"+
				" This is text2...\n"+
				"</root>\n";
			root = KXmlUtils.parseXml(source).getDocumentElement();
			assertEq("a", root.getNamespaceURI());
			assertEq("a",
				root.getElementsByTagName("child").item(0).getNamespaceURI());
			attr = root.getAttributeNode("atr");
			assertNull(attr.getNamespaceURI());
			child = (Element) root.getElementsByTagName("child").item(0);
			attr = child.getAttributeNodeNS("uu", "childAtr2");
			assertEq("atr3", attr.getValue());
			attr = child.getAttributeNodeNS("vv", "childAtr2");
			assertEq("atr2", attr.getValue());
			doc = KXmlUtils.parseXml("<a><a:T xmlns:a='a.b'/></a>");
			el = doc.getDocumentElement();
			el = (Element) el.getChildNodes().item(0);
			assertEq("a.b", el.getNamespaceURI());
		} catch (Exception ex) {fail(ex);}
		try {
			source=
"<?xml version=\"1.0\"?>\n" +
"<Envelope>\n" +
"	<Heade2r>\n" +
"		<Request KodPartnera=\"0001\" IdentZpravy=\"154\" RefMsgID=\"123\" />\n" +
"	</Header>\n" +
"	<Body>\n" +
"		<Get_PSP CisloSmlouvy=\"306000030\" PoradiVozidla=\"1\" KodPojistitele=\"0062\" />\n" +
"	</Body>\n" +
"</Envelope>";
			KXmlUtils.parseXml(source);
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("XML011") <0 &&
				ex.getMessage().indexOf("XML404") < 0) {
				fail(ex);
			}
		}
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(
			"<?xml version=\"1.0\" ?>\n<Envelope/>".getBytes());
			doc = KXmlUtils.parseXml(bais);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(baos, "Cp1252");
			KXmlUtils.writeXml(out, doc.getDocumentElement());
		} catch (Exception ex) {fail(ex);}
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(
			"<?xml version=\"1.0\" ?>\n<Envelope/>".getBytes());
			doc = KXmlUtils.parseXml(bais);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter out = new OutputStreamWriter(baos, "Cp1254");
			KXmlUtils.writeXml(out, doc.getDocumentElement());
		} catch (Exception ex) {fail(ex);}
		try { //check getXPathPosition and resolveXPosition
			doc = KXmlUtils.parseXml("<a>x<b/>y<c/><b><c a = 'a'/></b></a>");
			node = KXmlUtils.resolveXPosition(doc, "/a[1]/b[2]");
			s = KXmlUtils.nodeToString(node);
			assertEq("<b><c a=\"a\"/></b>",s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/a/b[2]", s);
			node = KXmlUtils.resolveXPosition(doc, "/a[1]/b[2]/c/@a");
			s = KXmlUtils.nodeToString(node);
			assertEq("a=\"a\"", s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/a/b[2]/c/@a", s);
			doc = KXmlUtils.parseXml(
				"<a xmlns='a'>x<b/>y<c/><b><c a = 'a'/></b></a>");
			node = KXmlUtils.resolveXPosition(doc, "/:a[1]/:b[2]");
			s = KXmlUtils.nodeToString(node);
			assertEq("<b xmlns=\"a\"><c a=\"a\"/></b>", s);
			node = KXmlUtils.resolveXPosition(doc, "/:a[1]/:b[2]/:c[1]@a");
			s = KXmlUtils.nodeToString(node);
			assertEq("a=\"a\"", s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/:a/:b[2]/:c/@a", s);
			node = KXmlUtils.resolveXPosition(doc, "/:a[1]/text()[2]");
			s = KXmlUtils.nodeToString(node);
			assertEq("y", s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/:a/text()[2]", s);
			doc = KXmlUtils.parseXml(
				"<b:a xmlns:b='a'>x<b/>y<c/><b:b><c a = 'a'/></b:b></b:a>");
			node = KXmlUtils.resolveXPosition(doc, "/b:a[1]/b:b[1]");
			s = KXmlUtils.nodeToString(node);
			assertEq("<b:b xmlns:b=\"a\"><c a=\"a\"/></b:b>", s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/b:a/b:b[1]", s);
			node = KXmlUtils.resolveXPosition(doc, "/b:a[1]/b:b[1]/:c[1]@a");
			s = KXmlUtils.nodeToString(node);
			assertEq("a=\"a\"", s);
			s = KXmlUtils.getXPosition(node);
			assertEq("/b:a/b:b[1]/c/@a", s);
		} catch (Exception ex) {fail(ex);}
		try {
			doc = KXmlUtils.newDocument();
			root = doc.createElementNS("syntea.cz", "a:root");
			root.setAttribute("a", "a");
			root.setAttributeNS("syntea.cz", "b:b", "b");
			root.setAttributeNS("syntea.com", "c:c", "c");
			el = doc.createElement("child1");
			root.appendChild(el);
			child = el = doc.createElementNS("syntea.com", "child2");
			root.appendChild(el);
			el = doc.createElementNS("syntea.cz", "b:child3");
			root.appendChild(el);
			el = doc.createElementNS("syntea.eu", "a:child4");
			root.appendChild(el);
			el = doc.createElementNS("syntea.eu", "x:child5");
			root.appendChild(el);
			doc.appendChild(root);
			KXmlUtils.setNecessaryXmlnsAttrs(root);
			s = KXmlUtils.nodeToString(root);
			cmpResult = KXmlUtils.compareXML(
				"<a:root a=\"a\" b:b=\"b\" c:c=\"c\" " +
				"xmlns:a=\"syntea.cz\" " +
				"xmlns:b=\"syntea.cz\" " +
				"xmlns:c=\"syntea.com\">" +
				"<child1/>" +
				"<child2 xmlns=\"syntea.com\"/>" +
				"<b:child3/>" +
				"<a:child4 xmlns:a=\"syntea.eu\"/>" +
				"<x:child5 xmlns:x=\"syntea.eu\"/>" +
				"</a:root>", s);
			if (cmpResult.errorWarnings()) {
				fail(cmpResult.toString() + "\n" + s);
			}
			el = KXmlUtils.cloneWithChangedNamespace(
				child, "syntea.com", "syntea.org");
			s = KXmlUtils.nodeToString(el);
			cmpResult = KXmlUtils.compareXML(
				"<child2 xmlns=\"syntea.org\"/>", s);
			if (cmpResult.errorWarnings()) {
				fail(cmpResult.toString() + "\n" + s);
			}
			el = KXmlUtils.cloneWithChangedNamespace(
				root, "syntea.cz", "syntea.org");
			s = KXmlUtils.nodeToString(el);
			cmpResult = KXmlUtils.compareXML(
				"<a:root a=\"a\" b:b=\"b\" c:c=\"c\" " +
				"xmlns:a=\"syntea.org\" " +
				"xmlns:b=\"syntea.org\" " +
				"xmlns:c=\"syntea.com\">" +
				"<child1/>" +
				"<child2 xmlns=\"syntea.com\"/>" +
				"<b:child3/>" +
				"<a:child4 xmlns:a=\"syntea.eu\"/>" +
				"<x:child5 xmlns:x=\"syntea.eu\"/>" +
				"</a:root>", s);
			if (cmpResult.errorWarnings()) {
				fail(cmpResult.toString() + "\n" + s);
			}
		} catch (Exception ex) {fail(ex);}
		try {
			el = KXmlUtils.parseXml("<a>\r\nxxx\r\n  yy  y\r\n</a>").
				getDocumentElement();
			assertEq("<a>\nxxx\n  yy  y\n</a>", KXmlUtils.nodeToString(el));
			StringWriter sw = new StringWriter();
			KXmlUtils.writeXml(sw, "UTF-8", el, null, false, false, true);
			sw.close();
			assertEq("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<a>\nxxx\n  yy  y\n</a>", sw.toString());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
			KXmlUtils.writeXml(osw, el, false, false, true);
			osw.close();
			assertEq("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<a>\nxxx\n  yy  y\n</a>", baos.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			s = "<a><b a='a\"' b=\"b'\">\n\nxxx\n yy   y\n\n</b></a>";
			el = KXmlUtils.parseXml(s).getDocumentElement();
			assertEq(KXmlUtils.nodeToString(el, false), s);
			assertEq(KXmlUtils.nodeToString(el, true),
"<a>\n" +
"  <b a='a\"' b=\"b'\">\n" +
"    xxx yy y\n" +
"  </b>\n" +
"</a>");
			assertEq(KXmlUtils.nodeToString(el, true, false, true),
"<a>\n" +
"  <b a='a\"' b=\"b'\">xxx\n" +
" yy   y</b>\n" +
"</a>");

			s = "<x a=\"A\nB\"><y/>text<z/></x>";
			el = KXmlUtils.parseXml(s).getDocumentElement();
			assertEq(KXmlUtils.nodeToString(el),
				"<x a=\"A B\"><y/>text<z/></x>");
			assertEq(KXmlUtils.nodeToString(el, false),
				"<x a=\"A B\"><y/>text<z/></x>");
			assertEq(KXmlUtils.nodeToString(el, true),
"<x a=\"A B\">\n" +
"  <y/>\n" +
"  text\n" +
"  <z/>\n" +
"</x>");
			assertEq(KXmlUtils.nodeToString(el, true, false, true),
"<x a=\"A B\">\n" +
"  <y/>\n" +
"  text\n" +
"  <z/>\n" +
"</x>");
		} catch (Exception ex) {fail(ex);}
		try {
			s = "<a a='xxx'/>";
			el = KXmlUtils.parseXml(s).getDocumentElement();
			assertEq(el, ((NodeList) KXpathExpr.evaluate(el, "/a")).item(0));
			assertEq("xxx", ((NodeList) KXpathExpr.evaluate(el, "/a/@a"))
				.item(0).getNodeValue());
			assertNull(KXpathExpr.evaluate(el, "@b"));
			assertEq(0, ((NodeList) KXpathExpr.evaluate(el, "/b")).getLength());

			assertNull(KXpathExpr.evaluate(el,
				XPathConstants.NODE, "@b", null, null, null));
			assertNull(((String) KXpathExpr.evaluate(el,
				XPathConstants.STRING, "@b", null, null, null)));
			assertEq(0, ((NodeList) KXpathExpr.evaluate(el,
				XPathConstants.NODESET, "@b", null, null, null)).getLength());
			assertEq(0, ((Number) KXpathExpr.evaluate(el,
				XPathConstants.NUMBER, "@b", null, null, null)).intValue());
			assertFalse(((Boolean) KXpathExpr.evaluate(el,
				XPathConstants.BOOLEAN, "@b", null, null, null)));
		} catch (Exception ex) {fail(ex);}
		try {
			el = KXmlUtils.parseXml("<a/>").	getDocumentElement();
			NamedNodeMap nnm = KXmlUtils.getAttributesNS(el, null);
			assertEq(0, nnm.getLength());
			nnm = KXmlUtils.getAttributesNS(el, "a.b");
			assertEq(0, nnm.getLength());
			el.setAttribute("a", "a");
			nnm = KXmlUtils.getAttributesNS(el, null);
			assertEq(1, nnm.getLength());
			nnm = KXmlUtils.getAttributesNS(el, "a.b");
			assertEq(0, nnm.getLength());
			el.setAttribute("b", "b");
			nnm = KXmlUtils.getAttributesNS(el, "a.b");
			assertEq(0, nnm.getLength());
			nnm = KXmlUtils.getAttributesNS(el, null);
			assertEq(2, nnm.getLength());
			el.setAttributeNS("a.b","a:a", "a");
			nnm = KXmlUtils.getAttributesNS(el, "a.b");
			assertEq(1, nnm.getLength());
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}