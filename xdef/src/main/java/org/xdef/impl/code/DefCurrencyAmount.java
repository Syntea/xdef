package org.xdef.impl.code;

import java.math.BigDecimal;
import org.xdef.XDCurrencyAmount;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.CurrencyAmount;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** The class implements the internal object with currency amount.
 * @author Vaclav Trojan
 */
public class DefCurrencyAmount extends XDValueAbstract
	implements XDCurrencyAmount {
	/** Value of CurrencyAmount. */
	private final CurrencyAmount _amount;

	/** Create new instance of DefGPosition (null). */
	public DefCurrencyAmount() {_amount = null;}

	/** Create new instance of DefGPosition (the value of altitude is set
	 * as unknown).
	 * @param amount string with amount as decimal number.
	 * @param code currency ISO4217 code.
	 * @throws SRuntimeException (code XDEF222) if parameters are incorrect.
	 */
	public DefCurrencyAmount(final String amount, final String code)
		throws SRuntimeException {
		_amount = new CurrencyAmount(amount, code);
	}

	/** Create new instance of DefGPosition.
	 * @param amount amount as decimal number.
	 * @param code currency ISO4217 code.
	 * in meters (-6376500) to MAX_VALUE (or MIN_VALUE if unknown).
	 * @throws SRuntimeException (code XDEF222) if parameters are incorrect.
	 */
	public DefCurrencyAmount(final BigDecimal amount, final String code)
		throws SRuntimeException {
		_amount = new CurrencyAmount(amount.toString(), code);
	}

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
	public CurrencyAmount CurrencyAmount() {return _amount;}
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