package org.xdef.impl.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Implemetation of empty object org.w3c.dom.Nodelist.
 * @author Vaclav Trojan
 */

public final class KEmptyNodeList implements NodeList {
	/** Creates a new instance of KEmptyNodeList */
	public KEmptyNodeList() {}
	@Override
	public final int getLength() {return 0;}
	@Override
	public final Node item(int index) {return null;}
}