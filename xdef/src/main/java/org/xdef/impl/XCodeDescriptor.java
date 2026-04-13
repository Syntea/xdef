package org.xdef.impl;

import java.io.IOException;
import org.xdef.XDPool;

/** Script code descriptor.
 * @author Vaclav Trojan
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
    /** flag to set element nillable. */
    public byte _nillable; //0 not set 'T' or 'F'
    /** flag to set a text as CDATA section. */
    public byte _cdata; //0 not set 'T' or 'F'
    /** Flag to set JSON values null is not allowed. */
    public byte _noJsonNull; //0 not set 'T' or 'F'
    ////////////////////////////////////////////////////////////////////////////
    /** version of XON/JSON transformation to XML (see XConstants: XON_xx). */
    public byte _xonVersion; //0...no XON/JSON, 1...XON/JSON to XML mode: W3C or XD
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
    }

    /** Creates the new instance as a copy of given argument.
     * @param x XCodeDescriptor object from which this instance of XCodeDescriptor is created.
     */
    public XCodeDescriptor(XCodeDescriptor x) {
        this(x.getName(), x.getNSUri(), x.getKind(), x);
        setSPosition(x.getSPosition());
        setXDPosition(x.getXDPosition());
    }

    /** Creates the new instance as a copy of given argument.
     * @param name Name of item.
     * @param nsURI namespace of the object (may be null).
     * @param kind The kind of object.
     * @param x XCodeDescriptor object from which this instance of XCodeDescriptor is created.
     */
    public XCodeDescriptor(final String name, final String nsURI, final short kind, XCodeDescriptor x) {
        super(nsURI, name, x.getXDPool(), kind);
        _varsize = x._varsize;
        _vartable = x._vartable == null ? null : x._vartable.cloneTable();
        setOccurrence(x);
        copyOptions(x);
        copyActions(x);
    }

    /** Clear all options in this descriptor. */
    public final void clearOptions() {
        _xonVersion = _ignoreComments = _attrWhiteSpaces = _textWhiteSpaces = _ignoreEmptyAttributes = _attrValuesCase
            = _textValuesCase = _trimAttr = _trimText = _moreElements = _moreText =  _moreAttributes
            = _resolveEntities = _resolveIncludes = _acceptQualifiedAttr = _nillable = _cdata = _noJsonNull = 0;
    }

    /** Copy all options from the given XCodeDescriptor to this object.
     * @param x XCodeDescriptor from which to copy options to this object.
     */
    public final void copyOptions(final XCodeDescriptor x) {
        _xonVersion = x. _xonVersion;
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
        _moreAttributes = x._moreAttributes;
        _resolveEntities = x._resolveEntities;
        _resolveIncludes = x._resolveIncludes;
        _acceptQualifiedAttr = x._acceptQualifiedAttr;
        _nillable = x._nillable;
        _cdata = x._cdata;
        _noJsonNull = x._noJsonNull;
    }

    /** Clear all actions in this descriptor. */
    public final void clearActions() {
        _init = _finaly = _match = _compose = _check = _onTrue = _onFalse = _deflt = _onStartElement
            = _onAbsence = _onExcess = _onIllegalAttr = _onIllegalText = _onIllegalElement = _varinit = -1;
    }

    /** Get options as array of bytes.
     * @return array of bytes with option values.
     */
    public final byte[] optionsToBytes() {
        return new byte[] { // write all options as byte array.
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
            _nillable, //0 not set 'T' or 'F'
            _cdata, //0 not set 'T' or 'F'
            _noJsonNull};//0 not set 'T' or 'F'
    }

    /** Set values of options from the array of bytes.
     * @param bytes array of bytes with values of optsions.
     */
    public final void optionsFromBytes(final byte[] bytes) {
        _ignoreComments = bytes[0];
        _attrWhiteSpaces = bytes[1];
        _textWhiteSpaces = bytes[2];
        _ignoreEmptyAttributes = bytes[3];
        _attrValuesCase = bytes[4];
        _textValuesCase = bytes[5];
        _trimAttr = bytes[6];
        _trimText = bytes[7];
        _resolveEntities = bytes[8];
        _resolveIncludes = bytes[9];
        _acceptQualifiedAttr = bytes[10];
        _moreAttributes = bytes[11];
        _moreElements = bytes[12];
        _moreText = bytes[13];
        _nillable = bytes[14];
        _cdata = bytes[15];
        _noJsonNull = bytes[16];
    }

    /** Copy all actions from the given XCodeDescriptor to this object.
     * @param x XCodeDescriptor from which to copy actions to this object.
     */
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

    /** Initialize code address or -1.
     * @return address of initialize code.
     */
    @Override
    public final int getInitCode() {return _init;}

    /** Finally code address or -1.
     * @return address of finally code or -1.
     */
    @Override
    public final int getFinallyCode() {return _finaly;}

    /** Match code address or -1.
     * @return address of match code or -1.
     */
    @Override
    public final int getMatchCode() {return _match;}

    /** Compose action code address or -1.
     * @return address of compose action code or -1.
     */
    @Override
    public final int getComposeCode() {return _compose;}

    /** Check value of attribute or text node code address or -1.
     * @return address of code of check value method or -1.
     */
    @Override
    public final int getCheckCode() {return _check;}

    /** Type check passed code address or -1.
     * @return address of check passed method code or -1.
     */
    @Override
    public final int getOnTrueCode() {return _onTrue;}

    /** Type check failed code address or -1.
     * @return address of failed method code or -1.
     */
    @Override
    public final int getOnFalseCode() {return _onFalse;}

    /** If text object is missing code address or -1.
     * @return address of text object is missing code or -1.
     */
    @Override
    public final int getDefltCode() {return _deflt;}

    /** On start of element (all source attributes are accessible) code address or -1.
     * @return address of on start of element method code or -1
     */
    @Override
    public final int getOnStartElementCode() {return _onStartElement;}

    /** OnAbsence code address or -1.
     * @return address of onAbsence code or -1.
     */
    @Override
    public final int getOnAbsenceCode() {return _onAbsence;}

    /** OnExcess code address or -1.
     * @return address of onExcess  code or -1.
     */
    @Override
    public final int getOnExcessCode() {return _onExcess;}

    /** Occurrence of illegal attribute code address or -1.
     * @return address of occurrence of illegal attribute code or -1.
     */
    @Override
    public final int getOnIllegalAttrCode() {return _onIllegalAttr;}

    /** Occurrence of illegal text node code address or -1.
     * @return address of occurrence of illegal text node code or -1.
     */
    @Override
    public final int getOnIllegalTextCode() {return _onIllegalText;}

    /** occurrence of illegal element code address or -1.
     * @return address of occurrence of illegal element code or -1.
     */
    @Override
    public final int getOnIllegalElementCode() {return _onIllegalElement;}

    /** Variables initialization code address or -1.
     * @return address of variables initialization code or -1.
     */
    @Override
    public final int getVarinitCode() {return _varinit;}

    /** Write descriptor to XDWriter.
     * @param xw where to write.
     * @throws IOException if an error occurs.
     */
    final void writeXCodeDescriptor(XDWriter xw) throws IOException {
        xw.writeShort(getKind());
        xw.writeString(getName());
        xw.writeString(getNSUri());
        // Occurrence
        xw.writeInt(minOccurs()); // occurrence minimum.
        xw.writeInt(maxOccurs()); // occurrence maximum.
        // Actions
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
        xw.writeByte(_xonVersion); // xon version.
        // Options
        xw.writeBytes(optionsToBytes());
        // variables
        xw.writeInt(_varsize); // size of variables.
        if (_vartable == null) {
            xw.writeBoolean(false);
        } else {
            xw.writeBoolean(true);
            _vartable.writeXD(xw);
        }
        // Position
        xw.writeSPosition(getSPosition());
        xw.writeString(getXDPosition());
    }

    /** Read descriptor from XDReader.
     * @param xw where to read.
     * @throws IOException if an error occurs.
     */
    final void readXCodeDescriptor(final XDReader xr) throws IOException {
        // kind, namespace UR name already processed in the upper class
        setOccurrence(xr.readInt(), xr.readInt()); // read occurrence
        // Actions
        _init = xr.readInt(); // Init action.
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
        _xonVersion = xr.readByte(); // xon version.
        // Options
        optionsFromBytes(xr.readBytes());
        // variables
        _varsize = xr.readInt(); // size of variables.
        _vartable = xr.readBoolean() ? XVariableTable.readXD(xr) : null;
        // Position
        setSPosition(xr.readSPosition());
        setXDPosition(xr.readString());
    }
}
