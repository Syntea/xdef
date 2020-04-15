package org.xdef.impl;

import org.xdef.XDConstants;
import org.xdef.msg.SYS;
import org.xdef.sys.SIOException;
import org.xdef.sys.SPosition;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.sys.SRuntimeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedHashMap;

/** Implementation of XMDefinition.
 * @author Vaclav Trojan
 */
public final class XDefinition extends XCodeDescriptor implements XMDefinition {
	/** table of xElements. */
	private final ArrayList<XElement> _xElements = new ArrayList<XElement>();
	/** Implementation properties. */
	public final Properties _properties = new Properties();
	/** root namespaces. */
	public final Map<String, String> _namespaces =
		new LinkedHashMap<String,String>();
	/** root selection. */
	public Map<String,XNode> _rootSelection = new LinkedHashMap<String,XNode>();
	/** Array of X-definitions names from where to accept local declarations. */
	public String[] _importLocal;

	/** Version of X-definition (see org.xdef.impl.XConstants.XDxx). */
	private byte _xdVersion;
	/** Version of XML from which the X-definition was created
	 *  (see org.xdef.impl.XConstants.XDxx).
	 */
	private byte _xmlVersion;

	////////////////////////////////////////////////////////////////////////////
	// Actions on Document
	////////////////////////////////////////////////////////////////////////////
	/** Action if the root was not found in X-definition. */
	public int _onIllegalRoot;
	/** Action if an XML error occurs. */
	public int _onXmlError;

	/** Source ID of this X-definition. */
	private final SPosition _sourcePosition;

	/** Creates a new instance of Definition
	 * @param name name of definition.
	 * @param xdp XPool object.
	 * @param nsURI Name space URI of X-definition.
	 * @param sourcePosition source position of X-definition.
	 * @param xmlVersion XML version of X-definition source.
	 */
	public XDefinition(final String name,
		final XDPool xdp,
		final String nsURI,
		final SPosition sourcePosition,
		final byte xmlVersion) {
		super(name, nsURI, (XPool) xdp, XNode.XMDEFINITION);
		_xdVersion = XDConstants.XDEF20_NS_URI.equals(nsURI) ? XConstants.XD20
			: XDConstants.XDEF31_NS_URI.equals(nsURI) ? XConstants.XD31
			: XDConstants.XDEF32_NS_URI.equals(nsURI) ? XConstants.XD32
			: XDConstants.XDEF40_NS_URI.equals(nsURI) ? XConstants.XD40 : 0;
		_xmlVersion = xmlVersion;
		_sourcePosition = sourcePosition;
		setXDPosition(name + '#');
		///////////////////////////////
		_onIllegalRoot = -1;
		_onXmlError = -1;
	}

	@Override
	/** Get source position of this X-definition.
	 * @return source ID of this X-definition..
	 */
	public final SPosition getSourcePosition() {return _sourcePosition;}

	@Override
	/** Get all Element models from this X-definition.
	 * @return The array of element models.
	 */
	public final XMElement[] getModels() {
		XElement[] result = new XElement[_xElements.size()];
		_xElements.toArray(result);
		return result;
	}

	@Override
	/** Get all Element models defined as root from this X-definition.
	 * @return The array of root element models.
	 */
	public final XMElement[] getRootModels() {
		XNode[] result = new XElement[_rootSelection.size()];
		_rootSelection.values().toArray(result);
		return (XMElement[]) result;
	}

	@Override
	/** Get Element model with given namespace and name.
	 * @param nsURI namespace URI of element or <tt>null</tt>.
	 * @param name name of element (may be prefixed).
	 * @return Element model with given namespace and name or return
	 * <tt>null</tt> if such model not exists.
	 */
	public final XMElement getModel(final String nsURI, final String name) {
		String lockey;
		XDefinition def;
		int ndx = name.lastIndexOf('#');
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = name;
			def = this;
		} else {
			def=(XDefinition) getXDPool().getXMDefinition(name.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = name.substring(ndx + 1);
		}
		if (nsURI == null || nsURI.isEmpty()) {
			for (int i = 0; i < def._xElements.size(); i++) {
				XElement xel  = def._xElements.get(i);
				if (xel.getNSUri() == null && name.equals(xel.getName())) {
					return xel;
				}
			}
		} else {
			ndx = lockey.indexOf(':');
			lockey = ndx >= 0 ? lockey.substring(ndx + 1) : lockey;
			for (int i = 0; i < _xElements.size(); i++) {
				XElement xel = def._xElements.get(i);
				if (nsURI.equals(xel.getNSUri())
					&& lockey.equals(xel.getLocalName())){
					return xel;
				}
			}
		}
		return null;
	}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	public final XMDefinition getXMDefinition() {return this;}

	@Override
	/** Create XDDocument.
	 * @return XDDocument created from this XMDefinition.
	 */
	public final XDDocument createXDDocument() {return new ChkDocument(this);}

	@Override
	/** Get version of X-definition.
	 * @return version of X-definition
	 * (see {@link org.xdef.XDConstants#XD2_0}
	 * or {@link org.xdef.XDConstants#XD3_1}).
	 */
	public final byte getXDVersion() {return _xdVersion;}

	@Override
	/** Get XML version of X-definition source.
	 * @return XML version of X-definition source ("1.0" -> 10, "1.1" -> 11).
	 */
	public final byte getXmlVersion() {return _xmlVersion;}

	@Override
	/** Check if given name is declared as local in this X-definition.
	 * @param name the name to be checked.
	 * @return true if given name is declared as local in this X-definition.
	 */
	public final boolean isLocalName(final String name) {
		if (name != null) {
			int ndx = name.indexOf('#');
			String localName;
			if (ndx >= 0) {
				if (!getName().equals(name.substring(0,ndx))) {
					return false;
				}
				localName = name;
			} else {
				localName = getName() + '#' + name;
			}
			for (String s: getXDPool().getVariableTable().getVariableNames()) {
				if (localName.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Add new XElement as model.
	 * @param newModel XElement
	 * @return <tt>true</tt> if and only if the new model was added
	 * to X-definition.
	 */
	public final boolean addModel(final XElement newModel) {
		String name = newModel.getName();
		for (XElement x: _xElements) {
			if (name.equals(x.getName())) {
				return false;
			}
		}
		_xElements.add(newModel);
		return true;
	}

	/** Create definition element of "any" type.
	 * @return The definition of "any" element.
	 */
	public final XElement createAnyDefElement() {
		XElement result = new XElement("$any", null, this);
		result._moreAttributes = 'T';
		result._moreText = 'T';
		result._moreElements = 'T';
		return result;
	}

	/** Find variable in local declarations or in global declaration.
	 * @param name name of variable.
	 * @return XVariable object or null if it is not found.
	 */
	public XVariable findVariable(final String name) {
		XPool xp = (XPool) getDefPool();
		for (String s: _importLocal) {
			// first look to the list of local declarations
			XVariable xv = xp.getVariable(s + name);
			if (xv != null) {
				return xv;
			}
		}
		return xp.getVariable(name); // not found in locals, get global
	}

	@Override
	/** Compare X-definition with an object.
	 * @param o object to be compared.
	 * @return <tt>true</tt> if and only if the compared object is an
	 * X-definition and if the name of it is equal to this.
	 */
	public final boolean equals(final Object o) {
		if (o instanceof String) {
			return getName().equals((String)o);
		}
		return o instanceof XDefinition &&
			getName().equals(((XDefinition)o).getName());
	}

	@Override
	public final int hashCode() {return getName().hashCode();}

	@Override
	/** Get implementation properties of X-definition.
	 * @return the implementation properties of X-definition.
	 */
	public final Properties getImplProperties() {return _properties;}

	@Override
	/** Get implementation property of X-definition.
	 * @param name The name of property.
	 * @return the value implementation property of X-definition.
	 */
	public final String getImplProperty(final String name) {
		return _properties.getProperty(name);
	}

	@Override
	public final void writeXNode(final XDWriter xw,
		final ArrayList<XNode> list) throws IOException {
		xw.writeSPosition(_sourcePosition);
		writeXCodeDescriptor(xw);
		xw.writeByte(_xdVersion);
		xw.writeByte(_xmlVersion);
		xw.writeInt(_onIllegalRoot);
		xw.writeInt(_onXmlError);
		int len = _properties == null ? 0 : _properties.size();
		xw.writeLength(len);
		if (len > 0) {
			for (Object e: _properties.keySet()) {
				String key = (String) e;
				xw.writeString(key);
				xw.writeString(_properties.getProperty(key));
			}
		}
		xw.writeLength(_namespaces.size());
		for (Map.Entry<String, String> e: _namespaces.entrySet()) {
			xw.writeString(e.getKey());
			xw.writeString(e.getValue());
		}
		xw.writeLength(len = _xElements.size());
		for (int i = 0; i < len; i++) {
			_xElements.get(i).writeXNode(xw, list);
		}
		xw.writeLength(len = _importLocal.length);
		for (String s: _importLocal) {
			xw.writeString(s);
		}
	}

	final static XDefinition readXDefinition(final XDReader xr,
		final XPool xp,
		final ArrayList<XNode> list) throws IOException {
		SPosition sourcePos = xr.readSPosition();
		if (xr.readShort() != XNode.XMDEFINITION) {//must be X-definition
			//SObject reader: incorrect format of data&{0}{: }
			throw new SIOException(SYS.SYS039, "XMDefinition expected");
		}
		String name = xr.readString();
		String nsUri = xr.readString();
		XDefinition x = new XDefinition(name, xp, nsUri, sourcePos, (byte) 0);
		x.readXCodeDescriptor(xr);
		x._xdVersion = xr.readByte();
		x._xmlVersion = xr.readByte();
		x._onIllegalRoot = xr.readInt();
		x._onXmlError = xr.readInt();
		int len = xr.readLength(); //properties
		for (int i = 0; i < len; i++) {
			x._properties.put(xr.readString(), xr.readString());
		}
		len = xr.readLength();
		for (int i = 0; i < len; i++) {
			x._namespaces.put(xr.readString(), xr.readString());
		}
		len = xr.readLength();
		for (int i = 0; i < len; i++) {
			short kind = xr.readShort(); //always XElement
			if (kind != XNode.XMELEMENT) {
				//SObject reader: incorrect format of data&{}{: }
				throw new SIOException(SYS.SYS039, "XMElement expected");
			}
			x._xElements.add(XElement.readXElement(xr, x, list));
		}
		len = xr.readLength();
		x._importLocal = new String[len];
		for (int i = 0; i < len; i++) {
			x._importLocal[i] = xr.readString();
		}
		return x;
	}

	@Override
	/** Add node as child.
	 * @param xnode The node to be added.
	 * @throws SRuntimeException if this method is invoked here.
	 */
	public final void addNode(final XNode xnode) {
		throw new SRuntimeException(SYS.SYS066, //Internal error: &{0}
			"Attempt to add node to ScriptCodeDescriptor");
	}
}