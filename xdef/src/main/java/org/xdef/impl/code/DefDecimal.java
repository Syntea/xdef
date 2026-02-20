package org.xdef.impl.code;

import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import java.math.BigInteger;
import static org.xdef.XDValueType.DECIMAL;

/** Implements the internal object with BigDecimal values.
 * @author Vaclav Trojan
 */
public final class DefDecimal extends XDValueAbstract {
    /** The value associated with this item. */
    private final BigDecimal _value;

    /** Creates a new instance of DefInteger.*/
    public DefDecimal() {_value = null;}

    /** Creates a new instance of DefInteger
     * @param value The integer value.
     */
    public DefDecimal(final BigDecimal value) {_value = value;}

    /** Creates a new instance of DefInteger
     * @param value The string with integer value.
     */
    public DefDecimal(final String value) {_value = new BigDecimal(value);}

    /** Creates a new instance of DefInteger
     * @param value The integer value.
     */
    public DefDecimal(final long value) {_value = new BigDecimal(value);}

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
    public short getItemId() {return XD_DECIMAL;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return DECIMAL;}

    /** Check if the object is null.
     * @return true if the object is null otherwise returns false.
     */
    @Override
    public boolean isNull() {return _value == null;}

    /** Get value as String.
     * @return The string from value.
     */
    @Override
    public String toString() {return _value==null ?"": String.valueOf(_value);}

    /** Get string value of this object.
     * @return string value of this object.
     */
    @Override
    public String stringValue() {return toString();}

    /** Clone the item.
     * @return the object with the copy of this one.
     */
    @Override
    public XDValue cloneItem() {
        return _value == null ? new DefDecimal() : new DefDecimal(new BigDecimal(_value.toString()));
    }

    @Override
    public int hashCode() {return _value == null ? 0 : _value.hashCode();}

    @Override
    public boolean equals(final Object x) {return x instanceof XDValue ? equals((XDValue) x) : false;}

    /** Check whether some other XDValue object is "equal to" this one.
     * @param x other XDValue object to which is to be compared.
     * @return true if argument is same type as this XDValue and the value of the object is comparable
     * and equals to this one.
     */
    @Override
    public boolean equals(final XDValue x) {
        return isNull() ? x == null || x.isNull()
            : x == null || x.isNull() ? false : _value.equals(x.decimalValue());
    }

    /** Compares this XDValue object with the other XDValue object.
     * @param x other XDValue object to which is to be compared.
     * @return If both objects are comparable then returns -1, 0, or a 1 as this XDValue object is less than,
     * equal to, or greater than the specified object.
     * @throws SIllegalArgumentException if objects are not comparable.
     */
    @Override
    public int compareTo(final XDValue x) throws SIllegalArgumentException {
        return _value.compareTo(x.decimalValue());
    }

    @Override
    public byte byteValue() {return _value==null ? 0 : _value.byteValue();}

    @Override
    public short shortValue() {return  _value==null ? 0 : _value.shortValue();}

    @Override
    public int intValue() { return  _value==null ? 0 : _value.intValue();}

    @Override
    public long longValue() {return  _value==null ? 0 : _value.longValue();}

    @Override
    public float floatValue() {return  _value==null ? 0 : _value.floatValue();}

    @Override
    public double doubleValue() {return  _value==null? 0 :_value.doubleValue();}

    @Override
    public BigDecimal decimalValue() {return _value;}

    @Override
    public BigInteger integerValue() {return  _value==null ? null : _value.toBigInteger();}

    @Override
    public boolean booleanValue() {return isNull() ? false : !_value.equals(BigDecimal.ZERO);}
}