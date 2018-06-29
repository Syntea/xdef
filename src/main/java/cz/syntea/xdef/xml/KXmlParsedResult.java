/*
 * Copyright 2016 Syntea software group a.s. All rights reserved.
 *
 * File: KXmlParsedResult.java, created 2016-12-28.
 * Package: cz.syntea.xdef.xml
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.xml;

import org.w3c.dom.Document;

/** Result of parsed XML.
 * @author Vaclav Trojan
 */
public interface KXmlParsedResult {

	/** Get parsed XML document.
	 * @return parsed XML document.
	 */
	public Document getDocument();

	/** Get encoding of parsed document.
	 * @return encoding of parsed document.
	 */
	public String getXmlEncoding();

	/** Get version of XML document.
	 * @return version of XML document.
	 */
	public String getXmlVersion();
}