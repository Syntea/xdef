package org.xdef.impl;

import org.xdef.msg.SYS;
import org.xdef.sys.SIOException;
import org.xdef.sys.SPosition;
import org.xdef.XDPool;
import org.xdef.model.XMNode;
import org.xdef.model.XMOccurrence;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;

/** Abstract XNode (part of X-definition implementation.
 * @author Vaclav Trojan
 */
public abstract class XNode implements XMNode {
	/** The "kind" of object. */
	private final short _kind;
	/** The name of item. */
	private String _name;
	/** NameSpace URI */
	private final String _nsURI;
	/** Pool of definitions. */
	private final XPool _xp;

	////////////////////////////////////////////////////////////////////////////
	// occurrence
	////////////////////////////////////////////////////////////////////////////
	private final XOccurrence _occ = new XOccurrence();
//	/** occurrence minimum. */
//	private int _min;
//	/** occurrence maximum. */
//	private int _max;

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
	public XNode(final String nsURI,
		final String name,
		final XDPool xp,
		final short kind) {
		_name = name == null ? "" : name.intern();
		_nsURI = nsURI == null ? null : nsURI.intern();
		_xp = (XPool) xp;
		_kind = kind;
	}

	/** Set name of node.
	 * @param name the new name of node.
	 */
	public final void setName(String name) {_name = name;}

////////////////////////////////////////////////////////////////////////////////
// XMNode interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Return the node kind
	 * @return kind of node.
	 */
	public final short getKind() {return _kind;}

	@Override
	/** Get namespace URI.
	 * @return namespace URI.
	 */
	public final String getNSUri() {return _nsURI;}

	@Override
	/** Get name of node.
	 * @return The name of node.
	 */
	public final String getName() {return _name;}

	@Override
	/** Get local name of node.
	 * @return The local name of node.
	 */
	public final String getLocalName() {
		int ndx = _name.indexOf(':');
		return (ndx < 0) ? _name : _name.substring(ndx + 1);
	}

	@Override
	/** Get prefix of name.
	 * @return prefix of name..
	 */
	public final String getNamePrefix() {
		int ndx = _name.indexOf(':');
		return (ndx < 0) ? "" : _name.substring(0, ndx);
	}

	@Override
	/** Get QName of model of the node.
	 * @return QName of node.
	 */
	public QName getQName() {
		int ndx = _name.indexOf(':');
		if (ndx >= 0) {
			String localPart = _name.substring(ndx + 1).intern();
			String prefix = _name.substring(0, ndx).intern();
			return new QName(_nsURI, localPart, prefix);
		}
		return new QName(_nsURI, _name);
	}

	/** Get defPool.
	 * @return The defPool.
	 */
	final XDPool getDefPool() {return _xp;}

	/** Set position to source X-definition.
	 * @param spos position to source X-definition.
	 */
	public final void setSPosition(final SPosition spos) {_spos = spos;}

	@Override
	/** Get position to source X-definition.
	 * @return position to source X-definition or <tt>null</tt>.
	 */
	public final SPosition getSPosition() {
		return _spos == null ? new SPosition() : _spos;
	}

	/** Set position of this node in XDPool.
	 * @param xpos position to XDPool.
	 */
	public final void setXDPosition(final String xpos) {_xdpos = xpos;}

	@Override
	/** Get position of this node in XDPool.
	 * @return position of this node in XDPool.
	 */
	public final String getXDPosition() {return _xdpos;}

	@Override
	/** Return printable value for debugging.
	 * @return  printable value
	 */
	public String toString() {
		switch (_kind) {
			case XMDEFINITION:
				return "XMDEFINITION: "+(_name.isEmpty() ? "(nameless)":_name);
			case XMELEMENT:
				return "XMELEMENT: " + _name;
			case XMTEXT:
				return "XMTEXT: text()";
			case XMATTRIBUTE:
				return "XMATTRIBUTE: " + _name;
			case XMSEQUENCE:
				return "XMSEQUENCE: " + _name;
			case XMCHOICE:
				return "XMCHOICE: " + _name;
			case XMMIXED:
				return "XMMIXED: " + _name;
			case XMCOMMENT:
				return "XMCOMMENT: " + _name;
			case XMPI:
				return "XMPI: " + _name;
			case XMSELECTOR_END:
				return "XMSELECTOR_END: " + _name;
			case XMSELECTOR_END + 1:
				return "REFERENCE: " + _name;
		}
		return "???";
	}

	//to be overriden!
	public abstract void writeXNode(XDWriter xw, ArrayList<XNode> list)
		throws IOException;

	final static XNode readXNode(final XDReader xr,
		final XDefinition xd,
		final ArrayList<XNode> list)
		throws IOException {
		short kind = xr.readShort();
		switch (kind) {
			case -1:
				return list.get(xr.readInt());
			case XMATTRIBUTE:
				return XData.readXData(xr, XMATTRIBUTE, xd);
			case XMCOMMENT:
				return XComment.readXComment(xr, xd);
			case XMELEMENT:
				return XElement.readXElement(xr, xd, list);
			case XMPI:
				return XPI.readXPI(xr, xd);
			case XMTEXT:
				return XData.readXData(xr, XMTEXT, xd);
			case XMSEQUENCE:
			case XMCHOICE:
			case XMMIXED:
				return XSelector.readXSelector(xr, kind);
			case XMSELECTOR_END:
				return new XSelectorEnd();
			default:
				//Internal error&{0}{: }
				throw new SIOException(SYS.SYS066,"Unexpected kind: "+kind);
		}
	}

	@Override
	/** Get XDPool.
	 * @return XDPool to which this XMDefinition belongs.
	 */
	public final XDPool getXDPool() {return getDefPool();}

////////////////////////////////////////////////////////////////////////////////
//implemetation of the interface XMOccurrence
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get min occurrence.
	 * @return min occurrence.
	 */
	public final int minOccurs() {return _occ.minOccurs();}

	@Override
	/** Get max occurrence.
	 * @return max occurrence.
	 */
	public final int maxOccurs() {return _occ.maxOccurs();}


	@Override
	/** Return true if value of occurrence had been specified.
	 * @return <tt>true</tt> if and only if occurrence is specified.
	 */
	public final boolean isSpecified() {return _occ.isSpecified();}

	@Override
	/** Return true if value of occurrence is set as illegal.
	 * @return <tt>true</tt> if and only if occurrence is set as illegal.
	 */
	public final boolean isIllegal() {return _occ.isIllegal();}

	@Override
	/** Return true if value of occurrence is set as ignored.
	 * @return <tt>true</tt> if and only if occurrence is set as ignored.
	 */
	public final boolean isIgnore() {return _occ.isIgnore();}

	@Override
	/** Return true if value of occurrence is set as fixed.
	 * @return <tt>true</tt> if and only if occurrence is set as fixed.
	 */
	public final boolean isFixed() {return _occ.isFixed();}

	@Override
	/** Return true if value of occurrence is set as required.
	 * @return <tt>true</tt> if and only if occurrence is set as required.
	 */
	public final boolean isRequired() {return _occ.isRequired();}

	@Override
	/** Return true if value of occurrence is set as optional.
	 * @return <tt>true</tt> if and only if occurrence is set as optional.
	 */
	public final boolean isOptional() {return _occ.isOptional();}

	@Override
	/** Return true if value of occurrence is set as unbounded.
	 * @return <tt>true</tt> if and only if occurrence is set as unbounded.
	 */
	public final boolean isUnbounded() {return _occ.isUnbounded();}

	@Override
	/** Return true if minimum is greater then 0 and maximum is unbounded.
	 * @return <tt>true</tt> if and only if minimum is greater then 0 and
	 * maximum is unbounded..
	 */
	public final boolean isMaxUnlimited() {return _occ.isMaxUnlimited();}

	@Override
	/** Get occurrence.
	 * @return Occurrence of the node.
	 */
	public final XMOccurrence getOccurence() {
		return new XOccurrence(_occ);
	}
////////////////////////////////////////////////////////////////////////////////
// Protected methods
////////////////////////////////////////////////////////////////////////////////

	/** Compare local name of node from argument with the name of this node.
	 * @param y XNode to be compared.
	 * @param rep reporter where to put errors.
	 * @return true if both local names are equal.
	 */
	protected final boolean compareName(final XNode y, final ArrayReporter rep){
		if (!getLocalName().equals(y.getLocalName())) {
			//Names differs: &{0}, &{1}
			rep.error(XDEF.XDEF289, getXDPosition(), y.getXDPosition());
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
		String path = getXDPosition() + "; " + y.getXDPosition();
		rep.error(XDEF.XDEF288, path); //Namespace differs: &{0}
		return false;
	}

	protected final boolean compareOccurrence(final XNode y,
		final ArrayReporter rep) {
		if (maxOccurs()==y.maxOccurs() && minOccurs()==y.minOccurs()) {
			return true;
		}
		String path = getXDPosition() + "; " + y.getXDPosition();
		rep.error(XDEF.XDEF287, path); //Occurrence differs: &{0}
		return false;
	}

	/** Compare name, namespace and occurrence of the node from argument with
	 * the this node.
	 * @param y XNode to be compared.
	 * @param rep reporter where to put errors.
	 * @return true names, namespaces and occurrences are equal.
	 */
	protected final boolean compareNameAndOccurrence(final XNode y,
		final ArrayReporter rep) {
		return compareName(y, rep)
			&& compareNamespace(y, rep) && compareOccurrence(y, rep);
	}

////////////////////////////////////////////////////////////////////////////////
//implemetation of the interface XOccurrence
////////////////////////////////////////////////////////////////////////////////

	/** Set occurrence values.
	 * @param o occurrence object from which values are imported.
	 */
	public final void setOccurrence(final XMOccurrence o) {
		_occ.setOccurrence(o);
	}

	/** Set occurrence from parameters.
	 * @param min minimum.
	 * @param max maximum.
	 */
	public final void setOccurrence(final int min, final int max) {
		_occ.setOccurrence(min, max);
	}

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