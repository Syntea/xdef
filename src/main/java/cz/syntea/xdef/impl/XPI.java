/*
 * File: XPI.java
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
import cz.syntea.xdef.model.XMNode;
import java.io.IOException;
import java.util.ArrayList;


/** Implementation of the model of processing instruction.
 *  deprecated - will be not public in future versions
 * @author Vaclav Trojan
 */
public class XPI extends XData {
	/** Position to source (for error reporting). */
//	private SPosition _spos;

	/** Creates a new instance of XPI.
	 * @param name The name of processing instruction.
	 * @param xp Refers to the XDefPool object.
	 */
	public XPI(final String name,
		final XDPool xp) {
		super(name, null, xp, XMNode.XMPI);
		setOccurrence(1, Integer.MAX_VALUE); //???
	}

//	public XPI(final XPI x) {
//		super(x);
//		setOccurrence(x.minOccurs(), x.maxOccurs());
//		setSPosition(x.getSPosition());
//		setXDPosition(x.getXDPosition());
//	}

	@Override
	void writeXNode(XDWriter xw, ArrayList<XNode> list) throws IOException {
		xw.writeString(getName());
		xw.writeString(getNSUri());
		writeXCodeDescriptor(xw);
	}

	static XPI readXPI(XDReader xr, XDefinition xd) throws IOException {
		String name = xr.readString();
		String uri = xr.readString();
		XPI x = new XPI(name, xd.getXDPool());
		x.readXCodeDescriptor(xr);
		return x;
	}
}
