package org.xdef.impl.xml;

import javax.xml.namespace.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Simple implementation of org.w3c.dom.NodeList interface.
 * @author Vaclav Trojan
 */
public class KNamedNodeMap extends KNodeList implements NamedNodeMap {

	public KNamedNodeMap() {super();}
	public KNamedNodeMap(Node x) {
		this();
		if (x != null){
			add(x);
		}
	}

	public KNamedNodeMap(final NamedNodeMap x) {
		this();
		if (x != null){
			for(int i = 0; i < x.getLength(); i++) {
				add(x.item(i));
			}
		}
	}

	@Override
	public final Node getNamedItem(final String name) {
		for (int i = 0; i < size(); i++) {
			Node n = get(i);
			if (name.equals(n.getNodeName())) {
				return n;
			}
		}
		return null;
	}

	@Override
	public final Node setNamedItem(final Node arg) throws DOMException {
		return setNamedItemNS(arg);
	}

	@Override
	public final Node removeNamedItem(final String name) throws DOMException {
		Node n = getNamedItem(name);
		if (n != null) {
			remove(n);
		}
		return n;
	}

	@Override
	public final Node getNamedItemNS(final String ns, final String localName)
		throws DOMException {
		QName qname = new QName(ns, localName);
		for (int i = 0; i < size(); i++) {
			Node n = get(i);
			String u = n.getNamespaceURI();
			QName qn = u == null ?
				new QName(n.getNodeName()) : new QName(u, n.getLocalName());
			if (qname.equals(qn)) {
				return n;
			}
		}
		return null;
	}

	@Override
	public final Node setNamedItemNS(final Node arg) throws DOMException {
		String ns = arg.getNamespaceURI();
		Node n;
		if (ns != null) {
			n = getNamedItemNS(ns, arg.getLocalName());
		} else {
			n = getNamedItem(arg.getNodeName());
		}
		if (n != null) {
			remove(n);
		}
		add(arg);
		return n;

	}

	@Override
	public final Node removeNamedItemNS(final String ns, final String localName)
		throws DOMException {
		Node n = getNamedItemNS(ns, localName);
		if (n != null) {
			remove(n);
		}
		return n;
	}

}