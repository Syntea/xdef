package org.xdef.impl;

import org.xdef.sys.SRuntimeException;
import org.xdef.XDPool;
import java.io.IOException;

/** Script code descriptor.
 * @author  Vaclav Trojan
 */
public abstract class XCodeDescriptor extends XNode {

	////////////////////////////////////////////////////////////////////////////
	// Actions
	////////////////////////////////////////////////////////////////////////////
	/** Initialize code address or -1. */
	public int _init;
	/** Finally code address or -1. */
	public int _finaly;
	/** Match code address or -1. */
	public int _match;
	/** Create code address or -1. */
	public int _compose;
	/** Check value of attribute or text node code address or -1. */
	public int _check;
	/** OnTrue code address passed code or -1. */
	public int _onTrue;
	/** OnFalse code address code or -1. */
	public int _onFalse;
	/** If text object is missing code address or -1. */
	public int _deflt;
	/** on start of element (attributes are accessible) code address or -1.*/
	public int _onStartElement;
	/** OnAbsence code address or -1. */
	public int _onAbsence;
	/** OnExcess  code address or -1. */
	public int _onExcess;
	/** occurrence of illegal attribute code address or -1. */
	public int _onIllegalAttr;
	/** occurrence of illegal text node code address or -1. */
	public int _onIllegalText;
	/** occurrence of illegal element code address or -1. */
	public int _onIllegalElement;
	/** Variables initialization code address or -1. */
	public int _varinit;

	////////////////////////////////////////////////////////////////////////////
	// Options
	////////////////////////////////////////////////////////////////////////////
	/** "ignore comments" flag. */
	public byte _ignoreComments; //0 not set, 'T' or 'F'
	/** "white spaces" flag for attributes. */
	public byte _attrWhiteSpaces; //0 not set, 'T' or 'F'
	/** "white spaces" flag for text nodes. */
	public byte _textWhiteSpaces; //0 not set, 'T' or 'F'
	/** "ignore empty attributes" flag. */
	public byte _ignoreEmptyAttributes; //0 not set, 'T' or 'F'
	/** flag set case of attribute values to upper(T) or lower(F). */
	public byte _attrValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	/** flag set case of text node values to upper(T) or lower(F). */
	public byte _textValuesCase; //0 not set, 'I' ignore, 'T' or 'F'
	/** flag to trim/not trim attribute value. */
	public byte _trimAttr; //0 not set, 'T' or 'F'
	/** flag to trim/not trim text values. */
	public byte _trimText; //0 not set 'T' or 'F'
	/** flag to ignore entities resolving. */
	public byte _resolveEntities;
	/** flag to ignore resolving of XInclude. */
	public byte _resolveIncludes;
	/** flag to accept qualified attributes for elements with namespace URI. */
	public byte _acceptQualifiedAttr; //0 not set 'T' or 'F'
	/** "more attributes" flag. */
	public byte _moreAttributes; //0 not set, 'T' or 'F'
	/** "more attributes" flag. */
	public byte _moreElements; //0 not set, 'T' or 'F'
	/** "more attributes" flag. */
	public byte _moreText; //0 not set, 'T' or 'F'
	/** version of JSON transformation to XML (see XConstants: JSON_xx). */
	public byte _json; //0 .. no JSON, JSON to XML mode: JSON_W3C or JSON_XD
	/** flag to set element nillable. */
	public byte _nillable; //0 not set 'T' or 'F'
	/** flag to set a text as CDATA section. */
	public byte _cdata; //0 not set 'T' or 'F'

	////////////////////////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////////////////////////

	/** Variable table declared in the node. */
	public XVariableTable _vartable;
	/** Number of variables. */
	public int _varsize;

	/** Creates a new instance of ScriptCode.
	 * @param name Name of item.
	 * @param nsUri NameSpace of X-definition.
	 * @param xp Refers to the XDefPool object.
	 * @param kind The kind of object.
	 */
	public XCodeDescriptor(final String name,
		final String nsUri,
		final XDPool xp,
		final short kind) {
		super(nsUri, name, xp, kind);
		setUnspecified(); // occurrence
		clearActions();
//		_varsize = 0; _vartable = null; clearOptions(); // Java makes it!
	}

	/** Creates the new instance as a copy of given argument.
	 * @param x the XCodeDescriptor object from which is the copy created.
	 */
	public XCodeDescriptor(XCodeDescriptor x) {
		this(x.getName(), x.getNSUri(), x.getKind(), x);
		setSPosition(x.getSPosition());
		setXDPosition(x.getXDPosition());
	}

	/** Creates the new instance as a copy of given argument.
	 * @param name Name of item.
	 * @param nsURI namespace of the object (may be <tt>null</tt>).
	 * @param kind The kind of object.
	 * @param x the XCodeDescriptor object from which is the copy created.
	 */
	public XCodeDescriptor(final String name,
		final String nsURI,
		final short kind,
		XCodeDescriptor x) {
		super(nsURI, name, x.getDefPool(), kind);
		_varsize = x._varsize;
		_vartable = x._vartable == null ? null : x._vartable.cloneTable();
		setOccurrence(x);
		copyOptions(x);
		copyActions(x);
	}

	public final void copyOptions(final XCodeDescriptor x) {
		_ignoreComments = x._ignoreComments;
		_attrWhiteSpaces = x._attrWhiteSpaces;
		_textWhiteSpaces = x._textWhiteSpaces;
		_ignoreEmptyAttributes = x._ignoreEmptyAttributes;
		_attrValuesCase = x._attrValuesCase;
		_textValuesCase = x._textValuesCase;
		_trimAttr = x._trimAttr;
		_trimText = x._trimText;
		_moreElements = x._moreElements;
		_moreText = x._moreText;
		_json = x._json;
		_moreAttributes = x._moreAttributes;
		_resolveEntities = x._resolveEntities;
		_resolveIncludes = x._resolveIncludes;
		_acceptQualifiedAttr = x._acceptQualifiedAttr;
		_nillable = x._nillable;
		_cdata = x._cdata;
	}

	public final void clearOptions() {
		_ignoreComments = _attrWhiteSpaces = _textWhiteSpaces =
			_ignoreEmptyAttributes = _attrValuesCase =
			_textValuesCase = _trimAttr = _trimText = _moreElements =
			_moreText = _json = _moreAttributes = _resolveEntities =
			_resolveIncludes = _acceptQualifiedAttr = _nillable = _cdata = 0;
	}

	public final void copyActions(final XCodeDescriptor x) {
		_init = x._init;
		_finaly = x._finaly;
		_match = x._match;
		_compose = x._compose;
		_check = x._check;
		_onTrue = x._onTrue;
		_onFalse = x._onFalse;
		_deflt = x._deflt;
		_onStartElement = x._onStartElement;
		_onAbsence = x._onAbsence;
		_onExcess = x._onExcess;
		_onIllegalAttr = x._onIllegalAttr;
		_onIllegalText = x._onIllegalText;
		_onIllegalElement = x._onIllegalElement;
		_varinit = x._varinit;
		_vartable = x._vartable;
	}

	public final void clearActions() {
		_init = _finaly = _match = _compose = _check = _onTrue = _onFalse =
		_deflt = _onStartElement = _onAbsence = _onExcess = _onIllegalAttr =
		_onIllegalText = _onIllegalElement = _varinit = -1;
	}

	/** Add node as child.
	 * @param xnode The node to be added.
	 * @throws SRuntimeException if an error occurs.
	 */
	abstract public void addNode(final XNode xnode);

	final void writeXCodeDescriptor(XDWriter xw) throws IOException {
		xw.writeShort(getKind());
		xw.writeString(getName());
		xw.writeString(getNSUri());
		////////////////////////////////////////////////////////////////////////
		// Occurrence
		////////////////////////////////////////////////////////////////////////
		xw.writeInt(minOccurs()); // occurrence minimum.
		xw.writeInt(maxOccurs()); // occurrence maximum.
		////////////////////////////////////////////////////////////////////////
		// Actions
		////////////////////////////////////////////////////////////////////////
		xw.writeInt(_init); // Init command.
		xw.writeInt(_finaly); // Finally.
		xw.writeInt(_match); // Check match condition of an element.
		xw.writeInt(_compose); // Compose action in the compose mode.
		xw.writeInt(_check); // Check value of atribute or text node.
		xw.writeInt(_onTrue); // Type check passed.
		xw.writeInt(_onFalse); // Type check failed.
		xw.writeInt(_deflt); // If text object is missing.
		xw.writeInt(_onStartElement); // on start of element.
		xw.writeInt(_onAbsence); // occurrence lower then minimum.
		xw.writeInt(_onExcess); // occurrence higher then maximum.
		xw.writeInt(_onIllegalAttr); // occurrence of illegal attribute.
		xw.writeInt(_onIllegalText); // occurrence of illegal text node.
		xw.writeInt(_onIllegalElement); // occurrence of illegal element.
		xw.writeInt(_varinit); // init variables.
		////////////////////////////////////////////////////////////////////////
		// Options
		////////////////////////////////////////////////////////////////////////
		byte[] b = new byte[] {
			_ignoreComments, //0 not set, 'T' or 'F'
			_attrWhiteSpaces, //0 not set, 'T' or 'F'
			_textWhiteSpaces, //0 not set, 'T' or 'F'
			_ignoreEmptyAttributes, //0 not set, 'T' or 'F'
			_attrValuesCase, //0 not set, 'I' ignore, 'T' or 'F'
			_textValuesCase, //0 not set, 'I' ignore, 'T' or 'F'
			_trimAttr, //0 not set, 'T' or 'F'
			_trimText, //0 not set 'T' or 'F'
			_resolveEntities,
			_resolveIncludes,
			_acceptQualifiedAttr, //0 not set 'T' or 'F'
			_moreAttributes, //0 not set, 'T' or 'F'
			_moreElements, //0 not set, 'T' or 'F'
			_moreText, //0 not set, 'T' or 'F'
			_json, //0 not set, or JSON version
			_nillable, //0 not set 'T' or 'F'
			_cdata //0 not set 'T' or 'F'
		};
		xw.writeBytes(b);
		////////////////////////////////////////////////////////////////////////
		// variables
		////////////////////////////////////////////////////////////////////////
		xw.writeInt(_varsize); // size of variables.
		if (_vartable == null) {
			xw.writeBoolean(false);
		} else {
			xw.writeBoolean(true);
			_vartable.writeXD(xw);
		}
		////////////////////////////////////////////////////////////////////////
		// Position
		////////////////////////////////////////////////////////////////////////
		xw.writeSPosition(getSPosition());
		xw.writeString(getXDPosition());
	}

	final void readXCodeDescriptor(final XDReader xr) throws IOException {
		// kind, namespace UR name already processed in the upper class
		////////////////////////////////////////////////////////////////////////
		// Occurrence
		////////////////////////////////////////////////////////////////////////
		setOccurrence(xr.readInt() , xr.readInt());// occurrence
		////////////////////////////////////////////////////////////////////////
		// Actions
		////////////////////////////////////////////////////////////////////////
		_init = xr.readInt(); // Init command.
		_finaly = xr.readInt(); // Finally.
		_match = xr.readInt(); // Check match condition of an element.
		_compose = xr.readInt(); // Compose action in the compose mode.
		_check = xr.readInt(); // Check value of atribute or text node.
		_onTrue = xr.readInt(); // Type check passed.
		_onFalse = xr.readInt(); // Type check failed.
		_deflt = xr.readInt(); // If text object is missing.
		_onStartElement = xr.readInt(); // on start of element.
		_onAbsence = xr.readInt(); // occurrence lower then minimum.
		_onExcess = xr.readInt(); // occurrence higher then maximum.
		_onIllegalAttr = xr.readInt(); // occurrence of illegal attribute.
		_onIllegalText = xr.readInt(); // occurrence of illegal text node.
		_onIllegalElement = xr.readInt(); // occurrence of illegal element.
		_varinit = xr.readInt(); // init variables.
		////////////////////////////////////////////////////////////////////////
		// Options
		////////////////////////////////////////////////////////////////////////
		byte[] b = xr.readBytes();
		_ignoreComments = (byte) (b[0] & 255); //0 not set, 'T' or 'F'
		_attrWhiteSpaces = (byte) (b[1] & 255); //0 not set, 'T' or 'F'
		_textWhiteSpaces = (byte) (b[2] & 255); //0 not set, 'T' or 'F'
		_ignoreEmptyAttributes = (byte) (b[3] & 255); //0 not set, 'T','P' or 'F'
		_attrValuesCase = (byte) (b[4] & 255); //0 not set, 'I' ignore, 'T' or 'F'
		_textValuesCase = (byte) (b[5] & 255); //0 not set, 'I' ignore, 'T' or 'F'
		_trimAttr = (byte) (b[6] & 255); //0 not set, 'T' or 'F'
		_trimText = (byte) (b[7] & 255); //0 not set 'T' or 'F'
		_resolveEntities = (byte) (b[8] & 255);
		_resolveIncludes = (byte) (b[9] & 255);
		_acceptQualifiedAttr = (byte) (b[10] & 255); //0 not set 'T' or 'F'
		_moreAttributes = (byte) (b[11] & 255); //0 not set, 'T' or 'F'
		_moreElements = (byte) (b[12] & 255); //0 not set, 'T' or 'F'
		_moreText = (byte) (b[13] & 255); //0 not set, 'T' or 'F'
		_json = (byte) (b[14] & 255); //0 not set, or version number
		_nillable = (byte) (b[15] & 255); //0 not set 'T' or 'F'
		_cdata = (byte) (b[16] & 255); //0 not set 'T' or 'F'

		////////////////////////////////////////////////////////////////////////
		// variables
		////////////////////////////////////////////////////////////////////////
		_varsize = xr.readInt(); // size of variables.
		_vartable = xr.readBoolean() ? XVariableTable.readXD(xr) : null;
		////////////////////////////////////////////////////////////////////////
		// Position
		////////////////////////////////////////////////////////////////////////
		setSPosition(xr.readSPosition());
		setXDPosition(xr.readString());
	}
	@Override
	/** Initialize code or -1.
	 * @return address of initialize code.
	 */
	public final int getInitCode() {return _init;}
	@Override
	/** Finally code or -1.
	 * @return address of finally code or -1.
	 */
	public final int getFinallyCode() {return _finaly;}
	@Override
	/** Match code or -1.
	 * @return address of match code or -1.
	 */
	public final int getMatchCode() {return _match;}
	@Override
	/** Compose action code or -1.
	 * @return address of compose action code or -1.
	 */
	public final int getComposeCode() {return _compose;}
	@Override
	/** Check value of attribute or text node code or -1.
	 * @return address of code of check value method or -1.
	 */
	public final int getCheckCode() {return _check;}
	@Override
	/** Type check passed code or -1.
	 * @return address of check passed method code or -1.
	 */
	public final int getOnTrueCode() {return _onTrue;}
	@Override
	/** Type check failed code or -1.
	 * @return address of failed method code or -1.
	 */
	public final int getOnFalseCode() {return _onFalse;}
	@Override
	/** If text object is missing code or -1.
	 * @return address of text object is missing code or -1.
	 */
	public final int getDefltCode() {return _deflt;}
	@Override
	/** On start of element (all source attributes are accessible) code or -1.
	 * @return address of on start of element method code or -1
	 */
	public final int getOnStartElementCode() {return _onStartElement;}
	@Override
	/** OnAbsence code or -1.
	 * @return address of onAbsence code or -1.
	 */
	public final int getOnAbsenceCode() {return _onAbsence;}
	@Override
	/** OnExcess  code or -1.
	 * @return address of onExcess  code or -1.
	 */
	public final int getOnExcessCode() {return _onExcess;}
	@Override
	/** Occurrence of illegal attribute code or -1.
	 * @return address of occurrence of illegal attribute code or -1.
	 */
	public final int getOnIllegalAttrCode() {return _onIllegalAttr;}
	@Override
	/** Occurrence of illegal text node code or -1.
	 * @return address of occurrence of illegal text node code or -1.
	 */
	public final int getOnIllegalTextCode() {return _onIllegalText;}
	@Override
	/** occurrence of illegal element code or -1.
	 * @return address of occurrence of illegal element code or -1.
	 */
	public final int getOnIllegalElementCode() {return _onIllegalElement;}
	@Override
	/** Variables initialization code or -1.
	 * @return address of variables initialization code or -1.
	 */
	public final int getVarinitCode() {return _varinit;}
}