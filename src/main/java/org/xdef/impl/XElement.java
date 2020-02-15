package org.xdef.impl;

import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SObjectWriter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.impl.xml.KNamespace;
import org.xdef.XDDocument;
import org.xdef.XDValue;
import org.xdef.impl.code.CodeTable;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.xdef.proc.XDLexicon;

/** Implementation of the model of an element.
 *  deprecated - will be not public in future versions
 * @author Vaclav Trojan
 */
public final class XElement extends XCodeDescriptor
	implements XMElement, CodeTable {

	/** attributes. */
	public Map<String, XData> _attrs;
	/** Child nodes. */
	public XNode[] _childNodes;
	/** The Definition object (or null). */
	public XDefinition _definition;
	/** Namespace context of this  this X-element.*/
	private KNamespace _nsContext;
	/** "forget" flag - if true the created object is removed. */
	public byte _forget;
	/** "clearAdoptedForgets" flag - if true all forgets in referred nodes
	 * are cleared. */
	public byte _clearAdoptedForgets;
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
	public XElement(final String name,
		final String nsURI,
		final XDefinition xdef) {
		super(name, nsURI, xdef.getDefPool(), XNode.XMELEMENT);
		_sqId = ((XPool)xdef.getDefPool()).getSqId();
		_definition = xdef;
		_attrs = new LinkedHashMap<String, XData>();
		_childNodes = new XNode[0];
		//copy global options from the definition
		copyOptions(xdef);
	}

	/** Creates the new instance of XElement as a copy of given argument.
	 * @param x The XElement object from which is the copy created.
	 */
	public XElement(final XElement x) {
		super(x);
		_attrs = x._attrs;
		_childNodes = new XNode[x._childNodes.length];
		_sqId = ((XPool)x.getDefPool()).getSqId();
		System.arraycopy(x._childNodes, 0, _childNodes, 0, _childNodes.length);
		_definition = x._definition;
		_forget = x._forget;
		_clearAdoptedForgets = x._clearAdoptedForgets;
		_template = x._template;
		copyOptions(x);
		setSPosition(x.getSPosition());
		setXDPosition(x.getXDPosition());
	}

	@Override
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

	@Override
	/** If this object is clone of other model (reference) return true.
	 * @return If this model is clone of other model (reference) return true.
	 */
	public final boolean isReference() {return _reference;}

	/** Set this if the model is clone of other model (reference).
	 * @param isref true if this model is clone of other model (reference).
-	 */
	public final void setReference(boolean isref) {_reference = isref;}

	@Override
	/** Get reference position if this model was created from other model
	 * (reference) or return null.
	 * @return reference position if this model was created from other model
	 * (reference) or return null.
	 */
	public final String getReferencePos() {return _refPosition;}

	@Override
	/** Get mode of JSON model.
	 * @return 0 .. no JSON, 1 .. w3c mode, 2 .. xd mode.
	 */
	public final byte getJsonMode() {return _json;}

	/** Set mode of JSON model.
	 * @param x 0 .. no JSON, 1 .. w3c mode, 2 .. xd mode.
	 */
	public final void setJsonMode(final byte x) {_json = x;}

	/** Set this model is created from other model (reference).
	 * @param ref string of position of model reference.
	 */
	public final void setReferencePos(String ref) {_refPosition = ref;}

	/** Get definition of attribute of given name.
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

	/** Get definition of attribute of given name.
	 * @param key The name of attribute (both local and/or prefixed).
	 * @param nsURI namespace URI.
	 * @param languageID language ID of the actual language in lexicon.
	 * @return the value of attribute definition.
	 */
	public final XData getDefAttrNS(final String nsURI,
		final String key,
		final int languageID) {
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
	public final boolean hasDefAttr(final String key) {
		return getDefAttr(key, -1) != null;
	}

	/** Check if exists definition of attribute of given name.
	 * @param nsURI namespace of attribute.
	 * @param key The name of attribute.
	 * @return true if the definition exists.
	 */
	public final boolean hasDefAttrNS(final String nsURI, final String key) {
		if (nsURI == null) {
			return hasDefAttr(key);
		}
		return getDefAttrNS(nsURI, key, -1) != null;
	}

	/** Check if exists definition of attribute of given name.
	 * @param xAttr XData model of the attribute.
	 * @return true if the definition exists.
	 */
	public final boolean hasDefAttr(final XData xAttr) {
		return hasDefAttr(xAttr.getName());
	}

	/** Set definition of attribute.
	 * @param xAttr XData model of the attribute.
	 */
	public final void setDefAttr(final XData xAttr) {
		String key = xAttr.getName();
		_attrs.put(key, xAttr);
	}

	/** Get names of attributes of this XElement.
	 * @return The array of names of attributes.
	 */
	public final String[] getXDAttrNames() {
		String[] result = new String[_attrs.size()];
		_attrs.keySet().toArray(result);
		return result;
	}

	/** Get attributes of this XElement.
	 * @return The array of XData models of attributes.
	 */
	public final XData[] getXDAttrs() {
		XData[] result = new XData[_attrs.size()];
		_attrs.values().toArray(result);
		return result;
	}

	/** Returns true if the option "isMoreAttributes" is specified or
	 * if there is specified an attribute "xd:any".
	 * @return value of option "isMoreAttributes".
	 */
	public final boolean isMoreAttributes() {
		return _moreAttributes == 'T' | getDefAttr("$any", - 1) != null;
	}

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
	public final void setXDNamespaceContext(final KNamespace nc) {
		_nsContext = nc;
	}

	/** Get SqId of this object.
	 * @return SqId of this object.
	 */
	public final int getSqId() {return _sqId;}

	/** Set SqId of this object.
	 * @param sqId ID to be set.
	 */
	public final void setSqId(int sqId) {_sqId = sqId;}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public final void writeXNode(final XDWriter xw,
		final ArrayList<XNode> list) throws IOException{
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
			XData[] xattrs = getXDAttrs();
			len = xattrs.length;
			xw.writeLength(len);
			for (int i = 0; i < xattrs.length; i++) {
				XData xattr = xattrs[i];
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

	final static XElement readXElement(final XDReader xr,
		final XDefinition xd,
		final ArrayList<XNode> list)
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
			x._attrs = null;
			x._childNodes = null;
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

	@Override
	/** Get XMDefinition assigned to this node.
	 * @return root XMDefinition node.
	 */
	public final XMDefinition getXMDefinition() {return _definition;}

////////////////////////////////////////////////////////////////////////////////
// XMElement interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Check if this model allows other elements.
	 * @return true if and only if this model may contain other elements then
	 * defined.
	 */
	public final boolean hasOtherElements() {return _moreElements == 'T';}

	@Override
	/** Check if this model allows other attributes.
	 * @return true if and only if this model may contain other attributes then
	 * defined.
	 */
	public final boolean hasOtherAttrs() {return _moreAttributes == 'T';}

	@Override
	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then
	 * defined.
	 */
	public final boolean hasOtherText() {return _moreText == 'T';}

	@Override
	/** Check if this model allows other text nodes.
	 * @return true if and only if this model may contain other text nodes then
	 * defined.
	 */
	public final XMNode[] getChildNodeModels() {return _childNodes;}

	@Override
	/** Get model of attribute (no namespace URI).
	 * @param name name of attribute.
	 * @return attribute model or <tt>null</tt> if attribute is hot specified.
	 */
	public final XMData getAttr(final String name) {return getDefAttr(name, -1);}

	@Override
	/** Get model of attribute with namespace URI.
	 * @param nsURI namespace URI of attribute or <tt>null</tt>.
	 * @param name local name of attribute.
	 * @return attribute model or <tt>null</tt> if attribute is hot specified.
	 */
	public final XMData getAttrNS(final String nsURI, final String name) {
		return nsURI == null ? getAttr(name) : getDefAttrNS(nsURI, name,-1);
	}

	@Override
	/** Get array of models of attributes.
	 * @return array of models of attributes.
	 */
	public final XMData[] getAttrs() {
		if (_attrs == null) {
			return null;
		}
		XData[] result = new XData[_attrs.size()];
		_attrs.values().toArray(result);
		return result;
	}

	private boolean compareName(final XNode x, final XNode y,
		final ArrayReporter reporter,
		final String path) {
		if (!x.getName().equals(y.getName())) {
			//Names differs: &{0}, &{1}
			reporter.error(XDEF.XDEF289, path+x.getName(), path+y.getName());
			return false;
		}
		return compareNamespace(x, y, reporter, path);
	}

	private boolean compareNamespace(final XNode x, final XNode y,
		final ArrayReporter reporter,
		final String path) {
		String ux = x.getNSUri();
		String uy = y.getNSUri();
		if (ux == null) {
			if (uy == null) {
				return true;
			}
		} else if (ux.equals(uy)) {
			return true;
		}
		reporter.error(XDEF.XDEF288, path); //Namespace differs: &{0}
		return false;
	}

	private boolean compareOccurrence(final XNode x, final XNode y,
		final ArrayReporter reporter,
		final String path) {
		if (x.maxOccurs()==y.maxOccurs() && x.minOccurs()==y.minOccurs()) {
			return true;
		}
		reporter.error(XDEF.XDEF287, path); //Occurrence differs: &{0}
		return false;
	}

	private boolean compareData(final XData x, final XData y,
		final ArrayReporter reporter,
		final String path,
		final boolean full) {
		boolean result = compareName(x, y, reporter, path) &&
			compareOccurrence(x, y, reporter, path);
		if ("$text".equals(x.getName())) {
			if (x._textValuesCase != y._textValuesCase ||
				x._textWhiteSpaces != y._textWhiteSpaces ||
				x._textValuesCase != y._textValuesCase ||
				x._trimText != y._trimText) {
				reporter.error(XDEF.XDEF290, path); //Options differs: &{0}
				result = false;
			}
		} else {
			if (x._attrValuesCase != y._attrValuesCase ||
				x._acceptQualifiedAttr != y._acceptQualifiedAttr ||
				x._ignoreEmptyAttributes != y._ignoreEmptyAttributes ||
				x._attrValuesCase != y._attrValuesCase ||
				x._attrWhiteSpaces != y._attrWhiteSpaces ||
				x._trimAttr != y._trimAttr) {
				reporter.error(XDEF.XDEF290, path); //Options differs: &{0}
				result = false;
			}
		}
		XDValue[] cx = ((XPool) x.getXDPool()).getCode();
		XDValue[] cy = ((XPool) y.getXDPool()).getCode();
		if (x.isFixed()) {
			if (!y.isFixed()) {
				if (y._check>=0||y._onAbsence>=0||y._onFalse>=0) {
					//Default or fixed values differs: &{0}
					reporter.error(XDEF.XDEF286, path);
					return false;
				}
				if (full || cx != cy) {
					return false;
				} else {
					y._check = x._check;
					y._onAbsence = x._onAbsence;
					y._onFalse = x._onFalse;
					return result;
				}
			}
			if (cx == cy && x._check == y._check && x._onAbsence == y._onAbsence
				&& x._onFalse == y._onFalse) {
				return result;
			}
			if (compareCode(cx, cy, x._check, y._check, false)
				&& compareCode(cx, cy, x._onAbsence, y._onAbsence, false)
				&& compareCode(cx, cy, x._onFalse, y._onFalse, false)) {
				return result;
			}
			//Default or fixed values differs:&{0}
			reporter.error(XDEF.XDEF286, path);
			return false;
		} else if (y.isFixed()) {
			//Default or fixed values differs:&{0}
			reporter.error(XDEF.XDEF286, path);
			return false;
		}
		XDValue vx, vy;
		if ((vx = x.getDefaultValue()) != null) {
			if ((vy = y.getDefaultValue()) == null) {
				if (full || cx != cy) {
					result = false;
				} else {
					y._deflt = x._deflt;
				}
			} else if (cx != cy || y._deflt != x._deflt) {
				if (vx == null || vy == null || !vx.equals(vy)) {
					//Default or fixed values differs: &{0}
					reporter.error(XDEF.XDEF286, path);
					result = false;
				}
			}
		}
		if ((vx = x.getFixedValue()) != null) {
			if ((vy = y.getFixedValue()) == null) {
				if (full || cx != cy) {
					result = false;
				} else {
					y._onAbsence = x._onAbsence;
				}
			} else if (cx != cy || y._onAbsence != x._onAbsence) {
				if (vx == null || vy == null || !vx.equals(vy)) {
					//Default or fixed values differs: &{0}
					reporter.error(XDEF.XDEF286, path);
					result = false;
				}
			}
		}
		int ix = x._check;
		int iy = y._check;
		if (ix == iy) {
			return result;
		}
		if (ix < 0) {
			return iy < 0 && result;
		} else {
			if (iy < 0) {
				if (full || cx != cy) {
					return false;
				}
				y._check = x._check;
				return result;
			} else {
				if (compareCode(cx, cy, ix,	iy, full)) {
					return result;
				}
			}
			reporter.error(XDEF.XDEF285, path); //Type of value differs: &{0}
			return false;
		}
	}

	private boolean compareCode(final XDValue[] cx,
		final XDValue[] cy,
		final int x,
		final int y,
		final boolean full) {
		if (x == y && (x == -1 || cx == cy)) {
			return true;
		}
		int p;
		int ix = x, iy = y;
		XDValue xx,xy;
		while (ix < cx.length && iy < cx.length &&
			(p = (xx = cx[ix]).getCode()) == (xy = cy[iy]).getCode()) {
			switch (p) {
				case STOP_OP:
					return true;
				case CALL_OP: {
					if (cx == cy && xx.getParam() == xy.getParam() ||
						!full && compareCode(cx,
							cy, xx.getParam(), xy.getParam(), full)) {
						ix++;
						iy++;
						continue;
					} else {
						return false;
					}
				}
				case JMPEQ:
				case JMPNE:
				case JMPLE:
				case JMPGE:
				case JMPLT:
				case JMPGT:
				case JMP_OP:
				case JMPF_OP:
				case JMPT_OP: {
					int m = xx.getParam();
					int n = xy.getParam();
					if (m - ix == n - iy) {
						ix++;
						iy++;
					} else {
						ix = Integer.MAX_VALUE;
					}
					continue;
				}
				default:
					if (!xx.equals(xy)) {
						return false;
					}
					ix++;
					iy++;
			}
		}
		return false;
	}

	private int compareGroup(final XNode[] x,
		final XNode[] y,
		final int i,
		final ArrayReporter reporter,
		final String path,
		final boolean full) {
		int j = i;
		for (; j < x.length; j++) {
			XNode ix = x[j];
			XNode iy = y[j];
			if (ix.getKind() != iy.getKind()) {
				reporter.error(XDEF.XDEF283, path); //Child nodes differs: &{0}
				return -1;
			}
			switch (ix.getKind()) {
				case XMNode.XMELEMENT:
					if (!compareElement((XElement) ix,
						(XElement) iy, reporter, path, full)) {
						//Child nodes differs:&{0}
						reporter.error(XDEF.XDEF283, path);
						return -1;
					}
					continue;
				case XMNode.XMATTRIBUTE:
				case XMNode.XMTEXT:
					if (!compareData((XData) ix,
						(XData) iy, reporter, path + "text()", full)){
						return -1;
					}
					continue;
				case XMNode.XMCHOICE:
				case XMNode.XMMIXED:
				case XMNode.XMSEQUENCE:
					if ((j=compareGroup(x, y, j+1, reporter, path, full)) < 0) {
						//Child nodes differs:&{0}
						reporter.error(XDEF.XDEF283, path);
						return -1;
					}
					continue;
				case XMNode.XMSELECTOR_END:
					return j + 1;
			}
		}
		return j;
	}

	private boolean compareNameAndOccurrence(final XNode x,
		final XNode y,
		final ArrayReporter reporter,
		String path) {
		return compareName(x, y, reporter, path) &&
			compareOccurrence(x, y, reporter, path);
	}

	private boolean compareElement(final XElement x,
		final XElement y,
		final ArrayReporter reporter,
		String path,
		final boolean full) {
		path += "/";
		return compareNameAndOccurrence(x, y, reporter, path) &
			compareElementStructure(x, y, reporter, path, full);
	}

	private boolean compareElementStructure(final XElement x,
		final XElement y,
		final ArrayReporter reporter,
		final String path,
		final boolean full) {
		boolean result = compareNamespace(x, y, reporter, path);
		if (x._nillable != y._nillable ||
			x._moreAttributes != y._moreAttributes ||
			x._moreElements != y._moreElements ||
			x._moreText != y._moreText) {
			reporter.error(XDEF.XDEF290, path); //Options differs: &{0}
			result = false;
		}
		//compare options
		XData[] ax = (XData[]) x.getAttrs();
		XData[] ay = (XData[]) y.getAttrs();
		if (ax == null) {
			if (ay != null) {
				//List of attributes differs: &{0}
				reporter.error(XDEF.XDEF284, path);
				result = false;
			}
		} else {
			if (ax.length != ay.length) {
				result = false;
			}
			for (int i = 0; i < ax.length; i++) {
				XData dx = ax[i];
				XData dy = y.getDefAttrNS(dx.getNSUri(), dx.getName(), -1);
				if (dy == null) {
					//List of attributes differs: &{0}
					reporter.error(XDEF.XDEF284, path + "@" + dx.getName());
					result = false;
				} else if (!compareData(dx,
					dy, reporter, path + "@" + dx.getName(), full)) {
					result = false;
				}
			}
		}
		XNode[] nx = x._childNodes;
		XNode[] ny = y._childNodes;
		if (nx == null) {
			if (ny != null) {
				//Number of child nodes differs: &{0}
				reporter.error(XDEF.XDEF283, path);
				return false;
			}
			return result;
		} else {
			if (nx.length != ny.length) {
				//Number of child nodes differs: &{0}
				reporter.error(XDEF.XDEF283, path);
				return false;
			}
			if (compareGroup(nx, ny, 0, reporter, path, full) < 0) {
				return false;
			}
			return result;
		}
	}

	/** Compare structure of this element model with the model from argument.
	 * @param y model to be compared.
	 * @param eq if true, then names of both models must be equal
	 * (i.e. as "implements").
	 * @return reporter with messages with differences, or returns null.
	 */
	public final ArrayReporter compareModel(final XMElement y,final boolean eq){
		ArrayReporter reporter = new ArrayReporter();
		if (!(y instanceof XElement)) {
			reporter.error(XDEF.XDEF281); //Can't compare different XDPools
			return reporter;
		}
		XElement yy = (XElement) y;
		if (getXDPool() != y.getXDPool()) {
			reporter.error(XDEF.XDEF281); //Can't compare different XDPools
			return reporter;
		}
		if  (eq ? !compareElement(this, yy, reporter, getName(), eq) :
			!compareElementStructure(this, yy, reporter, getName(),eq)){
			reporter.error(XDEF.XDEF282); //Models are differrent
			return reporter;
		}
		return null;
	}

	@Override
	/** Create XDDocument.
	 * @return XDDocument created from associated X-definition.
	 */
	public final XDDocument createXDDocument() {
		ChkDocument xdoc = new ChkDocument(_definition);
		xdoc.setRootModel(this);
		return xdoc;
	}

	@Override
	/** Get message digest of this model.
	 * @return message digest of this XDPool.
	 */
	public final String getDigest() {
		if (_digest == null) {
			genDigestInfo(this);
		}
		return _digest;
	}

	private static void genDataDigestInfo(final SObjectWriter xw,
		final XData x) throws Exception {
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
			XElement xxe = (XElement) xn;
			xe._digest = xxe.getDigest();
			return;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SObjectWriter xw = new SObjectWriter(baos);
			xw.writeShort(XNode.XMELEMENT);
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
					case XNode.XMCHOICE:
					case XNode.XMSEQUENCE:
					case XNode.XMMIXED:
						xw.writeShort(kind);
						xw.writeInt(x.minOccurs());
						xw.writeInt(x.maxOccurs());
						continue;
					case XNode.XMTEXT:
						genDataDigestInfo(xw, (XData) x);
						continue;
					case XNode.XMELEMENT:
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
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(baos.toByteArray());
			xe._digest = new String(
				SUtils.encodeHex(md.digest()), Charset.forName("UTF-8"));
		} catch (Exception ex) {
			//Internal error&{0}{: }
			throw new SRuntimeException(SYS.SYS066, ex.getMessage());
		}
	}
}