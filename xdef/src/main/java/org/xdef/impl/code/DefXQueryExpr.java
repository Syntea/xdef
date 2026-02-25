package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.proc.XXNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.xml.KXqueryExpr;
import org.xdef.XDContainer;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDXQueryExpr;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import org.xdef.XDValueType;
import java.math.BigInteger;
import static org.xdef.XDValueType.XQUERY;
import static org.xdef.impl.code.CodeTable.LD_CONST;

/** Compiled XQuery expression.
 * @author Vaclav Trojan
 */
public class DefXQueryExpr extends KXqueryExpr implements XDXQueryExpr {
    /** Implementation of XQueryImpl. */
    private final static XQueryImpl XQI;

    static {
        XQueryImpl x;
        if (KXqueryExpr.isXQueryImplementation()) {
            try {
                Class<?> cls = Class.forName("org.xdef.impl.saxon.XQuerySaxonImpl");
                Constructor<?> c = cls.getConstructor();
                x = (XQueryImpl) c.newInstance();
            } catch (Error | ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException | SecurityException
                | InvocationTargetException ex) {
                x = null;
            }
        } else {
            x = null;
        }
        XQI = x;
    }

    /** Creates a new empty instance of DefXqueryExpr. */
    public DefXQueryExpr() {super(".");}

    /** Creates a new instance of DefXqueryExpr
     * @param source String with XPath expression.
     */
    public DefXQueryExpr(final String source) {super(source);}

    /** Get value of item as String representation of value.
     * @return The string representation of value of the object.
     */
    public String sourceValue() {return getSourceExpr();}

    /** Return value of XQuery object.
     * @return the value of this KXqueryExpr object.
     */
    public KXqueryExpr getXQuery() {return this;}

    /** Execute XQuery expression and return result.
     * @param xNode node model or null.
     * @return result of execution of this object.
     */
    @Override
    public XDContainer exec(final Node node, final XXNode xNode) {
        return XQI == null ? null : XQI.exec(this, node, xNode);
    }

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

    @Override
    public Object getObject() {return this;}

    @Override
    public byte[] getBytes() {return null;}

    @Override
    public short getItemId() {return XD_XQUERY;}

    @Override
    public XDValueType getItemType() {return XQUERY;}

    @Override
    public String toString() {return getSourceExpr();}

    @Override
    public String stringValue() {return getSourceExpr();}

    @Override
    public XDValue cloneItem() {return this;}

    @Override
    public short getCode() {return LD_CONST;}

    @Override
    public void setCode(final short code) {}

    @Override
    public void setItemType(short resultType) {}

    @Override
    public int getParam() {return 0;}

    @Override
    public void setParam(final int param) {}

    @Override
    public int hashCode() {
        return getSourceExpr() == null ? 0 : getSourceExpr().hashCode();
    }

    @Override
    public boolean equals(final Object arg) {
        return arg instanceof DefXQueryExpr ? equals((DefXQueryExpr)arg) :false;
    }

    /** Check whether some other XDValue object is "equal to" this one.
     * @param arg other XDValue object to which is to be compared.
     * @return always false.
     */
    @Override
    public boolean equals(final XDValue arg) {return this == arg;}

    /** Compares this XDValue object with the other XDValue object.
     * @param arg other XDValue object to which is to be compared.
     * @return 0 or throws SIllegalArgumentException.
     * @throws SIllegalArgumentException if arguments are not comparable.
     */
    @Override
    public int compareTo(final XDValue arg) {
        if (arg  == this) return 0;
        throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
    }

    @Override
    public char charValue() {return 0;}

    @Override
    public byte byteValue() {return 0;}

    @Override
    public short shortValue() {return 0;}

    @Override
    public int intValue() {return 0;}

    @Override
    public long longValue() {return 0;}

    @Override
    public float floatValue() {return 0;}

    @Override
    public double doubleValue() {return 0;}

    @Override
    public BigDecimal decimalValue() {return null;}

    @Override
    public BigInteger integerValue() {return null;}

    @Override
    public boolean booleanValue() {return false;}

    @Override
    public SDatetime datetimeValue() {return null;}

    @Override
    public SDuration durationValue() {return null;}

    @Override
    public XDContainer containerValue() {return null;}

    @Override
    public XDService serviceValue(){return null;}

    @Override
    public XDStatement statementValue(){return null;}

    @Override
    public XDResultSet resultSetValue() {return null;}

    @Override
    public XDParseResult parseResultValue() {return null;}

    @Override
    public Node getXMLNode() {return null;}

    @Override
    public Element getElement() {return null;}

    @Override
    public boolean isNull() { return false;}
}