package org.xdef.model;

/** Table of XMVariables.
 * @author Vaclav Trojan
 */
public interface XMVariableTable {

	/** Get names of variables.
	 * @return array of names of variables.
	 */
	public String[] getVariableNames();

	/** Get variable.
	 * @param name the name of variable.
	 * @return XMvariable or null.
	 */
	public XMVariable getVariable(final String name);

	/** Get array with variables.
	 * @return array with variables.
	 */
	public XMVariable[] toArray();

	/** Get number of items.
	 * @return number of items.
	 */
	public int size();

}