package org.xdef.sys;

import java.util.Currency;
import org.xdef.msg.XDEF;

/** Price with currency code.
 * @author Vaclav Trojan
 */
public class Price {
	/** Amount of currency */
	private final double _amount;
	/** Currency */
	private final Currency _currency;

	/** Create instance of CurrencyAmount.
	 * @param amount currency amount.
	 * @param code ISO4217 currency code.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Price(final double amount, final String code)
		throws SRuntimeException {
		try {
			_amount = amount;
			_currency = Currency.getInstance(code);
		} catch (RuntimeException ex) {
			//"Invalid currency code: "{0}"
			throw new SRuntimeException(XDEF.XDEF575, amount + " " + code);
		}
	}

	/**	Get instance of java.util.Currency object from this object.
	 * @return instance of java.util.Currency object from this object.
	 */
	public final Currency getCurrency() {return _currency;}

	/**	Get amount of currency as decimal number.
	 * @return amount of currency as decimal number.
	 */
	public final double amount() {return _amount;}

	/**	Get ISO4217 code of currency.
	 * @return ISO4217 code of currency..
	 */
	public final String currencyCode() {return _currency.getCurrencyCode();}

	/** Get the default number of fraction digits used with this currency.
	 * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
	 * -1 is returned.
	 * @return default number of fraction digits used with this currency.
	 */
	public final int fractionDigits(){
		return _currency.getDefaultFractionDigits();
	}

	/** Get printable form of this currency amount.
	 * @return printable form of this currency amount with required decimal
	 * digits.
	 */
	public final String display() {
		int i = _currency.getDefaultFractionDigits();
		return (i >= 0 ? String.format("%." + i + "f", _amount)
			: String.valueOf(_amount)) + " " + currencyCode();
	}

	@Override
	public int hashCode() {return 7*((int)_amount) + _currency.hashCode();}

	@Override
	public boolean equals(Object x) {
		if (x instanceof Price) {
			Price y = (Price) x;
			return _amount == y._amount && _currency.equals(y._currency);
		}
		return false;
	}

	@Override
	public String toString() {return _amount + " " + currencyCode();}
}