package org.xdef.impl.util.conv.xd.xd_2_0;

/** Contains X-definition names as static constants.
 * @author Ilia Alexandrov
 */
public interface XdNames {

	//--------------------------------------------------------------------------
	//                          NODE NAMES
	//--------------------------------------------------------------------------
	/** X-definition <code>def</code> element local name.*/
	public static final String DEF = "def";
	/** X-definition <code>script</code> attribute local name.*/
	public static final String SCRIPT = "script";
	/** X-definition <code>collection</code> element local name.*/
	public static final String COLLECTION = "collection";
	/** X-definition <code>include</code> attribute local name.*/
	public static final String INCLUDE = "include";
	/** X-definition <code>name</code> attribute local name.*/
	public static final String NAME = "name";
	/** X-definition <code>root</code> attribute local name.*/
	public static final String ROOT = "root";
	/** X-definition <code>attr</code> attribute local name.*/
	public static final String ATTR = "attr";
	/** X-definition <code>declaration</code> element local name.*/
	public static final String DECLARATION = "declaration";
	/** X-definition <code>macro</code> element local name.*/
	public static final String MACRO = "macro";
	/** X-definition <code>any</code> element local name.*/
	public static final String ANY = "any";
	/** X-definition <code>choice</code> element local name.*/
	public static final String CHOICE = "choice";
	/** X-definition <code>mixed</code> element local name.*/
	public static final String MIXED = "mixed";
	/** X-definition <code>sequence</code> element local name.*/
	public static final String SEQUENCE = "sequence";
	/** X-definition <code>text</code> attribute local name.*/
	public static final String TEXT = "text";
	/** X-definition <code>textcontent</code> attribute local name.*/
	public static final String TEXTCONTENT = "textcontent";
	/** X-definition <code>required</code> occurrence.*/
	public static final String REQUIRED = "required";
	/** X-definition <code>optional</code> occurrence.*/
	public static final String OPTIONAL = "optional";
	/** X-definition <code>illegal</code> occurrence.*/
	public static final String ILLEGAL = "illegal";
	//--------------------------------------------------------------------------
	//                          TYPE NAMES
	//--------------------------------------------------------------------------
	/** X-definition <code>an</code> type name.*/
	public static final String ALFA_NUMERIC = "an";
	/** X-definition <code>base64</code> type name.*/
	public static final String BASE_64 = "base64Binary";
	/** X-definition <code>boolean</code> type name.*/
	public static final String BOOLEAN = "boolean";
	//** X-definition <code>BNF</code> type name.*/
	//public static final String BNF = "BNF";
	/** X-definition <code>contains</code> type name.*/
	public static final String CONTAINS = "contains";
	/** X-definition <code>containsi</code> type name.*/
	public static final String CONTAINS_I = "containsi";
	/** X-definition <code>datetime</code> type name.*/
	public static final String DATE = "date";
	/** X-definition <code>datetime</code> type name (deprecated).*/
	public static final String DATE_TIME = "datetime";
	/** X-definition <code>xdatetime</code> type name.*/
	public static final String XDATE_TIME = "xdatetime"; /*VT*/
	/** X-definition <code>dateYMDhms</code> type name.*/
	public static final String DATE_YMDHMS = "dateYMDhms";
	/** X-definition <code>dec</code> type name.*/
	public static final String DECIMAL = "decimal";
	/** X-definition <code>ENTITY</code> type name.*/
	public static final String ENTITY = "ENTITY";
	/** X-definition <code>ENTITIES</code> type name.*/
	public static final String ENTITIES = "ENTITIES";
	/** X-definition <code>ID</code> type name.*/
	public static final String ID = "ID";
	/** X-definition <code>IDREF</code> type name.*/
	public static final String IDREF = "IDREF";
	/** X-definition <code>IDREFS</code> type name.*/
	public static final String IDREFS = "IDREFS";
	/** X-definition <code>ISOdateTime</code> type name.*/
	public static final String ISO_DATE_TIME = "ISOdateTime";
	/** X-definition <code>ISOdate</code> type name.*/
	public static final String ISO_DATE = "ISOdate";
	/** X-definition <code>ISOtime</code> type name.*/
	public static final String ISO_TIME = "ISOtime";
	/** X-definition <code>ISOday</code> type name.*/
	public static final String ISO_DAY = "ISOday";
	/** X-definition <code>ISOlanguage</code> type name.*/
	public static final String ISO_LANGUAGE = "language";
	/** X-definition <code>ISOlanguages</code> type name.*/
	public static final String ISO_LANGUAGES = "ISOlanguages";
	/** X-definition <code>ISOmonth</code> type name.*/
	public static final String ISO_MONTH = "ISOmonth";
	/** X-definition <code>ISOmonthDay</code> type name.*/
	public static final String ISO_MONTH_DAY = "ISOmonthDay";
	/** X-definition <code>ISOyear</code> type name.*/
	public static final String ISO_YEAR = "ISOyear";
	/** X-definition <code>ISOyearMonth</code> type name.*/
	public static final String ISO_YEAR_MONTH = "ISOyearMonth";
	/** X-definition <code>ISOduration</code> type name.*/
	public static final String ISO_DURATION = "ISOduration";
	/** X-definition <code>email</code> type name.*/
	public static final String EMAIL = "email";
	/** X-definition <code>emailList</code> type name.*/
	public static final String EMAIL_LIST = "emailList";
	/** X-definition <code>emailDate</code> type name.*/
	public static final String EMAIL_DATE = "emailDate";
	/** X-definition <code>ends</code> type name.*/
	public static final String ENDS = "ends";
	/** X-definition <code>endsi</code> type name.*/
	public static final String ENDS_I = "endsi";
	/** X-definition <code>equals</code> type name.*/
	public static final String EQUALS = "equals";
	/** X-definition <code>equalsi</code> type name.*/
	public static final String EQUALS_I = "equalsi";
	/** X-definition <code>file</code> type name.*/
	public static final String FILE = "file";
	/** X-definition <code>float</code> type name.*/
	public static final String FLOAT = "float";
	/** X-definition <code>hex</code> type name.*/
	public static final String HEX = "hexBinary";
	/** X-definition <code>int</code> type name.*/
	public static final String INT = "int";
	/** X-definition <code>list</code> type name.*/
	public static final String ENUM = "enum";
	/** X-definition <code>listi</code> type name.*/
	public static final String ENUM_I = "enumi";
	/** X-definition <code>tokens</code> type name.*/
	public static final String TOKENS = "tokens";
	/** X-definition <code>tokensi</code> type name.*/
	public static final String TOKENS_I = "tokensi";
	/** X-definition <code>MD5</code> type name.*/
	public static final String MD5 = "MD5";
	/** X-definition <code>NCName</code> type name.*/
	public static final String NC_NAME = "NCName";
	/** X-definition <code>NCnameList</code> type name.*/
	public static final String NC_NAME_LIST = "NCnameList";
	/** X-definition <code>NMTOKEN</code> type name.*/
	public static final String NM_TOKEN = "NMTOKEN";
	/** X-definition <code>NMTOKENS</code> type name.*/
	public static final String NM_TOKENS = "NMTOKENS";
	/** X-definition <code>NOTATION</code> type name.*/
	public static final String NOTATION = "NOTATION";
	/** X-definition <code>normString</code> type name.*/
	public static final String NORM_STRING = "normalizedString";
	/** X-definition <code>normToken</code> type name.*/
	public static final String NORM_TOKEN = "token";
	/** X-definition <code>normTokens</code> type name.*/
	public static final String NORM_TOKENS = "nmTokens";
	/** X-definition <code>num</code> type name.*/
	public static final String NUMBER = "num";
	/** X-definition <code>pic</code> type name.*/
	public static final String PICTURE = "pic";
	/** X-definition <code>QName</code> type name.*/
	public static final String Q_NAME = "QName";
	/** X-definition <code>QnameURI</code> type name.*/
	public static final String Q_NAME_URI = "QnameURI";
	/** X-definition <code>QnameList</code> type name.*/
	public static final String Q_NAME_LIST = "QnameList";
	/** X-definition <code>QnameListURI</code> type name.*/
	public static final String Q_NAME_LIST_URI = "QNameURIList";
	/** X-definition <code>regex</code> type name.*/
	public static final String REGEX = "regex";
	/** X-definition <code>starts</code> type name.*/
	public static final String STARTS = "starts";
	/** X-definition <code>startsi</code> type name.*/
	public static final String STARTS_I = "startsi";
	/** X-definition <code>string</code> type name.*/
	public static final String STRING = "string";
	/** X-definition <code>uri</code> type name.*/
	public static final String URI = "uri";
	/** X-definition <code>uriList</code> type name.*/
	public static final String URI_LIST = "uriList";
	/** X-definition <code>url</code> type name.*/
	public static final String URL = "url";
	/** X-definition <code>urlList</code> type name.*/
	public static final String URL_LIST = "urlList";
	//--------------------------------------------------------------------------
	//                         SCHEMA TYPE NAMES
	//--------------------------------------------------------------------------
	/** Schema <code>anyURI</code> type name in X-definition.*/
	public static final String XS_ANY_URI = "xs:anyURI";
	/** Schema <code>base64Binary</code> type name in X-definition.*/
	public static final String XS_BASE_64_BINARY = "xs:base64Binary";
	/** Schema <code>boolean</code> type name in X-definition.*/
	public static final String XS_BOOLEAN = "xs:boolean";
	/** Schema <code>byte</code> type name in X-definition.*/
	public static final String XS_BYTE = "xs:byte";
	/** Schema <code>date</code> type name in X-definition.*/
	public static final String XS_DATE = "xs:date";
	/** Schema <code>dateTime</code> type name in X-definition.*/
	public static final String XS_DATE_TIME = "xs:dateTime";
	/** Schema <code>decimal</code> type name in X-definition.*/
	public static final String XS_DECIMAL = "xs:decimal";
	/** Schema <code>double</code> type name in X-definition.*/
	public static final String XS_DOUBLE = "xs:double";
	/** Schema <code>duration</code> type name in X-definition.*/
	public static final String XS_DURATION = "xs:duration";
	/** Schema <code>float</code> type name in X-definition.*/
	public static final String XS_FLOAT = "xs:float";
	/** Schema <code>gDay</code> type name in X-definition.*/
	public static final String XS_G_DAY = "xs:gDay";
	/** Schema <code>gMonth</code> type name in X-definition.*/
	public static final String XS_G_MONTH = "xs:gMonth";
	/** Schema <code>gMonthDay</code> type name in X-definition.*/
	public static final String XS_G_MONTH_DAY = "xs:gMonthDay";
	/** Schema <code>gYear</code> type name in X-definition.*/
	public static final String XS_G_YEAR = "xs:gYear";
	/** Schema <code>gYearMonth</code> type name in X-definition.*/
	public static final String XS_G_YEAR_MONTH = "xs:gYearMonth";
	/** Schema <code>hexBinary</code> type name in X-definition.*/
	public static final String XS_HEX_BINARY = "xs:hexBinary";
	/** Schema <code>int</code> type name in X-definition.*/
	public static final String XS_INT = "xs:int";
	/** Schema <code>integer</code> type name in X-definition.*/
	public static final String XS_INTEGER = "xs:integer";
	/** Schema <code>language</code> type name in X-definition.*/
	public static final String XS_LANGUAGE = "xs:language";
	/** Schema <code>long</code> type name in X-definition.*/
	public static final String XS_LONG = "xs:long";
	/** Schema <code>Name</code> type name in X-definition.*/
	public static final String XS_NAME = "xs:Name";
	/** Schema <code>NCName</code> type name in X-definition.*/
	public static final String XS_NCNAME = "xs:NCName";
	/** Schema <code>negativeInteger</code> type name in X-definition.*/
	public static final String XS_NEGATIVE_INTEGER = "xs:negativeInteger";
	/** Schema <code>NMTOKEN</code> type name in X-definition.*/
	public static final String XS_NMTOKEN = "xs:NMTOKEN";
	/** Schema <code>nonNegativeInteger</code> type name in X-definition.*/
	public static final String XS_NON_NEGATIVE_INTEGER = "xs:nonNegativeInteger";
	/** Schema <code>nonPositiveInteger</code> type name in X-definition.*/
	public static final String XS_NON_POSITIVE_INTEGER = "xs:nonPositiveInteger";
	/** Schema <code>normalizedString</code> type name in X-definition.*/
	public static final String XS_NORMALIZED_STRING = "xs:normalizedString";
	/** Schema <code>positiveInteger</code> type name in X-definition.*/
	public static final String XS_POSITIVE_INTEGER = "xs:positiveInteger";
	/** Schema <code>short</code> type name in X-definition.*/
	public static final String XS_SHORT = "xs:short";
	/** Schema <code>string</code> type name in X-definition.*/
	public static final String XS_STRING = "xs:string";
	/** Schema <code>time</code> type name in X-definition.*/
	public static final String XS_TIME = "xs:time";
	/** Schema <code>token</code> type name in X-definition.*/
	public static final String XS_TOKEN = "xs:token";
	/** Schema <code>unsignedByte</code> type name in X-definition.*/
	public static final String XS_UNSIGNED_BYTE = "xs:unsignedByte";
	/** Schema <code>unsignedInt</code> type name in X-definition.*/
	public static final String XS_UNSIGNED_INT = "xs:unsignedInt";
	/** Schema <code>unsignedLong</code> type name in X-definition.*/
	public static final String XS_UNSIGNED_LONG = "xs:unsignedLong";
	/** Schema <code>unsignedShort</code> type name in X-definition.*/
	public static final String XS_UNSIGNED_SHORT = "xs:unsignedShort";
	/** Schema <code>union</code> type name in X-definition.*/
	public static final String XS_UNION = "xs:union";
	/** Schema <code>list</code> type name in X-definition.*/
	public static final String XS_LIST = "xs:list";
	//--------------------------------------------------------------------------
	//                     SCHEMA TYPE PARAMETER NAMES
	//--------------------------------------------------------------------------
	/** Parameter <code>base</code> name in X-definition.*/
	public static final String BASE = "base";
	/** Parameter <code>enumeration</code> name in X-definition.*/
	public static final String ENUMERATION = "enumeration";
	/** Parameter <code>fractionDigits</code> name in X-definition.*/
	public static final String FRACTION_DIGITS = "fractionDigits";
	/** Parameter <code>length</code> name in X-definition.*/
	public static final String LENGTH = "length";
	/** Parameter <code>maxExclusive</code> name in X-definition.*/
	public static final String MAX_EXCLUSIVE = "maxExclusive";
	/** Parameter <code>maxInclusive</code> name in X-definition.*/
	public static final String MAX_INCLUSIVE = "maxInclusive";
	/** Parameter <code>maxLength</code> name in X-definition.*/
	public static final String MAX_LENGTH = "maxLength";
	/** Parameter <code>minExclusive</code> name in X-definition.*/
	public static final String MIN_EXCLUSIVE = "minExclusive";
	/** Parameter <code>minInclusive</code> name in X-definition.*/
	public static final String MIN_INCLUSIVE = "minInclusive";
	/** Parameter <code>minLength</code> name in X-definition.*/
	public static final String MIN_LENGTH = "minLength";
	/** Parameter <code>pattern</code> name in X-definition.*/
	public static final String PATTERN = "pattern";
	/** Parameter <code>totalDigits</code> name in X-definition.*/
	public static final String TOTAL_DIGITS = "totalDigits";
	/** Parameter <code>whiteSpace</code> name in X-definition.*/
	public static final String WHITE_SPACE = "whiteSpace";
	/** Parameter <code>item</code> name in X-definition schema
	 * <code>union</code> type.
	 */
	public static final String ITEM_UNION = "item";
	/** Parameter <code>item</code> name in X-definition schema
	 * <code>union</code> type.*/
	public static final String ITEM_LIST = "item";
}