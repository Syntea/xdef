package org.xdef.impl.code;

import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDContainer;
import static org.xdef.XDValueID.XD_ATTR;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.XDValueID.XD_BNFRULE;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTES;
import static org.xdef.XDValueID.XD_CHAR;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_CURRENCY;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_EMAIL;
import static org.xdef.XDValueID.XD_EXCEPTION;
import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueID.XD_INPUT;
import static org.xdef.XDValueID.XD_IPADDR;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_OUTPUT;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_PRICE;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueID.XD_REGEXRESULT;
import static org.xdef.XDValueID.XD_REPORT;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_SERVICE;
import static org.xdef.XDValueID.XD_STATEMENT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_TEXT;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.NULL;

/** The class DefNull implements the internal object with null value.
 * @author Vaclav Trojan
 */
public final class DefNull extends XDValueAbstract {
	/** NULL Value. */
	public static final XDValue NULL_VALUE = new DefNull();

	private final short _type;

	/** Creates a new instance of DefNull. */
	public DefNull() {_type = XD_NULL;}

	/** Creates a new instance of DefNull with type from argument.
	 * @param type type of null object.
	 */
	public DefNull(final short type) {_type = type;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return _type;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return NULL;}
	@Override
	/** Check if the object is <i>null</i>.
	 * @return <i>true</i> if the object is <i>null</i> otherwise returns
	 * <i>false</i>.
	 */
	public boolean isNull() {return true;}
	@Override
	/** Return DefBoolean object as boolean.
	 * @return the DefBoolean object as boolean.
	 */
	public boolean booleanValue() {return false;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return "";}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 */
	public String stringValue() {return null;}
	@Override
	/** Clone the item.
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return this;}
	@Override
	public int hashCode() {return 1;}
	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return true if argument is same type as this XDValue and the value
	 * of the object is comparable and equals to this one.
	 */
	public boolean equals(final XDValue arg) {return arg==null || arg.isNull();}
	@Override
	public boolean equals(final Object arg) {
		if (arg instanceof XDValue) {
			return ((XDValue) arg).isNull();
		}
		return arg == null;
	}
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
	public Node getXMLNode() {return null;}
	@Override
	public Element getElement() {return null;}
	@Override
	/** Get SDatetime value.
	 * @return SDatetime value of this object or return <i>null</i>.
	 */
	public SDatetime datetimeValue() {return null;}
	@Override
	/** Get SDuration value.
	 * @return SDuration value of this object or return <i>null</i>.
	 */
	public SDuration durationValue() {return null;}
	@Override
	/** Get XDContainer value.
	 * @return XDContext value of this object or return <i>null</i>.
	 */
	public XDContainer containerValue() {return null;}
	@Override
	/** Get XDService value.
	 * @return XDService value of this object or return <i>null</i>.
	 */
	public XDService serviceValue() {return null;}
	@Override
	/** Get XDStatement value.
	 * @return XDStatement value of this object or return <i>null</i>.
	 */
	public XDStatement statementValue() {return null;}
	@Override
	/** Get XDResultSet value.
	 * @return XDResultSet value of this object or return <i>null</i>.
	 */
	public XDResultSet resultSetValue() {return null;}
	@Override
	/** Get XDParseResult value.
	 * @return XDParseResult value of this object or return <i>null</i>.
	 */
	public XDParseResult parseResultValue() {return null;}
	public static XDValue genNullValue(final short type) {
		switch (type) {
			case XD_LONG: return new DefLong();
			case XD_DECIMAL: return new DefDecimal();
			case XD_BIGINTEGER: return new DefBigInteger();
			case XD_BOOLEAN: return new DefBoolean();
			case XD_CHAR: return new DefChar();
			case XD_DOUBLE: return new DefDouble();
			case XD_STRING: return new DefString();
			case XD_DATETIME: return new DefDate();
			case XD_DURATION: return new DefDuration();
			case XD_CONTAINER: return new DefContainer();
			case XD_GPSPOSITION: return new DefGPSPosition();
			case XD_PRICE: return new DefPrice();
			case XD_EMAIL: return new DefEmailAddr();
			case XD_IPADDR: return new DefIPAddr();
			case XD_CURRENCY: return new DefCurrency();
			case XD_REGEX: return new DefRegex();
			case XD_REGEXRESULT: return new DefRegexResult();
			case XD_BNFGRAMMAR: return new DefBNFGrammar();
			case XD_BNFRULE: return new DefBNFRule();
			case XD_INPUT: return new DefInStream();
			case XD_OUTPUT: return new DefOutStream();
			case XD_BYTES: return new DefBytes();
			case XD_ELEMENT: return new DefElement();
			case XD_ATTR: return new DefAttr();
			case XD_TEXT: return new DefText();
			case XD_EXCEPTION: return new DefException();
			case XD_REPORT: return new DefReport();
			case XD_PARSERESULT: return new DefParseResult();
			case XD_SERVICE: return new DefSQLService();
			case XD_STATEMENT: return new DefSQLStatement();
			case XD_RESULTSET: return new DefSQLResultSet();
			case X_UNIQUESET: // UNIQUESET value
			case X_UNIQUESET_M: // UNIQUESET value
				return new CodeUniqueset(new ParseItem[0], null, "");
			case X_PARSEITEM: return new ParseItem();
		}
		return new DefNull(type);
	}
}