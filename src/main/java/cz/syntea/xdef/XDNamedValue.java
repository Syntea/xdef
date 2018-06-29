/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XDNamedValue.java.
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

/** Named value (pair of name and value) in x-script.
 * @author Vaclav Trojan
 */
public interface XDNamedValue extends XDValue {

	/** Get key name of pair.
	 * @return key name of pair.
	 */
	public String getName();

	/** Get value of pair.
	 * @return value of pair.
	 */
	public XDValue getValue();

	/** Set value of pair.
	 * @param newValue new value of pair.
	 * @return original value of pair.
	 */
	public XDValue setValue(XDValue newValue);

}
