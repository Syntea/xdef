package org.xdef.impl;

import org.xdef.impl.code.CodeDisplay;
import org.xdef.model.XMVariable;
import java.io.IOException;

/** Contains variable parameters.
 * deprecated - will be not public in future versions
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
	private boolean _isFinal; // if represents constant.
	/** if true variable is external. */
	private boolean _isExternal; // if  represents externally set value.
	/** if true variable was initialized. */
	private boolean _initialized;

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
	}

	/** Create instance of XVariable object.
	 * @param var variable from which this object will be created.
	 */
	protected XVariable(final XVariable var) {
		_name =  var._name;
		_kind = var._kind;
		_type = var._type;
		_offset = var._offset;
		_isFinal = var._isFinal;
		_isExternal = var._isExternal;
		_initialized = var._initialized;
	}

	/** Get kind of variable (global, local, XModel).
	 * @return 'G' .. global, 'L' .. local, 'X' .. XModel.
	 */
	public final byte getKind() {return _kind;}

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
	public final void setInitialized(final boolean init) {_initialized = init;}

	/** Set isFinal flag.
	 * @param isFinal value of final flag.
	 */
	public final void setFinal(final boolean isFinal) {_isFinal = isFinal;}


	void writeXD(final XDWriter xw) throws IOException {
		xw.writeString(_name);
		xw.writeShort(_type);
		xw.writeByte(_kind);
		xw.writeInt(_offset);
		xw.writeBoolean(_isFinal);
		xw.writeBoolean(_isExternal);
		xw.writeBoolean(_initialized);
	}

	static XVariable readXD(final XDReader xr) throws IOException {
		return new XVariable(xr.readString(),
			xr.readShort(),
			xr.readByte(),
			xr.readInt(),
			xr.readBoolean(),
			xr.readBoolean(),
			xr.readBoolean());
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
			+ "; kind:" + (char) _kind;
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