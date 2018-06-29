/*
 * File: XComment.java
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

import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.model.XMDefinition;
import cz.syntea.xdef.model.XMNode;
import java.io.IOException;
import java.util.ArrayList;

/** Implementation of the model of XML comment.
 * @author Vaclav Trojan
 */
public class XComment extends XData implements XMNode {

	/** Creates a new instance of XComment.
	 * @param xp Refers to the XDefPool object.
	 */
	public XComment(final XDPool xp) {
		super("#comment", null, xp, XMNode.XMCOMMENT);
		setOccurrence(REQUIRED, Integer.MAX_VALUE); //???unlimited
	}
//
//	public XComment(final XComment x) {
//		super(x);
//		setOccurrence(x.minOccurs(), x.maxOccurs());
//		setSPosition(x.getSPosition());
//		setXDPosition(x.getXDPosition());
//	}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefintion node.
	 */
	public XMDefinition getXMDefinition() {return null;} //TODO!

	@Override
	void writeXNode(XDWriter xw, ArrayList<XNode> list) throws IOException {
		writeXCodeDescriptor(xw);
	}

	static XComment readXComment(XDReader xr, XDefinition xd)
		throws IOException {
//		xr.readString(); // NS URI
//		xr.readString(); // name
		XComment x = new XComment(xd.getDefPool());
		x.readXCodeDescriptor(xr);
		return x;
	}

}
