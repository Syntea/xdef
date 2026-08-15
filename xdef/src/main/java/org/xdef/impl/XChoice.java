package org.xdef.impl;

import static org.xdef.model.XMNodeKind.XMCHOICE;

/** Implementation of mixture, choice or sequence.
 * @author Vaclav Trojan
 */
public final class XChoice extends XSelector {

    /** Creates a new instance of DefSelector as the item of XElement body. */
    public XChoice() {super(XMCHOICE);}
}