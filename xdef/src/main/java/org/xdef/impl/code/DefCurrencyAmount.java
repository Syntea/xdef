package org.xdef.impl.code;

import org.xdef.XDCurrencyAmount;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.SIllegalArgumentException;

/** Implementation of objects with currency amount.
 * @author Vaclav Trojan
 */
public class DefCurrencyAmount extends XDValueAbstract
	implements XDCurrencyAmount {
	/** Value of CurrencyAmount. */
	private final CurrencyAmount _amount;

	/** Create new instance null DefCurrencyAmount. */
	public DefCurrencyAmount() {_amount = null;}

	/** Create new instance of DefCurrencyAmount for CurrencyAmount.
	 * @param amount Object contains amount as decimal number and ISO4217 code.
	 */
	public DefCurrencyAmount(final CurrencyAmount amount) {_amount = amount;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDCurrencyAmmount interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/**	Get amount of currency.
	 * @return amount of currency.
	 */
	public double amount() {
		return _amount != null ? _amount.amount() : 0;
	}
	@Override
	/**	Get ISO4217 code of currency.
	 * @return ISO4217 code of currency..
	 */
	public String code() {
		return _amount != null ? _amount.code() : null;
	}
	@Override
	/** Get the default number of fraction digits used with this currency.
	 * In the case of pseudo-currencies -1 is returned.
	 * @return default number of fraction digits used with this currency.
	 */
	public int fractionDigits() {
		return _amount != null ? _amount.fractionDigits() : -1;
	}
	@Override
	/** Get printable form of this currency amount.
	 * @return printable form of this currency amount with required decimal
	 * digits.
	 */
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