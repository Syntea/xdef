package org.xdef;

/** Values of types of values in the Script of X-definition.
 * @author Vaclav Trojan
 */
public enum XDValueType {
	/** "void" value. */
	VOID,
	/** Int value (implemented as long - 64 bit). */
	LONG,
	/** Int value (implemented as int - 32 bit). */
	INT,
	/** Int value (implemented as short - 16 bit). */
	SHORT,
	/** Int value (implemented as int - 8 bit). */
	BYTE,
	/** Character value. */
	CHAR,
	/** Boolean value. */
	BOOLEAN,
	/** Duration value. */
	DURATION,
	/** BNF grammar. */
	BNFGRAMMAR,
	/** BNF rule. */
	BNFRULE,
	/** BigDecimal value. */
	DECIMAL,
	/** BigInteger value. */
	BIGINTEGER,
	/** Float value (implemented as double - 64 bit ). */
	DOUBLE,
	/** Float value (implemented  as float - 32 bit ). */
	FLOAT,
	/** Byte array value. */
	BYTES,
	/** String value. */
	STRING,
	/** Date value. */
	DATETIME,
	/**  Uniform resource identifier (URI). */
	ANYURI,
	/** Container value. */
	CONTAINER,
	/** Currency value. */
	CURRENCY,
	/** GPS position. */
	GPSPOSITION,
	/** Price (amount with currency). */
	PRICE,
	/** Email address. */
	EMAIL,
	/** Internet IP address. */
	IPADDR,
	/** Telephone number. */
	TELEPHONE,
	/** Regular expression value. */
	REGEX,
	/** Regular expression result value. */
	REGEXRESULT,
	/** Input stream value. */
	INPUT,
	/** Output stream value. */
	OUTPUT,
	/** org.w3c.dom.Element value. */
	ELEMENT,
	/** org.w3c.dom.Attr value ID */
	ATTR,
	/** org.w3c.dom.Text node value ID */
	TEXT,
	/** Exception object */
	EXCEPTION,
	/** Report value ID */
	REPORT,
	/** XPATH expression (compiled object) */
	XPATH,
	/** XQUERY expression (compiled object) */
	XQUERY,
	/** Database service value (DB Connection etc). */
	SERVICE,
	/** Service statement. */
	STATEMENT,
	/** XDResultSet value. */
	RESULTSET,
	/** Parser value. */
	PARSER,
	/** Parser result value. */
	PARSERESULT,
	/** Named value. */
	NAMEDVALUE,
	/** XML stream writer. */
	XMLWRITER,
	/** Item with Locale values. */
	LOCALE,
	/** Key of uniqueSet table. */
	UNIQUESET_KEY,
	/** Any value (may be null). */
	ANY,
	/** Object value. */
	OBJECT,
	/** Null value. */
	NULL,
	/** Any number (integer, double, decimal). */
	NUMBER,

////////////////////////////////////////////////////////////////////////////////
// XX Types (implemented XM types)
////////////////////////////////////////////////////////////////////////////////
	/** XXElement value. */
	XXELEMENT,
	/** XXText value. */
	XXTEXT,
	/** XXAttr value. */
	XXATTR,
	/** XXData value (super type for both XXATTR and XXTEXT). */
	XXDATA,
	/** XXDocument value. */
	XXDOCUMENT,
	/** XXPI (Processing instruction) value. */
	XXPI,
	/** XXComment value. */
	XXCOMMENT,
	/** XXChoice value. */
	XXCHOICE,
	/** XXMixed value. */
	XXMIXED,
	/** XXsequence value. */
	XXSEQUENCE,
	/** XModel value. */
	XMMODEL,
	/** Undefined. */
	XDUNDEF,

////////////////////////////////////////////////////////////////////////////////
// Internally used types
////////////////////////////////////////////////////////////////////////////////
	/** Attribute reference. */
	XATTR_REF,
	/** Parse item. */
	XPARSEITEM,
	/** Value of UNIQUESET. */
	XUNIQUESET_M,
	/** Reference to attribute; used by compiler. */
	XUNIQUESET_KEY,
	/** Named value of UNIQUESET. */
	XUNIQUESET_NAMED,
	/** Value of UNIQUESET. */
	XUNIQUESET,
	/** No type: ref, undefined and methods which are not of any above. */
	XNOTYPE_VALUE;
}