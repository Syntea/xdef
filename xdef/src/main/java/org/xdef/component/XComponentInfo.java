package org.xdef.component;

/** Information about XComponent class.
 * @author Trojan
 */
public class XComponentInfo {
	final String _name;
	String _ns;
	public XComponentInfo(final String name) {_name = name;}
	public XComponentInfo(final String name,final String ns){_name=name;_ns=ns;}
	public final String getName() {return _name;}
	public final String getNS() {return _ns;}
	public final void setNS(final String ns) {_ns = ns;}
	@Override
	public int hashCode() {
		return _name.hashCode() * 3 + _ns == null ? 0 : _ns.hashCode();
	}
	@Override
	public boolean equals(final Object o) {
		if (o instanceof XComponentInfo) {
			XComponentInfo x = (XComponentInfo) o;
			return (_ns == null) ?  x._ns == null && _name.equals(x._name)
				: _ns.equals(x._ns) && _name.equals(x._name);
		}
		return false;
	}
}