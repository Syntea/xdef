package org.xdef.impl.util.conv.xsd2xd.util;

import java.net.URL;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.sys.SUtils;

/** Provides general static methods for manipulating with Document Object Model
 * nodes.
 * @author Alexandrov
 */
public class DOMUtils {

	/** Private constructor. */
	private DOMUtils() {}

	/** Creates URL from given namespace.
	 * @param namespace namespace.
	 * @return URL representation of given namespace.
	 * @throws Exception if cannot create URL from path.
	 */
	public static URL getURLFromNamespace(final String namespace)
		throws Exception {
		return SUtils.getExtendedURL(namespace);
	}

	/** Gets default namespace URI of given schema or empty string.
	 * @param schemaElement schema element.
	 * @return default namespace URI or empty string.
	 */
	public static String getDefaultNamespaceURI(Element schemaElement) {
		return schemaElement.getAttribute("xmlns");
	}

	/** Returns <tt>true</tt> if given node is element, has name as given name
	 * and is in given namespace.
	 * @param node node.
	 * @param nsURI namespace URI
	 * @param name local name.
	 * @return <tt>true</tt> if given node is element, has name as given
	 * name and is in given namespace.
	 */
	public static boolean isElement(Node node, String nsURI, String name) {
		String nodeFullName = node.getNodeName();
		String localName;
		int ndx;
		if ((ndx = nodeFullName.indexOf(":")) != -1 ) {
			localName = nodeFullName.substring(ndx + 1);
		} else {
			localName = nodeFullName;
		}
		return (Node.ELEMENT_NODE == node.getNodeType() &&
			localName.equals(name) &&
			node.getNamespaceURI().equals(nsURI));
	}

	/** Gets ancestor element with given name and in given namespace of given
	 * node.
	 * @param context node.
	 * @param nsURI namespace URI.
	 * @param name local name.
	 * @throws IllegalArgumentException node is not descendant of element with
	 * given parameters
	 * @return element.
	 */
	public static Element getElement(Node context, String nsURI, String name)
		throws IllegalArgumentException {
		if (isElement(context, nsURI, name)) {
			return (Element) context;
		}
		if (context.getParentNode() != null) {
			return getElement(context.getParentNode(), nsURI, name);
		}
		throw new IllegalArgumentException("Given node is not a descendant " +
			"of element with given namespace URI and local name");
	}

	/** Returns <tt>true</tt> if given node is child of element with given
	 * namespace URI and given local name.
	 * @param node      node.
	 * @param nsURI     namespace URI of parent element.
	 * @param localName local name of parent element.
	 * @return          <tt>true</tt> if given node is child of element with
	 *                  given namespace URI and given local name.
	 */
	public static boolean isChild(Node node, String nsURI, String localName) {
		Node parent;
		return (parent = node.getParentNode()) != null &&
			parent.getNodeType() == Node.ELEMENT_NODE &&
			parent.getNamespaceURI().equals(nsURI) &&
			parent.getLocalName().equals(localName);
	}

	/** Gets prefix of given namespace URI in given element or <tt>null</tt>.
	 * @param element element node.
	 * @param namespaceURI namesapce URI.
	 * @return prefix of namespace URI declaration or <tt>null</tt>.
	 */
	public static String getNamespaceDeclarationPrefix(Element element,
		String namespaceURI) {
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr attr = (Attr) attrs.item(i);
			String fullName = attr.getName();
			String prefix;
			int ndx;
			if ((ndx = fullName.indexOf(":")) != -1) {
				prefix = fullName.substring(0, ndx);
			} else {
				prefix = "";
			}
			if ("xmlns".equals(prefix) && namespaceURI.equals(attr.getValue())){
				return fullName.substring(ndx + 1);
			}
		}
		return null;
	}

	/** Adds namespace declaration with given prefix nad given namespace URI to
	 * the element node and returns <tt>true</tt>. If namespace declaration with
	 * given prefix already exists in given element node returns <tt>false</tt>.
	 * @param element element node to add namespace declaration.
	 * @param prefix prefix of nemaspace declaration.
	 * @param namespaceURI namespace URI of namespace declaration.
	 * @return <tt>true</tt> if namespace declaration was created.
	 */
	public static boolean addNamespaceDeclaration(Element element,
		String prefix,
		String namespaceURI) {
		String name = "xmlns:" + prefix;
		if (element.hasAttribute(name)) {
			return false;
		}
		element.setAttribute(name, namespaceURI);
		return true;
	}

	/** Adds namespace declaration with given namespace URI to given element
	 * node and returns prefix for that namespace declaration.
	 * @param element element to add namespace declaration.
	 * @param namespaceURI namespace URI of declaration.
	 * @return namespace prefix.
	 * @throws RuntimeException if cannot add namespace declaration.
	 */
	public static String addNamespaceDeclaration(Element element,
		String namespaceURI) throws RuntimeException {
		final String prefixes = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < prefixes.length(); i++) {
			String prefix = prefixes.substring(i, i + 1);
			if (addNamespaceDeclaration(element, prefix, namespaceURI)) {
				return prefix;
			}
		}
		for (int i = 0; i < prefixes.length(); i++) {
			for (int j = 0; j < prefixes.length(); j++) {
				String prefix = prefixes.substring(i, i + 1) +
					prefixes.substring(j, j + 1);
				if (addNamespaceDeclaration(element, prefix, namespaceURI)) {
					return prefix;
				}
			}
		}
		throw new RuntimeException("Could not add namespace declaration to " +
			"given element node");
	}
}