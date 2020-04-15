package org.xdef.xml;

import org.xdef.impl.xml.KNodeList;
import org.xdef.impl.xml.KNamespace;
import org.xdef.impl.xml.KNamedNodeMap;
import org.xdef.impl.xml.KEmptyNodeList;
import org.xdef.impl.xml.KEmptyNamedNodeMap;
import org.xdef.msg.XML;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import javax.xml.namespace.NamespaceContext;

/** Collection of static methods extending methods from org.w3c.dom interfaces.
 * @author Vaclav Trojan
 */
public class KDOMUtils {

////////////////////////////////////////////////////////////////////////////////
// Extension of org.w3c.dom interface *
////////////////////////////////////////////////////////////////////////////////

	private final static NodeList EMPTYNODELIST = new KEmptyNodeList();

	private final static NamedNodeMap EMPTYENTITIES = new KEmptyNamedNodeMap();

	/** Don't allow user to instantiate this class. */
	protected KDOMUtils() {}

	/** Get entities associated with a node.
	 * @param node Object from which entities are returned.
	 * @return entities associated with a node.
	 */
	public static final NamedNodeMap getEntities(final Node node) {
		if (node == null) {
			return EMPTYENTITIES;
		}
		Document doc;
		if (node.getNodeType() == Node.DOCUMENT_NODE){
			doc = (Document) node;
		} else {
			if ((doc = node.getOwnerDocument()) == null) {
				return EMPTYENTITIES;
			}
		}
		final DocumentType dt = doc.getDoctype();
		if (dt == null) {
			return EMPTYENTITIES;
		}
		NamedNodeMap result = dt.getEntities();
		return result == null ? EMPTYENTITIES : result;
	}

	/** Get notations associated with a node.
	 * @param node node which notations are returned.
	 * @return notations associated with a node.
	 */
	public static final NamedNodeMap getNotations(final Node node) {
		if (node == null) {
			return EMPTYENTITIES;
		}
		Document doc;
		if (node.getNodeType() == Node.DOCUMENT_NODE){
			doc = (Document) node;
		} else {
			if ((doc = node.getOwnerDocument()) == null) {
				return EMPTYENTITIES;
			}
		}
		final DocumentType dt = doc.getDoctype();
		if (dt == null) {
			return EMPTYENTITIES;
		}
		final NamedNodeMap result = dt.getNotations();
		return result == null ? EMPTYENTITIES : result;
	}

	/** Get all child Elements.
	 * @param node node which child nodes are inspected for occurrence
	 * of Elements.
	 * @return NodeList with Elements found.
	 */
	public static final NodeList getChildElements(final Node node) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				result.addItem(item);
			}
		}
		return result;
	}

	/** Remove all child nodes.
	 * @param node node which children will be removed.
	 */
	public static final void removeChildNodes(final Node node) {
		Node n;
		while ((n = node.getFirstChild()) != null) {
			node.removeChild(n);
		}
	}

	/** Trim all text nodes and remove empty text nodes.
	 * @param node node which blank text nodes will be removed.
	 * @param deep if true this method will be applied also to child elements.
	 */
	public static final void trimTextNodes(final Node node,
		boolean deep) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap nnm = node.getAttributes();
			for (int i = 0; nnm != null && i < nnm.getLength(); i++) {
				Node n = nnm.item(i);
				String s = n.getNodeValue();
				if (s != null) {
					n.setNodeValue(s.trim());
				}
			}
		}
		for (Node n = node.getLastChild(); n != null;){
			if (n.getNodeType() == Node.TEXT_NODE ||
				n.getNodeType() == Node.CDATA_SECTION_NODE) {
				StringBuilder sb = new StringBuilder();
				for (;;) {
					String s = n.getNodeValue();
					if (s != null && (s = s.trim()).length() > 0) {
						sb.insert(0, s);
					}
					Node n1 = n.getPreviousSibling();
					if (n1 == null || n1.getNodeType() != Node.TEXT_NODE
						&& n1.getNodeType() != Node.CDATA_SECTION_NODE) {
						if (sb.length() > 0) {
							Node n2 = node.getOwnerDocument().createTextNode(""+sb);
							node.replaceChild(n2, n);
						} else {
							node.removeChild(n);
						}
						n = n1;
						break;
					} else {
						node.removeChild(n);
						n = n1;
					}
				}
			} else {
				if (deep && n!= null && n.getNodeType() == Node.ELEMENT_NODE) {
					trimTextNodes(n, deep);
				}
				n = n.getPreviousSibling();
			}
		}
	}

	/** Get all attributes from the node with given namespace.
	 * @param node node to be inspected.
	 * @param ns required namespace.
	 * @return NamedNodeMap with attributes with namespace from argument.
	 */
	public static final NamedNodeMap getAttributesNS(final Node node,
		final String ns) {
		NamedNodeMap nnm = node.getAttributes();
		if (nnm == null) {
			return new KNamedNodeMap();
		}
		nnm = new KNamedNodeMap(nnm);
		// remove all other nodes.
		for (int i = nnm.getLength() - 1; i >= 0; i--) {
			final Node item = nnm.item(i);
			final String u = item.getNamespaceURI();
			if (u == null) {
				if (ns != null) {
					nnm.removeNamedItem(item.getNodeName());
				}
			} else {
				if (!u.equals(ns)) {
					String localName = item.getLocalName();
					if (localName == null) {
						localName = item.getNodeName();
					}
					nnm.removeNamedItemNS(u, localName);
				}
			}
		}
		return nnm;
	}

	/** Get all child nodes frmo the node which are Elements with the name
	 * equal to the argument tagName.
	 * @param node node which children are inspected for Elements
	 * with given tag name.
	 * @param tagName required tag name.
	 * @return NodeList with Elements found.
	 */
	public static final NodeList getChildElements(final Node node,
		final String tagName) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (tagName.equals(item.getNodeName())){
				result.addItem(item);
			}
		}
		return result;
	}

	/** Get all child node which are Elements and have the NameSpace URI
	 * equal to the argument ns.
	 * @param ns NameSpace URI.
	 * @param node node which children are inspected for Elements
	 * with given NameSpace URI.
	 * @return NodeList with Elements found.
	 */
	public static final NodeList getChildElementsNS(final Node node,
		final String ns) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final String uri = ns == null ? "" : ns;
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				String u;
				if (uri.equals((u = item.getNamespaceURI()) == null ? "" : u)) {
					result.addItem(item);
				}
			}
		}
		return result;
	}

	/** Get all child nodes which are Elements with the NameSpace equal to
	 * argument ns and the local name equal to the argument localName.
	 * URI and local name equal to values from arguments.
	 * @param ns NameSpace URI.
	 * @param localName required local name of Element.
	 * @param node node which child nodes are inspected for occurrence of
	 * Element with URI and local name equal to arguments above
	 * @return Element found or <tt>null</tt>.
	 */
	public static final NodeList getChildElementsNS(final Node node,
		final String ns,
		final String localName) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		final String uri = ns == null ? "" : ns;
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			String lname = item.getLocalName();
			if (lname == null) {
				lname = item.getNodeName();
			}
			if (removePrefix(localName).equals(lname)) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					result.addItem(item);
				}
			}
		}
		return result;
	}

	/** Get all child nodes which are Elements with the tag name equal
	 * to one of names from the list from the argument tagnameList.
	 * @param node node which children are inspected.
	 * @param tagnameList array with list of required tag names.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final NodeList getChildElements(final Node node,
		final String[] tagnameList) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final String name = item.getNodeName();
			for (int j = tagnameList.length - 1; j >= 0; j--) {
				if (name.equals(tagnameList[j])) {
					result.addItem(item);
				}
			}
		}
		return result;
	}

	/** Get the first child node which is an Element with NameSpace URI
	 * equal to argument nsURI and a local name equal to one of names from
	 * argument with array of names frm the argument localnameList.
	 * @param ns NameSpace URI of an Element to be found.
	 * @param localnameList array with list of local names.
	 * @param node children of this node are inspected for occurrence of
	 * an Element requiring to arguments.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final NodeList getChildElementsNS(final Node node,
		String ns,
		final String[] localnameList) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final String uri = ns == null ? "" : ns;
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String localName = item.getLocalName();
			if (localName == null) {
				localName = item.getNodeName();
			}
			final String u = item.getNamespaceURI();
			if (uri.equals(u == null ? "" : u)) {
				for (int j = localnameList.length - 1; j >= 0; j--) {
					if (localName.equals(removePrefix(localnameList[j]))) {
						result.addItem(item);
					}
				}
			}
		}
		return result;
	}

	/** Get the first child node which is an Element.
	 * @param node node which child nodes are inspected for Element occurrence.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChild(final Node node) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return null;
		}
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) item;
			}
		}
		return null;
	}

	/** Get the first child node which is an Element with the tag name equal to
	 * the argument tagName.
	 * @param node a node of which child nodes are inspected for the first
	 * occurrence of an Element with with name equal to argument.
	 * @param tagName required tag name.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChild(final Node node,
		final String tagName) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return null;
		}
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (tagName.equals(item.getNodeName())) {
				return (Element) item;
			}
		}
		return null;
	}

	/** Get the first child node which is an Element with the tag name equal
	 * to one of names from the list from the argument tagnameList.
	 * @param node children of this node are inspected for first occurrence of
	 * an Element requiring to argument.
	 * @param tagnameList array with list of tag names
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChild(final Node node,
		final String[] tagnameList) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return null;
		}
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final String name = item.getNodeName();
			for (int j = tagnameList.length - 1; j >= 0; j--) {
				if (name.equals(tagnameList[j])) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Get the first child node which is an Element with the NameSpace URI
	 * equal to the argument ns.
	 * @param node a node of which child nodes are inspected for the first
	 * occurrence of an Element with URI from argument.
	 * @param ns required NameSpace URI.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChildNS(final Node node,
		final String ns){
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return null;
		}
		final String uri = ns == null ? "" : ns;
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				final String u =item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Get the first child node which is an Element with the NameSpace
	 * URI and local name equal to values from arguments ns and localName.
	 * @param node a node which child nodes are inspected for first occurrence
	 * of an Element with URI and local name equal to arguments nsURI and
	 * local name.
	 * @param ns required NameSpace URI.
	 * @param localName required local name.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChildNS(final Node node,
		final String ns,
		final String localName) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return null;
		}
		final String uri = ns == null ? "" : ns;
		for (int i = 0; i < len; i++) {
			Node item = nl.item(i);
			String lname = item.getLocalName();
			if (lname == null) {
				lname = item.getNodeName();
			}
			if (removePrefix(localName).equals(lname)) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Remove prefix from name.
	 * @param qname name with prefix.
	 * @return name without prefix.
	 */
	private static String removePrefix(final String qname) {
		final int ndx = qname.indexOf(':');
		return (ndx <= 0) ? qname : qname.substring(ndx+1);
	}

	/** Get the first child node which is an Element with NameSpace URI
	 * equal to argument nsURI and a local name equal to one of names from
	 * the argument localnameList.
	 * @param ns namespace of Element to be found.
	 * @param localnameList array with list of local names.
	 * @param node children of this node are inspected for first occurrence of
	 * an Element requiring to arguments.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element firstElementChildNS(final Node node,
		final String ns,
		final String[] localnameList) {
		NodeList nl;
		int len;
		if (node == null || (len=(nl=node.getChildNodes()).getLength()) == 0) {
			return null;
		}
		String uri = ns == null ? "" : ns;
		for (int i = 0; i < len; i++) {
			Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String localName = item.getLocalName();
			if (localName == null) {
				localName = item.getNodeName();
			}
			String u;
			if (uri.equals((u = item.getNamespaceURI()) == null ? "" : u)) {
				for (int j = localnameList.length - 1; j >= 0; j--) {
					if (localName.equals(removePrefix(localnameList[j]))) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Get the first next node which is an Element.
	 * @param node actual node of wnich next sibling Element is returned.
	 * @return Element found or <tt>null</tt>.
	 */
	public static Element nextElementSibling(final Node node) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getNextSibling()) != null &&
			item.getNodeType() != Node.ELEMENT_NODE){}
		return (Element) item;
	}

	/** Return the first next node which is an Element type with given NameSpace
	 * URI frm the argument ns.
	 * @param node actual node of which next sibling Element is returned.
	 * @param ns namespace of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element nextElementSiblingNS(final Node node,
		final String ns) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getNextSibling()) != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Return the first next node which is an Element type with given name.
	 * @param node actual node of wnich next Element is returned.
	 * @param tagName tag name of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element nextElementSibling(final Node node,
		final String tagName) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getNextSibling()) != null
			&& !tagName.equals(item.getNodeName())) {}
		return (Element) item;
	}

	/** Return the first next node which is an Element type with name equal
	 * to the NameSpace URI and the name equal to arguments.
	 * @param node actual node of which next Element is returned.
	 * @param localname local name of Element to be found.
	 * @param ns namespace of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element nextElementSiblingNS(final Node node,
		final String ns,
		final String localname) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getNextSibling()) != null) {
			String u;
			if (item.getNodeType() == Node.ELEMENT_NODE &&
				removePrefix(localname).equals(item.getNodeName()) &&
				uri.equals((u=item.getNamespaceURI()) == null ? "" : u)) {
				return (Element) item;
			}
		}
		return (Element) item;
	}

	/** Return the first next node which is an Element type with tag name
	 * equal to one of names from the argument tagnameList.
	 * @param node actual node of which next Element is returned.
	 * @param tagnameList array with tag names.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element nextElementSibling(final Node node,
		final String[] tagnameList) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getNextSibling()) != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				for (int i = 0; i < tagnameList.length; i++) {
					if (item.getNodeName().equals(tagnameList[i])) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Return the first next node which is an Element type with NameSpace URI
	 * equal to argument and local name equal to one of names from the
	 * argument nameList.
	 * @param node actual node of which next Element is returned.
	 * @param ns namespace of Element to be found.
	 * @param nameList array with local names.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element nextElementSiblingNS(final Node node,
		final String ns,
		final String[] nameList) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getNextSibling()) != null) {
			final String u = item.getNamespaceURI();
			if (uri.equals(u == null ? "" : u)) {
				for (int i = 0; i < nameList.length; i++) {
					String localName = item.getLocalName();
					if (localName == null) {
						localName = item.getNodeName();
					}
					if (removePrefix(nameList[i]).equals(localName)) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Get the last child node which is an Element.
	 * @param node actual node of which last Element is returned.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element lastElementChild(final Node node) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		for (i--; i >= 0; i--) {
			Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) item;
			}
		}
		return null;
	}

	/** Get the last child node which is an Element with the tag name equal to
	 * the argument tagName.
	 * @param node actual node of which last Element is returned.
	 * @param tagName tag name of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element lastElementChild(final Node node,
		final String tagName) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		for (i--; i >= 0; i--) {
			Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE &&
				tagName.equals(item.getNodeName())) {
				return (Element) item;
			}
		}
		return null;
	}

	/** Get the last child node which is an Element with the NameSpace URI
	 * equal to the argument ns.
	 * @param node actual node of which last Element is returned.
	 * @param ns namespace of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static Element lastElementChildNS(final Node node,
		final String ns) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		final String uri = ns == null ? "" : ns;
		for (i--; i >= 0; i--) {
			final Node item = nl.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Get the last child node which is an Element with the NameSpace
	 * URI and local name equal to values from arguments.
	 * @param node actual node of which last Element is returned.
	 * @param ns namespace of Element to be found.
	 * @param lname local name of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static Element lastElementChildNS(final Node node,
		final String ns,
		final String lname) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		final String uri = ns == null ? "" : ns;
		for (i--; i >= 0; i--) {
			final Node item = nl.item(i);
			String localName = item.getLocalName();
			if (localName == null) {
				localName = item.getNodeName();
			}
			if (removePrefix(lname).equals(localName)) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Get the last child node which is an Element with the tag name equal
	 * to one of names from the argument tagnameList.
	 * @param tagnameList array with list of names.
	 * @param node actual node of which last Element is returned.
	 * @return Element found or <tt>null</tt>.
	 */
	public static Element lastElementChild(final Node node,
		final String[] tagnameList) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		for (i--; i >= 0; i--) {
			final Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String name = item.getNodeName();
			for (int j = tagnameList.length - 1; j >= 0; j--) {
				if (name.equals(tagnameList[j])) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Get the last child node which is an Element with NameSpace URI
	 * equal to given argument and a local name equal to one of names from
	 * the argument nameList.
	 * @param ns namespace of Element to be found.
	 * @param nameList array with list of local names.
	 * @param node actual node of which last Element is returned.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element lastElementChildNS(final Node node,
		final String ns,
		final String[] nameList) {
		final NodeList nl = node.getChildNodes();
		int i = nl.getLength();
		if (node == null || i == 0) {
			return null;
		}
		final String uri = ns == null ? "" : ns;
		for (i--; i >= 0; i--) {
			final Node item = nl.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final String u = item.getNamespaceURI();
			if (uri.equals(u == null ? "" : u)) {
				for (int j = nameList.length - 1; j >= 0; j--) {
					String localName = item.getLocalName();
					if (localName == null) {
						localName = item.getNodeName();
					}
					if (localName.equals(removePrefix(nameList[j]))) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Get the first previous node which is an Element.
	 * @param node actual node from which previous Element is returned.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSibling(final Node node) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getPreviousSibling()) != null &&
			item.getNodeType() != Node.ELEMENT_NODE){}
		return (Element) item;
	}

	/** Return the first previous node which is an Element type with given
	 * NameSpace URI from the qrument ns.
	 * @param node actual node from which previous Element is returned.
	 * @param ns namespace of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSiblingNS(final Node node,
		final String ns) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getPreviousSibling()) != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				final String u = item.getNamespaceURI();
				if (uri.equals(u == null ? "" : u)) {
					return (Element) item;
				}
			}
		}
		return null;
	}

	/** Return the first previous node which is an Element type with given name.
	 * @param node actual node from which previous Element is returned.
	 * @param tagName tag name of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSibling(final Node node,
		final String tagName) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getPreviousSibling()) != null &&
			!tagName.equals(item.getNodeName())) {}
		return (Element) item;
	}

	/** Return the first previous node which is an Element type with name equal
	 * to the NameSpace URI and the name equal to arguments.
	 * @param node actual node from which previous Element is returned.
	 * @param localname local name of Element to be found.
	 * @param ns namespace of Element to be found.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSiblingNS(final Node node,
		final String ns,
		final String localname) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getPreviousSibling()) != null) {
			String u;
			if (item.getNodeType() == Node.ELEMENT_NODE &&
				removePrefix(localname).equals(item.getNodeName()) &&
				uri.equals((u=item.getNamespaceURI()) == null ? "" : u)) {
				return (Element) item;
			}
		}
		return (Element) item;
	}

	/** Return the previous next node which is an Element type with tag name
	 * equal to one of names from the list.
	 * @param node actual node from which previous Element is returned.
	 * @param tagnameList array with tag names.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSibling(final Node node,
		final String[] tagnameList) {
		if (node == null) {
			return null;
		}
		Node item = node;
		while ((item = item.getPreviousSibling()) != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				for (int i = 0; i < tagnameList.length; i++) {
					if (item.getNodeName().equals(tagnameList[i])) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Return the first previous node which is an Element type with NameSpace
	 * URI equal to argument and local name equal to one of names from the list.
	 * @param node actual node from which previous Element is returned.
	 * @param ns NameSpace of Element to be found.
	 * @param nameList array with local names.
	 * @return Element found or <tt>null</tt>.
	 */
	public static final Element previousElementSiblingNS(final Node node,
		final String ns,
		final String[] nameList) {
		if (node == null) {
			return null;
		}
		Node item = node;
		final String uri = ns == null ? "" : ns;
		while ((item = item.getNextSibling()) != null) {
			final String u = item.getNamespaceURI();
			if (uri.equals(u == null ? "" : u)) {
				for (int i = 0; i < nameList.length; i++) {
					String localName = item.getLocalName();
					if (localName == null) {
						localName = item.getNodeName();
					}
					if (localName.equals(removePrefix(nameList[i]))) {
						return (Element) item;
					}
				}
			}
		}
		return null;
	}

	/** Set necessary xmlns attributes to this element and all child elements.
	 * @param el element to which xmlns attributes will be added.
	 */
	public static void setNecessaryXmlnsAttrs(final Element el) {
		if (el == null) {
			return;
		}
		setNecessaryXmlnsAttrs(el, new KNamespace());
	}

	/** Set necessary XMLns attributes to this element and all child elements.
	 * @param context actual NameSpace context.
	 * @param el element to which XMLns attributes will be added.
	 */
	private static void setNecessaryXmlnsAttrs(final Element el,
		final KNamespace context) {
		String ns = el.getNamespaceURI();
		if (ns != null) {
			String qname;
			int ndx;
			String prefix =
				(ndx = (qname = el.getNodeName()).indexOf(':')) > 0 ?
					qname.substring(0, ndx) : "";
			if (!prefix.equals(context.getPrefix(ns))) {
				context.setPrefix(prefix, ns);
			}
		}
		final NamedNodeMap nm = el.getAttributes();
		int len;
		if (nm != null && (len = nm.getLength()) > 0) {
			for (int i = 0; i < len; i++) {
				final Node n = nm.item(i);
				if ((ns = n.getNamespaceURI()) != null) {
					final String name = n.getNodeName();
					final int ndx = name.indexOf(':');
					String prefix;
					if (ndx > 0) {
						prefix = name.substring(0, ndx);
						if ("xmlns".equals(prefix)) {
							continue;
						}
					} else {
						if ("xmlns".equals(name)) {
							continue;
						}
						prefix = "";
					}
					if (!ns.equals(context.getNamespaceURI(prefix))) {
						context.setPrefix(prefix, ns);
					}
				}
			}
		}
		final String[] prefixes = context.getRecentPrefixes();
		if (prefixes != null && (len = prefixes.length) > 0) {
			for (int i = 0; i < len; i++) {
				final String prefix = prefixes[i];
				final String attname = prefix.length() > 0
					? "xmlns:"+prefix : "xmlns";
				el.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
					attname, context.getNamespaceURI(prefix));
			}
		}
		final NodeList nl = el.getChildNodes();
		if ((len = nl.getLength()) > 0) {
			for (int i = 0; i < len; i++) {
				final Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					context.pushContext();
					setNecessaryXmlnsAttrs((Element) n, context);
					context.popContext();
				}
			}
		}
	}

	/** Return clone of an element and all attributes and child nodes with
	 * changed given NameSpace URI to the new value.
	 * @param elem source element.
	 * @param oldns old NameSpace URI (may be null, NOT an empty string!).
	 * @param newns new NameSpace (may be null, NOT an empty string!).
	 * @return clone of element with changed NameSpace.
	 */
	public static final Element cloneWithChangedNamespace(final Element elem,
		final String oldns,
		final String newns) {
		if (oldns == null) {
			if (newns == null) {
				return (Element) elem.cloneNode(true);
			}
		} else if (oldns.equals(newns)) {
			return (Element) elem.cloneNode(true);
		}
		final Document doc = elem.getOwnerDocument();
		final Element result = changeNamespace(doc, elem, oldns, newns);
		setNecessaryXmlnsAttrs(result);
		return result;
	}

	/** Return clone of an element and all attributes and child nodes with
	 * changed given NameSpace URI to the new value.
	 * @param doc owner document.
	 * @param elem source element.
	 * @param newelem new element.
	 * @param oldns old NameSpace URI (may be null, NOT an empty string!).
	 * @param newns new NameSpace (may be null, NOT an empty string!).
	 * @return clone of element with changed NameSpace.
	 */
	private static Element changeNamespace(final Document doc,
		final Element elem,
		final String oldns,
		final String newns) {
		Element newelem;
		String ns;
		if ((ns = elem.getNamespaceURI()) == null) {
			if (oldns == null) {
				if (newns != null) {
					newelem = doc.createElementNS(newns, elem.getNodeName());
				} else {
					newelem = doc.createElement(elem.getNodeName());
				}
			} else {
				newelem = doc.createElement(elem.getNodeName());
			}
		} else {
			if (ns.equals(oldns)) {
				if (newns != null) {
					newelem = doc.createElementNS(newns, elem.getNodeName());
				} else {
					newelem = doc.createElement(elem.getNodeName());
				}
			} else {
				newelem = doc.createElementNS(ns, elem.getNodeName());
			}
		}
		NamedNodeMap nm;
		int len;
		if ((nm = elem.getAttributes()) != null && (len = nm.getLength()) > 0) {
			for (int i = 0; i < len; i++) {
				final Node n = nm.item(i);
				final String qname = n.getNodeName();
				//skip xmlns attributes
				if (!qname.startsWith("xmlns")) {
					//change ns if it is required
					if (qname.indexOf(':') >= 0) {
						if ((ns = n.getNamespaceURI()) != null &&
							ns.equals(oldns) && newns != null) {
							ns = newns;
						}
						if (ns != null) {
							newelem.setAttributeNS(
								ns, qname, n.getNodeValue());
						} else {
							//error: qualified name without NameSpace
							newelem.setAttribute(qname, n.getNodeValue());
						}
					} else {
						newelem.setAttribute(qname, n.getNodeValue());
					}
				}
			}
		}
		final NodeList nl = elem.getChildNodes();
		if ((len = nl.getLength()) > 0) {
			for (int i = 0; i < len; i++) {
				final Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					newelem.appendChild(
						changeNamespace(doc, (Element) n, oldns, newns));
				} else {
					newelem.appendChild(n.cloneNode(true));
				}
			}
		}
		return newelem;
	}

	/** Get concatenated text from all child nodes (see
	 * org.w3c.dom.Node.getTesxtcontent() DOM Level 3).
	 * @param node node from which text content is collected.
	 * @return String with text concatenated from all child nodes or
	 * the empty string.
	 */
	public static final String getTextContent(final Node node) {
		final StringBuilder sb = new StringBuilder();
		getTextContent(node, sb);
		return sb.toString();
	}

	private static void getTextContent(final Node node,
		final StringBuilder sb) {
		Node item;
		if ((item = node) == null) {
			return;
		}
		switch (item.getNodeType()) {
			case Node.DOCUMENT_NODE:
				getTextContent(((Document) item).getDocumentElement(), sb);
				return;
			case Node.ATTRIBUTE_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE:
				sb.append(item.getNodeValue());
				return;
			case Node.ENTITY_REFERENCE_NODE:
			case Node.ENTITY_NODE:
			case Node.ELEMENT_NODE: {
				final NodeList nl = item.getChildNodes();
				final int len = nl.getLength();
				for (int i = 0; i < len; i++) {
					getTextContent(nl.item(i), sb);
				}
			}
		}
	}

	/** Get text nodes from all textual child nodes of node from argument.
	 * As textual nodes are considered nodes of type:
	 * <ul>
	 * <li>org.w3c.dom.CDATASection</li>
	 * <li>org.w3c.dom.Text</li>
	 * <li>org.w3c.dom.EntityReference</li>
	 * </ul>
	 * @param node actual node; it's children are inspected.
	 * @param coalesce if true text nodes are coalesced.
	 * @return NodeList with text nodes.
	 */
	public static final NodeList getTextNodes(final Node node,
		final boolean coalesce) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			switch (item.getNodeType()) {
				case Node.CDATA_SECTION_NODE:
				case Node.TEXT_NODE:
					result.addItem(item);
					continue;
				case Node.ENTITY_REFERENCE_NODE: {
					final NodeList nl1 = item.getChildNodes();
					for (int j = 0; j < nl1.getLength(); j++) {
						Node item1 = nl1.item(j);
						switch (item1.getNodeType()) {
							case Node.CDATA_SECTION_NODE:
							case Node.TEXT_NODE:
								result.addItem(item1);
								continue;
							case Node.ENTITY_REFERENCE_NODE: {
								final NodeList nl2 = getTextNodes(item1, true);
								for (int k = 0; k < nl2.getLength(); k++) {
									result.addItem(nl2.item(k));
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/** Get coalesced text nodes from all textual child nodes of a node.
	 * As textual nodes are considered nodes of type:
	 * <ul>
	 * <li>org.w3c.dom.CDATASection</li>
	 * <li>org.w3c.dom.Text</li>
	 * <li>org.w3c.dom.EntityReference</li>
	 * </ul>
	 * All node items in the returned NodeList object are created as new
	 * Text nodes, i.e. consequently their parents are <tt>null</tt>.
	 * @param node it's children are inspected.
	 * @return NodeList with text nodes.
	 */
	public static final NodeList getTextNodesCoalesced(final Node node) {
		final NodeList nl = node.getChildNodes();
		final int len = nl.getLength();
		if (node == null || len == 0) {
			return EMPTYNODELIST;
		}
		final KNodeList result = new KNodeList();
		final Document doc = node.getOwnerDocument();
		Text text = null;
		for (int i = 0; i < len; i++) {
			final Node item = nl.item(i);
			switch (item.getNodeType()) {
				case Node.ENTITY_REFERENCE_NODE: {
					if (text == null) {
						text =
							doc.createTextNode(getEntityReferenceValue(item));
						result.addItem(text);
					} else {
						text.setData(text.getNodeValue() +
							getEntityReferenceValue(item));
					}
					continue;
				}
				case Node.CDATA_SECTION_NODE:
				case Node.TEXT_NODE: {
					if (text == null) {
						text = doc.createTextNode(item.getNodeValue());
						result.addItem(text);
					} else {
						text.setNodeValue(
							text.getNodeValue() + item.getNodeValue());
					}
				}
				continue;
				default:
					text = null;
			}
		}
		return result;
	}

	/** Get text concatenated from all direct child nodes.
	 * @param node actual node; it's children are inspected.
	 * @return String with text value or <tt>null</tt>.
	 */
	public static final String getTextValue(final Node node) {
		if (node == null) {
			return "";
		}
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE:
				return node.getNodeValue();
			case Node.ENTITY_REFERENCE_NODE:
				return getEntityReferenceValue((EntityReference) node);
		}
		final StringBuilder sb = new StringBuilder();
		NodeList nl = node.getChildNodes();
		if (nl.getLength() == 0) {
			return "";
		}
		for (int i = 0, maxi = nl.getLength(); i < maxi; i++) {
			final Node item = nl.item(i);
			switch (item.getNodeType()) {
				case Node.CDATA_SECTION_NODE:
				case Node.TEXT_NODE:
					sb.append(item.getNodeValue());
					continue;
				case Node.ENTITY_REFERENCE_NODE:
					sb.append(getEntityReferenceValue((EntityReference) item));
			}
		}
		return sb.toString();
	}

	/** Get NodeList with texts from all direct child nodes.
	 * @param node actual node; it's children are inspected.
	 * @return NodeList with texts from all direct child nodes.
	 */
	public static final NodeList getTextValues(final Node node) {
		if (node == null) {
			return EMPTYNODELIST;
		}
		final KNodeList knl = new KNodeList();
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE:
			case Node.ENTITY_REFERENCE_NODE:
				knl.addItem(node);
				return knl;
		}
		NodeList nl = node.getChildNodes();
		if (nl.getLength() == 0) {
			return EMPTYNODELIST;
		}
		for (int i = 0, maxi = nl.getLength(); i < maxi; i++) {
			Node item;
			switch ((item = nl.item(i)).getNodeType()) {
				case Node.ATTRIBUTE_NODE:
				case Node.CDATA_SECTION_NODE:
				case Node.TEXT_NODE:
				case Node.ENTITY_REFERENCE_NODE:
					knl.addItem(item);
			}
		}
		return knl;
	}

	/** Get text value associated with entity reference.
	 * @param node entity reference.
	 * @return String with text or empty string.
	 */
	public static final String getEntityReferenceValue(final Node node) {
		if (node == null) {
			return "";
		}
		final DocumentType dt = node.getOwnerDocument().getDoctype();
		if (dt == null) {
			return "";
		}
		final NamedNodeMap nm =
			node.getOwnerDocument().getDoctype().getEntities();
		return nm == null
			? "" :getTextContent(nm.getNamedItem(node.getNodeName()));
	}

	/** Return prefix of given QName.
	 * @param qname qualified name.
	 * @return prefix of given QName or the empty string.
	 */
	public static final String getQNamePrefix(String qname) {
		final int ndx = qname.indexOf(':');
		return ndx > 0 ? qname.substring(0, ndx) : "";
	}

	/** Return local part of given QName.
	 * @param qname qualified name.
	 * @return local part of given QName.
	 */
	public static final String getQNameLocalpart(String qname) {
		final int ndx = qname.indexOf(':');
		return ndx >= 0 ? qname.substring(ndx + 1) : qname;
	}

	/** Return element bound to a node. If the argument is an Element then
	 * it is returned. If it is a Document then document element is returned.
	 * If it is an Attribute, then owner element is returned. Otherwise if
	 * the parent node is an element it is returned. If no it is no one of
	 * above cases null is returned.
	 * @param node node which bound element is returned.
	 * @return bound element or null.
	 */
	public static final Element getBoundElement(Node node) {
		if (node == null) {
			return null;
		}
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				return (Element) node;
			case Node.ATTRIBUTE_NODE:
				return ((Attr) node).getOwnerElement();
			default: {
				final Node n = node.getParentNode();
				if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
					return (Element) n;
				}
			}
		}
		return null;
	}

	/** Return NameSpace URI of prefix in context of given element.
	 * @param prefix prefix of NameSpace or <tt>null</tt>.
	 * @param elem element in which context an URI is searched.
	 * @return URI or empty string.
	 */
	public static final String getNSURI(final String prefix,
		final Element elem) {
		final String nsAttr = prefix == null || prefix.isEmpty() ?
			"xmlns" : "xmlns:" + prefix;
		Element element = elem;
		for (;;) {
			if (element.hasAttribute(nsAttr)) {
				return element.getAttribute(nsAttr);
			}
			final Node n = element.getParentNode();
			if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) n;
			} else { //returns empty string if NameSpace not found
				return "";
			}
		}
	}

	/** Create copy of node in given Document. This method is similar to
	 * org.w3c.dom.Node.cloneNode(deep). However, if document of given node
	 * is not equal to document from argument <tt>doc</tt> then the new copy
	 * of node is created in the document from argument.
	 * @param doc document where clone of node is created.
	 * @param node object which to be cloned/copied.
	 * @param deep if <tt>true</tt> child nodes are created, otherwise ignored.
	 * @return copy of node from argument created in given document.
	 * @throws SRuntimeException XML201 if an error occurs.
	 */
	public static final Node createNodeCopyInDocument(final Document doc,
		final Node node,
		final boolean deep) throws SRuntimeException {
		if (doc == node.getOwnerDocument()) {
			return node.cloneNode(deep);
		}
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE: {
				final String u = node.getNamespaceURI();
				final Attr a = (u != null && u.length() > 0)
					? doc.createAttributeNS(u, node.getNodeName())
					: doc.createAttribute(node.getNodeName());
				a.setValue(node.getNodeValue());
				return a;
			}
			case Node.COMMENT_NODE:
				return doc.createComment(node.getNodeValue());
			case Node.CDATA_SECTION_NODE:
				return doc.createCDATASection(node.getNodeValue());
			case Node.TEXT_NODE:
				return doc.createTextNode(node.getNodeValue());
			case Node.ELEMENT_NODE: {
				String u = node.getNamespaceURI();
				final String name = node.getNodeName();
				Element e;
				if (u != null && u.length() > 0) {
					e = doc.createElementNS(u, name);
					final int x = name.indexOf(':');
					final String s = x >= 0
						? "xmlns:" + name.substring(0, x) : "xmlns";
					if (!((Element) node).hasAttribute(s)) {
						e.setAttribute(s, u);
					}
				} else {
					e = doc.createElement(name);
				}
				final NamedNodeMap nm = node.getAttributes();
				if (nm != null) {
					for (int i = 0, maxi = nm.getLength(); i < maxi; i++) {
						Attr a = (Attr) createNodeCopyInDocument(doc,
							nm.item(i), deep);
						if ((u = a.getNamespaceURI()) != null &&
							u.length() > 0) {
							e.setAttributeNodeNS(a);
						} else {
							e.setAttributeNode(a);
						}
					}
				}
				if (deep) {
					final NodeList nl = node.getChildNodes();
					for (int i = 0, maxi = nl.getLength(); i < maxi; i++) {
						e.appendChild(createNodeCopyInDocument(doc,
							nl.item(i), true));
					}
				}
				return e;
			}
			case Node.ENTITY_REFERENCE_NODE: {
				final EntityReference eref =
					doc.createEntityReference(node.getNodeName());
				final NodeList nl = node.getChildNodes();
				if (nl != null) {
					for (int i = 0, maxi = nl.getLength(); i < maxi; i++) {
						eref.appendChild(createNodeCopyInDocument(doc,
							nl.item(i), true)); //allways deep!
					}
				}
				return eref;
			}
			case Node.PROCESSING_INSTRUCTION_NODE: {
				final ProcessingInstruction pi = (ProcessingInstruction) node;
				return doc.createProcessingInstruction(
					pi.getTarget(), pi.getData());
			}
			default:
				//Can't create clone of node to given document
				throw new SRuntimeException(XML.XML201);
		}
	}

	/** Resolve relative position in given node. Note that the position is NOT
	 * full XPath expression. XPosition may contain only an abbreviated location
	 * path with single "/" selectors and with index specifiers "[n]" where n is
	 * an integer and the last step may point only to an attribute ("@name") or
	 * to a text node ("text()[n]") or element node (name[n]).
	 * @param n a node.
	 * @param xpos XPosition.
	 * @return the node or null.
	 */
	public static final Node resolveXPosition(final Node n, final String xpos) {
		return resolveXPosition(n, xpos, null);
	}

	/** Resolve relative position in given node. Note that the position is NOT
	 * full XPath expression. XPosition may contain only an abbreviated location
	 * path with single "/" selectors and with index specifiers "[n]" where n is
	 * an integer and the last step may point only to an attribute ("@name") or
	 * to a text node ("text()[n]") or element node (name[n]).
	 * @param n a node.
	 * @param xpos XPath position.
	 * @param context namespaceContext
	 * @return the node or null.
	 */
	public static final Node resolveXPosition(final Node n,
		final String xpos,
		final NamespaceContext context) {
		if (n == null || xpos == null || xpos.isEmpty()) {
			return null;
		}
		Node m;
		if (n.getNodeType() == Node.DOCUMENT_NODE) {
			m = ((Document) n).getDocumentElement();
			if (m == null) {
				return null;
			}
		} else {
			m = n;
		}
		byte xmlVersion = n.getOwnerDocument() == null
			? (byte) 10
			: "1.1".equals(n.getOwnerDocument().getXmlVersion())
				? (byte) 11 : 10;
		final StringParser p = new StringParser();
		p.setSourceBuffer(xpos);
		if (p.isChar('/')) {
			Node x;
			while((x = m.getParentNode()) != null) {
				if (x.getNodeType() == Node.DOCUMENT_NODE) {
					break;
				}
				m = x;
			}
			if (p.isXMLName(xmlVersion)) {
				String name = p.getParsedString();
				if (p.isChar('[') && p.isInteger() && p.isChar(']')) {
					if (Integer.parseInt(p.getParsedString()) != 1) {
						return null;
					}
				}
				final int ndx;
				if (context == null || (ndx = name.indexOf(':')) < 0) {
					if (name.charAt(0) == ':') {
						name = name.substring(1);
					}
					if (!name.equals(m.getNodeName())) {
						return null;
					}
				} else {
					final String uri =
						context.getNamespaceURI(name.substring(0,ndx));
					String localName = m.getLocalName();
					if (localName == null) {
						localName = m.getNodeName();
					}
					if (uri == null || !name.substring(ndx+1).equals(localName)
						|| !uri.equals(m.getNamespaceURI())) {
						return null;
					}
				}
			}
		}
		return resolveXPosition(m, p, context, xmlVersion);
	}

	private static Node resolveXPosition(final Node n,
		final StringParser p,
		final NamespaceContext context,
		final byte xmlVersion) {
		if (p.isChar('/')) {
			final boolean isAttr = p.isChar('@');
			if (p.isXMLName(xmlVersion)) {
				String name = p.getParsedString();
				if (isAttr) {
					if (n.getNodeType() != Node.ELEMENT_NODE) {
						return null;
					}
					int ndx;
					if (context == null || (ndx = name.indexOf(':')) < 0) {
						if (name.charAt(0) == ':') {
							name = name.substring(1);
						}
						return ((Element) n).getAttributeNode(name);
					} else {
						final String u =
							context.getNamespaceURI(name.substring(0,ndx));
						if (u != null) {
							return ((Element) n).getAttributeNodeNS(
								u, name.substring(ndx + 1));
						}
					}
					return null;
				}
				int i = 1;
				if (p.isToken("()")) {
					if ("text".equals(name)) {
						name = "#text";
					}
				}
				if (p.isChar('[') && p.isInteger() && p.isChar(']')) {
					i = Integer.parseInt(p.getParsedString());
				}
				final NodeList nl = n.getChildNodes();
				final int len = nl.getLength();
				if (len < i) {
					return null;
				}
				for (int j = 0; j < len; j++) {
					final Node m = nl.item(j);
					if (name.charAt(0) == '#') {//"#text"
						final short type = m.getNodeType();
						if ((type == Node.TEXT_NODE ||
							type == Node.CDATA_SECTION_NODE) && --i == 0) {
							return resolveXPosition(m, p, context, xmlVersion);
						}
					} else if (m.getNodeType() == Node.ELEMENT_NODE) {
						int ndx;
						if (context == null ||
							(ndx = name.indexOf(':')) < 0) {
							if (name.charAt(0) == ':') {
								name = name.substring(1);
							}
							if (name.equals(m.getNodeName()) && --i == 0) {
								return resolveXPosition(
									m, p, context, xmlVersion);
							}
						} else {
							final String u =
								context.getNamespaceURI(name.substring(0, ndx));
							String localName = m.getLocalName();
							if (localName == null) {
								localName = m.getNodeName();
							}
							if (u != null &&
								name.substring(ndx + 1).equals(localName)
								&& u.equals(m.getNamespaceURI()) && --i == 0) {
									return resolveXPosition(
										m, p, context, xmlVersion);
							}
						}
					}
				}
			}
		} else if (p.isChar('@') && p.isXMLName(xmlVersion)) {
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				return null;
			}
			String name = p.getParsedString();
			int ndx;
			if (context == null || (ndx = name.indexOf(':')) < 0) {
				if (name.charAt(0) == ':') {
					name = name.substring(1);
				}
				return ((Element) n).getAttributeNode(name);
			} else {
				final String u = context.getNamespaceURI(name.substring(0,ndx));
				if (u != null) {
					return ((Element) n).getAttributeNodeNS(
						u, name.substring(ndx + 1));
				}
			}
		} else if (p.eos()) {
			return n;
		}
		return null;
	}

	/** Get string with xpath position of a node. The result position is NOT
	 * the full XPath expression. Generated position cotains the abbreviated
	 * location path with single "/" selectors and with index specifiers "[n]"
	 * where n is index of a child node. If the argument is an attribute then
	 * the last step is "@name", if it is a text node thle last step is
	 * "text()[n]" and if it is an element node than ths last location step is
	 * the name of element followed with the childnode index [n].
	 * @param n node of which xpath position is to be created.
	 * @return string with the XPath position.
	 */
	public static final String getXPosition(final Node n) {
		final StringBuilder sb = new StringBuilder();
		final short type = n.getNodeType();
		if (type == Node.ATTRIBUTE_NODE) {
			final Element el = ((Attr) n).getOwnerElement();
			if (el == null) {
				return null;
			}
			getXPosition(el, sb);
			sb.append("/@");
			sb.append(n.getNodeName());
		} else if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
			final Node m = n.getParentNode();
			if (m == null || m.getNodeType() != Node.ELEMENT_NODE) {
				return null;
			}
			getXPosition(m, sb);
			sb.append("/text()");
			final NodeList nl = m.getChildNodes();
			final int len = nl.getLength();
			for (int i = 0, ndx = 0; i < len; i++) {
				final Node o = nl.item(i);
				if (o.getNodeType() == Node.TEXT_NODE ||
					o.getNodeType() == Node.CDATA_SECTION_NODE) {
					ndx++;
					if (o == n) {
						if (len > 1) {
							sb.append('[').append(ndx).append(']');
						}
						break;
					}
				}
			}
		} else if (type == Node.ELEMENT_NODE) {
			getXPosition(n, sb);
		} else if (type == Node.DOCUMENT_NODE) {
			getXPosition(((Document) n).getDocumentElement(), sb);
		} else {
			return null;
		}
		return sb.toString();
	}

	/** Get xpath position of a node (the auxiliary recursive method).
	 * @param n the node.
	 * @param sb the Stringbuffer object where xpath is constructed.
	 */
	private static void getXPosition(final Node n, final StringBuilder sb) {
		final Node parent = n.getParentNode();
		if (parent == null || parent.getNodeType() == Node.DOCUMENT_NODE) {
			sb.append('/');
			String name = n.getNodeName();
			if (name.indexOf(':') < 0) {
				final String u = n.getNamespaceURI();
				if (u != null && u.length() > 0) {
					name = ':' + name;
				}
			}
			sb.append(name);
		} else {
			getXPosition(parent, sb);
			sb.append('/');
			String name = n.getNodeName();
			if (name.indexOf(':') < 0) {
				final String u = n.getNamespaceURI();
				if (u != null && u.length() > 0) {
					name = ':' + name;
				}
			}
			sb.append(name);
			final NodeList nl = parent.getChildNodes();
			final int len = nl.getLength();
			int ndx = 1;
			int i = 0;
			while (i < len) {
				Node item;
				if ((item = nl.item(i++)) == n) {
					break;
				}
				String name1 = item.getNodeName();
				if (name1.indexOf(':') < 0) {
					final String uri = n.getNamespaceURI();
					if (uri != null && uri.length() > 0) {
						name1 = ':' + name1;
					}
				}
				if (name1.equals(name)) {
					ndx++;
				}
			}
			if (len > 1) {
				sb.append('[');
				sb.append(String.valueOf(ndx));
				sb.append(']');
			}
		}
	}
}