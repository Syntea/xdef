package org.xdef.util.xsd2xd.xd;

import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KDOMUtils;
import javax.xml.namespace.QName;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Provides methods for working with XML documents.
 * @author Ilia Alexandrov
 */
public final class Utils extends KDOMUtils {

	/** DOM Builder. */
	private static final KDOMBuilder BUILDER = new KDOMBuilder();

	/** Gets DOM builder.
	 * @return DOM builder instance.
	 */
	public static KDOMBuilder getBuilder() {return BUILDER;}

	/** Returns true if given node is element node and has same
	 * name space as given name space and same local name as given local name.
	 * @param node node to test.
	 * @param namespace name space URI.
	 * @param localName local name.
	 * @return true if given node is element node and has same
	 * name space as given name space and same local name as given local name.
	 * @throws NullPointerException if given node or element local name is
	 * null.
	 * @throws IllegalArgumentException if given element local name is empty.
	 */
	public static boolean isElement(Node node,
		String namespace,
		String localName) {
		if (node == null) {
			throw new NullPointerException("Given node is null");
		}
		if (localName == null) {
			throw new NullPointerException("Given element local name is null");
		}
		if (localName.length() == 0) {
			throw new IllegalArgumentException(
				"Given element local name is empty");
		}
		return Node.ELEMENT_NODE ==
			node.getNodeType() && namespace == null || "".equals(namespace)
			? node.getNamespaceURI() == null
			: namespace.equals(node.getNamespaceURI())
				&& localName.equals(node.getLocalName());
	}

	/** Returns true if given child node is a valid child node
	 * of parent element with given name space and given local name.
	 * @param child child node to test.
	 * @param namespace parent node name space URI.
	 * @param localName parent node local name.
	 * @return true if given child node is a valid child node of parent
	 * element with given name space and given local name.
	 * @throws NullPointerException if given child node or parent node local
	 * name is null.
	 * @throws IllegalArgumentException if parent node local name is empty.
	 */
	public static boolean isChild(Node child,
		String namespace,
		String localName) {
		if (child == null) {
			throw new NullPointerException("Given child node is null");
		}
		if (localName == null) {
			throw new NullPointerException("Given parent local name is null");
		}
		if (localName.length() == 0) {
			throw new IllegalArgumentException(
				"Given parent local name is empty");
		}
		Node parent = child.getParentNode();
		return isElement(parent, namespace, localName);
	}

	/** Gets value of an attribute with given name space and given local name
	 * contained in given node.
	 * @param node node to get attribute value from.
	 * @param namespace attribute name space.
	 * @param localName attribute local name.
	 * @return attribute value.
	 * @throws NullPointerException if given node or attribute local name is
	 * null.
	 * @throws IllegalArgumentException if given attribute local name is empty
	 * or given node is not an element node or if given node does not contain
	 * attribute with such name space and local name.
	 */
	public static String getAttrValue(Node node,
		String namespace,
		String localName) {
		if (node == null) {
			throw new NullPointerException("Given node is null");
		}
		if (localName == null) {
			throw new NullPointerException(
				"Given attribute local name is null");
		}
		if (localName.length() == 0) {
			throw new IllegalArgumentException(
				"Given attribute local name is empty");
		}
		if (Node.ELEMENT_NODE != node.getNodeType()) {
			throw new IllegalArgumentException(
				"Given node is not an element node");
		}
		Element element = (Element) node;
		Attr attr = element.getAttributeNodeNS(namespace, localName);
		if (attr == null) {
			attr = element.getAttributeNode(localName);
		}
		if (attr == null) {
			throw new IllegalArgumentException("Given node does not contain"
				+ " attribute node with given namespace and local name");
		}
		return attr.getValue();
	}

	/** Adds attribute node with given name space, given qualified name and
	 * given value to given parent element.
	 * @param parent parent element to add attribute to.
	 * @param namespace attribute name space.
	 * @param qName attribute qualified name.
	 * @param value attribute value.
	 * @return created and added attribute node.
	 * @throws NullPointerException if given parent element or attribute
	 * qualified name is null.
	 * @throws IllegalArgumentException if given attribute qualified name
	 * is empty.
	 */
	public static Attr addAttr(Element parent, String namespace, String qName,
			String value) {
		if (parent == null) {
			throw new NullPointerException("Given parent element is null");
		}
		if (qName == null) {
			throw new NullPointerException(
				"Given attribute qualified name is null");
		}
		if (qName.length() == 0) {
			throw new IllegalArgumentException(
				"Given attribute qualified name is empty");
		}
		if (namespace == null || namespace.length() == 0) {
			return addAttr(parent, qName, value);
		}
		Attr attr=parent.getOwnerDocument().createAttributeNS(namespace, qName);
		attr.setValue(value);
		parent.setAttributeNodeNS(attr);
		return attr;
	}

	/** Adds attribute node with given name and value to given parent element.
	 * @param parent parent element to add attribute to.
	 * @param name attribute name.
	 * @param value attribute value.
	 * @return created and added attribute node.
	 * @throws NullPointerException if given parent element or attribute name
	 * is null.
	 * @throws IllegalArgumentException if given attribute name is empty.
	 * @throws RuntimeException if could not add name space declaration
	 * attribute to given parent element.
	 */
	public static Attr addAttr(Element parent, String name, String value) {
		if (parent == null) {
			throw new NullPointerException("Given pasrent element is null");
		}
		if (name == null) {
			throw new NullPointerException("Given attribute name is null");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given attribute name is empty");
		}
		Attr attr = parent.getOwnerDocument().createAttribute(name);
		attr.setValue(value);
		try {
			parent.setAttributeNode(attr);
			return attr;
		} catch (DOMException ex) {
			throw new RuntimeException("Could not add namespace declaration "
					+ "attribute to given parent element", ex);
		}
	}

	/** Adds attribute with given name and given value to given parent element.
	 * If attribute with given already exists it sets value to given attribute
	 * value.
	 * @param parent parent element to add attribute.
	 * @param attrName name of attribute.
	 * @param attrValue attribute value.
	 * @throws NullPointerException if given parent element or attribute name is
	 * null.
	 * @throws IllegalArgumentException if given attribute name is empty.
	 */
	public static void setAttr(Element parent,
		String attrName,
		String attrValue) {
		if (parent == null) {
			throw new NullPointerException("Given parent element is null");
		}
		if (attrName == null) {
			throw new NullPointerException("Given attribute name is null");
		}
		if (attrName.length() == 0) {
			throw new IllegalArgumentException(
				"Given attribute name is empty");
		}
		Attr attr = parent.getAttributeNode(attrName);
		if (attr == null) {
			addAttr(parent, attrName, attrValue);
		} else {
			attr.setValue(attrValue);
		}
	}

	/** Returns node qualified name according to given prefix and local name.
	 * @param prefix qualified name prefix or null.
	 * @param localName qualified name local part.
	 * @return full qualified name.
	 */
	public static String getNodeQName(String prefix, String localName) {
		return new MyQName(prefix, localName).getQName();
	}

	/** Adds namespace declaration attribute with given prefix and namespace URI
	 * to given element.
	 * @param element element to add namespace declaration.
	 * @param prefix prefix of name space or null.
	 * @param namespaceURI name space URI of name space.
	 * @return name space declaration attribute declaration.
	 * @throws NullPointerException if given element or name space URI is
	 * null.
	 * @throws IllegalArgumentException if given name space URI is empty.
	 */
	public static Attr addNamespaceDecl(Element element,
		String prefix,
		String namespaceURI) {
		if (element == null) {
			throw new NullPointerException("Given element node is null");
		}
		if (namespaceURI == null) {
			throw new NullPointerException("Given namespace URI is null");
		}
		if (namespaceURI.length() == 0) {
			throw new IllegalArgumentException("Given namespace URI is empty");
		}
		if (prefix == null || prefix.length() == 0) {
			return Utils.addAttr(element,
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns", namespaceURI);
		} else {
			return Utils.addAttr(element,
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:" + prefix, namespaceURI);
		}
	}

	/** Adds name space declaration with given name space URI to given element
	 * node and returns prefix for that name space declaration.
	 * @param element element to add name space declaration.
	 * @param namespaceURI name space URI of declaration.
	 * @return name space prefix.
	 * @throws RuntimeException if cannot add name space declaration.
	 */
	public static String addNamespaceDeclaration(Element element,
			String namespaceURI) throws RuntimeException {
		final String prefixes = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < prefixes.length(); i++) {
			String prefix = prefixes.substring(i, i + 1);
			if (addNamespaceDecl(element, prefix, namespaceURI) != null) {
				return prefix;
			}
		}
		for (int i = 0; i < prefixes.length(); i++) {
			for (int j = 0; j < prefixes.length(); j++) {
				String prefix = prefixes.substring(i, i + 1)
						+ prefixes.substring(j, j + 1);
				if (addNamespaceDecl(element, prefix, namespaceURI) != null) {
					return prefix;
				}
			}
		}
		throw new RuntimeException(
			"Could not add namespace declaration to given element node");
	}

	/** Adds given element at first position to given parent element.
	 * @param parent element to add to.
	 * @param inserted element to add.
	 * @throws NullPointerException if given parent element or inserted element
	 * is null.
	 * @throws RuntimeException if cannot add given inserted element to given
	 * parent element.
	 */
	public static void insertFirst(Element parent, Element inserted) {
		if (parent == null) {
			throw new NullPointerException("Given parent element is null");
		}
		if (inserted == null) {
			throw new NullPointerException("Given element to insert is null");
		}
		Node first = parent.getFirstChild();
		try {
			if (first == null) {
				parent.appendChild(inserted);
			} else {
				parent.insertBefore(inserted, first);
			}
		} catch (DOMException ex) {
			throw new RuntimeException(
				"Could not add given element to parent", ex);
		}
	}

	/** Parses given qualified name and returns representation of qualified name.
	 * @param qualifiedName qualified name.
	 * @return qualified name representation.
	 */
	public static MyQName parseQName(String qualifiedName) {
		return MyQName.parseQName(qualifiedName);
	}

	/** Returns name space prefix of name space declaration with given
	 * name space URI in given context element. If given element does not
	 * contain name space declaration it looks in parent element.
	 * @param contextNode context node to search in.
	 * @param namespaceURI name space URI of searched name space declaration.
	 * @return prefix of name space declaration or null if there is
	 * no such name space declaration.
	 * @throws NullPointerException if given context element or name space URI
	 * is null.
	 * @throws IllegalArgumentException if given name space URI is empty.
	 */
	public static String getNSPrefix(Node contextNode, String namespaceURI) {
		if (contextNode == null) {
			throw new NullPointerException("Given context node is null");
		}
		if (namespaceURI == null) {
			throw new NullPointerException("Given namespace URI is null");
		}
		if (namespaceURI.length() == 0) {
			throw new IllegalArgumentException("Given namespace URI is empty");
		}
		return getNSPrefixRec(contextNode, namespaceURI);
	}

	/** Returns name space prefix of name space declaration with given
	 * name space URI in given context element. If given element does not
	 * contain name space declaration it looks in parent element.
	 * @param contextNode context node to search in.
	 * @param namespaceURI name space URI of searched name space declaration.
	 * @return prefix of name space declaration or null if there is
	 * no such name space declaration.
	 */
	private static String getNSPrefixRec(Node contextNode, String namespaceURI){
		//get prefix in node
		String prefix = getNSPrefixInNode(contextNode, namespaceURI);
		if (prefix != null) {
			return prefix;
		}
		//get parent element
		Node parent = null;
		if (Node.ATTRIBUTE_NODE == contextNode.getNodeType()) {
			parent = ((Attr) contextNode).getOwnerElement();
		} else if (Node.ELEMENT_NODE == contextNode.getNodeType()
				|| Node.TEXT_NODE == contextNode.getNodeType()) {
			parent = contextNode.getParentNode();
		}
		if (parent == null) {
			return null;
		}
		//get prefix in parent
		if (Node.ELEMENT_NODE == parent.getNodeType()) {
			return getNSPrefixRec(parent, namespaceURI);
		}
		return null;
	}

	/** Returns prefix of name space declaration with given name space URI
	 * in given node.
	 * @param node node to search in.
	 * @param namespaceURI name space URI of name space declaration.
	 * @return prefix of given name space URI declaration if declaration exists,
	 * empty string if name space is declared as default name space or
	 * null if given node does not contain name space declaration with
	 * given name space URI.
	 */
	private static String getNSPrefixInNode(Node node, String namespaceURI) {
		if (Node.ELEMENT_NODE == node.getNodeType()) {
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attr = (Attr) attrs.item(i);
				if (namespaceURI.equals(attr.getValue())) {
					if ("xmlns".equals(attr.getPrefix())) {
						return attr.getLocalName();
					} else if ("xmlns".equals(attr.getLocalName())) {
						return "";
					}
				}
			}
		}
		return null;
	}

	/** Returns true if given element node has attribute node with
	 * given name space URI and given local name declaration.
	 * @param contextElem context element to search in.
	 * @param attrNS attribute name space URI or null.
	 * @param attrLocalName attribute local name.
	 * @return true if given element node has attribute node with
	 * given name space URI and given local name declaration.
	 * @throws NullPointerException if given context element or attribute
	 * local name is null.
	 * @throws IllegalArgumentException if given attribute local name is empty.
	 */
	public static boolean hasAttrDecl(Element contextElem,
		String attrNS,
		String attrLocalName) {
		if (contextElem == null) {
			throw new NullPointerException("Given context element is null");
		}
		if (attrLocalName == null) {
			throw new NullPointerException(
				"Given attribute local name is null");
		}
		if (attrLocalName.length() == 0) {
			throw new IllegalArgumentException(
				"Given attribute local name is empty");
		}
		if (attrNS == null) {
			return contextElem.hasAttribute(attrLocalName);
		} else {
			return contextElem.hasAttributeNS(attrNS, attrLocalName);
		}
	}

	/** Gets attribute name prefix of given attribute node.
	 * @param attr attribute node to get prefix from.
	 * @return attribute name prefix or null if given attribute name
	 * does not contain prefix.
	 * @throws NullPointerException if given attribute is null.
	 */
	public static String getAttrPrefix(Attr attr) {
		return getAttrQName(attr).getPrefix();
	}

	/** Gets local name of given attribute node.
	 * @param attr attribute node to get local name from.
	 * @return attribute local name.
	 */
	public static String getAttrLocalName(Attr attr) {
		return getAttrQName(attr).getLocalPart();
	}

	/** Gets qualified name representation of given attribute.
	 * @param attr attribute to get qualified name from.
	 * @return qualified name representation.
	 * @throws NullPointerException if given attribute node is null.
	 */
	private static QName getAttrQName(Attr attr) {
		if (attr == null) {
			throw new NullPointerException("Given attribute node is null");
		}
		String name = attr.getName();
		int ndx = name.indexOf(":");
		if (ndx != -1) {
			String prefix = name.substring(0, ndx);
			String namespace = attr.getNamespaceURI();
			name = name.substring(ndx + 1);
			return new QName(namespace, name, prefix);
		}
		return new QName(name);
	}

	/** Write given schemas map into directory with given name.
	 * @param schemas map with schema file name as key and document as value.
	 * @param path output directory path.
	 */
	public static void printSchemas(Map<String, Document> schemas, String path){
		if (schemas == null) {
			throw new NullPointerException("Given schemas map is null");
		}
		if (schemas.isEmpty()) {
			throw new IllegalArgumentException("Given schemas map is empty");
		}
		if (path == null) {
			throw new NullPointerException("Given path is null");
		}
		if (path.length() == 0) {
			throw new IllegalArgumentException("Given path is empty");
		}
		//creating folder
		File folder = new File(path);
		if (folder.exists()) {
			if (!folder.isDirectory()) {
				throw new IllegalArgumentException(
					"Given path is not a directory");
			}
		} else {
			if (!folder.mkdir()) {
				throw new RuntimeException(
					"Could not create folder with given path");
			}
		}
		Iterator it = schemas.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Entry) it.next();
			String fName =
				new File(path, (String) entry.getKey()).getAbsolutePath();
			try {
				KXmlUtils.writeXml(fName, (Document)entry.getValue(),true,true);
			} catch (Exception ex) {
				throw new RuntimeException(
					"Could not write schema document", ex);
			}
		}
	}

	/** Represents qualified name. */
	public static final class MyQName {

		/** Hash code. */
		private int _hashCode = 0;
		/** Qualified name prefix part. */
		private final String _prefix;
		/** Qualified name local name part. */
		private final String _name;

		/** Creates instance of QName with given local name part and no prefix
		 * (prefix is null).
		 * @param name qualified name local part.
		 * @throws NullPointerException if given name is null.
		 * @throws IllegalArgumentException if given name is empty.
		 */
		public MyQName(String name) {this(null, name);}

		/** Creates instance of QName with given prefix and local name.
		 * @param prefix qualified name prefix (if is empty then
		 * it is null).
		 * @param name qualified name local part.
		 * @throws NullPointerException if given name is null.
		 * @throws IllegalArgumentException if given name is empty.
		 */
		public MyQName(String prefix, String name) {
			if (name == null) {
				throw new NullPointerException("Given name is null");
			}
			if (name.length() == 0) {
				throw new IllegalArgumentException("Given naem is empty");
			}
			_name = name;
			if (prefix != null && prefix.length() == 0) {
				_prefix = null;
			} else {
				_prefix = prefix;
			}
		}

		/** Qualified name prefix part getter.
		 * @return qualified name prefix.
		 */
		public String getPrefix() {return _prefix;}

		/** Qualified name local name part getter.
		 * @return qualified name local part.
		 */
		public String getName() {return _name;}

		/** Returns full qualified name string as in XML.
		 * @return full qualified name.
		 */
		public String getQName() {
			return _prefix == null ? _name : _prefix + ":" + _name;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof MyQName)) {
				return false;
			}
			MyQName n = (MyQName) obj;
			if (_prefix == null
				? n._prefix != null : !_prefix.equals(n._prefix)) {
				return false;
			}
			return _name.equals(n._name);
		}

		@Override
		public int hashCode() {
			if (_hashCode == 0) {
				_hashCode = 541;
				_hashCode = 541 * _hashCode
					+ (_prefix == null ? 0 : _prefix.hashCode());
				_hashCode = 541 * _hashCode + _name.hashCode();
			}
			return _hashCode;
		}

		@Override
		public String toString() {
			return "QName[prefix='" + _prefix + "', " + "name='" + _name + "']";
		}

		/** Parses given qualified name as string and returns representation of
		 * qualified name.
		 * @param qName qualified name as string.
		 * @return representation of qualified name.
		 * @throws NullPointerException if given qualified name is null.
		 * @throws IllegalArgumentException if given qualified name is empty.
		 */
		public static MyQName parseQName(String qName) {
			if (qName == null || qName.length() == 0) {
				throw new IllegalArgumentException(
					"Given qualified name is empty");
			}
			String prefix;
			String name;
			int ndx;
			if ((ndx = qName.indexOf(":")) != -1) {
				prefix = qName.substring(0, ndx);
				name = qName.substring(ndx + 1, qName.length());
			} else {
				prefix = null;
				name = qName;
			}
			return new MyQName(prefix, name);
		}
	}
}
