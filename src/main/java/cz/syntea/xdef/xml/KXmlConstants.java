/*
 * Copyright 2007 Syntea software group a.s. All rights reserved.
 *
 * File: KXmlConstants.java.
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

import javax.xml.XMLConstants;


/** Important XML constants and URIs
 * @author Vaclav Trojan
 */
public interface KXmlConstants {

////////////////////////////////////////////////////////////////////////////////
	/** URI of xmlns:xlink */
	public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
	/** URI of w3c Xinclude */
	public static final String XINCLUDE_NS_URI="http://www.w3.org/2001/XInclude";
////////////////////////////////////////////////////////////////////////////////
	/** URI of JSON/XML conversion nodes. */
	public static final String JSON_NS_URI = "http://www.syntea.cz/json/1.0";
	/** Recommended namespace prefix used for JSON/XML conversion nodes. */
	public static final String JSON_NS_PREFIX = "js";
	/** Recommended namespace prefix used for X-definition nodes. */
	public static final String XDEF_NS_PREFIX = "xd";
	/** URI of X-definition 2.0. */
	public static final String XDEF20_NS_URI = "http://www.syntea.cz/xdef/2.0";
	/** URI of X-definition 3.1. */
	public static final String XDEF31_NS_URI = "http://www.syntea.cz/xdef/3.1";
	/** The namespace URI for X-definition instance */
	public static final String XDEF_INSTANCE_NS_URI =
		"http://www.syntea.cz/xdef/instance";

////////////////////////////////////////////////////////////////////////////////

	@Deprecated
	/** Reserved namespace prefix of XML W3C.
	 * @deprecated use javax.xml.XMLConstants.XML_NS_PREFIX instead.
	 */
	public final String XML_NS_PREFIX = XMLConstants.XML_NS_PREFIX;
	@Deprecated
	/** URI of XML W3C.
	 * @deprecated use javax.xml.XMLConstants.XML_NS_URI instead.
	 */
	public final String XML_NS_URI = XMLConstants.XML_NS_URI;
	@Deprecated
	/** Reserved namespace prefix of namespaces.
	 * @deprecated use javax.xml.XMLNS_ATTRIBUTE instead.
	 */
	public final String XMLNS_ATTRIBUTE = XMLConstants.XMLNS_ATTRIBUTE;
	@Deprecated
	/** URI of namespaces.
	 * @deprecated use javax.xml.XMLNS_ATTRIBUTE instead.
	 */
	public final String XMLNS_ATTRIBUTE_NS_URI =
		XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
	@Deprecated
	/** URI of w3c XML schema instance.
	 * @deprecated use javax.xml.W3C_XML_SCHEMA_INSTANCE_NS_URI instead.
	*/
	public static final String W3C_XML_SCHEMA_INSTANCE_NS_URI =
		XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	@Deprecated
	/** URI of XML schema.
	 * @deprecated use javax.xml.W3C_XML_SCHEMA_NS_URI instead.
	*/
	public static final String W3C_XML_SCHEMA_NS_URI =
		XMLConstants.W3C_XML_SCHEMA_NS_URI;
	@Deprecated
	/** The namespace URI for X-definition instance,
	 * @deprecated Please use XDEF_INSTANCE_NS_URI".
	 */
	public static final String NS_XDEF_2_0_INSTANCE =
		"http://www.syntea.cz/xdef/2.0/instance";

}
