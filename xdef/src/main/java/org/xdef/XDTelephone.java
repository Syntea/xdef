package org.xdef;

/** Telephone number in Xscript.
 * @author Vaclav Trojan
 */
public interface XDTelephone extends XDValue {

	/**	Get the area code from telephone number.
	 * @return area code part from telephone number.
	 */
	public int getAreaCode();

	/**	Get the local telephone number.
	 * @return local part from telephone number.
	 */
	public int getLocalNumber();
}