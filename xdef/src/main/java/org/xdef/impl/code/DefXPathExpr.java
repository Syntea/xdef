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
import org.xdef.XDValueType;
import java.math.BigInteger;
import static org.xdef.XDValueType.XPATH;
import org.xdef.impl.XCodeProcessor;
import static org.xdef.impl.code.CodeTable.COMPILE_XPATH;

/** Implementation of compiled XPath expression.
 * @author Vaclav Trojan
 */
public final class DefXPathExpr extends KXpathExpr implements XDValue {
	public XPathFunctionResolver _fr;
	public XPathVariableResolver _vr;
	public NamespaceContext _nc;

	/** Creates a new instance of DefXpathExpr
	 * @param source the String with XPath expression.
	 * @param nc NameSpace context or null.
	 * @param fr Function resolver or null.
	 * @param vr Variable resolver or null.
	 */
	public DefXPathExpr(final String source,
		final NamespaceContext nc,
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
	 * @param node node or null.
	 * @return result of XPath expression.
	 */
	public XDContainer exec(final Node node) {return exec(node, false);}

	/** Execute XPath expression and return result (it can repeat execution).
	 * @param node node or null.
	 * @param invoked if false it can be invoke again (if exception thrown)
	 * @return result of XPath expression.
	 */
	private XDContainer exec(final Node node, final boolean invoked) {
		try {
			Object o = evaluate(node, (QName) null);
			if (o == null) {
				return (XDContainer) DefNull.genNullValue(XD_CONTAINER);
			}
			if (o instanceof NodeList) {
				NodeList nl = (NodeList) o;
				int size = nl.getLength();
				return size == 0
					? new DefContainer() : size == 1 ? new DefContainer(nl.item(0)): new DefContainer(nl);
			}
			DefContainer result;
			if (o instanceof Number) {
				Number n = (Number) o;
				if (o instanceof Long || o instanceof Integer) {
					result = new DefContainer(n);
				} else {
					double d = n.doubleValue();
					result = (Math.floor(d) == n.longValue())
						? new DefContainer(n.longValue()) : new DefContainer(d);
				}
			} else {
				result = (o instanceof Node || o instanceof String)
					? new DefContainer(o) : new DefContainer((Boolean) o);
			}
			return result;
		} catch (Exception ex) {
			try {
				return new DefContainer((String) evaluate(node, XPathConstants.STRING));
			} catch (Exception ex1) {
				// Very nasted trick!!! (maybe bind???)
				if (!invoked) {
					XPathVariableResolver xpvr = getVariableResolver();
					XCodeProcessor.XDVariableResolver x;
					if (xpvr != null && (xpvr instanceof XCodeProcessor.XDVariableResolver)
						&& (x = (XCodeProcessor.XDVariableResolver) xpvr).XPATH2) {
						x.convertToString = true;
						return exec(node, true);// try to execute it again
					}
				}
				throw new SRuntimeException(ex1.toString());
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_XPATH;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return XPATH;}

	/** Get string value of this object.
	 * @return string value of this object.
	 */
	@Override
	public String stringValue() {return toString();}

	/** Clone the item - used internally in XD processor.
	 * @return the object with the copy of this one.
	 */
	@Override
	public XDValue cloneItem() {return this;}

	@Override
	public Object getObject() {return this;}

	/** Get bytes array representing value.
	 * @return array of bytes or null.
	 */
	@Override
	public byte[] getBytes() {return null;}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return always false.
	 */
	@Override
	public boolean equals(final XDValue arg) {return arg == this;}

	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return 0 or throws SIllegalArgumentException.
	 * @throws SIllegalArgumentException if arguments are not comparable.
	 */
	@Override
	public int compareTo(final XDValue arg) {
		if (arg  == this) {
			return 0;
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}

	/** Check if the object is null.
	 * @return true if the object is null otherwise return false.
	 */
	@Override
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
	public short getCode() {return COMPILE_XPATH;}

	@Override
	public void setCode(final short code) {}

	@Override
	public void setItemType(short resultType) {}

	@Override
	public int getParam() {return 0;}

	@Override
	public void setParam(final int param) {}
}