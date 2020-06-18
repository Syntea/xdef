package org.xdef.impl.code;

/** Constants of script code ID.
 * @author Vaclav Trojan
 */
public interface CodeTable {

	////////////////////////////////////////////////////////////////////////////
	// Folowing code is desperate attempt to implement enum to Java 1.3/1.4.
	////////////////////////////////////////////////////////////////////////////
	/** Load clone of DefValue on the top of stack (load constant). */
	static final short LD_CONST = 0; // 0
	/** no operation. */
	static final short NO_OP = LD_CONST + 1;
	/** Load XPath to the stack. */
	static final short LD_TRUE_AND_SKIP = NO_OP + 1;
	/** Load false on the top of stack. */
	static final short LD_FALSE_AND_SKIP = LD_TRUE_AND_SKIP + 1;
	/** Load local value. */
	static final short LD_LOCAL = LD_FALSE_AND_SKIP + 1;
	/** Load XModel value. */
	static final short LD_XMODEL = LD_LOCAL + 1;
	/** Load global value. */
	static final short LD_GLOBAL = LD_XMODEL + 1;
	/** Load final constant variable. */
	static final short LD_CODE = LD_GLOBAL + 1;
	/** Store local value. */
	static final short ST_LOCAL = LD_CODE + 1;
	/** Store XModel value. */
	static final short ST_XMODEL = ST_LOCAL + 1;
	/** Store global value. */
	static final short ST_GLOBAL = ST_XMODEL + 1;
	/** Load copy of DefValue on the top of stack (load constant direct). */
	static final short LD_CONST_I = ST_GLOBAL + 1;

	////////////////////////////////////////////////////////////////////////////
	// Unary operators
	////////////////////////////////////////////////////////////////////////////
	/** Unary integer "minus". */
	static final short NEG_I = LD_CONST_I + 1;
	/** Unary real "minus". */
	static final short NEG_R = NEG_I + 1;
	/** Logical "not". */
	static final short NOT_B = NEG_R + 1;
	/** Binary complement. */
	static final short NEG_BINARY = NOT_B + 1;
	/** Increase integer by one. */
	static final short INC_I = NEG_BINARY + 1;
	/** Decrease real number by one. */
	static final short INC_R = INC_I + 1;
	/** Decrease integer by one. */
	static final short DEC_I = INC_R + 1;
	/** Decrease real by one. */
	static final short DEC_R = DEC_I + 1;

	////////////////////////////////////////////////////////////////////////////
	// Conversions
	////////////////////////////////////////////////////////////////////////////
	/** Convert top of stack to Decimal. */
	static final short TO_DECIMAL_X = DEC_R + 1;
	/** Convert top of stack to float. */
	static final short TO_FLOAT = TO_DECIMAL_X + 1;
	/** Float from integer on stack item (top - index). */
	static final short TO_FLOAT_X = TO_FLOAT + 1;
	/** Set null or a string created from any value on the top of stack. */
	static final short NULL_OR_TO_STRING = TO_FLOAT_X + 1;
	/** String from any value  on the top of stack. */
	static final short TO_STRING = NULL_OR_TO_STRING + 1;
	/** String from any value stack item (top - index). */
	static final short TO_STRING_X = TO_STRING + 1;
	/** Date to milliseconds  on the top of stack. */
	static final short TO_MILLIS = TO_STRING_X + 1;
	/** Date to milliseconds on the stack item (top - index). */
	static final short TO_MILLIS_X = TO_MILLIS + 1;
	/** Any value to boolean. */
	static final short TO_BOOLEAN = TO_MILLIS_X + 1;
	/** Stack to context. */
	static final short STACK_TO_CONTEXT = TO_BOOLEAN + 1;
	/** Stack to named value. */
	static final short CREATE_NAMEDVALUE = STACK_TO_CONTEXT + 1;
	/** Set stack item to lower case. */
	static final short LOWERCASE = CREATE_NAMEDVALUE + 1;
	/** Set to upper case. */
	static final short UPPERCASE = LOWERCASE + 1;
	/** Trim string. */
	static final short TRIM_S = UPPERCASE + 1;
	/** Modify string according to patterns s1,s2. */
	static final short MODIFY_S = TRIM_S + 1;
	/** Translate string according to patterns p,q. */
	static final short TRANSLATE_S = MODIFY_S + 1;
	/** Replace first occurrence of s1 by s2 in the string s. */
	static final short REPLACEFIRST_S = TRANSLATE_S + 1;
	/** Replace all occurrences of s1 by s2 in the string s. */
	static final short REPLACE_S = REPLACEFIRST_S + 1;
	/** Remove ignorable white spaces. */
	static final short WHITESPACES_S = REPLACE_S + 1;

	////////////////////////////////////////////////////////////////////////////
	//parsing with parsers
	////////////////////////////////////////////////////////////////////////////
	/** Get value of the parsed boolean. */
	static final short GET_PARSED_BOOLEAN = WHITESPACES_S + 1;
	/** Get value of the parsed bytes. */
	static final short GET_PARSED_BYTES = GET_PARSED_BOOLEAN + 1;
	/** Get value of the parsed decimal. */
	static final short GET_PARSED_DECIMAL = GET_PARSED_BYTES + 1;
	/** Get value of the parsed int. */
	static final short GET_PARSED_LONG = GET_PARSED_DECIMAL + 1;
	/** Get value of the parsed float. */
	static final short GET_PARSED_DOUBLE = GET_PARSED_LONG + 1;
	/** Get value of the parsed date. */
	static final short GET_PARSED_DATETIME = GET_PARSED_DOUBLE + 1;
	/** Get value of the parsed date. */
	static final short GET_PARSED_DURATION = GET_PARSED_DATETIME + 1;

	////////////////////////////////////////////////////////////////////////////
	//stack operations
	////////////////////////////////////////////////////////////////////////////
	/** POP stack. */
	static final short POP_OP = GET_PARSED_DURATION + 1;
	/** Duplicate top of stack. */
	static final short STACK_DUP = POP_OP + 1;
	/** Swap items on top of stack. */
	static final short STACK_SWAP = STACK_DUP + 1;

	////////////////////////////////////////////////////////////////////////////
	//Binary operators
	////////////////////////////////////////////////////////////////////////////
	/** Logical "and". */
	static final short AND_B = STACK_SWAP + 1;
	/** Integer multiplication. */
	static final short MUL_I = AND_B + 1;
	/** Float multiplication. */
	static final short MUL_R = MUL_I + 1;
	/** Integer division. */
	static final short DIV_I = MUL_R + 1;
	/** Real division. */
	static final short DIV_R = DIV_I + 1;
	/** Integer addition. */
	static final short ADD_I = DIV_R + 1;
	/** Real addition. */
	static final short ADD_R = ADD_I + 1;
	/** String addition (concatenation). */
	static final short ADD_S = ADD_R + 1;
	/** Integer subtraction. */
	static final short SUB_I = ADD_S + 1;
	/** Real subtraction. */
	static final short SUB_R = SUB_I + 1;
	/** Logical "or". */
	static final short OR_B = SUB_R + 1;
	/** Logical "xor". */
	static final short XOR_B = OR_B + 1;
	/** Integer modulo. */
	static final short MOD_I = XOR_B + 1;
	/** Integer modulo. */
	static final short MOD_R = MOD_I + 1;
	/** Left bit shift. */
	static final short LSHIFT_I = MOD_R + 1;
	/** Right bit shift. */
	static final short RSHIFT_I = LSHIFT_I + 1;
	/** Right bit shift unsigned. */
	static final short RRSHIFT_I = RSHIFT_I + 1;

	////////////////////////////////////////////////////////////////////////////
	// Compare two items on the top of stack.
	// Note we MUST respect following sequence of codes!!!
	////////////////////////////////////////////////////////////////////////////
	/** Check if value is null. */
	static final short IS_EMPTY = RRSHIFT_I + 1;
	/** Compare equal to null. */
	static final short EQ_NULL = IS_EMPTY + 1;
	/** Compare not equal to null. */
	static final short NE_NULL = EQ_NULL + 1;
	/** Compare equal. */
	static final short CMPEQ = NE_NULL + 1;
	/** Compare not equal. */
	static final short CMPNE = CMPEQ + 1;
	/** Compare less then. */
	static final short CMPLE = CMPNE + 1;
	/** Compare less then or equal to. */
	static final short CMPGE = CMPLE + 1;
	/** Compare greater then. */
	static final short CMPLT = CMPGE + 1;
	/** Compare greater then or equal to. */
	static final short CMPGT = CMPLT + 1;

	////////////////////////////////////////////////////////////////////////////
	// Conditional jumps.
	// Note we MUST respect following sequence of codes!!!
	////////////////////////////////////////////////////////////////////////////
	/** Compare equal and jump if true. */
	static final short JMPEQ = CMPGT + 1;
	/** Compare not equal and jump if true. */
	static final short JMPNE = JMPEQ + 1;
	/** Compare less then and jump if true. */
	static final short JMPLE = JMPNE + 1;
	/** Compare less then or equal to and jump if true. */
	static final short JMPGE = JMPLE + 1;
	/** Compare greater then and jump if true. */
	static final short JMPLT = JMPGE + 1;
	/** Compare greater then or equal to and jump if true. */
	static final short JMPGT = JMPLT + 1;

	////////////////////////////////////////////////////////////////////////////
	// Program control (stop, throw, jumps, call, return, switch).
	// Note we MUST respect following sequence of codes!!!
	////////////////////////////////////////////////////////////////////////////
	/** Stop code */
	static final short STOP_OP = JMPGT + 1;
	/** Jump operator. */
	static final short JMP_OP = STOP_OP + 1;
	/** Jump on false. */
	static final short JMPF_OP = JMP_OP + 1;
	/** Jump on true. */
	static final short JMPT_OP = JMPF_OP + 1;
	/** Call code */
	static final short CALL_OP = JMPT_OP + 1;
	/** Return without value. */
	static final short RET_OP = CALL_OP + 1;
	/** Return with value. */
	static final short RETV_OP = RET_OP + 1;
	/** Switch integer. */
	static final short SWITCH_I = RETV_OP + 1;
	/** Switch string. */
	static final short SWITCH_S = SWITCH_I + 1;
	/** Throw exception. */
	static final short THROW_EXCEPTION = SWITCH_S + 1;

	////////////////////////////////////////////////////////////////////////////
	// auxiliary codes
	////////////////////////////////////////////////////////////////////////////
	/** Initialize method code without parameters */
	static final short INIT_NOPARAMS_OP = THROW_EXCEPTION + 1;
	/** Initialize method code with parameters */
	static final short INIT_PARAMS_OP = INIT_NOPARAMS_OP + 1;
	/** Set catch exception. */
	static final short SET_CATCH_EXCEPTION = INIT_PARAMS_OP + 1;
	/** Reset catch exception. */
	static final short RELEASE_CATCH_EXCEPTION = SET_CATCH_EXCEPTION + 1;

	////////////////////////////////////////////////////////////////////////////
	// methods
	////////////////////////////////////////////////////////////////////////////
//	static final short CHK_LIST_START = RELEASE_CATCH_EXCEPTION + 1;
//	/** End of list method  sequence. */
//	static final short CHK_LIST_END = CHK_LIST_START + 1;
	/** Check string greater then */
	static final short CHK_GT = RELEASE_CATCH_EXCEPTION + 1;
	/** Check string less then */
	static final short CHK_LT = CHK_GT + 1;
	/** Check string greater then or equal */
	static final short CHK_GE = CHK_LT + 1;
	/** Check string less then or equal */
	static final short CHK_LE = CHK_GE + 1;
	/** Check string not equal */
	static final short CHK_NE = CHK_LE + 1;
	/** Check string not equal case insensitive */
	static final short CHK_NEI = CHK_NE + 1;

	////////////////////////////////////////////////////////////////////////////

	/** Check parser result. */
	static final short PARSERESULT_MATCH = CHK_NEI + 1;
	/** Compile BNF grammar. */
	static final short COMPILE_BNF = PARSERESULT_MATCH + 1;
	/** Load clone of DefValue on the top of stack (load constant). */
	static final short COMPILE_XPATH = COMPILE_BNF + 1;
	/** Create new regular expression to check text value. */
	static final short COMPILE_REGEX = COMPILE_XPATH + 1;
	/** Get value of text node or attribute. */
	static final short GET_TEXTVALUE = COMPILE_REGEX + 1;
	/** Get value of the actual element. */
	static final short GET_ELEMENT = GET_TEXTVALUE + 1;
	/** Get value of root the actual element. */
	static final short GET_ROOTELEMENT = GET_ELEMENT + 1;
	/** Get bytes from string */
	static final short GET_BYTES_FROM_STRING = GET_ROOTELEMENT + 1;
	/** Create default error message. */
	static final short DEFAULT_ERROR = GET_BYTES_FROM_STRING + 1;
	/** Reference of attribute  */
	static final short ATTR_EXIST = DEFAULT_ERROR + 1;
	/** Reference of attribute - direct */
	static final short ATTR_REF = ATTR_EXIST + 1;

	/** Check if string is numeric */
	static final short IS_NUM = ATTR_REF + 1;
	/** Check if string is integer */
	static final short IS_INT = IS_NUM + 1;
	/** Check if string is float */
	static final short IS_FLOAT = IS_INT + 1;
	/** Check if string is datetime */
	static final short IS_DATETIME = IS_FLOAT + 1;

	////////////////////////////////////////////////////////////////////////////
	/** Put value to standard output. */
	static final short OUT_STREAM = IS_DATETIME + 1;
	/** Put value and new line to standard output. */
	static final short OUTLN_STREAM = OUT_STREAM + 1;
	/** Put value to stream. */
	static final short OUT1_STREAM = OUTLN_STREAM + 1;
	/** Put value and new line to stream. */
	static final short OUTLN1_STREAM = OUT1_STREAM + 1;
	/** Put value and new line to stream. */
	static final short PRINTF_STREAM = OUTLN1_STREAM + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Set value of attribute. */
	static final short SET_ATTR = PRINTF_STREAM + 1;
	/** Get value of attribute. */
	static final short GET_ATTR = SET_ATTR + 1;
	/** Check if attribute exists. */
	static final short HAS_ATTR = GET_ATTR + 1;
	/** Delete attribute. */
	static final short DEL_ATTR = HAS_ATTR + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Get occurrence number of the actual element. */
	static final short GET_OCCURRENCE = DEL_ATTR + 1;
	/** Get value of X-definition implementation property. */
	static final short GET_IMPLROPERTY = GET_OCCURRENCE + 1;
	/** Trace script of X-definition. */
	static final short DEBUG_TRACE = GET_IMPLROPERTY + 1;
	/** Break script of X-definition. */
	static final short DEBUG_PAUSE = DEBUG_TRACE + 1;
	/** Format integer to string. */
	static final short INTEGER_FORMAT = DEBUG_PAUSE + 1;
	/** Format float to string. */
	static final short FLOAT_FORMAT = INTEGER_FORMAT + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Format date to string. */
	static final short DATE_FORMAT = FLOAT_FORMAT + 1;
	/** Get day from date. */
	static final short GET_DAY = DATE_FORMAT + 1;
	/** Get week day from date. */
	static final short GET_WEEKDAY = GET_DAY + 1;
	/** Get month from date. */
	static final short GET_MONTH = GET_WEEKDAY + 1;
	/** Get year from date. */
	static final short GET_YEAR = GET_MONTH + 1;
	/** Get hour from date. */
	static final short GET_HOUR = GET_YEAR + 1;
	/** Get minute from date. */
	static final short GET_MINUTE = GET_HOUR + 1;
	/** Get second from date. */
	static final short GET_SECOND = GET_MINUTE + 1;
	/** Get millisecond from date. */
	static final short GET_MILLIS = GET_SECOND + 1;
	/** Get nanosecond from date. */
	static final short GET_NANOS = GET_MILLIS + 1;
	/** Get nanosecond from date. */
	static final short GET_FRACTIONSECOND = GET_NANOS + 1;
	/** Get easter Monday. */
	static final short GET_EASTERMONDAY = GET_FRACTIONSECOND + 1;
	/** Get easter Monday. */
	static final short GET_LASTDAYOFMONTH = GET_EASTERMONDAY + 1;
	/** Get day time in milliseconds from date. */
	static final short GET_DAYTIMEMILLIS = GET_LASTDAYOFMONTH + 1;
	/** Get zone shift to GMT date in milliseconds. */
	static final short GET_ZONEOFFSET = GET_DAYTIMEMILLIS + 1;
	/** Get name of time zone. */
	static final short GET_ZONEID = GET_ZONEOFFSET + 1;
	/** Return true if date is leap year. */
	static final short IS_LEAPYEAR = GET_ZONEID + 1;
	/** Add day to date. */
	static final short ADD_DAY = IS_LEAPYEAR + 1;
	/** Add month to date. */
	static final short ADD_MONTH = ADD_DAY + 1;
	/** Add year to date. */
	static final short ADD_YEAR = ADD_MONTH + 1;
	/** Add hour to date. */
	static final short ADD_HOUR = ADD_YEAR + 1;
	/** Add minute to date. */
	static final short ADD_MINUTE = ADD_HOUR + 1;
	/** Add second to date. */
	static final short ADD_SECOND = ADD_MINUTE + 1;
	/** Add millisecond to date. */
	static final short ADD_MILLIS = ADD_SECOND + 1;
	/** Add nanosecond to date. */
	static final short ADD_NANOS = ADD_MILLIS + 1;
	/** Check if value contains given argument */
	static final short CONTAINS = ADD_NANOS + 1;
	/** Check case insensitive if value contains given argument */
	static final short CONTAINSI = CONTAINS + 1;
	/** Set day in date. */
	static final short SET_DAY = CONTAINSI + 1;
	/** Set month in date. */
	static final short SET_MONTH = SET_DAY + 1;
	/** Set year in date. */
	static final short SET_YEAR = SET_MONTH + 1;
	/** Set hour in date. */
	static final short SET_HOUR = SET_YEAR + 1;
	/** Set minute in date. */
	static final short SET_MINUTE = SET_HOUR + 1;
	/** Set second in date. */
	static final short SET_SECOND = SET_MINUTE + 1;
	/** Set millisecond in date. */
	static final short SET_MILLIS = SET_SECOND + 1;
	/** Set nanosecond in date. */
	static final short SET_NANOS = SET_MILLIS + 1;
	/** Set nanosecond in date. */
	static final short SET_FRACTIONSECOND = SET_NANOS + 1;
	/** Set day time in milliseconds in date. */
	static final short SET_DAYTIMEMILLIS = SET_FRACTIONSECOND + 1;
	/** Set zone shift to GMT date in milliseconds. */
	static final short SET_ZONEOFFSET = SET_DAYTIMEMILLIS + 1;
	/** Set name of time zone. */
	static final short SET_ZONEID = SET_ZONEOFFSET + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Put error message to stdErr. */
	static final short PUT_ERROR = SET_ZONEID + 1;
	/** Put error message to error file. */
	static final short PUT_ERROR1 = PUT_ERROR + 1;
	/** Put report to reporter. */
	static final short PUT_REPORT = PUT_ERROR1 + 1;
	/** Get number of errors. */
	static final short GET_NUMOFERRORS = PUT_REPORT + 1;
	/** Get number of errors and warnings. */
	static final short GET_NUMOFERRORWARNINGS = GET_NUMOFERRORS + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Substring(s,i). */
	static final short GET_STRING_TAIL = GET_NUMOFERRORWARNINGS + 1;
	/** Substring(s,i[, j]). */
	static final short GET_SUBSTRING = GET_STRING_TAIL + 1;
	/** Position of substring in the string (indexOf(s)). */
	static final short GET_INDEXOFSTRING = GET_SUBSTRING + 1;
	/** Last position of substring in the string (lastIndexOf(s)). */
	static final short GET_LASTINDEXOFSTRING = GET_INDEXOFSTRING + 1;
	/** Get length of string. */
	static final short GET_STRING_LENGTH = GET_LASTINDEXOFSTRING + 1;
	/** Get length of string. */
	static final short EQUALSI = GET_STRING_LENGTH + 1;
	/** Check prefix of value. */
	static final short STARTSWITH = EQUALSI + 1;
	/** Check case insensitive prefix of value. */
	static final short STARTSWITHI = STARTSWITH + 1;
	/** Check postfix of value */
	static final short ENDSWITH = STARTSWITHI + 1;
	/** Check case insensitive postfix of value */
	static final short ENDSWITHI = ENDSWITH + 1;
	/** Check case insensitive postfix of value */
	static final short FORMAT_STRING = ENDSWITHI + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Add byte at the and of byte array. */
	static final short BYTES_ADDBYTE = FORMAT_STRING + 1;
	/** Clear byte array. */
	static final short BYTES_CLEAR = BYTES_ADDBYTE + 1;
	/** Get byte at given position. */
	static final short BYTES_GETAT = BYTES_CLEAR + 1;
	/** Insert byte before given position. */
	static final short BYTES_INSERT = BYTES_GETAT + 1;
	/** Remove byte(s) from given position. */
	static final short BYTES_REMOVE = BYTES_INSERT + 1;
	/** Get size of byte array. */
	static final short BYTES_SIZE = BYTES_REMOVE + 1;
	/** Set byte value on given position. */
	static final short BYTES_SETAT = BYTES_SIZE + 1;
	/** Create Base64 string from Bytes array. */
	static final short BYTES_TO_BASE64 = BYTES_SETAT + 1;
	/** Create hexadecimal string from Bytes array. */
	static final short BYTES_TO_HEX = BYTES_TO_BASE64 + 1;
	////////////////////////////////////////////////////////////////////////////
	/** Clear associated reports. */
	static final short CLEAR_REPORTS = BYTES_TO_HEX + 1;
	/** Set given string as value of text node. */
	static final short SET_TEXT = CLEAR_REPORTS + 1;
	/** Remove actual text node. */
	static final short REMOVE_TEXT = SET_TEXT + 1;
	/** Set argument as value of element. */
	static final short SET_ELEMENT = REMOVE_TEXT + 1;
	/** Set value of "now" date. */
	static final short GET_NOW = SET_ELEMENT + 1;
	/** Get prefix from QName. */
	static final short GET_QNPREFIX = GET_NOW + 1;
	/** Get local part from QName. */
	static final short GET_QNLOCALPART = GET_QNPREFIX + 1;
	/** Cut string to max size. */
	static final short CUT_STRING = GET_QNLOCALPART + 1;
	////////////////////////////////////////////////////////////////////////////
	/** getNamespaceURI(String|node...)Get namespace associated with an prefix*/
	static final short GET_NS = CUT_STRING + 1;
	/** getNamespaceURI(QName)Get namespace associated with an QName*/
	static final short GET_QNAMEURI = GET_NS + 1;
	/** Get XQuery result from element. */
	static final short GET_XQUERY = GET_QNAMEURI + 1;
	/** Get XQuery result from element. */
	static final short DB_PREPARESTATEMENT = GET_XQUERY + 1;
	/** Get DBquery iterator result from query. */
	static final short GET_DBQUERY = DB_PREPARESTATEMENT + 1;
	/** Get DBquery iterator result from query. */
	static final short HAS_DBITEM =  GET_DBQUERY + 1;
	/** Get DBquery iterator string result from specified item. */
	static final short GET_DBQUERY_ITEM = HAS_DBITEM + 1;
	/** Execute DB statement. */
	static final short DB_EXEC = GET_DBQUERY_ITEM + 1;
	/** Close the instance of DB object. */
	static final short DB_CLOSE = DB_EXEC + 1;
	/** Close both the ResultSet and the Statement. */
	static final short DB_CLOSESTATEMENT = DB_CLOSE + 1;
	/** Check if the instance of DB object is closed. */
	static final short DB_ISCLOSED = DB_CLOSESTATEMENT + 1;
	/** Commit DB connection. */
	static final short DB_COMMIT = DB_ISCLOSED + 1;
	/** Rollback DB connection. */
	static final short DB_ROLLBACK = DB_COMMIT + 1;
	/** Set auto commit for DB connection. */
	static final short DB_SETPROPERTY = DB_ROLLBACK + 1;
	////////////////////////////////////////////////////////////////////////////
	// XPath
	////////////////////////////////////////////////////////////////////////////
	/** Get XPath result from element. */
	static final short GET_XPATH = DB_SETPROPERTY + 1;
	/** Get XPath from source. */
	static final short GET_XPATH_FROM_SOURCE = GET_XPATH + 1;
	/** Get string from ResultSet item from the actual context. */
	static final short GET_RESULTSET_ITEM = GET_XPATH_FROM_SOURCE + 1;
	/** Check if ResultSet item exists in the actual context. */
	static final short HAS_RESULTSET_ITEM = GET_RESULTSET_ITEM + 1;
	/** Get string value from context. */
	static final short HAS_RESULTSET_NEXT = HAS_RESULTSET_ITEM + 1;
	/** Get string value from context. */
	static final short RESULTSET_NEXT = HAS_RESULTSET_NEXT + 1;
	/** Get number of processed items from ResultSet. */
	static final short GET_RESULTSET_COUNT = RESULTSET_NEXT + 1;
	/** get actual XPath position. */
	static final short GET_XPOS = GET_RESULTSET_COUNT + 1;
	/** Call compose method. */
	static final short COMPOSE_OP = GET_XPOS + 1;
	////////////////////////////////////////////////////////////////////////////
	// XML
	////////////////////////////////////////////////////////////////////////////
	/** Set source element name (deprecated). */
	static final short FROM_ELEMENT = COMPOSE_OP + 1;
	/** Get source item (attribute or named item from context). */
	static final short GET_ITEM = FROM_ELEMENT + 1;
	/** Create new element. */
	static final short CREATE_ELEMENT = GET_ITEM + 1;
	/** Create new element. */
	static final short CREATE_ELEMENTNS = CREATE_ELEMENT + 1;
	/** Create list with new elements. */
	static final short CREATE_ELEMENTS = CREATE_ELEMENTNS + 1;
	/** Parse XML document and return element. */
	static final short PARSE_XML = CREATE_ELEMENTS + 1;
	/** Get actual node counter. */
	static final short GET_COUNTER = PARSE_XML + 1;
	/** Get name of actual element. */
	static final short GET_ELEMENT_NAME = GET_COUNTER + 1;
	/** Get local name of actual element. */
	static final short GET_ELEMENT_LOCALNAME = GET_ELEMENT_NAME + 1;
	/** Get name of actual attribute. */
	static final short GET_ATTR_NAME = GET_ELEMENT_LOCALNAME + 1;
	////////////////////////////////////////////////////////////////////////////
	// Container
	////////////////////////////////////////////////////////////////////////////
	/** getElements(List[, string]). */
	static final short CONTEXT_GETELEMENTS = GET_ATTR_NAME + 1;
	/** getElement(List, index) */
	static final short CONTEXT_GETELEMENT_X = CONTEXT_GETELEMENTS + 1;
	/** getText(List) */
	static final short CONTEXT_GETTEXT = CONTEXT_GETELEMENT_X + 1;
	/** getLength(List) */
	static final short CONTEXT_GETLENGTH = CONTEXT_GETTEXT + 1;
	/** List.sort() */
	static final short CONTEXT_SORT = CONTEXT_GETLENGTH + 1;
	/** List.addItem(object) */
	static final short CONTEXT_ADDITEM = CONTEXT_SORT + 1;
	/** List.addItem(object) */
	static final short CONTEXT_REMOVEITEM = CONTEXT_ADDITEM + 1;
	/** item(List, index) */
	static final short CONTEXT_ITEM = CONTEXT_REMOVEITEM + 1;
	/** item(List, index) */
	static final short CONTEXT_REPLACEITEM = CONTEXT_ITEM + 1;
	/** Create element from source.*/
	static final short CONTEXT_TO_ELEMENT = CONTEXT_REPLACEITEM + 1;
	/** getItemType(List, index) */
	static final short CONTEXT_ITEMTYPE = CONTEXT_TO_ELEMENT + 1;
	////////////////////////////////////////////////////////////////////////////
	// Regex
	////////////////////////////////////////////////////////////////////////////
	/** get result of regular expression. */
	static final short GET_REGEX_RESULT = CONTEXT_ITEMTYPE + 1;
	/** check if result of regular expression matches. */
	static final short MATCHES_REGEX = GET_REGEX_RESULT + 1;
	/** get group of result of regular expression. */
	static final short GET_REGEX_GROUP = MATCHES_REGEX + 1;
	/** get number of items of group of result of regular expression. */
	static final short GET_REGEX_GROUP_NUM = GET_REGEX_GROUP + 1;
	/** get start of group of result of regular expression. */
	static final short GET_REGEX_GROUP_START = GET_REGEX_GROUP_NUM + 1;
	/** get end of group of result of regular expression. */
	static final short GET_REGEX_GROUP_END = GET_REGEX_GROUP_START + 1;
	////////////////////////////////////////////////////////////////////////////
	// Stream
	////////////////////////////////////////////////////////////////////////////
	/** Read line to string from stream. */
	static final short STREAM_READLN = GET_REGEX_GROUP_END + 1;
	/** Check end of stream. */
	static final short STREAM_EOF = STREAM_READLN + 1;
	/** Get report from exception. */
	static final short GET_MESSAGE = STREAM_EOF + 1;
	/** Get report from exception or from error reporter. */
	static final short GET_REPORT = GET_MESSAGE + 1;
	/** Get report from exception or from error reporter. */
	static final short GET_LASTERROR = GET_REPORT + 1;
	/** Get report from exception or from error reporter. */
	static final short IS_CREATEMODE = GET_LASTERROR + 1;
	/** Get parameter from report modification. */
	static final short REPORT_GETPARAM = IS_CREATEMODE + 1;
	/** Set parameter to report modification. */
	static final short REPORT_SETPARAM = REPORT_GETPARAM + 1;
	/** Set type of report. */
	static final short REPORT_SETTYPE = REPORT_SETPARAM + 1;
	/** Set report type. */
	static final short REPORT_TOSTRING = REPORT_SETTYPE + 1;
	/** Get BNF role from grammar. */
	static final short GET_BNFRULE = REPORT_TOSTRING + 1;
	////////////////////////////////////////////////////////////////////////////
	// BNF
	////////////////////////////////////////////////////////////////////////////
	/** Parse string with BNF rule. */
	static final short BNF_PARSE = GET_BNFRULE + 1;
	/** Parse string with BNF rule. */
	static final short BNFRULE_PARSE = BNF_PARSE + 1;
	////////////////////////////////////////////////////////////////////////////
	// Parser
	////////////////////////////////////////////////////////////////////////////
	/** Parse string with parser, result is PARSE_RESULT_VALUE. */
	static final short PARSE_OP = BNFRULE_PARSE + 1;
	/** Parse string with parser, result is BOOLEAN_VALUE. */
	static final short PARSEANDCHECK = PARSE_OP + 1;
	/** Parse string with parser, result is a XDValue (may be DefNull). */
	static final short PARSE_STRING = PARSEANDCHECK + 1;
	/** Set parsed error. */
	static final short SET_PARSED_ERROR = PARSE_STRING + 1;
	/** Get parsed error. */
	static final short GET_PARSED_ERROR = SET_PARSED_ERROR + 1;
	/** Set parsed string. */
	static final short SET_PARSED_STRING = GET_PARSED_ERROR + 1;
	/** Get parsed string. */
	static final short GET_PARSED_STRING = SET_PARSED_STRING + 1;
	/** Set parsed value. */
	static final short SET_PARSED_VALUE = GET_PARSED_STRING + 1;
	/** Get parsed value. */
	static final short GET_PARSED_VALUE = SET_PARSED_VALUE + 1;
	////////////////////////////////////////////////////////////////////////////
	// Named value
	////////////////////////////////////////////////////////////////////////////
	/** Set key value to to container. */
	static final short SET_NAMEDVALUE = GET_PARSED_VALUE + 1;
	/** Get key value from container. */
	static final short GET_NAMEDVALUE = SET_NAMEDVALUE+ 1;
	/** Has key value in container. */
	static final short HAS_NAMEDVALUE = GET_NAMEDVALUE+ 1;
	/** Remove key value from container. */
	static final short REMOVE_NAMEDVALUE = HAS_NAMEDVALUE + 1;
	/** Get named item as string. */
	static final short GET_NAMED_AS_STRING = REMOVE_NAMEDVALUE + 1;
	/** Get value from named value. */
	static final short NAMEDVALUE_GET = GET_NAMED_AS_STRING + 1;
	/** Set value to container. */
	static final short NAMEDVALUE_SET = NAMEDVALUE_GET + 1;
	/** Get key value to container. */
	static final short NAMEDVALUE_NAME = NAMEDVALUE_SET + 1;
	////////////////////////////////////////////////////////////////////////////
	// XML writer
	////////////////////////////////////////////////////////////////////////////
	/** Set XML writer indenting. */
	static final short SET_XMLWRITER_INDENTING = NAMEDVALUE_NAME + 1;
	/** Write element start. */
	static final short WRITE_ELEMENT_START = SET_XMLWRITER_INDENTING + 1;
	/** Write element end tag. */
	static final short WRITE_ELEMENT_END = WRITE_ELEMENT_START + 1;
	/** Write element. */
	static final short WRITE_ELEMENT = WRITE_ELEMENT_END + 1;
	/** Write text node. */
	static final short WRITE_TEXTNODE = WRITE_ELEMENT + 1;
	/** Close XML writer. */
	static final short CLOSE_XMLWRITER = WRITE_TEXTNODE + 1;
	////////////////////////////////////////////////////////////////////////////
	// uniqueset
	////////////////////////////////////////////////////////////////////////////
	/** Create new instance of UNIQUESET. */
	static final short UNIQUESET_NEWINSTANCE = CLOSE_XMLWRITER + 1;
	/** Check value reference (IDREF)  (postdefine allowed). */
	static final short UNIQUESET_IDREF = UNIQUESET_NEWINSTANCE + 1;
	/** Check value references (IDREFS) (postdefine allowed). */
	static final short UNIQUESET_IDREFS = UNIQUESET_IDREF + 1;
	/** Check value reference (CHKID)  (postdefine NOT allowed). */
	static final short UNIQUESET_CHKID = UNIQUESET_IDREFS + 1;
	/** Check value references (CHKIDS)  (postdefine NOT allowed). */
	static final short UNIQUESET_CHKIDS = UNIQUESET_CHKID + 1;
	/** Set Id value (ID) (error if key already exists) */
	static final short UNIQUESET_ID = UNIQUESET_CHKIDS + 1;
	/** Set Id value (ID) (no errors) */
	static final short UNIQUESET_SET = UNIQUESET_ID + 1;
	/** Set key part and check reference of key (postdefine allowed). */
	static final short UNIQUESET_KEY_IDREF = UNIQUESET_SET + 1;
	/** Set key part and check reference of key (postdefine NOT allowed). */
	static final short UNIQUESET_KEY_CHKID = UNIQUESET_KEY_IDREF + 1;
	/** Set key part (setKey) (error if key part was declared). */
	static final short UNIQUESET_KEY_SETKEY = UNIQUESET_KEY_CHKID + 1;
	/** Set key with key part (error if key already exists). */
	static final short UNIQUESET_KEY_ID = UNIQUESET_KEY_SETKEY + 1;
	/** Set Id value (ID) (no errors) */
	static final short UNIQUESET_KEY_SET = UNIQUESET_KEY_ID + 1;
	/** Clear value of key part */
	static final short UNIQUESET_KEY_NEWKEY = UNIQUESET_KEY_SET + 1;
	/** Set key (error if key already exists). */
	static final short UNIQUESET_M_ID = UNIQUESET_KEY_NEWKEY + 1;
	/** Set key (error if key already exists). */
	static final short UNIQUESET_M_SET = UNIQUESET_M_ID + 1;
	/** Check value reference (chkId)  (postdefine allowed). */
	static final short UNIQUESET_M_IDREF = UNIQUESET_M_SET + 1;
	/** Check value reference (CHKID)  (postdefine NOT allowed). */
	static final short UNIQUESET_M_CHKID = UNIQUESET_M_IDREF + 1;
	/** Clear all parts of key value. */
	static final short UNIQUESET_M_NEWKEY = UNIQUESET_M_CHKID + 1;
	/** Get size of uniqueSet. */
	static final short UNIQUESET_M_SIZE = UNIQUESET_M_NEWKEY + 1;
	/** Create Container from uniqueSet. */
	static final short UNIQUESET_M_TOCONTAINER = UNIQUESET_M_SIZE + 1;
	/** Close uniqueSet. */
	static final short UNIQUESET_CLOSE = UNIQUESET_M_TOCONTAINER + 1;
	/** Check unresolved Id references and clear unique set (CLEAR, close). */
	static final short UNIQUESET_CHEKUNREF = UNIQUESET_CLOSE + 1;
	/** Set named value to the key of unique set. */
	static final short UNIQUESET_SETVALUEX = UNIQUESET_CHEKUNREF + 1;
	/** Get named value to the key of unique set. */
	static final short UNIQUESET_GETVALUEX = UNIQUESET_SETVALUEX + 1;
	/** Set index of multiple key  (0 if key is simple). */
	static final short UNIQUESET_KEY_LOAD = UNIQUESET_GETVALUEX + 1;
	/** Get the actual key of uniqueSet. */
	static final short UNIQUESET_GET_ACTUAL_KEY = UNIQUESET_KEY_LOAD + 1;
	/** Reset actual key of the uniqueSet from saved key. */
	static final short UNIQUESET_KEY_RESET = UNIQUESET_GET_ACTUAL_KEY + 1;

	////////////////////////////////////////////////////////////////////////////
	//Object
	////////////////////////////////////////////////////////////////////////////
	/** Get User Object. */
	static final short GET_USEROBJECT = UNIQUESET_KEY_RESET + 1;
	/** Get User Object. */
	static final short SET_USEROBJECT = GET_USEROBJECT + 1;

	////////////////////////////////////////////////////////////////////////////
	//ANY VALUE
	////////////////////////////////////////////////////////////////////////////
	/** Get type ID. */
	static final short GET_TYPEID = SET_USEROBJECT + 1;
	/** Get type name. */
	static final short GET_TYPENAME = GET_TYPEID + 1;
	/** Check type. */
	static final short CHECK_TYPE = GET_TYPENAME + 1;
	////////////////////////////////////////////////////////////////////////////
	//Costructors
	////////////////////////////////////////////////////////////////////////////
	/** create new exception. */
	static final short NEW_EXCEPTION = CHECK_TYPE + 1;
	/** create new exception. */
	static final short NEW_CONTEXT = NEW_EXCEPTION + 1;
	/** create new Element object. */
	static final short NEW_ELEMENT = NEW_CONTEXT + 1;
	/** Create bytes array. */
	static final short NEW_BYTES = NEW_ELEMENT + 1;
	/** Create output stream. */
	static final short NEW_INSTREAM = NEW_BYTES + 1;
	/** Create output stream. */
	static final short NEW_OUTSTREAM = NEW_INSTREAM + 1;
	/** Create BNF grammar. */
	static final short NEW_BNFGRAMAR = NEW_OUTSTREAM + 1;
	/** Create parser. */
	static final short NEW_PARSER = NEW_BNFGRAMAR + 1;
	/** Create parse result. */
	static final short NEW_PARSERESULT = NEW_PARSER + 1;
	/** Create DB connection. */
	static final short NEW_SERVICE = NEW_PARSERESULT + 1;
	/** Create named value. */
	static final short NEW_NAMEDVALUE = NEW_SERVICE + 1;
	/** Create namedValue. */
	static final short NEW_XMLWRITER = NEW_NAMEDVALUE + 1;
	/** Create XML writer. */
	static final short NEW_REPORT = NEW_XMLWRITER + 1;
	/** Create XML writer. */
	static final short NEW_LOCALE = NEW_REPORT + 1;

	////////////////////////////////////////////////////////////////////////////
	//External methods
	////////////////////////////////////////////////////////////////////////////
	/** External method with fixed parameters. */
	static final short EXTMETHOD =  NEW_LOCALE + 1;
	/** External method with array of parameters. */
	static final short EXTMETHOD_ARRAY = EXTMETHOD + 1;
	/** External check method */
	static final short EXTMETHOD_CHECK = EXTMETHOD_ARRAY + 1;
	/** External set text value method. */
	static final short EXTMETHOD_TEXT = EXTMETHOD_CHECK + 1;
	/** External set Element value method. */
	static final short EXTMETHOD_SET_ELEMENT = EXTMETHOD_TEXT + 1;
	/** External set text value method. */
	static final short EXTMETHOD_VOID_TEXT = EXTMETHOD_SET_ELEMENT + 1;
	/** External set Element value method. */
	static final short EXTMETHOD_VOID_ELEMENT = EXTMETHOD_VOID_TEXT + 1;
	/** External method, first parameter is ChkElement, follows
	 * list of parameters. */
	static final short EXTMETHOD_CHKEL = EXTMETHOD_VOID_ELEMENT + 1;
	/** External method, first parameter is XXNode, follows
	 * list of parameters. */
	static final short EXTMETHOD_XXNODE = EXTMETHOD_CHKEL + 1;
	/** External method, first parameter is ChkElement, the second is
	 * array of parameters. */
	static final short EXTMETHOD_CHKEL_ARRAY = EXTMETHOD_XXNODE + 1;
	/** External method, first parameter is ChkElement, the second is
	 * array of XDValues. */
	static final short EXTMETHOD_CHKEL_XDARRAY =
		EXTMETHOD_CHKEL_ARRAY + 1;
	/** External method, first parameter is XXNode, the second is
	 * array of XDValues. */
	static final short EXTMETHOD_XXNODE_XDARRAY =
		EXTMETHOD_CHKEL_XDARRAY + 1;
	/** External method, parameter array of XDValues. */
	static final short EXTMETHOD_XDARRAY = EXTMETHOD_XXNODE_XDARRAY + 1;
	/** External method, XXElement[,...]. */
	static final short EXTMETHOD_XXELEM = EXTMETHOD_XDARRAY + 1;

	//Implemented methods
	static final short PARSE_BOOLEAN = EXTMETHOD_XXELEM + 1;
	static final short PARSE_INT = PARSE_BOOLEAN + 1;
	static final short PARSE_FLOAT = PARSE_INT + 1;
	static final short PARSE_DATE = PARSE_FLOAT + 1;
	static final short PARSE_DURATION = PARSE_DATE + 1;
	static final short DURATION_GETYEARS = PARSE_DURATION + 1;
	static final short DURATION_GETMONTHS = DURATION_GETYEARS + 1;
	static final short DURATION_GETDAYS = DURATION_GETMONTHS  + 1;
	static final short DURATION_GETHOURS = DURATION_GETDAYS + 1;
	static final short DURATION_GETMINUTES = DURATION_GETHOURS + 1;
	static final short DURATION_GETSECONDS = DURATION_GETMINUTES + 1;
	static final short DURATION_GETRECURRENCE = DURATION_GETSECONDS+1;
	static final short DURATION_GETFRACTION = DURATION_GETRECURRENCE+1;
	static final short DURATION_GETSTART = DURATION_GETFRACTION + 1;
	static final short DURATION_GETEND = DURATION_GETSTART + 1;
	static final short DURATION_GETNEXTTIME = DURATION_GETEND + 1;

	static final short ELEMENT_CHILDNODES = DURATION_GETNEXTTIME + 1;
	static final short ELEMENT_NAME = ELEMENT_CHILDNODES + 1;
	static final short ELEMENT_NSURI = ELEMENT_NAME + 1;
	static final short ELEMENT_GETTEXT = ELEMENT_NSURI + 1;
	static final short ELEMENT_ADDELEMENT = ELEMENT_GETTEXT + 1;
	static final short ELEMENT_ADDTEXT = ELEMENT_ADDELEMENT + 1;
	static final short ELEMENT_TOSTRING = ELEMENT_ADDTEXT + 1;
	static final short ELEMENT_TOCONTEXT = ELEMENT_TOSTRING + 1;
	static final short ELEMENT_GETATTR = ELEMENT_TOCONTEXT + 1;
	static final short ELEMENT_HASATTR = ELEMENT_GETATTR + 1;
	static final short ELEMENT_SETATTR = ELEMENT_HASATTR + 1;

	/** XPath optimizing */
	static final short GETATTR_FROM_CONTEXT = ELEMENT_SETATTR + 1;
	static final short GETELEM_FROM_CONTEXT = GETATTR_FROM_CONTEXT + 1;
	static final short GETELEMS_FROM_CONTEXT = GETELEM_FROM_CONTEXT + 1;

	/** This code keeps information about X-definition source. */
	static final short SRCINFO_CODE = GETELEMS_FROM_CONTEXT + 1;

	////////////////////////////////////////////////////////////////////////////
	//	Following values are used only in compile time.
	////////////////////////////////////////////////////////////////////////////
	/** Last code - this is the last code value. */
	static final short LAST_CODE = SRCINFO_CODE + 10;
}