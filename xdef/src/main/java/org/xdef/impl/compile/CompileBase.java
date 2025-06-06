package org.xdef.impl.compile;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import org.xdef.XDContainer;
import org.xdef.XDFactory;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_ANYURI;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.XDValueID.XD_BNFRULE;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTE;
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
import static org.xdef.XDValueID.XD_FLOAT;
import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueID.XD_INPUT;
import static org.xdef.XDValueID.XD_INT;
import static org.xdef.XDValueID.XD_IPADDR;
import static org.xdef.XDValueID.XD_LOCALE;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_OBJECT;
import static org.xdef.XDValueID.XD_OUTPUT;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_PRICE;
import static org.xdef.XDValueID.XD_REGEX;
import static org.xdef.XDValueID.XD_REGEXRESULT;
import static org.xdef.XDValueID.XD_REPORT;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_SERVICE;
import static org.xdef.XDValueID.XD_SHORT;
import static org.xdef.XDValueID.XD_STATEMENT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_TELEPHONE;
import static org.xdef.XDValueID.XD_UNDEF;
import static org.xdef.XDValueID.XD_UNIQUESET_KEY;
import static org.xdef.XDValueID.XD_VOID;
import static org.xdef.XDValueID.XD_XMLWRITER;
import static org.xdef.XDValueID.XD_XPATH;
import static org.xdef.XDValueID.XD_XQUERY;
import static org.xdef.XDValueID.XM_MODEL;
import static org.xdef.XDValueID.XX_ATTR;
import static org.xdef.XDValueID.XX_DATA;
import static org.xdef.XDValueID.XX_ELEMENT;
import static org.xdef.XDValueID.X_NOTYPE_VALUE;
import static org.xdef.XDValueID.X_UNIQUESET_KEY;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.impl.code.CodeTable;
import static org.xdef.impl.code.CodeTable.ADD_DAY;
import static org.xdef.impl.code.CodeTable.ADD_HOUR;
import static org.xdef.impl.code.CodeTable.ADD_MILLIS;
import static org.xdef.impl.code.CodeTable.ADD_MINUTE;
import static org.xdef.impl.code.CodeTable.ADD_MONTH;
import static org.xdef.impl.code.CodeTable.ADD_NANOS;
import static org.xdef.impl.code.CodeTable.ADD_SECOND;
import static org.xdef.impl.code.CodeTable.ADD_YEAR;
import static org.xdef.impl.code.CodeTable.BNFRULE_PARSE;
import static org.xdef.impl.code.CodeTable.BNFRULE_VALIDATE;
import static org.xdef.impl.code.CodeTable.BNF_PARSE;
import static org.xdef.impl.code.CodeTable.BYTES_ADDBYTE;
import static org.xdef.impl.code.CodeTable.BYTES_CLEAR;
import static org.xdef.impl.code.CodeTable.BYTES_GETAT;
import static org.xdef.impl.code.CodeTable.BYTES_INSERT;
import static org.xdef.impl.code.CodeTable.BYTES_REMOVE;
import static org.xdef.impl.code.CodeTable.BYTES_SETAT;
import static org.xdef.impl.code.CodeTable.BYTES_SIZE;
import static org.xdef.impl.code.CodeTable.BYTES_TO_BASE64;
import static org.xdef.impl.code.CodeTable.BYTES_TO_HEX;
import static org.xdef.impl.code.CodeTable.CHAR_AT;
import static org.xdef.impl.code.CodeTable.CLEAR_REPORTS;
import static org.xdef.impl.code.CodeTable.CLOSE_XMLWRITER;
import static org.xdef.impl.code.CodeTable.COMPILE_REGEX;
import static org.xdef.impl.code.CodeTable.COMPOSE_OP;
import static org.xdef.impl.code.CodeTable.CONTAINS;
import static org.xdef.impl.code.CodeTable.CONTAINSI;
import static org.xdef.impl.code.CodeTable.CONTEXT_ADDITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETELEMENTS;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETELEMENT_X;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETLENGTH;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXQueryExpr;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETTEXT;
import static org.xdef.impl.code.CodeTable.CONTEXT_ITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_ITEMTYPE;
import static org.xdef.impl.code.CodeTable.CONTEXT_REMOVEITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_REPLACEITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_SORT;
import static org.xdef.impl.code.CodeTable.CONTEXT_TO_ELEMENT;
import static org.xdef.impl.code.CodeTable.CREATE_ELEMENT;
import static org.xdef.impl.code.CodeTable.CREATE_ELEMENTS;
import static org.xdef.impl.code.CodeTable.CURRENCYCODE;
import static org.xdef.impl.code.CodeTable.CUT_STRING;
import static org.xdef.impl.code.CodeTable.DB_CLOSE;
import static org.xdef.impl.code.CodeTable.DB_CLOSESTATEMENT;
import static org.xdef.impl.code.CodeTable.DB_COMMIT;
import static org.xdef.impl.code.CodeTable.DB_EXEC;
import static org.xdef.impl.code.CodeTable.DB_ISCLOSED;
import static org.xdef.impl.code.CodeTable.DB_PREPARESTATEMENT;
import static org.xdef.impl.code.CodeTable.DB_ROLLBACK;
import static org.xdef.impl.code.CodeTable.DB_SETPROPERTY;
import static org.xdef.impl.code.CodeTable.DEBUG_PAUSE;
import static org.xdef.impl.code.CodeTable.DEBUG_TRACE;
import static org.xdef.impl.code.CodeTable.DEFAULT_ERROR;
import static org.xdef.impl.code.CodeTable.DEL_ATTR;
import static org.xdef.impl.code.CodeTable.DURATION_GETDAYS;
import static org.xdef.impl.code.CodeTable.DURATION_GETEND;
import static org.xdef.impl.code.CodeTable.DURATION_GETFRACTION;
import static org.xdef.impl.code.CodeTable.DURATION_GETHOURS;
import static org.xdef.impl.code.CodeTable.DURATION_GETMINUTES;
import static org.xdef.impl.code.CodeTable.DURATION_GETMONTHS;
import static org.xdef.impl.code.CodeTable.DURATION_GETNEXTTIME;
import static org.xdef.impl.code.CodeTable.DURATION_GETRECURRENCE;
import static org.xdef.impl.code.CodeTable.DURATION_GETSECONDS;
import static org.xdef.impl.code.CodeTable.DURATION_GETSTART;
import static org.xdef.impl.code.CodeTable.DURATION_GETYEARS;
import static org.xdef.impl.code.CodeTable.ELEMENT_ADDELEMENT;
import static org.xdef.impl.code.CodeTable.ELEMENT_ADDTEXT;
import static org.xdef.impl.code.CodeTable.ELEMENT_ATTRS;
import static org.xdef.impl.code.CodeTable.ELEMENT_CHILDNODES;
import static org.xdef.impl.code.CodeTable.ELEMENT_GETATTR;
import static org.xdef.impl.code.CodeTable.ELEMENT_GETTEXT;
import static org.xdef.impl.code.CodeTable.ELEMENT_HASATTR;
import static org.xdef.impl.code.CodeTable.ELEMENT_NAME;
import static org.xdef.impl.code.CodeTable.ELEMENT_NSURI;
import static org.xdef.impl.code.CodeTable.ELEMENT_SETATTR;
import static org.xdef.impl.code.CodeTable.ELEMENT_TOCONTAINER;
import static org.xdef.impl.code.CodeTable.ELEMENT_TOSTRING;
import static org.xdef.impl.code.CodeTable.ENDSWITH;
import static org.xdef.impl.code.CodeTable.ENDSWITHI;
import static org.xdef.impl.code.CodeTable.EQUALSI;
import static org.xdef.impl.code.CodeTable.FORMAT_STRING;
import static org.xdef.impl.code.CodeTable.FROM_ELEMENT;
import static org.xdef.impl.code.CodeTable.GET_ATTR;
import static org.xdef.impl.code.CodeTable.GET_ATTR_NAME;
import static org.xdef.impl.code.CodeTable.GET_BNFRULE;
import static org.xdef.impl.code.CodeTable.GET_BYTES_FROM_STRING;
import static org.xdef.impl.code.CodeTable.GET_COUNTER;
import static org.xdef.impl.code.CodeTable.GET_DAY;
import static org.xdef.impl.code.CodeTable.GET_DAYTIMEMILLIS;
import static org.xdef.impl.code.CodeTable.GET_DBQUERY;
import static org.xdef.impl.code.CodeTable.GET_DBQUERY_ITEM;
import static org.xdef.impl.code.CodeTable.GET_DEFAULTZONE;
import static org.xdef.impl.code.CodeTable.GET_ELEMENT;
import static org.xdef.impl.code.CodeTable.GET_ELEMENT_LOCALNAME;
import static org.xdef.impl.code.CodeTable.GET_ELEMENT_NAME;
import static org.xdef.impl.code.CodeTable.GET_FRACTIONSECOND;
import static org.xdef.impl.code.CodeTable.GET_HOUR;
import static org.xdef.impl.code.CodeTable.GET_IMPLROPERTY;
import static org.xdef.impl.code.CodeTable.GET_INDEXOFSTRING;
import static org.xdef.impl.code.CodeTable.GET_ITEM;
import static org.xdef.impl.code.CodeTable.GET_LASTDAYOFMONTH;
import static org.xdef.impl.code.CodeTable.GET_LASTERROR;
import static org.xdef.impl.code.CodeTable.GET_LASTINDEXOFSTRING;
import static org.xdef.impl.code.CodeTable.GET_MESSAGE;
import static org.xdef.impl.code.CodeTable.GET_MILLIS;
import static org.xdef.impl.code.CodeTable.GET_MINUTE;
import static org.xdef.impl.code.CodeTable.GET_MONTH;
import static org.xdef.impl.code.CodeTable.GET_NAMEDITEMS;
import static org.xdef.impl.code.CodeTable.GET_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.GET_NAMED_AS_STRING;
import static org.xdef.impl.code.CodeTable.GET_NANOS;
import static org.xdef.impl.code.CodeTable.GET_NOW;
import static org.xdef.impl.code.CodeTable.GET_NS;
import static org.xdef.impl.code.CodeTable.GET_NUMOFERRORS;
import static org.xdef.impl.code.CodeTable.GET_NUMOFERRORWARNINGS;
import static org.xdef.impl.code.CodeTable.GET_OCCURRENCE;
import static org.xdef.impl.code.CodeTable.GET_PARSED_BOOLEAN;
import static org.xdef.impl.code.CodeTable.GET_PARSED_BYTES;
import static org.xdef.impl.code.CodeTable.GET_PARSED_DATETIME;
import static org.xdef.impl.code.CodeTable.GET_PARSED_DECIMAL;
import static org.xdef.impl.code.CodeTable.GET_PARSED_DOUBLE;
import static org.xdef.impl.code.CodeTable.GET_PARSED_DURATION;
import static org.xdef.impl.code.CodeTable.GET_PARSED_ERROR;
import static org.xdef.impl.code.CodeTable.GET_PARSED_LONG;
import static org.xdef.impl.code.CodeTable.GET_PARSED_STRING;
import static org.xdef.impl.code.CodeTable.GET_PARSED_VALUE;
import static org.xdef.impl.code.CodeTable.GET_QNAMEURI;
import static org.xdef.impl.code.CodeTable.GET_REGEX_GROUP;
import static org.xdef.impl.code.CodeTable.GET_REGEX_GROUP_END;
import static org.xdef.impl.code.CodeTable.GET_REGEX_GROUP_NUM;
import static org.xdef.impl.code.CodeTable.GET_REGEX_GROUP_START;
import static org.xdef.impl.code.CodeTable.GET_REGEX_RESULT;
import static org.xdef.impl.code.CodeTable.GET_REPORT;
import static org.xdef.impl.code.CodeTable.GET_RESULTSET_COUNT;
import static org.xdef.impl.code.CodeTable.GET_RESULTSET_ITEM;
import static org.xdef.impl.code.CodeTable.GET_ROOTELEMENT;
import static org.xdef.impl.code.CodeTable.GET_SECOND;
import static org.xdef.impl.code.CodeTable.GET_STRING_LENGTH;
import static org.xdef.impl.code.CodeTable.GET_STRING_TAIL;
import static org.xdef.impl.code.CodeTable.GET_SUBSTRING;
import static org.xdef.impl.code.CodeTable.GET_TEXTVALUE;
import static org.xdef.impl.code.CodeTable.GET_TYPEID;
import static org.xdef.impl.code.CodeTable.GET_TYPENAME;
import static org.xdef.impl.code.CodeTable.GET_USEROBJECT;
import static org.xdef.impl.code.CodeTable.GET_WEEKDAY;
import static org.xdef.impl.code.CodeTable.GET_XPATH;
import static org.xdef.impl.code.CodeTable.GET_XPATH_FROM_SOURCE;
import static org.xdef.impl.code.CodeTable.GET_XPOS;
import static org.xdef.impl.code.CodeTable.GET_XQUERY;
import static org.xdef.impl.code.CodeTable.GET_YEAR;
import static org.xdef.impl.code.CodeTable.GET_ZONEID;
import static org.xdef.impl.code.CodeTable.GET_ZONEOFFSET;
import static org.xdef.impl.code.CodeTable.GPS_ALTITUDE;
import static org.xdef.impl.code.CodeTable.GPS_DISTANCETO;
import static org.xdef.impl.code.CodeTable.GPS_LATITUDE;
import static org.xdef.impl.code.CodeTable.GPS_LONGITUDE;
import static org.xdef.impl.code.CodeTable.GPS_NAME;
import static org.xdef.impl.code.CodeTable.HAS_ATTR;
import static org.xdef.impl.code.CodeTable.HAS_DBITEM;
import static org.xdef.impl.code.CodeTable.HAS_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.HAS_RESULTSET_ITEM;
import static org.xdef.impl.code.CodeTable.HAS_RESULTSET_NEXT;
import static org.xdef.impl.code.CodeTable.IS_CREATEMODE;
import static org.xdef.impl.code.CodeTable.IS_DATETIME;
import static org.xdef.impl.code.CodeTable.IS_EMPTY;
import static org.xdef.impl.code.CodeTable.IS_NUM;
import static org.xdef.impl.code.CodeTable.LAST_CODE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.LOWERCASE;
import static org.xdef.impl.code.CodeTable.MATCHES_REGEX;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_GET;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_NAME;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_SET;
import static org.xdef.impl.code.CodeTable.NEW_BNFGRAMAR;
import static org.xdef.impl.code.CodeTable.NEW_BYTES;
import static org.xdef.impl.code.CodeTable.NEW_CONTAINER;
import static org.xdef.impl.code.CodeTable.NEW_CURRENCY;
import static org.xdef.impl.code.CodeTable.NEW_ELEMENT;
import static org.xdef.impl.code.CodeTable.NEW_EMAIL;
import static org.xdef.impl.code.CodeTable.NEW_EXCEPTION;
import static org.xdef.impl.code.CodeTable.NEW_GPSPOSITION;
import static org.xdef.impl.code.CodeTable.NEW_INSTREAM;
import static org.xdef.impl.code.CodeTable.NEW_IPADDR;
import static org.xdef.impl.code.CodeTable.NEW_LOCALE;
import static org.xdef.impl.code.CodeTable.NEW_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.NEW_OUTSTREAM;
import static org.xdef.impl.code.CodeTable.NEW_PARSERESULT;
import static org.xdef.impl.code.CodeTable.NEW_PRICE;
import static org.xdef.impl.code.CodeTable.NEW_REPORT;
import static org.xdef.impl.code.CodeTable.NEW_SERVICE;
import static org.xdef.impl.code.CodeTable.NEW_TELEPHONE;
import static org.xdef.impl.code.CodeTable.NEW_URI;
import static org.xdef.impl.code.CodeTable.NEW_XMLWRITER;
import static org.xdef.impl.code.CodeTable.OUT1_STREAM;
import static org.xdef.impl.code.CodeTable.OUTLN1_STREAM;
import static org.xdef.impl.code.CodeTable.OUTLN_STREAM;
import static org.xdef.impl.code.CodeTable.OUT_STREAM;
import static org.xdef.impl.code.CodeTable.PARSERESULT_MATCH;
import static org.xdef.impl.code.CodeTable.PARSE_DATE;
import static org.xdef.impl.code.CodeTable.PARSE_DURATION;
import static org.xdef.impl.code.CodeTable.PARSE_FLOAT;
import static org.xdef.impl.code.CodeTable.PARSE_INT;
import static org.xdef.impl.code.CodeTable.PARSE_OP;
import static org.xdef.impl.code.CodeTable.PARSE_XML;
import static org.xdef.impl.code.CodeTable.PRICE_AMOUNT;
import static org.xdef.impl.code.CodeTable.PRICE_CURRENCY;
import static org.xdef.impl.code.CodeTable.PRICE_DISPLAY;
import static org.xdef.impl.code.CodeTable.PRICE_FRACTDIGITS;
import static org.xdef.impl.code.CodeTable.PRINTF_STREAM;
import static org.xdef.impl.code.CodeTable.PUT_ERROR;
import static org.xdef.impl.code.CodeTable.PUT_ERROR1;
import static org.xdef.impl.code.CodeTable.PUT_REPORT;
import static org.xdef.impl.code.CodeTable.REMOVE_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.REMOVE_TEXT;
import static org.xdef.impl.code.CodeTable.REPLACEFIRST_S;
import static org.xdef.impl.code.CodeTable.REPLACE_S;
import static org.xdef.impl.code.CodeTable.REPORT_GETPARAM;
import static org.xdef.impl.code.CodeTable.REPORT_SETPARAM;
import static org.xdef.impl.code.CodeTable.REPORT_SETTYPE;
import static org.xdef.impl.code.CodeTable.REPORT_TOSTRING;
import static org.xdef.impl.code.CodeTable.RESULTSET_NEXT;
import static org.xdef.impl.code.CodeTable.SET_ATTR;
import static org.xdef.impl.code.CodeTable.SET_DAY;
import static org.xdef.impl.code.CodeTable.SET_DAYTIMEMILLIS;
import static org.xdef.impl.code.CodeTable.SET_ELEMENT;
import static org.xdef.impl.code.CodeTable.SET_FRACTIONSECOND;
import static org.xdef.impl.code.CodeTable.SET_HOUR;
import static org.xdef.impl.code.CodeTable.SET_MILLIS;
import static org.xdef.impl.code.CodeTable.SET_MINUTE;
import static org.xdef.impl.code.CodeTable.SET_MONTH;
import static org.xdef.impl.code.CodeTable.SET_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.SET_NANOS;
import static org.xdef.impl.code.CodeTable.SET_PARSED_ERROR;
import static org.xdef.impl.code.CodeTable.SET_PARSED_STRING;
import static org.xdef.impl.code.CodeTable.SET_PARSED_VALUE;
import static org.xdef.impl.code.CodeTable.SET_SECOND;
import static org.xdef.impl.code.CodeTable.SET_TEXT;
import static org.xdef.impl.code.CodeTable.SET_USEROBJECT;
import static org.xdef.impl.code.CodeTable.SET_XMLWRITER_INDENTING;
import static org.xdef.impl.code.CodeTable.SET_YEAR;
import static org.xdef.impl.code.CodeTable.SET_ZONEID;
import static org.xdef.impl.code.CodeTable.SET_ZONEOFFSET;
import static org.xdef.impl.code.CodeTable.STARTSWITH;
import static org.xdef.impl.code.CodeTable.STARTSWITHI;
import static org.xdef.impl.code.CodeTable.STREAM_EOF;
import static org.xdef.impl.code.CodeTable.STREAM_READLN;
import static org.xdef.impl.code.CodeTable.TO_MILLIS;
import static org.xdef.impl.code.CodeTable.TO_STRING;
import static org.xdef.impl.code.CodeTable.TRANSLATE_S;
import static org.xdef.impl.code.CodeTable.TRIM_S;
import static org.xdef.impl.code.CodeTable.UNIQUESET_BIND;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHEKUNREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKIDS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CLOSE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_GET_ACTUAL_KEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREFS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_RESET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_NEWKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_SIZE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_TOCONTAINER;
import static org.xdef.impl.code.CodeTable.UPPERCASE;
import static org.xdef.impl.code.CodeTable.WHITESPACES_S;
import static org.xdef.impl.code.CodeTable.WRITE_ELEMENT;
import static org.xdef.impl.code.CodeTable.WRITE_ELEMENT_END;
import static org.xdef.impl.code.CodeTable.WRITE_ELEMENT_START;
import static org.xdef.impl.code.CodeTable.WRITE_TEXTNODE;

/** Provides the implemented methods.
 * Declared literals and declared objects
 * see GenCode, GenCodeObj, CompileExpression etc.
 * @author  Vaclav Trojan
 */
public class CompileBase implements CodeTable, XDValueID {
	////////////////////////////////////////////////////////////////////////////
	//Compilation modes (context where code can be executed)
	////////////////////////////////////////////////////////////////////////////
	/** No mode */
	static final byte NO_MODE = 0;
	/** Methods associated with value of attributes or text nodes. */
	static final byte TEXT_MODE = 1;
	/** Compilation of actions associated with elements. */
	static final byte ELEM_MODE = 2;
	/** Compilation of actions in declaration section. */
	static final byte GLOBAL_MODE = 4;
	/** All modes. */
	static final byte ANY_MODE = 127;
	////////////////////////////////////////////////////////////////////////////
	// Dummy codes, used by compiler
	////////////////////////////////////////////////////////////////////////////
	/** Undefined code - used by compiler. */
	static final short UNDEF_CODE = LAST_CODE + 1;
	////////////////////////////////////////////////////////////////////////////
	//Value types
	////////////////////////////////////////////////////////////////////////////
	/** Array of classes corresponding to implemented types. */
	private final static String[] TYPENAMES = new String[X_NOTYPE_VALUE];
	/** Table of type names and type IDs.*/
	private static final String TYPEIDS;
	/** Table of names used only in declaration of external objects. */
	private static final String EXT_TYPEIDS;
	/** Array of classes corresponding to implemented types. */
	private final static Class<?>[] TYPECLASSES = new Class<?>[X_NOTYPE_VALUE];
	/** Table of internal methods.*/
	private static final List<Map<String, InternalMethod>> METHODS = new ArrayList<>(X_NOTYPE_VALUE + 1);
	/** List of predefined parsers*/
	private static final Map<String, Constructor<?>> PARSERS = new LinkedHashMap<>();

////////////////////////////////////////////////////////////////////////////////
// Initialization.
////////////////////////////////////////////////////////////////////////////////
	/** Set type parameters.
	 * @param type type ID (see org.xdef.XDValueID).
	 * @param name the name of type used in script.
	 * @param clazz the class rep[resenting the type.
	 * @param typeCodeAbbr the abbreviation used in code display.
	 */
	private static void setType(final short type, final String name, final Class<?> clazz) {
		TYPECLASSES[type] = clazz;
		TYPENAMES[type] = name;
	}

	static {
		for (int i = 0; i < X_NOTYPE_VALUE + 1; i++) METHODS.add(null);
		// Set type tables.
		setType(XD_VOID, "void", Void.TYPE);
		setType(XD_BYTE, "int", Long.TYPE); // internally int
		setType(XD_INT, "int", Long.TYPE);
		setType(XD_SHORT, "int", Long.TYPE); // internally int
		setType(XD_LONG, "int", Long.TYPE); // internally int
		setType(XD_CHAR, "char", Character.TYPE);
		setType(XD_FLOAT, "float", Double.TYPE);
		setType(XD_DOUBLE, "float", Double.TYPE); // internally float
		setType(XD_BOOLEAN, "boolean", Boolean.TYPE);
		setType(XD_DECIMAL, "Decimal", java.math.BigDecimal.class);
		setType(XD_BIGINTEGER, "BigInteger", java.math.BigInteger.class);
		setType(XD_STRING, "String", String.class);
		setType(XD_DATETIME, "Datetime", org.xdef.sys.SDatetime.class);
		setType(XD_DURATION, "Duration", org.xdef.sys.SDuration.class);
		setType(XD_BYTES, "Bytes", byte[].class);
		setType(XD_CURRENCY, "Currency", java.util.Currency.class);
		setType(XD_GPSPOSITION, "GPSPosition", org.xdef.XDGPSPosition.class);
		setType(XD_PRICE, "Price",org.xdef.XDPrice.class);
		setType(XD_ANYURI, "URI", java.net.URI.class);
		setType(XD_EMAIL, "EmailAddr", org.xdef.XDEmailAddr.class);
		setType(XD_IPADDR, "IPAddr", java.net.InetAddress.class);
		setType(XD_TELEPHONE, "Telephone", org.xdef.XDTelephone.class);
		setType(XD_CONTAINER, "Container", org.xdef.XDContainer.class);
		setType(XD_REGEX, "Regex", org.xdef.XDRegex.class);
		setType(XD_REGEXRESULT, "RegexResult", org.xdef.XDRegexResult.class);
		setType(XD_BNFGRAMMAR, "BNFGrammar", org.xdef.XDBNFGrammar.class);
		setType(XD_BNFRULE, "BNFRule", org.xdef.XDBNFRule.class);
		setType(XD_INPUT, "Input", org.xdef.XDInput.class);
		setType(XD_OUTPUT, "Output", org.xdef.XDOutput.class);
		setType(XX_ELEMENT, "", org.xdef.proc.XXElement.class);
		setType(XX_DATA, "", org.xdef.proc.XXData.class);
		setType(XD_ELEMENT, "Element", org.w3c.dom.Element.class);
		setType(XD_EXCEPTION, "Exception", org.xdef.XDException.class);
		setType(XD_REPORT, "Report", org.xdef.sys.Report.class);
		setType(XD_XPATH, "XpathExpr", org.xdef.xml.KXpathExpr.class);
		setType(XD_XQUERY, "XqueryExpr", org.xdef.XDXQueryExpr.class);
		setType(XD_PARSER, "Parser", org.xdef.XDParser.class);
		setType(XD_PARSERESULT, "ParseResult", org.xdef.XDParseResult.class);
		setType(XD_SERVICE, "Service", org.xdef.XDService.class);
		setType(XD_STATEMENT, "Statement", org.xdef.XDStatement.class);
		setType(XD_RESULTSET, "ResultSet", org.xdef.XDResultSet.class);
		setType(XM_MODEL, "XModel", null);
		setType(XD_NAMEDVALUE, "NamedValue", org.xdef.XDNamedValue.class);
		setType(XD_XMLWRITER, "XmlOutStream", org.xdef.XDXmlOutStream.class);
		setType(XD_LOCALE, "Locale", Locale.class);
		setType(XD_UNIQUESET_KEY, "uniqueSetKey",org.xdef.XDUniqueSetKey.class);
		setType(XD_ANY, "AnyValue", org.xdef.XDValue.class);
		setType(XD_OBJECT, "Object", java.lang.Object.class);
		setType(X_UNIQUESET_M, "uniqueSet", null);
		// Table of type names and typeIds
		TYPEIDS = ((char) XD_VOID) + ";void;" +
			((char) XD_LONG) + ";int;" +
			((char) XD_CHAR) + ";char;" +
			((char) XD_DOUBLE) + ";float;" +
			((char) XD_BOOLEAN) + ";boolean;" +
			((char) XD_DECIMAL) + ";BigDecimal;" +
			((char) XD_BIGINTEGER) + ";BigInteger;" +
			((char) XD_STRING) + ";String;" +
			((char) XD_DATETIME) + ";SDatetime;" +
			((char) XD_DURATION) + ";SDuration;" +
			((char) XD_BYTES) + ";byte[];" +
			((char) XD_REGEX) + ";Pattern;" +
			((char) XD_REGEXRESULT) + ";Matcher;" +
			((char) XD_DURATION) + ";SDuration;" +
			((char) XD_CONTAINER) + ";XDContainer;" +
			((char) XD_GPSPOSITION) + ";GPSPosition;" +
			((char) XD_PRICE) + ";Price;" +
			((char) XD_ANYURI) + ";URI;" +
			((char) XD_CURRENCY) + ";Currency;" +
			((char) XD_EMAIL) + ";EmailAddr;" +
			((char) XD_IPADDR) + ";IPAddr;" +
			((char) XD_TELEPHONE) + ";Telephone;" +
			((char) XD_BNFGRAMMAR) + ";DefBNFGrammar;" +
			((char) XD_LOCALE) + ";Locale;" +
			((char) XD_UNIQUESET_KEY) + ";uniqueSetKey;" +
			((char) XD_ANY) + ";XDValue;" +

			((char) XD_ELEMENT) + ";Element;" +
			((char) XD_INPUT) + ";XDInput;" +
			((char) XD_OUTPUT) + ";XDOutput;" +
			((char) XD_REPORT) + ";Report;" +
			((char) XD_XPATH) + ";KXpathExpr;" +
				(XDFactory.isXQuerySupported() ? ((char) XD_XQUERY) + ";KXqueryExpr;" : "") +
			((char) XD_PARSER) + ";XDParser;" +
			((char) XD_PARSERESULT) + ";XDParseResult;" +
			((char) XD_SERVICE) + ";XDService;" +
			((char) XD_STATEMENT) + ";XDStatement;" +
			((char) XD_RESULTSET) + ";XDResultSet;" +
			((char) XD_NAMEDVALUE) + ";XDNamedItem;" +
			((char) XD_XMLWRITER) + ";XDXmlOutStream;";
		// Table of type names used only in an external object declatation.
		EXT_TYPEIDS =
			((char) XD_DOUBLE) + ";Double;" +
			((char) XD_DOUBLE) + ";Float;" +
			((char) XD_DOUBLE) + ";Double;" +
			((char) XD_DOUBLE) + ";double;" +
			((char) XD_DOUBLE) + ";Float;" +
			((char) XD_LONG) + ";long;" +
			((char) XD_LONG) + ";Long;" +
			((char) XD_LONG) + ";int;" +
			((char) XD_LONG) + ";Integer;" +
			((char) XD_LONG) + ";Short;" +
			((char) XD_LONG) + ";short;" +
			((char) XD_LONG) + ";byte;" +
			((char) XD_LONG) + ";Byte;" +
			((char) XD_BOOLEAN) + ";Boolean;" +
			((char) XD_CHAR) + ";Character;" +
			((char) XX_ELEMENT) + ";XXNode;" + //???
			((char) XX_ELEMENT) + ";XXElement;" +
			((char) XX_ATTR) + ";XXAttr;" +
			((char) XX_DATA) + ";XXData;";
///////////////////////////////////////////////////////////////////////////////
//  parsers
///////////////////////////////////////////////////////////////////////////////
		KeyParam[] keyPars = new KeyParam[] {
			keyParam("pattern", XD_STRING, false, -1, false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse"))
		};
		InternalMethod im = genParserMetnod(0, 0, null, XD_BOOLEAN, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseFalse.class, "false_parser");
		parser(im, org.xdef.impl.parsers.XDParseTrue.class, "true_parser");
		parser(im, org.xdef.impl.parsers.XSParseBoolean.class, "boolean", "?xs:boolean", "?bool");
		parser(im, org.xdef.impl.parsers.XDParseJBoolean.class,"jboolean");
		im = genParserMetnod(0, 0, null, XD_NULL, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseJNull.class, "jnull");
		im = genParserMetnod(0, 0, null, XD_GPSPOSITION, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseGPS.class, "gps");
		im = genParserMetnod(0, 0, null, XD_PRICE, keyPars);
		parser(im, org.xdef.impl.parsers.XDParsePrice.class, "price");
		im = genParserMetnod(0, 0, null, XD_IPADDR, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseIPAddr.class, "ipAddr");
		im = genParserMetnod(0, 0, null, XD_TELEPHONE, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseTelephone.class, "telephone");
		im = genParserMetnod(0, 0, null, XD_CURRENCY, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseCurrency.class, "currency");
		im = genParserMetnod(0, 0, null, XD_CHAR, keyPars);
		parser(im, org.xdef.impl.parsers.XDParseChar.class, "char");
		im = genParserMetnod(0, 2, new short[] {XD_DECIMAL, XD_DECIMAL}, XD_DECIMAL,
			keyParam("base", XD_STRING, true,-1,false),
			keyParam("enumeration", XD_DECIMAL, true,-1,false),
			keyParam("fractionDigits", XD_LONG,false,-1,false),
			keyParam("maxExclusive", XD_DECIMAL,false,-1,false),
			keyParam("maxInclusive", XD_DECIMAL,false,1,false),
			keyParam("minExclusive", XD_DECIMAL,false,-1,false),
			keyParam("minInclusive", XD_DECIMAL,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_LONG,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDecimal.class, "decimal", "?xs:decimal");
		im = genParserMetnod(0, 2, new short[] {XD_DOUBLE, XD_DOUBLE}, XD_DOUBLE,
			keyParam("base", XD_STRING, true, -1, false),
			keyParam("enumeration", XD_DOUBLE, false, -1, false),
			keyParam("fractionDigits", XD_LONG,false, -1, false),
			keyParam("maxExclusive", XD_DOUBLE, false,-1,false),
			keyParam("maxInclusive", XD_DOUBLE, false,1,false),
			keyParam("minExclusive", XD_DOUBLE, false,-1,false),
			keyParam("minInclusive", XD_DOUBLE, false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_LONG,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDouble.class, "double", "?xs:double");
		parser(im,org.xdef.impl.parsers.XSParseFloat.class,"float","?xs:float");
		im = genParserMetnod(0, 2, new short[] {XD_DATETIME, XD_DATETIME}, XD_DATETIME,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_DATETIME, true,-1,false),
			keyParam("maxExclusive",XD_DATETIME,false,-1,false),
			keyParam("maxInclusive",XD_DATETIME,false,1,false),
			keyParam("minExclusive",XD_DATETIME,false,-1,false),
			keyParam("minInclusive",XD_DATETIME,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDatetime.class, "dateTime", "?xs:dateTime");
		parser(im, org.xdef.impl.parsers.XSParseDate.class, "date", "?xs:date");
		parser(im, org.xdef.impl.parsers.XSParseGDay.class, "gDay", "?xs:gDay"); //"?ISOday"
		parser(im, org.xdef.impl.parsers.XSParseGMonth.class, "gMonth", "?xs:gMonth");//"?ISOmonth"
		parser(im, org.xdef.impl.parsers.XSParseGMonthDay.class, "gMonthDay", "?xs:gMonthDay");
		parser(im, org.xdef.impl.parsers.XSParseGYear.class, "gYear", "?xs:gYear"); //"?ISOYear"
		parser(im, org.xdef.impl.parsers.XSParseGYearMonth.class, "gYearMonth", "?xs:gYearMonth");
		parser(im, org.xdef.impl.parsers.XSParseTime.class, "time", "?xs:time", "?ISOtime");
		parser(im, org.xdef.impl.parsers.XDParseDateYMDhms.class, "dateYMDhms");
		parser(im, org.xdef.impl.parsers.XDParseEmailDate.class, "emailDate");
		parser(im, org.xdef.impl.parsers.XDParsePrintableDate.class, "printableDate");
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_LONG,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_LONG, true, -1,false),
			keyParam("maxExclusive", XD_LONG,false, -1,false),
			keyParam("maxInclusive", XD_LONG,false, 1,false),
			keyParam("minExclusive", XD_LONG,false, -1,false),
			keyParam("minInclusive", XD_LONG,false, 0,false),
			keyParam("pattern",XD_STRING,true, -1,false),
			keyParam("totalDigits", XD_LONG,false, -1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseByte.class, "byte", "?xs:byte");
		parser(im, org.xdef.impl.parsers.XSParseInt.class, "int", "?xs:int");
		parser(im, org.xdef.impl.parsers.XSParseLong.class, "long", "?xs:long");
		parser(im, org.xdef.impl.parsers.XSParseShort.class, "short", "?xs:short");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedByte.class,
			"unsignedByte","?xs:unsignedByte");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedInt.class,"unsignedInt","?xs:unsignedInt");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedShort.class,
			"unsignedShort", "?xs:unsignedShort");
		im = genParserMetnod(0, 2, new short[] {XD_BIGINTEGER, XD_BIGINTEGER}, XD_BIGINTEGER,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_BIGINTEGER, true, -1,false),
			keyParam("maxExclusive", XD_BIGINTEGER,false,-1,false),
			keyParam("maxInclusive", XD_BIGINTEGER,false,1,false),
			keyParam("minExclusive", XD_BIGINTEGER,false,-1,false),
			keyParam("minInclusive", XD_BIGINTEGER,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_LONG,false,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseInteger.class, "integer", "?xs:integer");
		parser(im, org.xdef.impl.parsers.XSParseUnsignedLong.class, "unsignedLong", "?xs:unsignedLong");
		parser(im, org.xdef.impl.parsers.XSParseNegativeInteger.class,
			"negativeInteger", "?xs:negativeInteger");
		parser(im, org.xdef.impl.parsers.XSParseNonNegativeInteger.class,
			"nonNegativeInteger", "?xs:nonNegativeInteger");
		parser(im, org.xdef.impl.parsers.XSParseNonPositiveInteger.class,
			"nonPositiveInteger", "?xs:nonPositiveInteger");
		parser(im, org.xdef.impl.parsers.XSParsePositiveInteger.class,
			"positiveInteger", "?xs:positiveInteger");
		keyPars = new KeyParam[] {
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_LONG, false,  -1, false),
			keyParam("maxLength", XD_LONG, false,  1, false),
			keyParam("minLength", XD_LONG, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse"))};
		im = genParserMetnod(0, 2, new short[] {XD_LONG,XD_LONG}, XD_ANYURI, keyPars);
		parser(im, org.xdef.impl.parsers.XSParseAnyURI.class, "anyURI", "?xs:anyURI");
		parser(im, org.xdef.impl.parsers.XSParseName.class, "Name", "?xs:Name");
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_STRING, keyPars);
		parser(im, org.xdef.impl.parsers.XSParseID.class, "ID", "?xs:ID");
		parser(im, org.xdef.impl.parsers.XSParseIDREF.class, "IDREF", "?xs:IDREF");
		parser(im, org.xdef.impl.parsers.XSParseENTITY.class, "ENTITY", "?xs:ENTITY");
		parser(im, org.xdef.impl.parsers.XSParseNMTOKEN.class, "NMTOKEN", "?xs:NMTOKEN");
		parser(im, org.xdef.impl.parsers.XSParseToken.class, "token", "?xs:token");
		parser(im, org.xdef.impl.parsers.XDParseCHKID.class, "CHKID");
		parser(im, org.xdef.impl.parsers.XDParseSET.class, "SET");
		parser(im, org.xdef.impl.parsers.XDParseCHKIDS.class, "CHKIDS");
		parser(im, org.xdef.impl.parsers.XSParseNOTATION.class, "NOTATION", "?xs:NOTATION");
		parser(im, org.xdef.impl.parsers.XSParseNCName.class, "NCName", "?xs:NCName"); //"?NCname"
		parser(im, org.xdef.impl.parsers.XSParseQName.class, "QName", "?xs:QName", "?Qname");
		parser(im, org.xdef.impl.parsers.XSParseLanguage.class, "language", "?xs:language");
		parser(im, org.xdef.impl.parsers.XDParseCountry.class, "country");
		im = genParserMetnod(0, 2, new short[] {XD_LONG,XD_LONG}, XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, 1,false),
			keyParam("minLength", XD_LONG, false, 0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, false,
				new DefString("preserve"), new DefString("collapse"), new DefString("replace")));
		parser(im, org.xdef.impl.parsers.XSParseString.class, "string", "?xs:string");
		im = genParserMetnod(0, 2, new short[] {XD_LONG,XD_LONG}, XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_LONG, false,  -1, false),
			keyParam("maxLength", XD_LONG, false,  1, false),
			keyParam("minLength", XD_LONG, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false,  -1, false,
				new DefString("replace"), new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseNormalizedString.class,
			"normalizedString", "?xs:normalizedString"); //"?normString"
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_STRING,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, 1,false),
			keyParam("minLength", XD_LONG, false, 0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false,  -1, false,
				new DefString("preserve"), new DefString("collapse"), new DefString("replace")));
		parser(im, org.xdef.impl.parsers.XDParseLetters.class, "letters");
		parser(im, org.xdef.impl.parsers.XDParseAn.class, "an");
		parser(im, org.xdef.impl.parsers.XDParseNum.class, "num");
		parser(im, org.xdef.impl.parsers.XDParseCDATA.class, "CDATA");
		parser(im, org.xdef.impl.parsers.XDParseJNumber.class, "jnumber");
		parser(im, org.xdef.impl.parsers.XDParseJString.class, "jstring");
		im = genParserMetnod(0, 0, null, XD_ANY,
			keyParam("enumeration", XD_ANY, true, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false));
		parser(im, org.xdef.impl.parsers.XDParseJValue.class, "jvalue");
		im = genParserMetnod(0, 0, null, XD_ANY,
			keyParam("enumeration", XD_ANY, true, -1,false),
			keyParam("item", XD_PARSER, true, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false));
		parser(im, org.xdef.impl.parsers.XSParseUnion.class, "union", "?xs:union");
		im = genParserMetnod(0, 1, new short[] {XD_PARSER}, XD_CONTAINER,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("item", XD_PARSER, false, 0,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, -1,false),
			keyParam("minLength", XD_LONG, false, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseList.class, "list", "?xs:list");
		im = genParserMetnod(0, 2, new short[] {XD_ANY, XD_ANY, XD_PARSER}, XD_CONTAINER,
			keyParam("item", XD_PARSER, false, 2,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, 1,false),
			keyParam("minLength", XD_LONG, false, 0,false),
			keyParam("enumeration", XD_ANY, true, -1,false),
			keyParam("pattern", XD_STRING, true, -1,false));
		parser(im, org.xdef.impl.parsers.XDParseJList.class, "jlist");
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_CONTAINER,
			keyParam("enumeration", XD_STRING, true, -1, false),
			keyParam("length", XD_LONG, false,  -1, false),
			keyParam("maxLength", XD_LONG, false,  1, false),
			keyParam("minLength", XD_LONG, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseIDREFS.class, "IDREFS", "?xs:IDREFS");
		parser(im, org.xdef.impl.parsers.XSParseENTITIES.class, "ENTITIES", "?xs:ENTITIES");
		parser(im, org.xdef.impl.parsers.XSParseNMTOKENS.class, "NMTOKENS", "?xs:NMTOKENS");
		parser(im, org.xdef.impl.parsers.XDParseCHKIDS.class, "CHKIDS");
		im = genParserMetnod(0, 1, new short[]{XD_ELEMENT}, XD_CONTAINER,
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, -1,false),
			keyParam("minLength", XD_LONG, false, -1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("separator", XD_STRING, true, 0,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseNCNameList.class, "NCNameList", "?NCnameList");
		parser(im, org.xdef.impl.parsers.XDParseQNameList.class, "QNameList", "?QnameList");
		parser(im, org.xdef.impl.parsers.XDParseLanguages.class, "languages", "?ISOlanguages");
		parser(im, org.xdef.impl.parsers.XDParseCountries.class, "countries");
		im = genParserMetnod(0, 1, new short[] {XD_STRING}, XD_CONTAINER,
			keyParam("argument", XD_ANY, true, 1,false),
				keyParam("enumeration", XD_STRING, true, -1,false),
				keyParam("length", XD_LONG, false, -1,false),
				keyParam("maxLength", XD_LONG, false, -1,false),
				keyParam("minLength", XD_LONG, false, -1,false),
				keyParam("pattern",XD_STRING,true,-1,false),
				keyParam("separator", XD_STRING, true,  0, false),
				keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseQNameURIList.class,"QNameURIList", "?QnameListURI");
		im = genParserMetnod(0, 2, new short[] {XD_DURATION,XD_DURATION}, XD_DURATION,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_DURATION, true,-1,false),
			keyParam("maxExclusive",XD_DURATION,false,-1,false),
			keyParam("maxInclusive",XD_DURATION,false,1,false),
			keyParam("minExclusive",XD_DURATION,false,-1,false),
			keyParam("minInclusive",XD_DURATION,false,0,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseDuration.class, "duration", "?xs:duration");
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_BYTES,
			keyParam("base", XD_STRING, true, -1,false),
			keyParam("enumeration", XD_BYTES, true,  -1, false),
			keyParam("length", XD_LONG, false,  -1, false),
			keyParam("maxLength", XD_LONG, false,  1, false),
			keyParam("minLength", XD_LONG, false,  0, false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XSParseBase64Binary.class,
			"base64Binary", "?xs:base64Binary", "?base64");
		parser(im, org.xdef.impl.parsers.XSParseHexBinary.class, "hexBinary", "?xs:hexBinary");
		parser(im, org.xdef.impl.parsers.XDParseHex.class, "hex");
////////////////////////////////////////////////////////////////////////////////
// X-script parsers
////////////////////////////////////////////////////////////////////////////////
		im = genParserMetnod(0, 2, new short[] {XD_LONG, XD_LONG}, XD_STRING,
			keyParam("pattern", XD_ANY, true,  -1, false),
			keyParam("enumeration", XD_ANY, true,  -1, false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")),
			keyParam("item", XD_PARSER, true,  -1,false),
			keyParam("length", XD_LONG, false,  -1, false),
			keyParam("maxLength", XD_LONG, false,  1, false),
			keyParam("minLength", XD_LONG, false,  0, false),
			keyParam("pattern", XD_STRING, true,  -1, false),
			keyParam("separator", XD_STRING, true, 0,false));
		parser(im, org.xdef.impl.parsers.XDParseSequence.class, "sequence"); //"?parseSequence"
		im = genParserMetnod(0, 2, new short[]{XD_LONG, XD_LONG}, XD_DECIMAL,
			keyParam("enumeration", XD_DECIMAL, true, -1,false),
			keyParam("fractionDigits", XD_LONG,false,1,false),
			keyParam("maxExclusive", XD_DECIMAL,false,-1,false),
			keyParam("maxInclusive", XD_DECIMAL,false,-1,false),
			keyParam("minExclusive", XD_DECIMAL,false,-1,false),
			keyParam("minInclusive", XD_DECIMAL,false,-1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("totalDigits", XD_LONG,false,0,false));
		parser(im, org.xdef.impl.parsers.XDParseDec.class, "dec");
		im = genParserMetnod(0, 0, null, XD_STRING,
			keyParam("enumeration", XD_BYTES, true, -1,false),
			keyParam("length", XD_LONG, false, -1, true, new DefLong(16))); /*fixed*/
		parser(im, org.xdef.impl.parsers.XDParseMD5.class, "MD5");
		im = genParserMetnod(0, 0, null, XD_STRING,
			keyParam("enumeration", XD_BYTES, true, -1,false),
			keyParam("length", XD_LONG, false, -1, true, new DefLong(20))); /*fixed*/
		parser(im, org.xdef.impl.parsers.XDParseSHA1.class, "SHA1");
		im = genParserMetnod(0, 1, null, XD_STRING,
			keyParam("argument", XD_ANY, true, 0,false),
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, -1,false),
			keyParam("minLength", XD_LONG, false, -1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")));
		parser(im, org.xdef.impl.parsers.XDParseQNameURI.class, "QNameURI", "?QnameURI");
		im = genParserMetnod(1, 2, new short[] {XD_STRING, XD_STRING}, XD_DATETIME,
			keyParam("enumeration", XD_STRING, true, -1,false),
			keyParam("length", XD_LONG, false, -1,false),
			keyParam("maxLength", XD_LONG, false, -1,false),
			keyParam("minLength", XD_LONG, false, -1,false),
			keyParam("pattern",XD_STRING,true,-1,false),
			keyParam("whiteSpace", XD_STRING, false, -1, true, new DefString("collapse")),
			keyParam("format", XD_STRING, true, 0,false),
			keyParam("outFormat", XD_STRING, true, 1,false));
		parser(im, org.xdef.impl.parsers.XDParseXDatetime.class, "xdatetime", "?datetime");
		parser(im, org.xdef.impl.parsers.XDParseYDatetime.class, "ydatetime");
		im = genParserMetnod(0, 0, null, XD_STRING);
		parser(im, org.xdef.impl.parsers.XDParseEmpty.class, "empty");
		im = genParserMetnod(1, 1, new short[] {XD_STRING}, XD_STRING,
			keyParam("argument", XD_STRING, false,  0, false));
		parser(im, org.xdef.impl.parsers.XDParseEq.class, "eq");
		parser(im, org.xdef.impl.parsers.XDParseEqi.class, "eqi");
		parser(im, org.xdef.impl.parsers.XDParseStarts.class, "starts");
		parser(im, org.xdef.impl.parsers.XDParseStartsi.class, "startsi");
		parser(im, org.xdef.impl.parsers.XDParseEnds.class, "ends");
		parser(im, org.xdef.impl.parsers.XDParseEndsi.class, "endsi");
		parser(im, org.xdef.impl.parsers.XDParseContains.class,"contains");
		parser(im, org.xdef.impl.parsers.XDParseContainsi.class, "containsi");
		parser(im, org.xdef.impl.parsers.XDParsePic.class, "pic");
		parser(im, org.xdef.impl.parsers.XDParseRegex.class, "regex");
		im = genParserMetnod(1, Integer.MAX_VALUE, new short[] {XD_STRING}, XD_STRING,
			keyParam("argument", XD_CONTAINER, true,  0, false));
		parser(im, org.xdef.impl.parsers.XDParseEnum.class, "enum");
		parser(im, org.xdef.impl.parsers.XDParseEnumi.class, "enumi");
		im = genParserMetnod(1, 2, new short[] {XD_ANY, XD_STRING}, XD_STRING,
			keyParam("a2", XD_ANY, true, 1, false),
			keyParam("a1", XD_ANY, true, 0, false));
		parser(im, org.xdef.impl.parsers.XDParseBNF.class, "BNF");
		im = genParserMetnod(0, 0, new short[] {XD_PARSER}, XD_STRING);
		parser(im,org.xdef.impl.parsers.XDParseEmailAddr.class, "emailAddr", "?email");
		parser(im,org.xdef.impl.parsers.XDParseEmailAddrList.class, "emailAddrList", "?emailList");
		parser(im,org.xdef.impl.parsers.XDParseFile.class, "file");
		parser(im,org.xdef.impl.parsers.XDParseFileList.class,"fileList");
		parser(im,org.xdef.impl.parsers.XDParseXDType.class, "xdType");
		parser(im,org.xdef.impl.parsers.XDParseUri.class, "uri");
		parser(im,org.xdef.impl.parsers.XDParseUriList.class, "uriList");
		parser(im,org.xdef.impl.parsers.XDParseUrl.class, "url");
		parser(im,org.xdef.impl.parsers.XDParseUrlList.class, "urlList");
		parser(im,org.xdef.impl.parsers.XDParseDomainAddr.class, "domainAddr");
////////////////////////////////////////////////////////////////////////////////
// implemented methods
////////////////////////////////////////////////////////////////////////////////
		short ti = X_NOTYPE_VALUE; // no base methods
		method(ti, genInternalMethod(UNIQUESET_BIND, XD_VOID,
			ELEM_MODE, 1, Integer.MAX_VALUE, X_UNIQUESET_M), "bindSet");
		method(ti, genInternalMethod(CLEAR_REPORTS, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 0, 0), "clearReports");
		method(ti, genInternalMethod(DEFAULT_ERROR, XD_BOOLEAN,
			(byte)(TEXT_MODE+ELEM_MODE), 0, 0), "defaultError");
		method(ti, genInternalMethod(PUT_ERROR, XD_BOOLEAN, ANY_MODE, 1, 4,
			XD_ANY, XD_STRING, XD_STRING, XD_STRING), "error");
		method(ti, genInternalMethod(GET_NUMOFERRORS, XD_LONG, (byte)(TEXT_MODE+ELEM_MODE), 0, 0), "errors");
		method(ti, genInternalMethod(GET_NUMOFERRORWARNINGS, XD_LONG,
			(byte)(TEXT_MODE+ELEM_MODE), 0, 0), "errorWarnings");
		method(ti, genInternalMethod(FORMAT_STRING, XD_STRING, ANY_MODE,1,Integer.MAX_VALUE,XD_ANY),"format");
		method(ti, genInternalMethod(GET_XPATH_FROM_SOURCE, XD_CONTAINER,
			(byte)(TEXT_MODE+ELEM_MODE), 0, 2, XD_ANY, XD_STRING), "from");
		method(ti, genInternalMethod(FROM_ELEMENT, XD_CONTAINER, ELEM_MODE, 1, 1, XD_ELEMENT), "fromElement");
		method(ti, genInternalMethod(GET_ATTR, XD_STRING,
			(byte)(TEXT_MODE+ELEM_MODE),1,2,XD_STRING,XD_STRING), "getAttr");
		method(ti, genInternalMethod(GET_ATTR_NAME, XD_STRING, TEXT_MODE, 0, 0), "getAttrName");
		method(ti, genInternalMethod(GET_DEFAULTZONE, XD_STRING, ANY_MODE, 0, 0), "getDefaultZone");
		method(ti, genInternalMethod(GET_ELEMENT, XD_ELEMENT, ANY_MODE, 0, 0), "getElement");
		method(ti, genInternalMethod(GET_ELEMENT_NAME, XD_STRING,ANY_MODE, 0, 0), "getElementName");
		method(ti, genInternalMethod(GET_ELEMENT_LOCALNAME, XD_STRING, ANY_MODE, 0, 0),"getElementLocalName");
		method(ti, genInternalMethod(ELEMENT_GETTEXT, XD_STRING,ELEM_MODE, 0, 0), "getElementText");
		method(ti, genInternalMethod(GET_IMPLROPERTY, XD_STRING,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "getImplProperty");
		method(ti, genInternalMethod(GET_ITEM, XD_STRING,
			(byte)(TEXT_MODE+ELEM_MODE),1,1,XD_STRING), "getItem", "?fromAttr");
		method(ti, genInternalMethod(GET_LASTERROR,XD_REPORT,(byte)(TEXT_MODE+ELEM_MODE),0,0),"getLastError");
		method(ti, genInternalMethod(GET_NS,XD_STRING, ANY_MODE, 0, 2, XD_ANY, XD_ELEMENT),"getNamespaceURI");
// ELEM_MODE?
		method(ti, genInternalMethod(GET_COUNTER, XD_LONG, ANY_MODE, 0, 0), "getOccurrence", "?getCounter");
		method(ti, genInternalMethod(GET_PARSED_BOOLEAN, XD_BOOLEAN, TEXT_MODE, 0, 0), "getParsedBoolean");
		method(ti, genInternalMethod(GET_PARSED_BYTES, XD_BYTES, TEXT_MODE, 0,0), "getParsedBytes");
		method(ti, genInternalMethod(GET_PARSED_DATETIME, XD_DATETIME, TEXT_MODE, 0, 0), "getParsedDatetime");
		method(ti, genInternalMethod(GET_PARSED_DECIMAL, XD_DECIMAL, TEXT_MODE, 0, 0), "getParsedDecimal");
		method(ti, genInternalMethod(GET_PARSED_DURATION, XD_DATETIME, TEXT_MODE, 0, 0), "getParsedDuration");
		method(ti, genInternalMethod(GET_PARSED_DOUBLE, XD_DOUBLE, TEXT_MODE, 0, 0), "getParsedFloat");
		method(ti, genInternalMethod(GET_PARSED_LONG, XD_LONG, TEXT_MODE, 0, 0), "getParsedInt");
		method(ti, genInternalMethod(GET_PARSED_RESULT, XD_PARSERESULT, TEXT_MODE, 0, 0),
			"getParseResult", "?getParsedResult");
		method(ti, genInternalMethod(GET_PARSED_VALUE, XD_ANY, TEXT_MODE, 0, 0), "getParsedValue");
		method(ti, genInternalMethod(GET_QNAMEURI, XD_STRING, ANY_MODE, 1,2,XD_ANY,XD_ELEMENT),"getQnameURI");
		method(ti, genInternalMethod(GET_ROOTELEMENT, XD_ELEMENT,ELEM_MODE, 0,1,XD_ELEMENT),"getRootElement");
		method(ti, genInternalMethod(CONTEXT_GETTEXT, XD_STRING,(byte)(TEXT_MODE+ELEM_MODE), 0, 0),"getText");
		method(ti, genInternalMethod(GET_USEROBJECT, XD_OBJECT, ANY_MODE, 0, 0), "getUserObject");
		method(ti, genInternalMethod(GET_XPOS, XD_STRING, ANY_MODE, 0, 0), "getXpos");
		method(ti, genInternalMethod(HAS_ATTR, XD_BOOLEAN,
			(byte) (TEXT_MODE+ELEM_MODE),1,2,XD_STRING,XD_STRING), "hasAttr");
		method(ti, genInternalMethod(IS_CREATEMODE, XD_BOOLEAN,
			(byte) (TEXT_MODE+ELEM_MODE), 0, 0), "isCreateMode");
		method(ti, genInternalMethod(IS_DATETIME, XD_BOOLEAN,ANY_MODE, 1,2,XD_STRING,XD_STRING),"isDatetime");
		method(ti, genInternalMethod(IS_NUM, XD_BOOLEAN, ANY_MODE, 1, 1, XD_STRING), "isNumeric");
		method(ti, genInternalMethod(CREATE_ELEMENT, XD_ELEMENT,
			ANY_MODE, 0, 2, XD_STRING, XD_STRING), "newElement");
		method(ti, genInternalMethod(CREATE_ELEMENTS, XD_CONTAINER,
			ANY_MODE, 1, 3, XD_LONG, XD_STRING, XD_STRING), "newElements");
		method(ti, genInternalMethod(GET_NOW, XD_DATETIME, ANY_MODE, 0, 0), "now");
		method(ti, genInternalMethod(GET_OCCURRENCE, XD_LONG,
			(byte) (TEXT_MODE+ELEM_MODE), 0, 0), "occurrence");
		method(ti, genInternalMethod(OUT_STREAM, XD_VOID, ANY_MODE, 1, 1, XD_STRING), "out");
		method(ti, genInternalMethod(OUTLN_STREAM, XD_VOID, ANY_MODE, 0, 1, XD_STRING), "outln");
		method(ti, genInternalMethod(PRINTF_STREAM, XD_VOID, ANY_MODE, 1, Integer.MAX_VALUE,XD_ANY),"printf");
		method(ti, genInternalMethod(PARSE_DATE, XD_DATETIME,ANY_MODE, 1,2, XD_STRING,XD_STRING),"parseDate");
		method(ti, genInternalMethod(PARSE_FLOAT, XD_DOUBLE,ANY_MODE, 1,2, XD_STRING,XD_STRING),"parseFloat");
		method(ti, genInternalMethod(PARSE_INT,XD_LONG, ANY_MODE, 1, 2, XD_STRING, XD_STRING), "parseInt");
		method(ti, genInternalMethod(DEBUG_PAUSE, XD_VOID, ANY_MODE, 0, 2, XD_ANY), "pause");
		method(ti, genInternalMethod(DEL_ATTR, XD_VOID,
			(byte) (TEXT_MODE+ELEM_MODE), 1, 2, XD_STRING,XD_STRING), "removeAttr");
		method(ti, genInternalMethod(REMOVE_TEXT, XD_VOID, TEXT_MODE, 0, 0), "removeText");
		method(ti, genInternalMethod(WHITESPACES_S, XD_STRING, ANY_MODE, 1,1, XD_STRING),"removeWhiteSpaces");
		method(ti, genInternalMethod(REPLACE_S, XD_STRING,
			ANY_MODE, 3, 3, XD_STRING, XD_STRING, XD_STRING),"replace");
		method(ti, genInternalMethod(REPLACEFIRST_S, XD_STRING,
			ANY_MODE, 3, 3, XD_STRING, XD_STRING, XD_STRING), "replaceFirst");
		method(ti, genInternalMethod(SET_ELEMENT, XD_VOID,
			(byte) (TEXT_MODE+ELEM_MODE), 1, 1, XD_ANY), "returnElement");
		method(ti, genInternalMethod(SET_ATTR,XD_VOID,
			(byte) (TEXT_MODE+ELEM_MODE), 2, 3, XD_STRING, XD_STRING, XD_STRING), "setAttr");
		method(ti, genInternalMethod(SET_ELEMENT, XD_VOID,
			(byte) (TEXT_MODE+ELEM_MODE), 1, 1, XD_ANY), "setElement");
		method(ti, genInternalMethod(SET_PARSED_VALUE, XD_VOID, TEXT_MODE, 1, 1, XD_ANY), "setParsedValue");
		method(ti, genInternalMethod(SET_TEXT, XD_VOID, TEXT_MODE, 1, 1, XD_STRING), "setText");
		method(ti, genInternalMethod(SET_USEROBJECT, XD_VOID, ANY_MODE, 1, 1, XD_OBJECT), "SetUserObject");
		method(ti, genInternalMethod(GET_STRING_TAIL, XD_STRING, ANY_MODE, 2, 2, XD_STRING,XD_LONG), "tail");
		method(ti, genInternalMethod(TO_STRING, XD_STRING, ANY_MODE, 2, 2, XD_ANY,XD_STRING), "toString");
		method(ti, genInternalMethod(DEBUG_TRACE, XD_VOID, ANY_MODE, 0, 2, XD_ANY), "trace");
		method(ti, genInternalMethod(TRANSLATE_S, XD_STRING,
			ANY_MODE, 3 ,3, XD_STRING,XD_STRING,XD_STRING), "translate");
		method(ti, genInternalMethod(COMPOSE_OP, XD_ELEMENT, ELEM_MODE, 1,2, XD_STRING,XD_ELEMENT),"xcreate");
		method(ti, genInternalMethod(PARSE_XML, XD_ELEMENT, ANY_MODE, 1, 2, XD_ANY, XD_STRING), "xparse");
		method(ti, genInternalMethod(GET_XPATH, XD_CONTAINER, ANY_MODE, 1, 2, XD_STRING,XD_ANY), "xpath");
		if (DefXQueryExpr.isXQueryImplementation()) {
			method(ti, genInternalMethod(GET_XQUERY, XD_CONTAINER,
				ANY_MODE, 1, 2, XD_STRING,XD_ANY), "xquery", "?fromXQ");
		}
////////////////////////////////////////////////////////////////////////////////
// auxiliary methods
////////////////////////////////////////////////////////////////////////////////
		method(ti, genInternalMethod(CONTEXT_GETELEMENT_X, XD_ELEMENT,
			ANY_MODE, 2, 2, XD_CONTAINER,XD_LONG), "#getElement");
		method(ti, genInternalMethod(GET_TEXTVALUE, XD_STRING, TEXT_MODE, 0, 0), "#getValue");
////////////////////////////////////////////////////////////////////////////////
// methods above all types
////////////////////////////////////////////////////////////////////////////////
		method(ti, genInternalMethod(GET_TYPENAME, XD_STRING, ANY_MODE, 1, 1, XD_ANY), "typeName");
		method(ti, genInternalMethod(GET_TYPEID, XD_LONG, ANY_MODE, 1, 1, XD_ANY), "valueType");
////////////////////////////////////////////////////////////////////////////////
// ATTR REFERENCE (reference to direct attribute of the processed element)
////////////////////////////////////////////////////////////////////////////////
//		ti = X_ATTR_REF;
////////////////////////////////////////////////////////////////////////////////
// ANY VALUE (touple name, value)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_ANY;
		method(ti, genInternalMethod(GET_TYPEID, XD_LONG, ANY_MODE, 1, 1, XD_ANY), "valueType");
		method(ti, genInternalMethod(GET_TYPENAME, XD_STRING, ANY_MODE, 1, 1, XD_ANY), "typeName");
////////////////////////////////////////////////////////////////////////////////
// ANYURI (URI identifier object)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_ANYURI;
		method(ti, genInternalMethod(NEW_URI, XD_ANYURI, ANY_MODE, 1, 1, XD_STRING), "#");
////////////////////////////////////////////////////////////////////////////////
// BNF GRAMMAR
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BNFGRAMMAR;
		method(ti, genInternalMethod(NEW_BNFGRAMAR, XD_BNFGRAMMAR, ANY_MODE,1,2,XD_STRING,XD_BNFGRAMMAR),"#");
		method(ti, genInternalMethod(BNF_PARSE, XD_PARSERESULT,
			ANY_MODE, 2, 3, XD_BNFGRAMMAR, XD_STRING, XD_STRING), "parse", "?check");
		method(ti, genInternalMethod(GET_BNFRULE, XD_BNFRULE, ANY_MODE, 2,2, XD_BNFGRAMMAR,XD_STRING),"rule");
////////////////////////////////////////////////////////////////////////////////
// BNF RULE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BNFRULE;
		method(ti, genInternalMethod(BNFRULE_PARSE, XD_PARSERESULT,
			ANY_MODE, 1, 2, XD_BNFRULE, XD_ANY), "parse", "?check");
		method(ti, genInternalMethod(BNFRULE_VALIDATE, XD_BOOLEAN,
			ANY_MODE, 1, 2, XD_BNFRULE, XD_ANY), "validate");
////////////////////////////////////////////////////////////////////////////////
// BYTES (array)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_BYTES;
		method(ti, genInternalMethod(NEW_BYTES, XD_BYTES, ANY_MODE, 0, 1, XD_LONG), "#");
		method(ti, genInternalMethod(BYTES_ADDBYTE, XD_VOID, ANY_MODE, 2, 2, XD_BYTES, XD_LONG), "add");
		method(ti, genInternalMethod(BYTES_CLEAR, XD_VOID, ANY_MODE, 1, 1, XD_BYTES), "clear");
		method(ti, genInternalMethod(BYTES_GETAT, XD_LONG, ANY_MODE, 2, 2, XD_BYTES, XD_LONG), "getAt");
		method(ti, genInternalMethod(BYTES_INSERT, XD_VOID, ANY_MODE, 3,3,XD_BYTES,XD_LONG,XD_LONG),"insert");
		method(ti, genInternalMethod(BYTES_REMOVE, XD_BYTES, ANY_MODE,2,3,XD_BYTES,XD_LONG,XD_LONG),"remove");
		method(ti, genInternalMethod(BYTES_SIZE, XD_LONG, ANY_MODE, 1, 1, XD_BYTES), "size");
		method(ti, genInternalMethod(BYTES_SETAT, XD_VOID, ANY_MODE, 3,3, XD_BYTES, XD_LONG,XD_LONG),"setAt");
		method(ti, genInternalMethod(BYTES_TO_BASE64, XD_STRING, ANY_MODE, 1, 1, XD_BYTES), "toBase64");
		method(ti, genInternalMethod(BYTES_TO_HEX, XD_STRING, ANY_MODE, 1, 1, XD_BYTES), "toHex");
////////////////////////////////////////////////////////////////////////////////
// CONTAINER (general object envelope)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_CONTAINER;
		method(ti, genInternalMethod(NEW_CONTAINER, XD_CONTAINER, ANY_MODE, 0, Integer.MAX_VALUE), "#");
		method(ti, genInternalMethod(CONTEXT_ADDITEM, XD_VOID, ANY_MODE, 2,2, XD_CONTAINER,XD_ANY),"addItem");
		method(ti, genInternalMethod(CONTEXT_GETELEMENT_X, XD_ELEMENT,
			ANY_MODE, 1, 2, XD_CONTAINER, XD_LONG), "getElement");
		method(ti, genInternalMethod(CONTEXT_GETELEMENTS, XD_CONTAINER,
			ANY_MODE, 1, 2, XD_CONTAINER, XD_STRING), "getElements");
		method(ti, genInternalMethod(CONTEXT_ITEMTYPE, XD_LONG,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_LONG), "getItemType", "?itemType");
		method(ti, genInternalMethod(CONTEXT_GETLENGTH, XD_LONG, ANY_MODE, 1, 1, XD_CONTAINER), "getLength");
		method(ti, genInternalMethod(GET_NAMEDVALUE, XD_ANY,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "getNamedItem");
		method(ti, genInternalMethod(GET_NAMEDITEMS, XD_CONTAINER,
			ANY_MODE, 1, 1, XD_CONTAINER, XD_STRING), "getNamedItems");
		method(ti, genInternalMethod(GET_NAMED_AS_STRING, XD_STRING, ANY_MODE,
			2, 2, XD_CONTAINER, XD_STRING),"getNamedString", "?fromAttr");
		method(ti, genInternalMethod(CONTEXT_GETTEXT, XD_STRING,ANY_MODE,1,2,XD_CONTAINER,XD_LONG),"getText");
		method(ti, genInternalMethod(HAS_NAMEDVALUE, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "hasNamedItem");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN, ANY_MODE, 1, 1, XD_CONTAINER), "isEmpty");
		method(ti, genInternalMethod(CONTEXT_ITEM, XD_ANY, ANY_MODE, 2, 2, XD_CONTAINER, XD_LONG), "item");
		method(ti, genInternalMethod(CONTEXT_REMOVEITEM, XD_ANY,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_LONG), "removeItem");
		method(ti, genInternalMethod(REMOVE_NAMEDVALUE, XD_VOID,
			ANY_MODE, 2, 2, XD_CONTAINER, XD_STRING), "removeNamedItem");
		method(ti, genInternalMethod(CONTEXT_REPLACEITEM, XD_ANY,
			ANY_MODE, 3, 3, XD_CONTAINER, XD_LONG, XD_ANY), "replaceItem");
		method(ti, genInternalMethod(SET_NAMEDVALUE, XD_VOID,
			ANY_MODE, 2, 3, XD_CONTAINER,XD_ANY,XD_ANY),"setNamedItem");
		method(ti, genInternalMethod(CONTEXT_SORT, XD_CONTAINER,
			ANY_MODE, 1, 3, XD_CONTAINER, XD_STRING, XD_BOOLEAN),"sort");
		method(ti, genInternalMethod(CONTEXT_TO_ELEMENT, XD_ELEMENT,
			ANY_MODE, 1, 3, XD_CONTAINER, XD_STRING, XD_STRING), "toElement");
////////////////////////////////////////////////////////////////////////////////
// CURRENCY
////////////////////////////////////////////////////////////////////////////////
		ti = XD_CURRENCY;
		method(ti, genInternalMethod(NEW_CURRENCY, XD_CURRENCY, ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(CURRENCYCODE, XD_STRING, ANY_MODE, 1, 1, XD_CURRENCY), "currencyCode");
////////////////////////////////////////////////////////////////////////////////
// DATETIME
////////////////////////////////////////////////////////////////////////////////
		ti = XD_DATETIME;
		method(ti, genInternalMethod(PARSE_DATE, XD_DATETIME, ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(ADD_DAY, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "addDay");
		method(ti, genInternalMethod(ADD_HOUR, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "addHour");
		method(ti, genInternalMethod(ADD_MILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_LONG), "addMillisecond");
		method(ti, genInternalMethod(ADD_MINUTE, XD_DATETIME, ANY_MODE, 2,2,XD_DATETIME,XD_LONG),"addMinute");
		method(ti, genInternalMethod(ADD_MONTH, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG),"addMonth");
		method(ti, genInternalMethod(ADD_NANOS,XD_DATETIME,ANY_MODE,2,2,XD_DATETIME,XD_LONG),"addNanosecond");
		method(ti, genInternalMethod(ADD_SECOND, XD_DATETIME, ANY_MODE, 2,2,XD_DATETIME,XD_LONG),"addSecond");
		method(ti, genInternalMethod(ADD_YEAR, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "addYear");
		method(ti, genInternalMethod(GET_DAY, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getDay");
		method(ti, genInternalMethod(GET_FRACTIONSECOND, XD_DOUBLE,
			ANY_MODE, 1, 1, XD_DATETIME), "getFractionalSecond");
		method(ti, genInternalMethod(GET_HOUR, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getHour");
		method(ti, genInternalMethod(GET_MILLIS, XD_LONG,
			ANY_MODE, 1, 1, XD_DATETIME), "getMillis", "?getMillisecond");
		method(ti, genInternalMethod(GET_MINUTE, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME),"getMinute");
		method(ti, genInternalMethod(GET_MONTH, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getMonth");
		method(ti, genInternalMethod(GET_NANOS, XD_LONG,
			ANY_MODE, 1, 1, XD_DATETIME), "getNanos", "?getNanosecond");
		method(ti, genInternalMethod(GET_SECOND, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME),"getSecond");
		method(ti, genInternalMethod(GET_WEEKDAY, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getWeekDay");
		method(ti, genInternalMethod(GET_YEAR, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getYear");
		method(ti, genInternalMethod(GET_DAYTIMEMILLIS, XD_LONG,ANY_MODE,1,1,XD_DATETIME),"getDaytimeMillis");
		method(ti, genInternalMethod(GET_ZONEID, XD_STRING, ANY_MODE, 1, 1, XD_DATETIME), "getZoneName");
		method(ti, genInternalMethod(GET_ZONEOFFSET, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "getZoneOffset");
		method(ti, genInternalMethod(GET_LASTDAYOFMONTH, XD_LONG, ANY_MODE,1,1,XD_DATETIME),"lastDayOfMonth");
		method(ti, genInternalMethod(SET_DAY, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "setDay");
		method(ti, genInternalMethod(SET_DAYTIMEMILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "setDaytimeMillis");
		method(ti, genInternalMethod(SET_FRACTIONSECOND, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_DOUBLE),"setFractionalSecond");
		method(ti, genInternalMethod(SET_HOUR, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "setHour");
		method(ti, genInternalMethod(SET_MINUTE, XD_DATETIME, ANY_MODE, 2,2,XD_DATETIME,XD_LONG),"setMinute");
		method(ti, genInternalMethod(SET_MILLIS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_LONG),"setMillis","?setMillisecond");
		method(ti, genInternalMethod(SET_MONTH, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG),"setMonth");
		method(ti, genInternalMethod(SET_NANOS, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME, XD_LONG), "setNanos","?setNanosecond");
		method(ti, genInternalMethod(SET_SECOND, XD_DATETIME, ANY_MODE, 2,2,XD_DATETIME,XD_LONG),"setSecond");
		method(ti, genInternalMethod(SET_YEAR, XD_DATETIME, ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "setYear");
		method(ti, genInternalMethod(SET_ZONEID, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_STRING), "setZoneName");
		method(ti, genInternalMethod(SET_ZONEOFFSET, XD_DATETIME,
			ANY_MODE, 2, 2, XD_DATETIME,XD_LONG), "setZoneOffset");
		method(ti, genInternalMethod(TO_MILLIS, XD_LONG, ANY_MODE, 1, 1, XD_DATETIME), "toMillis");
		method(ti, genInternalMethod(TO_STRING, XD_STRING, ANY_MODE, 1, 2, XD_DATETIME,XD_STRING),"toString");
////////////////////////////////////////////////////////////////////////////////
// DURATION
///////////////////////////////
		ti = XD_DURATION;
		method(ti, genInternalMethod(PARSE_DURATION, XD_DURATION, ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(DURATION_GETDAYS, XD_LONG, ANY_MODE, 1, 1, XD_DURATION), "getDays");
		method(ti, genInternalMethod(DURATION_GETEND, XD_DATETIME, ANY_MODE, 1, 1, XD_DURATION), "getEnd");
		method(ti, genInternalMethod(DURATION_GETFRACTION, XD_DOUBLE,
			ANY_MODE, 1, 1, XD_DURATION), "getFractionalSecond");
		method(ti, genInternalMethod(DURATION_GETHOURS, XD_LONG, ANY_MODE, 1, 1, XD_DURATION), "getHours");
		method(ti, genInternalMethod(DURATION_GETMINUTES, XD_LONG, ANY_MODE, 1,1, XD_DURATION),"getMinutes");
		method(ti, genInternalMethod(DURATION_GETMONTHS, XD_LONG, ANY_MODE, 1, 1, XD_DURATION), "getMonths");
		method(ti, genInternalMethod(DURATION_GETNEXTTIME, XD_DATETIME,
			ANY_MODE, 1, 1, XD_DURATION), "getNextDate");
		method(ti, genInternalMethod(DURATION_GETRECURRENCE, XD_LONG,
			ANY_MODE, 1, 1, XD_DURATION), "getRecurrence");
		method(ti, genInternalMethod(DURATION_GETSECONDS, XD_LONG, ANY_MODE, 1, 1, XD_DURATION),"getSeconds");
		method(ti, genInternalMethod(DURATION_GETSTART, XD_DATETIME, ANY_MODE, 1, 1, XD_DURATION),"getStart");
		method(ti, genInternalMethod(DURATION_GETYEARS, XD_LONG, ANY_MODE, 1, 1, XD_DURATION), "getYears");
////////////////////////////////////////////////////////////////////////////////
// EMAILADDR
////////////////////////////////////////////////////////////////////////////////
		ti = XD_EMAIL;
		method(ti, genInternalMethod(NEW_EMAIL, XD_EMAIL, ANY_MODE, 1, 1, XD_STRING), "#");
////////////////////////////////////////////////////////////////////////////////
// ELEMENT
////////////////////////////////////////////////////////////////////////////////
		ti = XD_ELEMENT;
		method(ti, genInternalMethod(NEW_ELEMENT, XD_ELEMENT, ANY_MODE, 1, 2, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(ELEMENT_ADDELEMENT, XD_VOID,
			ANY_MODE, 2, 2, XD_ELEMENT, XD_ELEMENT), "addElement");
		method(ti, genInternalMethod(ELEMENT_ADDTEXT, XD_VOID, ANY_MODE, 2,2,XD_ELEMENT,XD_STRING),"addText");
		method(ti, genInternalMethod(ELEMENT_CHILDNODES, XD_CONTAINER,
			ANY_MODE, 1, 1, XD_ELEMENT), "getChidNodes");
		method(ti, genInternalMethod(ELEMENT_ATTRS, XD_CONTAINER, ANY_MODE, 1,1, XD_ELEMENT),"getAttributes");
		method(ti, genInternalMethod(ELEMENT_NSURI, XD_STRING, ANY_MODE, 1, 1, XD_ELEMENT),"getNamespaceURI");
		method(ti, genInternalMethod(ELEMENT_NAME, XD_STRING, ANY_MODE, 1, 1, XD_ELEMENT), "getTagName");
		method(ti, genInternalMethod(ELEMENT_GETTEXT, XD_STRING, ANY_MODE, 1, 1, XD_ELEMENT), "getText");
		method(ti, genInternalMethod(ELEMENT_GETATTR, XD_STRING, ANY_MODE, 2, 3,
			XD_ELEMENT, XD_STRING, XD_STRING), "getAttribute", "?getAttr");
		method(ti, genInternalMethod(ELEMENT_HASATTR, XD_BOOLEAN,
			ANY_MODE, 2, 3, XD_ELEMENT, XD_STRING, XD_STRING), "hasAttribute");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN, ANY_MODE, 1, 1, XD_ELEMENT), "isEmpty");
		method(ti, genInternalMethod(ELEMENT_SETATTR, XD_VOID,
			ANY_MODE, 3, 4, XD_ELEMENT,XD_STRING,XD_STRING,XD_STRING), "setAttribute", "?setAttr");
		method(ti, genInternalMethod(ELEMENT_TOCONTAINER, XD_CONTAINER,
			ANY_MODE, 1, 1, XD_ELEMENT), "toContainer", "?toContext");
		method(ti, genInternalMethod(ELEMENT_TOSTRING, XD_STRING,
			ANY_MODE, 1, 2, XD_ELEMENT, XD_BOOLEAN), "toString");
////////////////////////////////////////////////////////////////////////////////
// EXCEPTION (internal)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_EXCEPTION;
		method(ti, genInternalMethod(NEW_EXCEPTION, XD_EXCEPTION,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(GET_REPORT, XD_REPORT, ANY_MODE, 1, 1, XD_EXCEPTION), "getReport");
		method(ti, genInternalMethod(GET_MESSAGE, XD_STRING, ANY_MODE, 1, 1, XD_EXCEPTION), "getMessage");
////////////////////////////////////////////////////////////////////////////////
// IPADDR
////////////////////////////////////////////////////////////////////////////////
		ti = XD_IPADDR;
		method(ti, genInternalMethod(NEW_IPADDR, XD_IPADDR, ANY_MODE, 1, 1, XD_STRING), "#");
////////////////////////////////////////////////////////////////////////////////
// GPS POSITION
////////////////////////////////////////////////////////////////////////////////
		ti = XD_GPSPOSITION;
		method(ti, genInternalMethod(NEW_GPSPOSITION, XD_GPSPOSITION,
			ANY_MODE, 2, 4, XD_DOUBLE, XD_DOUBLE, XD_ANY, XD_STRING), "#");
		method(ti, genInternalMethod(GPS_LATITUDE, XD_DOUBLE, ANY_MODE, 1, 1, XD_GPSPOSITION), "latitude");
		method(ti, genInternalMethod(GPS_LONGITUDE, XD_DOUBLE, ANY_MODE, 1, 1, XD_GPSPOSITION), "longitude");
		method(ti, genInternalMethod(GPS_ALTITUDE, XD_DOUBLE, ANY_MODE, 1, 1, XD_GPSPOSITION), "altitude");
		method(ti, genInternalMethod(GPS_NAME, XD_STRING, ANY_MODE, 1, 1, XD_GPSPOSITION), "name");
		method(ti, genInternalMethod(GPS_DISTANCETO, XD_DOUBLE,
			ANY_MODE, 2, 2, XD_GPSPOSITION,  XD_GPSPOSITION), "distanceTo");
////////////////////////////////////////////////////////////////////////////////
// LOCALE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_LOCALE;
		method(ti, genInternalMethod(NEW_LOCALE, XD_LOCALE, ANY_MODE, 1,3,XD_STRING,XD_STRING,XD_STRING),"#");
////////////////////////////////////////////////////////////////////////////////
// INPUT STREAM
////////////////////////////////////////////////////////////////////////////////
		ti = XD_INPUT;
		method(ti, genInternalMethod(NEW_INSTREAM, XD_INPUT,
			GLOBAL_MODE, 1, 3, XD_STRING,XD_STRING,XD_BOOLEAN), "#");
		method(ti, genInternalMethod(STREAM_EOF, XD_BOOLEAN, ANY_MODE, 0, 1, XD_INPUT), "eof");
		method(ti, genInternalMethod(STREAM_READLN, XD_STRING, ANY_MODE, 0, 1, XD_INPUT), "readln");
////////////////////////////////////////////////////////////////////////////////
// NAMED VALUE (touple name, value)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_NAMEDVALUE;
		method(ti, genInternalMethod(NEW_NAMEDVALUE, XD_NAMEDVALUE, ANY_MODE, 2, 2, XD_STRING, XD_ANY), "#");
		method(ti, genInternalMethod(NAMEDVALUE_NAME, XD_STRING, ANY_MODE, 1, 1, XD_NAMEDVALUE), "getName");
		method(ti, genInternalMethod(NAMEDVALUE_GET, XD_ANY, ANY_MODE, 1, 1, XD_NAMEDVALUE), "getValue");
		method(ti, genInternalMethod(NAMEDVALUE_SET, XD_VOID, ANY_MODE, 2,3,XD_NAMEDVALUE,XD_ANY),"setValue");
////////////////////////////////////////////////////////////////////////////////
// OBJECT (not used)
////////////////////////////////////////////////////////////////////////////////
//		ti = XD_OBJECT;
////////////////////////////////////////////////////////////////////////////////
// OUTPUT STREAM
////////////////////////////////////////////////////////////////////////////////
		ti = XD_OUTPUT;
		method(ti, genInternalMethod(NEW_OUTSTREAM, XD_OUTPUT,GLOBAL_MODE, 1, 3,
			XD_STRING,XD_STRING,XD_BOOLEAN), "#");
		method(ti, genInternalMethod(PUT_ERROR1, XD_BOOLEAN, ANY_MODE, 2, 4,
			XD_OUTPUT, XD_STRING, XD_STRING, XD_STRING), "error");
		method(ti, genInternalMethod(GET_REPORT, XD_REPORT, ANY_MODE, 1, 1,
			XD_OUTPUT), "getLastError");
		method(ti, genInternalMethod(OUT1_STREAM, XD_VOID, ANY_MODE, 2, 2,
			XD_OUTPUT, XD_STRING), "out");
		method(ti, genInternalMethod(OUTLN1_STREAM, XD_VOID, ANY_MODE, 1, 2, XD_OUTPUT, XD_STRING), "outln");
		method(ti, genInternalMethod(PRINTF_STREAM, XD_VOID, ANY_MODE, 2,
			Integer.MAX_VALUE, XD_OUTPUT, XD_ANY), "printf");
		method(ti, genInternalMethod(PUT_REPORT, XD_VOID, ANY_MODE, 2, 2, XD_OUTPUT, XD_REPORT), "putReport");
////////////////////////////////////////////////////////////////////////////////
// PARSER
////////////////////////////////////////////////////////////////////////////////
		ti = XD_PARSER;
		method(ti, genInternalMethod(PARSE_OP, XD_PARSERESULT,
			ANY_MODE, 1, 2, XD_PARSER, XD_STRING), "parse", "?check");
////////////////////////////////////////////////////////////////////////////////
// PARSERESULT (result of parsing by parsers)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_PARSERESULT;
		method(ti, genInternalMethod(NEW_PARSERESULT, XD_PARSERESULT, ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(GET_PARSED_BOOLEAN, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_PARSERESULT), "booleanValue");
		method(ti, genInternalMethod(GET_PARSED_BYTES, XD_BYTES, ANY_MODE, 1,1, XD_PARSERESULT),"bytesValue");
		method(ti, genInternalMethod(PARSERESULT_MATCH, XD_BOOLEAN,
			ANY_MODE, 1, 1, XD_PARSERESULT), "matches", "?check");
		method(ti, genInternalMethod(GET_PARSED_DATETIME, XD_DATETIME,
			ANY_MODE, 1, 1, XD_PARSERESULT), "datetimeValue");
		method(ti, genInternalMethod(GET_PARSED_DURATION, XD_DURATION,
			ANY_MODE, 1, 1, XD_PARSERESULT), "durationValue");
		method(ti, genInternalMethod(GET_PARSED_DECIMAL, XD_DECIMAL,
			ANY_MODE, 1, 1, XD_PARSERESULT), "decimalValue");
		method(ti, genInternalMethod(GET_PARSED_LONG, XD_LONG, ANY_MODE, 1, 1, XD_PARSERESULT), "intValue");
		method(ti, genInternalMethod(SET_PARSED_ERROR, XD_PARSERESULT,
			ANY_MODE, 1, 4, XD_PARSERESULT, XD_STRING, XD_STRING, XD_STRING), "error", "?setError");
		method(ti, genInternalMethod(GET_PARSED_DOUBLE, XD_DOUBLE, ANY_MODE,1,1,XD_PARSERESULT),"floatValue");
		method(ti, genInternalMethod(GET_PARSED_ERROR, XD_REPORT, ANY_MODE, 1, 1, XD_PARSERESULT),"getError");
		method(ti, genInternalMethod(GET_PARSED_STRING, XD_STRING,
			ANY_MODE, 1, 1, XD_PARSERESULT), "getParsedString");
		method(ti, genInternalMethod(GET_PARSED_VALUE, XD_ANY, ANY_MODE, 1, 1, XD_PARSERESULT), "getValue");
		method(ti, genInternalMethod(SET_PARSED_STRING, XD_PARSERESULT,
			ANY_MODE, 2, 2, XD_PARSERESULT, XD_STRING),"setParsedString");
 //???
		method(ti, genInternalMethod(SET_PARSED_VALUE, XD_VOID,
			ANY_MODE, 2, 2, XD_PARSERESULT, XD_ANY), "setValue");
////////////////////////////////////////////////////////////////////////////////
// PRICE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_PRICE;
		method(ti, genInternalMethod(NEW_PRICE, XD_PRICE, ANY_MODE, 1, 2, XD_DOUBLE, XD_STRING), "#");
		method(ti, genInternalMethod(PRICE_AMOUNT, XD_DOUBLE, ANY_MODE, 1, 1, XD_PRICE), "amount");
		method(ti, genInternalMethod(PRICE_CURRENCY, XD_CURRENCY, ANY_MODE, 1, 1, XD_PRICE), "currency");
		method(ti, genInternalMethod(PRICE_FRACTDIGITS, XD_LONG, ANY_MODE, 1, 1, XD_PRICE), "fractionDigits");
		method(ti, genInternalMethod(PRICE_DISPLAY, XD_STRING, ANY_MODE, 1, 1, XD_PRICE),"display");
////////////////////////////////////////////////////////////////////////////////
// REGEX
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REGEX;
		method(ti, genInternalMethod(COMPILE_REGEX, XD_REGEX, ANY_MODE, 1, 1, XD_STRING), "#");
		method(ti, genInternalMethod(GET_REGEX_RESULT, XD_REGEXRESULT,
			ANY_MODE, 2, 2, XD_REGEX, XD_STRING), "getMatcher");
		method(ti, genInternalMethod(MATCHES_REGEX, XD_BOOLEAN, ANY_MODE, 2,2, XD_REGEX,XD_STRING),"matches");
////////////////////////////////////////////////////////////////////////////////
// REGEX RESULT
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REGEXRESULT;
		//get group end index from regex result
		method(ti, genInternalMethod(GET_REGEX_GROUP_END, XD_LONG,ANY_MODE,2,2,XD_REGEXRESULT,XD_LONG),"end");
		method(ti, genInternalMethod(GET_REGEX_GROUP, XD_STRING,ANY_MODE,2,2,XD_REGEXRESULT,XD_LONG),"group");
		method(ti, genInternalMethod(GET_REGEX_GROUP_NUM, XD_LONG,ANY_MODE, 1,1,XD_REGEXRESULT),"groupCount");
		method(ti, genInternalMethod(MATCHES_REGEX, XD_BOOLEAN, ANY_MODE, 1, 1, XD_REGEXRESULT), "matches");
		method(ti, genInternalMethod(GET_REGEX_GROUP_START, XD_LONG,
			ANY_MODE, 2, 2, XD_REGEXRESULT, XD_LONG), "start");
////////////////////////////////////////////////////////////////////////////////
// REPORT (see org.xdef.sys.Report)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_REPORT;
		method(ti, genInternalMethod(NEW_REPORT, XD_REPORT,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(REPORT_GETPARAM, XD_STRING,
			ANY_MODE, 2, 2, XD_REPORT, XD_STRING), "getParameter");
		method(ti, genInternalMethod(REPORT_SETPARAM, XD_REPORT,
			ANY_MODE, 3, 3, XD_REPORT, XD_STRING, XD_STRING), "setParameter");
		method(ti, genInternalMethod(REPORT_SETTYPE, XD_REPORT, ANY_MODE, 2,2,XD_REPORT,XD_STRING),"setType");
		method(ti, genInternalMethod(REPORT_TOSTRING, XD_STRING, ANY_MODE, 1, 1, XD_REPORT), "toString");
////////////////////////////////////////////////////////////////////////////////
// RESULT SET (result of statement of service)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_RESULTSET;
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID, ANY_MODE, 1, 1, XD_RESULTSET), "close");
		method(ti, genInternalMethod(DB_CLOSESTATEMENT, XD_VOID, ANY_MODE,1,1,XD_RESULTSET),"closeStatement");
		method(ti, genInternalMethod(GET_RESULTSET_COUNT, XD_LONG,
			(byte) (TEXT_MODE + ELEM_MODE),1,1,XD_RESULTSET),"getCount");
		method(ti, genInternalMethod(GET_RESULTSET_ITEM, XD_STRING,
			(byte) (TEXT_MODE + ELEM_MODE), 1, 2, XD_RESULTSET, XD_STRING), "getItem");
		method(ti, genInternalMethod(HAS_RESULTSET_ITEM, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEM_MODE), 2, 2, XD_RESULTSET, XD_STRING), "hasItem");
		method(ti, genInternalMethod(HAS_RESULTSET_NEXT, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEM_MODE), 1, 1,XD_RESULTSET),"hasNext");
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN, ANY_MODE, 1, 1, XD_RESULTSET), "isClosed");
		method(ti, genInternalMethod(RESULTSET_NEXT, XD_BOOLEAN,
			(byte) (TEXT_MODE + ELEM_MODE), 1, 1, XD_RESULTSET), "next");
////////////////////////////////////////////////////////////////////////////////
// SERVICE (e.g. database)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_SERVICE;
		method(ti, genInternalMethod(NEW_SERVICE, XD_SERVICE, ANY_MODE, 4, 4,
			XD_STRING, XD_STRING,XD_STRING, XD_STRING), "#");
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID, ANY_MODE, 1, 1, XD_SERVICE), "close");
		method(ti, genInternalMethod(DB_COMMIT, XD_VOID, ANY_MODE, 1, 1, XD_SERVICE), "commit");
		method(ti, genInternalMethod(DB_EXEC, XD_BOOLEAN, ANY_MODE, 2, Integer.MAX_VALUE,
			XD_SERVICE, XD_STRING), "execute"); //mode, min, max parameters
		method(ti, genInternalMethod(HAS_DBITEM, XD_BOOLEAN, ANY_MODE, 2, Integer.MAX_VALUE,
			XD_SERVICE, XD_STRING, XD_STRING), "hasItem"); //mode, min, max parameters
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN, ANY_MODE, 1, 1, XD_SERVICE), "isClosed");
		method(ti, genInternalMethod(DB_PREPARESTATEMENT, XD_STATEMENT,
			ANY_MODE, 2, 2, XD_SERVICE, XD_STRING), "prepareStatement");
		method(ti, genInternalMethod(GET_DBQUERY, XD_RESULTSET,
			ANY_MODE, 2, Integer.MAX_VALUE, XD_SERVICE, XD_STRING, XD_STRING), "query");
		method(ti, genInternalMethod(GET_DBQUERY_ITEM, XD_RESULTSET, ANY_MODE, 3, Integer.MAX_VALUE,
			XD_SERVICE, XD_STRING, XD_STRING), "queryItem"); //mode, min, max parameters
		method(ti, genInternalMethod(DB_ROLLBACK, XD_VOID, ANY_MODE, 1, 1, XD_SERVICE), "rollback");
		method(ti, genInternalMethod(DB_SETPROPERTY, XD_BOOLEAN, ANY_MODE, 3, 3,
			XD_SERVICE, XD_STRING, XD_STRING), "setProperty");
////////////////////////////////////////////////////////////////////////////////
// STATEMENT (on service, e.g. database)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_STATEMENT;
		method(ti, genInternalMethod(DB_CLOSE, XD_VOID, ANY_MODE, 1, 1, XD_STATEMENT), "close");
		method(ti, genInternalMethod(DB_EXEC, XD_BOOLEAN, //execute
			ANY_MODE, 1, Integer.MAX_VALUE, XD_STATEMENT, XD_STRING), "execute");
		method(ti, genInternalMethod(HAS_DBITEM, XD_BOOLEAN,
			ANY_MODE, 1, Integer.MAX_VALUE, XD_STATEMENT, XD_STRING), "hasItem");
		method(ti, genInternalMethod(DB_ISCLOSED, XD_BOOLEAN, ANY_MODE, 1, 1, XD_STATEMENT), "isClosed");
		method(ti, genInternalMethod(GET_DBQUERY, XD_RESULTSET,
			ANY_MODE, 1, Integer.MAX_VALUE, XD_STATEMENT, XD_STRING), "query");
		method(ti, genInternalMethod(GET_DBQUERY_ITEM, XD_RESULTSET,
			ANY_MODE, 2, Integer.MAX_VALUE, XD_STATEMENT, XD_STRING), "queryItem");
////////////////////////////////////////////////////////////////////////////////
// STRING
////////////////////////////////////////////////////////////////////////////////
		ti = XD_STRING;
		method(ti, genInternalMethod(CHAR_AT, XD_CHAR, ANY_MODE, 2, 2, XD_STRING, XD_LONG), "charAt");
		method(ti, genInternalMethod(CONTAINS, XD_BOOLEAN, ANY_MODE, 2, 2, XD_STRING,XD_STRING), "contains");
		method(ti, genInternalMethod(CONTAINSI, XD_BOOLEAN, ANY_MODE, 2,2, XD_STRING,XD_STRING),"containsi");
		method(ti, genInternalMethod(CUT_STRING, XD_STRING, ANY_MODE, 2, 2, XD_STRING, XD_LONG), "cut");
		method(ti, genInternalMethod(ENDSWITH, XD_BOOLEAN, ANY_MODE, 2, 2, XD_STRING, XD_STRING), "endsWith");
		method(ti, genInternalMethod(ENDSWITHI, XD_BOOLEAN, ANY_MODE, 2,2, XD_STRING, XD_STRING),"endsWithi");
		method(ti,genInternalMethod(EQUALSI, XD_BOOLEAN,ANY_MODE,2,2,XD_STRING,XD_STRING),"equalsIgnoreCase");
		method(ti, genInternalMethod(GET_BYTES_FROM_STRING, XD_BYTES,
			ANY_MODE, 1, 2, XD_STRING, XD_STRING), "getBytes");
		method(ti, genInternalMethod(GET_INDEXOFSTRING, XD_LONG,
			ANY_MODE, 2, 3, XD_STRING, XD_STRING, XD_LONG), "indexOf");
		method(ti, genInternalMethod(IS_EMPTY, XD_BOOLEAN, ANY_MODE, 1, 1, XD_STRING), "isEmpty");
		method(ti, genInternalMethod(GET_LASTINDEXOFSTRING, XD_LONG,
			ANY_MODE, 2, 3, XD_STRING, XD_STRING, XD_LONG), "lastIndexOf");
		method(ti, genInternalMethod(GET_STRING_LENGTH, XD_LONG, ANY_MODE, 1, 1, XD_STRING), "length");
		method(ti, genInternalMethod(STARTSWITH, XD_BOOLEAN,ANY_MODE, 2,2, XD_STRING,XD_STRING),"startsWith");
		method(ti, genInternalMethod(STARTSWITHI, XD_BOOLEAN,
			ANY_MODE, 2, 2, XD_STRING, XD_STRING), "startsWithi");
		method(ti, genInternalMethod(GET_SUBSTRING, XD_STRING,
			ANY_MODE, 2, 3, XD_STRING, XD_LONG, XD_LONG), "substring");
		method(ti, genInternalMethod(LOWERCASE,	XD_STRING, ANY_MODE, 1, 1, XD_STRING), "toLower");
		method(ti, genInternalMethod(UPPERCASE, XD_STRING, ANY_MODE, 1, 1, XD_STRING), "toUpper");
		method(ti, genInternalMethod(TRIM_S, XD_STRING, ANY_MODE, 1, 1, XD_STRING), "trim");
////////////////////////////////////////////////////////////////////////////////
// TELEPHONE
////////////////////////////////////////////////////////////////////////////////
		ti = XD_TELEPHONE;
		method(ti, genInternalMethod(NEW_TELEPHONE, XD_TELEPHONE, ANY_MODE, 1, 1, XD_STRING), "#");
////////////////////////////////////////////////////////////////////////////////
// X_UNIQUESET_KEY (part of key list)
////////////////////////////////////////////////////////////////////////////////
		ti = X_UNIQUESET_KEY;
		method(ti, genInternalMethod(UNIQUESET_KEY_ID, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY, XD_PARSERESULT), "ID");
		method(ti, genInternalMethod(UNIQUESET_KEY_SET, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY, XD_PARSERESULT), "SET");
		method(ti, genInternalMethod(UNIQUESET_KEY_IDREF, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY, XD_PARSERESULT), "IDREF");
		method(ti, genInternalMethod(UNIQUESET_KEY_CHKID, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY, XD_PARSERESULT), "CHKID");
		method(ti, genInternalMethod(UNIQUESET_IDREFS, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY, XD_PARSERESULT), "IDREFS");
		method(ti, genInternalMethod(UNIQUESET_CHKIDS, XD_PARSERESULT,
			TEXT_MODE, 1, 2, X_UNIQUESET_KEY,XD_PARSERESULT), "CHKIDS");
////////////////////////////////////////////////////////////////////////////////
// X_UNIQUESET_M (Multiple key uniqueset)
////////////////////////////////////////////////////////////////////////////////
		ti = X_UNIQUESET_M;
		method(ti, genInternalMethod(UNIQUESET_M_ID, XD_BOOLEAN,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 2, X_UNIQUESET_M), "ID");
		method(ti, genInternalMethod(UNIQUESET_M_SET, XD_BOOLEAN,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 2, X_UNIQUESET_M), "SET");
		method(ti, genInternalMethod(UNIQUESET_M_IDREF, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 2, X_UNIQUESET_M,XD_PARSERESULT), "IDREF");
		method(ti, genInternalMethod(UNIQUESET_M_CHKID, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 2, X_UNIQUESET_M, XD_PARSERESULT), "CHKID");
		method(ti, genInternalMethod(UNIQUESET_M_NEWKEY, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 1, X_UNIQUESET_M), "NEWKEY");
		method(ti, genInternalMethod(UNIQUESET_CLOSE, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 1, X_UNIQUESET_M), "CLEAR");
		method(ti, genInternalMethod(UNIQUESET_CHEKUNREF, XD_VOID,
			(byte)(TEXT_MODE+ELEM_MODE), 1, 1, X_UNIQUESET_M), "checkUnref");
		method(ti, genInternalMethod(UNIQUESET_M_SIZE, XD_LONG, ANY_MODE, 1, 1, X_UNIQUESET_M), "size");
		method(ti, genInternalMethod(UNIQUESET_M_TOCONTAINER, XD_CONTAINER,
			ANY_MODE, 1, 1, X_UNIQUESET_M), "toContainer");
		method(ti, genInternalMethod(UNIQUESET_GET_ACTUAL_KEY, XD_UNIQUESET_KEY,
			ANY_MODE, 1, 1, X_UNIQUESET_M), "getActualKey");
////////////////////////////////////////////////////////////////////////////////
// UNIQUESET_KEY (key uniqueSet)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_UNIQUESET_KEY;
		method(ti, genInternalMethod(UNIQUESET_KEY_RESET, XD_VOID, ANY_MODE,1,1,XD_UNIQUESET_KEY),"resetKey");
////////////////////////////////////////////////////////////////////////////////
// XML Writer (output XML stream)
////////////////////////////////////////////////////////////////////////////////
		ti = XD_XMLWRITER;
		method(ti, genInternalMethod(NEW_XMLWRITER, XD_XMLWRITER,
			ANY_MODE, 1, 3, XD_STRING, XD_STRING, XD_BOOLEAN), "#");
		method(ti, genInternalMethod(SET_XMLWRITER_INDENTING, XD_VOID,
			ANY_MODE, 2, 2, XD_XMLWRITER, XD_BOOLEAN), "setIndenting");
		method(ti, genInternalMethod(WRITE_ELEMENT_START, XD_VOID,
			ANY_MODE, 1, 2,XD_XMLWRITER, XD_ELEMENT),"writeElementStart");
		method(ti, genInternalMethod(WRITE_ELEMENT_END, XD_VOID,ANY_MODE,1,1,XD_XMLWRITER),"writeElementEnd");
		method(ti, genInternalMethod(WRITE_ELEMENT, XD_VOID,
			ANY_MODE, 1, 2, XD_XMLWRITER, XD_ELEMENT), "writeElement");
		method(ti, genInternalMethod(WRITE_TEXTNODE, XD_VOID,
			ANY_MODE, 1, 2, XD_XMLWRITER, XD_STRING), "writeText");
		method(ti, genInternalMethod(CLOSE_XMLWRITER, XD_VOID, ANY_MODE, 1, 1, XD_XMLWRITER), "close");
	}
////////////////////////////////////////////////////////////////////////////////

	/** Register parser.
	 * @param im Internal method object.
	 * @param clazz class of parser.
	 * @param names alias names of the parser. If a name starts with "?" then that item is deprecated
	 * (and the previous item is a recommended parser name - i.e. alias name can't be the first one).
	 */
	private static void parser(final InternalMethod im, final Class<?> clazz, final String... names) {
		try {
			Constructor<?> c = ((Class<?>) clazz).getConstructor();
			Map<String, InternalMethod> hm;
			if ((hm = getTypeMethods(X_NOTYPE_VALUE)) == null) {
				METHODS.set(X_NOTYPE_VALUE, hm = new LinkedHashMap<>());
			}
			for (int i = 0; i < names.length; i++) {
				InternalMethod im1 = im;
				String name = names[i];
				if (name.charAt(0) == '?') {
					name = name.substring(1);
					String recommended = null;
					int j = i;
					// find name without leading '?'
					while (j>0 && (recommended = names[--j])!=null && recommended.charAt(0)=='?') {}
					if (recommended != null) {
						im1 = new InternalMethod(im, recommended);
					}
				}
				hm.put(name, im1);
				PARSERS.put(name, c);
			}
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new Error("Internal error: " + ex);
		}
	}

	/** Register method.
	 * @param typeId id of given type. that item is deprecated (and the previous item is a recommended method
	 * name - i.e. alias name can't be the first one).
	 * @param im Internal method object.
	 */
	private static void method(final short typeId, final InternalMethod im, final String... names) {
		Map<String, InternalMethod> hm;
		if ((hm = getTypeMethods(typeId)) == null) {
			METHODS.set(typeId, hm = new LinkedHashMap<>());
		}
		for (int i = 0; i < names.length; i++) {
			InternalMethod im1 = im;
			String name = names[i];
			if (name.charAt(0) == '?') {
				name = name.substring(1);
				int j = i;
				String recommended = null;
				while (j>0 && (recommended = names[--j])!=null && recommended.charAt(0)=='?') {}
				if (recommended != null) {
					im1 = new InternalMethod(im, recommended);
				}
			}
			hm.put(name, im1);
		}
	}

	/** Create KeyParam (descriptor of key parameter).
	 * @param key name of parameter.
	 * @param type type of parameter value.
	 * @param list true if parameter is list of values.
	 * @param seqIndex sequential parameter index or -1.
	 * @param fixed true if value is fixed.
	 * @param legalValues default value of parameter or <i>null</i>.
	 * @return created KeyParam object
	 */
	private static KeyParam keyParam(final String key,
		final short type,
		final boolean list,
		final int seqIndex,
		final boolean fixed,
		final XDValue... legalValues) {
		return new KeyParam(key,type,list,seqIndex,fixed,false,legalValues);
	}

	/** Create internal method descriptor of parser with keyword parameters. Note the parameter code is fixed
	 * value LD_CONST, parameter restrictions is ANY_MODE and resultType is XD_PARSER.
	 * @param minPars Minimal number of parameters.
	 * @param maxPars Maximal number of parameters.
	 * @param paramTypes List of type id's of parameters.
	 * @param parsedResult type of parsed result.
	 * @param keyparams keyword parameters.
	 */
	private static InternalMethod genParserMetnod(final int minPars,
		final int maxPars,
		final short[] paramTypes,
		final short parsedResult,
		KeyParam... keyparams) {
		return new InternalMethod(
			LD_CONST, XD_PARSER, parsedResult, ANY_MODE, minPars, maxPars, paramTypes, keyparams);
	}

////////////////////////////////////////////////////////////////////////////////
// Methods called from classes of compiler.
////////////////////////////////////////////////////////////////////////////////
	/** Create new internal method descriptor with void parsedResult type.
	 * @param code The code of method.
	 * @param resultType Id of the result type.
	 * @param restrictions Modes where the method is allowed.
	 * @param minPars Minimal number of parameters.
	 * @param maxPars Maximal number of parameters.
	 * @param paramTypes List of type id's of parameters.
	 */
	static InternalMethod genInternalMethod(final short code,
		final short resultType,
		final byte restrictions,
		final int minPars,
		final int maxPars,
		final short... paramTypes) {
		return new InternalMethod(code,
			resultType, XD_VOID, restrictions, minPars, maxPars, paramTypes, (KeyParam[]) null);
	}

	/** Get type ID from class name.
	 * @param className the name of class.
	 * @param classLoader the class loader used for the project.
	 * @return type ID or XD_UNDEF.
	 */
	static short getClassTypeID(final String className, final ClassLoader classLoader) {
		int ndx = TYPEIDS.indexOf(';' + className + ';');
		if (ndx > 0) { //names of X-script types
			return (short) TYPEIDS.charAt(ndx - 1);
		}
		ndx = EXT_TYPEIDS.indexOf(';' + className + ';');
		if (ndx > 0) { // names of parameters of external object declarations.
			return (short) EXT_TYPEIDS.charAt(ndx - 1);
		}
		try {
			Class<?> clazz = Class.forName(className, false, classLoader);
			for (short i = 0; i < TYPECLASSES.length; i++) {
				if (clazz.equals(TYPECLASSES[i])) {
					return i;
				}
			}
		} catch (ClassNotFoundException ex) {}//ignore exception return XD_UNDEF
		return XD_UNDEF;
	}

	/** Get type ID from class.
	 * @param clazz class representing type.
	 * @return type ID or XD_UNDEF.
	 */
	static short getClassTypeID(final Class<?> clazz) {
		if (clazz == null || clazz.equals(java.lang.Void.TYPE) ||
			clazz.equals(java.lang.Void.class)) {
			return XD_VOID;
		} else if (clazz.equals(Long.TYPE) || clazz.equals(Long.class) || clazz.equals(Integer.TYPE)
			|| clazz.equals(Integer.class) || clazz.equals(Short.TYPE) || clazz.equals(Short.class)
			|| clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)) {
			return XD_LONG;
		} else if (clazz.equals(Float.TYPE) || clazz.equals(Float.class) ||
			clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
			return XD_DOUBLE;
		} else if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
			return XD_BOOLEAN;
		}
		for (short i = 0; i < TYPECLASSES.length; i++) {
			if (TYPECLASSES[i] != null && clazz.equals(TYPECLASSES[i])) {
				return i;
			}
		}
		return XD_UNDEF;
	}

	/** Get type id from type name.
	 * @param name name of type.
	 * @return ype Id or -1.
	 */
	public static short getTypeId(final String name) {
		for (short i = 0; i < X_NOTYPE_VALUE; i++) {
			if (name.equals(TYPENAMES[i])) {
				return i;
			}
		}
		return -1;
	}

	/** Get type ID of parsed result of method.
	 * @param name of method,
	 * @return type ID of parsed result (return XD_STRING if name is null or method is not found).
	 */
	static short getParsedType(final String name) {
		if (name == null) {
			return XD_STRING;
		}
		InternalMethod m = getTypeMethod(X_NOTYPE_VALUE, name);
		return (m == null) ? XD_STRING : m.getParsedResult();
	}

	/** Get type methods.
	 * @param typ base type.
	 * @return map with method names and internal method objects.
	 */
	public static final Map<String, InternalMethod> getTypeMethods(final short typ) {return METHODS.get(typ);}

	/** Get internal method.
	 * @param typ base type.
	 * @param name name of method.
	 * @return InternalMethod object.
	 */
	public static final InternalMethod getTypeMethod(final short typ, final String name) {
		Map<String, InternalMethod> hm = getTypeMethods(typ);
		return hm == null ? null : hm.get(name);
	}

	/** Get class object corresponding to the type.
	 * @param typ type id.
	 * @return the Class object corresponding to type argument.
	 */
	static Class<?> getTypeClass(short typ) {
		Class<?> result = TYPECLASSES[typ];
		return result == null ? org.xdef.impl.compile.CodeUndefined.class : result;
	}

	/** Get instance of object parser with given name.
	 * @param name of parser.
	 * @return instance of object of parser with given name or null.
	 */
	public static final XDParser getParser(final String name) {
		try {
			return (XDParser) ((Constructor)PARSERS.get(name)).newInstance();
		} catch (ReflectiveOperationException | RuntimeException ex) {
			return null; // such parser not exists
		}
	}

	/** Get type name from type id.
	 * @param type The type id.
	 * @return The type name or null.
	 */
	public static final String getTypeName(final short type) {
		return type >= 0 && type < TYPENAMES.length ? TYPENAMES[type] : "UNDEF_VALUE";
	}

////////////////////////////////////////////////////////////////////////////////
// Classes used by compiler.
////////////////////////////////////////////////////////////////////////////////
	/** Description item of an internal method. */
	public static final class InternalMethod {
		/** Code of the method.*/
		private final short _code;
		/** Minimal number of parameters.*/
		private final int _minParams;
		/** Maximal number of parameters.*/
		private final int _maxParams;
		/** List of id's of types of parameters.*/
		private final short[] _paramTypes;
		/** Id of the result type.*/
		private final short _resultType;
		/** Set of modes where the method is allowed.*/
		private final byte _restrictions;
		/** Keyword parameters or null. */
		private final KeyParam[] _keyparams;
		/** Keyword names oof sequential parameters. */
		private final String[] _sqKeynames;
		/** Result of parsed object (only parsers). */
		private final short _parsedResult;
		/** Text of recommendation for deprecated methods. */
		private final String _recommended;

		/** Create new internal method descriptor of method with keyword parameters.
		 * @param code The code of method.
		 * @param restrictions Modes where the method is allowed.
		 * @param minPars Minimal number of parameters.
		 * @param maxPars Maximal number of parameters.
		 * @param paramTypes List of type id's of parameters.
		 * @param resultType Id of the result type.
		 * @param parsedResult type of parsed result.
		 * @param keyparams keyword parameters.
		 */
		private InternalMethod(final short code,
			final short resultType,
			final short parsedResult,
			final byte restrictions,
			final int minPars,
			final int maxPars,
			final short[] paramTypes,
			KeyParam... keyparams) {
			_code = code;
			_restrictions = restrictions;
			_minParams = minPars;
			_maxParams = maxPars;
			_paramTypes = paramTypes;
			_resultType = resultType;
			_parsedResult = parsedResult;
			_keyparams = keyparams;
			if (_keyparams == null) {
				_sqKeynames = null;
			} else {
				int maxpar = -1;
				for (KeyParam keyparam : _keyparams) {
					int j = keyparam.getSeqIndex();
					if (j > maxpar) {
						maxpar = j;
					}
				}
				_sqKeynames = new String[maxpar+1];
				for (KeyParam keyparam : _keyparams) {
					int j = keyparam.getSeqIndex();
					if (j >= 0) {
						_sqKeynames[j] = keyparam.getName();
					}
				}
			}
			 _recommended = null;
		}

		/** Create clone of deprecated InternalMethod object and add the recommended name.
		 * @param x original InternalMethod.
		 * @param recommended name of recommended method instead of this one.
		 */
		private InternalMethod (final InternalMethod x, final String recommended) {
			_code = x._code;
			_minParams = x._minParams;
			_maxParams = x._maxParams;
			_paramTypes = x._paramTypes;
			_resultType = x._resultType;
			_restrictions = x._restrictions;
			_keyparams = x._keyparams;
			_sqKeynames = x._sqKeynames;
			_parsedResult = x._parsedResult;
			_recommended = recommended;
		}
		final short getCode() {return _code;}
		final byte getRestrictions() {return _restrictions;}
		public final int getMinParams() {return _minParams;}
		public final int getMaxParams() {return _maxParams;}
		public final String[] getSqParamNames() {return _sqKeynames;}
		public final short[] getParamTypes() {return _paramTypes;}
		public final boolean isDeprecated() {return _recommended != null;}
		public final String getRecommendedName() {return _recommended;}
		public short getResultType() {return _resultType;}
		public KeyParam[] getKeyParams() {return _keyparams;}
		public short getParsedResult() {return _parsedResult;}
		public XDContainer getFixedParams() {
			if (_keyparams == null) {
				return null;
			}
			XDContainer fixedParams = null;
			for (KeyParam _keyparam : _keyparams) {
				if (_keyparam.isFixed()) {
					if (fixedParams == null) {
						fixedParams = new DefContainer();
					}
					fixedParams.setXDNamedItem(_keyparam.getName(), _keyparam.getLegalValues()[0]);
				}
			}
			return fixedParams;
		}
	}

	/** Description of keyword parameter. */
	public static class KeyParam {
		private final String _name; //name of parameter
		private final short _type;  //type of parameter value
		private final boolean _list; //true if parameter is list of values
		private final XDValue[] _legalValues; //legal values (default is first)
		private final int _seqIndex; // sequential param index
		private final boolean _fixed; //true if parameter fixed
		private final boolean _required; //true if parameter is required

		/** Create descriptor of key parameter.
		 * @param name name of parameter.
		 * @param type type of parameter value.
		 * @param list true if parameter is list of values.
		 * @param legalValues array with legal values or null
		 * @param seqIndex sequential parameter index or -1.
		 * @param fixed true if parameter value is fixed.
		 * @param required true if parameter value is required.
		 */
		private KeyParam(final String name,
			final short type,
			final boolean list,
			final int seqIndex,
			final boolean fixed,
			final boolean required,
			final XDValue... legalValues) {
			_name = name;
			_type = type;
			_list = list;
			_legalValues = legalValues;
			_seqIndex = seqIndex;
			_fixed = fixed;
			_required = required;
		}
		/** Get name of parameter.
		 * @return name of parameter.
		 */
		public final String getName() {return _name;}
		/** Get type of parameter value.
		 * @return type of parameter value.
		 */
		public final short getType() {return _type;}
		/** Get default value of parameter.
		 * @return default value of parameter or null.
		 */
		public final XDValue[] getLegalValues() {return _legalValues;}
		/** Get default value of parameter.
		 * @return default value of parameter or null.
		 */
		public final XDValue getDefaultValue() {
			return _legalValues == null || _legalValues.length == 0 ? null : _legalValues[0];
		}
		/** Get parameter index (if specified as sequential).
		 * @return sequential parameter index or -1.
		 */
		public final int getSeqIndex() {return _seqIndex;}
		/** Check if parameter is list of values or a single value.
		 * @return true if parameter is list of values.
		 */
		public boolean isList() {return _list;}
		/** Check if parameter value is fixed.
		 * @return true if parameter value is fixed.
		 */
		public final boolean isFixed() {return _fixed;}
		/** Check if parameter value is required.
		 * @return true if parameter value is required.
		 */
		public final boolean isRequired() {return _required;}
	}
}
