package org.xdef;


/** Unique set (table of unique items - rows). It is used for validation methods
 * ID, IDREF, CHECKID etc.
 */
public interface XDUniqueSet extends XDValue {

	/** Get name of this uniqueSet.
	 * @return name of this uniqueSet.
	 */
	public String getName();

	/** Get names of key parts.
	 * @return array with names of key parts.
	 */
	public String[] getKeyPartNames();

	/** Get names of assigned variables.
	 * @return array with names of assigned variables.
	 */
	public String[] getVarNames();

	/** Get printable form of actual value of the key.
	 * @return printable form of actual value of the key.
	 */
	public String printActualKey();

	/** Get actual actual value of the uniqueSet key or null if uniqueSet
	 * item not exists.
	 * @return actual actual value of the uniqueSet key or null.
	 */
	public XDUniqueSetKey getActualKey();

	/** Get items (rows) from the table.
	 * @return Container with rows of the table.
	 */
	public XDContainer getUniqueSetItems();

	/** Get size of the uniqueSet table.
	 * @return size of the table.
	 */
	public int size();
}