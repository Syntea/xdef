package org.xdef.proc;

/** Control data of selectors (choice, sequence, mixed).
 * @author Vaclav Trojan
 */
public interface XXSelector extends XXNode {

	/** Return flag if selector may be empty sequence.
	 * @return the value of empty flag.
	 */
	public boolean isEmptyFlag();

	/** Get index where selector begins.
	 * @return index of beginning of the group.
	 */
	public int getBegIndex();

	/** Get index where selector ends.
	 * @return the index of beginning of the group.
	 */
	public int getEndIndex();

}