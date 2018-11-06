package org.xdef.impl.code;

import org.xdef.xml.KXqueryExpr;
import org.xdef.XDContainer;
import org.xdef.proc.XXNode;
import org.w3c.dom.Node;

/** the interface for implementation of the XQuery command.
 * @author Vaclav Trojan
 */
public interface XQueryImpl {

	/** Execute XPath expression and return result.
	 * @param x XQuery expression object.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return The string representation of value of the object.
	 */
	public XDContainer exec(KXqueryExpr x, Node node, XXNode xNode);

}