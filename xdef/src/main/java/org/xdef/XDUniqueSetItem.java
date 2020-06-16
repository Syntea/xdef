package org.xdef;

/** Implements item of unique set (row of table).
 * @author Vaclav Trojan
 */
public interface XDUniqueSetItem extends XDValue {

	/** Get name of uniqueSet table.
	 * @return name of uniqueSet table.
	 */
	public String getTableName();

	/** Get names of key parts.
	 * @return array with names of key parts.
	 */
	public String[] getKeyPartNames();

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
}