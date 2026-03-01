package org.xdef;

/** QName in X-script.
 * @author trojan
 */
public interface XDQName extends XDValue {
    /** Get QName value from this object.
     * @return the associated object, or return null.
     */
    public javax.xml.namespace.QName getQName();

    /** Get local name from QName value.
     * @return local name from this QName value, or return null.
     */
    public String getLocalName();

    /** Get prefix from QName value.
     * @return local name from this QName value, or return null.
     */
    public String getPrefix();

    /** Get namespace URI from QName value.
     * @return namespace URI from this QName value, or return null.
     */
    public String getNamespace();
}
