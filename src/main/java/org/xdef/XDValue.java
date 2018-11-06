package org.xdef;

import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Provides the interface for usage of internal objects of x-script.
 * This interface provides general access methods to values of variables and
 * of parameters of methods in X-definitions.
 * @author Vaclav Trojan
 */
public interface XDValue extends Comparable<XDValue>, XDValueID {

	/** Get ID of the type of value (int, float, boolean, date, regex
	 * see enumeration org.xdef.XDValueID).
	 * @return item type.
	 */
	public short getItemId();

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType();

	/** Clone the item. In some cases it may return the object itself.
	 * @return the object with values of this one.
	 */
	public XDValue cloneItem();

	/** Get string value of this object.
	 * @return string value of this object or empty string or null.
	 */
	public String stringValue();

	/** Get character value of this object.
	 * @return character value of this object or '\0'.
	 */
	public char charValue();

	/** Get short value of this object.
	 * @return byte value of this object or return 0.
	 */
	public byte byteValue();

	/** Get short value of this object.
	 * @return short value of this object or return 0.
	 */
	public short shortValue();

	/** Get integer value of this object.
	 * @return integer value of this object or return 0.
	 */
	public int intValue();

	/** Get long value of this object.
	 * @return long value of this object or return 0.
	 */
	public long longValue();

	/** Get float value of this object.
	 * @return float value of this object or return 0.
	 */
	public float floatValue();

	/** Get double value of this object.
	 * @return double value of this object or return 0.
	 */
	public double doubleValue();

	/** Get BigDecimal value of this object.
	 * @return BigDecimal value of this object or return  <tt>null</tt>.
	 */
	public BigDecimal decimalValue();

	/** Get BigInteger value of this object.
	 * @return BigInteger value of this object or return  <tt>null</tt>.
	 */
	public BigInteger integerValue();

	/** Get boolean value of this object.
	 * @return boolean value of this object or <tt>false</tt>.
	 */
	public boolean booleanValue();

	/** Get SDatetime value.
	 * @return SDatetime value of this object or <tt>null</tt>.
	 */
	public SDatetime datetimeValue();

	/** Get SDuration value.
	 * @return SDuration value of this object or return <tt>null</tt>.
	 */
	public SDuration durationValue();

	/** Get bytes array representing value.
	 * @return array of bytes or null.
	 */
	public byte[] getBytes();

	/** Get XDContainer value.
	 * @return XDContainer value of this object or return <tt>null</tt>.
	 */
	public XDContainer contextValue();

	/** Get XDService value.
	 * @return XDService value of this object or return <tt>null</tt>.
	 */
	public XDService serviceValue();

	/** Get XDStatement value.
	 * @return XDStatement value of this object or return <tt>null</tt>.
	 */
	public XDStatement statementValue();

	/** Get XDResultSet value.
	 * @return XDResultSet value of this object or return <tt>null</tt>.
	 */
	public XDResultSet resultSetValue();

	/** Get XDParseResult value.
	 * @return XDParseResult value of this object or return <tt>null</tt>.
	 */
	public XDParseResult parseResultValue();

	/** Get associated XML node.
	 * @return the associated XML node or return <tt>null</tt>.
	 */
	public Node getXMLNode();

	/** Get associated XML element.
	 * @return the associated XML element or return <tt>null</tt>.
	 */
	public Element getElement();

	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject();

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(XDValue arg);

	@Override
	/** Compares this XDValue object with the other XDValue object.
	 * @param arg other XDValue object to which is to be compared.
	 * @return If both objects are comparable then returns -1, 0, or a 1
	 * as this XDValue object is less than, equal to, or greater than the
	 * specified object.
	 * @throws IllegalArgumentException If both objects are not comparable.
	 */
	public int compareTo(XDValue arg) throws IllegalArgumentException;

	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull();

	////////////////////////////////////////////////////////////////////////////

	/** Get code of operation. This method is used internally only in the
	 * code interpreter. Any user implementation of this interface
	 * MUST return 0!
	 * @return code of operation.
	 */
	public short getCode();

	/** This method is used internally only in the code interpreter.
	 * Get parameter of operation. Any user implementation of this interface
	 * MUST return 0!.
	 * @return parameter.
	 */
	public int getParam();

	/** This method is used internally only in the code interpreter.
	 * Set code of an operation (if this is not an operation it makes nothing).
	 * @param code the new code of operation.
	 */
	public void setCode(final short code);

	/** This method is used internally only in the code interpreter.
	 * Set result type of operation (if this is an operation it makes nothing).
	 * @param type id of type.
	 */
	public void setItemType(final short type);

	/** This method is used internally only in the code interpreter.
	 * Set parameter of operation (if this is an operation it makes nothing).
	 * @param param value of operation parameter.
	 */
	public void setParam(final int param);

}