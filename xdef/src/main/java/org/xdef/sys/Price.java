package org.xdef.sys;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import org.xdef.msg.XDEF;

/** Price with currency code.
 * @author Vaclav Trojan
 */
public class Price {
	/** Amount of currency */
	private final BigDecimal _amount;
	/** Currency */
	private final Currency _currency;
//
//	/** Create instance of CurrencyAmount.
//	 * @param amount currency amount.
//	 * @param code ISO4217 currency code.
//	 * @throws SRuntimeException if an error occurs.
//	 */
//	public Price(final double amount, final String code) {
//		this(new BigDecimal(amount), code);
//	}

	/** Create instance of CurrencyAmount.
	 * @param source string with price.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Price(final String source) throws SRuntimeException {
			int ndx = source.indexOf(' ');
			if (ndx > 0) {
				try {
					_amount = new BigDecimal(source.substring(0, ndx));
					_currency = Currency.getInstance(source.substring(ndx + 1));
				} catch (Exception ex) {
			}
		}
		//Incorrect value of '&{0}'&{1}{: }
		throw new SRuntimeException(XDEF.XDEF809, "price", source);
	}

	/** Create instance of CurrencyAmount.
	 * @param amount currency amount.
	 * @param code ISO4217 currency code.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Price(final BigDecimal amount, final String code)
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
	public final BigDecimal amount() {return _amount;}

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
		// Locale.US -> force dot as decimal point
		return (i >= 0 ? String.format(Locale.US, "%." + i + "f", _amount)
			: String.valueOf(_amount)) + " " + currencyCode();
	}

	@Override
	public int hashCode() {return 7*_amount.hashCode() + _currency.hashCode();}

	@Override
	public boolean equals(Object x) {
		if (x instanceof Price) {
			Price y = (Price) x;
			return _amount.equals(y._amount) && _currency.equals(y._currency);
		}
		return false;
	}

	@Override
	public String toString() {return _amount + " " + currencyCode();}
}
