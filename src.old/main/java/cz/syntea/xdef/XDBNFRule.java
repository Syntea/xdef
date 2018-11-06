package cz.syntea.xdef;

import cz.syntea.xdef.sys.BNFRule;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.StringParser;

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
