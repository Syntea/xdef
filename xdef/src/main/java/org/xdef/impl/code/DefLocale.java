package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import java.util.Locale;
import static org.xdef.XDValueType.LOCALE;

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
    public DefLocale(final String lang) {_value = new Locale(lang);}

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
    public DefLocale(final String lang, final String country, final String variant) {
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

    /** Get associated object.
     * @return the associated object or null.
     */
    @Override
    public Object getObject() {return _value;}

    /** Get type of value.
     * @return The id of item type.
     */
    @Override
    public short getItemId() {return XD_LOCALE;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return LOCALE;}

    /** Get value as String.
     * @return The string from value.
     */
    @Override
    public String toString() {return _value == null ? "" : _value.toString();}

    /** Get string value of this object.
     * @return string value of this object.
     * string value.
     */
    @Override
    public String stringValue() {return toString();}

    /** Clone the item.
     * @return the object with the copy of this one.
     */
    @Override
    public XDValue cloneItem() {return new DefLocale(_value);}

    @Override
    public int hashCode() {return _value.hashCode();}

    @Override
    public boolean equals(final Object arg) {return arg instanceof XDValue ? equals(((XDValue) arg)) : false;}

    /** Check whether some other XDValue object is "equal to" this one.
     * @param arg other XDValue object to which is to be compared.
     * @return true if argument is same type as this XDValue and the value of the object is comparable and
     * equals to this one.
     */
    @Override
    public boolean equals(final XDValue arg) {
        if (isNull()) {
            return arg == null || arg.isNull();
        }
        if (arg == null || arg.isNull() || arg.getItemId() != XD_LOCALE) {
            return false;
        }
        return _value.equals(((DefLocale)arg)._value);
    }

    /** Check if the object is null.
     * @return true if the object is null otherwise returns false.
     */
    @Override
    public boolean isNull() {return _value == null;}
}