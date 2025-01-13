package org.xdef;

import java.math.BigDecimal;
import static org.xdef.XDValueType.PRICE;
import org.xdef.msg.SYS;
import org.xdef.sys.Price;
import org.xdef.sys.SIllegalArgumentException;

/** Price (amount and currency).
 * @author Vaclav Trojan
 */
public final class XDPrice extends XDValueAbstract {
	/** Value of Price. */
	private final Price _amount;

	/** Create new instance null DefPrice. */
	public XDPrice() {_amount = null;}

	/** Create new instance of DefPrice for Price.
	 * @param amount Object contains amount as decimal number and ISO4217 code.
	 */
	public XDPrice(final Price amount) {_amount = amount;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of methods of XDPrice
////////////////////////////////////////////////////////////////////////////////

	/**	Get amount of currency.
	 * @return amount of currency.
	 */
	public BigDecimal amount() {return _amount!=null ? _amount.amount() : null;}

	/**	Get ISO4217 code of currency.
	 * @return ISO4217 code of currency..
	 */
	public String currencyCode() {return _amount != null ? _amount.currencyCode() : null;}

	/** Get the default number of fraction digits used with this currency. In the case of pseudo-currencies
	 * -1 is returned.
	 * @return default number of fraction digits used with this currency.
	 */
	public int fractionDigits() {return _amount != null ? _amount.fractionDigits() : -1;}

	/** Get printable form of this currency amount.
	 * @return printable form of this currency amount with required decimal digits.
	 */
	public final String display() {return _amount != null ? _amount.display() : "null";}

////////////////////////////////////////////////////////////////////////////////
// Implementation of methods from XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof XDPrice) {
			XDPrice x = (XDPrice) arg;
			return _amount != null ? _amount.equals(x._amount) : x._amount == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof XDPrice) {
			if (this.equals((XDPrice) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085); //Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_PRICE;}
	@Override
	public XDValueType getItemType() {return PRICE;}
	@Override
	public String stringValue() {return isNull() ? "null" : _amount.toString();}
	@Override
	public boolean isNull() {return _amount == null;}
	@Override
	public Price getObject() {return _amount;}
	@Override
	public String toString() {return stringValue();}
}