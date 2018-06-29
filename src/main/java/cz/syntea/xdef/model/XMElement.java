/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XMElement.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.model;

import cz.syntea.xdef.XDDocument;

/** Interface of model of XML Element.
 * @author Vaclav Trojan
 */
public interface XMElement extends XMNode {

	/** Check if this model allows other elements.
	 * @return true if and only if this model may contain other elements then
	 * defined.
	 */
	public boolean hasOtherElements();

	/** Check if this model allows other attributes.
	 * @return true if and only if this model may contain other attributes then
	 * defined.
	 */
	public boolean hasOtherAttrs();

	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then
	 * defined.
	 */
	public boolean hasOtherText();

	/** Get array of child nodes of this element.
	 * @return array of child nodes of this element.
	 */
	public XMNode[] getChildNodeModels();

	/** Get model of attribute (no name space URI).
	 * @param name name of attribute.
	 * @return attribute model or <tt>null</tt> if attribute is hot specified.
	 */
	public XMData getAttr(String name);

	/** Get model of attribute with name space URI.
	 * @param nsURI name space URI of attribute or <tt>null</tt>.
	 * @param name local name of attribute.
	 * @return attribute model or <tt>null</tt> if attribute is hot specified.
	 */
	public XMData getAttrNS(String nsURI, String name);

	/** Get array of models of attributes.
	 * @return array of models of attributes.
	 */
	public XMData[] getAttrs();

	/** Create XDDocument.
	 * @return XDDocument created from this model of element.
	 */
	public XDDocument createXDDocument();

	/** If this model is just reference to other model return true.
	 * @return If this model is just reference to other model return true.
	 */
	public boolean isReference();

	/** Return reference position if this model was created from reference.
	 * @return reference position if this model was created from reference.
	 */
	public String getReferencePos();

	/** Get message digest of this XDPool.
	 * @return message digest of this XDPool.
	 */
	public String getDigest();

}