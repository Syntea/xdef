package org.xdef;

import org.xdef.sys.BNFRule;
import org.xdef.sys.SBuffer;
import org.xdef.sys.StringParser;

/** BNF rule in x-script.
 * @author Vaclav Trojan
 */
public interface XDBNFRule extends XDValue {

	public XDParseResult perform(XDValue source);

	public XDParseResult perform(String source);

	public XDParseResult perform(SBuffer source);

	public XDParseResult perform(StringParser p);

	public String getName();

	public BNFRule ruleValue();

	public String getParsedString();

	public int getParsedPosition();

}