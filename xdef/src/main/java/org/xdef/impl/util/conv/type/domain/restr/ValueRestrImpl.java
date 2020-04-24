package org.xdef.impl.util.conv.type.domain.restr;

/** Skeletal implementation of value restricted type.
 * @author Ilia Alexandrov
 */
public class ValueRestrImpl implements ValueRestricted {

	/** Minimal exclusive value. */
	private String _minExclusive;
	/** Minimal inclusive value. */
	private String _minInclusive;
	/** Maximal inclusive value. */
	private String _maxInclusive;
	/** Maximal exclusive value. */
	private String _maxExclusive;
	@Override
	public String getMinExclusive() {return _minExclusive;}
	@Override
	public String getMinInclusive() {return _minInclusive;}
	@Override
	public String getMaxInclusive() {return _maxInclusive;}
	@Override
	public String getMaxExclusive() {return _maxExclusive;}
	@Override
	public void setMinExclusive(String minExclusive) {
		if (minExclusive == null || minExclusive.length() == 0) {
			throw new IllegalArgumentException("Value is empty!");
		}
		_minExclusive = minExclusive;
	}
	@Override
	public void setMinInclusive(String minInclusive) {
		if (minInclusive == null || minInclusive.length() == 0) {
			throw new IllegalArgumentException("Value is empty!");
		}
		_minInclusive = minInclusive;
	}
	@Override
	public void setMaxInclusive(String maxInclusive) {
		if (maxInclusive == null || maxInclusive.length() == 0) {
			throw new IllegalArgumentException("Value is empty!");
		}
		_maxInclusive = maxInclusive;
	}
	@Override
	public void setMaxExclusive(String maxExclusive) {
		if (maxExclusive == null || maxExclusive.length() == 0) {
			throw new IllegalArgumentException("Value is empty!");
		}
		_maxExclusive = maxExclusive;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ValueRestrImpl)) {
			return false;
		}
		ValueRestrImpl v = (ValueRestrImpl) obj;
		if (_maxExclusive == null ? v._maxExclusive != null
				: !_maxExclusive.equals(v._maxExclusive)) {
			return false;
		}
		if (_maxInclusive == null ? v._maxInclusive != null
				: !_maxInclusive.equals(v._maxInclusive)) {
			return false;
		}
		if (_minExclusive == null ? v._minExclusive != null
				: !_minExclusive.equals(v._minExclusive)) {
			return false;
		}
		return !(_minInclusive == null ? v._minInclusive != null
				: !_minInclusive.equals(v._minInclusive));
	}

	@Override
	public int hashCode() {
		int hash = (this._minExclusive != null
			? this._minExclusive.hashCode() : 0);
		hash = 29 * hash + (this._minInclusive != null
			? this._minInclusive.hashCode() : 0);
		hash = 29 * hash + (this._maxInclusive != null
			? this._maxInclusive.hashCode() : 0);
		hash = 29 * hash + (this._maxExclusive != null
			? this._maxExclusive.hashCode() : 0);
		return hash;
	}
	@Override
	public String toString() {
		return "ValueRestrImpl[minExclusive='" + _minExclusive + "', "
			+ "minInclusive='" + _minInclusive + "', "
			+ "maxInclusive='" + _maxInclusive + "', "
			+ "maxExclusive='" + _maxExclusive + "']";
	}
}