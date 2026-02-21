package org.xdef.impl;

import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import org.xdef.XDPool;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMCOMMENT;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMPI;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMOccurrence;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SIOException;
import org.xdef.sys.SPosition;

/** Abstract XNode (part of X-definition implementation.
 * @author Vaclav Trojan
 */
public abstract class XNode implements XMNode {
	/** The "kind" of object. */
	private final short _kind;
	/** Pool of definitions. */
	private final XPool _xp;
	/** Occurrence. */
	private final XOccurrence _occ;
	/** The name of item. */
	private String _name;
	/** NameSpace URI */
	private String _nsURI;

	////////////////////////////////////////////////////////////////////////////
	// position (used in error reporting and debugging).
	////////////////////////////////////////////////////////////////////////////
	/** Position in source. */
	private SPosition _spos;
	/** Position in DefPool. */
	private String _xdpos;

	/** Creates a new instance of XNode.
	 * @param nsURI namespace of node.
	 * @param name name of node.
	 * @param xp XPool.
	 * @param kind kind of node.
	 */
	public XNode(final String nsURI, final String name, final XDPool xp, final short kind) {
		_occ = new XOccurrence();
		_name = name == null ? "" : name.intern();
		_nsURI = nsURI == null ? null : nsURI.intern();
		_xp = (XPool) xp;
		_kind = kind;
	}

	/** Set name of node.
	 * @param name the new name of node.
	 */
	public final void setName(String name) {_name = name;}

	/** Change namespace of node. Both new namespace and tho old one must not be empty. Otherwise do nothing.
	 * @param ns the new name of node.
	 */
	public final void changeNS(String ns) {
		if (ns != null && !ns.isEmpty() && _nsURI != null) {
			_nsURI = ns;
		}
	}
////////////////////////////////////////////////////////////////////////////////
// XMNode interface
////////////////////////////////////////////////////////////////////////////////

	/** Return the node kind
	 * @return kind of node.
	 */
	@Override
	public final short getKind() {return _kind;}

	/** Get namespace URI.
	 * @return namespace URI.
	 */
	@Override
	public final String getNSUri() {return _nsURI;}

	/** Get name of node.
	 * @return The name of node.
	 */
	@Override
	public final String getName() {return _name;}

	/** Get local name of node.
	 * @return The local name of node.
	 */
	@Override
	public final String getLocalName() {
		int ndx = _name.indexOf(':');
		return (ndx < 0) ? _name : _name.substring(ndx + 1);
	}

	/** Get prefix of name.
	 * @return prefix of name..
	 */
	@Override
	public final String getNamePrefix() {
		int ndx = _name.indexOf(':');
		return (ndx < 0) ? "" : _name.substring(0, ndx);
	}

	/** Get QName of model of the node.
	 * @return QName of node.
	 */
	@Override
	public QName getQName() {
		int ndx = _name.indexOf(':');
		if (ndx >= 0) {
			String localPart = _name.substring(ndx + 1).intern();
			String prefix = _name.substring(0, ndx).intern();
			return new QName(_nsURI, localPart, prefix);
		}
		return new QName(_nsURI, _name);
	}

	/** Set position to source X-definition.
	 * @param spos position to source X-definition.
	 */
	public final void setSPosition(final SPosition spos) {_spos = spos;}

	/** Get position to source X-definition.
	 * @return position to source X-definition or <i>null</i>.
	 */
	@Override
	public final SPosition getSPosition() {return _spos == null ? new SPosition() : _spos;}

	/** Set position of this node in XDPool.
	 * @param xpos position to XDPool.
	 */
	public final void setXDPosition(final String xpos) {_xdpos = xpos;}

	/** Get position of this node in XDPool.
	 * @return position of this node in XDPool.
	 */
	@Override
	public final String getXDPosition() {return _xdpos;}

	/** Return printable value for debugging.
	 * @return  printable value
	 */
	@Override
	public String toString() {
		switch (_kind) {
			case XMDEFINITION: return "XMDEFINITION: " + (_name.isEmpty()?"(nameless)":_name);
			case XMATTRIBUTE: return "XMATTRIBUTE: " + _name + (_nsURI!=null?" : "+_nsURI:"");
			case XMELEMENT: return "XMELEMENT: " + _name + (_nsURI!=null?" : "+_nsURI:"");
			case XMTEXT: return "XMTEXT: text()";
			case XMSEQUENCE: return "XMSEQUENCE: " + _name;
			case XMCHOICE: return "XMCHOICE: " + _name;
			case XMMIXED: return "XMMIXED: " + _name;
			case XMCOMMENT: return "XMCOMMENT: " + _name;
			case XMPI: return "XMPI: " + _name;
			case XMSELECTOR_END: return "XMSELECTOR_END: " + _name;
			case XMSELECTOR_END + 1: return "REFERENCE: " + _name;
		}
		return "???";
	}

	/** Write this X object to XDWriter.
	 * @param w XDWriter used for writing.
	 * @param l list of XNodes to be written.
	 * @throws IOException if an error occurs.
	 */
	public abstract void writeXNode(XDWriter w,List<XNode> l) throws IOException;

	final static XNode readXNode(final XDReader xr, final XDefinition xd, final List<XNode> list)
		throws IOException {
		short kind = xr.readShort();
		switch (kind) {
			case -1: return list.get(xr.readInt());
			case XMATTRIBUTE: return XData.readXData(xr, XMATTRIBUTE, xd);
			case XMCOMMENT: return XComment.readXComment(xr, xd);
			case XMELEMENT: return XElement.readXElement(xr, xd, list);
			case XMPI: return XPI.readXPI(xr, xd);
			case XMTEXT: return XData.readXData(xr, XMTEXT, xd);
			case XMSEQUENCE:
			case XMCHOICE:
			case XMMIXED: return XSelector.readXSelector(xr, kind);
			case XMSELECTOR_END: return new XSelectorEnd();
			default: throw new SIOException(SYS.SYS066,"Unexpected kind: "+kind); //Internal error&{0}{: }
		}
	}

	/** Get XDPool.
	 * @return XDPool to which this XMDefinition belongs.
	 */
	@Override
	public final XDPool getXDPool() {return _xp;}

////////////////////////////////////////////////////////////////////////////////
//implemetation of the interface XMOccurrence
////////////////////////////////////////////////////////////////////////////////

	/** Get min occurrence.
	 * @return min occurrence.
	 */
	@Override
	public final int minOccurs() {return _occ.minOccurs();}

	/** Get max occurrence.
	 * @return max occurrence.
	 */
	@Override
	public final int maxOccurs() {return _occ.maxOccurs();}

	/** Return true if value of occurrence had been specified.
	 * @return true if and only if occurrence is specified.
	 */
	@Override
	public final boolean isSpecified() {return _occ.isSpecified();}

	/** Return true if value of occurrence is set as illegal.
	 * @return true if and only if occurrence is set as illegal.
	 */
	@Override
	public final boolean isIllegal() {return _occ.isIllegal();}

	/** Return true if value of occurrence is set as ignored.
	 * @return true if and only if occurrence is set as ignored.
	 */
	@Override
	public final boolean isIgnore() {return _occ.isIgnore();}

	/** Return true if value of occurrence is set as fixed.
	 * @return true if and only if occurrence is set as fixed.
	 */
	@Override
	public final boolean isFixed() {return _occ.isFixed();}

	/** Return true if value of occurrence is set as required.
	 * @return true if and only if occurrence is set as required.
	 */
	@Override
	public final boolean isRequired() {return _occ.isRequired();}

	/** Return true if value of occurrence is set as optional.
	 * @return true if and only if occurrence is set as optional.
	 */
	@Override
	public final boolean isOptional() {return _occ.isOptional();}

	/** Return true if value of occurrence is set as unbounded.
	 * @return true if and only if occurrence is set as unbounded.
	 */
	@Override
	public final boolean isUnbounded() {return _occ.isUnbounded();}

	/** Return true if minimum is greater then 0 and maximum is unbounded.
	 * @return true if and only if minimum is greater then 0 and maximum is unbounded.
	 */
	@Override
	public final boolean isMaxUnlimited() {return _occ.isMaxUnlimited();}

	/** Get occurrence.
	 * @return Occurrence of this node.
	 */
	@Override
	public final XMOccurrence getOccurence() {return new XOccurrence(_occ);}

////////////////////////////////////////////////////////////////////////////////
// Protected methods
////////////////////////////////////////////////////////////////////////////////

	/** Compare local name and NS of node from argument with this node.
	 * @param y XNode to be compared.
	 * @param rep reporter where to put errors.
	 * @return true if both local names are equal.
	 */
	protected final boolean compareNameAndNS(final XNode y, final ArrayReporter rep) {
		if (!getLocalName().equals(y.getLocalName()) || getNSUri() != null && !getNSUri().equals(y.getNSUri())
			|| getNSUri() == null && y.getNSUri() != null) {
			rep.error(XDEF.XDEF289, getXDPosition(), y.getXDPosition()); //Names differs: &{0} and &{1}
			compareNamespace(y, rep);
			return false;
		}
		return compareNamespace(y, rep);
	}

	/** Compare namespace of node from argument with the namespace of this node.
	 * @param y XNode to be compared.
	 * @param rep reporter where to put errors.
	 * @return true if both local namespaces are equal.
	 */
	protected boolean compareNamespace(final XNode y, final ArrayReporter rep) {
		String ux = getNSUri();
		String uy = y.getNSUri();
		if (ux == null) {
			if (uy == null) {
				return true;
			}
		} else if (ux.equals(uy)) {
			return true;
		}
		rep.error(XDEF.XDEF288, getXDPosition(), y.getXDPosition()); //Namespace differs: &{0} and &{1}
		return false;
	}

	protected final boolean compareOccurrence(final XNode y, final ArrayReporter rep) {
		if (maxOccurs()==y.maxOccurs() && minOccurs()==y.minOccurs()) {
			return true;
		}
		rep.error(XDEF.XDEF287, getXDPosition(), y.getXDPosition()); //Occurrence differs: &{0} and &{1}
		return false;
	}

	/** Compare name, namespace and occurrence of the node from argument with the this node.
	 * @param y XNode to be compared.
	 * @param rep reporter where to put errors.
	 * @return true names, namespaces and occurrences are equal.
	 */
	protected final boolean compareNameAndOccurrence(final XNode y, final ArrayReporter rep) {
		return compareNameAndNS(y, rep) && compareNamespace(y, rep) && compareOccurrence(y, rep);
	}

////////////////////////////////////////////////////////////////////////////////
//implemetation of the interface XOccurrence
////////////////////////////////////////////////////////////////////////////////

	/** Set occurrence values.
	 * @param o occurrence object from which values are imported.
	 */
	public final void setOccurrence(final XMOccurrence o) {_occ.setOccurrence(o);}

	/** Set occurrence from parameters.
	 * @param min minimum.
	 * @param max maximum.
	 */
	public final void setOccurrence(final int min, final int max) {_occ.setOccurrence(min, max);}

	/** Set min occurrence.
	 * @param min value of minimal occurrence.
	 */
	public final void setMinOccur(final int min) {_occ.setMinOccur(min);}

	/** Set value of occurrence as required. */
	public final void setRequired() {_occ.setRequired();}

	/** Set value of occurrence as optional. */
	public final void setOptional() {_occ.setOptional();}

	/** Set value of occurrence as unspecified. */
	public final void setUnspecified() {_occ.setUnspecified();}

	/** Set value of occurrence as unbounded. */
	public final void setUnbounded() {_occ.setUnbounded();}
}