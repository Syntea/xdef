package org.xdef;

import org.xdef.sys.BNFGrammar;

/** BNF grammar in Xscript.
 * @author Vaclav Trojan
 */
public interface XDBNFGrammar extends XDValue {

	/** Get object with BNF rule from BNF grammar.
	 * @param name name of BNF rule.
	 * @return object with BNF rule with given name.
	 */
	public XDBNFRule getRule(final String name);

	/** Get this object as BNFGrammar.
	 * @return value of this item as BNFGrammar object.
	 */
	public BNFGrammar grammarValue();
}