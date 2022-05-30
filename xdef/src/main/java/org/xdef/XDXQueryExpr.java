package org.xdef;

import org.xdef.proc.XXNode;
import org.w3c.dom.Node;

/** Interface of compiled XQuery expression.
 * @author Vaclav Trojan
 */
public interface XDXQueryExpr extends XDValue {

	/** Execute XQuery expression and return result.
	 * @param node node or null.
	 * @param xNode node model or null.
	 * @return result of execution of this object.
	 */
	public XDContainer exec(Node node, XXNode xNode);
}