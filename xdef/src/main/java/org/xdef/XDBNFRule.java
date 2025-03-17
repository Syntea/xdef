package org.xdef;

import org.xdef.sys.BNFRule;
import org.xdef.sys.SBuffer;
import org.xdef.sys.StringParser;

/** BNF rule in X-script.
 * @author Vaclav Trojan
 */
public interface XDBNFRule extends XDValue {

	/** Parse the source data from argument and return parsed result.
	 * @param source source to be parsed.
	 * @return Object with parsed result.
	 */
	public XDParseResult perform(String source);

	/** Parse the source data from argument and return parsed result.
	 * @param source source to be parsed.
	 * @return Object with parsed result.
	 */
	public XDParseResult perform(SBuffer source);

	/** Parse the source data from argument and return parsed result.
	 * @param p parser object with data.
	 * @return Object with parsed result.
	 */
	public XDParseResult perform(StringParser p);

	/** Get name of rule.
	 * @return name of rule.
	 */
	public String getName();

	/** Get object with the rule.
	 * @return object with the rule.
	 */
	public BNFRule ruleValue();

	/** Get string with parsed part.
	 * @return string with parsed part.
	 */
	public String getParsedString();

	/** Get index to parsed data.
	 * @return index to parsed data.
	 */
	public int getParsedPosition();
}