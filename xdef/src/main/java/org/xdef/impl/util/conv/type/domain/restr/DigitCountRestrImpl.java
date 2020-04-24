package org.xdef.impl.util.conv.type.domain.restr;

/** Skeletal implementation of digit count restriction.
 * @author Ilia Alexandrov
 */
public class DigitCountRestrImpl implements DigitCountRestricted {

	/**
	 *  Fraction digits count restriction.
	 */
	private Integer _fractiohDigits;
	/**
	 *  Total digits count restriction.
	 */
	private Integer _totalDigits;
	@Override
	public void setFractionDigits(int fractionDigits) {
		if (fractionDigits < 0) {
			throw new IllegalArgumentException("Fraction digits count is negative: '" + fractionDigits + "'!");
		}
		_fractiohDigits = fractionDigits;
	}
	@Override
	public void setTotalDigits(int totalDigits) {
		if (totalDigits < 0) {
			throw new IllegalArgumentException("Total digits count is negative: '" + totalDigits + "'!");
		}
		_totalDigits = totalDigits;
	}
	@Override
	public Integer getFractionDigits() {
		return _fractiohDigits;
	}
	@Override
	public Integer getTotalDigits() {
		return _totalDigits;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DigitCountRestrImpl)) {
			return false;
		}
		DigitCountRestrImpl d = (DigitCountRestrImpl) obj;
		if (_fractiohDigits == null ? d._fractiohDigits != null
				: !_fractiohDigits.equals(d._fractiohDigits)) {
			return false;
		}
		return !(_totalDigits == null ? d._totalDigits != null
				: !_totalDigits.equals(d._totalDigits));
	}
	@Override
	public int hashCode() {
		int hash = (this._fractiohDigits != null ? this._fractiohDigits.hashCode() : 0);
		return 79 * hash + (this._totalDigits != null ? this._totalDigits.hashCode() : 0);
	}

	@Override
	public String toString() {
		return "DigitCountRestrImpl[fractionDigits='" + _fractiohDigits + "', "
				+ "totalDigits='" + _totalDigits + "']";
	}
}