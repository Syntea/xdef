package org.xdef;

import java.util.Currency;
import static org.xdef.XDValueType.CURRENCY;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** X-script object with currency (see ISO 4217).
 * @author Vaclav Trojan
 */
public final class XDCurrency extends XDValueAbstract {
	/** Value of java.util.Currency. */
	private final Currency _value;

	/** Create "null" instance of XDCurrency. */
	public XDCurrency() {_value = null;}

	/** Create new instance of XDCurrency from java.util.Currency.
	 * @param c currency object.
	 */
	public XDCurrency(final Currency c) {_value = c;}

	/** Create new instance of XDCurrency from string with ISO 4217 code.
	 * @param code string with ISO 4217 code.
	 */
	public XDCurrency(final String code) {
		_value = Currency.getInstance(code);
		if (_value == null) {
			//Incorrect value&{0}{ of '}{'}&{1}{: '}{'}&{#SYS000}
			throw new SRuntimeException(XDEF.XDEF809, "Currency", code);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implemened methods of XDCurrency
////////////////////////////////////////////////////////////////////////////////

	/** Gets the ISO 4217 currency code of this currency.
	 * @return currency code of this currency.
	 */
	public final String getCurrencyCode() {return _value.getCurrencyCode();}

////////////////////////////////////////////////////////////////////////////////
// Implemened methods of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public final boolean equals(final Object arg) {
		return arg instanceof XDValue ? equals((XDValue) arg) : false;
	}
	@Override
	public final boolean equals(final XDValue arg) {
		if (arg instanceof XDCurrency) {
			XDCurrency x = (XDCurrency) arg;
			return _value != null ? _value.equals(x._value) : x._value == null;
		}
		return false;
	}
	@Override
	public final int compareTo(final XDValue arg)
		throws IllegalArgumentException {
		if (arg instanceof XDCurrency) {
			if (this.equals((XDCurrency) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public final short getItemId() {return XD_CURRENCY;}
	@Override
	public final XDValueType getItemType() {return CURRENCY;}
	@Override
	public final String stringValue() {
		return isNull()?"":_value.getCurrencyCode();
	}
	@Override
	public final boolean isNull() {return _value == null;}
	@Override
	public final Currency getObject() {return _value;}
	@Override
	public final String toString() {return stringValue();}
}
