package org.xdef;

import org.w3c.dom.Attr;

/** Iterface of X-script value with org.w3c.dom.Attr.
 * @author  Vaclav Trojan
 */
public interface XDAttr extends XDValue, XDNamedValue{

    /** Return the value of org.w3c.dom.Attr.
     * @return the value of the node as org.w3c.dom.Attr.
     */
    public Attr attrValue();

}
