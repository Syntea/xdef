package org.xdef.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.impl.code.CodeTable;
import org.xdef.impl.xml.KNamespace;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SObjectWriter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;

/** Implementation of the model of an element.
 * @author Vaclav Trojan
 */
public final class XElement extends XCodeDescriptor implements XMElement, CodeTable {

	/** attributes. */
	public final Map<String, XData> _attrs;
	/** Child nodes. */
	public XNode[] _childNodes;
	/** The Definition object (or null). */
	public XDefinition _definition;
	/** Namespace context of this  this X-element.*/
	private KNamespace _nsContext;
	/** "forget" flag - if true the created object is removed. */
	public byte _forget;
	/** "clearAdoptedForgets" flag - if true all forgets in referred nodes are cleared. */
	public byte _clearAdoptedForgets;
	/** Switch if the actual reporter is cleared on invoked action. */
	public byte _clearReports;
	/** flag if node is template. */
	public boolean _template;
	/** Not null if this object equal to a reference.*/
	private boolean _reference;
	/** Position of model reference.*/
	private String _refPosition;
	/** Hash code (message digest) of this object. */
	public String _digest;
	/** SqId of this object. */
	private int _sqId;

	/** Creates a new instance of XElement.
	 * @param name name of XElement.
	 * @param nsURI namespace URI.
	 * @param xdef X-definition object.
	 */
	public XElement(final String name, final String nsURI, final XDefinition xdef) {
		super(name, nsURI, xdef.getXDPool(), XMELEMENT);
		_sqId = ((XPool)xdef.getXDPool()).getSqId();
		_definition = xdef;
		_attrs = new LinkedHashMap<>();
		_childNodes = new XNode[0];
		copyOptions(xdef); //copy global options from the definition
	}

	/** Creates the new instance of XElement as a copy of given argument.
	 * @param x The XElement object from which is the copy created.
	 */
	public XElement(final XElement x) {
		super(x);
		_clearReports = x._clearReports;
		_sqId = ((XPool)x.getXDPool()).getSqId();
		_definition = x._definition;
		_attrs = new LinkedHashMap<>(x._attrs);
		_childNodes = new XNode[x._childNodes.length];
		System.arraycopy(x._childNodes, 0, _childNodes, 0, _childNodes.length);
		_forget = x._forget;
		_clearAdoptedForgets = x._clearAdoptedForgets;
		_template = x._template;
		copyOptions(x);
		setSPosition(x.getSPosition());
		setXDPosition(x.getXDPosition());
	}

	/** Add node as child.
	 * @param xnode The node to be added.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final void addNode(final XNode xnode) {
		XNode[] oldlist = _childNodes;
		_childNodes = new XNode[oldlist.length + 1];
		System.arraycopy(oldlist, 0, _childNodes, 0, oldlist.length);
		_childNodes[oldlist.length] = xnode;
	}

	/** If this object is clone of other model (reference) return true.
	 * @return If this model is clone of other model (reference) return true.
	 */
	@Override
	public final boolean isReference() {return _reference;}

	/** Set this if the model is clone of other model (reference).
	 * @param isref true if this model is clone of other model (reference).
-	 */
	public final void setReference(boolean isref) {_reference = isref;}

	/** Get reference position if this model was created from other model (reference) or return null.
	 * @return reference position if this model was created from other model
	 * (reference) or return null.
	 */
	@Override
	public final String getReferencePos() {return _refPosition;}

	/** Get mode of XON/JSON model.
	 * @return 0 .. no XON/JSON, 1 .. XON/JSON w3c mode, 2 .. XON/JSON xd mode.
	 */
	@Override
	public final byte getXonMode() {return _xon;}

	/** Set mode of XON/JSON model.
	 * @param x 0 .. no XON/JSON, 1 .. XON/JSON w3c mode, 2 .. XON/JSON xd mode.
	 */
	public final void setXonMode(final byte x) {_xon = x;}

	/** Set this model is created from other model (reference).
	 * @param ref string of position of model reference.
	 */
	public final void setReferencePos(String ref) {_refPosition = ref;}

	/** Get model of attribute of given name.
	 * @param key The name of attribute.
	 * @param languageID language ID of the actual language in lexicon.
	 * @return the value of attribute definition.
	 */
	public final XData getDefAttr(final String key, final int languageID) {
		if (languageID >= 0) {
			XDLexicon t = ((XPool) getXDPool())._lexicon;
			for (XData xattr : _attrs.values()) {
				String s = xattr.getXDPosition();
				if (key.equals(t.findText(s, languageID))) {
					return xattr;
				}
			}
		}
		return _attrs.get(key);
	}

	/** Get model of attribute of given name.
	 * @param key The name of attribute (both local and/or prefixed).
	 * @param nsURI namespace URI.
	 * @param languageID language ID of the actual language in lexicon.
	 * @return the value of attribute definition.
	 */
	public final XData getDefAttrNS(final String nsURI, final String key, final int languageID) {
		if (nsURI == null) {
			return getDefAttr(key, languageID);
		}
		int i = key.indexOf(':');
		String k;
		k = i >= 0 ? key.substring(i + 1) : key;
		XDLexicon t =
			languageID >= 0 ? ((XPool) getXDPool())._lexicon : null;
		for (XData xattr : _attrs.values()) {
			String locName = xattr.getName();
			i = locName.indexOf(':');
			if (i >= 0) {
				locName = locName.substring(i + 1);
			}
			String name = t != null
				? t.findText(xattr.getXDPosition(), languageID) : null;
			if (name != null) {
				locName = name;
			}
			if (locName.equals(k) &&
				nsURI.equals(xattr.getNSUri())) {
				return xattr;
			}
		}
		return null;
	}

	/** Check if exists definition of attribute of given name.
	 * @param key The name of attribute.
	 * @return true if the definition exists.
	 */
	public final boolean hasDefAttr(final String key) {return getDefAttr(key, -1) != null;}

	/** Check if exists definition of attribute of given name.
	 * @param nsURI namespace of attribute.
	 * @param key The name of attribute.
	 * @return true if the definition exists.
	 */
	public final boolean hasDefAttrNS(final String nsURI, final String key) {
		return nsURI == null ? hasDefAttr(key) : getDefAttrNS(nsURI, key, -1) != null;
	}

	/** Check if exists definition of attribute of given name.
	 * @param xAttr XData model of the attribute.
	 * @return true if the definition exists.
	 */
	public final boolean hasDefAttr(final XData xAttr) {return hasDefAttr(xAttr.getName());}

	/** Set definition of attribute.
	 * @param xAttr XData model of the attribute.
	 */
	public final void setDefAttr(final XData xAttr) {_attrs.put(xAttr.getName(), xAttr);}

	/** Remove definition of attribute.
	 * @param xAttr XData model of the attribute to be removed.
	 * @return original value of attribute;
	 */
	public final XData removeDefAttr(final XData xAttr) {return _attrs.remove(xAttr.getName());}

	/** Get names of attributes of this XElement.
	 * @return The array of names of attributes.
	 */
	public final String[] getXDAttrNames() {
		String[] result = new String[_attrs.size()];
		_attrs.keySet().toArray(result);
		return result;
	}

	/** Returns true if the option "isMoreAttributes" is specified or
	 * if there is specified an attribute "xd:any".
	 * @return value of option "isMoreAttributes".
	 */
	public final boolean isMoreAttributes() {return _moreAttributes == 'T' | getDefAttr("$any", - 1) != null;}

	/** Get X-definition object.
	 * @return X-definition object.
	 */
	public final XDefinition getDefinition() {return _definition;}

	/** Create definition element of "any" type.
	 * @return The definition of "any" element.
	 */
	public final XElement createAnyDefElement() {
		XElement result = new XElement("$any", null, _definition);
		result._moreAttributes = 'T';
		result._moreElements = 'T';
		result._moreText = 'T';
		result.setOccurrence(0, Integer.MAX_VALUE);
		return result;
	}

	/** Get namespace context of this XElement.
	 * @return NamespaceContext of XElement.
	 */
	public final KNamespace getXDNamespaceContext() {return _nsContext;}

	/** Set namespace context of this XElement.
	 * @param nc namespace context to be set to this XElement.
	 */
	public final void setXDNamespaceContext(final KNamespace nc) {_nsContext = nc;}

	/** Get SqId of this object.
	 * @return SqId of this object.
	 */
	public final int getSqId() {return _sqId;}

	/** Set SqId of this object.
	 * @param sqId ID to be set.
	 */
	public final void setSqId(int sqId) {_sqId = sqId;}

////////////////////////////////////////////////////////////////////////////////

	/** Write this XElement to XDWriter. */
	@Override
	public final void writeXNode(final XDWriter xw, final List<XNode> list) throws IOException{
		if (list.indexOf(this) < 1) {
			list.add(this);
		}
		writeXCodeDescriptor(xw);
		xw.writeString(getDigest());
		xw.writeBoolean(_reference);
		xw.writeString(_refPosition);
		xw.writeInt(_sqId);
		int len;
		if (!_reference) {
			XData[] xattrs = (XData[]) getAttrs();
			len = xattrs.length;
			xw.writeLength(len);
			for (XData xattr : xattrs) {
				xattr.writeXNode(xw, list);
			}
			len = _childNodes.length;
			xw.writeLength(len);
			for (int i = 0; i < len; i++) {
				if (_childNodes[i].getKind() == XMELEMENT) {
					XElement x = (XElement) _childNodes[i];
					int j = list.indexOf(x);
					if (j > 0) {
						xw.writeShort((short) -1);
						xw.writeInt(j);
						continue;
					}
				}
				_childNodes[i].writeXNode(xw, list);
			}
		}
		xw.writeByte(_forget);
		//_definition ... from constructor
		xw.writeByte(_clearAdoptedForgets);
		xw.writeByte(_clearReports);
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

	final static XElement readXElement(final XDReader xr, final XDefinition xd, final List<XNode> list)
		throws IOException {
		String name = xr.readString();
		String namespaceURI = xr.readString();
		XElement x = new XElement(name, namespaceURI, xd);
		list.add(x);
		x.readXCodeDescriptor(xr);
		x._digest = xr.readString();
		x._reference = xr.readBoolean();
		x._refPosition = xr.readString();
		x._sqId = xr.readInt();
		int len;
		if (!x._reference) {
			len = xr.readLength();
			for (int i = 0; i < len; i++) {
				short kind = xr.readShort();
				XData xattr = XData.readXData(xr, kind, xd);
				x._attrs.put(xattr.getName(), xattr);
			}
			len = xr.readLength();
			x._childNodes = new XNode[len];
			for (int i = 0; i < len; i++) {
				x._childNodes[i] = XNode.readXNode(xr, xd, list);
			}
		} else {
			x._attrs.clear();
			x._childNodes = null;
		}
		x._forget = xr.readByte();
		//_definition ... from constructor
		x._clearAdoptedForgets = xr.readByte();
		x._clearReports = xr.readByte();
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

	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	@Override
	public final XMDefinition getXMDefinition() {return _definition;}

////////////////////////////////////////////////////////////////////////////////
// XMElement interface
////////////////////////////////////////////////////////////////////////////////

	/** Check if this model allows other elements.
	 * @return true if and only if this model may contain other elements then defined.
	 */
	@Override
	public final boolean hasOtherElements() {return _moreElements == 'T';}

	/** Check if this model allows other attributes.
	 * @return true if and only if this model may contain other attributes then defined.
	 */
	@Override
	public final boolean hasOtherAttrs() {return _moreAttributes == 'T';}

	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then defined.
	 */
	@Override
	public final boolean hasOtherText() {return _moreText == 'T';}

	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then defined.
	 */
	@Override
	public final XMNode[] getChildNodeModels() {return _childNodes;}

	/** Get model of attribute (no namespace URI).
	 * @param name name of attribute.
	 * @return attribute model or <i>null</i> if attribute is hot specified.
	 */
	@Override
	public final XMData getAttr(final String name) {return getDefAttr(name, -1);}

	/** Get model of attribute with namespace URI.
	 * @param nsURI namespace URI of attribute or <i>null</i>.
	 * @param name local name of attribute.
	 * @return attribute model or <i>null</i> if attribute is hot specified.
	 */
	@Override
	public final XMData getAttrNS(final String nsURI, final String name) {
		return nsURI == null ? getAttr(name) : getDefAttrNS(nsURI, name,-1);
	}

	/** Get array of models of attributes.
	 * @return array of models of attributes.
	 */
	@Override
	public final XMData[] getAttrs() {
		XData[] result = new XData[_attrs.size()];
		_attrs.values().toArray(result);
		return result;
	}

	/** Compare child nodes of this element model with the model from argument.
	 * @param y model to be compared.
	 * @param rep reporter where write error reports.
	 * @param full if true, then names of both models must be equal (i.e. as "implements").
	 * @return true if structures are compatible.
	 */
	private int compareGroup(final XNode[] x,
		final XNode[] y,
		final int i,
		final ArrayReporter rep,
		final boolean full) {
		int j = i;
		for (; j < x.length; j++) {
			XNode ix = x[j];
			XNode iy = y[j];
			if (ix.getKind() != iy.getKind()) {
				 //Child nodes differs: &{0} and &{1}
				rep.error(XDEF.XDEF283, ix.getXDPosition(), iy.getXDPosition());
				return -1;
			}
			switch (ix.getKind()) {
				case XMNode.XMELEMENT:
					if (!((XElement) ix).compareElement((XElement) iy, rep, full)) {
						//Child nodes differs: &{0} and &{1}
						rep.error(XDEF.XDEF283, ix.getXDPosition(), iy.getXDPosition());
						return -1;
					}
					continue;
				case XMNode.XMATTRIBUTE:
				case XMNode.XMTEXT:
					if (!((XData) ix).compareData((XData) iy, rep, full)){
						return -1;
					}
					continue;
				case XMNode.XMCHOICE:
				case XMNode.XMMIXED:
				case XMNode.XMSEQUENCE:
					if ((j=compareGroup(x, y, j+1, rep, full)) < 0) {
						//Child nodes differs: &{0} and &{1}
						rep.error(XDEF.XDEF283, ix.getXDPosition(), iy.getXDPosition());
						return -1;
					}
					continue;
				case XMNode.XMSELECTOR_END: return j + 1;
			}
		}
		return j;
	}

	/** Compare structure of this element model with the model from argument.
	 * @param y model to be compared.
	 * @param rep reporter where write error reports.
	 * @param full if true, then names of both models must be equal (i.e. as "implements").
	 * @return true if structures are compatible.
	 */
	private boolean compareElement(final XElement y, final ArrayReporter reporter, final boolean full) {
		return compareNameAndOccurrence(y, reporter) && compareElementStructure(y, reporter, full);
	}

	/** Compare structure of this element model with the model from argument.
	 * @param y model to be compared.
	 * @param full if true, then names of both models must be equal (i.e. as "implements").
	 * @return reporter with messages with differences, or returns null.
	 */
	private boolean compareElementStructure(final XElement y,final ArrayReporter reporter,final boolean full){
		boolean result = compareNamespace(y, reporter);
		if (_nillable != y._nillable || _moreAttributes != y._moreAttributes ||
			_moreElements != y._moreElements || _moreText != y._moreText) {
			reporter.error(XDEF.XDEF290, getXDPosition(), y.getXDPosition());//Options differs: &{0} and &{1}
			result = false;
		}
		//compare options
		XData[] ax = (XData[]) getAttrs();
		XData[] ay = (XData[]) y.getAttrs();
		if (ax == null) {
			if (ay != null) {
				//List of attributes differs: &{0} and &{1}
				reporter.error(XDEF.XDEF284, getXDPosition(),y.getXDPosition());
				result = false;
			}
		} else {
			if (ax.length != ay.length) {
				//List of attributes differs: &{0} and &{1}
				reporter.error(XDEF.XDEF284, getXDPosition(),y.getXDPosition());
				result = false;
			}
			for (XData dx : ax) {
				XData dy = y.getDefAttrNS(dx.getNSUri(), dx.getName(), -1);
				if (dy == null) {
					//List of attributes differs: &{0} and &{1}
					reporter.error(XDEF.XDEF284, getXDPosition(), "null");
					result = false;
				} else if (!dx.compareData(dy, reporter, full)) {
					result = false;
				}
			}
		}
		XNode[] nx = _childNodes;
		XNode[] ny = y._childNodes;
		if (nx == null) {
			if (ny != null) {
				//Child nodes differs: &{0} and &{1}
				reporter.error(XDEF.XDEF283, getXDPosition(),y.getXDPosition());
				return false;
			}
			return result;
		} else {
			if (nx.length != ny.length) {
				//Child nodes differs: &{0} and &{1}
				reporter.error(XDEF.XDEF283, getXDPosition(),y.getXDPosition());
				return false;
			}
			if (compareGroup(nx, ny, 0, reporter, full) < 0) {
				return false;
			}
			return result;
		}
	}

	/** Compare structure of this element model with the model from argument.
	 * @param y model to be compared.
	 * @param full if true, then names of both models must be equal (i.e. as "implements").
	 * @return reporter with messages with differences, or returns null.
	 */
	public final ArrayReporter compareModel(final XMElement y, final boolean full) {
		ArrayReporter reporter = new ArrayReporter();
		if (!(y instanceof XElement)) {
			//Can't compare different XDPool objects: &{0} and &{1}
			reporter.error(XDEF.XDEF281, getXDPosition(), getXDPosition());
			return reporter;
		}
		XElement yy = (XElement) y;
		if (getXDPool() != y.getXDPool()) {
			//Can't compare different XDPool objects: &{0} and &{1}
			reporter.error(XDEF.XDEF281, getXDPosition(), getXDPosition());
			return reporter;
		}
		if  (full ? !compareElement(yy, reporter, full) :
			!compareElementStructure(yy, reporter, full)) {
			//Models are differrent: &{0} and &{1}
			reporter.error(XDEF.XDEF282, getXDPosition(), y.getXDPosition());
			return reporter;
		}
		return null;
	}

	/** Create XDDocument.
	 * @return XDDocument created from associated X-definition.
	 */
	@Override
	public final XDDocument createXDDocument() {
		ChkDocument xdoc = new ChkDocument(_definition);
		xdoc.setRootModel(this);
		return xdoc;
	}

	/** Get message digest of this model.
	 * @return message digest of this XDPool.
	 */
	@Override
	public final String getDigest() {
		if (_digest == null) {
			genDigestInfo(this);
		}
		return _digest;
	}

	private static void genDataDigestInfo(final SObjectWriter xw, final XData x) throws Exception {
		xw.writeShort(x.getKind());
		xw.writeString(x.getName());
		xw.writeInt(x.minOccurs());
		xw.writeInt(x.maxOccurs());
		xw.writeShort(x.getParserType());
		xw.writeString(x.getDateMask());
	}

	private static void genDigestInfo(final XElement xe) {
		if (xe.isReference()) {
			XMNode xn = xe.getXDPool().findModel(xe.getReferencePos());
			xe._digest = ((XElement) xn).getDigest();
			return;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SObjectWriter xw = new SObjectWriter(baos);
			xw.writeShort(XMELEMENT);
			xw.writeString(xe.getName());
			xw.writeString(xe.getNSUri());
			xw.writeInt(xe.minOccurs());
			xw.writeInt(xe.maxOccurs());
			if (xe._attrs != null) {
				String[] names = new String[xe._attrs.size()];
				xe._attrs.keySet().toArray(names);
				Arrays.sort(names);
				for (String name: names) {
					genDataDigestInfo(xw, xe._attrs.get(name));
				}
			}
			for (XNode x: xe._childNodes) {
				short kind = x.getKind();
				switch (kind) {
					case XMCHOICE:
					case XMSEQUENCE:
					case XMMIXED:
						xw.writeShort(kind);
						xw.writeInt(x.minOccurs());
						xw.writeInt(x.maxOccurs());
						continue;
					case XMTEXT:
						genDataDigestInfo(xw, (XData) x);
						continue;
					case XMELEMENT:
						xw.writeShort(kind);
						xw.writeInt(x.minOccurs());
						xw.writeInt(x.maxOccurs());
						xw.writeString(x.getName());
						continue;
					default:
						xw.writeShort(kind);
				}
			}
			xw.close();
			MessageDigest md = MessageDigest.getInstance("SHA-256"); //was MD5
			md.update(baos.toByteArray());
			xe._digest = new String(SUtils.encodeHex(md.digest()), StandardCharsets.UTF_8);
		} catch (Exception ex) {

			throw new SRuntimeException(SYS.SYS066, ex.getMessage());//Internal error&{0}{: }
		}
	}
}