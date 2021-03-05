package org.xdef.impl.code;

import java.math.BigDecimal;
import org.xdef.XDCurrencyAmount;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.SIllegalArgumentException;

/** The class implements the internal object with currency amount.
 * @author Vaclav Trojan
 */
public class DefCurrencyAmount extends XDValueAbstract
	implements XDCurrencyAmount {
	/** Value of CurrencyAmount. */
	private final CurrencyAmount _amount;

	/** Create new instance of DefCurrencyAmount fro CurrencyAmount.
	 * @param amount string with amount as decimal number.
	 */
	public DefCurrencyAmount(final CurrencyAmount amount) {_amount = amount;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDCurrencyAmmount interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public BigDecimal amount() {
		return _amount != null ? _amount.amount() : null;
	}
	@Override
	public String code() {
		return _amount != null ? _amount.code() : null;
	}
	@Override
	public int fractionDigits() {
		return _amount != null ? _amount.fractionDigits() : -1;
	}
	@Override
	public final String display() {
		return _amount != null ? _amount.display() : "null";
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public CurrencyAmount currencyValue() {return _amount;}
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefCurrencyAmount) {
			DefCurrencyAmount x = (DefCurrencyAmount) arg;
			return _amount != null ? _amount.equals(x._amount)
				: x._amount == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefCurrencyAmount) {
			if (this.equals((DefCurrencyAmount) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_CURRAMOUNT;}
	@Override
	public XDValueType getItemType() {return XDValueType.CURRAMOUNT;}
	@Override
	public String stringValue() {return isNull() ? "" : _amount.toString();}
	@Override
	public boolean isNull() {return _amount == null;}
	@Override
	public Object getObject() {return _amount;}
	@Override
	public String toString() {return _amount.toString();}
}