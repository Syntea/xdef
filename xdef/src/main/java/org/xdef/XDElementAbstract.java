package org.xdef;

import java.util.Map;
import java.util.LinkedHashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.DOMConfiguration;
import static org.xdef.XDValueType.ELEMENT;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.xml.KXmlUtils;

/** Abstract class for implementation of "pseudo" elements.
 * @author Vaclav Trojan
 */
public abstract class XDElementAbstract extends XDValueAbstract
implements Element, XDElement, XDValue, NamedNodeMap {

	private static final NodeList EMPTYNODELIST = new NodeList() {
		@Override
		public Node item(int index) {return null;}
		@Override
		public int getLength() {return 0;}
	};

	private Document _doc;
	private final String _name;
	private final String _uri;
	Map<String, Attr> _attrs;

	public XDElementAbstract() {_name = "_"; _uri = null;}
	public XDElementAbstract(String name) {_name = name; _uri = null;}
	public XDElementAbstract(String uri, String name) {_name=name; _uri=uri;}

	@Override
	public abstract String getAttribute(String name);
	@Override
	public abstract boolean hasAttribute(String name);
	@Override
	public abstract int getXDNamedItemsNumber();
	@Override
	public abstract String getXDNamedItemName(int index);
	@Override
	public final String getTagName() {return _name;}
	@Override
	public void setAttribute(String name, String value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void removeAttribute(String name) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Attr getAttributeNode(String name) {
		if (_attrs == null) {
			createAttrs();
		}
		return _attrs.get(name);
	}
	@Override
	public Attr setAttributeNode(Attr newAttr) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Attr removeAttributeNode(Attr oldAttr) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public NodeList getElementsByTagName(String name) {return EMPTYNODELIST;}
	@Override
	public String getAttributeNS(String namespaceURI, String localName) {
		return namespaceURI != null ? "" : getAttribute(localName);
	}
	@Override
	public void setAttributeNS(String nsURI, String qName, String value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void removeAttributeNS(String namespaceURI, String localName) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Attr getAttributeNodeNS(String namespaceURI, String localName) {
		return namespaceURI != null ? null : getAttributeNode(localName);
	}
	@Override
	public Attr setAttributeNodeNS(Attr newAttr) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public NodeList getElementsByTagNameNS(String nsURI, String localName) {
		return EMPTYNODELIST;
	}
	@Override
	public boolean hasAttributeNS(String namespaceURI, String localName) {
		return namespaceURI != null ? false : hasAttribute(localName);
	}
	@Override
	public TypeInfo getSchemaTypeInfo() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void setIdAttribute(String name, boolean isId) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void setIdAttributeNS(String namespaceURI,
		String localName,
		boolean isId) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void setIdAttributeNode(Attr idAttr, boolean isId) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public String getNodeName() {return _name;}
	@Override
	public String getNodeValue() {return null;}
	@Override
	public void setNodeValue(String nodeValue) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public short getNodeType() {return ELEMENT_NODE;}
	@Override
	public final Node getParentNode() {
		return _doc == null ? _doc = new MyDocument(this) : _doc;
	}
	@Override
	public NodeList getChildNodes() {return EMPTYNODELIST;}
	@Override
	public Node getFirstChild() {return null;}
	@Override
	public Node getLastChild() {return null;}
	@Override
	public Node getPreviousSibling() {return null;}
	@Override
	public Node getNextSibling() {return null;}
	@Override
	public final NamedNodeMap getAttributes() {
		return this;
	}
	@Override
	public final Document getOwnerDocument() {
		if (_doc == null) {
			_doc = new MyDocument(this);
		}
		return _doc;
	}
	@Override
	public Node insertBefore(Node newChild, Node refChild) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node replaceChild(Node newChild, Node oldChild) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node removeChild(Node oldChild) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node appendChild(Node newChild) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public boolean hasChildNodes() {return false;}
	@Override
	public Node cloneNode(boolean deep) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void normalize() {
		throw new SUnsupportedOperationException();
	}
	@Override
	public boolean isSupported(String feature, String version) {return false;}
	@Override
	public String getNamespaceURI() {return null;}
	@Override
	public String getPrefix() {return null;}
	@Override
	public void setPrefix(String prefix) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public String getLocalName() {return _name;}
	@Override
	public boolean hasAttributes() {return true;}
	@Override
	public String getBaseURI() {return null;}
	@Override
	public short compareDocumentPosition(Node other) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public String getTextContent() {return null;}
	@Override
	public void setTextContent(String textContent) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public boolean isSameNode(Node other) {return other == this;}
	@Override
	public String lookupPrefix(String namespaceURI) {return null;}
	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		return namespaceURI == null;
	}
	@Override
	public String lookupNamespaceURI(String prefix) {return null;}
	@Override
	public boolean isEqualNode(Node arg) {return arg == this;}
	@Override
	public Object getFeature(String feature, String version) {return null;}
	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Object getUserData(String key) {return null;}

	////////////////////////////////////////////////////////////////////////////
	// NamedNodeMap interface
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Node getNamedItem(final String name) {
		return getAttributeNode(name);
	}
	@Override
	public final Node item(final int index) {
		if (_attrs == null) {
			createAttrs();
		}
		return index < _attrs.size() ?
			(Attr) _attrs.values().toArray()[index]: null;
	}
	@Override
	public final int getLength() {
		if (_attrs == null) {
			createAttrs();
		}
		return _attrs.size();
	}

	@Override
	public Node setNamedItem(Node arg) throws DOMException {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node removeNamedItem(String name) throws DOMException {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node getNamedItemNS(String nsURI, String localName) {
		return getAttributeNodeNS(nsURI, localName);
	}
	@Override
	public Node setNamedItemNS(Node arg) throws DOMException {
		throw new SUnsupportedOperationException();
	}
	@Override
	public Node removeNamedItemNS(String nsURI, String localName) {
		throw new SUnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////
	// XDElement interface
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final short getItemId() {return XD_ELEMENT;}
	@Override
	public XDValueType getItemType() {return ELEMENT;}
	@Override
	public Element getElement() {return this;}
	@Override
	public Object getObject() {return this;}
	@Override
	public String getName() {return getTagName();}
	@Override
	public XDValue getXDItem(int index) {return null;}
	@Override
	public void addXDItem(XDValue value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void addXDItem(String value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void addXDItem(Element value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public XDValue replaceXDItem(final int index, XDValue value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public void insertXDItemBefore(int index, XDValue value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public XDValue removeXDItem(int index) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public int getXDItemsNumber() {return 0;}
	@Override
	public XDValue[] getXDItems() {return new XDValue[0];}
	@Override
	public XDValue setXDNamedItem(XDNamedValue item) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public XDValue setXDNamedItem(String name, XDValue value) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public boolean hasXDNamedItem(String name) {return hasAttribute(name);}
	@Override
	public XDNamedValue getXDNamedItem(String name) {
		return hasAttribute(name) ?
			new org.xdef.impl.code.DefNamedValue(name,
				new org.xdef.impl.code.DefString(getAttribute(name))) : null;
	}
	@Override
	public String getXDNamedItemAsString(String name) {
		String s = getAttribute(name);
		return s.isEmpty() && !hasAttribute(name) ? null : s;
	}
	@Override
	public XDValue getXDNamedItemValue(String name) {
		return hasAttribute(name) ?
			new org.xdef.impl.code.DefString(getAttribute(name)) : null;
	}
	@Override
	public XDValue removeXDNamedItem(String name) {
		throw new SUnsupportedOperationException();
	}
	@Override
	public XDContainer getXDElements() {
		return new org.xdef.impl.code.DefContainer();
	}
	@Override
	public Element getXDElement(int n) {return null;}
	@Override
	public XDContainer getXDElements(String name) {
		return new org.xdef.impl.code.DefContainer();
	}
	@Override
	public XDContainer getXDElementsNS(String nsURI, String localName) {
		return new org.xdef.impl.code.DefContainer();
	}
	@Override
	public String getXDText() {return null;}
	@Override
	public String getXDTextItem(int n) {return null;}
	@Override
	public XDContainer sortXD(boolean asc) {return this;}
	@Override
	public XDContainer sortXD(String key, boolean asc) {return this;}
	@Override
	public Element toElement(String nsUri, String name) {return this;}

	@Override
	public String toString() {
		return KXmlUtils.nodeToString(this);
	}

	@Override
	/** Create XDContainer from this object.
	 * @return XDContainer constructed from this object.
	 */
	public XDContainer toContainer() {
		throw new SUnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////

	private final class MyDocument implements Document {

		private final XDElementAbstract _elem;

		MyDocument(XDElementAbstract elem) {_elem = elem;}

		@Override
		public DocumentType getDoctype() {return null;}
		@Override
		public DOMImplementation getImplementation() {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final Element getDocumentElement() {return _elem;}
		@Override
		public Element createElement(String tagName) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public DocumentFragment createDocumentFragment() {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Text createTextNode(String data) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Comment createComment(String data) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public CDATASection createCDATASection(String data) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public ProcessingInstruction createProcessingInstruction(String target,
			String data) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Attr createAttribute(String name) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public EntityReference createEntityReference(String name) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public NodeList getElementsByTagName(String tagname) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node importNode(Node importedNode, boolean deep) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Element createElementNS(String namespaceURI, String qName) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Attr createAttributeNS(String namespaceURI, String qName) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public NodeList getElementsByTagNameNS(String uri, String localName) {
			if (localName.equals(_name) &&
				(uri==null && _uri==null ||	uri!=null && uri.equals(_uri))) {
				return getChildNodes();
			}
			return EMPTYNODELIST;
		}
		@Override
		public Element getElementById(String elementId) {return null;}
		@Override
		public String getInputEncoding() {return "UTF-8";}
		@Override
		public final String getXmlEncoding() {return "UTF-8";}
		@Override
		public boolean getXmlStandalone() {return true;	}
		@Override
		public void setXmlStandalone(boolean xmlStandalone) {}
		@Override
		public final String getXmlVersion() {return "1.0";}
		@Override
		public void setXmlVersion(String xmlVersion) {}
		@Override
		public boolean getStrictErrorChecking() {return false;}
		@Override
		public void setStrictErrorChecking(boolean strictErrorChecking) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final String getDocumentURI() {return null;}
		@Override
		public void setDocumentURI(String documentURI) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node adoptNode(Node source) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public DOMConfiguration getDomConfig() {return null;}
		@Override
		public void normalizeDocument() {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node renameNode(Node n, String namespaceURI, String qualifiedName) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final String getNodeName() {return "#document";}
		@Override
		public final String getNodeValue() {return null;}
		@Override
		public void setNodeValue(String nodeValue) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final short getNodeType() {return DOCUMENT_NODE;}
		@Override
		public final Node getParentNode() {return null;}
		@Override
		public final NodeList getChildNodes() {
			return new NodeList() {
				@Override
				public final Node item(int index) {
					return index == 0 ? _elem : null;
				}
				@Override
				public final int getLength() {return 1;}
			};
		}
		@Override
		public final Node getFirstChild() {return _elem;}
		@Override
		public final Node getLastChild() {return _elem;}
		@Override
		public final Node getPreviousSibling() {return null;}
		@Override
		public final Node getNextSibling() {return null;}
		@Override
		public NamedNodeMap getAttributes() {return null;}
		@Override
		public Document getOwnerDocument() {return this;}
		@Override
		public Node insertBefore(Node newChild, Node refChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node replaceChild(Node newChild, Node oldChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node removeChild(Node oldChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node appendChild(Node newChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final boolean hasChildNodes() {return true;}
		@Override
		public Node cloneNode(boolean deep) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public void normalize() {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean isSupported(String feature, String version) {
			return false;
		}
		@Override
		public final String getNamespaceURI() {return null;}
		@Override
		public final String getPrefix() {return null;}
		@Override
		public void setPrefix(String prefix) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public final String getLocalName() {return "#document";}
		@Override
		public boolean hasAttributes() {return false;}
		@Override
		public String getBaseURI() {return null;}
		@Override
		public short compareDocumentPosition(Node other) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public String getTextContent() {return null;}
		@Override
		public void setTextContent(String textContent) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean isSameNode(Node other) {return other == this;}
		@Override
		public String lookupPrefix(String namespaceURI) {return null;}
		@Override
		public boolean isDefaultNamespace(String namespaceURI) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public String lookupNamespaceURI(String prefix) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean isEqualNode(Node arg) {return arg == this;}
		@Override
		public Object getFeature(String feature, String version) {
			return null;
		}
		@Override
		public Object setUserData(String key, Object data, UserDataHandler h) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Object getUserData(String key) {return null;}
	}

	private void createAttrs() {
		_attrs = new LinkedHashMap<>();
		for (int i = 0; i < getXDNamedItemsNumber(); i++) {
			String name = getXDNamedItemName(i);
			if (name != null && hasAttribute(name)) {
				String val = getAttribute(name);
				_attrs.put(name, new MyAttr(name, val, this));
			}
		}
	}

	protected Attr createAttr(String name, String val) {
		return new MyAttr(name, val, this);
	}

	final class MyAttr implements Attr {

		private final String _name;
		private final String _value;
		private final Element _elem;

		MyAttr(String name, String value, Element elem) {
			_name = name;
			_value = value;
			_elem = elem;
		}
		@Override
		public String getName() {return _name;}
		@Override
		public boolean getSpecified() {return true;}
		@Override
		public String getValue() {return _value;}
		@Override
		public void setValue(String value) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Element getOwnerElement() {return _elem;}
		@Override
		public TypeInfo getSchemaTypeInfo() {return null;}
		@Override
		public boolean isId() {return false;}
		@Override
		public String getNodeName() {return _name;}
		@Override
		public String getNodeValue() {return _value;}
		@Override
		public void setNodeValue(String nodeValue) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public short getNodeType() {return ATTRIBUTE_NODE;}
		@Override
		public Node getParentNode() {return _elem;}
		@Override
		public NodeList getChildNodes() {return null;}
		@Override
		public Node getFirstChild() {return null;}
		@Override
		public Node getLastChild() {return null;}
		@Override
		public Node getPreviousSibling() {return null;}
		@Override
		public Node getNextSibling() {return null;}
		@Override
		public NamedNodeMap getAttributes() {return null;}
		@Override
		public Document getOwnerDocument() {return _elem.getOwnerDocument();}
		@Override
		public Node insertBefore(Node newChild, Node refChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node replaceChild(Node newChild, Node oldChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node removeChild(Node oldChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Node appendChild(Node newChild) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean hasChildNodes() {return false;}
		@Override
		public Node cloneNode(boolean deep) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public void normalize() {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean isSupported(String feature, String version) {
			return false;
		}
		@Override
		public String getNamespaceURI() {return null;}
		@Override
		public String getPrefix() {return null;}
		@Override
		public void setPrefix(String prefix) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public String getLocalName() {return _name;}
		@Override
		public boolean hasAttributes() {return false;}
		@Override
		public String getBaseURI() {return null;}
		@Override
		public short compareDocumentPosition(Node other) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public String getTextContent() {return null;}
		@Override
		public void setTextContent(String textContent) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public boolean isSameNode(Node other) {return other == this;}
		@Override
		public String lookupPrefix(String namespaceURI) {return null;}
		@Override
		public boolean isDefaultNamespace(String namespaceURI) {
			return namespaceURI == null;
		}
		@Override
		public String lookupNamespaceURI(String prefix) {return null;}
		@Override
		public boolean isEqualNode(Node arg) {return arg == this;}
		@Override
		public Object getFeature(String feature, String version) {
			return null;
		}
		@Override
		public Object setUserData(String key, Object data, UserDataHandler h) {
			throw new SUnsupportedOperationException();
		}
		@Override
		public Object getUserData(String key) {return null;}
	}
}