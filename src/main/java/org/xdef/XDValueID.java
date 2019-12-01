package org.xdef;

/** Constants of ID's of implemented types of values of script.
 * @author Vaclav Trojan
 */
public interface XDValueID {
	/** "void" value. */
	public static final short XD_VOID = 0; // must be 0!
	/** Int value (implemented as long). */
	public static final short XD_INT = XD_VOID + 1; // 1
	/** Boolean value ID. */
	public static final short XD_BOOLEAN = XD_INT + 1; // 2
	/** Duration value ID. */
	public static final short XD_DURATION = XD_BOOLEAN + 1; // 3
	/** BNF grammar ID. */
	public static final short XD_BNFGRAMMAR = XD_DURATION + 1; // 4
	/** BNF rule ID. */
	public static final short XD_BNFRULE = XD_BNFGRAMMAR + 1; // 5
	/** BigDecimal value ID. */
	public static final short XD_DECIMAL = XD_BNFRULE + 1; // 6
	/** BigDecimal value ID. */
	public static final short XD_BIGINTEGER = XD_DECIMAL + 1; // 7
	/** Float value ID (implemented as double). */
	public static final short XD_FLOAT = XD_BIGINTEGER + 1; // 8
	/** Input stream value. */
	public static final short XD_INPUT = XD_FLOAT + 1; // 9
	/** Output stream value. */
	public static final short XD_OUTPUT = XD_INPUT + 1; // 10
	/** Byte array value. */
	public static final short XD_BYTES = XD_OUTPUT + 1; // 11
	/** String value ID. */
	public static final short XD_STRING = XD_BYTES + 1; // 12
	/** Date value ID. */
	public static final short XD_DATETIME = XD_STRING + 1; // 13
	/** Regular expression value ID. */
	public static final short XD_REGEX = XD_DATETIME + 1; // 14
	/** Regular expression result value ID. */
	public static final short XD_REGEXRESULT = XD_REGEX + 1; // 15
	/** Container value ID. */
	public static final short XD_CONTAINER = XD_REGEXRESULT + 1; // 16
	/** org.w3c.dom.Element value ID. */
	public static final short XD_ELEMENT = XD_CONTAINER + 1; // 17
	/** org.w3c.dom.Attr value ID */
	public static final short XD_ATTR = XD_ELEMENT + 1; // 18
	/** org.w3c.dom.Text node value ID */
	public static final short XD_TEXT = XD_ATTR + 1; // 19
	/** Exception object */
	public static final short XD_EXCEPTION = XD_TEXT + 1; // 20
	/** Report value ID */
	public static final short XD_REPORT = XD_EXCEPTION + 1; // 21
	/** value of XPATH */
	public static final short XD_XPATH = XD_REPORT + 1; // 22
	/** value of XQUERY */
	public static final short XD_XQUERY = XD_XPATH + 1; // 23
	/** Database service value (DB Connection etc). */
	public static final short XD_SERVICE = XD_XQUERY + 1; // 24
	/** Service statement. */
	public static final short XD_STATEMENT = XD_SERVICE + 1; // 25
	/** XDResultSet value. */
	public static final short XD_RESULTSET = XD_STATEMENT + 1; // 26
	/** Parser value. */
	public static final short XD_PARSER = XD_RESULTSET + 1; // 27
	/** Parser result value. */
	public static final short XD_PARSERESULT = XD_PARSER + 1; // 28
	/** Named value. */
	public static final short XD_NAMEDVALUE = XD_PARSERESULT + 1; // 29
	/** XML stream writer. */
	public static final short XD_XMLWRITER = XD_NAMEDVALUE + 1; // 30
	/** Any value (may be null). */
	public static final short XD_LOCALE = XD_XMLWRITER + 1; // 31
	/** Any value (may be null). */
	public static final short XD_ANY = XD_LOCALE + 1; // 32
	/** Object value. */
	public static final short XD_OBJECT = XD_ANY + 1; // 33
	/** Null type. */
	static final short XD_NULL = XD_OBJECT + 1; // 34
	/** XXElement value. */
	public static final short XX_ELEMENT = XD_NULL + 1; // 35
	/** XXText value. */
	public static final short XX_TEXT = XX_ELEMENT + 1; // 36
	/** XXAttr value. */
	public static final short XX_ATTR = XX_TEXT + 1; // 37
	/** XXData value (supertype for both XXATTR and XXTEXT). */
	public static final short XX_DATA = XX_ATTR + 1; // 38
	/** XXDocument value. */
	public static final short XX_DOCUMENT = XX_DATA + 1; // 39
	/** XXPI (Processing instruction) value. */
	public static final short XX_PI = XX_DOCUMENT + 1; // 40
	/** XXComment (comment) value. */
	public static final short XX_COMMENT = XX_PI + 1; // 41
	/** XXChoice value. */
	public static final short XX_CHOICE = XX_COMMENT + 1; // 42
	/** XXMixed value. */
	public static final short XX_MIXED = XX_CHOICE + 1; // 43
	/** XXsequence value. */
	public static final short XX_SEQUENCE = XX_MIXED + 1; // 44
	/** XModel value. */
	public static final short XM_MODEL = XX_SEQUENCE + 1; // 45
	/** Undefined type. */
	public static final short XD_UNDEF = XM_MODEL + 1; // 46
}