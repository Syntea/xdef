package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import org.xdef.XDPool;
import org.xdef.model.XMNode;

/** Implementation of the model of XML comment.
 * @author Vaclav Trojan
 */
public final class XComment extends XData implements XMNode {

	/** Creates a new instance of XComment.
	 * @param xp Refers to the XDefPool object.
	 */
	public XComment(final XDPool xp) {
		super("#comment", null, xp, XMCOMMENT);
		setOccurrence(REQUIRED, Integer.MAX_VALUE); //???unlimited
	}
//
//	public XComment(final XComment x) {
//		super(x);
//		setOccurrence(x.minOccurs(), x.maxOccurs());
//		setSPosition(x.getSPosition());
//		setXDPosition(x.getXDPosition());
//	}
//
//	@Override
//	/** Get XMDefinition assigned to this node.
//	 * @return root XMDefintion node.
//	 */
//	public XMDefinition getXMDefinition() {return null;} //TODO!
//
	@Override
	/** Write this XComment to XDWriter. */
	public final void writeXNode(final XDWriter xw,
		final List<XNode> list) throws IOException {
		 //TODO!
		writeXCodeDescriptor(xw);
	}

	final static XComment readXComment(final XDReader xr,
		final XDefinition xd)
		throws IOException {
//		xr.readString(); // NS URI
//		xr.readString(); // name
		XComment x = new XComment(xd.getXDPool());
		x.readXCodeDescriptor(xr);
		return x;
	}
}