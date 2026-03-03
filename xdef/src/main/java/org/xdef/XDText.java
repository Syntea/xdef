package org.xdef;

import org.w3c.dom.CharacterData;

/** Iterface of X-script value with org.w3c.dom.Attr.
 * @author  Vaclav Trojan
 */
public interface XDText extends XDValue{

    /** Return the value of text node
     * @return the value of the node as org.w3c.dom.CharacterData.
     */
    public CharacterData characterDataValue();

}
