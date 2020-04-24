package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import java.util.Locale;

/** The class DefLocale implements items with Locale values.
 * @author Vaclav Trojan
 */
public final class DefLocale extends XDValueAbstract {
	/** The string value of this item. */
	private Locale _value;

	/** Creates a new instance of empty DefString. */
	public DefLocale() {}

	/** Creates a new instance of DeLocale.
	 * @param lang lowercase two-letter ISO-639 code.
	 */
	public DefLocale(final String lang) {
		_value = new Locale(lang.toLowerCase(_value));
	}

	/** Creates a new instance of DeLocale.
	 * @param lang lowercase two-letter ISO-639 code.
	 * @param country uppercase two-letter ISO-3166 code.
	 */
	public DefLocale(final String lang, final String country) {
		_value = new Locale(lang.toLowerCase(), country.toUpperCase());
	}

	/** Creates a new instance of DeLocale.
	 * @param lang lowercase two-letter ISO-639 code.
	 * @param country uppercase two-letter ISO-3166 code.
	 * @param variant vendor and browser specific code..
	 */
	public DefLocale(final String lang,
		final String country,
		final String variant) {
		_value = new Locale(lang.toLowerCase(), country.toUpperCase(), variant);
	}

	/** Creates a new instance of DeLocale.
	 * @param locale Locale.
	 */
	public DefLocale(final Locale locale) {_value = locale;}

	/** Get Locale value.
	 * @return Locale value.
	 */
	public Locale getLocale() {return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _value;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_LOCALE;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.LOCALE;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _value == null ? "" : _value.toString();}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return new DefLocale(_value);}
	@Override
	public int hashCode() {return _value.hashCode();}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return equals(((XDValue) arg));
		}
		return false;
	}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_LOCALE) {
			return false;
		}
		return _value.equals(((DefLocale)arg)._value);
	}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() {return _value == null;}
}