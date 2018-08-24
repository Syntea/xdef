/*
 * File: XDefinition.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.sys.SIOException;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.xml.KXmlConstants;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.Thesaurus;
import cz.syntea.xdef.model.XMDefinition;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.sys.SRuntimeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/** Implementation of X-definition.
 * @author Vaclav Trojan
 */
public final class XDefinition extends XCodeDescriptor implements XMDefinition {

	/** table of xElements. */
	private final ArrayList<XElement> _xElements;
	/** Implementation properties. */
	public final Properties _properties;
	/** root selection. */
	public Map<String, XNode> _rootSelection;
	/** root namespaces. */
	public Map<String, String> _namespaces = new TreeMap<String, String>();
	/** Version of X-definition (XDConstants.XD20_ID or XDConstants.XD31_ID). */
	private byte _xdVersion;
	/** Version of XML from which the X-definition was created. */
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
		_xdVersion = KXmlConstants.XDEF20_NS_URI.equals(nsURI)
			? XDConstants.XD20_ID : XDConstants.XD31_ID;
		_xmlVersion = xmlVersion;
		_sourcePosition = sourcePosition;
		_xElements = new ArrayList<XElement>();
		_rootSelection = new TreeMap<String, XNode>();
		_properties = new Properties();
		setXDPosition(name + '#');
		///////////////////////////////
		_onIllegalRoot = -1;
		_onXmlError = -1;
	}

	/** Returns the available element model represented by given name or
	 * <i>null</i> if definition item is not available.
	 * @param key a name of definition item used for search.
	 * @param nsURI an namespace URI.
	 * @return The required XElement or null.
	 */
	public final XElement getXElement(final String key, final String nsURI) {
		int ndx = key.lastIndexOf('#');
		String lockey;
		XDefinition def;
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = key;
			def = this;
		} else {
			def=(XDefinition) getXDPool().getXMDefinition(key.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = key.substring(ndx + 1);
		}
		if (nsURI == null || nsURI.length() == 0) {
			for (int i = 0; i < def._xElements.size(); i++) {
				XElement xel  = def._xElements.get(i);
				if (xel.getNSUri() == null && key.equals(xel.getName())) {
					return xel;
				}
			}
		} else {
			ndx = lockey.indexOf(':');
			lockey = ndx >= 0 ? lockey.substring(ndx + 1) : lockey;
			for (int i = 0; i < _xElements.size(); i++) {
				XElement xel  = def._xElements.get(i);
				String lname = xel.getName();
				ndx = lname.indexOf(':');
				if (ndx >= 0) {
					lname = lname.substring(ndx + 1);
				}
				if (nsURI.equals(xel.getNSUri()) && lockey.equals(lname)){
					return xel;
				}
			}
		}
		return null;
	}

	/** Get all XElements from this X-definition (XModels).
	 * @return The array of objects of element models.
	 */
	public final XElement[] getXElements() {
		XElement[] result = new XElement[_xElements.size()];
		_xElements.toArray(result);
		return result;
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
	public final XMElement[] getModels() {return getXElements();}

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
	/** Get the Element model with given NameSpace and name.
	 * @param nsURI NameSpace URI of element or <tt>null</tt>.
	 * @param name name of element (may be qualified).
	 * @return Element model with given NameSpace and name or return
	 * <tt>null</tt> if such model not exists.
	 */
	public final XMElement getRootModel(final String nsURI, final String name) {
		XMElement[] models = getRootModels();
		for (int i = 0; models != null && i < models.length; i++) {
			XMElement model = models[i];
			if (nsURI == null) {
				if (model.getNSUri() == null && name.equals(model.getName())) {
					return model;
				}
			} else if (nsURI.equals(model.getNSUri())) {
				String lname = model.getName();
				int ndx = lname.indexOf(':');
				if (ndx >= 0) {
					lname = lname.substring(ndx + 1);
				}
				if (name.equals(lname)) {
					return model;
				}
			}
		}
		return null;
	}

	@Override
	/** Get Element model with given namespace and name.
	 * @param nsURI namespace URI of element or <tt>null</tt>.
	 * @param name name of element (may be prefixed).
	 * @return Element model with given namespace and name or return
	 * <tt>null</tt> if such model not exists.
	 */
	public final XMElement getModel(final String nsURI, final String name) {
		return getXElement(name, nsURI);
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
	 * (see {@link cz.syntea.xd.XDConstants#XD2_0}
	 * or {@link cz.syntea.xd.XDConstants#XD3_1}).
	 */
	public final byte getXDVersion() {return _xdVersion;}

	@Override
	/** Get XML version of X-definition source.
	 * @return XML version of X-definition source ("1.0" -> 10, "1.1" -> 11).
	 */
	public final byte getXmlVersion() {return _xmlVersion;}

	/** Add new XElement as model.
	 * @param newModel XElement
	 * @return <tt>true</tt> if and only if the new model was added
	 * to X-definition.
	 */
	public final boolean addModel(final XElement newModel) {
		if (getXElement(newModel.getName(), newModel.getNSUri()) != null) {
			return false;
		}
		_xElements.add(newModel);
		return true;
	}

	/** Select root element.
	 * @param name The name of element.
	 * @param namespaceURI namespace URI or <tt>null</tt>.
	 * @param languageID the actual thesaurus language or null.
	 * @return The X-element or <tt>null</tt> if not found.
	 */
	final XElement selectRoot(final String name,
		final String namespaceURI,
		final int languageID) {
		XElement result;
		String nm = name;
		Thesaurus t =
			languageID >= 0 ? ((XPool) getXDPool())._thesaurus : null;
		if (namespaceURI != null && namespaceURI.length() > 0) { // has NS URI
			int i = name.indexOf(':');
			nm = name.substring(i + 1);
			for (XNode xe: _rootSelection.values()){
				String xName = xe.getName(); // XElement name
				i = xName.indexOf(':');
				if (i >= 0) {
					xName = xName.substring(i + 1); // XElement local name
				}
				if (t != null) {
					String s = t.findText(xName, languageID);
					if (s != null) {
						xName = s;
					}
				}
				if (nm.equals(xName) && namespaceURI.equals(xe.getNSUri())) {
					return (XElement) xe;
				}
			}
		} else if (t != null) { // not NS URI, thesaurus
			for (XNode xe: _rootSelection.values()) {
				// get translated name
				String newName = t.findText(xe.getXDPosition(), languageID);
				if (nm.equals(newName) && xe.getNSUri() == null){
					return (XElement) xe;
				}
			}
		} else {  // not NS URI, not thesaurus
			for (XNode xe: _rootSelection.values()) {
				if (xe != null && nm.equals(xe.getName()) &&
					xe.getNSUri() == null){
					return (XElement) xe;
				}
			}
		}
		// not found, now try model renerence to an xd:any
		for (XNode xe: _rootSelection.values()) {
			String lockey = xe.getName();
			if (lockey.endsWith("$any") && lockey.length() > 4) {
				// reference of the named any
				return ((XElement) xe)._childNodes.length == 0 ?
					null : (XElement) ((XElement) xe)._childNodes[0];
			}
		}
		// not found, try if there is "*"
		return (XElement) _rootSelection.get("*");
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
		XElement[] xelems = getXElements();
		len = xelems.length;
		xw.writeLength(len);
		for (int i = 0; i < len; i++) {
			xelems[i].writeXNode(xw, list);
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
		return x;
	}

	@Override
	/** Add node as child.
	 * @param xnode The node to be added.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final void addNode(final XNode xnode) {
		throw new SRuntimeException(SYS.SYS066, //Internal error: &{0}
			"Attempt to add node to ScriptCodeDescriptor");
	}
}