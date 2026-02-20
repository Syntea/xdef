package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_OBJECT;
import static org.xdef.XDValueID.X_PARSEITEM;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.XPARSEITEM;

/** Implements uniqueSet parse item.
 * @author Vaclav Trojan
 */
public final class ParseItem extends XDValueAbstract {
    /** Address of check method. */
    private final int _parseMethodAddr;
    /** Type of parsed object. */
    private final short _itemType;
    /** True if this key is optional, false if it is required. */
    private final boolean _optional;
    /** Key name. */
    final String _name;
    /** Reference name to declared type or null. */
    private final String _refName;
    /** Resulting value of parsing. */
    XDValue _itemValue;
    /** Index of key item. */
    private final int _keyIndex;

    /** Creates a new null instance of UniquesetParseItem. */
    ParseItem() {this(null, null, -1, -1, XD_OBJECT, false);}

    /** Creates a new instance of UniquesetParseItem (must be public because of XDReader).
     * @param name name of parse item or null;
     * @param chkAddr address of code of the method.
     * @param refName name of type;
     * @param keyIndex index of this key part
     * @param parsedType type of id.
     * @param optional if true this key value is required or if false it is optional
     */
    public ParseItem(final String name,
        final String refName,
        final int chkAddr,
        final int keyIndex,
        final short parsedType,
        final boolean optional) {
        _name = name;
        _refName = refName;
        _parseMethodAddr = chkAddr;
        _keyIndex = keyIndex;
        _itemType = parsedType;
        _optional = optional;
    }

    ////////////////////////////////////////////////////////////////////////
    // Implementation of XDUniquesetParseItem interface
    ////////////////////////////////////////////////////////////////////////

    /** Get address of parsing method.
     * @return the address of code.
     */
    public final int getParseMethodAddr() {return _parseMethodAddr;}

    /** Get parsed type.
     * @return the type id.
     */
    public final short getParsedType() {return _itemType;}

    /** Get parsed type.
     * @return the type id.
     */
    public final String getParseName() {return _name;}

    /** Get reference name to declared type.
     * @return reference name to declared type or null.
     */
    public final String getDeclaredTypeName() {return _refName;}

    /** Set parsed object (used in XDCodeProcessor).
     * @param val the value of parsed object.
     */
    public final void setParsedObject(XDValue val) {_itemValue = val;}

    /** Check if this item is optional or required.
     * @return true if this item is required.
     */
    public final boolean isOptional() {return _optional;}

    ////////////////////////////////////////////////////////////////////////
    // Implementation of XDValue interface
    ////////////////////////////////////////////////////////////////////////

    @Override
    public final short getItemId() {return X_PARSEITEM;}

    /** Get ID of the type of value
     * @return enumeration item of this type.
     */
    @Override
    public XDValueType getItemType() {return XPARSEITEM;}

    @Override
    public String toString() {
        return ("[" + _keyIndex + "]" + (_name == null ? "null"
            : ((!_name.isEmpty() ? ":" +_name : "") + "=" + _itemValue)))
            + "; method addr: " + _parseMethodAddr + "; refName: " + _refName;
    }

    @Override
    public final String stringValue() {return _itemValue.stringValue();}

    @Override
    public final XDValue cloneItem() {
        return new ParseItem(_name, _refName, _parseMethodAddr, _keyIndex, _itemType, _optional);
    }

    @Override
    public final boolean isNull() {return _parseMethodAddr == -1;}

    @Override
    public int hashCode() {return _name.hashCode();}

    @Override
    public boolean equals(final Object arg) {
         return arg != null && arg instanceof XDValue ? equals((XDValue) arg) : false;
    }

    @Override
    public final boolean equals(final XDValue arg) {
        return arg == null ? false
            : arg.getItemId() != X_PARSEITEM ? false
            : _name != null ? _name.equals(((ParseItem)arg)._name)
            : ((ParseItem) arg)._name == null;
    }

    ////////////////////////////////////////////////////////////////////////
    // Methods used in CodeUniqueset.
    ////////////////////////////////////////////////////////////////////////

    /** Get index of actual uniqueSet parse item.
     * @return parse item index.
     */
    public final int getKeyIndex() {return _keyIndex;}

    /** Get object of actual uniqueSet parse item.
     * @return value of parsed object or null.
     */
    final XDValue getParsedObject() {return _itemValue;}
}