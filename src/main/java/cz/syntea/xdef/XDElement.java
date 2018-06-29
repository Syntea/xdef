/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XDElement.java, created 2011-08-25.
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

/** Elements in script
 *
 * @author Vaclav Trojan
 */
public interface XDElement extends XDContainer {

	/** Get name of underlying element or empty string.
	 * @return element name or empty string.
	 */
	public String getName();

	/** Get namespace URI of underlying element or null.
	 * @return namespace URI of underlying element or null.
	 */
	public String getNamespaceURI();

	/** Get local name of underlying element or the empty string.
	 * @return local name of underlying element or the empty string.
	 */
	public String getLocalName();

	/** Create XDContainer from this object.
	 * @return XDContainer constructed from this object.
	 */
	public XDContainer toContext();

}
