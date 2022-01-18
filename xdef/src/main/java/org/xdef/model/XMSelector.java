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

	/** Check if it is an ALL group.
	 * @return true if it is a mixed group and the maxOcuurence is &lt;= 1 and
	 * no one item is a group and the maxOcuurence of all items is &lt;= 1.
	 */
	public boolean isGroupAll();
}