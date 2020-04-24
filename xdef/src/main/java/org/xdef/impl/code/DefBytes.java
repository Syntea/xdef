package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SException;
import org.xdef.sys.SIllegalArgumentException;
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
import org.xdef.XDValueType;

/** The class DefBytes implements the internal object with byte array.
 * @author Vaclav Trojan
 */
public final class DefBytes extends XDValueAbstract implements XDBytes {

	/** The boolean value of item. */
	private byte[] _value;

	/** Creates a new instance of DefBytes */
	public DefBytes() {_value = null;}

	/** Creates a new instance of DefBytes
	 * @param value The initial value of object.
	 */
	public DefBytes(final byte[] value) {_value = value;}

	/** Creates a new instance of DefBytes from string in Base64 format
	 * @param s The string with encoded Base64 data.
	 * @return DefBytes object.
	 * @throws SException if an error occurs.
	 */
	public static DefBytes parseBase64(final String s) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SUtils.decodeBase64(new StringReader(s), out);
		return new DefBytes(out.toByteArray());
	}

	/** Creates a new instance of DefBytes from string in hexadecimal format
	 * @param s The string with encoded hexadecimal data.
	 * @return DefBytes object.
	 * @throws SException if an error occurs.
	 */
	public static DefBytes parseHex(final String s) throws SException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SUtils.decodeHex(new StringReader(s), out);
		return new DefBytes(out.toByteArray());
	}

	/** Get byte from given position (as positive integer). If position is out
	 * of limits the method returns -1.
	 * @param pos The position (index).
	 * @return integer value of byte from given position
	 */
	public int getAt(int pos) {
		if (pos >= 0 && _value != null && pos < _value.length) {
			byte b = _value[pos];
			return b < 0 ? 256 + b : b;
		}
		return -1;
	}

	/** Get byte from given position (as positive integer). If position is out
	 * of limits the method returns -1.
	 * @return size of byle array.
	 */
	public int size() {return _value != null ? _value.length : 0; }

	/** Set byte at given position. If position is out of limits the
	 * method does nothing.
	 * @param pos The position (index).
	 * @param b inserted byte.
	 */
	public void setAt(final int pos, final int b) {
		if (pos >= 0 && _value != null && pos < _value.length) {
			_value[pos] = (byte) b;
		}
	}

	/** Clear byte array. */
	public void clear() {_value = new byte[0];}

	/** Add byte to the array on the last position.
	 * @param b added byte.
	 */
	public void add(final int b) {
		if (_value == null) {
			_value = new byte[] {(byte) b};
		} else {
			int len = _value.length;
			if (len > 0) {
				byte[] old = _value;
				_value = new byte[len + 1];
				System.arraycopy(old, 0, _value, 0, len);
			} else {
				_value = new byte[1];
			}
			_value[len] = (byte) b;
		}
	}

	/** Insert byte before given position. If position is out of limits the
	 * method does nothing.
	 * @param pos The position (index).
	 * @param b inserted byte.
	 */
	public void insertBefore(final int pos, final int b) {
		int len;
		if (pos < 0 || _value == null || pos > (len = _value.length)) {
			return;
		}
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

	/** Remove byte(s) from given position. Ignore positions out of limits.
	 * @param pos The position (index).
	 * @param size number of removed bytes.
	 */
	public void remove(final int pos, final int size) {
		int len;
		if (size<=0 || pos<0 || _value==null || pos>=(len= _value.length)) {
			return;
		}
		int csize; //corrected size
		if (pos + size > len) {
			csize = len - pos;
		} else {
			csize = size;
		}
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
	public String toString() {return _value == null ? "" : getHex();}
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
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
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