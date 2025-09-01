package org.xdef.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDCallItem;
import org.xdef.XDConstants;
import static org.xdef.XDConstants.XDPROPERTYVALUE_DEBUG_FALSE;
import static org.xdef.XDConstants.XDPROPERTYVALUE_DEBUG_TRUE;
import static org.xdef.XDConstants.XDPROPERTY_DEBUG;
import static org.xdef.XDConstants.XDPROPERTY_MESSAGES;
import static org.xdef.XDConstants.XDPROPERTY_MSGLANGUAGE;
import org.xdef.XDContainer;
import org.xdef.XDCurrency;
import org.xdef.XDDebug;
import org.xdef.XDElement;
import org.xdef.XDException;
import org.xdef.XDGPSPosition;
import org.xdef.XDInput;
import org.xdef.XDNamedValue;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDPrice;
import org.xdef.XDRegex;
import org.xdef.XDRegexResult;
import org.xdef.XDReport;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDUniqueSet;
import org.xdef.XDUniqueSetKey;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_INPUT;
import static org.xdef.XDValueID.XD_LOCALE;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_OUTPUT;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_REPORT;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_SERVICE;
import static org.xdef.XDValueID.XD_STATEMENT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_XPATH;
import static org.xdef.XDValueID.XD_XQUERY;
import static org.xdef.XDValueID.XX_ATTR;
import static org.xdef.XDValueID.XX_ELEMENT;
import static org.xdef.XDValueID.XX_TEXT;
import static org.xdef.XDValueID.X_PARSEITEM;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_KEY;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.OBJECT;
import org.xdef.XDXmlOutStream;
import org.xdef.impl.code.CodeParser;
import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeSWTableInt;
import org.xdef.impl.code.CodeSWTableStr;
import static org.xdef.impl.code.CodeTable.ADD_DAY;
import static org.xdef.impl.code.CodeTable.ADD_HOUR;
import static org.xdef.impl.code.CodeTable.ADD_I;
import static org.xdef.impl.code.CodeTable.ADD_MILLIS;
import static org.xdef.impl.code.CodeTable.ADD_MINUTE;
import static org.xdef.impl.code.CodeTable.ADD_MONTH;
import static org.xdef.impl.code.CodeTable.ADD_NANOS;
import static org.xdef.impl.code.CodeTable.ADD_R;
import static org.xdef.impl.code.CodeTable.ADD_S;
import static org.xdef.impl.code.CodeTable.ADD_SECOND;
import static org.xdef.impl.code.CodeTable.ADD_YEAR;
import static org.xdef.impl.code.CodeTable.AND_B;
import static org.xdef.impl.code.CodeTable.ATTR_EXIST;
import static org.xdef.impl.code.CodeTable.ATTR_REF;
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
import static org.xdef.impl.code.CodeTable.CALL_OP;
import static org.xdef.impl.code.CodeTable.CHAR_AT;
import static org.xdef.impl.code.CodeTable.CHECK_TYPE;
import static org.xdef.impl.code.CodeTable.CHK_GE;
import static org.xdef.impl.code.CodeTable.CHK_GT;
import static org.xdef.impl.code.CodeTable.CHK_LE;
import static org.xdef.impl.code.CodeTable.CHK_LT;
import static org.xdef.impl.code.CodeTable.CHK_NE;
import static org.xdef.impl.code.CodeTable.CHK_NEI;
import static org.xdef.impl.code.CodeTable.CLEAR_REPORTS;
import static org.xdef.impl.code.CodeTable.CLOSE_XMLWRITER;
import static org.xdef.impl.code.CodeTable.CMPEQ;
import static org.xdef.impl.code.CodeTable.CMPGE;
import static org.xdef.impl.code.CodeTable.CMPGT;
import static org.xdef.impl.code.CodeTable.CMPLE;
import static org.xdef.impl.code.CodeTable.CMPLT;
import static org.xdef.impl.code.CodeTable.CMPNE;
import static org.xdef.impl.code.CodeTable.COMPILE_BNF;
import static org.xdef.impl.code.CodeTable.COMPILE_REGEX;
import static org.xdef.impl.code.CodeTable.COMPILE_XPATH;
import static org.xdef.impl.code.CodeTable.COMPOSE_OP;
import static org.xdef.impl.code.CodeTable.CONTAINS;
import static org.xdef.impl.code.CodeTable.CONTAINSI;
import static org.xdef.impl.code.CodeTable.CONTEXT_ADDITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETELEMENTS;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETELEMENT_X;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETLENGTH;
import static org.xdef.impl.code.CodeTable.CONTEXT_GETTEXT;
import static org.xdef.impl.code.CodeTable.CONTEXT_ITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_ITEMTYPE;
import static org.xdef.impl.code.CodeTable.CONTEXT_REMOVEITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_REPLACEITEM;
import static org.xdef.impl.code.CodeTable.CONTEXT_SORT;
import static org.xdef.impl.code.CodeTable.CONTEXT_TO_ELEMENT;
import static org.xdef.impl.code.CodeTable.CREATE_ELEMENT;
import static org.xdef.impl.code.CodeTable.CREATE_ELEMENTS;
import static org.xdef.impl.code.CodeTable.CREATE_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.CURRENCYCODE;
import static org.xdef.impl.code.CodeTable.CUT_STRING;
import static org.xdef.impl.code.CodeTable.DATE_FORMAT;
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
import static org.xdef.impl.code.CodeTable.DEC_I;
import static org.xdef.impl.code.CodeTable.DEC_R;
import static org.xdef.impl.code.CodeTable.DEFAULT_ERROR;
import static org.xdef.impl.code.CodeTable.DEL_ATTR;
import static org.xdef.impl.code.CodeTable.DIV_I;
import static org.xdef.impl.code.CodeTable.DIV_R;
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
import static org.xdef.impl.code.CodeTable.EQUALS_OP;
import static org.xdef.impl.code.CodeTable.EQ_NULL;
import static org.xdef.impl.code.CodeTable.FLOAT_FORMAT;
import static org.xdef.impl.code.CodeTable.FORMAT_STRING;
import static org.xdef.impl.code.CodeTable.FROM_ELEMENT;
import static org.xdef.impl.code.CodeTable.GETATTR_FROM_CONTEXT;
import static org.xdef.impl.code.CodeTable.GETELEMS_FROM_CONTEXT;
import static org.xdef.impl.code.CodeTable.GETELEM_FROM_CONTEXT;
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
import static org.xdef.impl.code.CodeTable.GET_NUMOFERRORS;
import static org.xdef.impl.code.CodeTable.GET_NUMOFERRORWARNINGS;
import static org.xdef.impl.code.CodeTable.GET_OCCURRENCE;
import static org.xdef.impl.code.CodeTable.GET_PARSED_ERROR;
import static org.xdef.impl.code.CodeTable.GET_PARSED_RESULT;
import static org.xdef.impl.code.CodeTable.GET_PARSED_STRING;
import static org.xdef.impl.code.CodeTable.GET_PARSED_VALUE;
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
import static org.xdef.impl.code.CodeTable.INC_I;
import static org.xdef.impl.code.CodeTable.INC_R;
import static org.xdef.impl.code.CodeTable.INIT_NOPARAMS_OP;
import static org.xdef.impl.code.CodeTable.INIT_PARAMS_OP;
import static org.xdef.impl.code.CodeTable.INTEGER_FORMAT;
import static org.xdef.impl.code.CodeTable.IS_DATETIME;
import static org.xdef.impl.code.CodeTable.IS_EMPTY;
import static org.xdef.impl.code.CodeTable.IS_FLOAT;
import static org.xdef.impl.code.CodeTable.IS_INT;
import static org.xdef.impl.code.CodeTable.IS_NUM;
import static org.xdef.impl.code.CodeTable.JMPEQ;
import static org.xdef.impl.code.CodeTable.JMPF_OP;
import static org.xdef.impl.code.CodeTable.JMPGE;
import static org.xdef.impl.code.CodeTable.JMPGT;
import static org.xdef.impl.code.CodeTable.JMPLE;
import static org.xdef.impl.code.CodeTable.JMPLT;
import static org.xdef.impl.code.CodeTable.JMPNE;
import static org.xdef.impl.code.CodeTable.JMPT_OP;
import static org.xdef.impl.code.CodeTable.JMP_OP;
import static org.xdef.impl.code.CodeTable.LD_CODE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import static org.xdef.impl.code.CodeTable.LD_GLOBAL;
import static org.xdef.impl.code.CodeTable.LD_LOCAL;
import static org.xdef.impl.code.CodeTable.LD_TRUE_AND_SKIP;
import static org.xdef.impl.code.CodeTable.LD_XMODEL;
import static org.xdef.impl.code.CodeTable.LOWERCASE;
import static org.xdef.impl.code.CodeTable.LSHIFT_I;
import static org.xdef.impl.code.CodeTable.MATCHES_REGEX;
import static org.xdef.impl.code.CodeTable.MOD_I;
import static org.xdef.impl.code.CodeTable.MOD_R;
import static org.xdef.impl.code.CodeTable.MUL_I;
import static org.xdef.impl.code.CodeTable.MUL_R;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_GET;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_NAME;
import static org.xdef.impl.code.CodeTable.NAMEDVALUE_SET;
import static org.xdef.impl.code.CodeTable.NEG_BINARY;
import static org.xdef.impl.code.CodeTable.NEG_I;
import static org.xdef.impl.code.CodeTable.NEG_R;
import static org.xdef.impl.code.CodeTable.NEW_BNFGRAMAR;
import static org.xdef.impl.code.CodeTable.NEW_BYTES;
import static org.xdef.impl.code.CodeTable.NEW_CONTAINER;
import static org.xdef.impl.code.CodeTable.NEW_CURRENCY;
import static org.xdef.impl.code.CodeTable.NEW_ELEMENT;
import static org.xdef.impl.code.CodeTable.NEW_EMAIL;
import static org.xdef.impl.code.CodeTable.NEW_INSTREAM;
import static org.xdef.impl.code.CodeTable.NEW_IPADDR;
import static org.xdef.impl.code.CodeTable.NEW_LOCALE;
import static org.xdef.impl.code.CodeTable.NEW_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.NEW_OUTSTREAM;
import static org.xdef.impl.code.CodeTable.NEW_PARSER;
import static org.xdef.impl.code.CodeTable.NEW_PARSERESULT;
import static org.xdef.impl.code.CodeTable.NEW_REPORT;
import static org.xdef.impl.code.CodeTable.NEW_SERVICE;
import static org.xdef.impl.code.CodeTable.NEW_TELEPHONE;
import static org.xdef.impl.code.CodeTable.NEW_URI;
import static org.xdef.impl.code.CodeTable.NEW_XMLWRITER;
import static org.xdef.impl.code.CodeTable.NE_NULL;
import static org.xdef.impl.code.CodeTable.NOT_B;
import static org.xdef.impl.code.CodeTable.NO_OP;
import static org.xdef.impl.code.CodeTable.NULL_OR_TO_STRING;
import static org.xdef.impl.code.CodeTable.OR_B;
import static org.xdef.impl.code.CodeTable.OUT1_STREAM;
import static org.xdef.impl.code.CodeTable.OUTLN1_STREAM;
import static org.xdef.impl.code.CodeTable.OUTLN_STREAM;
import static org.xdef.impl.code.CodeTable.OUT_STREAM;
import static org.xdef.impl.code.CodeTable.PARSEANDCHECK;
import static org.xdef.impl.code.CodeTable.PARSEANDSTOP;
import static org.xdef.impl.code.CodeTable.PARSERESULT_MATCH;
import static org.xdef.impl.code.CodeTable.PARSE_DATE;
import static org.xdef.impl.code.CodeTable.PARSE_DURATION;
import static org.xdef.impl.code.CodeTable.PARSE_OP;
import static org.xdef.impl.code.CodeTable.PARSE_XML;
import static org.xdef.impl.code.CodeTable.POP_OP;
import static org.xdef.impl.code.CodeTable.PRICE_AMOUNT;
import static org.xdef.impl.code.CodeTable.PRICE_CURRENCY;
import static org.xdef.impl.code.CodeTable.PRICE_DISPLAY;
import static org.xdef.impl.code.CodeTable.PRICE_FRACTDIGITS;
import static org.xdef.impl.code.CodeTable.PRINTF_STREAM;
import static org.xdef.impl.code.CodeTable.PUT_ERROR;
import static org.xdef.impl.code.CodeTable.PUT_ERROR1;
import static org.xdef.impl.code.CodeTable.PUT_REPORT;
import static org.xdef.impl.code.CodeTable.RELEASE_CATCH_EXCEPTION;
import static org.xdef.impl.code.CodeTable.REMOVE_NAMEDVALUE;
import static org.xdef.impl.code.CodeTable.REMOVE_TEXT;
import static org.xdef.impl.code.CodeTable.REPLACEFIRST_S;
import static org.xdef.impl.code.CodeTable.REPLACE_S;
import static org.xdef.impl.code.CodeTable.REPORT_SETPARAM;
import static org.xdef.impl.code.CodeTable.REPORT_SETTYPE;
import static org.xdef.impl.code.CodeTable.REPORT_TOSTRING;
import static org.xdef.impl.code.CodeTable.RESULTSET_NEXT;
import static org.xdef.impl.code.CodeTable.RETV_OP;
import static org.xdef.impl.code.CodeTable.RET_OP;
import static org.xdef.impl.code.CodeTable.RRSHIFT_I;
import static org.xdef.impl.code.CodeTable.RSHIFT_I;
import static org.xdef.impl.code.CodeTable.SET_ATTR;
import static org.xdef.impl.code.CodeTable.SET_CATCH_EXCEPTION;
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
import static org.xdef.impl.code.CodeTable.STACK_DUP;
import static org.xdef.impl.code.CodeTable.STACK_SWAP;
import static org.xdef.impl.code.CodeTable.STACK_TO_CONTAINER;
import static org.xdef.impl.code.CodeTable.STARTSWITH;
import static org.xdef.impl.code.CodeTable.STARTSWITHI;
import static org.xdef.impl.code.CodeTable.STOP_OP;
import static org.xdef.impl.code.CodeTable.STREAM_EOF;
import static org.xdef.impl.code.CodeTable.STREAM_READLN;
import static org.xdef.impl.code.CodeTable.ST_GLOBAL;
import static org.xdef.impl.code.CodeTable.ST_LOCAL;
import static org.xdef.impl.code.CodeTable.ST_XMODEL;
import static org.xdef.impl.code.CodeTable.SUB_I;
import static org.xdef.impl.code.CodeTable.SUB_R;
import static org.xdef.impl.code.CodeTable.SWITCH_I;
import static org.xdef.impl.code.CodeTable.SWITCH_S;
import static org.xdef.impl.code.CodeTable.THROW_EXCEPTION;
import static org.xdef.impl.code.CodeTable.TO_BIGINTEGER_X;
import static org.xdef.impl.code.CodeTable.TO_BOOLEAN;
import static org.xdef.impl.code.CodeTable.TO_CHAR_X;
import static org.xdef.impl.code.CodeTable.TO_DECIMAL_X;
import static org.xdef.impl.code.CodeTable.TO_FLOAT;
import static org.xdef.impl.code.CodeTable.TO_FLOAT_X;
import static org.xdef.impl.code.CodeTable.TO_INT_X;
import static org.xdef.impl.code.CodeTable.TO_MILLIS;
import static org.xdef.impl.code.CodeTable.TO_MILLIS_X;
import static org.xdef.impl.code.CodeTable.TO_STRING;
import static org.xdef.impl.code.CodeTable.TO_STRING_X;
import static org.xdef.impl.code.CodeTable.TRANSLATE_S;
import static org.xdef.impl.code.CodeTable.TRIM_S;
import static org.xdef.impl.code.CodeTable.UNIQUESET_BIND;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHEKUNREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CHKIDS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_CLOSE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_GETVALUEX;
import static org.xdef.impl.code.CodeTable.UNIQUESET_GET_ACTUAL_KEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_IDREFS;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_LOAD;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_NEWKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_RESET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_KEY_SETKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_CHKID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_ID;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_IDREF;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_NEWKEY;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_SIZE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_M_TOCONTAINER;
import static org.xdef.impl.code.CodeTable.UNIQUESET_NEWINSTANCE;
import static org.xdef.impl.code.CodeTable.UNIQUESET_SET;
import static org.xdef.impl.code.CodeTable.UNIQUESET_SETVALUEX;
import static org.xdef.impl.code.CodeTable.UPPERCASE;
import static org.xdef.impl.code.CodeTable.WHITESPACES_S;
import static org.xdef.impl.code.CodeTable.WRITE_TEXTNODE;
import static org.xdef.impl.code.CodeTable.XOR_B;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.CodeXD;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefChar;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefEmailAddr;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefLocale;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefObject;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefURI;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.impl.code.DefXQueryExpr;
import org.xdef.impl.code.ParseItem;
import static org.xdef.impl.compile.CompileBase.getParser;
import static org.xdef.impl.compile.CompileBase.getTypeName;
import org.xdef.impl.debug.ChkGUIDebug;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMDefinition;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXException;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SError;
import org.xdef.sys.SManager;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonTools;

/** Provides processor engine of script code.
 * @author Vaclav Trojan
 */
public final class XCodeProcessor {

	/** This identifier is created if it is undefined. */
	private static final String UNDEF_ID = "__UNDEF_ID__";
	/** Switch to allow/restrict DOCTYPE in XML. */
	/** Executable code */
	private XDValue[] _code;
	/** Address of code initialization. */
	private int _init;
	/** Processor stack. */
	private XDValue[] _stack;
	/** Local variables. */
	private XDValue[] _localVariables;
	/** Local string parser */
	private StringParser _textParser;
	/** Call list - must be implementation, not interface! */
	private CallItem _callList;
	/** Catch list. */
	private CatchItem _catchItem;
	/** Properties. */
	private Properties _props;
	/** Switch to debug mode. */
	private boolean _debug = false; //debug switch
	/** X-definition from which processor was created. */
	private XDefinition _xd;
	/** Global variables:<p>
	 * _globalVariables[i]; i=0 stdOut, 1 stdErr, 2 stdIn, 3 $IDParser$, 4 $IDuniqueSet$</p>
	 * Follows global variables from DefPool.
	 */
	private XDValue[] _globalVariables;
	/** Temporary  reporter */
	ArrayReporter _reporter;
	/** XML writer for default output. */
	private XDXmlOutStream _outWriter;
	/** flag 'init1' processed. */
	private boolean _initialized1;
	/** flag global variables initialized. */
	private boolean _initialized2;
	/** debugger */
	private XDDebug _debugger;
	/** debug information. */
	private XMDebugInfo _debugInfo;
	/** List of items to be managed at the end of process. */
	private List<XDValue> _finalList;
	/** Map of named user objects. */
	private final Map<String, Object> _userObjects = new LinkedHashMap<>();
	/** XPath function resolver. */
	final XPathFunctionResolver _functionResolver = new XDFunctionResolver();
	/** XPath variable resolver. */
	final XPathVariableResolver _variableResolver = new XDVariableResolver();

	/** Creates a new instance of ScriptCodeProcessor
	 * @param xd X-definition.
	 * @param r reporter for error messages.
	 * @param stdOut standard output stream (if null then java.lang.System.out).
	 * @param stdIn standard input stream (if null then java.lang.System.in).
	 * @param userObj Assigned user's object.
	 */
	XCodeProcessor(final XDefinition xd, final SReporter r, final XDOutput stdOut, final XDInput stdIn) {
		init(xd, null);
		init1(xd, r.getReportWriter(), stdOut, stdIn);
	}

	/** Creates new instance of ScriptCodeProcessor. This constructor is called
	 * only internally from ChkComposer.
	 * @param xd X-definition.
	 * @param ce ChkElement from which the object is created.
	 */
	XCodeProcessor(final XDefinition xd, final ChkElement ce) {
		init(xd, ce._scp._props);
		_globalVariables = ce._scp._globalVariables;
		_debugger = ce._scp.getDebugger();
		_reporter = ce.getTemporaryReporter();
		_reporter.clear();
		_textParser.setReportWriter(_reporter);
		_globalVariables = ce._scp._globalVariables;
		_code = ce._scp._code;
		_init = ce._scp._init;
		_initialized1 = true;
		_initialized2 = true;
	}

	/** Get root X-definition. */
	final XDefinition getXDefinition() {return _xd;}

	/** Set properties.
	 * @param props properties used by processor.
	 */
	final void setProperties(final Properties props) {
		if (props == null) {
			_props = new Properties();
		} else {
			_props = props;
			_debug = XDPROPERTYVALUE_DEBUG_TRUE.equals(SManager.getProperty(_props, XDPROPERTY_DEBUG));
			SManager.setProperties(props);
		}
	}

	/** Set X-definition property to SManager. If properties are null the new properties  will be created.
	 * @param key name of X-definition property.
	 * @param value value of property or null. If the value is null the property is removed from properties.
	 */
	public final void setProperty(final String key, final String value) {
		String newKey = key.startsWith("xdef.") ? key.replace('.', '_') : key;
		if (XDPROPERTY_DEBUG.equals(newKey)) {
			_debug = XDPROPERTYVALUE_DEBUG_TRUE.equals(value);
		}
		if (_props == null) {
			if (value == null) {
				return;
			}
			_props = new Properties();
		}
		_props.remove(key);
		if (value != null) {
			_props.setProperty(newKey, value);
		} else {
			_props.remove(newKey);
		}
		if (newKey.startsWith(XDPROPERTY_MESSAGES) || newKey.startsWith(XDPROPERTY_MSGLANGUAGE)) {
			SManager.setProperty(newKey, value);
		}
	}

	/** Get assigned properties.
	 * @return assigned properties.
	 */
	final Properties getProperties() {return _props;}

	/** Get actual value of default time zone.
	 * @return actual value of default time zone.
	 */
	final TimeZone getDefaultZone() {
		String s;
		if (_props!=null && (s=_props.getProperty(XDConstants.XDPROPERTY_DEFAULTZONE))!=null && !s.isEmpty()){
			return TimeZone.getTimeZone(s);
		} else {
			return _xd.getXDPool().getDefaultZone();
		}
	}

	/** Get assigned standard output stream.
	 * @return assigned standard output stream.
	 */
	final XDOutput getStdOut() {return (XDOutput) _globalVariables[0];}

	/** Get assigned standard error stream.
	 * @return assigned standard error stream.
	 */
	final XDOutput getStdErr() {return (XDOutput) _globalVariables[1];}

	/** Get assigned standard input stream.
	 * @return assigned standard input stream.
	 */
	final XDInput getStdIn() {return (XDInput) _globalVariables[2];}

	/** Set standard output stream.
	 * @param out standard output stream.
	 */
	final void setStdOut(final XDOutput out) {_globalVariables[0] = out;}

	/** Set standard error stream.
	 * @param err standard error stream.
	 */
	final void setStdErr(final XDOutput err) {_globalVariables[1] = err;}

	/** Set standard input stream.
	 * @param in standard input stream.
	 */
	final void setStdIn(final XDInput in) {_globalVariables[2] = in;}

	/** Set debugger.
	 * @param debugger the debugger.
	 */
	final void setDebugger(final XDDebug debugger) {_debugger = debugger;}

	/** Get debugger.
	 * @return the debugger.
	 */
	final XDDebug getDebugger() {return _debugger;}

	/** Set debugging mode.
	 * @param debug debugging mode.
	 */
	final void setDebug(final boolean debug) { _debug = debug; }

	/** Check debugging mode is set ON.
	 * @return value of debugging mode.
	 */
	final boolean isDebugMode() { return _debug && _debugger != null; }

	/** Base initialization of code processor engine. */
	private void init(final XDefinition xd, final Properties props) {
		_xd = xd;
		XPool xp = (XPool) xd.getXDPool();
		_init = xp.getInitAddress();
		_stack = new XDValue[xp.getStackSize()];
		_localVariables = new XDValue[xp.getLocalVariablesSize()];
		_textParser = new StringParser(""); //create string parser
		if (props != null) {
			_props = props;
		}
		_debug = xp.isDebugMode() || _props != null &&
			XDPROPERTYVALUE_DEBUG_FALSE.equals(SManager.getProperty(_props, XDPROPERTY_DEBUG));
		if (_debug) {
			if (_debugger == null) {
				String debugEditor = xp.getDebugEditor();
				if (debugEditor != null) {
					try { // try to get external debugger
						Class<?> cls = Class.forName(debugEditor);
						Constructor<?> c = cls.getDeclaredConstructor(Properties.class, XDPool.class);
						_debugger = (XDDebug) c.newInstance(null, xp);
					} catch (Exception ex) {
						_debugger = null; // will be used the default debugger
						// Class with the external debug editor &{0}{"}{"} is not available.
						throw new SRuntimeException(XDEF.XDEF850, ex, debugEditor);
					}
				}
				if (_debugger == null) { // set default debugger
					_debugger = new ChkGUIDebug(getProperties(), xp);
				}
			}
			_debugInfo = xp.getDebugInfo();
		}
	}

	/** Extended initialization of code processor engine. */
	private void init1(final XDefinition xd, final ReportWriter rw, final XDOutput out, final XDInput in) {
		if (!_initialized1) {
			_reporter = new ArrayReporter(); //create temporary reporter.
			_textParser.setReportWriter(_reporter); // set reporter
			XPool xp = (XPool) xd.getXDPool();
			XDValue[] code = xp.getCode();
			_code = new XDValue[code.length];
			for (int i = 0; i < code.length; i++) {
				XDValue x = code[i];
				// to assure reeentrancy of XDPool create clones of constants.
				_code[i] = x.getCode() != LD_CONST ? x : x.cloneItem();
			}
			_globalVariables = new XDValue[xp.getGlobalVariablesSize()];
			_globalVariables[0] = out == null ? new DefOutStream(System.out) : out;
			_globalVariables[1] = rw == null ? new DefOutStream(System.err) : new DefOutStream(rw);
			_globalVariables[2] = in == null ? new DefInStream(System.in, false) : in;
			_globalVariables[3] = null; // "QName" parser
			_globalVariables[4] = null; // CodeUniqueset for ID,IDREF, ...
			_initialized1 = true;
		}
		_initialized2 = false; //initialize global variables at execution
	}

	/** Add the object to final list.
	 * @param x the object to be added.
	 */
	private void addToFinalList(final ChkNode chkNode, final XDValue x) {
		if (chkNode != null && chkNode._parent != null) {
			//probably in initialization of XDDocument
			chkNode._parent.addToFinalList(x);
		} else  {
			if (_finalList == null) {
				_finalList = new ArrayList<>();
			}
			_finalList.add(x);
		}
	}

	/** Check if the value from argument is assigned to a global variable.
	 * @param xv the value to be checked.
	 * @return true if variable is assigned to a global variable.
	 */
	private boolean isInInGlobals(final XDValue xv) {
		for (int i = 2; i < _globalVariables.length; i++) {
			if (xv == _globalVariables[i]) {
				return true;
			}
		}
		return false;
	}

	/** Close ResultSet object (a database result).
	 * @param x ResultSet object.
	 */
	final void closeResultSet(final XDResultSet x) {
		if (x != null && !x.isClosed() && !isInInGlobals(x)) {
			x.close();
		}
	}

	/** Close items in final list (if present).
	 * @param finalList array of items in final list or null.
	 */
	final void closeFinalList(final List<XDValue> finalList) {
		if (finalList == null) { // if final list not exists, do nothing
			return;
		}
		// 1) close all ResultSet objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_RESULTSET) {
				closeResultSet((XDResultSet) xv);
			}
		}
		// 2) close all Statement objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_STATEMENT && !isInInGlobals(xv)) {
				((XDResultSet) xv).close();
			}
		}
		//3) close all Service objects.
		for (XDValue xv: finalList) {
			if (xv.getItemId() == XD_SERVICE && !isInInGlobals(xv)) {
				((XDService) xv).close();
			}
		}
	}

	/** Check all lists of unresolved references and close XD objects.
	 * @return true if no unresolved references were found.
	 */
	final boolean endXDProcessing() {
		XDDebug debuger = getDebugger();
		if (_debug && debuger != null) { // close debugger
			debuger.closeDebugger();
		}
		closeFinalList(_finalList);
		boolean result = true;
		if (_globalVariables != null) {
			XVariableTable vartab = (XVariableTable) _xd.getXDPool().getVariableTable();
			for (int i = 0; i < _globalVariables.length; i++) {
				XVariable var = vartab.getXVariable(i);
				XDValue val;
				if ((val = _globalVariables[i]) != null && !val.isNull()) {
					short itemId;
					switch (itemId = val.getItemId()) {
						case X_UNIQUESET:
						case X_UNIQUESET_M: {
							result &= ((CodeUniqueset) val).checkAndClear(_reporter); // pending references
							break;
						}
						case XD_SERVICE:
						case XD_STATEMENT:
						case XD_RESULTSET:
							// close all not external database objects
							if (var != null && !var.isExternal()) {
								switch (itemId) {
									case XD_SERVICE: ((XDService) val).close(); break;
									case XD_STATEMENT: ((XDStatement) val).close(); break;
									default: ((XDResultSet) val).close();
								}
							}
							break;
						case XD_INPUT: //close input streams
							if (var != null && !var.isExternal() && !var.getName().equals("$stdIn")) {
								// close if not $stdIn and not external
								((DefInStream) val).close();
							}
							break;
						case XD_OUTPUT: //close out streams
							if (var != null) {
								DefOutStream out = (DefOutStream) val;
								if (var.isExternal() || var.getName().equals("$stdOut")
									|| var.getName().equals("$stdErr")) {
									out.flush(); // external, stdOut and stdErr just flush
								} else {
									out.close(); // other streams close
								}
							}
					}
				}
			}
		}
		return result;
	}

	/** Get value of variable from global variables.
	 * @param index index to variable block.
	 * @return XDValue object or <i>null</i>.
	 */
	final XDValue getVariable(final String name) {
		if (_xd != null) {
			XVariable xv = _xd.findVariable(name);
			if (xv != null) {
				int addr = xv.getOffset();
				if (addr >= 0 && addr < _globalVariables.length) {
					return _globalVariables[addr];
				}
			}
		}
		return null;
	}

	/** Set global value of variable from variables.
	 * @param val XDValue object to be set.
	 * @param var global XVariable.
	 */
	final void setVariable(final XVariable var, final XDValue val) {_globalVariables[var.getOffset()] = val;}

	/** Get temporary reporter.
	 * @return ArrayReporter used as temporary reporter.
	 */
	final ArrayReporter getTemporaryReporter() {return _reporter;}

	/** Set temporary reporter.
	 * @param reporter ArrayReporter to be set as temporary reporter.
	 */
	final void setTemporaryReporter(final ArrayReporter reporter) {_reporter = reporter;}

	/** Set XML output stream.
	 *  @param stream XML output stream to be set.
	 */
	final void setXmlStreamWriter(final XDXmlOutStream stream) {_outWriter = stream;}

	/** Get XML output stream.
	 *  @return the XML output stream.
	 */
	final XDXmlOutStream getXmlStreamWriter() {return _outWriter;}

	/** Get the default uniqueSet (used for ID, IDREF etc).
	 * @return the default uniqueSet object.
	 */
	final CodeUniqueset getIdRefTable() {
		if (_globalVariables[4] == null) {
			 ParseItem[] parseItems = new ParseItem[] {
					new ParseItem("", // no key name
						null, // refName
						-1, // chkAddr,
						0, // itemIndex,
						XD_STRING, // parsedType,
						true) // required item
				}; // optional
			_globalVariables[3] = getParser("QName");
			_globalVariables[4] = new CodeUniqueset(parseItems, new String[0], "");
		}
		return (CodeUniqueset) _globalVariables[4];
	}

	final StringParser getStringParser() {return _textParser;}

	/** Initialize script variables and methods. */
	final void initscript() {
		if (!_initialized2) { //not initialized.
			XVariableTable vt = (XVariableTable) _xd.getXDPool().getVariableTable();
			for (int i = 0; i < _globalVariables.length; i++) {
				//set DefNull(type) to all not initialized global variables.
				XVariable xv = vt.getXVariable(i);
				if (xv == null) {
					continue;
				}
				if (_globalVariables[i] == null) {
					_globalVariables[i] = xv.getType() == X_PARSEITEM || xv.getType() == X_UNIQUESET_KEY
						? new ParseItem(xv.getName(),
							xv.getKeyRefName(),
							xv.getParseMethodAddr(),
							xv.getKeyIndex(),
							xv.getParseResultType(),
							true)
						: DefNull.genNullValue(xv.getType());
				}
			}
			_initialized2 = true; //set initialized
			if (_init >= 0) { // initFrame currencyCode not yet called.
				exec(_init, null); //call initFrame currencyCode
			}
		}
	}
	final void throwInfo(final ChkNode chkNode, final long errCode, final String inf) {
		Report r = Report.error(errCode, inf);
		throw new SRuntimeException(updateReport(r, chkNode));
	}
	final void putError(final ChkNode chkNode, final long id) {putReport(chkNode, Report.error(id));}
	final void putError(final ChkNode chkNode, final long id, final String mod) {
		putReport(chkNode, Report.error(id, mod));
	}
	final void putReport(final ChkNode chkNode, final Report rep) {
		updateReport(rep, chkNode);
		_reporter.putReport(rep);
	}

	/** Execute script code starting from given address.
	 * @param start The index of starting code address.
	 * @param chkEl The actual ChkElement object.
	 * @return XDValue object or null.
	 */
	final XDValue exec(final int start, final ChkElement chkEl) {
		_callList = null; // list of call objects
		_catchItem = null; // catch item
		int pc = start; //program counter
		int sp = -1; //stack index (pointer)
		int step = XDDebug.NOSTEP;
		XDValue item; //actual instruction
		for (;;)
			try {
			if (_debug) {
				if (_debugger.hasStopAddr(pc) || step != XDDebug.NOSTEP) {
					step =_debugger.debug(chkEl,_code,pc,sp,_stack,_localVariables,_debugInfo,_callList,step);
					if (step == XDDebug.KILL) {
						throw new SError(XDEF.XDEF906); //X-definition canceled
					}
				}
			}
			int code;
			switch (code = (item = _code[pc++]).getCode()) {
				case NO_OP: continue; //No operation
				case LD_CONST: _stack[++sp] = item.cloneItem(); continue;
				case LD_CODE: _stack[++sp] = _code[item.getParam()].cloneItem(); continue;
				case LD_TRUE_AND_SKIP: _stack[++sp] = new DefBoolean(true); pc++; continue;
				case LD_LOCAL:{ // load local variable
					XDValue v = _localVariables[item.getParam()];
					_stack[++sp] = v != null ? v : new DefNull();
					continue;
				}
				case LD_XMODEL: { // load xmodel variable
					XDValue v = chkEl == null ? null  : chkEl.loadModelVariable(item.stringValue());
					_stack[++sp] = v != null ? v : new DefNull();
					continue;
				}
				case LD_GLOBAL: { // load global variable
					XDValue val = _globalVariables[item.getParam()];
					if (val == null) {
						XVariable xv = ((XVariableTable)_xd.getXDPool().getVariableTable()).getXVariable(
							item.getParam());
						if (xv.isExternal()) { // null value of external varialble
							putError(chkEl, //Null value of &{0}
								XDEF.XDEF573, "external " + getTypeName(xv.getType()) + " " + xv.getName());
						}
						_stack[++sp] = DefNull.genNullValue(xv.getType());
					} else {
						_stack[++sp] = val;
					}
					continue;
				}
				case ST_LOCAL: _localVariables[item.getParam()] = _stack[sp--];continue;//store local variable
				case ST_XMODEL: chkEl.storeModelVariable(item.stringValue(), _stack[sp--]); continue;
				case ST_GLOBAL: _globalVariables[item.getParam()] = _stack[sp--];continue;//store global var
////////////////////////////////////////////////////////////////////////////////
//stack operations
////////////////////////////////////////////////////////////////////////////////
				case STACK_DUP: _stack[++sp] = _stack[sp - 1]; continue;//Duplicate top of stack
				case POP_OP: _stack[sp--] = null; continue;
				case STACK_SWAP: {
					XDValue x = _stack[sp];
					_stack[sp] = _stack[sp - 1];
					_stack[sp - 1] = x;
					continue;
				}
////////////////////////////////////////////////////////////////////////////////
//unary operators
////////////////////////////////////////////////////////////////////////////////
				case NEG_I: _stack[sp] = new DefLong(- _stack[sp].longValue()); continue;
				case NEG_R: _stack[sp] = new DefDouble(- _stack[sp].doubleValue()); continue;
				case NOT_B: _stack[sp] = new DefBoolean(!_stack[sp].booleanValue()); continue;
				case NEG_BINARY: _stack[sp] = new DefLong(~ _stack[sp].longValue()); continue;
				case INC_I: _stack[sp] = new DefLong(_stack[sp].longValue()+1); continue;//Increase integer
				case INC_R: _stack[sp] = new DefDouble(_stack[sp].doubleValue()+1); continue;
				case DEC_I: _stack[sp] = new DefLong(_stack[sp].longValue()-1); continue;//Decrease integer
				case DEC_R: _stack[sp] = new DefDouble(_stack[sp].doubleValue()-1); continue;
////////////////////////////////////////////////////////////////////////////////
//conversions
////////////////////////////////////////////////////////////////////////////////
				case TO_DECIMAL_X: {
					int i = item.getParam();
					_stack[sp - i] = new DefDecimal(_stack[sp - i].decimalValue());
					continue;
				}
				case TO_BIGINTEGER_X: {
					int i = sp - item.getParam();
					_stack[i] = new DefBigInteger(_stack[i].integerValue());
					continue;
				}
				case TO_FLOAT: _stack[sp] = new DefDouble(_stack[sp].doubleValue()); continue;
				case TO_FLOAT_X: {
					int i = sp - item.getParam(); _stack[i] = new DefDouble(_stack[i].doubleValue());continue;
				}
				case TO_INT_X: {
					int i = sp - item.getParam(); _stack[i] = new DefLong(_stack[i].longValue()); continue;
				}
				case TO_CHAR_X: {
					int i = sp - item.getParam(); _stack[i] = new DefChar(_stack[i].longValue()); continue;
				}
				case NULL_OR_TO_STRING:
					if (_stack[sp] == null || _stack[sp].isNull()) {
						_stack[sp] = new DefString();
						continue;
					}
				case TO_STRING:
					if (_stack[sp] == null || _stack[sp].getItemId() != XD_STRING) {
						_stack[sp] = new DefString(_stack[sp].toString());
					}
					continue;
				case EQUALS_OP: _stack[--sp] = new DefBoolean(_stack[sp].equals(_stack[sp+1])); continue;
				case TO_STRING_X:
					_stack[sp-item.getParam()]=new DefString(_stack[sp-item.getParam()].toString()); continue;
				case TO_MILLIS:_stack[sp]= new DefLong(_stack[sp].datetimeValue().getTimeInMillis());continue;
				case TO_MILLIS_X:
					_stack[sp - item.getParam()] =
						new DefLong(_stack[sp - item.getParam()].datetimeValue().getTimeInMillis());
					continue;
				case TO_BOOLEAN: _stack[sp] = new DefBoolean(_stack[sp].booleanValue()); continue;
				case STACK_TO_CONTAINER: { // create container from stack values
					int n = item.getParam(); // number of stack items
					sp = sp - n + 1;
					_stack[sp] = new DefContainer(_stack, sp, sp + n - 1);
					for (int i = 1; i < n; i++) { //clear used stack items
						_stack[sp + i] = null;
					}
					continue;
				}
				case CREATE_NAMEDVALUE: _stack[sp]= new DefNamedValue(item.stringValue(),_stack[sp]);continue;
////////////////////////////////////////////////////////////////////////////////
//binary operators
				case AND_B:
					_stack[--sp] = new DefBoolean(_stack[sp].booleanValue() & _stack[sp + 1].booleanValue());
					continue;
				case MUL_I:
					_stack[--sp] = new DefLong(_stack[sp].longValue() * _stack[sp + 1].longValue()); continue;
				case MUL_R:
					_stack[--sp] = new DefDouble(_stack[sp].doubleValue() * _stack[sp + 1].doubleValue());
					continue;
				case DIV_I:
					_stack[--sp] = new DefLong(_stack[sp].longValue() / _stack[sp + 1].longValue()); continue;
				case DIV_R:
					_stack[--sp] = new DefDouble(_stack[sp].doubleValue() / _stack[sp + 1].doubleValue());
					continue;
				case ADD_I:
					_stack[--sp] = new DefLong(_stack[sp].longValue() + _stack[sp + 1].longValue()); continue;
				case ADD_R:
					_stack[--sp] = new DefDouble(_stack[sp].doubleValue() + _stack[sp + 1].doubleValue());
					continue;
				case ADD_S: //string concatenation
					_stack[--sp] = new DefString(_stack[sp].toString() + _stack[sp + 1].toString()); continue;
				case SUB_I:
					_stack[--sp] = new DefLong(_stack[sp].longValue() - _stack[sp + 1].longValue()); continue;
				case SUB_R:
					_stack[--sp] =new DefDouble(_stack[sp].doubleValue()-_stack[sp+1].doubleValue());continue;
				case OR_B:
					_stack[--sp] = new DefBoolean(_stack[sp].booleanValue() | _stack[sp + 1].booleanValue());
					continue;
				case XOR_B:
					_stack[--sp] = new DefBoolean(_stack[sp].booleanValue() ^ _stack[sp + 1].booleanValue());
					continue;
				case MOD_I:
					_stack[--sp] = new DefLong(_stack[sp].longValue() % _stack[sp + 1].longValue()); continue;
				case MOD_R:
					_stack[--sp] =new DefDouble(_stack[sp].doubleValue()%_stack[sp+1].doubleValue());continue;
				case LSHIFT_I:/** Left bit shift. */
					_stack[--sp] = new DefLong(_stack[sp].longValue() << _stack[sp + 1].intValue()); continue;
				case RSHIFT_I:/** Right bit shift. */
					_stack[--sp] = new DefLong(_stack[sp].longValue() >> _stack[sp + 1].intValue()); continue;
				case RRSHIFT_I:/** Right bit shift unsigned.*/
					_stack[--sp] = new DefLong(_stack[sp].longValue() >>> _stack[sp + 1].intValue()); continue;
////////////////////////////////////////////////////////////////////////////////
//comparings
////////////////////////////////////////////////////////////////////////////////
				case CMPEQ: _stack[--sp] = new DefBoolean((_stack[sp]).equals(_stack[sp + 1])); continue;
				case CMPNE: _stack[--sp] = new DefBoolean(!(_stack[sp]).equals(_stack[sp + 1])); continue;
				case CMPLE: _stack[--sp] = new DefBoolean((_stack[sp]).compareTo(_stack[sp + 1])<=0);continue;
				case CMPGE: _stack[--sp] = new DefBoolean((_stack[sp]).compareTo(_stack[sp + 1])>=0);continue;
				case CMPLT: _stack[--sp] = new DefBoolean((_stack[sp]).compareTo(_stack[sp + 1])<0);continue;
				case CMPGT: _stack[--sp] = new DefBoolean((_stack[sp]).compareTo(_stack[sp + 1])>0);continue;
////////////////////////////////////////////////////////////////////////////////
// conditional jumps
////////////////////////////////////////////////////////////////////////////////
				case JMPEQ:
					if (_stack[sp - 1].equals(_stack[sp])) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPNE:
					if (!_stack[sp - 1].equals(_stack[sp])) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPLE:
					if (_stack[sp - 1].compareTo(_stack[sp]) <= 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPGE:
					if (_stack[sp - 1].compareTo(_stack[sp]) >= 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPLT:
					if (_stack[sp - 1].compareTo(_stack[sp]) < 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
				case JMPGT:
					if (_stack[sp - 1].compareTo(_stack[sp]) > 0) {
						pc = item.getParam();
					}
					sp -= 2;
					continue;
////////////////////////////////////////////////////////////////////////////////
// jumps and program controls
////////////////////////////////////////////////////////////////////////////////
				case STOP_OP: {
					XDValue result = sp == -1 ? null : _stack[sp]; Arrays.fill(_stack, null); return result;
				}
				case JMP_OP: pc = item.getParam(); continue;
				case JMPF_OP:
					if (!_stack[sp--].booleanValue()) {
						pc = item.getParam();
					}
					continue;
				case JMPT_OP:
					if (_stack[sp--].booleanValue()) {
						pc = item.getParam();
					}
					continue;
				case CALL_OP:
					_callList = new CallItem(pc, _callList, step);
					pc = item.getParam();
					if (step != XDDebug.NOSTEP) {
						step--;
					}
					continue;
				case RET_OP:
					if (_callList == null) {
						Arrays.fill(_stack, null); // clear stack
						return null;
					}
					pc = _callList._returnAddr;
					_localVariables = _callList._parentLocalVariables;
					step = _callList._step;
					_callList = _callList._parent;
					continue;
				case RETV_OP:
					if (_callList == null) {
						XDValue result = sp < 0 ? null : _stack[sp];
						Arrays.fill(_stack, null); // clear stack
						return result;
					}
					pc = _callList._returnAddr;
					_localVariables = _callList._parentLocalVariables;
					step = _callList._step;
					_callList = _callList._parent;
					continue;
				case SWITCH_I: pc = ((CodeSWTableInt) item).getTabAddr(_stack[sp--].longValue()); continue;
				case SWITCH_S: pc = ((CodeSWTableStr) item).getTabAddr(_stack[sp--].stringValue()); continue;
				case INIT_NOPARAMS_OP:
					_localVariables = _callList.init(item.getParam(), _localVariables); continue;
				case INIT_PARAMS_OP: {//init method - parameters
					_localVariables = _callList.init(item.getParam(), _localVariables);
					int numpars = item.intValue();// number of parameters
					sp -= numpars;
					System.arraycopy(_stack, sp+1, _localVariables, 0, numpars);
					continue;
				}
////////////////////////////////////////////////////////////////////////////////
// exceptions try/catch clause
////////////////////////////////////////////////////////////////////////////////
				case SET_CATCH_EXCEPTION:
					_catchItem = new CatchItem(item.getParam(), _localVariables, _catchItem); continue;
				case RELEASE_CATCH_EXCEPTION: _catchItem = _catchItem.release(); continue;
				case THROW_EXCEPTION: {
					if (_catchItem != null) {
						pc = _catchItem.getCatchAddr();
						_stack[0] = _stack[sp];
						sp = 0;
						_localVariables = _catchItem.getVariables();
						_catchItem = _catchItem.getPrevItem();
						continue;
					}
					//X-definition script exception, PC=&{0}&{1}{; }
					Report rep = Report.error(XDEF.XDEF905, pc - 1, ((XDException) _stack[sp--]).toString());
					updateReport(rep, chkEl);
					throw new XXException(rep);
				}
////////////////////////////////////////////////////////////////////////////////
// get values
////////////////////////////////////////////////////////////////////////////////
				case GET_TEXTVALUE: _stack[++sp] = new DefString(chkEl.getTextValue()); continue;
				case ELEMENT_GETTEXT: {
					Element el = (item.getParam() == 1)
						? _stack[sp--].getElement() : chkEl != null ? chkEl.getElement() : null;
					_stack[++sp] = new DefString(el == null ? null : KXmlUtils.getTextValue(el));
					continue;
				}
				case GET_ELEMENT: _stack[++sp] = new DefElement(chkEl.getElement()); continue;
				case GET_ROOTELEMENT: {
					Element el = item.getParam() == 0 ? chkEl.getElemValue() : _stack[sp--].getElement();
					if (el != null) {
						el = el.getOwnerDocument().getDocumentElement();
					}
					_stack[++sp] = new DefElement(el);
					continue;
				}
				case GPS_LATITUDE:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "GPSPosition"); //Null value of &{0}
					}
					_stack[sp] = new DefDouble(((XDGPSPosition) _stack[sp]).latitude());
					continue;
				case GPS_LONGITUDE:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "GPSPosition"); //Null value of &{0}
					}
					_stack[sp] = new DefDouble(((XDGPSPosition)_stack[sp]).longitude());
					continue;
				case GPS_ALTITUDE:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "GPSPosition"); //Null value of &{0}
					}
					_stack[sp] = new DefDouble(((XDGPSPosition) _stack[sp]).altitude());
					continue;
				case GPS_NAME:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "GPSPosition"); //Null value of &{0}
					}
					_stack[sp] = new DefString(((XDGPSPosition) _stack[sp]).name());
					continue;
				case GPS_DISTANCETO:
					if (_stack[sp - 1].isNull() || _stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "GPSPosition"); //Null value of &{0}
					}
					_stack[--sp] =
						new DefDouble(((XDGPSPosition) _stack[sp]).distanceTo(((XDGPSPosition)_stack[sp+1])));
					continue;
				case PRICE_AMOUNT:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Price"); //Null value of &{0}
					}
					_stack[sp] = new DefDecimal(((XDPrice)_stack[sp]).amount());
					continue;
				case PRICE_CURRENCY:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Price"); //Null value of &{0}
					}
					_stack[sp] = ((XDPrice) _stack[sp]).currency();
					continue;
				case PRICE_FRACTDIGITS:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Price"); //Null value of &{0}
					}
					_stack[sp] = new DefLong(((XDPrice) _stack[sp]).fractionDigits());
					continue;
				case PRICE_DISPLAY:
					if (_stack[sp].isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Price"); //Null value of &{0}
					}
					_stack[sp] = new DefString(((XDPrice) _stack[sp]).display());
					continue;
				case COMPILE_BNF: { //Compile BNF grammar
					DefBNFGrammar x = (DefBNFGrammar) item;
					int extndx = x.getParam(); //extension
					String source = x.stringValue();
					try {
						x = new DefBNFGrammar(extndx == -1 ? null
							: (DefBNFGrammar) _globalVariables[extndx], extndx, new SBuffer(source), null);
					} catch (SRuntimeException ex) {
						putReport(chkEl, ex.getReport());
					}
					x.setCode(LD_CONST);
					_stack[++sp] = _code[pc - 1] = x;
					continue;
				}
				case COMPILE_XPATH: { //compile XPath
					DefXPathExpr xp = (DefXPathExpr) item;
					//we MUST recompile this with actual resolvers!!!
					xp = new DefXPathExpr(xp.sourceValue(),
						chkEl.getXXNamespaceContext(), _functionResolver, _variableResolver);
					xp.setCode(LD_CONST); //However, we do it just first time!
					_code[pc - 1] = _stack[++sp] = _code[pc-1] = xp;
					continue;
				}
				case GET_XPATH: {
					Node node;
					if (item.getParam() == 1) {
						if (chkEl == null) {
							node = KXmlUtils.newDocument(); // empty document
						} else if (chkEl.getItemId() != XX_ELEMENT) {
							if ((node = chkEl._node) == null) {
								node = chkEl.getElement();
							}
						} else {
							node = chkEl.getElement();
						}
					} else {// params == 2
						if (_stack[sp].getItemId() == XD_ELEMENT) {
							node = _stack[sp--].getElement();
						} else {
							XDContainer dc = ((XDContainer) _stack[sp--]);
							node = (dc.getXDItemsNumber() > 0 && dc.getXDItem(0).getItemId() == XD_ELEMENT)
								? dc.getXDElement(0) : KXmlUtils.newDocument();
						}
					}
					try {
						DefXPathExpr x;
						if (_stack[sp].getItemId() == XD_XPATH) {
							x = ((DefXPathExpr) _stack[sp]);
						} else {
							x = new DefXPathExpr(_stack[sp].toString(),
								chkEl != null ? chkEl.getXXNamespaceContext() : null,
								_functionResolver,
								_variableResolver);
						}
						_stack[sp] = x.exec(node);
					} catch (SRuntimeException ex) {
						if (chkEl == null) {
							_reporter.putReport(ex.getReport());
						} else {
							putReport(chkEl, ex.getReport());
						}
						_stack[sp] = new DefContainer();
					}
					continue;
				}
				case GET_XPATH_FROM_SOURCE: { // optimized XPath expression
					Element el;
					if (item.getParam() == 2) {
						el = _stack[--sp].getElement();
						_stack[sp] = _stack[sp + 1];
						_stack[sp + 1] = null;
					} else {
						Object obj = chkEl.getCreateContext();
						el = (obj != null && (obj instanceof Element)) ? (Element) obj : chkEl.getElemValue();
					}
					if (item.getParam() == 0) {
						if (el == null) {
							_stack[sp++] = new DefContainer();
							continue;
						}
						switch (chkEl.getItemId()) {
							case XX_ATTR: {
								int ndx = chkEl._xPos.lastIndexOf("/@");
								String s = null;
								if (ndx > 0) {
									Node n = el.getAttributeNode(chkEl._xPos.substring(ndx + 1));
									s = n == null ? null : n.getNodeValue();
								}
								_stack[++sp] = new DefString(s);
								continue;
							}
							case XX_TEXT: _stack[++sp] = new DefString(KXmlUtils.getTextValue(el)); continue;
							default: {
								NodeList nl =
									KXmlUtils.getChildElementsNS(el, null, chkEl.getXXElement().getXXName());
								DefContainer c = new DefContainer(nl);
								if (nl.getLength() == 0) {
									nl = KXmlUtils.getChildElements(el);
									if (nl.getLength() != 0) {
										c.addXDItem(KXmlUtils.firstElementChild(el));
									}
								}
								_stack[++sp] =  c;
								continue;
							}
						}
					} else {
						try {
							DefXPathExpr x;
							if (_stack[sp].getItemId() == XD_XPATH) {
								x = (DefXPathExpr) _stack[sp];
								x.setNamespaceContext(chkEl.getXXNamespaceContext());
								x.setFunctionResolver(_functionResolver);
								x.setVariableResolver(_variableResolver);
							} else {
								x = new DefXPathExpr(_stack[sp].toString(),
									chkEl.getXXNamespaceContext(), _functionResolver, _variableResolver);
								if (_code[pc-2].equals(_stack[sp])) {
									x.setCode(LD_CONST);
									_code[pc-2] = x;
								}
							}
							_stack[sp] = x.exec(el);
						} catch (SRuntimeException ex) {
							putReport(chkEl, ex.getReport());
							_stack[sp] = new DefContainer();
						}
					}
					continue;
				}
				case GET_XQUERY: {//execute xquery
					Node node;
					if (item.getParam() == 1) {
						if (chkEl == null) {
							node = null;
						} else {
							switch (chkEl.getItemId()) {
								case XX_ATTR:
								case XX_TEXT:
									if ((node = chkEl._node) == null) {
										node = chkEl.getElemValue();
									}
									break;
								default: node = chkEl.getElemValue();
							}
						}
					} else { // params == 2
						switch (_stack[sp].getItemId()) {
							case XD_ELEMENT: node = _stack[sp--].getElement(); break;
							case XD_CONTAINER:
								XDContainer dc = ((XDContainer) _stack[sp--]);
								if (dc.getXDItemsNumber() > 0 && dc.getXDItem(0).getItemId() == XD_ELEMENT) {
									node = dc.getXDElement(0);
								} else {
									_stack[sp]= new DefContainer();
									continue;
								}
								break;
							default:
								DefXQueryExpr x = _stack[sp].getItemId()==XD_XQUERY
									? (DefXQueryExpr) _stack[sp] : new DefXQueryExpr(_stack[sp].toString());
								_stack[--sp] = x.exec(_stack[sp].getElement(), chkEl);
								continue;
						}
					}
					try {
						DefXQueryExpr x = _stack[sp].getItemId() == XD_XQUERY
							? (DefXQueryExpr) _stack[sp] : new DefXQueryExpr(_stack[sp].toString());
						_stack[sp] = x != null ? x.exec(node, chkEl) : new DefContainer();
					} catch (SRuntimeException ex) {
						if (chkEl == null) {
							_reporter.putReport(ex.getReport());
						} else {
							chkEl.putReport(ex.getReport());
						}
						_stack[sp] = new DefContainer();
					}
					continue;
				}
				case GETATTR_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkEl.getCreateContext();
						el = obj != null && (obj instanceof Element) ? (Element) obj : chkEl.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					Node node;
					if ((node = el) != null) {
						String s;
						if ((s = item.stringValue()).charAt(0) == '{') { // namespace
							int ndx = s.lastIndexOf('}');
							node = el.getAttributeNodeNS(s.substring(1, ndx), s.substring(ndx+1));
						} else { // no namespace
							node = el.getAttributeNode(s);
						}
					}
					_stack[++sp] = new DefContainer(node);
					continue;
				}
				case GETELEM_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkEl.getCreateContext();
						el = obj != null && (obj instanceof Element) ? (Element) obj : chkEl.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					Node node;
					if ((node = el) != null) {
						String s;
						int ndx = (s = item.stringValue()).lastIndexOf('}');
						if (ndx < 0) { // no namespaceURI
							node = el.getNodeName().equals(s) && el.getNamespaceURI() == null ? el : null;
						} else { // namespaceURI
							node = el.getLocalName().equals(s.substring(ndx + 1))
								&& s.substring(1, ndx).equals(el.getNamespaceURI()) ? el : null;
						}
					}
					_stack[++sp] = new DefContainer(node);
					continue;
				}
				case GETELEMS_FROM_CONTEXT: {
					Element el;
					if (item.getParam() == 1) {
						Object obj = chkEl.getCreateContext();
						el = obj != null && (obj instanceof Element) ? (Element) obj : chkEl.getElemValue();
					} else {
						el = _stack[sp--].getElement();
					}
					if (el == null) {
						_stack[++sp] = new DefContainer();
					} else {
						String s;
						int ndx = (s = item.stringValue()).lastIndexOf('}');
						NodeList nl = ndx < 0 ? KXmlUtils.getChildElements(el, s)
							: KXmlUtils.getChildElementsNS(el, s.substring(1, ndx), s.substring(ndx + 1));
						_stack[++sp] = new DefContainer(nl);
					}
					continue;
				}
				case COMPILE_REGEX: _stack[sp] = new XDRegex(_stack[sp].toString(), false); continue;
				case CHAR_AT: { // charAt
					int i = _stack[sp--].intValue();
					String s = _stack[sp].stringValue();
					_stack[sp] = new DefChar(s.charAt(i));
					continue;
				}
				case CONTAINS: {
					String s = _stack[sp--].stringValue();
					String t = _stack[sp].stringValue();
					_stack[sp] = new DefBoolean((s == null || t == null) ? false : t.contains(s));
					continue;
				}
				case CONTAINSI: {
					String s = _stack[sp--].stringValue();
					String t = _stack[sp].stringValue();
					_stack[sp] = new DefBoolean((s == null || t == null)
						? false : t.toLowerCase().indexOf(s.toLowerCase()) >= 0);
					continue;
				}
				case IS_NUM: {
					boolean result = true;
					String s = _stack[sp].toString();
					for (int i = 0; i < s.length(); i++) {
						if (!Character.isDigit(s.charAt(i))) {
							result = false;
							break;
						}
					}
					_stack[sp] = new DefBoolean(result);
					continue;
				}
				case IS_INT:
					try {
						String s = _stack[sp].toString();
						Long.valueOf(s.startsWith("+") ? s.substring(1) : s);
						_stack[sp] = new DefBoolean(true);
						continue;
					} catch (NumberFormatException ex) {
						_stack[sp] = new DefBoolean(false);
						continue;
					}
				case IS_FLOAT:
					try {
						Double.valueOf(_stack[sp].toString());
						_stack[sp] = new DefBoolean(true);
						continue;
					} catch (NumberFormatException ex) {
						_stack[sp] = new DefBoolean(false);
						continue;
					}

				case IS_DATETIME:
					if (item.getParam() == 1) {
						_textParser.setSourceBuffer(_stack[sp].stringValue());
						int i;
						_stack[sp] = new DefBoolean(_textParser.isISO8601Datetime()
							&& _textParser.eos() && _textParser.testParsedDatetime()
							&& ((i=_textParser.getParsedSDatetime().getYear()) == Integer.MIN_VALUE
								|| i > 1800 && i <= 3000));
					} else {
						_textParser.setSourceBuffer(_stack[--sp].stringValue());
						int i;
						_stack[sp] = new DefBoolean(_textParser.isDatetime(_stack[sp+1].stringValue())
							&& _textParser.eos() && _textParser.testParsedDatetime()
							&& ((i=_textParser.getParsedSDatetime().getYear()) == Integer.MIN_VALUE
								|| i > 1800 && i <= 3000));
					}
					continue;
				case EQUALSI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? false : t.equalsIgnoreCase(s));
					continue;
				}
				case STARTSWITH: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? false : t.startsWith(s));
					continue;
				}
				case STARTSWITHI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null || s.length() > t.length()
						? false : s.equalsIgnoreCase(t.substring(0, s.length())));
					continue;
				}
				case ENDSWITH: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? false : t.endsWith(s));
					continue;
				}
				case ENDSWITHI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null || s.length() > t.length()
						? false : s.equalsIgnoreCase(t.substring(t.length() - s.length())));
					continue;
				}
				case CHK_GT: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? false : t.compareTo(s) > 0);
					continue;
				}
				case CHK_LT: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? true : t.compareTo(s) < 0);
					continue;
				}
				case CHK_GE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? false : t.compareTo(s) >= 0);
					continue;
				}
				case CHK_LE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? true : t.compareTo(s) <= 0);
					continue;
				}
				case CHK_NE: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? s != null : !t.equals(s));
					continue;
				}
				case CHK_NEI: {
					String s = _stack[sp].stringValue();
					String t = _stack[--sp].stringValue();
					_stack[sp] = new DefBoolean(t == null ? s != null : !s.equalsIgnoreCase(t));
					continue;
				}
				case GET_USEROBJECT: _stack[++sp] = new DefObject(chkEl.getUserObject()); continue;
				case SET_USEROBJECT:
					chkEl.setUserObject(_stack[sp].getObject());
					_stack[sp--] = null;
					continue;
				case UNIQUESET_NEWINSTANCE: {
					CodeXD x = (CodeXD) item;
					CodeUniqueset u = (CodeUniqueset) x.getParam2().cloneItem();
					_stack[++sp] = new CodeUniqueset(u.getParsedItems(), u.getVarNames(), u.getName());
					continue;
				}
				case UNIQUESET_M_NEWKEY: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp--];
					for (ParseItem i : u.getParsedItems()) {
						i.setParsedObject(null);
					}
					continue;
				}
				case UNIQUESET_KEY_LOAD: ((CodeUniqueset) _stack[sp]).setKeyIndex(item.getParam()); continue;
				case UNIQUESET_ID:
				case UNIQUESET_SET:
				case UNIQUESET_IDREF:
				case UNIQUESET_CHKID:
				case UNIQUESET_M_ID:
				case UNIQUESET_M_SET:
				case UNIQUESET_M_IDREF:
				case UNIQUESET_M_CHKID:
				case UNIQUESET_KEY_ID:
				case UNIQUESET_KEY_SET:
				case UNIQUESET_KEY_IDREF:
				case UNIQUESET_KEY_CHKID:
				case UNIQUESET_KEY_SETKEY: {
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					if (code != UNIQUESET_ID && code != UNIQUESET_IDREF && code != UNIQUESET_CHKID
						&& code != UNIQUESET_SET) {
						dt.setKeyIndex(item.getParam());
					}
					if (code != UNIQUESET_M_CHKID && code != UNIQUESET_M_IDREF && code != UNIQUESET_M_SET
						&& code != UNIQUESET_M_ID) {
						CodeUniqueset assumed = execUniqueParser(dt,sp,chkEl);
						_stack[sp] = chkEl._parseResult;
						if (code == UNIQUESET_KEY_SETKEY || chkEl._parseResult.errors()) {
							continue; // just set key
						}
						if (assumed != null) {
							execUniqueOperation(assumed, chkEl, code);
						}
					}
					execUniqueOperation(dt, chkEl, code);
					continue;
				}
				case UNIQUESET_M_SIZE: _stack[sp] = new DefLong(((CodeUniqueset) _stack[sp]).size());continue;
				case UNIQUESET_M_TOCONTAINER:
					_stack[sp] = ((CodeUniqueset) _stack[sp]).getUniqueSetItems(); continue;
				case UNIQUESET_GET_ACTUAL_KEY:
					_stack[sp] = ((CodeUniqueset)_stack[sp]).getActualKey();
					if (_stack[sp] == null || _stack[sp].isNull()) {
						//The key is not in the uniqueSet
						Report rep = Report.error(XDEF.XDEF538, pc-1, ((XDException)_stack[sp--]).toString());
						updateReport(rep, chkEl);
						_stack[sp] = DefNull.NULL_VALUE;
					}
					continue;
				case UNIQUESET_KEY_RESET: {
					XDUniqueSetKey usk = (XDUniqueSetKey)_stack[sp--];
					if (usk == null || usk.isNull() || !usk.resetKey()) {
						//Can't reset uniqueSetKey or it is null
						Report rep = Report.error(XDEF.XDEF540, pc-1, ((XDException)_stack[sp--]).toString());
						updateReport(rep, chkEl);
					}
					continue;
				}
				case UNIQUESET_IDREFS:
				case UNIQUESET_CHKIDS: {
					String s = chkEl.getTextValue().trim();
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					dt.setKeyIndex(item.getParam());
					DefParseResult p = new DefParseResult(s);
					ArrayReporter reporter = new ArrayReporter();
					int ndx = 0;
					DefContainer val = new DefContainer();
					while (ndx < s.length()) {
						String t;
						int ndx1;
						if (s.charAt(ndx) == '\'') {
							ndx1 = s.indexOf('\'', ndx+1);
							boolean doubleapos = false;
							while (ndx1+1 < s.length() && s.charAt(ndx1+1) == '\'') {
								ndx1 = s.indexOf('\'', ndx1+2);
								doubleapos = true;
							}
							if (ndx1 < ndx) {
								t = s.substring(ndx);
								ndx1 = s.length();
							} else {
								t = s.substring(ndx+1, ndx1);
							}
							if (doubleapos) {
								t = t.replaceAll("''", "'");
							}
						} else {
							ndx1 = s.indexOf(' ', ndx);
							if (ndx1 < ndx) {
								t = s.substring(ndx);
								ndx1 = s.length();
							} else {
								t = s.substring(ndx, ndx1);
							}
						}
						for (ndx = ndx1+1; ndx < s.length() && s.charAt(ndx) == ' '; ndx++);
						chkEl.setTextValue(t);
						execUniqueParser(dt, sp, chkEl);
						XDValue v = chkEl._parseResult;
						val.addXDItem(v);
						if (chkEl._parseResult.errors()) {
							reporter.addAll(chkEl._parseResult.getReporter());
						} else {
							ArrayReporter list = dt.chkId();
							if (list != null) {
								Report rep = Report.error(XDEF.XDEF522, //Unique value "&{0}" was not set
									(dt.getName() != null ? dt.getName() + " " : "") + v);
								updateReport(rep, chkEl);
								if (code == UNIQUESET_IDREFS) {
									list.putReport(rep);
								} else {
									reporter.putReport(rep);
								}
							}
						}
					}
					chkEl.setTextValue(s);
					p.setParsedValue(val);
					p.addReports(reporter);
					_stack[sp] = chkEl._parseResult = p;
					continue;
				}
				case UNIQUESET_KEY_NEWKEY: {
					CodeUniqueset dt = (CodeUniqueset) _stack[sp];
					_stack[sp--] = null;
					ParseItem[] o = dt.getParsedItems();
					o[item.getParam()].setParsedObject(null);
					continue;
				}
				case UNIQUESET_BIND: {
					int npar = item.getParam();
					chkEl._boundKeys = new XDUniqueSetKey[npar];
					for (int i = 0; i < npar; i++) {
						XDUniqueSet v = (XDUniqueSet) _stack[sp + i];
						chkEl._boundKeys[i] = v.getActualKey();
					}
					sp -= npar;
					continue;
				}
				case UNIQUESET_CLOSE: // Report unresolved Id references and clear list.
					_stack[sp] = new DefBoolean(((CodeUniqueset) _stack[sp]).checkAndClear(_reporter));
					continue;
				case UNIQUESET_CHEKUNREF: {
					CodeUniqueset x = (CodeUniqueset) _stack[sp--];
					x.setMarker(chkEl);
					chkEl._markedUniqueSets.add(x);
					continue;
				}
				case UNIQUESET_SETVALUEX: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp--];
					String s = ((CodeS1)item).stringValue();
					if (!u.setNamedValue(s, _stack[sp--])) {
						//The uniqueSet item not exists, value "{0}" was not set
						putError(chkEl, XDEF.XDEF537, s);
					}
					continue;
				}
				case UNIQUESET_GETVALUEX: {
					CodeUniqueset u = (CodeUniqueset) _stack[sp];
					_stack[sp] = u.getNamedValue(((CodeS1)item).stringValue());
					continue;
				}
				case DEFAULT_ERROR: // put default error
					putError(chkEl, XDEF.XDEF515); //Incorrect value&{0}{: }
					_stack[++sp] = new DefBoolean(false); //returns always false
					continue;
				case ATTR_EXIST:
					if (chkEl.getElemValue() == null) {
						_stack[++sp] = new DefBoolean(false);
					} else {
						String s = item.stringValue();
						if (s.charAt(0) == '{'){
							int i = s.lastIndexOf('}');
							_stack[++sp] = new DefBoolean(
								chkEl.getElemValue().hasAttributeNS(s.substring(1, i), s.substring(i + 1)));
						} else {
							_stack[++sp] = new DefBoolean(chkEl.getElemValue().hasAttribute(s));
						}
					}
					continue;
				case ATTR_REF:
					if (chkEl.getElemValue() == null) {
						_stack[++sp] = DefNull.genNullValue(XD_STRING);
					} else {
						String s;
						Node n;
						if ((s = item.stringValue()).charAt(0) == '{'){
							int i = s.lastIndexOf('}');
							n = chkEl.getElemValue().getAttributeNodeNS(s.substring(1, i), s.substring(i+1));
						} else {
							n = chkEl.getElemValue().getAttributeNode(s);
						}
						_stack[++sp] =
							n == null ? DefNull.genNullValue(XD_STRING) : new DefString(n.getNodeValue());
					}
					continue;
////////////////////////////////////////////////////////////////////////////////
//other methods
////////////////////////////////////////////////////////////////////////////////
				case OUT_STREAM:
				case OUTLN_STREAM: {
					XDOutput out = (XDOutput) _globalVariables[0];
					if (item.getParam() == 1) {
						if (out != null) {
							out.writeString(_stack[sp--].toString());
							if (code == OUTLN_STREAM){
								out.writeString("\n");
							}
						} else {
							sp--;
						}
					} else if (out != null) {
						out.writeString("\n");
					}
					continue;
				}
				case OUT1_STREAM:
				case OUTLN1_STREAM: {
					if (item.getParam() == 2) {
						XDOutput out = (XDOutput) _stack[sp - 1];
						out.writeString(_stack[sp].toString());
						if (code == OUTLN1_STREAM) {
							out.writeString("\n");
						}
						sp -= 2;
					} else {
						((XDOutput)_stack[sp--]).writeString("\n");
					}
					continue;
				}
				case FORMAT_STRING: {
					int npar = item.getParam();
					XDValue v =  _stack[sp - npar + 1];
					int ndx;
					Locale loc;
					String mask;
					if (v.getItemId() == XD_LOCALE) {
						loc = ((DefLocale) v).getLocale();
						mask = _stack[sp - npar + 2].toString();
						ndx = 1;
					} else {
						loc = Locale.US;
						mask = v.toString();
						ndx = 0;
					}
					Object[] pars = new Object[npar - 1 - ndx];
					for (int i = 0; i < npar - 1 - ndx; i++) {
						v = _stack[sp - npar + 2 + i + ndx];
						if (v == null || v.isNull()) {
							pars[i] = null;
						} else {
							switch (v.getItemId()) {
								case XD_LONG: pars[i] = v.longValue(); break;
								case XD_DOUBLE: pars[i] = v.doubleValue(); break;
								case XD_DATETIME: pars[i] = v.datetimeValue().getCalendar(); break;
								default: pars[i] = v.stringValue();
							}
						}
					}
					_stack[sp = sp - npar + 1] = new DefString(String.format(loc, mask, pars));
					continue;
				}
				case PRINTF_STREAM: {
					int npar = item.getParam();
					XDValue v = _stack[sp - npar + 1];
					int ndx;
					XDOutput out;
					if (v.getItemId() != XD_OUTPUT) {
						out = (XDOutput) _globalVariables[0];
						ndx = 1;
					} else {
						out = (XDOutput) _stack[sp - npar + 1];
						ndx = 2;
					}
					Locale loc;
					if (_stack[sp - npar + ndx].getItemId() == XD_LOCALE) {
						loc = ((DefLocale) _stack[sp - npar + ndx]).getLocale();
						ndx++;
					} else {
						loc = Locale.US;
					}
					String mask = _stack[sp - npar + ndx].toString();
					Object[] pars = new Object[npar - ndx];
					for (int i = 0; i < npar - ndx; i++) {
						v = _stack[ndx + i];
						if (v == null || v.isNull()) {
							pars[i] = null;
						} else {
							switch (v.getItemId()) {
								case XD_LONG: pars[i] = v.longValue(); break;
								case XD_DOUBLE: pars[i] = v.doubleValue(); break;
								case XD_DATETIME: pars[i] = v.datetimeValue().getCalendar(); break;
								default: pars[i] = v.stringValue();
							}
						}
					}
					sp -= npar;
					out.writeString(String.format(loc, mask, pars));
					continue;
				}
				case SET_ATTR: {//set attribute
					String ns = item.getParam() == 3 ? _stack[sp--].toString() : null;
					String s = _stack[sp--].stringValue();
					Element e;
					if ((e = chkEl.getElemValue()) == null) {
						sp--;
					} else {
						String name = _stack[sp--].toString();
						if (ns == null) {
							if (chkEl._node != null && chkEl._node.getNodeType() == Node.ATTRIBUTE_NODE
								&& name.equals(chkEl._node.getNodeName())) {
								chkEl.setTextValue(s);
							} else {
								if (name.equals("xmlns") || name.startsWith("xmlns:")) {
									e.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, name, s);
								} else {
									e.setAttribute(name, s);
								}
							}
						} else {
							if (chkEl._node != null && chkEl._node.getNodeType() == Node.ATTRIBUTE_NODE
								&& name.equals(chkEl._node.getLocalName()) &&
								ns.equals(chkEl._node.getNamespaceURI())) {
								chkEl.setTextValue(s);
							} else {
								e.setAttributeNS(ns, name, s);
							}
						}
					}
					continue;
				}
				case HAS_ATTR: {//check attribute exists
					String ns =
						item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e;
					_stack[sp] = (e = chkEl.getElemValue()) == null ? new DefBoolean(false)
						: new DefBoolean(ns == null ? e.hasAttribute(_stack[sp].stringValue())
							: e.hasAttributeNS(ns, _stack[sp].stringValue()));
					continue;
				}
				case DEL_ATTR: {//delete attribute
					String ns = item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e;
					if ((e = chkEl.getElemValue()) == null) {
						sp--;
					} else {
						String name = _stack[sp--].toString();
						if (ns == null) {
							if (chkEl._node != null && chkEl._node.getNodeType() ==  Node.ATTRIBUTE_NODE
								&& name.equals(chkEl._node.getNodeName())) {
								chkEl.setTextValue(null);
							} else {
								e.removeAttribute(name);
							}
						} else {
							if (chkEl._node != null && chkEl._node.getNodeType() == Node.ATTRIBUTE_NODE
								&& name.equals(chkEl._node.getLocalName()) &&
								ns.equals(chkEl._node.getNamespaceURI())) {
								chkEl.setTextValue(null);
							} else {
								e.removeAttributeNS(ns, name);
							}
						}
					}
					continue;
				}
				case GET_OCCURRENCE: _stack[++sp] = new DefLong(chkEl.getOccurrence()); continue;
				case GET_IMPLROPERTY: {
					String s;
					if (item.getParam() == 1) {
						s = _xd.getImplProperty(_stack[sp].toString());
					} else {
						s = _stack[sp--].toString();
						XDefinition xd = (XDefinition) _xd.getXDPool().getXMDefinition(_stack[sp].toString());
						s = xd == null ? null : xd.getImplProperty(s);
					}
					_stack[sp] = s==null ? new DefString("") : new DefString(s);
					continue;
				}
				case DEBUG_TRACE:
				case DEBUG_PAUSE: {
					if (_debug && _debugger != null) {
						step = _debugger.debug(chkEl,
							_code,
							pc - 1, // pc is already increased
							sp,
							_stack,
							_localVariables,
							_debugInfo, _callList,
							step);
					}
					sp -= item.getParam();
					continue;
				}
				case PUT_ERROR: //error message to stdErr.
					switch (item.getParam()) {
						case 1:
							if (_stack[sp].getItemId() == XD_REPORT) {
								Report r = ((XDReport)_stack[sp]).reportValue(); // report is in argument
								_reporter.putReport(Report.error(r.getMsgID(),
									r.getText(), r.getModification()));
							} else { // text of report
								_reporter.putReport(Report.error(null, _stack[sp].stringValue())); // txt
							}
							break;
						case 2:  // error(id, txt)
							putReport(chkEl,
								Report.error(_stack[--sp].stringValue(), _stack[sp+1].stringValue()));
							break;
						default: // // error(id, txt, modif)
							putReport(chkEl, Report.error(_stack[sp-=2].stringValue(), //id
								_stack[sp+1].stringValue(), _stack[sp+2].stringValue()));
					}
					_stack[sp] = new DefBoolean(false);
					continue;
				case PUT_ERROR1: //Put error message to stdErr.
					switch (item.getParam()) {
						case 2: // error(txt);
							((XDOutput)_stack[--sp]).putReport(Report.error(null,_stack[sp+1].stringValue()));
							break;
						case 3: // error(id, txt);
							((XDOutput) _stack[sp-=2]).putReport(Report.error(_stack[sp+1].stringValue(),
								_stack[sp+2].stringValue()));
							break;
						default:  // error(id, txt, modif);
							((XDOutput)_stack[sp-=3]).putReport(Report.error(_stack[sp+1].stringValue(),
								_stack[sp+2].stringValue(), _stack[sp+3].stringValue()));
					}
					_stack[sp] = new DefBoolean(false);
					continue;
				case GET_NUMOFERRORS: //Get number of errors.
					_stack[++sp] = new DefLong(chkEl.getReporter().getErrorCount()
						+ _reporter.getErrorCount() - chkEl._errCount);
					continue;
				case GET_NUMOFERRORWARNINGS:
					_stack[++sp] = new DefLong(chkEl.getReporter().getErrorCount()
						+ _reporter.getErrorCount() - chkEl._errCount + chkEl.getReporter().getWarningCount()
						+ _reporter.getWarningCount());
					continue;
				case CLEAR_REPORTS: //clear temp reports
					try {// clear all reports if it is called from _onXmlError
						if (chkEl._parent==chkEl._rootChkDocument && _reporter.toString().contains("XML080")){
							chkEl.getReporter().getReportWriter().clear();
						}
					} catch (Exception ex) {}
					_reporter.clear();
					continue;
				case SET_TEXT: {//set string value
					XDValue x =_stack[sp--];
					String s = x == null || x.isNull() ? null : x.toString();
					chkEl.setTextValue(s);
					continue;
				}
				case SET_ELEMENT: {//set actual element
					Element el;
					if (_stack[sp].getItemId() == XD_ELEMENT) {
						el = ((DefElement) _stack[sp--]).getElement();
					} else {
						XDValue x = _stack[sp--];
						if (x == null || x.isNull()) {
							el = null;
						} else {
							XDContainer c = (XDContainer) x;
							el = (c.getXDItemsNumber() > 0 && c.getXDItem(0).getItemId() == XD_ELEMENT)
								? c.getXDItem(0).getElement() : null;
						}
					}
					if (el == null) {
						//Required element is missing in setElement method
						putError(chkEl, XDEF.XDEF529);
						el = chkEl.getDocument().createElementNS(null, UNDEF_ID);
					} else {
						if (el.getOwnerDocument() != chkEl.getDocument()) {
							el = (Element) chkEl.getDocument().importNode(el, true);
						}
					}
					chkEl.getChkElement().updateElement(el);
					chkEl.setElemValue(el);
					continue;
				}
				case REMOVE_TEXT: chkEl.setTextValue(null); continue;//Remove actual text node or attribute
				case GET_NOW: {//set actual date and time
					SDatetime d = new SDatetime(new GregorianCalendar());
					TimeZone tz = getDefaultZone();
					if (tz != null) {
						d.setTZ(tz);
					}
					_stack[++sp] = new DefDate(new SDatetime(d));
					continue;
				}
				case GET_DEFAULTZONE: {
					TimeZone tz = getDefaultZone();
					_stack[++sp] = new DefString(tz == null ? null : tz.getID());
					continue;
				}
				case CONTEXT_GETELEMENTS: //getElements(container)
					if (item.getParam() == 2) {
						_stack[--sp] = ((XDContainer) _stack[sp]).getXDElements(_stack[sp+1].stringValue());
					} else {
						_stack[sp] = ((XDContainer) _stack[sp]).getXDElements();
					}
					continue;
				case CONTEXT_GETELEMENT_X: { //getElement(container, index)
					int i = item.getParam() == 2 ? _stack[sp--].intValue() : 0;
					Element elem = ((XDContainer) _stack[sp]).getXDElement(i);
					if (elem == null) {
						elem = chkEl._rootChkDocument._doc.createElementNS(null, UNDEF_ID);
					}
					_stack[sp] = new DefElement(elem);
					continue;
				}
				case CONTEXT_GETTEXT: //getText(container)
					if (item.getParam() == 2) {
						int i = _stack[sp--].intValue();
						_stack[sp] = new DefString(((XDContainer) _stack[sp]).getXDTextItem(i));
					} else {
						_stack[sp] = new DefString(((XDContainer) _stack[sp]).getXDText());
					}
					continue;
				case CONTEXT_GETLENGTH: //getLength(container)
					_stack[sp] = new DefLong(((XDContainer) _stack[sp]).getXDItemsNumber()); continue;
				case CONTEXT_SORT: //container.sort()
					switch (item.getParam()) {
						case 2: ((XDContainer) _stack[--sp]).sortXD(_stack[sp+1].toString(), true); continue;
						case 3:
							((XDContainer) _stack[sp -= 2]).sortXD(_stack[sp+1].toString(),
								_stack[sp+2].booleanValue());
							continue;
						default: ((XDContainer) _stack[sp]).sortXD(null, true);
					}
					continue;
				case CONTEXT_ADDITEM: //container.add(obj);
					sp -= 2;
					((XDContainer) _stack[sp + 1]).addXDItem(_stack[sp + 2]);
					continue;
				case CONTEXT_REMOVEITEM: //container.remove(index);
					_stack[--sp] = ((XDContainer) _stack[sp]).removeXDItem(_stack[sp+1].intValue()); continue;
				case CONTEXT_ITEM: { //container.getXDItem(index)
					XDValue x = ((XDContainer) _stack[--sp]).getXDItem(_stack[sp + 1].intValue());
					_stack[sp] = (x == null) ?  new DefNull() : x;
					continue;
				}
				case CONTEXT_REPLACEITEM: //container.rreplace(index, value)
					sp -= 2;
					_stack[sp]= ((XDContainer)_stack[sp]).replaceXDItem(_stack[sp+1].intValue(),_stack[sp+2]);
					continue;
				case CONTEXT_TO_ELEMENT: {
					String uri = null;
					String name = null;
					if (item.getParam() >= 2) {
						name = _stack[sp--].stringValue();
						if (item.getParam() == 3) {
							uri = _stack[sp--].stringValue();
						}
					}
					XDContainer x = (XDContainer) _stack[sp];
					_stack[sp] = new DefElement(x.toElement(uri, name));
					continue;
				}
				case CONTEXT_ITEMTYPE:{ //container.temType(index)
					int index = _stack[sp--].intValue();
					_stack[sp] = new DefLong(((XDContainer)_stack[sp]).getXDItem(index).getItemId());
					continue;
				}
				case GET_RESULTSET_ITEM: {
					String s = _stack[sp--].stringValue();
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkEl);
					}
					_stack[sp] = new DefString(it.itemAsString(s));
					continue;
				}
				case HAS_RESULTSET_ITEM: {
					String s = _stack[sp--].stringValue();
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkEl);
					}
					_stack[sp] = new DefBoolean(it.itemAsString(s) != null);
					continue;
				}
				case HAS_RESULTSET_NEXT: {
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkEl);
					}
					_stack[sp] = new DefBoolean(it.lastXDItem() != null);
					continue;
				}
				case RESULTSET_NEXT: {
					XDResultSet it = (XDResultSet) _stack[sp];
					if (it.getCount() == 0) {
						it.nextXDItem(chkEl);
					}
					_stack[sp] = new DefBoolean(it.nextXDItem(chkEl) != null);
					continue;
				}
				case GET_RESULTSET_COUNT:
					_stack[sp] = new DefLong(((XDResultSet) _stack[sp]).getCount()); continue;
				case GET_XPOS: _stack[++sp] = new DefString(chkEl.getXPos()); continue;
				case DB_PREPARESTATEMENT: {
					String query = _stack[sp--].stringValue();
					XDValue dv = _stack[sp];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Service"); //Null value of &{0}
						continue; //never execute
					}
					_stack[sp] = ((XDService) dv).prepareStatement(query);
					addToFinalList(chkEl, _stack[sp]);
					continue;
				}
				case GET_DBQUERY: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv = _stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Service"); //Null value of &{0}
						continue; //never execute
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					switch (xtype) {
						case XD_SERVICE: {
							String query = _stack[sp--].stringValue();
							XDService conn = (XDService) dv;
							_stack[sp] = conn.query(query, params);
							break;
						}
						case XD_STATEMENT: {
							XDStatement ds = (XDStatement) dv;
							_stack[sp] = ds.query(params);
							break;
						}
						case XD_STRING: { // ???
							XDStatement ds = (XDStatement) dv;
							_stack[sp] = ds.query(params);
							break;
						}
						default: throwInfo(chkEl, XDEF.XDEF561, null); //XQuery expression error
					}
					addToFinalList(chkEl, _stack[sp]);
					continue;
				}
				case GET_DBQUERY_ITEM: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, //Null value of &{0}
							dv == null || dv.getItemId() != XD_STATEMENT ? "Service" : "Statement");
						continue;
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 3 : 2);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					switch (xtype) {
						case XD_SERVICE: {
							String itemName = _stack[sp--].stringValue();
							String query = _stack[sp--].stringValue();
							XDService conn = (XDService) dv;
							_stack[sp] = conn.queryItems(query,itemName,params);
							break;
						}
						case XD_STATEMENT: {
							String itemName = _stack[sp--].stringValue();
							XDStatement ds = (XDStatement) dv;
							_stack[sp] = ds.queryItems(itemName, params);
							break;
						}
						default: throwInfo(chkEl, XDEF.XDEF561, null); //XQuery expression error
					}
					addToFinalList(chkEl, _stack[sp]);
					continue;
				}
				case HAS_DBITEM: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, //Null value of &{0}
							dv == null || dv.getItemId() != XD_STATEMENT ? "Service" : "Statement");
						continue;  //never execute
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDResultSet di;
					switch (xtype) {
						case XD_SERVICE: di=((XDService) dv).query(_stack[sp--].stringValue(), params); break;
						case XD_STATEMENT: di = ((XDStatement) dv).query(params); break;
						case XD_RESULTSET: di = (XDResultSet) dv; break;
						default: //???? this happens
							throwInfo(chkEl, XDEF.XDEF561, null); //XQuery expression error
							di = null; //never execute
					}
					_stack[sp] = new DefBoolean(di.nextXDItem(chkEl) != null);
					di.close();
					continue;
				}
				case DB_EXEC: {
					int npar = item.getParam();
					XDValue params;
					XDValue dv =_stack[sp - (npar - 1)];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, //Null value of &{0}
							dv == null || dv.getItemId()!=XD_STATEMENT ? "Service" : "Statement");
						continue;  // never execute
					}
					short xtype = dv.getItemId();
					int nx = npar - (xtype == XD_SERVICE ? 2 : 1);
					if (nx > 0) {
						if (nx == 1) {
							params = _stack[sp--];
						} else {
							params = new DefContainer(_stack, sp - nx + 1, sp);
							sp -= nx;
						}
					} else {
						params = null;
					}
					XDStatement ds;
					switch (xtype) {
						case XD_SERVICE:
							ds = ((XDService) dv).prepareStatement(_stack[sp--].stringValue()); break;
						case XD_STATEMENT: ds = (XDStatement) dv; break;
						default:
							throwInfo(chkEl, XDEF.XDEF573, "not Statement"); //Null value of &{0}
							ds = null; // never execute
					}
					_stack[sp] = ds.execute(params);
					addToFinalList(chkEl, _stack[sp]);
					continue;
				}
				case DB_CLOSE: {
					XDValue dv =_stack[sp--];
					if (dv != null && !dv.isNull()) {
						switch (dv.getItemId()) {
							case XD_SERVICE: ((XDService) dv).close(); continue;
							case XD_STATEMENT: ((XDStatement) dv).close(); continue;
							case XD_RESULTSET: ((XDResultSet) dv).close(); continue;
						}
					}
					continue;
				}
				case DB_ISCLOSED: {
					XDValue dv =_stack[sp];
					boolean b = true;
					if (dv != null && !dv.isNull()) {
						switch (dv.getItemId()) {
							case XD_SERVICE: b = ((XDService) dv).isClosed(); break;
							case XD_STATEMENT: b = ((XDStatement) dv).isClosed(); break;
							case XD_RESULTSET: b = ((XDResultSet) dv).isClosed(); break;
						}
					}
					_stack[sp] = new DefBoolean(b);
					continue;
				}
				case DB_CLOSESTATEMENT: ((XDResultSet)_stack[sp--]).closeStatement(); continue;
				case DB_COMMIT: {
					XDValue dv = _stack[sp--];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Service"); //Null value of &{0}
						continue; //never execute
					}
					((XDService) dv).commit();
					continue;
				}
				case DB_ROLLBACK: {
					XDValue dv = _stack[sp--];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Service"); //Null value of &{0}
						continue; //never execute
					}
					((XDService) dv).rollback();
					continue;
				}
				case DB_SETPROPERTY: {
					String value = _stack[sp--].stringValue();
					String name = _stack[sp--].stringValue();
					XDValue dv = _stack[sp];
					if (dv == null || dv.isNull()) {
						throwInfo(chkEl, XDEF.XDEF573, "Service"); //Null value of &{0}
						continue; //never execute
					}
					try {
						((XDService) dv).setProperty(name, value);
						_stack[sp] = new DefBoolean(true);
					} catch (RuntimeException ex) {
						_stack[sp] = new DefBoolean(false);
					}
					continue;
				}
				case COMPOSE_OP: {
					Element oldContext = chkEl._sourceElem;
					if (item.getParam() == 2) {
						Element context = _stack[sp--].getElement();
						if (context != null) {
							chkEl._sourceElem = context;
						}
					}
					String rootName = _stack[sp].toString();
					XMDefinition xdef;
					int i;
					if ((i = rootName.indexOf('#')) >= 0) {
						if (i > 0) {
							xdef = _xd.getXDPool().getXMDefinition(rootName.substring(0,i));
							if (xdef == null) {
								chkEl.fatal(XDEF.XDEF530, rootName); //Missing X-definition &{0}
								_stack[sp] = new DefElement(chkEl.getElemValue());
								chkEl._sourceElem = oldContext;
								continue;
							}
						} else {
							xdef = _xd;
						}
						rootName = rootName.substring(i + 1);
					} else {
						xdef = _xd;
					}
					Map<Integer, CodeUniqueset> idrefTables =
						new LinkedHashMap<>();
					// save and clear all unique
					for (int j = 3; j < _globalVariables.length; j++) {
						XDValue xv;
						if ((xv = _globalVariables[j]) != null && xv.getItemId() == X_UNIQUESET) {
							CodeUniqueset x = (CodeUniqueset)xv;
							idrefTables.put(j, x);
							_globalVariables[j] =
								new CodeUniqueset(x.getParsedItems(),x.getVarNames(),x.getName());
						}
					}
					Element elem =
						ChkComposer.compose(_reporter, (XDefinition) xdef, rootName, chkEl.getChkElement());
					// restore all unique
					for (Integer j : idrefTables.keySet()) {
						CodeUniqueset x = idrefTables.get(j);
						_globalVariables[j] = x;
					}
					if (elem == null) {
						chkEl.error(XDEF.XDEF529); //Required element is missing in setElement method
						elem = chkEl.getDocument().createElementNS(null, UNDEF_ID); //create dumy element
					} else if (elem.getOwnerDocument() != chkEl.getDocument()) {
						elem = (Element) chkEl.getDocument().importNode(elem, true);
					}
					chkEl._sourceElem = oldContext;
					_stack[sp] = new DefElement(elem);
					continue;
				}
				case FROM_ELEMENT: _stack[sp] = new DefContainer(_stack[sp]); continue;
				case GET_ITEM: {
					String t;
					if (chkEl == null) {
						t = null;
					} else {
						Object obj = chkEl.getCreateContext();
						Element e =
							(obj != null && (obj instanceof Element)) ? (Element) obj : chkEl.getElemValue();
						if (e == null) {
							t = null;
						} else {
							String s = _stack[sp].toString();
							if (s.startsWith("@")) {
								s = s.substring(1);
							}
							t = e.getAttribute(s);
							if (t.length() == 0 && !e.hasAttribute(s)) {
								t = null;
							}
						}
					}
					_stack[sp] = new DefString(t);
					continue;
				}
				case GET_ATTR: {//get attribute value
					String ns = item.getParam() == 2 ? _stack[sp--].toString() : null;
					Element e = chkEl.getElemValue();
					_stack[sp] = new DefString(e == null ? ""
						: ns == null ? e.getAttribute(_stack[sp].toString())
							: e.getAttributeNS(ns, _stack[sp].toString()));
					continue;
				}
				case CREATE_ELEMENT: {
					String ns;
					String name;
					if (item.getParam() == 0) {
						ns = chkEl._xElement.getNSUri();
						name = chkEl._xElement.getName();
					} else {
						ns = item.getParam() == 2 ? _stack[sp--].toString() : null;
						name = _stack[sp--].toString();
					}
					_stack[++sp] = new DefElement(chkEl._rootChkDocument._doc, ns, name);
					continue;
				}
				case CREATE_ELEMENTS: {
					String ns;
					String name;
					if (item.getParam() == 1) {
						ns = chkEl._xElement.getNSUri();
						name = chkEl._xElement.getName();
					} else {
						ns = item.getParam() == 3 ? _stack[sp--].toString() : null;
						name = _stack[sp--].toString();
					}
					int i = _stack[sp].intValue();
					DefElement[] values = new DefElement[i];
					for (int j = 0; j < i; j++) {
						 values[j] = new DefElement(chkEl._rootChkDocument._doc, ns, name);
					}
					_stack[sp] = new DefContainer(values);
					continue;
				}
				case PARSE_XML: {
					String s; //name of xdef
					if (item.getParam() == 1 || (s=_stack[sp--].stringValue()) == null) {//no xdef,just parse
						Document d = KXmlUtils.parseXml(_stack[sp].toString());
						_stack[sp] = new DefElement(d.getDocumentElement());
						continue;
					}
					XMDefinition xdef;
					int ndx;
					if ((ndx = s.indexOf('#')) >= 0) {
						if (ndx > 0) {
							xdef = _xd.getXDPool().getXMDefinition(s.substring(0,ndx));
							if (xdef == null) {
								chkEl.fatal(XDEF.XDEF530, s); //Missing X-definition &{0}
								_stack[sp] = new DefElement(chkEl.getElemValue());
								continue;
							}
						} else {
							xdef = _xd;
						}
						s = s.substring(ndx + 1);
					} else {
						xdef = _xd;
					}
					//parse element with X-definition
					ChkDocument x = (ChkDocument) ("*".equals(s)
						? xdef.getXDPool().createXDDocument() : xdef.getXDPool().createXDDocument(s));
					//set our global variables to parser!!!
					x._rootChkDocument._scp._initialized1 = true;
					x._rootChkDocument._scp._initialized2 = true;
					x._scp._code = _code;
					x._rootChkDocument._scp._globalVariables = _globalVariables;
					x._rootChkDocument._scp._textParser = _textParser;
					_stack[sp] = new DefElement(x.xparse(_stack[sp].toString(), _reporter));
					_globalVariables = x._rootChkDocument._scp._globalVariables;
					continue;
				}
				case GET_COUNTER: {
					int result = -1;
					if (chkEl != null) {
						if (chkEl.getItemId() == XX_ELEMENT) {
							result = chkEl.getOccurrence();
						} else if (chkEl.getItemId() == XX_TEXT) {
							result = chkEl.getRefNum();
						}
					}
					_stack[++sp] = new DefLong(result);
					continue;
				}
				case GET_ELEMENT_NAME:
					_stack[++sp] = new DefString(chkEl._element == null ? "" : chkEl._element.getNodeName());
					continue;
				case GET_ELEMENT_LOCALNAME: {
					String s;
					if (chkEl._element != null) {
						s = chkEl._element.getLocalName();
						if (s == null) {
							s = chkEl._element.getNodeName();
						}
					} else {
						s = "";
					}
					_stack[++sp] = new DefString(s);
					continue;
				}
				case GET_ATTR_NAME:
					_stack[++sp] = new DefString(chkEl.getItemId() == XX_ATTR ? chkEl.getNodeName() : null);
					continue;
				case CURRENCYCODE: //get currency code
					_stack[sp] = new DefString(((XDCurrency) _stack[sp]).getCurrencyCode()); continue;
				case GET_REGEX_RESULT:
					_stack[sp - 1] = ((XDRegex) _stack[sp - 1]).getRegexResult(_stack[sp].stringValue());
					sp -= 1;
					continue;
				case MATCHES_REGEX: {
					XDRegexResult rr;
					if (item.getParam() == 2) {
						rr = ((XDRegex) _stack[sp - 1]).getRegexResult(_stack[sp].stringValue());
						_stack[sp] = null;
						sp--;
					} else {
						rr = (XDRegexResult) _stack[sp];
					}
					_stack[sp] = new DefBoolean(rr.matches());
					continue;
				}
				case GET_REGEX_GROUP:
					_stack[--sp] = new DefString(((XDRegexResult) _stack[sp]).group(_stack[sp+1].intValue()));
					continue;
				case GET_REGEX_GROUP_NUM:
					_stack[sp] = new DefLong(((XDRegexResult) _stack[sp]).groupCount()); continue;
				case GET_REGEX_GROUP_START:
					_stack[--sp] =
						new DefLong(((XDRegexResult) _stack[sp]).groupStart(_stack[sp+1].intValue()));
					continue;
				case GET_REGEX_GROUP_END:
					_stack[--sp] = new DefLong(((XDRegexResult)_stack[sp]).groupEnd(_stack[sp+1].intValue()));
					continue;
				case STREAM_READLN: {
					XDInput in;
					if (item.getParam() == 1) {
						in = (XDInput) _stack[sp];
					} else {
						in = (XDInput) _globalVariables[2];
						sp++;
					}
					_stack[sp] = new DefString(in.readString());
					continue;
				}
				case STREAM_EOF: {
					XDInput in;
					if (item.getParam() == 1) {
						in = (XDInput) _stack[sp];
					} else {
						in = (XDInput) _globalVariables[2];
						sp++;
					}
					_stack[sp] = new DefBoolean(in.isOpened());
					continue;
				}
				case GET_MESSAGE: _stack[sp] = new DefString(_stack[sp].toString()); continue;
				case GET_BNFRULE: {
					String s = _stack[sp--].toString();
					DefBNFRule br =  ((DefBNFGrammar) _stack[sp]).getRule(s);
					if (br == null || br.isNull()) {
						throwInfo(chkEl, XDEF.XDEF572, s); //BNF rule '&{0}' not exists
					}
					_stack[sp] = br;
					continue;
				}
				case BNF_PARSE: {
					String s;
					boolean quoted = false;
					if (item.getParam() == 2) {
						s = chkEl.getTextValue();
						if (chkEl != null && chkEl.getXonMode() > 0 && s!= null && s.length() > 1
							&& s.startsWith("\"") && s.endsWith("\"")) {
							StringParser p = new StringParser(s);
							p.setIndex(1);
							s = XonTools.readJString(p);
							quoted = true;
						}
					} else {
						s = _stack[sp--].toString();
					}
					String ruleName = _stack[sp--].stringValue();
					DefBNFRule r = ((DefBNFGrammar) _stack[sp]).getRule(ruleName);
					DefParseResult result;
					if (r.ruleValue() == null) {
						result = new DefParseResult(s);
						result.error(XDEF.XDEF567, ruleName); //Script error: BNF rule '&{0}' not exists
					} else {
						result =  r.perform(s);
						if (quoted) {
							result.setParsedValue(chkEl.getTextValue());
							result.setEos();
						}
					}
					_stack[sp] = result;
					continue;
				}
				case BNFRULE_PARSE: {
					String s;
					boolean quoted = false;
					if (item.getParam() == 1) {
						s = chkEl.getTextValue();
						if (chkEl != null && chkEl.getXonMode() > 0 && s!= null && s.length() > 1
							&& s.startsWith("\"") && s.endsWith("\"")) {
							StringParser p = new StringParser(s);
							p.setIndex(1);
							s = XonTools.readJString(p);
							quoted = true;
						}
					} else {
						s = _stack[sp--].toString();
					}
					DefParseResult result = ((DefBNFRule) _stack[sp]).perform(s);
					if (result.matches() && quoted) {
						result.setParsedValue(chkEl.getTextValue());
						result.setEos();
					}
					_stack[sp] = result;
					continue;
				}
				case BNFRULE_VALIDATE: {
					String s;
					if (item.getParam() == 1) {
						s = chkEl.getTextValue();
						if (chkEl != null && chkEl.getXonMode() > 0 && s!= null && s.length() > 1
							&& s.startsWith("\"") && s.endsWith("\"")) {
							StringParser p = new StringParser(s);
							p.setIndex(1);
							s = XonTools.readJString(p);
						}
					} else {
						s = _stack[sp--].toString();
					}
					_stack[sp] = new DefBoolean(((DefBNFRule) _stack[sp]).perform(s).matches());
					continue;
				}
				case PARSE_OP: {
					String s = item.getParam() == 1 ? chkEl.getTextValue() : _stack[sp--].toString();
					XDParseResult result;
					if (_stack[sp]==null || !(_stack[sp] instanceof XDParser)) {
						result = new DefParseResult(s);
						result.error(XDEF.XDEF820, //Value of type "Parser" expected&{0}{, found: }
							_stack[sp]==null ? "null" : _stack[sp].getClass());
					} else {
						result = ((XDParser) _stack[sp]).check(chkEl, s);
						if (result.matches()) {
							if (item.getParam() == 1) {
								chkEl.setTextValue(result.getSourceBuffer());
							}
						}
						if (chkEl != null && item.getParam() == 1) {
							chkEl._parseResult = result;
						}
					}
					_stack[sp] = result;
					continue;
				}
				case PARSEANDSTOP: {// parser from next code, parse and return
					XDParseResult result = ((XDParser) _code[pc]).check(chkEl, chkEl.getTextValue());
					if (result.matches()) {
						chkEl.setTextValue(result.getSourceBuffer());
					}
					return chkEl._parseResult = result;
				}
				case PARSEANDCHECK:
					_stack[sp] = new DefBoolean(((XDParser) _stack[sp]).check(chkEl,
						item.getParam()==1 ? chkEl.getTextValue() : _stack[sp--].toString()).matches());
					continue;
				case PARSERESULT_MATCH:
					_stack[sp] = new DefBoolean(((XDParseResult)_stack[sp]).matches()); continue;
				case SET_PARSED_ERROR: {// x.error(...)
					String id, txt, modif;
					switch (item.getParam()) {
						case 4: // x.error(id, txt, modif)
							modif = _stack[sp--].toString();
							txt = _stack[sp--].toString();
							id = _stack[sp--].toString();
							break;
						case 3: // x.error(id, txt)
							txt = _stack[sp--].toString();
							id = _stack[sp--].toString();
							modif = null;
							break;
						case 2: // x.error(txt)
							txt = _stack[sp--].toString();
							id = modif = null;
							break;
						default:
							id = Report.error(XDEF.XDEF809).getMsgID(); //Incorrect value
							txt = modif = null;
					}
					((DefParseResult) _stack[sp]).error(id, txt, modif);
					continue;
				}
				case GET_PARSED_ERROR: {//result.getError()
					XDParseResult x = (XDParseResult) _stack[sp];
					_stack[sp] =
						x.matches() ? new XDReport() : new XDReport(x.getReporter().getLastErrorReport());
					continue;
				}
				case SET_PARSED_STRING: {//result.setSourceString
					String s = _stack[sp--].toString();
					((XDParseResult) _stack[sp]).setSourceBuffer(s);
					continue;
				}
				case SET_PARSED_VALUE: {//result.setParsedValue
					XDValue v = _stack[sp--];
					XDParseResult pr = item.getParam()==2 ? (XDParseResult) _stack[sp--] : chkEl._parseResult;
					pr.setParsedValue(v);
					continue;
				}
				case GET_PARSED_VALUE: {//get result of parsed value
					XDParseResult pr = item.getParam()==1 ? (XDParseResult) _stack[sp--] : chkEl._parseResult;
					_stack[++sp] = null == pr ? new DefNull() : pr.getParsedValue();
					continue;
				}
				case GET_PARSED_RESULT: //get parsed result
					_stack[++sp] = chkEl._parseResult == null ? new DefParseResult() : chkEl._parseResult;
					continue;
				case SET_NAMEDVALUE:
					if (item.getParam() == 2) {
						XDValue v = _stack[sp--];
						XDNamedValue x;
						if (v.getItemId() == XD_CONTAINER) {
							XDContainer c = (XDContainer) v;
							x = c.getXDNamedItemsNumber() == 0 ? new DefNamedValue("", new DefString())
								: c.getXDNamedItem(c.getXDNamedItemName(0));
						} else {
							x = (XDNamedValue) v;
						}
						((XDContainer)_stack[sp--]).setXDNamedItem(x);
					} else {// item.getParam() == 3
						XDValue val = _stack[sp--];
						String key = _stack[sp--].stringValue();
						((XDContainer)_stack[sp--]).setXDNamedItem(key, val);
					}
					continue;
				case GET_NAMEDITEMS: {//get list of named items from Container
					XDContainer c = (XDContainer) _stack[sp];
					XDContainer d = new DefContainer();
					_stack[sp] = d;
					for (XDNamedValue x: c.getXDNamedItems()) {
						d.setXDNamedItem(x);
					}
					continue;
				}
				case GET_NAMEDVALUE: {//get named item from Container
					String name = (item.getParam() == 1) ? item.stringValue() : _stack[sp--].toString();
					XDValue v = ((XDContainer)_stack[sp]).getXDNamedItemValue(name);
					_stack[sp] = v == null ? new DefString() : v;
					continue;
				}
				case HAS_NAMEDVALUE: {//has named item in Container
					String name = (item.getParam() == 1) ? item.stringValue() : _stack[sp--].toString();
					_stack[sp] = new DefBoolean(((XDContainer)_stack[sp]).hasXDNamedItem(name));
					continue;
				}
				case REMOVE_NAMEDVALUE: {//get named item from Container
					String name = (item.getParam() == 1) ? item.stringValue() : _stack[sp--].toString();
					XDValue v = ((XDContainer)_stack[sp]).removeXDNamedItem(name);
					_stack[sp] = v == null ? new DefNull() : v;
					continue;
				}
				case GET_NAMED_AS_STRING: {//named item from Container as string
					String name = (item.getParam() == 1) ? item.stringValue() : _stack[sp--].toString();
					XDValue v = ((XDContainer)_stack[sp]).getXDNamedItemValue(name);
					_stack[sp] = v == null ? new DefString() : new DefString(v.stringValue());
					continue;
				}
				case NAMEDVALUE_GET: _stack[sp] = ((XDNamedValue) _stack[sp]).getValue(); continue;
				case NAMEDVALUE_SET:_stack[--sp]= ((XDNamedValue)_stack[sp]).setValue(_stack[sp+1]);continue;
				case NAMEDVALUE_NAME:_stack[sp]= new DefString(((XDNamedValue)_stack[sp]).getName());continue;
				case EQ_NULL:
					 _stack[--sp] = new DefBoolean((_stack[sp] == null || _stack[sp].isNull())
						 && (_stack[sp+1] == null || _stack[sp+1].isNull()));
					continue;
				case NE_NULL:
					 _stack[--sp] = new DefBoolean(!((_stack[sp] == null ||_stack[sp].isNull())
						 && (_stack[sp+1] == null ||_stack[sp+1].isNull())));
					continue;
				case IS_EMPTY: {
					XDValue v = _stack[sp];
					if (_stack[sp] != null && !_stack[sp].isNull()) {
						switch (v.getItemId()) {
							case XD_STRING: _stack[sp] = new DefBoolean(v.stringValue().isEmpty()); continue;
							case XD_ELEMENT: _stack[sp] = new DefBoolean(((XDElement)v).isEmpty()); continue;
							case XD_CONTAINER:_stack[sp]=new DefBoolean(((DefContainer)v).isEmpty());continue;
						}
					}
					_stack[sp] = new DefBoolean(true);
					continue;
				}
				case CLOSE_XMLWRITER: ((XDXmlOutStream) _stack[sp--]).closeStream(); continue;
				case NEW_PARSERESULT: _stack[sp] = new DefParseResult(_stack[sp].toString()); continue;
				case NEW_CURRENCY: _stack[sp] = new XDCurrency(_stack[sp].toString()); continue;
				case NEW_EMAIL: _stack[sp] = new DefEmailAddr(_stack[sp].toString()); continue;
				case NEW_IPADDR: _stack[sp] = new DefIPAddr(_stack[sp].toString()); continue;
				case NEW_URI: _stack[sp] = new DefURI(_stack[sp].toString()); continue;
				case NEW_PARSER: {
					XDParser p = getParser(item.stringValue());
					int np = item.getParam();
					if (np > 0) { //number of parameters >= 1
						XDContainer d;
						XDValue val = _stack[sp];
						if (np == 1 && val.getItemId() == XD_NAMEDVALUE) {
							XDNamedValue ni = (XDNamedValue) val;
							d = new DefContainer();
							d.setXDNamedItem(ni.getName(), ni.getValue());
						} else {
							if (val.getItemId() == XD_CONTAINER) {
								d = new DefContainer((XDContainer) val);
								np--;
								sp--;
							} else {
								d = new DefContainer();
							}
							String [] sqParamNames = ((CodeParser) item).getSqParamNames();
							for (int i = np-1; i >= 0; i--) {
							   d.setXDNamedItem(sqParamNames[i], _stack[sp--]);
							}
						}
						p.setNamedParams(chkEl, d);
					}
					_stack[++sp] = (XDValue) p;
					continue;
				}
				case GET_BYTES_FROM_STRING: {
					String s = _stack[sp].toString();
					if (item.getParam() == 1) {
						_stack[sp] = new DefBytes(s.getBytes());
					} else {
						_stack[--sp] = new DefBytes(_stack[sp].toString().getBytes(s));
					}
					continue;
				}
			//Codes implemented in XCodeImplMethods
				case GET_TYPEID: //get type of a value (as integer type id)
				case GET_TYPENAME: // get name of type of a value
				case CHECK_TYPE:
			//Bytes
				case BYTES_CLEAR:
				case BYTES_SIZE: //size of byte array
				case BYTES_TO_BASE64:
				case BYTES_TO_HEX:
			//Duration
				case PARSE_DURATION:
				case DURATION_GETYEARS:
				case DURATION_GETMONTHS:
				case DURATION_GETDAYS:
				case DURATION_GETHOURS:
				case DURATION_GETMINUTES:
				case DURATION_GETSECONDS:
				case DURATION_GETRECURRENCE:
				case DURATION_GETFRACTION:
				case DURATION_GETSTART:
				case DURATION_GETEND:
				case DURATION_GETNEXTTIME:
			//Element
				case ELEMENT_CHILDNODES:
				case ELEMENT_ATTRS:
				case ELEMENT_NAME:
				case ELEMENT_NSURI:
			//ParseResult
				case GET_PARSED_STRING:
			//Datetime
				case GET_DAY:
				case GET_WEEKDAY:
				case GET_MONTH:
				case GET_YEAR:
				case GET_HOUR:
				case GET_MINUTE:
				case GET_SECOND:
				case GET_MILLIS:
				case GET_NANOS:
				case GET_FRACTIONSECOND:
				case GET_LASTDAYOFMONTH:
				case GET_DAYTIMEMILLIS:
				case GET_ZONEOFFSET:
				case GET_ZONEID:
			//String
				case LOWERCASE:
				case UPPERCASE:
				case TRIM_S:
				case GET_STRING_LENGTH: //s.length()
				case WHITESPACES_S:
			//Report
				case GET_REPORT: _stack[sp] = XCodeProcessorExt.perform1v(item, _stack[sp]); continue;
			//Element
				case ELEMENT_ADDELEMENT:
				case ELEMENT_ADDTEXT:
			//Bytes
				case BYTES_ADDBYTE: //Add byte
			//Report
				case PUT_REPORT:
			//XmlWriter
				case SET_XMLWRITER_INDENTING: // Set writer indenting.
				case WRITE_TEXTNODE:
					XCodeProcessorExt.perform2(item, _stack[sp-1], _stack[sp]); sp-=2; continue;
			//formating to string
				case INTEGER_FORMAT:
				case FLOAT_FORMAT:
			//Bytes
				case BYTES_GETAT: //Get byte at position
			//Date
				case DATE_FORMAT:
				case ADD_DAY:
				case ADD_MONTH:
				case ADD_YEAR:
				case ADD_HOUR:
				case ADD_MINUTE:
				case ADD_SECOND:
				case ADD_MILLIS:
				case ADD_NANOS:
				case SET_DAY:
				case SET_MONTH:
				case SET_YEAR:
				case SET_HOUR:
				case SET_MINUTE:
				case SET_SECOND:
				case SET_MILLIS:
				case SET_NANOS:
				case SET_FRACTIONSECOND:
				case SET_DAYTIMEMILLIS:
				case SET_ZONEOFFSET:
				case SET_ZONEID:
			//String
				case GET_STRING_TAIL: //tail(s,i);
				case CUT_STRING: //cut(s,i);
			//Report
				case REPORT_TOSTRING:
				case NEW_NAMEDVALUE:
					_stack[--sp] = XCodeProcessorExt.perform2v(item,_stack[sp], _stack[sp+1]); continue;
			//Bytes
				case BYTES_INSERT: //Insert byte before
				case BYTES_REMOVE: //remove byte(s)
				case BYTES_SETAT: //set byte at position
			//Element
				case ELEMENT_TOSTRING:
				case ELEMENT_GETATTR:
				case ELEMENT_HASATTR:
				case ELEMENT_SETATTR:
				case ELEMENT_TOCONTAINER:
			//Datetime
				case PARSE_DATE:
			//String
				case TRANSLATE_S:
				case REPLACEFIRST_S:
				case REPLACE_S:
				case GET_SUBSTRING: //s.substring(i[,j]);
				case GET_INDEXOFSTRING:
				case GET_LASTINDEXOFSTRING:
			//Report
				case REPORT_SETPARAM:
				case REPORT_SETTYPE: //set report type ('E', 'W', 'F', ...)
			//Constructors
				case NEW_CONTAINER:
				case NEW_ELEMENT:
				case NEW_BYTES:
				case NEW_INSTREAM:
				case NEW_OUTSTREAM:
				case NEW_BNFGRAMAR:
				case NEW_SERVICE:
				case NEW_TELEPHONE:
				case NEW_XMLWRITER:
				case NEW_REPORT:
				case NEW_LOCALE: sp = XCodeProcessorExt.perform(this, item, sp, _stack); continue;
				}//switch
				//Other codes (implemented in XCodeProcessorExt)
				sp = XCodeProcessorExt.performX(this, item, chkEl, sp, _stack, pc);
			} catch (SRuntimeException ex) {
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkEl);
					sp = 0;
					continue;
				}
				Report report = ex.getReport();
				throw new XXException(updateReport(report, chkEl), ex);
			} catch (InvocationTargetException ex) {
				Throwable thr;
				thr = ex.getCause();
				if (thr instanceof Error) {
					throw (Error) thr;
				}
				if (_catchItem != null) {
					pc = genDefException(pc, thr, chkEl);
					sp = 0;
					continue;
				}
				throwError(pc, sp, thr, chkEl);
			} catch (Exception ex) {
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkEl);
					sp = 0;
					continue;
				}
				if (ex instanceof XXException) {
					XXException exx = (XXException) ex;
					if ("XDEF900".equals(exx.getMsgID())) {
						throw exx;
					}
				}
				throwError(pc, sp, ex, chkEl);
			} catch (SError ex) {
				if ("XDEF569".equals(ex.getMsgID())) {
					throw ex;
				}
				if (_catchItem != null) {
					pc = genDefException(pc, ex, chkEl);
					sp = 0;
					continue;
				}
				throwError(pc, sp, ex, chkEl);
			}
		//end of for(;;)
	}

	private static Report updateReport(final Report rep, final ChkNode chkNode){
		if (rep != null && chkNode != null) {
			chkNode.ensurePosInfo(rep);
		}
		return rep;
	}

	/** Generate script exception.
	 * @param pc program counter.
	 * @param ex exception object
	 * @param chkNode processed XXNode.
	 * @return new program counter of catch block.
	 */
	private int genDefException(final int pc, final Throwable ex, final XXNode xNode) {
		int result = _catchItem.getCatchAddr();
		_localVariables = _catchItem.getVariables();
		Report report = (ex instanceof SThrowable)
			? ((SThrowable) ex).getReport() : Report.error(null, ex.toString());
		_stack[0] = new DefException(report, xNode != null ? xNode.getXPos() : null, pc);
		_catchItem = _catchItem.getPrevItem();
		return result;
	}

	private CodeUniqueset execUniqueParser(final CodeUniqueset dt, final int sp, final ChkElement chkElem) {
		XDValue[] stack = new XDValue[sp];
		System.arraycopy(_stack, 0, stack, 0, sp);
		XDValue x = exec(dt.getParseMethod(), chkElem);
		XDParseResult y;
		CodeUniqueset result = null;
		switch (x.getItemId()) {
			case XD_BOOLEAN:
				y =	new DefParseResult(chkElem.getTextValue());
				if (!x.booleanValue()) {
					y.putDefaultParseError(); //XDEF515 value error&{0}{ :}
				}
				break;
			case XD_PARSERESULT: y = (XDParseResult) x; break;
			case X_UNIQUESET:
			case X_UNIQUESET_M:
				result = (CodeUniqueset) x;
			default:
				y = chkElem._parseResult;
				if (x instanceof CodeUniqueset) {
					CodeUniqueset z = (CodeUniqueset) x;
					if (z != dt) {
						z.getParsedItems()[z.getKeyItemIndex()].setParsedObject(y.getParsedValue());
					}
				}
		}
		chkElem._parseResult = y;
		dt.getParseKeyItem(dt.getKeyItemIndex()).setParsedObject(y.getParsedValue());
		System.arraycopy(stack, 0, _stack, 0, sp);
		return result;
	}

	private void execUniqueOperation(final CodeUniqueset dt, final ChkNode chkNode, final int code) {
		switch (code) {
			case UNIQUESET_ID:
			case UNIQUESET_SET:
			case UNIQUESET_KEY_ID:
			case UNIQUESET_KEY_SET:
			case UNIQUESET_M_ID:
			case UNIQUESET_M_SET: {
				Report rep = updateReport(dt.setId(), chkNode);
				if (rep!=null && code!=UNIQUESET_SET && code!=UNIQUESET_KEY_SET && code!=UNIQUESET_M_SET) {
					if (chkNode._parseResult == null) {
						putReport(chkNode, rep);
					} else {
						chkNode._parseResult.putReport(rep);
					}
				}
				break;
			}
			case UNIQUESET_CHKID:
			case UNIQUESET_M_CHKID:
			case UNIQUESET_KEY_CHKID: {
				if (!dt.hasId()) {
					String modif = (dt.getName()!=null ? dt.getName()+" " : "") + dt.printActualKey();
					Report rep = Report.error(XDEF.XDEF522, modif); //Unique value "&{0}" was not set
					updateReport(rep, chkNode);
					if (chkNode._parseResult == null) {
						_reporter.putReport(rep);
					} else {
						chkNode._parseResult.putReport(rep);
					}
				}
				break;
			}
			default:
				ArrayReporter list = dt.chkId();
				if (list != null) {
					String modif = (dt.getName()!=null ? dt.getName()+" " : "") + dt.printActualKey();
					Report rep = Report.error(XDEF.XDEF522, modif); //Unique value "&{0}" was not set
					updateReport(rep, chkNode);
					switch (code) {
						case UNIQUESET_KEY_IDREF:
						case UNIQUESET_M_IDREF:
						case UNIQUESET_IDREF:
							if (chkNode._parseResult != null) {
								_reporter.putReport(rep);
							}
							list.putReport(rep);
							break;
						default :
							if (chkNode._parseResult != null) {
								chkNode._parseResult.putReport(rep);
							} else {
								_reporter.putReport(rep);
							}
					}
				}
		}
	}

	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <i>null</i>.
	 */
	final Object setUserObject(final String id, final Object obj) {return _userObjects.put(id, obj);}

	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <i>null</i>.
	 */
	final Object removeUserObject(final String id) {return _userObjects.remove(id);}

	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or null.
	 */
	public final Object getUserObject(final String id) {return _userObjects.get(id);}

	/** This method is revoked if an exception is thrown when Xscript
	 * is processed and process X-definition is to be finished with fatal error.
	 * @param pc program counter.
	 * @param sp stack pointer.
	 * @param ex exception.
	 * @param xNode processed node.
	 * @throws SError this SError is thrown.
	 */
	private void throwError(final int pc, final int sp, final Throwable ex, final XXNode xNode)throws SError {
		String s = ex.getMessage();
		StackTraceElement[] ste = ex.getStackTrace();
		s = (xNode != null ? "&{xpath}" + xNode.getXPos() + "\n": "") +
			ex.getClass().getName() + (s != null ? ": " + s : "") + "\n" +
			(ste != null && ste.length > 0 ? "at " + ste[0] + "\n" : "") +
			"PC = " + (pc - 1) + "; " +
			(xNode != null ? "XPOS: " + xNode.getXPos() : "INIT section") +"\n";
		if (sp >= 0) {
			s += "STACK:\n";
			for (int i = sp; i >= sp; i--) {
				if (i >= _stack.length) {
					s += "STACK OVERFLOW: [" + i + "]" + "\n";
				} else {
					s += "[" + i + "]: " + _stack[i] + "\n";
				}
			}
		} else {
			s += "STACK: empty\n";
		}
		if (_globalVariables != null && _globalVariables.length > 0) {
			s += "GLOBAL VARIABLES:\n";
			for (String name:
				_xd.getXDPool().getVariableTable().getVariableNames()) {
				s += name + ": " + getVariable(name) + "\n";
			}
		}
		if (_localVariables != null && _localVariables.length > 0) {
			s += "LOCAL VARIABLES BLOCK:\n";
			for (int i = 0; i < _localVariables.length; i++) {
				s += "[" + i + "]: " + _localVariables[i] + "\n";
			}
		}
		java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
		try (java.io.PrintStream ps = new java.io.PrintStream(bs)) {
			org.xdef.impl.code.CodeDisplay.displayCode(_code, ps, pc-4, pc+1);
		}
		if (!bs.toString().isEmpty()) {
			s += "\nCODE:\n" + (pc-4 < 0 ? "...\n" : "") + bs.toString();
			s += (pc + 1 < _code.length ? "..." : "") + '\n';
		}
		//put error to reporter.
		if (xNode != null) {
			_reporter.error(XDEF.XDEF569, s); //Fatal error&{0}{: }
			xNode.copyTemporaryReports();
		} else if (getStdErr() != null) {
			getStdErr().putReport(Report.error(XDEF.XDEF569, s));//Fatal error&{0}{: }
		}
		//Fatal error&{0}{: }
		SError err = new SError(Report.fatal(XDEF.XDEF569, s), ex);
		err.setStackTrace(ex.getStackTrace());
		throw err; //throw fatal error.
	}

	public class XDFunctionResolver implements XPathFunctionResolver {
		@Override
		public XPathFunction resolveFunction(final QName functionName, final int arity) { return null;} //TODO
	}

	public class XDVariableResolver implements XPathVariableResolver {
		public final boolean XPATH2 = DefXPathExpr.isXPath2();
		public boolean convertToString;

		@Override
		public Object resolveVariable(final QName qname) {
			String name = qname.toString();
			String uri;
			if ((uri = qname.getNamespaceURI()) == null || uri.length() == 0) {
				XVariable xv = _xd.findVariable('$' + name);
				if (xv == null) {
					xv = _xd.findVariable(name);
				}
				if (xv != null) {
					XDValue value = _globalVariables[xv.getOffset()];
					if (XPATH2 && convertToString) { //Xpath2 ???? bind??? this is a nasted trick!!
						convertToString = false;
						return  value.stringValue();
					}
					switch (value.getItemId()) {
						case XD_DECIMAL: return value.decimalValue();
						case XD_BOOLEAN: return value.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
						case XD_LONG: return value.longValue();
						case XD_DOUBLE: return value.doubleValue();
						case XD_ELEMENT: return value.getElement();
						default: return value.stringValue();
					}
				}
			}
			convertToString = false;
			return null;
		}
	}

	/** try/catch block {throws link}. */
	private static final class CatchItem {
		private XDValue[] _variables;
		private final int _catchAddr;
		private CatchItem _prevItem;

		/** Create the instance of catch item.
		 * @param catchAddr address of catch block.
		 * @param variables local variables.
		 * @param prevCatchItem link to previous catch item.
		 */
		CatchItem(final int catchAddr, final XDValue[] variables, final CatchItem prevCatchItem) {
			_variables = variables;
			_catchAddr = catchAddr;
			_prevItem = prevCatchItem;
		}

		/** Release catch item. */
		private CatchItem release() {
			CatchItem result = _prevItem;
			_prevItem = null;
			_variables = null; //let GC do the job
			return result;
		}

		//getters and setters.
		private int getCatchAddr() {return _catchAddr;}
		private CatchItem getPrevItem() {return _prevItem;}
		private XDValue[] getVariables() {return _variables;}
	}

	/** Call method block. */
	private static final class CallItem extends XDValueAbstract implements XDCallItem {

		/** Stack (local variables) of parent node. */
		private XDValue[] _parentLocalVariables;
		/** Parent call item. */
		private final CallItem _parent;
		/** Return address, */
		private final int _returnAddr;
		/** Debug step mode. */
		private final int _step;
		/** Counter of nesting. */
		private int _nestCount;

		/** Create the instance of call method block.
		 * @param returnAddr return address.
		 * @param parent parent call block.
		 * @param step debug step mode.
		 */
		private CallItem(final int returnAddr, CallItem parent, int step) {
			if (parent != null) {
				if ((_nestCount = parent._nestCount+1) > 999999) {
					throw new XXException(XDEF.XDEF553, _nestCount);// Too many of recursive call: &{0}
				}
			}
			_parent = parent;
			_parentLocalVariables = null;
			_returnAddr = returnAddr;
			_step = step;
		}

		/** Initialize call block.
		 * @param localVariablesSize size of local variables.
		 * @param localVariables local variables o f parent.
		 * @return new local variables.
		 */
		private XDValue[] init(final int localVariablesSize, final XDValue[] localVariables) {
			_parentLocalVariables = localVariables;
			return localVariablesSize > 0 ? new XDValue[localVariablesSize] : null;
		}

		////////////////////////////////////////////////////////////////////////
		// XDValue methods
		////////////////////////////////////////////////////////////////////////

		@Override
		public final short getItemId() {return XD_ANY;}

		@Override
		public final XDValueType getItemType() {return OBJECT;}

		////////////////////////////////////////////////////////////////////////
		// XDCallItem methods
		////////////////////////////////////////////////////////////////////////

		@Override
		public final XDCallItem getParentCallItem() {return _parent;}

		@Override
		public final int getDebugMode() {return _step;}

		@Override
		public final int getReturnAddr() {return _returnAddr;}
	}
}
