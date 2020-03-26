package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDElement;
import org.xdef.XDNamedValue;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDValueType;

/** Implementation of item with org.w3c.dom.Element.
 * @author Vaclav Trojan
 */
public final class DefElement extends XDValueAbstract implements XDElement {

	/** The element as value of this item. */
	private final Element _value;

	/** Creates a new instance of DefElement. */
	public DefElement() {_value = null;}

	/** Creates a new instance of DefElement.
	 * @param value The element to be set as value of the item.
	 */
	public DefElement(final Element value) {_value = value;}

	/** Creates a new instance of DefElement and create nwe empty element
	 * with given name.
	 * @param doc The document where the new element will be created.
	 * @param name The name of element.
	 */
	public DefElement(final Document doc, final String name) {
		_value = doc.createElementNS(null, name);
	}

	/** Creates a new instance of DefElement and create nwe empty element
	 * with given name.
	 * @param doc The document where the new element will be created.
	 * @param name The name of element.
	 * @param ns namespace uri.
	 */
	public DefElement(final Document doc, final String ns, final String name) {
		if (ns == null || ns.length() == 0) {
			_value = doc.createElementNS(null, name);
		} else {
			_value = doc.createElementNS(ns, name);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Implementation of XDElement
	////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get name of underlying element or empty string.
	 * @return element name or empty string.
	 */
	public String getName() {
		return _value == null ? "" : _value.getNodeName();
	}

	@Override
	/** Get namespace URI of underlying element or null.
	 * @return namespace URI of underlying element or null.
	 */
	public String getNamespaceURI() {
		return _value == null ? null : _value.getNamespaceURI();
	}

	@Override
	/** Get local name of underlying element or the empty string.
	 * @return local name of underlying element or the empty string.
	 */
	public String getLocalName() {
		if (_value == null) {
			return null;
		}
		String s = _value.getLocalName();
		return s == null ? _value.getNodeName() : s;
	}

	private static DefContainer elementToContext(Element el) {
		DefContainer c = new DefContainer();
		String name = el.getNodeName();
		String nsuri = el.getNamespaceURI();
		if (nsuri != null) {
			int ndx = name.indexOf(':');
			String s = "xmlns" + (ndx > 0 ? ':' + name.substring(0, ndx) : "");
			c.setXDNamedItem(s, new DefString(nsuri));
		}
		NamedNodeMap nnm = el.getAttributes();
		for (int i = 0; nnm != null && i < nnm.getLength(); i++) {
			Node x = nnm.item(i);
			c.setXDNamedItem(x.getNodeName(), new DefString(x.getNodeValue()));
		}
		NodeList nl = el.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node x = nl.item(i);
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				c.addXDItem(elementToContext((Element) x));
			} else if (x.getNodeType() == Node.TEXT_NODE
				|| x.getNodeType() == Node.CDATA_SECTION_NODE) {
				String s = x.getNodeValue();
				while (i + 1 < nl.getLength()
					&& ((x = nl.item(i + 1)).getNodeType() == Node.TEXT_NODE
					|| x.getNodeType() == Node.CDATA_SECTION_NODE)) {
					s += x.getNodeValue();
					i++;
				}
				c.addXDItem(s);
			}
		}
		DefContainer c1 = new DefContainer();
		if (c.isEmpty() || c.isNull()) {
			c1.setXDNamedItem(el.getNodeName(), null);
		} else {
			c1.setXDNamedItem(el.getNodeName(), c);
		}
		return c1;
	}

	@Override
	/** Create XDContext from this object.
	 * @return XDContext constructed from this object.
	 */
	public XDContainer toContext() {
		return _value == null ? new DefContainer() : elementToContext(_value);
	}

	////////////////////////////////////////////////////////////////////////////
	// Implementation of XDContainer
	////////////////////////////////////////////////////////////////////////////
	@Override
	public XDValue getXDItem(final int index) {
		Node n;
		if (_value == null || (n = _value.getChildNodes().item(index)) == null){
			return null;
		}
		switch (n.getNodeType()) {
			case Node.ELEMENT_NODE:
				return new DefElement((Element) n);
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				return new DefText((CharacterData) n);
//			case Node.COMMENT_NODE:
//				return new DefCommentNode((Comment) n);
//			case Node.PROCESSING_INSTRUCTION_NODE:
//				return new DefPINode((ProcessingInstruction) n);
			default:
				return null;
		}
	}

	@Override
	public void addXDItem(final XDValue value) {}//ignore

	@Override
	public void addXDItem(final String value) {}//ignore

	@Override
	public void addXDItem(final Element value) {}//ignore

	@Override
	public XDValue replaceXDItem(final int index, XDValue value) {return null;}

	@Override
	public void insertXDItemBefore(final int index, final XDValue value){}

	@Override
	public XDValue removeXDItem(final int index) {return null;}//ignore

	@Override
	public int getXDItemsNumber() {
		return _value == null ? 0 : _value.getChildNodes().getLength();

	}

	@Override
	public XDValue[] getXDItems() {
		if (_value == null) {
			return null;
		}
		DefContainer d = new DefContainer(_value.getChildNodes());
		return d.getXDItems();
	}

	@Override
	public XDValue setXDNamedItem(final XDNamedValue item) {return null;}//ignore

	@Override
	public XDValue setXDNamedItem(final String name, final XDValue value) {
		return null; //ignore
	}

	@Override
	public boolean hasXDNamedItem(final String name) {
		return _value != null && _value.hasAttribute(name);
	}

	@Override
	public XDNamedValue getXDNamedItem(final String name) {
		return _value != null && _value.hasAttribute(name) ?
			new DefAttr(_value.getAttributeNode(name)) : null;
	}

	@Override
	public String getXDNamedItemAsString(final String name) {
		return _value != null && _value.hasAttribute(name) ?
			_value.getAttribute(name) :	null;
	}

	@Override
	public XDValue getXDNamedItemValue(final String name) {
		return _value != null && _value.hasAttribute(name) ?
			new DefString(_value.getAttribute(name)) : null;
	}

	@Override
	public XDValue removeXDNamedItem(final String name) { return null;}

	@Override
	public int getXDNamedItemsNumber() {
		NamedNodeMap nnm = _value == null ? null : _value.getAttributes();
		return nnm == null ? 0 : nnm.getLength();
	}

	@Override
	/** Get name of i-th named item.
	 * @param index index of item.
	 * @return name of item.
	 */
	public String getXDNamedItemName(final int index) {
		NamedNodeMap nnm = _value == null ? null : _value.getAttributes();
		return nnm == null ? null : nnm.item(index).getNodeName();
	}

	@Override
	/** Get array with named items in the table.
	 * @return array with named items or null.
	 */
	public final XDNamedValue[] getXDNamedItems() {
		NamedNodeMap nnm = _value == null ? null : _value.getAttributes();
		if (nnm == null) {
			return null;
		}
		XDNamedValue[] result = new XDNamedValue[nnm.getLength()];
		for (int i = 0; i < result.length; i++) {
			Node n = nnm.item(i);
			result[i] = new DefNamedValue(n.getNodeName(),
				new DefString(n.getNodeValue()));
		}
		return result;
	}

	@Override
	/** Create new XDContext with all elements from context.
	 * @return The new XDContext with elements.
	 */
	public XDContainer getXDElements() {
		return new DefContainer(KXmlUtils.getChildElements(_value));

	}

	@Override
	/** Get the n-th element from context or null.
	 * @param n The index of element.
	 * @return the n-th element from context or null..
	 */
	public Element getXDElement(final int n) {
		NodeList nl = KXmlUtils.getChildElements(_value);
		return n < nl.getLength() ?
			(Element) KXmlUtils.getChildElements(_value).item(n) : null;
	}

	@Override
	/** Get all elements with given name from context.
	 * @param name The name of element.
	 * @return The new context with elements.
	 */
	public XDContainer getXDElements(final String name) {
		return new DefContainer(KXmlUtils.getChildElements(_value, name));
	}

	@Override
	/** Get all elements with given name and namespace from context.
	 * @param nsURI namespace URI.
	 * @param localName local name of element.
	 * @return The new context with all elements with given name and namespace.
	 */
	public XDContainer getXDElementsNS(final String nsURI,final String localName){
		return new DefContainer(KXmlUtils.getChildElementsNS(_value,
			nsURI, localName));
	}

	@Override
	/** Get all text nodes concatenated as a string.
	 * @return The string with all text nodex.
	 */
	public String getXDText() {
		return KXmlUtils.getTextValue(_value);
	}

	@Override
	/** Get string form text node with index i. If the node does not exist or if
	 * it is not text node return empty string.
	 * @param index The index of node item.
	 * @return The string.
	 */
	public String getXDTextItem(final int index) {
		NodeList nl = KXmlUtils.getTextNodes(_value, true);
		return index < nl.getLength() ? nl.item(index).getNodeValue() : "";
	}

	@Override
	/** Get new element from this one.
	 * @param nsUri of created element
	 * @param name of created element.
	 * @return element created from this context.
	 */
	public final Element toElement(final String nsUri, final String name) {
		Document doc = KXmlUtils.newDocument(nsUri, name, null);
		Element el = doc.getDocumentElement();
		if (_value != null) {
			NamedNodeMap nm = _value.getAttributes();
			if (nm != null) {
				for (int i = 0; i < nm.getLength(); i++) {
					Node n = nm.item(i);
					String uri = n.getNamespaceURI();
					if (uri == null) {
						el.setAttribute(n.getNodeName(), n.getNodeValue());
					} else {
						el.setAttributeNS(uri,
							n.getNodeName(), n.getNodeValue());
					}
				}
			}
			NodeList nl = _value.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				el.appendChild(doc.importNode(nl.item(i), true));
			}
		}
		return el;
	}

	@Override
	/** Sorts this context.
	 * If an item is an org.w3c.Node object then as a key it is used
	 * the text value of an item).
	 * @param asc if true context will be sorted ascendant, else descendant.
	 */
	public XDContainer sortXD(final boolean asc) {
		return new DefContainer(_value.getChildNodes()).sortXD(asc);
	}

	@Override
	/** Sorts this context.
	 * @param key String with xpath expression or null (if null or empty string
	 * then for org.w3c.Node items it is used as a key the text value of
	 * an item). For items other then  org.w3c.Node objects this parameter is
	 * ignored.
	 * @param asc if true context will be sorted ascendant, else descendant.
	 */
	public XDContainer sortXD(String key, boolean asc) {
		return new DefContainer(_value.getChildNodes()).sortXD(key, asc);
	}

	////////////////////////////////////////////////////////////////////////////
	// Implementation of XDValue interface
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Element getElement() {return _value;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_ELEMENT;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.ELEMENT;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {
		return _value == null ? "" : KXmlUtils.nodeToString(_value);
	}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return KXmlUtils.getTextValue(_value);}

	@Override
	/** Clone the item - here return the object itself.
	 * @return this object.
	 */
	public XDValue cloneItem() {return this;}

	@Override
	public int hashCode() {return _value.hashCode();}

	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof DefElement) {
			return equals((DefElement) arg);
		}
		return false;
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_ELEMENT) {
			return false;
		}
		return _value.equals(((DefElement) arg)._value);
	}

	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) throws SIllegalArgumentException {
		if (equals(arg)) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	/** Compares this object with the other DefElement object.
	 * @param arg other DefElement object to which is to be compared.
	 * @return returns -1, 0, or a 1 as this object is less than, equal to, or
	 * greater than the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final DefElement arg) throws SIllegalArgumentException{
		if (equals(arg)) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return _value == null;}

	@Override
	/** Check if the object is empty.
	 * @return <tt>true</tt> if the object is empty; otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isEmpty() {
		NodeList nl = _value.getChildNodes();
		NamedNodeMap nnm = _value.getAttributes();
		return nl.getLength() == 0 && (nnm == null || nnm.getLength() == 0);
	}
}