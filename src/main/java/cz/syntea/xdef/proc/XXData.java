/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XXData.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.proc;

import cz.syntea.xdef.model.XMData;

/** Model of text data (attributes and text nodes).
 * @author Vaclav Trojan
 */
public interface XXData extends XXNode {

	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	public XMData getXMData();

	/** Get value of the actual attribute or Text node.
	 * @return The value of attribute or text node.
	 */
	public String getTextValue();

	/** Get value of the actual attribute or Text node.
	 * @param value the value to be set to attribute or text node.
	 */
	public void setTextValue(String value);

}
