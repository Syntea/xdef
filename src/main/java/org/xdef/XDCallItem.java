package org.xdef;

/** Interface used to connect ScriptProcessor to debugging tools.
 * @author Vaclav Trojan
 */
public interface XDCallItem {

	/** Get parent call item.
	 * @return parent CallItemInterface
	 */
	XDCallItem getParentCallItem();

	/** Get return code address.
	 * @return index to code array.
	 */
	int getReturnAddr();

	/** Get debug mode (see constants in org.xdef.XXDebug).
	 * @return debug mode (see constants in org.xdef.XXDebug).
	 */
	public int getDebugMode();

}