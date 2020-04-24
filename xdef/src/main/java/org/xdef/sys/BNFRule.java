package org.xdef.sys;

/** Provides BNF grammar rule.
 * @author Vaclav Trojan
 */
public interface BNFRule {

	/** Get name of this rule.
	 * @return name of this rule.
	 */
	public String getName();

	/** Get string with parsed part by this rule.
	 * @return string with parsed part by this rule.
	 */
	public String getParsedString();

	/** Get array of objects created by this rule.
	 * @return array of objects created by this rule or null.
	 */
	public Object[] getParsedObjects();

	/** Get position of parsed part of string by this rule.
	 * @return position of parsed part by this rule.
	 */
	public int getParsedPosition();

	/** Parse string assigned to SParser by this rule.
	 * @param parser SParser containing string and position from which parsing
	 * will be started.
	 * @return true if parsing was successful.
	 */
	public boolean parse(StringParser parser);

}