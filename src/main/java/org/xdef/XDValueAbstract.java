package org.xdef;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SUnsupportedOperationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.impl.code.CodeTable;

/** Abstract class for implementing of XDValues.
 * @author Vaclav Trojan
 */
public abstract class XDValueAbstract implements XDValue {

////////////////////////////////////////////////////////////////////////////////
// Methods of XDValue.
////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(final XDValue arg) {return arg == this;}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg == this) return 0;
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public String stringValue() {return null;}
	@Override
	public char charValue() {return (char) intValue();}
	@Override
	public byte byteValue() {return (byte) longValue();}
	@Override
	public short shortValue() {return  (short) longValue();}
	@Override
	public int intValue() {return  (int) longValue();}
	@Override
	public long longValue() {return 0;}
	@Override
	public float floatValue() {return (float) doubleValue();}
	@Override
	public double doubleValue() {return 0.0d;}
	@Override
	public BigDecimal decimalValue() {return null;}
	@Override
	public BigInteger integerValue() {return null;}
	@Override
	public boolean booleanValue() {return false;}
	@Override
	public Node getXMLNode() {return null;}
	@Override
	public Element getElement() {return null;}
	@Override
	public SDatetime datetimeValue() {return null;}
	@Override
	public SDuration durationValue() {return null;}
	@Override
	public byte[] getBytes() {return null;}
	@Override
	public XDContainer contextValue() {return null;}
	@Override
	public XDService serviceValue() {return null;}
	@Override
	public XDStatement statementValue() {return null;}
	@Override
	public XDResultSet resultSetValue() {return null;}
	@Override
	public XDParseResult parseResultValue() {return null;}
	@Override
	public boolean isNull() {return false;}
	@Override
	public Object getObject() {return null;}

////////////////////////////////////////////////////////////////////////////////
// Methods used in XD processor for internal code - DO NOT IMPLEMENT!
////////////////////////////////////////////////////////////////////////////////

	@Override
	public short getCode() {return CodeTable.LD_CONST;}
	@Override
	public int getParam() {return 0;}
	@Override
	public XDValue cloneItem() {return this;}
	@Override
	public String toString() {return stringValue();}
	@Override
	public void setItemType(final short type) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setItemType(short)", getClass().getName());
	}
	@Override
	public void setCode(final short code) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setCode(short)", getClass().getName());
	}
	@Override
	public void setParam(final int param) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setParam(int)", getClass().getName());
	}
}