package org.xdef.component;

/** Contains information about XComponent class.
 * @author Trojan
 */
final class XComponentInfo {

    private final String _name;
    private final String _ns;

    /** Create new instance of XComponentInfo.
     * @param name name of XComponent model.
     */
    public XComponentInfo(final String name) {_name = name; _ns = null;}

    /** Create new instance of XComponentInfo.
     * @param name name of XComponent model.
     * @param ns namespace URI of XComponent model.
     */
    XComponentInfo(final String name,final String ns){_name = name;_ns = ns;}

    /** Get name of XComponent model.
     * @return name of XComponent model.
     */
    public final String getName() {return _name;}

    /** Get namespace of XComponent model.
     * @return namespace of XComponent model.
     */
    public final String getNS() {return _ns;}

    @Override
    public final int hashCode() {return _name.hashCode() * 3 + _ns == null ? 0 : _ns.hashCode();}

    @Override
    public final boolean equals(final Object o) {
        if (o instanceof XComponentInfo) {
            XComponentInfo x = (XComponentInfo) o;
            return _name.equals(x._name) && _ns == null ? x._ns == null : _ns.equals(x._ns);
        }
        return false;
    }

    @Override
    public final String toString() {return _ns != null ? "{" + _ns + "}" + _name : _name;}
}