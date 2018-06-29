/*
 * File: XChoice.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl;

/** Implementation of mixture, choice or sequence.
 * @author Vaclav Trojan
 */
public class XChoice extends XSelector {

	/** Creates a new instance of DefSelector as the item of XElement body. */
	public XChoice() {super(XMCHOICE);}

}