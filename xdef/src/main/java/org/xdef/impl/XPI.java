package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import org.xdef.XDPool;

/** Implementation of the model of processing instruction.
 * @author Vaclav Trojan
 */
public final class XPI extends XData {

	/** Creates a new instance of XPI.
	 * @param name The name of processing instruction.
	 * @param xp Refers to the XDefPool object.
	 */
	public XPI(final String name, final XDPool xp) {
		super(name, null, xp, XMPI);
		setOccurrence(1, Integer.MAX_VALUE); //???
	}

	@Override
	/** Write this XPI to XDWriter. */
	public final void writeXNode(final XDWriter xw, final List<XNode> list) throws IOException {
		xw.writeString(getName());
		xw.writeString(getNSUri());
		writeXCodeDescriptor(xw);
	}

	final static XPI readXPI(final XDReader xr, final XDefinition xd) throws IOException {
		String name = xr.readString();
		XPI x = new XPI(name, xd.getXDPool());
		x.readXCodeDescriptor(xr);
		return x;
	}
}