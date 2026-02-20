package org.xdef.impl.code;

import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import java.math.BigInteger;
import static org.xdef.XDValueType.DOUBLE;

/** Implements the internal object with float values.
 * @author Vaclav Trojan
 */
public final class DefDouble extends XDValueAbstract {
    /** The value associated with this item. */
    private final double _value;
    private final boolean _isNull;

    /** Creates a new instance of DefFloat as null.*/
    public DefDouble() {_isNull = true; _value = Double.NaN;}

    /** Creates a new instance of DefFloat from double value.
     * @param value The value to be assigned to the object.
     */
    public DefDouble(final double value) {_value = value; _isNull = false;}

    /** Creates a new instance of DefFloat from source string.
     * @param source The string with float number to be assigned to the object.
     */
    public DefDouble(final String source) {
        _value = "NaN".equals(source) ? Double.NaN :
            "INF".equals(source) ? Double.POSITIVE_INFINITY :
            "-INF".equals(source) ? Double.NEGATIVE_INFINITY : Double.parseDouble(source);
         _isNull = false;
    }

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

    /** Get associated object.
     * @return the associated object or null.
     */
    @Override
    public Object getObject() {return isNull() ? null : _value;}

    /** Get type of value.
     * @return The id of item type.
     */
    @Override
    public short getItemId() {return XD_DOUBLE;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return DOUBLE;}

    /** Get value as String.
     * @return The string from value.
     */
    @Override
    public String toString() {return stringValue();}

    /** Get string value of this object.
     * @return string value of this object.
     */
    @Override
    public String stringValue() {
        return isNull() ? "" :
            _value == Double.NEGATIVE_INFINITY ? "-INF" :
            _value == Double.POSITIVE_INFINITY ? "INF" :
            _value == Double.NaN ? "NaN" : String.valueOf(_value);
    }

    /** Clone the item.
     * @return the object with the copy of this one.
     */
    @Override
    public XDValue cloneItem() {return new DefDouble(_value);}

    @Override
    public int hashCode() {long bits = Double.doubleToLongBits(_value); return (int)(bits ^ (bits >>> 32));}

    @Override
    public boolean equals(final Object arg) {return arg instanceof XDValue ? equals((XDValue) arg) : false;}

    /** Check whether some other XDValue object is "equal to" this one.
     * @param arg other XDValue object to which is to be compared.
     * @return <i>true</i> if argument is same type as this XDValue and the value of the object is comparable
     * and equals to this one.
     */
    @Override
    public boolean equals(final XDValue arg) {
        if (isNull()) {
            return arg == null || arg.isNull();
        }
        if (arg == null || arg.isNull()) {
            return false;
        }
        return Double.isNaN(_value) ? Double.isNaN(arg.doubleValue()) :  _value == arg.doubleValue();
    }

    /** Compares this XDValue object with the other XDValue object.
     * @param arg other XDValue object to which is to be compared.
     * @return If both objects are comparable then returns -1, 0, or a 1as this XDValue object is less than,
     * equal to, or greater than the specified object.
     * @throws SIllegalArgumentException if arguments are not comparable.
     */
    @Override
    public int compareTo(final XDValue arg) throws SIllegalArgumentException {
        return Double.compare(_value, arg.doubleValue());
    }

    @Override
    public byte byteValue() {return (byte) _value;}

    @Override
    public short shortValue() {return (short) _value;}

    @Override
    public int intValue() {return (int) _value;}

    @Override
    public long longValue() {return (long) _value;}

    @Override
    public float floatValue() {return (float) _value;}

    @Override
    public double doubleValue() {return _value;}

    @Override
    public BigDecimal decimalValue() {return isNull() ? null : new BigDecimal(_value);}

    @Override
    public BigInteger integerValue() {return isNull() ? null : decimalValue().toBigInteger();}

    /** Check if the object is null.
     * @return true if the object is null otherwise returns false.
     */
    @Override
    public boolean isNull() {return _isNull;}
}