package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.xml.KXpathExpr;
import org.xdef.XDValue;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import java.math.BigDecimal;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.SRuntimeException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.NodeList;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.XDValueType;
import java.math.BigInteger;

/** Contains compiled XPath expression.
 * @author Vaclav Trojan
 */
public final class DefXPathExpr extends KXpathExpr implements XDValue {

	/** Creates a new empty instance of DefXpathExpr. */
	DefXPathExpr() {super("*", (NamespaceContext) null);}

	/** Creates a new instance of DefXpathExpr
	 * @param source the String with XPath expression.
	 * @param nc NameSpace context or <tt>null</tt>.
	 * @param fr Function resolver or <tt>null</tt>.
	 * @param vr Variable resolver or <tt>null</tt>.
	 */
	public DefXPathExpr(final String source,
		NamespaceContext nc,
		final XPathFunctionResolver fr,
		final XPathVariableResolver vr) {
		super(source, nc, fr, vr);
	}

	/** Get value of item as String representation of value.
	 * @return The string representation of value of the object.
	 */
	public String sourceValue() {return toString();}

	/** Return value of XQuery object.
	 * @return the value of this KXqueryExpr object.
	 */
	public KXpathExpr getXPath() {return this;}

	/** Execute XPath expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @return result of XPath expression.
	 */
	public XDContainer exec(final Node node) {
		try {
			Object o = evaluate(node, (QName) null);
			if (o == null) {
				return (XDContainer) DefNull.genNullValue(XD_CONTAINER);
			}
			if (o instanceof NodeList) {
				NodeList nl = (NodeList) o;
				int size = nl.getLength();
				return size == 0 ? new DefContainer() :
					size==1 ? new DefContainer(nl.item(0)):new DefContainer(nl);
			}
			if (o instanceof Number) {
				Number n = (Number) o;
				if (o instanceof Long || o instanceof Integer)
					return new DefContainer(n.longValue());
				double d = n.doubleValue();
				if (Math.floor(d) == n.longValue())
					return new DefContainer(n.longValue());
				return new DefContainer(n.floatValue());
			}
			if (o instanceof Node || o instanceof String)
				return new DefContainer(o);
			return new DefContainer((Boolean) o);
		} catch (Exception ex) {
			try {
				return new DefContainer(
					(String) evaluate(node, XPathConstants.STRING));
			} catch (Exception ex1) {
				throw new SRuntimeException(ex1.toString());
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_XPATH;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.XPATH;}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return toString();}

	@Override
	/** Clone the item - used internally in XD processor.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return this;}

	@Override
	public Object getObject() {return this;}

	@Override
	/** Get bytes array representing value.
	 * @return array of bytes or null.
	 */
	public byte[] getBytes() {return null;}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return always <tt>false</tt>.
	 */
	public boolean equals(final XDValue arg) {return arg == this;}

	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0 or throws SIllegalArgumentException.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	public int compareTo(final XDValue arg) {
		if (arg  == this) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return false;}

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
	public XDContainer contextValue() {return null;}
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
	public short getCode() {return CodeTable.COMPILE_XPATH;}
	@Override
	public void setCode(final short code) {}
	@Override
	public void setItemType(short resultType) {}
	@Override
	public int getParam() {return 0;}
	@Override
	public void setParam(final int param) {}
}