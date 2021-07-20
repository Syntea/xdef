package org.xdef.impl;

import java.io.IOException;
import org.xdef.XDValueID;
import org.xdef.impl.code.CodeDisplay;
import org.xdef.model.XMVariable;

/** Implementation of XMVariable.
 * @author Vaclav Trojan
 */
public class XVariable implements XMVariable {
	/** Name of variable. */
	private final String _name;
	/** Type of variable. */
	private final short _type;
	/** Kind of variable 'G' .. global, 'L' .. local, 'X' .. Model. */
	private final byte _kind;
	/** offset (address) of variable */
	private int _offset;
	/** if true variable is final. */
	private boolean _isFinal; // if it is a constant.
	/** if true variable is external. */
	private boolean _isExternal; // if it is externally set value.
	/** if true variable was initialized. */
	private boolean _initialized;
	/** Type of parsed object or XDValueID.XD_VOID if it not exists. */
	private short _parseResultType;
	/** Reference name to declared type (valid only for uniqueset keys).*/
	/** Code address of check method or -1 if it not exists. */
	private int _parseMethodAddr;
	/** Name of key reference. */
	private String _refTypeName;
	/** Key index in Uniqueset. */
	private int _keyIndex;

	/** Create instance of XVariable object.
	 * @param name name of variable.
	 * @param type type of variable (see org.xdef.XDValueID).
	 * @param kind kind of variable ('G' .. global,'L' .. local,'X' .. XModel).
	 * @param offset address of variable.
	 * @param isFinal if true the variable is final (can't be changed).
	 * @param isExternal if true the variable is external.
	 * @param initialized if true the variable is initialized.
	 */
	protected XVariable(final String name,
		final short type,
		final byte kind,
		final int offset,
		final boolean isFinal,
		final boolean isExternal,
		final boolean initialized) {
		_name = name.intern();
		_kind = kind;
		_type = type;
		_offset = offset;
		_isFinal = isFinal;
		_isExternal = isExternal;
		_initialized = initialized;
		_parseResultType =  XDValueID.XD_VOID; // no parsed type
		_parseMethodAddr = -1;
//		_refTypeName = null; // java makes it
		_keyIndex = -1;
	}

////////////////////////////////////////////////////////////////////////////////
// internally used methods
////////////////////////////////////////////////////////////////////////////////
	/** Set offset of variable.
	 * @param offset variable offset.
	 */
	public final void setOffset(final int offset) {_offset=offset;}

	/** Set isExternal flag.
	 * @param ext value of external flag.
	 */
	public final void setExternal(final boolean ext) {_isExternal = ext;}
	/** Set this field is initialized.
	 * @param init value of initialized flag.
	 */
	public final void setInitialized(final boolean init) {
		_initialized = init;
	}
	/** Set isFinal flag.
	 * @param isFinal value of final flag.
	 */
	public final void setFinal(final boolean isFinal) {_isFinal = isFinal;}
	/** Get parsed result type of variable.
	 * @return type of parseResult value.
	 */
	public final short getParseResultType() {return _parseResultType;}
	/** Set parsed result type of variable.
	 * @param type type of parseResult value or void.
	 */
	public final void setParseResultType(short type) {_parseResultType = type;}
	/** Get parse method address.
	 * @return address of parse method or -1;
	 */
	public final int getParseMethodAddr() {return _parseMethodAddr;}
	/** Set parse method address.
	 * @param method  address of parse method or -1
	 */
	public final void setParseMethodAddr(int method) {_parseMethodAddr=method;}
	/** Get reference name of declared type (valid only for Uniqueset keys).
	 * @return reference name of declared type or null.
	 */
	public final String getKeyRefName() {return _refTypeName;}
	/** Set reference name of declared type (valid only for Uniqueset keys).
	 * @param x reference name of declared type or null.
	 */
	public final void setKeyRefName(final String x) {_refTypeName = x;}
	/** Get key index of Uniqueset key (valid only for Uniqueset key).
	 * @return key index of Uniqueset key or -1.
	 */
	public final int getKeyIndex() {return _keyIndex;}
	/** Set key index of Uniqueset key (valid only for Uniqueset key).
	 * @param keyIndex key index of Uniqueset key or -1.
	 */
	public final void setKeyIndex(int keyIndex) {_keyIndex = keyIndex;}
	/** Write this object to XDWriter
	 * @param xw XDWriter where to write.
	 * @throws IOException if an error occurs.
	 */
	void writeXD(final XDWriter xw) throws IOException {
		xw.writeString(_name);
		xw.writeShort(_type);
		xw.writeByte(_kind);
		xw.writeInt(_offset);
		xw.writeBoolean(_isFinal);
		xw.writeBoolean(_isExternal);
		xw.writeBoolean(_initialized);
		xw.writeShort(_parseResultType);
		xw.writeInt(_parseMethodAddr);
		xw.writeString(_refTypeName);
		xw.writeInt(_keyIndex);
	}
	/** Read XVariable from XDReader.
	 * @param xr the XDReader from which to read XVariable.
	 * @return XVariable read from XDReader.
	 * @throws IOException if an error occurs.
	 */
	static XVariable readXD(final XDReader xr) throws IOException {
		XVariable result = new XVariable(xr.readString(),
			xr.readShort(),
			xr.readByte(),
			xr.readInt(),
			xr.readBoolean(),
			xr.readBoolean(),
			xr.readBoolean());
		result._parseResultType = xr.readShort();
		result._parseMethodAddr = xr.readInt();
		result._refTypeName = xr.readString();
		result._keyIndex = xr.readInt();
		return result;
	}
	@Override
	public final int hashCode() {return _name.hashCode();}
	@Override
	public final boolean equals(final Object o) {
		return (o == null) ? false :
			(o instanceof XVariable) ? _name.equals(((XVariable) o)._name) :
			(o instanceof String) ? _name.equals((String) o) : false;
	}
	@Override
	public String toString() {
		return (isExternal() ? "external " : "") + (isFinal() ? "final " : "")+
			CodeDisplay.getTypeName(_type) + " " + getName() +
			", offset:" + _offset + ", initialized:" + _initialized
			+ "; kind:" + (char) _kind + "; parseMethodAddr:" + _parseMethodAddr
			+ "; keyIndex:" + _keyIndex;
	}

////////////////////////////////////////////////////////////////////////////////
// implementation of XMVariable
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get name of variable.
	 * @return name of variable.
	 */
	public final String getName() {return _name;}
	@Override
	/** Get type of variable (see org.xdef.XDValueID).
	 * @return type of variable.
	 */
	public final short getType() {return _type;}
	@Override
	/** Get kind of variable (global, local, XModel).
	 * @return 'G' .. global, 'L' .. local, 'X' .. XModel.
	 */
	public final byte getKind() {return _kind;}
	@Override
	/** Get "final" flag.
	 * @return true if and only if variable is declared as final.
	 */
	public final boolean isFinal() {return _isFinal;}
	@Override
	/** Get "external" flag.
	 * @return true if and only if variable is declared as external.
	 */
	public final boolean isExternal() {return _isExternal;}
	@Override
	/** Check if this field is initialized. */
	public final boolean isInitialized() {return _kind == 'G' || _initialized;}
	@Override
	/** Get offset (address) of variable.
	 * @return offset of variable.
	 */
	public final int getOffset() {return _offset;}
}