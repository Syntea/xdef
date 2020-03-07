package org.xdef.model;

/** Interface of model of XML selector (groups).
 * @author Vaclav Trojan
 */
public interface XMSelector extends XMNode {

	/** Get index where selector begins in child nodes list.
	 * @return index of beginning of the group.
	 */
	public int getBegIndex();

	/** Get index where selector ends in child nodes list.
	 * @return the index of beginning of the group.
	 */
	public int getEndIndex();

}