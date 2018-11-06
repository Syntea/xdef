package org.xdef.impl;

import org.xdef.xml.KNamespace;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMDocument;
import org.xdef.model.XMNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/** Implementation of the model of an element.
 * @author Vaclav Trojan
 */
public final class XDocument extends XCodeDescriptor implements XMDocument {

	/** attributes. */
	private final Map<String, XData> _attrs;
	/** Child nodes. */
	public XNode[] _childNodes;
	/** The Definition object (or null). */
	public XDefinition _definition;
	/** "forget" flag - if true the created object is removed. */
	public byte _forget;
	/** "clearAdoptedForgets" flag - if true all forgets in referred nodes
	 * are cleared. */
	public byte _clearAdoptedForgets;
	/** flag if node is template. */
	public boolean _template;
	/** Name space context of this  this X-element.*/
	private KNamespace _nsContext;

	/** Creates a new instance of XElement.
	 * @param name name of XElement.
	 * @param nsURI name space URI.
	 * @param xdef X-definition object.
	 */
	public XDocument(final String name,
		final String nsURI,
		final XDefinition xdef) {
		super(name, nsURI, xdef.getDefPool(), XNode.XMELEMENT);
		_definition = xdef;
		_forget = 0;
		_clearAdoptedForgets = 0;
		_attrs = new TreeMap<String, XData>();
		_childNodes = new XNode[0];
		//copy global options from the definition
		copyOptions(xdef);
		_match = xdef._match;
		_template = false;
	}

//	/** Creates the new instance of XElement as a copy of given argument.
//	 * @param x The XElement object from which is the copy created.
//	 */
//	public XDocument(final XDocument x) {
//		super(x);
//		_attrs = x._attrs;
//		_childNodes = new XNode[x._childNodes.length];
//		System.arraycopy(x._childNodes, 0, _childNodes, 0, _childNodes.length);
//		_definition = x._definition;
//		_forget = x._forget;
//		_clearAdoptedForgets = x._clearAdoptedForgets;
//		_template = x._template;
//		_textWhiteSpaces = 'F';
//		if (_template) {
//			_trimText = x._trimText;
//		}
//		setSPosition(x.getSPosition());
//		setXDPosition(x.getXDPosition());
//	}

	@Override
	/** Add node as child.
	 * @param xnode The node to be added.
	 */
	public void addNode(final XNode xnode) {
		XNode[] oldlist = _childNodes;
		_childNodes = new XNode[oldlist.length + 1];
		System.arraycopy(oldlist, 0, _childNodes, 0, oldlist.length);
		_childNodes[oldlist.length] = xnode;
	}

	/** Get the X-definition object.
	 * @return The X-definition object.
	 */
	public final XDefinition getDefinition() {return _definition;}

	/** Create definition element of "any" type.
	 * @return The definition of "any" element.
	 */
	public XDocument createAnyDefElement() {
		XDocument result = new XDocument("$any", null, _definition);
		result._moreAttributes = 'T';
		result._moreElements = 'T';
		result._moreText = 'T';
		result.setOccurrence(0, Integer.MAX_VALUE);
		return result;
	}

	/** Get name space context of this X-element.
	 * @return namespace context.
	 */
	public KNamespace getXDNamespaceContext() {
		return _nsContext;
	}

	/** Set name space context of this X-element.
	 * @param nc namespace context.
	 */
	public void setXDNamespaceContext(KNamespace nc) {
		_nsContext = nc;
	}

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	public XMDefinition getXMDefinition() {return _definition;}

////////////////////////////////////////////////////////////////////////////////
// XMDocument interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then
	 * defined.
	 */
	public XMNode[] getChildNodeModels() {return _childNodes;}

////////////////////////////////////////////////////////////////////////////////

	@Override
	public final void writeXNode(final XDWriter xw,
		final ArrayList<XNode> list) throws IOException {
		if (list.indexOf(this) < 1) {
			list.add(this);
		}
		writeXCodeDescriptor(xw);
		int len = _childNodes.length;
		xw.writeLength(len);
		for (int i = 0; i < len; i++) {
			if (_childNodes[i].getKind() == XMELEMENT) {
				XDocument x = (XDocument) _childNodes[i];
				int j = list.indexOf(x);
				if (j > 0) {
					xw.writeShort((short) -1);
					xw.writeInt(j);
					continue;
				}
			}
			_childNodes[i].writeXNode(xw, list);
		}
		xw.writeByte(_forget);
		//_definition ... from constructor
		xw.writeByte(_clearAdoptedForgets);
		xw.writeBoolean(_template);
		if (_nsContext == null) {
			xw.writeLength(0);
		} else {
			String[] px = _nsContext.getAllPrefixes();
			len = px.length;
			xw.writeLength(len);
			for (int i = 0; i < len; i++) {
				xw.writeString(px[i]);
				xw.writeString(_nsContext.getNamespaceURI(px[i]));
			}
		}
	}

	final static XDocument readXDocument(final XDReader xr,
		final XDefinition xd,
		final ArrayList<XNode> list)
		throws IOException {
		String name = xr.readString();
		String namespaceURI = xr.readString();
		XDocument x = new XDocument(name, namespaceURI, xd);
		list.add(x);
		x.readXCodeDescriptor(xr);
		int len = xr.readLength();
		x._childNodes = new XNode[len];
		for (int i = 0; i < len; i++) {
			x._childNodes[i] = XNode.readXNode(xr, xd, list);
		}
		x._forget = xr.readByte();
		//_definition ... from constructor
		x._clearAdoptedForgets = xr.readByte();
		x._template = xr.readBoolean();
		len = xr.readLength();
		if (len > 0) {
			x._nsContext = new KNamespace();
			for (int i = 0; i < len; i++) {
				String pfx = xr.readString();
				String uri = xr.readString();
				if (!pfx.startsWith("xml")) {
					x._nsContext.setPrefix(pfx, uri);
				}
			}
		}
		return x;
	}
}