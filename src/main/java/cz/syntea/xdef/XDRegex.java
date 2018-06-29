/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDRegex.java, created 2011-08-23.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */

package cz.syntea.xdef;

/** Regular expression in x-script.
 *
 * @author Vaclav Trojan
 */
public interface XDRegex extends XDValue {

	/** Check if given data matches the regular expression.
	 * @param data The data to be checked.
	 * @return <tt>true</tt> if and only if the data matches regular expression.
	 */
	public boolean matches(String data);

	/** Return regex result.
	 * @param source string to be processed with this regular expression.
	 * @return XDRegexResult object.
	 */
	public XDRegexResult getRegexResult(String source);

	/** Get value of item as String representation of value.
	 * @return The string representation of value of the object.
	 */
	public String sourceValue();

//	/** Check if regular expression was correctly compiled.
//	 * @return true if regular expression was correctly compiled.
//	 */
//	public boolean isCompiled();

}
