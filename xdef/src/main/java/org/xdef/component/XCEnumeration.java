package org.xdef.component;

public interface XCEnumeration {

	/** Get object associated with this item of enumeration.
	 * @return object associated with this item of enumeration.
	 */
	public Object itemValue();

	/** Get string which is used to create enumeration.
	 * @return string which is used to create enumeration.
	 */
	@Override
	public String toString();
}