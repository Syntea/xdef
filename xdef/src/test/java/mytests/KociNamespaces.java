package mytests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.impl.xml.KNamespace;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;

public class KociNamespaces {

	public static Object eval(final Node node, final String expr, final NamespaceContext nc) {
		try {
			XPath xp = XPathFactory.newInstance().newXPath();
			xp.setNamespaceContext(nc);
			return (NodeList) xp.compile(expr).evaluate(node, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String[] args) {
		String namespace = "http://example.org";
		String xml = "<a xmlns=\"" + namespace + "\"><b><c/></b></a>";
		Document document = KXmlUtils.parseXml(xml);
		CustomNamespaceContext cns = new CustomNamespaceContext();
		cns.addNamespace("ns", namespace);
		NodeList list;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("=========== NO SAXON============");
		// najde se jenom b
		list = (NodeList) eval(document.getDocumentElement(), "ns:a | ns:b | ns:c", cns);
		System.out.println("1. " + printElements(list));
		list = (NodeList) eval(document, "ns:a | ns:b | ns:c", cns);
		System.out.println("1. " + printElements(list));

		// najde se a, b
		list = (NodeList) eval(document.getDocumentElement(), "/ns:a | ns:b | ns:c", cns);
		System.out.println("2. " + printElements(list));
		list = (NodeList) eval(document, "/ns:a | ns:b | ns:c", cns);
		System.out.println("2. " + printElements(list));

		// najde se a, b, c
		list = (NodeList) eval(document.getDocumentElement(), "/ns:a | ns:b | //ns:c", cns);
		System.out.println("3. " + printElements(list));
		list = (NodeList) eval(document, "/ns:a | ns:b | //ns:c", cns);
		System.out.println("3. " + printElements(list));

		cns = new CustomNamespaceContext();
		cns.addNamespace("", namespace);
		// najde se a, b, c
		list = (NodeList) eval(document.getDocumentElement(), "/:a | :b | //:c", cns);
		System.out.println("4. " + printElements(list));
		list = (NodeList) eval(document, "/:a | :b | //:c", cns);
		System.out.println("4. " + printElements(list));

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("=========== SAXON============");

		KNamespace kns = new KNamespace();
		kns.setPrefix("ns", namespace);

		// najde se jenom b
		list = (NodeList) KXpathExpr.evaluate(document.getDocumentElement(), "ns:a | ns:b | ns:c", kns);
		System.out.println("1. " + printElements(list));
		list = (NodeList) KXpathExpr.evaluate(document, "ns:a | ns:b | ns:c", kns);
		System.out.println("1. " + printElements(list));

		// najde se a, b
		list = (NodeList) KXpathExpr.evaluate(document.getDocumentElement(), "/ns:a | ns:b | ns:c", kns);
		System.out.println("2. " + printElements(list));
		list = (NodeList) KXpathExpr.evaluate(document, "/ns:a | ns:b | ns:c", kns);
		System.out.println("2. " + printElements(list));

		// najde se a, b, c
		list = (NodeList) KXpathExpr.evaluate(document.getDocumentElement(), "/ns:a | ns:b | //ns:c", kns);
		System.out.println("3. " + printElements(list));
		list = (NodeList) KXpathExpr.evaluate(document, "/ns:a | ns:b | //ns:c", kns);
		System.out.println("3. " + printElements(list));
		try {
			// vychozi namespace funguje pouze s dvojteckami
			kns = new KNamespace();
			kns.setPrefix("", namespace);

			list = (NodeList) KXpathExpr.evaluate(document.getDocumentElement(), "/:a | :b | //:c", kns);
			System.out.println("4. " + printElements(list));
			list = (NodeList) KXpathExpr.evaluate(document, "/:a | :b | //:c", kns);
			System.out.println("4. " + printElements(list));
		} catch (SRuntimeException ex) {System.out.println("4. " + ex);}
		try {
			Object o = KXpathExpr.evaluate(document, XPathConstants.NUMBER, "/*", kns, null, null);
			System.out.println("5. " + o);
		} catch (Exception ex) {System.out.println("5. " + ex);}
		try {
			Object o = KXpathExpr.evaluate(document, XPathConstants.NUMBER, "/*", kns, null, null);
			System.out.println("6. " + o);
		} catch (Exception ex) {System.out.println("6. " + ex);}
		try {
			XPath xp = XPathFactory.newInstance().newXPath();
			kns = new KNamespace();
			kns.setPrefix("ns", namespace);
			xp.setNamespaceContext(kns);
			Object o = xp.compile("//*").evaluate(document.getDocumentElement(), XPathConstants.NUMBER);
			System.out.println("7. " + o);
		} catch (XPathExpressionException | RuntimeException ex) {System.out.println("7. " + ex);}
	}

	static String printElements(NodeList list) {
		String nodes = "";
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (nodes.length() > 0) {
				nodes += ", ";
			}
			nodes += node.getNodeName();
		}
		return nodes;
	}

	private static class CustomNamespaceContext implements NamespaceContext {
		private final Map<String, String> prefixToNs = new HashMap<>();
		private final Map<String, String> nsToPrefix = new HashMap<>();

		public CustomNamespaceContext() {}

		public void addNamespace(String prefix, String namespaceURI) {
			prefixToNs.put(prefix, namespaceURI);
			nsToPrefix.put(namespaceURI, prefix);
		}

		// implementation of javax.xml.namespace.NamespaceContext
		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) throw new IllegalArgumentException("Prefix cannot be null");
			return prefixToNs.getOrDefault(prefix, "");
		}
		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI == null) throw new IllegalArgumentException("Namespace URI cannot be null");
			return nsToPrefix.get(namespaceURI);
		}
		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {return prefixToNs.keySet().iterator();}
	}
}