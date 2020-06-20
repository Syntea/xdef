package org.xdef;

/** Implements key of uniqueSet table.
 * @author Vaclav Trojan
 */
public interface XDUniqueSetKey extends XDValue {

	/** Get name of uniqueSet table.
	 * @return name of uniqueSet table.
	 */
	public String getTableName();

	/** Get values of key parts.
	 * @return array with values of key parts.
	 */
	public XDValue[] getKeyParts();

	/** Get value of a key part.
	 * @param name the name of key part.
	 * @return value of key part.
	 */
	public XDValue getKeyPart(final String name);

	/** Get value of an assigned value.
	 * @param name the name of assigned value.
	 * @return assigned value.
	 */
	public XDValue getValue(final String name);

	/** Reset actual key of the table from this position.
	 * @return true if the key was reset to the value from this object
	 * or return false if item not exists in the set.
	 */
	public boolean resetKey();
}