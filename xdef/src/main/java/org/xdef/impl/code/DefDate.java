package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDDatetime;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.util.Calendar;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.DATETIME;

/** The class DefDate implements the internal object with date value.
 * @author Vaclav Trojan
 */
public final class DefDate extends XDValueAbstract implements XDDatetime {
    /** value of this object */
    private SDatetime _value;

    /** Creates a new instance of DefDate, date and time is set to zeroes. */
    public DefDate() {}

    /** Creates a new instance of DefDate. The value is set from parameter.
     * @param value The initial date and time value
     */
    public DefDate(final SDatetime value) {_value = value;}

    /** Creates a new instance of DefDate. The value is set from parameter.
     * @param value The initial date and time value
     */
    public DefDate(final String value) {_value = new SDatetime(value);}
    /** Creates a new instance of DefDate. The value is set from parameter.
     * @param value The initial date and time value
     */
    public DefDate(final Calendar value) {_value = new SDatetime(value);}

    /** Return the value of DefDate object.
     * @return the SDatetime value of this item.
     */
    @Override
    public SDatetime datetimeValue() {return _value;}

    /** Set datetime.
     * @param value SDatetime object.
     */
    @Override
    public void setDatetime(final SDatetime value) {_value = value;}

    /** Set datetime.
     * @param value SDatetime object.
     */
    @Override
    public void setCalendar(final Calendar value) {_value = new SDatetime(value);}

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
    public short getItemId() {return XD_DATETIME;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return DATETIME;}

    /** Check if the object is null.
     * @return true if the object is null otherwise return false.
     */
    @Override
    public boolean isNull() {return _value == null;}

    /** Get value as String.
     * @return ISO8601 string created from this object or "null".
     */
    @Override
    public String toString() {return _value == null ? "" : _value.toISO8601();}

    /** Get ISO8601 string value of this object.
     * @return ISO8601 string created from this object or "null".
     */
    @Override
    public String stringValue() {return toString();}

    @Override
    public XDValue cloneItem() {return _value==null? new DefDate() : new DefDate((SDatetime) _value.clone());}

    @Override
    public int hashCode() {return _value == null ? 0 : _value.hashCode();}

    @Override
    public boolean equals(final Object arg) {return (arg instanceof XDValue) ? equals((XDValue) arg) : false;}

    @Override
    public boolean equals(final XDValue arg) {
        if (isNull()) {
            return arg == null || arg.isNull();
        }
        if (arg == null || arg.isNull() || arg.getItemId() != XD_DATETIME) {
            return false;
        }
        return _value.equals(arg.datetimeValue());
    }

    @Override
    public int compareTo(final XDValue arg) throws SIllegalArgumentException {
        if (arg.getItemId() == XD_DATETIME) {
            try {
                return _value.compareTo(arg.datetimeValue());
            } catch (RuntimeException ex) {}
        }
        throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
    }
}