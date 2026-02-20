package org.xdef.impl.code;

import java.net.URI;
import java.net.URISyntaxException;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.ANYURI;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;

/** Implements the internal object with URI value.
 * @author Vaclav Trojan
 */
public final class DefURI extends XDValueAbstract {

    /** The file value. */
    private final URI _value;

    /** Creates a new instance of DefURI as null.*/
    public DefURI() {_value = null;}

    /** Creates a new instance of DefURI
     * @param value The initial value of object.
     */
    public DefURI(final URI value) {_value = value;}

    /** Creates a new instance of DefURI
     * @param value The string with initial value of object ("true" or "false").
     */
    public DefURI(final String value) {
        try {
            _value = new URI(value);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

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
    public short getItemId() {return XD_ANYURI;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return ANYURI;}

    /** Get value as String.
     * @return The string from value.
     */
    @Override
    public String toString() {return stringValue();}

    /** Get string value of this object.
     * @return string value of this object.
     */
    @Override
    public String stringValue() {return _value==null ? "" : _value.toString();}

    /** Clone the item.
     * @return the object with the copy of this one.
     */
    @Override
    public XDValue cloneItem() {return new DefURI(_value);}

    @Override
    public int hashCode() {return _value == null ? 1 : _value.hashCode();}

    @Override
    public boolean equals(final Object arg) {return arg instanceof XDValue ?  equals((XDValue) arg) : false;}

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
        if (arg == null || arg.isNull()) {
            return false;
        }
        return _value.equals(arg.getObject());
    }

    /** Compares this object with the other DefURI object.
     * @param arg other DefURI object to which is to be compared.
     * @return returns 0 if this object is equal to the specified object.
     * @throws SIllegalArgumentException if arguments are not comparable.
     */
    @Override
    public int compareTo(final XDValue arg) throws SIllegalArgumentException {
        if (arg.getItemId() == XD_BOOLEAN) {
            if (equals(arg)) return 0;
        }
        throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
    }

    /** Check if the object is null.
     * @return true if the object is null otherwise return false.
     */
    @Override
    public boolean isNull() {return _value == null;}
}