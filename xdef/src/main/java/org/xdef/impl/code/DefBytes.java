package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SException;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.XDBytes;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_BYTES;
import org.xdef.XDValueType;

/** The class DefBytes implements the internal object with byte array.
 * @author Vaclav Trojan
 */
public final class DefBytes extends XDValueAbstract implements XDBytes {

	/** The bytes value of item. */
	byte[] _value;
	/** True if this object was created from base64 otherwise from hex.*/
	boolean _format;

	/** Creates a new instance of DefBytes */
	public DefBytes() {_value = null;}

	/** Creates a new instance of DefBytes
	 * @param value The initial value of object.
	 */
	public DefBytes(final byte[] value) {
		_value = value;
		_format = false;
	}

	/** Creates a new instance of DefBytes
	 * @param value initial value of object.
	 * @param format true if this object was created from base64, false if hex.
	 */
	public DefBytes(final byte[] value, final boolean format) {
		_value = value;
		_format = format;
	}

	/** Creates new instance of DefBytes from a string in Base64 format.
	 * @param s string with encoded Base64 data.
	 * @return parsed DefBytes object.
	 * @throws SException if an error occurs.
	 */
	public static DefBytes parseBase64(final String s) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SUtils.decodeBase64(new StringReader(s), out);
		return new DefBytes(out.toByteArray(), true);
	}

	/** Creates a new instance of DefBytes from a string in hexadecimal format.
	 * @param s string with encoded hexadecimal data.
	 * @return parsed DefBytes object.
	 * @throws SException if an error occurs.
	 */
	public static DefBytes parseHex(final String s) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SUtils.decodeHex(new StringReader(s), out);
		return new DefBytes(out.toByteArray(), false);
	}

	/** Get byte from given position (as a positive integer). If position is out
	 * of limits the method returns -1.
	 * @param pos position (index).
	 * @return integer value of byte from given position or -1.
	 */
	public int getAt(int pos) {
		return _value != null && pos >= 0 && pos < _value.length
			? 0x000000ff & _value[pos] : -1;
	}

	/** Get size of byte array.
	 * @return size of byte array.
	 */
	public int size() {return _value != null ? _value.length : 0; }

	/** Set byte at given position. If position is out of limits the
	 * method does nothing.
	 * @param pos The position (index).
	 * @param b inserted byte.
	 */
	public void setAt(final int pos, final int b) {
		if (_value != null && pos >= 0 && pos < _value.length) {
			_value[pos] = (byte) b;
		}
	}

	/** Clear byte array. */
	public void clear() {_value = new byte[0];}

	/** Add byte to the array on the last position.
	 * @param b added byte.
	 */
	public void add(final int b) {
		if (_value == null || _value.length == 0) {
			_value = new byte[] {(byte) b};
		} else {
			int len = _value.length;
			byte[] old = _value;
			_value = new byte[len + 1];
			System.arraycopy(old, 0, _value, 0, len);
			_value[len] = (byte) b;
		}
	}
	/** Insert byte before given position. If position is out of bounds of the
	 * byte array the SRuntimeException is thrown.
	 * @param pos position (index).
	 * @param b inserted byte.
	 * @throws SRuntimeException if position is out of bounds.
	 */
	public void insertBefore(final int pos, final int b) {
		int len;
		if (_value != null && pos >= 0 && pos <= (len = _value.length)) {
			if (pos == 0 && len == 0) {
				_value = new byte[] {(byte) b};
			} else {
				byte[] old = _value;
				_value = new byte[len + 1];
				if (pos == len) {
					if (len > 0) {
						System.arraycopy(old, 0, _value, 0, len);
					}
					_value[len] = (byte) b;
				} else if (pos == 0) {
					System.arraycopy(old, 0, _value, 1, len);
					_value[0] = (byte) b;
				} else {
					System.arraycopy(old, 0, _value, 0, pos);
					System.arraycopy(old, pos, _value, pos + 1, len - pos);
					_value[pos] = (byte) b;
				}
			}
			return;
		}
		throw new SRuntimeException(SYS.SYS080);//Index out of array
	}

	/** Remove byte(s) from given position. If size exceeds end of byte array
	 * those bytes are ignored.
	 * @param pos position (index).
	 * @param size number of bytes to be removed.
	 */
	public void remove(final int pos, final int size) {
		int len;
		if (size<=0 || pos<0 || _value==null || pos>=(len= _value.length)) {
			return;
		}
		int csize = pos + size > len ? len - pos : size; //corrected size
		int newLen = len - csize; //new length of data
		byte[] old = _value;
		_value = new byte[newLen];
		if (pos == 0) {
			if (newLen == 0) {
				return;
			}
			System.arraycopy(old, csize, _value, 0, newLen);
		} else {
			System.arraycopy(old, 0, _value, 0, pos);
			if (newLen - pos > 0) {
				System.arraycopy(old, pos + csize, _value, pos, newLen - pos);
			}
		}
	}

	@Override
	/** Return true if the format is base64.
	 * @return true if the format is base64 otherwise it is hexadecimal.
	 */
	public boolean isBase64() {return _format;}

	@Override
	/** Return the value of DefBytes as string in Base64 format.
	 * @return string with value of this object in Base64 format.
	 */
	public String getBase64() {
		StringWriter sw = new StringWriter();
		try {
			SUtils.encodeBase64(new ByteArrayInputStream(_value), sw, false);
		} catch (Exception ex) {}
		return sw.toString();
	}

	@Override
	/** Return the value of DefBytes as string in hexadecimal format.
	 * @return string with value of this object in hexadecimal format.
	 */
	public String getHex() {
		StringWriter sw = new StringWriter();
		try {
			SUtils.encodeHex(new ByteArrayInputStream(_value), sw);
		} catch (Exception ex) {}
		return sw.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _value;}
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_BYTES;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.BYTES;}
	@Override
	/** Get object representing value */
	public byte[] getBytes() {return _value;}
	@Override
	/** Get value as String.
	 * @return string with hexadecimal created from value.
	 */
	public String toString() {
		return _value == null ? "" : _format ? getBase64() :getHex();
	}
	@Override
	/** Get string in Base64 format of value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}
	@Override
	public char charValue() {
		return isNull() || _value.length != 1 ? 0 : (char) _value[0];
	}
	@Override
	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	public boolean isNull() {return _value == null;}
	@Override
	public int hashCode() {
		if (_value == null) return 0;
		int result = _value.length*5;
		for (byte b: _value) {result+= result + b;}
		return result;
	}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return equals((XDValue) arg);
		}
		return false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_BYTES) {
			return false;
		}
		return Arrays.equals(_value, ((XDBytes) arg).getBytes());
	}
	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return returns 0 if this object is equal to the specified object.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) {
		if (equals(arg)) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
}