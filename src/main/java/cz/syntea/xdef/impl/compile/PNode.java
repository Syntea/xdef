/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: PNode.java, created 2018-07-22.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.impl.XDefinition;
import static cz.syntea.xdef.impl.compile.PreCompiler._predefinedNSPrefixes;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.xml.KXmlConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Trojan
 */
/** Object with the parsed precompiled node. */
public final class PNode {
	final List<PAttr> _attrs; //attributes
	final List<PNode> _childNodes; //child nodes
	final Map<String, Integer> _nsPrefixes; // namespace prefixes
	final SBuffer _name; //qualified name of node
	String _localName;  //local name of node
	String _nsURI;  //namespace URI
	final PNode _parent; //parent node
	int _level; //nesting level of this node
	SBuffer _value; //String node assigned to this node
	int _nsindex; //namespace index of this node
	XDefinition _xdef;  //XDefinition associated with this node
	byte _xdVersion;  // version of X-definion
	byte _xmlVersion;  // version of xml
	boolean _template;  //template

	/** Creates a new instance of PNode.
	 * @param name The node name.
	 * @param position The position in the source text.
	 * @param parent The parent node.
	 * @param xdVersion version of XDefinition.
	 * @param xmlVersion version of XDefinition.
	 */
	PNode(final String name,
		final SPosition position,
		final PNode parent,
		final byte xdVersion,
		final byte xmlVersion) {
		_name = new SBuffer(name, position);
		_childNodes = new ArrayList<PNode>();
		_attrs = new ArrayList<PAttr>();
		_nsPrefixes = new TreeMap<String, Integer>();
		_xdVersion = xdVersion;
		_xmlVersion = xmlVersion;
		if (parent == null) {
			_nsPrefixes.putAll(_predefinedNSPrefixes);
			_template = false;
		} else {
			_template = parent._template;
			_nsPrefixes.putAll(parent._nsPrefixes);
		}
		_parent = parent;
		_nsindex = -1;
//       java makes it: _level = 0; _value = null; _def = null;
	}

	/** Get list of child nodes of given name (not recursive).
	 * @param name The name.
	 * @return the list of child nodes of given name.
	 */
	final ArrayList<PNode> getXDefChildNodes(final String name) {
		ArrayList<PNode> result = new ArrayList<PNode>();
		for (PNode node : _childNodes) {
			if (node._nsindex == 0 && node._localName.equals(name)) {
				result.add(node);
			}
		}
		return result;
	}

	/** Remove child nodes.
	 * @param list The list of nodes to be removed.
	 */
	final void removeChildNodes(final ArrayList<PNode> list) {
		_childNodes.removeAll(list);
	}

	/** Get attribute of given name with X=definition name space.
	 * If required attribute doesn't exist return null.
	 * @param localName key name of attribute.
	 * @param nsIndex The index of name space (0 == XDEF).
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

	/** Get attribute of given name with or without name space prefix from
	 * node. The attribute is removed from the list. If the argument
	 * required is set to true put error message that required attribute
	 * is missing.
	 * @param localName The local name of attribute.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the object SParsedData with the attribute value or null.
	 */
	final SBuffer getXdefAttr(final String localName,
		final boolean required,
		final boolean remove,
		final ReportWriter reporter) {
		PAttr attr = null;
		PAttr xattr = null;
		for (PAttr a : _attrs) {
			if (localName.equals(a._localName) && a._nsindex <= 0) {
				if (a._nsindex == 0) {
					xattr = a;
				} else {
					attr = a;
				}
			}
		}
		if (xattr != null && attr != null) {
			//The attribute '&{0}' can't be specified simultanously
			//with and without namespace
			attr._value.putReport(
				Report.error(XDEF.XDEF230, localName), reporter);
		} else if (xattr == null) {
			xattr = attr;
		}
		if (xattr == null) {
			if (required) {
				//Required attribute '&{0}' is missing
				_name.putReport(
					Report.error(XDEF.XDEF323, "xd:"+localName), reporter);
			}
			return null;
		} else {
			if (remove) {
				_attrs.remove(xattr);
			}
			return xattr._value;
		}
	}

	/** Get "name" (or "prefix:name") of node.
	 * If the argument required is set to true put error message that
	 * required attribute is missing.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the name or null.
	 */
	final String getNameAttr(final boolean required,
		final boolean remove,
		final ReportWriter reporter) {
		SBuffer sval = getXdefAttr("name", required, remove, reporter);
		if (sval == null) {
			return required ? ("_UNKNOWN_REQUIRED_NAME_") : null;
		}
		String name = sval.getString().trim();
		if (name == null || name.length() == 0) {
			//Incorrect name
			sval.putReport(Report.error(XDEF.XDEF258), reporter);
			return "__UNKNOWN_ATTRIBUTE_NAME_";
		}
		if (!PreCompiler.chkDefName(name, _xmlVersion)) {
			 //Incorrect name
			sval.putReport(Report.error(XDEF.XDEF258), reporter);
			return "__UNKNOWN_INCORRECT_NAME_";
		}
		return name;
	}

	void expandMacros(final ReportWriter reporter,
		final String actDefName,
		final Map<String, XScriptMacro> macros) {
		if ("macro".equals(_localName) &&
			(KXmlConstants.XDEF20_NS_URI.equals(_nsURI)
			|| KXmlConstants.XDEF31_NS_URI.equals(_nsURI))) {
			return; // do not process macro definitions
		}
		XScriptMacroResolver p = new XScriptMacroResolver(
			actDefName, _xmlVersion, macros, reporter);
		for (PAttr x: _attrs) {
			if (x._value.getString().indexOf("${") >= 0) {
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

}