package org.xdef.impl.compile;

import org.xdef.impl.XDWriter;
import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.impl.XPool;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XSelector;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;
import org.xdef.msg.SYS;
import org.xdef.sys.ReportWriter;
import java.io.IOException;
import java.util.ArrayList;

/** Provides an object for resolving references in X-definition source. This
 * object is pseudo XNode and will be replaced by referred object.
 * @author Vaclav Trojan
 */
final class CompileReference extends XNode {

	/** Kind of XNode XReference */
	static final short XMREFERENCE = XNode.XMSELECTOR_END + 1;
	/** Kind of XNode  XINCLUDE */
	static final short XMINCLUDE = XMREFERENCE + 1;

	private final String _refXdefName;
	/** The X-definition associated with parent. */
	private final XDefinition _definition;
	/** The parent node. */
	final XNode _parent;
	/** empty parameter. */
	final int _empty;
	/** Initial method address. */
	final int _initMethod;
	/** Match method. */
	final int _matchMethod;
	/** Absence method. */
	final int _absenceMethod;
	/** Excess method. */
	final int _excessMethod;
	/** setSource method address (in create mode). */
	final int _setSourceMethod;
	/** finally method address . */
	final int _finallyMethod;
	/** Model initialization. */
	final int _varinit;

	/** Creates a new instance of XReference.
	 * @param kind XMREFERENCE or XMINCLUDE.
	 * @param parent parent - X-definition or XArchive object.
	 * @param nsURI namespace URI of identifier.
	 * @param refName reference name.
	 * @param position source position where the reference was specified.
	 * @throws SRuntimeException if an error occurs.
	 */
	CompileReference(final short kind,
		final XNode parent,
		final String nsURI,
		final String refName,
		final SPosition position) {
		this(kind, parent, nsURI, refName, position, null);
	}

	/** Creates a new instance of XReference.
	 * @param kind type of object: XMREFERENCE or XMINCLUDE.
	 * @param parent parent - X-definition or XArchive object.
	 * @param nsURI name space URI of identifier.
	 * @param refName reference name.
	 * @param position source position where the reference was specified.
	 * @param xsel XSelector object.
	 * @throws SRuntimeException if an error occurs.
	 */
	CompileReference(final short kind,
		final XNode parent,
		final String nsURI,
		final String refName,
		final SPosition position,
		final XSelector xsel) {
		super(nsURI, getRefPart(refName), parent.getXDPool(), kind);
		_parent = parent;
		if (xsel == null) {
			_empty = -1;
			_matchMethod = -1;
			_initMethod = -1;
			_absenceMethod = -1;
			_excessMethod = -1;
			_setSourceMethod = -1;
			_finallyMethod = -1;
		} else {
			setOccurrence(xsel.minOccurs(), xsel.maxOccurs());
			_empty = xsel.isEmptyDeclared() ? xsel.isEmptyFlag() ? 1 : 0 : -1;
			_matchMethod = xsel.getMatchCode();
			_initMethod = xsel.getInitCode();
			_absenceMethod = xsel.getOnAbsenceCode();
			_excessMethod = xsel.getOnExcessCode();
			_setSourceMethod = xsel.getComposeCode();
			_finallyMethod = xsel.getFinallyCode();
		}
		setSPosition(position);
		switch (parent.getKind()) {
			case XNode.XMELEMENT:
				_definition = ((XElement) parent)._definition;
				_varinit = ((XElement) parent)._varinit;
				break;
			case XNode.XMDEFINITION:
				_definition = (XDefinition) parent;
				_varinit = -1;
				break;
			default:
				throw new SRuntimeException(XDEF.XDEF309,//Internal error:&{0}
					"Incorrect reference node: " + parent.getKind() +
					": " + getName() +
					(position != null && position.getLineNumber() > 0 ?
					"&{line}" + position.getLineNumber()
					+ "&{column}" + position.getColumnNumber() : "")
					+ (position.getSystemId()==null
						&& !position.getSystemId().isEmpty()
						? "&{sysId}" + position.getSystemId() : ""));
		}
		int ndx = refName.lastIndexOf('#');
		_refXdefName = ndx > 0 ?
			refName.substring(0, ndx) : ndx == 0 ? "" : _definition.getName();
	}

	private static String getRefPart(final String refName) {
		int ndx = refName.indexOf('/');
		String s = ndx > 0 ? refName.substring(0, ndx) : refName;
		String t = ndx > 0 ? refName.substring(ndx) : "";
		ndx = s.lastIndexOf('#');
		return (ndx >= 0 ? s.substring(ndx + 1) : s) + t;
	}

	/** Put report of unresolved target reference to reporter.
	 * @reporter where to put report.
	 */
	void putTargetError(final ReportWriter reporter) {
		String s = ((_refXdefName == null || (_refXdefName.length() == 0)
			? (_definition.getName() + '#' + getName())
			: (_refXdefName + '#' + getName())));
		//Referred object doesn't exist: &{0}
		getSPosition().putReport(Report.error(XDEF.XDEF307, s), reporter);
	}

	/** Get reference target model XNode.
	 * @param name the name of required model of element.
	 * @return the found XElement or null.
	 */
	public XNode getTargetModel() {
		String name = getName();
		if ("*".equals(name)) { // any in root selection
			XElement result = new XElement("$any", null, _definition);
			result.setSPosition(getSPosition());
			result.setXDPosition(_definition.getXDPosition() + "*");
			result._moreAttributes = 'T';
			result._moreElements = 'T';
			result.setUnbounded();
			return result;
		}
		XMDefinition xdef = getXDPool().getXMDefinition(_refXdefName);
		if (xdef == null) { // X-definition not found
			return null;
		}
		int ndx = name.indexOf('/');
		String mName = ndx > 0 ? name.substring(0, ndx) : name; //model name
		XElement xe = (XElement) xdef.getModel(getNSUri(), mName);
		if (xe == null) {
			String s = mName + "$any";
			xe = (XElement) xdef.getModel(getNSUri(), s);
			if (xe != null) {
				setName(s);
			} else {
				XPool xp = (XPool) xdef.getXDPool();
				s = _refXdefName + '#' + mName;
				if (ndx > 0) {
					String t = name.substring(ndx);
					String u = "$choice/$choice";
					XMNode xn = xp.findModel(s + u + t);
					if (xn == null) {
						u = "$mixed/$mixed";
						xn = xp.findModel(s + u + t);
					}
					if (xn == null) {
						u = "$sequence/$sequence";
						xn = xp.findModel(s + u + t);
					}
					if (xn != null) {
						setName(s + u + t);
					}
					return (XNode) xn;
				} else {
					XMNode xn = xp.findModel(s + "$choice");
					if (xn != null) {
						setName(s + "$choice");
						return (XNode) xn;
					}
				}
			}

		}
		if (xe != null) {
			if(ndx > 0) {
				XMNode xn = XPool.findXMNode(xe, name.substring(ndx+1), 0, -1);
				if (xn == null) {
					return null;
				}
				return (XNode) xn;
			} else if (xe._json > 0) {
				XMNode[] models = xe.getChildNodeModels();
				if (models.length==1 && models[0].getKind()==XMNode.XMELEMENT) {
					return (XElement) models[0];
				}
			}
		}
		return xe;
	}

////////////////////////////////////////////////////////////////////////////////
//  XNode interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public XMDefinition getXMDefinition() {return _definition;}
	@Override
	public String toString() {
		return (getKind() == XMREFERENCE ? "REFERENCE: " + getName() :
			getKind() == XMINCLUDE ? "INCLUDE: " + getName() :
			super.toString()) +
			getNSUri() != null ? '{' + getNSUri() + '}' : "";
	}
	@Override
	public int getInitCode() {return -1;} //not supported here
	@Override
	public int getFinallyCode() {return -1;} //not supported here
	@Override
	public int getMatchCode() {return -1;} //not supported here
	@Override
	public int getComposeCode() {return -1;} //not supported here
	@Override
	public int getCheckCode() {return -1;} //not supported here
	@Override
	public int getOnTrueCode() {return -1;} //not supported here
	@Override
	public int getOnFalseCode() {return -1;} //not supported here
	@Override
	public int getDefltCode() {return -1;} //not supported here
	@Override
	public int getOnStartElementCode() {return -1;} //not supported here
	@Override
	public int getOnAbsenceCode() {return -1;} //not supported here
	@Override
	public int getOnExcessCode() {return -1;} //not supported here
	@Override
	public int getOnIllegalAttrCode() {return -1;} //not supported here
	@Override
	public int getOnIllegalTextCode() {return -1;} //not supported here
	@Override
	public int getOnIllegalElementCode() {return -1;} //not supported here
	@Override
	public int getVarinitCode() {return -1;} //not supported here

	@Override
	public final void writeXNode(final XDWriter xw, final ArrayList<XNode> list)
		throws IOException {
		throw new SRuntimeException(SYS.SYS066, //Internal error&{0}{: }
			"this method can't be called here");
	}
}