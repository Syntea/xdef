package org.xdef.impl.code;

import org.xdef.xml.KXqueryExpr;
import org.xdef.XDContainer;
import org.xdef.proc.XXNode;
import org.w3c.dom.Node;

/** Interface of XQuery expression object.
 * @author Vaclav Trojan
 */
public interface XQueryImpl {

	/** Execute XQuery expression and return result.
	 * @param x XQuery expression object.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return result of execution of XQuery expression.
	 */
	public XDContainer exec(KXqueryExpr x, Node node, XXNode xNode);

}