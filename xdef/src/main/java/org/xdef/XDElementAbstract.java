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
import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.DOCUMENT_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueType.ELEMENT;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefString;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.xml.KXmlUtils;

/** Abstract class for implementation of "pseudo" elements  in X-script.
 * @author Vaclav Trojan
 */
public abstract class XDElementAbstract extends XDValueAbstract implements Element, XDElement, NamedNodeMap {

	private final static NodeList EMPTYNODELIST = new NodeList() {

		@Override
		public Node item(int index) {return null;}

		@Override
		public int getLength() {return 0;}
	};

	private Document _doc;
	private final String _name;
	private final String _uri;
	private Map<String, Attr> _attrs;

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
	public void setAttribute(final String name, final String value) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public void removeAttribute(final String name) {throw new SUnsupportedOperationException();}


	@Override
	public Attr getAttributeNode(final String name) {
		if (_attrs == null) {
			createAttrs();
		}
		return _attrs.get(name);
	}

	@Override
	public Attr setAttributeNode(final Attr newAttr) {throw new SUnsupportedOperationException();}

	@Override
	public Attr removeAttributeNode(final Attr oldAttr){throw new SUnsupportedOperationException();}

	@Override
	public NodeList getElementsByTagName(final String name) {return EMPTYNODELIST;}

	@Override
	public String getAttributeNS(final String namespaceURI, final String localName) {
		return namespaceURI != null ? "" : getAttribute(localName);
	}

	@Override
	public void setAttributeNS(final String nsURI, final String qName, final String value) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public void removeAttributeNS(final String namespaceURI, final String localName) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Attr getAttributeNodeNS(final String namespaceURI, final String localName) {
		return namespaceURI != null ? null : getAttributeNode(localName);
	}

	@Override
	public Attr setAttributeNodeNS(final Attr newAttr) {throw new SUnsupportedOperationException();}

	@Override
	public NodeList getElementsByTagNameNS(final String nsURI, final String localName) {
		return EMPTYNODELIST;
	}

	@Override
	public boolean hasAttributeNS(final String namespaceURI, final String localName) {
		return namespaceURI != null ? false : hasAttribute(localName);
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {throw new SUnsupportedOperationException();}

	@Override
	public void setIdAttribute(final String name, final boolean isId) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public void setIdAttributeNS(final String namespaceURI, final String localName, final boolean isId) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public void setIdAttributeNode(final Attr idAttr, final boolean isId) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public String getNodeName() {return _name;}

	@Override
	public String getNodeValue() {return null;}

	@Override
	public void setNodeValue(final String nodeValue) {throw new SUnsupportedOperationException();}

	@Override
	public final short getNodeType() {return ELEMENT_NODE;}

	@Override
	public Node getParentNode() {return _doc == null ? _doc = new MyDocument(this) : _doc;}

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
	public final NamedNodeMap getAttributes() {return this;}

	@Override
	public final Document getOwnerDocument() {return _doc == null ? new MyDocument(this) : _doc;}

	@Override
	public Node insertBefore(final Node newChild, final Node refChild) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Node replaceChild(final Node newChild, final Node oldChild) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Node removeChild(final Node oldChild) {throw new SUnsupportedOperationException();}

	@Override
	public Node appendChild(final Node newChild) {throw new SUnsupportedOperationException();}

	@Override
	public boolean hasChildNodes() {return false;}

	@Override
	public Node cloneNode(final boolean deep) {throw new SUnsupportedOperationException();}

	@Override
	public void normalize() {throw new SUnsupportedOperationException();}

	@Override
	public boolean isSupported(final String feature, final String version) {return false;}

	@Override
	public String getNamespaceURI() {return null;}

	@Override
	public String getPrefix() {return null;}

	@Override
	public void setPrefix(final String prefix) {throw new SUnsupportedOperationException();}

	@Override
	public String getLocalName() {return _name;}

	@Override
	public boolean hasAttributes() {return true;}

	@Override
	public String getBaseURI() {return null;}

	@Override
	public short compareDocumentPosition(final Node other) {throw new SUnsupportedOperationException();}

	@Override
	public String getTextContent() {return null;}

	@Override
	public void setTextContent(final String content) {throw new SUnsupportedOperationException();}

	@Override
	public boolean isSameNode(Node other) {return other == this;}

	@Override
	public String lookupPrefix(final String namespaceURI) {return null;}

	@Override
	public boolean isDefaultNamespace(final String namespaceURI) {return namespaceURI == null;}

	@Override
	public String lookupNamespaceURI(final String prefix) {return null;}

	@Override
	public boolean isEqualNode(Node arg) {return arg == this;}

	@Override
	public Object getFeature(final String feature, final String version) {return null;}

	@Override
	public Object setUserData(final String key, final Object data, final UserDataHandler handler) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Object getUserData(final String key) {return null;}

	////////////////////////////////////////////////////////////////////////////
	// NamedNodeMap interface
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final Node getNamedItem(final String name) {return getAttributeNode(name);}

	@Override
	public final Node item(final int index) {
		if (_attrs == null) {
			createAttrs();
		}
		return index < _attrs.size() ? (Attr) _attrs.values().toArray()[index]: null;
	}

	@Override
	public final int getLength() {
		if (_attrs == null) {
			createAttrs();
		}
		return _attrs.size();
	}

	@Override
	public Node setNamedItem(final Node arg) throws DOMException {throw new SUnsupportedOperationException();}

	@Override
	public Node removeNamedItem(final String name) throws DOMException {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Node getNamedItemNS(final String nsURI, final String localName) {
		return getAttributeNodeNS(nsURI, localName);
	}

	@Override
	public Node setNamedItemNS(final Node arg) throws DOMException {
		throw new SUnsupportedOperationException();
	}

	@Override
	public Node removeNamedItemNS(final String nsURI, final String localName) {
		throw new SUnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////
	// XDElement interface
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final short getItemId() {return XD_ELEMENT;}

	@Override
	public final XDValueType getItemType() {return ELEMENT;}

	@Override
	public Element getElement() {return this;}

	@Override
	public Object getObject() {return this;}

	@Override
	public String getName() {return getTagName();}

	@Override
	public XDValue getXDItem(int index) {return null;}

	@Override
	public void addXDItem(XDValue value) {throw new SUnsupportedOperationException();}

	@Override
	public void addXDItem(final String value) {throw new SUnsupportedOperationException();}

	@Override
	public void addXDItem(final Element value) {throw new SUnsupportedOperationException();}

	@Override
	public XDValue replaceXDItem(final int index, final XDValue value) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public void insertXDItemBefore(final int index, final XDValue value) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public XDValue removeXDItem(final int index) {throw new SUnsupportedOperationException();}

	@Override
	public int getXDItemsNumber() {return 0;}

	@Override
	public XDValue[] getXDItems() {return new XDValue[0];}

	@Override
	public XDValue setXDNamedItem(final XDNamedValue item) {throw new SUnsupportedOperationException();}

	@Override
	public XDValue setXDNamedItem(final String name, final XDValue value) {
		throw new SUnsupportedOperationException();
	}

	@Override
	public boolean hasXDNamedItem(final String name) {return hasAttribute(name);}

	@Override
	public XDNamedValue getXDNamedItem(final String name) {
		return hasAttribute(name)? new DefNamedValue(name,new DefString(getAttribute(name))) : null;
	}

	@Override
	public String getXDNamedItemAsString(final String name) {
		String s = getAttribute(name);
		return s.isEmpty() && !hasAttribute(name) ? null : s;
	}

	@Override
	public XDValue getXDNamedItemValue(final String name) {
		return hasAttribute(name) ? new org.xdef.impl.code.DefString(getAttribute(name)) : null;
	}

	@Override
	public XDValue removeXDNamedItem(final String name){throw new SUnsupportedOperationException();}

	@Override
	public XDContainer getXDElements() {return new DefContainer();}

	@Override
	public Element getXDElement(final int n) {return null;}

	@Override
	public XDContainer getXDElements(final String name) {return new DefContainer();}

	@Override
	public XDContainer getXDElementsNS(final String nsURI,final String localName) {return new DefContainer();}

	@Override
	public String getXDText() {return null;}

	@Override
	public String getXDTextItem(final int n) {return null;}

	@Override
	public XDContainer sortXD(final boolean asc) {return this;}

	@Override
	public XDContainer sortXD(final String key, final boolean asc) {return this;}

	@Override
	public Element toElement(final String nsUri, final String name) {return this;}

	@Override
	public String toString() {return KXmlUtils.nodeToString(this);}

	/** Create XDContainer from this object.
	 * @return XDContainer constructed from this object.
	 */
	@Override
	public XDContainer toContainer() {throw new SUnsupportedOperationException();}

	////////////////////////////////////////////////////////////////////////////

	private final class MyDocument implements Document {

		private final XDElementAbstract _elem;

		MyDocument(final XDElementAbstract elem) {_elem = elem;}

		@Override
		public final DocumentType getDoctype() {return null;}

		@Override
		public final DOMImplementation getImplementation() {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Element getDocumentElement() {return _elem;}

		@Override
		public final Element createElement(final String tagName) {throw new SUnsupportedOperationException();}

		@Override
		public final DocumentFragment createDocumentFragment() {throw new SUnsupportedOperationException();}

		@Override
		public final Text createTextNode(final String data) {throw new SUnsupportedOperationException();}

		@Override
		public final Comment createComment(final String data) {throw new SUnsupportedOperationException();}

		@Override
		public final CDATASection createCDATASection(final String data) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final ProcessingInstruction createProcessingInstruction(final String target,final String data){
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Attr createAttribute(final String name) {throw new SUnsupportedOperationException();}

		@Override
		public final EntityReference createEntityReference(final String name) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final NodeList getElementsByTagName(final String tagname) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Node importNode(final Node importedNode, final boolean deep) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Element createElementNS(final String namespaceURI, final String qName) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Attr createAttributeNS(final String namespaceURI, final String qName) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final NodeList getElementsByTagNameNS(final String uri, final String localName) {
			if (localName.equals(_name) &&
				(uri==null && _uri==null ||	uri!=null && uri.equals(_uri))) {
				return getChildNodes();
			}
			return EMPTYNODELIST;
		}

		@Override
		public final Element getElementById(String elementId) {return null;}

		@Override
		public final String getInputEncoding() {return "UTF-8";}

		@Override
		public final String getXmlEncoding() {return "UTF-8";}

		@Override
		public final boolean getXmlStandalone() {return true;}

		@Override
		public final void setXmlStandalone(boolean xmlStandalone) {}

		@Override
		public final String getXmlVersion() {return "1.0";}

		@Override
		public final void setXmlVersion(String xmlVersion) {}

		@Override
		public final boolean getStrictErrorChecking() {return false;}

		@Override
		public void setStrictErrorChecking(final boolean strictErrorChecking) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final String getDocumentURI() {return null;}

		@Override
		public final void setDocumentURI(final String uti) {throw new SUnsupportedOperationException();}

		@Override
		public final Node adoptNode(final Node source) {throw new SUnsupportedOperationException();}

		@Override
		public final DOMConfiguration getDomConfig() {return null;}

		@Override
		public final void normalizeDocument() {throw new SUnsupportedOperationException();}

		@Override
		public final Node renameNode(final Node n, final String namespaceURI, final String qualifiedName) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final String getNodeName() {return "#document";}

		@Override
		public final String getNodeValue() {return null;}

		@Override
		public final void setNodeValue(final String nodeValue) {throw new SUnsupportedOperationException();}

		@Override
		public final short getNodeType() {return DOCUMENT_NODE;}

		@Override
		public final Node getParentNode() {return null;}

		@Override
		public final NodeList getChildNodes() {
			return new NodeList() {

				@Override
				public final Node item(final int index) {
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
		public final NamedNodeMap getAttributes() {return null;}

		@Override
		public final Document getOwnerDocument() {return this;}

		@Override
		public final Node insertBefore(final Node newChild, final Node refChild) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Node replaceChild(final Node newChild, final Node oldChild) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Node removeChild(final Node oldChild) {throw new SUnsupportedOperationException();}

		@Override
		public final Node appendChild(final Node newChild) {throw new SUnsupportedOperationException();}

		@Override
		public final boolean hasChildNodes() {return true;}

		@Override
		public final Node cloneNode(final boolean deep){throw new SUnsupportedOperationException();}

		@Override
		public final void normalize() {throw new SUnsupportedOperationException();}

		@Override
		public final boolean isSupported(final String feature, final String version) {return false;}

		@Override
		public final String getNamespaceURI() {return null;}

		@Override
		public final String getPrefix() {return null;}

		@Override
		public final void setPrefix(final String prefix) {throw new SUnsupportedOperationException();}

		@Override
		public final String getLocalName() {return "#document";}

		@Override
		public final boolean hasAttributes() {return false;}

		@Override
		public final String getBaseURI() {return null;}

		@Override
		public final short compareDocumentPosition(final Node x) {throw new SUnsupportedOperationException();}

		@Override
		public final String getTextContent() {return null;}

		@Override
		public final void setTextContent(final String text) {throw new SUnsupportedOperationException();}

		@Override
		public boolean isSameNode(Node other) {return other == this;}

		@Override
		public final String lookupPrefix(final String namespaceURI) {return null;}

		@Override
		public final boolean isDefaultNamespace(final String s) {throw new SUnsupportedOperationException();}

		@Override
		public final String lookupNamespaceURI(final String prefix) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final boolean isEqualNode(final Node arg) {return arg == this;}

		@Override
		public final Object getFeature(final String feature, final String version) {return null;}

		@Override
		public final Object setUserData(final String key, final Object data, final UserDataHandler h) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Object getUserData(final String key) {return null;}
	}

	private void createAttrs() {
		_attrs = new LinkedHashMap<>();
		for (int i = 0; i < getXDNamedItemsNumber(); i++) {
			String name = getXDNamedItemName(i);
			if (name != null && hasAttribute(name)) {
				_attrs.put(name, new MyAttr(name, getAttribute(name), this));
			}
		}
	}

	protected Attr createAttr(final String name,final  String val) {return new MyAttr(name, val, this);}

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
		public final String getName() {return _name;}

		@Override
		public final boolean getSpecified() {return true;}

		@Override
		public final String getValue() {return _value;}

		@Override
		public final void setValue(final String value) {throw new SUnsupportedOperationException();}

		@Override
		public final Element getOwnerElement() {return _elem;}

		@Override
		public final TypeInfo getSchemaTypeInfo() {return null;}

		@Override
		public final boolean isId() {return false;}

		@Override
		public final String getNodeName() {return _name;}

		@Override
		public final String getNodeValue() {return _value;}

		@Override
		public final void setNodeValue(final String nodeValue) {throw new SUnsupportedOperationException();}

		@Override
		public final short getNodeType() {return ATTRIBUTE_NODE;}

		@Override
		public final Node getParentNode() {return _elem;}

		@Override
		public final NodeList getChildNodes() {return null;}

		@Override
		public final Node getFirstChild() {return null;}

		@Override
		public final Node getLastChild() {return null;}

		@Override
		public final Node getPreviousSibling() {return null;}

		@Override
		public final Node getNextSibling() {return null;}

		@Override
		public final NamedNodeMap getAttributes() {return null;}

		@Override
		public final Document getOwnerDocument() {return _elem.getOwnerDocument();}

		@Override
		public final Node insertBefore(final Node newChild, final Node refChild) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Node replaceChild(final Node newChild, final Node oldChild) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Node removeChild(final Node oldChild) {throw new SUnsupportedOperationException();}

		@Override
		public final Node appendChild(final Node newChild) {throw new SUnsupportedOperationException();}

		@Override
		public final boolean hasChildNodes() {return false;}

		@Override
		public final Node cloneNode(final boolean deep){throw new SUnsupportedOperationException();}

		@Override
		public final void normalize() {throw new SUnsupportedOperationException();}

		@Override
		public final boolean isSupported(final String feature, final String version) {return false;}

		@Override
		public final String getNamespaceURI() {return null;}

		@Override
		public final String getPrefix() {return null;}

		@Override
		public final void setPrefix(final String prefix) {throw new SUnsupportedOperationException();}

		@Override
		public final String getLocalName() {return _name;}

		@Override
		public final boolean hasAttributes() {return false;}

		@Override
		public final String getBaseURI() {return null;}

		@Override
		public final short compareDocumentPosition(final Node x) {throw new SUnsupportedOperationException();}

		@Override
		public final String getTextContent() {return null;}

		@Override
		public final void setTextContent(final String text) {throw new SUnsupportedOperationException();}

		@Override
		public final boolean isSameNode(final Node other) {return other == this;}

		@Override
		public final String lookupPrefix(final String namespaceURI) {return null;}

		@Override
		public final boolean isDefaultNamespace(final String nsURI) {return nsURI == null;}

		@Override
		public final String lookupNamespaceURI(final String prefix) {return null;}

		@Override
		public final boolean isEqualNode(final Node arg) {return arg == this;}

		@Override
		public final Object getFeature(final String feature, final String version) {return null;}

		@Override
		public final Object setUserData(final String key, final Object data, final UserDataHandler h) {
			throw new SUnsupportedOperationException();
		}

		@Override
		public final Object getUserData(final String key) {return null;}
	}
}