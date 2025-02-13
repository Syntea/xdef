package org.xdef.util.xsd2xd.xd;

/** Contains Xdefinition names as static constants.
 * @author Ilia Alexandrov
 */
public interface XdNames {

	//--------------------------------------------------------------------------
	//                          NODE NAMES
	//--------------------------------------------------------------------------
	/** Xdefinition <code>any</code> element local name.*/
	public static final String ANY = "any";
	/** Xdefinition <code>attr</code> attribute local name.*/
	public static final String ATTR = "attr";
	/** Xdefinition <code>choice</code> element local name.*/
	public static final String CHOICE = "choice";
	/** Xdefinition <code>collection</code> element local name.*/
	public static final String COLLECTION = "collection";
	/** Xdefinition <code>declaration</code> element local name.*/
	public static final String DECLARATION = "declaration";
	/** Xdefinition <code>def</code> element local name.*/
	public static final String DEF = "def";
	/** Xdefinition <code>illegal</code> occurrence.*/
	public static final String ILLEGAL = "illegal";
	/** Xdefinition <code>include</code> attribute local name.*/
	public static final String INCLUDE = "include";
	/** Xdefinition <code>mixed</code> element local name.*/
	public static final String MIXED = "mixed";
	/** Xdefinition <code>name</code> attribute local name.*/
	public static final String NAME = "name";
	/** Xdefinition <code>optional</code> occurrence.*/
	public static final String OPTIONAL = "optional";
	/** Xdefinition <code>required</code> occurrence.*/
	public static final String REQUIRED = "required";
	/** Xdefinition <code>root</code> attribute local name.*/
	public static final String ROOT = "root";
	/** Xdefinition <code>script</code> attribute local name.*/
	public static final String SCRIPT = "script";
	/** Xdefinition <code>sequence</code> element local name.*/
	public static final String SEQUENCE = "sequence";
	/** Xdefinition <code>text</code> attribute local name.*/
	public static final String TEXT = "text";
	//--------------------------------------------------------------------------
	//                          XDEFINITION TYPE NAMES
	//--------------------------------------------------------------------------
	/** Xdefinition <code>an</code> type name.*/
	public static final String ALFA_NUMERIC = "an";
	/** Xdefinition <code>base64</code> type name.*/
	public static final String BASE_64 = "base64Binary";
	/** Xdefinition <code>boolean</code> type name.*/
	public static final String BOOLEAN = "boolean";
	//** Xdefinition <code>BNF</code> type name.*/
	//public static final String BNF = "BNF";
	/** Xdefinition <code>contains</code> type name.*/
	public static final String CONTAINS = "contains";
	/** Xdefinition <code>containsi</code> type name.*/
	public static final String CONTAINS_I = "containsi";
	/** Xdefinition <code>datetime</code> type name.*/
	public static final String DATE = "date";
	/** Xdefinition <code>dateYMDhms</code> type name.*/
	public static final String DATE_YMDHMS = "dateYMDhms";
	/** Xdefinition <code>dec</code> type name.*/
	public static final String DECIMAL = "decimal";
	/** Xdefinition <code>ENTITY</code> type name.*/
	public static final String ENTITY = "ENTITY";
	/** Xdefinition <code>ENTITIES</code> type name.*/
	public static final String ENTITIES = "ENTITIES";
	/** Xdefinition <code>email</code> type name.*/
	public static final String EMAIL = "emailAddr";
	/** Xdefinition <code>emailList</code> type name.*/
	public static final String EMAIL_LIST = "emaiAddrlList";
	/** Xdefinition <code>emailDate</code> type name.*/
	public static final String EMAIL_DATE = "emailDate";
	/** Xdefinition <code>ends</code> type name.*/
	public static final String ENDS = "ends";
	/** Xdefinition <code>endsi</code> type name.*/
	public static final String ENDS_I = "endsi";
	/** Xdefinition <code>enum</code> type name.*/
	public static final String ENUM = "enum";
	/** Xdefinition <code>listi</code> type name.*/
	public static final String ENUM_I = "enumi";
	/** Xdefinition <code>equals</code> type name.*/
	public static final String EQUALS = "equals";
	/** Xdefinition <code>equalsi</code> type name.*/
	public static final String EQUALS_I = "equalsi";
	/** Xdefinition <code>file</code> type name.*/
	public static final String FILE = "file";
	/** Xdefinition <code>float</code> type name.*/
	public static final String FLOAT = "float";
	/** Xdefinition <code>hex</code> type name.*/
	public static final String HEX = "hexBinary";
	/** Xdefinition <code>ID</code> type name.*/
	public static final String ID = "ID";
	/** Xdefinition <code>IDREF</code> type name.*/
	public static final String IDREF = "IDREF";
	/** Xdefinition <code>IDREFS</code> type name.*/
	public static final String IDREFS = "IDREFS";
	/** Xdefinition <code>int</code> type name.*/
	public static final String INT = "int";
	/** Xdefinition <code>ISOdateTime</code> type name.*/
	public static final String ISO_DATE_TIME = "ISOdateTime";
	/** Xdefinition <code>ISOdate</code> type name.*/
	public static final String ISO_DATE = "ISOdate";
	/** Xdefinition <code>ISOtime</code> type name.*/
	public static final String ISO_TIME = "ISOtime";
	/** Xdefinition <code>ISOday</code> type name.*/
	public static final String ISO_DAY = "ISOday";
	/** Xdefinition <code>ISOlanguage</code> type name.*/
	public static final String ISO_LANGUAGE = "language";
	/** Xdefinition <code>ISOlanguages</code> type name.*/
	public static final String ISO_LANGUAGES = "ISOlanguages";
	/** Xdefinition <code>ISOmonth</code> type name.*/
	public static final String ISO_MONTH = "ISOmonth";
	/** Xdefinition <code>ISOmonthDay</code> type name.*/
	public static final String ISO_MONTH_DAY = "ISOmonthDay";
	/** Xdefinition <code>ISOyear</code> type name.*/
	public static final String ISO_YEAR = "ISOyear";
	/** Xdefinition <code>ISOyearMonth</code> type name.*/
	public static final String ISO_YEAR_MONTH = "ISOyearMonth";
	/** Xdefinition <code>ISOduration</code> type name.*/
	public static final String ISO_DURATION = "ISOduration";
	/** Xdefinition <code>MD5</code> type name.*/
	public static final String MD5 = "MD5";
	/** Xdefinition <code>NCName</code> type name.*/
	public static final String NC_NAME = "NCName";
	/** Xdefinition <code>NCnameList</code> type name.*/
	public static final String NC_NAME_LIST = "NCnameList";
	/** Xdefinition <code>NMTOKEN</code> type name.*/
	public static final String NM_TOKEN = "NMTOKEN";
	/** Xdefinition <code>NMTOKENS</code> type name.*/
	public static final String NM_TOKENS = "NMTOKENS";
	/** Xdefinition <code>normString</code> type name.*/
	public static final String NORM_STRING = "normalizedString";
	/** Xdefinition <code>normToken</code> type name.*/
	public static final String NORM_TOKEN = "token";
	/** Xdefinition <code>normTokens</code> type name.*/
	public static final String NORM_TOKENS = "nmTokens";
	/** Xdefinition <code>NOTATION</code> type name.*/
	public static final String NOTATION = "NOTATION";
	/** Xdefinition <code>num</code> type name.*/
	public static final String NUMBER = "num";
	/** Xdefinition <code>pic</code> type name.*/
	public static final String PICTURE = "pic";
	/** Xdefinition <code>QName</code> type name.*/
	public static final String Q_NAME = "QName";
	/** Xdefinition <code>QnameList</code> type name.*/
	public static final String Q_NAME_LIST = "QnameList";
	/** Xdefinition <code>QnameListURI</code> type name.*/
	public static final String Q_NAME_LIST_URI = "QNameURIList";
	/** Xdefinition <code>QnameURI</code> type name.*/
	public static final String Q_NAME_URI = "QnameURI";
	/** Xdefinition <code>regex</code> type name.*/
	public static final String REGEX = "regex";
	/** Xdefinition <code>starts</code> type name.*/
	public static final String STARTS = "starts";
	/** Xdefinition <code>startsi</code> type name.*/
	public static final String STARTS_I = "startsi";
	/** Xdefinition <code>string</code> type name.*/
	public static final String STRING = "string";
	/** Xdefinition <code>tokens</code> type name.*/
	public static final String TOKENS = "tokens";
	/** Xdefinition <code>tokensi</code> type name.*/
	public static final String TOKENS_I = "tokensi";
	/** Xdefinition <code>uri</code> type name.*/
	public static final String URI = "uri";
	/** Xdefinition <code>uriList</code> type name.*/
	public static final String URI_LIST = "uriList";
	/** Xdefinition <code>url</code> type name.*/
	public static final String URL = "url";
	/** Xdefinition <code>urlList</code> type name.*/
	public static final String URL_LIST = "urlList";
	/** Xdefinition <code>xdatetime</code> type name.*/
	public static final String XDATETIME = "xdatetime";
	//--------------------------------------------------------------------------
	//                         XML SCHEMA TYPE NAMES
	//--------------------------------------------------------------------------
	/** Schema <code>anyURI</code> type name in Xdefinition.*/
	public static final String XS_ANY_URI = "xs:anyURI";
	/** Schema <code>base64Binary</code> type name in Xdefinition.*/
	public static final String XS_BASE_64_BINARY = "xs:base64Binary";
	/** Schema <code>boolean</code> type name in Xdefinition.*/
	public static final String XS_BOOLEAN = "xs:boolean";
	/** Schema <code>byte</code> type name in Xdefinition.*/
	public static final String XS_BYTE = "xs:byte";
	/** Schema <code>date</code> type name in Xdefinition.*/
	public static final String XS_DATE = "xs:date";
	/** Schema <code>dateTime</code> type name in Xdefinition.*/
	public static final String XS_DATE_TIME = "xs:dateTime";
	/** Schema <code>decimal</code> type name in Xdefinition.*/
	public static final String XS_DECIMAL = "xs:decimal";
	/** Schema <code>double</code> type name in Xdefinition.*/
	public static final String XS_DOUBLE = "xs:double";
	/** Schema <code>duration</code> type name in Xdefinition.*/
	public static final String XS_DURATION = "xs:duration";
	/** Schema <code>float</code> type name in Xdefinition.*/
	public static final String XS_FLOAT = "xs:float";
	/** Schema <code>gDay</code> type name in Xdefinition.*/
	public static final String XS_G_DAY = "xs:gDay";
	/** Schema <code>gMonth</code> type name in Xdefinition.*/
	public static final String XS_G_MONTH = "xs:gMonth";
	/** Schema <code>gMonthDay</code> type name in Xdefinition.*/
	public static final String XS_G_MONTH_DAY = "xs:gMonthDay";
	/** Schema <code>gYear</code> type name in Xdefinition.*/
	public static final String XS_G_YEAR = "xs:gYear";
	/** Schema <code>gYearMonth</code> type name in Xdefinition.*/
	public static final String XS_G_YEAR_MONTH = "xs:gYearMonth";
	/** Schema <code>hexBinary</code> type name in Xdefinition.*/
	public static final String XS_HEX_BINARY = "xs:hexBinary";
	/** Schema <code>int</code> type name in Xdefinition.*/
	public static final String XS_INT = "xs:int";
	/** Schema <code>integer</code> type name in Xdefinition.*/
	public static final String XS_INTEGER = "xs:integer";
	/** Schema <code>language</code> type name in Xdefinition.*/
	public static final String XS_LANGUAGE = "xs:language";
	/** Schema <code>long</code> type name in Xdefinition.*/
	public static final String XS_LONG = "xs:long";
	/** Schema <code>Name</code> type name in Xdefinition.*/
	public static final String XS_NAME = "xs:Name";
	/** Schema <code>NCName</code> type name in Xdefinition.*/
	public static final String XS_NCNAME = "xs:NCName";
	/** Schema <code>negativeInteger</code> type name in Xdefinition.*/
	public static final String XS_NEGATIVE_INTEGER = "xs:negativeInteger";
	/** Schema <code>NMTOKEN</code> type name in Xdefinition.*/
	public static final String XS_NMTOKEN = "xs:NMTOKEN";
	/** Schema <code>nonNegativeInteger</code> type name in Xdefinition.*/
	public static final String XS_NON_NEGATIVE_INTEGER = "xs:nonNegativeInteger";
	/** Schema <code>nonPositiveInteger</code> type name in Xdefinition.*/
	public static final String XS_NON_POSITIVE_INTEGER = "xs:nonPositiveInteger";
	/** Schema <code>normalizedString</code> type name in Xdefinition.*/
	public static final String XS_NORMALIZED_STRING = "xs:normalizedString";
	/** Schema <code>positiveInteger</code> type name in Xdefinition.*/
	public static final String XS_POSITIVE_INTEGER = "xs:positiveInteger";
	/** Schema <code>short</code> type name in Xdefinition.*/
	public static final String XS_SHORT = "xs:short";
	/** Schema <code>string</code> type name in Xdefinition.*/
	public static final String XS_STRING = "xs:string";
	/** Schema <code>time</code> type name in Xdefinition.*/
	public static final String XS_TIME = "xs:time";
	/** Schema <code>token</code> type name in Xdefinition.*/
	public static final String XS_TOKEN = "xs:token";
	/** Schema <code>unsignedByte</code> type name in Xdefinition.*/
	public static final String XS_UNSIGNED_BYTE = "xs:unsignedByte";
	/** Schema <code>unsignedInt</code> type name in Xdefinition.*/
	public static final String XS_UNSIGNED_INT = "xs:unsignedInt";
	/** Schema <code>unsignedLong</code> type name in Xdefinition.*/
	public static final String XS_UNSIGNED_LONG = "xs:unsignedLong";
	/** Schema <code>unsignedShort</code> type name in Xdefinition.*/
	public static final String XS_UNSIGNED_SHORT = "xs:unsignedShort";
	/** Schema <code>union</code> type name in Xdefinition.*/
	public static final String XS_UNION = "xs:union";
	/** Schema <code>list</code> type name in Xdefinition.*/
	public static final String XS_LIST = "xs:list";
	//--------------------------------------------------------------------------
	//                     ITEM PARAMETER NAME (union, list)
	//--------------------------------------------------------------------------
	/** Parameter <code>item</code> name in Xdefinition schema union. */
	public static final String ITEM_UNION = "item";
	/** Parameter <code>item</code> name in Xdefinition schemalist. type.*/
	public static final String ITEM_LIST = "item";
	//--------------------------------------------------------------------------
	//                     XML SCHEMA TYPE PARAMETER NAMES
	//--------------------------------------------------------------------------
	/** Parameter <code>enumeration</code> name in Xdefinition.*/
	public static final String ENUMERATION = "enumeration";
	/** Parameter <code>fractionDigits</code> name in Xdefinition.*/
	public static final String FRACTION_DIGITS = "fractionDigits";
	/** Parameter <code>length</code> name in Xdefinition.*/
	public static final String LENGTH = "length";
	/** Parameter <code>maxExclusive</code> name in Xdefinition.*/
	public static final String MAX_EXCLUSIVE = "maxExclusive";
	/** Parameter <code>maxInclusive</code> name in Xdefinition.*/
	public static final String MAX_INCLUSIVE = "maxInclusive";
	/** Parameter <code>maxLength</code> name in Xdefinition.*/
	public static final String MAX_LENGTH = "maxLength";
	/** Parameter <code>minExclusive</code> name in Xdefinition.*/
	public static final String MIN_EXCLUSIVE = "minExclusive";
	/** Parameter <code>minInclusive</code> name in Xdefinition.*/
	public static final String MIN_INCLUSIVE = "minInclusive";
	/** Parameter <code>minLength</code> name in Xdefinition.*/
	public static final String MIN_LENGTH = "minLength";
	/** Parameter <code>pattern</code> name in Xdefinition.*/
	public static final String PATTERN = "pattern";
	/** Parameter <code>totalDigits</code> name in Xdefinition.*/
	public static final String TOTAL_DIGITS = "totalDigits";
	/** Parameter <code>whiteSpace</code> name in Xdefinition.*/
	public static final String WHITE_SPACE = "whiteSpace";
	//--------------------------------------------------------------------------
	//                     XDEFINITION TYPE PARAMETER NAMES
	//--------------------------------------------------------------------------
	/** Parameter <code>base</code> name in Xdefinition.*/
	public static final String BASE = "base";
	/** Parameter <code>base</code> name in Xdefinition.*/
	public static final String ARGUMENT = "argument";
	/** Parameter <code>base</code> name in Xdefinition.*/
	public static final String FORMAT = "format";
	/** Parameter <code>base</code> name in Xdefinition.*/
	public static final String OUTFORMAT = "outFormat";
	//--------------------------------------------------------------------------

}