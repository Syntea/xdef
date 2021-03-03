package org.xdef;

import java.math.BigDecimal;

/** Currency amount objects.
 * @author Vaclav Trojan
 */
public interface XDCurrencyAmount extends XDValue {

	/**	Get amount of currency as decimal number.
	 * @return amount of currency as decimal number.
	 */
	public BigDecimal amount();

	/**	Get ISO4217 code of currency.
	 * @return ISO4217 code of currency..
	 */
	public String code();

	/** Get the default number of fraction digits used with this currency.
	 * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
	 * -1 is returned.
	 * @return default number of fraction digits used with this currency.
	 */
	public int fractionDigits();

	/** Get printable form of this currency amount.
	 * @return printable form of this currency amount with required decimal
	 * digits.
	 */
	public String display();
}
