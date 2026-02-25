package org.xdef.impl.xml;

import org.xdef.sys.SPosition;

/** Container for attribute name, value and source position of value.
 * @author Vaclav Trojan
 */
public final class KParsedAttr {
    private String _nsURI;
    private final String _name;
    private String _value;
    private final SPosition _pos;

    /** Creates a new instance of KParsedAttr.
     * @param name the name of attribute.
     * @param value value of attribute.
     * @param pos SPosition of attribute.
     */
    public KParsedAttr(final String name, final String value, final SPosition pos) {
        _name = name.intern();
        _value = value;
        _pos = pos;
    }

    public KParsedAttr(final String nsURI, final String name, final String value, final SPosition pos) {
        _nsURI = nsURI;
        _name = name;
        _value = value;
        _pos = pos;
    }

    /* Get parsed attribute name (may be qualified name). */
    public final String getName() {return _name;}

    /* Get namespace URI.
     * @return namespace URI or <i>null</i>.
     */
    public final String getNamespaceURI() {return _nsURI;}

    /* Set namespace URI.
     * @param namespace URI.
     */
    public final void setNamespaceURI(final String nsURI) {_nsURI = nsURI;}

    /* Get string with value of attribute. */
    public String getValue() {return _value;}

    /* Set value of attribute. */
    public final void setValue(final String value) {_value = value;}

    /* Get source position of parset value of attribute. */
    public final SPosition getPosition() {return _pos;}

    @Override
    public final String toString() {
        return _name + " = \"" + _value + '"' + (_nsURI == null ? "" : "; URI = " + _nsURI);
    }
}