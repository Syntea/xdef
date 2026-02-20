package org.xdef.util.xsd2xd;

/** Contains XML Schema node names and values as static constants.
 * @author Ilia Alexandrov
 */
public interface XsdNames {
    /** Constant for interpreting unbounded occurrence. */
    public static final int UNBOUNDED_CONST = -256;
    //--------------------------------------------------------------------------
    //                          NODE NAMES
    //--------------------------------------------------------------------------
    /** XML Schema schema element local name. */
    public static final String SCHEMA = "schema";
    /** XML Schema simpleType element local name. */
    public static final String SIMPLE_TYPE = "simpleType";
    /** XML Schema group element local name. */
    public static final String GROUP = "group";
    /** XML Schema complexType element local name. */
    public static final String COMPLEX_TYPE = "complexType";
    /** XML Schema attributeGroup element local name. */
    public static final String ATTRIBUTE_GROUP = "attributeGroup";
    /** XML Schema includeImport element local name. */
    public static final String INCLUDE = "include";
    /** XML Schema import element local name. */
    public static final String IMPORT = "import";
    /** XML Schema element element local name. */
    public static final String ELEMENT = "element";
    /** XML Schema any element local name. */
    public static final String ANY = "any";
    /** XML Schema any element local name. */
    public static final String ALL = "all";
    /** XML Schema annotation element local name. */
    public static final String ANNOTATION = "annotation";
    /** XML Schema documentation element local name. */
    public static final String DOCUMENTATION = "documentation";
    /** XML Schema anyAttribute element local name. */
    public static final String ANY_ATTRIBUTE = "anyAttribute";
    /** XML Schema attribute element local name. */
    public static final String ATTRIBUTE = "attribute";
    /** XML Schema complexContent element local name. */
    public static final String COMPLEX_CONTENT = "complexContent";
    /** XML Schema choice element local name. */
    public static final String CHOICE = "choice";
    /** XML Schema extension element local name. */
    public static final String EXTENSION = "extension";
    /** XML Schema restriction element local name. */
    public static final String RESTRICTION = "restriction";
    /** XML Schema sequence element local name. */
    public static final String SEQUENCE = "sequence";
    /** XML Schema simpleContent element local name. */
    public static final String SIMPLE_CONTENT = "simpleContent";
    /** XML Schema union element local name. */
    public static final String UNION = "union";
    /** XML Schema list element local name. */
    public static final String LIST = "list";
    /** XML Schema enumeration element local name. */
    public static final String ENUMERATION = "enumeration";
    /** XML Schema fractionDigits element local name. */
    public static final String FRACTION_DIGITS = "fractionDigits";
    /** XML Schema length element local name. */
    public static final String LENGTH = "length";
    /** XML Schema maxExclusive element local name. */
    public static final String MAX_EXCLUSIVE = "maxExclusive";
    /** XML Schema maxInclusive element local name. */
    public static final String MAX_INCLUSIVE = "maxInclusive";
    /** XML Schema maxLength element local name. */
    public static final String MAX_LENGTH = "maxLength";
    /** XML Schema minExclusive element local name. */
    public static final String MIN_EXCLUSIVE = "minExclusive";
    /** XML Schema minInclusive element local name. */
    public static final String MIN_INCLUSIVE = "minInclusive";
    /** XML Schema minLength element local name. */
    public static final String MIN_LENGTH = "minLength";
    /** XML Schema pattern element local name. */
    public static final String PATTERN = "pattern";
    /** XML Schema totalDigits element local name. */
    public static final String TOTAL_DIGITS = "totalDigits";
    /** XML Schema whiteSpace element local name. */
    public static final String WHITE_SPACE = "whiteSpace";
    /** XML Schema memberTypes attribute local name. */
    public static final String MEMBER_TYPES = "memberTypes";
    /** XML Schema targetNamespace attribute local name. */
    public static final String TARGET_NAMESPACE = "targetNamespace";
    /** XML Schema name attribute local name. */
    public static final String NAME = "name";
    /** XML Schema schemaLocation attribute local name. */
    public static final String SCHEMA_LOCATION = "schemaLocation";
    /** XML Schema namespace attribute local name. */
    public static final String NAMESPACE = "namespace";
    /** XML Schema type attribute local name. */
    public static final String TYPE = "type";
    /** XML Schema default attribute local name. */
    public static final String DEFAULT = "default";
    /** XML Schema fixed attribute local name. */
    public static final String FIXED = "fixed";
    /** XML Schema minOccurs attribute local name. */
    public static final String MIN_OCCURS = "minOccurs";
    /** XML Schema maxOccurs attribute local name. */
    public static final String MAX_OCCURS = "maxOccurs";
    /** XML Schema nillable attribute local name. */
    public static final String NILLABLE = "nillable";
    /** XML Schema mixed attribute local name. */
    public static final String MIXED = "mixed";
    /** XML Schema form attribute local name. */
    public static final String FORM = "form";
    /** XML Schema value attribute local name. */
    public static final String VALUE = "value";
    /** XML Schema itemType attribute local name. */
    public static final String ITEM_TYPE = "itemType";
    /** XML Schema processContents attribute local name. */
    public static final String PROCESS_CONTENTS = "processContents";
    /** XML Schema use attribute local name. */
    public static final String USE = "use";
    /** XML Schema ref attribute local name. */
    public static final String REF = "ref";
    /** XML Schema base attribute local name. */
    public static final String BASE = "base";
    /** XML Schema required attribute use. */
    public static final String REQUIRED = "required";
    /** XML Schema optional attribute use. */
    public static final String OPTIONAL = "optional";
    /** XML Schema prohibited attribute use. */
    public static final String PROHIBITED = "prohibited";
    /** XML Schema qualified attribute form. */
    public static final String QUALIFIED = "qualified";
    /** XML Schema unqualified attribute form. */
    public static final String UNQUALIFIED = "unqualified";
    /** XML Schema unbounded occurrence attribute value. */
    public static final String UNBOUNDED = "unbounded";
    //-------------------------------------------------------------------------
    //                          TYPE NAMES
    //--------------------------------------------------------------------------
    /** XML Schema <code>anySimpleType</code> type name. */
    public static final String ANY_SIMPLE_TYPE = "anySimpleType";
    /** XML Schema <code>anyURI</code> type name. */
    public static final String ANY_URI = "anyURI";
    /** XML Schema <code>base64Binary</code> type name. */
    public static final String BASE_64_BINARY = "base64Binary";
    /** XML Schema <code>boolean</code> type name. */
    public static final String BOOLEAN = "boolean";
    /** XML Schema <code>byte</code> type name. */
    public static final String BYTE = "byte";
    /** XML Schema <code>date</code> type name. */
    public static final String DATE = "date";
    /** XML Schema <code>dateTime</code> type name. */
    public static final String DATE_TIME = "dateTime";
    /** XML Schema <code>decimal</code> type name. */
    public static final String DECIMAL = "decimal";
    /** XML Schema <code>double</code> type name. */
    public static final String DOUBLE = "double";
    /** XML Schema <code>duration</code> type name. */
    public static final String DURATION = "duration";
    /** XML Schema <code>ENTITY</code> type name. */
    public static final String ENTITY = "ENTITY";
    /** XML Schema <code>ENTITIES</code> type name. */
    public static final String ENTITIES = "ENTITIES";
    /** XML Schema <code>float</code> type name. */
    public static final String FLOAT = "float";
    /** XML Schema <code>gDay</code> type name. */
    public static final String G_DAY = "gDay";
    /** XML Schema <code>gMonth</code> type name. */
    public static final String G_MONTH = "gMonth";
    /** XML Schema <code>gMonthDay</code> type name. */
    public static final String G_MONTH_DAY = "gMonthDay";
    /** XML Schema <code>gYear</code> type name. */
    public static final String G_YEAR = "gYear";
    /** XML Schema <code>gYearMonth</code> type name. */
    public static final String G_YEAR_MONTH = "gYearMonth";
    /** XML Schema <code>hexBinary</code> type name. */
    public static final String HEX_BINARY = "hexBinary";
    /** XML Schema <code>ID</code> type name. */
    public static final String ID = "ID";
    /** XML Schema <code>IDREF</code> type name. */
    public static final String IDREF = "IDREF";
    /** XML Schema <code>IDREFS</code> type name. */
    public static final String IDREFS = "IDREFS";
    /** XML Schema <code>int</code> type name. */
    public static final String INT = "int";
    /** XML Schema <code>integer</code> type name. */
    public static final String INTEGER = "integer";
    /** XML Schema <code>language</code> type name. */
    public static final String LANGUAGE = "language";
    /** XML Schema <code>long</code> type name. */
    public static final String LONG = "long";
    /** XML Schema <code>Name</code> type name. */
    public static final String NAME_TYPE = "Name";
    /** XML Schema <code>NCName</code> type name. */
    public static final String NCNAME = "NCName";
    /** XML Schema <code>negativeInteger</code> type name. */
    public static final String NEGATIVE_INTEGER = "negativeInteger";
    /** XML Schema <code>NMTOKEN</code> type name. */
    public static final String NMTOKEN = "NMTOKEN";
    /** XML Schema <code>NMTOKENS</code> type name. */
    public static final String NMTOKENS = "NMTOKENS";
    /** XML Schema <code>nonNegativeInteger</code> type name. */
    public static final String NON_NEGATIVE_INTEGER = "nonNegativeInteger";
    /** XML Schema <code>nonPositiveInteger</code> type name. */
    public static final String NON_POSITIVE_INTEGER = "nonPositiveInteger";
    /** XML Schema <code>normalizedString</code> type name. */
    public static final String NORMALIZED_STRING = "normalizedString";
    /** XML Schema <code>NOTATION</code> type name. */
    public static final String NOTATION = "NOTATION";
    /** XML Schema <code>positiveInteger</code> type name. */
    public static final String POSITIVE_INTEGER = "positiveInteger";
    /** XML Schema <code>QName</code> type name. */
    public static final String QNAME = "QName";
    /** XML Schema <code>short</code> type name. */
    public static final String SHORT = "short";
    /** XML Schema <code>string</code> type name. */
    public static final String STRING = "string";
    /** XML Schema <code>time</code> type name. */
    public static final String TIME = "time";
    /** XML Schema <code>token</code> type name. */
    public static final String TOKEN = "token";
    /** XML Schema <code>unsignedByte</code> type name. */
    public static final String UNSIGNED_BYTE = "unsignedByte";
    /** XML Schema <code>unsignedShort</code> type name. */
    public static final String UNSIGNED_SHORT = "unsignedShort";
    /** XML Schema <code>unsignedInt</code> type name. */
    public static final String UNSIGNED_INT = "unsignedInt";
    /** XML Schema <code>unsignedLong</code> type name. */
    public static final String UNSIGNED_LONG = "unsignedLong";
}