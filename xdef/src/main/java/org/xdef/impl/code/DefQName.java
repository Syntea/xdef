package org.xdef.impl.code;

import javax.xml.namespace.QName;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDQName;
import static org.xdef.XDValueID.XD_QNAME;
import static org.xdef.XDValueType.QNAME;

/** Implements the internal object with QName value.
 * @author Vaclav Trojan
 */
public class DefQName extends XDValueAbstract implements XDQName {

    /** The QName value. */
    private final QName _value;

    /** Creates a new instance of DefQName as null.*/
    public DefQName() {_value = null;}

    /** Creates a new instance of DefQName
     * @param value The initial value of object.
     */
    public DefQName(final QName value) {_value = value;}

    /** Creates a new instance of DefQName (prefix and namespace are null)
     * @param name locla name .
     */
    public DefQName(final String name) {_value = new QName(name);}

    /** Creates a new instance of DefQName (prefix and namespace are null)
     * @param namespace String with namespace URI or null.
     * @param localName String with local name.
     * @param prefix String with prefix name or null.
     */
    public DefQName(final String namespace, final String localName, final String prefix) {
        if (localName == null || localName.isEmpty()) {
            throw new RuntimeException("Local name must be a name");
        }
        _value = new QName(namespace, localName, prefix);
    }

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDQName interface
////////////////////////////////////////////////////////////////////////////////

    /** Get QName value from this object.
     * @return the associated object, or return null.
     */
    @Override
    public javax.xml.namespace.QName getQName() {return _value;}

    /** Get local name from QName value.
     * @return local name from this QName value, or return null.
     */
    @Override
    public String getLocalName() {return _value != null ? _value.getLocalPart() : null;}

    /** Get prefix from QName value.
     * @return local name from this QName value, or return null.
     */
    @Override
    public String getPrefix() {return _value != null ? _value.getPrefix(): null;}

    /** Get namespace URI from QName value.
     * @return namespace URI from this QName value, or return null.
     */
    @Override
    public String getNamespace() {return _value != null ? _value.getNamespaceURI(): null;}

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
    public short getItemId() {return XD_QNAME;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return QNAME;}

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
    public XDValue cloneItem() {return new DefQName(_value);}

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
        if (arg.getItemId() == XD_QNAME) {
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
