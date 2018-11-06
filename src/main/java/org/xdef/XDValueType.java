package org.xdef;

/** Values of types of values in the Script of X-definition.
 * @author Vaclav Trojan
 */
public enum XDValueType {
	/** "void" value. */
	VOID,
	/** Int value (implemented as long). */
	INT,
	/** Boolean value ID. */
	BOOLEAN,
	/** Duration value ID. */
	DURATION,
	/** BNF grammar ID. */
	BNFGRAMMAR,
	/** BNF rule ID. */
	BNFRULE,
	/** BigDecimal value ID. */
	DECIMAL,
	/** BigInteger value ID. */
	BIGINTEGER,
	/** Float value ID (implemented as double). */
	FLOAT,
	/** Input stream value. */
	INPUT,
	/** Output stream value. */
	OUTPUT,
	/** Byte array value. */
	BYTES,
	/** String value ID. */
	STRING,
	/** Date value ID. */
	DATETIME,
	/** Regular expression value ID. */
	REGEX,
	/** Regular expression result value ID. */
	REGEXRESULT,
	/** Container value ID. */
	CONTAINER,
	/** org.w3c.dom.Element value ID. */
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
	/** Any value (may be null). */
	ANY,
	/** Any value (may be null). */
	LOCALE,
	/** Object value. */
	OBJECT,
	/** Null type. */
	NULL,
	/** XXElement value. */
	XXELEMENT,
	/** XXText value. */
	XXTEXT,
	/** XXAttr value. */
	XXATTR,
	/** XXData value (supertype for both XXATTR and XXTEXT). */
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
	/** Undefined type. */
	XD_UNDEF;
}