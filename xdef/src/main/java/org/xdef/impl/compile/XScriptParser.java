package org.xdef.impl.compile;

import org.xdef.msg.XDEF;
import org.xdef.XDValue;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.XOccurrence;
import org.xdef.sys.Report;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import java.util.LinkedHashMap;
import java.util.Map;

/** XScriptParser - lexical parser for the symbols of XD script. Before parsing
 * all macros are expanded.
 * @author  Vaclav Trojan
 */
public class XScriptParser extends StringParser
	implements org.xdef.XDValueID {
	/** Maximal number of nested macros. */
	private static final int MAX_NESTED_MACRO = 100;
	/** Decimal digits and "_" (used in readNumber method). */
	private final static String DEC_DIGITS = "0123456789_";
	/** Hexadecimal digits and "_" (used in readNumber method). */
	private final static String HEX_DIGITS = "0123456789abcdefABCDEF_";
	// basic alphabet
	public static final char UNDEF_SYM = 1000;
	public static final char ASSGN_SYM = '=';
	public static final char EQ_SYM = 1001; // "=="
	public static final char NE_SYM = 1002; // "!="
	public static final char LT_SYM = '<';
	public static final char LE_SYM = 1003; // "<="
	public static final char LSH_SYM = 1004; // "<<"
	public static final char LSH_EQ_SYM = 1005; // "<<="
	public static final char GT_SYM = '>';
	public static final char GE_SYM = 1006; // ">="
	public static final char RSH_SYM = 1007; // ">>"
	public static final char RSH_EQ_SYM = 1008; // ">>="
	public static final char RRSH_SYM = 1009; // ">>>"
	public static final char RRSH_EQ_SYM = 1010; // ">>>="
	public static final char DIV_SYM = '/';
	public static final char DIV_EQ_SYM = 1011; // "/="
	public static final char MUL_SYM = '*';
	public static final char MUL_EQ_SYM = 1012; // "*="
	public static final char MOD_SYM = '%';
	public static final char MOD_EQ_SYM = 1013; // "%="
	public static final char AND_SYM = '&';
	public static final char AND_EQ_SYM = 1014; // "&="
	public static final char PLUS_SYM = '+';
	public static final char INC_SYM = 1015; // "++"
	public static final char PLUS_EQ_SYM = 1016; // "+="
	public static final char MINUS_SYM = '-';
	public static final char DEC_SYM = 1017; // "--"
	public static final char MINUS_EQ_SYM = 1018; // "-="
	public static final char OR_SYM = '|';
	public static final char OR_EQ_SYM = 1019; // "|="
	public static final char XOR_SYM = '^';
	public static final char XOR_EQ_SYM = 1020; // "^="
	public static final char DOT_SYM = '.';
	public static final char DDOT_SYM = 1021; // ".."
	public static final char OOR_SYM = 1023; // "||"
	public static final char AAND_SYM = 1024; // "&&"

	public static final char ASK_SYM = '?';
	public static final char NUMSIGN_SYM = '#';
	public static final char ATCHAR_SYM = '@';
	public static final char DOLAR_SYM = '$';
	public static final char NEG_SYM = '~';
	public static final char NOT_SYM = '!';
	public static final char LPAR_SYM = '(';
	public static final char RPAR_SYM = ')';
	public static final char LSQ_SYM = '[';
	public static final char RSQ_SYM = ']';
	public static final char COMMA_SYM = ',';
	public static final char COLON_SYM = ':';
	public static final char SEMICOLON_SYM = ';';
	public static final char BEG_SYM = '{';
	public static final char END_SYM = '}';

	// XScript statement keywords
	public static final char IF_SYM = 1101;
	public static final char ELSE_SYM = 1102;
	public static final char DO_SYM = 1103;
	public static final char WHILE_SYM = 1104;
	public static final char BREAK_SYM = 1105;
	public static final char CONTINUE_SYM = 1106;
	public static final char SWITCH_SYM = 1107;
	public static final char CASE_SYM = 1108;
	public static final char FOR_SYM = 1109;
	public static final char RETURN_SYM = 1110;
	public static final char DEF_SYM = 1111;
	public static final char DEFAULT_SYM = 1112;
	public static final char TRY_SYM = 1113;
	public static final char CATCH_SYM = 1114;
	public static final char THROW_SYM = 1115;
	public static final char FINAL_SYM = 1116;
	public static final char EXTERNAL_SYM = 1117;
	public static final char NEW_SYM = 1118;

	// XSctipt action names
	public static final char FIXED_SYM = 1301;
	public static final char REQUIRED_SYM = 1302;
	public static final char OPTIONAL_SYM = 1303;
	public static final char IGNORE_SYM = 1304;
	public static final char ILLEGAL_SYM = 1305;

	public static final char OCCURS_SYM = 1310;
	public static final char ON_TRUE_SYM = 1311;
	public static final char ON_FALSE_SYM = 1312;
	public static final char ON_XML_ERROR_SYM = 1313;
	public static final char ON_ABSENCE_SYM = 1314;
	public static final char ON_EXCESS_SYM = 1315;
	public static final char ON_START_ELEMENT_SYM = 1316;
	public static final char ON_ILLEGAL_ATTR_SYM = 1317;
	public static final char ON_ILLEGAL_TEXT_SYM = 1318;
	public static final char ON_ILLEGAL_ELEMENT_SYM = 1319;
	public static final char ON_ILLEGAL_ROOT_SYM = 1320;
	public static final char CREATE_SYM = 1321;
	public static final char INIT_SYM = 1322;
	public static final char OPTIONS_SYM = 1323;
	public static final char REF_SYM = 1324;
	public static final char MATCH_SYM = 1325;
	public static final char FINALLY_SYM = 1326;
	public static final char FORGET_SYM = 1327;
	public static final char TEMPLATE_SYM = 1328;
	public static final char TYPE_SYM = 1329;
	public static final char UNIQUE_SET_SYM = 1330;
	public static final char VAR_SYM = 1333;
	public static final char IMPLEMENTS_SYM = 1335;
	public static final char USES_SYM = 1336;
	public static final char COMPONENT_SYM = 1337;
	public static final char CHECK_SYM = 1338; //never generated

	// Constants identifiers
	public static final char TRUE_SYM = 1401;
	public static final char FALSE_SYM = 1402;
	public static final char PI_SYM = 1403;
	public static final char E_SYM = 1404;
	public static final char MAXINT_SYM = 1405;
	public static final char MININT_SYM = 1406;
	public static final char MAXFLOAT_SYM = 1407;
	public static final char MINFLOAT_SYM = 1408;
	public static final char NEGATIVE_INFINITY_SYM = 1409;
	public static final char POSITIVE_INFINITY_SYM = 1410;
	public static final char NOT_A_NUMBER_SYM = 1411;
	public static final char NULL_SYM = 1412;

	public static final char CONSTANT_SYM = 1501;
	public static final char IDENTIFIER_SYM = 1502;
	public static final char REFERENCE_SYM = 1503;

	// Type ids
	private static final char BASE_ID = 1600;
	public static final char LONG_ID_SYM = (char) (BASE_ID + XD_INT);
	public static final char DECIMAL_ID_SYM = (char) (BASE_ID + XD_DECIMAL);
	public static final char BOOLEAN_ID_SYM = (char) (BASE_ID + XD_BOOLEAN);
	public static final char FLOAT_ID_SYM = (char) (BASE_ID + XD_FLOAT);
	public static final char STRING_ID_SYM = (char) (BASE_ID + XD_STRING);
	public static final char DATETIME_ID_SYM = (char) (BASE_ID + XD_DATETIME);
	public static final char DURATION_ID_SYM = (char) (BASE_ID + XD_DURATION);
	public static final char CONTEXT_ID_SYM = (char) (BASE_ID + XD_CONTAINER);
	public static final char REGEX_ID_SYM = (char) (BASE_ID + XD_REGEX);
	public static final char REGEXRESULT_ID_SYM =(char)(BASE_ID+XD_REGEXRESULT);
	public static final char BNFGRAMMAR_ID_SYM = (char) (BASE_ID+XD_BNFGRAMMAR);
	public static final char BNFRULE_ID_SYM = (char) (BASE_ID + XD_BNFRULE);
	public static final char INSTREAM_ID_SYM = (char) (BASE_ID + XD_INPUT);
	public static final char OUTSTREAM_ID_SYM = (char) (BASE_ID + XD_OUTPUT);
	public static final char BYTES_ID_SYM = (char) (BASE_ID + XD_BYTES);
	public static final char ELEMENT_ID_SYM = (char) (BASE_ID + XD_ELEMENT);
	public static final char EXCEPTION_ID_SYM = (char) (BASE_ID + XD_EXCEPTION);
	public static final char REPORT_ID_SYM = (char) (BASE_ID + XD_REPORT);
	public static final char XPATH_EXPR_ID_SYM = (char) (BASE_ID + XD_XPATH);
	public static final char XQUERY_EXPR_ID_SYM = (char) (BASE_ID + XD_XQUERY);
	public static final char PARSER_ID_SYM = (char) (BASE_ID + XD_PARSER);
	public static final char PARSERESULT_ID_SYM =(char)(BASE_ID+XD_PARSERESULT);
	public static final char SERVICE_ID_SYM = (char) (BASE_ID + XD_SERVICE);
	public static final char STATEMENT_ID_SYM = (char) (BASE_ID + XD_STATEMENT);
	public static final char RESULTSET_ID_SYM = (char) (BASE_ID + XD_RESULTSET);
	public static final char XMODEL_ID_SYM = (char) (BASE_ID + XM_MODEL);
	public static final char NAMED_ID_SYM = (char) (BASE_ID + XD_NAMEDVALUE);
	public static final char XMLWRITER_ID_SYM = (char) (BASE_ID + XD_XMLWRITER);
	public static final char OBJECT_ID_SYM = (char) (BASE_ID + XD_OBJECT);

	/** Symbols which separates sections in the X-script. */
	public static final String SCRIPT_SEPARATORS = new String(new char[] {
		OPTIONS_SYM,
		REQUIRED_SYM,
		OPTIONAL_SYM,
		FIXED_SYM,
		IGNORE_SYM,
		ILLEGAL_SYM,
		OCCURS_SYM,
		ON_TRUE_SYM,
		ON_FALSE_SYM,
		ON_ABSENCE_SYM,
		ON_EXCESS_SYM,
		ON_ILLEGAL_ATTR_SYM,
		ON_ILLEGAL_TEXT_SYM,
		ON_ILLEGAL_ELEMENT_SYM,
		MATCH_SYM,
		CREATE_SYM,
		INIT_SYM,
		FINALLY_SYM,
		FORGET_SYM});

	/** Name of actual X-definition. */
	public String _actDefName;
	/** Array of X-definitions names from where to accept local declarations. */
	public String[] _importLocals;
	/** Version of X-definition (see XD2_0, XD3_1, XD3_2, XD4_0). */
	public byte _xdVersion;
	/** Last parsed identifier */
	public String _idName;
	/** parsed value (integer, float, string). */
	public XDValue _parsedValue;
	/** parsed symbol id. */
	public char _sym;
	/** Parsed unary minus. */
	public boolean _unaryMinus;
	/** XML version (10 -&gt; "1.0", 11 -&gt; "1.1" )*/
	public byte _xmlVersion;
	/** Saved position of last symbol (for error reports) */
	private SPosition _lastSPos;
	/** Actual XPath position. */
	private String _xpath;

	private static final String KEYWORDS = ";" +
		// script command names
		IF_SYM + ";if;" +
		ELSE_SYM + ";else;" +
		DO_SYM + ";do;" +
		WHILE_SYM + ";while;" +
		CONTINUE_SYM + ";continue;" +
		BREAK_SYM + ";break;" +
		SWITCH_SYM + ";switch;" +
		CASE_SYM + ";case;" +
		FOR_SYM + ";for;" +
		RETURN_SYM + ";return;" +
		DEF_SYM + ";def;" +
		TRY_SYM + ";try;" +
		CATCH_SYM + ";catch;" +
		THROW_SYM + ";throw;" +
		// script keywords
		FINAL_SYM + ";final;" +
		EXTERNAL_SYM + ";external;" +
		NEW_SYM + ";new;" +
		FIXED_SYM + ";fixed;" +
		REQUIRED_SYM + ";required;" +
		OPTIONAL_SYM + ";optional;" +
		IGNORE_SYM + ";ignore;" +
		ILLEGAL_SYM + ";illegal;" +
		OCCURS_SYM + ";occurs;" +
		ON_TRUE_SYM + ";onTrue;" +
		ON_FALSE_SYM + ";onFalse;" +
		ON_FALSE_SYM + ";onError;" + //alias of onFalse
		ON_ABSENCE_SYM + ";onAbsence;" +
		DEFAULT_SYM + ";default;" +
		ON_EXCESS_SYM + ";onExcess;" +
		ON_START_ELEMENT_SYM + ";onStartElement;" +
		ON_ILLEGAL_ATTR_SYM + ";onIllegalAttr;" +
		ON_ILLEGAL_TEXT_SYM + ";onIllegalText;" +
		ON_ILLEGAL_ELEMENT_SYM + ";onIllegalElement;" +
		ON_ILLEGAL_ROOT_SYM + ";onIllegalRoot;" +
		CREATE_SYM + ";create;" +
		INIT_SYM + ";init;" +
		OPTIONS_SYM + ";options;" +
		OPTIONS_SYM + ";option;" +  //allow also this
		REF_SYM + ";ref;" +
		MATCH_SYM + ";match;" +
		FINALLY_SYM + ";finally;" +
		FORGET_SYM + ";forget;" +
		TEMPLATE_SYM + ";template;" +
		TYPE_SYM + ";type;" +
		UNIQUE_SET_SYM + ";uniqueSet;" +
		VAR_SYM + ";var;" +
		IMPLEMENTS_SYM + ";implements;" +
		USES_SYM + ";uses;" +
		COMPONENT_SYM + ";component;"+

		// Aliases for relational and logical operators
		EQ_SYM + ";EQ;" +
		NE_SYM + ";NE;" +
		LT_SYM + ";LT;" +
		LE_SYM + ";LE;" +
		GT_SYM + ";GT;" +
		GE_SYM + ";GE;" +
		LSH_SYM + ";LSH;" +
		RSH_SYM + ";RSH;" +
		RRSH_SYM + ";RRSH;" +
		AND_SYM + ";AND;" +
		OR_SYM + ";OR;" +
		XOR_SYM + ";XOR;" +
		MOD_SYM + ";MOD;" +
		NOT_SYM + ";NOT;" +
		NEG_SYM + ";NEG;" +
		OOR_SYM + ";OOR;" +
		AAND_SYM + ";AAND;" +
		AND_EQ_SYM + ";ANDEQ;" +
		LSH_EQ_SYM + ";LSHEQ;" +
		RSH_EQ_SYM + ";RSHEQ;" +
		RRSH_EQ_SYM + ";RRSHEQ;" +
		MOD_EQ_SYM + ";MODEQ;" +
		OR_EQ_SYM + ";OREQ;" +
		XOR_EQ_SYM + ";XOREQ;" +
		//Predefined constants
		TRUE_SYM + ";true;" +
		FALSE_SYM + ";false;" +
		PI_SYM + ";$PI;" + //Math.PI
		E_SYM + ";$E;" + //Math.E
		MININT_SYM + ";$MININT;" +
		MAXINT_SYM + ";$MAXINT;" +
		MINFLOAT_SYM + ";$MINFLOAT;" +
		MAXFLOAT_SYM + ";$MAXFLOAT;" +
		NEGATIVE_INFINITY_SYM + ";$NEGATIVEINFINITY;" +
		POSITIVE_INFINITY_SYM + ";$POSITIVEINFINITY;" +
		NOT_A_NUMBER_SYM + ";$NaN;" +
		NULL_SYM + ";null;" +

		// Names of Value types
		BNFGRAMMAR_ID_SYM + ";$BNFGRAMMAR;" +
		BNFRULE_ID_SYM + ";$BNFRULE;" +
		BOOLEAN_ID_SYM + ";$BOOLEAN;" +
		BYTES_ID_SYM + ";$BYTES;" +
		CONTEXT_ID_SYM + ";$CONTTAINER;" +
		DATETIME_ID_SYM + ";$DATETIME;" +
		DURATION_ID_SYM + ";$DURATION;" +
		ELEMENT_ID_SYM + ";$ELEMENT;" +
		EXCEPTION_ID_SYM + ";$EXCEPTION;" +
		LONG_ID_SYM + ";$INT;" +
		DECIMAL_ID_SYM + ";$DECIMAL;" +
		FLOAT_ID_SYM + ";$FLOAT;" +
		INSTREAM_ID_SYM + ";$INPUT;" +
		OBJECT_ID_SYM + ";$OBJECT;" +
		OUTSTREAM_ID_SYM + ";$OUTPUT;" +
		NAMED_ID_SYM + ";$NAMEDVALUE;" +
		PARSER_ID_SYM + ";$PARSER;" +
		PARSERESULT_ID_SYM + ";$PARSERESULT;" +
		REGEX_ID_SYM + ";&REGEX;" +
		REGEXRESULT_ID_SYM + ";&REGEXRESULT;" +
		REPORT_ID_SYM + ";$REPORT;" +
		RESULTSET_ID_SYM + ";$RESULTSET;" +
		STRING_ID_SYM + ";$STRING;" +
		XPATH_EXPR_ID_SYM + ";$XPATH;" +
		XQUERY_EXPR_ID_SYM + ";$XQUERY;" +
		SERVICE_ID_SYM + ";$SERVICE;" +
		STATEMENT_ID_SYM + ";$STATEMENT;" +
		XMLWRITER_ID_SYM + ";$XMLWRITER;";

	/** Table to convert base symbols to the source form. */
	private static final Map<Character, String> BASESYMBOLTABLE =
		new LinkedHashMap<Character, String>();

	static {
		BASESYMBOLTABLE.put(CONSTANT_SYM, "constant");
		BASESYMBOLTABLE.put(IDENTIFIER_SYM, "identifier");
		BASESYMBOLTABLE.put(REFERENCE_SYM, "reference specification");
		BASESYMBOLTABLE.put(AAND_SYM, "&&");
		BASESYMBOLTABLE.put(OOR_SYM, "||");
		BASESYMBOLTABLE.put(EQ_SYM, "==");
		BASESYMBOLTABLE.put(NE_SYM, "!=");
		BASESYMBOLTABLE.put(GE_SYM, ">=");
		BASESYMBOLTABLE.put(LE_SYM, "<=");
		BASESYMBOLTABLE.put(DDOT_SYM, "..");
		BASESYMBOLTABLE.put(LSH_SYM, "<<");
		BASESYMBOLTABLE.put(RSH_SYM, ">>");
		BASESYMBOLTABLE.put(RRSH_SYM, ">>>");
		BASESYMBOLTABLE.put(PLUS_EQ_SYM, "+=");
		BASESYMBOLTABLE.put(MINUS_EQ_SYM, "-=");
		BASESYMBOLTABLE.put(OR_EQ_SYM, "|=");
		BASESYMBOLTABLE.put(MUL_EQ_SYM, "*=");
		BASESYMBOLTABLE.put(DIV_EQ_SYM, "/=");
		BASESYMBOLTABLE.put(MOD_EQ_SYM, "%=");
		BASESYMBOLTABLE.put(AND_EQ_SYM, "&=");
		BASESYMBOLTABLE.put(XOR_EQ_SYM, "^=");
		BASESYMBOLTABLE.put(UNDEF_SYM, "UNDEFINED SYMBOL");
	}

	/** Creates a new instance of ScriptParser.
	 * @param xmlVersion 10 .. "1.0" (see XConstants.XML10),
	 * 11 .. "1.1"(see XConstants.XML11).
	 */
	public XScriptParser(final byte xmlVersion) {
		super();
		super.setLineInfoFlag(true); // generate line information
		_xmlVersion = xmlVersion;
		_actDefName = "";
		_importLocals = new String[0];
//		_lastPos=0;idName=null;_parsedValue=null;_unaryMinus=false;// Java makes
	}

////////////////////////////////////////////////////////////////////////////////
// Script parsing methods.
////////////////////////////////////////////////////////////////////////////////

	/** Set source buffer to parser and expand macros.
	 * @param source buffer with source code.
	 * @param actDefName name of actually processed X-definition.
	 * @param importLocal array of X-definition names to accept locals.
	 * @param xdVersion version ID of XDefinition.
	 */
	public final void setSource(final SBuffer source,
		final String actDefName,
		final String[] importLocal,
		final byte xdVersion,
		final String xpath) {
		_actDefName = actDefName;
		_xpath = xpath;
		_importLocals = importLocal != null ? importLocal
			: actDefName == null ? new String[] {actDefName + '#'}
			: new String[0];
		_xdVersion = xdVersion;
		if(source != null) {
			setSourceBuffer(source);
		} else {
			setSourceBuffer("");
		}
		setLastPosition();
	}

	/** Set last position for error reporting. */
	final void setLastPosition() {_lastSPos = new SPosition(this);}

	/** Get last position for error reporting.
	 * @return source position.
	 */
	public final SPosition getLastPosition() {return _lastSPos;}

	/** Skip all white spaces and comments. */
	public final void skipBlanksAndComments() {
		if (!chkBufferIndex()) {
			return;
		}
		for (;;) {
			char ch;
			if ("\n\t\r ".indexOf(ch = getCurrentChar()) >= 0) {
				if (ch == '\n') {
					setNewLine();
				}
				while (incBufIndex() >= 0 &&
					"\n\t\r ".indexOf(ch = getCurrentChar()) >= 0) {
				}
			}
			if (ch != '/' || getIndex() + 1 >= getEndBufferIndex() ||
				((ch = getCharAtPos(getIndex() + 1)) != '*' && ch != '/')) {
				return;
			}
			if (ch == '/') {
				setLastPosition();
				error(XDEF.XDEF400); //'//' comment is not allowed in the script
				int ndx = getSourceBuffer().indexOf('\n', getIndex() + 1);
				if (ndx > 0) {
					setBufIndex(ndx);
					continue;
				}
			} else { //ch == '*'
				int ndx;
				if ((ndx = getSourceBuffer().indexOf("*/", getIndex()+2)) > 0) {
					int i;
					while ((i = incBufIndex()) < ndx) {}
					if (ndx + 2 < getEndBufferIndex()) {
						setBufIndex(ndx + 2);
						continue;
					}
				} else {
					setLastPosition();
					error(XDEF.XDEF401); //Unclosed comment in the script
				}
			}
			setEos();
			return;
		}
	}

	/** If follows "(" then after last dot should follow a method name. So, set
	 * as the actual character the dot and remove is from the identifier.
	 */
	final void separateMethodNameFromIdentifier() {
		int i = _idName.lastIndexOf('.');
		if (i > 0) {//contains '.'
			int pos = getIndex();
			skipBlanksAndComments();
			if (getCurrentChar() == '(') { //follows "(" => method name
				//we remove last part of the identifier and
				//we set position to '.'
				pos -= _idName.length() - i;
				_idName = _idName.substring(0, i);
				setBufIndex(pos);//set position to dot
			}
		}
	}

	/** Read next lexical symbol.
	 * @return id of parsed symbol and save it to _sym. If the symbol was
	 * an identifier, then the identifier is stored to _idName. If it is
	 * a keyword the corresponding id is returned. If it was a constant
	 * the parsed constant to _parsedValue.
	 */
	public final char nextSymbol() {
		skipBlanksAndComments(); //skip blanks and comments
		if (!chkBufferIndex()) {
			return _sym = NOCHAR;
		}
		setLastPosition();
		if (_sym == REF_SYM) {
			// after "ref" keyword read reference name!
			if (!isXModelPosition()) {
				return _sym = UNDEF_SYM;
			} else {
				_idName = getParsedString();
				return _sym = REFERENCE_SYM;
			}
		}
		char ch;
		switch (ch = peekChar()) {
			case '.':
				if (isChar('.')) return _sym = DDOT_SYM;
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case ',':
			case ':':
			case ';':
			case '?':
			case '~':
			case '#':
				return _sym = ch;
			case '<':
			case '>':
				if (isChar(ch)) {
					if (isChar('=')) {
						return _sym = ch == '<' ? LSH_EQ_SYM : RSH_EQ_SYM;
					}
					if (ch == '<') {
						return _sym = LSH_SYM;
					}
					if (!isChar('>')) {
						return _sym = RSH_SYM;
					}
					return _sym = isChar('=') ? RRSH_EQ_SYM : RRSH_SYM;
				} else if (ch != '=') {
					return _sym = ch;
				}
				return _sym = nextChar() == '<' ? LE_SYM : GE_SYM;
			case '=':
			case '!':
				return _sym = isChar('=') ? ch == '=' ? EQ_SYM : NE_SYM : ch;
			case '+':
			case '-':
				if (isChar(ch)) {
					return _sym = ch == '+' ? INC_SYM :  DEC_SYM;
				} else if (isChar('=')) {
					return _sym = ch == '+' ? PLUS_EQ_SYM : MINUS_EQ_SYM;
				}
				return _sym = ch;
			case '&':
			case '|':
				if (isChar(ch)) { // '&&' or '||'
					return _sym = ch == '&' ? AAND_SYM : OOR_SYM;
				}
			case '/':
			case '*':
			case '^': //XOR
			case '%': //MOD
				if (isChar('=')) {
					switch (ch) { // "&=", "|=", "/=", "*=", "%="
						case '&': return _sym = AND_EQ_SYM;
						case '|': return _sym = OR_EQ_SYM;
						case '/': return _sym = DIV_EQ_SYM;
						case '*': return _sym = MUL_EQ_SYM;
						case '^': return _sym = XOR_EQ_SYM;
					}
					return _sym = MOD_EQ_SYM; // '%'
				}
				return _sym = ch;
			case '@': {
				if (!isXMLName(_xmlVersion)) {
					error(XDEF.XDEF402); //Name of attribute expected
				}
				_idName = getParsedString();
				separateMethodNameFromIdentifier();
				return _sym = ATCHAR_SYM;
			}
			case '\'':
			case '"': // string literal
				readStringLiteral(ch);
				return _sym = CONSTANT_SYM;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				readNumber(ch);
				return _sym = CONSTANT_SYM;
			default: {
				boolean wasDollar;
				if (!(wasDollar = ch == '$')) {
					setBufIndex(getIndex() - 1);
				}
				if (!isXMLName(_xmlVersion)) {
					return _sym = UNDEF_SYM;
				}
				String s =
					wasDollar ? '$' + getParsedString() : getParsedString();
				if (s.endsWith("--")) {
					setBufIndex(getIndex() - 2);//set position before "--"
					s = s.substring(0, s.length() - 2);
				} else if (s.endsWith(":") || s.endsWith(".")) {
					s = s.substring(0, s.length() - 1);
					setBufIndex(getIndex() - 1);//set position before ":"
				}
				// find keyword index
				int keyindex = KEYWORDS.indexOf(';' + s + ';');
				if (keyindex < 0) { //not found
					_idName = s;
					return _sym = IDENTIFIER_SYM;
				}
				switch (keyindex = KEYWORDS.charAt(keyindex - 1)) {
					case TRUE_SYM:
						_parsedValue = new DefBoolean(true);
						return _sym = CONSTANT_SYM;
					case FALSE_SYM:
						_parsedValue = new DefBoolean(false);
						return _sym = CONSTANT_SYM;
					case PI_SYM: //3.141592653589793
						_parsedValue = new DefDouble(Math.PI);
						return _sym = CONSTANT_SYM;
					case E_SYM: //2.718281828459045
						_parsedValue = new DefDouble(Math.E);
						return _sym = CONSTANT_SYM;
					case MAXFLOAT_SYM: //1.7976931348623157E308
						_parsedValue = new DefDouble(Double.MAX_VALUE);
						return _sym = CONSTANT_SYM;
					case MINFLOAT_SYM:
						_parsedValue = new DefDouble(Double.MIN_VALUE);
						return _sym = CONSTANT_SYM;
					case NEGATIVE_INFINITY_SYM:
						_parsedValue = new DefDouble(Double.NEGATIVE_INFINITY);
						return _sym = CONSTANT_SYM;
					case POSITIVE_INFINITY_SYM:
						_parsedValue = new DefDouble(Double.POSITIVE_INFINITY);
						return _sym = CONSTANT_SYM;
					case MAXINT_SYM:
						_parsedValue = new DefLong(Long.MAX_VALUE);
						return _sym = CONSTANT_SYM;
					case MININT_SYM:
						_parsedValue = new DefLong(Long.MIN_VALUE);
						return _sym = CONSTANT_SYM;
					case LSH_SYM:
					case RSH_SYM:
					case RRSH_SYM:
					case AND_SYM:
					case OR_SYM:
					case XOR_SYM:
					case MOD_SYM:
						if (getCurrentChar() == '=') {
							setBufIndex(getIndex() + 1); // ????????????????????
							switch (keyindex) {
								case LSH_SYM:
									return _sym = LSH_EQ_SYM;  //<<=
								case RSH_SYM:
									return _sym = RSH_EQ_SYM; //>>=
								case RRSH_SYM:
									return _sym = RRSH_EQ_SYM; //>>>=
								case AND_SYM:
									return _sym = AND_EQ_SYM; //&=
								case OR_SYM:
									return _sym = OR_EQ_SYM; //!=
								case XOR_SYM:
									return _sym = XOR_EQ_SYM; //^=
								case MOD_SYM:
									return _sym = MOD_EQ_SYM; //%=
							}
						}
						return _sym = (char) keyindex;
					default:
						if (keyindex < BASE_ID
							|| keyindex > BASE_ID+CompileBase.XD_UNDEF) {
							return _sym = (char) keyindex;//parser ID(see above)
						}
						_parsedValue = new DefLong(keyindex - BASE_ID);//type ID
						return _sym = CONSTANT_SYM;
				} // end of switch (sym.charValue())
			} // default
		} // end of switch (ch = peekChar())
	} // end of nextSymbol()

	/** get parsed symbol ID.
	 * @return parsed symbol ID.
	 */
	public final char parsedSymbol() {return _sym;}

	/** get parsed identifier.
	 * @return parsed identifier.
	 */
	public final String parsedIdentifier() {return _idName;}

	/** Get actual parsed value.
	 * @return parsed value.
	 */
	public final XDValue parsedValue() {return _parsedValue;}

	/** Parse XDPosition (may be also position of a text or of an attribute).
	 * XDPosition ::= modelPosition ("/$text" ("["number"]")? | "/@"name)) ?
	 *   ClassName (("extends" ClassName) ?
	 *   ("implements" ClassName ("," ClassName)* )*
	 * @return true if XDPositione was parsed and set parseResult.
	 */
	final boolean isXDPosition() {
		if (!isXModelPosition()) {
			return false;
		}
		String result = getParsedString();
		int pos = getIndex();
		if (isToken("/@")) {
			if (!isXMLName(_xmlVersion)) {
				error(XDEF.XDEF328);//Reference specification expected
				return false;
			}
		} else if (isToken("/$text")) {
			if (isChar('[')) {
				if (!isInteger()&& isChar(']')) {
					error(XDEF.XDEF328);//Reference specification expected
					return false;
				}
			}
		}
		if (getIndex() > pos) {
			setParsedString(result + getParsedBufferPartFrom(pos));
		}
		return true;
	}

	/** Parse XDPosition (of model).
	 * modelPosition ::= (xdefName? "#")?
	 *   (modelName ("/" modelName ("["number"]")?)* )?
	 * @return true if XDPositione was parsed and set parseResult.
	 */
	public final boolean isXModelPosition() {
		String xdName = _actDefName;
		String modelName = isChar('*') && !isChar('#')
			? "*" : isXMLName(_xmlVersion) ? getParsedString() : "";
		if (isChar('#')) {
			xdName = modelName;
			if (isChar('*') && !isChar('/')) {
				modelName = "*";
			} else if (isXMLName(_xmlVersion)) {
				modelName = getParsedString();
				if (isChar('!') && isXMLName(_xmlVersion)) {
					modelName += '!' + getParsedString();
				}
			} else {
				error(XDEF.XDEF104); //Name of model expected
				return false;
			}
		} else if (modelName.length() == 0){
			return false;
		}
		if (isChar('!')) {
			if (isXMLName(_xmlVersion)) {
				modelName += '!' + getParsedString();
			} else {
				error(XDEF.XDEF104); //Name of model expected
				return false;
			}
		}
		int pos = getIndex();
		while (isChar('/')) {
			boolean wasText;
			if (modelName.endsWith("!mixed") || modelName.endsWith("!choice")
				|| modelName.endsWith("!sequence")) {
				if (isOneOfTokens("$mixed", "$choice", "?sequence") < 0) {
					int ndx = modelName.indexOf('!');
					String s = modelName.substring(ndx + 1);
					modelName += "/$" + s + '/';
					pos = getIndex();
				}
			}
			if ((wasText = isToken("$text")) || isXMLName(_xmlVersion)
				|| isOneOfTokens("$mixed", "$choice", "$sequence") >= 0) {
				if (isChar('[')) {
					if (isInteger()&& isChar(']')) {
						continue;
					} else {
						error(XDEF.XDEF328); //Reference specification expected
						return false;
					}
				} else {
					if (wasText) {
						break;
					}
					continue;
				}
			} else if (isChar('@')) {
				if (isXMLName(_xmlVersion)) {
					break;
				}
			}
			return false;
		}
		if (getIndex() > pos) {
			modelName += getParsedBufferPartFrom(pos);
		}
		setParsedString(xdName + '#' + modelName);
		return true;
	}

	/** Read number. Result is saved to _parsedValue.
	 * @param ch first character.
	 */
	private void readNumber(char ch) {
		int startNumber = getIndex();
		char c = ch;
		if (ch == '0' && ((c = getCurrentChar()) == 'x' || c == 'X'
			 || c == 'd' || c == 'D') ) {
			// hexadecimal number
			incBufIndex();
			if (c == 'd' || c == 'D') { // Decimal
				if ((c = getCurrentChar()) >= '0' && c <= '9') {
					while ((DEC_DIGITS.indexOf(c = getCurrentChar())) >= 0){
						incBufIndex();
					}
					if (c == '.') {
						incBufIndex();
						while((DEC_DIGITS.indexOf(c = getCurrentChar())) >= 0) {
							setBufIndex(getIndex() + 1);
						}
					}
					if ((c == 'e' || c == 'E')) {
						if (incBufIndex() > 0 &&
							((c = getCurrentChar()) == '-' || c == '+')) {
							incBufIndex(); //sign
						}
						if ((c = getCurrentChar()) >= '0' && c <= '9') {
							while((DEC_DIGITS.indexOf(getCurrentChar()))>=0){
								incBufIndex();
							}
						}
					}
					String s =  getParsedBufferPartFrom(startNumber + 1);
					s = SUtils.modifyString(s, "_", "");
					if (_unaryMinus) {
						s = '-' + s;
					}
					setParsedString(s);
					try {
						_parsedValue = new DefDecimal(s);
						return;
					} catch (Exception ex) {}
				}
				error(XDEF.XDEF409); //Decimal number error
				_parsedValue = new DefDecimal(0);
				return;
			}
			long j = 0L;
			boolean wasdigit = false;
			boolean errorreported = false;
			int i;
			while ((i = HEX_DIGITS.indexOf(getCurrentChar())) >= 0) {
				incBufIndex();
				if (i > 15) {
					if (i == 22) {
						continue;
					}
					i -= 6;
				}
				wasdigit = true;
				if ((j & 0xf000000000000000L) != 0 && !errorreported) {
					error(XDEF.XDEF406); //Integer is too large
					errorreported = true;
				} else {
					j = (j << 4) + i;
				}
			}
			if (!wasdigit) {
				error(XDEF.XDEF407); //Hexadecimal digit expected
			}
			if (_unaryMinus) {
				j = -j;
				_unaryMinus = false;
			}
			_parsedValue = new DefLong(j);
			return;
		}
		while (DEC_DIGITS.indexOf(c = getCurrentChar()) >= 0) {
			incBufIndex();
		}
		if (((c == '.') || c == 'e' || c == 'E')) {
			int pos = getIndex();
			boolean wasDecPoint;
			if (wasDecPoint = c == '.') {
				if (incBufIndex() > 0 &&
					DEC_DIGITS.indexOf(c = getCurrentChar()) >= 0) {
					while (incBufIndex() > 0 &&
						DEC_DIGITS.indexOf(c = getCurrentChar()) >= 0) {
					}
					if (!chkBufferIndex()) {
						c = NOCHAR;
					}
					pos = getIndex();
				} else{//not digit after dec. point
					setBufIndex(getIndex() - 1);
					wasDecPoint = false;
				}
			}
			if ((c == 'e' || c == 'E')) {
				//exponent
				if (incBufIndex() > 0 &&
					((c = getCurrentChar()) == '-' || c == '+')) {
					incBufIndex(); //sign
				}
				if ((c = getCurrentChar()) < '0' || c > '9') {
					if (wasDecPoint) {
						setBufIndex(pos);
						try {
							String s = getParsedBufferPartFrom(startNumber - 1);
							s = SUtils.modifyString(s, "_", "");
							setParsedString(s);
							_parsedValue = new DefDouble(getParsedDouble());
						} catch (Exception ex) {
							error(XDEF.XDEF408); //Float number error
							_parsedValue = new DefDouble(0.0);
						}
						return;
					}
					//missing number after exponent
					pos--;
					if (wasDecPoint) {
						try {
							String s = getParsedBufferPartFrom(startNumber - 1);
							s = SUtils.modifyString(s, "_", "");
							setParsedString(s);
							_parsedValue = new DefDouble(getParsedDouble());
						} catch (Exception ex) {
							error(XDEF.XDEF408); //Float number error
							_parsedValue = new DefDouble(0.0);
						}
						return;
					}
				}
				while (incBufIndex() > 0 &&
					DEC_DIGITS.indexOf(getCurrentChar()) >= 0){}
				try {
					String s = getParsedBufferPartFrom(startNumber - 1);
					s = SUtils.modifyString(s, "_", "");
					setParsedString(s);
					_parsedValue = new DefDouble(getParsedDouble());
				} catch (Exception ex) {
					error(XDEF.XDEF408); //Float number error
					_parsedValue = new DefDouble(0.0);
				}
				return;
			}
			if (wasDecPoint) {
				try {
					String s = getParsedBufferPartFrom(startNumber - 1);
					s = SUtils.modifyString(s, "_", "");
					setParsedString(s);
					_parsedValue = new DefDouble(getParsedDouble());
				} catch (Exception ex) {
					error(XDEF.XDEF408); //Float number error
					_parsedValue = new DefDouble(0.0);
				}
				return;
			}
		}
		long j = _unaryMinus ? '0' - ch :ch - '0';
		for (int i = startNumber; i < getIndex(); i++) {
			c = getCharAtPos(i);
			if (c == '_') {
				continue;
			}
			if (_unaryMinus) {
				//Long.MIN_VALUE / 10;
				if (j <= -922337203685477580L) {
					if (j < -922337203685477580L || c > '8') {
						error(XDEF.XDEF406); //Integer is too large
						break;
					}
				}
				j = j * 10 - (c - '0');
			} else {
				if (j >= 922337203685477580L) {
					if (j > 922337203685477580L || c >'7') {
						error(XDEF.XDEF406); //Integer is too large
						break;
					}
				}
				j = j * 10 + c - '0';
			}
		}
		_unaryMinus = false;
		_parsedValue = new DefLong(j);
	}

	/** Read literal. Result is saved to _parsedString.
	 * @param delimiter first character.
	 */
	private void readStringLiteral(final char delimiter) {
		int pos = getIndex();
		StringBuilder sb = new StringBuilder();
		while (true) {
			if (!chkBufferIndex()) {
				error(XDEF.XDEF403); //Unclosed string  specification
				_parsedValue = new DefString("");
				break;
			}
			char c = getCharAtPos(getIndex());
			incBufIndex();
			if (c == delimiter) {
				break;
			}
			if (c == '\n') {
				setNewLine();
			}
			if (c == '\\') {
				if (pos < getIndex() - 1) {
					sb.append(getBufferPart(pos, getIndex() - 1));
				}
				if (eos()) {
					error(XDEF.XDEF403); //Unclosed string specification
					_parsedValue = new DefString("");
					break;
				}
				switch (c = getCharAtPos(getIndex())) {
					case 'n':   // linefeed LF (0x000a)
						c = '\n';
						incBufIndex();
						break;
					case 'r':   // carriage return CR (0x000d)
						c = '\r';
						incBufIndex();
						break;
					case 't':   // horizontal tab HT (0x0009)
						c = '\t';
						incBufIndex();
						break;
					case '\'':  // apostroph
					case '"':   // quote
					case '\\':  // backslash
						incBufIndex();
						break;
					case 'u': { // unicode escapes
						incBufIndex();
						int j, k = 0;
						for (int i = 0; i < 4; i++) {
							if (eos() ||
								(j = "0123456789abcdefABCDEF".indexOf(
								getCurrentChar())) < 0) {
								//Illegal unicode escape char
								error(XDEF.XDEF404);
								break;
							}
							incBufIndex();
							if (j > 15) {
								i -= 6; //capital letters
							}
							k = k * 16 + j;
						}
						c = (char) k;
						break;
					}
					default:
						error(XDEF.XDEF405); //Illegal escape character
				}
				sb.append(c);
				pos = getIndex();
			} else if (c == NOCHAR) {
				error(XDEF.XDEF403); //Unclosed string specification
				_parsedValue = new DefString("");
				break;
			}
		}
		if (pos < getIndex() - 1) {
			sb.append(getBufferPart(pos, getIndex() - 1));
		}
		_parsedValue = new DefString(sb.toString());
	}

	/** Parse occurrence.
	 * XOccurrence::= "occurs" ? OccurrenceInterval
	 * @param occ Container of occurrence.
	 * @return true if occurrence was parsed
	 */
	public final boolean isOccurrence(final XOccurrence occ) {
		boolean wasOccurs;
		if ((wasOccurs = _sym == OCCURS_SYM)) {
			nextSymbol();
		}
		boolean specified = isOccurrenceInterval(occ);
		if (wasOccurs && !specified) {
			error(XDEF.XDEF429); //After 'occurs' is expected the interval
		}
		return specified || wasOccurs;
	}

	/** Parse occurrence interval.
	 * OccurrenceInterval ::= "?" | "*" | "+" | "required"
	 *    | "unlimited" | "illegal" | "ignore"
	 *    | IntegerLiteral [ ".." [IntegerLiteral]]
	 * @param occ Container of occurrence.
	 * @return true if occurrence interval was parsed
	 */
	public final boolean isOccurrenceInterval(final XOccurrence occ) {
		boolean duplicated;
		switch (_sym) {
			case MUL_SYM:
				duplicated = occ.isSpecified();
				occ.setMinOccur(0);
				occ.setMaxOccur(Integer.MAX_VALUE);
				break;
			case PLUS_SYM:
				duplicated = occ.isSpecified();
				occ.setMinOccur(1);
				occ.setMaxOccur(Integer.MAX_VALUE);
				break;
			case REQUIRED_SYM:
				duplicated = occ.isSpecified();
				occ.setRequired();
				break;
			case ASK_SYM:
			case OPTIONAL_SYM:
				duplicated = occ.isSpecified();
				occ.setOptional();
				break;
			case IGNORE_SYM:
				duplicated = occ.isSpecified();
				occ.setIgnore();
				break;
			case ILLEGAL_SYM:
				duplicated = occ.isSpecified();
				occ.setIllegal();
				break;
			case CONSTANT_SYM:
				if (_parsedValue.getItemId() != XD_INT) {
					return false;
				}
				if (occ.isSpecified()) {
					error(XDEF.XDEF422); //Duplicated script section
				}
				occ.setMinOccur(_parsedValue.intValue());
				occ.setMaxOccur(occ.minOccurs());
				if (nextSymbol() == DDOT_SYM) {
					if (nextSymbol() == CONSTANT_SYM) {
						if (_parsedValue.getItemId() != XD_INT) {
							//Value of type '&{0}' expected
							error(XDEF.XDEF423, "int");
						} else {
							occ.setMaxOccur(_parsedValue.intValue());
							if (occ.minOccurs() > occ.maxOccurs()) {
								//Maximum must be greater or equal to minimum
								error(XDEF.XDEF427);
							}
						}
						if (occ.minOccurs() == 0 && occ.maxOccurs() == 0) {
							//'occurs 0' is not allowed - use 'illegal'
							lightError(XDEF.XDEF428);
						}
						nextSymbol();
					} else {
						if (_sym == MUL_SYM) {
							nextSymbol();
						}
						occ.setMaxOccur(Integer.MAX_VALUE);
					}
				}
				if (occ.minOccurs() == 0 && occ.maxOccurs() == 0) {
					occ.setIllegal();
				}
				return true;
			default:
				return false;
		}
		if (duplicated) {
			error(XDEF.XDEF422); //Duplicated script section
		}
		nextSymbol();
		return true;
	}

	/** Check the semicolon and if it is not at the actual position then put
	 * the error message and skip to specified symbols. If it is found
	 * then read next symbol and return true.
	 * @param expected The string with symbols to which will be skipped
	 * the input source.
	 * @return true if the symbol was at the actual position.
	 */
	public final boolean checkSemicolon(final String expected) {
		if (_sym == SEMICOLON_SYM) {
			nextSymbol();
			return true;
		} else if (_sym == NOCHAR) {
			return false;
		}
		errorAndSkip(XDEF.XDEF410, expected, ";"); //'&{0}' expected
		if (_sym == SEMICOLON_SYM) {
			nextSymbol();
			return true;
		}
		return false;
	}

	/** Read the symbol at the next position. If it is expected symbol then
	 * return true, otherwise put the error message and return false.
	 * @param sym the symbol to be checked.
	 * @return true if the symbol was the expected one.
	 */
	public final boolean checkNextSymbol(final char sym) {
		if ( sym == nextSymbol()) {
			nextSymbol();
			return true;
		} else {
			error(XDEF.XDEF410, symToName(sym)); //'&{0}' expected
			return false;
		}
	}

	/** Check the symbol and if it is not at the actual position then put
	 * the error message. If it is found then read next symbol and return true.
	 * @param sym the symbol to be checked.
	 * @return true if the symbol was at the actual position.
	 */
	public final boolean checkSymbol(final char sym) {
		if (_sym == sym) {
			nextSymbol();
			return true;
		} else {
			error(XDEF.XDEF410, symToName(sym)); //'&{0}' expected
			return false;
		}
	}

	/** Check the symbol and if it is not at the actual position then put
	 * the error message. If it is found then read next symbol and return true.
	 * @param sym the symbol to be checked.
	 * @param expected message what is expected.
	 * @return true if the symbol was at the actual position.
	 */
	public final boolean checkSymbol(final char sym, final String expected) {
		if (_sym == sym) {
			nextSymbol();
			return true;
		} else if (_sym == NOCHAR) {
			return false;
		}
		//'&{0}' expected
		errorAndSkip(XDEF.XDEF410, expected + sym, symToName(sym));
		if (_sym == sym) {
			nextSymbol();
			return true;
		}
		return false;
	}

	/** Check the symbol and if it is not at the actual position then put
	 * the error message. If it is found then read next symbol and return true.
	 * @param sym the symbol to be checked.
	 * @param registeredID The registered message ID.
	 * @param mod The modification string of error message.
	 * @return true if the symbol was at the actual position.
	 */
	public final boolean checkSymbol(final char sym,
		final long registeredID,
		final String mod) {
		if (_sym == sym) {
			nextSymbol();
			return true;
		} else {
			error(registeredID, mod);
			return false;
		}
	}

	public final boolean isBlanksAndComments() {
		int start = getIndex();
		skipBlanksAndComments();
		return getIndex() > start;
	}

	private boolean resetPosAndReturn(final int pos, final String javaQName) {
		setBufIndex(pos);
		setParsedString(javaQName);
		return true;
	}

	/** Parse Java fully qualified Java identifier with template type and save
	 * result to _parsedString.TemplateType declaration.
	 * @return true if identifier with template type was recognized.
	 */
	public final boolean isJavaTypedQName() {
		if (!isJavaQName()) {
			return false;
		}
		String javaQName = getParsedString();
		int pos = getIndex();
		isSpaces();
		if (!isChar('<')) {
			return resetPosAndReturn(pos, javaQName);
		}
		StringBuilder sb = new StringBuilder(javaQName);
		isSpaces();
		if (isJavaTypedQName()) {
			sb.append('<').append(getParsedString());
		} else {
			return resetPosAndReturn(pos, javaQName);
		}
		for(;;) {
			isSpaces();
			if (isChar('>')) {
				setParsedString(sb.append('>').toString());
				return true;
			}
			if (!isChar(',')) {
				return resetPosAndReturn(pos, javaQName);
			}
			isSpaces();
			if (isJavaTypedQName()) {
				sb.append(',').append(getParsedString());
			} else {
				return resetPosAndReturn(pos, javaQName);
			}
		}
	}

	/** Parse component declaration.
	 * component ::= ("%interface" | "%ref" | "%class"?) JavaTypedQName |
	 *   JavaTypedQName (("extends" JavaTypedQName) ?
	 *   ("implements" JavaTypedQName ("," JavaTypedQName)* )*
	 * @param fromRequired if link reference is required.
	 * @return string with component declaration or null.
	 */
	public final String parseComponent(boolean fromRequired) {
		String result;
		setLastPosition();
		final String[] commands = new String[]{
			"interface",	// 0
			"class",		// 1
			"bind",			// 2
			"enum",			// 3
			"ref",			// 4
		};
		int command = isOneOfTokens(commands);
		boolean isClass = command == 1;
		boolean isRef = command == 4;
		if (command >= 0 && isBlanksAndComments()) {
			setLastPosition();
			String sep = " ";
			if (isRef && isToken("%enum") && isBlanksAndComments()) {
				setLastPosition();
				command = 3; // set enum
				sep = " %";
			}
			if (isJavaTypedQName()) { //command and class name
				result = "%" + commands[command] + sep + getParsedString();
				isBlanksAndComments();
			} else {
				//Specification of class expected
				error(XDEF.XDEF361);
				return null;
			}
		} else {
			error(XDEF.XDEF354); //declaration error
			return null;
		}
		if (command == 3) { // enum
			if (isJavaQName()) {
				return result + " " + getParsedString();
			}
			error(XDEF.XDEF378); //Name of enumeration type is expected
			return null;
		} else if (command == 2) { // bind
			if (isToken("%with")) {
				if (isBlanksAndComments()) {
					setLastPosition();
					if (isJavaTypedQName()) {
						result += " %with " + getParsedString();
						skipBlanksAndComments();
					} else {
						error(XDEF.XDEF361); //Specification of class expected
						return null;
					}
				} else {
					error(XDEF.XDEF356, "%with"); //'&{0}' expected
					return null;
				}
			}
			if (isToken("%link")) {
				result += " %link ";
				skipBlanksAndComments();
				setLastPosition();
				if (isXModelPosition()) {
					for(;;) {
						result += getParsedString();
						skipBlanksAndComments();
						if (!isChar(',')) {
							break;
						}
						skipBlanksAndComments();
						if (!isXModelPosition()) {
							break;
						}
						result += ',';
					}
				} else {
					error(XDEF.XDEF359);//Reference to model expected
					return null;
				}
			} else {
				error(XDEF.XDEF356, "%link"); //'&{0}' expected
				return null;
			}
			skipBlanksAndComments();
			if (isToken("%bind")) {
				setLastPosition();
				if (isBlanksAndComments()) {
					if (isJavaTypedQName()) {
						skipBlanksAndComments();
						result += " %bind " + getParsedString();
						setLastPosition();
						if (isToken("%with")) {
							if (isBlanksAndComments()) {
								setLastPosition();
								if (isJavaTypedQName()) {
									result += " %with " + getParsedString();
									skipBlanksAndComments();
								} else {
									//Specification of class expected
									error(XDEF.XDEF361);
									return null;
								}
							} else {
								//Specification of class expected
								error(XDEF.XDEF361);
								return null;
							}
						}
					} else {
						error(XDEF.XDEF362); //Alias name expected
						return null;
					}
				} else {
					error(XDEF.XDEF356, "%bind"); //'&{0}' expected
					return null;
				}
			}
			return result;
		}
		skipBlanksAndComments();
		setLastPosition();
		if (isToken("extends")) {
			if (!isClass) { // not %class
				//{0} is not allowed here
				error(XDEF.XDEF363, "extends");
			} else {
				if (isBlanksAndComments()) {
					setLastPosition();
					if (isJavaTypedQName()) {
						setLastPosition();
						result += " extends " + getParsedString();
						skipSpaces();
					} else {
						//Specification of class expected
						error(XDEF.XDEF361);
						return null;
					}
				} else {
					//'&{0}' expected
					error(XDEF.XDEF356, "class name");
					return null;
				}
			}
		}
		boolean isImplements = false;
		int pos = getIndex();
		if (isToken("implements")) {
			if (!isClass) { // not %class
				//{0} is not allowed here
				error(XDEF.XDEF363, "implements");
			}
			if (!isBlanksAndComments()) {
				return null;
			}
			setLastPosition();
			isImplements = true;
		} else {
			if (!isToken("%interface")) {
				if (isBlanksAndComments()) {
					//'&{0}' expected
					error(XDEF.XDEF356, "interface name");
					return null;
				}
			} else {
				if (!isClass) { // not %class
					//{0} is not allowed here
					error(XDEF.XDEF363, "%interface");
				}
				setLastPosition();
			}
		}
		if (pos < getIndex()) {
			if (result.startsWith("%interface ")) {
				//'&{0}' not allowed here
				error(XDEF.XDEF363, "%interface");
				return null;
			}
			if (isJavaTypedQName()) {
				result += " " + (isRef
					? "%ref" : isImplements ? "implements" : "%interface")
					+ " " + getParsedString();
				skipBlanksAndComments();
				setLastPosition();
				while (isImplements && isChar(',')) {
					skipBlanksAndComments();
					setLastPosition();
					if (isJavaTypedQName()) {
						result += "," + getParsedString();
						skipBlanksAndComments();
					} else {
						//Specification of class expected
						error(XDEF.XDEF361);
						return null;
					}
				}
				setLastPosition();
				if (isImplements && isToken("%interface")) {
					if (!isBlanksAndComments()) {
						//'&{0}' expected
						error(XDEF.XDEF356, "%interface");
						return null;
					}
					if (isJavaTypedQName()) {
						result += " %interface " + getParsedString();
						skipSpaces();
					} else {
						//Specification of class expected
						error(XDEF.XDEF361);
						return null;
					}
				}
			} else {
				//Specification of class expected
				error(XDEF.XDEF361);
				return null;
			}
		}
		if (fromRequired) {
			skipBlanksAndComments();
			if (isToken("%link")) {
				skipBlanksAndComments();
				setLastPosition();
				if (isXModelPosition()) {
					result += " %link " + getParsedString();
				} else {
					//Reference to model expected
					error(XDEF.XDEF359);
					return null;
				}
			} else {
				error(XDEF.XDEF356, "%link"); //'&{0}' expected
				return null;
			}
		}
		return result;
	}

	/** Put error message and skip to one of specified symbols.
	 * @param registeredID The registered message ID.
	 * @param expected The symbol to which will be skipped the input source.
	 * @return true if the symbol to be skipped was found.
	 */
	public final boolean errorAndSkip(final long registeredID,
		final char expected) {
		error(registeredID);
		while (_sym != NOCHAR && _sym != expected) {
			nextSymbol();
		}
		return _sym != expected;
	}

	/** Put error message and skip to one of specified symbols.
	 * @param registeredID The registered message ID.
	 * @param expected The symbol to which will be skipped the input source.
	 * @param mod The modification string of error message.
	 * @return true if the symbol to be skipped was found.
	 */
	public final boolean errorAndSkip(final long registeredID,
		final char expected,
		final String mod) {
		error(registeredID, mod);
		while (_sym != NOCHAR && _sym != expected) {
			nextSymbol();
		}
		return _sym != expected;
	}

	/** Put error message and skip to one of specified symbols.
	 * @param registeredID The registered message ID.
	 * @param skipTo The string with symbols to which will be skipped
	 * the input source.
	 * @return true if the symbol to be skipped was found.
	 */
	public final boolean errorAndSkip(final long registeredID,
		final String skipTo) {
		return errorAndSkip(registeredID, skipTo, null);
	}

	/** Put error message and skip to one of specified symbols.
	 * @param registeredID The registered message ID.
	 * @param skipTo The string with symbols to which will be skipped
	 * the input source.
	 * @param mod The modification string of error message.
	 * @return true if the symbol to be skipped was found.
	 */
	public final boolean errorAndSkip(final long registeredID,
		final String skipTo,
		final String mod) {
		error(registeredID, mod);
		while (_sym != NOCHAR && skipTo.indexOf(_sym) < 0) {
			if (nextSymbol() == UNDEF_SYM) {//???
				nextChar();//???
			}
		}
		return skipTo.indexOf(_sym) >= 0;
	}

	/** Return name of symbol (used for error messages).
	 * @param sym The symbol.
	 * @return name of symbol.
	 */
	public static final String symToName(final char sym) {
		String result = BASESYMBOLTABLE.get(sym);
		if (result != null) {
			return result;
		}
		// find keyword ID
		int i = KEYWORDS.indexOf(";" + sym + ";");
		if (i >= 0) {
			i += 3;
			return KEYWORDS.substring(i, KEYWORDS.indexOf(';', i));//get keyword
		}
		return String.valueOf(sym);
	}

	/** Put error message that the actual symbol is not allowed here.
	 * @param sym The symbol id.
	 */
	public final void errToken(final char sym) {
		//The token '&{0}' is not allowed here
		error(XDEF.XDEF411, symToName(sym));
	}

	/** Put error message the actual symbol is not allowed here.
	 * @param sym The symbol id.
	 * @param symbols string with symbols to find.
	 */
	public final void errToken(final char sym, String symbols) {
		//The token '&{0}' is not allowed here
		error(XDEF.XDEF411, symToName(sym));
		while (_sym != NOCHAR && symbols.indexOf(_sym) < 0) {
			nextSymbol();
		}
	}

	@Override
	public void warning(final String id,
		final String msg,
		final Object... mod) {
		putReportOnLastPos(Report.warning(id, msg, mod));
	}

	@Override
	public void lightError(final String id,
		final String msg,
		final Object... mod) {
		putReportOnLastPos(Report.lightError(id, msg, mod));
	}

	@Override
	public void error(final String id,
		final String msg,
		final Object... mod) {
		putReportOnLastPos(Report.error(id, msg, mod));
	}

	@Override
	/** Put warning message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public void warning(final long registeredID, final Object... mod){
		putReportOnLastPos(Report.warning(registeredID, mod));
	}

	@Override
	/** Put light error message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public void lightError(final long registeredID, final Object... mod){
		putReportOnLastPos(Report.lightError(registeredID, mod));
	}

	@Override
	/** Put error message with modification parameters.
	 * @param registeredID registered message ID.
	 * @param mod Modification string.
	 */
	public void error(final long registeredID, final Object... mod) {
		putReportOnLastPos(Report.error(registeredID, mod));
	}

	/** Put report with saved last position.
	 * @param typ type of message.
	 * @param id Message id.
	 * @param msg Message text.
	 * @param modif Modification string.
	 */
	private void putReportOnLastPos(final Report report) {
		putReport(getLastPosition(), report);
	}

	/** Put error message.
	 * @param pos SPosition of source.
	 * @param id Message id.
	 * @param msg Message text.
	 * @param mod Message modification parameters.
	 */
	public final void error(final SPosition pos,
		final String id,
		final String msg,
		final Object... mod) {
		putReport(pos, Report.error(id, msg, mod));
	}

	/** Put error message.
	 * @param pos SPosition of source.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public final void error(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.error(registeredID, mod));
	}

	/** Put warning message.
	 * @param pos SPosition of source.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final SPosition pos,
		final long registeredID,
		final Object... mod) {
		putReport(pos, Report.warning(registeredID, mod));
	}

	@Override
	/** Put report at position.
	 * @param pos Source position.
	 * @param report The report.
	 */
	public void putReport(final SPosition pos, final Report report) {
		if (_xpath != null && !_xpath.isEmpty()) {
			String s = report.getModification();
			if (s == null || (s=s.trim()).isEmpty()) {
				s = "";
			}
			s += "&{xpath}" + _xpath;
			report.setModification(s);
		}
		pos.putReport(report, getReportWriter());
	}
}