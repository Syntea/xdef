package org.xdef.impl.compile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.impl.XDefinition;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.xml.KXmlUtils;

/** Contains parsed source item.
 * @author Trojan
 */
public final class PNode {
	private final List<PAttr> _attrs = new ArrayList<>(); //attributes
	private final List<PNode> _childNodes = new ArrayList<>();//child nodes
	final Map<String,Integer> _nsPrefixes = new LinkedHashMap<>(); // namespace prefixes
	SBuffer _name; //qualified name of node
	String _localName;  //local name of node
	String _nsURI;  //namespace URI
	byte _xdVersion;  //version of Xdefinion
	byte _xmlVersion;  //version of xml
	XDefinition _xdef;  //X-definition associated with this node
	SBuffer _value; //value of this node
	PNode _parent; //parent PNode
	int _nsindex; //namespace index of this node
	boolean _template;  //template switch
	byte _xonMode = 0; // XON/JSON to XML transformation mode
	String _xpathPos; // xpath position

	/** Creates a new instance of PNode.
	 * @param name The node name.
	 * @param position The position in the source text.
	 * @param parent The parent node.
	 * @param xdVer version of X-definition.
	 * @param xmlVer version of X-definition.
	 */
	PNode(final String name,final SPosition position,final PNode parent,final byte xdVer,final byte xmlVer) {
		_name = new SBuffer(name, position);
		_xdVersion = xdVer;
		_xmlVersion = xmlVer;
		if (parent == null) {
			_nsPrefixes.putAll(XPreCompiler.DEFINED_PREFIXES);
			_template = false;
		} else {
			_template = parent._template;
			_nsPrefixes.putAll(parent._nsPrefixes);
		}
		_parent = parent;
		_nsindex = -1;
	}

	/** Get node name (as SBufer).
	 * @return node name (as SBufer).
	 */
	public final SBuffer getName() {return _name;}

	/** Get prefix of name.
	 * @return prefix of node name.
	 */
	public final String getPrefix() {
		String s = _name.getString();
		int ndx = s.indexOf(':');
		return ndx < 0 ? "" : s.substring(0, ndx);
	}

	/** Get local part of name.
	 * @return local part of name.
	 */
	public final String getLocalName() {
		String s = _name.getString();
		int ndx = s.indexOf(':');
		return ndx < 0 ? s : s.substring(ndx + 1);
	}

	/** Get namespace index of the node .
	 * @return node name (as SBufer).
	 */
	public final int getNSIndex() {return _nsindex;}

	/** Get node namespace.
	 * @return node namespace.
	 */
	public final String getNamespace() {return _nsURI;}

	/** Get list of attributes.
	 * @return list of attributes.
	 */
	public final List<PAttr> getAttrs() {return _attrs;}

	/** Get list of child nodes.
	 * @return list of child nodes.
	 */
	public final List<PNode> getChildNodes() {return _childNodes;}

	/** Get XPath position of this node.
	 * @return XPath position of this node.
	 */
	private String getXPath() {
		String name = _name.getString();
		if (_parent == null) {
			return '/' + name;
		}
		int index = 1;
		for (int i = 0; i < _parent._childNodes.size(); i++) {
			PNode x = _parent._childNodes.get(i);
			if (x == this) {
				break;
			}
			if (name.equals(x.getName().getString())) {
				index++;
			}
		}
		return _parent.getXPath() + "/" + name + "[" + index + "]";
	}

	/** Add child node.
	 * @param p PNode to add.
	 */
	public final void addChildNode(final PNode p) {
		p._parent = this;
		_childNodes.add(p);
		p._xpathPos = p.getXPath();
	}

	/** Get text value of PNode.
	 * @return text value of PNode or null.
	 */
	public final SBuffer getValue() {return _value;}

	/** Get version of the X-definition.
	 * @return version of the X-definition ("3.2"...."4.2" see org.xdef.impl.XConstants.XDxx).
	 */
	public final byte getXdefVersion() {return _xdVersion;}

	/** Get version of XML document.
	 * @return version of XML document ("1.0" .. 10, "1.1" .. 11; see org.xdef.impl.XConstants.XMLxx).
	 */
	public final byte getXMLVersion() {return _xmlVersion;}

	/** Get attribute of given name with X=definition name space.
	 * If required attribute doesn't exist return null.
	 * @param localName key name of attribute.
	 * @param nsIndex The index of namespace (0 == XDEF).
	 * @return the object SParsedData with the attribute value or null.
	 */
	final PAttr getAttrNS(final String localName, final int nsIndex) {
		PAttr xattr = null;
		for (PAttr a : _attrs) {
			if (localName.equals(a._localName) && a._nsindex == nsIndex) {
				xattr = a;
			}
		}
		return xattr;
	}

	/** Set attribute.
	 * @param attr the attribute.
	 * @return true if the attribute was set.
	 */
	final boolean setAttr(final PAttr attr) {
		attr._xpathPos = getXPath() + "/@" + attr._name;
		attr._parent = this;
		return _attrs.add(attr);
	}

	/** Remove attribute.
	 * @param patt the attribute to be removed..
	 * @return true if the attribute was removed.
	 */
	final boolean removeAttr(final PAttr patt) {return removeAttr(patt.getName());}

	/** Remove attribute.
	 * @param name the name of attribute.
	 * @return true if the attribute was removed.
	 */
	final boolean removeAttr(final String name) {
		for (PAttr att: _attrs) {
			if (att.getName().equals(name)) {
				_attrs.remove(att);
				return true;
			}
		}
		return false;
	}

	/** Expand macros.
	 * @param reporter error reporter.
	 * @param actDefName actual X-definition name.
	 * @param macros map with macros.
	 */
	public void expandMacros(final ReportWriter reporter,
		final String actDefName,
		final Map<String, XScriptMacro> macros) {
		if ("macro".equals(_localName)
			&& (XDConstants.XDEF31_NS_URI.equals(_nsURI) || XDConstants.XDEF32_NS_URI.equals(_nsURI)
				|| XDConstants.XDEF40_NS_URI.equals(_nsURI) || XDConstants.XDEF41_NS_URI.equals(_nsURI)
				|| XDConstants.XDEF42_NS_URI.equals(_nsURI))) {
			return; // it is not a macro definition
		}
		XScriptMacroResolver p = new XScriptMacroResolver(
			actDefName, _xmlVersion, macros, reporter);
		for (PAttr x: _attrs) {
			if (x._value.getString().contains("${")) {
				p.expandMacros(x._value);
			}
		}
		if (_value != null) {
			String s = _value.getString();
			int ndx = s.lastIndexOf("${");
			if (ndx >= 0) {
				p.expandMacros(_value);
			}
		}
		for (PNode x: _childNodes) {
			x.expandMacros(reporter, actDefName, macros);
		}
	}

	@Override
	public String toString() {return "PNode: " + _name.getString();}

	/** Create XML element from this PNode.
	 * @return XML element created from this PNode.
	 */
	public org.w3c.dom.Element toXML() {return pnodeToXML(this, null);}

	/** Create XML element from PNode.
	 * @param p the PNode.
	 * @param node w3c.dom.Node where to create child element (if null, then root element is created).
	 * @return created element.
	 */
	private static org.w3c.dom.Element pnodeToXML(final PNode p, final org.w3c.dom.Node node) {
		org.w3c.dom.Document doc;
		org.w3c.dom.Node parent;
		if (node == null) {
			parent = doc = KXmlUtils.newDocument();
		} else {
			doc = node.getOwnerDocument();
			parent = node;
		}
		org.w3c.dom.Element e = doc.createElementNS(p.getNamespace(), p.getName().getString());
		parent.appendChild(e);
		for (PAttr a: p.getAttrs()) {
			e.setAttributeNS(a.getNamespace(), a.getName(), a.getValue().getString());
		}
		for (PNode child: p.getChildNodes()) {
			pnodeToXML(child, e);
		}
		if (p.getValue() != null) {
			e.appendChild(doc.createTextNode(p.getValue().getString()));
		}
		return e;
	}
}