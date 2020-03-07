package test.common.xml;

import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.net.URL;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.UserDataHandler;
import buildtools.STester;
import buildtools.XDTester;

/** Test KDOMBuilder.
 * @author  Vaclav Trojan
 */
public class TestKDOMBuilder extends STester {

	public TestKDOMBuilder() {super();}

	private void displayNodeList(final NodeList nl, final int level) {
		if (nl == null) {
			System.out.println("; no childNodes");
			return;
		}
		int len = nl.getLength();
		System.out.println("; num of nodes: " + len);
		for (int i = 0; i < len; i++) {
			System.out.print("[" + i + "]: ");
			displayNode(nl.item(i), level);
		}
	}

	private void displayNode(final Node node, final int level) {
		if (node == null) {
			System.out.println("NULL");
			return;
		}
		System.out.print("level: " + level + ", " + node.getNodeName() + ", ");
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
				System.out.println("(ATTRIBUTE) value: '" +
					node.getNodeValue() + "'");
				return;
			case Node.CDATA_SECTION_NODE:
				System.out.println("(CDATA_SECTION) value: '" +
					node.getNodeValue() + "'");
				return;
			case Node.COMMENT_NODE:
				System.out.println("(COMMENT)");
				return;
			case Node.DOCUMENT_FRAGMENT_NODE:
				System.out.println("(DOCUMENT_FRAGMENT)");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.DOCUMENT_NODE:
				System.out.println("(DOCUMENT)");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.DOCUMENT_TYPE_NODE:
				System.out.println("(DOCUMENT_TYPE)");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.ELEMENT_NODE:
				System.out.println("(ELEMENT)");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.ENTITY_NODE:
				System.out.println("(ENTITY) value: value: '" +
					node.getNodeValue() + "'");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.ENTITY_REFERENCE_NODE:
				System.out.print("(ENTITY_REFERENCE) value: '" +
					node.getNodeValue() + "'");
				displayNodeList(node.getChildNodes(), level + 1);
				return;
			case Node.NOTATION_NODE:
				System.out.println("(NOTATION)");
				return;
			case Node.PROCESSING_INSTRUCTION_NODE:
				System.out.println("(PROCESSING_INSTRUCTION) value: '" +
					node.getNodeValue() + "'");
				return;
			case Node.TEXT_NODE:
				System.out.println("(TEXT) value: '" +
					node.getNodeValue() + "'");
				return;
			default:
				System.out.println("(UNKNOWN) value: '" +
					node.getNodeValue() + "'");
				displayNodeList(node.getChildNodes(), level + 1);
		}
	}

	private static String getPrintableString(final String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c <= ' ') {
				sb.append("&$x");
				sb.append(Integer.toString((int) c, 16));
				sb.append("; ");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
			DocumentBuilderFactory builderFactory =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			DOMImplementation di = builder.getDOMImplementation();
			DocumentType dt = di.createDocumentType("root", null, null);
			Document doc = di.createDocument(null, "root", dt);
			Element selem = doc.getDocumentElement();
			selem.setAttribute("a", "1");
			selem.setAttribute("aa", "3");
			selem.setIdAttribute("a", true);
			Element selem1 = doc.createElement("a");
			selem1.setAttribute("att", "1");
			selem1.setIdAttribute("att", true);
			selem.appendChild(selem1);
			selem1 = doc.createElement("b");
			selem1.setAttribute("att", "2");
			selem1.setIdAttribute("att", true);
			selem.appendChild(selem1);
			selem1 = doc.createElement("c");
			selem1.setAttribute("att", "3");
			selem1.setIdAttribute("att", true);
			selem.appendChild(selem1);
			Element el = doc.getElementById("2");
			assertEq("b", el.getTagName());
		} catch (Exception ex) {fail(ex);}
		KDOMBuilder builder;
		DOMImplementation di;
		Document doc;
		DocumentType dt;
		NodeList nl;
		NamedNodeMap nm;
		Attr att;
		Element el, el1, el2;
		Node n;
		String s, data;
		StringBuffer sb;
		int len;
		Object obj;
		Text txt;
		Entity e;
		try {// test xmlns attributes
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			doc = builder.parse("<a/>"); // default namespace
			el = doc.getDocumentElement();
			assertEq("a", el.getNodeName());
			assertEq("a", el.getLocalName());
			assertNull(el.getPrefix());
			assertNull(el.getNamespaceURI());
			doc = builder.parse("<a xmlns = 'abc'/>"); // default namespace
			el = doc.getDocumentElement();
			assertEq("a", el.getNodeName());
			assertEq("a", el.getLocalName());
			assertNull(el.getPrefix());
			assertEq("abc", el.getNamespaceURI());
			assertEq(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				el.getAttributeNode("xmlns").getNamespaceURI());
			att = el.getAttributeNodeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns");
			assertEq(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,att.getNamespaceURI());
			att = el.getAttributeNodeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,"");
			assertEq(null, att);
			doc = builder.parse("<n:a xmlns:n = 'abc'/>"); // namespace prefix n
			el = doc.getDocumentElement();
			assertEq("n:a", el.getNodeName());
			assertEq("a", el.getLocalName());
			assertEq("n", el.getPrefix());
			assertEq("abc", el.getNamespaceURI());
			att = el.getAttributeNode("xmlns:n");
			assertEq(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				att.getNamespaceURI());
			att = el.getAttributeNodeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"n");
			assertEq(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,att.getNamespaceURI());
			doc = builder.parse("<a xmlns='a.b'><b xmlns=''><c/></b></a>");
			el = doc.getDocumentElement();
			assertEq("a.b", el.getNamespaceURI());
			el = (Element) el.getElementsByTagName("b").item(0);
			assertNull(el.getNamespaceURI());
			att = el.getAttributeNodeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns");
			assertEq("", att.getNodeValue());
			el = (Element) el.getElementsByTagName("c").item(0);
			assertNull(el.getNamespaceURI());
		} catch (Exception ex) {fail(ex);}
		try {// prefixed xmlns can't be empty
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			builder.parse("<a:a xmlns:a='a.b'><b xmlns:x=''/></a:a>");
			fail("Error not reported");
		} catch (Exception x) {}
		try {
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(false);
			doc = builder.parse("<a xmlns='x.y'><b><c/></b></a>");
			assertEq("x.y", doc.getDocumentElement().getNamespaceURI());
			assertEq("x.y",
				doc.getDocumentElement().getFirstChild().getNamespaceURI());
			assertEq("x.y",
				doc.getElementsByTagName("c").item(0).getNamespaceURI());
			doc = builder.parse("<a><b xmlns='x.y'><c/></b></a>");
			assertNull(doc.getDocumentElement().getNamespaceURI());
			assertEq("x.y",
				doc.getDocumentElement().getFirstChild().getNamespaceURI());
			assertEq("x.y",
				doc.getElementsByTagName("c").item(0).getNamespaceURI());
			// test character reference in value of attribute or text
			doc = builder.parse(
				"<doc a='&#60;&#62;'>&#60;&#62;<![CDATA[]]&#62;]]></doc>");
			el = doc.getDocumentElement();
			assertEq("<>", el.getAttribute("a"));
			assertEq("<>", el.getChildNodes().item(0).getNodeValue());
			assertEq("]]&#62;", el.getChildNodes().item(1).getNodeValue());
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(true);
			builder.setValidating(false);
			doc = builder.parse(
				"<doc a='&#60;&#62;'>&#60;&#62;<![CDATA[]]&#62;]]></doc>");
			el = doc.getDocumentElement();
			assertEq("<>", el.getAttribute("a"));
			assertEq("<>]]&#62;", el.getFirstChild().getNodeValue());
			// test character reference in value of attribute or text
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(true);
			builder.setValidating(false);
			s = "<!-- xx -->\n"+
				"<?p1 x?>\n"+
				"<!DOCTYPE doc [\n"+
				"<!ELEMENT doc (#PCDATA)*>\n"+
				"<!ENTITY a '&#60;&#62;'>]>\n"+
				"<!-- yy -->\n"+
				"<?p2 x?>\n"+
				"<doc a='&#60;&#62;'>&#60;&#62;<![CDATA[]]&#62;]]></doc>";
			doc = builder.parse(s);
			dt = doc.getDoctype();
			assertEq("doc", doc.getDoctype().getName());
			assertNull(dt.getPublicId());
			assertNull(dt.getSystemId());
			assertTrue(doc == dt.getOwnerDocument());
			s = doc.getDoctype().getInternalSubset();
			assertTrue(s.indexOf("<!ELEMENT ") >= 0
				&& s.indexOf("doc") > 0 && s.indexOf("<!ENTITY") > 0);
			assertEq("<>", doc.getDocumentElement().getAttribute("a"));
			assertEq("<>]]&#62;", doc.getDocumentElement().getTextContent());
			// test Processing instruction
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setIgnoringComments(false);
			builder.setValidating(false);
			doc = builder.newDocument();
			doc.appendChild(doc.createComment("1"));
			doc.appendChild(doc.createProcessingInstruction("A", "A"));
			doc.appendChild(doc.createProcessingInstruction("B", "B"));
			el = doc.createElement("root");
			el.appendChild(doc.createProcessingInstruction("C", "C"));
			doc.appendChild(el);
			doc.appendChild(doc.createProcessingInstruction("D", "D"));
			doc.appendChild(doc.createComment("2"));
			assertEq("<?xml version=\"1.0\"?>\n"
				+"<!--1--><?A A?><?B B?><root><?C C?></root><?D D?><!--2-->",
				KXmlUtils.nodeToString(doc));
			java.io.ByteArrayOutputStream baos =
				new java.io.ByteArrayOutputStream();
			java.io.OutputStreamWriter osw =
				new java.io.OutputStreamWriter(baos, "UTF-8");
			KXmlUtils.writeXml(osw, doc);
			osw.close();
			assertEq("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+"<!--1--><?A A?><?B B?><root><?C C?></root><?D D?><!--2-->",
				baos.toString());
			doc = builder.parse(
				new java.io.ByteArrayInputStream(baos.toByteArray()));
			assertEq("<?xml version=\"1.0\"?>\n"
				+ "<!--1--><?A A?><?B B?><root><?C C?></root><?D D?><!--2-->",
				KXmlUtils.nodeToString(doc));
			builder.setIgnoringComments(true);
			doc = builder.parse(
				new java.io.ByteArrayInputStream(baos.toByteArray()));
			assertEq("<?xml version=\"1.0\"?>\n"
				+ "<?A A?><?B B?><root><?C C?></root><?D D?>",
				KXmlUtils.nodeToString(doc));
			doc = builder.newDocument(null, "root", null);
			el = doc.getDocumentElement();
			el.appendChild(doc.createProcessingInstruction("C", "C"));
			doc.insertBefore(doc.createProcessingInstruction("A", "A"), el);
			doc.insertBefore(doc.createProcessingInstruction("B", "B"), el);
			doc.appendChild(doc.createProcessingInstruction("D", "D"));
			assertEq("<?xml version=\"1.0\"?>\n"
				+ "<?A A?><?B B?><root><?C C?></root><?D D?>",
				KXmlUtils.nodeToString(doc));
			baos = new java.io.ByteArrayOutputStream();
			osw = new java.io.OutputStreamWriter(baos);
			KXmlUtils.writeXml(osw, doc);
			osw.close();
			doc = builder.parse(
				new java.io.ByteArrayInputStream(baos.toByteArray()));
			assertEq("<?xml version=\"1.0\"?>\n"
				+ "<?A A?><?B B?><root><?C C?></root><?D D?>",
				KXmlUtils.nodeToString(doc));
			// test KXmlUtils.getTextContent
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setValidating(false);
			doc = builder.parse("<a/>");
			el = doc.getDocumentElement();
			assertEq("", KXmlUtils.getTextContent(el));
			doc = builder.parse("<a>a<b>b<c>c</c><c>c1</c>b1</b>a1</a>");
			el = doc.getDocumentElement();
			assertEq("abcc1b1a1", KXmlUtils.getTextContent(el));
			builder = new KDOMBuilder();
			doc = builder.newDocument();
			el = doc.createElement("a");
			assertEq("", KXmlUtils.getTextContent(el));
			assertTrue((el1 = doc.getDocumentElement()) == null, "'"+el1+"'");
			doc.appendChild(el);
			assertTrue((el1 = doc.getDocumentElement()) == el, "'" + el1 + "'");
			el1 = doc.createElement("b");
			try {
				doc.appendChild(el1);
				fail("Error not thrown");
			} catch (DOMException ex) {
				assertTrue(ex.code == DOMException.HIERARCHY_REQUEST_ERR, ex);
			}
			doc = builder.newDocument(null, "a", null);
			el = doc.getDocumentElement();
			assertEq("", KXmlUtils.getTextContent(el));
			assertEq("a", el.getNodeName());
			assertTrue((s = el.getPrefix()) == null, "'" + s +"'");
			assertTrue((s = el.getNamespaceURI()) == null, "; '" + s + "'");
			assertEq(0, el.getAttributes().getLength());
			doc = builder.newDocument("http://some.com", "b:a", null);
			el = doc.getDocumentElement();
			assertEq("", KXmlUtils.getTextContent(el));
			assertEq("b:a", el.getNodeName());
			assertEq("b", el.getPrefix());
			assertEq("a", el.getLocalName());
			assertEq("http://some.com", el.getNamespaceURI());
			assertEq(0, el.getAttributes().getLength());
			try {
				builder.newDocument(null, "b:a", null);
				fail("Error not thrown");
			} catch (DOMException ex) {
				assertTrue(ex.code == DOMException.NAMESPACE_ERR, ex);
			}
			// text data
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(false);
			el = builder.parse("<a a='a' b='b'>12</a>").getDocumentElement();
			nm = el.getAttributes();
			assertEq(2, nm.getLength());
			el.removeAttribute("a");
			assertEq(1, nm.getLength());
			nl = el.getChildNodes();
			assertEq(1, nl.getLength());
			assertEq("12", nl.item(0).getNodeValue());
			txt = (Text) nl.item(0);
			txt.setData("123");
			assertEq("123", nl.item(0).getNodeValue());
			assertEq("2", txt.substringData(1,1));
			txt.replaceData(1,2,"3");
			assertEq("13", nl.item(0).getNodeValue());
			assertEq("13", KXmlUtils.getTextContent(el));
			assertEq("13", txt.getWholeText());
			txt.replaceWholeText("987");
			assertEq("987", txt.getWholeText());
			assertEq("987", nl.item(0).getNodeValue());
			el = builder.parse("<a><![CDATA[ab]]></a>").getDocumentElement();
			nl = el.getChildNodes();
			assertEq(1, nl.getLength());
			assertEq("ab", nl.item(0).getNodeValue());
			txt = (Text) nl.item(0);
			txt.setData("xyz");
			assertEq("xyz", nl.item(0).getNodeValue());
			assertEq("y", txt.substringData(1,1));
			txt.replaceData(1,2,"p");
			assertEq("xp", nl.item(0).getNodeValue());
			assertEq("xp", KXmlUtils.getTextContent(el));
			assertEq("xp", txt.getWholeText());
			txt.replaceWholeText("rst");
			assertEq("rst", txt.getWholeText());
			assertEq("rst", nl.item(0).getNodeValue());
			el = builder.parse("<a>1<![CDATA[2]]></a>").getDocumentElement();
			nl = el.getChildNodes();
			assertEq(2, nl.getLength());
			assertEq("1", nl.item(0).getNodeValue());
			txt = (Text) nl.item(0);
			try {
				txt.substringData(1,1);
				fail("Error not thrown");
			} catch (DOMException ex) {
				assertTrue(ex.code == DOMException.INDEX_SIZE_ERR, ex);
			}
			txt.setData("123");
			assertEq("123", nl.item(0).getNodeValue());
			txt.replaceData(1,2,"3");
			assertEq("13", nl.item(0).getNodeValue());
			assertEq("132", KXmlUtils.getTextContent(el));
			assertEq("132", txt.getWholeText());
			txt.replaceWholeText("987");
			assertEq("987", txt.getWholeText());
			assertEq("987", nl.item(0).getNodeValue());
			// text data
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(false);
			doc = builder.parse("<a>12<![CDATA[34]]><b>56</b>78</a>");
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			assertEq(4, nl.getLength());
			assertEq("12", nl.item(0).getNodeValue());
			txt = (Text) nl.item(0);
			txt.setData("123");
			assertEq("123", nl.item(0).getNodeValue());
			assertEq("2", txt.substringData(1,1));
			txt.replaceData(1,2,"3");
			assertEq("13", nl.item(0).getNodeValue());
			assertEq("1334", ((Text) nl.item(0)).getWholeText());
			assertEq("1334", ((CDATASection) nl.item(1)).getWholeText());
			txt = ((Text) el.getFirstChild()).replaceWholeText("987");
			assertEq("987", txt.getWholeText());
			assertEq("b", txt.getNextSibling().getNodeName());
			////////////////////////////////////////////////////////////////////
			doc = builder.parse("<a><![CDATA[12]]>34<b>56</b>78</a>");
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			assertEq(4, nl.getLength());
			assertEq("12", nl.item(0).getNodeValue());
			CDATASection cd = (CDATASection) nl.item(0);
			cd.setData("123");
			assertEq("123", nl.item(0).getNodeValue());
			assertEq("2", cd.substringData(1,1));
			cd.replaceData(1,2,"3");
			assertEq("13", nl.item(0).getNodeValue());
			assertEq("1334",
				((CDATASection) nl.item(0)).getWholeText());
			assertEq("1334", ((Text) nl.item(1)).getWholeText());
			txt = ((CDATASection) el.getFirstChild()).replaceWholeText("987");
			assertEq("987", txt.getWholeText());
			assertEq("b", txt.getNextSibling().getNodeName());
			//getDOMImplementation, newDocument
			builder = new KDOMBuilder();
			di = builder.getDOMImplementation();
			assertTrue(di != null,"; DOMImplementation returns null");
			doc = builder.newDocument();
			assertTrue(doc != null, "newDocument should return null");
			//setCoalescing
			builder = new KDOMBuilder();
			builder.setCoalescing(false);
			doc = builder.parse("<a>x<![CDATA[y]]></a>");
			assertEq(2, doc.getDocumentElement().getChildNodes().getLength(),
				"Incorrect number of text nodes");
			builder.setCoalescing(true);
			doc = builder.parse("<a>x<![CDATA[y]]></a>");
			assertEq(1, doc.getDocumentElement().getChildNodes().getLength(),
				"Incorrect number of text nodes");
			assertEq("xy", doc.getDocumentElement().getChildNodes().item(0)
				.getNodeValue());
			//setNamespaceAware
			data = "<a xmlns:x='x' xmlns:y='y' xmlns='_' y:c='c' c='c'>" +
				"<d e='e'/></a>\n";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertTrue(null == el.getNamespaceURI());
			att = el.getAttributeNode("c");
			assertEq("c", att.getNodeName());
			assertTrue(null == el.getNamespaceURI());
			assertEq("c", att.getValue());
			att = el.getAttributeNode("y:c");
			assertEq("c", att.getValue());
			assertEq("y:c", att.getNodeName());
			assertTrue(att.getNamespaceURI() == null, "not null");
			att = el.getAttributeNode("xmlns:x");
			assertEq("xmlns:x", att.getNodeName());
			assertTrue(att.getNamespaceURI() == null);
			att = el.getAttributeNode("xmlns");
			assertTrue(att.getNamespaceURI() == null);
			assertTrue(el.getAttributeNodeNS("b","c") == null);
			el = (Element) el.getFirstChild();
			assertTrue(el.getNamespaceURI() == null);
			assertEq("e", el.getAttribute("e"));
			assertTrue(el.getAttributeNode("e").getNamespaceURI() == null);
			////////////////////////////////
			builder.setNamespaceAware(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq("_", el.getNamespaceURI());
			assertEq("a", el.getLocalName());
			att = el.getAttributeNode("c");
			assertEq("c", att.getNodeName());
			assertEq("c", att.getLocalName());
			assertTrue(null == att.getNamespaceURI());
			assertEq("c", att.getValue());
			att = el.getAttributeNode("y:c");
			assertEq("y:c", att.getNodeName());
			assertEq("c", att.getLocalName());
			assertEq("c", att.getValue());
			assertEq("y", att.getNamespaceURI());
			att = el.getAttributeNode("xmlns:x");
			assertEq("xmlns:x", att.getNodeName());
			assertEq("x", att.getLocalName());
			assertEq("http://www.w3.org/2000/xmlns/", att.getNamespaceURI());
			att = el.getAttributeNode("xmlns");
			assertEq("http://www.w3.org/2000/xmlns/", att.getNamespaceURI());
			att = el.getAttributeNodeNS("y","c");
			if (att == null) {
				fail("setNamespaceAware(true)");
			} else {
				assertEq("c", att.getValue());
			}
			el = (Element)el.getFirstChild();
			assertTrue(el != null);
			assertEq("_", el.getNamespaceURI());
			assertEq("e", el.getAttribute("e"));
			assertTrue(el.getAttributeNode("e").getNamespaceURI() == null);
			//setNamespaceAware
			data =
"<a xmlns:x='x' xmlns:y='y' xmlns='_' y:c='c'><d e='e'/></a>\n";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertNull(el.getNamespaceURI(), "setNamespaceAware(false)");
			att = el.getAttributeNode("y:c");
			assertEq("c", att.getValue(), "setNamespaceAware(false)");
			assertNull(att.getNamespaceURI(), "setNamespaceAware(false)");
			att = el.getAttributeNode("xmlns:x");
			assertNull(att.getNamespaceURI(), "setNamespaceAware(false)");
			att = el.getAttributeNode("xmlns");
			assertNull(att.getNamespaceURI(), "setNamespaceAware(false)");
			att = el.getAttributeNodeNS("b","c");
			assertNull(att, "setNamespaceAware(false)");
			el = (Element)el.getFirstChild();
			assertTrue(el != null, "setNamespaceAware(false)");
			assertNull(el.getNamespaceURI(), "setNamespaceAware(false)");
			assertEq("e", el.getAttribute("e"), "setNamespaceAware(false)");
			s = el.getAttributeNode("e").getNamespaceURI();
			assertNull(s, "setNamespaceAware(false)");
			builder.setNamespaceAware(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			s = el.getNamespaceURI();
			assertEq("_", s, "setNamespaceAware(true): '" + s + "'");
			assertEq("a", el.getLocalName(), "setNamespaceAware(false)");
			att = el.getAttributeNode("y:c");
			assertEq("c", att.getValue(), "setNamespaceAware(true)");
			assertEq("y", att.getNamespaceURI(), "setNamespaceAware(true)");
			att = el.getAttributeNode("xmlns:x");
			assertEq("http://www.w3.org/2000/xmlns/", att.getNamespaceURI(),
				"setNamespaceAware(false)");
			att = el.getAttributeNode("xmlns");
			assertEq("http://www.w3.org/2000/xmlns/", att.getNamespaceURI(),
				"setNamespaceAware(false)");
			att = el.getAttributeNodeNS("y","c");
			assertEq("c", att.getValue(), "setNamespaceAware(true)");
			el = (Element)el.getFirstChild();
			assertTrue(el != null,"setNamespaceAware(true)");
			assertEq("_", el.getNamespaceURI(), "setNamespaceAware(true)");
			assertEq("e", el.getAttribute("e"), "setNamespaceAware(true)");
			assertNull(el.getAttributeNode("e").getNamespaceURI(),
				"setNamespaceAware(true)");
			// test setExpandEntityReferences
			builder = new KDOMBuilder();
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(true);
			data = "<!-- t1 -->\n" +
				"<!DOCTYPE a [\n<!ENTITY e1 \"abcd\">\n" +
				"<!ENTITY e2 \"&e1;e\">\n"+
				"<!ELEMENT a (#PCDATA)>\n" +
				"<!ATTLIST a b CDATA #REQUIRED>\n]>"+
				"<a b='&e2;'>&lt;x&e2;y&gt;</a>" +
				"<!-- t2 -->";
			doc = builder.parse(data);
			dt = doc.getDoctype();
			try {
				nm = dt.getEntities();
				e = (Entity) nm.getNamedItem("e1");
				txt = doc.createTextNode("f");
				e.appendChild(txt);
				fail("Error not thrown");
			} catch (DOMException ex) {
				assertEq(ex.code, DOMException.NO_MODIFICATION_ALLOWED_ERR, ex);
			}
			el = doc.getDocumentElement();
			assertEq("abcde", el.getAttributeNode("b").getValue());
			nl = el.getChildNodes();
			len = nl.getLength();
			if (len > 0) {
				assertTrue(nl.item(0).getNodeType() == Node.TEXT_NODE, s);
			} else {
				fail("len == 0");
			}
			if (len > 1) {
				NodeList nl1;
				if (nl.item(1).getNodeType() != Node.ENTITY_REFERENCE_NODE) {
					fail("ENTITY_REFERENCE_NODE expected");
				} else if (!"e2".equals(s =  nl.item(1).getNodeName())) {
					fail("NodeName: '" + s + "'");
				} else if ((nl1 = nl.item(1).getChildNodes()) == null ||
					nl1.getLength() != 2) {
					fail("len=" + nl1.getLength());
				} else if (!"abcde".equals(
					s = KXmlUtils.getTextContent(nl.item(1)))) {
					fail("value: '" + s + "'");
				} else {
					n = nl1.item(0);
					if (n.getNodeType() != Node.ENTITY_REFERENCE_NODE) {
						fail("ENTITY_REFERENCE_NODE expected, " + n);
					}
					n = nl1.item(1);
					if (n.getNodeType() != Node.TEXT_NODE) {
						fail("TEXT_NODE expected, " + n);
					}
				}
			}
			if (len > 2) {
				if (nl.item(2).getNodeType() != Node.TEXT_NODE) {
					fail("TEXT_NODE expected, " + nl.item(2));
				} else if (!"y>".equals(s =  nl.item(2).getNodeValue())) {
					fail("unexpected: '" + s + "'");
				}
			} else {
				fail("len > 2: " + len);
			}
			assertEq("<xabcdey>", KXmlUtils.getTextContent(el));
			builder.setExpandEntityReferences(true);
			builder.setCoalescing(true);
			builder.setValidating(true);
			doc = builder.parse("<!DOCTYPE a [<!ENTITY a \"abc\">\n" +
				"<!ELEMENT a (#PCDATA)>\n"+
				"<!ATTLIST a b CDATA #REQUIRED>\n]>"+
				"<a b='&a;'>&lt;x&a;y&gt;</a>");
			el = doc.getDocumentElement();
			assertEq("abc", el.getAttributeNode("b").getValue());
			if (el.getChildNodes().getLength() != 1) {
				displayNodeList(el.getChildNodes(), 0);
				fail("setExpandEntityReferences false");
			} else {
				assertEq("<xabcy>", el.getChildNodes().item(0).getNodeValue());
			}
			dt = doc.getDoctype();
			e = (Entity) dt.getEntities().getNamedItem("a");
			try {
				e.setPrefix("p");
				fail("rror not thrown");
			} catch (DOMException ex) {
				assertTrue(ex.code == DOMException.NAMESPACE_ERR
					|| ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, ex);
			}
			try {
				e.appendChild(doc.createTextNode("a"));
				fail("Exception not thrown");
			} catch (DOMException ex) {
				assertEq(ex.code, DOMException.NO_MODIFICATION_ALLOWED_ERR, ex);
			}
			try {
				e.setNodeValue("b");
				assertNull(e.getNodeValue());
				fail("Exception not thrown");
			} catch (DOMException ex) {//new version of javax
				assertEq(ex.code, DOMException.NO_MODIFICATION_ALLOWED_ERR, ex);
			}
			try {
				e.normalize();
			} catch (Exception ex) {fail(ex);}
			//test XML from file
			builder = new KDOMBuilder();
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(true);
			data = getDataDir() + "TestDTD001.xml";
			doc = builder.parse(new File(data));
			el = doc.getDocumentElement();
			assertEq("a1", el.getAttribute("a1"));
			assertEq("a2", el.getAttribute("a2"));
			assertEq("%pe", el.getAttribute("a3"));
			assertEq("text 1,text 2", el.getTextContent());
			builder = new KDOMBuilder();
			builder.setExpandEntityReferences(false);
			builder.setCoalescing(false);
			builder.setValidating(true);
			data = "<!-- t1 -->\n" +
				"<!DOCTYPE a [\n<!ENTITY e1 \"abcd\">\n" +
				"<!ENTITY e2 \"&e1;<b>?</b>e\">\n"+
				"<!ELEMENT a (#PCDATA|b)*>\n" +
				"<!ELEMENT b (#PCDATA)>\n]>\n" +
				"<a>&lt;x&e2;y&gt;</a>" +
				"<!-- t2 -->";
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			len = nl.getLength();
			assertEq("<a>&lt;xabcd<b>?</b>ey></a>",KXmlUtils.nodeToString(el));
			if (len == 3) {
				n = nl.item(0);
				if (n.getNodeType() == Node.TEXT_NODE) {
					assertEq("<x", n.getNodeValue());
				} else {
					fail("Expected: TEXT_NODE");
				}
				n = nl.item(1);
				if (n.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
					NodeList nl1 = n.getChildNodes();
					if (nl1.getLength() == 3) {
						n = nl1.item(0);
						if (n.getNodeType() != Node.ENTITY_REFERENCE_NODE) {
							fail("Expected ENTITY_REFERENCE_NODE, " + n);
						}
					} else {
						fail("len =" + nl1.getLength());
					}
				} else {
					fail("Expected ENTITY_REFERENCE_NODE, " + n);
				}
				n = nl.item(2);
				if (n.getNodeType() == Node.TEXT_NODE) {
					assertEq("ey>", n.getNodeValue());
				} else {
					fail("nodetype:" + n);
				}
			} else {
				fail("len = " + len);
			}
			builder = new KDOMBuilder();
			builder.setExpandEntityReferences(true);
			builder.setCoalescing(true);
			builder.setValidating(true);
			data = "<!-- t1 -->\n" +
				"<!DOCTYPE a [\n<!ENTITY e1 \"abcd\">\n" +
				"<!ENTITY e2 \"&e1;<b>?</b>e\">\n"+
				"<!ELEMENT a (#PCDATA|b)*>\n" +
				"<!ELEMENT b (#PCDATA)>\n]>\n" +
				"<a>&lt;x&e2;y&gt;</a>" +
				"<!-- t2 -->";
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq("<a>&lt;xabcd<b>?</b>ey></a>", KXmlUtils.nodeToString(el));
			//getWholeText, replaceWholeText, getTextContent, setTextContent
			data = "<!-- t1 -->\n" +
				"<!DOCTYPE a [\n<!ENTITY a \"abcd\">\n<!ENTITY b \"&a;e\">\n" +
				"<!ELEMENT a ANY>\n" +
				"<!ATTLIST a b CDATA #IMPLIED>\n" +
				"<!ELEMENT b ANY>\n" +
				"<!ELEMENT c ANY>]>\n" +
				"<a b='&a;'>&lt;x&b;<![CDATA[y>]]><b>q</b>xx<c/></a>" +
				"<!-- t1 -->";
			builder = new KDOMBuilder();
			builder.setCoalescing(false);
			builder.setExpandEntityReferences(false);
			builder.setValidating(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			n = el.getParentNode();
			if (n == null) {
				fail("Parent is null");
			} else if (n.getNodeType() != Node.DOCUMENT_NODE) {
				fail("Parent is not document");
			}
			nl = el.getChildNodes();
			if (nl.getLength() != 6) {
				fail("len : " + nl.getLength());
			} else {
				n = nl.item(2);
				if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
					nl = el.getChildNodes();
					assertEq(nl.getLength(), 6);
					el.setTextContent("ahoj");
					assertEq(nl.getLength(), 1);
					assertEq("ahoj", el.getTextContent());
					Attr a = el.getAttributeNode("b");
					assertTrue(a.isEqualNode(a.cloneNode(true)),
						"EqualNode not true");
					assertNull(((Attr) a.cloneNode(true)).getOwnerElement(),
						"OwnerElement not null");
					assertTrue(el.isEqualNode(el.cloneNode(true)),
						"EqualNode not true");
					assertFalse(el.isSameNode(el.cloneNode(true)),
						"Same Node true");
				} else {
					fail("nodeType : " + n.getNodeType());
				}
			}
			//User data
			class MyHandler implements UserDataHandler {
				short _operation;
				public String _key;
				public Object _data;
				public Node _src;
				public Node _dst;

				public MyHandler() {
					_operation = -1;
					_key = null;
					_data = null;
					_src = null;
					_dst = null;
				}
				@Override
				/** This method is called whenever the node for which this
				 * handler is registered is imported or cloned.
				 * <br>DOM applications must not raise exceptions in a
				 * <code>UserDataHandler</code>. The effect of throwing
				 * exceptions from the handler is DOM implementation
				 * dependent.
				 * @param operation Specifies the type of operation that is
				 * being performed on the node.
				 * @param key Specifies the key for which this handler
				 * is being called.
				 * @param data Specifies the data for which this handler
				 * is being called.
				 * @param src Specifies the node being cloned, adopted,
				 * imported, or renamed. This is <code>null</code> when
				 * the node is being deleted.
				 * @param dst Specifies the node newly created if any, or
				 * <code>null</code>.
				 */
				public void handle(short operation,
					String key,
					Object data,
					Node src,
					Node dst) {
					_operation = operation;
					_key = key;
					_data = data;
					_src = src;
					_dst = dst;
				}
			}

			MyHandler u1 = new MyHandler();
			MyHandler u2 = new MyHandler();
			MyHandler u3 = new MyHandler();
			data = "<!-- t1 -->\n" +
				"<!DOCTYPE a [\n<!ENTITY a \"abcd\">\n" +
				"<!ELEMENT a (#PCDATA|b|c)*>\n" +
				"<!ELEMENT b (#PCDATA)*>\n" +
				"<!ENTITY b \"&a;e\">\n]>"+
				"<a b='&a;'>&lt;x&b;<![CDATA[y>]]><b>q</b>xx<c/></a>" +
				"<!-- t1 -->";
			builder = new KDOMBuilder();
			builder.setCoalescing(false);
			builder.setExpandEntityReferences(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
/////////////////////////////////////////
			obj = doc.setUserData("key", "doc0", u1);
			assertNull(obj);
			obj = doc.setUserData("key", "doc", u1);
			assertTrue("doc0".equals(obj), "'doc0' expected, got: " + obj);
			assertNull(el.setUserData("key", "el0", u2));
			obj = el.setUserData("key", "el", u2);
			assertTrue("el0".equals(obj), "'el0' expected, got: " + obj);
			Attr a = el.getAttributeNode("b");
			assertNull(a.setUserData("key", "a0", u3));
			obj = a.setUserData("key", "a", u3);
			assertTrue("a0".equals(obj), "'a0' expected, got: " + obj);
			n = doc.importNode(a, true);
			if (u3._operation != UserDataHandler.NODE_IMPORTED) {
				fail("Incorrect operation: " + u3._operation);
			}
			assertEq("a", u3._data);
			assertEq("key", u3._key);
			assertTrue(a == u3._src, "src: " + u3._src);
			assertTrue(n == u3._dst, "dst: " + u3._src);
			//parse
			data = "<!-- t1 -->" +
				"<!DOCTYPE a [\n<!ENTITY a \"abcd\">\n<!ENTITY b \"&a;e\">\n"+
				"<!ELEMENT a ANY>\n"+
				"<!ATTLIST a b CDATA #IMPLIED>]>\n" +
				"<a b='&a;'>&lt;x&b;y&gt;</a>" +
				"<!-- t1 -->";
			builder = new KDOMBuilder();
			builder.setExpandEntityReferences(false);
			builder.setValidating(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			s = el.getAttributeNode("b").getValue();
			assertEq("abcd", s);
			len = el.getChildNodes().getLength();
			if (len != 3 && len != 5) {
				displayNodeList(el.getChildNodes(), 0);
				fail("setExpandEntityReferences false");
			}
			el.normalize();
			len = el.getChildNodes().getLength();
			if (len != 3 && len != 5) {
				displayNodeList(el.getChildNodes(), 0);
				fail("setExpandEntityReferences false");
			}
			assertEq("<xabcdey>", KXmlUtils.getTextContent(el));
			builder.setExpandEntityReferences(true);
			doc = builder.parse("<!DOCTYPE a [<!ENTITY a \"abc\">\n" +
				"<!ELEMENT a ANY>\n"+
				"<!ATTLIST a b CDATA #IMPLIED>]>\n" +
				"<a b='&a;'>&lt;x&a;y&gt;</a>");
			el = doc.getDocumentElement();
			s = el.getAttributeNode("b").getValue();
			assertEq("abc", s);
			if (el.getChildNodes().getLength() != 1) {
				displayNodeList(el.getChildNodes(), 0);
				fail("setExpandEntityReferences false");
			} else {
				assertEq("<xabcy>", el.getChildNodes().item(0).getNodeValue());
			}
			//setIgnoringComments
			builder = new KDOMBuilder();
			builder.setIgnoringComments(false);
			doc = builder.parse("<a><!--comment--></a>");
			len = doc.getDocumentElement().getChildNodes().getLength();
			if (len != 1) {
				fail("Incorrect number of comment nodes (1 expected): " + len);
			}
			s = doc.getDocumentElement().getChildNodes().item(0).getNodeValue();
			assertEq("comment", s, "Incorrect text value ('xy' expected)");
			builder.setIgnoringComments(true);
			doc = builder.parse("<a><!--comment--></a>");
			len = doc.getDocumentElement().getChildNodes().getLength();
			if (len != 0) {
				fail("Incorrect number of comment nodes (0 expected): " + len);
			}
			//parse
			data = "<a xml:space='default' b='   \n\t\rb&#xd;c  '>" +
				"  \n\t\ra&#xd;b  </a>\n";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			att = el.getAttributeNode("b");
			s = att.getValue();
			assertEq("      b\rc  ", s,	getPrintableString(s));
			s = el.getFirstChild().getNodeValue();
			assertEq("  \n\t\na\rb  ", s, getPrintableString(s));
			data = "<a xml:space='preserve' b='   \n\t\rb&#xd;c  '>" +
				"  \n\t\ra&#xd;  </a>\n";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			att = el.getAttributeNode("b");
			s = att.getValue();
			assertEq("      b\rc  ", s, getPrintableString(s));
			s = el.getFirstChild().getNodeValue();
			assertEq("  \n\t\na\r  ", s, getPrintableString(s));
			//parse
			data = "<:a>\n</:a>";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			builder.setValidating(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq(":a", el.getTagName());
			assertEq(":a", el.getNodeName());
			s = el.getLocalName();
			assertTrue(s == null || "a".equals(s), s);//java: null, syntea: "a"
			s = el.getPrefix();
			assertTrue(s == null || "".equals(s), s); //java: null, syntea: ""
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq(":a", el.getTagName());
			assertEq(":a", el.getNodeName());
			assertEq("a", el.getLocalName());
			assertEq("", el.getPrefix());
			//parse
			data = "<a: xmlns:a = 'xyz'>\n</a:>";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			builder.setValidating(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq("a:", el.getTagName());
			assertEq("a:", el.getNodeName());
			s = el.getLocalName();
			assertTrue(s == null || "".equals(s), s); //java: null, syntea: ""
			s = el.getPrefix();
			assertTrue(s == null || "a".equals(s), s); //java: null, syntea: "a"
			data = "<:>\n</:>";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			builder.setValidating(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq(":", el.getTagName());
			assertEq(":", el.getNodeName());
			s = el.getLocalName();
			assertTrue(s == null || "".equals(s), s); //java: null, syntea: ""
			s = el.getPrefix();
			assertTrue(s == null || "".equals(s), s); //java: null, syntea: ""
			data = "<::>\n</::>";
			builder = new KDOMBuilder();
			builder.setNamespaceAware(false);
			builder.setValidating(false);
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			assertEq("::", el.getTagName());
			assertEq("::", el.getNodeName());
			s = el.getLocalName();
			assertTrue(s == null || "".equals(s), s); //java: null, syntea: ""
			s = el.getPrefix();
			assertTrue(s == null || ":".equals(s), s); //java: null, syntea: ":"
		} catch (Exception ex) {fail(ex);}
		if (XDTester.getFulltestMode()) {// run this test only in fullmode
			try {//test XML from URL (internet connection required))
				builder = new KDOMBuilder();
				builder.setExpandEntityReferences(false);
				builder.setCoalescing(false);
				builder.setValidating(true);
				data = "http://xdef.syntea.cz/tutorial/test/TestDTD001.xml";
				doc = builder.parse(new URL(data));
				el = doc.getDocumentElement();
				assertEq("a1", el.getAttribute("a1"));
				assertEq("a2", el.getAttribute("a2"));
				assertEq("%pe", el.getAttribute("a3"));
				assertEq("text 1,text 2", el.getTextContent());
				data =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE root SYSTEM 'http://xdef.syntea.cz/tutorial/test/TestDTD001.dtd'>\n"+
"<root a1=\"a1\" >&t1;,&t2;</root>";
				doc = builder.parse(data);
				el = doc.getDocumentElement();
				assertEq("a1", el.getAttribute("a1"));
				assertEq("a2", el.getAttribute("a2"));
				assertEq("%pe", el.getAttribute("a3"));
				assertEq("text 1,text 2", el.getTextContent());
			} catch (Exception ex) {
				s = ex.getMessage(); // Internet not available?
				if (s != null && (s.contains("java.net.UnknownHostException")
					|| s.contains("java.net.ConnectException")
					|| s.contains("java.io.FileNotFoundException"))) {
					setResultInfo("skipped; internet data not available: " + s);
				} else {
					fail(ex); // other error!
				}
			}
		}
		try {// test DOCTYPE declared in an external file.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD002.xml");
			el = doc.getDocumentElement();
			nl = el.getElementsByTagName("child");
			if (nl.getLength() != 11) {
				fail("number of elements 'image': " + nl.getLength());
			} else {
				el = (Element) nl.item(0);
				assertEq("B1", el.getAttribute("key"));
				el = (Element) nl.item(1);
				assertEq("B2", el.getAttribute("key"));
				el = (Element) nl.item(2);
				assertEq("B1", el.getAttribute("ref"));
				el = (Element) nl.item(3);
				assertEq("B1 B2", el.getAttribute("ref")) ;
				el = (Element) nl.item(4);
				assertEq("A1" , el.getAttribute("name"));
				el = (Element) nl.item(5);
				assertEq("A1", el.getAttribute("names"));
				el = (Element) nl.item(6);
				assertEq("A1 A2 A3", el.getAttribute("names"));
				el = (Element) nl.item(7);
				assertEq("N1", el.getAttribute("notation"));
				el = (Element) nl.item(8);
				assertEq("N2", el.getAttribute("notation"));
				el = (Element) nl.item(9);
				assertEq("N2", el.getAttribute("notation"));
				el = (Element) nl.item(10);
				assertEq("N3", el.getAttribute("notation"));
			}
		} catch (Exception ex) {fail(ex);}
		try {
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			builder.parse(getDataDir() + "TestDTD002_1.xml");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf(" XML404:") < 0 && s.indexOf(" XML098:") < 0) {
				fail(ex);
			}
		}
		try {// test DOCTYPE declared in an external file.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD003.xml");
			el = doc.getDocumentElement();
			nl = el.getElementsByTagName("image");
			if (nl.getLength() != 1) {
				fail("number of elements 'image': " + nl.getLength());
			} else {
				el = (Element) nl.item(0);
				assertEq("image/gif", el.getAttribute("type"));
			}
			// test DOCTYPE declared in an external file; external entity.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD010.xml");
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			assertEq("xtest2ytest2z", nl.item(0).getNodeValue());
		} catch (Exception ex) {fail(ex);}
		try {// test external entity in an attribute value.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.parse(getDataDir() + "TestDTD011.xml");
			fail(" Error not reported");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null ||
				(ex.getMessage().indexOf(" XML029:") < 0 &&
				ex.getMessage().indexOf(" XML403:") < 0)) {
				fail(ex);
			}
		}
		try {
			//test parameter entity
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD012.xml");
			el = doc.getDocumentElement();
			if (!"x'y'z".equals(s = el.getAttribute("a1"))) {
				fail(s);
			}
			if (!"x'y'z".equals(s = el.getAttribute("a2"))) {
				fail(s);
			}
			// test external parameter entity.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD013.xml");
			el = doc.getDocumentElement();
			assertEq("xtest2y", el.getAttribute("a1"));
			assertEq("xtest2y", el.getAttribute("a2"));
		} catch (Exception ex) {fail(ex);}
		try {// test external parameter entity.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			builder.parse(getDataDir() + "TestDTD014.xml");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null ||
				(ex.getMessage().indexOf(" XML028:") < 0 &&
				ex.getMessage().indexOf(" XML403:") < 0)) {
				fail(ex);
			}
		}
		try {// test INCLUDE section.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD015.xml");
			el = doc.getDocumentElement();
			assertEq("a1", el.getAttribute("a1"));
			// test IGNORE section.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD016.xml");
			el = doc.getDocumentElement();
			assertFalse(el.hasAttribute("a1"));
		} catch (Exception ex) {
			fail(ex);
		}
		try {// test parameter entity and defalut value in enumeration
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD017.xml");
			el = doc.getDocumentElement();
			assertEq("a1", el.getAttribute("a1"));
			assertEq("a2", el.getAttribute("a2"));
			assertEq("a3", el.getAttribute("a3"));
			builder.parse(getDataDir() + "TestDTD017_1.xml");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML009") < 0) {
				fail(ex);
			}
		}
		try {// test parameter entity and default value in enumeration error
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(true);
			builder.parse(getDataDir() + "TestDTD017_a.xml");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML009") < 0) {
				fail(ex);
			}
		}
		try {
			// check allow ']>' in text node
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			doc = builder.parse("<a b='b'>a]>b</a>");
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			assertEq("a]>b", nl.item(0).getNodeValue());
			// check allow ']]>' in attribute
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			doc = builder.parse("<a b=']]>' />");
			el = doc.getDocumentElement();
			assertEq("]]>", el.getAttribute("b"));
		} catch (Exception ex) {fail(ex);}
		try {// check illegal ']]>' in text node
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			builder.parse("<a b='b'>a]]]>b</a>");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf(" XML404") < 0 && s.indexOf(" XML053") < 0) {
				fail(ex);
			}
		}
		try {// check illegal ']]>' in text node
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			builder.parse("<a b='b'>a]]]>b\r\n</a>");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf(" XML404") < 0 && s.indexOf(" XML053") < 0) {
				fail(ex);
			}
		}
		try {// check illegal '<' in attribute
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			builder.parse("<a b='c<d'/>");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf(" XML404") < 0 && s.indexOf(" XML041") < 0) {
				fail(ex);
			}
		}
		try {
			// test white spaces in attributes and bodies
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setValidating(false);
			doc = builder.parse("<a b='\r\na\r\nb\r\n'>\r\na\r\nb\r\n</a>");
			el = doc.getDocumentElement();
			assertEq(" a b ", s = el.getAttribute("b"), getPrintableString(s));
			nl = el.getChildNodes();
			assertEq("\na\nb\n", s = nl.item(0).getNodeValue(),
				getPrintableString(s));
			// test DOCTYPE declared in an external file - expand entities.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD004.xml");
			el = doc.getDocumentElement();
			assertEq("'a'x'a'", el.getAttribute("att"));
			nl = el.getChildNodes();
			if ((len = nl.getLength()) == 1) {
				assertEq("x'a'y'a'z", nl.item(0).getNodeValue());
			} else {
				fail(" len: " + len);
			}
			// test DOCTYPE declared parameter entities in element names.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			doc = builder.parse(getDataDir() + "TestDTD020.xml");
			el = doc.getDocumentElement();
			assertEq("ListOfAdjustments", el.getNodeName());
			assertEq("%ListOfAdjustments-Dtypes;", el.getAttribute("a-dtype"));
			assertEq("XCBL30.sox", el.getAttribute("xmlns"));
			nl = el.getElementsByTagName("Adjustment");
			if (nl.getLength() != 1) {
				fail(" num: " + nl.getLength());
			} else if (!"y".equals(s = ((Element) nl.item(0)).
				getAttribute("x"))) {
				fail(s);
			}
			// test DOCTYPE declared parameter entities in element names.
			builder = new KDOMBuilder();
			builder.setValidating(true);
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			doc = builder.parse(getDataDir() + "TestDTD021.xml");
			el = doc.getDocumentElement();
			assertEq("a", el.getAttribute("att"));
		} catch (Exception ex) {
			fail(ex);
		}
		try {// test DOCTYPE declared parameter entities in element names.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.parse(getDataDir() + "TestDTD021_a.xml");
			fail("Error not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML082") < 0) {
				fail(ex);
			}
		}
		try {// test DOCTYPE declared parameter entities in element names.
			builder = new KDOMBuilder();
			builder.setValidating(true);
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			doc = builder.parse(getDataDir() + "TestDTD024.xml");
			el = doc.getDocumentElement();
			assertEq("%Types;", el.getAttribute("type"));
		} catch (Exception ex) {fail(ex);}
		try {// test processing instruction.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			doc = builder.parse(getDataDir() + "TestDTD023.xml");
			el = doc.getDocumentElement();
			dt = doc.getDoctype();
			nl = dt.getChildNodes();
			if (nl.getLength() != 0) {
				fail(" len: " + nl.getLength());
			}
			nl = el.getChildNodes();
			if (nl.getLength() != 2) {
				fail(" len: " + nl.getLength());
			} else {
				ProcessingInstruction pi = (ProcessingInstruction) nl.item(0);
				assertEq("ProcInstr1", pi.getNodeName());
				assertEq("MyProcInstr ", pi.getNodeValue());
				assertEq("ProcInstr1", pi.getTarget());
				assertEq("MyProcInstr ", pi.getData());
				Comment c = (Comment) nl.item(1);
				assertEq("#comment", c.getNodeName());
				assertEq(" comment ", c.getNodeValue());
			}
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML082") < 0) {
				fail(ex);
			}
		}
		try {
			// test processing instruction.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			doc = builder.parse(getDataDir() + "TestDTD025.xml");
			el = doc.getDocumentElement();
			nl = el.getChildNodes();
			if (nl.getLength() != 5) {
				fail("" + nl.item(0));
			} else if (nl.item(0).getNodeValue().length() != 102402) {
				fail("" +
					nl.item(0).getNodeValue().length());
			}
			// test entity redefinition.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			doc = builder.parse(getDataDir() + "TestDTD026.xml");
			el = doc.getDocumentElement();
			assertEq("<x", el.getAttribute("b"));
			assertEq(">x", el.getChildNodes().item(0).getNodeValue());
			// test ANY.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [<!ELEMENT a ANY>\n"+
				"<!ELEMENT b ANY>\n"+
				"<!ELEMENT c ANY>\n"+
				"<!ELEMENT d EMPTY>]>\n"+
				"<a>a<b>b1<c>c1<d></d>c2<b/></c>b2</b>c</a>";
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			if (el == null) {
				fail(" root is null");
			}
			//test empty string
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [<!ELEMENT a EMPTY>\n"+
				"<!ATTLIST a b CDATA #REQUIRED>]>\n" +
				"<a b=''/>";
			builder.parse(data);
		} catch (Exception ex) {
			fail(ex);
		}
		try { //test NOTATION
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [\n"+ //notation B is missing
				"<!ELEMENT a EMPTY>\n"+
				"<!ATTLIST a b NOTATION (A | B) #REQUIRED>\n" +
				"<!NOTATION A PUBLIC \"Test\" >\n" +
				"]>\n" +
				"<a b = 'A'/>";
			builder.parse(data);
			fail("Error not thrown");
		} catch (SRuntimeException ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML095") < 0) {
				fail(ex);
			}
		}
		try {
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data =
"<?xml version=\"1.0\" standalone=\"no\" ?>\n" +
"<!DOCTYPE img [\n" +
"  <!ELEMENT img EMPTY>\n" +
"  <!ATTLIST img src ENTITY #REQUIRED>\n" +
"  <!ENTITY logo SYSTEM\n" +
"    \"http://www.xmlwriter.net/logo.gif\" NDATA gif>\n" +
"  <!NOTATION gif PUBLIC \"gif viewer\" \"xyz\">\n" +
"  <!ENTITY ent \"ENT\">\n" +
"]>\n" +
"<img src=\"logo\"/>";
			doc = builder.parse(data);
			dt = doc.getDoctype();
			nm  = dt.getEntities();
			assertTrue(nm.getNamedItem("ent") != null);
			assertTrue(nm.getNamedItem("logo") != null);
			nm  = dt.getNotations();
			assertTrue(nm.getNamedItem("gif") != null);
			data =
"<?xml version='1.1' encoding='windows-1250'?>\n"+
"<!-- <a x='1'>t1<b/>t2</a> -->\n"+
"<!DOCTYPE doc [\n"+
"<!ELEMENT doc (#PCDATA)*>\n"+
"<!ENTITY a \"&#60;&#62;\">\n"+
"<!ATTLIST doc a CDATA #REQUIRED>\n" +
"<!NOTATION n PUBLIC 'http://xdef.syntea.cz/x'>\n"+
"]>\n"+
"<?xx yyy?>\n"+
"<doc\n"+
"  a='&#60;&#62;'>&#60;&#62;<![CDATA[]]&#62;]]&gt;]]>&lt;&gt;abc</doc>\n"+
"<!-- c -->";
			doc = builder.parse(data);
			assertEq(" <a x='1'>t1<b/>t2</a> ",
				doc.getFirstChild().getNodeValue());
			assertEq(doc.getFirstChild().getNextSibling().getNodeType(),
				Node.DOCUMENT_TYPE_NODE);
			el = doc.getDocumentElement();
			assertEq("<>", el.getAttribute("a"));
			assertEq("<>]]&#62;]]&gt;<>abc", el.getTextContent());
			dt = doc.getDoctype();
			nm  = dt.getEntities();
			assertTrue(nm.getNamedItem("a") != null);
//				assertTrue(nm.getNamedItem("logo") != null);
//				nm  = dt.getNotations();
//				assertTrue(nm.getNamedItem("gif") != null);
		} catch (Exception ex) {fail(ex);}
		try {
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [\n"+ //C is not notation
				"<!ELEMENT a EMPTY>\n"+
				"<!ATTLIST a b NOTATION (A | B) #REQUIRED>\n" +
				"<!NOTATION A PUBLIC \"Test\" >\n" +
				"<!NOTATION B SYSTEM \"Test1\" >\n" +
				"]>\n" +
				"<a b = 'C'/>";
			builder.parse(data);
			fail("Error not thrown");
		} catch (SRuntimeException ex) {
			s = ex.getMessage();
			if (s.indexOf("XML404") < 0 && s.indexOf("XML085") < 0) {
				fail(ex);
			}
		}
		try {// test nsURIs and xml attributes specified by DTD.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [<!ELEMENT a (#PCDATA | b)* >\n"+
				"<!ELEMENT b EMPTY>\n"+
				"<!ATTLIST a xmlns CDATA #FIXED 'a.b.c'>\n"+
				"<!ATTLIST b xmlns CDATA #FIXED 'd.e.f'>]>\n"+
				"<a><b/></a>";
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			if (el == null) {
				fail(" root is null");
			}
			assertEq("a.b.c", el.getAttribute("xmlns"));
			assertEq("a.b.c", el.getNamespaceURI());
			el1 = (Element) el.getFirstChild();
			assertEq("d.e.f", el1.getAttribute("xmlns"));
			assertEq("d.e.f", el1.getNamespaceURI());
			data = "<!DOCTYPE x:a [<!ELEMENT x:a (#PCDATA | y:b)* >\n"+
				"<!ELEMENT y:b EMPTY>\n"+
				"<!ATTLIST x:a xmlns:x CDATA #FIXED 'a.b.c'\n" +
				"          xml:lang CDATA 'en'\n"+
				"          xml:space CDATA #FIXED 'preserve'>\n"+
				"<!ATTLIST y:b xmlns:y CDATA #FIXED 'd.e.f'>]>\n"+
				"<x:a><y:b/></x:a>";
			doc = builder.parse(data);
			el = doc.getDocumentElement();
			if (el == null) {
				fail(" root is null");
			}
			assertEq("a.b.c", el.getAttribute("xmlns:x"));
			assertEq("preserve", el.getAttribute("xml:space"));
			assertEq("en", el.getAttribute("xml:lang"));
			assertEq("a.b.c", el.getNamespaceURI());
			el1 = (Element) el.getFirstChild();
			assertEq("d.e.f", el1.getAttribute("xmlns:y"));
			assertEq("d.e.f", el1.getNamespaceURI());
			// test setPrefix.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			doc = builder.parse("<a:b xmlns:a = 'anyUri'/>");
			el = doc.getDocumentElement();
			assertEq("a", el.getPrefix());
			el.setPrefix("");
			assertNull(el.getPrefix());
			el.setPrefix(null);
			 //doctype is child of document, Processing instruction ignored
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(true);
			builder.setIgnoringComments(false);
			data = "<!DOCTYPE a [\n"+ //
				"<!ELEMENT a EMPTY>\n"+
				"<!ATTLIST a b CDATA #REQUIRED>\n" +
				"<?PI pi?>\n" +
				"]>\n" +
				"<a b = 'A'/>";
			doc = builder.parse(data);
			dt = doc.getDoctype();
			el = doc.getDocumentElement();
			nl = doc.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				n = nl.item(i);
				switch (i) {
					case 0:
						if (n != dt) {
							fail("first node is not DOCTYPE");
						}
						break;
					case 1:
						if (n != el) {
							fail("second node is not root element");
						}
						break;
					default:
						fail("too many nodes");

				}
			}
			nl = dt.getChildNodes();
			if (nl == null || nl.getLength() != 0) {
				fail("DOCTYPE should return empty listy of child nodes");
			}
			// set xmlns empty.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			doc = builder.parse(
				"<a xmlns='a'><b><c xmlns=''><d/><e/></c><f/></b><g/></a>");
			el = doc.getDocumentElement();
			assertEq("a", el.getNamespaceURI());
			el1 = (Element) el.getElementsByTagName("b").item(0);
			if (!"a".equals(s = el1 == null ? "null" : el1.getNamespaceURI())) {
				fail(s);
			}
			el1 = (Element) el.getElementsByTagName("c").item(0);
			if ((s = el1 == null ? "null" : el1.getNamespaceURI()) != null) {
				fail(s);
			}
			el1 = (Element) el.getElementsByTagName("d").item(0);
			if ((s = el1 == null ? "null" : el1.getNamespaceURI()) != null) {
				fail(s);
			}
			el1 = (Element) el.getElementsByTagName("e").item(0);
			if ((s = el1 == null ? "null" : el1.getNamespaceURI()) != null) {
				fail(s);
			}
			el1 = (Element) el.getElementsByTagName("f").item(0);
			if (!"a".equals(s = el1 == null ? "null" : el1.getNamespaceURI())) {
				fail(s);
			}
			el1 = (Element) el.getElementsByTagName("g").item(0);
			if (!"a".equals(s = el1 == null ? "null" : el1.getNamespaceURI())) {
				fail(s);
			}
			try { //incorrect specification of empty prefix
				builder.parse(
					"<a:a xmlns:a='a'><a:b><a:c xmlns:a=''/></a:b></a:a>");
				fail("Error not thrown");
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("xmlns:a") < 0) {
					fail("" + ex);
				}
			}
			// test URI of xml and xmlns attributes.
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(false);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			doc = builder.parse(
				"<a xmlns='a' xmlns:b='b' xml:space='preserve'" +
				" xml:lang='en' a='a' >" +
				"<b:b b='b' b:b='c' /></a>\n");
			el = doc.getDocumentElement();
			assertEq("a", el.getNamespaceURI());
			if ((s = el.getAttributeNode("a").getNamespaceURI()) != null) {
				fail(s);
			}
			assertEq("http://www.w3.org/2000/xmlns/",
				el.getAttributeNode("xmlns").getNamespaceURI());
			assertEq("http://www.w3.org/2000/xmlns/",
				el.getAttributeNode("xmlns:b").getNamespaceURI());
			assertEq("http://www.w3.org/XML/1998/namespace",
				el.getAttributeNode("xml:space").getNamespaceURI());
			assertEq("http://www.w3.org/XML/1998/namespace",
				el.getAttributeNode("xml:lang").getNamespaceURI());
			el1 = (Element) el.getFirstChild();
			assertEq("b", el1.getNamespaceURI());
			if ((s = el1.getAttributeNode("b").getNamespaceURI()) != null) {
				fail(s);
			}
			if (!"b".equals(s = el1.getAttributeNode("b:b").getNamespaceURI())){
				fail(s);
			}
			doc = builder.parse(
				"<a xmlns='a' xmlns:b='b' xml:space='xyz'" +
				" xml:lang='en' a='a' >" +
				"<b:b b='b' b:b='c' /></a>\n");
			el = doc.getDocumentElement();
			assertEq("http://www.w3.org/XML/1998/namespace",
				el.getAttributeNode("xml:space").getNamespaceURI());
			assertEq("xyz", el.getAttribute("xml:space"));
			doc = builder.parse("<a xmlns='a' xmlns:b='b' xml:SPACE='preserve'"+
				" xml:LANG='en' a='a' >" +
				"<b:b b='b' b:b='c' /></a>\n");
			el = doc.getDocumentElement();
			assertEq("http://www.w3.org/XML/1998/namespace",
				el.getAttributeNode("xml:SPACE").getNamespaceURI());
			assertEq("http://www.w3.org/XML/1998/namespace",
				el.getAttributeNode("xml:LANG").getNamespaceURI());
			try {
				builder.parse("<a XMLNS='a' XMLNS:b='b' XML:space='preserve'" +
					" XML:lang='en' a='a' >" +
					"<b:b b='b' b:b='c' /></a>\n");
				fail("Error not thrown");
			} catch (Exception ex) {
				if (ex.getMessage().indexOf("XMLNS") < 0) {
					fail("" + ex);
				}
			}
			 //test builder
			builder = new KDOMBuilder();
			doc = builder.newDocument("a.b", "a", null);
			el = doc.getDocumentElement();
			if (!"a".equals(s = el.getNodeName())) {
				fail(s);
			}
			nm = el.getAttributes();
			if ((len = nm.getLength()) != 0) {
				fail("" + len);
			}
			el1 = doc.createElementNS("b.c", "x:b");
			el1.setAttributeNS("a.b", "y:b", "x");
			el.appendChild(el1);
			assertEq("x", el1.getAttribute("y:b"));
			n = el1.getAttributeNode("y:b");
			assertEq("a.b", n.getNamespaceURI());
			assertEq("y", n.getPrefix());
			nm = el1.getAttributes();
			assertEq(nm.getLength(), 1);
			el1.setAttribute("xml:spc", "preser");
			att = el1.getAttributeNode("xml:spc");
			assertEq("preser", att.getNodeValue());
			assertNull(att.getNamespaceURI());
			el1.removeAttribute("xml:spc");
			try {
				el1.setAttributeNS("a.b", "xml:space", "preserve");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			try {
				el1.setAttributeNS("http://www.w3.org/2000/xmlns/",
					"xml:space", "preserve");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			try {
				el1.setAttributeNS(null, "xml:space", "preserve");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			el1.setAttribute("xml:space", "preserve");
			att = el1.getAttributeNode("xml:space");
			assertEq("preserve", att.getNodeValue());
			assertNull(att.getNamespaceURI());
			KXmlUtils.setNecessaryXmlnsAttrs(el);
			assertEq("a.b", el.getAttribute("xmlns"));
			assertEq("b.c", el1.getAttribute("xmlns:x"));
			assertEq("a.b", el1.getAttribute("xmlns:y"));
			el1 = (Element) el1.cloneNode(false);
			assertEq("a.b", el.getAttribute("xmlns"));
			assertEq("b.c", el1.getAttribute("xmlns:x"));
			assertEq("a.b", el1.getAttribute("xmlns:y"));
			el = (Element) el.cloneNode(false);
			assertEq(el.getChildNodes().getLength(), 0);
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttributeNS("a.b", "xmlns:q", "c.d");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttribute("xmlns", "c.d");
			} catch (DOMException ex) {fail(ex);}
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttribute("xmlns:u", "c.d");
			} catch (DOMException ex) {fail(ex);}
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttributeNS("a.b", "xmlns", "c.d");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttributeNS(null, "xmlns", "c.d");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			try {
				el1 = doc.createElementNS(null, "a");
				el1.setAttributeNS("http://www.w3.org/2000/xmlns/",//NSURI_XMLNS
					"xmlns", "c.d");
			} catch (DOMException ex) {	fail(ex);}
			//test NodeList, NamedNodeList
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			doc = builder.parse("<a b='b' c='c'>x<b>y<c/>z<d/></b><e/></a>");
			el = doc.getDocumentElement();
			assertEq("a", el.getNodeName());
			el1 = doc.createElement("aa");
			att = el.getAttributeNode("b");
			try {
				el1.setAttributeNode(att);
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.INUSE_ATTRIBUTE_ERR) {
					fail("exception: " + ex);
				}
			}
			el.removeAttribute("q");
			el.removeAttributeNS("x", "y");
			el.setAttributeNS(null, "y", "z");
			el.removeAttributeNS(null, "y");
			try {
				el.setAttributeNS(null, "x:y", "z");
				el.removeAttributeNS(null, "y");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NAMESPACE_ERR) {
					fail("exception: " + ex);
				}
			}
			el.setAttributeNS("x", "x:y", "z");
			el.removeAttributeNS("x", "y");
			el.setAttributeNS("", "y", "z");
			el.removeAttributeNS("", "y");
			el.removeAttribute("y");
			nm = el.getAttributes();
			try {
				nm.removeNamedItem("q");
				fail("Error not thrown");
			} catch (DOMException ex) {
				if (ex.code != DOMException.NOT_FOUND_ERR) {
					fail("exception: " + ex);
				}
			}
			assertEq(nm.getLength(), 2);
			for (int i = 0; i < len; i++) {
				n = nm.item(i);
				if (i == 1) {
					if (n != null) {
						fail("not null: " + n);
					}
				} else {
					el.removeAttributeNode((Attr) n);
					el1.setAttributeNode((Attr) n);
				}
			}
			el2 = KXmlUtils.firstElementChild(el, "b");
			nl = el2.getChildNodes();
			assertEq((len = nl.getLength()), 4);
			for (int i = 0; i < len; i++) {
				n = nl.item(i);
				if (n == null) {
					assertTrue(i >= 2);
				} else {
					el1.appendChild(n);
				}
			}
			nl = el2.getChildNodes();
			assertEq(nl.getLength(), 2);
		} catch (Exception ex) {fail(ex);}
		try { //test attributes with same local name and nsURI
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.parse("<root atr=\"atr1\" xmlns=\"a\">\n"+
				"<child xmlns:u=\"t\" xmlns:v=\"t\"\n"+
				" childAtr1='atr1' u:childAtr2='atr3' v:childAtr2=\"atr2\"/>\n"+
				"</root>\n");
			fail("Error not thrown");
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("childAtr2") < 0) {
				fail(ex);
			}
		}
		try { //test include - href exists
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(true);
			doc = builder.parse(getDataDir() + "TestInclude01.xml");
			el = doc.getDocumentElement();
			el1 = KXmlUtils.firstElementChild(el);
			assertEq("a", el1.getNodeName());
			el1 = KXmlUtils.firstElementChild(el1);
			assertTrue(el1 != null && "b".equals(el1.getNodeName()),
				KXmlUtils.nodeToString(el));
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(true);
			doc = builder.parse(getDataDir() + "testInclude01_1.xml");
			el = doc.getDocumentElement();
			assertEq("a", el.getNodeName());
			nl = el.getChildNodes();
			assertTrue(nl != null && nl.getLength() == 1
				&&"\n  <b>txt</b>\n".equals(el.getFirstChild().getNodeValue()));
			//test include - href not exists (no fallback)
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(true);
			try {
				builder.parse(getDataDir() + "TestInclude02.xml");
				fail("Error not thrown");
			} catch (Exception ex) {
				s = ex.getMessage();
				if (s == null || !s.contains("XML404")) {
					fail(ex);
				}
			}
			//test include - href not exists (fallback element is prezent)
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(true);
			doc = builder.parse(getDataDir() + "TestInclude03.xml");
			el = doc.getDocumentElement();
			assertEq("file 'xxx.xxx' not exists",
				s = KXmlUtils.getTextValue(el).trim(), s);
			data =
"<!DOCTYPE Map [\n"+
"    <!ELEMENT Map ANY>\n"+
"    <!ELEMENT Style ANY>\n"+
"    <!ATTLIST Symbol name CDATA #IMPLIED>\n"+
"    <!ELEMENT Rule ANY>\n"+
"    <!ELEMENT Symbol ANY>\n"+
"    <!ATTLIST Symbol file CDATA #IMPLIED\n"+
"                     type CDATA #IMPLIED\n"+
"                     width CDATA #IMPLIED\n"+
"                     height CDATA #IMPLIED>\n"+
"    <!ELEMENT Icons (#PCDATA)*>\n"+
"    <!ENTITY home_dir '/home/user'>\n"+
"    <!ENTITY icons    '&home_dir;/map/icons'>\n"+
"]>\n"+
"<Map>\n"+
"  <Style name='volcanos'>\n"+
"    <Rule>\n"+
"      <Symbol file='&icons;/xyz.png' type='png' width='16' height='16'/>\n"+
"    </Rule>\n"+
"    <Icons>&icons;</Icons>\n"+
"  </Style>\n"+
"</Map>";
			builder = new KDOMBuilder();
			builder.setCoalescing(false);
			builder.setNamespaceAware(false);
			builder.setExpandEntityReferences(false);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(false);
			doc = builder.parse(data);
			s = doc.getElementsByTagName("Symbol").
				item(0).getAttributes().getNamedItem("file").getNodeValue();
			assertEq("/home/user/map/icons/xyz.png", s);
			s = KXmlUtils.getTextContent(
				doc.getElementsByTagName("Icons").item(0));
			assertEq("/home/user/map/icons", s);
			// empty DOCTYPE.
			builder = new KDOMBuilder();
			builder.parse("<!DOCTYPE root><root/>");
			// combination of external and internal DOCTYPE subset.
			builder = new KDOMBuilder();
			doc = builder.parse(getDataDir() + "TestDTD027.xml");
			s = doc.getDoctype().getInternalSubset().trim();
			assertEq("<!ATTLIST zamestnanec id CDATA #IMPLIED>", s);
			// combination of external and internal DOCTYPE subset.
			builder = new KDOMBuilder();
			doc = builder.parse(getDataDir() + "TestDTD027_1.xml");
			s = doc.getDocumentElement().getChildNodes().item(0).getNodeValue();
			assertEq("bbb", s);
			// expansion of entities and character references.
			data =
"<!DOCTYPE test [\n"+
"<!ELEMENT test (#PCDATA)* >\n"+
"<!ENTITY % xx '&#37;zz;'>\n"+
"<!ENTITY % zz '&#60;!ENTITY tricky \"error-prone\" >' >\n"+
"%xx;\n"+
"]>\n"+
"<test>This sample shows a &tricky; method.</test>\n";
			builder = new KDOMBuilder();
			builder.setCoalescing(true);
			builder.setNamespaceAware(false);
			builder.setExpandEntityReferences(true);
			builder.setValidating(false);
			builder.setIgnoringComments(false);
			builder.setXIncludeAware(false);
			doc = builder.parse(data);
			assertEq("This sample shows a error-prone method.",
				KXmlUtils.getTextContent(doc.getDocumentElement()));
			data =
"<!DOCTYPE test [\n"+
"<!ELEMENT test (#PCDATA)* >\n"+
"<!ENTITY example \"&#38;#38;, &#38;#38;#38;, &amp;amp;.\" >\n"+
"]>\n"+
"<test>&example;</test>\n";
			doc = builder.parse(data);
			assertEq("&, &#38;, &amp;.",
				KXmlUtils.getTextContent(doc.getDocumentElement()));
			data =
"<!DOCTYPE foo [\n"+
"<!ELEMENT foo (#PCDATA)* >\n"+
"<!ATTLIST foo attr CDATA #IMPLIED>\n"+
"<!ENTITY x \"&lt;\">\n"+
"]>\n"+
"<foo attr=\"&x;\"/>";
			doc = builder.parse(data);
			assertEq("<", doc.getDocumentElement().getAttribute("attr"));
			data =
"<!DOCTYPE foo [\n"+
"<!ELEMENT foo (#PCDATA)* >\n"+
"<!ENTITY x \"&lt;\">\n"+
"]>\n"+
"<foo>&x;</foo>";
			builder.setExpandEntityReferences(false);
			doc = builder.parse(data);
			n = doc.getDocumentElement().getChildNodes().item(0).
				getChildNodes().item(0);
			assertEq(n.getNodeType(), Node.TEXT_NODE, "NodeType: " + n);
			data =
"<!DOCTYPE foo [\n"+
"<!ELEMENT foo EMPTY >\n"+
"<!ATTLIST foo attr CDATA #IMPLIED>\n"+
"<!ENTITY x \"&lt;\">\n"+
"]>\n"+
"<foo attr = \"&x;\"/>"; // => entity content is four letters!
			builder.setExpandEntityReferences(false);
			doc = builder.parse(data);
			assertEq("<", doc.getDocumentElement().getAttribute("attr"));
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
