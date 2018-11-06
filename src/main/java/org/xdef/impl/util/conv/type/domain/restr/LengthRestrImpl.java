package org.xdef.impl.util.conv.type.domain.restr;

/** Skeletal implementation of item length restricted type.
 * @author Ilia Alexandrov
 */
public class LengthRestrImpl implements LengthRestricted {

	/** Length restriction. */
	private Integer _length;
	/** Minimal length restriction. */
	private Integer _minLength;
	/** Maximal length restriction. */
	private Integer _maxLength;

	@Override
	public Integer getLength() {
		return _length;
	}
	@Override
	public Integer getMinLength() {
		return _minLength;
	}
	@Override
	public Integer getMaxLength() {
		return _maxLength;
	}
	@Override
	public void setLength(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("Length is negative: '"
				+ length + "'!");
		}
		_length = length;
	}
	@Override
	public void setMinLength(int minLength) {
		if (minLength < 0) {
			throw new IllegalArgumentException("Minimal length is negative: '"
				+ minLength + "'!");
		}
		_minLength = minLength;
	}
	@Override
	public void setMaxLength(int maxLength) {
		if (maxLength < 0) {
			throw new IllegalArgumentException("Length is negative: '"
				+ maxLength + "'!");
		}
		_maxLength = maxLength;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LengthRestrImpl)) {
			return false;
		}
		LengthRestrImpl l = (LengthRestrImpl) obj;
		if (_length == null ? l._length != null : !_length.equals(l._length)) {
			return false;
		}
		if (_maxLength == null ?
			l._maxLength != null : !_maxLength.equals(l._maxLength)) {
			return false;
		}
		return !(_minLength == null ?
			l._minLength != null : !_minLength.equals(l._minLength));
	}
	@Override
	public int hashCode() {
		int hash = (this._length != null ? this._length.hashCode() : 0);
		hash = 3*hash+(this._minLength != null ? this._minLength.hashCode():0);
		return 3*hash+(this._maxLength != null ? this._maxLength.hashCode():0);
	}
	@Override
	public String toString() {
		return "LengthRestrImpl[length='" + _length + "', "
				+ "minLength='" + _minLength + "', "
				+ "maxLength='" + _maxLength + "']";
	}
}