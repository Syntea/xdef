/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDBytes.java, created 2011-08-24.
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

/** Value of array of bytes in x-script.
 *
 * @author Vaclav Trojan
 */
public interface XDBytes extends XDValue {

	/** Return the value of DefBytes as string in Base64 format.
	 * @return string with value of this object in Base64 format.
	 */
	public String getBase64();

	/** Return the value of DefBytes as string in hexadecimal format.
	 * @return string with value of this object in hexadecimal format.
	 */
	public String getHex();

}
