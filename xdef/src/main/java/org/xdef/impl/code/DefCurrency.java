package org.xdef.impl.code;

import java.util.Currency;
import org.xdef.XDCurrency;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_CURRENCY;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.CURRENCY;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** Object with currency (see ISO 4217).
 * @author Vaclav Trojan
 */
public class DefCurrency extends XDValueAbstract implements XDCurrency {
	/** Value of java.util.Currency. */
	private final Currency _value;

	/** Create new instance null DefCurrency. */
	public DefCurrency() {_value = null;}

	/** Create new instance of DefCurrency from java.util.Currency.
	 * @param c currency object.
	 */
	public DefCurrency(final Currency c) {_value = c;}

	/** Create new instance of DefCurrency from string with ISO 4217 code.
	 * @param code string with ISO 4217 code.
	 */
	public DefCurrency(final String code) {
		_value = Currency.getInstance(code);
		if (_value == null) {
			//Incorrect value&{0}{ of '}{'}&{1}{: '}{'}&{#SYS000}
			throw new SRuntimeException(XDEF.XDEF809, "Currency", code);
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {return _value == null ? 0 : _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		return arg instanceof XDValue ? equals((XDValue) arg) : false;
	}
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefCurrency) {
			DefCurrency x = (DefCurrency) arg;
			return _value != null ? _value.equals(x._value) : x._value == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefCurrency) {
			if (this.equals((DefCurrency) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_CURRENCY;}
	@Override
	public XDValueType getItemType() {return CURRENCY;}
	@Override
	public String stringValue() {return isNull()?"":_value.getCurrencyCode();}
	@Override
	public boolean isNull() {return _value == null;}
	@Override
	public Currency getObject() {return _value;}
	@Override
	public String toString() {return stringValue();}
}