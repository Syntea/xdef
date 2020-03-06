package org.xdef.impl.util.conv;

import org.xdef.impl.compile.XScriptParser;
import org.xdef.sys.SBuffer;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.xdef.impl.XConstants;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Provides methods for working with XML documents.
 * @author Ilia Alexandrov
 */
public final class Util {

	/** XML name space declaration. */
	public static final String XMLNS = "xmlns";
	/** DOM Builder. */
	private static final KDOMBuilder BUILDER = new KDOMBuilder();

	/** Gets DOM builder.
	 * @return DOM builder instance.
	 */
	public static KDOMBuilder getBuilder() {return BUILDER;}

	/** Returns <tt>true</tt> if given node is element node and has same
	 * name space as given name space and same local name as given local name.
	 * @param node node to test.
	 * @param namespace name space URI.
	 * @param localName local name.
	 * @return <tt>true</tt> if given node is element node and has same
	 * name space as given name space and same local name as given local name.
	 * @throws NullPointerException if given node or element local name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given element local name is empty.
	 */
	public static boolean isElement(final Node node,
		final String namespace,
		final String localName) {
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

	/** Returns <tt>true</tt> if given child node is a valid child node
	 * of parent element with given name space and given local name.
	 * @param child child node to test.
	 * @param namespace parent node name space URI.
	 * @param localName parent node local name.
	 * @return <tt>true</tt> if given child node is a valid child node of parent
	 * element with given name space and given local name.
	 * @throws NullPointerException if given child node or parent node local
	 * name is <tt>null</tt>.
	 * @throws IllegalArgumentException if parent node local name is empty.
	 */
	public static boolean isChild(final Node child,
		final String namespace,
		final String localName) {
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
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute local name is empty
	 * or given node is not an element node or if given node does not contain
	 * attribute with such name space and local name.
	 */
	public static String getAttrValue(final Node node,
		final String namespace,
		final String localName) {
		Element element = (Element) node;
		Attr attr = element.getAttributeNodeNS(namespace, localName);
		if (attr == null) {
			attr = element.getAttributeNode(localName);
		}
		if (attr == null) {
			return null;
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
	 * qualified name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute qualified name
	 * is empty.
	 */
	public static Attr addAttr(Element parent, String namespace, String qName,
			String value) {
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
	 * @param el parent element to add attribute to.
	 * @param name attribute name.
	 * @param value attribute value.
	 * @return created and added attribute node.
	 * @throws NullPointerException if given parent element or attribute name
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute name is empty.
	 * @throws RuntimeException if could not add name space declaration
	 * attribute to given parent element.
	 */
	public static Attr addAttr(final Element el,
		final String name,
		final String value) {
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given attribute name is empty");
		}
		Attr attr = el.getOwnerDocument().createAttribute(name);
		attr.setValue(value);
		try {
			el.setAttributeNode(attr);
			return attr;
		} catch (DOMException ex) {
			throw new RuntimeException("Could not add namespace declaration "
					+ "attribute to given parent element", ex);
		}
	}

	/** Adds attribute with given name and given value to given parent element.
	 * If attribute with given already exists it sets value to given attribute
	 * value.
	 * @param el parent element to add attribute.
	 * @param attrName name of attribute.
	 * @param attrValue attribute value.
	 * @throws NullPointerException if given parent element or attribute name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute name is empty.
	 */
	public static void setAttr(final Element el,
		final String attrName,
		final String attrValue) {
		if (attrName.length() == 0) {
			throw new IllegalArgumentException("Given attribute name is empty");
		}
		Attr attr = el.getAttributeNode(attrName);
		if (attr == null) {
			addAttr(el, attrName, attrValue);
		} else {
			attr.setValue(attrValue);
		}
	}

	/** Returns node qualified name according to given prefix and local name.
	 * @param prefix qualified name prefix or <tt>null</tt>.
	 * @param localName qualified name local part.
	 * @return full qualified name.
	 */
	public static String getNodeQName(final String prefix,
		final String localName) {
		return new MyQName(prefix, localName).getQName();
	}

	/** Adds namespace declaration attribute with given prefix and namespace URI
	 * to given element.
	 * @param element element to add namespace declaration.
	 * @param prefix prefix of name space or <tt>null</tt>.
	 * @param namespaceURI name space URI of name space.
	 * @return name space declaration attribute declaration.
	 * @throws NullPointerException if given element or name space URI is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given name space URI is empty.
	 */
	public static Attr addNamespaceDecl(final Element element,
		final String prefix,
		final String namespaceURI) {
		if (namespaceURI == null) {
			throw new NullPointerException("Given namespace URI is null");
		}
		if (namespaceURI.length() == 0) {
			throw new IllegalArgumentException("Given namespace URI is empty");
		}
		if (prefix == null || prefix.length() == 0) {
			return Util.addAttr(element, XMLNS, namespaceURI);
		} else {
			return Util.addAttr(element,
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLNS + ":" + prefix, namespaceURI);
		}
	}

	/** Adds name space declaration with given name space URI to given element
	 * node and returns prefix for that name space declaration.
	 * @param element element to add name space declaration.
	 * @param namespaceURI name space URI of declaration.
	 * @return name space prefix.
	 * @throws RuntimeException if cannot add name space declaration.
	 */
	public static String addNamespaceDeclaration(final Element element,
			final String namespaceURI) throws RuntimeException {
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
	 * @param el element to add to.
	 * @param inserted element to add.
	 * @throws NullPointerException if given parent element or inserted element
	 * is <tt>null</tt>.
	 * @throws RuntimeException if cannot add given inserted element to given
	 * parent element.
	 */
	public static void insertFirst(final Element el, final Element inserted) {
		if (inserted == null) {
			throw new NullPointerException("Given element to insert is null");
		}
		Node first = el.getFirstChild();
		try {
			if (first == null) {
				el.appendChild(inserted);
			} else {
				el.insertBefore(inserted, first);
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
	public static MyQName parseQName(final String qualifiedName) {
		return MyQName.parseQName(qualifiedName);
	}

	/** Returns name space prefix of name space declaration with given
	 * name space URI in given context element. If given element does not
	 * contain name space declaration it looks in parent element.
	 * @param contextNode context node to search in.
	 * @param namespaceURI name space URI of searched name space declaration.
	 * @return prefix of name space declaration or <tt>null</tt> if there is
	 * no such name space declaration.
	 * @throws NullPointerException if given context element or name space URI
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given name space URI is empty.
	 */
	public static String getNSPrefix(final Node contextNode,
		final String namespaceURI) {
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
	 * @return prefix of name space declaration or <tt>null</tt> if there is
	 * no such name space declaration.
	 */
	private static String getNSPrefixRec(final Node contextNode,
		final String namespaceURI){
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
	 * <tt>null</tt> if given node does not contain name space declaration with
	 * given name space URI.
	 */
	private static String getNSPrefixInNode(final Node node,
		final String namespaceURI) {
		if (Node.ELEMENT_NODE == node.getNodeType()) {
			NamedNodeMap attrs = node.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attr = (Attr) attrs.item(i);
				if (namespaceURI.equals(attr.getValue())) {
					if (Util.XMLNS.equals(attr.getPrefix())) {
						return attr.getLocalName();
					} else if (Util.XMLNS.equals(attr.getLocalName())) {
						return "";
					}
				}
			}
		}
		return null;
	}

	/** Returns <tt>true</tt> if given element node has attribute node with
	 * given name space URI and given local name declaration.
	 * @param contextElem context element to search in.
	 * @param attrNS attribute name space URI or <tt>null</tt>.
	 * @param attrLocalName attribute local name.
	 * @return <tt>true</tt> if given element node has attribute node with
	 * given name space URI and given local name declaration.
	 * @throws NullPointerException if given context element or attribute
	 * local name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given attribute local name is empty.
	 */
	public static boolean hasAttrDecl(final Element contextElem,
		final String attrNS,
		final String attrLocalName) {
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
	 * @return attribute name prefix or <tt>null</tt> if given attribute name
	 * does not contain prefix.
	 * @throws NullPointerException if given attribute is <tt>null</tt>.
	 */
	public static String getAttrPrefix(final Attr attr) {
		return getAttrQName(attr).getPrefix();
	}

	/** Gets local name of given attribute node.
	 * @param attr attribute node to get local name from.
	 * @return attribute local name.
	 */
	public static String getAttrLocalName(final Attr attr) {
		return getAttrQName(attr).getLocalPart();
	}

	/** Gets qualified name representation of given attribute.
	 * @param attr attribute to get qualified name from.
	 * @return qualified name representation.
	 * @throws NullPointerException if given attribute node is <tt>null</tt>.
	 */
	private static QName getAttrQName(final Attr attr) {
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

	/** Write given schema map into directory with given name.
	 * @param schemas map with schema file name as key and document as value.
	 * @param path output directory path.
	 */
	public static void printSchemas(final Map<String, Document> schemas,
		final String path){
		if (schemas.isEmpty()) {
			throw new IllegalArgumentException("Given schemas map is empty");
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
		Iterator<Entry<String, Document>> it = schemas.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Document> entry = it.next();
			try {
				String fName =
					new File(path, (String) entry.getKey()).getCanonicalPath();
				KXmlUtils.writeXml(fName, (Document)entry.getValue(),true,true);
			} catch (Exception ex) {
				throw new RuntimeException(
					"Could not write schema document", ex);
			}
		}
	}

	/*VT*/
	/** Read and set declared types to map.
	 * @param decl Element with declaration section.
	 * @param map with declared types.
	 */
	public static void getDeclaredTypes(final Element decl,
		final Map<String, String> map) {
		String localNamePrefix = ""; // nonempty if declaration scope is local
		if ("local".equals(getAttrValue(decl, decl.getNamespaceURI(), "scope"))
			&& "def".equals(decl.getParentNode().getLocalName())) {
			String xdName = getAttrValue(decl.getParentNode(),
				decl.getNamespaceURI(), "name");
			localNamePrefix = xdName != null ? '_' + xdName + '.' : "_.";
		}
		XScriptParser p = new XScriptParser(XConstants.XML10);
		p.setSource(new SBuffer(KXmlUtils.getTextValue(decl)), "", (byte) 10);
		while (!p.eos()) {
		   if (XScriptParser.TYPE_SYM == p.nextSymbol()) {
				int pos = p.getIndex();
				if (XScriptParser.IDENTIFIER_SYM == p.nextSymbol()) {
					char sym;
					String name = p.getParsedBufferPartFrom(pos).trim();
					pos = p.getIndex();
					while ((sym=p.nextSymbol()) != XScriptParser.SEMICOLON_SYM
						&& sym != XScriptParser.END_SYM
						&& sym != XScriptParser.NOCHAR){}
					String typeDecl = p.getParsedBufferPartFrom(pos).trim();
					if (sym != XScriptParser.NOCHAR) {
						typeDecl = typeDecl.substring(0, typeDecl.length() - 1);
					}
					map.put(localNamePrefix + name, typeDecl);
				}
		   }
		}
	}
	/*VT*/

	/** Represents qualified name. */
	public static final class MyQName {
		/** Qualified name prefix part. */
		private final String _prefix;
		/** Qualified name local name part. */
		private final String _name;

		/** Creates instance of QName with given local name part and no prefix
		 * (prefix is <tt>null</tt>).
		 * @param name qualified name local part.
		 * @throws NullPointerException if given name is <tt>null</tt>.
		 * @throws IllegalArgumentException if given name is empty.
		 */
		public MyQName(final String name) {this(null, name);}

		/** Creates instance of QName with given prefix and local name.
		 * @param prefix qualified name prefix (if is empty then
		 * it is <tt>null</tt>).
		 * @param name qualified name local part.
		 * @throws NullPointerException if given name is <tt>null</tt>.
		 * @throws IllegalArgumentException if given name is empty.
		 */
		public MyQName(final String prefix, final String name) {
			if (name.length() == 0) {
				throw new IllegalArgumentException("Given naem is empty");
			}
			_name = name;
			_prefix = (prefix != null && prefix.length() == 0) ? null : prefix;
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
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof MyQName)) {
				return false;
			}
			MyQName x = (MyQName) obj;
			return (_prefix==null?x._prefix!=null:!_prefix.equals(x._prefix))
				? false : _name.equals(x._name);
		}

		@Override
		public int hashCode() {
			return (_prefix!=null?_prefix.hashCode()*5:0) + _name.hashCode();
		}

		@Override
		public String toString() {
			return "QName[prefix='" + _prefix + "', " + "name='" + _name + "']";
		}

		/** Parses given qualified name as string and returns representation of
		 * qualified name.
		 * @param qName qualified name as string.
		 * @return representation of qualified name.
		 * @throws IllegalArgumentException if given qualified name is empty.
		 */
		public static MyQName parseQName(final String qName) {
			if (qName == null || qName.length() == 0) {
				throw new IllegalArgumentException("Qualified name is empty");
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