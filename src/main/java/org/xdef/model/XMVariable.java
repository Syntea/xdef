package org.xdef.model;

/** Model of script variable.
 * @author Vaclav Trojan
 */
public interface XMVariable {

	/** Get name of variable.
	 * @return name of variable.
	 */
	public String getName();

	/** Get type of variable (see org.xdef.XDValueTypes).
	 * @return type of variable.
	 */
	public short getType();

	/** Get "final" flag.
	 * @return true if and only if variable is declared as final.
	 */
	public boolean isFinal();

	/** Get "external" flag (true if variable was specified as external).
	 * @return true if and only if variable is declared as external.
	 */
	public boolean isExternal();

	/** Check if this field is initialized.
	 * @return true if variable is initialized.
	 */
	public boolean isInitialized();

	/** Get offset (address) of variable to the variables table.
	 * @return offset of variable.
	 */
	public int getOffset();

}