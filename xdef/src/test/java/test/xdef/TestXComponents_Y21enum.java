package test.xdef;

import org.xdef.component.XCEnumeration;

public enum TestXComponents_Y21enum implements XCEnumeration {
    a, b, c;

    /** Get object associated with this item of enumeration.
     * @return object associated with this item of enumeration.
     */
    @Override
    public Object itemValue() {return name();}

    /** Get string which is used to create enumeration.
     * @return string which is used to create enumeration.
     */
    @Override
    public final String toString() {return name();}

    /** Create enumeration item from an object.
     * @param x object to be converted.
     * @return an item of this enumeration (or null).
     */
    public static final TestXComponents_Y21enum toEnum(final Object x) {
        for(TestXComponents_Y21enum y:values()) {
            if (y.itemValue().equals(x)) return y;
        }
        return null;
    }
}