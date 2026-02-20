package org.xdef.impl.xml;

import javax.xml.namespace.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.xml.KXmlUtils;

/** Simple implementation of org.w3c.dom.NodeList interface.
 * @author Vaclav Trojan
 */
public class KNamedNodeMap extends KNodeList implements NamedNodeMap {

    public KNamedNodeMap() {super();}

    public KNamedNodeMap(Node x) {
        super();
        if (x != null){
            add(x);
        }
    }

    public KNamedNodeMap(final NamedNodeMap x) {
        super();
        if (x != null){
            for(int i = 0; i < x.getLength(); i++) {
                add(x.item(i));
            }
        }
    }

    @Override
    public final Node getNamedItem(final String name) {
        for (Node n : this) {
            if (name.equals(n.getNodeName())) {
                return n;
            }
        }
        return null;
    }

    @Override
    public final Node setNamedItem(final Node arg) throws DOMException {return setNamedItemNS(arg);}

    @Override
    public final Node removeNamedItem(final String name) throws DOMException {
        Node n;
        if ((n = getNamedItem(name)) != null) {
            remove(n);
        }
        return n;
    }

    @Override
    public final Node getNamedItemNS(final String ns, final String localName) throws DOMException {
        QName qname = new QName(ns, localName);
        for (Node n: this) {
            if (qname.equals(KXmlUtils.getQName(n))) {
                return n;
            }
        }
        return null;
    }

    @Override
    public final Node setNamedItemNS(final Node arg) throws DOMException {
        String ns = arg.getNamespaceURI();
        Node n = ns != null ? getNamedItemNS(ns, arg.getLocalName()) : getNamedItem(arg.getNodeName());
        if (n != null) {
            remove(n);
        }
        add(arg);
        return n;
    }

    @Override
    public final Node removeNamedItemNS(final String ns, final String localName) throws DOMException {
        Node n;
        if ((n = getNamedItemNS(ns, localName)) != null) {
            remove(n);
        }
        return n;
    }
}