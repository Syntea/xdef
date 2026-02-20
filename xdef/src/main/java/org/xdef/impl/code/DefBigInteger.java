package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import java.math.BigInteger;
import static org.xdef.XDValueType.BIGINTEGER;

/** The class DefLong implements the internal object with integer values.
 * @author Vaclav Trojan
 */
public final class DefBigInteger extends XDValueAbstract {
    /** The value associated with this item. */
    private final BigInteger _value;

    /** Creates a new instance of DefInteger.*/
    public DefBigInteger() {_value = null;}

    /** Creates a new instance of DefBigInteger
     * @param value The integer value.
     */
    public DefBigInteger(final BigInteger value) {_value = value;}

    /** Creates a new instance of DefBigInteger
     * @param value The string with integer value.
     */
    public DefBigInteger(final String value) {_value = new BigInteger(value);}

    /** Creates a new instance ofDefBigInteger
     * @param value The long integer value.
     */
    public DefBigInteger(final long value) {this(String.valueOf(value));}

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
    public short getItemId() {return XD_BIGINTEGER;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return BIGINTEGER;}

    /** Check if the object is <i>null</i>.
     * @return <i>true</i> if the object is <i>null</i> otherwise returns
     * <i>false</i>.
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
     * string value.
     */
    @Override
    public String stringValue() {return toString();}

    /** Clone the item.
     * @return the object with the copy of this one.
     */
    @Override
    public XDValue cloneItem() {
        return _value == null ? new DefBigInteger() : new DefBigInteger(_value);
    }

    @Override
    public int hashCode() {return _value == null ? 0 : _value.hashCode();}

    @Override
    public boolean equals(final Object x) {return x instanceof XDValue ? equals((XDValue) x) : false;}

    /** Check whether some other XDValue object is "equal to" this one.
     * @param x other XDValue object to which is to be compared.
     * @return true if argument is same type as this XDValue and the value
     * of the object is comparable and equals to this one.
     */
    @Override
    public boolean equals(final XDValue x) {
        return isNull() ? x == null || x.isNull()
            : x == null || x.isNull() ? false
            : _value.equals(x.integerValue());
    }

    /** Compares this XDValue object with the other XDValue object.
     * @param x other XDValue object to which is to be compared.
     * @return If both objects are comparable then returns -1, 0, or a 1
     * as this XDValue object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(final XDValue x) {return _value.compareTo(x.integerValue());}

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
    public BigDecimal decimalValue() {return new BigDecimal(_value);}

    @Override
    public BigInteger integerValue() {return _value;}

    @Override
    public boolean booleanValue() {return isNull() ? false : !_value.equals(BigInteger.ZERO);}
}