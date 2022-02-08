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
	/** Byte array ID. */
	public static final short XD_BYTES = XD_FLOAT + 1; // 14
	/** String value ID. */
	public static final short XD_STRING = XD_BYTES + 1; // 15
	/** Date value ID. */
	public static final short XD_DATETIME = XD_STRING + 1; // 16
	/**  Uniform resource identifier ID (URI). */
	public static final short XD_ANYURI = XD_DATETIME + 1; // 17
	/** Container value ID. */
	public static final short XD_CONTAINER = XD_ANYURI + 1; // 18
	/** Currency ID. */
	public static final short XD_CURRENCY = XD_CONTAINER + 1; // 19
	/** GPS position ID. */
	public static final short XD_GPSPOSITION = XD_CURRENCY + 1; // 20
	/** Price ID (amount with currency). */
	public static final short XD_PRICE = XD_GPSPOSITION + 1; // 21
	/** Email address ID. */
	public static final short XD_EMAIL = XD_PRICE + 1; // 22
	/** Internet IP address. */
	public static final short XD_IPADDR = XD_EMAIL + 1; // 23
	/** Telephone number. */
	public static final short XD_TELEPHONE = XD_IPADDR + 1; // 24
	/** Regular expression value ID. */
	public static final short XD_REGEX = XD_TELEPHONE + 1; // 25
	/** Regular expression result value ID. */
	public static final short XD_REGEXRESULT = XD_REGEX + 1; // 26
	/** Input stream value ID. */
	public static final short XD_INPUT = XD_REGEXRESULT + 1; // 27
	/** Output stream value ID. */
	public static final short XD_OUTPUT = XD_INPUT + 1; // 28
	/** org.w3c.dom.Element value ID. */
	public static final short XD_ELEMENT = XD_OUTPUT + 1; // 29
	/** org.w3c.dom.Attr value ID */
	public static final short XD_ATTR = XD_ELEMENT + 1; // 30
	/** org.w3c.dom.Text node value ID */
	public static final short XD_TEXT = XD_ATTR + 1; // 31
	/** Exception object */
	public static final short XD_EXCEPTION = XD_TEXT + 1; // 32
	/** Report value ID */
	public static final short XD_REPORT = XD_EXCEPTION + 1; // 33
	/** value of XPATH */
	public static final short XD_XPATH = XD_REPORT + 1; // 34
	/** value of XQUERY ID*/
	public static final short XD_XQUERY = XD_XPATH + 1; // 35
	/** Database service value (DB Connection etc) ID. */
	public static final short XD_SERVICE = XD_XQUERY + 1; // 36
	/** Service statement ID. */
	public static final short XD_STATEMENT = XD_SERVICE + 1; // 37
	/** XDResultSet value ID. */
	public static final short XD_RESULTSET = XD_STATEMENT + 1; // 38
	/** Parser value ID. */
	public static final short XD_PARSER = XD_RESULTSET + 1; // 39
	/** Parser result value ID. */
	public static final short XD_PARSERESULT = XD_PARSER + 1; // 40
	/** Named value ID. */
	public static final short XD_NAMEDVALUE = XD_PARSERESULT + 1; // 41
	/** XML stream writer ID. */
	public static final short XD_XMLWRITER = XD_NAMEDVALUE + 1; // 42
	/** Item with Locale values ID. */
	public static final short XD_LOCALE = XD_XMLWRITER + 1; // 43
	/** Key of uniqueSet table ID. */
	public static final short XD_UNIQUESET_KEY = XD_LOCALE + 1; // 44
	/** Any value ID (may be null). */
	public static final short XD_ANY = XD_UNIQUESET_KEY + 1; // 45
	/** Object value ID. */
	public static final short XD_OBJECT = XD_ANY + 1; // 46
	/** Null value ID. */
	static final short XD_NULL = XD_OBJECT + 1; // 47
	/** XXElement value ID. */
	public static final short XX_ELEMENT = XD_NULL + 1; // 48
	/** XXText value ID. */
	public static final short XX_TEXT = XX_ELEMENT + 1; // 49
	/** XXAttr value ID. */
	public static final short XX_ATTR = XX_TEXT + 1; // 50
	/** XXData value ID (super type for both XXATTR and XXTEXT). */
	public static final short XX_DATA = XX_ATTR + 1; // 51
	/** XXDocument value ID. */
	public static final short XX_DOCUMENT = XX_DATA + 1; // 52
	/** XXPI (Processing instruction) value ID. */
	public static final short XX_PI = XX_DOCUMENT + 1; // 53
	/** XXComment (comment) value ID. */
	public static final short XX_COMMENT = XX_PI + 1; // 54
	/** XXChoice value ID. */
	public static final short XX_CHOICE = XX_COMMENT + 1; // 55
	/** XXMixed value ID. */
	public static final short XX_MIXED = XX_CHOICE + 1; // 56
	/** XXsequence value ID. */
	public static final short XX_SEQUENCE = XX_MIXED + 1; // 57
	/** XModel value ID. */
	public static final short XM_MODEL = XX_SEQUENCE + 1; // 58
	/** Undefined value ID. */
	public static final short XD_UNDEF = XM_MODEL + 1; // 59

////////////////////////////////////////////////////////////////////////////////
// Internally used types
////////////////////////////////////////////////////////////////////////////////
	/** Attribute reference ID. */
	static final short X_ATTR_REF = XD_UNDEF + 1; // 60
	/** Parser item ID. */
	static final short X_PARSEITEM = X_ATTR_REF + 1; // 61
	/** Value of UNIQUESET ID. */
	static final short X_UNIQUESET_M = X_PARSEITEM + 1; // 62
	/** Reference to attribute ID; used by compiler. */
	static final short X_UNIQUESET_KEY = X_UNIQUESET_M + 1; // 63
	/** Named value of UNIQUESET ID. */
	static final short X_UNIQUESET_NAMED = X_UNIQUESET_KEY+1; // 64
	/** Value of UNIQUESET ID. */
	static final short X_UNIQUESET = X_UNIQUESET_NAMED  + 1; // 65
	/** No type ID: ref, undefined and methods which are not of any above. */
	static final short X_NOTYPE_VALUE = X_UNIQUESET + 1; // 66
}