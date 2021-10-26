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
	/** GPS position. */
	GPSPOSITION,
	/** Price (amount with currency). */
	PRICE,
	/** Email address. */
	EMAIL,
	/** Internet IP address. */
	INETADDR,
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
	XX_PI,
	/** XXComment value. */
	XX_COMMENT,
	/** XXChoice value. */
	XX_CHOICE,
	/** XXMixed value. */
	XX_MIXED,
	/** XXsequence value. */
	XX_SEQUENCE,
	/** XModel value. */
	XM_MODEL,
	/** Undefined. */
	XD_UNDEF,

////////////////////////////////////////////////////////////////////////////////
// Internally used types
////////////////////////////////////////////////////////////////////////////////
	/** Attribute reference. */
	X_ATTR_REF,
	/** Parse item. */
	X_PARSEITEM,
	/** Value of UNIQUESET. */
	X_UNIQUESET_M,
	/** Reference to attribute; used by compiler. */
	X_UNIQUESET_KEY,
	/** Named value of UNIQUESET. */
	X_UNIQUESET_NAMED,
	/** Value of UNIQUESET. */
	X_UNIQUESET,
	/** No type: ref, undefined and methods which are not of any above. */
	X_NOTYPE_VALUE;
}