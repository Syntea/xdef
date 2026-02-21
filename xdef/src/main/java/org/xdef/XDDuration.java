package org.xdef;

import org.xdef.sys.SDuration;

/** Duration in X-script.
 * @author Vaclav Trojan
 */
public interface XDDuration extends XDValue {

    /** Set duration.
     * @param value SDuration object.
     */
    public void setDuration(SDuration value);
}