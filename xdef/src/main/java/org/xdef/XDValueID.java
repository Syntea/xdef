package org.xdef;

/** Constants of ID's of implemented types of values of script.
 * @author Vaclav Trojan
 */
public interface XDValueID {
	/** "void" value. */
	public static final short XD_VOID = 0; // must be 0!
	/** Int value (implemented as long - 64 bit). */
	public static final short XD_LONG = XD_VOID + 1; // 1
	/** Int value (implemented as int - 32 bit). */
	public static final short XD_INT = XD_LONG + 1; // 2
	/** Int value (implemented as short = 16 bit). */
	public static final short XD_SHORT = XD_INT + 1; // 3
	/** Int value (implemented as byte - 8 bit). */
	public static final short XD_BYTE = XD_SHORT + 1; // 4
	/** Int value (implemented as long). */
	public static final short XD_CHAR = XD_BYTE + 1; // 5
	/** Boolean value ID. */
	public static final short XD_BOOLEAN = XD_CHAR + 1; // 6
	/** Duration value ID. */
	public static final short XD_DURATION = XD_BOOLEAN + 1; // 7
	/** BNF grammar ID. */
	public static final short XD_BNFGRAMMAR = XD_DURATION + 1; // 8
	/** BNF rule ID. */
	public static final short XD_BNFRULE = XD_BNFGRAMMAR + 1; // 9
	/** BigDecimal value ID. */
	public static final short XD_DECIMAL = XD_BNFRULE + 1; // 10
	/** BigDecimal value ID. */
	public static final short XD_BIGINTEGER = XD_DECIMAL + 1; // 11
	/** Float value ID (implemented as 64 bit double). */
	public static final short XD_DOUBLE = XD_BIGINTEGER + 1; // 12
	/** Float value ID (implemented  as 32 bit float). */
	public static final short XD_FLOAT = XD_DOUBLE + 1; // 13
	/** Input stream value. */
	public static final short XD_INPUT = XD_FLOAT + 1; // 14
	/** Output stream value. */
	public static final short XD_OUTPUT = XD_INPUT + 1; // 14
	/** Byte array value. */
	public static final short XD_BYTES = XD_OUTPUT + 1; // 15
	/** String value ID. */
	public static final short XD_STRING = XD_BYTES + 1; // 16
	/** Date value ID. */
	public static final short XD_DATETIME = XD_STRING + 1; // 17
	/** Regular expression value ID. */
	public static final short XD_REGEX = XD_DATETIME + 1; // 18
	/** Regular expression result value ID. */
	public static final short XD_REGEXRESULT = XD_REGEX + 1; // 19
	/** Container value ID. */
	public static final short XD_CONTAINER = XD_REGEXRESULT + 1; // 20
	/** GPS position. */
	public static final short XD_GPSPOSITION = XD_CONTAINER + 1; // 21
	/** Price (amount with currency). */
	public static final short XD_PRICE = XD_GPSPOSITION + 1; // 22
	/** org.w3c.dom.Element value ID. */
	public static final short XD_ELEMENT = XD_PRICE + 1; // 23
	/** org.w3c.dom.Attr value ID */
	public static final short XD_ATTR = XD_ELEMENT + 1; // 24
	/** org.w3c.dom.Text node value ID */
	public static final short XD_TEXT = XD_ATTR + 1; // 25
	/** Exception object */
	public static final short XD_EXCEPTION = XD_TEXT + 1; // 26
	/** Report value ID */
	public static final short XD_REPORT = XD_EXCEPTION + 1; // 27
	/** value of XPATH */
	public static final short XD_XPATH = XD_REPORT + 1; // 28
	/** value of XQUERY */
	public static final short XD_XQUERY = XD_XPATH + 1; // 29
	/** Database service value (DB Connection etc). */
	public static final short XD_SERVICE = XD_XQUERY + 1; // 30
	/** Service statement. */
	public static final short XD_STATEMENT = XD_SERVICE + 1; // 31
	/** XDResultSet value. */
	public static final short XD_RESULTSET = XD_STATEMENT + 1; // 32
	/** Parser value. */
	public static final short XD_PARSER = XD_RESULTSET + 1; // 33
	/** Parser result value. */
	public static final short XD_PARSERESULT = XD_PARSER + 1; // 34
	/** Named value. */
	public static final short XD_NAMEDVALUE = XD_PARSERESULT + 1; // 35
	/** XML stream writer. */
	public static final short XD_XMLWRITER = XD_NAMEDVALUE + 1; // 36
	/** Item with Locale values. */
	public static final short XD_LOCALE = XD_XMLWRITER + 1; // 37
	/** Key of uniqueSet table. */
	public static final short XD_UNIQUESET_KEY = XD_LOCALE + 1; // 38
	/** Any value (may be null). */
	public static final short XD_ANY = XD_UNIQUESET_KEY + 1; // 39
	/** Object value. */
	public static final short XD_OBJECT = XD_ANY + 1; // 40
	/** Null type. */
	static final short XD_NULL = XD_OBJECT + 1; // 41
	/** XXElement value. */
	public static final short XX_ELEMENT = XD_NULL + 1; // 42
	/** XXText value. */
	public static final short XX_TEXT = XX_ELEMENT + 1; // 43
	/** XXAttr value. */
	public static final short XX_ATTR = XX_TEXT + 1; // 44
	/** XXData value (supertype for both XXATTR and XXTEXT). */
	public static final short XX_DATA = XX_ATTR + 1; // 45
	/** XXDocument value. */
	public static final short XX_DOCUMENT = XX_DATA + 1; // 46
	/** XXPI (Processing instruction) value. */
	public static final short XX_PI = XX_DOCUMENT + 1; // 47
	/** XXComment (comment) value. */
	public static final short XX_COMMENT = XX_PI + 1; // 48
	/** XXChoice value. */
	public static final short XX_CHOICE = XX_COMMENT + 1; // 49
	/** XXMixed value. */
	public static final short XX_MIXED = XX_CHOICE + 1; // 50
	/** XXsequence value. */
	public static final short XX_SEQUENCE = XX_MIXED + 1; // 51
	/** XModel value. */
	public static final short XM_MODEL = XX_SEQUENCE + 1; // 52
	/** Undefined type. */
	public static final short XD_UNDEF = XM_MODEL + 1; // 53

////////////////////////////////////////////////////////////////////////////////
// Internally used types
////////////////////////////////////////////////////////////////////////////////
	/** Attribute reference type. */
	static final short X_ATTR_REF = XD_UNDEF + 1; // 54
	/** Attribute reference type. */
	static final short X_PARSEITEM = X_ATTR_REF + 1; // 55
	/** Value of UNIQUESET. */
	static final short X_UNIQUESET_M = X_PARSEITEM + 1; // 56
	/** Value type: reference to attribute; used by compiler. */
	static final short X_UNIQUESET_KEY = X_UNIQUESET_M + 1; // 57
	/** Named value of UNIQUESET. */
	static final short X_UNIQUESET_NAMED = X_UNIQUESET_KEY+1; // 58
	/** Attribute ref, undefined type and methods which are not above a type. */
	static final short X_NOTYPE_VALUE = X_UNIQUESET_NAMED + 1; // 59
	/** Value of UNIQUESET. */
	static final short X_UNIQUESET = X_NOTYPE_VALUE + 1; // 60
}