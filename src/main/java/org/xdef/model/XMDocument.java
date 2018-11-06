package org.xdef.model;

/** Interface of model of XML Document.
 * @author Vaclav Trojan
 */
public interface XMDocument extends XMNode {

	/** Get array of child nodes of this document.
	 * @return array of child nodes of this document.
	 */
	public XMNode[] getChildNodeModels();

}