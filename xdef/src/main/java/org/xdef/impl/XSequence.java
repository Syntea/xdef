package org.xdef.impl;

import static org.xdef.model.XMNodeKind.XMSEQUENCE;

/** Implementation of mixture, choice or sequence.
 * @author Vaclav Trojan
 */
public final class XSequence extends XSelector {

    /** Creates a new instance of XSequence as the item of XElement body. */
    public XSequence() {super(XMSEQUENCE);}

}