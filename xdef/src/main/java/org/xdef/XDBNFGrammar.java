package org.xdef;

import org.xdef.sys.BNFGrammar;

/** Datetime in x-script.
 * @author Vaclav Trojan
 */
public interface XDBNFGrammar extends XDValue {

	public XDBNFRule getRule(final String name);

	/** Get this object as BNFGrammar.
	 * @return value of this item as BNFGrammar object.
	 */
	public BNFGrammar grammarValue();

	/** Set source.
	 * @param source set source to this object.
	 */
	public void setSource(String source);

}