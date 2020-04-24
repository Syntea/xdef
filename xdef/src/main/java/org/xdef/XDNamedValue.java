package org.xdef;

/** Named value (pair of name and value) in x-script.
 * @author Vaclav Trojan
 */
public interface XDNamedValue extends XDValue {

	/** Get key name of pair.
	 * @return key name of pair.
	 */
	public String getName();

	/** Get value of pair.
	 * @return value of pair.
	 */
	public XDValue getValue();

	/** Set value of pair.
	 * @param newValue new value of pair.
	 * @return original value of pair.
	 */
	public XDValue setValue(XDValue newValue);

}