package org.xdef.impl.code;

import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXpathExpr;
import org.xdef.XDResultSet;
import org.xdef.proc.XXNode;
import org.xdef.XDValue;
import org.xdef.impl.xml.KNamespace;
import org.xdef.XDConstructor;
import org.xdef.XDStatement;
import org.xdef.XDValueAbstract;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.RESULTSET;
import org.xdef.sys.StringParser;

/** Implementation of XML iterator.
 * @author Vaclav Trojan
 */
public class DefXmlIterator extends XDValueAbstract implements XDResultSet {
	private String _xpath;
	private NodeList _list;
	private Node _item;
	private int _index = -1;
	private KNamespace _nc; //will be set from xnode
	private XPathFunctionResolver _fr; //will be set from xnode
	private XPathVariableResolver _vr; //will be set from xnode
	private XDConstructor _constructor;

	public DefXmlIterator() {}

	public DefXmlIterator(XXNode chkEl, Node node, String xpath) {
		_nc = chkEl.getXXNamespaceContext();
		_fr = chkEl.getXXFunctionResolver();
		_vr = chkEl.getXXVariableResolver();
		_xpath = xpath;
		try {
			KXpathExpr expr = new KXpathExpr(xpath, _nc, _fr, _vr);
			_list = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
			_index = -1;
		} catch (Exception ex) {
			throw new RuntimeException("XPath error");
		}
	}

	public DefXmlIterator(NodeList nl) {
		_list = nl;
		_index = -1;
	}

	public DefXmlIterator(Element el) {
		class MyNodeList implements NodeList {
			Element _el;
			MyNodeList(Element el) {_el = el;}
			@Override
			public Node item(int index) {
				return index == 0 ? _el : null;
			}
			@Override
			public int getLength() {return 1;}
		}
		_list = new MyNodeList(el);
		_index = -1;
	}

	public DefXmlIterator(KXpathExpr expr, Node node) {
		try {
			_list = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
			_index = -1;
			_xpath = expr.toString();
		} catch (Exception ex) {
			throw new RuntimeException("XPath error");
		}
	}

	private XDValue nodeToXD(Node node) {
		if (node == null) {
			return null;
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			return new DefElement((Element) node);
		} else if (node.getNodeType() == Node.ATTRIBUTE_NODE ||
			node.getNodeType() == Node.TEXT_NODE) {
			return new DefString(node.getNodeValue());
		} else {
			return null;
		}
	}

	@Override
	public XDValue nextXDItem(XXNode xnode) {
		if (_list == null || _list.getLength() == ++_index) {
			try {
				close();
			} catch (Exception ex) {}
			return null;
		}
		return nodeToXD(_item = _list.item(_index - 1));
	}

	@Override
	public XDValue lastXDItem() {return nodeToXD(_item);}

	@Override
	public int getCount() {return _index;}

	@Override
	public String itemAsString() {return KXmlUtils.getTextValue(_item);}

	@Override
	public String itemAsString(int index) {
		return index < _list.getLength() ?KXmlUtils.getTextValue(_list.item(index)) : null;
	}

	@Override
	public String itemAsString(String name) {
		NamedNodeMap nm = _item.getAttributes();
		if (nm == null) {
			return null;
		}
		String s = name.startsWith("@") ? name.substring(1) : name;
		if (StringParser.chkXMLName(s, (byte) 10)) {
			int i;
			Node n;
			if ((i = s.indexOf(':')) < 0) {
				n = nm.getNamedItem(s);
			} else {
				String prefix = s.substring(0, i);
				String uri;
				if (_nc != null) {
					uri = _nc.getNamespaceURI(prefix);
				} else {
					uri = _item.lookupNamespaceURI(prefix);
				}
				if (uri == null) {
					return null;
				}
				n = nm.getNamedItemNS(uri, s.substring(i + 1));
			}
			return n == null ? null : n.getNodeValue();
		}
		KXpathExpr expr = new KXpathExpr(name, _nc, _fr, _vr);
		return (String) expr.evaluate(_item, XPathConstants.STRING);
	}

	/** If the iterated object contains the specified item then return true.
	 * @param name name item.
	 * @return true if and only if the specified item exists.
	 */
	@Override
	public boolean hasItem(String name) {return itemAsString(name) != null;}

	@Override
	public int getSize() {return _list == null ? -1 : _list.getLength();}

	/** Get statement from which ResultSet was created.
	 * @return null here.
	 */
	@Override
	public XDStatement getStatement() {return null;}

	/** Get constructor for creation of item.
	 * @return constructor for creation of item.
	 */
	@Override
	public XDConstructor getXDConstructor() {return _constructor;}

	/** Set constructor for creation of item.
	 * @param constructor constructor for creation of item.
	 */
	@Override
	public void setXDConstructor(XDConstructor constructor) {_constructor = constructor;}

	@Override
	public void close() {
		_list = null;
		_index = 0;
		_nc = null;
		_fr = null;
		_vr = null;
	}

	/** Closes both this iterator and the underlying Statement from which this ResultSet was created. */
	@Override
	public void closeStatement() {}

   /** Check if this object is closed.
	* @return true if and only if this object is closed.
	*/
	@Override
	public boolean isClosed() {return _list == null;}

	@Override
	public String stringValue() {return _xpath == null ? "XmlIterator":_xpath;}

	@Override
	public short getItemId() {return XD_RESULTSET;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return RESULTSET;}
}