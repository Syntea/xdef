package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.Price;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDPrice;

/** Price with currency amount.
 * @author Vaclav Trojan
 */
public class DefPrice extends XDValueAbstract
	implements XDPrice {
	/** Value of Price. */
	private final Price _amount;

	/** Create new instance null DefPrice. */
	public DefPrice() {_amount = null;}

	/** Create new instance of DefPrice for Price.
	 * @param amount Object contains amount as decimal number and ISO4217 code.
	 */
	public DefPrice(final Price amount) {_amount = amount;}

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
	public String currencyCode() {
		return _amount != null ? _amount.currencyCode() : null;
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
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefPrice) {
			DefPrice x = (DefPrice) arg;
			return _amount != null ? _amount.equals(x._amount)
				: x._amount == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefPrice) {
			if (this.equals((DefPrice) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_PRICE;}
	@Override
	public XDValueType getItemType() {return XDValueType.PRICE;}
	@Override
	public String stringValue() {return isNull() ? "null" : _amount.toString();}
	@Override
	public boolean isNull() {return _amount == null;}
	@Override
	public Price getObject() {return _amount;}
	@Override
	public String toString() {return stringValue();}
}