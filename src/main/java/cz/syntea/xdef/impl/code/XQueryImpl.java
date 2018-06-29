/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.xml.KXqueryExpr;
import cz.syntea.xdef.XDContainer;
import cz.syntea.xdef.proc.XXNode;
import org.w3c.dom.Node;

/**
 *
 * @author Vaclav Trojan
 */
public interface XQueryImpl {

	/** Execute XPath expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @param xNode node model or <tt>null</tt>.
	 * @return The string representation of value of the object.
	 */
	public XDContainer exec(KXqueryExpr x, Node node, XXNode xNode);

}