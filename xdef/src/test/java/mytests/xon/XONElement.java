package mytests.xon;

/** XONObject element
 * @author Vaclav Trojan
 */
public class XONElement implements XONObject {
	private final String _name;
	private String _nsuri;
	private XONMap _attrs;
	private XONArray _nodes;

	public XONElement(String name, String nsuri) {_name=name; _nsuri=nsuri;}

	public boolean isEmpty() {return _nodes==null || _nodes.isEmpty();}

	public int size() {return _nodes==null ? 0 : _nodes.size();}

	public void clear() {
		if (_nodes != null)_nodes.clear(); if (_attrs != null)_attrs.clear();
	}
////////////////////////////////////////////////////////////////////////////////
	@Override
	public byte getType() {return XON_ELEMENT;}
	@Override
	public Object getObject() {return this;}
	@Override
	public String toString() {
		return "{" + _name + ":["
			+ (_attrs != null ? _attrs.toString() + "," : "")
			+ (_nodes != null ? _nodes.toString() : "")
			+  "]}";
	}
	@Override
	public int hashCode() {
		return _name.hashCode() + 3*(_nsuri == null ? 0 : _nsuri.hashCode())
			+ 5*(_attrs == null || _attrs.isEmpty() ? 0 : _attrs.hashCode())
			+ 7*(_nodes == null || _nodes.isEmpty() ? 0 : _nodes.hashCode());
	}
	@Override
	public boolean equals(Object x) {
		if (x instanceof XONElement) {
			XONElement e = (XONElement) x;
			if (!_name.equals(e._name)) return false;
			if (_nsuri == null) {
				if (e._nsuri != null) return false;
			} else if (!_nsuri.equals(e._nsuri)) return false;
			if (_attrs == null) {
				if (e._attrs != null && !e._attrs.isEmpty()) return false;
			} else if (!_attrs.equals(e._attrs)) return false;
			if (_nodes == null) {
				if (e._nodes != null && !e._nodes.isEmpty()) return false;
			} else if (!_nodes.equals(e._nodes)) return false;
			return true;
		}
		return false;
	}
	public String getName() {return _name;}
	public String getNamespace() {return _nsuri;}
	public String setNamespace(String nsuri) {
		String s = _nsuri; _nsuri = nsuri; return s;
	}
	public XONMap getAttrs() {return _attrs;}
	public Object getAttr(String s) {
		return _attrs==null ? null : _attrs.get(s);
	}
	public Object setAttr(String s, Object e) {
		if (_attrs==null) _attrs = new XONMap();
		return _attrs.put(s, e);
	}
	public XONArray getNodes() {return _nodes;}
	public Object getNode(int index) {
		return _nodes==null ? null : _nodes.get(index);
	}
	public boolean addNode(Object e) {
		if (_nodes==null) _nodes = new XONArray();
		return _nodes.add(e);
	}
}