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

    /** Write this XComment to XDWriter. */
    @Override
    public final void writeXNode(final XDWriter xw, final List<XNode> list) throws IOException {
        writeXCodeDescriptor(xw); //TODO!
    }

    final static XComment readXComment(final XDReader xr, final XDefinition xd) throws IOException {
        XComment x = new XComment(xd.getXDPool());
        x.readXCodeDescriptor(xr);
        return x;
    }
}